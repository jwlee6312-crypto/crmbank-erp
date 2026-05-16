<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsst200s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam-fill me-2 text-primary" style="font-size: 18px;"></i>
        재고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">창고재고현황 (HSST200S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-outline-secondary" @click="print('Print')">인쇄</button>
        <button class="btn-erp btn-outline-success" @click="print('Excel')">엑셀</button>
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
                <th class="required" style="width: 100px;">기준일자</th>
                <td style="width: 200px;">
                  <input v-model="uiYMD" type="date" class="form-control form-control-sm fw-bold" style="width: 150px;" />
                </td>
                <th class="required" style="width: 100px;">창&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td style="width: 180px;">
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
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 창고별 품목 재고 현황</span>
          <div class="small text-muted">품목명을 클릭하면 상세 수불 이력(HSIO650S)으로 이동합니다.</div>
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
          <span class="me-4 small opacity-75">미입고 합계: <span class="fw-bold text-warning ms-1">{{ formatNumber(totals.NONTOT) }}</span></span>
          <span class="me-4 small opacity-75">재고수량 합계: <span class="fw-bold text-info ms-1">{{ formatNumber(totals.QTYTOT) }}</span></span>
          <span class="fs-5 ms-2 fw-light">총 재고금액: <span class="fw-bold text-white ms-2">{{ formatNumber(totals.AMTTOT) }}</span></span>
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

// 1. 상태 관리
const searchData = reactive({
  YMD: initYMD,
  WHCD: '000',
  ASTKIND: '120'
})

const uiYMD = computed({
  get: () => formatDateString(searchData.YMD, '-'),
  set: (v) => searchData.YMD = v.replace(/-/g, '')
})

const totals = reactive({ AMTTOT: 0, QTYTOT: 0, NONTOT: 0 })
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
      {
        title: "품목", field: "ITEMNM", minWidth: 250, cssClass: "fw-bold",
        formatter: (cell) => {
            const d = cell.getData();
            return `<span class="text-primary cursor-pointer text-decoration-underline">${d.ITEMCD} ${d.ITEMNM}</span>`;
        },
        cellClick: (e, cell) => navigateToHistory(cell.getData())
      },
      { title: "규격", field: "ITSIZE", width: 150 },
      { title: "단위", field: "UNIT", width: 60, hozAlign: "center" },
      { title: "적정재고", field: "STOCK", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      {
        title: "현재고", field: "STKQTY", width: 90, hozAlign: "right",
        formatter: (cell) => {
            const val = Number(cell.getValue()) || 0;
            const data = cell.getData();
            const formatted = formatQty(cell);
            // 적정재고보다 현재고가 적거나, 마이너스 재고인 경우 강조
            if (val < 0 || (Number(data.STOCK) > 0 && val < Number(data.STOCK))) {
                return `<span class="text-danger fw-bold">${formatted}</span>`;
            }
            return formatted;
        }
      },
      {
        title: "과부족", field: "DIFF", width: 90, hozAlign: "right",
        formatter: (cell) => {
            const d = cell.getData();
            if (Number(d.STOCK) === 0) return "";
            const diff = (Number(d.STKQTY) || 0) - (Number(d.STOCK) || 0);
            return formatNumberWithPnt(diff, Number(d.QTYPNT) || 0);
        }
      },
      { title: "재고금액", field: "STKAMT", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미입고량", field: "NONQTY", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      {
        title: "총 수량", field: "TOTALQTY", width: 100, hozAlign: "right",
        formatter: (cell) => {
            const d = cell.getData();
            const total = (Number(d.STKQTY) || 0) + (Number(d.NONQTY) || 0);
            return formatNumberWithPnt(total, Number(d.QTYPNT) || 0);
        },
        cssClass: "bg-light-blue fw-bold"
      }
    ]
  })
}

const formatQty = (cell: any) => {
    const data = cell.getData();
    const pnt = Number(data.QTYPNT) || 0;
    const val = Number(cell.getValue()) || 0;
    if (val === 0) return "";
    return formatNumberWithPnt(val, pnt);
}

const formatNumberWithPnt = (val: number, pnt: number) => {
    return new Intl.NumberFormat(undefined, { minimumFractionDigits: pnt, maximumFractionDigits: pnt }).format(val);
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
  if (!searchData.YMD) return vAlertError('기준일자를 선택하세요.')
  try {
    const res = await api.post('/api/hsst/HSST_200S_STR', {
      CMPYCD: authStore.CMPYCD,
      YMD: searchData.YMD,
      WHCD: searchData.WHCD,
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
    totals.AMTTOT = data.reduce((acc, cur) => acc + (Number(cur.STKAMT) || 0), 0)
    totals.QTYTOT = data.reduce((acc, cur) => acc + (Number(cur.STKQTY) || 0), 0)
    totals.NONTOT = data.reduce((acc, cur) => acc + (Number(cur.NONQTY) || 0), 0)
}

const navigateToHistory = (row: any) => {
    // 수불 상세 내역(HSIO650S)으로 이동
    const firstDayOfMonth = searchData.YMD.substring(0, 6) + '01'
    router.push({
        path: '/HSIO650S',
        query: {
            ASTKIND: searchData.ASTKIND,
            WHCD: searchData.WHCD,
            ITEMCD: row.ITEMCD,
            FYMD: firstDayOfMonth,
            TYMD: searchData.YMD
        }
    })
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { YMD: initYMD, WHCD: '000', ASTKIND: '120' })
  grid.value?.clearData()
  activeItemCount.value = 0
  totals.AMTTOT = 0; totals.QTYTOT = 0; totals.NONTOT = 0;
}

function print(type: string) {
    vAlert(`${type} 기능은 준비 중입니다.`)
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsst200s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

.bg-light-blue { background-color: #e7f3ff !important; }
</style>