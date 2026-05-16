<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio510u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 (조회/저장/초기화) -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매출정산 (HSIO510U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchCustomerList">조회</button>
        <button class="btn-erp btn-save" @click="saveSettlement" :disabled="!selectedCust.CUSTCD">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 (조회 조건 + 좌/우 분할 레이아웃) -->
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
                <th class="required">판매일자</th>
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

      <!-- 🅱️ 메인 작업 영역 (좌: 거래처 목록, 우: 정산 상세) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">

        <!-- 좌측: 거래처 목록 (원본 HSIO_510U_IF.asp 역할) -->
        <div class="card border shadow-sm d-flex flex-column" style="width: 250px;">
          <div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-list-ul me-1"></i> 정산 대상 상호
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="custGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <!-- 우측: 정산 상세 및 등록 (원본 HSIO_510U_IF_1.asp 역할) -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

          <!-- 정산 등록 정보 -->
          <div class="card border shadow-sm overflow-hidden">
            <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
              <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 정산 정보 (등록용)</span>
            </div>
            <div class="card-body p-0">
              <table class="erp-table-full">
                <tbody>
                  <tr>
                    <th>거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                    <td>
                      <input v-model="selectedCust.CUSTNM" type="text" class="form-control form-control-sm bg-light fw-bold" readonly />
                    </td>
                    <th class="required bg-light-primary">판매부서</th>
                    <td>
                      <div class="input-group input-group-sm" style="width: 220px;">
                        <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                        <input v-model="masterData.DEPTNM" type="text" class="form-control" @keyup.enter="openHelp('MASTER_DEPT')" />
                        <button class="btn btn-outline-secondary" @click="openHelp('MASTER_DEPT')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light-primary">발&nbsp;행&nbsp;일</th>
                    <td>
                      <input v-model="uiPUBYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
                    </td>
                  </tr>
                  <tr>
                    <th>사&nbsp;업&nbsp;장</th>
                    <td>
                      <select v-model="masterData.TAXUNIT" class="form-select form-select-sm">
                        <option v-for="opt in taxUnitOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                      </select>
                    </td>
                    <th>유&nbsp;&nbsp;&nbsp;&nbsp;형</th>
                    <td>
                      <select v-model="masterData.VATTYPE" class="form-select form-select-sm">
                        <option v-for="opt in vatTypeOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                      </select>
                    </td>
                    <td colspan="2" class="bg-light px-2 text-muted small py-1">
                      <i class="bi bi-info-circle me-1"></i> 세금계산서 발행 단위별로 정산하십시오.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 정산 품목 그리드 (여백 없이 꽉 채움) -->
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 정산 품목 내역</span>
              <div class="d-flex gap-2 align-items-center">
                <span class="small text-muted">전체선택</span>
                <input type="checkbox" v-model="allSelected" @change="toggleAllSelection" class="form-check-input" />
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white">
              <div ref="itemGridElement" style="height: 100%;"></div>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="me-4 small opacity-75">공급가: <span class="fw-bold text-info ms-1">{{ formatNumber(masterData.SPLAMT) }}</span></span>
          <span class="me-4 small opacity-75">부가세: <span class="fw-bold text-warning ms-1">{{ formatNumber(masterData.VATAMT) }}</span></span>
          <span class="fs-5 ms-2 fw-light">총 합계: <span class="fw-bold text-white ms-2">{{ formatNumber(sumTotal) }}</span></span>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick, watch } from 'vue'
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

const masterData = reactive<any>({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  TAXUNIT: '100', VATTYPE: '010', PUBYMD: initYMD,
  SPLAMT: 0, VATAMT: 0, PRICEGBN: '1', CLSYMD: '', SCLSYM: ''
})

const selectedCust = reactive({ CUSTCD: '', CUSTNM: '', DEF_VATTYPE: '010' })

// UI용 Computed 날짜 바인딩
const uiIOYMDFR = computed({ get: () => formatDateString(searchData.IOYMDFR, '-'), set: (v) => searchData.IOYMDFR = v.replace(/-/g, '') })
const uiIOYMDTO = computed({ get: () => formatDateString(searchData.IOYMDTO, '-'), set: (v) => searchData.IOYMDTO = v.replace(/-/g, '') })
const uiPUBYMD = computed({ get: () => formatDateString(masterData.PUBYMD, '-'), set: (v) => masterData.PUBYMD = v.replace(/-/g, '') })

