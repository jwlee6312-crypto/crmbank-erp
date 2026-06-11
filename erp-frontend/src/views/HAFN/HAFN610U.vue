<!--
	=============================================================
	프로그램명	: 지불등록 (HAFN610U)
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 거래처별 미지급 내역에 대한 지불 처리 및 전표 생성 (소문자 원칙 및 HSOD100U 디자인 적용)
	=============================================================
-->

<template>
	<app_alert :show="show_alert" :error="show_error" :message="alert_message" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-wallet2 me-2 text-primary" style="font-size: 18px;"></i>
				자금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">지불등록 (hafn610u)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save" :disabled="!can_save">지불확정</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- [상단] 조회 필터 영역 (HSOD100U 표준화) -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense" style="table-layout: fixed;">
						<colgroup>
							<col style="width: 100px;" /><col style="width: 300px;" />
							<col style="width: 100px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="text-center bg-light border-end">지불처</th>
								<td class="bg-white border-end px-2">
									<div class="input-group input-group-sm">
										<input v-model="search_form.custcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
										<input v-model="search_form.custnm" type="text" class="form-control" placeholder="거래처 검색" @keydown.enter="open_help('SEARCH_CUST')" />
										<button class="btn btn-outline-secondary px-2" @click="open_help('SEARCH_CUST')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="text-center bg-light border-end">기준일자</th>
								<td class="bg-white px-2">
									<input v-model="search_form.payymdt" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- [중간] 지불 마스터 정보 영역 (HSOD100U 표준화) -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 110px;" /><col />
							<col style="width: 110px;" /><col />
							<col style="width: 110px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="bg-light text-center border-end">지불일</th>
								<td class="border-end px-2">
									<input v-model="voucher_form.payymd" type="date" class="form-control form-control-sm" />
								</td>
								<th class="bg-light text-center border-end text-primary fw-bold">수수료</th>
								<td class="border-end px-2">
									<input v-model="voucher_form.jiamt" type="number" class="form-control form-control-sm text-end" />
								</td>
								<th class="bg-light text-center border-end text-danger fw-bold">가산세</th>
								<td class="px-2">
									<input v-model="voucher_form.gaamt" type="number" class="form-control form-control-sm text-end" />
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- [하단 그리드 1] 지불 대상 (미지급 내역) -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white" style="flex-basis: 40%;">
				<div class="card-header py-1 px-3 bg-white d-flex justify-content-between align-items-center border-bottom">
					<span class="fw-bold small text-dark"><i class="bi bi-list-task me-2 text-primary"></i>1. 지불 대상 (미지급 내역)</span>
					<span class="badge bg-primary">지불 대상 총액: {{ format_money(total_target_pay_amt) }}</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
					<div ref="target_grid_ref" class="tabulator-instance flex-grow-1"></div>
				</div>
			</div>

			<!-- [하단 그리드 2] 지불 방법 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-3 bg-white d-flex justify-content-between align-items-center border-bottom text-dark">
					<div class="d-flex gap-4 align-items-center">
						<span class="fw-bold small"><i class="bi bi-credit-card-2-back me-2 text-success"></i>2. 지불 방법 (출금/어음 등)</span>
					</div>
					<div class="d-flex gap-2 align-items-center">
						<span class="badge" :class="is_balanced ? 'bg-success' : 'bg-danger'">
							차액: {{ format_money(difference) }}
						</span>
						<button class="btn btn-xs btn-outline-primary py-0 fw-bold" @click="add_method_row">+ 방법추가</button>
					</div>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
					<div ref="method_grid_ref" class="tabulator-instance flex-grow-1"></div>
				</div>
				<div class="card-footer p-0 border-top bg-dark text-white">
					<table class="table table-sm table-dark table-bordered mb-0 text-end fw-bold" style="font-size: 11px;">
						<tbody>
							<tr>
								<td class="text-center" style="width: 25%;">지불대상(미지급+수수료+세)</td>
								<td style="width: 20%;" class="text-info">{{ format_money(total_required_amt) }}</td>
								<td class="text-center" style="width: 20%;">지불수단 합계</td>
								<td style="width: 20%;" class="text-warning">{{ format_money(total_method_pay_amt) }}</td>
								<td class="text-center" style="width: 15%;">선급금 발생: {{ format_money(prepayment_amt) }}</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>

	<modal_component v-model:visible="modal_visible" :modalProps="modal_props" />
