<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Ï∂îÍ?/Ï°∞Ï†ï Î∞∞Ï†ï
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : ?àÏÇ∞ Ï∂îÍ?/Ï°∞Ï†ï ?†Ï≤≠ ?¥Ïó≠???Ä???§Ï†ú Î∞∞Ï†ï Ï≤òÎ¶¨ Î∞??ïÏ†ï
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- ?? ?ÅÎã® ?°ÏÖò Î∞?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-check-fill me-2 text-primary" style="font-size: 18px;"></i>
				?àÏÇ∞Í¥ÄÎ¶?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">Ï∂îÍ?/Ï°∞Ï†ï Î∞∞Ï†ï (HABG120U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> Ï¥àÍ∏∞??
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> Ï°∞Ìöå
				</button>
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-check-lg"></i> Î∞∞Ï†ï?Ä??
				</button>
			</div>
		</div>

		<!-- ?í° Î©îÏù∏ Ïª®ÌÖêÏ∏??ÅÏó≠ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- ?îç Í≤Ä??Ï°∞Í±¥ ?ÅÏó≠ (HSOD100U ?úÏ? ?®ÌÑ¥) -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense" width="100%">
						<colgroup>
							<col style="width: 10%" /><col style="width: 23%" />
							<col style="width: 10%" /><col style="width: 23%" />
							<col style="width: 10%" /><col style="width: 24%" />
						</colgroup>
						<tbody>
							<tr>
								<th class="text-center bg-light">?àÏÇ∞Î∂Ä??/th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="Î∂Ä???†ÌÉù" @input="searchForm.deptcd = ''" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT_S')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="text-center bg-light">?†Ï≤≠?∞Ïõî</th>
								<td>
									<div class="d-flex gap-1">
										<select v-model="searchForm.bugtyy" class="form-select form-select-sm" style="width: 100px;">
											<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}??/option>
										</select>
										<select v-model="searchForm.bugtmm" class="form-select form-select-sm" style="width: 80px;">
											<option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ String(m).padStart(2, '0') }}??/option>
										</select>
									</div>
								</td>
								<th class="text-center bg-light">Í≤Ä?âÎ???/th>
								<td>
									<select v-model="searchForm.rstyn" class="form-select form-select-sm" style="width: 100px;">
										<option value="N">ÎØ∏Î∞∞??/option>
										<option value="Y">Î∞∞Ï†ï</option>
									</select>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- ?ìù Î∞∞Ï†ï ?ïÎ≥¥ ?ÖÎ†• ?ÅÏó≠ -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;" />
							<col style="width: 20%;" />
							<col style="width: 100px;" />
							<col style="width: 20%;" />
							<col style="width: 100px;" />
							<col style="width: auto;" />
						</colgroup>
						<tbody>
							<tr>
								<th>Íµ¨Î∂Ñ</th>
								<td>
									<div class="d-flex gap-1">
										<input v-model="inputForm.dockind" type="text" class="form-control form-control-sm text-center bg-light" style="width: 50px;" readonly />
										<input v-model="inputForm.dockindnm" type="text" class="form-control form-control-sm bg-light" readonly />
									</div>
								</td>
								<th>?àÏÇ∞Î∂Ä??/th>
								<td>
									<div class="d-flex gap-1 align-items-center">
										<span class="badge bg-secondary px-2 py-1">{{ inputForm.deptcd }}</span>
										<span class="fw-bold">{{ inputForm.deptnm }}</span>
									</div>
								</td>
								<th>?†Ï≤≠?∞Ïõî</th>
								<td>
									<div class="d-flex gap-1 align-items-center fw-bold">
										{{ inputForm.bugtyy }}??{{ inputForm.bugtmm }}??
									</div>
								</td>
							</tr>
							<tr>
								<th>?†Ï≤≠??/th>
								<td>
									<input :value="formatMoney(inputForm.reqamt)" type="text" class="form-control form-control-sm text-end bg-light" readonly />
								</td>
								<th>?àÏÇ∞?îÏï°</th>
								<td>
									<input :value="formatMoney(inputForm.janamt)" type="text" class="form-control form-control-sm text-end bg-light fw-bold text-danger" readonly />
								</td>
								<th class="bg-yellow">Î∞∞Ï†ï??/th>
								<td>
									<input v-model="inputForm.bugtamt" type="number" class="form-control form-control-sm text-end fw-bold text-primary border-primary" />
								</td>
							</tr>
							<tr>
								<th class="bg-yellow">Î∞∞Ï†ï??/th>
								<td>
									<input v-model="inputForm.bugtymd" type="date" class="form-control form-control-sm" />
								</td>
								<th>?àÏÇ∞ÏΩîÎìú</th>
								<td>
									<div class="small">
										<div class="text-muted">FROM: <span class="text-dark fw-bold">{{ inputForm.bugtnm_fr }}</span></div>
										<div v-if="inputForm.bugtnm_to" class="text-muted">TO: <span class="text-dark fw-bold">{{ inputForm.bugtnm_to }}</span></div>
									</div>
								</td>
								<th>Ï°∞Ï†ï?∞Ïõî</th>
								<td>
									<div class="small">
										<div class="text-muted">FROM: <span class="text-dark fw-bold">{{ formatym(inputForm.bugtym_fr) }}</span></div>
										<div v-if="inputForm.bugtym_to" class="text-muted">TO: <span class="text-dark fw-bold">{{ formatym(inputForm.bugtym_to) }}</span></div>
									</div>
								</td>
							</tr>
							<tr>
								<th>?ÅÏöî</th>
								<td colspan="3">
									<input v-model="inputForm.bigo" type="text" class="form-control form-control-sm bg-light" readonly />
								</td>
								<th>Î∞∞Ï†ï?¨Î?</th>
								<td>
									<div class="form-check mb-0">
										<input v-model="inputForm.rstyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="chkAssign">
										<label class="form-check-label fw-bold text-primary" for="chkAssign">Î∞∞Ï†ï?ïÏ†ï</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- ?ìä Í∑∏Î¶¨???ÅÏó≠ -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-3 bg-white border-bottom d-flex justify-content-between align-items-center">
					<span class="small fw-bold text-dark"><i class="bi bi-list-check me-1"></i>?†Ï≤≠ Î∞?Î∞∞Ï†ï ?¥Ïó≠</span>
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
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

