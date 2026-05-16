<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio620s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-text-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">거래명세표 (HSIO620S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="searchMaster">조회</button>
        <button class="btn-erp btn-outline-secondary" @click="printSlip('Print')">거래명세표 인쇄</button>
        <button class="btn-erp btn-outline-secondary" @click="printSlip('Print_Req')">출고의뢰서 인쇄</button>
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
                <th style="width: 100px;">출고창고</th>
                <td style="width: 200px;">
                  <select v-model="searchData.WHCD" class="form-select form-select-sm">
                    <option value="000">전체</option>
                    <option v-for="opt in whOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th style="width: 100px;">출고일자</th>
                <td style="width: 300px;">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="uiOUTYMDFR" type="date" class="form-control form-control-sm" />
                    <span>~</span>
                    <input v-model="uiOUTYMDTO" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th style="width: 100px;">확정여부</th>
                <td style="width: 150px;">
                  <select v-model="searchData.SLIPYN" class="form-select form-select-sm">
                    <option value="Y">확정</option>
                  </select>
                </td>
                <th style="width: 100px;">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="searchData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 메인 작업 영역 (좌: 마스터 목록, 우: 상세 내역) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 출고 마스터 목록 -->
        <div class="card border shadow-sm d-flex flex-column" style="width: 350px;">
          <div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-list-ul me-1"></i> 출고 목록
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="masterGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <!-- 우측: 품목 상세 내역 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
              <span class="fw-bold small text-dark">
                <i class="bi bi-grid-3x3-gap-fill me-1"></i> 상세 품목 내역
                <span v-if="selectedMasterInfo" class="ms-2 text-primary">[{{ selectedMasterInfo.CUSTNM }}] {{ selectedMasterInfo.IOYM }}-{{ selectedMasterInfo.IONO }}</span>
              </span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white">
              <div ref="detailGridElement" style="height: 100%;"></div>
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
          <span class="me-4 small opacity-75">공급가액: <span class="fw-bold text-info ms-1">{{ formatNumber(summary.amtsum) }}</span></span>
          <span class="me-4 small opacity-75">부가세: <span class="fw-bold text-warning ms-1">{{ formatNumber(summary.vatsum) }}</span></span>
          <span class="fs-5 ms-2 fw-light">총 합계: <span class="fw-bold text-white ms-2">{{ formatNumber(summary.amtsum + summary.vatsum) }}</span></span>
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

const now = new Date();
const initYMD = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;

// 1. 상태 관리
const searchData = reactive({
  WHCD: '000',
  OUTYMDFR: initYMD.replace(/-/g, ''),
  OUTYMDTO: initYMD.replace(/-/g, ''),
  SLIPYN: 'Y',
  CUSTCD: '',
  CUSTNM: ''
})

const uiOUTYMDFR = computed({ get: () => formatDateString(searchData.OUTYMDFR, '-'), set: (v) => searchData.OUTYMDFR = v.replace(/-/g, '') })
const uiOUTYMDTO = computed({ get: () => formatDateString(searchData.OUTYMDTO, '-'), set: (v) => searchData.OUTYMDTO = v.replace(/-/g, '') })

const summary = reactive({ amtsum: 0, vatsum: 0 })
const selectedMasterInfo = ref<any>(null)
const activeItemCount = ref(0)
const whOptions = ref<any[]>([])

const masterGridElement = ref<HTMLElement | null>(null)
const detailGridElement = ref<HTMLElement | null>(null)
let masterGrid: Tabulator | null = null
let detailGrid: Tabulator | null = null

// 2. 그리드 초기화
const initGrids = () => {
  if (masterGridElement.value) {
    masterGrid = new Tabulator(masterGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "데이터 없음",
      columns: [
        { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
        { title: "거래처", field: "CUSTNM", minWidth: 150, headerSort: false, cssClass: "fw-bold text-primary cursor-pointer" },
        {
          title: "출고번호", field: "IO_FULL", width: 120, hozAlign: "center", headerSort: false,
          formatter: (cell) => {
              const d = cell.getData();
              return `<span class="text-decoration-underline">${d.IOYM}-${d.IONO}</span>`;
          }
        }
      ]
    })
    masterGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      selectedMasterInfo.value = data
      fetchDetails(data)
    })
  }

  if (detailGridElement.value) {
    detailGrid = new Tabulator(detailGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "항목을 선택하세요.",
      columnDefaults: { headerSort: false },
      columns: [
        { title: "품목명", field: "ITEMNM", minWidth: 200, cssClass: "fw-bold" },
        { title: "규격", field: "ITSIZE", width: 120 },
        { title: "단위", field: "UNIT", width: 60, hozAlign: "center" },
        { title: "수량", field: "IOQTY", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 2 } },
        { title: "금액", field: "JSANAMT", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "부가세", field: "JSANVAT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        {
          title: "합계", field: "TOTAL", width: 120, hozAlign: "right",
          formatter: (cell) => {
              const d = cell.getData();
              return formatNumber(Number(d.JSANAMT || 0) + Number(d.JSANVAT || 0));
          },
          cssClass: "fw-bold"
        }
      ]
    })
  }
}

