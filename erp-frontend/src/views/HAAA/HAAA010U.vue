<!--
	=============================================================
	프로그램명	: 코드정보 관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 시스템 공통 코드 그룹 및 세부 코드 정보 관리 (함수명 표준화 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 (표준 규격 및 함수명 통일) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-tags-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">코드정보 관리 (HAAA010U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
				<button class="btn-erp btn-delete" @click="deleteData" :disabled="formData.actkind !== 'U1'">삭제</button>
				<button class="btn-erp btn-excel" @click="excel">엑셀</button>
				<button class="btn-erp btn-print" @click="print">인쇄</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-2 p-2">

			<!-- 🔍 2. 상단 상세 입력 영역 (표준 erp-table-full 및 80px 레이블 규격) -->
			<div class="card border-0 shadow-sm flex-shrink-0 overflow-hidden" style="border-radius: 8px;">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-pencil-square me-2 text-primary"></i>
					<span class="fw-bold small text-dark">코드 상세 정보 등록/수정</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">코드유형</th>
								<td>
									<select v-model="formData.cdtype" class="form-select" @change="searchDetails(formData.cdtype)">
										<option v-for="grp in groupOptions" :key="grp.codecd" :value="grp.codecd">{{ grp.codenm }}</option>
									</select>
								</td>
								<th class="required">코 드</th>
								<td><input v-model="formData.codecd" type="text" class="form-control fw-bold text-primary text-center" maxlength="3" :readonly="formData.actkind === 'U1'" /></td>
								<th class="required">코드명칭</th>
								<td><input v-model="formData.codenm" type="text" class="form-control" placeholder="코드 명칭 입력" /></td>
							</tr>
							<tr>
								<th>비 고</th>
								<td colspan="3"><input v-model="formData.remark" type="text" class="form-control" /></td>
								<th>순서/사용</th>
								<td>
									<div class="d-flex align-items-center gap-2 px-2">
										<input v-model="formData.dspord" type="number" class="form-control text-end" style="width: 60px;" />
										<div class="form-check form-switch m-0 ms-2">
											<input v-model="formData.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useYn010">
											<label class="form-check-label ms-1 small fw-bold" for="useYn010">사용</label>
										</div>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 3. 하단 분할 그리드 영역 (gap-2) -->
			<div class="d-flex flex-row gap-2 flex-grow-1 overflow-hidden">
				<!-- 좌측: 코드 그룹 -->
				<div class="card border-0 shadow-sm d-flex flex-column" style="width: 350px; border-radius: 8px;">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
						<i class="bi bi-list-stars me-2 text-primary"></i>
						<span class="fw-bold small text-dark">코드 그룹 리스트</span>
					</div>
                  <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                    <div ref="groupGridRef" class="tabulator-instance flex-grow-1"></div>
                  </div>
				</div>
				<!-- 우측: 상세 코드 -->
				<div class="card border-0 shadow-sm d-flex flex-column flex-grow-1" style="border-radius: 8px;">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
						<div>
							<i class="bi bi-check2-square me-2 text-primary"></i>
							<span class="fw-bold small text-dark">상세 코드 리스트</span>
						</div>
						<span class="text-muted small" style="font-size: 11px;">항목 클릭 시 수정 모드로 전환됩니다.</span>
					</div>
                    <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                      <div ref="listGridRef" class="tabulator-instance flex-grow-1"></div>
                    </div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useAlerts } from '@/composables/useAlerts'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const groupOptions = ref<any[]>([])
const formData = reactive({ actkind: 'I1', cdtype: '000', codecd: '', codenm: '', dspord: '1', remark: '', useyn: 'Y' })

const groupGridRef = ref<HTMLElement | null>(null); const listGridRef = ref<HTMLElement | null>(null)
let groupGrid: Tabulator | null = null; let listGrid: Tabulator | null = null

async function search() {
	try {
		const res = await api.post('/api/haaa/HAAA_010U_STR', { actkind: 'S2', cdtype: '000', cmpycd: authStore.cmpycd })
		if (res.data) {
			groupOptions.value = res.data; groupGrid?.setData(res.data)
			if (res.data.length > 0 && !formData.cdtype) {
				formData.cdtype = res.data[0].codecd
				searchDetails(formData.cdtype)
			}
		}
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('그룹 로드 실패') }
}

async function searchDetails(cdType: string) {
	try {
		const res = await api.post('/api/haaa/HAAA_010U_STR', { actkind: 'S3', cdtype: cdType, cmpycd: authStore.cmpycd })
		listGrid?.setData(res.data || [])
	} catch (e) { console.error('상세 목록 로드 실패') }
}

async function save() {
	if (!formData.codecd || !formData.codenm) return vAlertError('필수항목을 입력하세요.')
	try {
		await api.post('/api/haaa/HAAA_010U_STR', { ...formData, userid: authStore.userid, cmpycd: authStore.cmpycd })
		vAlert('저장되었습니다.')
		searchDetails(formData.cdtype)
		resetInputForm()
	} catch (e) { vAlertError('저장 실패') }
}

async function deleteData() {
	if (!formData.codecd) return vAlertError('삭제할 코드를 선택하세요.')
	if (!confirm('선택한 코드 정보를 삭제하시겠습니까?')) return
	try {
		await api.post('/api/haaa/HAAA_010U_STR', { ...formData, actkind: 'D1', userid: authStore.userid, cmpycd: authStore.cmpycd })
		vAlert('삭제되었습니다.')
		searchDetails(formData.cdtype)
		resetInputForm()
	} catch (e) { vAlertError('삭제 실패') }
}

function resetInputForm() {
	const currentType = formData.cdtype
	resetForm(formData)
	formData.cdtype = currentType; formData.actkind = 'I1'; formData.dspord = '1'; formData.useyn = 'Y'
}

function initialize() {
	resetForm(formData); formData.cdtype = '000'; formData.actkind = 'I1'; formData.dspord = '1'; formData.useyn = 'Y'
	listGrid?.clearData()
	search()
}

function excel() {
	listGrid?.download("xlsx", `코드정보_${formData.cdtype}_${new Date().toISOString().slice(0, 10)}.xlsx`)
}

function print() {
	window.open(`/api/haaa/HAAA_010P?cdtype=${formData.cdtype}&cmpycd=${authStore.cmpycd}`)
}

onMounted(async () => {
	if (groupGridRef.value) {
		groupGrid = new Tabulator(groupGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: "middle" },
			columns: [
				{ title: '유형코드', field: 'codecd', hozAlign: 'center', width: 80 },
				{ title: '그룹명', field: 'codenm', widthGrow: 1, cssClass: 'fw-bold text-primary' }
			]
		})
		groupGrid.on('rowClick', (e, row) => {
			const d = row.getData()
			formData.cdtype = d.codecd
			searchDetails(d.codecd)
			resetInputForm()
		})
	}
	if (listGridRef.value) {
		listGrid = new Tabulator(listGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: "middle" },
			columns: [
				{ title: '코드', field: 'codecd', hozAlign: 'center', width: 80, cssClass: 'fw-bold text-primary' },
				{ title: '명칭', field: 'codenm', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '순서', field: 'dspord', hozAlign: 'center', width: 70 },
				{ title: '사용', field: 'useyn', hozAlign: 'center', width: 70, formatter: "tickCross" }
			]
		})
		listGrid.on('rowClick', (e, row) => {
			Object.assign(formData, row.getData())
			formData.actkind = 'U1'
		})
	}
	await search()
})
</script>
