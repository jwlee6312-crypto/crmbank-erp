<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Ï∂îÍ?/Ï°∞Ï†ï?†Ï≤≠
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : ?àÏÇ∞ Ï∂îÍ?, Ï°∞Í∏∞, ?¥Ïõî, ?ÑÏö© ?†Ï≤≠ Î∞??¥Ïó≠ Í¥ÄÎ¶?
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- ?? 1. ?ÅÎã® ?°ÏÖò Î∞?-->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-pencil-square me-2 text-primary" style="font-size: 18px;"></i>
				?àÏÇ∞Í¥ÄÎ¶?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				?àÏÇ∞Ï°∞Ï†ï <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">Ï∂îÍ?/Ï°∞Ï†ï?†Ï≤≠ (HABG110U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">Ï¥àÍ∏∞??/button>
				<button class="btn-erp btn-search" @click="search">Ï°∞Ìöå</button>
				<button class="btn-erp btn-save" @click="save">?Ä??/button>
			</div>
		</div>

		<!-- ?í° 2. Î©îÏù∏ Ïª®ÌÖêÏ∏??ÅÏó≠ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- [?ÅÎã®] Ï°∞Ìöå ?ÑÌÑ∞ ?ÅÏó≠ -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense" width="100%">
						<colgroup>
							<col style="width: 10%" /><col style="width: 40%" />
							<col style="width: 10%" /><col style="width: 40%" />
						</colgroup>
						<tbody>
							<tr>
								<th class="text-center bg-light">?àÏÇ∞Î∂Ä??/th>
								<td>
									<div class="input-group input-group-sm" style="width: 250px;">
										<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
										<input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="Î∂Ä???†ÌÉù" @input="searchForm.deptcd = ''" />
										<button class="btn btn-outline-secondary" @click="openHelp('DEPT_S')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="text-center bg-light">?†Ï≤≠?∞Ïõî</th>
								<td>
									<div class="d-flex gap-1">
										<select v-model="searchForm.bugtyy" class="form-select form-select-sm" style="width: 100px;">
											<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}??/option>
										</select>
										<select v-model="searchForm.bugtmm" class="form-select form-select-sm" style="width: 100px;">
											<option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ String(m).padStart(2, '0') }}??/option>
										</select>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- ?ìù ?ÖÎ†• ???ÅÏó≠ (?úÏ? erp-table-full ?ÅÏö©) -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full">
						<colgroup>
                            <col style="width: 110px;" />
                            <col style="width: 25%;" />
                            <col style="width: 110px;" />
                            <col style="width: 25%;" />
                            <col style="width: 110px;" />
                            <col style="width: auto;" />
                        </colgroup>
						<tbody>
							<tr>
								<th class="required">Ï°∞Ï†ïÍµ¨Î∂Ñ</th>
								<td>
									<select v-model="inputForm.dockind" class="form-select form-select-sm">
										<option value="000">?†ÌÉù?òÏÑ∏??/option>
										<option v-for="opt in dockindOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
									</select>
								</td>
								<th>?àÏÇ∞Î∂Ä??/th>
								<td>
									<div class="d-flex gap-2 align-items-center px-2">
										<span class="badge bg-secondary">{{ inputForm.deptcd }}</span>
										<span class="fw-bold small">{{ inputForm.deptnm }}</span>
									</div>
								</td>
                                <th>?†Ï≤≠?∞Ïõî</th>
                                <td>
                                    <div class="d-flex gap-1 align-items-center px-2">
                                        <span class="fw-bold small text-primary">{{ inputForm.bugtyy }}-{{ inputForm.bugtmm }}</span>
                                    </div>
                                </td>
							</tr>
							<tr>
								<th class="required">?àÏÇ∞ÏΩîÎìú</th>
								<td>
									<div class="d-flex align-items-center gap-2 px-1">
										<div class="input-group input-group-sm" style="width: 250px;">
											<input v-model="inputForm.bugtcd_fr" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
											<input v-model="inputForm.bugtnm_fr" type="text" class="form-control" placeholder="FROM ?àÏÇ∞ÏΩîÎìú" />
											<button class="btn btn-outline-secondary" @click="openHelp('BUGT_FR')"><i class="bi bi-search"></i></button>
										</div>
										<template v-if="inputForm.dockind === '050'">
											<i class="bi bi-arrow-right text-primary"></i>
											<div class="input-group input-group-sm" style="width: 250px;">
												<input v-model="inputForm.bugtcd_to" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
												<input v-model="inputForm.bugtnm_to" type="text" class="form-control" placeholder="TO ?àÏÇ∞ÏΩîÎìú" />
												<button class="btn btn-outline-secondary" @click="openHelp('BUGT_TO')"><i class="bi bi-search"></i></button>
											</div>
										</template>
									</div>
								</td>
								<th class="required">?†Ï≤≠??/th>
								<td>
									<input v-model="inputForm.reqamt" type="number" class="form-control form-control-sm text-end fw-bold text-primary" placeholder="0" />
								</td>
                                <th>Ï°∞Ï†ï?∞Ïõî</th>
                                <td>
                                    <div class="d-flex align-items-center gap-2 px-1">
                                        <div class="d-flex gap-1">
                                            <select v-model="inputForm.bugtyy_fr" class="form-select form-select-sm" :disabled="!isFromPeriodEnabled" style="width: 100px;">
                                                <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}??/option>
                                            </select>
                                            <select v-model="inputForm.bugtmm_fr" class="form-select form-select-sm" :disabled="!isFromPeriodEnabled" style="width: 90px;">
                                                <option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
                                            </select>
                                        </div>
                                        <template v-if="inputForm.dockind === '040'">
                                            <i class="bi bi-arrow-right text-primary"></i>
                                            <div class="d-flex gap-1">
                                                <select v-model="inputForm.bugtyy_to" class="form-select form-select-sm" style="width: 100px;">
                                                    <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}??/option>
                                                </select>
                                                <select v-model="inputForm.bugtmm_to" class="form-select form-select-sm" style="width: 90px;">
                                                    <option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
                                                </select>
                                            </div>
                                        </template>
                                    </div>
                                </td>
							</tr>
							<tr>
								<th>?ÅÏöî</th>
								<td colspan="3">
									<input v-model="inputForm.bigo" type="text" class="form-control form-control-sm" placeholder="?†Ï≤≠ ?¨Ïú† Î∞??ÅÏÑ∏ ?¥Ïó≠???ÖÎ†•?òÏã≠?úÏò§." />
								</td>
								<th>??†ú?¨Î?</th>
								<td>
									<div class="form-check form-switch ms-2">
										<input v-model="inputForm.useyn" class="form-check-input" type="checkbox" true-value="N" false-value="Y" id="chkDelete">
										<label class="form-check-label text-danger fw-bold small" for="chkDelete">??†ú</label>
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
					<span class="small fw-bold text-dark"><i class="bi bi-list-ul me-1 text-primary"></i>?†Ï≤≠ ?¥Ïó≠ Î¶¨Ïä§??/span>
					<span v-if="inputForm.rstyn === 'Y'" class="badge bg-danger" style="font-size: 10px;">Î∞∞Ï†ï?ÑÎ£å (?òÏ†ïÎ∂àÍ?)</span>
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
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
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear + 1 - i))

