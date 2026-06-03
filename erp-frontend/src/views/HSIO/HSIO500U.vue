<!--
	=============================================================
	프로그램명	: 무주문 출고등록 (HSIO500U)
	작성일자	: 2025.02.24
	설명        : 주문 없이 직접 출고 데이터를 등록/관리 (소문자 표준화 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
          <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
            <i class="bi bi-cart-check-fill me-2 text-primary" style="font-size: 18px;"></i> 영업관리
            <i class="bi bi-chevron-right mx-1 small opacity-50"></i> 출고관리
            <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
            <span class="text-primary fw-bolder text-nowrap">무주문 출고등록</span>
        </div>
        <div class="btn-group-erp d-flex gap-1 pe-3">
            <button class="btn-erp btn-init" @click="initialize">초기화</button>
            <button class="btn-erp btn-search" @click="search">조회</button>
            <button class="btn-erp btn-save" @click="save">저장</button>
            <button class="btn-erp btn-delete" @click="handleFullDelete" :disabled="!form_02.out_no">전체삭제</button>
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
                <th class="text-center bg-light">판매부서</th>
                <td>
                  <div class="input-group input-group-sm w-50">
                    <input v-model="form_01.deptnm" class="form-control" readonly />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT_SCH')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">출고번호</th>
                <td>
                  <input v-model="form_01.out_no" class="form-control form-control-sm" placeholder="출고번호 입력" @keyup.enter="search" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 투-그리드 레이아웃 영역 -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 출고 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">출고 목록</div>
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
                    <th class="required bg-light">판매부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.deptnm" class="form-control" readonly />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light text-center">출고번호</th>
                    <td><input v-model="form_02.out_no" class="form-control bg-light text-primary fw-bold text-center" readonly placeholder="자동생성" /></td>
                    <th class="required bg-light text-center">출고일자</th>
                    <td><input v-model="form_02.outymd" type="date" class="form-control" /></td>
                    <th class="required bg-light text-center">출고창고</th>
                    <td>
                      <select v-model="form_02.whcd" class="form-select">
                        <option v-for="item in whcd_codes" :key="item.code" :value="item.code">{{ item.cdnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light">거래처</th>
                    <td colspan="3">
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.custnm" class="form-control" readonly />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light text-center">영업담당</th>
                    <td>
                      <select v-model="form_02.sale_userid" class="form-select">
                        <option v-for="item in userData" :key="item.userid" :value="item.userid">{{ item.usernm }}</option>
                      </select>
                    </td>
                    <th class="bg-light text-center">배송담당</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="form_02.dlv_usernm" class="form-control" readonly />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('EMP_DLV')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light">배송처</th>
                    <td colspan="3">
                      <AddressPopupForm
                        v-model:postno="form_02.postno"
                        v-model:address="form_02.address"
                        v-model:d_address="form_02.d_address"
                        @open-address="handleOpenHelp('ADDR')"
                      />
                    </td>
                    <th class="required bg-light text-center">현금조건</th>
                    <td>
                      <select v-model="form_02.paycndt" class="form-select">
                        <option value="1">현금</option>
                        <option value="2">외상</option>
                      </select>
                    </td>
                    <th class="bg-light text-center">만기일자</th>
                    <td><input v-model="form_02.endymd" type="date" class="form-control" /></td>
                  </tr>
                  <tr>
                    <th class="bg-light text-center">특기사항</th>
                    <td colspan="5"><input v-model="form_02.remark" class="form-control" /></td>
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
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>출고 품목 리스트</span>
              <div class="d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 12px;">+ 행추가</button>
                <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 12px;">- 행삭제</button>
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
import AddressPopupForm from '@/components/AddressPopupForm.vue'
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
const { today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

// [1] 데이터 모델링 (원칙: 완전 소문자 표준)
const form_01 = reactive({ deptcd: authStore.deptcd, deptnm: authStore.deptnm, out_no: '' })
const form_02 = reactive<any>({
  cmpycd: authStore.cmpycd, deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  out_no: '', outymd: today, whcd: '100', custcd: '', custnm: '',
  sale_userid: authStore.userid, dlv_userid: '', dlv_usernm: '',
  paycndt: '1', postno: '', address: '', d_address: '', addrcd: '',
  endymd: today, remark: '', totsum: 0
})

const closingInfo = reactive({ sclsym: '' })
const whcd_codes = ref<any[]>([])
const userData = ref<any[]>([])

// [2] 그리드 관리
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null
const MIN_ROWS = 12;

const isClosed = computed(() => {
  if (!closingInfo.sclsym || !form_02.outymd) return false
  return form_02.outymd.replace(/-/g, '').substring(0, 6) <= closingInfo.sclsym
})

const initGrids = () => {
  if (!tableRef1.value || !tableRef2.value) return

  grid1 = new Tabulator(tableRef1.value, {
    layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
      { title: "출고번호", field: "iono", hozAlign: "center", width: 120, cssClass: "fw-bold text-primary", headerSort: false },
      { title: "거래처", field: "custnm", hozAlign: "left", headerSort: false }
    ],
  });
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()));

  grid2 = new Tabulator(tableRef2.value, {
    layout: "fitColumns", height: "100%", placeholder: "품목 없음",
    columnCalcs: "table", selectable: true,
    selectableCheck: (row) => row.getData()._state !== 'EMPTY',
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "선택", width: 40, hozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection", headerSort: false, cellClick: (e) => e.stopPropagation() },
      { title: "상태", field: "_status", width: 50, hozAlign: "center",
        formatter: (c) => {
          const v = c.getValue();
          if (v === '신규') return '<span class="badge bg-primary" style="font-size:10px;">신규</span>';
          if (v === '수정') return '<span class="badge bg-warning text-dark" style="font-size:10px;">수정</span>';
          if (v === '삭제') return '<span class="badge bg-danger" style="font-size:10px;">삭제</span>';
          return '';
        }
      },
      { title: "No", formatter: (cell) => cell.getData()._state === 'EMPTY' ? "" : cell.getRow().getPosition(), width: 40, hozAlign: "center" },
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary',
        cellClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow())
      },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unitnm", width: 80, hozAlign: "center" },
      { title: "수량", field: "qty", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()), bottomCalc: "sum" },
      { title: "단가", field: "price", width: 120, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "금액", field: "amt", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
      { title: "부가세", field: "vat", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
      { title: "합계", field: "sumamt", width: 140, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
      { title: "삭제", width: 40, hozAlign: "center",
        formatter: (cell) => cell.getData()._state === 'EMPTY' ? "" : "<i class='bi bi-trash text-danger cursor-pointer'></i>",
        cellClick: (e, cell) => handleRowAction(cell.getRow())
      }
    ],
    rowFormatter: (row) => { if (row.getData()._state === 'EMPTY') row.getElement().classList.add("empty-row-style"); }
  });
}

