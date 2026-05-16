<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpba210u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-diagram-3-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">표준 BOM (HPBA210U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchProcesses">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="saveData" :disabled="!selectedProg.PROGCD">
          <i class="bi bi-save"></i> 저장
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
              <col>
            </colgroup>
            <tbody>
              <tr>
                <th class="required">생산라인</th>
                <td>
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 150px;" @change="onLineChange">
                    <option v-for="opt in lineOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
                  </select>
                </td>
                <th class="required">재고자산</th>
                <td>
                  <select v-model="searchData.ASTKIND" class="form-select form-select-sm" style="width: 120px;" @change="onLineChange">
                    <option value="200">제품</option>
                    <option value="210">반제품</option>
                  </select>
                </td>
                <th class="required">제품코드</th>
                <td colspan="2">
                  <div class="input-group input-group-sm" style="width: 450px;">
                    <input v-model="searchData.ITEMCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="searchData.ITEMNM" type="text" class="form-control" placeholder="제품 선택" @keyup.enter="openHelp('ITEM')" />
                    <input v-model="searchData.ITSIZE" type="text" class="form-control bg-light" style="max-width: 100px;" readonly />
                    <input v-model="searchData.UNIT" type="text" class="form-control bg-light text-center" style="max-width: 50px;" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('ITEM')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 영역 (좌: 공정목록, 우: 자재그리드) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 공정 목록 -->
        <div class="card border-0 shadow-sm d-flex flex-column" style="width: 250px; border-radius: 8px;">
          <div class="card-header bg-light py-2 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-gear-wide-connected me-1 text-primary"></i> 생산 공정 리스트
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-auto">
            <ul class="list-group list-group-flush process-list">
              <li v-if="processList.length === 0" class="list-group-item text-center py-5 text-muted small">제품을 먼저 조회하세요.</li>
              <li
                v-for="prog in processList"
                :key="prog.PROGCD"
                class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3"
                :class="{ active: selectedProg.PROGCD === prog.PROGCD }"
                @click="onProcessSelect(prog)"
              >
                <div>
                    <div class="fw-bold">{{ prog.PROGNM }}</div>
                    <div class="small opacity-75">{{ prog.PROGCD }}</div>
                </div>
                <i class="bi bi-chevron-right"></i>
              </li>
            </ul>
          </div>
        </div>

        <!-- 우측: 자재 상세 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark">
                <i class="bi bi-box-seam me-1 text-primary"></i> 투입 자재 상세 (BOM)
                <span v-if="selectedProg.PROGNM" class="badge bg-info ms-2 px-3">{{ selectedProg.PROGNM }}</span>
              </span>
              <div class="btn-group gap-1">
                <button class="btn btn-sm btn-outline-primary px-3" style="height: 28px; font-size: 12px; font-weight: 600;" @click="addRow">
                  <i class="bi bi-plus-circle me-1"></i> 행추가
                </button>
              </div>
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
        <div class="col-md-4 small">자재항목: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 공정별로 투입되는 원자재와 소요량(BOM)을 관리하는 화면입니다.
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
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

// 1. 상태 관리
const searchData = reactive({
  LINECD: '010',
  ASTKIND: '200',
  ITEMCD: '',
  ITEMNM: '',
  ITSIZE: '',
  UNIT: ''
})

const lineOptions = ref<any[]>([])
const processList = ref<any[]>([])
const selectedProg = reactive({ PROGCD: '', PROGNM: '' })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 기초 코드 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data
  } catch (e) {}
}

const onLineChange = () => {
    processList.value = []
    selectedProg.PROGCD = ''
    grid?.clearData()
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "공정을 선택하세요.",
      selectable: true,
      columns: [
        { title: "상태", field: "UPKIND", width: 60, hozAlign: "center", vertAlign: "middle", cssClass: "text-danger fw-bold" },
        { title: "코드", field: "MITEMCD", width: 100, hozAlign: "center" },
        {
          title: "투입자재명", field: "MITEMNM", minWidth: 200,
          cellClick: (e, cell) => openHelp('GRID_MAT', cell)
        },
        { title: "규격", field: "MITSIZE", width: 150 },
        { title: "단위", field: "MUNIT", width: 70, hozAlign: "center" },
        { title: "소요량", field: "INQTY", width: 100, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 4 }, cssClass: "bg-light-yellow fw-bold" },
        { title: "LOSS율(%)", field: "LOSRATE", width: 100, hozAlign: "right", editor: "number" },
        {
          title: "이전공정", field: "BPROGNM", width: 150,
          cellClick: (e, cell) => openHelp('GRID_PROG', cell)
        },
        { title: "사용", field: "USEYN", width: 60, hozAlign: "center", formatter: "tickCross", editor: true },
        { title: "삭제", width: 60, hozAlign: "center", formatter: "buttonCross", cellClick: (e, cell) => cell.getRow().delete() }
      ],
    })
    grid.on("cellEdited", (cell) => {
        const row = cell.getRow()
        if (row.getData().UPKIND !== 'A') row.update({ UPKIND: 'U' })
    })
  }
}

