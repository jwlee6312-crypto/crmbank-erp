<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- ?? 1. ?ÅÎã® ?°ÏÖò Î∞?-->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-list-columns-reverse me-2 text-primary" style="font-size: 18px;"></i>
        ÎßàÍ∞êÍ¥ÄÎ¶?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        ?µÍ≥Ñ?ÑÌô© <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">Í±∞ÎûòÏ≤?Îß§Ï∂ú?êÍ? list (HSCL520S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">Ï¥àÍ∏∞??/button>
        <button class="btn-erp btn-search" @click="search">Ï°∞Ìöå</button>
        <button class="btn-erp btn-excel" @click="excel">?ëÏ?</button>
      </div>
    </div>

    <!-- ?í° 2. Î©îÏù∏ Ïª®ÌÖêÏ∏??ÅÏó≠ -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">
      <!-- ?Ö∞Ô∏?Ï°∞Ìöå Ï°∞Í±¥ ?ÅÏó≠ -->
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
                <th class="required text-center bg-light">?êÎß§Î∂Ä??/th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="searchForm.deptnm" class="form-control fw-bold text-primary" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required text-center bg-light">Ï°∞Ìöå?∞Ïõî</th>
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
                  <span v-if="closingMonth" class="badge bg-primary px-2">ÎßàÍ∞ê: {{ formatYM(closingMonth) }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- ?Ö±Ô∏??∞Ïù¥??Í∑∏Î¶¨???ÅÏó≠ -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>Í±∞ÎûòÏ≤òÎ≥Ñ Îß§Ï∂ú?êÍ? Î∞??¥Ïùµ ?ÑÌô©</span>
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
import Modal from '@/components/Modal.vue'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { getDate } from '@/composables/useDate'

const authStore = useAuthStore()
const { today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps } = useCommonHelp()

const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 6 }, (_, i) => currentYear - i)
const monthOptions = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']

// ?îç Í≤Ä???∞Ïù¥??
const searchForm = reactive<any>({
	deptcd: authStore.deptcd,
	deptnm: authStore.deptnm,
	yy: currentYear,
    mm: today.substring(5, 7)
})

const closingMonth = ref('') // ?úÏä§??ÎßàÍ∞ê??(sclsym)
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

// Ï¥àÍ∏∞ ?∞Ïù¥??(ÎßàÍ∞ê?ïÎ≥¥) Ï°∞Ìöå
const getClosingInfo = async () => {
	try {
		const res = await api.post('/api/hs00/HS00_000S_STR', { gubun: 'CL', cmpycd: authStore.cmpycd })
		if (res.data?.length) {
			closingMonth.value = res.data[0].sclsym
			searchForm.yy = Number(closingMonth.value.substring(0, 4))
			searchForm.mm = closingMonth.value.substring(4, 6)
		}
	} catch (e) { console.error('ÎßàÍ∞ê?ïÎ≥¥ Ï°∞Ìöå ?§Ìå®') }
}

const search = async () => {
    if (!searchForm.deptcd) return vAlertError('?êÎß§Î∂Ä?úÎ? ?†ÌÉù??Ï£ºÏã≠?úÏò§.')
	const searchym = `${searchForm.yy}${searchForm.mm}`
    if (closingMonth.value && searchym > closingMonth.value) return vAlertError('?ÅÏóÖÎßàÍ∞ê?ëÏóÖ ??Ï°∞Ìöå?òÏãúÍ∏?Î∞îÎûç?àÎã§.')

	try {
		const res = await api.post('/api/hscl/HSCL_520S_STR', {
			cmpycd: authStore.cmpycd,
			deptcd: searchForm.deptcd,
			ym: searchym
		})
        const data = (res.data || []).map((i: any) => Object.fromEntries(Object.entries(i).map(([k, v]) => [k.toLowerCase(), v])));
		mainGrid?.setData(data)
		vAlert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { vAlertError('Ï°∞Ìöå ?§Ìå®') }
}

const initialize = () => {
	resetForm(searchForm)
    Object.assign(searchForm, { deptcd: authStore.deptcd, deptnm: authStore.deptnm })
	getClosingInfo()
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", "Í±∞ÎûòÏ≤òÎß§Ï∂úÏõêÍ∞Älist.xlsx")

// Î∂Ä???ÑÏ?Ï∞??§Ï†ï
function openHelp() {
    Object.assign(modalProps, {
        title: 'Î∂Ä???†ÌÉù', path: '/api/ha00/HA00_00P_STR',
        columns: [{ title: 'ÏΩîÎìú', field: 'deptcd', width: 80 }, { title: 'Î∂Ä?úÎ™Ö', field: 'deptnm', width: 180 }],
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
			groupBy: ["deptnm", "custnm"],
			groupHeader: (value, count, data, group) => {
				const label = group.getField() === "deptnm" ? "Î∂Ä?? " : "Í±∞ÎûòÏ≤? "
				return `<span class='fw-bold'>${label}${value || ''}</span> <span class='ms-2 text-muted small'>(${count}Í±?</span>`
			},
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: [
                { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
				{ title: "Î∂Ä?úÎ™Ö", field: "deptnm", width: 130, visible: false },
				{ title: "Í±∞ÎûòÏ≤?, field: "custnm", minWidth: 180, visible: false },
				{
					title: "Îß§Ï∂ú?ºÏûê", field: "salsymd", width: 110,
					formatter: (cell) => {
						const val = cell.getValue()
						return val && val.length === 8 ? `${val.substring(0, 4)}.${val.substring(4, 6)}.${val.substring(6, 8)}` : (val || '')
					}
				},
				{
					title: "Ï∂úÍ≥†Î≤àÌò∏", field: "io_no", width: 130,
					formatter: (cell) => {
						const d = cell.getData()
						return (d.ioym && d.iono) ? `${d.ioym}-${d.iono}` : ''
					}
				},
				{ title: "?àÎ™©Î™?, field: "itemnm", hozAlign: "left", minWidth: 200, cssClass: "fw-bold" },
				{ title: "?òÎüâ", field: "jsanqty", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{
					title: "Îß§Ï∂úÍ∏àÏï°", field: "jsanamt", width: 110, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money"
				},
				{
					title: "Îß§Ï∂ú?êÍ?", field: "wonamt", width: 110, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money"
				},
				{
					title: "?¥Ïùµ??, field: "bnfrate", width: 90, hozAlign: "right",
					formatter: (cell) => {
						const val = cell.getValue()
						return val ? Number(val).toFixed(2) + '%' : '0.00%'
					},
					bottomCalc: (values, data) => {
						const salesSum = data.reduce((acc: number, row: any) => acc + Number(row.jsanamt || 0), 0)
						const costSum = data.reduce((acc: number, row: any) => acc + Number(row.wonamt || 0), 0)
						return salesSum !== 0 ? (((salesSum - costSum) / salesSum) * 100).toFixed(2) + '%' : '0.00%'
					}
				}
			],
			columnCalcs: "table"
		})
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.tabulator-group) { background-color: #f8f9fa !important; }
:deep(.tabulator-group-level-1) { padding-left: 20px !important; background-color: #ffffff !important; }
</style>
