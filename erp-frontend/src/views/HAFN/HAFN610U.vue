<!--
	=============================================================
	프로그램명	: 미지급금 지불처리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 거래처별 미지급 내역을 조회하고 다양한 수단으로 지불 전표 발행
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-wallet2 me-2 text-primary" style="font-size: 18px;"></i>
				자금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">미지급금 지불처리 (HAFN610U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="save" :disabled="!canSave">
					<i class="bi bi-check-lg"></i> 저장 (전표발행)
				</button>
			</div>
		</div>

		<!-- 🔍 상단 검색 및 마스터 정보 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col />
						<col style="width: 100px;" /><col />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">기준일자</th>
							<td class="bg-white border-end">
								<input v-model="searchForm.PAyyMDT" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center border-end">거 래 처</th>
							<td class="bg-white border-end">
								<div class="input-group input-group-sm">
									<input v-model="searchForm.custcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
									<input v-model="searchForm.custnm" type="text" class="form-control" placeholder="거래처 선택" @keydown.enter="openHelp('SEARCH_CUST')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('SEARCH_CUST')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center border-end bg-info-subtle">발행부서</th>
							<td class="bg-white">
								<div class="input-group input-group-sm">
									<input v-model="voucherForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="voucherForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 지불 대상 목록 (상단 그리드) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column" style="max-height: 40%;">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-2 bg-light d-flex justify-content-between align-items-center border-bottom">
					<span class="fw-bold small text-secondary">1. 지불 대상 (미지급 내역)</span>
					<span class="badge bg-primary">지불 대상 총액: {{ formatMoney(totalTargetPayAmt) }}</span>
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="targetGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>

		<!-- 📊 지불 수단 및 기타 정보 (하단 그리드 및 폼) -->
		<div class="flex-grow-1 overflow-hidden p-2 pt-0 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-2 bg-light d-flex justify-content-between align-items-center border-bottom">
					<div class="d-flex gap-4 align-items-center">
						<span class="fw-bold small text-secondary">2. 지불 방법 (출금/어음 등)</span>
						<div class="d-flex gap-2">
							<div class="input-group input-group-sm" style="width: 180px;">
								<span class="input-group-text bg-transparent border-0 small">지불일</span>
								<input v-model="voucherForm.PAyyMD" type="date" class="form-control form-control-sm" />
							</div>
							<div class="input-group input-group-sm" style="width: 180px;">
								<span class="input-group-text bg-transparent border-0 small">수수료</span>
								<input v-model="voucherForm.JIAMT" type="number" class="form-control form-control-sm text-end" />
							</div>
							<div class="input-group input-group-sm" style="width: 180px;">
								<span class="input-group-text bg-transparent border-0 small">가산세</span>
								<input v-model="voucherForm.GAAMT" type="number" class="form-control form-control-sm text-end" />
							</div>
						</div>
					</div>
					<div class="d-flex gap-2 align-items-center">
						<span class="badge" :class="isBalanced ? 'bg-success' : 'bg-danger'">
							차액: {{ formatMoney(difference) }}
						</span>
						<button class="btn btn-xs btn-outline-primary py-0" @click="addMethodRow">방법추가</button>
					</div>
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="methodGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
				<div class="card-footer p-0 border-top bg-dark text-white">
					<table class="table table-sm table-dark table-bordered mb-0 text-end fw-bold" style="font-size: 11px;">
						<tbody>
							<tr>
								<td class="text-center" style="width: 20%;">지불대상(미지급+수수료+세)</td>
								<td style="width: 20%;" class="text-info">{{ formatMoney(totalRequiredAmt) }}</td>
								<td class="text-center" style="width: 20%;">지불수단 합계</td>
								<td style="width: 20%;" class="text-warning">{{ formatMoney(totalMethodPayAmt) }}</td>
								<td class="text-center" style="width: 10%;">선급금 발생</td>
								<td style="width: 10%; text-align: right;">{{ formatMoney(prepaymentAmt) }}</td>
							</tr>
						</tbody>
					</table>
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

