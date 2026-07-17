<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ì§€ê¸‰ì–´?Œê¸°ì´ˆìë£?
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ì§€ê¸‰ì–´?Œì˜ ê¸°ì´ˆ ?•ë³´ë¥??±ë¡?˜ê³  ê´€ë¦¬í•˜???”ë©´
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-ruled me-2 text-primary" style="font-size: 18px;"></i>
				ê¸°ë³¸?•ë³´ <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">ì§€ê¸‰ì–´?Œê¸°ì´ˆìë£?(HABA160U)</span>
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
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> ?¸ì‡„
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> ?‘ì?
				</button>
				<button v-if="masterForm.actkind === 'U1'" class="btn-erp btn-delete" @click="deleteData">
					<i class="bi bi-trash"></i> ?? œ
				</button>
			</div>
		</div>

		<!-- ?” ê²€??ì¡°ê±´ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>?´ìŒë²ˆí˜¸</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.billno" type="text" class="form-control form-control-sm" style="width: 150px;" maxlength="14" @keydown.enter="search" />
								<span>~</span>
								<input v-model="searchForm.billno_TO" type="text" class="form-control form-control-sm" style="width: 150px;" maxlength="14" @keydown.enter="search" />
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
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> ?´ìŒ ?ì„¸ ?•ë³´ [{{ masterForm.actkind === 'I1' ? '? ê·œ' : '?˜ì •' }}]</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">?´ìŒë²ˆí˜¸</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.billno" type="text" class="form-control form-control-sm" maxlength="14" :readonly="masterForm.actkind === 'U1'" />
							</td>
							<th class="text-center bg-light-subtle border-end">ë°œí–‰?€??/th>
							<td class="bg-white border-end px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.bankcd" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
									<input v-model="masterForm.banknm" type="text" class="form-control" @keydown.enter="openHelp('BANK')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('BANK')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end">ë°œí–‰??/th>
							<td class="bg-white px-2 py-1">
								<input v-model="masterForm.issuman" type="text" class="form-control form-control-sm" maxlength="20" />
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end">ë°œí–‰?¼ì</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.stdymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end">ë§Œê¸°?¼ì</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.endymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end">ê¸???/th>
							<td class="bg-white px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.billamt" type="number" class="form-control form-control-sm text-end" step="0" />
									<span>(??</span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end">?´ìŒ? í˜•</th>
							<td class="bg-white border-end px-2 py-1">
								<select v-model="masterForm.billtype" class="form-select form-select-sm">
									<option v-for="opt in billTypeOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
								</select>
							</td>
							<th class="text-center bg-light-subtle border-end">ì§€ê¸‰ê±°?˜ì²˜</th>
							<td class="bg-white border-end px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.custcd" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
									<input v-model="masterForm.custnm" type="text" class="form-control" @keydown.enter="openHelp('CUST')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end">?¬ìš©?¬ë?</th>
							<td class="bg-white px-2 py-1">
								<div class="form-check form-check-inline mb-0">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useYnCheck" true-value="Y" false-value="N" />
									<label class="form-check-label" for="useYnCheck">?¬ìš©</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
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
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import * as XLSX from 'xlsx'
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

// ?” ê²€??ì¡°ê±´
const searchForm = reactive({
	billno: '',
	billno_TO: ''
})

// ?“ ë§ˆìŠ¤???°ì´??
const masterForm = reactive({
	actkind: 'I1',
	billno: '',
	bankcd: '',
	banknm: '',
	issuman: '',
	stdymd: '',
	endymd: '',
	billamt: 0,
	billtype: '',
	custcd: '',
	custnm: '',
	useyn: 'Y'
})

const billTypeOptions = ref<{ value: string; text: string }[]>([])

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const fetchOptions = async () => {
	try {
		// ?´ìŒ? í˜• (170)
		const resType = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: ' ', search: '170' })
		billTypeOptions.value = resType.data?.map((i: any) => ({ value: i.col0, text: i.col1 })) || []
		if (billTypeOptions.value.length > 0) masterForm.billtype = billTypeOptions.value[0].value
	} catch (e) { console.error('Options Load Failed', e) }
}

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_160U_STR', {
			actkind: 'S1',
			cmpycd: authStore.cmpycd,
			BILLGU: '100',
			billno: searchForm.billno,
			billno_TO: searchForm.billno_TO
		})
		mainGrid?.setData(res.data || [])
		if (res.data?.length > 0) vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
		else vAlert('?°ì´?°ê? ì¡´ì¬?˜ì? ?ŠìŠµ?ˆë‹¤.')
	} catch (e) { vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

const save = async () => {
	if (!masterForm.billno) return vAlert('?´ìŒë²ˆí˜¸ë¥??…ë ¥?˜ì„¸??')
	if (!masterForm.banknm) return vAlert('ë°œí–‰?€?‰ì„ ?…ë ¥?˜ì„¸??')
	if (!masterForm.issuman) return vAlert('ë°œí–‰?¸ì„ ?…ë ¥?˜ì„¸??')
	if (!masterForm.stdymd) return vAlert('ë°œí–‰?¼ìë¥??…ë ¥?˜ì„¸??')
	if (!masterForm.endymd) return vAlert('ë§Œê¸°?¼ìë¥??…ë ¥?˜ì„¸??')
	if (!masterForm.billamt) return vAlert('?´ìŒê¸ˆì•¡???…ë ¥?˜ì„¸??')
	if (!masterForm.custcd) return vAlert('ì§€ê¸‰ê±°?˜ì²˜ë¥?? íƒ?˜ì„¸??')

	try {
		const payload = {
			actkind: masterForm.actkind,
			cmpycd: authStore.cmpycd,
			BILLGU: '100',
			billno: masterForm.billno,
			billno_TO: '',
			issuman: masterForm.issuman,
			bankcd: masterForm.bankcd,
			stdymd: masterForm.stdymd.replace(/-/g, ''),
			endymd: masterForm.endymd.replace(/-/g, ''),
			billamt: masterForm.billamt,
			custcd: masterForm.custcd,
			billtype: masterForm.billtype,
			useyn: masterForm.useyn,
			userid: authStore.userid
		}

		const res = await api.post('/api/haba/HABA_160U_STR', payload)
		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].ret_msg)
		} else {
			vAlert('?•ìƒ?¼ë¡œ ?€???˜ì—ˆ?µë‹ˆ??')
			search()
			initialize()
		}
	} catch (e) { vAlertError('?€???¤íŒ¨') }
}

