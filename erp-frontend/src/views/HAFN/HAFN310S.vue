<!--
	=============================================================
	프로그램명	: 유가증권명세서
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 기준일자 현재의 유가증권(주식, 채권 등) 명세 및 장부가액 현황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
				자금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">유가증권명세서 (HAFN310S)</span>
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
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">종 목</th>
							<td class="bg-white border-end">
								<select v-model="searchForm.bondkind" class="form-select form-select-sm" style="max-width: 200px;">
									<option value="000">전체</option>
									<option v-for="item in bondKindOptions" :key="item.codecd" :value="item.codecd">{{ item.codenm }}</option>
								</select>
							</td>
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
	bondkind: '000',
	ymd: today
})

const bondKindOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const formatYmdShort = (v: string) => v && v.length === 8 ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : v

// 초기 데이터 로드 (종목 옵션)
const loadInitData = async () => {
	try {
		const res = await api.post('/api/ha00/HA00_00P_STR', {
			gubun: 'E0',
			cmpycd: authStore.cmpycd,
			gbncd: '140'
		})
		bondKindOptions.value = res.data.map((item: any) => ({
			codecd: item.codecd,
			codenm: item.codenm
		}))
	} catch (e) {
		console.error('종목 코드 로드 실패', e)
	}
}

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_310S_STR', {
			cmpycd: authStore.cmpycd,
			bondkind: searchForm.bondkind,
			ymd: searchForm.ymd.replace(/-/g, '')
		})

		const data = (res.data || []).map((row: any) => ({
			acctcd: row.acctcd,
			acctnm: row.acctnm,
			custcd: row.bankcd,
			custnm: row.banknm,
			mgtno: row.mgtno,
			stdymd: formatYmdShort(row.stdymd),
			endymd: formatYmdShort(row.endymd),
			rate: Number(row.rate || 0),
			puchqty: Number(row.puchqty || 0),
			issuamt: Number(row.issuamt || 0),
			puchamt: Number(row.puchamt || 0),
			amt: Number(row.col11 || 0)
		}))

		mainGrid?.setData(data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.ymd = today
	searchForm.bondkind = '000'
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", `유가증권명세서_${searchForm.ymd}.xlsx`)

const print = () => {
	const params = `ymd=${searchForm.ymd}&bondkind=${searchForm.bondkind}`
	window.open(`/api/hafn/HAFN_310P?${params}`, 'SecuritiesPrint', 'width=1000,height=800,scrollbars=yes')
}

onMounted(() => {
	loadInitData()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: ["acctnm", "custnm"], // 계정과목 -> 발행처 순으로 그룹핑
			groupHeader: function(value, count, data, group) {
				const sumISSU = data.reduce((acc, curr) => acc + curr.issuamt, 0)
				const sumPUCH = data.reduce((acc, curr) => acc + curr.puchamt, 0)
				const sumamt = data.reduce((acc, curr) => acc + curr.amt, 0)

				return `
					<div class="d-flex justify-content-between w-100 pe-4">
						<span class="fw-bold text-dark">${value} 계</span>
						<div class="small fw-bold">
							<span class="mx-2 text-muted">액면가: ${sumISSU.toLocaleString()} / 취득가: ${sumPUCH.toLocaleString()} / 장부가: ${sumamt.toLocaleString()}</span>
						</div>
					</div>
				`
			},
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "증권번호", field: "mgtno", minWidth: 200 },
				{ title: "발행일", field: "stdymd", hozAlign: "center", width: 200 },
				{ title: "만기일", field: "endymd", hozAlign: "center", width: 200 },
				{ title: "이율", field: "rate", hozAlign: "right", width: 150, formatter: (c) => Number(c.getValue()).toFixed(2) },
				{ title: "매수", field: "puchqty", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 150 },
				{ title: "액면가", field: "issuamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 200 },
				{ title: "취득가", field: "puchamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 200 },
				{ title: "장부가", field: "amt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 200,
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
:deep(.tabulator-group-level-1) { background-color: #f8f9fa !important; padding-left: 20px !important; }
:deep(.tabulator-group-handle) { display: none; }
</style>
