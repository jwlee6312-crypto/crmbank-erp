<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio325u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-post me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        입금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">외부 입금전표발행 (HSIO325U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="issueSlip">전표발행</button>
        <button class="btn-erp btn-danger" @click="deleteSlip">전표삭제</button>
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
                <th class="required">입금부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="searchData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">입금일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiIMYMDFR" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiIMYMDTO" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th>영업사원</th>
                <td>
                  <select v-model="searchData.SALSEMP" class="form-select form-select-sm" style="width: 150px;">
                    <option value="">전체</option>
                    <option v-for="opt in empOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 전표 발행 설정 (등록 영역) -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 전표 발행 설정 (등록 정보)</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required bg-light-primary">전표일자</th>
                <td>
                  <input v-model="uiSLIPYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
                <th class="required bg-light-primary">발행부서</th>
                <td colspan="3">
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="slipMaster.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="slipMaster.DEPTNM" type="text" class="form-control" placeholder="발행부서 선택" @keyup.enter="openHelp('SLIP_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('SLIP_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 입금 내역</span>
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
        <div class="col-md-9 text-end">
          <span class="fs-5 ms-2 fw-light">선택 입금합계: <span class="fw-bold text-white ms-2">{{ formatNumber(sumTotal) }}</span></span>
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
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
const initYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initFRYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 상태 관리
const searchData = reactive({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  IMYMDFR: initFRYMD, IMYMDTO: initYMD, SALSEMP: ''
})

const slipMaster = reactive({
  SLIPYMD: initYMD, DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  CLSYMD: '', SCLSYM: ''
})

const uiIMYMDFR = computed({ get: () => formatDate(searchData.IMYMDFR, '-'), set: (v) => searchData.IMYMDFR = v.replace(/-/g, '') })
const uiIMYMDTO = computed({ get: () => formatDate(searchData.IMYMDTO, '-'), set: (v) => searchData.IMYMDTO = v.replace(/-/g, '') })
const uiSLIPYMD = computed({ get: () => formatDate(slipMaster.SLIPYMD, '-'), set: (v) => slipMaster.SLIPYMD = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)
const sumTotal = ref(0)
const allSelected = ref(false)
const empOptions = ref<any[]>([])

// 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "조회된 내역이 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "선택", field: "selected", width: 50, hozAlign: "center",
        formatter: "tickCross", editor: true, cellEdited: () => updateTotals()
      },
      { title: "입금번호", field: "IM_FULL", width: 140, hozAlign: "center" },
      { title: "입금일자", field: "IMYMD", width: 110, hozAlign: "center", formatter: (c) => formatDate(c.getValue(), '-') },
      { title: "입금부서", field: "DEPTNM", width: 150 },
      { title: "입금금액", field: "IMAMT", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "거래처명", field: "CUSTNM", minWidth: 200 },
      {
        title: "전송여부", field: "COFMYN", width: 100, hozAlign: "center",
        formatter: (c) => c.getValue() === 'Y' ? '<span class="badge bg-success">전송완료</span>' : '<span class="badge bg-secondary">전송미결</span>'
      },
      { title: "전표번호", field: "SLIP_FULL", width: 160, hozAlign: "center", cssClass: "fw-bold text-primary" }
    ]
  })
}

const updateTotals = () => {
  if (!grid.value) return
  const data = grid.value.getData().filter((i: any) => i.selected)
  activeItemCount.value = data.length
  sumTotal.value = data.reduce((acc, cur) => acc + (Number(cur.IMAMT) || 0), 0)
}

const toggleAllSelection = () => {
  if (!grid.value) return
  const data = grid.value.getData()
  grid.value.updateData(data.map(i => ({ ...i, selected: allSelected.value })))
  updateTotals()
}

// 기능 구현
async function search() {
  if (!searchData.DEPTCD) return vAlertError('입금부서를 선택하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_325U_STR', {
      ACTKIND: 'S', CMPYCD: authStore.CMPYCD,
      DEPTCD: searchData.DEPTCD, IMYMDFR: searchData.IMYMDFR,
      IMYMDTO: searchData.IMYMDTO, SALSEMP: searchData.SALSEMP
    })
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => ({
        ...i, selected: false,
        IM_FULL: `${i.IMYM}-${i.IMNO}`,
        SLIP_FULL: i.SLIPNO > '000' ? `${formatDate(i.SLIPYMD, '-')}-${i.SLIPNO}` : ''
      })))
      allSelected.value = false
      updateTotals()
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function issueSlip() {
  const selected = grid.value?.getData().filter((i: any) => i.selected && i.COFMYN !== 'Y')
  if (!selected || selected.length === 0) return vAlertError('발행할 대상을 선택하세요. (이미 완료된 항목 제외)')

  if (slipMaster.SLIPYMD <= slipMaster.CLSYMD) return vAlertError('회계 마감된 일자입니다.')
  if (confirm('선택한 내역에 대해 전표를 발행하시겠습니까?')) {
    try {
      for (const item of selected) {
        await api.post('/api/hsio/HSIO_325U_STR', {
          ACTKIND: 'A', CMPYCD: authStore.CMPYCD, DEPTCD: slipMaster.DEPTCD,
          IMYM: item.IMYM, IMNO: item.IMNO, SLIPYMD: slipMaster.SLIPYMD,
          SLIPKIND: '040', IMAMT: item.IMAMT, CUSTCD: item.CUSTCD, CUSTNM: item.CUSTNM,
          USERID: authStore.USERID
        })
      }
      vAlert('전표 발행 완료'); search()
    } catch (e) { vAlertError('전표 발행 중 오류 발생') }
  }
}

async function deleteSlip() {
  const selected = grid.value?.getData().filter((i: any) => i.selected && i.SLIPNO > '000')
  if (!selected || selected.length === 0) return vAlertError('삭제할 전표를 선택하세요.')

  if (confirm('선택한 전표를 삭제하시겠습니까?')) {
    try {
      for (const item of selected) {
        await api.post('/api/hsio/HSIO_325U_STR', {
          ACTKIND: 'D', CMPYCD: authStore.CMPYCD, DEPTCD: item.DEPTCD,
          IMYM: item.IMYM, IMNO: item.IMNO, SLIPYMD: item.SLIPYMD, SLIPNO: item.SLIPNO
        })
      }
      vAlert('전표 삭제 완료'); search()
    } catch (e) { vAlertError('전표 삭제 중 오류 발생') }
  }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, IMYMDFR: initFRYMD, IMYMDTO: initYMD })
  if (grid.value) grid.value.clearData()
  updateTotals()
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'DEPT' || type === 'SLIP_DEPT') {
    Object.assign(modalProps, {
      title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM', large: false,
      data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
      columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
      onConfirm: (data: any) => {
        if (type === 'DEPT') { searchData.DEPTCD = data.DEPTCD; searchData.DEPTNM = data.DEPTNM }
        else { slipMaster.DEPTCD = data.DEPTCD; slipMaster.DEPTNM = data.DEPTNM }
      }
    }); modalVisible.value = true
  }
}

// 유틸리티
const formatDate = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      slipMaster.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      slipMaster.SCLSYM = String(Object.values(r.data[0])[1]).trim()
    }
  })
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'SD', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 999 } }).then(r => {
    if (r.data) empOptions.value = r.data.map((i: any) => ({ CODECD: i.USERID, CODENM: i.USERNM }))
  })
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsio325u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }
.bg-light-primary { background-color: #f0f7ff !important; color: #005a9f !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
.btn-xs { padding: 2px 6px; font-size: 11px; border-radius: 3px; }
</style>
