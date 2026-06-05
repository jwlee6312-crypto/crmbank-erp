<!--
	=============================================================
	프로그램명	: 지급어음전표발행
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 만기 지급어음을 조회하여 출금 전표를 자동으로 생성
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journal-arrow-down me-2 text-primary" style="font-size: 18px;"></i>
				자금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">지급어음전표발행 (HAFN520U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="save" :disabled="selectedRows.length === 0">
					<i class="bi bi-check-lg"></i> 전표발행
				</button>
			</div>
		</div>

		<!-- 🔍 검색 및 마스터 정보 입력 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">만기일 범위</th>
							<td class="bg-white border-end">
								<div class="d-flex align-items-center gap-1">
									<input v-model="searchForm.ymD_FR" type="date" class="form-control form-control-sm" />
									<span class="text-muted">~</span>
									<input v-model="searchForm.ymD_TO" type="date" class="form-control form-control-sm" />
								</div>
							</td>
							<th class="text-center border-end">발행부서</th>
							<td class="bg-white">
								<div class="input-group input-group-sm">
									<input v-model="voucherForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="voucherForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center border-end border-top">출금계정</th>
							<td class="bg-white border-top border-end">
								<div class="input-group input-group-sm">
									<input v-model="voucherForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="voucherForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('ACCT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center border-end border-top">출금구좌</th>
							<td class="bg-white border-top">
								<div class="input-group input-group-sm">
									<input v-model="voucherForm.mgtno" type="text" class="form-control" placeholder="계정 선택 후 조회" @keydown.enter="openHelp('MGT')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('MGT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center border-end border-top bg-info-subtle">발행일자</th>
							<td class="bg-white border-top border-end">
								<input v-model="voucherForm.slipymd" type="date" class="form-control form-control-sm border-info shadow-sm" style="max-width: 150px;" />
							</td>
							<th class="text-center border-end border-top bg-warning-subtle">총 지불액</th>
							<td class="bg-white border-top px-2 fw-bold text-danger">
								{{ formatMoney(totalSelectedAmount) }} 원
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 대상 어음 그리드 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-2 bg-light d-flex justify-content-between align-items-center border-bottom text-secondary">
					<span class="fw-bold small"><i class="bi bi-list-check me-1"></i> 발행 대상 지급어음 리스트 (체크하여 선택)</span>
				</div>
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
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const today = now.toISOString().substring(0, 10)

// 🔍 검색 조건
const searchForm = reactive({
	ymD_FR: firstDay,
	ymD_TO: today
})

// 📝 전표 발행 정보
const voucherForm = reactive({
	deptcd: authStore.deptcd,
	deptnm: authStore.deptnm,
	slipymd: today,
	acctcd: '',
	acctnm: '',
	mgtno: '',
	mgtnm: '',
	custgbn: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null
const selectedRows = ref<any[]>([])

const totalSelectedAmount = computed(() => {
	return selectedRows.value.reduce((acc, row) => acc + (Number(row.billamt) || 0), 0)
})

const formatMoney = (val: any) => Number(val || 0).toLocaleString()
const formatYmd = (v: string) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_520S_STR', {
			cmpycd: authStore.cmpycd,
			ymD_FR: searchForm.ymD_FR.replace(/-/g, ''),
			ymD_TO: searchForm.ymD_TO.replace(/-/g, '')
		})

		const data = (res.data || []).map((row: any) => ({
			...row,
			endymd_F: formatYmd(row.endymd),
			stdymd_F: formatYmd(row.stdymd),
			billamt: Number(row.billamt || 0)
		}))

		mainGrid?.setData(data)
		selectedRows.value = []
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!voucherForm.deptcd) return vAlert('발행부서를 선택하십시오.')
	if (!voucherForm.acctcd) return vAlert('출금계정을 선택하십시오.')
	if (!voucherForm.mgtno) return vAlert('출금구좌를 선택하십시오.')
	if (selectedRows.value.length === 0) return vAlert('발행할 어음을 선택하십시오.')

	if (!confirm(`${selectedRows.value.length}건의 어음에 대해 전표를 발행하시겠습니까?`)) return

	try {
		// 분개 데이터 구성
		const details = []

		// 1. 차변: 선택된 어음들 (지급어음 계정)
		selectedRows.value.forEach(row => {
			details.push({
				upkind: 'A',
				DBCR: 'D',
				acctcd: row.acctcd,
				acctnm: '지급어음',
				remark: `만기 지급어음(${row.BILLNO}:${row.endymd_F})`,
				AMOUNT: row.billamt,
				USEdeptcd: voucherForm.deptcd,
				usedeptnm: voucherForm.deptnm,
				custcd: row.custcd,
				subnm: row.custnm,
				mgtno: row.BILLNO,
				typeacct: '060' // 지급어음 유형
			})
		})

		// 2. 대변: 출금계정 (보통예금 등)
		details.push({
			upkind: 'A',
			DBCR: 'C',
			acctcd: voucherForm.acctcd,
			acctnm: voucherForm.acctnm,
			remark: '만기 지급어음 지급',
			AMOUNT: totalSelectedAmount.value,
			USEdeptcd: voucherForm.deptcd,
			usedeptnm: voucherForm.deptnm,
			mgtno: voucherForm.mgtno
		})

		const payload = {
			actkind: 'A',
			MASTER: {
				cmpycd: authStore.cmpycd,
				slipymd: voucherForm.slipymd.replace(/-/g, ''),
				acctymd: voucherForm.slipymd.replace(/-/g, ''),
				deptcd: voucherForm.deptcd,
				business: '만기 지급어음 지불 건',
				SLIPGU: '010'
			},
			DETAILS: details
		}

		// 경리전표입력 통합 저장 로직 활용
		const res = await api.post('/api/hasl/HASL_110U_SAVE', payload)
		vAlert('전표가 발행되었습니다.')

		if (res.data && res.data.slipno) {
			window.open(`/api/hasl/HASL_SLIP_PRINT?SLIPGU=010&slipymd=${payload.MASTER.slipymd}&slipno=${res.data.slipno}&deptcd=${voucherForm.deptcd}`)
		}

		search()
	} catch (e) { vAlertError('전표 발행 중 오류가 발생했습니다.') }
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.ymD_FR = firstDay
	searchForm.ymD_TO = today
	voucherForm.acctcd = ''
	voucherForm.acctnm = ''
	voucherForm.mgtno = ''
	mainGrid?.clearData()
	selectedRows.value = []
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'ACCT') {
		Object.assign(modalProps, {
			title: '계정과목 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'A0', cmpycd: authStore.cmpycd, search: voucherForm.acctnm },
			columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 180 }],
			onConfirm: (d: any) => {
				voucherForm.acctcd = d.acctcd;
				voucherForm.acctnm = d.acctnm;
				voucherForm.mgtno = '';
			}
		})
	} else if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: voucherForm.deptnm },
			columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { voucherForm.deptcd = d.deptcd; voucherForm.deptnm = d.deptnm }
		})
	} else if (type === 'MGT') {
		if (!voucherForm.acctcd) return vAlert('출금계정을 먼저 선택하십시오.')
		Object.assign(modalProps, {
			title: '구좌번호 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'M0', acctcd: voucherForm.acctcd, cmpycd: authStore.cmpycd, search: voucherForm.mgtno },
			columns: [{ title: '관리번호', field: 'mgtno', width: 150 }, { title: '구좌명', field: 'mgtnm', width: 150 }],
			onConfirm: (d: any) => { voucherForm.mgtno = d.mgtno; voucherForm.mgtnm = d.mgtnm }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			selectable: true,
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerHozAlign: "center", width: 40 },
				{ title: "만기일", field: "endymd_F", hozAlign: "center", width: 100 },
				{ title: "어음번호", field: "BILLNO", hozAlign: "center", width: 120 },
				{ title: "발행은행", field: "banknm", width: 150 },
				{ title: "발행인", field: "ISSUMAN", width: 120 },
				{ title: "발행일", field: "stdymd_F", hozAlign: "center", width: 100 },
				{ title: "지급거래처", field: "custnm", minWidth: 150 },
				{ title: "금액", field: "billamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 120, cssClass: "fw-bold" }
			],
		})

		mainGrid.on("rowSelectionChanged", (data) => {
			selectedRows.value = data
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.btn-xs { padding: 1px 5px; font-size: 11px; }
</style>
