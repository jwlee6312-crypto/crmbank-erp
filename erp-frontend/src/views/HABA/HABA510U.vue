<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ì§€ë¶ˆê³„?•ê?ë¦?
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ì§€ë¶?? í˜•ë³??°ê²° ê³„ì •ê³¼ëª© ê´€ë¦?
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-credit-card-2-back me-2 text-primary" style="font-size: 18px;"></i>
				ê¸°ë³¸?•ë³´ <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">ì§€ë¶ˆê³„?•ê?ë¦?(HABA510U)</span>
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
				<button v-if="masterForm.actkind === 'U0'" class="btn-erp btn-danger" @click="deleteData">
					<i class="bi bi-trash"></i> ?? œ
				</button>
			</div>
		</div>

		<!-- ?“ ?…ë ¥ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> ì§€ë¶?ê³„ì • ?ì„¸ ?•ë³´ [{{ masterForm.actkind === 'A0' ? '? ê·œ' : '?˜ì •' }}]</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 15%;" />
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">ì§€ë¶ˆìœ ??/th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.paygbn" type="text" class="form-control form-control-sm text-center" maxlength="3" :readonly="masterForm.actkind === 'U0'" placeholder="3?ë¦¬" />
							</td>
							<th class="text-center bg-light-subtle border-end">ì§€ë¶ˆìœ ?•ëª…</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.paygbnm" type="text" class="form-control form-control-sm" maxlength="20" />
							</td>
							<th class="text-center bg-light-subtle border-end">ê³„ì •ê³¼ëª©</th>
							<td class="bg-white px-2 py-1">
								<div class="input-group input-group-sm" style="max-width: 300px;">
									<input v-model="masterForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
									<input v-model="masterForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp"><i class="bi bi-search"></i></button>
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
import { ref, reactive, onMounted, nextTick } from 'vue'
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

// ?“ ë§ˆìŠ¤???°ì´??
const masterForm = reactive({
	actkind: 'A0',
	paygbn: '',
	paygbnm: '',
	acctcd: '',
	acctnm: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

// ?? ?„ë“œëª??•ê·œ??(?€?Œë¬¸??ì²˜ë¦¬)
const normalizekeys = (obj: any) => {
  const n: any = {};
  if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_510U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd
		})

		const processedData = (res.data || []).map((r: any) => normalizekeys(r))
		mainGrid?.setData(processedData)
		if (processedData.length > 0) vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) { vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

const save = async () => {
	if (!masterForm.paygbn) return vAlert('ì§€ë¶ˆìœ ?•ì„ ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (masterForm.paygbn.length < 3) return vAlert('ì§€ë¶ˆìœ ?•ì? 3?ë¦¬ë¡??…ë ¥?˜ì…”???©ë‹ˆ??')
	if (!masterForm.paygbnm) return vAlert('ì§€ë¶ˆìœ ??ëª…ì„ ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (!masterForm.acctcd) return vAlert('ê³„ì •ê³¼ëª©??? íƒ??ì£¼ì‹­?œìš”.')

	try {
		const payload = {
			actkind: masterForm.actkind,
			cmpycd: authStore.cmpycd,
			paygbn: masterForm.paygbn,
			paygbnm: masterForm.paygbnm,
			acctcd: masterForm.acctcd,
			userid: authStore.userid
		}

		const res = await api.post('/api/haba/HABA_510U_STR', payload)
		const resData = normalizekeys(res.data?.[0]);

		if (resData.ret_yn === 'Y' || resData.res === 'FAIL') {
			vAlertError(resData.ret_msg || '?€??ì¤??¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤.')
		} else {
			vAlert('?•ìƒ?¼ë¡œ ?‘ì—…???˜ì—ˆ?µë‹ˆ??')
			search()
			initialize()
		}
	} catch (e) { vAlertError('?€???¤íŒ¨') }
}

const deleteData = async () => {
	if (!masterForm.paygbn) return vAlert('?? œ??ì§€ë¶ˆìœ ?•ì„ ? íƒ??ì£¼ì‹­?œìš”.')
	if (!confirm('?•ë§ë¡??? œ?˜ì‹œê² ìŠµ?ˆê¹Œ?')) return

	try {
		const res = await api.post('/api/haba/HABA_510U_STR', {
			actkind: 'D0',
			cmpycd: authStore.cmpycd,
			paygbn: masterForm.paygbn,
			userid: authStore.userid
		})
		const resData = normalizekeys(res.data?.[0]);

		if (resData.ret_yn === 'Y') {
			vAlertError(resData.ret_msg)
		} else {
			vAlert('?•ìƒ?ìœ¼ë¡??? œ?˜ì—ˆ?µë‹ˆ??')
			search()
			initialize()
		}
	} catch (e) { vAlertError('?? œ ì¤??¤ë¥˜ ë°œìƒ') }
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.actkind = 'A0'
}

// ?ì—… ?¤ì •
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp() {
	Object.assign(modalProps, {
		title: 'ê³„ì •ê³¼ëª© ? íƒ',
		path: '/api/ha00/HA00_00P_STR',
		data: { gubun: 'A0', cmpycd: authStore.cmpycd, code: masterForm.acctnm },
		columns: [
			{ title: 'ì½”ë“œ', field: 'acctcd', width: 100, hozAlign: 'center' },
			{ title: 'ê³„ì •ëª?, field: 'acctnm', width: 250 }
		],
		onConfirm: (d: any) => {
			const data = normalizekeys(d);
			masterForm.acctcd = data.acctcd
			masterForm.acctnm = data.acctnm
		}
	})
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "? í˜•", field: "paygbn", width: 100, hozAlign: "center", cssClass: "fw-bold text-primary" },
				{ title: "? í˜•ëª?, field: "paygbnm", width: 250 },
				{ title: "ê³„ì •ê³¼ëª©", field: "acctcd", width: 120, hozAlign: "center" },
				{ title: "ê³„ì •ê³¼ëª© ëª?, field: "acctnm", minWidth: 250 }
			]
		})

		// ?? ê·¸ë¦¬?????´ë¦­ ?´ë²¤?? ?ë‹¨ ?¼ì— ?°ì´??ë°”ì¸??
		mainGrid.on("rowClick", (e, row) => {
			const d = row.getData()
			Object.assign(masterForm, d)
			masterForm.actkind = 'U0'
		})

		nextTick(() => search())
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.bg-light-subtle { background-color: #f8f9fa !important; }
:deep(.tabulator-row) { cursor: pointer; }
:deep(.tabulator-row:hover) { background-color: #f0f7ff !important; }
</style>
