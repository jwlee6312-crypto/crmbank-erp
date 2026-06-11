<!--
	=============================================================
	프로그램명	: 부도어음명세서
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 부도일자별 부도어음의 상세 내역 및 정리 현황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-excel me-2 text-danger" style="font-size: 18px;"></i>
				자금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">부도어음명세서 (HAFN420S)</span>
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
							<th class="text-center border-end">부도일 범위</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-1">
									<input v-model="searchForm.ymd_fr" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
									<span class="text-muted">~</span>
									<input v-model="searchForm.ymd_to" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
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

const now = new Date()
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const today = now.toISOString().substring(0, 10)

// 🔍 검색 조건
const searchForm = reactive({
	ymd_fr: firstDay,
	ymd_to: today
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const formatYmdShort = (v: string) => v && v.length === 8 ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : v

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_420S_STR', {
			cmpycd: authStore.cmpycd,
			ymd_fr: searchForm.ymd_fr.replace(/-/g, ''),
			ymd_to: searchForm.ymd_to.replace(/-/g, '')
		})
		const data = (res.data || []).map((row: any) => ({
			budoymd: formatYmdShort(row.budoymd),
			billno: row.billno,
			issubank: row.issubank,
			issuman: row.issuman,
			stdymd: formatYmdShort(row.stdymd),
			dueymd: formatYmdShort(row.endymd),
			custnm: row.custnm,
			billamt: Number(row.billamt || 0),
			status: Number(row.amt || 0) === 0 ? '정리' : '미정리'
		}))

		mainGrid?.setData(data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.ymd_fr = firstDay
	searchForm.ymd_to = today
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", `부도어음명세서_${today}.xlsx`)

const print = () => {
	const params = `ymd_fr=${searchForm.ymd_fr}&ymd_to=${searchForm.ymd_to}`
	window.open(`/api/hafn/HAFN_420P?${params}`, 'BudoBillPrint', 'width=1000,height=800,scrollbars=yes')
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "부도일", field: "budoymd", hozAlign: "center", width: 90 },
				{ title: "어음번호", field: "billno", hozAlign: "center", width: 130 },
				{ title: "발행은행", field: "issubank", width: 180 },
				{ title: "발행인", field: "issuman", width: 120 },
				{ title: "발행일", field: "stdymd", hozAlign: "center", width: 90 },
				{ title: "만기일", field: "dueymd", hozAlign: "center", width: 90 },
				{ title: "받은거래처", field: "custnm", minWidth: 150 },
				{ title: "금액", field: "billamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 110, cssClass: "fw-bold" },
				{ title: "정리여부", field: "status", hozAlign: "center", width: 90,
					formatter: (cell) => {
						const val = cell.getValue()
						return val === '정리' ? `<span class="badge bg-success">${val}</span>` : `<span class="badge bg-danger">${val}</span>`
					}
				}
			],
			footerElement: "<tfoot><tr></tr></tfoot>" // 하단 합계용 (필요시 Tabulator의 footer 기능을 더 활용할 수 있음)
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
</style>
