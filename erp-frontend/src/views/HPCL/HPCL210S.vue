<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpcl210s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">창고수불부 (HPCL210S)</span>
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
                <th class="required">연&nbsp;&nbsp;&nbsp;&nbsp;월</th>
                <td>
                  <div class="d-flex align-items-center gap-2" style="width: 220px;">
                    <select v-model="searchData.YY" class="form-select form-select-sm">
                      <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
                    </select>
                    <select v-model="searchData.MM" class="form-select form-select-sm">
                      <option v-for="month in monthOptions" :key="month" :value="month">{{ month }}월</option>
                    </select>
                  </div>
                </td>
                <th class="required">출고창고</th>
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
          <span class="fw-bold small text-dark"><i class="bi bi-table me-1 text-primary"></i> 창고별 상세 수불 내역</span>
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
          <i class="bi bi-info-circle me-1"></i> 품목명을 클릭하면 해당 품목의 기간별 상세 수불현황(HPIO650S)으로 이동합니다.
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

// 1. 상태 관리
const searchData = reactive({
  YY: String(now.getFullYear()),
  MM: String(now.getMonth() + 1).padStart(2, '0'),
  WHCD: '000',
  ASTKIND: ''
})

const yearOptions = ref<string[]>([])
const monthOptions = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
const whOptions = ref<any[]>([])
const astOptions = ref<any[]>([])

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

const generateYearOptions = () => {
    const currentYear = new Date().getFullYear()
    for (let i = 0; i < 5; i++) {
        yearOptions.value.push(String(currentYear - i))
    }
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      columnHeaderVertAlign: "middle",
      columns: [
        {
          title: "품목정보", frozen: true,
          columns: [
            {
              title: "품 목 명", field: "ITEMNM", minWidth: 200, headerSort: false,
              formatter: "html",
              cellClick: (e, cell) => {
                const d = cell.getData()
                const fymd = `${searchData.YY}-${searchData.MM}-01`
                const lastDay = new Date(Number(searchData.YY), Number(searchData.MM), 0).getDate()
                const tymd = `${searchData.YY}-${searchData.MM}-${String(lastDay).padStart(2, '0')}`

                router.push({
                  path: '/HPIO650S',
                  query: {
                    ASTKIND: searchData.ASTKIND,
                    WHCD: searchData.WHCD,
                    ITEMCD: d.ITEMCD,
                    FYMD: fymd,
                    TYMD: tymd
                  }
                })
              },
              cssClass: "text-primary text-decoration-underline cursor-pointer fw-bold",
              bottomCalc: () => "합 계"
            }
          ]
        },
        {
          title: "전 월 이 월",
          columns: [
            { title: "수량", field: "BSQTY", width: 70, hozAlign: "right", formatter: "money", bottomCalc: "sum" },
            { title: "단가", field: "BSPRICE", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
            { title: "금액", field: "BSAMT", width: 85, hozAlign: "right", formatter: "money", bottomCalc: "sum" }
          ]
        },
        {
          title: "당 월 입 고",
          columns: [
            { title: "수량", field: "INQTY", width: 70, hozAlign: "right", formatter: "money", bottomCalc: "sum", cssClass: "text-success" },
            { title: "단가", field: "INPRICE", width: 70, hozAlign: "right", formatter: "money" },
            { title: "금액", field: "INAMT", width: 85, hozAlign: "right", formatter: "money", bottomCalc: "sum" }
          ]
        },
        {
          title: "당 월 출 고",
          columns: [
            { title: "수량", field: "OUTQTY", width: 70, hozAlign: "right", formatter: "money", bottomCalc: "sum", cssClass: "text-danger" },
            { title: "단가", field: "OUTPRICE", width: 70, hozAlign: "right", formatter: "money" },
            { title: "금액", field: "OUTAMT", width: 85, hozAlign: "right", formatter: "money", bottomCalc: "sum" }
          ]
        },
        {
          title: "이 관",
          columns: [
            { title: "수량", field: "OUTMQTY", width: 70, hozAlign: "right", formatter: "money", bottomCalc: "sum" },
            { title: "단가", field: "OUTMPRICE", width: 70, hozAlign: "right", formatter: "money" },
            { title: "금액", field: "OUTMAMT", width: 85, hozAlign: "right", formatter: "money", bottomCalc: "sum" }
          ]
        },
        {
          title: "타 계 정",
          columns: [
            { title: "수량", field: "OUTTQTY", width: 70, hozAlign: "right", formatter: "money", bottomCalc: "sum" },
            { title: "단가", field: "OUTTPRICE", width: 70, hozAlign: "right", formatter: "money" },
            { title: "금액", field: "OUTTAMT", width: 85, hozAlign: "right", formatter: "money", bottomCalc: "sum" }
          ]
        },
        {
          title: "재 고 현 황",
          columns: [
            { title: "수량", field: "STKQTY", width: 70, hozAlign: "right", formatter: "money", bottomCalc: "sum", cssClass: "fw-bold" },
            { title: "단가", field: "STKPRICE", width: 70, hozAlign: "right", formatter: "money" },
            { title: "금액", field: "STKAMT", width: 100, hozAlign: "right", formatter: "money", bottomCalc: "sum", cssClass: "text-primary fw-bold" }
          ]
        }
      ],
    })
  }
}

