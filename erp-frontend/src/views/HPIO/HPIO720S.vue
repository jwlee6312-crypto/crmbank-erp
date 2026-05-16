<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio720s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-house-door-fill me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">창고별현재고 (HPIO720S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchList">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-excel" @click="exportExcel">
          <i class="bi bi-file-earmark-excel"></i> 엑셀
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border-0 shadow-sm overflow-hidden" style="border-radius: 8px;">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <colgroup>
              <col style="width: 100px;"><col>
              <col style="width: 100px;"><col>
              <col style="width: 100px;"><col>
            </colgroup>
            <tbody>
              <tr>
                <th class="required">기준일자</th>
                <td>
                  <input v-model="uiYMD" type="date" class="form-control form-control-sm" style="width: 150px;" />
                </td>
                <th class="required">창&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td>
                  <select v-model="searchData.WHCD" class="form-select form-select-sm" style="width: 150px;">
                    <option value="000">전체</option>
                    <option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
                  </select>
                </td>
                <th class="required">재고자산</th>
                <td>
                  <select v-model="searchData.ASTKIND" class="form-select form-select-sm" style="width: 150px;">
                    <option v-for="opt in astOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-columns-reverse me-1 text-primary"></i> 창고별 품목 재고 현황</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">조회건수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 품목명을 클릭하면 해당 품목의 상세 수불현황(HPIO650S)으로 이동합니다.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

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
  ASTKIND: ''
})

const whOptions = ref<any[]>([])
const astOptions = ref<any[]>([])

const uiYMD = computed({ get: () => formatDateString(searchData.YMD, '-'), set: (v) => { if (v) searchData.YMD = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 기초 코드 로드
const fetchOptions = async () => {
  try {
    // 창고 코드 (HS00_000S_STR 'W0')
    const resWh = await api.get('/api/comm/codes/WH')
    whOptions.value = resWh.data

    // 재고자산 구분 (HS00_000S_STR 'E0')
    const resAst = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '100' } })
    astOptions.value = resAst.data
    if (astOptions.value.length > 0) searchData.ASTKIND = astOptions.value[0].CODE
  } catch (e) {}
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      columnDefaults: { headerHozAlign: "center" },
      columns: [
        { title: "품목코드", field: "ITEMCD", width: 100, hozAlign: "center" },
        {
          title: "품 목 명", field: "ITEMNM", minWidth: 250,
          formatter: "html",
          cellClick: (e, cell) => {
            const d = cell.getData()
            router.push({
              path: '/HPIO650S',
              query: {
                ASTKIND: searchData.ASTKIND,
                WHCD: searchData.WHCD,
                ITEMCD: d.ITEMCD,
                FYMD: searchData.YMD.substring(0, 6) + '01',
                TYMD: searchData.YMD
              }
            })
          },
          cssClass: "text-primary text-decoration-underline cursor-pointer fw-bold",
          bottomCalc: () => "합 계", bottomCalcHozAlign: "center"
        },
        { title: "규격", field: "ITSIZE", width: 150 },
        { title: "단위", field: "UNIT", width: 60, hozAlign: "center" },
        {
          title: "적정재고", field: "STOCK", width: 100, hozAlign: "right",
          formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 },
          bottomCalc: "sum"
        },
        {
          title: "현재고", field: "STKQTY", width: 100, hozAlign: "right",
          formatter: (cell) => {
              const d = cell.getData();
              const val = cell.getValue();
              const colorClass = Number(d.STOCK) > Number(val) ? 'text-danger fw-bold' : 'fw-bold';
              return `<span class="${colorClass}">${formatNumber(val, d.QTYPNT)}</span>`;
          },
          bottomCalc: "sum"
        },
        {
          title: "과부족", field: "DIFF", width: 100, hozAlign: "right",
          formatter: (cell) => {
              const d = cell.getData();
              const diff = Number(d.STKQTY || 0) - Number(d.STOCK || 0);
              return formatNumber(diff, d.QTYPNT);
          },
          bottomCalc: "sum"
        },
        { title: "재고금액", field: "STKAMT", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
        { title: "미입고량", field: "NONQTY", width: 100, hozAlign: "right", formatter: "money", bottomCalc: "sum" },
        {
          title: "총 수 량", field: "TOTAL_QTY", width: 110, hozAlign: "right",
          formatter: (cell) => {
              const d = cell.getData();
              return formatNumber(Number(d.STKQTY || 0) + Number(d.NONQTY || 0), d.QTYPNT);
          },
          bottomCalc: "sum", cssClass: "text-primary fw-bold"
        }
      ],
    })
  }
}

// 3. 비즈니스 로직
const fetchList = async () => {
  if (!searchData.ASTKIND) return vAlertError('재고자산을 선택하세요.')

  try {
    const res = await api.post('/api/hpio/HPIO_720S_STR', {
      CMPYCD: authStore.CMPYCD,
      YMD: searchData.YMD,
      WHCD: searchData.WHCD,
      ASTKIND: searchData.ASTKIND
    })
    grid?.setData(res.data)
    itemCount.value = res.data.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 중 오류 발생') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { YMD: initYMD, WHCD: '000' })
  if (astOptions.value.length > 0) searchData.ASTKIND = astOptions.value[0].CODE
  grid?.clearData()
  itemCount.value = 0
}

const exportExcel = () => {
  grid?.download("xlsx", `창고별현재고_${searchData.YMD}.xlsx`, { title: "창고별 현재고 현황" })
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')
const formatNumber = (val: any, precision: any = 0) => new Intl.NumberFormat('ko-KR', { minimumFractionDigits: precision, maximumFractionDigits: precision }).format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => { initGrid(); fetchList(); })
})
</script>

<style scoped>
.hpio720s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }
.btn-excel { background-color: #107c41 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* Tabulator 스타일 */
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-footer) { background-color: #f8f9fa !important; border-top: 2px solid #dee2e6 !important; font-weight: 800; }
.cursor-pointer { cursor: pointer; }
</style>
