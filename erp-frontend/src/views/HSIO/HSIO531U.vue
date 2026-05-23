<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio531u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-ruled me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">외부매출전표 (HSIO531U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="issueSlip">전표발행</button>
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
                <th class="required">정산일자</th>
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

      <!-- 🅱️ 전표 발행 설정 (등록 영역) - 시각적 차별화 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 전표 발행 정보 (등록용)</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required bg-light-primary">전표일자</th>
                <td style="width: 250px;">
                  <input v-model="uiSLIPYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
                <th class="required bg-light-primary">발행부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="registerData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="registerData.DEPTNM" type="text" class="form-control" placeholder="발행부서 선택" @keyup.enter="openHelp('REG_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('REG_DEPT')"><i class="bi bi-search"></i></button>
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
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 정산 내역</span>
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
          <span class="me-4 small opacity-75">공급가 합계: <span class="fw-bold text-info ms-1">{{ formatNumber(sumData.SPYSUM) }}</span></span>
          <span class="me-4 small opacity-75">부가세 합계: <span class="fw-bold text-warning ms-1">{{ formatNumber(sumData.VATSUM) }}</span></span>
          <span class="fs-5 ms-2 fw-light">총 합계: <span class="fw-bold text-white ms-2">{{ formatNumber(sumData.TOTSUM) }}</span></span>
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
  IOYMDFR: initFRYMD, IOYMDTO: initYMD, SALSEMP: '000'
})

const registerData = reactive({
  SLIPYMD: initYMD, DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  CLSYMD: '', SCLSYM: ''
})

const sumData = reactive({ SPYSUM: 0, VATSUM: 0, TOTSUM: 0 })

const uiIOYMDFR = computed({ get: () => formatDate(searchData.IOYMDFR, '-'), set: (v) => searchData.IOYMDFR = v.replace(/-/g, '') })
const uiIOYMDTO = computed({ get: () => formatDate(searchData.IOYMDTO, '-'), set: (v) => searchData.IOYMDTO = v.replace(/-/g, '') })
const uiSLIPYMD = computed({ get: () => formatDate(registerData.SLIPYMD, '-'), set: (v) => registerData.SLIPYMD = v.replace(/-/g, '') })

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
        title: "선택", field: "PROCYN", width: 50, hozAlign: "center",
        formatter: "tickCross", editor: true, cellEdited: () => updateTotals()
      },
      { title: "발행일", field: "JSANYMD", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue(), '-') },
      { title: "부서", field: "DEPTNM", width: 150 },
      { title: "거래처", field: "CUSTNM", minWidth: 200 },
      { title: "사업장", field: "UNITNM", width: 130 },
      { title: "유형", field: "VATTYPENM", width: 100 },
      { title: "공급가", field: "SPYAMT", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "부가세", field: "VATAMT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "합계", field: "JSANSUM", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" }
    ]
  })
}

