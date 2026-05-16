<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba140u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-globe me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        수출입거래처관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">수출입거래처관리 (HSBA140U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 -->
      <div class="card border shadow-sm">
        <div class="card-body p-2 bg-light">
          <div class="d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small">수출입구분</span>
              <select v-model="searchParams.IOGBN" class="form-select form-select-sm" style="width: 150px;" @change="fetchList">
                <option value="100">수입 거래처</option>
                <option value="200">수출 거래처</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- 🅱️ 거래처 상세 정보 입력 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1"></i> 수출입 관리 정보</span>
          <span v-if="formData.ACTKIND === 'U0'" class="badge bg-warning text-dark">수정 모드</span>
          <span v-else class="badge bg-primary">신규 등록</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">수 출 입</th>
                <td>
                  <select v-model="formData.IOGBN" class="form-select form-select-sm">
                    <option value="100">수입 거래처</option>
                    <option value="200">수출 거래처</option>
                  </select>
                </td>
                <th class="required">거 래 처</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 250px;">
                    <input v-model="formData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="formData.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">국가코드</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="formData.NACD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="formData.NANM" type="text" class="form-control" placeholder="국가 선택" @keyup.enter="openHelp('NACD')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('NACD')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required">선 적 항</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="formData.SHIPPORT" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="formData.SHIPPORTNM" type="text" class="form-control" placeholder="선적항 선택" @keyup.enter="openHelp('SHIPPORT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('SHIPPORT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">도 착 항</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="formData.ARVPORT" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="formData.ARVPORTNM" type="text" class="form-control" placeholder="도착항 선택" @keyup.enter="openHelp('ARVPORT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('ARVPORT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">통화단위</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="formData.CURRCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="formData.CURRNM" type="text" class="form-control" placeholder="통화 선택" @keyup.enter="openHelp('CURRCD')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CURRCD')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required">결제조건</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="formData.PAYCOND" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="formData.PAYCONDNM" type="text" class="form-control" placeholder="결제조건 선택" @keyup.enter="openHelp('PAYCOND')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('PAYCOND')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">가격조건</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="formData.PRICOND" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="formData.PRICONDNM" type="text" class="form-control" placeholder="가격조건 선택" @keyup.enter="openHelp('PRICOND')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('PRICOND')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>삭제여부</th>
                <td>
                  <div class="form-check form-check-inline mb-0">
                    <input v-model="formData.USEYN" class="form-check-input" type="checkbox" id="delYn140" true-value="N" false-value="Y">
                    <label class="form-check-label small" for="delYn140">삭제</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 목록 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark">
          <i class="bi bi-list-ul me-1"></i> 수출입 거래처 내역
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-6 small">
          조회 건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건
        </div>
        <div class="col-md-6 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 목록 행을 클릭하면 상세 내용을 수정할 수 있습니다.
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
const searchParams = reactive({
  IOGBN: '100'
})

const formData = reactive<any>({
  ACTKIND: 'A0',
  IOGBN: '100',
  CUSTCD: '',
  CUSTNM: '',
  NACD: '',
  NANM: '',
  SHIPPORT: '',
  SHIPPORTNM: '',
  ARVPORT: '',
  ARVPORTNM: '',
  CURRCD: '',
  CURRNM: '',
  PAYCOND: '',
  PAYCONDNM: '',
  PRICOND: '',
  PRICONDNM: '',
  USEYN: 'Y'
})

const activeItemCount = ref(0)
const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      columns: [
        { title: "거래처", field: "CUSTNM", minWidth: 200, cssClass: "fw-bold" },
        { title: "국가", field: "NANM", width: 100, hozAlign: "center" },
        { title: "선적항", field: "SHIPPORTNM", width: 120, hozAlign: "center" },
        { title: "도착항", field: "ARVPORTNM", width: 120, hozAlign: "center" },
        { title: "통화단위", field: "CURRNM", width: 100, hozAlign: "center" },
        { title: "결제조건", field: "PAYCONDNM", width: 120, hozAlign: "center" },
        { title: "가격조건", field: "PRICONDNM", width: 120, hozAlign: "center" }
      ]
    })

    grid.on("rowClick", (e, row) => {
      const data = row.getData()
      Object.assign(formData, data)
      formData.ACTKIND = 'U0'
      formData.USEYN = 'Y' // 폼에 바인딩 시 삭제여부 체크박스는 해제 상태로
    })
  }
}

