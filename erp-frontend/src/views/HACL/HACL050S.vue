<!--
	=============================================================
	프로그램명	: 손익계산서 (당월)
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 지정된 년월의 손익계산서 (전월누계, 당월, 누계) 조회 및 분석
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-graph-up me-2 text-primary" style="font-size: 18px;"></i>
				재무제표 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">손익계산서(당월) (HACL050S)</span>
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
		const res = await api.post('/api/hacl/HACL_050S_STR', {
			cmpycd: authStore.cmpycd,
			yymm: `${searchForm.yy}${searchForm.mm}`
		})

		const rawData = res.data || []
		let i = 1, j = 1, k = 1

		const processedData = rawData.map((row: any, idx: number) => {
			const acctCd = String(row.col0 || row.acctcd)
			const acctNm = row.col1 || row.acctnm

			const bamtl = Number(row.col2 || row.bamt_l || 0)
			const bamtr = Number(row.col3 || row.bamt_r || 0)
			const camtl = Number(row.col4 || row.camt_l || 0)
			const camtr = Number(row.col5 || row.camt_r || 0)
			const tamtL = Number(row.col6 || row.tamt_l || 0)
			const tamtR = Number(row.col7 || row.tamt_r || 0)

			const rstyn = row.col8 || row.rstyn
			const salesTotB = Number(row.col9 || row.salestot_b || 0)
			const salesTotC = Number(row.col10 || row.salestot_c || 0)
			const salesTotT = Number(row.col11 || row.salestot_t || 0)

			let formattedNm = acctNm
			let brate = 0, crate = 0, trate = 0
			let levelClass = ""

			// ASP 로직 포팅
			if (acctCd.substring(1, 7) === "000000" || ["1990000", "2980000", "3980000", "3990000"].includes(acctCd)) {
				formattedNm = `[ ${acctNm} ]`
				levelClass = "fw-bolder text-dark bg-light-subtle ps-4"
			} else if (acctCd.substring(2, 7) === "00000") {
				formattedNm = `${sNUM[i] || i}. ${acctNm}`
				i++
				if (bamtl !== 0) {
					if (salesTotB !== 0) brate = (bamtl / salesTotB) * 100
					if (salesTotC !== 0) crate = (camtl / salesTotC) * 100
					if (salesTotT !== 0) trate = (tamtL / salesTotT) * 100
				} else {
					if (salesTotB !== 0) brate = (bamtr / salesTotB) * 100
					if (salesTotC !== 0) crate = (camtr / salesTotC) * 100
					if (salesTotT !== 0) trate = (tamtR / salesTotT) * 100
				}
				levelClass = "fw-bold"
			} else if (acctCd.substring(3, 7) === "0000") {
				formattedNm = `  ${j}). ${acctNm}`
				j++
				if (bamtl !== 0) {
					if (salesTotB !== 0) brate = (bamtl / salesTotB) * 100
					if (salesTotC !== 0) crate = (camtl / salesTotC) * 100
					if (salesTotT !== 0) trate = (tamtL / salesTotT) * 100
				} else {
					if (salesTotB !== 0) brate = (bamtr / salesTotB) * 100
					if (salesTotC !== 0) crate = (camtr / salesTotC) * 100
					if (salesTotT !== 0) trate = (tamtR / salesTotT) * 100
				}
				levelClass = "fw-bold ps-2"
			} else if (acctCd.substring(5, 7) === "00") {
				if (rstyn === "Y" || acctCd === "5213000" || acctCd === "5223000") {
					formattedNm = `      ${acctNm}`
				} else {
					formattedNm = `    ${k}. ${acctNm}`
					k++
				}
				levelClass = "ps-3"
			} else {
				formattedNm = `     ${acctNm}`
				levelClass = "ps-4"
			}

			// 인덱스 리셋 로직
			const nextRow = rawData[idx + 1]
			if (nextRow) {
				const nextCd = String(nextRow.col0 || nextRow.acctcd)
				if (acctCd.substring(0, 3) !== nextCd.substring(0, 3)) {
					if (acctCd.substring(0, 2) !== "51") k = 1
				}
				if (acctCd.substring(0, 2) !== nextCd.substring(0, 2)) j = 1
				if (acctCd.substring(0, 1) !== nextCd.substring(0, 1)) i = 1
			}

			return {
				...row,
				FORMATTED_NM: formattedNm,
				bamt: bamtl !== 0 ? bamtl : bamtr,
				brate: brate,
				camt: camtl !== 0 ? camtl : camtr,
				crate: crate,
				tamt: tamtL !== 0 ? tamtL : tamtR,
				trate: trate,
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
	mainGrid?.download("xlsx", `손익계산서_당월_${searchForm.yy}${searchForm.mm}_${today}.xlsx`)
}

const print = () => {
	const params = `yy=${searchForm.yy}.mm=${searchForm.mm}&PRTGU=1`
	window.open(`/api/hacl/HACL_050P?${params}`, 'MonthlyIncomePrint', 'width=800,height=800,scrollbars=yes')
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
					cssClass: "border-end bg-light fw-bold"
				},
				{
					title: "전월누계",
					columns: [
						{
							title: "금  액", field: "bamt", width: 130, hozAlign: "right",
							formatter: "money", formatterParams: { precision: 0 }
						},
						{
							title: "%", field: "brate", width: 50, hozAlign: "center",
							formatter: (cell) => cell.getValue() === 0 ? "" : Number(cell.getValue()).toFixed(1)
						}
					],
					headerHozAlign: "center"
				},
				{
					title: "당  월",
					columns: [
						{
							title: "금  액", field: "camt", width: 130, hozAlign: "right",
							formatter: "money", formatterParams: { precision: 0 },
							cssClass: "bg-light-subtle fw-bold"
						},
						{
							title: "%", field: "crate", width: 50, hozAlign: "center",
							formatter: (cell) => cell.getValue() === 0 ? "" : Number(cell.getValue()).toFixed(1),
							cssClass: "bg-light-subtle"
						}
					],
					headerHozAlign: "center"
				},
				{
					title: "누  계",
					columns: [
						{
							title: "금  액", field: "tamt", width: 130, hozAlign: "right",
							formatter: "money", formatterParams: { precision: 0 },
							cssClass: "fw-bold text-primary"
						},
						{
							title: "%", field: "trate", width: 50, hozAlign: "center",
							formatter: (cell) => cell.getValue() === 0 ? "" : Number(cell.getValue()).toFixed(1)
						}
					],
					headerHozAlign: "center"
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
:deep(.ps-2) { padding-left: 0.5rem !important; }
:deep(.ps-3) { padding-left: 1rem !important; }
:deep(.ps-4) { padding-left: 1.5rem !important; }
</style>
