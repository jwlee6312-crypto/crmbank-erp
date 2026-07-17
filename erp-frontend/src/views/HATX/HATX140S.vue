<!--	=============================================================
	?�로그램�?: ?�용카드매출?�표?�발?�금?�집계표
	?�성?�자	: 2025.02.24
	?�성??    : AI Assistant
	?�명        : ?�용카드 �??�금?�수�?발행 ?�역??과세/면세�?총괄 집계 (HSOD100U ?��? UI ?�용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-collection-fill me-2 text-primary" style="font-size: 18px;"></i>
				?�무관�?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">?�용카드매출?�표?�발?�금?�집계표 (HATX140S)</span>
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

		<!-- ?�� 메인 컨텐�??�역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-3 bg-light main-content-wrapper">

			<!-- ?���?메인 집계 그리??-->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1 text-primary"></i> 발행금액 ?�황</span>
				</div>
				<div class="card-body p-0 overflow-hidden">
					<div ref="mainGridRef" class="tabulator-instance border-0"></div>
				</div>
			</div>

			<!-- ?���??�금계산??계산??교�??�역 그리??-->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden d-flex flex-column bg-white" style="max-width: 600px;">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark"><i class="bi bi-journal-text me-1 text-primary"></i> ?�금계산??계산?? 교�? ?�역</span>
				</div>
				<div class="card-body p-0 overflow-hidden">
					<div ref="subGridRef" class="tabulator-instance border-0"></div>
				</div>
			</div>

			<!-- ?�️ ?�성방법 ?�내 -->
			<div class="alert alert-secondary border-0 shadow-sm mb-0 p-3 flex-shrink-0">
				<div class="small text-muted">
					<p class="mb-2 fw-bold text-dark">???�성방법</p>
					<p class="mb-1">1. <strong>?�용카드매출?�표 ??발행금액 ?�황</strong>: 부가가치세 과세 매출�? 면세 매출�?�?봉사료로 각각 구분?�여 기입?�고, 과세 매출분�??� 공급?�가(부가가치세�??�함?�니??�?기입?�니??</p>
					<p class="mb-0">2. <strong>?�금계산??계산?? 교�??�역</strong>: ???�황???�계 금액 �??�금계산???�는 계산?��? 교�???금액??기입?�니??</p>
				</div>
			</div>

		</div>
	</div>
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

const mainGridRef = ref<HTMLElement | null>(null)
const subGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null
let subGrid: Tabulator | null = null

const fetchOptions = async () => {
	try {
		const res = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd, search: ' ' });
		taxUnitOptions.value = res.data || [];
		if (taxUnitOptions.value.length > 0) searchForm.taxunit = taxUnitOptions.value[0].code;
	} catch (e) { console.error(e) }
}

async function search() {
	try {
		const ymfr = searchForm.YY + searchForm.FMM;
		const ymto = searchForm.YY + searchForm.TMM;

		const res = await api.post('/api/hatx/HATX_140S_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			taxunit: searchForm.taxunit,
			ymfr: ymfr,
			ymto: ymto
		});

		if (res.data?.length) {
			const d = res.data[0];
			// 매핑 로직 (ASP rs ?�덱??기반)
			const mainData = [
				{ gubun: '?? �?, tot: d.col0, card: d.col1, cash: d.col2 },
				{ gubun: '과세 매출�?, tot: d.col3, card: d.col4, cash: d.col5 },
				{ gubun: '면세 매출�?, tot: d.col6, card: d.col7, cash: d.col8 },
				{ gubun: '봉사�?, tot: d.col9, card: d.col10, cash: d.col11 }
			];
			mainGrid?.setData(mainData);

			const subData = [
				{ item: '?�금계산??교�?금액', amt: d.col12 },
				{ item: '계산??교�?금액', amt: d.col13 }
			];
			subGrid?.setData(subData);
		} else {
			mainGrid?.clearData();
			subGrid?.clearData();
		}

		vAlert('조회?�었?�니??');
	} catch (e) { vAlertError('조회 ?�패'); }
}

const initialize = () => {
	searchForm.taxunit = '000';
	searchForm.YY = String(currentYear);
	searchForm.FMM = currentMonth;
	searchForm.TMM = currentMonth;
	mainGrid?.clearData();
	subGrid?.clearData();
}

const handlePrint = () => {
	const params = new URLSearchParams({
		taxunit: searchForm.taxunit, YY: searchForm.YY, FMM: searchForm.FMM, TMM: searchForm.TMM, PRTGU: 'Print'
	}).toString();
	window.open(`/api/hatx/HATX_140P?${params}`, 'TaxSummaryPrint', 'width=1000,height=800,scrollbars=yes');
}

const initGrids = () => {
	mainGrid = new Tabulator(mainGridRef.value!, {
		layout: "fitColumns",
		height: "auto",
		columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
		columns: [
			{ title: "�? �?, field: "gubun", width: 200, cssClass: "bg-light fw-bold" },
			{ title: "?? �?, field: "tot", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" },
			{ title: "?�용/직불/기명??직불카드", field: "card", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
			{ title: "?�금?�수�?, field: "cash", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } }
		]
	});

	subGrid = new Tabulator(subGridRef.value!, {
		layout: "fitColumns",
		height: "auto",
		columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
		columns: [
			{ title: "?? �?, field: "item", width: 300, cssClass: "bg-light fw-bold" },
			{ title: "�? ??, field: "amt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary fw-bold" }
		]
	});
}

onMounted(() => {
	nextTick(() => {
		initGrids();
		fetchOptions();
		search();
	});
})
</script>

<style scoped>
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; font-size: 13px; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; font-size: 13px; }
</style>
