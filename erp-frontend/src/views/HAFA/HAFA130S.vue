<!--	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Í∞êÍ??ÅÍ∞ÅÎπ?ÏßëÍ≥Ñ??
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?ëÏÑ±??    : AI Assistant
	?§Î™Ö        : Î∂Ä?úÎ≥Ñ/Í≥ÑÏ†ïÍ≥ºÎ™©Î≥?Í∞êÍ??ÅÍ∞ÅÎπ?Î∞úÏÉù ?ÑÌô© ÏßëÍ≥Ñ Ï°∞Ìöå (?úÏ? ?îÏûê??Î∞?ÏßëÍ≥Ñ ÏΩ§Îßà ?ÅÏö©)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- ?? 1. ?ÅÎã® ?°ÏÖò Î∞?-->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
				Í≥†Ï†ï?êÏÇ∞ <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				?ÅÍ∞Å?ÑÌô© <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">Í∞êÍ??ÅÍ∞ÅÎπ?ÏßëÍ≥Ñ??(HAFA130S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-search" @click="search">Ï°∞Ìöå</button>
				<button class="btn-erp btn-print" @click="print">?∏ÏáÑ</button>
				<button class="btn-erp btn-excel" @click="excel">?ëÏ?</button>
			</div>
		</div>

		<!-- ?í° 2. Î©îÏù∏ Ïª®ÌÖêÏ∏??ÅÏó≠ -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- [?ÅÎã®] Ï°∞Ìöå ?ÑÌÑ∞ ?ÅÏó≠ -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense" width="100%">
						<colgroup>
							<col style="width: 10%" /><col style="width: 90%" />
						</colgroup>
						<tbody>
							<tr>
								<th class="text-center bg-light">Í∏∞Ï??∞Ïõî</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<select v-model="searchForm.yy" class="form-select form-select-sm" style="width: 100px;">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}??/option>
										</select>
										<select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 80px;">
											<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}??/option>
										</select>
										<span class="small fw-bold ms-2 text-secondary">Î∂?Í∞êÍ??ÅÍ∞Å ÏßëÍ≥Ñ</span>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- ?ìä 3. Í∑∏Î¶¨???ÅÏó≠ -->
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

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({ yy: String(currentYear), mm: currentMonth })
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

// ?∞Ïù¥???§Î? ?åÎ¨∏?êÎ°ú ?ïÍ∑ú??
const normalizeKeys = (data: any): any => {
  if (!data) return data;
  if (Array.isArray(data)) return data.map(item => normalizeKeys(item));
  if (typeof data !== 'object' || data instanceof Date) return data;
  const normalized: any = {};
  Object.keys(data).forEach(key => { normalized[key.toLowerCase().trim()] = normalizeKeys(data[key]); });
  return normalized;
};

async function search() {
	try {
		const res = await api.post('/api/hafa/HAFA_130S_STR', { cmpycd: authStore.cmpycd, baseym: searchForm.yy + searchForm.mm })
		mainGrid?.setData(normalizeKeys(res.data || [])); vAlert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { vAlertError('Ï°∞Ìöå Ï§??§Î•ò Î∞úÏÉù') }
}

function excel() { mainGrid?.download("xlsx", `Í∞êÍ??ÅÍ∞ÅÎπÑÏßëÍ≥ÑÌëú_${searchForm.yy}${searchForm.mm}.xlsx`) }
function print() { const params = `yy=${searchForm.yy}&mm=${searchForm.mm}&PRTGU=1`; window.open(`/api/hafa/HAFA_130P?${params}`, 'DepreciationSummary', 'width=1000,height=800,scrollbars=yes') }

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "Î∂Ä?úÏΩî??, field: "deptcd", width: 100, hozAlign: "center" },
				{ title: "Î∂Ä?úÎ™Ö", field: "deptnm", width: 150 },
				{ title: "Í≥ÑÏ†ïÏΩîÎìú", field: "acctcd", width: 100, hozAlign: "center" },
				{ title: "Í≥ÑÏ†ïÍ≥ºÎ™©Î™?, field: "acctnm", minWidth: 150, cssClass: "fw-bold" },
				{
                    title: "Í∏∞Ï¥àÍ∞Ä??, field: "baseamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
                    bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
                },
				{
                    title: "?πÍ∏∞Ï¶ùÍ?", field: "incramt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
                    bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
                },
				{
                    title: "?πÍ∏∞?ÅÍ∞Å??, field: "dprsamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
                    bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 },
                    cssClass: "text-primary fw-bold"
                },
				{
                    title: "?ÅÍ∞Å?ÑÍ≥Ñ??, field: "dprssum", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
                    bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
                },
				{
                    title: "ÎØ∏ÏÉÅÍ∞ÅÏûî??, field: "janamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
                    bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 },
                    cssClass: "fw-bold"
                }
			]
		})
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
