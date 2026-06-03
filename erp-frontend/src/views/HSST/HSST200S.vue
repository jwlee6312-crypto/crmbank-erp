<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
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
                  <input v-model="uiymD" type="date" class="form-control form-control-sm fw-bold" style="width: 150px;" />
                </td>
                <th class="required" style="width: 100px;">창&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td style="width: 180px;">
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
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 창고별 품목 재고 현황</span>
          <div class="small text-muted">품목명을 클릭하면 상세 수불 이력(HSIO650S)으로 이동합니다.</div>
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

// 1. 상태 관리
const searchData = reactive({
  ymD: initymd,
  whcd: '000',
  astkind: '120'
})

const uiymD = computed({
  get: () => formatDateString(searchData.ymD, '-'),
  set: (v) => searchData.ymD = v.replace(/-/g, '')
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
        title: "품목", field: "itemnm", minWidth: 250, cssClass: "fw-bold",
        formatter: (cell) => {
            const d = cell.getData();
            return `<span class="text-primary cursor-pointer text-decoration-underline">${d.itemcd} ${d.itemnm}</span>`;
        },
        cellClick: (e, cell) => navigateToHistory(cell.getData())
      },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 60, hozAlign: "center" },
      { title: "적정재고", field: "stock", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      {
        title: "현재고", field: "stkqty", width: 90, hozAlign: "right",
        formatter: (cell) => {
            const val = Number(cell.getValue()) || 0;
            const data = cell.getData();
            const formatted = formatQty(cell);
            // 적정재고보다 현재고가 적거나, 마이너스 재고인 경우 강조
            if (val < 0 || (Number(data.stock) > 0 && val < Number(data.stock))) {
                return `<span class="text-danger fw-bold">${formatted}</span>`;
            }
            return formatted;
        }
      },
      {
        title: "과부족", field: "DIFF", width: 90, hozAlign: "right",
        formatter: (cell) => {
            const d = cell.getData();
            if (Number(d.stock) === 0) return "";
            const diff = (Number(d.stkqty) || 0) - (Number(d.stock) || 0);
            return formatNumberWithPnt(diff, Number(d.QTYPNT) || 0);
        }
      },
      { title: "재고금액", field: "STKAMT", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미입고량", field: "nonqty", width: 90, hozAlign: "right", formatter: (c) => formatQty(c) },
      {
        title: "총 수량", field: "TOTALQTY", width: 100, hozAlign: "right",
        formatter: (cell) => {
            const d = cell.getData();
            const total = (Number(d.stkqty) || 0) + (Number(d.nonqty) || 0);
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
    const resWh = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
    whOptions.value = resWh.data.map((i: any) => ({ codecd: Object.values(i)[0], codenm: Object.values(i)[1] }))

    const resAsset = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '140' } })
    assetOptions.value = resAsset.data.map((i: any) => ({ codecd: Object.values(i)[0], codenm: Object.values(i)[1] }))
  } catch (e) { console.error('옵션 로드 실패') }
}

async function search() {
  if (!searchData.ymD) return vAlertError('기준일자를 선택하세요.')
  try {
    const res = await api.post('/api/hsst/HSST_200S_STR', {
      cmpycd: authStore.cmpycd,
      ymD: searchData.ymD,
      whcd: searchData.whcd,
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
    totals.AMTTOT = data.reduce((acc, cur) => acc + (Number(cur.STKAMT) || 0), 0)
    totals.QTYTOT = data.reduce((acc, cur) => acc + (Number(cur.stkqty) || 0), 0)
    totals.NONTOT = data.reduce((acc, cur) => acc + (Number(cur.nonqty) || 0), 0)
}

const navigateToHistory = (row: any) => {
    // 수불 상세 내역(HSIO650S)으로 이동
    const firstDayOfMonth = searchData.ymD.substring(0, 6) + '01'
    router.push({
        path: '/HSIO650S',
        query: {
            astkind: searchData.astkind,
            whcd: searchData.whcd,
            itemcd: row.itemcd,
            fymd: firstDayOfMonth,
            tymd: searchData.ymD
        }
    })
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { ymD: initymd, whcd: '000', astkind: '120' })
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
