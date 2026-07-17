<!--	=============================================================
	?„ë¡œê·¸ë¨ëª?: ê°ê??ê°ëª…ì„¸??
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ê³„ì •ê³¼ëª©ë³?ê°ê??ê° ?„í™© ë°?ë¯¸ìƒê°??”ì•¡ ëª…ì„¸ ì¡°íšŒ (?œì? ?”ì???ìš©)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- ?? 1. ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-ruled me-2 text-primary" style="font-size: 18px;"></i>
				ê³ ì •?ì‚° <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				?ê°?„í™© <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">ê°ê??ê°ëª…ì„¸??(HAFA120S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-search" @click="search">ì¡°íšŒ</button>
				<button class="btn-erp btn-print" @click="print">?¸ì‡„</button>
				<button class="btn-erp btn-excel" @click="excel">?‘ì?</button>
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
								<th class="text-center bg-light">ê¸°ì??°ì›”</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<select v-model="searchForm.yy" class="form-select form-select-sm" style="width: 100px;">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}??/option>
										</select>
										<select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 80px;">
											<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}??/option>
										</select>
										<span class="small fw-bold ms-2 text-secondary">ë¶??ê°ëª…ì„¸ ì¡°íšŒ</span>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- ?“Š 3. ê·¸ë¦¬???ì—­ -->
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
import { useTabStore } from '@/stores/tabStore'

const authStore = useAuthStore()
const tabStore = useTabStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({ yy: String(currentYear), mm: currentMonth })
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

async function search() {
	try {
		const res = await api.post('/api/hafa/HAFA_120S_STR', { cmpycd: authStore.cmpycd, baseym: searchForm.yy + searchForm.mm })
		const data = (res.data || []).map((row: any) => ({ acctcd: row.col0, acctnm: row.col1, baseamt: Number(row.col2 || 0), bdprssum: Number(row.col3 || 0), dprsamt: Number(row.col4 || 0), dprssum: Number(row.col5 || 0), janamt: Number(row.col6 || 0), upacct: row.col7 ? row.col7.substring(0, 3) : '' }))
		mainGrid?.setData(data); vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) { vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

function excel() { mainGrid?.download("xlsx", `ê°ê??ê°ëª…ì„¸??${searchForm.yy}${searchForm.mm}.xlsx`) }
function print() { const params = `yy=${searchForm.yy}&mm=${searchForm.mm}&PRTGU=1`; window.open(`/api/hafa/HAFA_120P?${params}`, 'DepreciationStatement', 'width=1000,height=800,scrollbars=yes') }

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			groupBy: "upacct",
			groupHeader: (value, count) => `ê³„ì •ê·¸ë£¹ (${value}) - ${count}ê°???ª©`,
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "ê³„ì •ì½”ë“œ", field: "acctcd", width: 100, hozAlign: "center", cssClass: "text-primary fw-bold" },
				{ title: "ê³„ì •ëª?, field: "acctnm", width: 200 },
				{ title: "ê¸°ì´ˆê°€??, field: "baseamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "?„ê¸°ë§ìƒê°ëˆ„ê³?, field: "bdprssum", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "?¹ê¸°?ê°??, field: "dprsamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", cssClass: "text-primary fw-bold" },
				{ title: "?¹ê¸°ë§ìƒê°ëˆ„ê³?, field: "dprssum", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "ë¯¸ìƒê°ì”??, field: "janamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", cssClass: "fw-bold" }
			]
		})
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
