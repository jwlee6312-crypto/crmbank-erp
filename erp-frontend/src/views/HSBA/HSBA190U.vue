<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba190u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-person-badge-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">거래처 담당자등록 (HSBA190U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="searchCustomers">조회</button>
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
                <th style="width: 100px;">거래처명</th>
                <td>
                  <input v-model="searchData.QCUSTNM" type="text" class="form-control form-control-sm w-50" placeholder="거래처명 또는 코드 입력" @keyup.enter="searchCustomers" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 담당자 정보 입력 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 담당자 정보 입력</span>
          <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark">수정 모드 (No: {{ masterData.DROWNO }})</span>
          <span v-else class="badge bg-primary">신규 등록</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th style="width: 100px;">거래처명</th>
                <td style="width: 250px;">
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control bg-light" readonly />
                  </div>
                </td>
                <th class="required" style="width: 100px;">부&nbsp;서&nbsp;명</th>
                <td style="width: 200px;">
                  <input v-model="masterData.DEPTNM" type="text" class="form-control form-control-sm" maxlength="30" />
                </td>
                <th class="required" style="width: 100px;">담당자명</th>
                <td>
                  <input v-model="masterData.DAMDANG" type="text" class="form-control form-control-sm" maxlength="50" />
                </td>
              </tr>
              <tr>
                <th>전화번호</th>
                <td>
                  <input v-model="masterData.TELNO" type="text" class="form-control form-control-sm" maxlength="30" />
                </td>
                <th>메일주소</th>
                <td>
                  <input v-model="masterData.EMAIL" type="text" class="form-control form-control-sm" maxlength="50" style="ime-mode:inactive" />
                </td>
                <th>사용여부</th>
                <td>
                  <div class="form-check form-check-inline mb-0">
                    <input v-model="masterData.USEYN" class="form-check-input" type="checkbox" id="useYn190" true-value="Y" false-value="N">
                    <label class="form-check-label small" for="useYn190">사용</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 하단 목록 영역 (좌: 거래처, 우: 담당자 내역) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 거래처 목록 -->
        <div class="card border shadow-sm d-flex flex-column" style="width: 300px;">
          <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
            <span class="fw-bold small text-dark"><i class="bi bi-building me-1"></i> 거래처 목록</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="custGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <!-- 우측: 담당자 내역 목록 -->
        <div class="card border shadow-sm flex-grow-1 d-flex flex-column">
          <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
            <span class="fw-bold small text-dark"><i class="bi bi-person-lines-fill me-1"></i> 담당자 설정 내역</span>
            <span v-if="selectedCustName" class="text-primary fw-bold small">[{{ selectedCustName }}]</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="damGridElement" style="height: 100%;"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-12 small">
          ※ 거래처를 선택하면 해당 거래처의 담당자 내역이 표시됩니다. 파란색 상호는 활성화된 담당자가 있는 거래처입니다.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 1. 상태 관리
const searchData = reactive({
  QCUSTNM: ''
})

const masterData = reactive<any>({
  ACTKIND: 'A0',
  CMPYCD: authStore.CMPYCD,
  CUSTCD: '',
  CUSTNM: '',
  DROWNO: '',
  DEPTNM: '',
  DAMDANG: '',
  TELNO: '',
  EMAIL: '',
  USEYN: 'Y'
})

const selectedCustName = ref('')
const custGridElement = ref<HTMLElement | null>(null)
const damGridElement = ref<HTMLElement | null>(null)
let custGrid: Tabulator | null = null
let damGrid: Tabulator | null = null

