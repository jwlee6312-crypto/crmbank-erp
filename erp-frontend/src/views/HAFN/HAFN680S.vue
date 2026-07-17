<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Ïπ¥ÎìúÎØ∏Ï?Í∏âÍ∏à?ÑÌô© (HAFN680S)
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?§Î™Ö        : Ïπ¥Îìú?¨Î≥Ñ Ïπ¥Îìú ÎØ∏Ï?Í∏âÍ∏à??Î∞úÏÉù, ÏßÄÎ∂? ?îÏï° ?ÑÌô© Ï°∞Ìöå (HSOD100U ?úÏ? ?îÏûê??Î∞??åÎ¨∏???êÏπô ?ÅÏö©)
	=============================================================
-->

<template>
  <app_alert :show="show_alert" :error="show_error" :message="alert_message" />
  <modal_component v-model:visible="modal_visible" :modalProps="modal_props" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- ?? 1. ?ÅÎã® ?°ÏÖò Î∞?-->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-credit-card-2-front me-2 text-primary" style="font-size: 18px;"></i>
        ?êÍ∏àÍ¥ÄÎ¶?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">Ïπ¥ÎìúÎØ∏Ï?Í∏âÍ∏à?ÑÌô© (HAFN680S)</span>
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
              <col style="width: 10%" /><col style="width: 55%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light border-end">Í∏∞Ï??ºÏûê</th>
                <td class="border-end px-2">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="search_form.ymd" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
                    <span class="small fw-bold text-secondary">?ÑÏû¨</span>
                  </div>
                </td>
                <th class="text-center bg-light border-end">Ïπ¥Îìú??/th>
                <td>
                  <div class="d-flex align-items-center gap-1 px-1">
                    <div class="input-group input-group-sm" style="width: 220px;">
                      <input v-model="search_form.custcdfr" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                      <input v-model="search_form.custnmfr" type="text" class="form-control" placeholder="?úÏûë Ïπ¥Îìú?? @keydown.enter="open_help('CUSTFR')" />
                      <button class="btn btn-outline-secondary px-2" @click="open_help('CUSTFR')"><i class="bi bi-search"></i></button>
                    </div>
                    <span class="text-muted">~</span>
                    <div class="input-group input-group-sm" style="width: 220px;">
                      <input v-model="search_form.custcdto" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                      <input v-model="search_form.custnmto" type="text" class="form-control" placeholder="Ï¢ÖÎ£å Ïπ¥Îìú?? @keydown.enter="open_help('CUSTTO')" />
                      <button class="btn btn-outline-secondary px-2" @click="open_help('CUSTTO')"><i class="bi bi-search"></i></button>
                    </div>
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
          <span class="fw-bold small"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> Ïπ¥Îìú?¨Î≥Ñ ÎØ∏Ï?Í∏??ÑÌô©</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="main_grid_ref" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>

  <modal_component v-model:visible="modal_visible" :modalProps="modal_props" />
</template>

<script setup lang="ts">
import { ref as _ref, reactive as _reactive, onMounted as _on_mounted } from 'vue'
import { TabulatorFull as tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts as use_alerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore as use_auth_store } from '@/stores/authStore'
import { useFormReset as use_form_reset } from '@/composables/useFormReset'
import { useRouter as use_router } from 'vue-router'
import { addDynamicRoute as add_dynamic_route } from '@/router/dynamicRoute'
import app_alert from '@/components/AppAlert.vue'
import modal_component from '@/components/Modal.vue'
import type { ModalProps as modal_props_type } from '@/types/modal'

const auth_store = use_auth_store()
const router = use_router()
const { showAlert: show_alert, showError: show_error, alertMessage: alert_message, vAlert: v_alert, vAlertError: v_alert_error } = use_alerts()
const { resetForm: reset_form } = use_form_reset()

const today = new Date().toISOString().substring(0, 10)

// ?îç Í≤Ä???∞Ïù¥??(?åÎ¨∏???êÏπô ?ÅÏö©)
const search_form = _reactive({
	ymd: today,
	custcdfr: '',
	custnmfr: '',
	custcdto: '',
	custnmto: ''
})

