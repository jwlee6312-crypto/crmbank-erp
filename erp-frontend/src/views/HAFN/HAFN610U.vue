<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ì§€ë¶ˆë“±ë¡?(HAFN610U)
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ê±°ë˜ì²˜ë³„ ë¯¸ì?ê¸??´ì—­???€??ì§€ë¶?ì²˜ë¦¬ ë°??„í‘œ ?ì„± (?Œë¬¸???ì¹™ ë°?HSOD100U ?”ì???ìš©)
	=============================================================
-->

<template>
	<app_alert :show="show_alert" :error="show_error" :message="alert_message" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- ?? 1. ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-wallet2 me-2 text-primary" style="font-size: 18px;"></i>
				?ê¸ˆê´€ë¦?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">ì§€ë¶ˆë“±ë¡?(hafn610u)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">ì´ˆê¸°??/button>
				<button class="btn-erp btn-search" @click="search">ì¡°íšŒ</button>
				<button class="btn-erp btn-save" @click="save" :disabled="!can_save">ì§€ë¶ˆí™•??/button>
			</div>
		</div>

		<!-- ?’¡ 2. ë©”ì¸ ì»¨í…ì¸??ì—­ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- [?ë‹¨] ì¡°íšŒ ?„í„° ?ì—­ (HSOD100U ?œì??? -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense" style="table-layout: fixed;">
						<colgroup>
							<col style="width: 100px;" /><col style="width: 300px;" />
							<col style="width: 100px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="text-center bg-light border-end">ì§€ë¶ˆì²˜</th>
								<td class="bg-white border-end px-2">
									<div class="input-group input-group-sm">
										<input v-model="search_form.custcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
										<input v-model="search_form.custnm" type="text" class="form-control" placeholder="ê±°ë˜ì²?ê²€?? @keydown.enter="open_help('SEARCH_CUST')" />
										<button class="btn btn-outline-secondary px-2" @click="open_help('SEARCH_CUST')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="text-center bg-light border-end">ê¸°ì??¼ì</th>
								<td class="bg-white px-2">
									<input v-model="search_form.todt" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- [ì¤‘ê°„] ì§€ë¶?ë§ˆìŠ¤???•ë³´ ?ì—­ (HSOD100U ?œì??? -->
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
								<th class="bg-light text-center border-end">ì§€ë¶ˆì¼</th>
								<td class="border-end px-2">
									<input v-model="voucher_form.payymd" type="date" class="form-control form-control-sm" />
								</td>
								<th class="bg-light text-center border-end text-primary fw-bold">?˜ìˆ˜ë£?/th>
								<td class="border-end px-2">
									<input v-model="voucher_form.jiamt" type="number" class="form-control form-control-sm text-end" />
								</td>
								<th class="bg-light text-center border-end text-danger fw-bold">ê°€?°ì„¸</th>
								<td class="px-2">
									<input v-model="voucher_form.gaamt" type="number" class="form-control form-control-sm text-end" />
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- [?˜ë‹¨ ê·¸ë¦¬??1] ì§€ë¶??€??(ë¯¸ì?ê¸??´ì—­) -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white" style="flex-basis: 40%;">
				<div class="card-header py-1 px-3 bg-white d-flex justify-content-between align-items-center border-bottom">
					<span class="fw-bold small text-dark"><i class="bi bi-list-task me-2 text-primary"></i>1. ì§€ë¶??€??(ë¯¸ì?ê¸??´ì—­)</span>
					<span class="badge bg-primary">ì§€ë¶??€??ì´ì•¡: {{ format_money(total_target_pay_amt) }}</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
					<div ref="target_grid_ref" class="tabulator-instance flex-grow-1"></div>
				</div>
			</div>

			<!-- [?˜ë‹¨ ê·¸ë¦¬??2] ì§€ë¶?ë°©ë²• -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-3 bg-white d-flex justify-content-between align-items-center border-bottom text-dark">
					<div class="d-flex gap-4 align-items-center">
						<span class="fw-bold small"><i class="bi bi-credit-card-2-back me-2 text-success"></i>2. ì§€ë¶?ë°©ë²• (ì¶œê¸ˆ/?´ìŒ ??</span>
					</div>
					<div class="d-flex gap-2 align-items-center">
						<span class="badge" :class="is_balanced ? 'bg-success' : 'bg-danger'">
							ì°¨ì•¡: {{ format_money(difference) }}
						</span>
						<button class="btn btn-xs btn-outline-primary py-0 fw-bold" @click="add_method_row">+ ë°©ë²•ì¶”ê?</button>
					</div>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
					<div ref="method_grid_ref" class="tabulator-instance flex-grow-1"></div>
				</div>
				<div class="card-footer p-0 border-top bg-dark text-white">
					<table class="table table-sm table-dark table-bordered mb-0 text-end fw-bold" style="font-size: 11px;">
						<tbody>
							<tr>
								<td class="text-center" style="width: 25%;">ì§€ë¶ˆë???ë¯¸ì?ê¸??˜ìˆ˜ë£???</td>
								<td style="width: 20%;" class="text-info">{{ format_money(total_required_amt) }}</td>
								<td class="text-center" style="width: 20%;">ì§€ë¶ˆìˆ˜???©ê³„</td>
								<td style="width: 20%;" class="text-warning">{{ format_money(total_method_pay_amt) }}</td>
								<td class="text-center" style="width: 15%;">? ê¸‰ê¸?ë°œìƒ: {{ format_money(prepayment_amt) }}</td>
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

// ?” ? ì§œ ?¬ë§· ?¬í¼
const format_ymd = (v: string) => (v && v.length === 8) ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v

// ?” ê²€??ë°?ë§ˆìŠ¤???°ì´??(?Œë¬¸???ì¹™)
const search_form = _reactive({
	todt: today,
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
	if (!search_form.custcd) return v_alert('ê±°ë˜ì²˜ë? ? íƒ?˜ì‹­?œì˜¤.')

	try {
		const res_target = await api.post('/api/hafn/HAFN_610U_STR', {
			actkind: 'S0',
			cmpycd: auth_store.cmpycd,
			fromdt: first_day.replace(/-/g, ''),
			todt: search_form.todt.replace(/-/g, ''),
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
                acctnm: 'ë°›ì„?´ìŒ',
                cpaycndt: item.cpaycndt,
                mgtno: item.billno,
                custnm: item.custnm,
                custcd: item.custcd,
                remark: `${search_form.custnm} ?¸ìƒê¸?ì§€ë¶?,
                amount: Number(item.billamt || 0),
                fixed: true,
                cmgtgbn: '050',
                cacctcd: '1120',
                csubgbn: '010'
            }
        })
		method_grid?.setData(method_data)
		update_method_total()

	} catch (e) { v_alert_error('ì¡°íšŒ ?¤íŒ¨') }
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
	if (!is_balanced.value && prepayment_amt.value === 0) return v_alert('ê¸ˆì•¡ ì°¨ì•¡??ë°œìƒ?ˆìŠµ?ˆë‹¤.')
	if (!confirm('ì§€ë¶??•ì • ì²˜ë¦¬ë¥??˜ì‹œê² ìŠµ?ˆê¹Œ?')) return

	try {
		const selected_targets = target_grid?.getData().filter((r: any) => r.select) || []
		const selected_methods = method_grid?.getData().filter((r: any) => r.select) || []

		const details = []
		selected_targets.forEach(row => {
			details.push({ upkind: 'A', dbcr: 'D', acctcd: row.acctcd, remark: `${search_form.custnm} ?¸ìƒê¸?ì§€ë¶?, amount: row.input_pay_amt, custcd: search_form.custcd, sslipno: `${row.slipymd}${row.slipno}${row.srowno}` })
		})
		if (voucher_form.jiamt > 0) details.push({ upkind: 'A', dbcr: 'D', acctcd: '6355', remark: '?¡ê¸ˆ?˜ìˆ˜ë£?, amount: voucher_form.jiamt, custcd: search_form.custcd })
		if (voucher_form.gaamt > 0) details.push({ upkind: 'A', dbcr: 'D', acctcd: '6250', remark: 'ê°€?°ì„¸', amount: voucher_form.gaamt, custcd: search_form.custcd })

		selected_methods.forEach(row => {
			details.push({ upkind: 'A', dbcr: 'C', acctcd: row.cacctcd, remark: row.remark, amount: row.amount, mgtno: row.mgtno, custcd: row.custcd || search_form.custcd })
		})
		if (prepayment_amt.value > 0) details.push({ upkind: 'A', dbcr: 'D', acctcd: '1265', remark: '? ê¸‰ê¸?ë°œìƒ', amount: prepayment_amt.value, custcd: search_form.custcd })

		const payload = {
			actkind: 'A',
			master: { cmpycd: auth_store.cmpycd, slipymd: voucher_form.payymd.replace(/-/g, ''), acctymd: voucher_form.payymd.replace(/-/g, ''), deptcd: voucher_form.deptcd, business: `${search_form.custnm} ì§€ë¶ˆê±´`, slipgu: '010' },
			details: details
		}

		const res = await api.post('/api/hasl/HASL_010U_SAVE', payload)
		v_alert('?±ê³µ?ìœ¼ë¡?ì²˜ë¦¬?˜ì—ˆ?µë‹ˆ??')
		if (res.data.slipno) window.open(`/api/hasl/HASL_SLIP_PRINT?slipgu=010&slipymd=${payload.master.slipymd}&slipno=${res.data.slipno}&deptcd=${voucher_form.deptcd}`)
		initialize()
	} catch (e) { v_alert_error('?€???¤íŒ¨') }
}

const initialize = () => {
	reset_form(search_form); reset_form(voucher_form);
	search_form.todt = today; voucher_form.payymd = today;
	target_grid?.clearData(); method_grid?.clearData();
	total_target_pay_amt.value = 0; total_method_pay_amt.value = 0;
}

const add_method_row = () => {
	method_grid?.addRow({ select: true, acctnm: '', mgtno: '', custnm: search_form.custnm, remark: `${search_form.custnm} ì§€ë¶?, amount: difference.value > 0 ? difference.value : 0, fixed: false, csubgbn: '' })
}

const modal_visible = _ref(false)
const modal_props = _reactive<modal_props_type>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function open_help(type: string, cell?: any) {
	if (type === 'SEARCH_CUST') {
		Object.assign(modal_props, {
			title: 'ê±°ë˜ì²?? íƒ', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'c4', cmpycd: auth_store.cmpycd, gbncd: '', code: search_form.custnm },
			columns: [{ title: 'ì½”ë“œ', field: 'custcd', width: 80 }, { title: 'ê±°ë˜ì²˜ëª…', field: 'custnm', width: 180 }],
			onConfirm: (d: any) => { const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v])); search_form.custcd = item.custcd; search_form.custnm = item.custnm; search() }
		})
	} else if (type === 'DEPT') {
		Object.assign(modal_props, {
			title: 'ë¶€??? íƒ', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'D0', cmpycd: auth_store.cmpycd, search: voucher_form.deptnm },
			columns: [{ title: 'ì½”ë“œ', field: 'deptcd', width: 80 }, { title: 'ë¶€?œëª…', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v])); voucher_form.deptcd = item.deptcd; voucher_form.deptnm = item.deptnm }
		})
	} else if (type === 'METHOD_ACCT') {
		Object.assign(modal_props, {
			title: 'ê³„ì • ? íƒ', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'E1', cmpycd: auth_store.cmpycd, search: '300' },
			columns: [{ title: 'ì½”ë“œ', field: 'acctcd', width: 80 }, { title: 'ê³„ì •ëª?, field: 'paygbnm', width: 180 }],
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
			title: is_bank ? '?€??? íƒ' : 'ê±°ë˜ì²?? íƒ', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: is_cust ? 'c4' : (is_bank ? 'C3' : 'C0'), cmpycd: auth_store.cmpycd, gbncd: is_cust ? '' : undefined, code: '' },
			columns: [{ title: 'ì½”ë“œ', field: is_bank ? 'bankcd' : 'custcd', width: 80 }, { title: 'ëª…ì¹­', field: is_bank ? 'banknm' : 'custnm', width: 180 }],
			onConfirm: (d: any) => {
				const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
				row.custcd = is_bank ? item.bankcd : item.custcd; row.custnm = is_bank ? item.banknm : item.custnm
				cell.getRow().update(row)
			}
		})
	} else if (type === 'MGTNO') {
        const row = cell.getRow().getData()
        Object.assign(modal_props, {
            title: 'ê´€ë¦¬ë²ˆ??? íƒ', path: '/api/ha00/HA00_00P_STR',
            data: { gubun: 'M0', cmpycd: auth_store.cmpycd, gbncd: row.cmgtgbn, search: '', remark: row.cacctcd },
            columns: [{ title: 'ê´€ë¦¬ë²ˆ??, field: 'mgtno', width: 120 }, { title: 'ê´€ë¦¬ëª…ì¹?, field: 'mgtnm', width: 180 }],
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
				{ title: "? íƒ", field: "select", width: 40, hozAlign: "center", formatter: "tickCross", editor: true, cellClick: (e, cell) => { cell.setValue(!cell.getValue()); update_target_total() } },
				{ title: "?„í‘œë²ˆí˜¸", field: "slip_key", width: 150, hozAlign: "center" },
				{ title: "?ìš”", field: "remark", minWidth: 200 },
				{ title: "?”ì²­??, field: "reqymd", width: 150, hozAlign: "center", formatter: (c) => format_ymd(c.getValue()) },
				{ title: "?”ì•¡", field: "jan_amt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "ì§€ë¶ˆì•¡", field: "input_pay_amt", width: 150, hozAlign: "right", editor: "input", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-warning-subtle" }
			]
		})
		target_grid.on("cellEdited", update_target_total)
	}

	if (method_grid_ref.value) {
		method_grid = new tabulator(method_grid_ref.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "? íƒ", field: "select", width: 40, hozAlign: "center", formatter: "tickCross", editor: true, cellClick: (e, cell) => { cell.setValue(!cell.getValue()); update_method_total() } },
				{ title: "ê³„ì •ê³¼ëª©", field: "acctnm", width: 200, cellClick: (e, cell) => { if(!cell.getData().fixed) open_help('METHOD_ACCT', cell) }, cssClass: "cursor-pointer" },
				{ title: "ê´€ë¦¬ë²ˆ??, field: "mgtno", width: 150, editor: "input", cellClick: (e, cell) => open_help('MGTNO', cell), cssClass: "cursor-pointer" },
				{ title: "ê±°ë˜ì²?, field: "custnm", width: 250, cellClick: (e, cell) => open_help('METHOD_CUST', cell), cssClass: "cursor-pointer" },
				{ title: "?ìš”", field: "remark", minWidth: 250, editor: "input" },
				{ title: "ê¸ˆì•¡", field: "amount", width: 150, hozAlign: "right", editor: "input", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-info-subtle" },
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