//const currentYear = new Date().getFullYear()
const currentYear = 2011
const currentMonth = String(new Date().getMonth() + 1).padStart(2, '0')
const today = new Date().toISOString().substring(0, 10)
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))

const searchForm = reactive({
	bugtyy: String(currentYear),
	bugtmm: currentMonth,
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || '',
	rstyn: 'N'
})

const inputForm = reactive({
	actkind: 'U0',
	bugtno: '',
	dockind: '',
	dockindnm: '',
	deptcd: '',
	deptnm: '',
	bugtyy: '',
	bugtmm: '',
	reqamt: 0,
	janamt: 0,
	bugtamt: 0,
	bugtymd: today,
	bugtcd_fr: '',
	bugtnm_fr: '',
	bugtcd_to: '',
	bugtnm_to: '',
	bugtym_fr: '',
	bugtym_to: '',
	bigo: '',
	rstyn: 'N'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.deptcd) return vAlertError('?àÏÇ∞Î∂Ä?úÎ? ?†ÌÉù?òÏÑ∏??')
	try {
		const res = await api.post('/api/habg/HABG_120U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			bugtym: `${searchForm.bugtyy}${searchForm.bugtmm}`,
			deptcd: searchForm.deptcd,
			rstyn: searchForm.rstyn
		})
		mainGrid?.setData(res.data || [])
		vAlert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { vAlertError('Ï°∞Ìöå ?§Ìå®') }
}

const save = async () => {
	if (!inputForm.bugtno) return vAlertError('Î∞∞Ï†ï???¥Ïó≠??Î™©Î°ù?êÏÑú ?†ÌÉù??Ï£ºÏã≠?úÏò§.')
	if (Number(inputForm.bugtamt) <= 0 && inputForm.rstyn === 'Y') return vAlertError('Î∞∞Ï†ï?°ÏùÑ ?ÖÎ†•??Ï£ºÏã≠?úÏò§.')

	try {
		await api.post('/api/habg/HABG_120U_STR', {
			...inputForm,
			cmpycd: authStore.cmpycd,
			bugtym: `${inputForm.bugtyy}${inputForm.bugtmm}`,
			bugtymd: inputForm.bugtymd.replace(/-/g, '')
		})
		vAlert('Î∞∞Ï†ï Ï≤òÎ¶¨Í∞Ä ?ÑÎ£å?òÏóà?µÎãà??')
		search()
		initialize()
	} catch (e) { vAlertError('?Ä???§Ìå®') }
}

