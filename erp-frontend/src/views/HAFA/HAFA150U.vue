<!--	=============================================================
	?„ë¡œê·¸ë¨ëª?: ê°ê??ê°?„í‘œ ë°œí–‰
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ê°ê??ê° ê³„ì‚° ?´ì—­??ë°”íƒ•?¼ë¡œ ?Œê³„ ?„í‘œë¥??¼ê´„ ?ëŠ” ? íƒ ë°œí–‰ (?œì? ?”ì???ìš©)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- ?? 1. ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-receipt-cutoff me-2 text-primary" style="font-size: 18px;"></i>
				ê³ ì •?ì‚° <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				?”ë§ì²˜ë¦¬ <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">ê°ê??ê°?„í‘œ (HAFA150U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">ì´ˆê¸°??/button>
				<button class="btn-erp btn-search" @click="search">ì¡°íšŒ</button>
				<button class="btn-erp btn-save" @click="save">?„í‘œë°œí–‰</button>
			</div>
		</div>

		<!-- ?’¡ 2. ë©”ì¸ ì»¨í…ì¸??ì—­ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- [?ë‹¨] ì¡°íšŒ ?„í„° ?ì—­ -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense" width="100%">
						<colgroup>
							<col style="width: 10%" /><col style="width: 90%" />
						</colgroup>
						<tbody>
							<tr>
								<th class="text-center bg-light">?€?ì—°??/th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<select v-model="searchForm.yy" class="form-select form-select-sm" style="width: 100px;">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}??/option>
										</select>
										<select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 80px;">
											<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}??/option>
										</select>
										<span class="small fw-bold ms-2 text-secondary">ë¶?ê°ê??ê° ?´ì—­ ì¡°íšŒ</span>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- ?“ 3. ?„í‘œ ë°œí–‰ ?•ë³´ ?¤ì • -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-primary"></i>?„í‘œ ë°œí–‰ ?•ë³´ ?¤ì •</div>
					<div class="text-danger small fw-bold" style="font-size: 11px;">???„í‘œ ë°œí–‰ ??? íƒ????ª©???€???„í‘œê°€ ?ë™ ?ì„±?©ë‹ˆ??</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 110px;" /><col style="width: 300px;" />
							<col style="width: 110px;" /><col style="width: 200px;" />
							<col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light">ë°œí–‰ë¶€??/th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="issuingForm.deptcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 65px;" readonly />
										<input v-model="issuingForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" />
										<button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required bg-light">ë°œí–‰?¼ì</th>
								<td><input v-model="issuingForm.acctymd" type="date" class="form-control form-control-sm" /></td>
								<td class="px-3">
									<div class="d-flex justify-content-end gap-2">
										<div class="d-flex align-items-center bg-light border rounded px-2 py-1">
											<span class="text-muted small me-2">ì´??ê°??</span>
											<span class="fw-bold text-dark small">{{ formatMoney(summary.totalAmt) }}</span>
										</div>
										<div class="d-flex align-items-center bg-primary-subtle border border-primary-subtle rounded px-2 py-1">
											<span class="text-primary-emphasis small me-2 fw-bold">ë°œí–‰?€??</span>
											<span class="fw-bold text-primary">{{ formatMoney(summary.issuingAmt) }}</span>
										</div>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- ?“Š 4. ê·¸ë¦¬???ì—­ -->
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
import { useTabStore } from '@/stores/tabStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const tabStore = useTabStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

