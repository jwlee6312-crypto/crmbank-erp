<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 💡 근본 해결: 최외각 wrapper에 overflow-hidden 적용 -->
  <div class="hsba130u-wrapper d-flex flex-column h-100 bg-white p-0 overflow-hidden text-start">
    <!-- 🚀 1. 상단 액션 바: flex-shrink-0으로 영역 고정 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-truck me-2 text-primary"></i>
        기본정보 > <span class="text-primary fw-bolder">거래 배송처 등록 (HSBA130U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역: 전체 스크롤 허용 (그리드 높이 표준화 대응) -->
    <div class="flex-grow-1 overflow-y-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-1 bg-light-subtle">
          <div class="d-flex align-items-center gap-3 px-2">
            <div class="input-group input-group-sm flex-nowrap" style="width: 350px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">거래처 명</span>
              <input v-model="searchData.SCUSTNM" type="text" class="form-control border-0 bg-white" placeholder="거래처명 입력" @keyup.enter="search" />
              <button class="btn btn-dark btn-sm" @click="search"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 🅱️ 마스터 정보 입력 영역: 지시하신 대로 한 줄(1행)로 처리 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 배송처 정보 입력</span>
          <div class="d-flex gap-2 align-items-center">
            <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 모드 ({{ masterData.CUSTCD }} - {{ masterData.TRANCD }})</span>
            <span v-else class="badge bg-primary" style="font-size: 10px;">신규 등록</span>
          </div>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <tbody>
              <tr>
                <th class="required" style="width: 70px;">거 래 처</th>
                <td style="width: 220px;">
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control bg-light" placeholder="거래처 선택" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')" :disabled="masterData.ACTKIND === 'U0'"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required" style="width: 70px;">주&nbsp;&nbsp;&nbsp;&nbsp;소</th>
                <td style="width: 320px;">
                  <AddressPopupForm v-model:postno="masterData.POSTNO" v-model:address="masterData.ADDRESS" v-model:d_address="masterData.ADDRESS_DETAIL" active />
                </td>
                <th style="width: 70px;">지&nbsp;&nbsp;&nbsp;&nbsp;역</th>
                <td style="width: 120px;">
                  <select v-model="masterData.AREA" class="form-select form-select-sm">
                    <option v-for="opt in areaOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th style="width: 70px;">담 당 자</th>
                <td style="width: 110px;">
                  <input v-model="masterData.DAMDANG" type="text" class="form-control form-control-sm" maxlength="30" />
                </td>
                <th style="width: 70px;">연 락 처</th>
                <td style="width: 300px;">
                  <div class="d-flex gap-1">
                    <input v-model="masterData.TELNO" type="text" class="form-control form-control-sm" placeholder="전화" maxlength="30" />
                    <input v-model="masterData.EMAIL" type="text" class="form-control form-control-sm" placeholder="이메일" maxlength="50" style="ime-mode:inactive" />
                  </div>
                </td>
                <th style="width: 50px;">사용</th>
                <td style="width: 60px;">
                  <div class="form-check form-switch m-0 d-flex justify-content-center">
                    <input v-model="masterData.USEYN" class="form-check-input" type="checkbox" id="useYn130" true-value="Y" false-value="N">
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역: erp-main-grid 적용하여 높이 표준화(400px) -->
      <div class="card border shadow-sm erp-main-grid overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 배송처 목록 ({{ activeItemCount }} 건)</span>
          <span class="text-muted" style="font-size: 11px;">※ 행 클릭 시 상세 정보를 수정할 수 있습니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>
    <!-- 📊 하단 바 삭제됨 -->
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
const searchData = reactive({ SCUSTNM: '', SCUSTCD: '' })
const masterData = reactive<any>({
  ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, CUSTCD: '', CUSTNM: '', TRANCD: '',
  AREA: '000', POSTNO: '', ADDRESS: '', ADDRESS_DETAIL: '', DAMDANG: '', TELNO: '', EMAIL: '', USEYN: 'Y'
})

const areaOptions = ref<any[]>([])
const gridElement = ref<HTMLElement | null>(null); let grid: Tabulator | null = null; const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "조회된 데이터가 없습니다.",
    pagination: "local", paginationSize: 20, paginationButtonCount: 3,
    columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
    columns: [
      { title: "거래처코드", field: "CUSTCD", width: 100, hozAlign: "center" },
      { title: "상호", field: "CUSTNM", minWidth: 200, hozAlign: "left", cssClass: "fw-bold text-primary" },
      { title: "순번", field: "TRANCD", width: 60, hozAlign: "center" },
      { title: "담당자", field: "DAMDANG", width: 120, hozAlign: "center" },
      { title: "주소", field: "ADDRESS", minWidth: 300, hozAlign: "left" }
    ]
  })

  grid.on("rowClick", (e, row) => {
    const data = row.getData(); const cleaned: any = {};
    Object.keys(data).forEach(k => cleaned[k.toUpperCase()] = typeof data[k] === 'string' ? data[k].trim() : data[k]);
    Object.assign(masterData, {
      ACTKIND: 'U0', CMPYCD: cleaned.CMPYCD, CUSTCD: cleaned.CUSTCD, CUSTNM: cleaned.CUSTNM,
      TRANCD: cleaned.TRANCD, AREA: cleaned.AREA || '000', POSTNO: cleaned.POSTNO,
      DAMDANG: cleaned.DAMDANG, TELNO: cleaned.TELNO, EMAIL: cleaned.EMAIL, USEYN: 'Y'
    });

    if (cleaned.ADDRESS && cleaned.ADDRESS.includes('  ')) {
      const parts = cleaned.ADDRESS.split('  '); masterData.ADDRESS = parts[0]; masterData.ADDRESS_DETAIL = parts[1];
    } else {
      masterData.ADDRESS = cleaned.ADDRESS; masterData.ADDRESS_DETAIL = '';
    }
  })
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    const res = await api.post('/api/hs00/HS00_000S_STR', { GUBUN: 'AR', CMPYCD: authStore.CMPYCD });
    areaOptions.value = res.data.map((i: any) => ({ CODECD: String(i.CODE || i.CODECD || '').trim(), CODENM: String(i.CDNM || i.CODENM || '').trim() }))
    if (areaOptions.value.length === 0) areaOptions.value = [{ CODECD: '000', CODENM: '없음' }]
  } catch (e) {}
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_130U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, CUSTCD: searchData.SCUSTCD || '', CUSTNM: searchData.SCUSTNM || '',
      TRANCD: '', AREA: '', POSTNO: '', ADDRESS: '', DAMDANG: '', TELNO: '', EMAIL: '', USEYN: '', USERID: authStore.USERID
    })
    const processed = (res.data || []).map((i: any) => {
      const item: any = {}; Object.keys(i).forEach(k => item[k.toUpperCase()] = typeof i[k] === 'string' ? i[k].trim() : i[k]);
      return item;
    });
    grid?.setData(processed); activeItemCount.value = processed.length
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.CUSTCD) return vAlertError('거래처를 선택해 주십시요.')
  if (!masterData.ADDRESS) return vAlertError('주소를 입력해 주십시요.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const payload = { ...masterData, ADDRESS: `${masterData.ADDRESS}  ${masterData.ADDRESS_DETAIL}`.trim(), USERID: authStore.USERID }
    const res = await api.post('/api/hsba/HSBA_130U_STR', payload)
    if (res.data?.[0]?.RESULT === 'Y' || res.data?.[0]?.ERRYN === 'Y') vAlertError(res.data[0].MSG)
    else { vAlert('성공적으로 저장되었습니다.'); search(); initialize() }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(masterData);
  Object.assign(masterData, { ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, AREA: '000', USEYN: 'Y' })
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
  if (type === 'CUST') {
    Object.assign(modalProps, { title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CUSTNM', large: true, data: { GUBUN: 'C4', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CUSTCD', width: 80 }, { title: '거래처명', field: 'CUSTNM', width: 200 }, { title: '사업자번호', field: 'CUSTNO', width: 120 }, { title: '주소', field: 'ADDRESS', minWidth: 300 }], onConfirm: (d: any) => { masterData.CUSTCD = d.CUSTCD; masterData.CUSTNM = d.CUSTNM } })
    modalVisible.value = true
  }
}

onMounted(async () => { await fetchOptions(); nextTick(() => { initGrid(); search() }) })
</script>

<style scoped>
.hsba130u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; border: none; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; }

.flex-shrink-0 { flex-shrink: 0 !important; }
.flex-grow-1 { flex-grow: 1 !important; min-height: 0 !important; }
.overflow-hidden { overflow: hidden !important; }

/* 🚀 표준 그리드 높이 적용 (400px) */
.erp-main-grid { height: 590px !important; flex-shrink: 0 !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 6px 10px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #d32f2f; }

:deep(.tabulator) { border: 1px solid #dee2e6; font-size: 12px; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>