const initialize = () => {
	Object.assign(inputForm, {
		actkind: 'U0', bugtno: '', dockind: '', dockindnm: '', deptcd: '', deptnm: '',
		bugtyy: '', bugtmm: '', reqamt: 0, janamt: 0, bugtamt: 0, bugtymd: today,
		bugtcd_fr: '', bugtnm_fr: '', bugtcd_to: '', bugtnm_to: '',
		bugtym_fr: '', bugtym_to: '', bigo: '', rstyn: 'N'
	})
}

// ?†Ìã∏Î¶¨Ìã∞
const formatMoney = (val: any) => Number(val || 0).toLocaleString()
const formatym = (val: string) => (val && val.length === 6) ? `${val.substring(0, 4)}.${val.substring(4, 6)}` : val

// ?ùÏóÖ ?§Ï†ï
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT_S') {
        Object.assign(modalProps, {
            title: 'Î∂Ä???†ÌÉù', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
            data: { gubun: 'D0', cmpycd: authStore.cmpycd, code: searchForm.deptnm },
            columns: [{ title: 'ÏΩîÎìú', field: 'deptcd', width: 80 }, { title: 'Î∂Ä?úÎ™Ö', field: 'deptnm', width: 180 }],
            onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search() }
        })
		modalVisible.value = true
	}
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle", hozAlign: "center" },

			columns: [
				{ title: "Î≤àÌò∏", field: "bugtno", width: 50 },
				{
					title: "?†Ï≤≠??, field: "rqstymd", width: 90,
					formatter: (cell) => {
						const v = cell.getValue();
						return v ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : '';
					}
				},
				{ title: "Ï°∞Ï†ïÍµ¨Î∂Ñ", field: "dockindnm", width: 80 },
				{ title: "?†Ï≤≠??, field: "rqstamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{
					title: "FROM",
					columns: [
						{ title: "?àÏÇ∞Î™?, field: "bugtnmfr", width: 150, hozAlign: "left" },
						{
							title: "?∞Ïõî", field: "bugtymfr", width: 70,
							formatter: (cell) => { const v = cell.getValue(); return v ? `${v.substring(2, 4)}.${v.substring(4, 6)}` : '' }
						}
					]
				},
				{
					title: "TO",
					columns: [
						{ title: "?àÏÇ∞Î™?, field: "bugtnmto", width: 150, hozAlign: "left" },
						{
							title: "?∞Ïõî", field: "bugtymto", width: 70,
							formatter: (cell) => { const v = cell.getValue(); return v ? `${v.substring(2, 4)}.${v.substring(4, 6)}` : '' }
						}
					]
				},
				{ title: "?ÅÏöî", field: "bigo", minWidth: 200, hozAlign: "left" },
				{ title: "Î∞∞Ï†ï", field: "rstyn", width: 50, formatter: "tickCross", formatterParams: { tickElement: "<i class='bi bi-check-lg text-primary fw-bold'></i>", crossElement: "" } }
			]
		})

		mainGrid.on("rowClick", (e, row) => {
			const d = row.getData();
			Object.assign(inputForm, {
                actkind: 'U0',
                bugtno: d.bugtno,
                deptcd: d.deptcd,
                deptnm: d.deptnm,
                dockind: d.dockind,
                dockindnm: d.dockindnm,
                bugtcd_fr: d.bugtcdfr,
                bugtnm_fr: d.bugtnmfr,
                bugtym_fr: d.bugtymfr,
                reqamt: Number(d.rqstamt),
                bugtcd_to: d.bugtcdto,
                bugtnm_to: d.bugtnmto,
                bugtym_to: d.bugtymto,
                rstyn: d.rstyn,
                bigo: d.bigo,
                bugtamt: d.rstyn === 'Y' ? Number(d.bugtamt) : Number(d.rqstamt),
                bugtymd: d.rstyn === 'Y' ? (d.bugtymd ? `${d.bugtymd.substring(0, 4)}-${d.bugtymd.substring(4, 6)}-${d.bugtymd.substring(6, 8)}` : today) : today,
                janamt: Number(d.janamt),
                bugtyy: d.bugtym.substring(0, 4),
                bugtmm: d.bugtym.substring(4, 6)
			})
		})
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
.erp-table-full th { background-color: #f8f9fa; text-align: center; padding: 5px; font-size: 12px; border: 1px solid #dee2e6; }
.erp-table-full td { padding: 4px; border: 1px solid #dee2e6; background-color: #fff; }
.bg-yellow { background-color: #fff9db !important; }

:deep(.tabulator-header .tabulator-col) { background-color: #f1f5f9 !important; border-right: 1px solid #dee2e6 !important; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; }
</style>