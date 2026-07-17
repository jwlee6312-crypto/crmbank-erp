<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: ?àÏÇ∞?†Ï≤≠
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : Î∂Ä?úÎ≥Ñ/Í≥ÑÏ†ïÎ≥??∞Í∞Ñ ?àÏÇ∞???îÎ≥ÑÎ°??†Ï≤≠ Î∞??òÏ†ï
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- ?? ?ÅÎã® ?°ÏÖò Î∞?(?úÏ? Í∑úÍ≤©) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-pencil-square me-2 text-primary" style="font-size: 18px;"></i>
				?àÏÇ∞Í¥ÄÎ¶?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">?àÏÇ∞?†Ï≤≠ (HABG010U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">Ï¥àÍ∏∞??/button>
				<button class="btn-erp btn-search" @click="search">Ï°∞Ìöå</button>
				<button class="btn-erp btn-save" @click="save">?Ä??/button>
			</div>
		</div>

		<!-- ?í° Î©îÏù∏ Ïª®ÌÖêÏ∏??ÅÏó≠ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- ?îç Í≤Ä???ÖÎ†• Ï°∞Í±¥ ?ÅÏó≠ (HSOD100U ?úÏ? ?®ÌÑ¥) -->
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
								<th class="text-center bg-light">?àÏÇ∞?∞ÎèÑ</th>
								<td>
									<select v-model="searchForm.bugtyy" class="form-select form-select-sm" style="width: 100px;" @change="search">
										<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}??/option>
									</select>
								</td>
								<th class="text-center bg-light">?àÏÇ∞Î∂Ä??/th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" placeholder="Î∂Ä???†ÌÉù" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="text-center bg-light">?àÏÇ∞ÏΩîÎìú</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="searchForm.bugtcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.bugtnm" type="text" class="form-control" @keydown.enter="openHelp('BUGT')" placeholder="?àÏÇ∞ÏΩîÎìú ?†ÌÉù" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('BUGT')"><i class="bi bi-search"></i></button>
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
					<span class="small text-danger fw-bold"><i class="bi bi-exclamation-circle me-1"></i>Ï°∞Ï†ï Î∞?Î∞∞Ï†ï?°Ïù¥ Ï°¥Ïû¨?òÎ©¥ ?òÏ†ï??Î∂àÍ??©Îãà??</span>
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
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear + 1 - i))

const searchForm = reactive({
	bugtyy: String(currentYear),
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || '',
	bugtcd: '',
	bugtnm: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.deptcd || !searchForm.bugtcd) return

	try {
		const res = await api.post('/api/habg/HABG_010U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			bugtym: searchForm.bugtyy,
			deptcd: searchForm.deptcd,
			bugtcd: searchForm.bugtcd
		})

		const data = (res.data || []).map((row: any) => ({
		    mm: row.bugtym,
			gubun: row.bugtymnm,
			last_amt: Number(row.buseamt || 0),
			req_amt: Number(row.creqamt || 0),
			adj_amt: Number(row.cadstamt || 0),
			alloc_amt: Number(row.casgnamt || 0)
		}))

		mainGrid?.setData(data)
		vAlert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { vAlertError('Ï°∞Ìöå ?§Î•ò') }
}

const save = async () => {
	if (!searchForm.deptcd || !searchForm.bugtcd) {
		return vAlertError('Î∂Ä?úÏ? ?àÏÇ∞ÏΩîÎìúÎ•??†ÌÉù?òÏÑ∏??')
	}

	const gridData = mainGrid?.getData() || []
	if (gridData.length === 0) return

	try {
		for (const row of gridData) {
			await api.post('/api/habg/HABG_010U_STR', {
				actkind: 'A0',
				cmpycd: authStore.cmpycd,
				bugtym: `${searchForm.bugtyy}${row.mm}`,
				deptcd: searchForm.deptcd,
				bugtcd: searchForm.bugtcd,
				reqamt: row.req_amt,
				userid: authStore.userid
			})
		}
		vAlert('?ïÏÉÅ?ÅÏúºÎ°??Ä?•Îêò?àÏäµ?àÎã§.')
		search()
	} catch (e) { vAlertError('?Ä??Ï§??§Î•ò Î∞úÏÉù') }
}

const initialize = () => {
	searchForm.bugtcd = ''; searchForm.bugtnm = ''
	mainGrid?.clearData()
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: 'DEPT' | 'BUGT') {
	if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: 'Î∂Ä???†ÌÉù', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
			data: { gubun: 'D0', cmpycd: authStore.cmpycd, code: searchForm.deptnm },
			columns: [{ title: 'ÏΩîÎìú', field: 'deptcd', width: 80 }, { title: 'Î∂Ä?úÎ™Ö', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search() }
		})
	} else {
		Object.assign(modalProps, {
			title: '?àÏÇ∞ÏΩîÎìú ?†ÌÉù', path: '/api/ha00/HA00_00P_STR', defaultField: 'bugtnm',
			data: { gubun: 'B0', cmpycd: authStore.cmpycd, code: searchForm.bugtnm },
			columns: [{ title: 'ÏΩîÎìú', field: 'bugtcd', width: 80 }, { title: '?àÏÇ∞Î™?, field: 'bugtnm', width: 180 }],
			onConfirm: (d: any) => { searchForm.bugtcd = d.bugtcd; searchForm.bugtnm = d.bugtnm; search() }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "Íµ¨Î∂Ñ", field: "gubun", widthGrow: 1, hozAlign: "center" },
				{ title: "?ÑÎÖÑ?¨Ïö©??, field: "last_amt", widthGrow: 1, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{
					title: "?†Ï≤≠??, field: "req_amt", widthGrow: 1, hozAlign: "right",
					editor: "number", formatter: "money", formatterParams: { precision: 0 },
					cssClass: "fw-bold text-primary"
				},
				{ title: "Ï°∞Ï†ï??, field: "adj_amt", widthGrow: 1, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "Î∞∞Ï†ï??, field: "alloc_amt", widthGrow: 1, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "", field: "empty", widthGrow: 1 }
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
