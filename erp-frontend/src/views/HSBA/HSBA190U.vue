<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 💡 [진짜 표준] 최외각 wrapper: h-100 및 overflow-hidden으로 스크롤 고립 -->
  <div class="hsba190u-wrapper d-flex flex-column h-100 bg-white p-0 overflow-hidden text-start">
    <!-- 🚀 1. 상단 액션 바: flex-shrink-0으로 고정 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-person-badge-fill me-2 text-primary"></i>
        기본정보 > <span class="text-primary fw-bolder">거래처 담당자등록 (HSBA190U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize(false)">초기화</button>
        <button class="btn-erp btn-search" @click="searchCustomers">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역: flex-grow-1 및 overflow-hidden으로 하단 공백 제거 -->
    <div class="flex-grow-1 d-flex flex-column gap-2 p-2 overflow-hidden">
      <!-- 🅰️ 조회 조건 영역: flex-shrink-0 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-1 bg-light-subtle">
          <div class="d-flex align-items-center gap-3 px-2">
            <div class="input-group input-group-sm flex-nowrap" style="width: 350px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">거래처검색</span>
              <input v-model="searchData.QCUSTNM" type="text" class="form-control border-0 bg-white" placeholder="거래처명 또는 코드 입력" @keyup.enter="searchCustomers" />
              <button class="btn btn-dark btn-sm" @click="searchCustomers"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 🅱️ 담당자 정보 입력 영역: flex-shrink-0 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 담당자 정보 입력</span>
          <div class="d-flex gap-2 align-items-center">
            <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 모드 (No: {{ masterData.ROWNO }})</span>
            <span v-else class="badge bg-primary" style="font-size: 10px;">신규 등록</span>
          </div>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <tbody>
              <tr>
                <th style="width: 80px;">거 래 처</th>
                <td style="width: 350px;">
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 90px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control bg-light" placeholder="거래처 선택" readonly />
                  </div>
                </td>
                <th class="required" style="width: 70px;">부&nbsp;&nbsp;&nbsp;&nbsp;서</th>
                <td style="width: 250px;">
                  <input v-model="masterData.DEPTNM" type="text" class="form-control form-control-sm" maxlength="30" />
                </td>
                <th class="required" style="width: 70px;">담 당 자</th>
                <td style="width: 150px;">
                  <input v-model="masterData.DAMDANG" type="text" class="form-control form-control-sm" maxlength="50" />
                </td>
                <th style="width: 70px;">전화번호</th>
                <td style="width: 150px;">
                  <input v-model="masterData.TELNO" type="text" class="form-control form-control-sm" maxlength="30" />
                </td>
                <th style="width: 70px;">메일주소</th>
                <td>
                  <input v-model="masterData.EMAIL" type="text" class="form-control form-control-sm" maxlength="50" style="ime-mode:inactive" />
                </td>
                <th style="width: 50px;">사용</th>
                <td style="width: 60px;">
                  <div class="form-check form-switch m-0 d-flex justify-content-center">
                    <input v-model="masterData.USEYN" class="form-check-input" type="checkbox" id="useYn190" true-value="Y" false-value="N">
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 하단 목록 영역: flex-grow-1 및 min-height: 0으로 남은 공간 꽉 채움 -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden" style="min-height: 0;">
        <!-- 좌측: 거래처 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden flex-shrink-0" style="width: 320px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-building me-1"></i> 거래처 목록
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative; min-height: 0;">
            <div ref="custGridElement" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;"></div>
          </div>
        </div>

        <!-- 우측: 담당자 내역 목록 -->
        <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
            <span class="fw-bold small text-dark d-flex align-items-center">
              <i class="bi bi-person-lines-fill me-2 text-primary"></i> 담당자 설정 내역
              <span v-if="selectedCustName" class="badge bg-primary-subtle text-primary border border-primary-subtle ms-2">{{ selectedCustName }}</span>
            </span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative; min-height: 0;">
            <div ref="damGridElement" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;"></div>
          </div>
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

const searchData = reactive({ QCUSTNM: '' })
const masterData = reactive<any>({
  ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, CUSTCD: '', CUSTNM: '', ROWNO: '',
  DEPTNM: '', DAMDANG: '', TELNO: '', EMAIL: '', USEYN: 'Y', UPDEMP: authStore.USERID
})

const selectedCustName = ref('')
const custGridElement = ref<HTMLElement | null>(null)
const damGridElement = ref<HTMLElement | null>(null)
let custGrid: Tabulator | null = null
let damGrid: Tabulator | null = null

const initGrids = () => {
  if (custGridElement.value) {
    custGrid = new Tabulator(custGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
      pagination: "local", paginationSize: 20, paginationButtonCount: 3,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        {
          title: "거래처 상호", field: "CUSTNM", hozAlign: "left",
          formatter: (cell) => cell.getData().USEYN === 'Y' ? `<span class="text-primary fw-bold">${cell.getValue()}</span>` : cell.getValue()
        }
      ]
    })
    custGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      masterData.CUSTCD = (data.CUSTCD || '').trim();
      masterData.CUSTNM = (data.CUSTNM || '').trim();
      selectedCustName.value = masterData.CUSTNM;
      initialize(true)
      fetchDamList()
    })
  }

  if (damGridElement.value) {
    damGrid = new Tabulator(damGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "내역 없음",
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: "No", field: "ROWNO", width: 100 },
        { title: "부서명", field: "DEPTNM", width: 250, hozAlign: "left" },
        { title: "담당자명", field: "DAMDANG", width: 200 },
        { title: "전화번호", field: "TELNO", width: 200 },
        { title: "메일주소", field: "EMAIL", minWidth: 200, hozAlign: "left" },
        { title: "사용", field: "USEYN", width: 120, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
      ]
    })
    damGrid.on("rowClick", (e, row) => {
      const data = row.getData(); const cleaned: any = {};
      Object.keys(data).forEach(k => cleaned[k.toUpperCase()] = typeof data[k] === 'string' ? data[k].trim() : data[k]);
      Object.assign(masterData, cleaned);
      masterData.ACTKIND = 'U0';
    })
  }
}

