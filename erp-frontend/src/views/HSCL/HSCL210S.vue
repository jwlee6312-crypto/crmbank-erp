<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hscl210s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-journal-text me-2 text-primary" style="font-size: 18px;"></i>
        마감관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">창고수불부 (HSCL210S)</span>
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
                <th class="required" style="width: 100px;">조회연월</th>
                <td style="width: 200px;">
                  <div class="d-flex align-items-center gap-2">
                    <select v-model="searchData.yy" class="form-select form-select-sm" style="width: 100px;">
                      <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
                    </select>
                    <select v-model="searchData.mm" class="form-select form-select-sm" style="width: 80px;">
                      <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
                    </select>
                  </div>
                </td>
                <th class="required" style="width: 100px;">출고창고</th>
                <td style="width: 200px;">
                  <select v-model="searchData.whcd" class="form-select form-select-sm">
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
        <div class="card-header bg-white py-1 px-3 border-bottom">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 품목별 창고 수불 현황</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridElement" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const currentYear = now.getFullYear()

// 1. 상태 관리
const searchData = reactive({
  yy: String(currentYear),
  mm: String(now.getMonth() + 1).padStart(2, '0'),
  whcd: '',
  astkind: '120'
})

const totals = reactive({ bsamt: 0, inamt: 0, outamt: 0, outtamt: 0, stkamt: 0 })

const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))
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
      { title: "품목명", field: "itemnm", minWidth: 150, frozen: true, cssClass: "fw-bold border-end" },
      { title: "규격", field: "itsize", width: 100, frozen: true, cssClass: "border-end" },
      // 이월 그룹
      {
        title: "이 월",
        columns: [
          { title: "수량", field: "bsqty", width: 70, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "bsprice", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "bsamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        ]
      },
      // 입고 그룹
      {
        title: "입 고",
        columns: [
          { title: "수량", field: "inqty", width: 70, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "inprice", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "inamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        ]
      },
      // 출고 그룹
      {
        title: "출 고",
        columns: [
          { title: "수량", field: "outqty", width: 70, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "outprice", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "outamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        ]
      },
      // 타계정 그룹
      {
        title: "타계정",
        columns: [
          { title: "수량", field: "outtqty", width: 70, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "outtprice", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "outtamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        ]
      },
      // 재고 그룹
      {
        title: "재 고",
        columns: [
          { title: "수량", field: "stkqty", width: 70, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "stkprice", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "stkamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-light-yellow fw-bold" },
        ]
      }
    ]
  })
}

const formatQty = (cell: any) => {
  const data = cell.getData();
  const pnt = Number(data.qtypnt) || 0;
  const val = Number(cell.getValue()) || 0;
  return new Intl.NumberFormat(undefined, { minimumFractionDigits: pnt, maximumFractionDigits: pnt }).format(val);
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    const resWh = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
    whOptions.value = resWh.data.map((i: any) => ({ codecd: Object.values(i)[0], codenm: Object.values(i)[1] }))
    if (whOptions.value.length > 0) searchData.whcd = whOptions.value[0].codecd

    const resAsset = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '140' } })
    assetOptions.value = resAsset.data.map((i: any) => ({ codecd: Object.values(i)[0], codenm: Object.values(i)[1] }))
  } catch (e) { console.error('옵션 로드 실패') }
}

async function search() {
  if (!searchData.whcd) return vAlertError('창고를 선택하세요.')
  try {
    const res = await api.post('/api/hscl/HSCL_210S_STR', {
      cmpycd: authStore.cmpycd,
      whcd: searchData.whcd,
      yymm: `${searchData.yy}${searchData.mm}`,
      astkind: searchData.astkind
    })
    if (grid.value) {
      const mappedData = res.data.map((i: any) => ({
        ...i,
        bsprice: i.bsqty !== 0 ? (Number(i.bsamt) / Number(i.bsqty)) : 0,
        inprice: i.inqty !== 0 ? (Number(i.inamt) / Number(i.inqty)) : 0,
        outprice: i.outqty !== 0 ? (Number(i.outamt) / Number(i.outqty)) : 0,
        outtprice: i.outtqty !== 0 ? (Number(i.outtamt) / Number(i.outtqty)) : 0,
        stkprice: i.stkqty !== 0 ? (Number(i.stkamt) / Number(i.stkqty)) : 0,
      }))
      grid.value.setData(mappedData)
      activeItemCount.value = mappedData.length

      // 집계 계산
      totals.bsamt = mappedData.reduce((acc: number, cur: any) => acc + (Number(cur.bsamt) || 0), 0)
      totals.inamt = mappedData.reduce((acc: number, cur: any) => acc + (Number(cur.inamt) || 0), 0)
      totals.outamt = mappedData.reduce((acc: number, cur: any) => acc + (Number(cur.outamt) || 0), 0)
      totals.outtamt = mappedData.reduce((acc: number, cur: any) => acc + (Number(cur.outtamt) || 0), 0)
      totals.stkamt = mappedData.reduce((acc: number, cur: any) => acc + (Number(cur.stkamt) || 0), 0)
    }
  } catch (e) { vAlertError('조회 실패') }
}

function initialize() {
  resetForm(searchData)
  searchData.yy = String(currentYear)
  searchData.mm = String(now.getMonth() + 1).padStart(2, '0')
  searchData.astkind = '120'
  if (whOptions.value.length > 0) searchData.whcd = whOptions.value[0].codecd
  grid.value?.clearData()
  activeItemCount.value = 0
  Object.keys(totals).forEach(key => (totals as any)[key] = 0)
}

function print(type: string) {
  const url = `/api/report/HSCL_210P?astkind=${searchData.astkind}&whcd=${searchData.whcd}&yy=${searchData.yy}.mm=${searchData.mm}&PRTGU=${type}`
  window.open(url, 'print', 'width=800,height=700,scrollbars=yes')
}

onMounted(async () => {
  await fetchOptions()
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hscl210s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* Tabulator 다단 헤더 스타일 조정 */
:deep(.tabulator-col-group) { border-right: 1px solid #dee2e6 !important; border-bottom: 1px solid #dee2e6 !important; }
:deep(.tabulator-col) { border-right: 1px solid #dee2e6 !important; }
:deep(.bg-light-yellow) { background-color: #fff9e6 !important; }
</style>
