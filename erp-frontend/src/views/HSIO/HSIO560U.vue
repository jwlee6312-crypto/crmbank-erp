<!--
	=============================================================
	프로그램명	: 출고취소 (HSIO560U)
	작성일자	: 2025.02.24
	설명        : 출고 완료된 내역을 선택하여 취소 처리 (ASP 패턴 기반 순차 저장 로직 및 소문자 통일)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-x-circle-fill me-2 text-danger" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-danger fw-bolder">출고취소 (HSIO560U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-save" @click="saveCancel" :disabled="activeItemCount === 0">취소처리</button>
      </div>
    </div>

    <!-- 🔍 2. 최상단 조회 조건 바 -->
    <div class="p-2 pb-0 flex-shrink-0 bg-light">
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body py-2 px-3 bg-white">
          <div class="d-flex align-items-center gap-4">
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-dark" style="min-width: 60px;">출고창고</span>
              <select v-model="searchForm.whcd" class="form-select form-select-sm" style="width: 150px;">
                <option v-for="opt in whOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
              </select>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-dark" style="min-width: 60px;">출고일자</span>
              <DateForm v-model:fromdt="searchForm.outymdfr" v-model:todt="searchForm.outymdto" />
            </div>
            <button class="btn btn-sm btn-dark px-4 fw-bold ms-auto" @click="fetchCustList">
              <i class="bi bi-search me-1"></i>조회
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 💡 3. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex gap-2 bg-light">

      <!-- 🅰️ 좌측: 출고된 상호 목록 -->
      <div class="card border shadow-sm d-flex flex-column flex-shrink-0" style="width: 320px;">
        <div class="card-header bg-white py-1 px-3 border-bottom">
          <span class="fw-bold small text-dark"><i class="bi bi-list-check me-1 text-primary"></i> 출고 완료 상호</span>
        </div>
        <div class="flex-grow-1 overflow-auto bg-white p-0">
          <ul class="list-group list-group-flush cust-list">
            <li v-for="(cust, idx) in custList" :key="idx"
                class="list-group-item list-group-item-action py-2 px-3 border-bottom d-flex justify-content-between align-items-center clickable"
                :class="{ 'active bg-danger bg-opacity-10 text-danger fw-bold': selectedCust?.iono === cust.iono }"
                @click="selectCust(cust)">
              <div class="d-flex flex-column">
                <span class="small fw-bold">{{ cust.custnm }}</span>
                <span class="extra-small text-muted">No: {{ cust.ioym }}-{{ cust.iono }} ({{ formatDate(cust.ioymd) }})</span>
              </div>
              <i class="bi bi-chevron-right small opacity-50"></i>
            </li>
            <li v-if="custList.length === 0" class="text-center p-4 text-muted small italic">조회된 데이터가 없습니다.</li>
          </ul>
        </div>
      </div>

      <!-- 🅱️ 우측: 마스터 정보 및 품목 그리드 -->
      <div class="d-flex flex-column flex-grow-1 gap-2 overflow-hidden">
        <!-- 마스터 정보 -->
        <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
          <div class="card-body p-0 bg-white">
            <table class="erp-table-dense w-100">
              <colgroup>
                <col style="width: 100px;" /><col />
                <col style="width: 100px;" /><col />
                <col style="width: 100px;" /><col />
              </colgroup>
              <tbody>
                <tr>
                  <th class="bg-light text-center">거 래 처</th>
                  <td><input :value="masterData.custnm" class="form-control bg-light" readonly /></td>
                  <th class="bg-light text-center">창&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                  <td><input :value="masterData.whnm" class="form-control bg-light text-center" readonly /></td>
                  <th class="bg-light text-center">출고일자</th>
                  <td><input :value="formatDate(masterData.ioymd)" class="form-control bg-light text-center" readonly /></td>
                </tr>
                <tr>
                  <th class="bg-light text-center">배 송 처</th>
                  <td colspan="3"><input :value="masterData.address" class="form-control bg-light" readonly /></td>
                  <th class="bg-light text-center">배송담당</th>
                  <td><input :value="masterData.trnempnm" class="form-control bg-light text-center" readonly /></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 품목 그리드 -->
        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
            <span class="fw-bold small text-dark d-flex align-items-center">
              <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 출고 품목 상세 명세
            </span>
            <div class="d-flex align-items-center gap-2">
               <span class="text-danger fw-bold small">선택: {{ activeItemCount }} 건</span>
               <button class="btn btn-xs btn-outline-secondary px-2" style="height: 22px; font-size: 11px;" @click="toggleAllRows">전체선택</button>
            </div>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="gridElement" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
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

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp: openCommonHelp } = useCommonHelp()

