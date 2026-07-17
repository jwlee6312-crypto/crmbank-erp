<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Ïπ¥ÎìúÎØ∏Ï?Í∏??ÅÍ≥Ñ?ÑÌô© (HAFN690S)
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?§Î™Ö        : Ïπ¥Îìú?¨Î≥Ñ Ïπ¥Îìú ÎØ∏Ï?Í∏âÍ∏à??Î∞úÏÉù, ÏßÄÎ∂? ?îÏï° ?ÑÌô© Ï°∞Ìöå (HAFN680S ?úÏ? Î°úÏßÅ ?ÅÏö© Î∞??åÎ¨∏???êÏπô Ï§Ä??
	=============================================================
-->

<template>
  <app_alert :show="show_alert" :error="show_error" :message="alert_message" />
  <modal_component v-model:visible="modal_visible" :modalProps="modal_props" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- ?? 1. ?ÅÎã® ?°ÏÖò Î∞?-->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-arrow-left-right me-2 text-primary" style="font-size: 18px;"></i>
        ?êÍ∏àÍ¥ÄÎ¶?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">Ïπ¥ÎìúÎØ∏Ï?Í∏??ÅÍ≥Ñ?ÑÌô© (hafn690s)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">Ï¥àÍ∏∞??/button>
        <button class="btn-erp btn-search" @click="search">Ï°∞Ìöå</button>
        <button class="btn-erp btn-print" @click="print">?∏ÏáÑ</button>
        <button class="btn-erp btn-excel" @click="excel">?ëÏ?</button>
      </div>
    </div>

    <!-- ?í° 2. Î©îÏù∏ Ïª®ÌÖêÏ∏??ÅÏó≠ -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [?ÅÎã®] Ï°∞Ìöå ?ÑÌÑ∞ ?ÅÏó≠ (HSOD100U ?úÏ??? -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" /><col style="width: 25%" />
              <col style="width: 10%" /><col style="width: 25%" />
              <col style="width: 10%" /><col style="width: 20%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light border-end">?ÅÍ≥Ñ?ºÏûê</th>
                <td class="border-end px-2">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="search_form.ymd_fr" type="date" class="form-control form-control-sm" />
                    <span class="text-muted">~</span>
                    <input v-model="search_form.ymd_to" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th class="text-center bg-light border-end">Ïπ¥Îìú??/th>
                <td class="border-end px-2">
                  <div class="input-group input-group-sm">
                    <input v-model="search_form.custcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="search_form.custnm" type="text" class="form-control" placeholder="Ïπ¥Îìú???†ÌÉù" @keydown.enter="open_help('cust')" />
                    <button class="btn btn-outline-secondary px-2" @click="open_help('cust')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light border-end">Ïπ¥ÎìúÎ≤àÌò∏</th>
                <td class="px-2">
                  <div class="input-group input-group-sm">
                    <input v-model="search_form.mgtno" type="text" class="form-control text-center bg-light" style="max-width: 120px;" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="open_help('mgt')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [?òÎã®] Í∑∏Î¶¨???ÅÏó≠ -->
      <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom text-secondary">
          <span class="fw-bold small"><i class="bi bi-list-columns-reverse me-2 text-primary"></i> Ïπ¥Îìú?¨Î≥Ñ ?ÅÍ≥Ñ ?¥Ïó≠ Î¶¨Ïä§??/span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="main_grid_ref" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref as _ref, reactive as _reactive, onMounted as _on_mounted } from 'vue'
import { TabulatorFull as tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts as use_alerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore as use_auth_store } from '@/stores/authStore'
import { useFormReset as use_form_reset } from '@/composables/useFormReset'
import { useRouter as use_router, useRoute as use_route } from 'vue-router'
import { addDynamicRoute as add_dynamic_route } from '@/router/dynamicRoute'
import app_alert from '@/components/AppAlert.vue'
import modal_component from '@/components/Modal.vue'
import type { ModalProps as modal_props_type } from '@/types/modal'

const auth_store = use_auth_store()
const router = use_router()
const route = use_route()
const { showAlert: show_alert, showError: show_error, alertMessage: alert_message, vAlert: v_alert, vAlertError: v_alert_error } = use_alerts()
const { resetForm: reset_form } = use_form_reset()

const today = new Date().toISOString().substring(0, 10)
const first_day = today.substring(0, 8) + '01'

const search_form = _reactive({
	ymd_fr: (route.query.ymd_fr as string) || first_day,
	ymd_to: (route.query.ymd_to as string) || today,
	custcd: (route.query.custcd as string) || '',
	custnm: (route.query.custnm as string) || '',
	mgtno: (route.query.mgtno as string) || ''
})

const main_grid_ref = _ref<HTMLDivElement | null>(null)
let main_grid: tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_690S_STR', {
			cmpycd: auth_store.cmpycd,
			ymd_fr: search_form.ymd_fr.replace(/-/g, ''),
			ymd_to: search_form.ymd_to.replace(/-/g, ''),
			custcdfr: search_form.custcd,
			custcdto: search_form.custcd,
			mgtno: search_form.mgtno
		})

		const data = (res.data || []).map((row: any) => {
            const item = Object.fromEntries(Object.entries(row).map(([k, v]) => [k.toLowerCase(), v]))
            return {
                ...item,
                upyamt: Number(item.upyamt || 0),
                payamt: Number(item.payamt || 0),
                janamt: Number(item.janamt || 0),
                slip_no_full: `${item.slipymd}-${item.slipno}-${item.srowno}`
            }
        })
		main_grid?.setData(data)
		v_alert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { v_alert_error('Ï°∞Ìöå ?§Ìå®') }
}

