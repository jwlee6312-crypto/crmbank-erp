<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 💡 근본 해결: 최외각 wrapper에 overflow-hidden 적용 -->
  <div class="hsba170u-wrapper d-flex flex-column h-100 bg-white p-0 overflow-hidden text-start">
    <!-- 🚀 1. 상단 액션 바: flex-shrink-0으로 영역 고정 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-shield-check me-2 text-primary"></i>
        기본정보 > <span class="text-primary fw-bolder">거래처 담보관리 (HSBA170U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize(false)">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역: 전체 스크롤 허용 (그리드 높이 표준화 대응) -->
    <div class="flex-grow-1 overflow-y-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-1 bg-light-subtle">
          <div class="d-flex align-items-center gap-3">
            <div class="input-group input-group-sm flex-nowrap" style="width: 350px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">거래처검색</span>
              <input v-model="searchData.QCUSTNM" type="text" class="form-control border-0 bg-white" placeholder="거래처명 또는 코드" @keyup.enter="search" />
              <button class="btn btn-dark btn-sm" @click="search"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 🅱️ 담보 정보 입력 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f8f9fa;">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1"></i> 담보 설정 정보</span>
          <div class="d-flex gap-2 align-items-center">
            <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 중</span>
            <span v-else class="badge bg-primary" style="font-size: 10px;">신규 등록</span>
          </div>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">거 래 처</th>
                <td>
                   <div class="d-flex gap-1">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control form-control-sm text-center fw-bold bg-light" style="width: 70px;" readonly />
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
                    <input v-model="masterData.RCVDD" type="text" class="form-control form-control-sm text-end" style="max-width: 60px;" />
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
                <td colspan="3"><input v-model="masterData.BIGO" type="text" class="form-control form-control-sm w-100" maxlength="100" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 목록 영역: erp-main-grid 적용 및 네비게이션(페이징) 표준화 -->
      <div class="d-flex flex-grow-1 gap-2 erp-main-grid">
        <div class="card border shadow-sm d-flex flex-column" style="width: 320px; flex-shrink: 0;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-list-ul me-1"></i> 거래처 목록
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
            <div ref="custGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <div class="card border shadow-sm flex-grow-1 d-flex flex-column">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
            <span class="fw-bold small text-dark d-flex align-items-center">
              <i class="bi bi-shield-lock-fill me-2 text-primary"></i> 담보 설정 내역
              <span v-if="selectedCustName" class="badge bg-primary-subtle text-primary border border-primary-subtle ms-2">{{ selectedCustName }}</span>
            </span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
            <div ref="damGridElement" style="height: 100%;"></div>
          </div>
        </div>
      </div>
    </div>
    <!-- 📊 하단 바 삭제됨 -->
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

const searchData = reactive({ QCUSTNM: '' })
const masterData = reactive<any>({
  ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, CUSTCD: '', CUSTNM: '', DROWNO: '',
  DAMKIND: '100', DAMYEO: '100', DAMMGT: '', SOUNM: '',
  DAMSEL: '0', SAMHAN: '0', RCVDD: '0', FRYMD: initYMD, TOYMD: '', BIGO: '', USEYN: 'Y'
})

const damKindOptions = ref<any[]>([]); const damYeoOptions = ref<any[]>([]); const selectedCustName = ref('')
const uiFRYMD = computed({ get: () => formatDate(masterData.FRYMD, '-'), set: (v) => masterData.FRYMD = v.replace(/-/g, '') })
const uiTOYMD = computed({ get: () => formatDate(masterData.TOYMD, '-'), set: (v) => masterData.TOYMD = v.replace(/-/g, '') })

const custGridElement = ref<HTMLElement | null>(null); const damGridElement = ref<HTMLElement | null>(null)
let custGrid: Tabulator | null = null; let damGrid: Tabulator | null = null

const initGrids = () => {
  if (custGridElement.value) {
    custGrid = new Tabulator(custGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "거래처 없음",
      // 💡 [표준] 네비게이션(페이징) 처리 추가
      pagination: "local",
      paginationSize: 15,
      paginationButtonCount: 3,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        {
          title: "거래처 상호", field: "CUSTNM", hozAlign: "left",
          formatter: (cell) => cell.getData().ENDYN === 'Y' ? `<span class="text-primary fw-bold">${cell.getValue()}</span>` : cell.getValue()
        }
      ]
    })
    custGrid.on("rowClick", (e, row) => {
      const d = row.getData(); masterData.CUSTCD = d.CUSTCD; masterData.CUSTNM = d.CUSTNM;
      selectedCustName.value = d.CUSTNM; masterData.ACTKIND = 'A0'; masterData.DROWNO = '';
      fetchDamList()
    })
  }

  if (damGridElement.value) {
    damGrid = new Tabulator(damGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "내역 없음",

      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: "No", field: "ROWNO", width: 70 },
        { title: "담보구분", field: "DAMKINDNM", width: 150 },
        { title: "종류", field: "DAMYEONM", width: 150 },
        { title: "한도액", field: "SAMHAN", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "기한(일)", field: "RCVDD", width: 150 },
        { title: "설정일", field: "FRYMD_FMT", width: 150 },
        { title: "유효일", field: "TOYMD_FMT", width: 150 },
        { title: "증권번호", field: "DAMMGT", width: 150 },
        { title: "비고", field: "BIGO", minWidth: 130, hozAlign: "left" }
      ]
    })
    damGrid.on("rowClick", (e, row) => {
      const data = row.getData(); Object.assign(masterData, data);
      masterData.DAMSEL = formatNumber(data.DAMSEL); masterData.SAMHAN = formatNumber(data.SAMHAN);
      masterData.ACTKIND = 'U0'; masterData.DROWNO = data.ROWNO;
    })
  }
}