const main_grid_ref = _ref<HTMLDivElement | null>(null)
let main_grid: tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_680S_STR', {
			cmpycd: auth_store.cmpycd,
			ymd: search_form.ymd.replace(/-/g, ''),
			custcdfr: search_form.custcdfr,
			custcdto: search_form.custcdto
		})

		const data = (res.data || []).map((row: any) => {
            // ?åÎ¨∏???êÏπô: ???ïÍ∑ú??
            const item = Object.fromEntries(Object.entries(row).map(([k, v]) => [k.toLowerCase(), v]))
            return {
                ...item,
                upyamt: Number(item.upyamt || 0),
                payamt: Number(item.payamt || 0),
                janamt: Number(item.janamt || 0)
            }
        })

		main_grid?.setData(data)
		v_alert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { v_alert_error('Ï°∞Ìöå ?§Ìå®') }
}

const initialize = () => {
	reset_form(search_form)
	search_form.ymd = today
	main_grid?.clearData()
}

const excel = () => main_grid?.download("xlsx", `Ïπ¥ÎìúÎØ∏Ï?Í∏âÍ∏à?ÑÌô©_${today}.xlsx`)

const print = () => {
	const params = `ymd=${search_form.ymd}&custcdfr=${search_form.custcdfr}&custcdto=${search_form.custcdto}`
	window.open(`/api/hafn/HAFN_680P?${params}`, 'CardUnpaidStatusPrint', 'width=1000,height=800,scrollbars=yes')
}

/**
 * ?? ?ÅÏÑ∏ ?¥Îèô (?úÏ? ÎßÅÌÅ¨ Î∞©Ïãù ?ÅÏö©: HAFN660SÎ°??¥Îèô)
 */
const go_detail = (row: any) => {
    const pgmid = 'HAFN660S'
    add_dynamic_route(pgmid, 'Ïπ¥ÎìúÎØ∏Ï?Í∏âÏÉÅ?∏ÌòÑ??, 'HAFN')
	router.push({
		path: `/${pgmid}`,
		query: {
            ymd: search_form.ymd.replace(/-/g, ''),
            custcd: row.custcd,
            mgtno: row.mgtno,
            custnm: row.custnm
        }
	})
}

// ?ùÏóÖ ?§Ï†ï
const modal_visible = _ref(false)
const modal_props = _reactive<modal_props_type>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function open_help(type: string) {
	const is_fr = type === 'CUSTFR'
	Object.assign(modal_props, {
		title: 'Ïπ¥Îìú???†ÌÉù', path: '/api/ha00/HA00_00P_STR',
		data: { gubun: 'C3', custgbn: '020', cmpycd: auth_store.cmpycd, search: is_fr ? search_form.custnmfr : search_form.custnmto },
		columns: [{ title: 'ÏΩîÎìú', field: 'bankcd', width: 80,
		                     formatter: (cell: any) => {
                                 console.log('[HAFN680S ?ùÏóÖ Î°úÏö∞ ?∞Ïù¥??:', cell.getData());
                                 return cell.getValue();
                             }
		 }, { title: 'Ïπ¥Îìú?¨Î™Ö', field: 'banknm', width: 180 }],
		onConfirm: (d: any) => {
            const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
			if (is_fr) { search_form.custcdfr = item.bankcd; search_form.custnmfr = item.banknm }
			else { search_form.custcdto = item.bankcd; search_form.custnmto = item.banknm }
		}
	})
	modal_visible.value = true
}

_on_mounted(() => {
	if (main_grid_ref.value) {
		main_grid = new tabulator(main_grid_ref.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "Ïπ¥Îìú??, field: "custcd", width: 100, hozAlign: "center",
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer fw-bold">${cell.getValue()}</span>`,
					cellClick: (e, cell) => go_detail(cell.getData())
				},
				{
					title: "Ïπ¥Îìú?¨Î™Ö", field: "custnm", width: 200,
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</span>`,
					cellClick: (e, cell) => go_detail(cell.getData())
				},
				{
                    title: "Ïπ¥ÎìúÎ≤àÌò∏", field: "mgtno", width: 200,
                    formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</span>`,
					cellClick: (e, cell) => go_detail(cell.getData())
                },
				{ title: "Í≤∞Ï†ú?Ä??, field: "soname", minWidth: 200 },
				{
					title: "Î∞úÏÉù??, field: "upayamt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "ÏßÄÎ∂àÏï°", field: "payamt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "????, field: "janamt", width: 130, hozAlign: "right",
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
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
