<!--
	=============================================================
	프로그램명	: 외상매출금명세서
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 거래처별 외상매출금 이월, 매출, 입금, 잔액 명세 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journal-text me-2 text-primary" style="font-size: 18px;"></i>
				마감관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">외상매출금명세서 (HSCL310S)</span>
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
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
				</button>
			</div>
		</div>

		<!-- 🔍 검색 항목 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
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
									<span class="erp-label me-2">판매부서</span>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" @keydown.enter="openHelp" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
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
							<td class="text-end px-3">
								<span v-if="closingMonth" class="badge bg-info-subtle text-info border border-info-subtle">
									마감월: {{ closingMonth.substring(0, 4) }}-{{ closingMonth.substring(4, 6) }}
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
import { useRouter } from 'vue-router'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const router = useRouter()
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

const searchForm = reactive({
	deptcd: authStore.deptcd,
	deptnm: authStore.deptnm,
	yy: currentYear,
    mm: currentMonth
})

const closingMonth = ref('')
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

// 초기 데이터 및 마감정보 조회
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
	if (!searchForm.deptcd) return vAlert('판매부서를 선택해 주십시오.')

	const searchym = `${searchForm.yy}${searchForm.mm}`
	if (closingMonth.value && searchym > closingMonth.value) {
		return vAlert('마감작업 후 조회하시기 바랍니다.')
	}

	try {
		const res = await api.post('/api/hscl/HSCL_310S_STR', {
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

const excel = () => mainGrid?.download("xlsx", "외상매출금명세서.xlsx")
const print = () => {
	const params = `deptcd=${searchForm.deptcd}&deptnm=${searchForm.deptnm}&yy=${searchForm.yy}.mm=${searchForm.mm}`
	window.open(`/api/hscl/HSCL_310P?${params}`, 'Print', 'width=1000,height=800')
}

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
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: [
				{
					title: "거래처명", field: "custnm", minWidth: 200, hozAlign: "left", frozen: true,
					cssClass: "text-primary cursor-pointer fw-bold",
					cellClick: (e, cell) => {
						const d = cell.getData()
						const firstDay = `${searchForm.yy}-${searchForm.mm}-01`
						router.push({
							path: '/HSST/HSST110S',
							query: {
								SELGBN: '2',
								frymd: firstDay,
								toymd: d.lastdd,
								deptcd: searchForm.deptcd,
								custcd: d.custcd,
								custnm: d.custnm
							}
						})
					}
				},
				{ title: "이월액", field: "baseamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
				{ title: "공급가", field: "spyamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
				{ title: "부가세", field: "vatamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
				{
					title: "매출계", field: "salestot", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
					mutatorData: (v, d) => Number(d.spyamt || 0) + Number(d.vatamt || 0),
					bottomCalc: "sum", bottomCalcFormatter: "money", cssClass: "bg-light"
				},
				{ title: "현금", field: "cashamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
				{ title: "예금", field: "bankamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
				{ title: "어음/카드", field: "billamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
				{ title: "기타", field: "etcamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
				{
					title: "입금계", field: "recptot", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
					mutatorData: (v, d) => Number(d.cashamt || 0) + Number(d.bankamt || 0) + Number(d.billamt || 0) + Number(d.etcamt || 0),
					bottomCalc: "sum", bottomCalcFormatter: "money", cssClass: "bg-light"
				},
				{
					title: "잔액", field: "balamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
					mutatorData: (v, d) => Number(d.baseamt || 0) + Number(d.spyamt || 0) + Number(d.vatamt || 0) - (Number(d.cashamt || 0) + Number(d.bankamt || 0) + Number(d.billamt || 0) + Number(d.etcamt || 0)),
					bottomCalc: "sum", bottomCalcFormatter: "money", cssClass: "bg-light fw-bold text-primary"
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
</style>
