<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba701u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-link-45deg me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">외부회계연동 계정정보 등록 (HSBA701U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 데이터 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 계정 연동 설정 목록</span>
          <button class="btn btn-xs btn-primary fw-bold" @click="addRow">
            <i class="bi bi-plus-lg me-1"></i> 행추가
          </button>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-12 small">
          조회 건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건
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
const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)

const slipKindOptions = ref<any[]>([])
const assetKindOptions = ref<any[]>([])

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 내역이 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "선택", field: "PROCYN", width: 50, hozAlign: "center",
        formatter: "tickCross", editor: true
      },
      {
        title: "분개유형", field: "SLIPKIND", width: 200, editor: "list",
        editorParams: { values: slipKindOptions.value.reduce((acc, opt) => ({ ...acc, [opt.CODECD]: opt.CODENM }), {}) },
        formatter: (cell) => {
          const val = cell.getValue();
          const opt = slipKindOptions.value.find(o => o.CODECD === val);
          return opt ? opt.CODENM : val;
        }
      },
      {
        title: "재고자산", field: "ASTKIND", width: 200, editor: "list",
        editorParams: { values: assetKindOptions.value.reduce((acc, opt) => ({ ...acc, [opt.CODECD]: opt.CODENM }), {}) },
        formatter: (cell) => {
          const val = cell.getValue();
          const opt = assetKindOptions.value.find(o => o.CODECD === val);
          return opt ? opt.CODENM : val;
        }
      },
      {
        title: "차변계정코드", field: "ACCTCD1", width: 100, editor: "input",
        cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) openHelp('ACCT1', cell.getRow()) },
        formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>"
      },
      { title: "차변계정명", field: "ACCTNM1", width: 150, editor: "input" },
      {
        title: "대변계정코드", field: "ACCTCD2", width: 100, editor: "input",
        cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) openHelp('ACCT2', cell.getRow()) },
        formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>"
      },
      { title: "대변계정명", field: "ACCTNM2", width: 150, editor: "input" },
      {
        title: "부가세코드", field: "VACCTCD", width: 100, editor: "input",
        cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) openHelp('VACCT', cell.getRow()) },
        formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>"
      },
      { title: "부가세명", field: "VACCTNM", width: 150, editor: "input" },
      {
        title: "", width: 40, hozAlign: "center",
        formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>",
        cellClick: (e, c) => {
          c.getRow().delete();
          updateTotals();
        }
      }
    ]
  })
}

const updateTotals = () => {
  if (!grid.value) return
  activeItemCount.value = grid.value.getData().length
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    // 분개유형 (610)
    const resSlip = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'GB', CMPYCD: authStore.CMPYCD, GBNCD: '610' } })
    slipKindOptions.value = resSlip.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))

    // 재고자산 (100)
    const resAsset = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '100' } })
    assetKindOptions.value = resAsset.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
  } catch (e) { console.error('옵션 로드 실패') }
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_701U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD
    })
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => ({ ...i, PROCYN: false })))
      activeItemCount.value = res.data.length
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  const selectedRows = grid.value?.getData().filter((i: any) => i.PROCYN)
  if (!selectedRows || selectedRows.length === 0) {
    return vAlertError('저장할 대상을 선택해 주십시오.')
  }

  if (!confirm('선택한 항목들을 저장하시겠습니까?')) return

  try {
    for (const row of selectedRows) {
      const actKind = row.SEQ ? 'U0' : 'A0'
      await api.post('/api/hsba/HSBA_701U_STR', {
        ...row,
        ACTKIND: actKind,
        CMPYCD: authStore.CMPYCD,
        USERID: authStore.USERID
      })
    }
    vAlert('정상적으로 저장되었습니다.')
    search()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  if (grid.value) grid.value.clearData()
  activeItemCount.value = 0
  search()
}

function addRow() {
  if (grid.value) {
    grid.value.addRow({ PROCYN: true, SLIPKIND: '', ASTKIND: '', ACCTCD1: '', ACCTNM1: '', ACCTCD2: '', ACCTNM2: '', VACCTCD: '', VACCTNM: '' })
    updateTotals()
  }
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, row: any) {
  Object.assign(modalProps, {
    title: '계정코드 선택',
    path: '/api/comm/HELP_ACCTCD_LTD',
    defaultField: 'ACCTNM',
    data: { CMPYCD: authStore.CMPYCD },
    columns: [
      { title: '코드', field: 'ACCTCD', width: 100 },
      { title: '계정명', field: 'ACCTNM', minWidth: 200 }
    ],
    onConfirm: (data: any) => {
      if (type === 'ACCT1') row.update({ ACCTCD1: data.ACCTCD, ACCTNM1: data.ACCTNM })
      else if (type === 'ACCT2') row.update({ ACCTCD2: data.ACCTCD, ACCTNM2: data.ACCTNM })
      else if (type === 'VACCT') row.update({ VACCTCD: data.ACCTCD, VACCTNM: data.ACCTNM })
    }
  })
  modalVisible.value = true
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => {
    initGrid()
    search()
  })
})
</script>

<style scoped>
.hsba701u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px 15px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 8px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
.btn-xs { padding: 2px 6px; font-size: 11px; border-radius: 3px; }
</style>
