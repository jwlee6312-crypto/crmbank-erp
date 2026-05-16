<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpcl220s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-stack me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">재공수불부 (HPCL220S)</span>
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
                <th class="required">생산라인</th>
                <td>
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 180px;" @change="onLineChange">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
                </td>
                <th class="required">생산공정</th>
                <td>
                  <select v-model="searchData.PROGCD" class="form-select form-select-sm" style="width: 180px;" @change="fetchList">
                    <option value="">공정 선택</option>
                    <option v-for="opt in progOptions" :key="opt.PROGCD" :value="opt.PROGCD">
                      [{{ opt.PROGCD }}] {{ opt.PROGNM }}
                    </option>
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
          <span class="fw-bold small text-dark"><i class="bi bi-table me-1 text-primary"></i> 재공품 월간 수불 상세</span>
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
          <i class="bi bi-info-circle me-1"></i> 품목명을 클릭하면 해당 재공품의 기간별 상세 수불현황(HPIO670S)으로 이동합니다.
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

// 1. 상태 관리
const searchData = reactive({
  YY: String(now.getFullYear()),
  MM: String(now.getMonth() + 1).padStart(2, '0'),
  LINECD: '010',
  PROGCD: ''
})

const yearOptions = ref<string[]>([])
const monthOptions = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
const lineOptions = ref<any[]>([])
const progOptions = ref<any[]>([])
const closingInfo = reactive({ CLSYMD: '', SCLSYM: '', PCLSYM: '' })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

const generateYearOptions = () => {
    const currentYear = new Date().getFullYear()
    for (let i = 0; i < 5; i++) {
        yearOptions.value.push(String(currentYear - i))
    }
}

// 🏭 라인/공정 목록 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data
  } catch (e) {}
}

const fetchProgOptions = async (lineCd: string) => {
  if (!lineCd) { progOptions.value = []; return; }
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'I8', CMPYCD: authStore.CMPYCD, LINECD: lineCd } })
    progOptions.value = res.data
  } catch (e) {}
}

const onLineChange = () => {
    searchData.PROGCD = ''
    fetchProgOptions(searchData.LINECD)
    fetchList()
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
          title: "재공품정보", frozen: true,
          columns: [
            { title: "코드", field: "ITEMCD", width: 90, hozAlign: "center", headerSort: false },
            {
              title: "품 목 명", field: "ITEMNM", minWidth: 200, headerSort: false,
              formatter: "html",
              cellClick: (e, cell) => {
                const d = cell.getData()
                const fymd = `${searchData.YY}-${searchData.MM}-01`
                const lastDay = new Date(Number(searchData.YY), Number(searchData.MM), 0).getDate()
                const tymd = `${searchData.YY}-${searchData.MM}-${String(lastDay).padStart(2, '0')}`

                router.push({
                  path: '/HPIO670S',
                  query: {
                    LINECD: searchData.LINECD,
                    PROGCD: searchData.PROGCD,
                    ITEMCD: d.ITEMCD,
                    FYMD: fymd,
                    TYMD: tymd
                  }
                })
              },
              cssClass: "text-primary text-decoration-underline cursor-pointer fw-bold",
              bottomCalc: () => "합 계"
            },
            { title: "규격", field: "ITSIZE", width: 120, headerSort: false }
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
const fetchClosingStatus = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } })
    if (res.data?.length) {
      closingInfo.PCLSYM = String(Object.values(res.data[0])[2]).trim()
      if (closingInfo.PCLSYM.length === 6) {
          searchData.YY = closingInfo.PCLSYM.substring(0, 4)
          searchData.MM = closingInfo.PCLSYM.substring(4, 6)
      }
    }
  } catch (e) {}
}

const fetchList = async () => {
  if (!searchData.LINECD || !searchData.PROGCD) return

  try {
    const res = await api.post('/api/hpcl/HPCL_220S_STR', {
      CMPYCD: authStore.CMPYCD,
      YM: searchData.YY + searchData.MM,
      LINECD: searchData.LINECD,
      PROGCD: searchData.PROGCD
    })

    const mapped = res.data.map((i: any) => ({
        ...i,
        BSPRICE: Number(i.BSQTY) !== 0 ? Math.round(Number(i.BSAMT) / Number(i.BSQTY)) : 0,
        INPRICE: Number(i.INQTY) !== 0 ? Math.round(Number(i.INAMT) / Number(i.INQTY)) : 0,
        OUTPRICE: Number(i.OUTQTY) !== 0 ? Math.round(Number(i.OUTAMT) / Number(i.OUTQTY)) : 0,
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
  Object.assign(searchData, { YY: String(now.getFullYear()), MM: String(now.getMonth() + 1).padStart(2, '0'), LINECD: '010', PROGCD: '' })
  grid?.clearData()
  itemCount.value = 0
}

const exportExcel = () => {
  grid?.download("xlsx", `재공수불부_${searchData.YY}${searchData.MM}.xlsx`, { title: "재공 수불부" })
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)
const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

onMounted(() => {
  generateYearOptions()
  fetchLineOptions()
  fetchClosingStatus()
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hpcl220s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
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