// 3. 기능 구현
async function fetchList() {
  try {
    const res = await api.post('/api/hsba/HSBA_140U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      IOGBN: searchParams.IOGBN
    })
    grid?.setData(res.data)
    activeItemCount.value = res.data.length
  } catch (e) { vAlertError('조회 중 오류 발생') }
}

async function save() {
  if (!formData.CUSTCD) return vAlertError('거래처를 선택하세요.')
  if (!formData.NACD) return vAlertError('국가 코드를 입력하세요.')
  if (!formData.SHIPPORT) return vAlertError('선적항을 입력하세요.')
  if (!formData.ARVPORT) return vAlertError('도착항을 입력하세요.')
  if (!formData.CURRCD) return vAlertError('통화단위를 입력하세요.')
  if (!formData.PAYCOND) return vAlertError('결제조건을 입력하세요.')
  if (!formData.PRICOND) return vAlertError('가격조건을 입력하세요.')

  if (!confirm('저장하시겠습니까?')) return

  try {
    const res = await api.post('/api/hsba/HSBA_140U_STR', {
      ...formData,
      CMPYCD: authStore.CMPYCD,
      USERID: authStore.USERID
    })

    if (res.data?.[0]?.ERRYN === 'Y') {
      vAlertError(res.data[0].MSG || '저장 중 오류 발생')
    } else {
      vAlert('성공적으로 저장되었습니다.')
      fetchList()
      initialize()
    }
  } catch (e) { vAlertError('서버 통신 오류') }
}

function initialize() {
  const currentGbn = searchParams.IOGBN
  resetForm(formData)
  Object.assign(formData, {
    ACTKIND: 'A0',
    IOGBN: currentGbn,
    USEYN: 'Y'
  })
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let config: any = {}

  switch(type) {
    case 'CUST':
      config = { title: '거래처 선택', gubun: 'C4', field: 'CUSTNM', columns: [{ title: '코드', field: 'CUSTCD', width: 80 }, { title: '거래처명', field: 'CUSTNM', width: 200 }] }
      break
    case 'NACD':
      config = { title: '국가 선택', gubun: 'E2', gbncd: '305', field: 'NANM' }
      break
    case 'SHIPPORT':
      config = { title: '선적항 선택', gubun: 'E2', gbncd: '308', field: 'SHIPPORTNM' }
      break
    case 'ARVPORT':
      config = { title: '도착항 선택', gubun: 'E2', gbncd: '309', field: 'ARVPORTNM' }
      break
    case 'CURRCD':
      config = { title: '통화 선택', gubun: 'E2', gbncd: '310', field: 'CURRNM' }
      break
    case 'PAYCOND':
      config = { title: '결제조건 선택', gubun: 'E2', gbncd: '312', field: 'PAYCONDNM' }
      break
    case 'PRICOND':
      config = { title: '가격조건 선택', gubun: 'E2', gbncd: '314', field: 'PRICONDNM' }
      break
  }

  if (type === 'CUST') {
    Object.assign(modalProps, {
      title: config.title, path: '/api/ha00/HA00_00P_STR', defaultField: config.field,
      data: { GUBUN: config.gubun, CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
      columns: config.columns,
      onConfirm: (data: any) => { formData.CUSTCD = data.CUSTCD; formData.CUSTNM = data.CUSTNM }
    })
  } else {
    Object.assign(modalProps, {
      title: config.title, path: '/api/ha00/HA00_00P_STR', defaultField: config.field,
      data: { GUBUN: config.gubun, GBNCD: config.gbncd, CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
      columns: [
        { title: '코드', field: 'CODECD', width: 80 },
        { title: '명칭', field: 'CODENM', width: 200 }
      ],
      onConfirm: (data: any) => {
        const code = data.CODECD; const name = data.CODENM
        if (type === 'NACD') { formData.NACD = code; formData.NANM = name }
        else if (type === 'SHIPPORT') { formData.SHIPPORT = code; formData.SHIPPORTNM = name }
        else if (type === 'ARVPORT') { formData.ARVPORT = code; formData.ARVPORTNM = name }
        else if (type === 'CURRCD') { formData.CURRCD = code; formData.CURRNM = name }
        else if (type === 'PAYCOND') { formData.PAYCOND = code; formData.PAYCONDNM = name }
        else if (type === 'PRICOND') { formData.PRICOND = code; formData.PRICONDNM = name }
      }
    })
  }
  modalVisible.value = true
}

onMounted(() => {
  nextTick(() => {
    initGrid()
    fetchList()
  })
})
</script>

<style scoped>
.hsba140u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11px; padding: 6px 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