const sumTotal = computed(() => Number(masterData.SPLAMT) + Number(masterData.VATAMT))

// 2. 그리드 관련
const custGridElement = ref<HTMLElement | null>(null)
const itemGridElement = ref<HTMLElement | null>(null)
let custGrid: Tabulator | null = null
let itemGrid: Tabulator | null = null

const activeItemCount = ref(0)
const allSelected = ref(true)
const taxUnitOptions = ref<any[]>([])
const vatTypeOptions = ref<any[]>([])

const initGrids = () => {
  // 좌측 거래처 목록 그리드
  if (custGridElement.value) {
    custGrid = new Tabulator(custGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
      columns: [{ title: "거래처 상호", field: "CUSTNM", headerSort: false }],
    })
    custGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      selectedCust.CUSTCD = data.CUSTCD
      selectedCust.CUSTNM = data.CUSTNM
      selectedCust.DEF_VATTYPE = data.DEF_VATTYPE || '010'
      masterData.VATTYPE = selectedCust.DEF_VATTYPE
      fetchSettlementItems()
    })
  }

  // 우측 정산 품목 그리드
  if (itemGridElement.value) {
    itemGrid = new Tabulator(itemGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "거래처를 선택하세요.",
      columnDefaults: { headerSort: false },
      columns: [
        { title: "선택", field: "PROCYN", width: 40, hozAlign: "center", formatter: "tickCross", editor: true, cellEdited: () => updateTotals() },
        { title: "출고일", field: "SALSYMD", width: 100, hozAlign: "center", formatter: (c) => formatDateString(c.getValue(), '.') },
        { title: "품목명", field: "ITEMNM", minWidth: 200 },
        { title: "규격", field: "ITSIZE", width: 100 },
        { title: "단위", field: "UNIT", width: 50, hozAlign: "center" },
        { title: "수량", field: "IOQTY", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 2 } },
        { title: "정산수량", field: "JIOQTY", width: 80, hozAlign: "right", editor: "number", cellEdited: (c) => calcRow(c.getRow()) },
        { title: "단가", field: "PRICE", width: 100, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (c) => calcRow(c.getRow()) },
        { title: "공급가", field: "IOAMT", width: 110, hozAlign: "right", editor: "number", formatter: "money", cellEdited: (c) => calcRow(c.getRow(), 'AMT') },
        { title: "부가세", field: "IOVAT", width: 100, hozAlign: "right", editor: "number", formatter: "money", cellEdited: () => updateTotals() }
      ]
    })
  }
}

// 3. 로직 및 계산
const calcRow = (row: any, type: string = 'QTY') => {
  const d = row.getData()
  const prcGbn = masterData.PRICEGBN
  let amt = Number(d.IOAMT) || 0
  let vat = Number(d.IOVAT) || 0

  if (type === 'QTY') {
    if (prcGbn === '1') {
      amt = Math.round(Number(d.JIOQTY) * Number(d.PRICE))
      vat = Math.round(amt * 0.1)
    } else {
      amt = Math.round(Number(d.JIOQTY) * Number(d.PRICE) * (10 / 11))
      vat = Math.round(Number(d.JIOQTY) * Number(d.PRICE) * (1 / 11))
    }
  } else if (type === 'AMT') {
    vat = Math.floor(amt / 100) * 10
    if (prcGbn === '1') {
      row.update({ PRICE: Math.floor(amt / (Number(d.JIOQTY) || 1)) })
    } else {
      row.update({ PRICE: Math.floor((amt + vat) / (Number(d.JIOQTY) || 1)) })
    }
  }
  row.update({ IOAMT: amt, IOVAT: vat })
  updateTotals()
}

const updateTotals = () => {
  if (!itemGrid) return
  const data = itemGrid.getData().filter((i: any) => i.PROCYN)
  activeItemCount.value = data.length
  masterData.SPLAMT = data.reduce((acc, cur) => acc + (Number(cur.IOAMT) || 0), 0)
  masterData.VATAMT = data.reduce((acc, cur) => acc + (Number(cur.IOVAT) || 0), 0)
}

const toggleAllSelection = () => {
  if (!itemGrid) return
  const data = itemGrid.getData()
  itemGrid.updateData(data.map(i => ({ ...i, PROCYN: allSelected.value })))
  updateTotals()
}

