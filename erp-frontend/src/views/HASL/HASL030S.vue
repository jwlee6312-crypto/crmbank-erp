<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Î∂ÑÍ∞ú???ÑÏóÖ) (HASL_030S)
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : Î∂Ä?úÎ≥Ñ/Í∏∞Í∞ÑÎ≥??ÑÌëú Î∂ÑÍ∞ú ?¥Ïó≠ ?ÅÏÑ∏ Ï°∞Ìöå (?åÎ¨∏???êÏπô Ï§Ä?? ?ºÏö∞???òÏ†ï, Ï§ëÎ≥µ Î∞©Ï? Î∞?Î∂ÑÎ¶¨ ÎßÅÌÅ¨)
	=============================================================
-->

<template>
	<app_alert :show="show_alert" :error="show_error" :message="alert_message" />

	<div class="erp-container">
		<!-- ?? ?ÅÎã® ?°ÏÖò Î∞?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journals me-2 text-primary" style="font-size: 18px;"></i>
				?ÑÌëúÍ¥ÄÎ¶?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">Î∂ÑÍ∞ú???ÑÏóÖ) (HASL_030S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> Ï¥àÍ∏∞??
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> Ï°∞Ìöå
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> ?ëÏ?
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> ?∏ÏáÑ
				</button>
			</div>
		</div>

		<!-- ?îç Í≤Ä????™© ?ÅÏó≠ -->
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
									<span class="erp-label me-2">Î∞úÌñâÎ∂Ä??/span>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="searchform.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchform.deptnm" type="text" class="form-control" placeholder="Î∂Ä???†ÌÉù" @keydown.enter="open_help('dept')" />
										<button class="btn btn-outline-secondary px-2" @click="open_help('dept')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">Î∞úÌñâ?ºÏûê</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1">
										<input v-model="searchform.fromdt" type="date" class="form-control form-control-sm" />
										<span class="text-muted">~</span>
										<input v-model="searchform.todt" type="date" class="form-control form-control-sm" />
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">Í≥ÑÏ†ïÍ≥ºÎ™©</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1">
										<div class="input-group input-group-sm flex-nowrap">
											<input v-model="searchform.acctcd1" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
											<input v-model="searchform.acctnm1" type="text" class="form-control" @keydown.enter="open_help('acct1')" />
											<button class="btn btn-outline-secondary px-2" @click="open_help('acct1')"><i class="bi bi-search"></i></button>
										</div>
										<span class="text-muted">~</span>
										<div class="input-group input-group-sm flex-nowrap">
											<input v-model="searchform.acctcd2" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
											<input v-model="searchform.acctnm2" type="text" class="form-control" @keydown.enter="open_help('acct2')" />
											<button class="btn btn-outline-secondary px-2" @click="open_help('acct2')"><i class="bi bi-search"></i></button>
										</div>
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- ?ìä Ï§ëÏïô Í∑∏Î¶¨???ÅÏó≠ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="maingridref" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>

	<modal_component v-model:visible="modalvisible" :modalProps="modalprops" />
</template>

