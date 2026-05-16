<template><AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hscl200s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi- journals me-2 text-primary" style="font-size: 18px;"></i>
        마감관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">수불부 (HSCL200S)</span>
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
                <td style="width: 350px;">
                  <div class="d-flex align-items-center gap-2">
                    <select v-model="searchData.YY" class="form-select form-select-sm" style="width: 100px;">
                      <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
                    </select>
                    <select v-model="searchData.MM" class="form-select form-select-sm" style="width: 80px;">
                      <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
                    </select>
                    <span>~</span>
                    <select v-model="searchData.TOMM" class="form-select form-select-sm" style="width: 80px;">
                      <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
                    </select>
                  </div>
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
        <div class="card-header bg-white py-1 px-3 border-bottom">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 품목별 월간 수불 현황</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-12 small text-center opacity-75">
          <i class="bi bi-info-circle me-1"></i> 시스템 마감월: {{ closingInfo.SCLSYM_FMT }} | 원가 마감월: {{ closingInfo.WCLSYM_FMT }}
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
  YY: String(currentYear),
  MM: '01',
  TOMM: String(now.getMonth() + 1).padStart(2, '0'),
  ASTKIND: '120'
})

const closingInfo = reactive({
  CLSYMD: '', SCLSYM: '', WCLSYM: '',
  SCLSYM_FMT: '', WCLSYM_FMT: ''
})

const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))
const assetOptions = ref<any[]>([])

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)

// 2. 그리드 초기화 (다단 헤더 정의)
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      { title: "품목명", field: "ITEMNM", minWidth: 150, frozen: true, cssClass: "fw-bold border-end" },
      { title: "규격", field: "ITSIZE", width: 100, frozen: true, borderRight: true },
      // 이월 그룹
      {
        title: "이 월",
        columns: [
          { title: "수량", field: "BSQTY", width: 60, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "BSPRICE", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "BSAMT", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
        ]
      },
      // 입고 그룹
      {
        title: "입 고",
        columns: [
          { title: "수량", field: "INQTY", width: 60, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "INPRICE", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "INAMT", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
        ]
      },
      // 출고 그룹
      {
        title: "출 고",
        columns: [
          { title: "수량", field: "OUTQTY", width: 60, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "OUTPRICE", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "OUTAMT", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
        ]
      },
      // 타계정 그룹
      {
        title: "타계정",
        columns: [
          { title: "수량", field: "OUTTQTY", width: 60, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "OUTTPRICE", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "OUTTAMT", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
        ]
      },
      // 재고 그룹
      {
        title: "재 고",
        columns: [
          { title: "수량", field: "STKQTY", width: 60, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "STKPRICE", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "STKAMT", width: 95, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-light-yellow fw-bold", bottomCalc: "sum", bottomCalcFormatter: "money" },
        ]
      }
    ]
  })
}

const formatQty = (cell: any) => {
  const data = cell.getData();
  const pnt = Number(data.QTYPNT) || 0;
  const val = Number(cell.getValue()) || 0;
  return new Intl.NumberFormat(undefined, { minimumFractionDigits: pnt, maximumFractionDigits: pnt }).format(val);
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '140' } })
    assetOptions.value = res.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))

    const resCl = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } })
    if (resCl.data?.length) {
      const data = resCl.data[0]
      closingInfo.CLSYMD = data.CLSYMD;
      closingInfo.SCLSYM = data.SCLSYM;
      closingInfo.WCLSYM = data.WCLSYM;
      closingInfo.SCLSYM_FMT = formatDateString(data.SCLSYM, 'YM');
      closingInfo.WCLSYM_FMT = formatDateString(data.WCLSYM, 'YM');

      searchData.YY = data.SCLSYM.substring(0, 4);
      searchData.MM = data.SCLSYM.substring(4, 6);
      searchData.TOMM = data.SCLSYM.substring(4, 6);
    }
  } catch (e) { console.error('기초 정보 로드 실패') }
}

async function search() {
  // ASP 유효성 체크 로직 이식
  if (searchData.MM > searchData.TOMM) return vAlertError('조회년월을 확인하십시오. (시작월 > 종료월)')
  if (`${searchData.YY}${searchData.TOMM}` > closingInfo.SCLSYM) return vAlertError('조회년월을 확인하십시오. (종료월 > 시스템 마감월)')

  try {
    const res = await api.post('/api/hscl/HSCL_200S_STR', {
      CMPYCD: authStore.CMPYCD,
      YYMM_FR: `${searchData.YY}${searchData.MM}`,
      YYMM_TO: `${searchData.YY}${searchData.TOMM}`,
      ASTKIND: searchData.ASTKIND
    })
    if (grid.value) {
      const mappedData = res.data.map((i: any) => ({
        ...i,
        BSPRICE: i.BSQTY !== 0 ? (Number(i.BSAMT) / Number(i.BSQTY)) : 0,
        INPRICE: i.INQTY !== 0 ? (Number(i.INAMT) / Number(i.INQTY)) : 0,
        OUTPRICE: i.OUTQTY !== 0 ? (Number(i.OUTAMT) / Number(i.OUTQTY)) : 0,
        OUTTPRICE: i.OUTTQTY !== 0 ? (Number(i.OUTTAMT) / Number(i.OUTTQTY)) : 0,
        STKPRICE: i.STKQTY !== 0 ? (Number(i.STKAMT) / Number(i.STKQTY)) : 0,
      }))
      grid.value.setData(mappedData)
      activeItemCount.value = mappedData.length
    }
  } catch (e) { vAlertError('조회 실패') }
}

function initialize() {
  searchData.ASTKIND = '120'
  if (closingInfo.SCLSYM) {
    searchData.YY = closingInfo.SCLSYM.substring(0, 4)
    searchData.MM = closingInfo.SCLSYM.substring(4, 6)
    searchData.TOMM = closingInfo.SCLSYM.substring(4, 6)
  }
  grid.value?.clearData()
}

function print(type: string) {
  vAlert(`${type} 기능은 현재 준비 중입니다.`)
}

const formatDateString = (v: any, type: string) => {
    if (!v || v.length < 6) return v;
    if (type === 'YM') return `${v.substring(0, 4)}-${v.substring(4, 6)}`;
    return v;
}
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hscl200s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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