//const currentYear = new Date().getFullYear()
const currentYear = 2011
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')
const yearOptions = Array.from({ length: 5 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({ yy: String(currentYear), mm: currentMonth })
const issuingForm = reactive({ deptcd: authStore.deptcd || '', deptnm: authStore.deptnm || '', acctymd: new Date().toISOString().slice(0, 10) })
const summary = reactive({ totalAmt: 0, issuingAmt: 0 })

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const formatMoney = (val: any) => Number(val || 0).toLocaleString()

async function search() {
	try {
		const res = await api.post('/api/hafa/HAFA_150U_STR',
		{   cmpycd: authStore.cmpycd,
		    baseym: searchForm.yy + searchForm.mm
		})
		const data = (res.data || []).map((row: any) => ({
			deptcd: row.deptcd,
			deptnm: row.deptnm,
			acctcd: row.acctcd,
			acctnm: row.acctnm,
			costtype: row.costtype,
			costtypenm: row.costtypenm,
			dprsamt: Number(row.dprsamt || 0),
			cacctcd: row.cacctcd,
			cacctnm: row.cacctnm,
			macctcd: row.macctcd,
			macctnm: row.macctnm,
			sacctcd: row.sacctcd,
			sacctnm: row.sacctnm,
			slipymd: row.slipymd,
			slipno: row.slipno,
			srowno: row.srowno
		}))
		summary.totalAmt = data.reduce((acc: number, cur: any) => acc + cur.dprsamt, 0)
		summary.issuingAmt = 0; mainGrid?.setData(data); vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) { vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

async function save() {
	const selectedRows = mainGrid?.getSelectedData() || []
	if (selectedRows.length === 0) return vAlertError('?„í‘œ ë°œí–‰???€?ì„ ? íƒ?˜ì„¸??')
	if (!issuingForm.deptcd) return vAlertError('ë°œí–‰ë¶€?œë? ? íƒ?˜ì„¸??')
	if (!confirm('? íƒ????ª©?¤ì— ?€??ê°ê??ê° ?„í‘œë¥??ë™ ë°œí–‰?˜ì‹œê² ìŠµ?ˆê¹Œ?')) return
	try {
		await api.post('/api/hafa/HAFA_150U_SAVE',
		{ cmpycd: authStore.cmpycd,
		  yy: searchForm.yy,
		  mm: searchForm.mm,
		  deptcd: issuingForm.deptcd,
		  acctymd: issuingForm.acctymd.replace(/-/g, ''),
		  items: selectedRows,
		  userid: authStore.userid })
		vAlert('?„í‘œê°€ ?•ìƒ?ìœ¼ë¡?ë°œí–‰?˜ì—ˆ?µë‹ˆ??');
		search()
	} catch (e) { vAlertError('?„í‘œ ë°œí–‰ ì¤??¤ë¥˜ ë°œìƒ') }
}

function initialize() {
	searchForm.yy = String(currentYear); searchForm.mm = currentMonth;
	issuingForm.deptcd = authStore.deptcd || ''; issuingForm.deptnm = authStore.deptnm || '';
	issuingForm.acctymd = new Date().toISOString().slice(0, 10);
	mainGrid?.clearData(); summary.totalAmt = 0; summary.issuingAmt = 0;
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, { title: 'ë¶€??? íƒ', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'D0', cmpycd: authStore.cmpycd, code: issuingForm.deptnm }, columns: [{ title: 'ì½”ë“œ', field: 'deptcd', width: 80 }, { title: 'ë¶€?œëª…', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { issuingForm.deptcd = d.deptcd; issuingForm.deptnm = d.deptnm }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false, cellClick: (e, cell) => cell.getRow().toggleSelect() },
				{ title: "ë¶€?œì½”??, field: "deptcd", width: 80, hozAlign: "center" },
				{ title: "ë¶€?œëª…", field: "deptnm", widthGrow: 1 },
				{ title: "ê³„ì •ì½”ë“œ", field: "acctcd", width: 80, hozAlign: "center" },
				{ title: "ê³„ì •ê³¼ëª©ëª?, field: "acctnm", widthGrow: 1, cssClass: "fw-bold" },
				{ title: "ë¹„ìš©êµ¬ë¶„", field: "costtypenm", widthGrow: 1, hozAlign: "center" },
				{ title: "ê°ê??ê°??, field: "dprsamt", widthGrow: 1, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary fw-bold" },
				{ title: "?„í‘œë²ˆí˜¸", field: "slipno", widthGrow: 1, hozAlign: "center",
					formatter: (cell: any) => {
						const d = cell.getData(); if (!d.slipymd || d.slipymd === '00000000') return '';
						return `<span class="text-primary text-decoration-underline cursor-pointer">${d.slipymd}-${d.slipno}</span>`;
					},
					cellClick: (e, cell) => {
						const d = cell.getData(); if (!d.slipno) return;
						tabStore.addTab({ name: '?¼ë°˜?„í‘œê´€ë¦?, path: '/HASL/HASL110U', params: { slipymd: d.slipymd, slipno: d.slipno } });
					}
				}
			]
		})
		mainGrid.on("rowSelectionChanged", (data) => summary.issuingAmt = data.reduce((acc: number, cur: any) => acc + cur.dprsamt, 0))
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
