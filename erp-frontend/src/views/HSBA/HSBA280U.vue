<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba280u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-globe2 me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">수출 거래처관리 (HSBA280U)</span>
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
                <th style="width: 120px;">수출입구분</th>
                <td>
                  <select v-model="searchData.IOGBN" class="form-select form-select-sm" style="width: 150px;" @change="search">
                    <option value="200">수출 거래처</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 마스터 정보 입력 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 거래처 정보 입력</span>
          <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark ms-2">수정 모드</span>
          <span v-else class="badge bg-primary ms-2">신규 등록</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">수 출 입</th>
                <td style="width: 20%;">
                  <select v-model="masterData.IOGBN" class="form-select form-select-sm">
                    <option value="200">수출 거래처</option>
                  </select>
                </td>
                <th class="required">거 래 처</th>
                <td style="width: 30%;">
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">국가코드</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.NACD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.NANM" type="text" class="form-control" placeholder="국가 선택" @keyup.enter="openHelp('NA')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('NA')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required">선 적 항</th>
                <td>
                  <select v-model="masterData.SHIPPORT" class="form-select form-select-sm">
                    <option value="">-- 선택 --</option>
                    <option v-for="opt in shipPortOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">도 착 항</th>
                <td>
                  <select v-model="masterData.ARVPORT" class="form-select form-select-sm">
                    <option value="">-- 선택 --</option>
                    <option v-for="opt in arvPortOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">통화단위</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.CURRCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.CURRNM" type="text" class="form-control" placeholder="통화 선택" @keyup.enter="openHelp('CURR')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CURR')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required">결제조건</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.PAYCOND" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.PAYCONDNM" type="text" class="form-control" placeholder="결제조건 선택" @keyup.enter="openHelp('PAY')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('PAY')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">가격조건</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.PRICOND" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.PRICONDNM" type="text" class="form-control" placeholder="가격조건 선택" @keyup.enter="openHelp('PRI')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('PRI')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>사용여부</th>
                <td>
                  <div class="form-check form-check-inline mb-0">
                    <input v-model="masterData.USEYN" class="form-check-input" type="checkbox" id="useYn280" true-value="Y" false-value="N">
                    <label class="form-check-label small" for="useYn280">사용</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 수출 거래처 목록</span>
          <div class="small text-muted">행 클릭 시 상세 정보를 수정할 수 있습니다.</div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-12 small">
          조회 건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
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
  IOGBN: '200'
})

const masterData = reactive<any>({
  ACTKIND: 'A0',
  CMPYCD: authStore.CMPYCD,
  IOGBN: '200',
  CUSTCD: '',
  CUSTNM: '',
  NACD: '',
  NANM: '',
  SHIPPORT: '',
  ARVPORT: '',
  CURRCD: '',
  CURRNM: '',
  PAYCOND: '',
  PAYCONDNM: '',
  PRICOND: '',
  PRICONDNM: '',
  USEYN: 'Y'
})