const deleteData = async () => {
	if (masterForm.actkind !== 'U1' || !masterForm.billno) return

	if (!confirm('?•ë§ë¡??? œ?˜ì‹œê² ìŠµ?ˆê¹Œ?')) return

	try {
		const res = await api.post('/api/haba/HABA_160U_STR', {
			actkind: 'D1',
			cmpycd: authStore.cmpycd,
			BILLGU: '100',
			billno: masterForm.billno,
			userid: authStore.userid
		})

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].ret_msg)
		} else {
			vAlert('?? œ?˜ì—ˆ?µë‹ˆ??')
			search()
			initialize()
		}
	} catch (e) { vAlertError('?? œ ì¤??¤ë¥˜ ë°œìƒ') }
}

const excel = () => {
	const today = new Date().toISOString().substring(0, 10)
	mainGrid?.download("xlsx", `ì§€ê¸‰ì–´?Œê¸°ì´ˆìë£?${today}.xlsx`)
}

const print = () => {
	const params = `billno=${searchForm.billno}&billno_TO=${searchForm.billno_TO}`
	window.open(`/api/haba/HABA_160P?${params}`, 'NotesPrint', 'width=800,height=800,scrollbars=yes')
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.actkind = 'I1'
	masterForm.useyn = 'Y'
	if (billTypeOptions.value.length > 0) masterForm.billtype = billTypeOptions.value[0].value
}

// ?ì—… ?¤ì •
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: 'BANK' | 'CUST') {
	const isBank = type === 'BANK'
	Object.assign(modalProps, {
		title: isBank ? 'ë°œí–‰?€??? íƒ' : 'ê±°ë˜ì²?? íƒ',
		path: '/api/ha00/HA00_03P_STR',
		data: { custgbn: isBank ? '020' : '010', cmpycd: authStore.cmpycd, search: isBank ? masterForm.banknm : masterForm.custnm },
		columns: [
			{ title: 'ì½”ë“œ', field: 'col0', width: 100, hozAlign: 'center' },
			{ title: isBank ? '?€?‰ëª…' : 'ê±°ë˜ì²˜ëª…', field: 'col1', width: 250 }
		],
		onConfirm: (d: any) => {
			if (isBank) {
				masterForm.bankcd = d.col0
				masterForm.banknm = d.col1
			} else {
				masterForm.custcd = d.col0
				masterForm.custnm = d.col1
			}
		}
	})
	modalVisible.value = true
}

const formatDate = (val: string) => {
	if (!val || val.length !== 8) return ''
	return `${val.substring(0, 4)}-${val.substring(4, 6)}-${val.substring(6, 8)}`
}

onMounted(async () => {
	if (typeof window !== 'undefined') (window as any).XLSX = XLSX
	await fetchOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "?´ìŒë²ˆí˜¸", field: "col0", width: 130, hozAlign: "center" },
				{ title: "ë°œí–‰?€??, field: "col11", width: 150 },
				{ title: "ë°œí–‰??, field: "col1", width: 120 },
				{ title: "ë°œí–‰??, field: "col3", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
				{ title: "ë§Œê¸°??, field: "col4", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
				{ title: "ê¸???, field: "col5", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "? í˜•", field: "col10", width: 100, hozAlign: "center" },
				{ title: "ì§€ê¸‰ê±°?˜ì²˜", field: "COL9", minWidth: 200 },
				{
					title: "?¬ìš©",
					field: "col8",
					width: 60,
					hozAlign: "center",
					formatter: (cell) => cell.getValue() === 'Y' ? '?”ï¸' : '??
				}
			],
			rowClick: (e, row) => {
				const d = row.getData()
				masterForm.actkind = 'U1'
				masterForm.billno = d.col0
				masterForm.issuman = d.col1
				masterForm.bankcd = d.col2
				masterForm.banknm = d.col11
				masterForm.stdymd = formatDate(d.col3)
				masterForm.endymd = formatDate(d.col4)
				masterForm.billamt = Number(d.col5)
				masterForm.billtype = d.col7
				masterForm.custnm = d.COL9
				masterForm.custcd = d.col6
				masterForm.useyn = d.col8
			}
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.bg-light-subtle { background-color: #f8f9fa !important; }
:deep(.tabulator-row) { cursor: pointer; }
:deep(.tabulator-row:hover) { background-color: #f0f7ff !important; }
</style>
