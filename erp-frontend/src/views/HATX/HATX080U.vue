<!--	=============================================================
	?„ë¡œê·¸ë¨ëª?: ?„ì?¸ê¸ˆê³„ì‚°?œë°œ??
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ë§¤ì¶œ ?¸ê¸ˆê³„ì‚°?œì˜ êµ?„¸ì²??„ì†¡ ?íƒœ ì¡°íšŒ ë°??„ìë°œí–‰/?„ì†¡ ì²˜ë¦¬
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-send-check me-2 text-primary" style="font-size: 18px;"></i>
				?¸ë¬´ê´€ë¦?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">?„ì?¸ê¸ˆê³„ì‚°?œë°œ??(HATX080U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-save" @click="handleBatchAction('U0')">
					<i class="bi bi-send"></i> {{ searchForm.cfmyn === 'N' ? '?„ì†¡ì²˜ë¦¬' : '?¬ì „?¡ì²˜ë¦? }}
				</button>
				<button v-if="searchForm.cfmyn === 'Y'" class="btn-erp btn-danger" @click="handleBatchAction('D0')">
					<i class="bi bi-trash"></i> ?ê¸°ì²˜ë¦¬
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> ì¡°íšŒ
				</button>
			</div>
		</div>

		<!-- ?” ê²€??ì¡°ê±´ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>?¬ì—…??/span>
							<select v-model="searchForm.taxunit" class="form-select form-select-sm" style="width: 150px;" @change="search">
								<option v-for="opt in taxUnitOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
							</select>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>ë°œí–‰ë¶€??/span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
								<input v-model="searchForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" placeholder="ë¶€??? íƒ" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>ë°œí–‰??/span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.fromdt" type="date" class="form-control form-control-sm" style="width: 135px;" />
								<span>~</span>
								<input v-model="searchForm.todt" type="date" class="form-control form-control-sm" style="width: 135px;" />
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>ì¡°íšŒ?€??/span>
							<select v-model="searchForm.cfmyn" class="form-select form-select-sm" style="width: 100px;" @change="search">
								<option value="N">ë¯¸ì „??/option>
								<option value="Y">?„ì†¡</option>
							</select>
						</div>
					</div>
				</div>
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
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const today = new Date().toISOString().slice(0, 10)
const firstDay = today.slice(0, 8) + '01'

const taxUnitOptions = ref<any[]>([])

const searchForm = reactive({
	taxunit: '',
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || '',
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
	if (!searchForm.taxunit) return vAlertError('?¬ì—…?¥ì„ ? íƒ?˜ì„¸??')
	if (!searchForm.deptcd) return vAlertError('ë°œí–‰ë¶€?œë? ? íƒ?˜ì„¸??')

	// 2011-07-01 ?´í›„ ë¡œì§ ë°˜ì˜
	const frYmd = searchForm.fromdt.replace(/-/g, '')
	const toYmd = searchForm.todt.replace(/-/g, '')
	if (frYmd < '20110701' || toYmd < '20110701') {
		return vAlertError('2011??07??01???´í›„ ë°œí–‰ê³„ì‚°?œë§Œ ì¡°íšŒ ê°€?¥í•©?ˆë‹¤.')
	}

	try {
		const res = await api.post('/api/hatx/HATX_080U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			taxunit: searchForm.taxunit,
			deptcd: searchForm.deptcd,
			fromdt: frYmd,
			todt: toYmd,
			cfmyn: searchForm.cfmyn,
			taxkind: '200' // ë§¤ì¶œ
		})

		const data = (res.data || []).map((row: any) => ({
			pubymd: row.pubymd,
			custnm: row.custnm,
			SENDstatus: row.SENDstatus,
			DOCUMENTstatus: row.DOCUMENTstatus,
			NTS_status: row.FILLER2 === '1' ? '?„ì†¡' : 'ë¯¸ì „??,
			supyamt: Number(row.supyamt || 0),
			vatamt: Number(row.vatamt || 0),
			totamt: Number(row.totamt || 0),
			descnm: row.descnm,
			damdang: row.damdang,
			telno: row.telno,
			email: row.email,
			taxym: row.taxym,
			taxno: row.taxno,
			_selected: searchForm.cfmyn === 'N'
		}))

		mainGrid?.setData(data)
		if (searchForm.cfmyn === 'N') {
			mainGrid?.selectRow()
		}
		vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) { vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

const handleBatchAction = async (gbn: string) => {
	const selectedRows = mainGrid?.getSelectedData() || []
	if (selectedRows.length === 0) return vAlertError('ì²˜ë¦¬???€?ì„ ? íƒ??ì£¼ì‹­?œì˜¤.')

	let msg = ''
	if (gbn === 'U0') {
		msg = searchForm.cfmyn === 'N' ? '? íƒ???¸ê¸ˆê³„ì‚°?œë? ?„ì†¡ ?˜ì‹œê² ìŠµ?ˆê¹Œ?' : '? íƒ???¸ê¸ˆê³„ì‚°?œë? ?¬ì „???˜ì‹œê² ìŠµ?ˆê¹Œ?'
	} else {
		msg = '? íƒ???¸ê¸ˆê³„ì‚°?œë? ?ê¸° ?˜ì‹œê² ìŠµ?ˆê¹Œ?'
	}

	if (!confirm(msg)) return

	try {
		for (const row of selectedRows) {
			const res = await api.post('/api/hatx/HATX_080U_STR', {
				actkind: gbn,
				cmpycd: authStore.cmpycd,
				taxunit: searchForm.taxunit,
				deptcd: searchForm.deptcd,
				fromdt: searchForm.fromdt.replace(/-/g, ''),
				todt: searchForm.todt.replace(/-/g, ''),
				cfmyn: searchForm.cfmyn,
				taxkind: '200',
				taxym: row.taxym,
				taxno: row.taxno,
				elcyn: 'N', // ASP: 'N'?¼ë¡œ ?˜ë“œì½”ë”©??
				userid: authStore.userid
			})

			if (res.data && res.data[0] && res.data[0].col0 === 'N') {
				vAlertError(res.data[0].col2 || 'ì²˜ë¦¬ ì¤??¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤.')
				break
			}
		}

		vAlert('?•ìƒ?ìœ¼ë¡?ì²˜ë¦¬?˜ì—ˆ?µë‹ˆ??')
		search()
	} catch (e) { vAlertError('ì²˜ë¦¬ ì¤??¤ë¥˜ ë°œìƒ') }
}

// --- ?ì—… ?¤ì • ---
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: 'ë¶€??? íƒ', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'D0', cmpycd: authStore.cmpycd, code: searchForm.deptnm },
			columns: [{ title: 'ì½”ë“œ', field: 'deptcd', width: 80 }, { title: 'ë¶€?œëª…', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search() }
		})
	}
	modalVisible.value = true
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
					title: "ë°œí–‰??, field: "pubymd", width: 100, hozAlign: "center",
					formatter: (cell) => { const v = cell.getValue(); return v ? `${v.slice(0,4)}-${v.slice(4,6)}-${v.slice(6,8)}` : '' }
				},
				{ title: "?í˜¸", field: "custnm", width: 150 },
				{ title: "?„ì†¡?íƒœ", field: "SENDstatus", width: 80, hozAlign: "center" },
				{ title: "ë¬¸ì„œ?íƒœ", field: "DOCUMENTstatus", width: 80, hozAlign: "center" },
				{ title: "êµ?„¸ì²?, field: "NTS_status", width: 70, hozAlign: "center" },
				{ title: "ê³µê¸‰ê°€", field: "supyamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "ë¶€ê°€??, field: "vatamt", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "?©ê³„", field: "totamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" },
				{ title: "?ìš”", field: "descnm", width: 150 },
				{ title: "?´ë‹¹??, field: "damdang", width: 80 },
				{ title: "?°ë½ì²?, field: "telno", width: 100 },
				{ title: "?´ë©”??, field: "email", width: 130 }
			]
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; font-size: 11px; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; font-size: 11px; }
:deep(.tabulator-selected) { background-color: #e7f1ff !important; }
</style>
