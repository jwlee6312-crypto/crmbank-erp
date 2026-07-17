<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ?´ë‹¹?ë³„ ë§¤ì¶œ ì´ì´??(HSCL270S)
	?‘ì„±?¼ì	: 2025.02.24
	?¤ëª…        : ?´ë‹¹?ë³„ ?¹ì›”/?„ê³„ ë§¤ì¶œ ì´ì´???„í™© ì¡°íšŒ (HSOD100U ?”ì???œì? ?ìš©)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- ?? 1. ?ë‹¨ ?¡ì…˜ ë°?-->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-person-badge-fill me-2 text-primary" style="font-size: 18px;"></i>
        ë§ˆê°ê´€ë¦?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        ?µê³„?„í™© <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">?´ë‹¹?ë³„ ë§¤ì¶œ ì´ì´??(HSCL270S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">ì´ˆê¸°??/button>
        <button class="btn-erp btn-search" @click="search">ì¡°íšŒ</button>
        <button class="btn-erp btn-print" @click="print">?¸ì‡„</button>
        <button class="btn-erp btn-excel" @click="excel">?‘ì?</button>
      </div>
    </div>

    <!-- ?’¡ 2. ë©”ì¸ ì»¨í…ì¸??ì—­ -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">
      <!-- ?…°ï¸?ì¡°íšŒ ì¡°ê±´ ?ì—­ -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 25%" />
                <col style="width: 10%" /><col style="width: 20%" />
                <col style="width: 10%" /><col style="width: 25%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="required text-center bg-light">ë§¤ì¶œë¶€??/th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="searchForm.deptnm" class="form-control fw-bold text-primary" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required text-center bg-light">ì¡°íšŒ?°ì›”</th>
                <td>
                  <div class="d-flex align-items-center gap-1">
                    <select v-model="searchForm.yy" class="form-select form-select-sm" style="width: 100px;">
                      <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}??/option>
                    </select>
                    <select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 80px;">
                      <option v-for="month in monthOptions" :key="month" :value="month">{{ month }}??/option>
                    </select>
                  </div>
                </td>
                <td class="text-end pe-3 bg-white">
                  <span v-if="closingMonth" class="badge bg-primary px-2">ë§ˆê°: {{ formatYM(closingMonth) }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- ?…±ï¸??°ì´??ê·¸ë¦¬???ì—­ -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>?´ë‹¹?ë³„ ë§¤ì¶œ ì´ì´???„í™©</span>
          <div class="small text-muted">?ì—…?´ë‹¹ëª…ì„ ?´ë¦­?˜ë©´ ?ì„¸ ?´ì—­(HSCL280S)?¼ë¡œ ?´ë™?©ë‹ˆ??</div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import * as XLSX from 'xlsx'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useRouter } from 'vue-router'
import Modal from '@/components/Modal.vue'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { getDate } from '@/composables/useDate'

const authStore = useAuthStore()
const router = useRouter()
const { today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps } = useCommonHelp()

const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 6 }, (_, i) => currentYear - i)
const monthOptions = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']

const searchForm = reactive<any>({
	deptcd: authStore.deptcd,
	deptnm: authStore.deptnm,
	yy: currentYear,
    mm: today.substring(5, 7)
})

const closingMonth = ref('')
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const getClosingInfo = async () => {
	try {
		const res = await api.post('/api/hs00/HS00_000S_STR', { gubun: 'CL', cmpycd: authStore.cmpycd })
		if (res.data?.length) {
			closingMonth.value = res.data[0].sclsym
			searchForm.yy = Number(closingMonth.value.substring(0, 4))
			searchForm.mm = closingMonth.value.substring(4, 6)
		}
	} catch (e) { console.error('ë§ˆê°?•ë³´ ì¡°íšŒ ?¤íŒ¨') }
}

const search = async () => {
	if (!searchForm.deptcd) return vAlertError('ë§¤ì¶œë¶€?œë? ? íƒ??ì£¼ì‹­?œì˜¤.')
	const searchym = `${searchForm.yy}${searchForm.mm}`
	if (closingMonth.value && searchym > closingMonth.value) return vAlertError('?ì—…ë§ˆê°?‘ì—… ??ì¡°íšŒ?˜ì‹œê¸?ë°”ë?ˆë‹¤.')

	try {
		const res = await api.post('/api/hscl/HSCL_270S_STR', {
			cmpycd: authStore.cmpycd,
			deptcd: searchForm.deptcd,
			yymm: searchym
		})
        const data = (res.data || []).map((i: any) => Object.fromEntries(Object.entries(i).map(([k, v]) => [k.toLowerCase(), v])));
		mainGrid?.setData(data)
		vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) { vAlertError('ì¡°íšŒ ?¤íŒ¨') }
}

