<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba130u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-truck me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">거래 배송처 등록 (HSBA130U)</span>
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
                <th style="width: 100px;">거래처 명</th>
                <td>
                  <input v-model="searchData.SCUSTNM" type="text" class="form-control form-control-sm w-25" placeholder="거래처명 입력" @keyup.enter="search" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 마스터 정보 입력 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 배송처 정보 입력</span>
          <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark ms-2">수정 모드 ({{ masterData.CUSTCD }} - {{ masterData.TRANCD }})</span>
          <span v-else class="badge bg-primary ms-2">신규 등록</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">거&nbsp;래&nbsp;처</th>
                <td style="width: 30%;">
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control bg-light" placeholder="거래처 선택" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')" :disabled="masterData.ACTKIND === 'U0'"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">주&nbsp;&nbsp;&nbsp;&nbsp;소</th>
                <td style="width: 45%;">
                  <AddressPopupForm
                    v-model:postno="masterData.POSTNO"
                    v-model:address="masterData.ADDRESS"
                    v-model:d_address="masterData.ADDRESS_DETAIL"
                    active
                  />
                </td>
                <th>지&nbsp;&nbsp;&nbsp;&nbsp;역</th>
                <td>
                  <select v-model="masterData.AREA" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in areaOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>담&nbsp;당&nbsp;자</th>
                <td>
                  <input v-model="masterData.DAMDANG" type="text" class="form-control form-control-sm" maxlength="30" />
                </td>
                <th>연&nbsp;락&nbsp;처</th>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="input-group input-group-sm">
                      <span class="input-group-text px-2">전화</span>
                      <input v-model="masterData.TELNO" type="text" class="form-control" maxlength="30" />
                    </div>
                    <div class="input-group input-group-sm">
                      <span class="input-group-text px-2">이메일</span>
                      <input v-model="masterData.EMAIL" type="text" class="form-control" maxlength="50" style="ime-mode:inactive" />
                    </div>
                  </div>
                </td>
                <th>삭제여부</th>
                <td>
                  <div class="form-check form-check-inline mb-0">
                    <input v-model="masterData.USEYN" class="form-check-input" type="checkbox" id="useYn130" true-value="N" false-value="Y">
                    <label class="form-check-label small" for="useYn130">삭제</label>
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
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 배송처 목록</span>
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
</div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import AddressPopupForm from '@/components/AddressPopupForm.vue'
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
  SCUSTNM: ''
})

const masterData = reactive<any>({
  ACTKIND: 'A0',
  CMPYCD: authStore.CMPYCD,
  CUSTCD: '',
  CUSTNM: '',
  TRANCD: '', // Sequence
  AREA: '000',
  POSTNO: '',
  ADDRESS: '',
  ADDRESS_DETAIL: '',
  DAMDANG: '',
  TELNO: '',
  EMAIL: '',
  USEYN: 'Y' // Y: Use, N: Deleted (as per ASP logic)
})

const areaOptions = ref<any[]>([])
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
    pagination: true,
    paginationSize: 20,
    columnDefaults: { headerSort: false },
    columns: [
      { title: "거래처코드", field: "CUSTCD", width: 100, hozAlign: "center" },
      { title: "상호", field: "CUSTNM", minWidth: 200, cssClass: "fw-bold text-primary" },
      { title: "순번", field: "TRANCD", width: 60, hozAlign: "center" },
      { title: "지역", field: "AREANM", width: 120, hozAlign: "center" },
      { title: "주소", field: "ADDRESS", minWidth: 300 }
    ]
  })

  grid.value.on("rowClick", (e, row) => {
    const data = row.getData()
    Object.assign(masterData, data)

    // Address splitting (assuming it's stored as "Base Addr  Detail Addr" in DB based on typical patterns)
    if (data.ADDRESS && data.ADDRESS.includes('  ')) {
      const parts = data.ADDRESS.split('  ')
      masterData.ADDRESS = parts[0]
      masterData.ADDRESS_DETAIL = parts[1]
    }

    masterData.ACTKIND = 'U0'
    masterData.USEYN = 'Y' // Reset checkbox to 'Use' when clicking a row
  })
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'AR', CMPYCD: authStore.CMPYCD } })
    areaOptions.value = res.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
  } catch (e) { console.error('지역 코드 로드 실패') }
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_130U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      SCUSTNM: searchData.SCUSTNM,
      LIMITOFFSET: 0,
      LIMITROWS: 999
    })
    if (grid.value) {
      grid.value.setData(res.data)
      activeItemCount.value = res.data.length
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.CUSTCD) return vAlertError('거래처를 선택해 주십시요.')
  if (!masterData.ADDRESS) return vAlertError('주소를 입력해 주십시요.')

  const actionText = masterData.ACTKIND === 'A0' ? '등록' : '수정'
  if (!confirm(`배송처 정보를 ${actionText}하시겠습니까?`)) return

  try {
    const payload = {
      ...masterData,
      // Concatenate addresses with double spaces as seen in other modules
      ADDRESS: `${masterData.ADDRESS}  ${masterData.ADDRESS_DETAIL}`.trim(),
      USERID: authStore.USERID
    }
    const res = await api.post('/api/hsba/HSBA_130U_STR', payload)

    if (res.data?.[0]?.RESULT === 'Y' || res.data?.[0]?.ERRYN === 'Y') {
        vAlertError(res.data[0].MSG || '저장에 실패했습니다.')
    } else {
        vAlert('성공적으로 저장되었습니다.')
        search()
        initialize()
    }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'A0',
    CMPYCD: authStore.CMPYCD,
    AREA: '000',
    USEYN: 'Y'
  })
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'CUST') {
    Object.assign(modalProps, {
      title: '거래처 선택',
      path: '/api/ha00/HA00_00P_STR',
      defaultField: 'CUSTNM',
      large: true,
      data: { GUBUN: 'C4', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
      columns: [
        { title: '코드', field: 'CUSTCD', width: 80 },
        { title: '거래처명', field: 'CUSTNM', width: 200 },
        { title: '사업자번호', field: 'CUSTNO', width: 120 },
        { title: '주소', field: 'ADDRESS', minWidth: 300 }
      ],
      onConfirm: (data: any) => {
        masterData.CUSTCD = data.CUSTCD
        masterData.CUSTNM = data.CUSTNM
      }
    })
    modalVisible.value = true
  }
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
.hsba130u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
