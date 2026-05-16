<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio080s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        발주관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">발주현황 (HSIO080S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-print" @click="handlePrint">인쇄</button>
        <button class="btn-erp btn-excel" @click="handleExcel">엑셀</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 -->
    <div class="search-bar bg-light border-bottom p-2 px-3 d-flex align-items-center shadow-sm">
      <div class="d-flex align-items-center gap-4 flex-grow-1">
        <!-- 발주부서 -->
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 발주부서:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchParam.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchParam.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="fetchList" />
            <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
          </div>
        </div>
        <!-- 발주일자 -->
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 발주일자:</span>
          <div class="d-flex align-items-center gap-1">
            <input v-model="searchParam.FRYMD" type="date" class="form-control form-control-sm" style="width: 140px;" @change="fetchList" />
            <span class="text-muted mx-1">~</span>
            <input v-model="searchParam.TOYMD" type="date" class="form-control form-control-sm" style="width: 140px;" @change="fetchList" />
          </div>
        </div>
        <!-- 거래처 -->
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 거 래 처:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchParam.CUSTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchParam.CUSTNM" type="text" class="form-control" placeholder="거래처 검색" @keyup.enter="fetchList" />
            <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 3. 메인 그리드 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 bg-white">
      <div ref="gridElement" style="height: 100%;"></div>
    </div>

    <!-- 💰 4. 하단 합계 라인 (Summary) -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">조회 결과: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="me-4 small opacity-75">총 발주량: <span class="fw-bold text-white ms-1">{{ formatNumber(totals.qty) }}</span></span>
          <span class="me-4 small opacity-75">총 공급가: <span class="fw-bold text-white ms-1">{{ formatNumber(totals.amt) }}</span></span>
          <span class="me-4 small opacity-75">총 부가세: <span class="fw-bold text-white ms-1">{{ formatNumber(totals.vat) }}</span></span>
          <span class="fs-5 ms-2 fw-light">총 합계: <span class="fw-bold text-warning ms-2">{{ formatNumber(totals.sum) }}</span> 원</span>
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
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
const formatDateStr = (date: Date) => date.toISOString().substring(0, 10);

const searchParam = reactive({
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  FRYMD: formatDateStr(firstDay),
  TOYMD: formatDateStr(now),
  CUSTCD: '',
  CUSTNM: ''
})

const gridElement = ref<HTMLElement | null>(null);
const grid = ref<Tabulator | null>(null);
const activeItemCount = ref(0)
const totals = reactive({ qty: 0, amt: 0, vat: 0, sum: 0 })

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerHozAlign: 'center', minWidth: 90 },
    columns: [
      { title: "거래처", field: "CUSTNM", minWidth: 200, widthGrow: 1, cssClass: "fw-bold text-dark cursor-pointer", cellClick: (e, cell) => {
        console.log('Navigate to HSIO090S with', cell.getData().CUSTCD);
      }},
      { title: "구매요청자", field: "USERNM", width: 100, hozAlign: "center" },
      { title: "주요품목", field: "ITEMNM", minWidth: 250, widthGrow: 2, formatter: (cell) => {
        const d = cell.getData();
        const cnt = Number(d.ITEM_CNT) || 0;
        return cnt > 0 ? `${d.ITEMNM} 외 ${cnt}건` : d.ITEMNM;
      }},
      { title: "발주량", field: "BALQTY", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "공급가", field: "BALAMT", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "부가세", field: "BALVAT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "합계", field: "TOTAL", width: 120, hozAlign: "right", cssClass: "fw-bold", formatter: (cell) => {
        const d = cell.getData();
        return formatNumber((Number(d.BALAMT) || 0) + (Number(d.BALVAT) || 0));
      }},
      { title: "입고량", field: "INQTY", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: (cell:any) => cell.getData().QTYPNT || 0 } },
      { title: "미입고량", field: "JANQTY", width: 90, hozAlign: "right", cssClass: "text-danger fw-bold", formatter: (cell) => {
        const d = cell.getData();
        return formatNumber((Number(d.BALQTY) || 0) - (Number(d.INQTY) || 0));
      }}
    ]
  });
}

