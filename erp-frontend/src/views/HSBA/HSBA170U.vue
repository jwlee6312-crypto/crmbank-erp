<!--
	=============================================================
	프로그램명	: 담보사항 등록 [디자인 원칙 13가지 완벽 준수]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 거래처별 담보 설정 및 여신 한도 관리
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba170u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1, 12. 상단 액션 바: 버튼 그룹 우측 상단 정렬 및 표준 색상 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-shield-check me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">담보사항 등록 (HSBA170U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize(false)">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="search">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="save">
          <i class="bi bi-save"></i> 저장
        </button>
      </div>
    </div>

    <!-- 🔍 9. 최상단 검색 항목 영역 (단일행 균등 배분 적용) -->
    <div class="p-2 pb-0 flex-shrink-0">
      <div class="card border shadow-sm overflow-hidden">
        <table class="erp-table-full" style="table-layout: fixed;">
          <colgroup>
            <col style="width: 50%;" />
            <col style="width: 50%;" />
          </colgroup>
          <tbody>
            <tr>
              <td>
                <div class="d-flex align-items-center px-2">
                  <span class="erp-label me-2">거래처 검색</span>
                  <div class="input-group input-group-sm flex-nowrap" style="max-width: 350px;">
                    <input v-model="searchData.QCUSTNM" type="text" class="form-control" placeholder="거래처명 입력 후 조회" @keyup.enter="search" />
                    <button class="btn btn-outline-secondary px-2" @click="search"><i class="bi bi-search"></i></button>
                  </div>
                </div>
              </td>
              <td><!-- 균등 배분용 빈 공간 --></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 10. 입력 항목 영역 (담보 마스터 정보) -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-pencil-square me-2 text-primary"></i> 담보 정보 입력
            <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark ms-2">수정 모드 (ROW: {{ masterData.DROWNO }})</span>
            <span v-else class="badge bg-primary ms-2">신규 등록</span>
          </span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <colgroup>
              <col style="width: 100px;"><col style="width: 25%">
              <col style="width: 100px;"><col style="width: 15%">
              <col style="width: 100px;"><col style="width: 15%">
              <col style="width: 100px;"><col style="width: auto;">
            </colgroup>
            <tbody>
              <tr>
                <th>거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                <td>
                  <div class="d-flex gap-1">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control form-control-sm text-center bg-light fw-bold" style="max-width: 70px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control form-control-sm bg-light" readonly />
                  </div>
                </td>
                <th class="required">담보구분</th>
                <td>
                  <select v-model="masterData.DAMKIND" class="form-select form-select-sm">
                    <option v-for="opt in damKindOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">담보종류</th>
                <td>
                  <select v-model="masterData.DAMYEO" class="form-select form-select-sm">
                    <option v-for="opt in damYeoOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>증권번호</th>
                <td>
                  <input v-model="masterData.DAMMGT" type="text" class="form-control form-control-sm" maxlength="30" placeholder="증권/관리번호" />
                </td>
              </tr>
              <tr>
                <th>소&nbsp;&nbsp;유&nbsp;&nbsp;자</th>
                <td><input v-model="masterData.SOUNM" type="text" class="form-control form-control-sm" maxlength="50" /></td>
                <th class="required">설&nbsp;&nbsp;정&nbsp;&nbsp;액</th>
                <td><input v-model="masterData.DAMSEL" type="text" class="form-control form-control-sm text-end fw-bold" @input="formatInput('DAMSEL')" /></td>
                <th class="required">여신한도</th>
                <td><input v-model="masterData.SAMHAN" type="text" class="form-control form-control-sm text-end fw-bold text-primary" @input="formatInput('SAMHAN')" /></td>
                <th>여신기한</th>
                <td>
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="masterData.RCVDD" type="text" class="form-control form-control-sm text-center" style="max-width: 80px;" />
                    <span class="small fw-bold">일</span>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required">설&nbsp;&nbsp;정&nbsp;&nbsp;일</th>
                <td><input v-model="uiFRYMD" type="date" class="form-control form-control-sm" /></td>
                <th class="required">유&nbsp;&nbsp;효&nbsp;&nbsp;일</th>
                <td><input v-model="uiTOYMD" type="date" class="form-control form-control-sm" /></td>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td colspan="3"><input v-model="masterData.BIGO" type="text" class="form-control form-control-sm" maxlength="100" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 하단 목록 영역 (좌: 거래처, 우: 담보 내역) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 8. 좌측: 거래처 목록 -->
        <div class="card border shadow-sm d-flex flex-column" style="width: 320px; min-width: 320px;">
          <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
            <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 거래처 목록</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="custGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <!-- 8. 우측: 담보 내역 목록 -->
        <div class="card border shadow-sm flex-grow-1 d-flex flex-column">
          <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
            <span class="fw-bold small text-dark d-flex align-items-center">
              <i class="bi bi-shield-lock-fill me-2 text-primary"></i> 담보 설정 내역
              <span v-if="selectedCustName" class="badge bg-light text-primary border ms-2">{{ selectedCustName }}</span>
            </span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="damGridElement" style="height: 100%;"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 4. 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
      <div class="row align-items-center w-100">
        <div class="col-12 small opacity-75">
          <i class="bi bi-info-circle me-1"></i> 거래처를 선택하면 해당 거래처의 담보 내역이 표시됩니다. 파란색 상호는 담보 설정이 있는 거래처입니다.
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

