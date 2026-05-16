<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio020u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-magic me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        구매관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">구매 자동발주 요청등록 (HSIO020U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMaster">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-danger" @click="deleteRequest" :disabled="!masterData.REQNO || masterData.REQNO === '0000'">삭제</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 (균형 배치) -->
    <div class="search-bar bg-light border-bottom p-2 px-3 d-flex align-items-center shadow-sm">
      <div class="d-flex align-items-center gap-4 flex-grow-1">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 요청부서:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchParam.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchParam.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="fetchMaster" />
            <button class="btn btn-outline-secondary" @click="openHelp('S_DEPT')"><i class="bi bi-search"></i></button>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 요청번호:</span>
          <div class="d-flex align-items-center gap-1">
            <input v-model="searchParam.REQYM" type="month" class="form-control form-control-sm text-center" style="width: 130px;" />
            <input v-model="searchParam.REQNO" type="text" class="form-control form-control-sm text-center" placeholder="0000" style="width: 60px;" @keyup.enter="fetchMaster" />
            <div class="btn-group btn-group-sm ms-1">
              <button class="btn btn-outline-secondary py-0 px-1"><i class="bi bi-chevron-left"></i></button>
              <button class="btn btn-outline-secondary py-0 px-1"><i class="bi bi-chevron-right"></i></button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 💡 3. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 d-flex flex-column overflow-hidden">
      <!-- 🅰️ 마스터 정보 -->
      <div class="p-2">
        <div class="card border shadow-sm overflow-hidden">
          <div class="card-body p-0">
            <table class="erp-table-full">
              <colgroup>
                <col style="width: 10%"><col style="width: 23%">
                <col style="width: 10%"><col style="width: 23%">
                <col style="width: 10%"><col style="width: 24%">
              </colgroup>
              <tbody>
                <tr>
                  <th class="required">요청부서</th>
                  <td>
                    <div class="input-group input-group-sm w-100">
                      <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                      <input v-model="masterData.DEPTNM" type="text" class="form-control" @keyup.enter="openHelp('DEPT')" />
                      <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                    </div>
                  </td>
                  <th class="required">요청번호</th>
                  <td>
                    <div class="d-flex align-items-center gap-1 w-100">
                      <input v-model="uiREQYM" type="month" class="form-control form-control-sm text-center fw-bold bg-light" readonly style="flex: 2;" />
                      <input v-model="masterData.REQNO" type="text" class="form-control form-control-sm text-center fw-bold text-primary bg-light" style="flex: 1;" readonly />
                    </div>
                  </td>
                  <th class="required">요청일자</th>
                  <td>
                    <input v-model="uiREQYMD" type="date" class="form-control form-control-sm w-100" />
                  </td>
                </tr>
                <tr>
                  <th>가져오기</th>
                  <td>
                    <button class="btn btn-sm btn-outline-primary fw-bold w-100 py-1" @click="handleImportAnalysis">
                      <i class="bi bi-download me-1"></i> 소요량 분석 가져오기
                    </button>
                  </td>
                  <th class="required">입고요청일</th>
                  <td>
                    <input v-model="uiINYMD" type="date" class="form-control form-control-sm w-100" />
                  </td>
                  <th class="required">담&nbsp;&nbsp;당&nbsp;&nbsp;자</th>
                  <td>
                    <select v-model="masterData.USERID" class="form-select form-select-sm w-100">
                      <option value="">-- 선택 --</option>
                      <option v-for="item in empOptions" :key="item.USERID" :value="item.USERID">
                        {{ item.USERNM }}
                      </option>
                    </select>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 🅱️ 디테일 정보 -->
      <div class="flex-grow-1 d-flex flex-column border-top">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 소요량 분석 및 요청 리스트</span>
          <button class="btn btn-grid-row-add" @click="addRow">
            <i class="bi bi-plus-circle me-1"></i> 행추가
          </button>
        </div>
        <div class="flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">분석 품목: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="fs-5 ms-2 fw-light">총 요청액: <span class="fw-bold text-white ms-2">{{ formatNumber(amtTotal) }}</span> 원</span>
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