// [3] 로직 처리
const setGridDataWithPadding = (data: any[]) => {
  const displayData = data.map(i => ({ ...i, _state: 'EXIST', _status: '' }));
  while (displayData.length < MIN_ROWS) {
    displayData.push({ _state: 'EMPTY', qty: 0, price: 0, amt: 0, vat: 0, sumamt: 0 });
  }
  grid2?.setData(displayData);
}

const calcRow = (row: any) => {
  const data = row.StandardizedData();
  if (data._state === 'EMPTY') return;

  const amt = Math.round(Number(data.qty || 0) * Number(data.price || 0));
  const vat = Math.round(amt * 0.1);
  row.update({
    amt: amt, vat: vat, sumamt: amt + vat
  });
  if (data._state === 'EXIST' && data._status !== '삭제') row.update({ _status: '수정' });
  updateTotalSum();
}

const updateTotalSum = () => {
  const data = grid2?.getData().filter(r => r._state !== 'EMPTY' && r._status !== '삭제') || [];
  form_02.totsum = data.reduce((sum, r) => sum + Number(r.sumamt || 0), 0);
}

const handleOpenHelp = (type: string, target?: any) => {
  if (type === 'DEPT_SCH') openHelp('DEPT', (d) => { form_01.deptcd = d.deptcd; form_01.deptnm = d.deptnm });
  else if (type === 'DEPT') openHelp('DEPT', (d) => { form_02.deptcd = d.deptcd; form_02.deptnm = d.deptnm });
  else if (type === 'CUST') openHelp('CUST', (d) => { form_02.custcd = d.custcd; form_02.custnm = d.custnm });
  else if (type === 'EMP_DLV') openHelp('EMP', (d) => { form_02.dlv_userid = d.userid; form_02.dlv_usernm = d.usernm });
  else if (type === 'ADDR') {
    openHelp('ADDR', (d) => {
        form_02.postno = d.postno;
        form_02.address = d.address;
        form_02.addrcd = d.trancd;
    }, { custcd: form_02.custcd });
  }
  else if (type === 'ITEM') {
    openHelp('ITEM', (d) => {
      target.update({
        itemcd: d.itemcd, itemnm: d.itemnm, itsize: d.itsize, unitnm: d.unitnm,
        price: d.outcost || 0, qty: 1, _status: '신규', _state: 'NEW'
      });
      calcRow(target);
    });
  }
}

