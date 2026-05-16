<!--관리정보/기본정보/코드정보관리 [ERP 프리미엄 고밀도 표준 - 간격 및 밸런스 조정] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="haaa010u-wrapper d-flex flex-column h-100 bg-light p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-2 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-tags-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">코드정보 관리 (HAAA010U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchGroups">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 (섹션 간 확실한 간격 확보: gap-3) -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-3 p-3">

			<!-- 🔍 2. 상단 상세 입력 바 (한 줄 고밀도 배치) -->
			<div class="card border-0 shadow-sm flex-shrink-0">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-pencil-square me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">코드 상세 정보 등록</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">코드유형</th>
								<td>
									<select v-model="formData.CDTYPE" class="form-select">
										<option v-for="grp in groupOptions" :key="grp.CODECD" :value="grp.CODECD">{{ grp.CODENM }}</option>
									</select>
								</td>
								<th class="required">코    드</th>
								<td><input v-model="formData.CODECD" type="text" class="form-control fw-bold text-primary text-center" maxlength="3" /></td>
								<th class="required">코드명칭</th>
								<td><input v-model="formData.CODENM" type="text" class="form-control" placeholder="코드 명칭 입력" /></td>
							</tr>
							<tr>
								<th>비    고</th>
								<td colspan="3"><input v-model="formData.REMARK" type="text" class="form-control" /></td>
								<th>순서/사용</th>
								<td>
									<div class="d-flex align-items-center gap-3 h-100 px-2">
										<input v-model="formData.DSPORD" type="number" class="form-control text-end" style="width: 70px;" />
										<div class="form-check form-switch m-0">
											<input v-model="formData.USEYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useYn010">
											<label class="form-check-label ms-1 small fw-bold" for="useYn010">사용</label>
										</div>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 3. 하단 분할 그리드 영역 (좌: 그룹 리스트 / 우: 상세 리스트) -->
			<div class="d-flex flex-row gap-3 flex-grow-1 overflow-hidden">
				<!-- 좌측: 코드 그룹 -->
				<div class="card border-0 shadow-sm d-flex flex-column" style="width: 380px;">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
						<i class="bi bi-list-stars me-2 text-secondary"></i>
						<span class="fw-bold small text-dark">코드 그룹 리스트</span>
					</div>
					<div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
						<div ref="groupGridRef" class="tabulator-full-height"></div>
					</div>
				</div>
				<!-- 우측: 상세 코드 -->
				<div class="card border-0 shadow-sm d-flex flex-column flex-grow-1">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
						<i class="bi bi-check2-square me-2 text-secondary"></i>
						<span class="fw-bold small text-dark">상세 코드 리스트</span>
					</div>
					<div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
						<div ref="listGridRef" class="tabulator-full-height"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useAlerts } from '@/composables/useAlerts'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const groupOptions = ref<any[]>([])
const formData = reactive({ ACTKIND: 'I1', CDTYPE: '000', CODECD: '', CODENM: '', DSPORD: '1', REMARK: '', USEYN: 'Y' })

const groupGridRef = ref<HTMLElement | null>(null); const listGridRef = ref<HTMLElement | null>(null)
let groupGrid: Tabulator | null = null; let listGrid: Tabulator | null = null

async function fetchGroups() {
	try {
		const res = await api.post('/api/haaa/HAAA_010U_STR', { ACTKIND: 'S2', CDTYPE: '000', CMPYCD: authStore.CMPYCD })
		if (res.data) {
			groupOptions.value = res.data; groupGrid?.setData(res.data)
			if (res.data.length > 0 && !formData.CDTYPE) formData.CDTYPE = res.data[0].CODECD
		}
	} catch (e) { console.error('그룹 로드 실패') }
}

async function fetchDetails(cdType: string) {
	try {
		const res = await api.post('/api/haaa/HAAA_010U_STR', { ACTKIND: 'S3', CDTYPE: cdType, CMPYCD: authStore.CMPYCD })
		listGrid?.setData(res.data || [])
	} catch (e) { console.error('상세 목록 로드 실패') }
}

async function save() {
	if (!formData.CODECD || !formData.CODENM) return vAlertError('필수항목을 입력하세요.')
	try {
		await api.post('/api/haaa/HAAA_010U_STR', { ...formData, USERID: authStore.USER_ID, CMPYCD: authStore.CMPYCD })
		vAlert('저장되었습니다.')
		fetchDetails(formData.CDTYPE)
	} catch (e) { vAlertError('저장 실패') }
}

function initialize() {
	resetForm(formData); formData.CDTYPE = '000'; formData.ACTKIND = 'I1'; formData.DSPORD = '1'; formData.USEYN = 'Y'
	listGrid?.clearData()
}

onMounted(async () => {
	if (groupGridRef.value) {
		groupGrid = new Tabulator(groupGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columnDefaults: { headerSort: false, headerHozAlign: 'center' },
			columns: [{ title: '유형코드', field: 'CODECD', hozAlign: 'center', width: 80 }, { title: '그룹명', field: 'CODENM', widthGrow: 1, cssClass: 'fw-bold text-primary' }]
		})
		groupGrid.on('rowClick', (e, row) => { const d = row.getData(); formData.CDTYPE = d.CODECD; fetchDetails(d.CODECD) })
	}
	if (listGridRef.value) {
		listGrid = new Tabulator(listGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center' },
			columns: [
				{ title: '코드', field: 'CODECD', hozAlign: 'center', width: 100, cssClass: 'fw-bold text-primary border-end' },
				{ title: '명칭', field: 'CODENM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '순서', field: 'DSPORD', hozAlign: 'center', width: 80 },
				{ title: '사용', field: 'USEYN', hozAlign: 'center', width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		listGrid.on('rowClick', (e, row) => { Object.assign(formData, row.getData()); formData.ACTKIND = 'U1' })
	}
	await fetchGroups()
})
</script>

<style scoped>
.haaa010u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }
.btn-group-erp { display: flex; gap: 6px; }
.btn-erp { padding: 4px 18px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff; color: #6c757d; border: 1px solid #6c757d; }
.btn-init:hover { background-color: #f8f9fa; }
.btn-search { background-color: #2d3748; color: #fff; border: none; }
.btn-save { background-color: #005a9f; color: #fff; border: none; }
.erp-table-full { table-layout: fixed; border-collapse: collapse !important; width: 100%; border-style: hidden; }
.erp-table-full th { background-color: #f8fafc; border: 1px solid #e2e8f0 !important; text-align: center; font-weight: 800; font-size: 12px; color: #475569; vertical-align: middle; padding: 10px 15px !important; white-space: nowrap; }
.erp-table-full td { vertical-align: middle; padding: 8px 12px !important; border: 1px solid #e2e8f0 !important; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
.form-control, .form-select { border-radius: 4px; border: 1px solid #cbd5e1; font-size: 13px; height: 32px; width: 100%; font-weight: 500; }
:deep(.tabulator) { border: none; font-size: 12.5px; background-color: transparent !important; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 800; color: #334155; }
:deep(.tabulator-row.tabulator-selected) { background-color: #e2e8f0 !important; border-left: 4px solid #005a9f !important; }
</style>