</template>

<script setup lang="ts">
import { ref as _ref, reactive as _reactive, onMounted as _on_mounted, computed as _computed } from 'vue'
import { TabulatorFull as tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts as use_alerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore as use_auth_store } from '@/stores/authStore'
import { useFormReset as use_form_reset } from '@/composables/useFormReset'
import app_alert from '@/components/AppAlert.vue'
import modal_component from '@/components/Modal.vue'
import type { ModalProps as modal_props_type } from '@/types/modal'

const auth_store = use_auth_store()
const { showAlert: show_alert, showError: show_error, alertMessage: alert_message, vAlert: v_alert, vAlertError: v_alert_error } = use_alerts()
const { resetForm: reset_form } = use_form_reset()

const today = new Date().toISOString().substring(0, 10)
const first_day = today.substring(0, 8) + '01'

// 🔍 날짜 포맷 헬퍼
const format_ymd = (v: string) => (v && v.length === 8) ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v

// 🔍 검색 및 마스터 데이터 (소문자 원칙)
const search_form = _reactive({
	payymdt: today,
	custcd: '',
	custnm: ''
})

const voucher_form = _reactive({
	deptcd: auth_store.deptcd,
	deptnm: auth_store.deptnm,
	payymd: today,
	jiamt: 0,
	gaamt: 0,
	clsymd: '00000000'
})

const target_grid_ref = _ref<HTMLDivElement | null>(null)
const method_grid_ref = _ref<HTMLDivElement | null>(null)
let target_grid: tabulator | null = null
let method_grid: tabulator | null = null

const total_target_pay_amt = _ref(0)
const total_method_pay_amt = _ref(0)

const total_required_amt = _computed(() => {
	return total_target_pay_amt.value + Number(voucher_form.jiamt || 0) + Number(voucher_form.gaamt || 0)
})

const difference = _computed(() => total_required_amt.value - total_method_pay_amt.value)
const is_balanced = _computed(() => difference.value === 0)
const prepayment_amt = _computed(() => difference.value < 0 ? Math.abs(difference.value) : 0)

const can_save = _computed(() => {
	return search_form.custcd && total_target_pay_amt.value > 0 && total_method_pay_amt.value > 0
})

const format_money = (val: any) => Number(val || 0).toLocaleString()

const search = async () => {
	if (!search_form.custcd) return v_alert('거래처를 선택하십시오.')

	try {
		const res_target = await api.post('/api/hafn/HAFN_610U_STR', {
			actkind: 'S0',
			cmpycd: auth_store.cmpycd,
			payymdf: first_day.replace(/-/g, ''),
			payymdt: search_form.payymdt.replace(/-/g, ''),
			custcd: search_form.custcd
		})

		const target_data = (res_target.data || []).map((row: any) => {
            const item = Object.fromEntries(Object.entries(row).map(([k, v]) => [k.toLowerCase(), v]))
            return {
                ...item,
                slip_key: `${item.col0}-${item.col1}-${item.col2}`,
                unpaid_amt: Number(item.upyamt || 0),
                paid_amt: Number(item.payamt || 0),
                jan_amt: Number(item.janamt || 0),
                select: true,
                input_pay_amt: Number(item.janamt || 0)
            }
        })
		target_grid?.setData(target_data)
		update_target_total()

		const res_bills = await api.post('/api/hafn/HAFN_610U_STR', { actkind: 'S1', cmpycd: auth_store.cmpycd })
		const method_data = (res_bills.data || []).map((row: any) => {
            const item = Object.fromEntries(Object.entries(row).map(([k, v]) => [k.toLowerCase(), v]))
            return {
                select: false,
                acctnm: '받을어음',
                cpaycndt: item.cpaycndt,
                mgtno: item.billno,
                custnm: item.custnm,
                custcd: item.custcd,
                remark: `${search_form.custnm} 외상금 지불`,
                amount: Number(item.billamt || 0),
                fixed: true,
                cmgtgbn: '050',
                cacctcd: '1120',
                csubgbn: '010'
            }
        })
		method_grid?.setData(method_data)
		update_method_total()

	} catch (e) { v_alert_error('조회 실패') }
}

