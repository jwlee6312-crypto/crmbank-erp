<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio360s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calendar-check me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">생산일보 (HPIO360S)</span>
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
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-3">
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
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 180px;" @change="onLineChange">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
                </td>
                <th class="required">생산공정</th>
                <td>
                  <select v-model="searchData.PROGCD" class="form-select form-select-sm" style="width: 180px;">
                    <option value="">공정 선택</option>
                    <option v-for="opt in progOptions" :key="opt.PROGCD" :value="opt.PROGCD">
                      [{{ opt.PROGCD }}] {{ opt.PROGNM }}
                    </option>
                  </select>
                </td>
                <th class="required">생산일자</th>
                <td>
                  <input v-model="uiPROYMD" type="date" class="form-control form-control-sm" style="width: 150px;" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 생산내역 그리드 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px; min-height: 250px;">
        <div class="card-header bg-white py-2 px-3 border-bottom">
          <span class="fw-bold small text-dark"><i class="bi bi-list-stars me-1 text-primary"></i> 생산 내역</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="prodGridElement" style="height: 100%;"></div>
        </div>
      </div>

      <!-- 🅲 자재투입 그리드 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px; min-height: 300px;">
        <div class="card-header bg-white py-2 px-3 border-bottom">
          <span class="fw-bold small text-dark"><i class="bi bi-box-arrow-in-right me-1 text-success"></i> 자재 투입 현황</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="matGridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-12 small text-end opacity-75">
          <i class="bi bi-info-circle me-1"></i> 당일 생산 실적과 투입된 자재 내역을 한눈에 조회할 수 있는 일보 화면입니다.
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

// 1. 상태 관리
const searchData = reactive({
  LINECD: '010',
  PROGCD: '',
  PROYMD: initYMD
})

const lineOptions = ref<any[]>([])
const progOptions = ref<any[]>([])
const uiPROYMD = computed({ get: () => formatDateString(searchData.PROYMD, '-'), set: (v) => { if (v) searchData.PROYMD = v.replace(/-/g, '') } })

const prodGridElement = ref<HTMLElement | null>(null)
const matGridElement = ref<HTMLElement | null>(null)
let prodGrid: Tabulator | null = null
let matGrid: Tabulator | null = null

// 🏭 라인/공정 옵션 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data
  } catch (e) {}
}

const fetchProgOptions = async (lineCd: string) => {
  if (!lineCd) { progOptions.value = []; return; }
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'G6', CMPYCD: authStore.CMPYCD, LINECD: lineCd } })
    progOptions.value = res.data
  } catch (e) {}
}

const onLineChange = () => {
    searchData.PROGCD = ''
    fetchProgOptions(searchData.LINECD)
}

// 2. 그리드 초기화
const initGrids = () => {
  if (prodGridElement.value) {
    prodGrid = new Tabulator(prodGridElement.value, {
      layout: "fitColumns", height: "100%",
      columns: [
        { title: "코드", field: "ITEMCD", width: 100, hozAlign: "center" },
        { title: "품 목", field: "ITEMNM", minWidth: 200, cssClass: "fw-bold" },
        { title: "규격", field: "ITSIZE", width: 120 },
        { title: "단위", field: "UNIT", width: 60, hozAlign: "center" },
        { title: "생산구분", field: "PRODNM", width: 100, hozAlign: "center" },
        { title: "총생산량", field: "PRDQTY", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 } },
        { title: "양품수량", field: "GODQTY", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 }, cssClass: "text-primary" },
        { title: "불량수량", field: "ERRQTY", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 }, cssClass: "text-danger" },
        { title: "작업시간", field: "WORKMM", width: 90, hozAlign: "right" },
        { title: "월생산량", field: "PRDQTY_M", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 } },
        { title: "월작업시간", field: "WORKMM_M", width: 100, hozAlign: "right" }
      ]
    })
  }

  if (matGridElement.value) {
    matGrid = new Tabulator(matGridElement.value, {
      layout: "fitColumns", height: "100%",
      groupBy: "ITEMCD",
      groupHeader: (value, count, data) => `${data[0].ITEMNM} (${data[0].ITSIZE})`,
      columns: [
        { title: "생산구분", field: "PRODNM", width: 100, hozAlign: "center" },
        { title: "자재코드", field: "MITEMCD", width: 100, hozAlign: "center" },
        { title: "투입자재명", field: "MITEMNM", minWidth: 200, cssClass: "fw-bold" },
        { title: "규격", field: "MITSIZE", width: 150 },
        { title: "단위", field: "MUNIT", width: 60, hozAlign: "center" },
        { title: "투입량", field: "INQTY", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 }, cssClass: "text-success fw-bold" },
        { title: "월투입량", field: "INQTY_M", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 } }
      ]
    })
  }
}

// 3. 비즈니스 로직
const fetchList = async () => {
  if (!searchData.LINECD || !searchData.PROGCD) return vAlertError('라인과 공정을 선택하세요.')

  try {
    const resProd = await api.post('/api/hpio/HPIO_360S_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      LINECD: searchData.LINECD, PROGCD: searchData.PROGCD, PROYMD: searchData.PROYMD
    })
    prodGrid?.setData(resProd.data)

    const resMat = await api.post('/api/hpio/HPIO_360S_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD,
      LINECD: searchData.LINECD, PROGCD: searchData.PROGCD, PROYMD: searchData.PROYMD
    })
    matGrid?.setData(resMat.data)

    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 처리 중 오류 발생') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { LINECD: '010', PROGCD: '', PROYMD: initYMD })
  fetchProgOptions(searchData.LINECD)
}

const exportExcel = () => {
  prodGrid?.download("xlsx", `생산일보_${searchData.PROYMD}.xlsx`, { title: "생산일보" })
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(() => {
  fetchLineOptions()
  fetchProgOptions(searchData.LINECD)
  nextTick(() => initGrids())
})
</script>

<style scoped>
.hpio360s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }
.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-excel { background-color: #107c41 !important; color: #fff !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-group) { background: #f1f5f9 !important; font-weight: 800 !important; }
</style>
