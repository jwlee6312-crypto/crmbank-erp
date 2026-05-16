<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio541u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-x me-2 text-danger" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매출전표취소 (HSIO541U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-danger" @click="deleteSlips">전표삭제</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-search me-1"></i> 조회 조건</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">판매부서</th>
                <td style="width: 250px;">
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="searchData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('SEARCH_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('SEARCH_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">발행일자</th>
                <td style="width: 300px;">
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiIOYMDFR" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiIOYMDTO" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <td></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 전표 발행 내역</span>
          <div class="d-flex gap-2 align-items-center">
            <span class="small text-muted">전체선택</span>
            <input type="checkbox" v-model="allSelected" @change="toggleAllSelection" class="form-check-input" />
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 전송완료, 영업마감, 회계마감된 전표는 취소할 수 없습니다.
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
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  IOYMDFR: initFRYMD, IOYMDTO: initYMD
})

const closingInfo = reactive({ CLSYMD: '', SCLSYM: '' })
const autoSlip = ref('N')

const uiIOYMDFR = computed({ get: () => formatDate(searchData.IOYMDFR, '-'), set: (v) => searchData.IOYMDFR = v.replace(/-/g, '') })
const uiIOYMDTO = computed({ get: () => formatDate(searchData.IOYMDTO, '-'), set: (v) => searchData.IOYMDTO = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)
const allSelected = ref(false)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "조회된 내역이 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "선택", field: "selected", width: 50, hozAlign: "center",
        formatter: "tickCross", editor: true,
        cellEdited: (cell) => {
            if (cell.getValue()) {
                const canCancel = checkCanCancel(cell.getData())
                if (canCancel !== true) {
                    vAlertError(canCancel)
                    cell.setValue(false)
                }
            }
            updateTotals()
        }
      },
      { title: "전표번호", field: "SLIP_FULL", width: 160, hozAlign: "center", cssClass: "fw-bold" },
      {
        title: "더존전송", field: "CFMYN", width: 100, hozAlign: "center",
        formatter: (c) => c.getValue() === 'Y' ? '<span class="badge bg-success">전송완료</span>' : '<span class="badge bg-secondary">미전송</span>'
      },
      { title: "발행부서", field: "DEPTNM", width: 150 },
      { title: "거래처", field: "CUSTNM", minWidth: 200 },
      { title: "유형", field: "VATTYPENM", width: 100 },
      { title: "공급가", field: "SPYAMT", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "부가세", field: "VATAMT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "합계", field: "JSANSUM", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary" }
    ]
  })
}

const checkCanCancel = (row: any) => {
    if (row.CFMYN === 'Y') return "전송완료된 전표입니다. 취소할 수 없습니다."
    if (row.JSANYMD.substring(0, 6) < closingInfo.SCLSYM) return "영업정보가 마감된 자료입니다. 취소할 수 없습니다."
    if (row.JSANYMD <= closingInfo.CLSYMD) return "회계정보가 마감된 자료입니다. 취소할 수 없습니다."
    return true
}

const updateTotals = () => {
  if (!grid.value) return
  const data = grid.value.getData().filter((i: any) => i.selected)
  activeItemCount.value = data.length
}

const toggleAllSelection = () => {
  if (!grid.value) return
  const data = grid.value.getData()
  grid.value.updateData(data.map(i => {
      const can = checkCanCancel(i) === true
      return { ...i, selected: allSelected.value && can }
  }))
  updateTotals()
}

// 3. 기능 구현
async function search() {
  if (!searchData.DEPTCD) return vAlertError('판매부서를 선택하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_541U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, GUBUN: '200',
      IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO,
      DEPTCD: searchData.DEPTCD
    })
    if (grid.value) {
      // Grouping logic from ASP: only show checkbox/link for unique SLIPYMD + SLIPNO
      let lastSlip = ""
      const mappedData = res.data.map((i: any) => {
          const slipKey = (i.SLIPYMD || '') + (i.SLIPNO || '')
          const isUnique = slipKey !== lastSlip && i.SLIPYMD > '00000000'
          lastSlip = slipKey
          return {
              ...i,
              selected: false,
              isUnique: isUnique,
              SLIP_FULL: i.SLIPYMD > '00000000' ? `${formatDate(i.SLIPYMD, '-')}-${i.SLIPNO}` : '',
              JSANSUM: Number(i.SPYAMT || 0) + Number(i.VATAMT || 0)
          }
      })
      grid.value.setData(mappedData)
      allSelected.value = false
      updateTotals()
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function deleteSlips() {
  const selected = grid.value?.getData().filter((i: any) => i.selected)
  if (!selected || selected.length === 0) return vAlertError('전표발행 대상을 선택하시기 바랍니다.')

  if (!confirm('선택한 전표를 삭제하시겠습니까?')) return

  try {
    for (const item of selected) {
      // ASP logic: If autoSlip is Y, might need special handling.
      // Procedure HSIO_541U_STR typically handles internal state.
      await api.post('/api/hsio/HSIO_541U_STR', {
        ACTKIND: 'D',
        CMPYCD: authStore.CMPYCD,
        GUBUN: '200',
        IOYMDFR: searchData.IOYMDFR,
        IOYMDTO: searchData.IOYMDTO,
        DEPTCD: searchData.DEPTCD,
        UDEPTCD: item.DEPTCD,
        SLIPYMD: item.SLIPYMD,
        SLIPNO: item.SLIPNO,
        AUTOSLIP: autoSlip.value,
        USERID: authStore.USERID
      })
    }
    vAlert('정상적으로 작업이 되었습니다.')
    search()
  } catch (e) { vAlertError('전표 삭제 중 오류 발생') }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, IOYMDFR: initFRYMD, IOYMDTO: initYMD })
  if (grid.value) grid.value.clearData()
  updateTotals()
}

// 4. 팝업 및 기초정보
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'SEARCH_DEPT') {
    Object.assign(modalProps, {
      title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM', large: false,
      data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
      columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
      onConfirm: (data: any) => { searchData.DEPTCD = data.DEPTCD; searchData.DEPTNM = data.DEPTNM }
    })
    modalVisible.value = true
  }
}

const formatDate = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      closingInfo.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(r.data[0])[1]).trim()
    }
  })
  api.get('/api/ha00/HA00_010S_STR', { params: { GUBUN: 'P1', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) autoSlip.value = r.data[0].SLIPYN || 'N'
  })
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsio541u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
