<!--수입관리/수입단가추이현황-->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip210s-wrapper d-flex flex-column h-100 bg-white p-0 overflow-auto">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-1 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 13px;">
				<i class="bi bi-graph-up-arrow me-2 text-primary"></i>
				수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입단가추이현황</span>
			</div>
			<div class="btn-group-erp pe-2">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchTrendList">조회</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 -->
		<table class="erp-table-full border-bottom" style="table-layout: fixed;">
			<colgroup>
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
			</colgroup>
			<tbody>
				<tr>
					<th class="required border-end text-nowrap">수입부서</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.DEPTCD" type="text" class="form-control bg-light" style="max-width: 60px;" readonly />
							<input v-model="searchForm.DEPTNM" type="text" class="form-control" />
							<button class="btn btn-outline-secondary px-2" @click="openDeptPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="required border-end text-nowrap">수입연도</th>
					<td>
						<select v-model="searchForm.YY" class="form-select form-select-sm ms-1" style="width: 120px;">
							<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
						</select>
					</td>
					<td colspan="4" class="bg-light text-center small text-muted">
						※ 수입 연도별 월별 단가 및 금액 추이를 분석합니다.
					</td>
				</tr>
			</tbody>
		</table>

		<!-- 📊 3. 추이 분석 그리드 -->
		<div class="flex-grow-1 overflow-hidden bg-white d-flex flex-column">
			<div class="grid-title py-1 px-3 border-bottom fw-bold small text-primary d-flex justify-content-between align-items-center bg-light">
				<span><i class="bi bi-table me-1"></i> 월별 품목 수입 추이 데이터</span>
				<span class="badge bg-primary-subtle text-primary border border-primary-subtle px-2">단위: 원화(KRW)</span>
			</div>
			<div ref="mainGridRef" class="tabulator-full-height border-top" />
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const currentYear = new Date().getFullYear()
const searchForm = reactive({
	DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
	YY: currentYear.toString()
})

const yearOptions = ref<string[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const initYearOptions = () => {
	const years = []
	for (let i = 0; i < 6; i++) { years.push((currentYear - i).toString()) }
	yearOptions.value = years
}

async function fetchTrendList() {
	try {
		// 🛠️ [경로 수정] /api/hsip/list -> /api/hsip/HSIP_210S_STR (POST)
		const res = await api.post('/api/hsip/HSIP_210S_STR', {
			...searchForm,
			CMPYCD: authStore.CMPYCD
		})
		mainGrid?.setData(res.data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const openDeptPopup = () => { vAlert('부서 팝업 준비 중') }

function initialize() {
	resetForm(searchForm)
	searchForm.YY = currentYear.toString()
	mainGrid?.clearData()
}

onMounted(async () => {
	initYearOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', minWidth: 70 },
			columns: [
				{ title: '품목 명칭', field: 'ITEMNM', minWidth: 250, widthGrow: 1, cssClass: 'fw-bold text-dark border-end' },
				{ title: '합 계', field: 'AMTSUM', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'bg-info-subtle fw-bold' },
				{ title: '1월', field: 'AMT01', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '2월', field: 'AMT02', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '3월', field: 'AMT03', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '4월', field: 'AMT04', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '5월', field: 'AMT05', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '6월', field: 'AMT06', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '7월', field: 'AMT07', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '8월', field: 'AMT08', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '9월', field: 'AMT09', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '10월', field: 'AMT10', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '11월', field: 'AMT11', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '12월', field: 'AMT12', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } }
			]
		})
	}
	fetchTrendList()
})
</script>

<style scoped>
.hsip210s-wrapper { height: 100%; overflow: hidden; padding: 0 !important; }
.tabulator-full-height { height: 100% !important; border: none; }
.btn-erp { padding: 4px 15px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; border: 1px solid #ced4da; transition: all 0.2s; }
.btn-init { background-color: #fff; color: #6c757d; }
.btn-search { background-color: #2d3748; color: #fff; border: none; }
.erp-table-full { table-layout: fixed; border-collapse: collapse !important; width: 100%; border-bottom: 1px solid #dee2e6 !important; }
.erp-table-full th { background-color: #f1f5f9; border-right: 1px solid #dee2e6 !important; text-align: center; font-weight: 800; font-size: 12.5px; color: #334155; vertical-align: middle; padding: 8px 10px !important; white-space: nowrap !important; }
.erp-table-full td { border: 1px solid #dee2e6 !important; padding: 4px 8px !important; vertical-align: middle; white-space: nowrap !important; font-size: 12.5px; overflow: hidden; }
.grid-title { background-color: #f8fafc; font-size: 12px; }
:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; }
</style>
