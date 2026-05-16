<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio410u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-left me-2 text-danger" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">제품입고취소(자가생산) (HPIO410U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchProcesses">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" style="background-color: #ef4444 !important;" @click="saveCancellation" :disabled="!selectedProg.PROGCD">
          <i class="bi bi-x-circle"></i> 입고취소
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
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 180px;" @change="onLineChange">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
                </td>
                <th class="required">입고창고</th>
                <td>
                  <select v-model="searchData.WHCD" class="form-select form-select-sm" style="width: 180px;">
                    <option v-for="opt in whOptions" :key="opt.WHCD" :value="opt.WHCD">{{ opt.WHNM }}</option>
                  </select>
                </td>
                <th class="required">입고일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiINYMDFR" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiINYMDTO" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 작업 영역 -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 공정 목록 -->
        <div class="card border-0 shadow-sm d-flex flex-column" style="width: 200px; border-radius: 8px;">
          <div class="card-header bg-light py-2 px-3 border-bottom fw-bold small text-dark text-center">
            공정
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-auto">
            <ul class="list-group list-group-flush process-list">
              <li v-for="prog in processList" :key="prog.PROGCD"
                  class="list-group-item list-group-item-action py-2 text-center small"
                  :class="{ active: selectedProg.PROGCD === prog.PROGCD }"
                  @click="onProcessSelect(prog)">
                {{ prog.PROGNM }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 우측: 취소 대상 목록 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark">
                <i class="bi bi-list-check me-1 text-primary"></i> 입고 취소 대상 내역
                <span v-if="selectedProg.PROGNM" class="text-primary ms-2">[{{ selectedProg.PROGNM }}]</span>
              </span>
              <div class="small text-muted">※ 취소할 입고 항목을 선택해 주세요.</div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
              <div ref="gridElement" style="height: 100%;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">조회건수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 마감된 데이터는 취소할 수 없으며, 취소 시 재고가 즉시 차감됩니다.
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
  INYMDFR: initFRYMD,
  INYMDTO: initYMD,
  WHCD: '200'
})

const lineOptions = ref<any[]>([])
const processList = ref<any[]>([])
const whOptions = ref<any[]>([])
const selectedProg = reactive({ PROGCD: '', PROGNM: '' })
const closingInfo = reactive({ CLSYMD: '', SCLSYM: '', PCLSYM: '' })

const uiINYMDFR = computed({ get: () => formatDateString(searchData.INYMDFR, '-'), set: (v) => { if(v) searchData.INYMDFR = v.replace(/-/g, '') } })
const uiINYMDTO = computed({ get: () => formatDateString(searchData.INYMDTO, '-'), set: (v) => { if(v) searchData.INYMDTO = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 라인/공정 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data.map((i: any) => ({ LINECD: i.CODE, LINENM: i.CDNM }))
  } catch (e) {}
}

// 🏭 창고 로드 (HS00_000S_STR 사용)
const fetchWhOptions = async () => {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })
    whOptions.value = res.data.map((i: any) => ({
      WHCD: i.WHCD,
      WHNM: i.WHNM
    }))
  } catch (e) {}
}

const fetchProcesses = async () => {
  if (!searchData.LINECD) return
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'G0', CMPYCD: authStore.CMPYCD, GBNCD: searchData.LINECD } })
    processList.value = res.data.map((i: any) => ({ PROGCD: i.CODE, PROGNM: i.CDNM }))
    grid?.clearData(); itemCount.value = 0; selectedProg.PROGCD = ''; selectedProg.PROGNM = '';
  } catch (e) {}
}

const onLineChange = () => fetchProcesses()

const onProcessSelect = (prog: any) => {
  selectedProg.PROGCD = prog.PROGCD
  selectedProg.PROGNM = prog.PROGNM
  fetchGridData()
}