const today = new Date().toISOString().substring(0, 10)
const firstDay = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10)

// 🔍 검색 및 마스터 데이터
const searchForm = reactive({
	PAyyMDT: today,
	custcd: '',
	custnm: ''
})

const voucherForm = reactive({
	deptcd: authStore.deptcd,
	deptnm: authStore.deptnm,
	PAyyMD: today,
	JIAMT: 0,
	GAAMT: 0,
	clsymd: '00000000'
})

// 📊 그리드 레퍼런스
const targetGridRef = ref<HTMLDivElement | null>(null)
const methodGridRef = ref<HTMLDivElement | null>(null)
let targetGrid: Tabulator | null = null
let methodGrid: Tabulator | null = null

// 계산 로직
const totalTargetPayAmt = ref(0)
const totalMethodPayAmt = ref(0)

const totalRequiredAmt = computed(() => {
	return totalTargetPayAmt.value + Number(voucherForm.JIAMT || 0) + Number(voucherForm.GAAMT || 0)
})

const difference = computed(() => totalRequiredAmt.value - totalMethodPayAmt.value)
const isBalanced = computed(() => difference.value === 0)
const prepaymentAmt = computed(() => difference.value < 0 ? Math.abs(difference.value) : 0)

const canSave = computed(() => {
	return searchForm.custcd && totalTargetPayAmt.value > 0 && totalMethodPayAmt.value > 0
})

const formatMoney = (val: any) => Number(val || 0).toLocaleString()

// 🚀 데이터 조회
const search = async () => {
	if (!searchForm.custcd) return vAlert('거래처를 선택하십시오.')

	try {
		// 1. 미지급 내역 조회 (S0)
		const resTarget = await api.post('/api/hafn/HAFN_610U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			PAyyMDF: firstDay.replace(/-/g, ''),
			PAyyMDT: searchForm.PAyyMDT.replace(/-/g, ''),
			custcd: searchForm.custcd
		})

		const targetData = (resTarget.data || []).map((row: any) => ({
			...row,
			slip_key: `${row.col0}-${row.col1}-${row.col2}`,
			slipymd: row.col0,
			slipno: row.col1,
			srowno: row.col2,
			remark: row.col3,
			acctcd: row.col4,
			reqymd: row.col8,
			PAYCNDT: row.COL9,
			PAYCNDT_NM: row.col10,
			UNPAID_AMT: Number(row.col11 || 0),
			PAID_AMT: Number(row.col12 || 0),
			JAN_AMT: Number(row.col13 || 0),
			SELECT: true,
			INPUT_PAY_AMT: Number(row.col13 || 0) // 기본값은 전액
		}))
		targetGrid?.setData(targetData)
		updateTargetTotal()

		// 2. 보유 어음 조회 (S1 - 지불수단용)
		const resBills = await api.post('/api/hafn/HAFN_610U_STR', {
			actkind: 'S1',
			cmpycd: authStore.cmpycd
		})

		const methodData = (resBills.data || []).map((row: any) => ({
			SELECT: false,
			acctnm: '받을어음',
			CPAYCNDT: row.CPAYCNDT,
			mgtno: row.BILLNO,
			custnm: row.custnm,
			custcd: row.custcd,
			remark: `${searchForm.custnm} 외상금 지불`,
			AMOUNT: Number(row.billamt || 0),
			FIXED: true, // 어음은 금액 수정 불가
			Cmgtgbn: '050',
			Cacctcd: '1120' // 예시: 받을어음 계정
		}))

		methodGrid?.setData(methodData)
		updateMethodTotal()

	} catch (e) { vAlertError('조회 실패') }
}

const updateTargetTotal = () => {
	const data = targetGrid?.getData() || []
	totalTargetPayAmt.value = data.filter((r: any) => r.SELECT).reduce((sum, r) => sum + Number(r.INPUT_PAY_AMT || 0), 0)
}

