<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Ïπ¥ÎìúÏßÄÎ∂àÏ≤òÎ¶?(HAFN620U)
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?§Î™Ö        : Ïπ¥Îìú?¨Î≥Ñ ÎØ∏Ï?Í∏??¥Ïó≠??Ï°∞Ìöå?òÏó¨ ?†ÌÉù Í≤∞Ï†ú Î∞??ÑÌëú Î∞úÌñâ (HSOD100U ?úÏ? ?îÏûê??Î∞??åÎ¨∏???êÏπô ?ÅÏö©)
	=============================================================
-->

<template>
  <app_alert :show="show_alert" :error="show_error" :message="alert_message" />
  <modal_component v-model:visible="modal_visible" :modalProps="modal_props" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- ?? 1. ?ÅÎã® ?°ÏÖò Î∞?-->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-credit-card me-2 text-primary" style="font-size: 18px;"></i>
        ?êÍ∏àÍ¥ÄÎ¶?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">Ïπ¥ÎìúÏßÄÎ∂àÏ≤òÎ¶?(HAFN620U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">Ï¥àÍ∏∞??/button>
        <button class="btn-erp btn-search" @click="search">Ï°∞Ìöå</button>
        <button class="btn-erp btn-save" @click="save" :disabled="selected_rows.length === 0">?Ä??(?ÑÌëúÎ∞úÌñâ)</button>
      </div>
    </div>

    <!-- ?í° 2. Î©îÏù∏ Ïª®ÌÖêÏ∏??ÅÏó≠ -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [?ÅÎã®] Ï°∞Ìöå ?ÑÌÑ∞ ?ÅÏó≠ (HSOD100U ?îÏûê???úÏ??? -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%;" /><col style="width: 23%;" />
              <col style="width: 10%;" /><col style="width: 23%;" />
              <col style="width: 10%;" /><col style="width: 24%;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light border-end">Í∏∞Ï??ºÏûê</th>
                <td class="border-end px-2">
                  <input v-model="search_form.todt" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
                </td>
                <th class="text-center bg-light border-end">Ïπ¥Îìú??/th>
                <td class="border-end px-2">
                  <div class="input-group input-group-sm">
                    <input v-model="search_form.custcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="search_form.custnm" type="text" class="form-control" placeholder="Ïπ¥Îìú???†ÌÉù" @keydown.enter="open_help('SEARCH_CUST')" />
                    <button class="btn btn-outline-secondary px-2" @click="open_help('SEARCH_CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light border-end">Ïπ¥ÎìúÎ≤àÌò∏</th>
                <td class="px-2">
                  <div class="input-group input-group-sm">
                    <input v-model="search_form.mgtno" type="text" class="form-control text-center bg-light" style="max-width: 150px;" readonly />
                    <input v-model="search_form.mgtnm" type="text" class="form-control" placeholder="Ïπ¥Îìú ?†ÌÉù" @keydown.enter="open_help('SEARCH_MGT')" />
                    <button class="btn btn-outline-secondary px-2" @click="open_help('SEARCH_MGT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [Ï§ëÍ∞Ñ] ÏßÄÎ∂?ÎßàÏä§???ïÎ≥¥ ?ÅÏó≠ (HSOD100U ?îÏûê???úÏ??? -->
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
                <th class="bg-light text-center border-end">Î∞úÌñâÎ∂Ä??/th>
                <td class="border-end px-2">
                  <div class="input-group input-group-sm">
                    <input v-model="voucher_form.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="voucher_form.deptnm" type="text" class="form-control" @keydown.enter="open_help('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="open_help('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light text-center border-end fw-bold text-info">ÏßÄÎ∂àÏùº??/th>
                <td class="border-end px-2">
                  <input v-model="voucher_form.payymd" type="date" class="form-control form-control-sm" />
                </td>
                <th class="bg-light text-center border-end text-primary fw-bold">ÏßÄÍ∏âÏàò?òÎ£å</th>
                <td class="px-2">
                  <input v-model="voucher_form.jiamt" type="number" class="form-control form-control-sm text-end fw-bold" @input="update_total_amount" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [?òÎã®] Í∑∏Î¶¨???ÅÏó≠ -->
      <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0 text-secondary">
          <span class="fw-bold small"><i class="bi bi-list-check me-1 text-primary"></i> Ïπ¥Îìú ÎØ∏Ï?Í∏??¥Ïó≠ Î¶¨Ïä§??/span>
          <div class="d-flex gap-3 align-items-center fw-bold">
            <span class="text-muted small">Í≤∞Ï†úÍµ¨Ï¢å: {{ voucher_form.gujano || 'Ï°∞Ìöå ???úÏãú' }}</span>
            <span class="text-primary">Ï¥?Í≤∞Ï†úÍ∏àÏï°: {{ format_money(total_pay_amt) }}</span>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="main_grid_ref" class="tabulator-instance flex-grow-1"></div>
        </div>
        <div class="card-footer p-0 border-top bg-dark text-white flex-shrink-0">
          <table class="table table-sm table-dark table-bordered mb-0 text-end fw-bold" style="font-size: 11px;">
            <tbody>
              <tr>
                <td class="text-center" style="width: 35%;">Ï∞??ÄÎ≥Ä ?©Í≥Ñ</td>
                <td style="width: 20%;" class="text-info">{{ format_money(total_pay_amt) }}</td>
                <td style="width: 20%;" class="text-warning">{{ format_money(total_pay_amt) }}</td>
                <td style="width: 25%;" class="text-success">?ºÏπò</td>
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
import { ref as _ref, reactive as _reactive, onMounted as _on_mounted, computed as _computed, watch as _watch, nextTick as _next_tick } from 'vue'
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
const first_day = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10)

// ?îç Í≤Ä??Ï°∞Í±¥ (?åÎ¨∏???êÏπô)
const search_form = _reactive({
	todt: today,
	custcd: '',
	custnm: '',
	mgtno: '',
	mgtnm: ''
})

// ?ìù ?ÑÌëú Î∞úÌñâ ?ïÎ≥¥ (?åÎ¨∏???êÏπô)
const voucher_form = _reactive({
	deptcd: auth_store.deptcd,
	deptnm: auth_store.deptnm,
	payymd: today,
	jiamt: 0,
	gujano: '', // Í≤∞Ï†úÍµ¨Ï¢å
	clsymd: '00000000'
})

const main_grid_ref = _ref<HTMLDivElement | null>(null)
let main_grid: tabulator | null = null
const selected_rows = _ref<any[]>([])

const total_pay_amt = _computed(() => {
	const subtotal = selected_rows.value.reduce((acc, row) => acc + (Number(row.janamt) || 0), 0)
	return subtotal + Number(voucher_form.jiamt || 0)
})

const format_money = (val: any) => Number(val || 0).toLocaleString()

const search = async () => {
	if (!search_form.custcd) return v_alert('Ïπ¥Îìú?¨Î? ?†ÌÉù?òÏã≠?úÏò§.')
	try {
		const res = await api.post('/api/hafn/HAFN_620U_STR', {
			actkind: 'S0',
			cmpycd: auth_store.cmpycd,
			fromdt: first_day.replace(/-/g, ''),
			todt: search_form.todt.replace(/-/g, ''),
			custcd: search_form.custcd,
			mgtno: search_form.mgtno
		})

		const data = (res.data || []).map((row: any) => {
            // ?åÎ¨∏???êÏπô: ???ïÍ∑ú??
            const item = Object.fromEntries(Object.entries(row).map(([k, v]) => [k.toLowerCase(), v]))
            return {
                ...item,
                slip_key: `${item.slipymd}-${item.slipno}-${item.srowno}`,
                cardno: item.mgtno,
                cardnm: item.cardnm,
                unpaid_amt: Number(item.upyamt || 0),
                paid_amt: Number(item.payamt || 0),
                janamt: Number(item.janamt || 0),
                gujano: item.gujano,
                SELECT: true
            }
        })

		if (data.length > 0) {
			voucher_form.gujano = data[0].gujano
		} else {
			voucher_form.gujano = ''
		}

		main_grid?.setData(data)
		update_total_amount()
		v_alert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { v_alert_error('Ï°∞Ìöå ?§Ìå®') }
}

const update_total_amount = () => {
	const data = main_grid?.getData() || []
	selected_rows.value = data.filter((r: any) => r.SELECT)
}

const save = async () => {
	if (voucher_form.payymd.replace(/-/g, '') <= voucher_form.clsymd) {
		return v_alert('?åÍ≥Ñ ÎßàÍ∞ê???ºÏûê?ÖÎãà??')
	}
	if (!voucher_form.gujano) return v_alert('Í≤∞Ï†úÍµ¨Ï¢å ?ïÎ≥¥Í∞Ä ?ÜÏäµ?àÎã§. (Í∏∞Ï¥à ?ïÎ≥¥ ?ïÏù∏ ?ÑÏöî)')

	if (!confirm('?†ÌÉù????™©?§Ïóê ?Ä??Í≤∞Ï†ú ?ÑÌëúÎ•?Î∞úÌñâ?òÏãúÍ≤†Ïäµ?àÍπå?')) return

	try {
		const details = []

		// 1. Ï∞®Î? (Debits): Ïπ¥Îìú ÎØ∏Ï?Í∏âÍ∏à Î∞òÏ†ú
		selected_rows.value.forEach(row => {
			details.push({
				upkind: 'A', dbcr: 'D',
				acctcd: row.acctcd,
				remark: `${search_form.custnm} Ïπ¥Îìú?ÄÍ∏?ÏßÄÎ∂?,
				amount: row.janamt,
				custcd: search_form.custcd,
				mgtno: row.cardno,
				sslipno: `${row.slipymd}${row.slipno}${row.srowno}`
			})
		})

		// ÏßÄÍ∏âÏàò?òÎ£å Ï∞®Î? Ï∂îÍ?
		if (voucher_form.jiamt > 0) {
			details.push({
				upkind: 'A', dbcr: 'D',
				acctcd: '6355',
				remark: 'Ïπ¥Îìú?ÄÍ∏??òÏàòÎ£?,
				amount: voucher_form.jiamt,
				custcd: search_form.custcd
			})
		}

		// 2. ?ÄÎ≥Ä (Credits): Î≥¥ÌÜµ?àÍ∏à Ï∂úÍ∏à
		details.push({
			upkind: 'A', dbcr: 'C',
			acctcd: '1120',
			remark: `${search_form.custnm} Ïπ¥Îìú ?ÄÍ∏àÏ?Î∂?,
			amount: total_pay_amt.value,
			mgtno: voucher_form.gujano
		})

		const payload = {
			actkind: 'A',
			MASTER: {
				cmpycd: auth_store.cmpycd,
				slipymd: voucher_form.payymd.replace(/-/g, ''),
				acctymd: voucher_form.payymd.replace(/-/g, ''),
				deptcd: voucher_form.deptcd,
				business: `${search_form.custnm}(${search_form.mgtno}) Ïπ¥Îìú Í≤∞Ï†ú Í±?,
				slipgu: '010'
			},
			DETAILS: details
		}

		const res = await api.post('/api/hasl/HASL_010U_SAVE', payload)
		v_alert('?ÑÌëúÍ∞Ä Î∞úÌñâ?òÏóà?µÎãà??')
		if (res.data.slipno) {
			window.open(`/api/hasl/HASL_SLIP_PRINT?slipgu=010&slipymd=${payload.MASTER.slipymd}&slipno=${res.data.slipno}&deptcd=${voucher_form.deptcd}`)
		}
		initialize()
	} catch (e) { v_alert_error('?Ä???§Ìå®') }
}

const initialize = () => {
	reset_form(search_form)
	reset_form(voucher_form)
	search_form.todt = today
	voucher_form.payymd = today
	voucher_form.deptcd = auth_store.deptcd
	voucher_form.deptnm = auth_store.deptnm
	main_grid?.clearData()
	selected_rows.value = []
}

// ?ùÏóÖ ?§Ï†ï
const modal_visible = _ref(false)
const modal_props = _reactive<modal_props_type>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function open_help(type: string) {
	if (type === 'SEARCH_CUST') {
		Object.assign(modal_props, {
			title: 'Ïπ¥Îìú???†ÌÉù', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'C3', custgbn: '020', cmpycd: auth_store.cmpycd, gbncd: '', search: '', remark: '' },
			columns: [{ title: 'ÏΩîÎìú', field: 'bankcd', width: 80 }, { title: 'Ïπ¥Îìú?¨Î™Ö', field: 'banknm', width: 180 }],
			onConfirm: (d: any) => {
                // ?í° 020 ?Ä?âÏùò Í≤ΩÏö∞ bankcd, banknm ?Ä??(?åÎ¨∏???êÏπô)
                const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
                search_form.custcd = item.bankcd; search_form.custnm = item.banknm; search_form.mgtno = ''; search_form.mgtnm = '';
            }
		})
	} else if (type === 'SEARCH_MGT') {
		if (!search_form.custcd) return v_alert('Ïπ¥Îìú?¨Î? Î®ºÏ? ?†ÌÉù?òÏã≠?úÏò§.')
		Object.assign(modal_props, {
			title: 'Ïπ¥ÎìúÎ≤àÌò∏ ?†ÌÉù', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'M0', cmpycd: auth_store.cmpycd, gbncd: '040', remark: search_form.custcd },
			columns: [{ title: 'Ïπ¥ÎìúÎ≤àÌò∏', field: 'mgtno', width: 150 }, { title: 'Ïπ¥ÎìúÎ™?, field: 'mgtnm', width: 150 }],
			onConfirm: (d: any) => {
                const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
                search_form.mgtno = item.mgtno; search_form.mgtnm = item.mgtnm
            }
		})
	} else if (type === 'DEPT') {
		Object.assign(modal_props, {
			title: 'Î∂Ä???†ÌÉù', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'D0', cmpycd: auth_store.cmpycd, search: voucher_form.deptnm },
			columns: [{ title: 'ÏΩîÎìú', field: 'deptcd', width: 80 }, { title: 'Î∂Ä?úÎ™Ö', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => {
                const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
                voucher_form.deptcd = item.deptcd; voucher_form.deptnm = item.deptnm
            }
		})
	}
	modal_visible.value = true
}

_on_mounted(() => {
	if (main_grid_ref.value) {
		main_grid = new tabulator(main_grid_ref.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "?†ÌÉù", field: "SELECT", width: 40, hozAlign: "center", formatter: "tickCross", editor: true, cellClick: (e, cell) => { cell.setValue(!cell.getValue()); update_total_amount() } },
				{
					title: "Î∞úÏÉù?ÑÌëúÎ≤àÌò∏", field: "slip_key", width: 130, hozAlign: "center",
					formatter: (cell) => { const d = cell.getData(); return `${d.slipymd}-${d.slipno}-${d.srowno}` }
				},
				{ title: "?ÅÏöî", field: "remark", minWidth: 200 },
				{ title: "Ïπ¥ÎìúÎ≤àÌò∏", field: "cardno", width: 150, hozAlign: "center" },
				{ title: "Ïπ¥ÎìúÎ™?, field: "cardnm", width: 120 },
				{ title: "ÎØ∏Ï?Î∂àÏï°", field: "unpaid_amt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "?îÏï°", field: "janamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold text-danger" }
			]
		})
	}

	// ÎßàÍ∞ê ?ïÎ≥¥ Î°úÎìú
	api.post('/api/ha00/HA00_010S_STR', { gubun: 'P1', cmpycd: auth_store.cmpycd }).then(res => {
		if (res.data && res.data.length > 0) {
            const item = Object.fromEntries(Object.entries(res.data[0]).map(([k, v]) => [k.toLowerCase(), v]))
			voucher_form.clsymd = item.clsymd || '00000000'
		}
	})
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.btn-xs { padding: 1px 5px; font-size: 11px; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