// 3. 기능 구현
async function fetchWhOptions() {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })
    whOptions.value = res.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
  } catch (e) { console.error('창고 옵션 로드 실패') }
}

async function searchMaster() {
  try {
    const res = await api.post('/api/hsio/HSIO_620S_STR', {
      ACTKIND: 'S1',
      CMPYCD: authStore.CMPYCD,
      GUBUN: '200',
      WHCD: searchData.WHCD,
      OUTYMDFR: searchData.OUTYMDFR,
      OUTYMDTO: searchData.OUTYMDTO,
      CUSTCD: searchData.CUSTCD,
      SLIPYN: searchData.SLIPYN
    })
    if (masterGrid) {
      masterGrid.setData(res.data)
      detailGrid?.clearData()
      selectedMasterInfo.value = null
      summary.amtsum = 0
      summary.vatsum = 0
      activeItemCount.value = 0
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetails(row: any) {
  try {
    const res = await api.post('/api/hsio/HSIO_620S_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      GUBUN: '200',
      WHCD: searchData.WHCD,
      OUTYMDFR: searchData.OUTYMDFR,
      OUTYMDTO: searchData.OUTYMDTO,
      CUSTCD: row.CUSTCD,
      IOYM: row.IOYM,
      IONO: row.IONO
    })
    if (detailGrid) {
      detailGrid.setData(res.data)
      summary.amtsum = res.data.reduce((acc: number, cur: any) => acc + (Number(cur.JSANAMT) || 0), 0)
      summary.vatsum = res.data.reduce((acc: number, cur: any) => acc + (Number(cur.JSANVAT) || 0), 0)
      activeItemCount.value = res.data.length
    }
  } catch (e) { vAlertError('상세 로드 실패') }
}

function printSlip(type: string) {
  if (!selectedMasterInfo.value) return vAlertError('출고 내역을 먼저 선택하세요.')

  const m = selectedMasterInfo.value
  let url = ''
  if (type === 'Print') {
      const useVat = confirm('부가세 구분없이 합계금액으로 인쇄하시겠습니까?') ? 'Y' : 'N'
      url = `/api/report/HSIO_TRANS_PRINT?PRTGU=Print&IOYM=${m.IOYM}&IONO=${m.IONO}&QTYYN=${useVat}`
  } else {
      url = `/api/report/HSIO_REQOUT_PRINT?PRTGU=Print&IOYM=${m.IOYM}&IONO=${m.IONO}`
  }
  window.open(url, 'print', 'width=700,height=600,scrollbars=yes')
}

function initialize() {
  resetForm(searchData)
  searchData.WHCD = '000'
  searchData.SLIPYN = 'Y'
  searchData.OUTYMDFR = initYMD.replace(/-/g, '')
  searchData.OUTYMDTO = initYMD.replace(/-/g, '')
  masterGrid?.clearData()
  detailGrid?.clearData()
  selectedMasterInfo.value = null
  summary.amtsum = 0
  summary.vatsum = 0
  activeItemCount.value = 0
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'CUST') {
    Object.assign(modalProps, {
      title: '거래처 선택',
      path: '/api/ha00/HA00_00P_STR',
      defaultField: 'CUSTNM',
      large: true,
      data: { GUBUN: 'C4', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
      columns: [
        { title: '코드', field: 'CUSTCD', width: 80 },
        { title: '거래처명', field: 'CUSTNM', width: 200 },
        { title: '사업자번호', field: 'CUSTNO', width: 120 },
        { title: '주소', field: 'ADDRESS', minWidth: 300 }
      ],
      onConfirm: (data: any) => {
        searchData.CUSTCD = data.CUSTCD
        searchData.CUSTNM = data.CUSTNM
      }
    })
    modalVisible.value = true
  }
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchWhOptions()
  nextTick(() => {
    initGrids()
    searchMaster()
  })
})
</script>

<style scoped>
.hsio620s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

.bg-light-yellow { background-color: #fff9e6 !important; }
</style>
