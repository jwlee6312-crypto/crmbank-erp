<!--
	=============================================================
	프로그램명	: 시산표
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 합계잔액시산표 조회 (차변/대변 합계 및 잔액)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
				재무제표 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">시산표 (HACL010S)</span>
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
								<span>월</span>
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>구분</span>
							<select v-model="searchForm.actkind" class="form-select form-select-sm" style="width: 120px;" @change="search">
								<option value="88">당월시산표</option>
								<option value="00">이월시산표</option>
							</select>
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

// 년도 옵션 (현재년도 기준 5년 전까지)
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

// 🔍 검색 조건
const searchForm = reactive({
	yy: String(currentYear),
    mm: currentMonth,
	actkind: '88'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/hacl/HACL_010S_STR', {
			cmpycd: authStore.cmpycd,
			yymm: `${searchForm.yy}${searchForm.mm}`,
			actkind: searchForm.actkind
		})

		const data = (res.data || []).map((row: any) => ({
			...row,
			acctcd: row.col0 || row.acctcd,
			acctnm: row.col1 || row.acctnm,
			LEV: Number(row.col2 || row.LEV || 0),
			dbamt: Number(row.col3 || row.dbamt || 0),
			cramt: Number(row.col4 || row.cramt || 0),
			Jdbamt: Number(row.col5 || row.Jdbamt || 0),
			Jcramt: Number(row.col6 || row.Jcramt || 0),
			is_total: row.col0 === '9999999'
		}))

		mainGrid?.setData(data)
		if (data.length > 0) vAlert('조회되었습니다.')
		else vAlert('데이터가 존재하지 않습니다.')
	} catch (e) {
		vAlertError('조회 중 오류 발생')
	}
}

const excel = () => {
	const today = new Date().toISOString().substring(0, 10)
	mainGrid?.download("xlsx", `시산표_${searchForm.yy}${searchForm.mm}_${today}.xlsx`)
}

const print = () => {
	const params = `yy=${searchForm.yy}.mm=${searchForm.mm}&actkind=${searchForm.actkind}&PRTGU=1`
	window.open(`/api/hacl/HACL_010P?${params}`, 'TrialBalancePrint', 'width=800,height=800,scrollbars=yes')
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "차  변 (Debit)",
					columns: [
						{
							title: "잔  액", field: "Jdbamt", width: 150, hozAlign: "right",
							formatter: "money", formatterParams: { precision: 0 },
							cssClass: "border-end"
						},
						{
							title: "합  계", field: "dbamt", width: 150, hozAlign: "right",
							formatter: "money", formatterParams: { precision: 0 }
						}
					],
					headerHozAlign: "center"
				},
				{
					title: "과  목 (Account)", field: "acctnm", widthGrow: 1, hozAlign: "center",
					formatter: (cell) => {
						const d = cell.getData()
						if (d.is_total) return `<strong>${cell.getValue()}</strong>`
						return cell.getValue()
					},
					cssClass: "border-start border-end fw-bold bg-light"
				},
				{
					title: "대  변 (Credit)",
					columns: [
						{
							title: "합  계", field: "cramt", width: 150, hozAlign: "right",
							formatter: "money", formatterParams: { precision: 0 },
							cssClass: "border-end"
						},
						{
							title: "잔  액", field: "Jcramt", width: 150, hozAlign: "right",
							formatter: "money", formatterParams: { precision: 0 }
						}
					],
					headerHozAlign: "center"
				}
			],
			rowFormatter: (row) => {
				const d = row.getData()
				if (d.is_total) {
					row.getElement().style.backgroundColor = "#f0f7ff"
					row.getElement().style.fontWeight = "bold"
				}
			}
		})
	}
	search()
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; }
:deep(.tabulator-col-group) { border-right: 1px solid #dee2e6 !important; }
</style>
