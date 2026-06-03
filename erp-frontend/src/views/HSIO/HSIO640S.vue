<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
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
                    <input v-model="uifymd" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uitymd" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th class="required" style="width: 100px;">창&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td style="width: 200px;">
                  <select v-model="searchData.whcd" class="form-select form-select-sm">
                    <option value="000">전체</option>
                    <option v-for="opt in whOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="required" style="width: 100px;">재고자산</th>
                <td>
                  <select v-model="searchData.astkind" class="form-select form-select-sm" style="width: 150px;">
                    <option v-for="opt in assetOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
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
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridElement" class="tabulator-instance flex-grow-1"></div>
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
const initymd = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initfrymd = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  fymd: initfrymd,
  tymd: initymd,
  whcd: '000',
  astkind: '120'
})

const uifymd = computed({ get: () => formatDateString(searchData.fymd, '-'), set: (v) => searchData.fymd = v.replace(/-/g, '') })
const uitymd = computed({ get: () => formatDateString(searchData.tymd, '-'), set: (v) => searchData.tymd = v.replace(/-/g, '') })

const totals = reactive({ bqty: 0, iqty: 0, ieqty: 0, oqty: 0, oeqty: 0, tqty: 0, sqty: 0 })
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
      { title: "품목코드", field: "itemcd", width: 100, hozAlign: "center", cssClass: "fw-bold" },
      {
        title: "품목명", field: "itemnm", minWidth: 200,
        formatter: (cell) => `<span class="text-primary cursor-pointer text-decoration-underline">${cell.getValue()}</span>`,
        cellClick: (e, cell) => navigateToHistory(cell.getData())
      },
      { title: "전월이월", field: "bqty", width: 100, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "입고", field: "iqty", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "이관입고", field: "ieqty", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "출고", field: "oqty", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "이관출고", field: "oeqty", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "타계정", field: "tqty", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      {
        title: "현재고", field: "sqty", width: 110, hozAlign: "right",
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
    const resWh = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
    whOptions.value = resWh.data.map((i: any) => ({ codecd: Object.values(i)[0], codenm: Object.values(i)[1] }))

    const resAsset = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '140' } })
    assetOptions.value = resAsset.data.map((i: any) => ({ codecd: Object.values(i)[0], codenm: Object.values(i)[1] }))
  } catch (e) { console.error('옵션 로드 실패') }
}

async function search() {
  if (!searchData.fymd || !searchData.tymd) return vAlertError('조회 기간을 입력하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_640S_STR', {
      cmpycd: authStore.cmpycd,
      whcd: searchData.whcd,
      fymd: searchData.fymd,
      tymd: searchData.tymd,
      astkind: searchData.astkind
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
        totals.bqty += Number(item.bqty) || 0
        totals.iqty += Number(item.iqty) || 0
        totals.ieqty += Number(item.ieqty) || 0
        totals.oqty += Number(item.oqty) || 0
        totals.oeqty += Number(item.oeqty) || 0
        totals.tqty += Number(item.tqty) || 0
        totals.sqty += Number(item.sqty) || 0
    })
}

const navigateToHistory = (row: any) => {
    // Navigate to HSIO650S with parameters
    router.push({
        path: '/HSIO650S',
        query: {
            astkind: searchData.astkind,
            whcd: searchData.whcd,
            itemcd: row.itemcd,
            fymd: searchData.fymd,
            tymd: searchData.tymd
        }
    })
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { fymd: initfrymd, tymd: initymd, whcd: '000', astkind: '120' })
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
