<!--	=============================================================
	?�로그램�?: ?�출?�적명세??
	?�성?�자	: 2025.02.24
	?�성??    : AI Assistant
	?�명        : ?�업?�별 ?�출 ?�적(?�출?�화, 기�??�세?? ?�세 ?�황 조회 (HSOD100U ?��? UI ?�용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-airplane-engines me-2 text-primary" style="font-size: 18px;"></i>
				?�무관�?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">?�출?�적명세??(HATX600S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					초기??
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> ?�쇄
				</button>
			</div>
		</div>

		<!-- ?�� 검??조건 ?�역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-0">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 80px;" /><col style="width: 150px;" />
							<col style="width: 80px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="bg-light text-center">??????/th>
								<td>
									<select v-model="searchForm.taxunit" class="form-select form-select-sm">
										<option value="000">?�체</option>
										<option v-for="opt in taxUnitOptions" :key="opt.code" :value="opt.code">{{ opt.codenm }}</option>
									</select>
								</td>
								<th class="bg-light text-center">�?   �?/th>
								<td>
									<div class="d-flex align-items-center gap-1 px-1">
										<select v-model="searchForm.YY" class="form-select form-select-sm" style="width: 100px;">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}??/option>
										</select>
										<select v-model="searchForm.FMM" class="form-select form-select-sm" style="width: 70px;">
											<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}??/option>
										</select>
										<span>~</span>
										<select v-model="searchForm.TMM" class="form-select form-select-sm" style="width: 70px;">
											<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}??/option>
										</select>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- ?�� 메인 컨텐�??�역 (?�하 분할) -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- ?���??�출?�적 ?�계 그리??-->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1 text-primary"></i> ?�출?�적 ?�계</span>
				</div>
				<div class="card-body p-0 overflow-hidden">
					<div ref="summaryGridRef" class="tabulator-instance"></div>
				</div>
			</div>

			<!-- ?���??�출?�적 ?�세 목록 그리??-->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1 text-primary"></i> ?�출?�적 ?�세 ?�역</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
					<div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
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
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = String(now.getMonth() + 1).padStart(2, '0')

const taxUnitOptions = ref<any[]>([])
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({
	taxunit: '000',
	YY: String(currentYear),
	FMM: currentMonth,
	TMM: currentMonth
})

const summaryGridRef = ref<HTMLDivElement | null>(null)
const mainGridRef = ref<HTMLDivElement | null>(null)
let summaryGrid: Tabulator | null = null
let mainGrid: Tabulator | null = null

const fetchOptions = async () => {
	try {
		const res = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd, search: ' ' })
		taxUnitOptions.value = res.data || []
		if (taxUnitOptions.value.length > 0) searchForm.taxunit = taxUnitOptions.value[0].code
	} catch (e) { console.error(e) }
}

const search = async () => {
	try {
		const ymfr = searchForm.YY + searchForm.FMM
		const ymto = searchForm.YY + searchForm.TMM

		// 1. ?�계 ?�보 조회 (actkind: S0)
		const resSum = await api.post('/api/hatx/HATX_600S_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			taxunit: searchForm.taxunit,
			ymfr: ymfr,
			ymto: ymto
		})

		const rawSum = resSum.data?.[0] || {}
		const sumData = [
			{ category: '?�계', count: rawSum.col0, frgn_amt: rawSum.col1, won_amt: rawSum.col2, remark: '' },
			{ category: '?�출?�화', count: rawSum.col3, frgn_amt: rawSum.col4, won_amt: rawSum.col5, remark: '' },
			{ category: '기�??�세?�적??, count: rawSum.col6, frgn_amt: rawSum.col7, won_amt: rawSum.col8, remark: '' }
		]
		summaryGrid?.setData(sumData)

		// 2. ?�세 목록 조회 (actkind: S1)
		const resList = await api.post('/api/hatx/HATX_600S_STR', {
			actkind: 'S1',
			cmpycd: authStore.cmpycd,
			taxunit: searchForm.taxunit,
			ymfr: ymfr,
			ymto: ymto
		})

		const listData = (resList.data || []).map((row: any) => ({
			mgtno: row.MGTNO,
			pubymd: row.pubymd, // 8 digits
			currcd: row.currcd,
			frgnrate: Number(row.FRGNRATE || 0),
			frgnamt: Number(row.frgnamt || 0),
			supyamt: Number(row.supyamt || 0)
		}))
		mainGrid?.setData(listData)

		vAlert('조회?�었?�니??')
	} catch (e) { vAlertError('조회 �??�류 발생') }
}

const print = () => {
	const params = new URLSearchParams({
		taxunit: searchForm.taxunit,
		YY: searchForm.YY,
		FMM: searchForm.FMM,
		TMM: searchForm.TMM,
		PRTGU: '1'
	}).toString()
	window.open(`/api/hatx/HATX_600P?${params}`, 'ExportPerformancePrint', 'width=1000,height=800,scrollbars=yes')
}

const initialize = () => {
	searchForm.taxunit = taxUnitOptions.value.length > 0 ? taxUnitOptions.value[0].code : '000'
	searchForm.YY = String(currentYear)
	searchForm.FMM = currentMonth
	searchForm.TMM = currentMonth
	summaryGrid?.clearData()
	mainGrid?.clearData()
}

onMounted(() => {
	fetchOptions()

	if (summaryGridRef.value) {
		summaryGrid = new Tabulator(summaryGridRef.value, {
			layout: 'fitColumns',
			height: 'auto',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "구분", field: "category", widthGrow: 1, hozAlign: "center", cssClass: "bg-light fw-bold" },
				{ title: "건수", field: "count", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "?�화금액", field: "frgn_amt", width: 200, hozAlign: "right", formatter: "money", formatterParams: { precision: 2 } },
				{ title: "?�화금액", field: "won_amt", width: 200, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" },
				{ title: "비고", field: "remark", widthGrow: 1 }
			]
		})
	}

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "?�련번호", formatter: "rownum", width: 80, hozAlign: "center" },
				{ title: "?�출?�고번호", field: "mgtno", width: 180, hozAlign: "center", cssClass: "fw-bold" },
				{
					title: "?�적?�자", field: "pubymd", width: 120, hozAlign: "center",
					formatter: (cell) => { const v = cell.getValue(); return v && v.length === 8 ? `${v.slice(0,4)}-${v.slice(4,6)}-${v.slice(6,8)}` : v }
				},
				{ title: "?�화코드", field: "currcd", width: 100, hozAlign: "center" },
				{ title: "?�율", field: "frgnrate", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 4 } },
				{ title: "?�화금액", field: "frgnamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 2 } },
				{ title: "?�화금액", field: "supyamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary fw-bold" }
			]
		})
	}
})
</script>

<style scoped>
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; font-size: 12px; }
.main-content-wrapper { min-height: 0; }
</style>
