<!--
	=============================================================
	?�로그램�?: 거래처별 매출 총이??(HSCL220S)
	?�성?�자	: 2025.02.24
	?�명        : 거래처별 ?�월/?�계 매출 총이???�황 조회 (HSOD100U ?�자???��? ?�용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- ?? 1. ?�단 ?�션 �?-->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
        마감관�?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        ?�계?�황 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">거래처별 매출 총이??(HSCL220S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기??/button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-print" @click="print">?�쇄</button>
        <button class="btn-erp btn-excel" @click="excel">?��?</button>
      </div>
    </div>

    <!-- ?�� 2. 메인 컨텐�??�역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">
      <!-- ?���?조회 조건 ?�역 -->
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
                <th class="required text-center bg-light">매출부??/th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="searchForm.deptnm" class="form-control fw-bold text-primary" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required text-center bg-light">조회?�월</th>
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
                  <span v-if="closingMonth" class="badge bg-primary px-2">마감: {{ formatYM(closingMonth) }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- ?���??�이??그리???�역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>거래처별 매출 총이???�황</span>
          <div class="small text-muted">거래처명???�릭?�면 ?�세 ?�불 ?�역(HSCL230S)?�로 ?�동?�니??</div>
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
	} catch (e) { console.error('마감?�보 조회 ?�패') }
}

const search = async () => {
	if (!searchForm.deptcd) return vAlertError('매출부?��? ?�택??주십?�오.')
	const searchym = `${searchForm.yy}${searchForm.mm}`
	if (closingMonth.value && searchym > closingMonth.value) return vAlertError('?�업마감?�업 ??조회?�시�?바랍?�다.')

	try {
		const res = await api.post('/api/hscl/HSCL_220S_STR', {
			cmpycd: authStore.cmpycd,
			deptcd: searchForm.deptcd,
			yymm: searchym
		})
        const data = (res.data || []).map((i: any) => Object.fromEntries(Object.entries(i).map(([k, v]) => [k.toLowerCase(), v])));
		mainGrid?.setData(data)
		vAlert('조회?�었?�니??')
	} catch (e) { vAlertError('조회 ?�패') }
}

const initialize = () => {
	resetForm(searchForm)
    Object.assign(searchForm, { deptcd: authStore.deptcd, deptnm: authStore.deptnm })
	getClosingInfo()
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", "거래처별매출총이??xlsx")
const print = () => {
	const params = `deptcd=${searchForm.deptcd}&deptnm=${searchForm.deptnm}&yy=${searchForm.yy}&mm=${searchForm.mm}`
	window.open(`/api/hscl/HSCL_220P?${params}`, 'Print', 'width=1000,height=800')
}

function openHelp() {
    Object.assign(modalProps, {
        title: '부???�택', path: '/api/ha00/HA00_00P_STR',
        columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부?�명', field: 'deptnm', width: 180 }],
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
					title: "�???�?, field: "custnm", minWidth: 200, hozAlign: "left",
					cssClass: "text-primary cursor-pointer fw-bold border-end",
					cellClick: (e, cell) => {
						const d = cell.getData()
						router.push({ path: '/HSCL/HSCL230S', query: { yy: searchForm.yy, mm: searchForm.mm, deptcd: searchForm.deptcd, custcd: d.custcd } })
					}
				},
				{
					title: "????,
					columns: [
						{ title: "매출??, field: "salsamt", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
						{ title: "매출?��?", field: "salscost", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
						{
							title: "매총?�익", field: "salsprof", hozAlign: "right", width: 150,
							formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money"
						},
						{
							title: "%", field: "prof_rate", hozAlign: "right", width: 100,
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
					title: "??�?,
					columns: [
						{ title: "매출??, field: "salsamt_t", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
						{ title: "매출?��?", field: "salscost_t", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
						{
							title: "매총?�익", field: "salsprof_t", hozAlign: "right", width: 150,
							formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money"
						},
						{
							title: "%", field: "prof_rate_t", hozAlign: "right", width: 100,
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

/* ?? 2???�더?�서 ?�일 컬럼???�?��????�직 중앙 ?�렬 */
:deep(.tabulator-header .tabulator-col:not(.tabulator-col-group) .tabulator-col-content) {
	height: 100% !important;
	display: flex !important;
	align-items: center !important;
	justify-content: center !important;
}
</style>
