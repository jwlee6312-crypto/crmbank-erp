<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio390s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-diagram-3 me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목투입자재현황 (HPIO390S)</span>
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
                <th class="required">생산라인</th>
                <td>
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 200px;" @change="fetchProgOptions">
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">{{ opt.LINENM }}</option>
                  </select>
                </td>
                <th class="required">생산공정</th>
                <td>
                  <select v-model="searchData.PROGCD" class="form-select form-select-sm" style="width: 200px;">
                    <option value="">전체공정</option>
                    <option v-for="opt in progOptions" :key="opt.PROGCD" :value="opt.PROGCD">{{ opt.PROGNM }}</option>
                  </select>
                </td>
                <th class="required">투입일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiPROYMDF" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiPROYMDT" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-check me-1 text-primary"></i> 품목별 자재 투입 상세</span>
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
          <i class="bi bi-info-circle me-1"></i> 생산된 제품(그룹)별로 투입된 원자재 내역을 상세하게 대조하여 보여줍니다.
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
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
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const initYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initFRYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  LINECD: '010',
  PROGCD: '',
  PROYMDF: initFRYMD,
  PROYMDT: initYMD
})

const lineOptions = ref<any[]>([])
const progOptions = ref<any[]>([])

const uiPROYMDF = computed({ get: () => formatDateString(searchData.PROYMDF, '-'), set: (v) => { if (v) searchData.PROYMDF = v.replace(/-/g, '') } })
const uiPROYMDT = computed({ get: () => formatDateString(searchData.PROYMDT, '-'), set: (v) => { if (v) searchData.PROYMDT = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 콤보 옵션 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.post('/api/ha00/HA00_00P_STR', { GUBUN: 'L0', CMPYCD: authStore.CMPYCD })
    lineOptions.value = res.data.map((i: any) => ({ LINECD: i.CODE, LINENM: i.CDNM }))
    if (!searchData.LINECD && lineOptions.value.length > 0) searchData.LINECD = lineOptions.value[0].LINECD
    await fetchProgOptions()
  } catch (e) {}
}

const fetchProgOptions = async () => {
  try {
    const res = await api.post('/api/ha00/HA00_00P_STR', { GUBUN: 'G0', LINECD: searchData.LINECD, CMPYCD: authStore.CMPYCD })
    progOptions.value = res.data.map((i: any) => ({ PROGCD: i.CODE, PROGNM: i.CDNM }))
  } catch (e) {}
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      groupBy: "ITEMCD",
      groupHeader: (value, count, data) => {
          return `
            <div class="d-flex align-items-center gap-3">
                <span class="badge bg-primary px-2">생산제품</span>
                <span class="fw-bold">[${value}] ${data[0].ITEMNM}</span>
                <span class="text-secondary small">| 규격: ${data[0].ITSIZE || '-'}</span>
                <span class="text-secondary small">| 단위: ${data[0].UNIT}</span>
                <span class="ms-auto text-dark fw-bold">생산량: ${formatNumber(data[0].PRDQTY)}</span>
            </div>
          `;
      },
      columns: [
        { title: "생산구분", field: "PRODNM", width: 100, hozAlign: "center" },
        { title: "투입자재코드", field: "MITEMCD", width: 120, hozAlign: "center" },
        { title: "투입자재명", field: "MITEMNM", minWidth: 250, cssClass: "fw-bold text-success" },
        { title: "자재규격", field: "MITSIZE", width: 150 },
        { title: "자재단위", field: "MUNIT", width: 80, hozAlign: "center" },
        {
          title: "투입량", field: "INQTY", width: 120, hozAlign: "right",
          formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 },
          cssClass: "text-primary fw-bold"
        }
      ],
    })
  }
}

// 3. 비즈니스 로직
const fetchList = async () => {
  if (!searchData.LINECD) return vAlertError('생산라인을 선택하세요.')

  try {
    const res = await api.post('/api/hpio/HPIO_390S_STR', {
      CMPYCD: authStore.CMPYCD,
      LINECD: searchData.LINECD,
      PROGCD: searchData.PROGCD || '',
      PROYMDF: searchData.PROYMDF,
      PROYMDT: searchData.PROYMDT
    })

    grid?.setData(res.data)
    itemCount.value = res.data.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 처리 중 오류 발생') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { LINECD: '010', PROGCD: '', PROYMDF: initFRYMD, PROYMDT: initYMD })
  grid?.clearData()
  itemCount.value = 0
  fetchProgOptions()
}

const exportExcel = () => {
  grid?.download("xlsx", `품목투입자재현황_${searchData.PROYMDT}.xlsx`, { title: "품목투입자재현황" })
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  // LINE, PROG 도움창은 콤보로 대체됨
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchLineOptions()
  nextTick(() => { initGrid(); fetchList(); })
})
</script>

<style scoped>
.hpio390s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
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

:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }

/* Group Header Style */
:deep(.tabulator-group) { background: #eef2ff !important; border-top: 1px solid #cbd5e1 !important; border-bottom: 1px solid #cbd5e1 !important; color: #1e293b !important; }
</style>
