<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
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
        <button class="btn-erp btn-danger" @click="deleteRequest" :disabled="!masterData.reqno || masterData.reqno === '0000'">삭제</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 (균형 배치) -->
    <div class="search-bar bg-light border-bottom p-2 px-3 d-flex align-items-center shadow-sm">
      <div class="d-flex align-items-center gap-4 flex-grow-1">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 요청부서:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchParam.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchParam.deptnm" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="fetchMaster" />
            <button class="btn btn-outline-secondary" @click="openHelp('S_DEPT')"><i class="bi bi-search"></i></button>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 요청번호:</span>
          <div class="d-flex align-items-center gap-1">
            <input v-model="searchParam.reqym" type="month" class="form-control form-control-sm text-center" style="width: 130px;" />
            <input v-model="searchParam.reqno" type="text" class="form-control form-control-sm text-center" placeholder="0000" style="width: 60px;" @keyup.enter="fetchMaster" />
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
                      <input v-model="masterData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                      <input v-model="masterData.deptnm" type="text" class="form-control" @keyup.enter="openHelp('DEPT')" />
                      <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                    </div>
                  </td>
                  <th class="required">요청번호</th>
                  <td>
                    <div class="d-flex align-items-center gap-1 w-100">
                      <input v-model="reqym" type="month" class="form-control form-control-sm text-center fw-bold bg-light" readonly style="flex: 2;" />
                      <input v-model="masterData.reqno" type="text" class="form-control form-control-sm text-center fw-bold text-primary bg-light" style="flex: 1;" readonly />
                    </div>
                  </td>
                  <th class="required">요청일자</th>
                  <td>
                    <input v-model="reqymd" type="date" class="form-control form-control-sm w-100" />
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
                    <input v-model="inymd" type="date" class="form-control form-control-sm w-100" />
                  </td>
                  <th class="required">담&nbsp;&nbsp;당&nbsp;&nbsp;자</th>
                  <td>
                    <select v-model="masterData.userid" class="form-select form-select-sm w-100">
                      <option value="">-- 선택 --</option>
                      <option v-for="item in empOptions" :key="item.userid" :value="item.userid">
                        {{ item.usernm }}
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
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridElement" class="tabulator-instance flex-grow-1"></div>
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
const initym = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`;
const initymd = `${initym}${String(now.getDate()).padStart(2, '0')}`;

const searchParam = reactive({ deptcd: authStore.deptcd, deptnm: authStore.deptnm, reqym: initym, reqno: '' })
const masterData = reactive<any>({
  actkind: 'S0', cmpycd: authStore.cmpycd, reqym: initym, reqno: '0000',
  reqymd: initymd, inymd: initymd, deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  remark: '', asgbn: 'Y', userid: String(authStore.userid || '').trim()
})

const reqym = computed({ get: () => masterData.reqym ? `${masterData.reqym.substring(0, 4)}-${masterData.reqym.substring(4, 6)}` : '', set: (v) => masterData.reqym = v.replace('-', '') })
const reqymd = computed({ get: () => masterData.reqymd ? `${masterData.reqymd.substring(0, 4)}-${masterData.reqymd.substring(4, 6)}-${masterData.reqymd.substring(6, 8)}` : '', set: (v) => masterData.reqymd = v.replace(/-/g, '') })
const inymd = computed({ get: () => masterData.inymd ? `${masterData.inymd.substring(0, 4)}-${masterData.inymd.substring(4, 6)}-${masterData.inymd.substring(6, 8)}` : '', set: (v) => masterData.inymd = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null);
const grid = ref<Tabulator | null>(null);
const activeItemCount = ref(0);
const amttotal = ref(0);
const empOptions = ref<any[]>([])

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "분석된 소요량 데이터가 없습니다.",
    columnDefaults: { minWidth: 90, headerHozAlign: 'center' },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, headerSort: false, cssClass: "fw-bold" },
      { title: "규격", field: "itsize", width: 120, headerSort: false },
      { title: "단위", field: "unit", width: 60, hozAlign: "center", headerSort: false },
      { title: "소요량", field: "soqty", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "현재고", field: "stkqty", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary" },
      { title: "안전재고", field: "stock", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미입고", field: "nonqty", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-danger" },
      { title: "요청수량", field: "reqqty", width: 90, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-yellow fw-bold" },
      { title: "구매단가", field: "imprice", width: 100, hozAlign: "right", editor: "number", formatter: "money" },
      { title: "금액", field: "reqamt", width: 110, hozAlign: "right", formatter: "money", bottomCalc: "sum", bottomCalcFormatter: "money" },
      { title: "거래처명", field: "custnm", width: 150 },
      { title: "발주번호", field: "balno", width: 100, hozAlign: "center", cssClass: "small text-muted" },
      { title: "", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>", cellClick: (e, c) => { c.getRow().delete(); updateTotals() } }
    ]
  });

  grid.value.on("cellEdited", (cell: any) => {
    const row = cell.getRow(); const d = row.getData();
    if (['reqqty', 'imprice'].includes(cell.getField())) {
      const amt = Math.floor((Number(d.reqqty) || 0) * (Number(d.imprice) || 0));
      row.update({ reqamt: amt, upkind: d.upkind === 'A' ? 'A' : 'U' });
    }
    updateTotals();
  });
}

const updateTotals = () => {
  if (!grid.value) return;
  const data = grid.value.getData();
  activeItemCount.value = data.length;
  amttotal.value = data.reduce((acc, i) => acc + (Number(i.reqamt) || 0), 0);
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

async function fetchMaster() {
  if (!searchParam.reqym || !searchParam.reqno) return vAlertError('조회할 요청번호를 입력하세요.');
  try {
    const res = await api.post('/api/hsio/HSIO_020U_STR', { ...searchParam, actkind: 'S0', cmpycd: authStore.cmpycd });
    if (res.data?.length) {
      Object.assign(masterData, res.data[0]);
      fetchDetail();
    } else { vAlertError('조회 결과가 없습니다.') }
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetail() {
  try {
    const res = await api.post('/api/hsio/HSIO_021U_STR', { ...masterData, actkind: 'S0' });
    if (grid.value) { grid.value.setData(res.data); setTimeout(updateTotals, 100); }
  } catch (e) { vAlertError('상세 로드 실패') }
}

async function handleImportAnalysis() {
  if (!masterData.deptcd) return vAlertError('요청부서를 먼저 선택하세요.');
  vAlert('소요량 데이터를 분석하여 가져옵니다...');
  try {
    const res = await api.post('/api/hsio/HSIO_020U_STR', { ...masterData, actkind: 'S1' });
    if (grid.value && res.data) {
      grid.value.setData(res.data.map((i: any) => ({ ...i, upkind: 'A' })));
      setTimeout(updateTotals, 100);
      vAlert(`${res.data.length}건의 소요량이 분석되었습니다.`);
    }
  } catch (e) { vAlertError('가져오기 실패') }
}

function addRow() { grid.value?.addRow({ upkind: 'A', reqqty: 0, imprice: 0, reqamt: 0 }); updateTotals(); }

async function save() {
  if (activeItemCount.value === 0) return vAlertError('저장할 품목이 없습니다.');
  try {
    const act = (masterData.reqno === '0000' || !masterData.reqno) ? 'A0' : 'U0';
    const mRes = await api.post('/api/hsio/HSIO_020U_STR', { ...masterData, actkind: act, updemp: authStore.userid });
    if (mRes.data?.length) {
      const newNo = mRes.data[0].reqno;
      const items = grid.value!.getData();
      for (const item of items) {
        await api.post('/api/hsio/HSIO_021U_STR', { ...item, actkind: (item.upkind || 'U') + '0', cmpycd: authStore.cmpycd, reqym: masterData.reqym, reqno: newNo, deptcd: masterData.deptcd, updemp: authStore.userid });
      }
      vAlert('저장되었습니다.'); masterData.reqno = newNo; fetchMaster();
    }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

async function deleteRequest() {
  if (!confirm('요청 자료를 삭제하시겠습니까?')) return;
  try {
    await api.post('/api/hsio/HSIO_020U_STR', { ...masterData, actkind: 'D0' });
    vAlert('삭제되었습니다.'); initialize();
  } catch (e) { vAlertError('삭제 실패') }
}

function initialize() {
  resetForm(masterData);
  Object.assign(masterData, { actkind: 'S0', cmpycd: authStore.cmpycd, reqym: initym, reqno: '0000', reqymd: initymd, inymd: initymd, deptcd: authStore.deptcd, deptnm: authStore.deptnm, asgbn: 'Y', userid: String(authStore.userid || '').trim() });
  grid.value?.clearData(); updateTotals();
}

function openHelp(type: string) {
  const commonProps = { path: '/api/ha00/HA00_00P_STR', cmpycd: authStore.cmpycd };
  if (type === 'DEPT' || type === 'S_DEPT') {
    Object.assign(modalProps, { title: '부서 선택', ...commonProps, data: { gubun: 'D0', cmpycd: authStore.cmpycd }, columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 200 }, { title: '상위부서', field: 'Udeptnm', width: 150 }], onConfirm: (d: any) => { if (type === 'S_DEPT') { searchParam.deptcd = d.deptcd; searchParam.deptnm = d.deptnm } else { masterData.deptcd = d.deptcd; masterData.deptnm = d.deptnm } } });
  }
  modalVisible.value = true;
}

function formatNumber(v: any) { return new Intl.NumberFormat().format(Number(v) || 0) }

onMounted(() => {
  api.get('/api/ha00/HA00_00P_STR', { params: { gubun: 'SD', cmpycd: authStore.cmpycd } }).then(r => {
    if (r.data) {
      empOptions.value = r.data.map((i: any) => ({
        userid: String(i.userid || i.userid || Object.values(i)[0]).trim(),
        usernm: String(i.usernm || i.usernm || Object.values(i)[1]).trim()
      }))
    }
  })
  nextTick(() => { initGrid(); updateTotals() })
})
</script>
