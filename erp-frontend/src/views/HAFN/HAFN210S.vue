<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Ï∞®ÏûÖÍ∏àÎ™Ö?∏ÏÑú
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : Í∏∞Ï??ºÏûê ?ÑÏû¨??Ï∞®ÏûÖÍ∏?Î™ÖÏÑ∏ Î∞??îÏï° ?ÑÌô© Ï°∞Ìöå
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ÅÎã® ?°ÏÖò Î∞?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-cash me-2 text-primary" style="font-size: 18px;"></i>
				?êÍ∏àÍ¥ÄÎ¶?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">Ï∞®ÏûÖÍ∏àÎ™Ö?∏ÏÑú (HAFN210S)</span>
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

const formatYmdShort = (v: string) => v && v.length === 8 ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : v

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_210S_STR', {
			cmpycd: authStore.cmpycd,
			ymd: searchForm.ymd.replace(/-/g, '')
		})
		const data = (res.data || []).map((row: any) => ({
			acctcd: row.acctcd,
			acctnm: row.acctnm,
			custcd: row.bankcd,
			custnm: row.banknm,
			mgtno: row.mgtno,
			stdymd: formatYmdShort(row.stdymd),
			endymd: formatYmdShort(row.endymd),
			rate: Number(row.rate || 0),
			loanamt: Number(row.loanamt || 0),
			repayamt: Number(row.amt || 0),
			janamt: Number(row.loanamt || 0) - Number(row.amt || 0),
			bigo: row.remark
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

const excel = () => mainGrid?.download("xlsx", `Ï∞®ÏûÖÍ∏àÎ™Ö?∏ÏÑú_${searchForm.ymd}.xlsx`)

const print = () => {
	const params = `ymd=${searchForm.ymd}`
	window.open(`/api/hafn/HAFN_210P?${params}`, 'LoanStatementPrint', 'width=1000,height=800,scrollbars=yes')
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: "acctnm",
			groupHeader: function(value, count, data, group) {
				const sumLoan = data.reduce((acc, curr) => acc + curr.loanamt, 0)
				const sumRepay = data.reduce((acc, curr) => acc + curr.repayamt, 0)
				const sumJan = data.reduce((acc, curr) => acc + curr.janamt, 0)
				return `
					<div class="d-flex justify-content-between w-100 pe-4">
						<span class="fw-bold text-dark">${value} Í≥?/span>
						<div class="small fw-bold">
							<span class="mx-2 text-muted">Ï∞®ÏûÖ?? ${sumLoan.toLocaleString()} / ?ÅÌôò?? ${sumRepay.toLocaleString()} / ?îÏï°: ${sumJan.toLocaleString()}</span>
						</div>
					</div>
				`
			},
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "Ï∞®ÏûÖÏ≤?, field: "custnm", width: 180 },
				{ title: "Ï∞®ÏûÖÎ≤àÌò∏", field: "mgtno", width: 120 },
				{ title: "Ï∞®ÏûÖ??, field: "stdymd", hozAlign: "center", width: 90 },
				{ title: "ÎßåÍ∏∞??, field: "endymd", hozAlign: "center", width: 90 },
				{ title: "?¥Ïú®", field: "rate", hozAlign: "right", width: 70,
					formatter: (cell) => Number(cell.getValue() || 0).toFixed(2)
				},
				{ title: "Ï∞®ÏûÖ??, field: "loanamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 110 },
				{ title: "?ÅÌôò??, field: "repayamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 110 },
				{ title: "?îÏï°", field: "janamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 110,
					cssClass: "fw-bold text-danger"
				},
				{ title: "ÎπÑÍ≥†", field: "bigo", minWidth: 150 },
				{ title: "", field: "empty", visible: true }
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