const initialize = () => {
	resetForm(searchForm)
    Object.assign(searchForm, { deptcd: authStore.deptcd, deptnm: authStore.deptnm })
	getClosingInfo()
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", "?´ë‹¹?ë³„ë§¤ì¶œì´ì´??xlsx")
const print = () => {
	const params = `deptcd=${searchForm.deptcd}&deptnm=${searchForm.deptnm}&yy=${searchForm.yy}&mm=${searchForm.mm}`
	window.open(`/api/hscl/HSCL_270P?${params}`, 'Print', 'width=1000,height=800')
}

function openHelp() {
    Object.assign(modalProps, {
        title: 'ë¶€??? íƒ', path: '/api/ha00/HA00_00P_STR',
        columns: [{ title: 'ì½”ë“œ', field: 'deptcd', width: 80 }, { title: 'ë¶€?œëª…', field: 'deptnm', width: 180 }],
        data: { gubun: 'D0', cmpycd: authStore.cmpycd },
        onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search(); }
    })
	modalVisible.value = true
}

const formatYM = (v: string) => v ? `${v.substring(0, 4)}-${v.substring(4, 6)}` : '';

onMounted(() => {
	if (typeof window !== 'undefined') (window as any).XLSX = XLSX;
	getClosingInfo()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: [
                { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
				{
					title: "????????, field: "usernm", minWidth: 150, hozAlign: "left",
					cssClass: "text-primary cursor-pointer fw-bold border-end",
					cellClick: (e, cell) => {
						const d = cell.getData()
						router.push({ path: '/HSCL/HSCL280S', query: { yy: searchForm.yy, mm: searchForm.mm, deptcd: searchForm.deptcd, userid: d.userid } })
					}
				},
				{
					title: "????,
					columns: [
						{ title: "ë§¤ì¶œ??, field: "salsamt", hozAlign: "right", width: 130, formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
						{ title: "ë§¤ì¶œ?ê?", field: "salscost", hozAlign: "right", width: 130, formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
						{
							title: "ë§¤ì´?´ìµ", field: "salsprof", hozAlign: "right", width: 130,
							formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money"
						},
						{
							title: "%", field: "prof_rate", hozAlign: "right", width: 80,
							formatter: (cell) => {
								const d = cell.getData()
								const rate = d.salsamt ? ((d.salsamt - d.salscost) / d.salsamt * 100) : 0
								return rate.toFixed(1)
							},
							bottomCalc: (values, data) => {
								const amt = data.reduce((s: number, r: any) => s + Number(r.salsamt || 0), 0)
								const cost = data.reduce((s: number, r: any) => s + Number(r.salscost || 0), 0)
								return amt ? ((amt - cost) / amt * 100).toFixed(1) : "0.0"
							}
						}
					]
				},
				{
					title: "??ê³?,
					columns: [
						{ title: "ë§¤ì¶œ??, field: "salsamt_t", hozAlign: "right", width: 140, formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
						{ title: "ë§¤ì¶œ?ê?", field: "salscost_t", hozAlign: "right", width: 140, formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
						{
							title: "ë§¤ì´?´ìµ", field: "salsprof_t", hozAlign: "right", width: 140,
							formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money"
						},
						{
							title: "%", field: "prof_rate_t", hozAlign: "right", width: 80,
							formatter: (cell) => {
								const d = cell.getData()
								const rate = d.salsamt_t ? ((d.salsamt_t - d.salscost_t) / d.salsamt_t * 100) : 0
								return rate.toFixed(1)
							},
							bottomCalc: (values, data) => {
								const amt = data.reduce((s: number, r: any) => s + Number(r.salsamt_t || 0), 0)
								const cost = data.reduce((s: number, r: any) => s + Number(r.salscost_t || 0), 0)
								return amt ? ((amt - cost) / amt * 100).toFixed(1) : "0.0"
							}
						}
					]
				}
			],
			columnCalcs: "table"
		})
	}
})
</script>


<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; border-bottom: 3px solid #005a9f !important; }

/* ?? 2???¤ë”?ì„œ ?¨ì¼ ì»¬ëŸ¼???€?´í????˜ì§ ì¤‘ì•™ ?•ë ¬ */
:deep(.tabulator-header .tabulator-col:not(.tabulator-col-group) .tabulator-col-content) {
	height: 100% !important;
	display: flex !important;
	align-items: center !important;
	justify-content: center !important;
}
</style>
