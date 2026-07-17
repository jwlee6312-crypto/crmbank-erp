<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ê´€ë¦¬ë²ˆ??ê´€ë¦?
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ê³„ì •ê³¼ëª©??ê´€ë¦¬ë²ˆ??? í˜•??ê¸°í???ì½”ë“œë¥??±ë¡?˜ì—¬ ê´€ë¦?
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-gear-wide-connected me-2 text-primary" style="font-size: 18px;"></i>
				ê¸°ë³¸?•ë³´ <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">ê´€ë¦¬ë²ˆ??ê´€ë¦?(HABA090U)</span>
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
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 250px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">ê´€ë¦¬ë²ˆ??ëª?/th>
							<td class="bg-white border-end px-2">
								<input v-model="searchForm.mgtnm_h" type="text" class="form-control form-control-sm" placeholder="ê²€?‰ì–´ ?…ë ¥" @keydown.enter="search" />
							</td>
							<td class="bg-white px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> ê²€?‰í•˜??ê´€ë¦¬ë²ˆ??ëª…ì„ ?…ë ¥??ì£¼ì‹­?œìš”.
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- ?“ ?ì„¸ ?•ë³´ ?…ë ¥ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> ê´€ë¦¬ë²ˆ???ì„¸ ?•ë³´ [{{ masterForm.actkind === 'A0' ? '? ê·œ' : '?˜ì •' }}]</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col />
						<col style="width: 100px;" /><col style="width: 15%;" />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">ê´€ë¦¬ë²ˆ??/th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.mgtno" type="text" class="form-control form-control-sm" maxlength="5" :readonly="masterForm.actkind === 'U0'" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">ê´€ë¦¬ë²ˆ??ëª?/th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterForm.mgtnm" type="text" class="form-control form-control-sm" maxlength="30" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">ì¶œí˜„?œì„œ</th>
							<td class="bg-white border-top px-2 py-1">
								<input v-model="masterForm.dspord" type="text" class="form-control form-control-sm text-end" maxlength="3" />
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">ë¹?nbsp;&nbsp;&nbsp;&nbsp;ê³?/th>
							<td colspan="3" class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterForm.remark" type="text" class="form-control form-control-sm" maxlength="50" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">?¬ìš©?¬ë?</th>
							<td class="bg-white border-top px-3">
								<div class="form-check pt-1">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useCheck" true-value="Y" false-value="N">
									<label class="form-check-label small fw-bold" for="useCheck">?¬ìš©</label>
								</div>
							</td>
						</tr>
						<tr>
							<td colspan="6" class="bg-light-subtle px-3 py-1 text-primary small">
								<i class="bi bi-info-circle-fill me-1"></i> ê³„ì •ê³¼ëª©??ê´€ë¦¬ë²ˆ??? í˜•??ê¸°í???ì½”ë“œë¥??±ë¡?˜ì—¬ ê´€ë¦¬í•œ??
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

// ?” ê²€???°ì´??
const searchForm = reactive({
	mgtnm_h: ''
})

// ?“ ë§ˆìŠ¤???°ì´??
const masterForm = reactive({
	actkind: 'A0',
	mgtno: '',
	mgtnm: '',
	remark: '',
	dspord: '',
	useyn: 'Y'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_090U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			mgtnm: searchForm.mgtnm_h
		})

		const processedData = (res.data || []).map((r: any) => ({
			mgtno: r.mgtno,
			mgtnm: r.mgtnm,
			remark: r.bigo,
			dspord: r.dspord,
			useyn: r.useyn
		}))

		mainGrid?.setData(processedData)
		vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) { vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

const save = async () => {
	if (!masterForm.mgtno) return vAlert('ê´€ë¦¬ë²ˆ??ì½”ë“œë¥?ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (!masterForm.mgtnm) return vAlert('ê´€ë¦¬ë²ˆ??ëª…ì„ ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (!masterForm.dspord) return vAlert('ì¶œí˜„?œì„œë¥?ê¸°ì¬??ì£¼ì‹­?œìš”.')

	try {
		const payload = {
			...masterForm,
			cmpycd: authStore.cmpycd,
			userid: authStore.userid
		}

		const res = await api.post('/api/haba/HABA_090U_STR', payload)

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].ret_msg)
		} else {
			vAlert('?•ìƒ?¼ë¡œ ?‘ì—…???˜ì—ˆ?µë‹ˆ??')
			search()
			initialize()
		}
	} catch (e) { vAlertError('?€???¤íŒ¨') }
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.actkind = 'A0'
	masterForm.useyn = 'Y'
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "ê´€ë¦¬ë²ˆ??, field: "mgtno", width: 100, hozAlign: "center" },
				{ title: "ê´€ë¦¬ë²ˆ??ëª?, field: "mgtnm", width: 250 },
				{ title: "ë¹„ê³ ", field: "remark", minWidth: 200 },
				{ title: "ì¶œí˜„?œì„œ", field: "dspord", width: 100, hozAlign: "center" },
                { title: "?¬ìš©", field: "useyn", width: 80, hozAlign: "center",  editor: true,
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">?¬ìš©</b>' : '';
                  }
                }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				Object.assign(masterForm, d)
				masterForm.actkind = 'U0'
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