const updateMethodTotal = () => {
	const data = methodGrid?.getData() || []
	totalMethodPayAmt.value = data.filter((r: any) => r.SELECT).reduce((sum, r) => sum + Number(r.AMOUNT || 0), 0)
}

// 💾 저장 처리
const save = async () => {
	if (!isBalanced && prepaymentAmt.value === 0) {
		return vAlert('지불대상 금액과 지불수단 금액이 일치하지 않습니다.')
	}

	if (prepaymentAmt.value > 0) {
		if (!confirm('금액 차이만큼 선급금으로 발행하시겠습니까?')) return
	}

	try {
		const selectedTargets = targetGrid?.getData().filter((r: any) => r.SELECT) || []
		const selectedMethods = methodGrid?.getData().filter((r: any) => r.SELECT) || []

		const details = []

		// 1. 차변 (Debits): 부채 감소
		selectedTargets.forEach(row => {
			details.push({
				upkind: 'A', DBCR: 'D',
				acctcd: row.acctcd,
				remark: `${searchForm.custnm} 외상금 지불`,
				AMOUNT: row.INPUT_PAY_AMT,
				custcd: searchForm.custcd,
				Sslipno: `${row.slipymd}${row.slipno}${row.srowno}`
			})
		})

		// 송금수수료/가산세 차변 추가
		if (voucherForm.JIAMT > 0) details.push({ upkind: 'A', DBCR: 'D', acctcd: '6355', remark: '송금수수료', AMOUNT: voucherForm.JIAMT, custcd: searchForm.custcd })
		if (voucherForm.GAAMT > 0) details.push({ upkind: 'A', DBCR: 'D', acctcd: '6250', remark: '가산세', AMOUNT: voucherForm.GAAMT, custcd: searchForm.custcd })

		// 2. 대변 (Credits): 자산 감소 (지불수단)
		selectedMethods.forEach(row => {
			details.push({
				upkind: 'A', DBCR: 'C',
				acctcd: row.Cacctcd,
				remark: row.remark,
				AMOUNT: row.AMOUNT,
				mgtno: row.mgtno,
				custcd: row.custcd || searchForm.custcd
			})
		})

		// 선급금 대변 추가 (차액 발생 시)
		if (prepaymentAmt.value > 0) details.push({ upkind: 'A', DBCR: 'D', acctcd: '1265', remark: '선급금 발생', AMOUNT: prepaymentAmt.value, custcd: searchForm.custcd })

		const payload = {
			actkind: 'A',
			MASTER: {
				cmpycd: authStore.cmpycd,
				slipymd: voucherForm.PAyyMD.replace(/-/g, ''),
				acctymd: voucherForm.PAyyMD.replace(/-/g, ''),
				deptcd: voucherForm.deptcd,
				business: `${searchForm.custnm} 지불건`,
				SLIPGU: '010'
			},
			DETAILS: details
		}

		const res = await api.post('/api/hasl/HASL_010U_SAVE', payload)
		vAlert('성공적으로 처리되었습니다.')
		if (res.data.slipno) {
			window.open(`/api/hasl/HASL_SLIP_PRINT?SLIPGU=010&slipymd=${payload.MASTER.slipymd}&slipno=${res.data.slipno}&deptcd=${voucherForm.deptcd}`)
		}
		initialize()
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
	resetForm(searchForm)
	resetForm(voucherForm)
	searchForm.PAyyMDT = today
	voucherForm.PAyyMD = today
	voucherForm.deptcd = authStore.deptcd
	voucherForm.deptnm = authStore.deptnm
	targetGrid?.clearData()
	methodGrid?.clearData()
	totalTargetPayAmt.value = 0
	totalMethodPayAmt.value = 0
}

const addMethodRow = () => {
	methodGrid?.addRow({
		SELECT: true,
		acctnm: '',
		mgtno: '',
		custnm: searchForm.custnm,
		remark: `${searchForm.custnm} 외상금 지불`,
		AMOUNT: difference.value > 0 ? difference.value : 0,
		FIXED: false
	})
}

const formatSlipNo = () => {}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell?: any) {
	if (type === 'SEARCH_CUST') {
		Object.assign(modalProps, {
			title: '거래처 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'C0', cmpycd: authStore.cmpycd, search: searchForm.custnm },
			columns: [{ title: '코드', field: 'custcd', width: 80 }, { title: '거래처명', field: 'custnm', width: 180 }],
			onConfirm: (d: any) => { searchForm.custcd = d.custcd; searchForm.custnm = d.custnm; search() }
		})
	} else if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: voucherForm.deptnm },
			columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { voucherForm.deptcd = d.deptcd; voucherForm.deptnm = d.deptnm }
		})
	} else if (type === 'METHOD_ACCT') {
		Object.assign(modalProps, {
			title: '계정 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'E1', cmpycd: authStore.cmpycd, search: '300' }, // 지불수단용 300번대 조회
			columns: [{ title: '코드', field: 'col2', width: 80 }, { title: '계정명', field: 'col1', width: 180 }],
			onConfirm: (d: any) => {
				const row = cell.getRow().getData()
				row.Cacctcd = d.col2; row.acctnm = d.col1;
				row.Cmgtgbn = d.col3; row.CSUBGBN = d.col4;
				cell.getRow().update(row)
			}
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	// 1. 지불 대상 그리드 초기화
	if (targetGridRef.value) {
		targetGrid = new Tabulator(targetGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "선택", field: "SELECT", width: 40, hozAlign: "center", formatter: "tickCross", editor: true, cellClick: (e, cell) => { cell.setValue(!cell.getValue()); updateTargetTotal() } },
				{ title: "전표번호", field: "slip_key", width: 130, hozAlign: "center" },
				{ title: "적요", field: "remark", minWidth: 200 },
				{ title: "요청일", field: "reqymd", width: 90, hozAlign: "center", formatter: (c) => formatYmd(c.getValue()) },
				{ title: "미지급금", field: "UNPAID_AMT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "잔액", field: "JAN_AMT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "지불액", field: "INPUT_PAY_AMT", width: 110, hozAlign: "right", editor: "input", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-warning-subtle" }
			]
		})
		targetGrid.on("cellEdited", updateTargetTotal)
	}

	// 2. 지불 수단 그리드 초기화
	if (methodGridRef.value) {
		methodGrid = new Tabulator(methodGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "선택", field: "SELECT", width: 40, hozAlign: "center", formatter: "tickCross", editor: true, cellClick: (e, cell) => { cell.setValue(!cell.getValue()); updateMethodTotal() } },
				{ title: "계정과목", field: "acctnm", width: 150, cellClick: (e, cell) => { if(!cell.getData().FIXED) openHelp('METHOD_ACCT', cell) }, cssClass: "cursor-pointer" },
				{ title: "관리번호", field: "mgtno", width: 130, editor: "input" },
				{ title: "거래처", field: "custnm", width: 150, editor: "input" },
				{ title: "적요", field: "remark", minWidth: 200, editor: "input" },
				{ title: "금액", field: "AMOUNT", width: 120, hozAlign: "right", editor: "input", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-info-subtle" },
				{ title: "", width: 40, formatter: (c) => c.getData().FIXED ? "" : "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => { if(!cell.getData().FIXED) cell.getRow().delete(); updateMethodTotal() } }
			]
		})
		methodGrid.on("cellEdited", updateMethodTotal)
	}

	// 마감 정보 로드
	api.post('/api/ha00/HA00_010S_STR', { gubun: 'P1', cmpycd: authStore.cmpycd }).then(res => {
		if (res.data) voucherForm.clsymd = res.data.clsymd || '00000000'
	})
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.erp-label-sm { display: block; font-size: 11px; color: #666; margin-bottom: 2px; font-weight: 600; }
.bg-warning-subtle { background-color: #fffcf0 !important; }
.bg-info-subtle { background-color: #f0f7ff !important; }
.btn-xs { padding: 1px 5px; font-size: 11px; }
.cursor-pointer { cursor: pointer; }
</style>