<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: ?†Í∏âÎπÑÏö© Í∏∞Ï¥à?êÎ£å?±Î°ù
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : ?†Í∏âÎπÑÏö© Í∏∞Ï¥à ?êÎ£å Í¥ÄÎ¶?(Í≥ÑÏ†ïÎ≥?Î∞úÏÉù?? Í∏∞Í∞Ñ, ÎØ∏Ï?Í∏âÎπÑ????
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ÅÎã® ?°ÏÖò Î∞?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-check me-2 text-primary" style="font-size: 18px;"></i>
				Í∏∞Î≥∏?ïÎ≥¥ <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">?†Í∏âÎπÑÏö© Í∏∞Ï¥à?êÎ£å?±Î°ù (HABA260U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-plus-lg"></i> ?†Í∑ú
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> Ï°∞Ìöå
				</button>
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-check-lg"></i> ?Ä??
				</button>
			</div>
		</div>

		<!-- ?îç Í≤Ä??Ï°∞Í±¥ ?ÅÏó≠ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>Í≥ÑÏ†ïÍ≥ºÎ™©</span>
							<select v-model="searchForm.acctcd" class="form-select form-select-sm" style="width: 200px;">
								<option v-for="opt in acctOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
							</select>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>Î∞úÏÉù??/span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.symd" type="date" class="form-control form-control-sm" style="width: 140px;" />
								<span>~</span>
								<input v-model="searchForm.eymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- ?ìù ?ÖÎ†• ?ÅÏó≠ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> ?†Í∏âÎπÑÏö© ?ÅÏÑ∏ ?ïÎ≥¥ [{{ masterForm.actkind === 'I1' ? '?†Í∑ú' : '?òÏ†ï' }}]</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">Í≥ÑÏ†ïÍ≥ºÎ™©</th>
							<td class="bg-white border-end px-2 py-1">
								<select v-model="masterForm.acctcd" class="form-select form-select-sm">
									<option v-for="opt in acctOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
								</select>
							</td>
							<th class="text-center bg-light-subtle border-end">ÎØ∏Ï?Í∏âÎπÑ??/th>
							<td class="bg-white border-end px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.UNpayamt" type="number" class="form-control form-control-sm text-end" step="0" />
									<span>(??</span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end">Î∞úÏÉù??/th>
							<td class="bg-white px-2 py-1">
								<input v-model="masterForm.pubymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end">Í∏?Í∞?/th>
							<td class="bg-white border-end px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.fromdt" type="date" class="form-control form-control-sm" style="width: 140px;" />
									<span>~</span>
									<input v-model="masterForm.todt" type="date" class="form-control form-control-sm" style="width: 140px;" />
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end">????/th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.remark" type="text" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end">?¨Ïö©?¨Î?</th>
							<td class="bg-white px-2 py-1">
								<div class="form-check form-check-inline mb-0">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useYnCheck" true-value="Y" false-value="N" />
									<label class="form-check-label" for="useYnCheck">?¨Ïö©</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
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
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// ?†Ïßú Ï¥àÍ∏∞??
const today = new Date().toISOString().split('T')[0]
const firstDay = new Date(new Date().getFullYear(), new Date().getMonth(), 2).toISOString().split('T')[0]

// ?îç Í≤Ä??Ï°∞Í±¥
const searchForm = reactive({
	acctcd: '',
	symd: firstDay,
	eymd: today
})

// ?ìù ÎßàÏä§???∞Ïù¥??
const masterForm = reactive({
	actkind: 'I1',
	slipymd: '',
	slipno: '',
	srowno: '',
	acctcd: '',
	UNpayamt: 0,
	pubymd: today,
	fromdt: firstDay,
	todt: today,
	remark: '',
	useyn: 'Y'
})

const acctOptions = ref<{ value: string; text: string }[]>([])

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const fetchOptions = async () => {
	try {
		const res = await api.post('/api/ha00/HA00_00P_STR', {
			gubun: 'A7',
			cmpycd: authStore.cmpycd,
			search: '110'
		})
		acctOptions.value = res.data?.map((item: any) => ({
			value: item.col3 + item.col0,
			text: item.col1
		})) || []

		if (acctOptions.value.length > 0) {
			searchForm.acctcd = acctOptions.value[0].value
			masterForm.acctcd = acctOptions.value[0].value
		}
	} catch (e) { console.error('Í≥ÑÏ†ïÍ≥ºÎ™© Î°úÎìú ?§Ìå®', e) }
}

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_260U_STR', {
			actkind: 'SR',
			cmpycd: authStore.cmpycd,
			symd: searchForm.symd.replace(/-/g, ''),
			eymd: searchForm.eymd.replace(/-/g, ''),
			slipymd: '',
			slipno: '',
			srowno: '',
			acctcd: searchForm.acctcd ? searchForm.acctcd.substring(3) : '',
			pubymd: ''
		})

		mainGrid?.setData(res.data || [])
		vAlert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { vAlertError('Ï°∞Ìöå Ï§??§Î•ò Î∞úÏÉù') }
}

