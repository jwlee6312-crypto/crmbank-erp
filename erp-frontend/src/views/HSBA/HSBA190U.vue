<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 💡 [진짜 표준] 최외각 wrapper: h-100 및 overflow-hidden으로 스크롤 고립 -->
  <div class="erp-container">
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
              <input v-model="searchData.Qcustnm" type="text" class="form-control border-0 bg-white" placeholder="거래처명 또는 코드 입력" @keyup.enter="searchCustomers" />
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
            <span v-if="masterData.actkind === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 모드 (No: {{ masterData.rowno }})</span>
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
                    <input v-model="masterData.custcd" type="text" class="form-control text-center bg-light" style="max-width: 90px;" readonly />
                    <input v-model="masterData.custnm" type="text" class="form-control bg-light" placeholder="거래처 선택" readonly />
                  </div>
                </td>
                <th class="required" style="width: 70px;">부&nbsp;&nbsp;&nbsp;&nbsp;서</th>
                <td style="width: 250px;">
                  <input v-model="masterData.deptnm" type="text" class="form-control form-control-sm" maxlength="30" />
                </td>
                <th class="required" style="width: 70px;">담 당 자</th>
                <td style="width: 150px;">
                  <input v-model="masterData.DAMDANG" type="text" class="form-control form-control-sm" maxlength="50" />
                </td>
                <th style="width: 70px;">전화번호</th>
                <td style="width: 150px;">
                  <input v-model="masterData.telno" type="text" class="form-control form-control-sm" maxlength="30" />
                </td>
                <th style="width: 70px;">메일주소</th>
                <td>
                  <input v-model="masterData.email" type="text" class="form-control form-control-sm" maxlength="50" style="ime-mode:inactive" />
                </td>
                <th style="width: 50px;">사용</th>
                <td style="width: 60px;">
                  <div class="form-check form-switch m-0 d-flex justify-content-center">
                    <input v-model="masterData.useyn" class="form-check-input" type="checkbox" id="useYn190" true-value="Y" false-value="N">
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
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="custGridElement" class="tabulator-instance flex-grow-1"></div>
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
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                <div ref="damGridElement" class="tabulator-instance flex-grow-1"></div>
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

const searchData = reactive({ Qcustnm: '' })
const masterData = reactive<any>({
  actkind: 'A0', cmpycd: authStore.cmpycd, custcd: '', custnm: '', rowno: '',
  deptnm: '', DAMDANG: '', telno: '', email: '', useyn: 'Y', updemp: authStore.userid
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
          title: "거래처 상호", field: "custnm", hozAlign: "left",
          formatter: (cell) => cell.getData().useyn === 'Y' ? `<span class="text-primary fw-bold">${cell.getValue()}</span>` : cell.getValue()
        }
      ]
    })
    custGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      masterData.custcd = (data.custcd || '').trim();
      masterData.custnm = (data.custnm || '').trim();
      selectedCustName.value = masterData.custnm;
      initialize(true)
      fetchDamList()
    })
  }

  if (damGridElement.value) {
    damGrid = new Tabulator(damGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "내역 없음",
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: "No", field: "rowno", width: 100 },
        { title: "부서명", field: "deptnm", width: 250, hozAlign: "left" },
        { title: "담당자명", field: "DAMDANG", width: 200 },
        { title: "전화번호", field: "telno", width: 200 },
        { title: "메일주소", field: "email", minWidth: 200, hozAlign: "left" },
        { title: "사용", field: "useyn", width: 120, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
      ]
    })
    damGrid.on("rowClick", (e, row) => {
      const data = row.getData(); const cleaned: any = {};
      Object.keys(data).forEach(k => cleaned[k.toUpperCase()] = typeof data[k] === 'string' ? data[k].trim() : data[k]);
      Object.assign(masterData, cleaned);
      masterData.actkind = 'U0';
    })
  }
}

async function searchCustomers() {
  try {
    const res = await api.post('/api/hsba/HSBA_190U_STR', {
      actkind: 'S1', cmpycd: authStore.cmpycd, custcd: '', custnm: searchData.Qcustnm || '',
      rowno: '', deptnm: '', DAMDANG: '', telno: '', email: '', useyn: '', userid: authStore.userid
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
      actkind: 'S0', cmpycd: authStore.cmpycd, custcd: masterData.custcd,
      custnm: '', rowno: '', deptnm: '', DAMDANG: '', telno: '', email: '', useyn: '', userid: authStore.userid
    })
    const processed = (res.data || []).map((i: any) => {
      const item: any = {}; Object.keys(i).forEach(k => item[k.toUpperCase()] = typeof i[k] === 'string' ? i[k].trim() : i[k]);
      return item;
    });
    damGrid?.setData(processed)
  } catch (e) { vAlertError('내역 조회 실패') }
}

async function save() {
  if (!masterData.custcd) return vAlertError('거래처를 먼저 선택하세요.')
  if (!masterData.DAMDANG) return vAlertError('담당자명은 필수입니다.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const payload = { ...masterData, telno: (masterData.telno || '').replace(/-/g, ''), userid: authStore.userid }
    await api.post('/api/hsba/HSBA_190U_STR', payload)
    vAlert('정상 처리되었습니다.'); fetchDamList(); if (masterData.actkind === 'A0') initialize(true)
  } catch (e) { vAlertError('저장 실패') }
}

function initialize(keepCust = false) {
  const cd = masterData.custcd; const nm = masterData.custnm; resetForm(masterData)
  Object.assign(masterData, { actkind: 'A0', cmpycd: authStore.cmpycd, useyn: 'Y', updemp: authStore.userid })
  if (keepCust) { masterData.custcd = cd; masterData.custnm = nm } else { selectedCustName.value = ''; damGrid?.clearData() }
}

onMounted(() => { nextTick(() => { initGrids(); searchCustomers() }) })
</script>
