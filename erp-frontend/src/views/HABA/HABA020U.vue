<!--	=============================================================
	프로그램명	: 기초코드관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 시스템 전반에서 사용하는 공통 기초 코드 그룹 및 상세 코드 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 (표준 규격) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-collection-fill me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">기초코드 관리 (HABA020U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-2 p-2">

			<!-- 🔍 2. 상단 상세 입력 영역 (표준 erp-table-full) -->
			<div class="card border-0 shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-pencil-square me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">코드 정보 등록/수정</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">코드그룹</th>
								<td>
									<select v-model="formData.grpcd" class="form-select" @change="fetchDetails(formData.grpcd)">
										<option v-for="grp in groupOptions" :key="grp.code" :value="grp.code">[{{ grp.code }}] {{ grp.name }}</option>
									</select>
								</td>
								<th class="required">코 드</th>
								<td><input v-model="formData.code" type="text" class="form-control fw-bold text-primary text-center" maxlength="10" :readonly="formData.actkind === 'U0'" /></td>
								<th class="required">코드명칭</th>
								<td><input v-model="formData.name" type="text" class="form-control" placeholder="코드 명칭 입력" /></td>
							</tr>
							<tr>
								<th>참조값 1</th>
								<td><input v-model="formData.REF1" type="text" class="form-control" /></td>
								<th>참조값 2</th>
								<td><input v-model="formData.REF2" type="text" class="form-control" /></td>
								<th>순서/사용</th>
								<td>
									<div class="d-flex align-items-center gap-2 px-2">
										<input v-model="formData.sort" type="number" class="form-control text-end" style="width: 60px;" />
										<div class="form-check form-switch m-0 ms-2">
											<input v-model="formData.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useYn020">
											<label class="form-check-label ms-1 small fw-bold" for="useYn020">사용</label>
										</div>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 3. 하단 분할 그리드 영역 -->
			<div class="d-flex flex-row gap-2 flex-grow-1 overflow-hidden">
				<!-- 좌측: 코드 그룹 리스트 -->
				<div class="card border-0 shadow-sm d-flex flex-column" style="width: 350px;">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
						<i class="bi bi-list-ul me-2 text-secondary"></i>
						<span class="fw-bold small text-dark">코드 그룹</span>
					</div>
                    <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                      <div ref="groupGridRef" class="tabulator-instance flex-grow-1"></div>
                    </div>
				</div>
				<!-- 우측: 상세 코드 리스트 -->
				<div class="card border-0 shadow-sm d-flex flex-column flex-grow-1">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap me-2 text-secondary"></i>
						<span class="fw-bold small text-dark">세부 코드 리스트</span>
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

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const groupOptions = ref<any[]>([])
const formData = reactive({ actkind: 'A0', grpcd: '', code: '', name: '', REF1: '', REF2: '', sort: 1, useyn: 'Y' })

const groupGridRef = ref<HTMLElement | null>(null); const listGridRef = ref<HTMLElement | null>(null)
let groupGrid: Tabulator | null = null; let listGrid: Tabulator | null = null

async function search() {
	try {
		const res = await api.post('/api/haba/HABA_020U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd })
		groupOptions.value = res.data; groupGrid?.setData(res.data)
		if (res.data.length > 0 && !formData.grpcd) {
            formData.grpcd = res.data[0].code
            fetchDetails(formData.grpcd)
        }
	} catch (e) { console.error('그룹 로드 실패') }
}

async function fetchDetails(grpCd: string) {
	try {
		const res = await api.post('/api/haba/HABA_020U_STR', { actkind: 'S1', grpcd: grpCd, cmpycd: authStore.cmpycd })
		listGrid?.setData(res.data || [])
	} catch (e) { console.error('상세 목록 로드 실패') }
}

async function save() {
	if (!formData.code || !formData.name) return vAlertError('코드와 명칭은 필수 입력 항목입니다.')
	try {
		await api.post('/api/haba/HABA_020U_STR', { ...formData, userid: authStore.userid, cmpycd: authStore.cmpycd })
		vAlert('정상적으로 저장되었습니다.')
		fetchDetails(formData.grpcd)
		initialize()
	} catch (e) { vAlertError('저장 중 오류가 발생했습니다.') }
}

function initialize() {
    const currentGrp = formData.grpcd
	resetForm(formData)
    formData.grpcd = currentGrp; formData.actkind = 'A0'; formData.sort = 1; formData.useyn = 'Y'
}

onMounted(async () => {
	if (groupGridRef.value) {
		groupGrid = new Tabulator(groupGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columns: [
				{ title: '그룹코드', field: 'code', width: 100, hozAlign: 'center' },
				{ title: '그룹명칭', field: 'name', widthGrow: 1, cssClass: 'fw-bold text-primary' }
			]
		})
		groupGrid.on('rowClick', (e, row) => { const d = row.getData(); formData.grpcd = d.code; fetchDetails(d.code) })
	}
	if (listGridRef.value) {
		listGrid = new Tabulator(listGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columns: [
				{ title: '코드', field: 'code', width: 100, hozAlign: 'center', cssClass: 'fw-bold' },
				{ title: '코드명칭', field: 'name', minWidth: 150, widthGrow: 1 },
				{ title: '순서', field: 'sort', width: 60, hozAlign: 'center' },
				{ title: '사용', field: 'useyn', width: 60, hozAlign: 'center', formatter: 'tickCross' }
			]
		})
		listGrid.on('rowClick', (e, row) => { Object.assign(formData, row.getData()); formData.actkind = 'U0' })
	}
	await search()
})
</script>
