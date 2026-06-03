<!--
	=============================================================
	프로그램명	: 거래처 매출원가 list
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 마감 후 거래처별 매출원가 및 이익율 현황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-list-columns-reverse me-2 text-primary" style="font-size: 18px;"></i>
				영업계획 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">거래처 매출원가 list (HSCL520S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
			</div>
		</div>

		<!-- 🔍 검색 항목 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 35%;" />
						<col style="width: 35%;" />
						<col style="width: 30%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">연&nbsp;&nbsp;&nbsp;&nbsp;월</span>
									<div class="d-flex align-items-center gap-1">
										<select v-model="searchForm.yy" class="form-select form-select-sm" style="width: 100px;">
											<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
										</select>
										<select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 80px;">
											<option v-for="month in monthOptions" :key="month" :value="month">{{ month }}월</option>
										</select>
										<span class="ms-1 small text-muted">현재</span>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">판매부서</span>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" @keydown.enter="openHelp" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td class="text-end px-3">
								<span v-if="closingMonth" class="badge bg-info-subtle text-info border border-info-subtle">
									마감월: {{ closingMonth.substring(0,4) }}-{{ closingMonth.substring(4,6) }}
								</span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 중앙 그리드 영역 -->
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
import { ref, reactive, onMounted, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = String(now.getMonth() + 1).padStart(2, '0')

// 선택 옵션 생성
const yearOptions = computed(() => {
	const years = []
	for (let i = 0; i < 5; i++) years.push(currentYear - i)
	return years
})
const monthOptions = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']

// 🔍 검색 데이터
const searchForm = reactive({
	deptcd: authStore.deptcd,
	deptnm: authStore.deptnm,
	yy: currentYear,
    mm: currentMonth
})

const closingMonth = ref('') // 시스템 마감월 (sclsym)
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

// 초기 데이터 (마감정보) 조회
const getClosingInfo = async () => {
	try {
		const res = await api.post('/api/hs00/HS00_000S_STR', { gubun: 'CL', cmpycd: authStore.cmpycd })
		if (res.data) {
			closingMonth.value = res.data.sclsym
			if (closingMonth.value) {
				searchForm.yy = Number(closingMonth.value.substring(0, 4))
				searchForm.mm = closingMonth.value.substring(4, 6)
			}
		}
	} catch (e) { console.error('마감정보 조회 실패') }
}

const search = async () => {
	const searchym = `${searchForm.yy}${searchForm.mm}`

	try {
		const res = await api.post('/api/hscl/HSCL_520S_STR', {
			cmpycd: authStore.cmpycd,
			deptcd: searchForm.deptcd,
			yymm: searchym
		})
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm)
	getClosingInfo()
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", "거래처매출원가list.xlsx")

// 부서 도움창 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({
	title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
	columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
	data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: '' },
	onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm },
	type: 'table'
})

function openHelp() {
	modalProps.data.search = searchForm.deptnm
	modalVisible.value = true
}

onMounted(() => {
	getClosingInfo()

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: ["deptnm", "custnm"],
			groupHeader: (value, count, data, group) => {
				const label = group.getField() === "deptnm" ? "부서: " : "거래처: "
				return `<span class='fw-bold'>${label}${value}</span> <span class='ms-2 text-muted small'>(${count}건)</span>`
			},
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: [
				{ title: "부서명", field: "deptnm", visible: false },
				{ title: "거래처", field: "custnm", visible: false },
				{
					title: "매출일자", field: "salsymd", width: 100,
					formatter: (cell) => {
						const val = cell.getValue()
						return val && val.length === 8 ? `${val.substring(0, 4)}.${val.substring(4, 6)}.${val.substring(6, 8)}` : val
					}
				},
				{
					title: "출고번호", field: "io_no", width: 120,
					formatter: (cell) => {
						const d = cell.getData()
						return `${d.ioym}-${d.iono}`
					}
				},
				{ title: "품목명", field: "itemnm", hozAlign: "left", minWidth: 200 },
				{ title: "수량", field: "jsanqty", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{
					title: "매출금액", field: "jsanamt", width: 120, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money"
				},
				{
					title: "매출원가", field: "wonamt", width: 120, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money"
				},
				{
					title: "이익율", field: "bnfrate", width: 90, hozAlign: "right",
					formatter: (cell) => {
						const val = cell.getValue()
						return val ? Number(val).toFixed(2) + '%' : '0.00%'
					},
					bottomCalc: (values, data) => {
						const salesSum = data.reduce((acc: number, row: any) => acc + Number(row.jsanamt || 0), 0)
						const costSum = data.reduce((acc: number, row: any) => acc + Number(row.wonamt || 0), 0)
						return salesSum !== 0 ? (((salesSum - costSum) / salesSum) * 100).toFixed(2) + '%' : '0.00%'
					}
				}
			],
			columnCalcs: "table"
		})
	}
})
</script>

<style scoped>
.erp-label {
	min-width: 70px;
	font-weight: 500;
	color: #495057;
}

:deep(.tabulator-group) {
	background-color: #f8f9fa !important;
}

:deep(.tabulator-group-level-1) {
	padding-left: 20px !important;
	background-color: #ffffff !important;
}
</style>
