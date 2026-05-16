<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio210u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-list-task me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">작업지시 (HPIO210U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchProcesses">조회</button>
        <button class="btn-erp btn-save" @click="saveData" :disabled="!selectedProg.PROGCD">저장</button>
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
                <th class="required">생산라인</th>
                <td>
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 200px;" @change="onLineChange">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
                </td>
                <th class="required">생산일자</th>
                <td>
                  <input v-model="uiPRODYMD" type="date" class="form-control form-control-sm" style="width: 150px;" @change="onDateChange" />
                </td>
                <th>주문번호</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="uiORDYM" type="month" class="form-control" />
                    <input v-model="searchData.ORDNO" type="text" class="form-control text-center" placeholder="번호" maxlength="4" />
                    <button class="btn btn-outline-secondary" @click="openHelp('ORDER')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 작업 영역 (좌: 공정목록, 우: 지시내역) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 공정 목록 -->
        <div class="card border shadow-sm d-flex flex-column" style="width: 250px;">
          <div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-gear-wide-connected me-1"></i> 공정 목록
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-auto">
            <ul class="list-group list-group-flush process-list">
              <li v-if="processList.length === 0" class="list-group-item text-center py-4 text-muted small">라인을 먼저 선택하세요.</li>
              <li
                v-for="prog in processList"
                :key="prog.PROGCD"
                class="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
                :class="{ active: selectedProg.PROGCD === prog.PROGCD }"
                @click="onProcessSelect(prog)"
              >
                <span>{{ prog.PROGNM }}</span>
                <span class="badge rounded-pill bg-secondary small">{{ prog.PROGCD }}</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- 우측: 작업지시 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark">
                <i class="bi bi-grid-3x3-gap-fill me-1"></i> 지시 상세
                <span v-if="selectedProg.PROGNM" class="text-primary ms-2">[{{ selectedProg.PROGNM }}]</span>
              </span>
              <div class="small text-muted">※ 항목 선택 후 지시량을 수정하세요.</div>
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
        <div class="col-md-3 small">건수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-9 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 공정을 클릭하면 해당 공정의 작업지시 내역이 조회됩니다.
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick, watch } from 'vue'
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
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`

// 1. 상태 관리
const searchData = reactive({
  LINECD: '',
  PRODYMD: initYMD,
  ORDYM: initYM,
  ORDNO: ''
})

const lineOptions = ref<any[]>([])
const processList = ref<any[]>([])
const selectedProg = reactive({ PROGCD: '', PROGNM: '' })

const uiPRODYMD = computed({
  get: () => formatDateString(searchData.PRODYMD, '-'),
  set: (v) => { if (v) searchData.PRODYMD = v.replace(/-/g, '') }
})

const uiORDYM = computed({
  get: () => searchData.ORDYM ? `${searchData.ORDYM.substring(0, 4)}-${searchData.ORDYM.substring(4, 6)}` : '',
  set: (v) => { if (v) searchData.ORDYM = v.replace(/-/g, '') }
})

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 라인 목록 로드 (L0 규칙)
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', {
      params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y', CODE: '' }
    })
    lineOptions.value = res.data
  } catch (e) { console.error(e) }
}

// ⚙️ 공정 목록 로드 (G0 규칙)
const fetchProcesses = async () => {
  if (!searchData.LINECD) {
    vAlertError('생산라인을 먼저 선택하세요.')
    return
  }
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', {
      params: { GUBUN: 'G0', CMPYCD: authStore.CMPYCD, GBNCD: searchData.LINECD, CODE: '' }
    })
    processList.value = res.data
    itemCount.value = 0
    grid?.clearData()
    selectedProg.PROGCD = ''
    selectedProg.PROGNM = ''
  } catch (e) { console.error(e) }
}

const onLineChange = () => {
  fetchProcesses()
}

const onDateChange = () => {
  if (selectedProg.PROGCD) fetchGridData()
}

// 공정 선택 시 그리드 조회
const onProcessSelect = async (prog: any) => {
  selectedProg.PROGCD = prog.PROGCD
  selectedProg.PROGNM = prog.PROGNM

  // 1. 해당 공정의 주문번호 정보 먼저 조회 (S1)
  try {
    const resS1 = await api.post('/api/hpio/HPIO_210U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD,
      PRODYMD: searchData.PRODYMD, LINECD: searchData.LINECD, PROGCD: prog.PROGCD
    })
    if (resS1.data && resS1.data.length > 0) {
      searchData.ORDYM = resS1.data[0].ORDYM || searchData.ORDYM
      searchData.ORDNO = resS1.data[0].ORDNO || ''
    }
  } catch (e) {}

  fetchGridData()
}

// 그리드 데이터 조회 (S0)
const fetchGridData = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_210U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      PRODYMD: searchData.PRODYMD,
      LINECD: searchData.LINECD,
      PROGCD: selectedProg.PROGCD
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
      layout: "fitData",
      height: "100%",
      placeholder: "공정을 선택해 주세요.",
      selectable: true,
      headerSort: false,
      headerHeight: 60,
      columns: [
          { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 50, hozAlign: "center", headerSort: false },
          {
            title: "제품명", field: "ITEMNM", minWidth: 350,
            cellClick: (e, cell) => {
              if (!searchData.LINECD) return vAlertError('생산라인을 먼저 선택하세요.')
              openHelp('ITEM', cell)
            }
          },
          { title: "규격", field: "ITSIZE", width: 150, hozAlign: "center" },
          { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
          { title: "LOTSIZE", field: "LOTSIZE", width: 150, hozAlign: "right", editor: "number" },
          { title: "지시량", field: "ORDQTY", width: 150, hozAlign: "right", editor: "number" },
          { title: "비고", field: "BIGO", minWidth: 200, editor: "input" },
          { title: "삭제", field: "USEYN", width: 100, hozAlign: "center", formatter: "tickCross", editor: true, formatterParams: { crossElement: true } }
      ]
    })
  }
}

const saveData = async () => {
  const selectedData = grid?.getSelectedData() || []
  if (selectedData.length === 0) return vAlertError('저장할 항목을 선택하세요.')

  if (!confirm('작업지시 내역을 저장하시겠습니까?')) return

  try {
    for (const item of selectedData) {
      const actkind = item.EXITYN === 'A' ? 'A0' : 'U0' // 기존 ASP 로직 반영
      const res = await api.post('/api/hpio/HPIO_210U_STR', {
        ACTKIND: actkind,
        CMPYCD: authStore.CMPYCD,
        USERID: authStore.USERID,
        PRODYMD: searchData.PRODYMD,
        LINECD: searchData.LINECD,
        PROGCD: selectedProg.PROGCD,
        LOTYMD: item.LOTYMD,
        LOTNO: item.LOTNO,
        ITEMCD: item.ITEMCD,
        ITSIZE: item.ITSIZE,
        UNIT: item.UNIT,
        ORDQTY: item.ORDQTY,
        BIGO: item.BIGO,
        USEYN: item.USEYN || 'Y',
        ORDYM: searchData.ORDYM,
        ORDNO: searchData.ORDNO
      })

      if (res.data?.[0]?.RTN_CD === 'Y' || res.data?.[0]?.ERRYN === 'Y') {
        vAlertError(res.data[0].RTN_MSG || '처리 중 오류 발생')
        return
      }
    }
    vAlert('정상적으로 저장되었습니다.')
    fetchGridData()
  } catch (e) { vAlertError('서버 통신 오류') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { LINECD: '', PRODYMD: initYMD, ORDYM: initYM, ORDNO: '' })
  processList.value = []
  grid?.clearData()
  selectedProg.PROGCD = ''
  selectedProg.PROGNM = ''
}

const openHelp = (type: string) => {
  if (type === 'ORDER') {
    Object.assign(modalProps, {
      title: '주문번호 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ORDNO',
      data: { GUBUN: 'ORDER', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '주문연월', field: 'ORDYM', width: 100 }, { title: '순번', field: 'ORDNO', width: 80 }, { title: '거래처', field: 'CUSTNM', width: 200 }],
      onConfirm: (data: any) => { searchData.ORDYM = data.ORDYM; searchData.ORDNO = data.ORDNO }
    })
    modalVisible.value = true
  }
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v

onMounted(() => {
  fetchLineOptions()
  nextTick(() => { initGrid() })
})
</script>

<style scoped>
.hpio210u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #0056b3 !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; border: 1px solid #dee2e6; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 8px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* 공정 목록 스타일 */
.process-list .list-group-item { cursor: pointer; font-size: 13px; border-left: 4px solid transparent; transition: all 0.2s; }
.process-list .list-group-item:hover { background-color: #f8f9fa; }
.process-list .list-group-item.active { background-color: #e7f1ff; color: #0056b3; border-left-color: #0056b3; font-weight: bold; }

/* Tabulator 체크박스 정중앙 */
:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; }
</style>
