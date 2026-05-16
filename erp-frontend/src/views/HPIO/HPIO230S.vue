<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio230s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">생산지시현황 (HPIO230S)</span>
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
            <tbody>
              <tr>
                <th class="required">생산라인</th>
                <td>
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 200px;" @change="fetchList">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
                </td>
                <th class="required">지시일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiORDYMDF" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiORDYMDT" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
        <div class="card-header bg-white py-2 px-3 border-bottom fw-bold small text-dark">
          <i class="bi bi-list-ul me-1 text-primary"></i> 생산 지시 목록
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-3 small">조회건수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-9 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> LOT NO. 클릭 시 해당 부여작업 화면으로 이동합니다.
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
const initFRYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  LINECD: '010',
  ORDYMDF: initFRYMD,
  ORDYMDT: initYMD
})

const lineOptions = ref<any[]>([])
const uiORDYMDF = computed({ get: () => formatDateString(searchData.ORDYMDF, '-'), set: (v) => { if (v) searchData.ORDYMDF = v.replace(/-/g, '') } })
const uiORDYMDT = computed({ get: () => formatDateString(searchData.ORDYMDT, '-'), set: (v) => { if (v) searchData.ORDYMDT = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 라인 목록 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data
  } catch (e) { console.error(e) }
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      columns: [
        { title: "지시일자", field: "ORDYMD", width: 100, hozAlign: "center", formatter: (c) => formatDateString(c.getValue(), '-') },
        {
          title: "LOT NO.", field: "LOT_DISP", width: 150, hozAlign: "center",
          formatter: "html",
          cellClick: (e, cell) => {
              const d = cell.getData()
              console.log('Navigate to HPIO200U', d.LINECD, d.LOTYMD)
          },
          cssClass: "text-primary text-decoration-underline cursor-pointer fw-bold"
        },
        { title: "생산공정", field: "PROGNM", width: 120 },
        { title: "품목명", field: "ITEMNM", minWidth: 200, cssClass: "fw-bold" },
        { title: "규격", field: "ITSIZE", width: 120, hozAlign: "center" },
        { title: "LOT SIZE", field: "LOTSIZE", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "지시수량", field: "ORDQTY", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary" },
        { title: "생산수량", field: "PRODQTY", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-success" },
        { title: "생산일자", field: "PROYMD", width: 100, hozAlign: "center", formatter: (c) => formatDateString(c.getValue(), '-') },
        { title: "완료예정", field: "TOYMD", width: 100, hozAlign: "center", formatter: (c) => formatDateString(c.getValue(), '-') },
        {
          title: "주문번호", field: "ORD_DISP", width: 130, hozAlign: "center",
          formatter: "html",
          cssClass: "text-secondary text-decoration-underline cursor-pointer"
        },
        { title: "납품처", field: "CUSTNM", width: 150 }
      ],
    })
  }
}

// 3. 비즈니스 로직
const fetchList = async () => {
  if (!searchData.LINECD) return vAlertError('생산라인을 선택하세요.')
  try {
    const res = await api.post("/api/hpio/HPIO_230S_STR", {
        CMPYCD: authStore.CMPYCD,
        LINECD: searchData.LINECD,
        ORDYMDF: searchData.ORDYMDF,
        ORDYMDT: searchData.ORDYMDT
    })

    const mappedData = (res.data || []).map((item: any) => ({
        ...item,
        LOT_DISP: `${formatDateString(item.LOTYMD, '-')}-${item.LOTNO}`,
        ORD_DISP: `${formatDateString(item.ORDYM, '-')}-${item.ORDNO}`
    }))

    grid?.setData(mappedData)
    itemCount.value = mappedData.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 처리 중 오류가 발생했습니다.') }
}

const initialize = () => {
  searchData.LINECD = '010'
  searchData.ORDYMDF = initFRYMD
  searchData.ORDYMDT = initYMD
  grid?.clearData()
  itemCount.value = 0
}

const exportExcel = () => {
  grid?.download("xlsx", `생산지시현황_${searchData.ORDYMDT}.xlsx`, { title: "생산지시현황" })
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(() => {
  fetchLineOptions()
  nextTick(() => { initGrid() })
})
</script>

<style scoped>
.hpio230s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

/* 메인 버튼 스타일 통일 */
.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }
.btn-excel { background-color: #107c41 !important; color: #fff !important; }
.btn-excel:hover { background-color: #0b5e31 !important; transform: translateY(-1px); }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* Tabulator 스타일 기준 */
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
.cursor-pointer { cursor: pointer; }
</style>
