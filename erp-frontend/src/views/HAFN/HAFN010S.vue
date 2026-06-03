<!--
	=============================================================
	프로그램명	: 자금현황
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 기준일자별 자금(현금, 예금 등)의 잔액 및 증감 현황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-cash-stack me-2 text-primary" style="font-size: 18px;"></i>
				자금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">자금현황 (HAFN010S)</span>
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
							<th class="text-center border-end">기준일자</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-2">
									<input v-model="searchForm.ymD" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
									<span class="small fw-bold text-secondary">현재</span>
									<select v-model="searchForm.SELGBN" class="form-select form-select-sm ms-3" style="max-width: 120px;" @change="search">
										<option value=.mm">당월기준</option>
										<option value="DD">당일기준</option>
									</select>
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
import { ref, reactive, onMounted, watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const today = new Date().toISOString().substring(0, 10)

// 🔍 검색 조건
const searchForm = reactive({
	ymD: today,
	SELGBN: .mm'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

// 그리드 컬럼 정의 (조회구분에 따라 헤더 텍스트 변경)
const getColumns = () => {
	const prefix = searchForm.SELGBN === .mm' ? '월' : '일'
	const prevPrefix = searchForm.SELGBN === .mm' ? '전월' : '전일'

	return [
		{ title: "과목", field: "acctnm", width: 180,
			formatter: (cell) => `<span class="text-primary cursor-pointer text-decoration-underline fw-bold">${cell.getValue()}</span>`,
			cellClick: (e, cell) => goAcctLedger(cell.getData())
		},
		{ title: "보조과목(거래처)", field: "custnm", width: 220,
			formatter: (cell) => `<span class="text-success cursor-pointer text-decoration-underline">${cell.getValue()}</span>`,
			cellClick: (e, cell) => goCustLedger(cell.getData())
		},
		{ title: `${prevPrefix}잔액`, field: "bjamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 140 },
		{ title: `당${prefix}증가`, field: "Icamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 140 },
		{ title: `당${prefix}감소`, field: "Dcamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 140 },
		{ title: `당${prefix}잔액`, field: "janamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 140,
			cssClass: "fw-bold bg-light-subtle"
		}
	]
}

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_010S_STR', {
			cmpycd: authStore.cmpycd,
			ymD: searchForm.ymD.replace(/-/g, ''),
			SELGBN: searchForm.SELGBN
		})

		const data = (res.data || []).map((row: any) => ({
			DC: row.col0,
			acctcd: row.col1,
			acctnm: row.col2,
			custcd: row.col3,
			custnm: row.col4,
			bjamt: Number(row.col5 || 0),
			Icamt: Number(row.col6 || 0),
			Dcamt: Number(row.col7 || 0),
			janamt: Number(row.col8 || 0)
		}))

		// 컬럼 헤더 갱신
		mainGrid?.setColumns(getColumns())
		mainGrid?.setData(data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.ymD = today
	searchForm.SELGBN = .mm'
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", `자금현황_${searchForm.ymD}.xlsx`)

const print = () => {
	const params = `ymD=${searchForm.ymD}&SELGBN=${searchForm.SELGBN}`
	window.open(`/api/hafn/HAFN_010P?${params}`, 'CashStatusPrint', 'width=1000,height=800,scrollbars=yes')
}

// 상세 페이지 이동 로직
const goAcctLedger = (row: any) => {
	const firstDay = `${searchForm.ymD.substring(0, 7)}-01`
	router.push({
		path: '/HASL/HASL540S',
		query: { acctcd: row.acctcd, frymd: firstDay, toymd: searchForm.ymD }
	})
}

const goCustLedger = (row: any) => {
	const firstDay = `${searchForm.ymD.substring(0, 7)}-01`
	router.push({
		path: '/HASL/HASL550S',
		query: { acctcd: row.acctcd, custcd: row.custcd, frymd: firstDay, toymd: searchForm.ymD }
	})
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: "acctnm", // 과목별 그룹핑
			groupHeader: function(value, count, data, group) {
				const sumBJ = data.reduce((acc, curr) => acc + curr.bjamt, 0)
				const sumIC = data.reduce((acc, curr) => acc + curr.Icamt, 0)
				const sumDC = data.reduce((acc, curr) => acc + curr.Dcamt, 0)
				const sumJAN = data.reduce((acc, curr) => acc + curr.janamt, 0)

				return `
					<div class="d-flex justify-content-between w-100 pe-4">
						<span class="fw-bold text-dark">${value} 계</span>
						<div class="small fw-bold">
							<span class="mx-2 text-muted">잔액: ${sumJAN.toLocaleString()}</span>
						</div>
					</div>
				`
			},
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: getColumns()
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.cursor-pointer { cursor: pointer; }
:deep(.tabulator-group) { background-color: #e9ecef !important; border-top: 1px solid #dee2e6 !important; }
:deep(.tabulator-group-handle) { display: none; }
:deep(.bg-light-subtle) { background-color: #f8f9fa !important; }
</style>
