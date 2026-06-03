<!--
	=============================================================
	프로그램명	: 분개장(현업)
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 부서별/기간별 전표 분개 내역 및 상세 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi- journals me-2 text-primary" style="font-size: 18px;"></i>
				전표관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">분개장(현업) (HASL030S)</span>
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
						<col style="width: 25%;" />
						<col style="width: 30%;" />
						<col style="width: 45%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">발행부서</span>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" @keydown.enter="openHelp('DEPT')" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">발행일자</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1">
										<input v-model="searchForm.frymd" type="date" class="form-control form-control-sm" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.toymd" type="date" class="form-control form-control-sm" />
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">계정과목</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1">
										<div class="input-group input-group-sm flex-nowrap">
											<input v-model="searchForm.acctcd1" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
											<input v-model="searchForm.acctnm1" type="text" class="form-control" @keydown.enter="openHelp('ACCT1')" />
											<button class="btn btn-outline-secondary px-2" @click="openHelp('ACCT1')"><i class="bi bi-search"></i></button>
										</div>
										<span class="text-muted">~</span>
										<div class="input-group input-group-sm flex-nowrap">
											<input v-model="searchForm.acctcd2" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
											<input v-model="searchForm.acctnm2" type="text" class="form-control" @keydown.enter="openHelp('ACCT2')" />
											<button class="btn btn-outline-secondary px-2" @click="openHelp('ACCT2')"><i class="bi bi-search"></i></button>
										</div>
									</div>
								</div>
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
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const today = now.toISOString().substring(0, 10)

const searchForm = reactive({
	deptcd: authStore.deptcd,
	deptnm: authStore.deptnm,
	frymd: firstDay,
	toymd: today,
	acctcd1: '',
	acctnm1: '',
	acctcd2: '',
	acctnm2: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.deptcd) return vAlert('발행부서를 선택해 주십시오.')

	try {
		const res = await api.post('/api/hasl/HASL_030S_STR', {
			cmpycd: authStore.cmpycd,
			deptcd: searchForm.deptcd,
			frymd: searchForm.frymd.replace(/-/g, ''),
			toymd: searchForm.toymd.replace(/-/g, ''),
			acctcd1: searchForm.acctcd1,
			acctcd2: searchForm.acctcd2,
			actkind: 'SR'
		})

		const data = res.data || []
		mainGrid?.setData(data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.deptcd = authStore.deptcd
	searchForm.deptnm = authStore.deptnm
	searchForm.frymd = firstDay
	searchForm.toymd = today
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", "분개장.xlsx")
const print = () => {
	const params = new URLSearchParams(searchForm).toString()
	window.open(`/api/hasl/HASL_030P?${params}`, 'Print', 'width=1000,height=800')
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
			data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: searchForm.deptnm },
			columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm }
		})
	} else if (type.startsWith('ACCT')) {
		Object.assign(modalProps, {
			title: '계정과목 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'acctnm',
			data: { gubun: 'A0', cmpycd: authStore.cmpycd, search: type === 'ACCT1' ? searchForm.acctnm1 : searchForm.acctnm2 },
			columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 180 }],
			onConfirm: (d: any) => {
				if (type === 'ACCT1') { searchForm.acctcd1 = d.acctcd; searchForm.acctnm1 = d.acctnm }
				else { searchForm.acctcd2 = d.acctcd; searchForm.acctnm2 = d.acctnm }
			}
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: "SLIPNO",
			groupHeader: (value, count, data: any) => {
				const sumD = data.reduce((s: number, r: any) => s + Number(r.dbamt || 0), 0)
				const sumC = data.reduce((s: number, r: any) => s + Number(r.cramt || 0), 0)
				const isDiff = sumD !== sumC
				return `
					<div class="d-flex justify-content-between align-items-center w-100 pe-3">
						<span>전표번호: <b class="text-primary cursor-pointer" onclick="window.dispatchEvent(new CustomEvent('go-slip', {detail:'${value}'}))">${value}</b></span>
						<span class="small ${isDiff ? 'text-danger' : 'text-secondary'}">
							[소계] 차변: ${sumD.toLocaleString()} / 대변: ${sumC.toLocaleString()}
							${isDiff ? ' <i class="bi bi-exclamation-triangle-fill"></i> 대차불일치' : ''}
						</span>
					</div>`
			},
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: [
				{ title: "No", field: "srowno", width: 50 },
				{
					title: "계정과목", field: "acctnm", width: 150, hozAlign: "left",
					formatter: (cell) => `<div>${cell.getData().acctcd}</div><div class="small text-muted">${cell.getValue()}</div>`
				},
				{
					title: "적요 및 세부내역", field: "remark", hozAlign: "left", minWidth: 300,
					formatter: (cell) => {
						const d = cell.getData()
						const detail = d.DETAIL_INFO ? `<div class="mt-1 small text-primary border-top pt-1">${d.DETAIL_INFO}</div>` : ''
						return `<div>${cell.getValue() || ''}</div>${detail}`
					}
				},
				{
					title: "회계일", field: "acctymD", width: 90,
					formatter: (cell) => {
						const val = cell.getValue()
						return val && val.length === 8 ? `${val.substring(2, 4)}.${val.substring(4, 6)}.${val.substring(6, 8)}` : val
					}
				},
				{ title: "차변", field: "dbamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
				{ title: "대변", field: "cramt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" }
			],
			columnCalcs: "table"
		})

		// 전표 이동 이벤트 리스너
		window.addEventListener('go-slip', ((e: CustomEvent) => {
			const slipKey = e.detail; // 예: 20240101-001
			router.push({
				path: '/HASL/HASL010U',
				query: {
					slipymd: slipKey.substring(0, 8),
					SLIPNO: slipKey.substring(9, 12),
					deptcd: searchForm.deptcd
				}
			})
		}) as EventListener)
	}
})
</script>

<style scoped>
.erp-label {
	min-width: 65px;
	font-weight: 500;
	color: #495057;
}

:deep(.tabulator-group) {
	background-color: #f0f4f8 !important;
	border-top: 1px solid #dee2e6 !important;
}
:deep(.tabulator-group-header) {
	padding: 8px 12px !important;
}
</style>