// 3. 비즈니스 로직
const fetchOptions = async () => {
  try {
    // 창고 (HS00_000S_STR 'W0')
    const resWh = await api.get('/api/comm/codes/WH')
    whOptions.value = resWh.data

    // 재고자산 (HP00_000S_STR 'E0', '100')
    const resAst = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '100' } })
    astOptions.value = resAst.data.filter((i:any) => i.CODE <= '119')
    if (astOptions.value.length > 0) searchData.ASTKIND = astOptions.value[0].CODE

    // 마감 상태에 따른 연월 초기값
    const statusRes = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } })
    if (statusRes.data?.length) {
        const pclsym = String(Object.values(statusRes.data[0])[2]).trim()
        if (pclsym.length === 6) {
            searchData.YY = pclsym.substring(0, 4)
            searchData.MM = pclsym.substring(4, 6)
        }
    }
  } catch (e) {}
}

const fetchList = async () => {
  if (!searchData.ASTKIND) return vAlertError('재고자산을 선택하세요.')
  if (!searchData.WHCD) return vAlertError('창고를 선택하세요.')

  try {
    const res = await api.post('/api/hpcl/HPCL_210S_STR', {
      CMPYCD: authStore.CMPYCD,
      YM: searchData.YY + searchData.MM,
      WHCD: searchData.WHCD,
      ASTKIND: searchData.ASTKIND
    })

    // 단가 계산 로직
    const mapped = res.data.map((i: any) => ({
        ...i,
        BSPRICE: Number(i.BSQTY) !== 0 ? Math.round(Number(i.BSAMT) / Number(i.BSQTY)) : 0,
        INPRICE: Number(i.INQTY) !== 0 ? Math.round(Number(i.INAMT) / Number(i.INQTY)) : 0,
        OUTPRICE: Number(i.OUTQTY) !== 0 ? Math.round(Number(i.OUTAMT) / Number(i.OUTQTY)) : 0,
        OUTMPRICE: Number(i.OUTMQTY) !== 0 ? Math.round(Number(i.OUTMAMT) / Number(i.OUTMQTY)) : 0,
        OUTTPRICE: Number(i.OUTTQTY) !== 0 ? Math.round(Number(i.OUTTAMT) / Number(i.OUTTQTY)) : 0,
        STKPRICE: Number(i.STKQTY) !== 0 ? Math.round(Number(i.STKAMT) / Number(i.STKQTY)) : 0
    }))

    grid?.setData(mapped)
    itemCount.value = mapped.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { YY: String(now.getFullYear()), MM: String(now.getMonth() + 1).padStart(2, '0'), WHCD: '000' })
  if (astOptions.value.length > 0) searchData.ASTKIND = astOptions.value[0].CODE
  grid?.clearData()
  itemCount.value = 0
}

const exportExcel = () => {
  grid?.download("xlsx", `창고수불부_${searchData.YY}${searchData.MM}.xlsx`, { title: "창고별 재고자산 수불부" })
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)
const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(() => {
  generateYearOptions()
  fetchOptions()
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hpcl210s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }
.btn-excel { background-color: #107c41 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

:deep(.tabulator) { border: none; font-size: 11.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.2 !important; text-align: center !important; color: #333; }
:deep(.tabulator-footer) { background-color: #f8f9fa !important; border-top: 2px solid #dee2e6 !important; font-weight: 800; font-size: 12px; }
.cursor-pointer { cursor: pointer; }
</style>
