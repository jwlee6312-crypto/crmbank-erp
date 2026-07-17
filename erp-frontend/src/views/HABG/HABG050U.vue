<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: ?àÏÇ∞Î∞∞Ï†ï
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : Î∂Ä?úÎ≥Ñ ?àÏÇ∞ Ï°∞Ï†ï?°ÏùÑ ?ïÏù∏?òÍ≥† Î∞∞Ï†ï?°ÏùÑ ?ïÏ†ï/?Ä??
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- ?? 1. ?ÅÎã® ?°ÏÖò Î∞?(?úÏ? Í∑úÍ≤© ?ÅÏö©) -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-check me-2 text-primary" style="font-size: 18px;"></i>
				?àÏÇ∞Í¥ÄÎ¶?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				?àÏÇ∞Î∞∞Ï†ï <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">?àÏÇ∞Î∞∞Ï†ï (HABG050U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">Ï¥àÍ∏∞??/button>
				<button class="btn-erp btn-search" @click="search">Ï°∞Ìöå</button>
				<button class="btn-erp btn-save" @click="save">?Ä??/button>
			</div>
		</div>

		<!-- ?í° 2. Î©îÏù∏ Ïª®ÌÖêÏ∏??ÅÏó≠ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- [?ÅÎã®] Ï°∞Ìöå ?ÑÌÑ∞ ?ÅÏó≠ (HSOD100U ?úÏ? ?®ÌÑ¥) -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense" width="100%">
						<colgroup>
							<col style="width: 10%" /><col style="width: 40%" />
							<col style="width: 10%" /><col style="width: 40%" />
						</colgroup>
						<tbody>
							<tr>
								<th class="text-center bg-light">?àÏÇ∞?∞Ïõî</th>
								<td>
									<div class="d-flex gap-1">
										<select v-model="searchForm.bugtyy" class="form-select form-select-sm" style="width: 100px;">
											<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}??/option>
										</select>
										<select v-model="searchForm.bugtmm" class="form-select form-select-sm" style="width: 100px;">
											<option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
										</select>
									</div>
								</td>
								<th class="text-center bg-light">?àÏÇ∞Î∂Ä??/th>
								<td>
									<div class="input-group input-group-sm" style="width: 250px;">
										<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
										<input v-model="searchForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" placeholder="Î∂Ä???†ÌÉù" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- ?ìä 3. Í∑∏Î¶¨???ÅÏó≠ -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-3 bg-white border-bottom d-flex justify-content-between align-items-center">
					<div class="form-check form-check-inline mb-0">
						<input v-model="searchForm.allyn" class="form-check-input" type="checkbox" id="checkAdjApply" true-value="Y" false-value="N">
						<label class="form-check-label small fw-bold text-primary" for="checkAdjApply">Ï°∞Ï†ï?°ÏùÑ Î∞∞Ï†ï?°Ïóê ?ºÍ¥Ñ ?ÅÏö© ?©Îãà??</label>
					</div>
					<span class="small text-muted fw-bold"><i class="bi bi-info-circle me-1"></i>Î∞∞Ï†ï ?ëÏóÖ?Ä Ï°∞Ï†ï???àÏÇ∞???ïÏ†ï?òÎäî ?®Í≥Ñ?ÖÎãà??</span>
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
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

//const currentYear = new Date().getFullYear()
const currentYear = 2011
const yearOptions = Array.from({ length: 7 }, (_, i) => String(currentYear + 1 - i))

const bgType = ref('010')

const searchForm = reactive({
	bugtyy: String(currentYear),
	bugtmm: String(new Date().getMonth() + 1).padStart(2, '0'),
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || '',
	allyn: 'N'
})

