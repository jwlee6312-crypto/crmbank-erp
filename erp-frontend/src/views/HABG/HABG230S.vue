<!--	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: ?àÏÇ∞?§Ï†Å??
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : Î∂Ä?úÎ≥Ñ ?àÏÇ∞ ?§Ï†Å ?¥Ïó≠ Ï°∞Ìöå (?îÎ≥Ñ ?§Ï†Å ?ÑÌô©)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
				?àÏÇ∞Í¥ÄÎ¶?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">?àÏÇ∞?§Ï†Å??(HABG230S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> Ï°∞Ìöå
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> ?∏ÏáÑ
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> ?ëÏ?
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
							<col style="width: 12%" /><col style="width: 38%" />
							<col style="width: 12%" /><col style="width: 38%" />
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

			<!-- ?ìä Í∑∏Î¶¨???ÅÏó≠ -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
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
const yearOptions = Array.from({ length: 5 }, (_, i) => String(currentYear - i))

const searchForm = reactive({
	bugtyy: String(currentYear),
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || ''
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.deptcd) return vAlertError('Î∂Ä?úÎ? ?†ÌÉù?òÏÑ∏??')

	try {
		const res = await api.post('/api/habg/HABG_230S_STR', {
			cmpycd: authStore.cmpycd,
			bugtyy: searchForm.bugtyy,
			deptcd: searchForm.deptcd
		})

		const data = (res.data || []).map((row: any) => {
			const values = [
				Number(row.col2 || 0), Number(row.col3 || 0), Number(row.col4 || 0),
				Number(row.col5 || 0), Number(row.col6 || 0), Number(row.col7 || 0),
				Number(row.col8 || 0), Number(row.COL9 || 0), Number(row.col10 || 0),
				Number(row.col11 || 0), Number(row.col12 || 0), Number(row.col13 || 0)
			]
			const total = values.reduce((a, b) => a + b, 0)

			return {
				bugtcd: row.bugtcd,
				bugtnm: row.bugtnm,
				...Object.fromEntries(values.map((v, i) => [`M${i + 1}`, v])),
				total: total
			}
		})

		mainGrid?.setData(data)
		vAlert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { vAlertError('Ï°∞Ìöå Ï§??§Î•ò Î∞úÏÉù') }
}

const excel = () => {
	mainGrid?.download("xlsx", `?àÏÇ∞?§Ï†Å??${searchForm.deptnm}_${searchForm.bugtyy}.xlsx`)
}

const print = () => {
	const params = `bugtyy=${searchForm.bugtyy}&deptcd_h=${searchForm.deptcd}&deptnm_h=${searchForm.deptnm}&PRTGU=1`
	window.open(`/api/habg/HABG_230P?${params}`, 'BudgetPrint', 'width=1000,height=800,scrollbars=yes')
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
    Object.assign(modalProps, {
        title: 'Î∂Ä???†ÌÉù', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
        data: { gubun: 'D0', cmpycd: authStore.cmpycd, code: searchForm.deptnm },
        columns: [{ title: 'ÏΩîÎìú', field: 'deptcd', width: 80 }, { title: 'Î∂Ä?úÎ™Ö', field: 'deptnm', width: 180 }],
        onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search() }
    })
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		const monthCols = Array.from({ length: 12 }, (_, i) => ({
			title: `${i + 1}??, field: `M${i + 1}`, widthGrow: 1, hozAlign: "right",
			formatter: "money", formatterParams: { precision: 0 },
			bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
		}))

		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "?àÏÇ∞ÏΩîÎìú", field: "bugtcd", widthGrow: 1, hozAlign: "center", frozen: true },
				{ title: "?àÏÇ∞Î™?, field: "bugtnm", widthGrow: 1, frozen: true },
				...monthCols,
				{
					title: "??Í≥?, field: "total", widthGrow: 1, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 },
					cssClass: "fw-bold text-primary bg-light"
				}
			]
		})
	}
	if (searchForm.deptcd) search()
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; }
:deep(.tabulator-footer .tabulator-calcs) { background-color: #f0f7ff !important; font-weight: bold; }
</style>