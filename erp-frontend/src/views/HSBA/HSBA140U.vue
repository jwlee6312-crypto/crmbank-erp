<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 💡 Haion ERP 표준 레이아웃: overflow-hidden 적용 -->
  <div class="hsba140u-wrapper d-flex flex-column h-100 bg-white p-0 overflow-hidden text-start">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
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
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-1 bg-light-subtle">
          <div class="d-flex align-items-center gap-3 px-2">
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

      <!-- 🅱️ 거래처 상세 정보 입력: 5열 레이아웃 적용 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1"></i> 수출입 관리 정보</span>
          <div class="d-flex gap-2">
            <span v-if="formData.ACTKIND === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 모드</span>
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
              <col style="width: 80px;" /><col />
            </colgroup>
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
                  <div class="input-group input-group-sm">
                    <input v-model="formData.CUSTCD" type="text" class="form-control text-center fw-bold" style="max-width: 70px;" readonly />
                    <input v-model="formData.CUSTNM" type="text" class="form-control bg-light" placeholder="거래처 선택..." readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')" :disabled="formData.ACTKIND === 'U0'"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">국&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;가</th>
                <td>
                  <select v-model="formData.NACD" class="form-select form-select-sm">
                    <option value="">-- 국가 선택 --</option>
                    <option v-for="opt in comboOptions.na" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">선 적 항</th>
                <td>
                  <select v-model="formData.SHIPPORT" class="form-select form-select-sm">
                    <option value="">-- 선적항 선택 --</option>
                    <option v-for="opt in comboOptions.ship" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">도 착 항</th>
                <td>
                  <select v-model="formData.ARVPORT" class="form-select form-select-sm">
                    <option value="">-- 도착항 선택 --</option>
                    <option v-for="opt in comboOptions.arv" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="required">통화단위</th>
                <td>
                  <select v-model="formData.CURRCD" class="form-select form-select-sm">
                    <option value="">-- 통화 선택 --</option>
                    <option v-for="opt in comboOptions.curr" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">결제조건</th>
                <td>
                  <select v-model="formData.PAYCOND" class="form-select form-select-sm">
                    <option value="">-- 결제조건 선택 --</option>
                    <option v-for="opt in comboOptions.pay" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">가격조건</th>
                <td>
                  <select v-model="formData.PRICOND" class="form-select form-select-sm">
                    <option value="">-- 가격조건 선택 --</option>
                    <option v-for="opt in comboOptions.pri" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>삭제여부</th>
                <td>
                  <div class="form-check form-switch m-0">
                    <input v-model="formData.USEYN" class="form-check-input" type="checkbox" id="delYn140" true-value="N" false-value="Y">
                    <label class="form-check-label small fw-bold" for="delYn140">삭제</label>
                  </div>
                </td>
                <th class="bg-light"></th>
                <td></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 목록 영역: flex-grow-1로 하단 공백 없이 채움 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex justify-content-between flex-shrink-0">
          <span><i class="bi bi-list-ul me-1"></i> 수출입 거래처 내역 ({{ activeItemCount }} 건)</span>
          <span class="text-muted" style="font-size: 11px;">※ 행 클릭 시 상세 내용을 수정할 수 있습니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="min-height: 0; position: relative;">
          <div ref="gridElement" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;"></div>
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
const searchParams = reactive({ IOGBN: '100' })
const formData = reactive<any>({
  ACTKIND: 'A0', IOGBN: '100', CUSTCD: '', CUSTNM: '', NACD: '', SHIPPORT: '', ARVPORT: '', CURRCD: '', PAYCOND: '', PRICOND: '', USEYN: 'Y'
})

const comboOptions = reactive<any>({ na: [], ship: [], arv: [], curr: [], pay: [], pri: [] })
const gridElement = ref<HTMLElement | null>(null); let grid: Tabulator | null = null; const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "조회된 데이터가 없습니다.",
    pagination: "local", paginationSize: 20, paginationButtonCount: 3,
    columnDefaults: { headerSort: false, headerHozAlign: "center" },
    columns: [
      { title: "거래처", field: "CUSTNM", minWidth: 200, hozAlign: "left", cssClass: "fw-bold text-primary" },
      { title: "국가", field: "NANM", width: 100 },
      { title: "선적항", field: "SHIPPORTNM", width: 120 },
      { title: "도착항", field: "ARVPORTNM", width: 120 },
      { title: "통화단위", field: "CURRNM", width: 100 },
      { title: "결제조건", field: "PAYCONDNM", width: 120 },
      { title: "가격조건", field: "PRICONDNM", width: 120 }
    ]
  })

  grid.on("rowClick", (e, row) => {
    const data = row.getData();
    const cleaned: any = {};
    Object.keys(data).forEach(k => cleaned[k] = typeof data[k] === 'string' ? data[k].trim() : data[k]);
    Object.assign(formData, cleaned); formData.ACTKIND = 'U0'; formData.USEYN = 'Y';
  })
}

// 3. 기능 구현
async function fetchOptions() {
  const getOpt = (cd: string) => api.post('/api/hs00/HS00_000S_STR', { GUBUN: 'E2', CMPYCD: authStore.CMPYCD, GBNCD: cd })
  try {
    const [r1, r2, r3, r4, r5, r6] = await Promise.all([
      getOpt('305'), getOpt('308'), getOpt('309'), getOpt('310'), getOpt('312'), getOpt('314')
    ])
    const map = (r: any) => r.data.map((i: any) => ({ CODECD: String(i.CODE || i.CODECD || '').trim(), CODENM: String(i.CDNM || i.CODENM || '').trim() }))
    comboOptions.na = map(r1); comboOptions.ship = map(r2); comboOptions.arv = map(r3);
    comboOptions.curr = map(r4); comboOptions.pay = map(r5); comboOptions.pri = map(r6);
  } catch (e) {}
}

async function fetchList() {
  try {
    const res = await api.post('/api/hsba/HSBA_140U_STR', { ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOGBN: searchParams.IOGBN })
    grid?.setData(res.data); activeItemCount.value = res.data.length
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!formData.CUSTCD) return vAlertError('거래처를 선택하세요.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const res = await api.post('/api/hsba/HSBA_140U_STR', { ...formData, CMPYCD: authStore.CMPYCD, USERID: authStore.USERID })
    if (res.data?.[0]?.ERRYN === 'Y') vAlertError(res.data[0].MSG)
    else { vAlert('성공적으로 저장되었습니다.'); fetchList(); initialize() }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  const currentGbn = searchParams.IOGBN; resetForm(formData)
  Object.assign(formData, { ACTKIND: 'A0', IOGBN: currentGbn, USEYN: 'Y' })
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
  if (type === 'CUST') {
    Object.assign(modalProps, { title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CUSTNM', large: true, data: { GUBUN: 'C4', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CUSTCD', width: 80 }, { title: '거래처명', field: 'CUSTNM', width: 200 }], onConfirm: (d: any) => { formData.CUSTCD = d.CUSTCD; formData.CUSTNM = d.CUSTNM } })
    modalVisible.value = true
  }
}

onMounted(async () => { await fetchOptions(); nextTick(() => { initGrid(); fetchList() }) })
</script>

<style scoped>
.hsba140u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; border: none; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; }

.flex-shrink-0 { flex-shrink: 0 !important; }
.flex-grow-1 { flex-grow: 1 !important; min-height: 0 !important; }
.overflow-hidden { overflow: hidden !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 4px 5px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 2px 4px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>