async function searchCustomers() {
  try {
    const res = await api.post('/api/hsba/HSBA_190U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, CUSTCD: '', CUSTNM: searchData.QCUSTNM || '',
      ROWNO: '', DEPTNM: '', DAMDANG: '', TELNO: '', EMAIL: '', USEYN: '', USERID: authStore.USERID
    })
    const processed = (res.data || []).map((i: any) => {
      const item: any = {}; Object.keys(i).forEach(k => item[k.toUpperCase()] = typeof i[k] === 'string' ? i[k].trim() : i[k]);
      return item;
    });
    custGrid?.setData(processed); damGrid?.clearData(); selectedCustName.value = ''
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDamList() {
  try {
    const res = await api.post('/api/hsba/HSBA_190U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, CUSTCD: masterData.CUSTCD,
      CUSTNM: '', ROWNO: '', DEPTNM: '', DAMDANG: '', TELNO: '', EMAIL: '', USEYN: '', USERID: authStore.USERID
    })
    const processed = (res.data || []).map((i: any) => {
      const item: any = {}; Object.keys(i).forEach(k => item[k.toUpperCase()] = typeof i[k] === 'string' ? i[k].trim() : i[k]);
      return item;
    });
    damGrid?.setData(processed)
  } catch (e) { vAlertError('내역 조회 실패') }
}

async function save() {
  if (!masterData.CUSTCD) return vAlertError('거래처를 먼저 선택하세요.')
  if (!masterData.DAMDANG) return vAlertError('담당자명은 필수입니다.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const payload = { ...masterData, TELNO: (masterData.TELNO || '').replace(/-/g, ''), USERID: authStore.USERID }
    await api.post('/api/hsba/HSBA_190U_STR', payload)
    vAlert('정상 처리되었습니다.'); fetchDamList(); if (masterData.ACTKIND === 'A0') initialize(true)
  } catch (e) { vAlertError('저장 실패') }
}

function initialize(keepCust = false) {
  const cd = masterData.CUSTCD; const nm = masterData.CUSTNM; resetForm(masterData)
  Object.assign(masterData, { ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, USEYN: 'Y', UPDEMP: authStore.USERID })
  if (keepCust) { masterData.CUSTCD = cd; masterData.CUSTNM = nm } else { selectedCustName.value = ''; damGrid?.clearData() }
}

onMounted(() => { nextTick(() => { initGrids(); searchCustomers() }) })
</script>

<style scoped>
.hsba190u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; border: none; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; }

.flex-shrink-0 { flex-shrink: 0 !important; }
.flex-grow-1 { flex-grow: 1 !important; min-height: 0 !important; }
.overflow-hidden { overflow: hidden !important; }

/* 🚀 표준 그리드 높이 적용 */
.erp-main-grid { height: 400px !important; flex-shrink: 0 !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 6px 10px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #d32f2f; }
:deep(.tabulator) { border: 1px solid #dee2e6; font-size: 12px; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>
