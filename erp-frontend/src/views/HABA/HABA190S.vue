<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ê±°ë˜ì²˜í˜„??(haba190s)
	?‘ì„±?¼ì	: 2025.03.14
	?¤ëª…        : ?±ë¡??ëª¨ë“  ê±°ë˜ì²˜ì˜ ?ì„¸ ?„í™© ì¡°íšŒ ë°?ê´€ë¦??”ë©´ ?°ë™
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- ?? 1. ?ë‹¨ ?¡ì…˜ ë°?(?œì? ?¤ë¹„ê²Œì´???ìš©) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
				<i class="bi bi-card-list me-2 text-primary"></i>
				ê¸°ë³¸?•ë³´ <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				ê¸°ì´ˆê´€ë¦?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">ê±°ë˜ì²˜í˜„??(haba190s)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">ì´ˆê¸°??/button>
				<button class="btn-erp btn-search" @click="search">ì¡°íšŒ</button>
				<button class="btn-erp btn-excel" @click="excel">?‘ì?</button>
				<button class="btn-erp btn-print" @click="print">?¸ì‡„</button>
			</div>
		</div>

		<!-- ?” 2. ê²€??ì¡°ê±´ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0 bg-light">
			<div class="card border shadow-sm overflow-hidden bg-white">
				<table class="erp-table-dense w-100">
					<colgroup>
						<col style="width: 80px;" /><col style="width: 150px;" />
						<col style="width: 80px;" /><col style="width: 200px;" />
						<col style="width: 80px;" /><col style="width: 200px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="bg-light text-center">?…ì¶œêµ¬ë¶„</th>
							<td>
								<select v-model="searchForm.iogbn" class="form-select form-select-sm" @change="search">
									<option value="000">?„ì²´</option>
									<option v-for="opt in ioGbnOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
								</select>
							</td>
							<th class="bg-light text-center border-start">ê±°ë˜ì²˜ëª…</th>
							<td class="px-2">
								<input v-model="searchForm.custnm" type="text" class="form-control form-control-sm" placeholder="ê±°ë˜ì²˜ëª… ê²€?? @keydown.enter="search" />
							</td>
							<th class="bg-light text-center border-start">?€?œì</th>
							<td class="px-2">
								<input v-model="searchForm.bossnm" type="text" class="form-control form-control-sm" placeholder="?€?œìëª?ê²€?? @keydown.enter="search" />
							</td>
							<td class="px-3 text-muted small border-0">
								<i class="bi bi-info-circle me-1"></i> ?í˜¸ë¥??´ë¦­?˜ë©´ ê±°ë˜ì²?ê´€ë¦??”ë©´?¼ë¡œ ?´ë™?©ë‹ˆ??
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- ?“Š 3. ê·¸ë¦¬???ì—­ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column bg-light">
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
import * as XLSX from 'xlsx'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// ?” ê²€???°ì´??
const searchForm = reactive({
	iogbn: '010',
	custnm: '',
    bossnm: ''
})

const ioGbnOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const normalizekeys = (obj: any) => {
  const n: any = {};
  if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

// ?? ?ì„¸ ?•ë³´ ?˜ì´ì§€(HABA180U)ë¡??´ë™ (?¤ë¹„ê²Œì´??ì²˜ë¦¬)
const goDetail = (row: any) => {
    const data = normalizekeys(row);
	router.push({
		path: '/HABA/HABA180U',
		query: {
            custcd: data.custcd,   // ?ì„¸ ì¡°íšŒë¥??„í•´ ì½”ë“œ ?„ë‹¬
			custnm: data.custnm,
            custgbn: data.custgbn,
            status: data.status
		}
	})
}

// ?? ì´ˆê¸° ?•ë³´ ë¡œë“œ
const loadInitData = async () => {
	try {
		const resIo = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '340' })
		ioGbnOptions.value = (resIo.data || []).map((r: any) => normalizekeys(r)).map((n: any) => ({ CD: n.codecd, NM: n.codenm }))
	} catch (e) {
		console.error('ê¸°ì´ˆ ?°ì´??ë¡œë“œ ?¤íŒ¨')
	}
}

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_190S_STR', {
			iogbn: searchForm.iogbn,
			custnm: searchForm.custnm,
		    bossnm: searchForm.bossnm
		})

		const processedData = (res.data || []).map((r: any) => {
            const normalized = normalizekeys(r);
			let formattedNo = normalized.custno || ''
			if (formattedNo.length === 10) {
				formattedNo = `${formattedNo.substring(0, 3)}-${formattedNo.substring(3, 5)}-${formattedNo.substring(5)}`
			} else if (formattedNo.length === 13) {
				formattedNo = `${formattedNo.substring(0, 6)}-${formattedNo.substring(6)}`
			}

			return {
				...normalized,
				custno_f: formattedNo,
				bank_acc: `${normalized.banknm || ''} ${normalized.gujoa || ''}`.trim()
			}
		})

		mainGrid?.setData(processedData)
		vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) {
		vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ')
	}
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.iogbn = '010'
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", `ê±°ë˜ì²˜í˜„??${new Date().toISOString().substring(0, 10)}.xlsx`)
const print = () => {
    const params = new URLSearchParams(searchForm).toString();
    window.open(`/api/haba/HABA_190P?${params}`)
}

onMounted(() => {
	if (typeof window !== 'undefined') (window as any).XLSX = XLSX;
	loadInitData()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			pagination: "local",
			paginationSize: 50,
			paginationButtonCount: 10,
			paginationSizeSelector: [50, 100, 200, 500],
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "ê±°ë˜ì²?, field: "custcd", width: 85, hozAlign: "center",
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer fw-bold">${cell.getValue()}</span>`,
					cellClick: (e, cell) => goDetail(cell.getData())
				},
				{
					title: "ê±°ë˜ì²˜ìƒ??, field: "custnm", minWidth: 200,
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer fw-bold">${cell.getValue()}</span>`,
					cellClick: (e, cell) => goDetail(cell.getData())
				},
				{ title: "êµ¬ë¶„", field: "iogbnnm", width: 80, hozAlign: "center" },
				{ title: "ì¢…ë¥˜", field: "custgbnm", width: 80, hozAlign: "center" },
				{ title: "?¬ì—…?ë²ˆ??, field: "custno_f", width: 115, hozAlign: "center" },
				{ title: "?€?œì", field: "bossnm", width: 90, hozAlign: "center" },
				{ title: "?…íƒœ", field: "custtype", width: 120 },
				{ title: "ì¢…ëª©", field: "custkind", width: 120 },
				{ title: "?°ë½ì²?, field: "telno", width: 110, hozAlign: "center" },
				{ title: "?íƒœ", field: "statusnm", width: 80, hozAlign: "center" },
                { title: "?¬ìš©", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">?¬ìš©</b>' : '';
                  }
                }
			]
		})
	}
    search()
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.tabulator-row:hover) { background-color: #f0f7ff !important; cursor: pointer; }
.cursor-pointer { cursor: pointer; }
:deep(.tabulator-footer) {
	background-color: #f8f9fa !important;
	border-top: 1px solid #dee2e6 !important;
	font-size: 12px !important;
	padding: 5px !important;
}
:deep(.tabulator-page) {
	padding: 2px 8px !important;
	margin: 0 2px !important;
	border: 1px solid #ddd !important;
	border-radius: 3px !important;
	background: #fff !important;
}
:deep(.tabulator-page.active) {
	background: #0d6efd !important;
	color: #fff !important;
	border-color: #0d6efd !important;
}
</style>