const update_target_total = () => {
	const data = target_grid?.getData() || []
	total_target_pay_amt.value = data.filter((r: any) => r.select).reduce((sum, r) => sum + Number(r.input_pay_amt || 0), 0)
}

const update_method_total = () => {
	const data = method_grid?.getData() || []
	total_method_pay_amt.value = data.filter((r: any) => r.select).reduce((sum, r) => sum + Number(r.amount || 0), 0)
}

const save = async () => {
	if (!is_balanced.value && prepayment_amt.value === 0) return v_alert('금액 차액이 발생했습니다.')
	if (!confirm('지불 확정 처리를 하시겠습니까?')) return

	try {
		const selected_targets = target_grid?.getData().filter((r: any) => r.select) || []
		const selected_methods = method_grid?.getData().filter((r: any) => r.select) || []

		const details = []
		selected_targets.forEach(row => {
			details.push({ upkind: 'A', dbcr: 'D', acctcd: row.acctcd, remark: `${search_form.custnm} 외상금 지불`, amount: row.input_pay_amt, custcd: search_form.custcd, sslipno: `${row.slipymd}${row.slipno}${row.srowno}` })
		})
		if (voucher_form.jiamt > 0) details.push({ upkind: 'A', dbcr: 'D', acctcd: '6355', remark: '송금수수료', amount: voucher_form.jiamt, custcd: search_form.custcd })
		if (voucher_form.gaamt > 0) details.push({ upkind: 'A', dbcr: 'D', acctcd: '6250', remark: '가산세', amount: voucher_form.gaamt, custcd: search_form.custcd })

		selected_methods.forEach(row => {
			details.push({ upkind: 'A', dbcr: 'C', acctcd: row.cacctcd, remark: row.remark, amount: row.amount, mgtno: row.mgtno, custcd: row.custcd || search_form.custcd })
		})
		if (prepayment_amt.value > 0) details.push({ upkind: 'A', dbcr: 'D', acctcd: '1265', remark: '선급금 발생', amount: prepayment_amt.value, custcd: search_form.custcd })

		const payload = {
			actkind: 'A',
			master: { cmpycd: auth_store.cmpycd, slipymd: voucher_form.payymd.replace(/-/g, ''), acctymd: voucher_form.payymd.replace(/-/g, ''), deptcd: voucher_form.deptcd, business: `${search_form.custnm} 지불건`, slipgu: '010' },
			details: details
		}

		const res = await api.post('/api/hasl/HASL_010U_SAVE', payload)
		v_alert('성공적으로 처리되었습니다.')
		if (res.data.slipno) window.open(`/api/hasl/HASL_SLIP_PRINT?slipgu=010&slipymd=${payload.master.slipymd}&slipno=${res.data.slipno}&deptcd=${voucher_form.deptcd}`)
		initialize()
	} catch (e) { v_alert_error('저장 실패') }
}

const initialize = () => {
	reset_form(search_form); reset_form(voucher_form);
	search_form.payymdt = today; voucher_form.payymd = today;
	target_grid?.clearData(); method_grid?.clearData();
	total_target_pay_amt.value = 0; total_method_pay_amt.value = 0;
}

const add_method_row = () => {
	method_grid?.addRow({ select: true, acctnm: '', mgtno: '', custnm: search_form.custnm, remark: `${search_form.custnm} 지불`, amount: difference.value > 0 ? difference.value : 0, fixed: false, csubgbn: '' })
}

