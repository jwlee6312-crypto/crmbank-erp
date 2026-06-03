<!--
	=============================================================
	프로그램명	: 현금흐름표
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 지정된 년월의 현금흐름표(당월 vs 전월) 조회 및 분석
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-cash-stack me-2 text-primary" style="font-size: 18px;"></i>
				재무제표 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">현금흐름표 (HACL080S)</span>
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
								<span class="ms-1">월</span>
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
		const res = await api.post('/api/hacl/HACL_080S_STR', {
			cmpycd: authStore.cmpycd,
			yymm: `${searchForm.yy}${searchForm.mm}`
		})

		const rawData = res.data || []
		let i = 1, j = 1, k = 1
		let prevAcctCd = ""

		const processedData = rawData.map((row: any) => {
			const acctCd = String(row.upacct || row.col0)
			const acctNm = row.acctnm || row.col1
			const camtl = Number(row.C_AMT_L || row.col2 || 0)
			const camtr = Number(row.C_AMT_R || row.col3 || 0)
			const bamtl = Number(row.B_AMT_L || row.col4 || 0)
			const bamtr = Number(row.B_AMT_R || row.col5 || 0)

			// 계정 변경에 따른 인덱스 리셋 (ASP 로직)
			if (prevAcctCd) {
				if (acctCd.substring(0, 3) !== prevAcctCd.substring(0, 3)) k = 1
				if (acctCd.substring(0, 1) !== prevAcctCd.substring(0, 1)) j = 1
			}
			prevAcctCd = acctCd

			let formattedNm = acctNm
			let levelClass = ""

			if (acctCd.substring(1, 7) === "000000") {
				formattedNm = `${sNUM[i] || i}. ${acctNm}`
				i++
				levelClass = "fw-bolder text-dark"
			} else if (acctCd.substring(2, 7) === "00000") {
				formattedNm = `  ${j}. ${acctNm}`
				j++
				levelClass = "fw-bold ps-3"
			} else {
				formattedNm = `    ${k}) ${acctNm}`
				k++
				levelClass = "ps-4"
			}

			return {
				...row,
				acctcd: acctCd,
				acctnm: acctNm,
				FORMATTED_NM: formattedNm,
				camt_l: camtl,
				camt_r: camtr,
				bamt_l: bamtl,
				bamt_r: bamtr,
				LEVEL_CLASS: levelClass,
				DRILL_AMT: camtl !== 0 ? camtl : camtr
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
	mainGrid?.download("xlsx", `현금흐름표_${searchForm.yy}${searchForm.mm}_${today}.xlsx`)
}

const print = () => {
	const params = `yy=${searchForm.yy}.mm=${searchForm.mm}&PRTGU=1`
	window.open(`/api/hacl/HACL_080P?${params}`, 'CashFlowPrint', 'width=800,height=800,scrollbars=yes')
}

const goDrillDown = (data: any) => {
	if (!data.acctcd) return
	const params = `yy=${searchForm.yy}.mm=${searchForm.mm}&acctcd=${data.acctcd}&acctnm=${data.acctnm}&AMT=${data.DRILL_AMT}`
	window.open(`/api/hacl/HACL_081U?${params}`, 'CashFlowDetail', 'width=400,height=500,scrollbars=yes')
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
						return `<div class="${d.LEVEL_CLASS} text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</div>`
					},
					cellClick: (e, cell) => goDrillDown(cell.getData()),
					cssClass: "border-end bg-light"
				},
				{
					title: "당      월",
					columns: [
						{
							title: "금  액(차)", field: "camt_l", width: 150, hozAlign: "right",
							formatter: (cell) => cell.getValue() === 0 ? "" : new Intl.NumberFormat().format(cell.getValue())
						},
						{
							title: "금  액(대)", field: "camt_r", width: 150, hozAlign: "right",
							formatter: (cell) => cell.getValue() === 0 ? "" : new Intl.NumberFormat().format(cell.getValue()),
							cssClass: "border-start"
						}
					],
					headerHozAlign: "center"
				},
				{
					title: "전      월",
					columns: [
						{
							title: "금  액(차)", field: "bamt_l", width: 150, hozAlign: "right",
							formatter: (cell) => cell.getValue() === 0 ? "" : new Intl.NumberFormat().format(cell.getValue()),
							cssClass: "border-start"
						},
						{
							title: "금  액(대)", field: "bamt_r", width: 150, hozAlign: "right",
							formatter: (cell) => cell.getValue() === 0 ? "" : new Intl.NumberFormat().format(cell.getValue()),
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