const updateTotals = (data: any[]) => {
  activeItemCount.value = data.length;
  totals.qty = data.reduce((acc, i) => acc + (Number(i.BALQTY) || 0), 0);
  totals.amt = data.reduce((acc, i) => acc + (Number(i.BALAMT) || 0), 0);
  totals.vat = data.reduce((acc, i) => acc + (Number(i.BALVAT) || 0), 0);
  totals.sum = totals.amt + totals.vat;
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

async function fetchList() {
  if (!searchParam.DEPTCD) return vAlertError('발주부서를 선택하세요.');
  try {
    const res = await api.post('/api/hsio/HSIO_080S_STR', {
      CMPYCD: authStore.CMPYCD,
      DEPTCD: searchParam.DEPTCD,
      CUSTCD: searchParam.CUSTCD,
      CUSTNM: searchParam.CUSTNM,
      YMDFR: searchParam.FRYMD.replace(/-/g, ''),
      YMDTO: searchParam.TOYMD.replace(/-/g, '')
    });
    if (grid.value) {
      grid.value.setData(res.data);
      updateTotals(res.data);
      vAlert('조회되었습니다.');
    }
  } catch (e) { vAlertError('조회 실패') }
}

function initialize() {
  resetForm(searchParam);
  Object.assign(searchParam, {
    DEPTCD: authStore.DEPTCD,
    DEPTNM: authStore.DEPTNM,
    FRYMD: formatDateStr(firstDay),
    TOYMD: formatDateStr(now)
  });
  grid.value?.clearData();
  updateTotals([]);
}

function openHelp(type: string) {
  const commonProps = { path: '/api/ha00/HA00_00P_STR', CMPYCD: authStore.CMPYCD };
  if (type === 'DEPT') {
    Object.assign(modalProps, {
      title: '부서 선택', ...commonProps,
      data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
      columns: [
        { title: '코드', field: 'DEPTCD', width: 80 },
        { title: '부서명', field: 'DEPTNM', width: 180 },
        { title: '상위부서', field: 'UDEPTNM', width: 150 }
      ],
      onConfirm: (d: any) => { searchParam.DEPTCD = d.DEPTCD; searchParam.DEPTNM = d.DEPTNM }
    });
  } else if (type === 'CUST') {
    Object.assign(modalProps, {
      title: '거래처 선택', ...commonProps,
      data: { GUBUN: 'C4', CMPYCD: authStore.CMPYCD },
      columns: [
        { title: '코드', field: 'CUSTCD', width: 70 },
        { title: '거래처명', field: 'CUSTNM', width: 180 },
        { title: '사업자번호', field: 'CUSTNO', width: 110 },
        { title: '대표자', field: 'BOSSNM', width: 80 },
        { title: '주소', field: 'ADDRESS', minWidth: 250 }
      ],
      onConfirm: (d: any) => { searchParam.CUSTCD = d.CUSTCD; searchParam.CUSTNM = d.CUSTNM }
    });
  }
  modalVisible.value = true;
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0);
const handlePrint = () => vAlert('출력 기능을 준비 중입니다.');
const handleExcel = () => grid.value?.download("xlsx", "발주현황.xlsx", { sheetName: "발주현황" });

onMounted(() => {
  nextTick(() => {
    initGrid();
    fetchList();
  });
})
</script>

<style scoped>
.hsio080s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f8f9fa; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #ffffff !important; border: none !important; }
.btn-print { background-color: #6c757d !important; color: #fff !important; border: none !important; }
.btn-excel { background-color: #198754 !important; color: #fff !important; border: none !important; }

.search-bar { background-color: #f1f5f9 !important; }

:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 700 !important; }
:deep(.tabulator-row:hover) { background-color: #eef2ff !important; }

.erp-footer { background-color: #212529 !important; }
</style>