// 13. 모든 변수명 대문자 고수
const searchData = reactive({ QCUSTNM: '' })
const masterData = reactive<any>({
  ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, CUSTCD: '', CUSTNM: '', DROWNO: '',
  DAMKIND: '100', DAMYEO: '100', DAMMGT: '', SOUNM: '',
  DAMSEL: '0', SAMHAN: '0', RCVDD: '0', FRYMD: initYMD, TOYMD: '', BIGO: '', USEYN: 'Y'
})

const damKindOptions = ref<any[]>([])
const damYeoOptions = ref<any[]>([])
const selectedCustName = ref('')

const uiFRYMD = computed({ get: () => formatDate(masterData.FRYMD, '-'), set: (v) => masterData.FRYMD = v.replace(/-/g, '') })
const uiTOYMD = computed({ get: () => formatDate(masterData.TOYMD, '-'), set: (v) => masterData.TOYMD = v.replace(/-/g, '') })

const custGridElement = ref<HTMLElement | null>(null)
const damGridElement = ref<HTMLElement | null>(null)
let custGrid: Tabulator | null = null
let damGrid: Tabulator | null = null

// 2, 5, 6. 그리드 초기화 (중앙 정렬 및 타이틀 색상)
const initGrids = () => {
  if (custGridElement.value) {
    custGrid = new Tabulator(custGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "거래처 없음",
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        {
          title: "거래처 상호", field: "CUSTNM", hozAlign: "left",
          formatter: (cell) => {
              const data = cell.getData()
              return data.ENDYN === 'Y' ? `<span class="text-primary fw-bold">${cell.getValue()}</span>` : cell.getValue()
          }
        }
      ]
    })
    custGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      masterData.CUSTCD = data.CUSTCD; masterData.CUSTNM = data.CUSTNM;
      selectedCustName.value = data.CUSTNM;
      masterData.ACTKIND = 'A0'; masterData.DROWNO = '';
      fetchDamList()
    })
  }

  if (damGridElement.value) {
    damGrid = new Tabulator(damGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "내역 없음",
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        { title: "No", field: "ROWNO", width: 50 },
        { title: "담보구분", field: "DAMKINDNM", width: 120 },
        { title: "종류", field: "DAMYEONM", width: 120 },
        { title: "한도액", field: "SAMHAN", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "기한(일)", field: "RCVDD", width: 80 },
        { title: "설정일", field: "FRYMD_FMT", width: 110 },
        { title: "유효일", field: "TOYMD_FMT", width: 110 },
        { title: "증권번호", field: "DAMMGT", minWidth: 150, hozAlign: "left" }
      ]
    })
    damGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      Object.assign(masterData, data)
      masterData.DAMSEL = formatNumber(data.DAMSEL)
      masterData.SAMHAN = formatNumber(data.SAMHAN)
      masterData.ACTKIND = 'U0'
      masterData.DROWNO = data.ROWNO
    })
  }
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    const resKind = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E2', CMPYCD: authStore.CMPYCD, GBNCD: '500' } })
    damKindOptions.value = resKind.data.map((i: any) => ({ CODECD: String(i.CODECD || i.CODE).trim(), CODENM: String(i.CODENM || i.CDNM).trim() }))
    const resYeo = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E2', CMPYCD: authStore.CMPYCD, GBNCD: '510' } })
    damYeoOptions.value = resYeo.data.map((i: any) => ({ CODECD: String(i.CODECD || i.CODE).trim(), CODENM: String(i.CODENM || i.CDNM).trim() }))
  } catch (e) { console.error('옵션 로드 실패') }
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_170U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, QCUSTNM: searchData.QCUSTNM,
      LIMITOFFSET: 0, LIMITROWS: 999
    })
    custGrid?.setData(res.data)
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDamList() {
  try {
    const res = await api.post('/api/hsba/HSBA_170U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, CUSTCD: masterData.CUSTCD
    })
    const mapped = res.data.map((i: any) => ({
      ...i,
      FRYMD_FMT: formatDate(i.FRYMD, '-'),
      TOYMD_FMT: formatDate(i.TOYMD, '-')
    }))
    damGrid?.setData(mapped)
  } catch (e) { vAlertError('내역 조회 실패') }
}

