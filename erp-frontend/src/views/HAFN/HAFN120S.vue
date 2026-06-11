<!--
	=============================================================
	프로그램명	: 적금명세서
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 기준일자 현재의 적금 계약명세 및 불입 현황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-piggy-bank me-2 text-primary" style="font-size: 18px;"></i>
				자금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">적금명세서 (HAFN120S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
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
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">회계일자</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-2">
									<input v-model="searchForm.ymd" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
									<span class="small fw-bold text-secondary">현재</span>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
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
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const today = new Date().toISOString().substring(0, 10)

// 🔍 검색 조건
const searchForm = reactive({
	ymd: today
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const formatYmdShort = (v: string) => v && v.length === 8 ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : v

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_120S_STR', {
			cmpycd: authStore.cmpycd,
			ymd: searchForm.ymd.replace(/-/g, '')
		})

		const data = (res.data || []).map((row: any) => ({
			acctcd: row.acctcd,
			acctnm: row.acctnm,
			custcd: row.bankcd,
			custnm: row.banknm,
			mgtno: row.mgtno,
			wonamt: Number(row.wonamt || 0),
			stdymd: formatYmdShort(row.stdymd),
			endymd: formatYmdShort(row.endymd),
			rate: Number(row.rate || 0),
			payamt: Number(row.payamt || 0),
			paydd: row.paydd,
			amt: Number(row.amt || 0)
		}))

		mainGrid?.setData(data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.ymd = today
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", `적금명세서_${searchForm.ymd}.xlsx`)

const print = () => {
	const params = `ymd=${searchForm.ymd}`
	window.open(`/api/hafn/HAFN_120P?${params}`, 'SavingsStatementPrint', 'width=1000,height=800,scrollbars=yes')
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: "acctnm",
			groupHeader: function(value, count, data, group) {
				const sumamt = data.reduce((acc, curr) => acc + curr.amt, 0)
				return `
					<div class="d-flex justify-content-between w-100 pe-4">
						<span class="fw-bold text-dark">${value} 계</span>
						<div class="small fw-bold">
							<span class="mx-2 text-muted">총불입액 소계: ${sumamt.toLocaleString()}</span>
						</div>
					</div>
				`
			},
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "은행(거래처)", field: "custnm", minWidth: 200 },
				{ title: "구좌번호", field: "mgtno", width: 200 },
				{ title: "계약금", field: "wonamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 120 },
				{ title: "개설일", field: "stdymd", hozAlign: "center", width: 200 },
				{ title: "만기일", field: "endymd", hozAlign: "center", width: 200 },
				{ title: "이율", field: "rate", hozAlign: "center", width: 100,
					formatter: (cell) => Number(cell.getValue() || 0).toFixed(2)
				},
				{ title: "월불입액", field: "payamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 200 },
				{ title: "불입일", field: "paydd", hozAlign: "center", width: 100 },
				{ title: "총불입액", field: "amt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 200,
					cssClass: "fw-bold text-primary"
				},
				{ title: "", field: "empty", visible: true }
			]
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-group) { background-color: #f0f4f8 !important; border-top: 1px solid #d1d9e6 !important; }
:deep(.tabulator-group-handle) { display: none; }
</style>
