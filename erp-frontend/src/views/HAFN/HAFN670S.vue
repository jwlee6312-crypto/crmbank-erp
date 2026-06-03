<!--
	=============================================================
	프로그램명	: 미지급금현황
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 거래처별 미지급금 발생, 지불, 잔액 현황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
				자금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">미지급금현황 (HAFN670S)</span>
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
						<col style="width: 100px;" /><col style="width: 200px;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">기준일자</th>
							<td class="bg-white border-end">
								<div class="d-flex align-items-center gap-1">
									<input v-model="searchForm.ymD" type="date" class="form-control form-control-sm" />
									<span class="small fw-bold text-secondary">현재</span>
								</div>
							</td>
							<th class="text-center border-end">거 래 처</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-1">
									<div class="input-group input-group-sm" style="width: 45%;">
										<input v-model="searchForm.custcdFR" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.custnmFR" type="text" class="form-control" @keydown.enter="openHelp('CUSTFR')" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('CUSTFR')"><i class="bi bi-search"></i></button>
									</div>
									<span class="text-muted">~</span>
									<div class="input-group input-group-sm" style="width: 45%;">
										<input v-model="searchForm.custcdTO" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.custnmTO" type="text" class="form-control" @keydown.enter="openHelp('CUSTTO')" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('CUSTTO')"><i class="bi bi-search"></i></button>
									</div>
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

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useRouter } from 'vue-router'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const today = new Date().toISOString().substring(0, 10)

// 🔍 검색 데이터
const searchForm = reactive({
	ymD: today,
	custcdFR: '',
	custnmFR: '',
	custcdTO: '',
	custnmTO: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_670S_STR', {
			cmpycd: authStore.cmpycd,
			ymD: searchForm.ymD.replace(/-/g, ''),
			custcdFR: searchForm.custcdFR,
			custcdTO: searchForm.custcdTO
		})

		const data = (res.data || []).map((row: any) => ({
			custcd: row.col0,
			custnm: row.col1,
			UPYAMT: Number(row.col2 || 0),
			payamt: Number(row.col3 || 0),
			janamt: Number(row.col4 || 0),
			BANK_INFO: `${row.col5 || ''} ${row.col6 || ''}`.trim()
		}))

		mainGrid?.setData(data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.ymD = today
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", `미지급금현황_${today}.xlsx`)

const print = () => {
	const params = `ymD=${searchForm.ymD}&custcdFR=${searchForm.custcdFR}&custcdTO=${searchForm.custcdTO}`
	window.open(`/api/hafn/HAFN_670P?${params}`, 'UnpaidStatusPrint', 'width=1000,height=800,scrollbars=yes')
}

// 상세 이동 (HAFN_650S로 이동)
const goDetail = (row: any) => {
	router.push({
		path: '/HAFN/HAFN650S',
		query: { ymD: searchForm.ymD.replace(/-/g, ''), custcd: row.custcd, custnm: row.custnm }
	})
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	const isFr = type === 'CUSTFR'
	Object.assign(modalProps, {
		title: '거래처 선택', path: '/api/ha00/HA00_00P_STR',
		data: { gubun: 'C0', cmpycd: authStore.cmpycd, search: isFr ? searchForm.custnmFR : searchForm.custnmTO },
		columns: [{ title: '코드', field: 'custcd', width: 80 }, { title: '거래처명', field: 'custnm', width: 180 }],
		onConfirm: (d: any) => {
			if (isFr) { searchForm.custcdFR = d.custcd; searchForm.custnmFR = d.custnm }
			else { searchForm.custcdTO = d.custcd; searchForm.custnmTO = d.custnm }
		}
	})
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "거래처", field: "custcd", width: 100, hozAlign: "center",
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer fw-bold">${cell.getValue()}</span>`,
					cellClick: (e, cell) => goDetail(cell.getData())
				},
				{
					title: "거래처명", field: "custnm", width: 250,
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</span>`,
					cellClick: (e, cell) => goDetail(cell.getData())
				},
				{ title: "입금은행/계좌", field: "BANK_INFO", minWidth: 200 },
				{
					title: "발생액", field: "UPYAMT", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "지불액", field: "payamt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "잔 액", field: "janamt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 },
					cssClass: "fw-bold text-danger"
				}
			]
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.cursor-pointer { cursor: pointer; }
</style>