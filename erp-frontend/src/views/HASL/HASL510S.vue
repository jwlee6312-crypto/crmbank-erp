<!--
	=============================================================
	프로그램명	: 일계표
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 지정된 회계일자의 차변/대변 합계 및 현금 시재 현황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-check me-2 text-primary" style="font-size: 18px;"></i>
				장부관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">일계표 (HASL510S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
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
							<input v-model="searchForm.ymD" type="date" class="form-control form-control-sm" style="width: 140px;" @change="search" />
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>구분</span>
							<select v-model="searchForm.actkind" class="form-select form-select-sm" style="width: 100px;" @change="search">
								<option value="S0">상세</option>
								<option value="S1">요약</option>
							</select>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
			<!-- 메인 데이터 테이블 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>

			<!-- 💰 하단 현금 시재 현황 -->
			<div class="card border shadow-sm flex-shrink-0 bg-white overflow-hidden">
				<table class="erp-table-full small text-center">
					<colgroup>
						<col style="width: 80px;" />
						<col style="width: 120px;" />
						<col style="width: 120px;" />
						<col style="width: 120px;" />
						<col style="width: 120px;" />
						<col style="width: 100px;" />
						<col />
					</colgroup>
					<thead>
						<tr class="bg-light border-bottom">
							<th class="border-end py-1" rowspan="2">구분</th>
							<th class="border-end py-1" colspan="4">현금 시재 현황</th>
							<th class="border-end py-1" rowspan="2">전표건수</th>
							<th class="py-1" rowspan="2">비고</th>
						</tr>
						<tr class="bg-light border-bottom">
							<th class="border-end py-1 fw-normal">전일잔액</th>
							<th class="border-end py-1 fw-normal">당일증가</th>
							<th class="border-end py-1 fw-normal">당일감소</th>
							<th class="border-end py-1 fw-normal text-primary">당일잔액</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<th class="bg-light-subtle border-end py-2">현금</th>
							<td class="border-end text-end px-2">{{ formatNumber(summary.befocash) }}</td>
							<td class="border-end text-end px-2">{{ formatNumber(summary.currdbcash) }}</td>
							<td class="border-end text-end px-2">{{ formatNumber(summary.currcrcash) }}</td>
							<td class="border-end text-end px-2 fw-bold text-primary">{{ formatNumber(summary.currjancash) }}</td>
							<td class="border-end px-2">{{ summary.slipcnt }} 건</td>
							<td class="px-2 text-start text-muted italic">정상 조회되었습니다.</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const today = new Date().toISOString().substring(0, 10)

// 🔍 검색 조건
const searchForm = reactive({
	ymD: today,
	actkind: 'S0'
})

// 💰 시재 요약 정보
const summary = reactive({
	befocash: 0,
	currdbcash: 0,
	currcrcash: 0,
	currjancash: 0,
	slipcnt: 0
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/hasl/HASL_510S_STR', {
			actkind: searchForm.actkind,
			cmpycd: authStore.cmpycd,
			ymD: searchForm.ymD.replace(/-/g, '')
		})

		const data = res.data || []

		if (data.length >= 2) {
			// Row 0: Cash summary
			const row0 = data[0]
			summary.befocash = Number(row0.col2 || row0.bjanamt || 0)
			summary.currdbcash = Number(row0.col0 || row0.dbamt || 0)
			summary.currcrcash = Number(row0.col1 || row0.cramt || 0)
			summary.currjancash = summary.befocash + summary.currdbcash - summary.currcrcash

			// Row 1: Slip count
			const row1 = data[1]
			summary.slipcnt = Number(row1.col0 || row1.dbamt || 0)

			// Rows 2+: Main details
			const details = data.slice(2).map((row: any) => ({
				...row,
				dbamt: Number(row.col0 || row.dbamt || 0),
				cramt: Number(row.col1 || row.cramt || 0),
				acctcd: row.col2 || row.acctcd,
				acctnm: row.col3 || row.acctnm,
				is_total: row.col2 === '9999999'
			}))

			mainGrid?.setData(details)
			vAlert('조회되었습니다.')
		} else {
			mainGrid?.clearData()
			resetSummary()
			vAlert('데이터가 존재하지 않습니다.')
		}
	} catch (e) {
		vAlertError('조회 중 오류 발생')
		resetSummary()
	}
}

const resetSummary = () => {
	summary.befocash = 0
	summary.currdbcash = 0
	summary.currcrcash = 0
	summary.currjancash = 0
	summary.slipcnt = 0
}

const formatNumber = (val: number) => {
	return new Intl.NumberFormat('ko-KR').format(val)
}

const print = () => {
	const params = `ymD=${searchForm.ymD.replace(/-/g, '')}&actkind=${searchForm.actkind}&PRTGU=1`
	window.open(`/api/hasl/HASL_510P?${params}`, 'DailyStatementPrint', 'width=800,height=800,scrollbars=yes')
}

const goAccountDetail = (acctCd: string) => {
	if (searchForm.actkind !== 'S0' || !acctCd || acctCd === '9999999') return
	router.push({
		path: '/HASL/HASL540S',
		query: { frymd: searchForm.ymD, toymd: searchForm.ymD, acctcd: acctCd }
	})
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "차변 (Debit)", field: "dbamt", widthGrow: 1, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					cssClass: "fw-bold"
				},
				{
					title: "계정과목 (Account)", field: "acctnm", widthGrow: 1, hozAlign: "center",
					formatter: (cell) => {
						const d = cell.getData()
						if (d.is_total) return `<strong>${cell.getValue()}</strong>`
						if (searchForm.actkind === 'S0' && d.acctcd) {
							return `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</span>`
						}
						return cell.getValue()
					},
					cellClick: (e, cell) => {
						const d = cell.getData()
						goAccountDetail(d.acctcd)
					}
				},
				{
					title: "대변 (Credit)", field: "cramt", widthGrow: 1, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					cssClass: "fw-bold"
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
.bg-light-subtle { background-color: #f8f9fa !important; }
.italic { font-style: italic; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; }
</style>
