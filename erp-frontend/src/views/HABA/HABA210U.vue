<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ê³„ì •ë³„ê¸°ì´ˆìë£Œë“±ë¡?
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ê³„ì •ë³?ê¸°ì´ˆ ?ë£Œ(?´ì›”/?¹ì›”) ?±ë¡ ë°?ê´€ë¦?(?œì? ?ì—… ?ìš©)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journal-text me-2 text-primary" style="font-size: 18px;"></i>
				ê¸°ì´ˆ?ë£Œ <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">ê³„ì •ë³„ê¸°ì´ˆìë£Œë“±ë¡?(HABA210U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-2">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-plus-lg"></i> ? ê·œ
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> ì¡°íšŒ
				</button>
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-check-lg"></i> ?€??
				</button>
			</div>
		</div>

		<!-- ?” ê²€??ì¡°ê±´ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>?Œê³„?„ì›”</span>
							<div class="d-flex align-items-center gap-1">
								<select v-model="searchForm.yyyy" class="form-select form-select-sm" style="width: 80px;" @change="syncSearchToMaster">
									<option v-for="year in years" :key="year" :value="year">{{ year }}</option>
								</select>
								<span>??/span>
								<select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 65px;" @change="syncSearchToMaster">
									<option v-for="month in months" :key="month" :value="month">{{ month }}</option>
								</select>
								<span>??/span>
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>êµ?ë¶?/span>
							<select v-model="searchForm.mmgbn" class="form-select form-select-sm" style="width: 80px;" @change="syncSearchToMaster">
								<option value="00">?´ì›”</option>
								<option value="88">?¹ì›”</option>
							</select>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>ê³„ì •ê³¼ëª©</span>
							<div class="d-flex align-items-center gap-1">
								<div class="input-group input-group-sm" style="width: 180px;">
									<input v-model="searchForm.acctcd1" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchForm.acctcd1_T" type="text" class="form-control" placeholder="ê³„ì •ëª? @keydown.enter="handleOpenHelp('S1')" />
									<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('S1')"><i class="bi bi-search"></i></button>
								</div>
								<span>~</span>
								<div class="input-group input-group-sm" style="width: 180px;">
									<input v-model="searchForm.acctcd2" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchForm.acctcd2_T" type="text" class="form-control" placeholder="ê³„ì •ëª? @keydown.enter="handleOpenHelp('S2')" />
									<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('S2')"><i class="bi bi-search"></i></button>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- ?“ ?…ë ¥ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> ê³„ì • ê¸°ì´ˆ ?ì„¸ ?•ë³´ [{{ masterForm.actkind === 'I1' ? '? ê·œ' : '?˜ì •' }}]</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 30%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">?Œê³„?„ì›”</th>
							<td class="bg-white border-end px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<select v-model="masterForm.yyyy" class="form-select form-select-sm" style="width: 80px;">
										<option v-for="year in years" :key="year" :value="year">{{ year }}</option>
									</select>
									<span>??/span>
									<select v-model="masterForm.mm" class="form-select form-select-sm" style="width: 65px;">
										<option v-for="month in months" :key="month" :value="month">{{ month }}</option>
									</select>
									<span>??/span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end">ê³„ì •ê³¼ëª©</th>
							<td class="bg-white border-end px-2 py-1" colspan="3">
								<div class="input-group input-group-sm" style="max-width: 300px;">
									<input v-model="masterForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
									<input v-model="masterForm.acctcd_t" type="text" class="form-control" @keydown.enter="handleOpenHelp('M1')" />
									<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('M1')"><i class="bi bi-search"></i></button>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end">êµ?ë¶?/th>
							<td class="bg-white border-end px-2 py-1">
								<select v-model="masterForm.mmgbn" class="form-select form-select-sm">
									<option value="88">?¹ì›”</option>
									<option value="00">?´ì›”</option>
								</select>
							</td>
							<th class="text-center bg-light-subtle border-end">ì°¨ë?ê¸ˆì•¡</th>
							<td class="bg-white border-end px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.dbamt" type="number" class="form-control form-control-sm text-end" step="0" />
									<span>(??</span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end">?€ë³€ê¸ˆì•¡</th>
							<td class="bg-white px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.cramt" type="number" class="form-control form-control-sm text-end" step="0" />
									<span>(??</span>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<div class="px-2 py-1 text-muted small">
				???´ì›” ?ë£Œ???´ì›” ?”ì•¡?? ?¹ì›” ?ë£Œ???¹ì›” ë°œìƒ ê¸ˆì•¡???˜ë??©ë‹ˆ??
			</div>
		</div>

		<!-- ?“Š ê·¸ë¦¬???ì—­ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
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
import { ref, reactive, onMounted, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

// ?„ë„/???µì…˜
const currentYear = new Date().getFullYear()
const years = computed(() => Array.from({ length: 10 }, (_, i) => String(currentYear - i)))
const months = computed(() => Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0')))

// ?” ê²€??ì¡°ê±´
const searchForm = reactive({
	yyyy: String(currentYear),
    mm: String(new Date().getMonth() + 1).padStart(2, '0'),
    mmgbn: '00',
	acctcd1: '',
	acctcd1_T: '',
	acctcd2: '',
	acctcd2_T: ''
})

// ?“ ë§ˆìŠ¤???°ì´??
const masterForm = reactive({
	actkind: 'I1',
	yyyy: String(currentYear),
    mm: String(new Date().getMonth() + 1).padStart(2, '0'),
	acctcd: '',
	acctcd_t: '',
    mmgbn: '88',
	dbamt: 0,
	cramt: 0
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_210U_STR', {
			actkind: 'SR',
			cmpycd: authStore.cmpycd,
			acctcd1: searchForm.acctcd1,
			acctcd2: searchForm.acctcd2,
			acctym: searchForm.yyyy + searchForm.mm,
		    mmgbn: searchForm.mmgbn,
			acctcd: ''
		})

		mainGrid?.setData(res.data || [])
		vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) { vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

const save = async () => {
	if (!masterForm.yyyy || !masterForm.mm) return vAlert('?Œê³„?„ì›”??? íƒ??ì£¼ì‹­?œìš”.')
	if (!masterForm.acctcd) return vAlert('ê³„ì •ê³¼ëª©??? íƒ??ì£¼ì‹­?œìš”.')
	if (!masterForm.mmgbn) return vAlert('?Œê³„êµ¬ë¶„??? íƒ??ì£¼ì‹­?œìš”.')

	try {
		const payload = {
			actkind: masterForm.actkind,
			cmpycd: authStore.cmpycd,
			acctcd1: searchForm.acctcd1,
			acctcd2: searchForm.acctcd2,
			acctym: masterForm.yyyy + masterForm.mm,
		    mmgbn: masterForm.mmgbn,
			acctcd: masterForm.acctcd,
			dbamt: masterForm.dbamt || 0,
			cramt: masterForm.cramt || 0,
			acctnm: masterForm.acctcd_t
		}

		const res = await api.post('/api/haba/HABA_210U_STR', payload)

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].ret_msg)
		} else {
			vAlert('?•ìƒ?¼ë¡œ ?€???˜ì—ˆ?µë‹ˆ??')
			search()
			initialize()
		}
	} catch (e) { vAlertError('?€???¤íŒ¨') }
}

const initialize = () => {
	const prevYyyy = masterForm.yyyy
	const prevMm = masterForm.mm
	const prevMmgbn = masterForm.mmgbn

	resetForm(masterForm)

	masterForm.actkind = 'I1'
	masterForm.yyyy = prevYyyy
	masterForm.mm = prevMm
	masterForm.mmgbn = prevMmgbn
	masterForm.dbamt = 0
	masterForm.cramt = 0
}

const syncSearchToMaster = () => {
	masterForm.yyyy = searchForm.yyyy
	masterForm.mm = searchForm.mm
	masterForm.mmgbn = searchForm.mmgbn
}

/**
 * ?’¡ ê³„ì •ê³¼ëª© ?„ì?ì°??¸ì¶œ (useCommonHelp ?œì? ë°©ì‹)
 */
function handleOpenHelp(type: string) {
	let searchVal = ''
	if (type === 'S1') searchVal = searchForm.acctcd1_T
	else if (type === 'S2') searchVal = searchForm.acctcd2_T
	else if (type === 'M1') searchVal = masterForm.acctcd_t

	openHelp('ACCT', (d: any) => {
		if (type === 'S1') {
			searchForm.acctcd1 = d.ACCTCD
			searchForm.acctcd1_T = d.ACCTNM
		} else if (type === 'S2') {
			searchForm.acctcd2 = d.ACCTCD
			searchForm.acctcd2_T = d.ACCTNM
		} else if (type === 'M1') {
			masterForm.acctcd = d.ACCTCD
			masterForm.acctcd_t = d.ACCTNM
		}
	}, { search: searchVal })
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "ê³„ì •ê³¼ëª©", field: "col2", width: 120, hozAlign: "center" },
				{ title: "ê³???ê³?ëª?ëª?, field: "col3", minWidth: 200 },
				{ title: "ì°?ë³€", field: "col5", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "?€ ë³€", field: "col6", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "????, field: "col7", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } }
			],
		})
        mainGrid.on("rowClick", (e, row) => {
            const d = row.getData()
            // col0: yyyy, col1: mm, col2: acctcd, col3: acctnm, col4: mmgbn, col5: dbamt, col6: cramt
            masterForm.actkind = 'U1'
            masterForm.yyyy = d.col0
            masterForm.mm = d.col1
            masterForm.acctcd = d.col2
            masterForm.acctcd_t = d.col3
            masterForm.mmgbn = d.col4
            masterForm.dbamt = Number(d.col5)
            masterForm.cramt = Number(d.col6)
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