const updateTotals = () => {
  if (!grid.value) return
  const data = grid.value.getData().filter((i: any) => i.PROCYN)
  activeItemCount.value = data.length
  sumData.SPYSUM = data.reduce((acc, cur) => acc + (Number(cur.SPYAMT) || 0), 0)
  sumData.VATSUM = data.reduce((acc, cur) => acc + (Number(cur.VATAMT) || 0), 0)
  sumData.TOTSUM = sumData.SPYSUM + sumData.VATSUM
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
    const res = await api.post('/api/hsio/HSIO_531U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, GUBUN: '200',
      IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO,
      DEPTCD: searchData.DEPTCD, SALSEMP: searchData.SALSEMP === '000' ? '' : searchData.SALSEMP
    })
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => ({
        ...i,
        PROCYN: false,
        JSANSUM: Number(i.SPYAMT || 0) + Number(i.VATAMT || 0)
      })))
      allSelected.value = false
      updateTotals()
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function issueSlip() {
  const selected = grid.value?.getData().filter((i: any) => i.PROCYN)
  if (!selected || selected.length === 0) return vAlertError('전표발행 대상을 선택하시기 바랍니다.')

  // 마감 체크
  if (registerData.SLIPYMD <= registerData.CLSYMD) return vAlertError('회계정보가 마감이 되었습니다. 해당 전표일자로 작업할 수 없습니다.')
  if (registerData.SCLSYM >= registerData.SLIPYMD.substring(0, 6)) return vAlertError('영업정보가 마감이 되었습니다. 해당 전표일자로 작업할 수 없습니다.')

  // 데이터 검증
  for (const item of selected) {
    if (!item.CUSTCD) return vAlertError('거래처코드가 없는 항목이 있습니다.')
    if (!item.OUTCUSTCD) return vAlertError(`[${item.CUSTNM}] 더존거래처코드가 존재하지 않습니다.`)
    if (Number(item.JSANSUM) === 0) return vAlertError('합계금액이 0인 항목은 발행할 수 없습니다.')
  }

  if (!confirm('전표를 발행 하시겠습니까?')) return

  try {
    const business = `${registerData.SLIPYMD.substring(0, 4)}년 ${registerData.SLIPYMD.substring(4, 6)}월 매출 건`

    // 1. 전표 마스터 발행 (A0)
    // ASP 소스상 마스터 발행 시 대표 항목 정보를 넘기거나 프로시저에서 자동 처리함.
    // 여기서는 첫번째 항목 기준으로 마스터를 생성하고 루프 돌려 상세를 매핑하는 ASP 로직을 따름.
    const mRes = await api.post('/api/hsio/HSIO_531U_STR', {
      ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, GUBUN: '200',
      IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO,
      SLIPYMD: registerData.SLIPYMD, SLIPKIND: '040', DEPTCD: registerData.DEPTCD,
      BUSINESS: business, USERID: authStore.USERID
    })

    if (mRes.data?.length) {
      const newSlipNo = mRes.data[0].SLIPNO

      // 2. 전표 상세 매핑 (U0)
      for (const item of selected) {
        await api.post('/api/hsio/HSIO_531U_STR', {
          ...item,
          ACTKIND: 'U0', CMPYCD: authStore.CMPYCD, GUBUN: '200',
          IOYMDFR: searchData.IOYMDFR, IOYMDTO: searchData.IOYMDTO,
          SLIPYMD: registerData.SLIPYMD, SLIPNO: newSlipNo, SLIPKIND: '040',
          ISSU_DEPTCD: registerData.DEPTCD, BUSINESS: business,
          USERID: authStore.USERID
        })
      }

      vAlert('정상적으로 발행되었습니다.')
      // 인쇄 팝업 (필요 시 주석 해제)
      // window.open(`/api/report/HASL_SLIP_PRINT_OUT?SLIPGU=010&SLIPYMD=${registerData.SLIPYMD}&SLIPNO=${newSlipNo}&DEPTCD=${registerData.DEPTCD}`, 'print', 'width=700,height=650')
      search()
    }
  } catch (e) { vAlertError('전표 발행 실패') }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, IOYMDFR: initFRYMD, IOYMDTO: initYMD, SALSEMP: '000' })
  Object.assign(registerData, { SLIPYMD: initYMD, DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM })
  if (grid.value) grid.value.clearData()
  updateTotals()
}

// 4. 팝업 및 기초정보
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type.includes('DEPT')) {
    Object.assign(modalProps, {
      title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM', large: false,
      data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
      columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
      onConfirm: (data: any) => {
        if (type === 'SEARCH_DEPT') { searchData.DEPTCD = data.DEPTCD; searchData.DEPTNM = data.DEPTNM }
        else { registerData.DEPTCD = data.DEPTCD; registerData.DEPTNM = data.DEPTNM }
      }
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
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsio531u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.flex-shrink-0 { flex-shrink: 0 !important; }
.flex-grow-1 { flex-grow: 1 !important; min-height: 0 !important; }
.overflow-hidden { overflow: hidden !important; }
/* 🚀 입력 필드 글자 크기 및 높이 최적화 (HSBA070U 패턴) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}
.erp-table-full { width: 100%; border-collapse: collapse; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 4px 5px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 2px 4px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }

/* 🚀 팝업 가독성 표준 스타일 */
:deep(.modal-content) { background-color: #ffffff !important; }
:deep(.modal-content .tabulator) { background-color: #ffffff !important; color: #000000 !important; border: 1px solid #dee2e6 !important; }
:deep(.modal-content .tabulator-cell) { color: #000000 !important; font-size: 13px !important; padding: 8px !important; }

</style>
