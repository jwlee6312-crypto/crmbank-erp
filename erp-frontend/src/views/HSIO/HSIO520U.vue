<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio520u-wrapper d-flex flex-column h-100 bg-white p-0">
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
        <button class="btn-erp btn-save" @click="cancelSettlement" :disabled="!selectedCust.CUSTCD">정산취소</button>
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
                    <input v-model="searchData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('SEARCH_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('SEARCH_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">정산일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiIOYMDFR" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiIOYMDTO" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th>거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="searchData.CUSTCD" type="text" class="form-control text-center bg-light" readonly />
                    <input v-model="searchData.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('SEARCH_CUST')" />
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
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="custGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <!-- 우측: 정산 내역 및 취소 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
              <span class="fw-bold small text-dark">
                <i class="bi bi-grid-3x3-gap-fill me-1"></i> 정산 내역 ({{ selectedCust.CUSTNM || '거래처를 선택하세요' }})
              </span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white">
              <div ref="settleGridElement" style="height: 100%;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 전표나 세금계산서가 발행된 자료는 취소할 수 없습니다.
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
const initYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initFRYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  IOYMDFR: initFRYMD, IOYMDTO: initYMD, CUSTCD: '', CUSTNM: ''
})

const closingInfo = reactive({ CLSYMD: '', SCLSYM: '' })
const selectedCust = reactive({ CUSTCD: '', CUSTNM: '' })

const uiIOYMDFR = computed({ get: () => formatDateString(searchData.IOYMDFR, '-'), set: (v) => searchData.IOYMDFR = v.replace(/-/g, '') })
const uiIOYMDTO = computed({ get: () => formatDateString(searchData.IOYMDTO, '-'), set: (v) => searchData.IOYMDTO = v.replace(/-/g, '') })

const custGridElement = ref<HTMLElement | null>(null)
const settleGridElement = ref<HTMLElement | null>(null)
let custGrid: Tabulator | null = null
let settleGrid: Tabulator | null = null

const activeItemCount = ref(0)

const initGrids = () => {
  if (custGridElement.value) {
    custGrid = new Tabulator(custGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
      columns: [{ title: "거래처 상호", field: "CUSTNM", headerSort: false }],
    })
    custGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      selectedCust.CUSTCD = data.CUSTCD
      selectedCust.CUSTNM = data.CUSTNM
      fetchSettlementList()
    })
  }

  if (settleGridElement.value) {
    settleGrid = new Tabulator(settleGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "거래처를 선택하세요.",
      columnDefaults: { headerSort: false },
      columns: [
        {
          title: "선택", field: "PROCYN", width: 40, hozAlign: "center",
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
        { title: "발행일", field: "JSANYMD", width: 100, hozAlign: "center", formatter: (c) => formatDateString(c.getValue(), '-') },
        { title: "정산부서", field: "DEPTNM", width: 150 },
        { title: "사업장", field: "UNITNM", width: 120 },
        { title: "유형", field: "VATTYPENM", width: 100 },
        { title: "공급가", field: "SPYAMT", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "부가세", field: "VATAMT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "합계", field: "JSANSUM", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" }
      ]
    })
  }
}

// ASP ProcCheck 로직 구현
const checkCanCancel = (row: any) => {
  const jsanyym = String(row.JSANYMD || '').substring(0, 6)
  if (row.SLIPYMD && row.SLIPYMD > "00000000") return "전표를 발행한 자료입니다. 취소할 수 없습니다."
  if (row.PRTYMD && row.PRTYMD > "00000000") return "세금계산서를 발행한 자료입니다. 취소할 수 없습니다."
  if (jsanyym <= closingInfo.SCLSYM) return "영업정보가 마감된 자료입니다. 취소할 수 없습니다."
  if (row.JSANYMD <= closingInfo.CLSYMD) return "회계정보가 마감된 자료입니다. 취소할 수 없습니다."
  return true
}

async function fetchCustomerList() {
  if (!searchData.DEPTCD) return vAlertError('판매부서를 선택하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_520U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD,
      IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO,
      DEPTCD: searchData.DEPTCD, CUSTCD: searchData.CUSTCD
    })
    custGrid?.setData(res.data)
    settleGrid?.clearData()
    selectedCust.CUSTCD = ''
    selectedCust.CUSTNM = ''
    activeItemCount.value = 0
  } catch (e) { vAlertError('거래처 목록 조회 실패') }
}

async function fetchSettlementList() {
  try {
    const res = await api.post('/api/hsio/HSIO_520U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO,
      DEPTCD: searchData.DEPTCD, CUSTCD: selectedCust.CUSTCD
    })
    const mapped = res.data.map((i: any) => ({
      ...i,
      JSANSUM: Number(i.SPYAMT || 0) + Number(i.VATAMT || 0),
      PROCYN: false
    }))
    settleGrid?.setData(mapped)
    activeItemCount.value = mapped.length
  } catch (e) { vAlertError('정산 내역 조회 실패') }
}

async function cancelSettlement() {
  const selected = settleGrid?.getData().filter((i: any) => i.PROCYN)
  if (!selected || selected.length === 0) return vAlertError('취소할 정산 자료를 선택하세요.')

  if (!confirm('선택한 정산 자료를 취소하시겠습니까?')) return

  try {
    for (const item of selected) {
      const res = await api.post('/api/hsio/HSIO_520U_STR', {
        ...item,
        ACTKIND: 'D0',
        CMPYCD: authStore.CMPYCD,
        IOYMDFR: searchData.IOYMDFR,
        IOYMDTO: searchData.IOYMDTO,
        USERID: authStore.USERID
      })
      if (res.data?.[0]?.RESULT === 'Y' || res.data?.[0]?.ERRYN === 'Y') {
        vAlertError(res.data[0].MSG || '취소 처리 중 오류 발생')
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
  Object.assign(searchData, { DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, IOYMDFR: initFRYMD, IOYMDTO: initYMD })
  selectedCust.CUSTCD = ''
  selectedCust.CUSTNM = ''
  custGrid?.clearData()
  settleGrid?.clearData()
  activeItemCount.value = 0
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let gubun = 'D0', title = '부서 선택', field = 'DEPTNM'
  if (type.includes('CUST')) { gubun = 'C4'; title = '거래처 선택'; field = 'CUSTNM' }

  Object.assign(modalProps, {
    title, path: '/api/ha00/HA00_00P_STR', defaultField: field,
    data: { GUBUN: gubun, CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
    columns: gubun === 'D0'
      ? [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }]
      : [{ title: '코드', field: 'CUSTCD', width: 70 }, { title: '거래처명', field: 'CUSTNM', width: 180 }],
    onConfirm: (data: any) => {
      if (type === 'SEARCH_DEPT') { searchData.DEPTCD = data.DEPTCD; searchData.DEPTNM = data.DEPTNM }
      else if (type === 'SEARCH_CUST') { searchData.CUSTCD = data.CUSTCD; searchData.CUSTNM = data.CUSTNM }
    }
  })
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      closingInfo.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(r.data[0])[1]).trim()
    }
  })
  nextTick(() => initGrids())
})
</script>

<style scoped>
.hsio520u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #d32f2f !important; color: #fff !important; border: none !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