// 3. 비즈니스 로직
const fetchProcesses = async () => {
  if (!searchData.ITEMCD) return vAlertError('제품을 먼저 선택하세요.')
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', {
        params: { GUBUN: 'G1', CMPYCD: authStore.CMPYCD, LINECD: searchData.LINECD, ITEMCD: searchData.ITEMCD }
    })
    processList.value = res.data
    selectedProg.PROGCD = ''
    grid?.clearData()
    vAlert('공정 목록이 조회되었습니다.')
  } catch (e) { vAlertError('공정 조회 실패') }
}

const onProcessSelect = (prog: any) => {
    selectedProg.PROGCD = prog.PROGCD
    selectedProg.PROGNM = prog.PROGNM
    fetchBomDetails()
}

const fetchBomDetails = async () => {
  try {
    const res = await api.post('/api/hpba/HPBA_210U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      ITEMCD: searchData.ITEMCD,
      LINECD: searchData.LINECD,
      PROGCD: selectedProg.PROGCD
    })
    grid?.setData(res.data)
    itemCount.value = res.data.length
  } catch (e) { vAlertError('BOM 상세 조회 실패') }
}

const saveData = async () => {
  const data = grid?.getData() || []
  if (data.length === 0) return vAlertError('저장할 자재 항목이 없습니다.')

  if (!confirm('현재 공정의 BOM 설정을 저장하시겠습니까?')) return

  try {
    for (const row of data) {
      const actkind = row.UPKIND === 'A' ? 'A0' : 'U0'
      await api.post('/api/hpba/HPBA_210U_STR', {
        ACTKIND: actkind,
        CMPYCD: authStore.CMPYCD,
        USERID: authStore.USERID,
        ITEMCD: searchData.ITEMCD,
        LINECD: searchData.LINECD,
        PROGCD: selectedProg.PROGCD,
        MITEMCD: row.MITEMCD,
        MITSIZE: row.MITSIZE,
        MUNIT: row.MUNIT,
        INQTY: row.INQTY || 0,
        LOSRATE: row.LOSRATE || 0,
        BEFPROG: row.BEFPROG || '',
        USEYN: (row.USEYN === 'Y' || row.USEYN === true) ? 'Y' : 'N',
        CALGBN: '', INGBN: '', JODGBN: '', FRYMD: '', TOYMD: ''
      })
    }
    vAlert('정상적으로 저장되었습니다.')
    fetchBomDetails()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const addRow = () => {
    if (!selectedProg.PROGCD) return vAlertError('공정을 먼저 선택하세요.')
    grid?.addRow({ UPKIND: 'A', MITEMCD: '', MITEMNM: '', INQTY: 0, LOSRATE: 0, USEYN: 'Y' }, true)
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { LINECD: '010', ASTKIND: '200' })
  processList.value = []
  selectedProg.PROGCD = ''
  grid?.clearData()
  itemCount.value = 0
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell?: any) {
  let config: any = {}
  if (type === 'ITEM') {
    config = {
        title: '제품 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM',
        data: { GUBUN: 'I1', CMPYCD: authStore.CMPYCD, SELGBN: searchData.ASTKIND, LINECD: searchData.LINECD },
        columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '제품명', field: 'ITEMNM', width: 250 }, { title: '규격', field: 'ITSIZE', width: 120 }],
        onConfirm: (data: any) => {
            searchData.ITEMCD = data.ITEMCD; searchData.ITEMNM = data.ITEMNM;
            searchData.ITSIZE = data.ITSIZE; searchData.UNIT = data.UNIT;
            fetchProcesses();
        }
    }
  } else if (type === 'GRID_MAT') {
    config = {
        title: '자재 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM',
        data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD },
        columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '자재명', field: 'ITEMNM', width: 250 }],
        onConfirm: (data: any) => { cell.getRow().update({ MITEMCD: data.ITEMCD, MITEMNM: data.ITEMNM, MITSIZE: data.ITSIZE, MUNIT: data.UNIT }) }
    }
  } else if (type === 'GRID_PROG') {
    config = {
        title: '이전공정 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM',
        data: { GUBUN: 'G0', LINECD: searchData.LINECD, CMPYCD: authStore.CMPYCD },
        columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '공정명', field: 'CDNM', width: 150 }],
        onConfirm: (data: any) => { cell.getRow().update({ BEFPROG: data.CODE, BPROGNM: data.CDNM }) }
    }
  }
  Object.assign(modalProps, config); modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(() => {
  fetchLineOptions()
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hpba210u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }
.btn-save { background-color: #2ec4b6 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

.process-list .list-group-item { cursor: pointer; border-left: 4px solid transparent; transition: all 0.2s; }
.process-list .list-group-item:hover { background-color: #f8f9fa; }
.process-list .list-group-item.active { background-color: #eef2ff; color: #4361ee; border-left-color: #4361ee; }

:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }

.bg-light-yellow { background-color: #fffde7 !important; }
</style>
