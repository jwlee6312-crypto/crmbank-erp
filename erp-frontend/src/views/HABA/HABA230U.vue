<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ê´€ë¦¬ë²ˆ???”ì•¡?±ë¡
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ê³„ì •ê³¼ëª© ë°?ê´€ë¦¬ë²ˆ?¸ë³„ ê¸°ì´ˆ ?”ì•¡(?´ì›”/?¹ì›”) ?±ë¡ ë°?ê´€ë¦?
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-tag-fill me-2 text-primary" style="font-size: 18px;"></i>
				ê¸°ë³¸?•ë³´ <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">ê´€ë¦¬ë²ˆ???”ì•¡?±ë¡ (HABA230U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
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
							<div class="input-group input-group-sm" style="width: 200px;">
								<input v-model="searchForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
								<input v-model="searchForm.acctnm" type="text" class="form-control" placeholder="ê³„ì •ëª? @keydown.enter="openHelp('S_ACCT')" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('S_ACCT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>ê´€ë¦¬ë²ˆ??/span>
							<div class="d-flex align-items-center gap-1">
								<div class="input-group input-group-sm" style="width: 180px;">
									<input v-model="searchForm.mgtno1" type="text" class="form-control" placeholder="ê´€ë¦¬ë²ˆ?? @keydown.enter="openHelp('S_MGT1')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('S_MGT1')"><i class="bi bi-search"></i></button>
								</div>
								<span>~</span>
								<div class="input-group input-group-sm" style="width: 180px;">
									<input v-model="searchForm.mgtno2" type="text" class="form-control" placeholder="ê´€ë¦¬ë²ˆ?? @keydown.enter="openHelp('S_MGT2')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('S_MGT2')"><i class="bi bi-search"></i></button>
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
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> ê´€ë¦¬ë²ˆ???”ì•¡ ?ì„¸ ?•ë³´ [{{ masterForm.actkind === 'I1' ? '? ê·œ' : '?˜ì •' }}]</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 20%;" />
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
							<th class="text-center bg-light-subtle border-end">êµ?ë¶?/th>
							<td class="bg-white border-end px-2 py-1" colspan="3">
								<select v-model="masterForm.mmgbn" class="form-select form-select-sm" style="max-width: 100px;">
									<option value="88">?¹ì›”</option>
									<option value="00">?´ì›”</option>
								</select>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end">ê³„ì •ê³¼ëª©</th>
							<td class="bg-white border-end px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
									<input v-model="masterForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp('M_ACCT')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('M_ACCT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end">ê´€ë¦¬ë²ˆ??/th>
							<td class="bg-white border-end px-2 py-1" colspan="3">
								<div class="input-group input-group-sm" style="max-width: 350px;">
									<input v-model="masterForm.mgtno" type="text" class="form-control text-center bg-light" style="max-width: 120px;" readonly />
									<input v-model="masterForm.mgtnm" type="text" class="form-control" @keydown.enter="openHelp('M_MGT')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('M_MGT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end">ì°¨ë?ê¸ˆì•¡</th>
							<td class="bg-white border-end px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.dbamt" type="number" class="form-control form-control-sm text-end" step="0" />
									<span>(??</span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end">?€ë³€ê¸ˆì•¡</th>
							<td class="bg-white border-end px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.cramt" type="number" class="form-control form-control-sm text-end" step="0" />
									<span>(??</span>
								</div>
							</td>
							<td colspan="2" class="bg-white px-2 py-1"></td>
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
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// ?„ë„/???µì…˜
const currentYear = new Date().getFullYear()
const years = computed(() => Array.from({ length: 10 }, (_, i) => String(currentYear - i)))
const months = computed(() => Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0')))

// ?” ê²€??ì¡°ê±´
const searchForm = reactive({
	yyyy: String(currentYear),
    mm: String(new Date().getMonth() + 1).padStart(2, '0'),
    mmgbn: '00',
	acctcd: '',
	acctnm: '',
	mgtgbn: '',
	mgtno1: '',
	mgtnm1: '',
	mgtno2: '',
	mgtnm2: ''
})

// ?“ ë§ˆìŠ¤???°ì´??
const masterForm = reactive({
	actkind: 'I1',
	yyyy: String(currentYear),
    mm: String(new Date().getMonth() + 1).padStart(2, '0'),
    mmgbn: '88',
	acctcd: '',
	acctnm: '',
	mgtno: '',
	mgtnm: '',
	mgtgbn: '',
	dbamt: 0,
	cramt: 0
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.acctcd) return vAlert('ê²€?‰í•  ê³„ì •ê³¼ëª©??? íƒ??ì£¼ì‹­?œìš”.')

	try {
		const res = await api.post('/api/haba/HABA_230U_STR', {
			actkind: 'SR',
			cmpycd: authStore.cmpycd,
			acctym: searchForm.yyyy + searchForm.mm,
		    mmgbn: searchForm.mmgbn,
			acctcd: searchForm.acctcd,
			mgtno: '',
			mgtno1: searchForm.mgtno1,
			mgtno2: searchForm.mgtno2
		})

		mainGrid?.setData(res.data || [])
		vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) { vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

const save = async () => {
	if (!masterForm.acctcd) return vAlert('ê³„ì •ê³¼ëª©??? íƒ??ì£¼ì‹­?œìš”.')
	if (!masterForm.mgtno) return vAlert('ê´€ë¦¬ë²ˆ?¸ë? ? íƒ??ì£¼ì‹­?œìš”.')

	try {
		const payload = {
			actkind: masterForm.actkind,
			cmpycd: authStore.cmpycd,
			acctym: masterForm.yyyy + masterForm.mm,
		    mmgbn: masterForm.mmgbn,
			acctcd: masterForm.acctcd,
			mgtno: masterForm.mgtno,
			mgtno1: searchForm.mgtno1,
			mgtno2: searchForm.mgtno2,
			dbamt: masterForm.dbamt || 0,
			cramt: masterForm.cramt || 0,
			mgtnm: masterForm.mgtnm
		}

		const res = await api.post('/api/haba/HABA_230U_STR', payload)

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
	const prevyyyy = masterForm.yyyy
	const prevmm = masterForm.mm
	const prevmmgbn = masterForm.mmgbn
	const prevacctcd = masterForm.acctcd
	const prevacctnm = masterForm.acctnm
	const prevmgtgbn = masterForm.mgtgbn

	resetForm(masterForm)

	masterForm.actkind = 'I1'
	masterForm.yyyy = prevyyyy
	masterForm.mm = prevmm
	masterForm.mmgbn = prevmmgbn
	masterForm.acctcd = prevacctcd
	masterForm.acctnm = prevacctnm
	masterForm.mgtgbn = prevmgtgbn
	masterForm.dbamt = 0
	masterForm.cramt = 0
}

const syncSearchToMaster = () => {
	masterForm.yyyy = searchForm.yyyy
	masterForm.mm = searchForm.mm
	masterForm.mmgbn = searchForm.mmgbn
}

// ?ì—… ?¤ì •
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	let searchVal = ''
	if (type === 'S_ACCT') searchVal = searchForm.acctnm
	if (type === 'S_MGT1') searchVal = searchForm.mgtno1
	if (type === 'S_MGT2') searchVal = searchForm.mgtno2
	if (type === 'M_ACCT') searchVal = masterForm.acctnm
	if (type === 'M_MGT') searchVal = masterForm.mgtnm

	if (type.includes('MGT') && (!searchForm.acctcd && !masterForm.acctcd)) {
		return vAlert("ê³„ì •ê³¼ëª©???°ì„  ? íƒ??ì£¼ì‹­?œìš”.")
	}

	if (type.includes('ACCT')) {
		Object.assign(modalProps, {
			title: 'ê³„ì •ê³¼ëª© ? íƒ',
			path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'MGT', cmpycd: authStore.cmpycd, search: searchVal },
			columns: [
				{ title: 'ì½”ë“œ', field: 'col0', width: 100, hozAlign: 'center' },
				{ title: 'ê³„ì •ëª?, field: 'col1', width: 250 },
				{ title: 'ê´€ë¦¬êµ¬ë¶?, field: 'col2', visible: false }
			],
			onConfirm: (d: any) => {
				if (type === 'S_ACCT') {
					searchForm.acctcd = d.col0
					searchForm.acctnm = d.col1
					searchForm.mgtgbn = d.col2
				} else {
					masterForm.acctcd = d.col0
					masterForm.acctnm = d.col1
					masterForm.mgtgbn = d.col2
				}
			}
		})
	} else {
		const mgtGbn = type.startsWith('S') ? searchForm.mgtgbn : masterForm.mgtgbn
		const acctCd = type.startsWith('S') ? searchForm.acctcd : masterForm.acctcd
		Object.assign(modalProps, {
			title: 'ê´€ë¦¬ë²ˆ??? íƒ',
			path: '/api/ha00/HA00_05P_STR', // HELP_mgtno.asp ?€??API ?ˆìƒ
			data: { mgtgbn: mgtGbn, acctcd: acctCd, cmpycd: authStore.cmpycd, search: searchVal },
			columns: [
				{ title: 'ê´€ë¦¬ë²ˆ??, field: 'col0', width: 150, hozAlign: 'center' },
				{ title: 'ê´€ë¦¬ë²ˆ?¸ëª…', field: 'col1', width: 250 }
			],
			onConfirm: (d: any) => {
				if (type === 'S_MGT1') {
					searchForm.mgtno1 = d.col0
					searchForm.mgtnm1 = d.col1
				} else if (type === 'S_MGT2') {
					searchForm.mgtno2 = d.col0
					searchForm.mgtnm2 = d.col1
				} else {
					masterForm.mgtno = d.col0
					masterForm.mgtnm = d.col1
				}
			}
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
				{ title: "ê´€ë¦¬ë²ˆ??, field: "col4", width: 150, hozAlign: "center" },
				{ title: "ê´€ ë¦?ë²???ëª?, field: "col5", minWidth: 200 },
				{ title: "ì°?ë³€", field: "col7", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 } },
				{ title: "?€ ë³€", field: "col8", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 } },
				{ title: "????, field: "col9", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 } }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				// col0: yyyy, col1:.mm, col2: acctcd, col3: acctnm, col4: mgtno, col5: mgtnm, col6:.mmgbn, col7: dbamt, col8: cramt
				masterForm.actkind = 'U1'
				masterForm.yyyy = d.col0
				masterForm.mm = d.col1
				masterForm.mmgbn = d.col6
				masterForm.acctcd = d.col2
				masterForm.acctnm = d.col3
				masterForm.mgtno = d.col4
				masterForm.mgtnm = d.col5
				masterForm.dbamt = Number(d.col7)
				masterForm.cramt = Number(d.col8)
			}
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 70px; font-weight: 500; font-size: 13px; }
.bg-light-subtle { background-color: #f8f9fa !important; }
:deep(.tabulator-row) { cursor: pointer; }
:deep(.tabulator-row:hover) { background-color: #f0f7ff !important; }
:deep(.tabulator-footer) { font-weight: bold; background-color: #dfd9bd !important; }
</style>