const save = async () => {
	if (!masterForm.pubymd) return vAlert('Î∞úÏÉù?ºÏùÑ ?†ÌÉù??Ï£ºÏã≠?úÏöî.')
	if (!masterForm.fromdt) return vAlert('Í∏∞Í∞Ñ(FROM)???†ÌÉù??Ï£ºÏã≠?úÏöî.')
	if (!masterForm.todt) return vAlert('Í∏∞Í∞Ñ(TO)???†ÌÉù??Ï£ºÏã≠?úÏöî.')

	try {
		const payload = {
			actkind: masterForm.actkind,
			cmpycd: authStore.cmpycd,
			symd: searchForm.symd.replace(/-/g, ''),
			eymd: searchForm.eymd.replace(/-/g, ''),
			slipymd: masterForm.slipymd.replace(/-/g, ''),
			slipno: masterForm.slipno,
			srowno: masterForm.srowno,
			acctcd: masterForm.acctcd ? masterForm.acctcd.substring(3) : '',
			pubymd: masterForm.pubymd.replace(/-/g, ''),
			UNpayamt: masterForm.UNpayamt || 0,
			fromdt: masterForm.fromdt.replace(/-/g, ''),
			todt: masterForm.todt.replace(/-/g, ''),
			remark: masterForm.remark,
			useyn: masterForm.useyn,
			userid: authStore.userid
		}

		const res = await api.post('/api/haba/HABA_260U_STR', payload)

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].ret_msg)
		} else {
			vAlert('?ïÏÉÅ?ºÎ°ú ?Ä???òÏóà?µÎãà??')
			search()
			initialize()
		}
	} catch (e) { vAlertError('?Ä???§Ìå®') }
}

const initialize = () => {
	const prevacctcd = searchForm.acctcd
	const prevsymd = searchForm.symd
	const preveymd = searchForm.eymd

	resetForm(masterForm)

	masterForm.actkind = 'I1'
	masterForm.acctcd = prevacctcd
	masterForm.pubymd = today
	masterForm.fromdt = firstDay
	masterForm.todt = today
	masterForm.useyn = 'Y'
}

const formatDate = (val: string) => {
	if (!val || val.length !== 8) return ''
	return `${val.substring(0, 4)}-${val.substring(4, 6)}-${val.substring(6, 8)}`
}

onMounted(async () => {
	await fetchOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "Î∞úÏÉù??, field: "acctymd", width: 110, hozAlign: "center", formatter: (cell) => formatDate(cell.getValue()) },
				{ title: "?ÅÏöî", field: "remark", minWidth: 250 },
				{ title: "ÎØ∏Ï?Í∏âÎπÑ??, field: "amt", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "Í∏∞Í∞Ñ", field: "stdymd", width: 220, hozAlign: "center", formatter: (cell) => {
					const data = cell.getRow().getData()
					return `${formatDate(data.stdymd)} ~ ${formatDate(data.endymd)}`
				}},
                { title: "?¨Ïö©", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">?¨Ïö©</b>' : '';
                  }
                }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				masterForm.actkind = 'U1'
				masterForm.slipymd = formatDate(d.slipymd)
				masterForm.slipno = d.slipno
				masterForm.srowno = d.srowno
				masterForm.acctcd = d.TYPESUB + d.acctcd
				masterForm.pubymd = formatDate(d.acctymd)
				masterForm.UNpayamt = Number(d.amt)
				masterForm.fromdt = formatDate(d.stdymd)
				masterForm.todt = formatDate(d.endymd)
				masterForm.remark = d.remark
				masterForm.useyn = d.useyn
			}
		})
		search()
	}
})
</script>

<style scoped>
.erp-label { min-width: 70px; font-weight: 500; font-size: 13px; }
.bg-light-subtle { background-color: #f8f9fa !important; }
:deep(.tabulator-row) { cursor: pointer; }
:deep(.tabulator-row:hover) { background-color: #f0f7ff !important; }
</style>