const now = new Date();
const searchForm = reactive({
  whcd: '100',
  outymdfr: `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`,
  outymdto: `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
})

const masterData = reactive<any>({
  custcd: '', custnm: '', whcd: '', whnm: '', ioymd: '', address: '', trnempnm: '', ioym: '', iono: '',
  clsymd: '', sclsym: ''
})

const gridElement = ref<HTMLElement | null>(null);
let grid: Tabulator | null = null;
const activeItemCount = ref(0);

const custList = ref<any[]>([]);
const selectedCust = ref<any>(null);
const whOptions = ref<any[]>([]);

const initGrid = () => {
  if (!gridElement.value) return
  grid = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "목록에서 출고 건을 선택하세요.",
    selectable: true,
    columnDefaults: { minWidth: 100, headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "", formatter: "rowSelection", titleFormatter: "rowSelection", width: 60, hozAlign: "center" },
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: "fw-bold" },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 60, hozAlign: "center" },
      { title: "출고수량", field: "ioqty", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "금액", field: "jsanamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "부가세", field: "jsanvat", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "합계", field: "iosum", width: 130, hozAlign: "right", cssClass: "fw-bold text-primary", formatter: "money", formatterParams: { precision: 0 } },
      {
        title: "정산여부", field: "jyn", width: 80, hozAlign: "center",
        formatter: (c) => c.getValue() === 'y' || c.getValue() === 'Y' ? '<span class="badge bg-success">완료</span>' : '<span class="badge bg-secondary">미정산</span>'
      }
    ]
  });
  grid.on("rowSelectionChanged", (data) => { activeItemCount.value = data.length; });
}

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_560U_STR', {
      actkind: 'S1', cmpycd: authStore.cmpycd, iogbn: '200',
      ioymdfr: searchForm.outymdfr.replace(/-/g, ''),
      ioymdto: searchForm.outymdto.replace(/-/g, ''),
      whcd: searchForm.whcd
    });
    custList.value = res.data || [];
    grid?.clearData(); selectedCust.value = null; resetForm(masterData);
    vAlert('조회되었습니다.');
  } catch (e) { vAlertError('목록 조회 실패'); }
}

async function selectCust(cust: any) {
  selectedCust.value = cust;
  Object.assign(masterData, cust);
  masterData.address = cust.address || cust.addres || '';
  masterData.trnempnm = cust.trnempnm || '';
  fetchDetail(cust);
}

async function fetchDetail(cust: any) {
  try {
    const res = await api.post('/api/hsio/HSIO_560U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, iogbn: '200',
      ioymdfr: searchForm.outymdfr.replace(/-/g, ''),
      ioymdto: searchForm.outymdto.replace(/-/g, ''),
      custcd: cust.custcd, whcd: cust.whcd, ioym: cust.ioym, iono: cust.iono
    });
    const data = (res.data || []).map((i: any) => ({
        ...i,
        iosum: (Number(i.jsanamt) || 0) + (Number(i.jsanvat) || 0)
    }));
    grid?.setData(data);
  } catch (e) { vAlertError('상세 내역 로드 실패'); }
}

/**
 * 🚀 저장(취소) 로직 (ASP 패턴: 선택 항목별 d0 순차 호출)
 */
async function saveCancel() {
  const items = grid.value?.getSelectedData() || [];
  if (items.length === 0) return vAlertError('취소할 품목을 선택하세요.');

  // 1. 정산 완료 체크
  const hasSettled = items.some((i: any) => i.jyn === 'y' || i.jyn === 'Y');
  if (hasSettled) return vAlertError('정산 완료된 자료는 취소할 수 없습니다.');

  // 2. 마감 체크
  const ioymd = String(masterData.ioymd).replace(/-/g, '');
  if (ioymd <= (masterData.clsymd || '')) return vAlertError('회계 마감된 일자입니다.');
  if (ioymd.substring(0, 6) <= (masterData.sclsym || '')) return vAlertError('영업 마감된 월입니다.');

  if (!confirm("선택한 품목의 출고 처리를 취소하시겠습니까?")) return;

  try {
    const ioymdfr = searchForm.outymdfr.replace(/-/g, '');
    const ioymdto = searchForm.outymdto.replace(/-/g, '');

    for (const item of items) {
      const params = {
        actkind: 'd0',
        cmpycd: authStore.cmpycd,
        iogbn: '200',
        ioymdfr: ioymdfr,
        ioymdto: ioymdto,
        whcd: masterData.whcd,
        custcd: masterData.custcd,
        ioym: masterData.ioym,
        iono: masterData.iono,
        iorowno: item.iorowno,
        updemp: authStore.userid
      }
      const res = await api.post('/api/hsio/HSIO_560U_STR', params);
      const resData = res.data?.[0];
      if (resData && (resData.ioym === '000000' || resData.status === 'y' || resData.erryn === 'y')) {
          throw new Error(resData.iono || resData.msg || '취소 중 업무 오류가 발생했습니다.');
      }
    }
    vAlert('출고 취소가 완료되었습니다.');
    fetchCustList(); initialize();
  } catch (e: any) { vAlertError(e.message || '취소 처리 실패'); }
}

const toggleAllRows = () => {
  const rows = grid?.getRows(); if (!rows) return
  const allSelected = grid?.getSelectedRows().length === rows.length
  if (allSelected) grid?.deselectRow(); else grid?.selectRow();
}

function initialize() {
  resetForm(searchForm);
  searchForm.outymdfr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;
  searchForm.outymdto = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
  custList.value = []; selectedCust.value = null; resetForm(masterData);
  grid?.clearData(); activeItemCount.value = 0;
}

function openHelp(type: string) {
    if (type === 'DEPT_search') {
        openCommonHelp('DEPT', (d) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm });
    }
}

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } })
     .then(r => { if (r.data?.length) { masterData.clsymd = r.data[0].clsymd; masterData.sclsym = r.data[0].sclsym; } })

  api.get('/api/ha00/HA00_00P_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
     .then(r => whOptions.value = r.data.map((i:any)=>({code: i.code || i.whcd, cdnm: i.cdnm || i.whnm})));

  nextTick(initGrid);
})

function formatNumber(val: any) { return new Intl.NumberFormat().format(Number(val) || 0) }
function formatDate(val: any) { return val && val.length === 8 ? `${val.substring(0,4)}-${val.substring(4,6)}-${val.substring(6,8)}` : val; }
</script>

<style scoped>
.main-content-wrapper { padding-bottom: 0px !important; }
.grid-container-right { border-bottom: 3px solid #005a9f !important; }
.erp-table-dense th, .erp-table-dense td {
  height: 32px !important; padding: 0 8px !important; font-size: 12px; vertical-align: middle; border: 1px solid #dee2e6;
}
.erp-table-dense .form-control, .erp-table-dense .form-select, .erp-table-dense .btn {
  height: 26px !important; font-size: 12px !important; border-radius: 2px;
}
.erp-table-dense th { font-weight: 600; color: #495057; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
