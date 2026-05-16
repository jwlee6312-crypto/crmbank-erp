<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsba750u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-gear-wide-connected me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입비용코드관리</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
			<!-- 🅰️ 마스터 입력 폼 -->
			<div class="card border shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-body p-0">
					<table class="erp-table-form">
						<colgroup>
							<col style="width: 100px;"><col style="width: 15%">
							<col style="width: 100px;"><col style="width: 20%">
							<col style="width: 100px;"><col style="width: 20%">
							<col style="width: 100px;"><col style="width: 20%">
						</colgroup>
						<tbody>
							<tr>
								<th class="required">비용코드</th>
								<td><input v-model="formData.COSTCD" class="form-control form-control-sm text-center fw-bold" maxlength="3" :readonly="formData.mode === 'U'" /></td>
								<th class="required">코 드 명</th>
								<td><input v-model="formData.COSTNM" class="form-control form-control-sm" maxlength="30" /></td>
								<th class="required">비용구분</th>
								<td>
									<select v-model="formData.COSTGBN" class="form-select form-select-sm fw-bold">
										<option value="">-- 선택 --</option>
										<option v-for="opt in costGbnOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
									</select>
								</td>
								<th class="required">배부기준</th>
								<td>
									<select v-model="formData.DIVCD" class="form-select form-select-sm fw-bold">
										<option value="">-- 선택 --</option>
										<option v-for="opt in divideOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
									</select>
								</td>
							</tr>
							<tr>
								<th>비&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;고</th>
								<td colspan="5"><input v-model="formData.BIGO" class="form-control form-control-sm" maxlength="50" /></td>
								<th>사용여부</th>
								<td>
									<div class="form-check form-switch mt-1">
										<input v-model="formData.USEYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useynSwitch">
										<label class="form-check-label small" for="useynSwitch">{{ formData.USEYN === 'Y' ? '사용' : '미사용' }}</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 🅱️ 데이터 그리드 영역 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center" style="height: 40px; min-height: 40px;">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 수입비용코드 목록
					</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="gridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const formData = reactive<any>({
	mode: 'N',
	COSTCD: '',
	COSTNM: '',
	COSTGBN: '',
	DIVCD: '',
	BIGO: '',
	USEYN: 'Y'
})

const costGbnOptions = ref<any[]>([])
const divideOptions = ref<any[]>([])
const gridRef = ref<HTMLDivElement | null>(null)
let grid: Tabulator | null = null

// 콤보박스 데이터 로드
async function fetchComboOptions() {
	try {
		// 비용구분 (315)
		const resGbn = await api.get('/api/hs00/HS00_000S_STR', {
			params: { GUBUN: 'E2', CMPYCD: authStore.CMPYCD, GBNCD: '315', CODE: '', CODENM: '', ETCVAL: '' }
		})
		costGbnOptions.value = resGbn.data

		// 배부기준 (316)
		const resDiv = await api.get('/api/hs00/HS00_000S_STR', {
			params: { GUBUN: 'E2', CMPYCD: authStore.CMPYCD, GBNCD: '316', CODE: '', CODENM: '', ETCVAL: '' }
		})
		divideOptions.value = resDiv.data
	} catch (e) {
		console.error('콤보박스 로드 실패', e)
	}
}

async function fetchList() {
	try {
		const res = await api.post('/api/hsba/HSBA_750U_STR', {
			ACTKIND: 'S0',
			CMPYCD: authStore.CMPYCD
		})
		grid?.setData(res.data)
	} catch (e) { vAlertError('조회 실패') }
}

async function handleSave() {
	if (!formData.COSTCD || !formData.COSTNM || !formData.COSTGBN || !formData.DIVCD) {
		return vAlertError('필수 입력 항목을 확인하세요.')
	}
	if (!confirm('저장하시겠습니까?')) return

	try {
		const actkind = formData.mode === 'U' ? 'U0' : 'A0'
		await api.post('/api/hsba/HSBA_750U_STR', {
			...formData,
			ACTKIND: actkind,
			CMPYCD: authStore.CMPYCD,
			USERID: authStore.USERID
		})
		vAlert('저장되었습니다.')
		initialize()
		fetchList()
	} catch (e) { vAlertError('저장 실패') }
}

function initialize() {
	resetForm(formData)
	Object.assign(formData, { mode: 'N', USEYN: 'Y', COSTGBN: '', DIVCD: '', COSTCD: '', COSTNM: '', BIGO: '' })
	grid?.deselectRow()
}

onMounted(async () => {
	await fetchComboOptions()

	if (gridRef.value) {
		grid = new Tabulator(gridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			selectable: 1,
			columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 80 },
			columns: [
				{ title: '비용코드', field: 'COSTCD', width: 120, hozAlign: 'center', cssClass: 'fw-bold' },
				{ title: '코드명', field: 'COSTNM', minWidth: 200, widthGrow: 1.5 },
				{ title: '구분코드', field: 'COSTGBN', width: 100, hozAlign: 'center' },
				{ title: '비용구분명', field: 'COSTGBNM', width: 150 },
				{ title: '기준코드', field: 'DIVCD', width: 100, hozAlign: 'center' },
				{ title: '배부기준명', field: 'DIVNM', width: 150 },
				{ title: '사용', field: 'USEYN', width: 100, hozAlign: 'center', formatter: (c) => c.getValue() === 'Y' ? '사용' : '미사용' }
			]
		})

		grid.on('rowClick', (e, row) => {
			const data = row.getData()
			Object.assign(formData, data)
			formData.mode = 'U'
		})
	}
	fetchList()
})
</script>

<style scoped>
.hsba750u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #ffffff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #ffffff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #ffffff !important; border: none !important; }

.erp-table-form { width: 100%; border-collapse: collapse; border: 1px solid #dee2e6; }
.erp-table-form th {
    background-color: #f8f9fa;
    border: 1px solid #dee2e6;
    text-align: center;
    font-weight: 700;
    font-size: 12px;
    padding: 8px 12px !important;
    color: #495057;
}
.erp-table-form td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; font-size: 12.5px; }
.required::after { content: ' *'; color: #dc3545; }

:deep(.tabulator) { border: none; font-size: 12.5px; background-color: transparent !important; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 800; color: #334155; }
:deep(.tabulator-row.tabulator-selected) { background-color: #f0f7ff !important; border-left: 4px solid #005a9f !important; }
</style>