<script setup lang="ts">
import { ref as _ref, reactive as _reactive, onMounted as _on_mounted, nextTick as _next_tick } from 'vue'
import { TabulatorFull as tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import * as XLSX from 'xlsx'
import { useAlerts as use_alerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore as use_auth_store } from '@/stores/authStore'
import { useFormReset as use_form_reset } from '@/composables/useFormReset'
import { useRouter as use_router } from 'vue-router'
import { addDynamicRoute as add_dynamic_route } from '@/router/dynamicRoute'
import modal_component from '@/components/Modal.vue'
import app_alert from '@/components/AppAlert.vue'
import type { ModalProps as modal_props_type } from '@/types/modal'

const authstore = use_auth_store()
const router = use_router()
const { showAlert: show_alert, showError: show_error, alertMessage: alert_message, vAlert: v_alert, vAlertError: v_alert_error } = use_alerts()
const { resetForm: reset_form } = use_form_reset()

const now = new Date()
const firstday = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const today = now.toISOString().substring(0, 10)

const searchform = _reactive({
	deptcd: authstore.deptcd,
	deptnm: authstore.deptnm,
	fromdt: firstday,
	todt: today,
	acctcd1: '',
	acctnm1: '',
	acctcd2: '',
	acctnm2: ''
})

const maingridref = _ref<HTMLDivElement | null>(null)
let maingrid: tabulator | null = null

const search = async () => {
	if (!searchform.deptcd) return v_alert('Î∞úÌñâÎ∂Ä?úÎ? ?†ÌÉù??Ï£ºÏã≠?úÏò§.')

	try {
		const res = await api.post('/api/hasl/HASL_030S_STR', {
			cmpycd: authstore.cmpycd,
			deptcd: searchform.deptcd,
			fromdt: searchform.fromdt.replace(/-/g, ''),
			todt: searchform.todt.replace(/-/g, ''),
			acctcd1: searchform.acctcd1,
			acctcd2: searchform.acctcd2,
			actkind: 'sr'
		})

		// ?í° ?åÎ¨∏???êÏπô: API ?∞Ïù¥?∞Î? Ï¶âÏãú ?åÎ¨∏???§Î°ú ?ïÍ∑ú??
		const data = (res.data || []).map((item: any) => {
			return Object.fromEntries(
				Object.entries(item).map(([k, v]) => [k.toLowerCase(), v])
			)
		})

		maingrid?.setData(data)
		v_alert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { v_alert_error('Ï°∞Ìöå ?§Ìå®') }
}

const initialize = () => {
	reset_form(searchform)
	searchform.deptcd = authstore.deptcd
	searchform.deptnm = authstore.deptnm
	searchform.fromdt = firstday
	searchform.todt = today
	maingrid?.clearData()
}

const excel = () => {
	if (!maingrid) return v_alert_error('Ï°∞Ìöå???∞Ïù¥?∞Í? ?ÜÏäµ?àÎã§.')
	maingrid.download("xlsx", `Î∂ÑÍ∞ú??${searchform.fromdt}_${searchform.todt}.xlsx`, { sheetName: 'Î∂ÑÍ∞ú?? })
}
const print = () => {
	const params = new URLSearchParams(searchform).toString().toLowerCase()
	window.open(`/api/hasl/HASL_030P?${params}`, 'Print', 'width=1000,height=800')
}

// ?ùÏóÖ ?§Ï†ï
const modalvisible = _ref(false)
const modalprops = _reactive<modal_props_type>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function open_help(type: string) {
	if (type === 'dept') {
		Object.assign(modalprops, {
			title: 'Î∂Ä???†ÌÉù', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
			data: { gubun: 'D0', cmpycd: authstore.cmpycd, code: searchform.deptnm },
			columns: [{ title: 'ÏΩîÎìú', field: 'deptcd', width: 80 }, { title: 'Î∂Ä?úÎ™Ö', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { searchform.deptcd = d.deptcd; searchform.deptnm = d.deptnm }
		})
	} else if (type.startsWith('acct')) {
		Object.assign(modalprops, {
			title: 'Í≥ÑÏ†ïÍ≥ºÎ™© ?†ÌÉù', path: '/api/ha00/HA00_00P_STR', defaultField: 'acctnm',
			data: { gubun: 'A0', cmpycd: authstore.cmpycd, search: type === 'acct1' ? searchform.acctnm1 : searchform.acctnm2 },
			columns: [{ title: 'ÏΩîÎìú', field: 'acctcd', width: 80 }, { title: 'Í≥ÑÏ†ïÎ™?, field: 'acctnm', width: 180 }],
			onConfirm: (d: any) => {
				if (type === 'acct1') { searchform.acctcd1 = d.acctcd; searchform.acctnm1 = d.acctnm }
				else { searchform.acctcd2 = d.acctcd; searchform.acctnm2 = d.acctnm }
			}
		})
	}
	modalvisible.value = true
}

/**
 * ?? ?ÑÌëú ?¥Îèô (?ºÏö∞??Í≤ΩÎ°ú ?òÏ†ï Î∞?Î∂ÑÎ¶¨ Ï≤òÎ¶¨)
 */
const go_to_slip = (slipkey: string) => {
	if (!slipkey) return;
	let ymd = ""; let no = "";

	// ?í° ?ÑÌëúÎ≤àÌò∏ (20260601-001) Î∂ÑÎ¶¨ Ï≤òÎ¶¨
	if (slipkey.includes('-')) {
		const parts = slipkey.split('-');
		ymd = parts[0]; no = parts[1];
	} else if (slipkey.length >= 11) {
		ymd = slipkey.substring(0, 8); no = slipkey.substring(8);
	} else return;

	// ?í° ?úÏä§???ºÏö∞??Í∑úÏπô(dynamicRoute.ts)???∞Îùº Í≤ΩÎ°ú??'/HASL010U'?ÖÎãà??
	const pgmid = 'HASL_010U'
	add_dynamic_route(pgmid, '?ÑÏóÖ?ÑÌëú?±Î°ù', 'HASL')

	router.push({
		path: `/${pgmid}`,
		query: {
			slipymd: ymd,
			slipno: no,
			deptcd: searchform.deptcd
		}
	})
}

_on_mounted(() => {
	if (typeof window !== 'undefined') (window as any).XLSX = XLSX
	if (maingridref.value) {
		maingrid = new tabulator(maingridref.value, {
			layout: 'fitColumns',
			height: '100%',
			pagination: "local",
			paginationSize: 50,
			paginationSizeSelector: [20, 50, 100, 500],
			paginationCounter: "rows",
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: [
				{
					title: "?ÑÌëúÎ≤àÌò∏", field: "slipno", width: 150, cssClass: "fw-bold",hozAlign: "center",
					formatter: (cell) => {
						const value = cell.getValue();
						if (!value) return "";
						// ?í° Ï§ëÎ≥µ Î∞©Ï?: ?¥Ï†Ñ ?âÍ≥º ?ÑÌëúÎ≤àÌò∏Í∞Ä Í∞ôÏúºÎ©??úÏãú ????
						const prevrow = cell.getRow().getPrevRow();
						if (prevrow && prevrow.getData().slipno === value) {
							return "";
						}
						return `<span class="text-primary cursor-pointer text-decoration-underline">${value}</span>`;
					},
					cellClick: (e, cell) => {
						const value = cell.getData().slipno; // ?úÏãú ?????âÏù¥?ºÎèÑ ?§Ï†ú ?∞Ïù¥?∞Ïóê??Í∞Ä?∏Ïò¥
						if(value) go_to_slip(String(value));
					},
					accessorDownload: (v) => v
				},
				{ title: "?âÎ≤à??, field: "srowno",hozAlign: "center", width: 65 },
				{
					title: "Í≥ÑÏ†ïÍ≥ºÎ™©", field: "acctnm", width: 160, hozAlign: "left",
					formatter: (cell) => `<div>${cell.getData().acctcd}</div><div class="small text-muted">${cell.getValue()}</div>`
				},
				{
					title: "?ÅÏöî", field: "remark", hozAlign: "left", minWidth: 300,
					formatter: (cell) => `<div class="fw-bold">${cell.getValue() || ''}</div>`
				},
				{
					title: "?åÍ≥Ñ??, field: "acctymd", width: 90,
					formatter: (cell) => {
						const val = cell.getValue()
						return val && val.length === 8 ? `${val.substring(2, 4)}.${val.substring(4, 6)}.${val.substring(6, 8)}` : val
					},
					accessorDownload: (val) => {
						return val && val.length === 8 ? `${val.substring(0, 4)}-${val.substring(4, 6)}-${val.substring(6, 8)}` : val
					}
				},
				{ title: "Ï∞®Î?", field: "dbamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
				{ title: "?ÄÎ≥Ä", field: "cramt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" }
			],
			columnCalcs: "table",
			rowFormatter: (row) => {
				const data = row.getData();
				if (data.detail_info) {
					const holder = document.createElement("div");
					holder.style.padding = "4px 8px 4px 365px"; // ?ÑÌëú(150)+?âÎ≤à(65)+Í≥ÑÏ†ï(160) ?Ä?µÏ†Å???ÑÏπò ?§Î???
					holder.style.borderTop = "1px dashed #eee";
					holder.style.backgroundColor = "#f8f9fa";
					holder.style.fontSize = "11px";
					holder.className = "text-primary fw-normal";
					holder.innerHTML = `<i class="bi bi-info-circle me-1"></i><b>?∏Î??¥Ïó≠:</b> ${data.detail_info}`;
					row.getElement().appendChild(holder);
				}
			}
		})

		// ?í° Í∏∞Ï°¥ Î¶¨Ïä§???Ä??(?òÏúÑ ?∏Ìôò??
		window.addEventListener('go-slip', ((e: CustomEvent) => {
			go_to_slip(e.detail);
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

:deep(.tabulator-row) { min-height: 42px; border-bottom: 1px solid #eee !important; }
.cursor-pointer { pointer: pointer; }
</style>