const fetchGridData = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_410U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      LINECD: searchData.LINECD, PROGCD: selectedProg.PROGCD, WHCD: searchData.WHCD,
      INYMDFR: searchData.INYMDFR, INYMDTO: searchData.INYMDTO
    })
    grid?.setData(res.data)
    itemCount.value = res.data.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns", height: "100%", selectable: true, headerHeight: 40,
      columns: [
          { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", vertAlign: "middle", headerSort: false },
          { title: "입고일자", field: "INYMD", width: 120, hozAlign: "center", vertAlign: "middle", formatter: (c) => formatDateString(c.getValue(), '-') },
          { title: "품목명", field: "ITEMNM", minWidth: 200, vertAlign: "middle", cssClass: "fw-bold" },
          { title: "규격", field: "ITSIZE", width: 150, vertAlign: "middle" },
          { title: "단위", field: "UNIT", width: 70, hozAlign: "center", vertAlign: "middle" },
          { title: "생산량", field: "PRDQTY", width: 100, hozAlign: "right", vertAlign: "middle", formatter: "money", formatterParams: { precision: 0 } },
          { title: "입고량", field: "INQTY", width: 100, hozAlign: "right", vertAlign: "middle", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary fw-bold" },
          { title: "비고", field: "WORKNM", width: 150, vertAlign: "middle", formatter: (cell) => cell.getData().PRODCD === '100' ? cell.getValue() : cell.getData().CUSTNM }
      ]
    })
  }
}

// 3. 취소 저장 로직
const saveCancellation = async () => {
  const selected = grid?.getSelectedData() || []
  if (selected.length === 0) return vAlertError('취소할 입고 항목을 선택하세요.')

  // 마감 체크
  const today = initYMD
  if (today <= closingInfo.CLSYMD) return vAlertError('회계 마감된 데이터는 취소할 수 없습니다.')
  if (today.substring(0, 6) <= closingInfo.SCLSYM) return vAlertError('영업 마감된 월의 데이터는 취소할 수 없습니다.')
  if (today.substring(0, 6) <= closingInfo.PCLSYM) return vAlertError('생산 마감된 월의 데이터는 취소할 수 없습니다.')

  if (!confirm('선택한 입고 내역을 취소 처리하시겠습니까?\n취소 시 재고가 즉시 환원됩니다.')) return

  try {
    for (const item of selected) {
      await api.post('/api/hpio/HPIO_410U_STR', {
        ACTKIND: 'D0',
        CMPYCD: authStore.CMPYCD,
        USERID: authStore.USERID,
        LINECD: searchData.LINECD,
        PROGCD: selectedProg.PROGCD,
        WHCD: searchData.WHCD,
        PROYMD: item.PROYMD,
        JINYMD: item.INYMD,
        EQUPCD: '000',
        PRODCD: item.PRODCD,
        WORKJO: '000',
        WKGBN: item.WKGBN,
        ITEMCD: item.ITEMCD,
        CUSTCD: item.CUSTCD || '',
        IOGBN: item.IOGBN,
        IOYM: item.IOYM,
        IONO: item.IONO,
        IOROWNO: item.IOROWNO
      })
    }
    vAlert('정상적으로 취소 처리되었습니다.')
    fetchGridData()
  } catch (e) { vAlertError('취소 처리 중 오류 발생') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { LINECD: '010', INYMDFR: initFRYMD, INYMDTO: initYMD, WHCD: '200' })
  processList.value = []
  grid?.clearData()
  itemCount.value = 0
  fetchProcesses()
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(async () => {
  // 마감 정보 로드
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      closingInfo.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(r.data[0])[1]).trim()
      closingInfo.PCLSYM = String(Object.values(r.data[0])[2]).trim()
    }
  })
  fetchLineOptions()
  fetchWhOptions()
  fetchProcesses()
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hpio410u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }
.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }
.btn-save { color: #fff !important; }
.btn-save:hover { transform: translateY(-1px); }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 90px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
.process-list .list-group-item { cursor: pointer; font-size: 12.5px; border-left: 4px solid transparent; transition: all 0.2s; }
.process-list .list-group-item:hover { background-color: #f8f9fa; }
.process-list .list-group-item.active { background-color: #eef2ff; color: #4361ee; border-left-color: #4361ee; font-weight: bold; }
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #fff1f1 !important; }
</style>