const handleRowAction = (row: any) => {
  const data = row.getData();
  if (data._state === 'EMPTY') return;
  if (data._state === 'NEW') row.delete();
  else row.update({ _status: data._status === '삭제' ? '' : '삭제' });
  updateTotalSum();
}

async function search() {
  try {
    const res = await api.post('/api/hsio/HSIO_500U_STR', form_01);
    const list = res.data.data || (Array.isArray(res.data) ? res.data : []);
    grid1?.setData(list);
    vAlert('조회되었습니다.');
  } catch (e) { vAlertError('조회 실패'); }
}

async function fetchDetail(row: any) {
  Object.assign(form_02, row);
  try {
    const res = await api.post(`/api/hsio/HSIO_501U_STR`, { out_no: row.out_no });
    const list = res.data.data || (Array.isArray(res.data) ? res.data : []);
    setGridDataWithPadding(list);
    updateTotalSum();
  } catch (e) { vAlertError('상세 로드 실패'); }
}

async function save() {
  if (isClosed.value) return vAlertError('마감된 월입니다.');
  if (!form_02.custcd) return vAlertError('거래처를 선택하세요.');

  const details = grid2?.getData().filter(r => r._state !== 'EMPTY' && r._status) || [];
  if (!details.length && !form_02.out_no) return vAlertError('저장할 품목이 없습니다.');

  try {
    await api.post('/api/hsio/HSIO_500U_SAVE', { mst: form_02, dtl: details });
    vAlert('저장되었습니다.'); search();
  } catch (e) { vAlertError('저장 실패'); }
}

function addRow() {
  grid2?.addRow({ _status: '신규', qty: 0, price: 0, amt: 0, vat: 0, sumamt: 0, _state: 'NEW' }, true);
}

function deleteSelectedRows() {
  const selected = grid2?.getSelectedRows();
  if (!selected?.length) return vAlertError('삭제할 행을 선택하십시오.');
  selected.forEach(row => handleRowAction(row));
}

function initialize() {
  resetForm(form_02);
  form_02.cmpycd = authStore.cmpycd;
  form_02.outymd = today; form_02.endymd = today; form_02.whcd = '100';
  form_02.deptcd = authStore.deptcd; form_02.deptnm = authStore.deptnm;
  form_02.sale_userid = authStore.userid;
  form_02.totsum = 0;
  grid1?.clearData(); setGridDataWithPadding([]);
}

async function handleFullDelete() {
  if (!form_02.out_no) return;
  if (isClosed.value) return vAlertError('마감된 월입니다.');
  if (!confirm('정말 전체 삭제하시겠습니까?')) return;
  try {
    await api.post('/api/hsio/HSIO_500U_SAVE', { mst: { ...form_02, actkind: 'D0' }, dtl: [] });
    vAlert('삭제되었습니다.'); initialize(); search();
  } catch (e) { vAlertError('삭제 실패'); }
}

onMounted(async () => {
  api.post('/api/hs00/hs00_000s_str', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '030', code: '', codenm: '' }).then(r => { whcd_codes.value = r.data; });
  api.post('/api/ha00/ha00_00p_str', { gubun: 'SD', cmpycd: authStore.cmpycd }).then(r => { userData.value = r.data; });
  api.get('/api/hp00/hp00_000s_str', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => {
    if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym;
  });
  initGrids(); initialize();
})

onUnmounted(() => { searchStore.removeTab(route.name as string) });
</script>
<style scoped>
.main-content-wrapper { padding-bottom: 1vh !important; }
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