const modal_visible = _ref(false)
const modal_props = _reactive<modal_props_type>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function open_help(type: string, cell?: any) {
	if (type === 'SEARCH_CUST') {
		Object.assign(modal_props, {
			title: '거래처 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'c4', cmpycd: auth_store.cmpycd, gbncd: '', code: search_form.custnm },
			columns: [{ title: '코드', field: 'custcd', width: 80 }, { title: '거래처명', field: 'custnm', width: 180 }],
			onConfirm: (d: any) => { const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v])); search_form.custcd = item.custcd; search_form.custnm = item.custnm; search() }
		})
	} else if (type === 'DEPT') {
		Object.assign(modal_props, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'D0', cmpycd: auth_store.cmpycd, search: voucher_form.deptnm },
			columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v])); voucher_form.deptcd = item.deptcd; voucher_form.deptnm = item.deptnm }
		})
	} else if (type === 'METHOD_ACCT') {
		Object.assign(modal_props, {
			title: '계정 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'E1', cmpycd: auth_store.cmpycd, search: '300' },
			columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'paygbnm', width: 180 }],
			onConfirm: (d: any) => {
				const row = cell.getRow().getData(); const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
				row.cacctcd = item.acctcd; row.acctnm = item.paygbnm; row.cmgtgbn = item.typemgt; row.csubgbn = item.typesub;
				cell.getRow().update(row)
			}
		})
	} else if (type === 'METHOD_CUST') {
		const row = cell.getRow().getData(); const is_bank = row.csubgbn === '020'
		const is_cust = row.csubgbn === '010'
		Object.assign(modal_props, {
			title: is_bank ? '은행 선택' : '거래처 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: is_cust ? 'c4' : (is_bank ? 'C3' : 'C0'), cmpycd: auth_store.cmpycd, gbncd: is_cust ? '' : undefined, code: '' },
			columns: [{ title: '코드', field: is_bank ? 'bankcd' : 'custcd', width: 80 }, { title: '명칭', field: is_bank ? 'banknm' : 'custnm', width: 180 }],
			onConfirm: (d: any) => {
				const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
				row.custcd = is_bank ? item.bankcd : item.custcd; row.custnm = is_bank ? item.banknm : item.custnm
				cell.getRow().update(row)
			}
		})
	} else if (type === 'MGTNO') {
        const row = cell.getRow().getData()
        Object.assign(modal_props, {
            title: '관리번호 선택', path: '/api/ha00/HA00_00P_STR',
            data: { gubun: 'M0', cmpycd: auth_store.cmpycd, gbncd: row.cmgtgbn, search: '', remark: row.cacctcd },
            columns: [{ title: '관리번호', field: 'mgtno', width: 120 }, { title: '관리명칭', field: 'mgtnm', width: 180 }],
            onConfirm: (d: any) => { const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v])); row.mgtno = item.mgtno; cell.getRow().update(row) }
        })
    }
	modal_visible.value = true
}

_on_mounted(() => {
	if (target_grid_ref.value) {
		target_grid = new tabulator(target_grid_ref.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "선택", field: "select", width: 40, hozAlign: "center", formatter: "tickCross", editor: true, cellClick: (e, cell) => { cell.setValue(!cell.getValue()); update_target_total() } },
				{ title: "전표번호", field: "slip_key", width: 150, hozAlign: "center" },
				{ title: "적요", field: "remark", minWidth: 200 },
				{ title: "요청일", field: "reqymd", width: 150, hozAlign: "center", formatter: (c) => format_ymd(c.getValue()) },
				{ title: "잔액", field: "jan_amt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "지불액", field: "input_pay_amt", width: 150, hozAlign: "right", editor: "input", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-warning-subtle" }
			]
		})
		target_grid.on("cellEdited", update_target_total)
	}

	if (method_grid_ref.value) {
		method_grid = new tabulator(method_grid_ref.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "선택", field: "select", width: 40, hozAlign: "center", formatter: "tickCross", editor: true, cellClick: (e, cell) => { cell.setValue(!cell.getValue()); update_method_total() } },
				{ title: "계정과목", field: "acctnm", width: 200, cellClick: (e, cell) => { if(!cell.getData().fixed) open_help('METHOD_ACCT', cell) }, cssClass: "cursor-pointer" },
				{ title: "관리번호", field: "mgtno", width: 150, editor: "input", cellClick: (e, cell) => open_help('MGTNO', cell), cssClass: "cursor-pointer" },
				{ title: "거래처", field: "custnm", width: 250, cellClick: (e, cell) => open_help('METHOD_CUST', cell), cssClass: "cursor-pointer" },
				{ title: "적요", field: "remark", minWidth: 250, editor: "input" },
				{ title: "금액", field: "amount", width: 150, hozAlign: "right", editor: "input", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-info-subtle" },
				{ title: "", width: 40, formatter: (c) => c.getData().fixed ? "" : "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => { if(!cell.getData().fixed) cell.getRow().delete(); update_method_total() } }
			]
		})
		method_grid.on("cellEdited", update_method_total)
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.bg-warning-subtle { background-color: #fffcf0 !important; }
.bg-info-subtle { background-color: #f0f7ff !important; }
.btn-xs { padding: 1px 5px; font-size: 11px; }
.cursor-pointer { cursor: pointer; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
