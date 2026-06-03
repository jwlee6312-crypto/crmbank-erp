<!--
	=============================================================
	프로그램명	: 매출반품등록 (HSIO490U)
	작성일자	: 2025.02.24
	설명        : 영업 매출반품 마스터/상세 관리 (HSOD100U 디자인 UI + HSIO580U 좌측그리드 이식)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-arrow-return-left me-2 text-primary" style="font-size: 18px;"></i>
        영업관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">매출반품등록 (HSIO490U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save" :disabled="isClosed">저장</button>
        <button class="btn-erp btn-delete" @click="handleFullDelete" :disabled="!form_02.iono || isClosed">전체삭제</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 40%" />
                <col style="width: 10%" /><col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">조회일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="form_01.fromdt" v-model:todt="form_01.todt" />
                </td>
                <th class="text-center bg-light">거래처명</th>
                <td>
                  <input v-model="form_01.schcustnm" class="form-control form-control-sm" placeholder="매출거래처 검색" @keyup.enter="search" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 투-그리드 레이아웃 영역 -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 반품 목록 (HSIO580U 스타일) -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">반품 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ➡️ 우측: 마스터 상세 폼 + 품목 상세 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

          <!-- 상세 마스터 정보 폼 -->
          <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup>
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light">출고부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.deptnm" class="form-control" readonly />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')" :disabled="isClosed"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light text-center">반품번호</th>
                    <td><input v-model="form_02.iono" class="form-control bg-light text-primary fw-bold text-center" readonly placeholder="자동생성" /></td>
                    <th class="required bg-light text-center">반품일자</th>
                    <td><input v-model="form_02.ioymd" type="date" class="form-control" :readonly="isClosed" /></td>
                    <th class="required bg-light text-center">담당자</th>
                    <td>
                      <select v-model="form_02.userid" class="form-select" :disabled="isClosed">
                        <option value="">선택</option>
                        <option v-for="item in userData" :key="item.userid" :value="item.userid">{{ item.usernm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light">매출거래처</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.custnm" class="form-control" readonly />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')" :disabled="isClosed"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light text-center">입고창고</th>
                    <td>
                      <select v-model="form_02.whcd" class="form-select" :disabled="isClosed">
                        <option v-for="item in whcdData" :key="item.CODE" :value="item.CODE">{{ item.cdnm }}</option>
                      </select>
                    </td>
                    <th class="bg-light text-center">특이사항</th>
                    <td><input v-model="form_02.remark" class="form-control" :readonly="isClosed" /></td>
                    <th class="bg-light text-center">합계금액</th>
                    <td><input v-model="form_02.totsum" class="form-control bg-light text-end fw-bold" readonly /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 상세 품목 그리드 영역 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>반품 품목 리스트</span>
              <div class="d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" :disabled="isClosed" style="font-size: 12px;">+ 행추가</button>
                <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" :disabled="isClosed" style="font-size: 12px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1"></div>
            </div>
          </div>
        </div>
      </div>
      <div class="bottom-spacer"></div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, onUnmounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { getDate } from '@/composables/useDate'
import { useSearchStore } from '@/stores/useSearchStore'
import { useRoute } from 'vue-router'

const authStore = useAuthStore()
const searchStore = useSearchStore()
const route = useRoute()
const { firstDay, today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

// [1] 데이터 모델링
const form_01 = reactive({ fromdt: firstDay, todt: today, schcustnm: '' })
const form_02 = reactive<any>({
  deptcd: authStore.deptCode, deptnm: authStore.deptName,
  iono: '', ioymd: today,
  custcd: '', custnm: '', whcd: '100', userid: authStore.userId,
  remark: '', totsum: 0
})

const closingInfo = reactive({ sclsym: '' })
const whcdData = ref<any[]>([])
const userData = ref<any[]>([])

// [2] 그리드 관리
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null
const MIN_ROWS = 12;

const isClosed = computed(() => {
  if (!closingInfo.sclsym || !form_02.ioymd) return false
  return form_02.ioymd.replace(/-/g, '').substring(0, 6) <= closingInfo.sclsym
})

const initGrids = () => {
  if (!tableRef1.value || !tableRef2.value) return

  // ⬅️ 좌측 그리드
  grid1 = new Tabulator(tableRef1.value, {
    layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
      { title: "거래처명", field: "custnm", hozAlign: "left", headerSort: false },
      { title: "반품번호", field: "iono", hozAlign: "center", width: 110, cssClass: "fw-bold text-primary", headerSort: false }
    ],
  });
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()));

  // ➡️ 우측 그리드
  grid2 = new Tabulator(tableRef2.value, {
    layout: "fitColumns", height: "100%", placeholder: "품목 없음",
    columnCalcs: "table", selectable: true,
    selectableCheck: (row) => row.getData()._STATE !== 'EMPTY',
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "선택", width: 40, hozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection", headerSort: false, cellClick: (e) => e.stopPropagation() },
      { title: "상태", field: "state", width: 50, hozAlign: "center",
        formatter: (c) => {
          const v = c.getValue();
          if (v === 'C') return '<span class="badge bg-primary" style="font-size:10px;">신규</span>';
          if (v === 'U') return '<span class="badge bg-warning text-dark" style="font-size:10px;">수정</span>';
          if (v === 'D') return '<span class="badge bg-danger" style="font-size:10px;">삭제</span>';
          return '';
        }
      },
      { title: "No", formatter: (cell) => cell.getData()._STATE === 'EMPTY' ? "" : cell.getRow().getPosition(), width: 40, hozAlign: "center" },
      { title: "품목명", field: "itemnm", minWidth: 180, widthGrow: 1, cssClass: 'fw-bold text-primary',
        cellClick: (e, cell) => { if(!isClosed.value) handleOpenHelp('ITEM', cell.getRow()) }
      },
      { title: "규격", field: "itsize", width: 120 },
      { title: "단위", field: "unit", width: 60, hozAlign: "center" },
      { title: "재고", field: "stock", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "수량", field: "ioqty", width: 90, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()), bottomCalc: "sum" },
      { title: "단가", field: "price", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "공급가", field: "ioamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
      { title: "부가세", field: "iovat", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
      { title: "합계", field: "totamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
      { title: "삭제", width: 40, hozAlign: "center",
        formatter: (cell) => cell.getData()._STATE === 'EMPTY' ? "" : "<i class='bi bi-trash text-danger cursor-pointer'></i>",
        cellClick: (e, cell) => handleRowAction(cell.getRow())
      }
    ],
    rowFormatter: (row) => { if (row.getData()._STATE === 'EMPTY') row.getElement().classList.add("empty-row-style"); }
  });
}

// [3] 로직 처리
const setGridDataWithPadding = (data: any[]) => {
  const displayData = data.map(i => ({ ...i, _STATE: 'EXIST' }));
  while (displayData.length < MIN_ROWS) {
    displayData.push({ _STATE: 'EMPTY', ioqty: 0, price: 0, ioamt: 0, iovat: 0, totamt: 0 });
  }
  grid2?.setData(displayData);
}

const calcRow = (row: any) => {
  const data = row.getData();
  if (data._STATE === 'EMPTY') return;

  const amt = Math.floor(Number(data.ioqty || 0) * Number(data.price || 0));
  const vat = Math.floor(amt * 0.1);
  row.update({
    ioamt: amt, iovat: vat, totamt: amt + vat,
    state: data.iorowno ? 'U' : 'C'
  });
  updateTotalSum();
}

const updateTotalSum = () => {
  const data = grid2?.getData().filter(r => r._STATE !== 'EMPTY' && r.state !== 'D') || [];
  form_02.totsum = data.reduce((sum, r) => sum + Number(r.totamt || 0), 0);
}

const handleOpenHelp = (type: string, target?: any) => {
  if (isClosed.value) return;

  if (type === 'DEPT') {
    openHelp('DEPT', (d) => { form_02.deptcd = d.deptcd; form_02.deptnm = d.deptnm });
  } else if (type === 'CUST') {
    openHelp('CUST', (d) => { form_02.custcd = d.custcd; form_02.custnm = d.custnm });
  } else if (type === 'ITEM') {
    openHelp('ITEM', (d) => {
      target.update({
        itemcd: d.itemcd, itemnm: d.itemnm, itsize: d.itsize, unit: d.unit, stock: d.stock || 0,
        price: d.outprice || 0, ioqty: 1, state: 'C', _STATE: 'NEW'
      });
      calcRow(target);
    });
  }
}

const handleRowAction = (row: any) => {
  const data = row.getData();
  if (data._STATE === 'EMPTY') return;
  if (!data.iorowno) row.delete();
  else row.update({ state: data.state === 'D' ? 'U' : 'D' });
  updateTotalSum();
}

async function search() {
  try {
    const params = { fromdt: form_01.fromdt.replace(/-/g, ''), todt: form_01.todt.replace(/-/g, ''), schcustnm: form_01.schcustnm }
    const res = await api.post('/api/hsio/HSIO_490U_STR', params);
    grid1?.setData(res.data);
    vAlert('조회되었습니다.');
  } catch (e) { vAlertError('조회 실패'); }
}

async function fetchDetail(row: any) {
  Object.assign(form_02, row);
  try {
    const res = await api.post('/api/hsio/HSIO_491U_STR', { iono: row.iono });
    setGridDataWithPadding(res.data);
    updateTotalSum();
  } catch (e) { vAlertError('상세 로드 실패'); }
}

async function save() {
  const details = grid2?.getData().filter(r => r._STATE !== 'EMPTY' && r.state) || [];
  if (!details.length) return vAlertError('저장할 품목이 없습니다.');

  const payload = {
    inout_MstDto: { ...form_02, ioymd: form_02.ioymd.replace(/-/g, '') },
    inout_DtlDto: details
  };

  try {
    await api.post('/api/hsio/HSIO_490U_SAVE', payload);
    vAlert('저장되었습니다.');
    search();
  } catch (e) { vAlertError('저장 실패'); }
}

function addRow() {
  if (isClosed.value) return;
  grid2?.addRow({ state: 'C', ioqty: 0, price: 0, ioamt: 0, iovat: 0, totamt: 0, _STATE: 'NEW' }, true);
}

function deleteSelectedRows() {
  const selected = grid2?.getSelectedRows();
  if (!selected?.length) return vAlertError('삭제할 행을 선택하십시오.');
  selected.forEach(row => handleRowAction(row));
}

function initialize() {
  resetForm(form_02);
  form_02.deptcd = authStore.deptCode; form_02.deptnm = authStore.deptName;
  form_02.ioymd = today; form_02.whcd = '100'; form_02.userid = authStore.userId;
  form_02.totsum = 0;
  grid1?.clearData(); setGridDataWithPadding([]);
}

async function handleFullDelete() {
  if (!form_02.iono) return;
  if (!confirm(`반품번호 ${form_02.iono}를 삭제하시겠습니까?`)) return;
  try {
    await api.post('/api/hsio/HSIO_490U_DELETE', { iono: form_02.iono });
    vAlert('삭제되었습니다.');
    initialize(); search();
  } catch (e) { vAlertError('삭제 실패'); }
}

onMounted(async () => {
  api.post('/api/hs00/HS00_000S_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '030', CODE: '', codenm: '' }).then(r => {
    whcdData.value = r.data.status === 200 ? r.data.data : r.data;
  });
  api.get('/api/user').then(res => { userData.value = res.data; });
  api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => {
    if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym;
  });
  initGrids(); initialize();
})

onUnmounted(() => { searchStore.removeTab(route.name as string) });
</script>

<style scoped>
.main-content-wrapper { padding-bottom: 0px !important; }
.grid-container-left, .grid-container-right { border-bottom: 3px solid #005a9f !important; }
.erp-table-dense th, .erp-table-dense td {
  height: 32px !important;
  padding: 0 8px !important;
  font-size: 12px;
  vertical-align: middle;
  border: 1px solid #dee2e6;
}
.erp-table-dense .form-control, .erp-table-dense .form-select, .erp-table-dense .btn {
  height: 26px !important;
  font-size: 12px !important;
  border-radius: 2px;
}
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.empty-row-style) { opacity: 0.6; }
</style>
