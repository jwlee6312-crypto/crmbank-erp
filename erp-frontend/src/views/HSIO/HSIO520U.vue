<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-x-circle-fill me-2 text-danger" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매출정산취소 (HSIO520U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchCustomerList">조회</button>
        <button class="btn-erp btn-save" @click="cancelSettlement" :disabled="!selectedCust.custcd">정산취소</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">판매부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="searchData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.deptnm" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('SEARCH_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('SEARCH_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">정산일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="fromdt" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="todt" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th>거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="searchData.custcd" type="text" class="form-control text-center bg-light" readonly />
                    <input v-model="searchData.custnm" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('SEARCH_CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('SEARCH_CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 메인 작업 영역 (좌: 거래처 목록, 우: 정산 내역) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 정산 대상 거래처 -->
        <div class="card border shadow-sm d-flex flex-column" style="width: 250px;">
          <div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-list-ul me-1"></i> 정산 대상 상호
          </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                <div ref="custGridElement" class="tabulator-instance flex-grow-1"></div>
            </div>
        </div>

        <!-- 우측: 정산 내역 및 취소 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
              <span class="fw-bold small text-dark">
                <i class="bi bi-grid-3x3-gap-fill me-1"></i> 정산 내역 ({{ selectedCust.custnm || '거래처를 선택하세요' }})
              </span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                <div ref="settleGridElement" class="tabulator-instance flex-grow-1"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const initymd = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initfromdt = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  fromdt: initfromdt, todt: initymd, custcd: '', custnm: ''
})

const closingInfo = reactive({ clsymd: '', sclsym: '' })
const selectedCust = reactive({ custcd: '', custnm: '' })

const fromdt = computed({ get: () => formatDateString(searchData.fromdt, '-'), set: (v) => searchData.fromdt = v.replace(/-/g, '') })
const todt = computed({ get: () => formatDateString(searchData.todt, '-'), set: (v) => searchData.todt = v.replace(/-/g, '') })

const custGridElement = ref<HTMLElement | null>(null)
const settleGridElement = ref<HTMLElement | null>(null)
let custGrid: Tabulator | null = null
let settleGrid: Tabulator | null = null

const activeItemCount = ref(0)

const initGrids = () => {
  if (custGridElement.value) {
    custGrid = new Tabulator(custGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
      columns: [{ title: "거래처 상호", field: "custnm", headerSort: false }],
    })
    custGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      selectedCust.custcd = data.custcd
      selectedCust.custnm = data.custnm
      fetchSettlementList()
    })
  }

  if (settleGridElement.value) {
    settleGrid = new Tabulator(settleGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "거래처를 선택하세요.",
      columnDefaults: { headerSort: false },
      columns: [
        {
          title: "선택", field: "procyn", width: 60, hozAlign: "center",
          formatter: "tickCross", editor: true,
          cellEdited: (cell) => {
            const data = cell.getData()
            if (cell.getValue()) {
              const checkResult = checkCanCancel(data)
              if (checkResult !== true) {
                vAlertError(checkResult)
                cell.setValue(false)
              }
            }
          }
        },
        { title: "발행일", field: "jsanymd", width: 150, hozAlign: "center", formatter: (c) => formatDateString(c.getValue(), '-') },
        { title: "정산부서", field: "deptnm", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
        { title: "사업장", field: "unitnm", width: 180 },
        { title: "유형", field: "vattypenm", width: 180 },
        { title: "공급가", field: "spyamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "부가세", field: "vatamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "합계", field: "jsansum", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" }
      ]
    })
  }
}

// 마감 및 상태 체크
const checkCanCancel = (row: any) => {
  const jsanymd = row.jsanymd || row.jsanymD || ''
  const jsanyym = String(jsanymd).substring(0, 6)
  const slipymd = row.slipymd || row.SLIPYMD || ''
  const prtymd = row.prtymd || row.PRTYMD || ''

  if (slipymd && slipymd > "00000000") return "전표를 발행한 자료입니다. 취소할 수 없습니다."
  if (prtymd && prtymd > "00000000") return "세금계산서를 발행한 자료입니다. 취소할 수 없습니다."
  if (closingInfo.sclsym && jsanyym <= closingInfo.sclsym) return "영업정보가 마감된 자료입니다. 취소할 수 없습니다."
  if (closingInfo.clsymd && jsanymd <= closingInfo.clsymd) return "회계정보가 마감된 자료입니다. 취소할 수 없습니다."
  return true
}