const initialize = () => {
	reset_form(search_form); search_form.ymd_fr = first_day; search_form.ymd_to = today; main_grid?.clearData();
}

const excel = () => main_grid?.download("xlsx", `Ïπ¥Îìú?ÅÍ≥Ñ?ÑÌô©_${today}.xlsx`)
const print = () => {
	const params = new URLSearchParams({ ymd_fr: search_form.ymd_fr.replace(/-/g, ''), ymd_to: search_form.ymd_to.replace(/-/g, ''), custcd: search_form.custcd, custnm: search_form.custnm, mgtno: search_form.mgtno }).toString()
	window.open(`/api/hafn/HAFN_690P?${params}`, 'Print', 'width=1000,height=800,scrollbars=yes')
}

const go_slip_detail = (row: any) => {
    const pgmid = 'HASL110U'; add_dynamic_route(pgmid, '?ÑÌëúÍ¥ÄÎ¶?, 'HASL')
	router.push({ path: `/${pgmid}`, query: { slipymd: row.slipymd, slipno: row.slipno } })
}

const print_slip = (row: any) => {
    const params = `slipgu=020&slipymd=${row.slipymd}&slipno=${row.slipno}&deptcd=${auth_store.deptcd}`
    window.open(`/api/hasl/HASL_SLIP_G_PRINT?${params}`, 'SlipPrint', 'width=750,height=650')
}

const modal_visible = _ref(false)
const modal_props = _reactive<modal_props_type>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function open_help(type: string) {
	if (type === 'cust') {
		Object.assign(modal_props, {
			title: 'Ïπ¥Îìú???†ÌÉù', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'C3', custgbn: '020', cmpycd: auth_store.cmpycd, search: search_form.custnm },
			// ?í° HAFN680S?Ä ?ÑÎìúÎ™ÖÏùÑ ?ôÏùº?òÍ≤å ?ºÏπò?úÏºú ?∞Ïù¥??Î≥¥Ïù¥ÏßÄ ?äÎäî Î¨∏Ï†ú ?¥Í≤∞
			columns: [{ title: 'ÏΩîÎìú', field: 'bankcd', width: 80 }, { title: 'Ïπ¥Îìú?¨Î™Ö', field: 'banknm', width: 180 }],
			onConfirm: (d: any) => {
                const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
                search_form.custcd = item.bankcd; search_form.custnm = item.banknm
            }
		})
	} else if (type === 'mgt') {
		Object.assign(modal_props, {
			title: 'Ïπ¥ÎìúÎ≤àÌò∏ ?†ÌÉù', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'M0', gbncd: '040', remark: search_form.custcd, cmpycd: auth_store.cmpycd, search: '' },
			columns: [{ title: 'Ïπ¥ÎìúÎ≤àÌò∏', field: 'mgtno', width: 120 }, { title: 'Ïπ¥ÎìúÎ™?, field: 'mgtnm', width: 150 }],
			onConfirm: (d: any) => {
                const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
                search_form.mgtno = item.mgtno
            }
		})
	}
	modal_visible.value = true
}

_on_mounted(() => {
	if (main_grid_ref.value) {
		main_grid = new tabulator(main_grid_ref.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "?ÅÍ≥Ñ?ÑÌëú", field: "slip_no_full", width: 150, hozAlign: "center", formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer fw-bold">${cell.getValue()}</span>`, cellClick: (e, cell) => go_slip_detail(cell.getData()) },
				{ title: "?ÅÏöî", field: "remark", minWidth: 250, formatter: (cell) => `<span class="text-dark text-decoration-underline cursor-pointer">${cell.getValue()}</span>`, cellClick: (e, cell) => print_slip(cell.getData()) },
				{ title: "Ïπ¥Îìú??, field: "custnm", width: 150 },
				{ title: "Ïπ¥ÎìúÎ≤àÌò∏", field: "cardno", width: 150, hozAlign: "center" },
				{ title: "Í≤∞Ï†ú?Ä??, field: "banknm", width: 150 },
				{ title: "Î∞úÏÉù??, field: "upyamt", width: 130, hozAlign: "right", formatter: "money" },
				{ title: "ÏßÄÎ∂àÏï°", field: "payamt", width: 130, hozAlign: "right", formatter: "money" },
				{ title: "????, field: "janamt", width: 130, hozAlign: "right", formatter: "money", cssClass: "fw-bold text-danger" }
			]
		})
	}
    if (search_form.custcd) search()
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.cursor-pointer { cursor: pointer; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>