const periodOptions = computed(() => {
	if (bgType.value === '020') {
		return [
			{ value: '01', text: '01 Î∂ÑÍ∏∞' }, { value: '04', text: '02 Î∂ÑÍ∏∞' },
			{ value: '07', text: '03 Î∂ÑÍ∏∞' }, { value: '10', text: '04 Î∂ÑÍ∏∞' }
		]
	} else if (bgType.value === '030') {
		return [{ value: '01', text: '?ÅÎ∞òÍ∏? }, { value: '07', text: '?òÎ∞òÍ∏? }]
	} else if (bgType.value === '040') {
		return [{ value: '01', text: `${searchForm.bugtyy} ?? }]
	}
	return Array.from({ length: 12 }, (_, i) => ({
		value: String(i + 1).padStart(2, '0'),
		text: `${String(i + 1).padStart(2, '0')} ??
	}))
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.deptcd) return vAlertError('?àÏÇ∞Î∂Ä?úÎ? ?†ÌÉù??Ï£ºÏã≠?úÏò§.')

	try {
		const res = await api.post('/api/habg/HABG_050U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			bugtym: `${searchForm.bugtyy}${searchForm.bugtmm}`,
			deptcd: searchForm.deptcd
		})

		const data = (res.data || []).map((row: any) => ({
			bugtcd: row.bugtcd || row.col0,
			bugtnm: row.bugtnm || row.col1,
			req_amt: Number(row.rqstamt || row.col2 || 0),
			adj_amt: Number(row.adstamt || row.col3 || 0),
			asgnamt: Number(row.asgnamt || row.col4 || 0)
		}))

		mainGrid?.setData(data)
		vAlert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { vAlertError('Ï°∞Ìöå ?§Î•ò') }
}

const save = async () => {
	if (!searchForm.deptcd) return vAlertError('?àÏÇ∞Î∂Ä?úÎ? ?†ÌÉù?òÏÑ∏??')
	const gridData = mainGrid?.getData() || []
	if (gridData.length === 0) return

	try {
		for (const row of gridData) {
			const res = await api.post('/api/habg/HABG_050U_STR', {
				actkind: 'A0',
				cmpycd: authStore.cmpycd,
				bugtym: `${searchForm.bugtyy}${searchForm.bugtmm}`,
				deptcd: searchForm.deptcd,
				bugtcd: row.bugtcd,
				asgnamt: row.asgnamt,
				allyn: searchForm.allyn
			})

			const resData = res.data?.[0];
			if (resData) {
				const rtnCode = String(resData.col0 || resData.COL0 || '').trim();
				const rtnMsg = resData.col1 || resData.COL1;

				if (rtnCode !== '' && rtnCode !== '000' && rtnCode !== '0') {
					vAlertError(rtnMsg || '?Ä??Ï§??§Î•ò Î∞úÏÉù');
					return;
				}
			}
		}
		vAlert('?ïÏÉÅ?ÅÏúºÎ°??Ä?•Îêò?àÏäµ?àÎã§.')
		search()
	} catch (e) { vAlertError('?Ä???§Ìå®') }
}

const initialize = () => {
	mainGrid?.clearData()
	searchForm.allyn = 'N'
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: 'DEPT') {
    Object.assign(modalProps, {
        title: 'Î∂Ä???†ÌÉù', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
        data: { gubun: 'D0', cmpycd: authStore.cmpycd, code: searchForm.deptnm },
        columns: [{ title: 'ÏΩîÎìú', field: 'deptcd', width: 80 }, { title: 'Î∂Ä?úÎ™Ö', field: 'deptnm', width: 180 }],
        onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search() }
    })
	modalVisible.value = true
}

const fetchBgType = async () => {
	try {
		const res = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: ' ', gbncd: '200' })
		if (res.data && res.data.length > 0) {
			bgType.value = res.data[0].remark || '010'
		} else {
			bgType.value = '010'
		}
	} catch (e) { bgType.value = '010' }
}

onMounted(async () => {
	await fetchBgType()

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "?àÏÇ∞ÏΩîÎìú", field: "bugtcd", widthGrow: 1, hozAlign: "center" },
				{ title: "?àÏÇ∞Î™?, field: "bugtnm", widthGrow: 2, hozAlign: "left" },
				{ title: "?†Ï≤≠??, field: "req_amt", widthGrow: 1.5, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "Ï°∞Ï†ï??, field: "adj_amt", widthGrow: 1.5, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{
					title: "Î∞∞Ï†ï??, field: "asgnamt", widthGrow: 1.5, hozAlign: "right",
					editor: "number", formatter: "money", formatterParams: { precision: 0 },
					cssClass: "bg-warning-subtle fw-bold"
				}
			],
			columnCalcs: "both",
			bottomCalc: "sum"
		})
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
