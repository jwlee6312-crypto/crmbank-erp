<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ì§€ê¸‰ì–´?Œëª…?¸ì„œ
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ë§Œê¸°?¼ìë³?ì§€ê¸‰ì–´?Œì˜ ?ì„¸ ?´ì—­ ë°??”ì•¡ ?„í™© ì¡°íšŒ
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-check me-2 text-primary" style="font-size: 18px;"></i>
				?ê¸ˆê´€ë¦?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">ì§€ê¸‰ì–´?Œëª…?¸ì„œ (HAFN510S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> ì´ˆê¸°??
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> ì¡°íšŒ
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> ?¸ì‡„
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> ?‘ì?
				</button>
			</div>
		</div>

		<!-- ?” ê²€??ì¡°ê±´ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">ë§Œê¸°??ë²”ìœ„</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-1">
									<input v-model="searchForm.ymd_fr" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
									<span class="text-muted">~</span>
									<input v-model="searchForm.ymd_to" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
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

const now = new Date()
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const today = now.toISOString().substring(0, 10)

// ?” ê²€??ì¡°ê±´
const searchForm = reactive({
	ymd_fr: firstDay,
	ymd_to: today
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const formatYmdShort = (v: string) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_510S_STR', {
			cmpycd: authStore.cmpycd,
			ymd_fr: searchForm.ymd_fr.replace(/-/g, ''),
			ymd_to: searchForm.ymd_to.replace(/-/g, '')
		})

		const data = (res.data || []).map((row: any) => ({
			dueymd: formatYmdShort(row.endymd),
			billno: row.billno,
			issubank: row.banknm,
			issuman: row.issuman,
			stdymd: formatYmdShort(row.stdymd),
			custnm: row.custnm,
			billtype: row.billtype_nm,
			billamt: Number(row.billamt || 0)
		}))

		mainGrid?.setData(data)
		vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) { vAlertError('ì¡°íšŒ ?¤íŒ¨') }
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.ymd_fr = firstDay
	searchForm.ymd_to = today
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", `ì§€ê¸‰ì–´?Œëª…?¸ì„œ_${today}.xlsx`)

const print = () => {
	const params = `ymd_fr=${searchForm.ymd_fr}&ymd_to=${searchForm.ymd_to}`
	window.open(`/api/hafn/HAFN_510P?${params}`, 'NotesPayablePrint', 'width=1000,height=800,scrollbars=yes')
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: "dueymd", // ë§Œê¸°?¼ë³„ ê·¸ë£¹??
			groupHeader: function(value, count, data, group) {
				const sumamt = data.reduce((acc, curr) => acc + curr.billamt, 0)
				return `
					<div class="d-flex justify-content-between w-100 pe-4">
						<span class="fw-bold text-dark">ë§Œê¸°?? ${value}</span>
						<div class="small fw-bold">
							<span class="mx-2 text-muted">?Œê³„: ${sumamt.toLocaleString()}??(${count}ê±?</span>
						</div>
					</div>
				`
			},
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "?´ìŒë²ˆí˜¸", field: "billno", hozAlign: "center", width: 200 },
				{ title: "ë°œí–‰?€??, field: "issubank", width: 200 },
				{ title: "ë°œí–‰??, field: "issuman", hozAlign: "center", width: 150 },
				{ title: "ë°œí–‰??, field: "stdymd", hozAlign: "center", width: 150 },
				{ title: "ì§€ê¸‰ê±°?˜ì²˜", field: "custnm", minWidth: 250 },
				{ title: "?•íƒœ", field: "billtype", hozAlign: "center", width: 150 },
				{
					title: "ê¸ˆì•¡", field: "billamt", hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					width: 150, cssClass: "fw-bold",
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				}
			]
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-group) { background-color: #f0f4f8 !important; border-top: 1px solid #d1d9e6 !important; }
:deep(.tabulator-group-handle) { display: none; }
</style>