async function fetchOptions() {
  try {
    const r1 = await api.post('/api/hs00/HS00_000S_STR', { GUBUN: 'E0', GBNCD: '500', CMPYCD: authStore.CMPYCD })
    damKindOptions.value = r1.data.map((i: any) => ({ CODECD: String(i.CODE || i.CODECD).trim(), CODENM: String(i.CDNM || i.CODENM).trim() }))
    const r2 = await api.post('/api/hs00/HS00_000S_STR', { GUBUN: 'E0', GBNCD: '510', CMPYCD: authStore.CMPYCD })
    damYeoOptions.value = r2.data.map((i: any) => ({ CODECD: String(i.CODE || i.CODECD).trim(), CODENM: String(i.CDNM || i.CODENM).trim() }))
  } catch (e) {}
}

async function search() {
  try {
    // 💡 [표준] XML 18개 파라미터 규격 준수 및 NULL 방지
    const res = await api.post('/api/hsba/HSBA_170U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, CUSTCD: '', CUSTNM: searchData.QCUSTNM || '',
      ROWNO: '', DAMKIND: '', DAMGBN: '', DAMYEO: '', DAMMGT: '', DAMSEL: 0, SAMHAN: 0, RCVDD: 0,
      FRYMD: '', TOYMD: '', SOUNM: '', BIGO: '', USEYN: '', USERID: authStore.USERID
    })

    // 💡 [표준] 데이터 정제 (대문자 및 Trim)
    const processed = (res.data || []).map((i: any) => {
      const item: any = {};
      Object.keys(i).forEach(k => item[k.toUpperCase()] = typeof i[k] === 'string' ? i[k].trim() : i[k]);
      return item;
    });
    custGrid?.setData(processed)
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDamList() {
  try {
    const res = await api.post('/api/hsba/HSBA_170U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, CUSTCD: masterData.CUSTCD,
      CUSTNM: '', ROWNO: '', DAMKIND: '', DAMGBN: '', DAMYEO: '', DAMMGT: '', DAMSEL: 0, SAMHAN: 0, RCVDD: 0,
      FRYMD: '', TOYMD: '', SOUNM: '', BIGO: '', USEYN: '', USERID: authStore.USERID
    })

    const mapped = (res.data || []).map((i: any) => {
      const item: any = {};
      Object.keys(i).forEach(k => item[k.toUpperCase()] = typeof i[k] === 'string' ? i[k].trim() : i[k]);
      return { ...item, FRYMD_FMT: formatDate(item.FRYMD, '-'), TOYMD_FMT: formatDate(item.TOYMD, '-') }
    })
    damGrid?.setData(mapped)
  } catch (e) { vAlertError('내역 조회 실패') }
}

async function save() {
  if (!masterData.CUSTCD) return vAlertError('거래처를 선택하세요.')
  if (!masterData.FRYMD || !masterData.TOYMD) return vAlertError('설정일과 유효일은 필수입니다.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const payload = { ...masterData, DAMSEL: String(masterData.DAMSEL).replace(/,/g, ''), SAMHAN: String(masterData.SAMHAN).replace(/,/g, ''), RCVDD: String(masterData.RCVDD).replace(/,/g, ''), USERID: authStore.USERID }
    await api.post('/api/hsba/HSBA_170U_STR', payload)
    vAlert('정상 처리되었습니다.'); fetchDamList(); initialize(true)
  } catch (e) { vAlertError('저장 실패') }
}

function initialize(keepCust = false) {
  const cd = masterData.CUSTCD; const nm = masterData.CUSTNM; resetForm(masterData)
  Object.assign(masterData, { ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, DAMKIND: '100', DAMYEO: '100', DAMSEL: '0', SAMHAN: '0', RCVDD: '0', FRYMD: initYMD, USEYN: 'Y' })
  if (keepCust) { masterData.CUSTCD = cd; masterData.CUSTNM = nm } else { selectedCustName.value = ''; damGrid?.clearData() }
}

const formatInput = (field: string) => { let val = String(masterData[field]).replace(/[^0-9]/g, ''); masterData[field] = val.replace(/\B(?=(\d{3})+(?!\d))/g, ',') }
const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
const formatDate = (v: any, sep: string) => v && String(v).length === 8 ? `${String(v).substring(0, 4)}${sep}${String(v).substring(4, 6)}${sep}${String(v).substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => { await fetchOptions(); nextTick(() => { initGrids(); search() }) })
</script>

<style scoped>
.hsba170u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
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
