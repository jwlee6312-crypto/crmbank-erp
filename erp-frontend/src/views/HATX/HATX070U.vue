<!--	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Îß§ÏûÖÎ∂ÄÍ∞Ä?∏Ï†ë??
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : Îß§ÏûÖ Î∂ÄÍ∞Ä???¥Ïó≠???ëÏàò ?ïÏù∏ Î∞??ÑÏûêÎ∞úÌñâ ?¨Î? ?ºÍ¥Ñ Í¥ÄÎ¶?
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-check2-all me-2 text-primary" style="font-size: 18px;"></i>
				?∏Î¨¥Í¥ÄÎ¶?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">Îß§ÏûÖÎ∂ÄÍ∞Ä?∏Ï†ë??(HATX070U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-save" @click="handleBatchSave">
					<i class="bi bi-check-lg"></i> {{ searchForm.cfmyn === 'N' ? '?ëÏàòÏ≤òÎ¶¨' : 'Ï∑®ÏÜåÏ≤òÎ¶¨' }}
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> Ï°∞Ìöå
				</button>
			</div>
		</div>

		<!-- ?îç Í≤Ä??Ï°∞Í±¥ ?ÅÏó≠ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>?¨ÏóÖ??/span>
							<select v-model="searchForm.taxunit" class="form-select form-select-sm" style="width: 150px;" @change="search">
								<option v-for="opt in taxUnitOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
							</select>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>Î∞úÌñâ??/span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.fromdt" type="date" class="form-control form-control-sm" style="width: 135px;" />
								<span>~</span>
								<input v-model="searchForm.todt" type="date" class="form-control form-control-sm" style="width: 135px;" />
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>Ï°∞Ìöå?Ä??/span>
							<select v-model="searchForm.cfmyn" class="form-select form-select-sm" style="width: 100px;" @change="search">
								<option value="N">ÎØ∏Ï†ë??/option>
								<option value="Y">?ëÏàò</option>
							</select>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- ?ìä Í∑∏Î¶¨???ÅÏó≠ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const today = new Date().toISOString().slice(0, 10)
const firstDay = today.slice(0, 8) + '01'

const taxUnitOptions = ref<any[]>([])

const searchForm = reactive({
	taxunit: '',
	fromdt: firstDay,
	todt: today,
	cfmyn: 'N'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const fetchOptions = async () => {
	try {
		const res = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd, search: ' ' })
		taxUnitOptions.value = res.data || []
		if (taxUnitOptions.value.length > 0) searchForm.taxunit = taxUnitOptions.value[0].code
	} catch (e) { console.error(e) }
}

const search = async () => {
	if (!searchForm.taxunit) return vAlertError('?¨ÏóÖ?•ÏùÑ ?†ÌÉù?òÏÑ∏??')

	try {
		const res = await api.post('/api/hatx/HATX_010U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			taxunit: searchForm.taxunit,
			fromdt: searchForm.fromdt.replace(/-/g, ''),
			todt: searchForm.todt.replace(/-/g, ''),
			cfmyn: searchForm.cfmyn,
			taxkind: '100' // Îß§ÏûÖ
		})

		const data = (res.data || []).map((row: any) => ({
			pubymd: row.pubymd,
			custnm: row.custnm,
			typenm: row.typenm,
			supyamt: Number(row.supyamt || 0),
			vatamt: Number(row.vatamt || 0),
			totamt: Number(row.totamt || 0),
			descnm: row.descnm,
			taxym: row.taxym,
			taxno: row.taxno,
			elcyn: row.elcyn === 'Y',
			_selected: searchForm.cfmyn === 'N'
		}))

		mainGrid?.setData(data)
		if (searchForm.cfmyn === 'N') {
			mainGrid?.selectRow()
		}
		vAlert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { vAlertError('Ï°∞Ìöå Ï§??§Î•ò Î∞úÏÉù') }
}

const handleBatchSave = async () => {
	const selectedRows = mainGrid?.getSelectedData() || []
	if (selectedRows.length === 0) return vAlertError('Ï≤òÎ¶¨???Ä?ÅÏùÑ ?†ÌÉù??Ï£ºÏã≠?úÏò§.')

	const msg = searchForm.cfmyn === 'N' ? '?†ÌÉù??Î∂ÄÍ∞Ä?∏Î? ?ëÏàòÏ≤òÎ¶¨ ?òÏãúÍ≤†Ïäµ?àÍπå?' : '?†ÌÉù??Î∂ÄÍ∞Ä?∏Î? Ï∑®ÏÜåÏ≤òÎ¶¨ ?òÏãúÍ≤†Ïäµ?àÍπå?'
	if (!confirm(msg)) return

	try {
		for (const row of selectedRows) {
			await api.post('/api/hatx/HATX_060U_STR', {
				actkind: 'U0',
				cmpycd: authStore.cmpycd,
				taxunit: searchForm.taxunit,
				fromdt: searchForm.fromdt.replace(/-/g, ''),
				todt: searchForm.todt.replace(/-/g, ''),
				cfmyn: searchForm.cfmyn,
				taxkind: '100', // Îß§ÏûÖ
				taxym: row.taxym,
				taxno: row.taxno,
				elcyn: row.elcyn ? 'Y' : 'N',
				userid: authStore.userid
			})
		}

		vAlert('?ïÏÉÅ?ÅÏúºÎ°?Ï≤òÎ¶¨?òÏóà?µÎãà??')
		search()
	} catch (e) { vAlertError('Ï≤òÎ¶¨ Ï§??§Î•ò Î∞úÏÉù') }
}

onMounted(() => {
	fetchOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			selectable: true,
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false },
				{
					title: "?ÑÏûê", field: "elcyn", width: 50, hozAlign: "center",
					formatter: "tickCross", formatterParams: { tickElement: '<i class="bi bi-check-square-fill text-primary"></i>', crossElement: '<i class="bi bi-square text-muted"></i>' },
					cellClick: (e, cell) => { cell.setValue(!cell.getValue()) }
				},
				{
					title: "Î∞úÌñâ??, field: "pubymd", width: 100, hozAlign: "center",
					formatter: (cell) => { const v = cell.getValue(); return v ? `${v.slice(0,4)}-${v.slice(4,6)}-${v.slice(6,8)}` : '' }
				},
				{ title: "?ÅÌò∏", field: "custnm", width: 180 },
				{ title: "?†Ìòï", field: "typenm", width: 80, hozAlign: "center" },
				{ title: "Í≥µÍ∏âÍ∞Ä", field: "supyamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "Î∂ÄÍ∞Ä??, field: "vatamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "?©Í≥Ñ", field: "totamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" },
				{ title: "?ÅÏöî", field: "descnm", widthGrow: 1 }
			]
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; font-size: 12px; }
:deep(.tabulator-selected) { background-color: #e7f1ff !important; }
</style>
