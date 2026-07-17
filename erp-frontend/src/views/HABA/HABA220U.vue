<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ê±°ë˜ì²˜ë³„ ?”ì•¡?±ë¡
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ê³„ì •ê³¼ëª© ë°?ê±°ë˜ì²˜ë³„ ê¸°ì´ˆ ?”ì•¡(?´ì›”/?¹ì›”) ?±ë¡ ë°?ê´€ë¦?
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ë‹¨ ?¡ì…˜ ë°?(?œì? ê·œê²©) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-person-lines-fill me-2 text-primary" style="font-size: 18px;"></i>
				ê¸°ì´ˆ?ë£Œ <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">ê±°ë˜ì²˜ë³„ ?”ì•¡?±ë¡ (HABA220U)</span>
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

		<!-- ?” ê²€??ì¡°ê±´ ?ì—­ (?œì? erp-label ?ìš©) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3">
						<div class="d-flex align-items-center">
							<span class="erp-label">?Œê³„?„ì›”</span>
							<div class="d-flex align-items-center gap-1">
								<select v-model="searchForm.yyyy" class="form-select" style="width: 80px;" @change="syncSearchToMaster">
									<option v-for="year in years" :key="year" :value="year">{{ year }}</option>
								</select>
								<span class="small fw-bold">??/span>
								<select v-model="searchForm.mm" class="form-select" style="width: 65px;" @change="syncSearchToMaster">
									<option v-for="month in months" :key="month" :value="month">{{ month }}</option>
								</select>
								<span class="small fw-bold">??/span>
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label">êµ?ë¶?/span>
							<select v-model="searchForm.mmgbn" class="form-select" style="width: 80px;" @change="syncSearchToMaster">
								<option value="00">?´ì›”</option>
								<option value="88">?¹ì›”</option>
							</select>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label">ê³„ì •ê³¼ëª©</span>
							<div class="input-group" style="width: 200px;">
								<input v-model="searchForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
								<input v-model="searchForm.acctnm" type="text" class="form-control" placeholder="ê³„ì •ëª? @keydown.enter="openHelp('S_ACCT')" />
								<button class="btn" @click="openHelp('S_ACCT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label">ê±???ì²?/span>
							<div class="d-flex align-items-center gap-1">
								<div class="input-group" style="width: 180px;">
									<input v-model="searchForm.custcd1" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
									<input v-model="searchForm.custnm1" type="text" class="form-control" placeholder="?œì‘" @keydown.enter="openHelp('S_CUST1')" />
									<button class="btn" @click="openHelp('S_CUST1')"><i class="bi bi-search"></i></button>
								</div>
								<span class="text-muted">~</span>
								<div class="input-group" style="width: 180px;">
									<input v-model="searchForm.custcd2" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
									<input v-model="searchForm.custnm2" type="text" class="form-control" placeholder="ì¢…ë£Œ" @keydown.enter="openHelp('S_CUST2')" />
									<button class="btn" @click="openHelp('S_CUST2')"><i class="bi bi-search"></i></button>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- ?“ ?…ë ¥ ?ì—­ (?œì? erp-table-full ?ìš©) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-3 bg-white border-bottom d-flex align-items-center">
					<i class="bi bi-pencil-square me-2 text-primary"></i>
					<span class="fw-bold small text-dark">ê¸°ì´ˆ ?”ì•¡ ?ì„¸ ?…ë ¥ [{{ masterForm.actkind === 'I1' ? '? ê·œ?±ë¡' : '?•ë³´?˜ì •' }}]</span>
				</div>
				<table class="erp-table-full">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th>?Œê³„?„ì›”</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-1 px-1">
									<select v-model="masterForm.yyyy" class="form-select" style="width: 80px;">
										<option v-for="year in years" :key="year" :value="year">{{ year }}</option>
									</select>
									<span class="small fw-bold">??/span>
									<select v-model="masterForm.mm" class="form-select" style="width: 65px;">
										<option v-for="month in months" :key="month" :value="month">{{ month }}</option>
									</select>
									<span class="small fw-bold">??/span>
								</div>
							</td>
							<th>êµ?ë¶?/th>
							<td class="bg-white" colspan="3">
								<select v-model="masterForm.mmgbn" class="form-select" style="max-width: 100px;">
									<option value="88">?¹ì›”</option>
									<option value="00">?´ì›”</option>
								</select>
							</td>
						</tr>
						<tr>
							<th class="required">ê³„ì •ê³¼ëª©</th>
							<td class="bg-white">
								<div class="input-group px-1">
									<input v-model="masterForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
									<input v-model="masterForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp('M_ACCT')" />
									<button class="btn" @click="openHelp('M_ACCT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="required">ê±???ì²?/th>
							<td class="bg-white" colspan="3">
								<div class="input-group px-1" style="max-width: 350px;">
									<input v-model="masterForm.custcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
									<input v-model="masterForm.custnm" type="text" class="form-control" @keydown.enter="openHelp('M_CUST')" />
									<button class="btn" @click="openHelp('M_CUST')"><i class="bi bi-search"></i></button>
								</div>
							</td>
						</tr>
						<tr>
							<th>ì°¨ë?ê¸ˆì•¡</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-1 px-1">
									<input v-model="masterForm.dbamt" type="number" class="form-control text-end fw-bold text-primary" />
									<span class="small text-muted">(??</span>
								</div>
							</td>
							<th>?€ë³€ê¸ˆì•¡</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-1 px-1">
									<input v-model="masterForm.cramt" type="number" class="form-control text-end fw-bold text-danger" />
									<span class="small text-muted">(??</span>
								</div>
							</td>
							<td colspan="2" class="bg-white"></td>
						</tr>
					</tbody>
				</table>
			</div>
			<div class="px-2 py-1 text-muted" style="font-size: 11px;">
				<i class="bi bi-info-circle me-1"></i> ?´ì›” ?ë£Œ???„ê¸°?´ì›” ?”ì•¡?? ?¹ì›” ?ë£Œ???¹ì›” ê¸°ì´ˆ ë°œìƒ ê¸ˆì•¡???˜ë??©ë‹ˆ??
			</div>
		</div>

		<!-- ?“Š ê·¸ë¦¬???ì—­ (?œì? ?µí•©CSS ?ìš©) -->
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

const currentYear = new Date().getFullYear()
const years = computed(() => Array.from({ length: 10 }, (_, i) => String(currentYear - i)))
const months = computed(() => Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0')))

const searchForm = reactive({
	yyyy: String(currentYear),
    mm: String(new Date().getMonth() + 1).padStart(2, '0'),
    mmgbn: '00',
	acctcd: '',
	acctnm: '',
	custgbn: '',
	custcd1: '',
	custnm1: '',
	custcd2: '',
	custnm2: ''
})

const masterForm = reactive({
	actkind: 'I1',
	yyyy: String(currentYear),
    mm: String(new Date().getMonth() + 1).padStart(2, '0'),
    mmgbn: '88',
	acctcd: '',
	acctnm: '',
	custcd: '',
	custnm: '',
	custgbn: '',
	dbamt: 0,
	cramt: 0
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.acctcd) return vAlert('ê²€?‰í•  ê³„ì •ê³¼ëª©??? íƒ??ì£¼ì‹­?œìš”.')
	try {
		const res = await api.post('/api/haba/HABA_220U_STR', {
			actkind: 'SR', cmpycd: authStore.cmpycd, acctym: searchForm.yyyy + searchForm.mm,
		    mmgbn: searchForm.mmgbn, acctcd: searchForm.acctcd, custcd: '',
			custcd1: searchForm.custcd1, custcd2: searchForm.custcd2
		})
		mainGrid?.setData(res.data || [])
		vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) { vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

const save = async () => {
	if (!masterForm.acctcd) return vAlert('ê³„ì •ê³¼ëª©??? íƒ??ì£¼ì‹­?œìš”.')
	if (!masterForm.custcd) return vAlert('ê±°ë˜ì²˜ë? ? íƒ??ì£¼ì‹­?œìš”.')
	try {
		const res = await api.post('/api/haba/HABA_220U_STR', {
			...masterForm, actkind: masterForm.actkind, cmpycd: authStore.cmpycd,
			acctym: masterForm.yyyy + masterForm.mm, custcd1: searchForm.custcd1, custcd2: searchForm.custcd2
		})
		if (res.data?.[0]?.ret_yn === 'Y') { vAlertError(res.data[0].ret_msg) }
		else { vAlert('?•ìƒ?¼ë¡œ ?€???˜ì—ˆ?µë‹ˆ??'); search(); initialize() }
	} catch (e) { vAlertError('?€???¤íŒ¨') }
}

const initialize = () => {
	const prev = { ...masterForm }
	resetForm(masterForm)
	Object.assign(masterForm, { actkind: 'I1', yyyy: prev.yyyy, mm: prev.mm, mmgbn: prev.mmgbn, acctcd: prev.acctcd, acctnm: prev.acctnm, custgbn: prev.custgbn })
}

const syncSearchToMaster = () => { masterForm.yyyy = searchForm.yyyy; masterForm.mm = searchForm.mm; masterForm.mmgbn = searchForm.mmgbn }

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	let searchVal = type === 'S_ACCT' ? searchForm.acctnm : type === 'S_CUST1' ? searchForm.custnm1 : type === 'S_CUST2' ? searchForm.custnm2 : type === 'M_ACCT' ? masterForm.acctnm : masterForm.custnm
	if (type.includes('CUST') && (!searchForm.acctcd && !masterForm.acctcd)) return vAlert("ê³„ì •ê³¼ëª©???°ì„  ? íƒ??ì£¼ì‹­?œìš”.")

	if (type.includes('ACCT')) {
		Object.assign(modalProps, {
			title: 'ê³„ì •ê³¼ëª© ? íƒ', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'SUB', cmpycd: authStore.cmpycd, search: searchVal },
			columns: [{ title: 'ì½”ë“œ', field: 'col0', width: 100, hozAlign: 'center' }, { title: 'ê³„ì •ëª?, field: 'col1', width: 250 }],
			onConfirm: (d: any) => { if (type === 'S_ACCT') { searchForm.acctcd = d.col0; searchForm.acctnm = d.col1; searchForm.custgbn = d.col2 } else { masterForm.acctcd = d.col0; masterForm.acctnm = d.col1; masterForm.custgbn = d.col2 } }
		})
	} else {
		const custGbn = type.startsWith('S') ? searchForm.custgbn : masterForm.custgbn
		Object.assign(modalProps, {
			title: 'ê±°ë˜ì²?? íƒ', path: '/api/ha00/HA00_03P_STR', data: { custgbn: custGbn, cmpycd: authStore.cmpycd, search: searchVal },
			columns: [{ title: 'ì½”ë“œ', field: 'col0', width: 100, hozAlign: 'center' }, { title: 'ê±°ë˜ì²˜ëª…', field: 'col1', width: 250 }],
			onConfirm: (d: any) => { if (type === 'S_CUST1') { searchForm.custcd1 = d.col0; searchForm.custnm1 = d.col1 } else if (type === 'S_CUST2') { searchForm.custcd2 = d.col0; searchForm.custnm2 = d.col1 } else { masterForm.custcd = d.col0; masterForm.custnm = d.col1 } }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columns: [
				{ title: "ê±°ë˜ì²˜ì½”??, field: "col4", width: 100, hozAlign: "center" },
				{ title: "ê±°ë˜ì²˜ëª…", field: "col5", minWidth: 200, cssClass: "fw-bold" },
				{ title: "ì°?ë³€", field: "col7", width: 140, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "?€ ë³€", field: "col8", width: 140, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "????, field: "col9", width: 140, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", cssClass: "fw-bold text-primary" }
			],
			rowClick: (e, row) => { const d = row.getData(); Object.assign(masterForm, { actkind: 'U1', yyyy: d.col0, mm: d.col1, mmgbn: d.col6, acctcd: d.col2, acctnm: d.col3, custcd: d.col4, custnm: d.col5, dbamt: Number(d.col7), cramt: Number(d.col8) }) }
		})
	}
})
</script>