const shipPortOptions = ref<any[]>([])
const arvPortOptions = ref<any[]>([])

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      { title: "수출거래처", field: "CUSTNM", minWidth: 200, cssClass: "fw-bold" },
      { title: "국가", field: "NANM", width: 100, hozAlign: "center" },
      { title: "선적항", field: "SHIPPORTNM", width: 120, hozAlign: "center" },
      { title: "도착항", field: "ARVPORTNM", width: 120, hozAlign: "center" },
      { title: "통화단위", field: "CURRNM", width: 100, hozAlign: "center" },
      { title: "결제조건", field: "PAYCONDNM", width: 120, hozAlign: "center" },
      { title: "가격조건", field: "PRICONDNM", width: 120, hozAlign: "center" },
      {
        title: "사용", field: "USEYN", width: 60, hozAlign: "center",
        formatter: (c) => c.getValue() === 'Y' ? '<i class="bi bi-check-lg text-success"></i>' : '<i class="bi bi-x-lg text-danger"></i>'
      }
    ]
  })

  grid.value.on("rowClick", (e, row) => {
    const data = row.getData()
    Object.assign(masterData, data)
    masterData.ACTKIND = 'U0'
  })
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    const resShip = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E2', CMPYCD: authStore.CMPYCD, GBNCD: '318' } })
    shipPortOptions.value = resShip.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))

    const resArv = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E2', CMPYCD: authStore.CMPYCD, GBNCD: '319' } })
    arvPortOptions.value = resArv.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
  } catch (e) { console.error('옵션 로드 실패') }
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_280U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      IOGBN: searchData.IOGBN
    })
    if (grid.value) {
      grid.value.setData(res.data)
      activeItemCount.value = res.data.length
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.CUSTCD || !masterData.NACD || !masterData.SHIPPORT || !masterData.ARVPORT || !masterData.CURRCD || !masterData.PAYCOND || !masterData.PRICOND) {
    return vAlertError('필수 입력 항목이 누락되었습니다.')
  }

  const actionText = masterData.ACTKIND === 'A0' ? '등록' : '수정'
  if (!confirm(`거래처 정보를 ${actionText}하시겠습니까?`)) return

  try {
    const res = await api.post('/api/hsba/HSBA_280U_STR', {
      ...masterData,
      USERID: authStore.USERID
    })
    vAlert('정상적으로 저장되었습니다.')
    initialize()
    search()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  const currentIo = searchData.IOGBN
  resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'A0',
    CMPYCD: authStore.CMPYCD,
    IOGBN: currentIo,
    USEYN: 'Y'
  })
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let config: any = {}
  if (type === 'CUST') {
    config = { title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', data: { GUBUN: 'C4' }, field: 'CUSTNM', columns: [{ title: '코드', field: 'CUSTCD', width: 80 }, { title: '거래처명', field: 'CUSTNM', width: 180 }] }
  } else if (type === 'NA') {
    config = { title: '국가 선택', path: '/api/ha00/HA00_00P_STR', data: { GUBUN: 'E2', CODEGBN: '305' }, field: 'CDNM', columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '국가명', field: 'CDNM', width: 180 }] }
  } else if (type === 'CURR') {
    config = { title: '통화 선택', path: '/api/ha00/HA00_00P_STR', data: { GUBUN: 'E2', CODEGBN: '310' }, field: 'CDNM', columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '통화명', field: 'CDNM', width: 180 }] }
  } else if (type === 'PAY') {
    config = { title: '결제조건 선택', path: '/api/ha00/HA00_00P_STR', data: { GUBUN: 'E2', CODEGBN: '312' }, field: 'CDNM', columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '결제조건', field: 'CDNM', width: 180 }] }
  } else if (type === 'PRI') {
    config = { title: '가격조건 선택', path: '/api/ha00/HA00_00P_STR', data: { GUBUN: 'E2', CODEGBN: '314' }, field: 'CDNM', columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '가격조건', field: 'CDNM', width: 180 }] }
  }

  if (!config.path) return

  Object.assign(modalProps, {
    title: config.title, path: config.path, defaultField: config.field,
    data: { ...config.data, CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
    columns: config.columns,
    onConfirm: (data: any) => {
      if (type === 'CUST') { masterData.CUSTCD = data.CUSTCD; masterData.CUSTNM = data.CUSTNM }
      else if (type === 'NA') { masterData.NACD = data.CODE; masterData.NANM = data.CDNM }
      else if (type === 'CURR') { masterData.CURRCD = data.CODE; masterData.CURRNM = data.CDNM }
      else if (type === 'PAY') { masterData.PAYCOND = data.CODE; masterData.PAYCONDNM = data.CDNM }
      else if (type === 'PRI') { masterData.PRICOND = data.CODE; masterData.PRICONDNM = data.CDNM }
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
.hsba280u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px 15px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 8px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