// 4. 데이터 페칭
async function fetchCustomerList() {
  try {
    const res = await api.post('/api/hsio/HSIO_510U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD,
      IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO,
      DEPTCD: searchData.DEPTCD, CUSTCD: searchData.CUSTCD
    })
    custGrid?.setData(res.data)
    itemGrid?.clearData()
    selectedCust.CUSTCD = ''
  } catch (e) { vAlertError('거래처 목록 조회 실패') }
}

async function fetchSettlementItems() {
  try {
    const res = await api.post('/api/hsio/HSIO_510U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO,
      DEPTCD: searchData.DEPTCD, CUSTCD: selectedCust.CUSTCD
    })
    itemGrid?.setData(res.data.map((i: any) => {
      // ASP 로직: 수량, 금액, 부가세 초기 계산
      const ioQty = (Number(i.JSANQTY) || 0) - (Number(i.JQTY) || 0)
      const ioAmt = (Number(i.JSANAMT) || 0) - (Number(i.JAMT) || 0)
      const ioVat = (Number(i.JSANVAT) || 0) - (Number(i.JVAT) || 0)
      let price = 0
      if (ioQty !== 0) {
        price = masterData.PRICEGBN === '1' ? ioAmt / ioQty : (ioAmt + ioVat) / ioQty
      }
      return { ...i, PROCYN: true, IOQTY: ioQty, JIOQTY: ioQty, PRICE: price, IOAMT: ioAmt, IOVAT: ioVat }
    }))
    allSelected.value = true
    updateTotals()
  } catch (e) { vAlertError('정산 품목 조회 실패') }
}

async function saveSettlement() {
  if (!masterData.DEPTCD || !masterData.PUBYMD) return vAlertError('부서 및 발행일은 필수입니다.')
  if (masterData.PUBYMD <= masterData.CLSYMD) return vAlertError('회계 마감된 일자입니다.')

  const details = itemGrid?.getData().filter((i: any) => i.PROCYN)
  if (!details || details.length === 0) return vAlertError('정산할 품목을 선택하세요.')

  try {
    // 1. 마스터 저장 (A0)
    const mRes = await api.post('/api/hsio/HSIO_510U_STR', {
      ...masterData, ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, CUSTCD: selectedCust.CUSTCD,
      IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO, JSANYMD: masterData.PUBYMD
    })

    if (mRes.data?.length) {
      const { JSANYM, JSANNO } = mRes.data[0]
      // 2. 상세 저장 (U0) - 루프
      for (const item of details) {
        await api.post('/api/hsio/HSIO_510U_STR', {
          ...item, ACTKIND: 'U0', CMPYCD: authStore.CMPYCD, CUSTCD: selectedCust.CUSTCD,
          DEPTCD: masterData.DEPTCD, JSANYM, JSANNO, JSANYMD: masterData.PUBYMD,
          TAXUNIT: masterData.TAXUNIT, VATTYPE: masterData.VATTYPE
        })
      }
      vAlert('정산 작업이 완료되었습니다.')
      fetchCustomerList()
    }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

// 5. 기초 정보 및 팝업
function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, IOYMDFR: initFRYMD, IOYMDTO: initYMD })
  Object.assign(masterData, { DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, TAXUNIT: '100', VATTYPE: '010', PUBYMD: initYMD, SPLAMT: 0, VATAMT: 0 })
  custGrid?.clearData(); itemGrid?.clearData(); selectedCust.CUSTCD = ''
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
      else if (type === 'MASTER_DEPT') { masterData.DEPTCD = data.DEPTCD; masterData.DEPTNM = data.DEPTNM }
    }
  })
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) { masterData.CLSYMD = String(Object.values(r.data[0])[0]).trim() }
  })
  api.get('/api/ha00/HA00_010S_STR', { params: { GUBUN: 'P1', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) masterData.PRICEGBN = r.data[0].PRICEGBN || '1'
  })
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'SA', CMPYCD: authStore.CMPYCD } }).then(r => taxUnitOptions.value = r.data.map((i: any) => ({ CODECD: i.CODE, CODENM: i.CDNM })))
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'E0', GBNCD: '130', CMPYCD: authStore.CMPYCD } }).then(r => vatTypeOptions.value = r.data.map((i: any) => ({ CODECD: i.CODE, CODENM: i.CDNM })))

  nextTick(() => initGrids())
})
</script>

<style scoped>
.hsio510u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.bg-light-primary { background-color: #f0f7ff !important; color: #005a9f !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>