<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio640s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        재고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">입출고현황 (HSIO640S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-outline-secondary" @click="print('Print')">인쇄</button>
        <button class="btn-erp btn-outline-success" @click="print('Excel')">엑셀</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-search me-1"></i> 조회 조건</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required" style="width: 100px;">입출일자</th>
                <td style="width: 320px;">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="uiFYMD" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiTYMD" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th class="required" style="width: 100px;">창&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td style="width: 200px;">
                  <select v-model="searchData.WHCD" class="form-select form-select-sm">
                    <option value="000">전체</option>
                    <option v-for="opt in whOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required" style="width: 100px;">재고자산</th>
                <td>
                  <select v-model="searchData.ASTKIND" class="form-select form-select-sm" style="width: 150px;">
                    <option v-for="opt in assetOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 입출고 현황 리스트</span>
          <div class="small text-muted">품목명을 클릭하면 품목별 이력을 확인할 수 있습니다.</div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">조회 품목: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="me-4 small opacity-75">입고합계: <span class="fw-bold text-info ms-1">{{ formatNumber(totals.IQTY) }}</span></span>
          <span class="me-4 small opacity-75">출고합계: <span class="fw-bold text-warning ms-1">{{ formatNumber(totals.OQTY) }}</span></span>
          <span class="fs-6 ms-2 fw-light">총 현재고: <span class="fw-bold text-white ms-2">{{ formatNumber(totals.SQTY) }}</span></span>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
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
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const initYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initFRYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  FYMD: initFRYMD,
  TYMD: initYMD,
  WHCD: '000',
  ASTKIND: '120'
})

const uiFYMD = computed({ get: () => formatDateString(searchData.FYMD, '-'), set: (v) => searchData.FYMD = v.replace(/-/g, '') })
const uiTYMD = computed({ get: () => formatDateString(searchData.TYMD, '-'), set: (v) => searchData.TYMD = v.replace(/-/g, '') })

const totals = reactive({ BQTY: 0, IQTY: 0, IEQTY: 0, OQTY: 0, OEQTY: 0, TQTY: 0, SQTY: 0 })
const whOptions = ref<any[]>([])
const assetOptions = ref<any[]>([])

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      { title: "품목코드", field: "ITEMCD", width: 100, hozAlign: "center", cssClass: "fw-bold" },
      {
        title: "품목명", field: "ITEMNM", minWidth: 200,
        formatter: (cell) => `<span class="text-primary cursor-pointer text-decoration-underline">${cell.getValue()}</span>`,
        cellClick: (e, cell) => navigateToHistory(cell.getData())
      },
      { title: "전월이월", field: "BQTY", width: 100, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "입고", field: "IQTY", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "이관입고", field: "IEQTY", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "출고", field: "OQTY", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "이관출고", field: "OEQTY", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "타계정", field: "TQTY", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      {
        title: "현재고", field: "SQTY", width: 110, hozAlign: "right",
        formatter: (cell) => {
          const val = Number(cell.getValue()) || 0;
          const formatted = new Intl.NumberFormat().format(val);
          return val < 0 ? `<span class="text-danger fw-bold">${formatted}</span>` : formatted;
        }
      }
    ]
  })
}

const formatQty = (cell: any) => {
    const data = cell.getData();
    const pnt = Number(data.QTYPNT) || 0;
    return new Intl.NumberFormat(undefined, { minimumFractionDigits: pnt, maximumFractionDigits: pnt }).format(Number(cell.getValue()) || 0);
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    const resWh = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })
    whOptions.value = resWh.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))

    const resAsset = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '140' } })
    assetOptions.value = resAsset.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
  } catch (e) { console.error('옵션 로드 실패') }
}

async function search() {
  if (!searchData.FYMD || !searchData.TYMD) return vAlertError('조회 기간을 입력하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_640S_STR', {
      CMPYCD: authStore.CMPYCD,
      WHCD: searchData.WHCD,
      FYMD: searchData.FYMD,
      TYMD: searchData.TYMD,
      ASTKIND: searchData.ASTKIND
    })
    if (grid.value) {
      grid.value.setData(res.data)
      activeItemCount.value = res.data.length
      calculateTotals(res.data)
    }
  } catch (e) { vAlertError('조회 실패') }
}

const calculateTotals = (data: any[]) => {
    Object.keys(totals).forEach(key => totals[key as keyof typeof totals] = 0)
    data.forEach(item => {
        totals.BQTY += Number(item.BQTY) || 0
        totals.IQTY += Number(item.IQTY) || 0
        totals.IEQTY += Number(item.IEQTY) || 0
        totals.OQTY += Number(item.OQTY) || 0
        totals.OEQTY += Number(item.OEQTY) || 0
        totals.TQTY += Number(item.TQTY) || 0
        totals.SQTY += Number(item.SQTY) || 0
    })
}

const navigateToHistory = (row: any) => {
    // Navigate to HSIO650S with parameters
    router.push({
        path: '/HSIO650S',
        query: {
            ASTKIND: searchData.ASTKIND,
            WHCD: searchData.WHCD,
            ITEMCD: row.ITEMCD,
            FYMD: searchData.FYMD,
            TYMD: searchData.TYMD
        }
    })
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { FYMD: initFRYMD, TYMD: initYMD, WHCD: '000', ASTKIND: '120' })
  grid.value?.clearData()
  activeItemCount.value = 0
  Object.keys(totals).forEach(key => totals[key as keyof typeof totals] = 0)
}

function print(type: string) {
    vAlert(`${type} 기능은 준비 중입니다.`)
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  // Logic for help popups if needed (none required for this specific ASP source)
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsio640s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

/* 🚀 입력 필드 글자 크기 및 높이 최적화 (표준) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12px; padding: 6px 10px !important; color: #495057;
  white-space: nowrap;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }

:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>