const searchForm = reactive({
	bugtyy: String(currentYear),
	bugtmm: currentMonth,
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || ''
})

const inputForm = reactive({
	actkind: 'A0',
	bugtno: '',
	dockind: '000',
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || '',
	bugtyy: String(currentYear),
	bugtmm: currentMonth,
	reqamt: 0,
	bugtcd_fr: '',
	bugtnm_fr: '',
	bugtcd_to: '',
	bugtnm_to: '',
	bugtyy_fr: String(currentYear),
	bugtmm_fr: currentMonth,
	bugtyy_to: String(currentYear),
	bugtmm_to: currentMonth,
	bigo: '',
	useyn: 'Y',
	rstyn: 'N'
})

const bgType = ref('010')
const dockindOptions = ref<any[]>([])

const isFromPeriodEnabled = computed(() => ['030'].includes(inputForm.dockind))

const periodOptions = computed(() => {
	if (bgType.value === '020') {
		return [{ value: '01', text: '1Î∂ÑÍ∏∞' }, { value: '04', text: '2Î∂ÑÍ∏∞' }, { value: '07', text: '3Î∂ÑÍ∏∞' }, { value: '10', text: '4Î∂ÑÍ∏∞' }]
	} else if (bgType.value === '030') {
		return [{ value: '01', text: '?ÅÎ∞òÍ∏? }, { value: '07', text: '?òÎ∞òÍ∏? }]
	} else if (bgType.value === '040') {
		return [{ value: '01', text: '?ÑÏ≤¥' }]
	}
	return Array.from({ length: 12 }, (_, i) => ({ value: String(i + 1).padStart(2, '0'), text: `${i + 1}?? }))
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.deptcd) return vAlertError('?àÏÇ∞Î∂Ä?úÎ? ?†ÌÉù?òÏÑ∏??')
	try {
		const res = await api.post('/api/habg/HABG_110U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			bugtym: `${searchForm.bugtyy}${searchForm.bugtmm}`,
			deptcd: searchForm.deptcd
		})
		mainGrid?.setData(res.data || [])
		vAlert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { vAlertError('Ï°∞Ìöå ?§Ìå®') }
}

const save = async () => {
	if (inputForm.rstyn === 'Y') return vAlertError('Î∞∞Ï†ï???êÎ£å?ÖÎãà?? ?òÏ†ï?????ÜÏäµ?àÎã§.')
	if (inputForm.dockind === '000') return vAlertError('Íµ¨Î∂Ñ???†ÌÉù??Ï£ºÏã≠?úÏò§.')
	if (!inputForm.bugtcd_fr) return vAlertError('?àÏÇ∞ÏΩîÎìúÎ•??†ÌÉù??Ï£ºÏã≠?úÏò§.')
	if (Number(inputForm.reqamt) <= 0) return vAlertError('?†Ï≤≠?°ÏùÑ ?ÖÎ†•??Ï£ºÏã≠?úÏò§.')

	try {
		await api.post('/api/habg/HABG_110U_STR', {
			...inputForm,
			cmpycd: authStore.cmpycd,
			bugtym: `${inputForm.bugtyy}${inputForm.bugtmm}`,
			bugtym_fr: `${inputForm.bugtyy_fr}${inputForm.bugtmm_fr}`,
			bugtym_to: `${inputForm.bugtyy_to}${inputForm.bugtmm_to}`
		})
		vAlert('?Ä?•Îêò?àÏäµ?àÎã§.')
		search()
		initialize()
	} catch (e) { vAlertError('?Ä???§Ìå®') }
}

const initialize = () => {
	Object.assign(inputForm, {
		actkind: 'A0', bugtno: '', dockind: '000', reqamt: 0,
		bugtcd_fr: '', bugtnm_fr: '', bugtcd_to: '', bugtnm_to: '',
		bigo: '', useyn: 'Y', rstyn: 'N',
		bugtyy: searchForm.bugtyy, bugtmm: searchForm.bugtmm,
		bugtyy_fr: searchForm.bugtyy, bugtmm_fr: searchForm.bugtmm,
		bugtyy_to: searchForm.bugtyy, bugtmm_to: searchForm.bugtmm
	})
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type.startsWith('DEPT')) {
        Object.assign(modalProps, {
            title: 'Î∂Ä???†ÌÉù', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
            data: { gubun: 'D0', cmpycd: authStore.cmpycd, code: searchForm.deptnm },
            columns: [{ title: 'ÏΩîÎìú', field: 'deptcd', width: 80 }, { title: 'Î∂Ä?úÎ™Ö', field: 'deptnm', width: 180 }],
            onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search() }
        })
	} else {
		Object.assign(modalProps, {
			title: '?àÏÇ∞ÏΩîÎìú ?†ÌÉù', path: '/api/ha00/HA00_00P_STR', defaultField: 'bugtnm',
			data: { gubun: 'B1', cmpycd: authStore.cmpycd, search: type === 'BUGT_FR' ? inputForm.bugtnm_fr : inputForm.bugtnm_to },
			columns: [{ title: 'ÏΩîÎìú', field: 'bugtcd', width: 80 }, { title: '?àÏÇ∞Î™?, field: 'bugtnm', width: 180 }],
			onConfirm: (d: any) => {
				if (type === 'BUGT_FR') { inputForm.bugtcd_fr = d.bugtcd; inputForm.bugtnm_fr = d.bugtnm }
				else { inputForm.bugtcd_to = d.bugtcd; inputForm.bugtnm_to = d.bugtnm }
			}
		})
	}
	modalVisible.value = true
}

const loadCodes = async () => {
	try {
		const [resType, resDockind] = await Promise.all([
			api.post('/api/habg/GET_BGTYPE', { cmpycd: authStore.cmpycd }),
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '210' })
		])
		bgType.value = resType.data || '010'
		dockindOptions.value = resDockind.data || []
	} catch (e) {}
}

onMounted(() => {
	loadCodes()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle", hozAlign: "center" },

			columns: [
				{ title: "Î≤àÌò∏", field: "bugtno", width: 70 },
				{
					title: "?†Ï≤≠??, field: "rqstymd", widthGrow: 1,
					formatter: (cell) => {
						const v = cell.getValue();
						return v ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : '';
					}
				},
				{ title: "Ï°∞Ï†ïÍµ¨Î∂Ñ", field: "bugtnmfr", widthGrow: 1 },
				{ title: "?†Ï≤≠??, field: "rqstamt", widthGrow: 1, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{
					title: "FROM",
					columns: [
						{ title: "?àÏÇ∞Î™?, field: "bugtnmfr", widthGrow: 1, hozAlign: "left" },
						{
							title: "?∞Ïõî", field: "bugtymfr", widthGrow: 1,
							formatter: (cell) => { const v = cell.getValue(); return v ? `${v.substring(0, 4)}.${v.substring(4, 6)}` : '' }
						}
					]
				},
				{
					title: "TO",
					columns: [
						{ title: "?àÏÇ∞Î™?, field: "bugtnmto", widthGrow: 1, hozAlign: "left" },
						{
							title: "?∞Ïõî", field: "bugtymto", widthGrow: 1,
							formatter: (cell) => { const v = cell.getValue(); return v ? `${v.substring(0, 4)}.${v.substring(4, 6)}` : '' }
						}
					]
				},
				{ title: "?ÅÏöî", field: "bigo", minWidth: 200, hozAlign: "left" },
                { title: "??†ú", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">?¨Ïö©</b>' : '<span class="text-danger">??†ú</span>';
                  }
                }
			]
		})

		mainGrid.on("rowClick", (e, row) => {
			const d = row.getData();
			Object.assign(inputForm, {
				actkind: 'U0', bugtno: d.bugtno, dockind: d.dockind,
				deptcd: d.deptcd, deptnm: d.deptnm, reqamt: Number(d.rqstamt),
				bugtcd_fr: d.bugtcdfr, bugtnm_fr: d.bugtnmfr, bugtcd_to: d.bugtcdto, bugtnm_to: d.bugtnmto,
				bugtyy_fr: d.bugtymfr.substring(0, 4), bugtmm_fr: d.bugtymfr.substring(4, 6),
				bugtyy_to: d.bugtymto.substring(0, 4), bugtmm_to: d.bugtymto.substring(4, 6),
				rstyn: d.rstyn, bigo: d.bigo, useyn: d.useyn
			})
		})
	}
})

watch(() => [searchForm.bugtyy, searchForm.bugtmm], ([y, m]) => {
	inputForm.bugtyy = y; inputForm.bugtmm = m;
	inputForm.bugtyy_fr = y; inputForm.bugtmm_fr = m;
	inputForm.bugtyy_to = y; inputForm.bugtmm_to = m;
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