async function fetchCustomerList() {
  if (!searchData.deptcd) return vAlertError('판매부서를 선택하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_520U_STR', {
      actkind: 'S1',
      cmpycd: authStore.cmpycd,
      gubun: '200',
      fromdt: searchData.fromdt,
      todt: searchData.todt,
      deptcd: searchData.deptcd,
      custcd: searchData.custcd
    })
    custGrid?.setData(res.data)
    settleGrid?.clearData()
    selectedCust.custcd = ''
    selectedCust.custnm = ''
    activeItemCount.value = 0
  } catch (e) { vAlertError('거래처 목록 조회 실패') }
}

async function fetchSettlementList() {
  try {
    const res = await api.post('/api/hsio/HSIO_520U_STR', {
      actkind: 'S0',
      cmpycd: authStore.cmpycd,
      gubun: '200',
      fromdt: searchData.fromdt,
      todt: searchData.todt,
      deptcd: searchData.deptcd,
      custcd: selectedCust.custcd
    })
    const mapped = res.data.map((i: any) => ({
      ...i,
      jsanymd: i.jsanymd,
      spyamt: i.spyamt,
      vatamt: i.vatamt,
      jsansum: Number(i.spyamt || 0) + Number(i.vatamt || 0),
      procyn: false
    }))
    settleGrid?.setData(mapped)
    activeItemCount.value = mapped.length
  } catch (e) { vAlertError('정산 내역 조회 실패') }
}

/**
 * 🚀 정산 취소 처리 (ASP 로직 반영)
 */
async function cancelSettlement() {
  const selected = settleGrid?.getData().filter((i: any) => i.procyn)
  if (!selected || selected.length === 0) return vAlertError('취소할 정산 자료를 선택하세요.')

  if (!confirm('선택한 정산 자료를 취소하시겠습니까?')) return

  try {
    for (const item of selected) {
      const res = await api.post('/api/hsio/HSIO_520U_STR', {
        actkind: 'D0',
        cmpycd: authStore.cmpycd,
        gubun: '200',
        fromdt: searchData.fromdt,
        todt: searchData.todt,
        deptcd: searchData.deptcd,
        custcd: selectedCust.custcd,
        jsanym: item.jsanym,
        jsanno: item.jsanno,
        userid: authStore.userid
      })

      const resData = res.data?.[0]
      // ASP: if rs(0) = "Y" then ErrYn = "Y" (에러 발생 시 중단 및 롤백 개념)
      if (resData && (resData.result === 'Y' || resData.RESULT === 'Y' || resData.erryn === 'Y' || resData.ERRYN === 'Y')) {
        vAlertError(resData.msg || resData.MSG || '취소 처리 중 업무 오류가 발생했습니다.')
        fetchSettlementList()
        return
      }
    }
    vAlert('정상적으로 취소되었습니다.')
    fetchCustomerList()
  } catch (e) { vAlertError('취소 처리 실패') }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { deptcd: authStore.deptcd, deptnm: authStore.deptnm, fromdt: initfromdt, todt: initymd })
  selectedCust.custcd = ''
  selectedCust.custnm = ''
  custGrid?.clearData()
  settleGrid?.clearData()
  activeItemCount.value = 0
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let gubun = 'D0', title = '부서 선택', field = 'deptnm'
  if (type.includes('CUST')) { gubun = 'c4'; title = '거래처 선택'; field = 'custnm' }

  Object.assign(modalProps, {
    title, path: '/api/ha00/HA00_00P_STR', defaultField: field,
    data: { gubun, cmpycd: authStore.cmpycd },
    columns: gubun === 'D0'
      ? [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }]
      : [{ title: '코드', field: 'custcd', width: 70 }, { title: '거래처명', field: 'custnm', width: 180 }],
    onConfirm: (data: any) => {
      if (type === 'SEARCH_DEPT') { searchData.deptcd = data.deptcd; searchData.deptnm = data.deptnm }
      else if (type === 'SEARCH_CUST') { searchData.custcd = data.custcd; searchData.custnm = data.custnm }
    }
  })
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'cl', cmpycd: authStore.cmpycd } }).then(r => {
    if (r.data?.length) {
      const d = r.data[0]
      closingInfo.clsymd = String(d.clsymd || d.CLSYMD || Object.values(d)[0]).trim()
      closingInfo.sclsym = String(d.sclsym || d.SCLSYM || Object.values(d)[1]).trim()
    }
  })
  nextTick(() => initGrids())
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
