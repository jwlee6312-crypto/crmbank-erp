<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio590u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매출정산(일괄) (HSIO590U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
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
                <th class="required">판매일자</th>
                <td style="width: 300px;">
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiIOYMDFR" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiIOYMDTO" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th>영업사원</th>
                <td>
                  <select v-model="searchData.SALSEMP" class="form-select form-select-sm" style="width: 150px;">
                    <option value="000">전체</option>
                    <option v-for="opt in empOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 발행 설정 (등록 영역) - 시각적 차별화 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 정산 발행 정보 (등록용)</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required bg-light-primary">사업장</th>
                <td style="width: 250px;">
                  <select v-model="registerData.TAXUNIT" class="form-select form-select-sm" style="width: 220px;">
                    <option v-for="opt in taxUnitOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required bg-light-primary">유형</th>
                <td style="width: 300px;">
                  <select v-model="registerData.VATTYPE" class="form-select form-select-sm" style="width: 150px;">
                    <option v-for="opt in vatTypeOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required bg-light-primary">발행일</th>
                <td>
                  <input v-model="uiPUBYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 정산 대상 내역</span>
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
  IOYMDFR: initFRYMD, IOYMDTO: initYMD, SALSEMP: authStore.USERID
})

const registerData = reactive({
  TAXUNIT: '100', VATTYPE: '010', PUBYMD: initYMD,
  SPLAMT: 0, VATAMT: 0, SUMAMT: 0,
  CLSYMD: '', SCLSYM: ''
})

const uiIOYMDFR = computed({ get: () => formatDate(searchData.IOYMDFR, '-'), set: (v) => searchData.IOYMDFR = v.replace(/-/g, '') })
const uiIOYMDTO = computed({ get: () => formatDate(searchData.IOYMDTO, '-'), set: (v) => searchData.IOYMDTO = v.replace(/-/g, '') })
const uiPUBYMD = computed({ get: () => formatDate(registerData.PUBYMD, '-'), set: (v) => registerData.PUBYMD = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)
const allSelected = ref(true)

const empOptions = ref<any[]>([])
const taxUnitOptions = ref<any[]>([])
const vatTypeOptions = ref<any[]>([])

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "조회된 내역이 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "선택", field: "PROCYN", width: 50, hozAlign: "center",
        formatter: "tickCross", editor: true, cellEdited: () => updateTotals()
      },
      { title: "거래처", field: "CUSTNM", minWidth: 200 },
      { title: "판매부서", field: "DEPTNM", width: 150 },
      { title: "수량", field: "IOQTY", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "공급가", field: "IOAMT", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "부가세", field: "IOVAT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "합계", field: "IOSUM", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" }
    ]
  })
}

const updateTotals = () => {
  if (!grid.value) return
  const data = grid.value.getData().filter((i: any) => i.PROCYN)
  activeItemCount.value = data.length
  registerData.SPLAMT = data.reduce((acc, cur) => acc + (Number(cur.IOAMT) || 0), 0)
  registerData.VATAMT = data.reduce((acc, cur) => acc + (Number(cur.IOVAT) || 0), 0)
  registerData.SUMAMT = registerData.SPLAMT + registerData.VATAMT
}

const toggleAllSelection = () => {
  if (!grid.value) return
  const data = grid.value.getData()
  grid.value.updateData(data.map(i => ({ ...i, PROCYN: allSelected.value })))
  updateTotals()
}

// 3. 기능 구현
async function search() {
  if (!searchData.DEPTCD) return vAlertError('판매부서를 선택하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_590U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, GUBUN: '200',
      IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO,
      DEPTCD: searchData.DEPTCD, SALSEMP: searchData.SALSEMP === '000' ? '' : searchData.SALSEMP
    })
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => {
        const ioQty = (Number(i.QTY) || 0) - (Number(i.JQTY) || 0)
        const ioAmt = (Number(i.AMT) || 0) - (Number(i.JAMT) || 0)
        const ioVat = (Number(i.VAT) || 0) - (Number(i.JVAT) || 0)
        return { ...i, PROCYN: true, IOQTY: ioQty, IOAMT: ioAmt, IOVAT: ioVat, IOSUM: ioAmt + ioVat }
      }))
      allSelected.value = true
      updateTotals()
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  const selected = grid.value?.getData().filter((i: any) => i.PROCYN)
  if (!selected || selected.length === 0) return vAlertError('정산할 자료를 선택해 주십시오.')

  if (registerData.PUBYMD <= registerData.CLSYMD) return vAlertError('회계 마감된 일자입니다.')
  if (confirm('정산 작업을 진행하시겠습니까?')) {
    try {
      for (const item of selected) {
        await api.post('/api/hsio/HSIO_590U_STR', {
          ACTKIND: 'U0', CMPYCD: authStore.CMPYCD, GUBUN: '200',
          IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO,
          DEPTCD: item.DEPTCD, CUSTCD: item.CUSTCD, SALSEMP: searchData.SALSEMP,
          TAXUNIT: registerData.TAXUNIT, VATTYPE: registerData.VATTYPE,
          JSANYMD: registerData.PUBYMD, IOAMT: item.IOAMT, IOVAT: item.IOVAT,
          USERID: authStore.USERID
        })
      }
      vAlert('정상으로 작업이 되었습니다.')
      search()
    } catch (e) { vAlertError('저장 중 오류 발생') }
  }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, IOYMDFR: initFRYMD, IOYMDTO: initYMD, SALSEMP: authStore.USERID })
  Object.assign(registerData, { TAXUNIT: '100', VATTYPE: '010', PUBYMD: initYMD, SPLAMT: 0, VATAMT: 0, SUMAMT: 0 })
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
      registerData.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      registerData.SCLSYM = String(Object.values(r.data[0])[1]).trim()
    }
  })
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'SD', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 999 } }).then(r => {
    if (r.data) empOptions.value = r.data.map((i: any) => ({ CODECD: i.USERID, CODENM: i.USERNM }))
  })
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'SA', CMPYCD: authStore.CMPYCD } }).then(r => taxUnitOptions.value = r.data.map((i: any) => ({ CODECD: i.CODE, CODENM: i.CDNM })))
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'E0', GBNCD: '130', CMPYCD: authStore.CMPYCD } }).then(r => vatTypeOptions.value = r.data.map((i: any) => ({ CODECD: i.CODE, CODENM: i.CDNM })))

  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsio590u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

/* 🚀 입력 필드 글자 크기 및 높이 최적화 (표준) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12px; padding: 6px 10px !important; color: #495057;
  white-space: nowrap;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }

:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>

