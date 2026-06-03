<!--
	=============================================================
	프로그램명	: 제조원가명세서
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 지정된 년월의 제조원가명세서(당기 vs 전기) 조회 및 분석
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-ruled me-2 text-primary" style="font-size: 18px;"></i>
				재무제표 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">제조원가명세서 (HACL040S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>회계일자</span>
							<div class="d-flex align-items-center gap-1">
								<select v-model="searchForm.yy" class="form-select form-select-sm" style="width: 90px;" @change="search">
									<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
								</select>
								<select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 70px;" @change="search">
									<option v-for="month in monthOptions" :key="month" :value="month">{{ month }}</option>
								</select>
								<span class="ms-1">월 까지</span>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
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
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = String(now.getMonth() + 1).padStart(2, '0')

const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({
	yy: String(currentYear),
    mm: currentMonth
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const sNUM = ['', 'Ⅰ', 'Ⅱ', 'Ⅲ', 'Ⅳ', 'Ⅴ', 'Ⅵ', 'Ⅶ', 'Ⅷ', 'Ⅸ', 'Ⅹ', 'Ⅺ', 'Ⅻ', 'XIII', 'XIV']

const search = async () => {
	try {
		const res = await api.post('/api/hacl/HACL_040S_STR', {
			cmpycd: authStore.cmpycd,
			yymm: `${searchForm.yy}${searchForm.mm}`
		})

		const rawData = res.data || []
		let i = 1, k = 1
		let prevAcctCd = ""

		const processedData = rawData.map((row: any) => {
			const acctCd = String(row.col0 || row.acctcd)
			let acctNm = row.col1 || row.acctnm
			const camt = Number(row.col2 || row.camt || 0)
			const bamt = Number(row.col3 || row.bamt || 0)
			const rstyn = row.col4 || row.rstyn
			const mfTot = Number(row.col5 || row.mftot || 0)
			const mfTotB = Number(row.col6 || row.mftot_b || 0)

			// 계정 변경에 따른 인덱스 리셋
			if (prevAcctCd && acctCd.substring(0, 3) !== prevAcctCd.substring(0, 3)) {
				k = 1
			}
			prevAcctCd = acctCd

			let formattedNm = acctNm
			let rate = 0
			let rateB = 0
			let levelClass = ""

			// ASP 로직 포팅: 계정코드 패턴에 따른 포맷팅
			if (acctCd === "4113000" || acctCd === "4160000") {
				formattedNm = `      ${acctNm}`
				levelClass = "ps-4"
			} else if (acctCd.substring(3, 7) === "0000") {
				formattedNm = `${sNUM[i] || i}. ${acctNm}`
				i++
				if (mfTot !== 0) rate = (camt / mfTot) * 100
				if (mfTotB !== 0) rateB = (bamt / mfTotB) * 100
				levelClass = "fw-bold"
			} else if (acctCd.substring(5, 7) === "00") {
				if (rstyn === "Y") {
					formattedNm = `      ${acctNm}`
					levelClass = "ps-4"
				} else {
					formattedNm = `    ${k}. ${acctNm}`
					k++
					levelClass = "ps-3"
				}
			} else {
				formattedNm = `      ${acctNm}`
				levelClass = "ps-4"
			}

			// 특정 조건에 따른 비율 표시 제어 (ASP: acctcd < "414")
			if (acctCd >= "414") {
				rate = 0
				rateB = 0
			}

			return {
				...row,
				FORMATTED_NM: formattedNm,
				camt: camt,
				rate: rate,
				bamt: bamt,
				rate_B: rateB,
				LEVEL_CLASS: levelClass
			}
		})

		mainGrid?.setData(processedData)
		if (processedData.length > 0) vAlert('조회되었습니다.')
		else vAlert('데이터가 존재하지 않습니다.')
	} catch (e) {
		vAlertError('조회 중 오류 발생')
	}
}

const excel = () => {
	const today = new Date().toISOString().substring(0, 10)
	mainGrid?.download("xlsx", `제조원가명세서_${searchForm.yy}${searchForm.mm}_${today}.xlsx`)
}

const print = () => {
	const params = `yy=${searchForm.yy}.mm=${searchForm.mm}&PRTGU=1`
	window.open(`/api/hacl/HACL_040P?${params}`, 'MfgCostPrint', 'width=800,height=800,scrollbars=yes')
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "과  목", field: "FORMATTED_NM", widthGrow: 1.5,
					formatter: (cell) => {
						const d = cell.getData()
						return `<div class="${d.LEVEL_CLASS}">${cell.getValue()}</div>`
					},
					cssClass: "border-end"
				},
				{
					title: "당      기",
					columns: [
						{
							title: "금  액", field: "camt", width: 150, hozAlign: "right",
							formatter: (cell) => cell.getValue() === 0 ? "" : new Intl.NumberFormat().format(cell.getValue())
						},
						{
							title: "%", field: "rate", width: 60, hozAlign: "center",
							formatter: (cell) => cell.getValue() === 0 ? "" : Number(cell.getValue()).toFixed(1),
							cssClass: "border-start"
						}
					],
					headerHozAlign: "center"
				},
				{
					title: "전      기",
					columns: [
						{
							title: "금  액", field: "bamt", width: 150, hozAlign: "right",
							formatter: (cell) => cell.getValue() === 0 ? "" : new Intl.NumberFormat().format(cell.getValue()),
							cssClass: "border-start"
						},
						{
							title: "%", field: "rate_B", width: 60, hozAlign: "center",
							formatter: (cell) => cell.getValue() === 0 ? "" : Number(cell.getValue()).toFixed(1),
							cssClass: "border-start"
						}
					],
					headerHozAlign: "center",
					cssClass: "border-start"
				}
			]
		})
	}
	search()
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; height: 32px !important; display: flex !important; align-items: center !important; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; }
:deep(.ps-3) { padding-left: 1rem !important; }
:deep(.ps-4) { padding-left: 1.5rem !important; }
</style>
