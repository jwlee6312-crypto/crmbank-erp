<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hspl110u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-graph-up-arrow me-2 text-primary" style="font-size: 18px;"></i>
        매출계획 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목그룹별판매계획 (HSPL110U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-outline-secondary" @click="print('Print')">인쇄</button>
        <button class="btn-erp btn-outline-success" @click="print('Excel')">엑셀</button>
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
                <th class="required" style="width: 100px;">연&nbsp;&nbsp;&nbsp;&nbsp;도</th>
                <td style="width: 150px;">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="searchData.YYYY" type="number" class="form-control form-control-sm text-center fw-bold" style="width: 80px;" />
                    <span class="small">년</span>
                  </div>
                </td>
                <th class="required">영업부서</th>
                <td style="width: 250px;">
                  <div class="input-group input-group-sm">
                    <input v-model="searchData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">영업사원</th>
                <td style="width: 220px;">
                  <div class="input-group input-group-sm">
                    <input v-model="searchData.USERID" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="searchData.USERNM" type="text" class="form-control" placeholder="사원 선택" @keyup.enter="openHelp('EMP')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('EMP')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <td></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 그룹별 월간 계획</span>
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
      <div class="row align-items-center text-center">
        <div class="col-md-2 small border-end border-secondary">총 그룹 수: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-10 text-end">
          <span class="fs-5 fw-light">연간 계획 총 합계: <span class="fw-bold text-white ms-2">{{ formatNumber(grandTotal) }}</span></span>
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

// 1. 상태 관리
const searchData = reactive({
  YYYY: now.getFullYear(),
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  USERID: authStore.USERID,
  USERNM: authStore.USERNM
})

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)
const grandTotal = ref(0)
const allSelected = ref(false)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "선택", field: "PROCYN", width: 40, hozAlign: "center",
        formatter: "tickCross", editor: true
      },
      { title: "구분", field: "ASTKINDNM", width: 80, hozAlign: "center" },
      { title: "대분류", field: "AGRPNM", width: 120 },
      { title: "중분류", field: "BGRPNM", width: 120 },
      {
        title: "합계", field: "PLANSUM", width: 90, hozAlign: "right",
        cssClass: "bg-light-blue fw-bold",
        formatter: (cell) => formatNumber(cell.getValue())
      },
      ...Array.from({ length: 12 }, (_, i) => {
        const month = String(i + 1).padStart(2, '0')
        return {
          title: `${month}월`,
          field: `MM${month}`,
          width: 80,
          hozAlign: "right",
          editor: "number",
          formatter: "money",
          formatterParams: { precision: 0 }
        }
      })
    ]
  })

  // 셀 수정 시 합계 및 전체 합계 재계산
  grid.value.on("cellEdited", (cell: any) => {
    const row = cell.getRow()
    const data = row.getData()
    if (!data.PROCYN) row.update({ PROCYN: true })

    // 행 합계 계산
    let rowSum = 0
    for(let i=1; i<=12; i++) {
        rowSum += Number(data[`MM${String(i).padStart(2, '0')}`]) || 0
    }
    row.update({ PLANSUM: rowSum })
    updateGrandTotal()
  })
}

const updateGrandTotal = () => {
  if (!grid.value) return
  const data = grid.value.getData()
  grandTotal.value = data.reduce((acc, cur) => acc + (Number(cur.PLANSUM) || 0), 0)
}

const toggleAllSelection = () => {
  if (!grid.value) return
  const data = grid.value.getData()
  grid.value.updateData(data.map(i => ({ ...i, PROCYN: allSelected.value })))
}

// 3. 기능 구현
async function search() {
  if (!searchData.DEPTCD || !searchData.USERID) return vAlertError('영업부서와 사원을 선택하세요.')
  try {
    const res = await api.post('/api/hspl/HSPL_110U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      YYYY: searchData.YYYY,
      DEPTCD: searchData.DEPTCD,
      USERID: searchData.USERID
    })
    if (grid.value) {
      const mapped = res.data.map((i: any) => {
          let rowSum = 0
          for(let m=1; m<=12; m++) rowSum += Number(i[`MM${String(m).padStart(2, '0')}`]) || 0
          return { ...i, PROCYN: false, PLANSUM: rowSum }
      })
      grid.value.setData(mapped)
      activeItemCount.value = mapped.length
      updateGrandTotal()
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  const selected = grid.value?.getData().filter((i: any) => i.PROCYN)
  if (!selected || selected.length === 0) return vAlertError('저장할 대상을 선택하세요.')

  if (!confirm('선택한 품목 그룹의 판매계획을 저장하시겠습니까?')) return

  try {
    for (const item of selected) {
      await api.post('/api/hspl/HSPL_110U_STR', {
        ...item,
        ACTKIND: 'A0',
        CMPYCD: authStore.CMPYCD,
        YYYY: searchData.YYYY,
        DEPTCD: searchData.DEPTCD,
        USERID: searchData.USERID,
        UPD_USER: authStore.USERID
      })
    }
    vAlert('정상적으로 저장되었습니다.')
    search()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, {
    YYYY: now.getFullYear(),
    DEPTCD: authStore.DEPTCD,
    DEPTNM: authStore.DEPTNM,
    USERID: authStore.USERID,
    USERNM: authStore.USERNM
  })
  if (grid.value) grid.value.clearData()
  grandTotal.value = 0
  activeItemCount.value = 0
}

function print(type: string) {
    vAlert(`${type} 기능은 준비 중입니다.`)
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'DEPT') {
    Object.assign(modalProps, {
      title: '영업부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM',
      data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
      onConfirm: (data: any) => { searchData.DEPTCD = data.DEPTCD; searchData.DEPTNM = data.DEPTNM }
    })
  } else if (type === 'EMP') {
    Object.assign(modalProps, {
      title: '영업사원 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'USERNM',
      data: { GUBUN: 'SD', CMPYCD: authStore.CMPYCD },
      columns: [{ title: 'ID', field: 'USERID', width: 100 }, { title: '사원명', field: 'USERNM', width: 150 }],
      onConfirm: (data: any) => { searchData.USERID = data.USERID; searchData.USERNM = data.USERNM }
    })
  }
  modalVisible.value = true
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(() => {
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hspl110u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.bg-light-blue { background-color: #e7f3ff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px 15px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 8px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>