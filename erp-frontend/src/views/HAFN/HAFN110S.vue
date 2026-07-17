<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: ?àÍ∏àÎ™ÖÏÑ∏??
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : Í∏∞Ï??ºÏûê ?ÑÏû¨???àÏ†ÅÍ∏?Î™ÖÏÑ∏ Î∞??îÏï° ?ÑÌô© Ï°∞Ìöå
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ÅÎã® ?°ÏÖò Î∞?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-bank me-2 text-primary" style="font-size: 18px;"></i>
				?êÍ∏àÍ¥ÄÎ¶?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">?àÍ∏àÎ™ÖÏÑ∏??(HAFN110S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> Ï¥àÍ∏∞??
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> Ï°∞Ìöå
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> ?∏ÏáÑ
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> ?ëÏ?
				</button>
			</div>
		</div>

		<!-- ?îç Í≤Ä??Ï°∞Í±¥ ?ÅÏó≠ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">?åÍ≥Ñ?ºÏûê</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-2">
									<input v-model="searchForm.ymd" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
									<span class="small fw-bold text-secondary">?ÑÏû¨</span>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- ?ìä Í∑∏Î¶¨???ÅÏó≠ -->
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
import * as XLSX from 'xlsx'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const today = new Date().toISOString().substring(0, 10)

// ?îç Í≤Ä??Ï°∞Í±¥
const searchForm = reactive({
	ymd: today
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const formatYmd = (v: string) => v && v.length === 8 ? `${v.substring(0, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : v

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_110S_STR', {
			cmpycd: authStore.cmpycd,
			ymd: searchForm.ymd.replace(/-/g, '')
		})

		const data = (res.data || []).map((row: any) => ({
			acctcd: row.acctcd,
			acctnm: row.acctnm,
			custcd: row.bankcd,
			custnm: row.banknm,
			mgtno: row.mgtno,
			stdymd: formatYmd(row.stdymd),
			endymd: formatYmd(row.endymd),
			rate: Number(row.rate || 0),
			amt: Number(row.amt || 0)
		}))

		mainGrid?.setData(data)
		vAlert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { vAlertError('Ï°∞Ìöå ?§Ìå®') }
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.ymd = today
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", `?àÍ∏àÎ™ÖÏÑ∏??${searchForm.ymd}.xlsx`)

const print = () => {
	const params = `ymd=${searchForm.ymd}`
	window.open(`/api/hafn/HAFN_110P?${params}`, 'DepositStatementPrint', 'width=1000,height=800,scrollbars=yes')
}

onMounted(() => {
	if (typeof window !== 'undefined') (window as any).XLSX = XLSX
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: "acctnm", // Í≥ÑÏ†ïÍ≥ºÎ™©Î≥?Í∑∏Î£π??
			groupHeader: function(value, count, data, group) {
				const sumamt = data.reduce((acc, curr) => acc + curr.amt, 0)
				return `
					<div class="d-flex justify-content-between w-100 pe-4">
						<span class="fw-bold text-dark">${value} Í≥?/span>
						<div class="small fw-bold">
							<span class="mx-2 text-muted">?àÍ∏à???©Í≥Ñ: ${sumamt.toLocaleString()}</span>
						</div>
					</div>
				`
			},
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "?Ä??Í±∞ÎûòÏ≤?", field: "custnm", minWidth: 300 },
				{ title: "Íµ¨Ï¢åÎ≤àÌò∏", field: "mgtno", width: 250 },
				{ title: "Í∞úÏÑ§??, field: "stdymd", hozAlign: "center", width: 200 },
				{ title: "ÎßåÍ∏∞??, field: "endymd", hozAlign: "center", width: 200 },
				{ title: "?¥Ïú®", field: "rate", hozAlign: "center", width: 200,
					formatter: (cell) => Number(cell.getValue() || 0).toFixed(2) + '%'
				},
				{ title: "?àÍ∏à??, field: "amt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 200,
					cssClass: "fw-bold"
				},
				{ title: "", field: "empty", visible: true } // ?¨Î∞±??
			],
			columnHeaderVertAlign: "middle"
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-group) { background-color: #f0f4f8 !important; border-top: 1px solid #d1d9e6 !important; }
:deep(.tabulator-group-handle) { display: none; }
</style>
