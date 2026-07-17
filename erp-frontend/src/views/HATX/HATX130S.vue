<!--	=============================================================
	?�로그램�?: ?�용카드매출?�표?�취명세??
	?�성?�자	: 2025.02.24
	?�성??    : AI Assistant
	?�명        : ?�용카드 �??�금?�수�?매입 ?�액 공제�??�한 ?�취 명세 ?�황 조회 (HSOD100U ?��? UI ?�용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-credit-card-2-front me-2 text-primary" style="font-size: 18px;"></i>
				?�무관�?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">?�용카드매출?�표?�취명세??(HATX130S)</span>
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

			<!-- ?���??�계 ?�약 그리??-->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1 text-primary"></i> ?�취 명세 총괄 ?�계</span>
					<span class="text-muted small" style="font-size: 11px;">* 구분???�릭?�면 거래 ?�세 ?�역???�시?�니??</span>
				</div>
				<div class="card-body p-0 overflow-hidden">
					<div ref="summaryGridRef" class="tabulator-instance"></div>
				</div>
			</div>

			<!-- ?���?카드/가맹점�??�세 목록 그리??-->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1 text-primary"></i> 카드?�원/가맹점�?집계 ?�세</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
					<div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
				</div>
			</div>
		</div>
	</div>

	<!-- ?�� 거래 ?�세 ?�역 ?�업 모달 -->
	<div class="modal fade" :class="{ show: showDetailModal }" :style="{ display: showDetailModal ? 'block' : 'none' }" tabindex="-1" @click.self="showDetailModal = false">
		<div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
			<div class="modal-content shadow-lg border-0">
				<div class="modal-header bg-primary text-white py-2">
					<h6 class="modal-title fw-bold"><i class="bi bi-search me-2"></i> [{{ detailModalTitle }}] 거래 ?�세 ?�역 ?�황</h6>
					<button type="button" class="btn-close btn-close-white" @click="showDetailModal = false"></button>
				</div>
				<div class="modal-body p-0" style="height: 600px;">
					<div ref="detailGridRef" class="tabulator-instance h-100"></div>
				</div>
				<div class="modal-footer py-1 bg-light">
					<button type="button" class="btn btn-sm btn-secondary" @click="showDetailModal = false">?�기</button>
				</div>
			</div>
		</div>
	</div>
	<div v-if="showDetailModal" class="modal-backdrop fade show"></div>

</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
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
const detailGridRef = ref<HTMLDivElement | null>(null)
let summaryGrid: Tabulator | null = null
let mainGrid: Tabulator | null = null
let detailGrid: Tabulator | null = null

const showDetailModal = ref(false)
const detailModalTitle = ref('')

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

		// 1. ?�계 ?�보 조회 (ACTKIND: S0)
		const resSum = await api.post('/api/hatx/HATX_130S_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			taxunit: searchForm.taxunit,
			ymfr: ymfr,
			ymto: ymto
		})

		const d = resSum.data?.[0] || {}
		const sumData = [
			{ category: '?�계', count: d.col0, supply: d.col1, tax: d.col2, taxtype: '05' },
			{ category: '?�금?�수�?, count: d.col3, supply: d.col4, tax: d.col5, taxtype: '051' },
			{ category: '?�물?�전?�복지카드', count: d.col6, supply: d.col7, tax: d.col8, taxtype: '052' },
			{ category: '?�업?�신?�카??, count: d.col12, supply: d.col13, tax: d.col14, taxtype: '053' },
			{ category: '기�??�용카드??, count: d.col9, supply: d.col10, tax: d.col11, taxtype: '050' }
		]
		summaryGrid?.setData(sumData)

		// 2. 카드/가맹점�?목록 조회 (ACTKIND: S1)
		const resList = await api.post('/api/hatx/HATX_130S_STR', {
			actkind: 'S1',
			cmpycd: authStore.cmpycd,
			taxunit: searchForm.taxunit,
			ymfr: ymfr,
			ymto: ymto
		})

		const listData = (resList.data || []).map((row: any) => ({
			cardno: row.MGTNO,
			custno: row.CUSTNO,
			custnm: row.custnm,
			count: Number(row.TRNCNT || 0),
			supply: Number(row.spyamt || 0),
			tax: Number(row.vatamt || 0)
		}))
		mainGrid?.setData(listData)

		vAlert('조회?�었?�니??')
	} catch (e) { vAlertError('조회 �??�류 발생') }
}

const openDetailView = async (data: any) => {
	detailModalTitle.value = data.category
	showDetailModal.value = true

	await nextTick()
	initDetailGrid()

	try {
		const ymfr = searchForm.YY + searchForm.FMM
		const ymto = searchForm.YY + searchForm.TMM
		const res = await api.post('/api/hatx/HATX_130S_STR', {
			actkind: 'S2',
			cmpycd: authStore.cmpycd,
			taxunit: searchForm.taxunit,
			ymfr: ymfr,
			ymto: ymto,
			taxtype: data.taxtype
		})

		const list = (res.data || []).map((i: any) => ({
			ymd: i.CLTYMD,
			custno: i.CUSTNO,
			custnm: i.custnm,
			supply: i.supyamt,
			tax: i.vatamt,
			cardno: i.MGTNO,
			typenm: i.taxtypeNM
		}))
		detailGrid?.setData(list)
	} catch (e) { vAlertError('?�세 ?�역 조회 ?�패') }
}

const print = () => {
	const params = `taxunit=${searchForm.taxunit}&YY=${searchForm.YY}&FMM=${searchForm.FMM}&TMM=${searchForm.TMM}&PRTGU=1`
	window.open(`/api/hatx/HATX_130P?${params}`, 'CreditCardReceiptPrint', 'width=1000,height=800,scrollbars=yes')
}

const initialize = () => {
	searchForm.taxunit = taxUnitOptions.value.length > 0 ? taxUnitOptions.value[0].code : '000'
	searchForm.YY = String(currentYear)
	searchForm.FMM = currentMonth
	searchForm.TMM = currentMonth
	summaryGrid?.clearData()
	mainGrid?.clearData()
}

const initDetailGrid = () => {
	if (detailGrid || !detailGridRef.value) return
	detailGrid = new Tabulator(detailGridRef.value, {
		layout: 'fitColumns',
		height: '100%',
		columnDefaults: { headerSort: false, vertAlign: "middle" },
		columns: [
			{
				title: "거래?�자", field: "ymd", width: 100, hozAlign: "center",
				formatter: (cell) => { const v = cell.getValue(); return v && v.length === 8 ? `${v.slice(0,4)}-${v.slice(4,6)}-${v.slice(6,8)}` : v }
			},
			{
				title: "?�업?�번??, field: "custno", width: 120, hozAlign: "center",
				formatter: (cell) => { const v = cell.getValue(); return v && v.length === 10 ? `${v.slice(0,3)}-${v.slice(3,5)}-${v.slice(5)}` : v }
			},
			{ title: "?�호", field: "custnm", widthGrow: 1 },
			{ title: "공급가??, field: "supply", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
			{ title: "?�액", field: "tax", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
			{ title: "카드(?�인)번호", field: "cardno", width: 150, hozAlign: "center" },
			{ title: "구분", field: "typenm", width: 100, hozAlign: "center" }
		]
	})
}

onMounted(() => {
	fetchOptions()

	if (summaryGridRef.value) {
		summaryGrid = new Tabulator(summaryGridRef.value, {
			layout: 'fitColumns',
			height: 'auto',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "구분", field: "category", widthGrow: 1, hozAlign: "center", cssClass: "bg-light fw-bold",
					formatter: (cell) => `<span class="text-primary text-decoration-underline" style="cursor:pointer;">${cell.getValue()}</span>`,
					cellClick: (e, cell) => openDetailView(cell.getData())
				},
				{ title: "거래건수", field: "count", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "공급가??, field: "supply", width: 200, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "?�액", field: "tax", width: 180, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } }
			]
		})
	}

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "?�련번호", formatter: "rownum", width: 60, hozAlign: "center" },
				{ title: "카드?�원번호", field: "cardno", width: 150, hozAlign: "center" },
				{
					title: "?�업?�등록번??, field: "custno", width: 130, hozAlign: "center",
					formatter: (cell) => { const v = cell.getValue(); return v && v.length === 10 ? `${v.slice(0,3)}-${v.slice(3,5)}-${v.slice(5)}` : v }
				},
				{ title: "공급??가맹점) ?�호", field: "custnm", widthGrow: 1, cssClass: "fw-bold text-dark" },
				{ title: "거래건수", field: "count", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "공급가??, field: "supply", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "?�액", field: "tax", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary fw-bold" }
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