const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrids = () => {
  // 좌측 거래처 그리드
  if (custGridElement.value) {
    custGrid = new Tabulator(custGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "데이터 없음",
      pagination: true,
      paginationSize: 20,
      columns: [
        {
          title: "거래처 상호",
          field: "CUSTNM",
          headerSort: false,
          formatter: (cell) => {
              const data = cell.getData()
              return data.USEYN === 'Y' ? `<span class="text-primary fw-bold">${cell.getValue()}</span>` : cell.getValue()
          }
        }
      ]
    })
    custGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      masterData.CUSTCD = data.CUSTCD
      masterData.CUSTNM = data.CUSTNM
      selectedCustName.value = data.CUSTNM

      // Reset entry fields for new manager
      masterData.ACTKIND = 'A0'
      masterData.DROWNO = ''
      masterData.DEPTNM = ''
      masterData.DAMDANG = ''
      masterData.TELNO = ''
      masterData.EMAIL = ''
      masterData.USEYN = 'Y'

      fetchDamList()
    })
  }

  // 우측 담당자 내역 그리드
  if (damGridElement.value) {
    damGrid = new Tabulator(damGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "내역 없음",
      columnDefaults: { headerSort: false },
      columns: [
        { title: "번호", field: "ROWNO", width: 60, hozAlign: "center" },
        { title: "부서명", field: "DEPTNM", width: 150 },
        { title: "담당자명", field: "DAMDANG", width: 120, hozAlign: "center" },
        { title: "전화번호", field: "TELNO", width: 150 },
        { title: "메일주소", field: "EMAIL", minWidth: 200 },
        {
          title: "사용",
          field: "USEYN",
          width: 70,
          hozAlign: "center",
          formatter: (cell) => cell.getValue() === 'Y' ? '<i class="bi bi-check-lg text-success"></i>' : '<i class="bi bi-x-lg text-danger"></i>'
        }
      ]
    })

    damGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      Object.assign(masterData, data)
      masterData.ACTKIND = 'U0'
      masterData.DROWNO = data.ROWNO
    })
  }
}

// 3. 기능 구현
async function searchCustomers() {
  try {
    const res = await api.post('/api/hsba/HSBA_190U_STR', {
      ACTKIND: 'S1',
      CMPYCD: authStore.CMPYCD,
      QCUSTNM: searchData.QCUSTNM,
      LIMITOFFSET: 0,
      LIMITROWS: 999
    })
    custGrid?.setData(res.data)
    damGrid?.clearData()
    activeItemCount.value = 0
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDamList() {
  try {
    const res = await api.post('/api/hsba/HSBA_190U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      CUSTCD: masterData.CUSTCD
    })
    damGrid?.setData(res.data)
    activeItemCount.value = res.data.length
  } catch (e) { vAlertError('내역 조회 실패') }
}

async function save() {
  if (!masterData.CUSTCD) return vAlertError('거래처를 먼저 선택하세요.')
  if (!masterData.DAMDANG) return vAlertError('담당자명은 필수입니다.')

  const actionText = masterData.ACTKIND === 'A0' ? '등록' : '수정'
  if (!confirm(`담당자 정보를 ${actionText}하시겠습니까?`)) return

  try {
    const payload = {
      ...masterData,
      TELNO: masterData.TELNO.replace(/-/g, ''),
      USERID: authStore.USERID
    }
    await api.post('/api/hsba/HSBA_190U_STR', payload)
    vAlert('정상적으로 처리되었습니다.')
    fetchDamList()

    // Switch to new entry mode after success if it was new entry
    if (masterData.ACTKIND === 'A0') {
        initialize(true)
    }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize(keepCust = false) {
  const cd = masterData.CUSTCD
  const nm = masterData.CUSTNM
  resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'A0',
    CMPYCD: authStore.CMPYCD,
    USEYN: 'Y'
  })
  if (keepCust) {
    masterData.CUSTCD = cd
    masterData.CUSTNM = nm
  } else {
    selectedCustName.value = ''
    damGrid?.clearData()
    activeItemCount.value = 0
  }
}

onMounted(() => {
  nextTick(() => {
    initGrids()
    searchCustomers()
  })
})
</script>

<style scoped>
.hsba190u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