const now = new Date();
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`;
const initYMD = `${initYM}${String(now.getDate()).padStart(2, '0')}`;

const searchParam = reactive({ DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, REQYM: initYM, REQNO: '' })
const masterData = reactive<any>({
  ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, REQYM: initYM, REQNO: '0000',
  REQYMD: initYMD, INYMD: initYMD, DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  REMARK: '', ASGBN: 'Y', USERID: String(authStore.USERID || '').trim()
})

const uiREQYM = computed({ get: () => masterData.REQYM ? `${masterData.REQYM.substring(0, 4)}-${masterData.REQYM.substring(4, 6)}` : '', set: (v) => masterData.REQYM = v.replace('-', '') })
const uiREQYMD = computed({ get: () => masterData.REQYMD ? `${masterData.REQYMD.substring(0, 4)}-${masterData.REQYMD.substring(4, 6)}-${masterData.REQYMD.substring(6, 8)}` : '', set: (v) => masterData.REQYMD = v.replace(/-/g, '') })
const uiINYMD = computed({ get: () => masterData.INYMD ? `${masterData.INYMD.substring(0, 4)}-${masterData.INYMD.substring(4, 6)}-${masterData.INYMD.substring(6, 8)}` : '', set: (v) => masterData.INYMD = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null);
const grid = ref<Tabulator | null>(null);
const activeItemCount = ref(0);
const amtTotal = ref(0);
const empOptions = ref<any[]>([])

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "분석된 소요량 데이터가 없습니다.",
    columnDefaults: { minWidth: 90, headerHozAlign: 'center' },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
      { title: "품목명", field: "ITEMNM", minWidth: 200, widthGrow: 1, headerSort: false, cssClass: "fw-bold" },
      { title: "규격", field: "ITSIZE", width: 120, headerSort: false },
      { title: "단위", field: "UNIT", width: 60, hozAlign: "center", headerSort: false },
      { title: "소요량", field: "NEEDQTY", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "현재고", field: "STOCK", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary" },
      { title: "안전재고", field: "SAFESTOCK", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미입고", field: "UNINQTY", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-danger" },
      { title: "요청수량", field: "REQQTY", width: 90, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-yellow fw-bold" },
      { title: "구매단가", field: "IMPRICE", width: 100, hozAlign: "right", editor: "number", formatter: "money" },
      { title: "금액", field: "REQAMT", width: 110, hozAlign: "right", formatter: "money", bottomCalc: "sum", bottomCalcFormatter: "money" },
      { title: "거래처명", field: "CUSTNM", width: 150 },
      { title: "발주번호", field: "BALNO", width: 100, hozAlign: "center", cssClass: "small text-muted" },
      { title: "", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>", cellClick: (e, c) => { c.getRow().delete(); updateTotals() } }
    ]
  });

  grid.value.on("cellEdited", (cell: any) => {
    const row = cell.getRow(); const d = row.getData();
    if (['REQQTY', 'IMPRICE'].includes(cell.getField())) {
      const amt = Math.floor((Number(d.REQQTY) || 0) * (Number(d.IMPRICE) || 0));
      row.update({ REQAMT: amt, UPKIND: d.UPKIND === 'A' ? 'A' : 'U' });
    }
    updateTotals();
  });
}

const updateTotals = () => {
  if (!grid.value) return;
  const data = grid.value.getData();
  activeItemCount.value = data.length;
  amtTotal.value = data.reduce((acc, i) => acc + (Number(i.REQAMT) || 0), 0);
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

async function fetchMaster() {
  if (!searchParam.REQYM || !searchParam.REQNO) return vAlertError('조회할 요청번호를 입력하세요.');
  try {
    const res = await api.post('/api/hsio/HSIO_020U_STR', { ...searchParam, ACTKIND: 'S0', CMPYCD: authStore.CMPYCD });
    if (res.data?.length) {
      Object.assign(masterData, res.data[0]);
      fetchDetail();
    } else { vAlertError('조회 결과가 없습니다.') }
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetail() {
  try {
    const res = await api.post('/api/hsio/HSIO_021U_STR', { ...masterData, ACTKIND: 'S0' });
    if (grid.value) { grid.value.setData(res.data); setTimeout(updateTotals, 100); }
  } catch (e) { vAlertError('상세 로드 실패') }
}

async function handleImportAnalysis() {
  if (!masterData.DEPTCD) return vAlertError('요청부서를 먼저 선택하세요.');
  vAlert('소요량 데이터를 분석하여 가져옵니다...');
  try {
    const res = await api.post('/api/hsio/HSIO_020U_STR', { ...masterData, ACTKIND: 'S1' });
    if (grid.value && res.data) {
      grid.value.setData(res.data.map((i: any) => ({ ...i, UPKIND: 'A' })));
      setTimeout(updateTotals, 100);
      vAlert(`${res.data.length}건의 소요량이 분석되었습니다.`);
    }
  } catch (e) { vAlertError('가져오기 실패') }
}

function addRow() { grid.value?.addRow({ UPKIND: 'A', REQQTY: 0, IMPRICE: 0, REQAMT: 0 }); updateTotals(); }

async function save() {
  if (activeItemCount.value === 0) return vAlertError('저장할 품목이 없습니다.');
  try {
    const act = (masterData.REQNO === '0000' || !masterData.REQNO) ? 'A0' : 'U0';
    const mRes = await api.post('/api/hsio/HSIO_020U_STR', { ...masterData, ACTKIND: act, UPDEMP: authStore.USERID });
    if (mRes.data?.length) {
      const newNo = mRes.data[0].REQNO;
      const items = grid.value!.getData();
      for (const item of items) {
        await api.post('/api/hsio/HSIO_021U_STR', { ...item, ACTKIND: (item.UPKIND || 'U') + '0', CMPYCD: authStore.CMPYCD, REQYM: masterData.REQYM, REQNO: newNo, DEPTCD: masterData.DEPTCD, UPDEMP: authStore.USERID });
      }
      vAlert('저장되었습니다.'); masterData.REQNO = newNo; fetchMaster();
    }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

async function deleteRequest() {
  if (!confirm('요청 자료를 삭제하시겠습니까?')) return;
  try {
    await api.post('/api/hsio/HSIO_020U_STR', { ...masterData, ACTKIND: 'D0' });
    vAlert('삭제되었습니다.'); initialize();
  } catch (e) { vAlertError('삭제 실패') }
}

function initialize() {
  resetForm(masterData);
  Object.assign(masterData, { ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, REQYM: initYM, REQNO: '0000', REQYMD: initYMD, INYMD: initYMD, DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, ASGBN: 'Y', USERID: String(authStore.USERID || '').trim() });
  grid.value?.clearData(); updateTotals();
}

function openHelp(type: string) {
  const commonProps = { path: '/api/ha00/HA00_00P_STR', CMPYCD: authStore.CMPYCD };
  if (type === 'DEPT' || type === 'S_DEPT') {
    Object.assign(modalProps, { title: '부서 선택', ...commonProps, data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 200 }, { title: '상위부서', field: 'UDEPTNM', width: 150 }], onConfirm: (d: any) => { if (type === 'S_DEPT') { searchParam.DEPTCD = d.DEPTCD; searchParam.DEPTNM = d.DEPTNM } else { masterData.DEPTCD = d.DEPTCD; masterData.DEPTNM = d.DEPTNM } } });
  }
  modalVisible.value = true;
}

function formatNumber(v: any) { return new Intl.NumberFormat().format(Number(v) || 0) }

onMounted(() => {
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'SD', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data) {
      empOptions.value = r.data.map((i: any) => ({
        USERID: String(i.USERID || i.userid || Object.values(i)[0]).trim(),
        USERNM: String(i.USERNM || i.usernm || Object.values(i)[1]).trim()
      }))
    }
  })
  nextTick(() => { initGrid(); updateTotals() })
})
</script>

<style scoped>
.hsio020u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f8f9fa; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }

/* 💎 HPIO200U 스타일 기반 행추가 버튼 표준화 */
.btn-grid-row-add { height: 28px; padding: 0 15px; border-radius: 4px; font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: 1px solid #4361ee; background-color: #fff; color: #4361ee; }
.btn-grid-row-add:hover { background-color: #eef2ff; color: #374fc7; border-color: #374fc7; }

.search-bar { background-color: #f1f5f9 !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; background-color: #fff; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 8px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 10px !important; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
.bg-yellow { background-color: #fffde7 !important; }

:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 700 !important; }
</style>