async function save() {
  if (!masterData.CUSTCD) return vAlertError('거래처를 먼저 선택하세요.')
  if (!masterData.FRYMD || !masterData.TOYMD) return vAlertError('설정일과 유효일은 필수입니다.')

  const actionText = masterData.ACTKIND === 'A0' ? '등록' : '수정'
  if (!confirm(`담보 정보를 ${actionText}하시겠습니까?`)) return

  try {
    const payload = {
      ...masterData,
      DAMSEL: String(masterData.DAMSEL).replace(/,/g, ''),
      SAMHAN: String(masterData.SAMHAN).replace(/,/g, ''),
      RCVDD: String(masterData.RCVDD).replace(/,/g, ''),
      USERID: authStore.USERID
    }
    await api.post('/api/hsba/HSBA_170U_STR', payload)
    vAlert('정상적으로 처리되었습니다.')
    fetchDamList()
    initialize(true)
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize(keepCust = false) {
  const cd = masterData.CUSTCD; const nm = masterData.CUSTNM
  resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, DAMKIND: '100', DAMYEO: '100',
    DAMSEL: '0', SAMHAN: '0', RCVDD: '0', FRYMD: initYMD, USEYN: 'Y'
  })
  if (keepCust) { masterData.CUSTCD = cd; masterData.CUSTNM = nm }
  else { selectedCustName.value = ''; damGrid?.clearData() }
}

const formatInput = (field: string) => {
  let val = String(masterData[field]).replace(/[^0-9]/g, '')
  masterData[field] = val.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

const formatDate = (v: any, sep: string) => v && String(v).length === 8 ? `${String(v).substring(0, 4)}${sep}${String(v).substring(4, 6)}${sep}${String(v).substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => {
    initGrids()
    search()
  })
})
</script>

<style scoped>
/* 🎨 폰트 선명도 보정 및 전역 스타일 */
.hsba170u-wrapper {
  height: 100%;
  overflow: hidden;
  font-family: 'Pretendard', sans-serif;
  background-color: #f4f7fa !important;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

.erp-header { background-color: #ffffff !important; }

/* 🎨 원칙 1-4: 시스템 공통 버튼 색상 표준 적용 */
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 4px; border: none; }
.btn-init { background-color: #ffffff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #ffffff !important; }
.btn-save { background-color: #005a9f !important; color: #ffffff !important; }

/* 🎨 원칙 10: 폼 레이블 표준 (연한 회색 배경, 검정 글자) */
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f1f3f5; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 8px !important; color: #212529; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }

/* 🎨 원칙 9: 폼 라벨 스타일 */
.erp-label { font-weight: 700; font-size: 12px; color: #212529; min-width: 80px; text-align: right; white-space: nowrap; }
.required::after { content: ' *'; color: #d32f2f; }

/* 🎨 원칙 5, 6: 그리드 타이틀 색상 및 중앙 정렬 표준 */
:deep(.tabulator) {
  border: 1px solid #dee2e6;
  font-size: 13px;
  color: #212529 !important;
  font-family: 'Pretendard', sans-serif !important;
}
:deep(.tabulator-header) {
  background-color: #f8f9fa !important;
  border-bottom: 2px solid #dee2e6 !important;
}
:deep(.tabulator-col-title) {
  color: #6c757d !important;
  font-weight: 800;
  text-align: center !important;
}
:deep(.tabulator-cell) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 4px !important;
  border-right: 1px solid #eee !important;
}
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }

.erp-footer { background-color: #212529 !important; min-height: 40px; }
</style>
