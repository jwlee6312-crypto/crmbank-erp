<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio082s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-ruled me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        발주관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">발주상세현황 (HSIO082S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-print" @click="handlePrint">인쇄</button>
        <button class="btn-erp btn-excel" @click="handleExcel">엑셀</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 (고밀도 2줄 배치) -->
    <div class="search-bar bg-light border-bottom p-2 px-3 shadow-sm">
      <div class="d-flex flex-column gap-2">
        <!-- 첫 번째 줄 -->
        <div class="d-flex align-items-center gap-4 flex-wrap">
          <div class="d-flex align-items-center gap-2">
            <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 발주일자:</span>
            <div class="d-flex align-items-center gap-1">
              <input v-model="searchParam.FRYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
              <span class="text-muted mx-1">~</span>
              <input v-model="searchParam.TOYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 거 래 처:</span>
            <div class="input-group input-group-sm" style="width: 250px;">
              <input v-model="searchParam.CUSTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
              <input v-model="searchParam.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="fetchList" />
              <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 주문번호:</span>
            <div class="input-group input-group-sm" style="width: 220px;">
              <input v-model="searchParam.ORDYM" type="month" class="form-control text-center bg-white" style="max-width: 100px;" />
              <input v-model="searchParam.ORDNO" type="text" class="form-control text-center" placeholder="0000" />
              <button class="btn btn-outline-secondary" @click="openHelp('ORDER')"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
        <!-- 두 번째 줄 -->
        <div class="d-flex align-items-center gap-4 flex-wrap border-top pt-2">
          <div class="d-flex align-items-center gap-2">
            <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 담 당 자:</span>
            <select v-model="searchParam.USERID" class="form-select form-select-sm" style="width: 150px;">
              <option value="">-- 전체 --</option>
              <option v-for="item in empOptions" :key="item.USERID" :value="item.USERID">{{ item.USERNM }}</option>
            </select>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 입고여부:</span>
            <select v-model="searchParam.IPGOYN" class="form-select form-select-sm" style="width: 100px;">
              <option value="Y">전체</option>
              <option value="N">미입고</option>
            </select>
          </div>
          <div class="d-flex align-items-center gap-2 flex-grow-1">
            <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 특이사항:</span>
            <input v-model="searchParam.REMARK" type="text" class="form-control form-control-sm" placeholder="특기사항 검색어 입력" @keyup.enter="fetchList" />
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 3. 메인 그리드 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 bg-white">
      <div ref="gridElement" style="height: 100%;"></div>
    </div>

    <!-- 💰 4. 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-2 small">조회 건수: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-10 text-end">
          <span class="me-3 small opacity-75">발주량: <span class="fw-bold text-white">{{ formatNumber(totals.qty) }}</span></span>
          <span class="me-3 small opacity-75">공급가: <span class="fw-bold text-white">{{ formatNumber(totals.amt) }}</span></span>
          <span class="me-3 small opacity-75">부가세: <span class="fw-bold text-white">{{ formatNumber(totals.vat) }}</span></span>
          <span class="me-3 small opacity-75">입고량: <span class="fw-bold text-white">{{ formatNumber(totals.inQty) }}</span></span>
          <span class="fs-6 ms-2 fw-light">미입고량: <span class="fw-bold text-warning">{{ formatNumber(totals.qty - totals.inQty) }}</span></span>
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
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date();
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
const formatDateStr = (date: Date) => date.toISOString().substring(0, 10);

const searchParam = reactive({
  FRYMD: formatDateStr(firstDay),
  TOYMD: formatDateStr(now),
  CUSTCD: '',
  CUSTNM: '',
  ORDYM: '',
  ORDNO: '',
  USERID: '',
  IPGOYN: 'Y',
  REMARK: ''
})

const gridElement = ref<HTMLElement | null>(null);
const grid = ref<Tabulator | null>(null);
const activeItemCount = ref(0)
const totals = reactive({ qty: 0, amt: 0, vat: 0, inQty: 0 })
const empOptions = ref<any[]>([])

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerHozAlign: 'center', minWidth: 80 },
    columns: [
      { title: "발주일자", field: "BALYMD", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
      { title: "거래처", field: "CUSTNM", minWidth: 150, cssClass: "fw-bold text-dark cursor-pointer", cellClick: (e, cell) => {
        const d = cell.getData();
        const path = d.GUBUN === '1' ? '/HSIO/HSIO050U' : '/HSIO/HSIO052U';
        router.push({ path, query: { BALYM: d.BALYM, BALNO: d.BALNO } });
      }},
      { title: "담당자", field: "USERNM", width: 90, hozAlign: "center" },
      { title: "주문번호", field: "ORDER_NO", width: 120, formatter: (c) => {
        const d = c.getData(); return d.ORDYM ? `${d.ORDYM}-${d.ORDNO}` : '';
      }},
      { title: "품목코드", field: "ITEMCD", width: 100 },
      { title: "품목명", field: "ITEMNM", minWidth: 180 },
      { title: "규격", field: "ITSIZE", width: 120 },
      { title: "발주량", field: "BALQTY", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 } },
      { title: "공급가", field: "BALAMT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "부가세", field: "BALVAT", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "입고량", field: "INQTY", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 } },
      { title: "미입고량", field: "JANQTY", width: 80, hozAlign: "right", cssClass: "text-danger fw-bold", formatter: (c) => {
        const d = c.getData(); return formatNumber((Number(d.BALQTY) || 0) - (Number(d.INQTY) || 0));
      }}
    ]
  });
}

const updateTotals = (data: any[]) => {
  activeItemCount.value = data.length;
  totals.qty = data.reduce((acc, i) => acc + (Number(i.BALQTY) || 0), 0);
  totals.amt = data.reduce((acc, i) => acc + (Number(i.BALAMT) || 0), 0);
  totals.vat = data.reduce((acc, i) => acc + (Number(i.BALVAT) || 0), 0);
  totals.inQty = data.reduce((acc, i) => acc + (Number(i.INQTY) || 0), 0);
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

async function fetchList() {
  try {
    const res = await api.post('/api/hsio/HSIO_082S_STR', {
      CMPYCD: authStore.CMPYCD,
      YMDFR: searchParam.FRYMD.replace(/-/g, ''),
      YMDTO: searchParam.TOYMD.replace(/-/g, ''),
      ORDYM: searchParam.ORDYM.replace(/-/g, ''),
      ORDNO: searchParam.ORDNO,
      CUSTCD: searchParam.CUSTCD,
      USERID: searchParam.USERID,
      IPGOYN: searchParam.IPGOYN,
      REMARK: searchParam.REMARK
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
    FRYMD: formatDateStr(firstDay), TOYMD: formatDateStr(now),
    CUSTCD: '', CUSTNM: '', ORDYM: '', ORDNO: '', USERID: '', IPGOYN: 'Y', REMARK: ''
  });
  grid.value?.clearData();
  updateTotals([]);
}

function openHelp(type: string) {
  const commonProps = { path: '/api/ha00/HA00_00P_STR', CMPYCD: authStore.CMPYCD };
  if (type === 'CUST') {
    Object.assign(modalProps, {
      title: '거래처 선택', ...commonProps,
      data: { GUBUN: 'C4', CMPYCD: authStore.CMPYCD },
      columns: [
        { title: '코드', field: 'CUSTCD', width: 70 },
        { title: '거래처명', field: 'CUSTNM', width: 180 },
        { title: '사업자번호', field: 'CUSTNO', width: 110 },
        { title: '대표자', field: 'BOSSNM', width: 80 }
      ],
      onConfirm: (d: any) => { searchParam.CUSTCD = d.CUSTCD; searchParam.CUSTNM = d.CUSTNM }
    });
  } else if (type === 'ORDER') {
    Object.assign(modalProps, {
      title: '주문 선택', ...commonProps,
      data: { GUBUN: 'OR', CMPYCD: authStore.CMPYCD },
      columns: [
        { title: '주문월', field: 'ORDYM', width: 90 },
        { title: '번호', field: 'ORDNO', width: 70 },
        { title: '주문처', field: 'CUSTNM', width: 180 },
        { title: '주문일자', field: 'ORDYMD', width: 100 }
      ],
      onConfirm: (d: any) => { searchParam.ORDYM = d.ORDYM; searchParam.ORDNO = d.ORDNO }
    });
  }
  modalVisible.value = true;
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0);
const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(0,4)}-${val.substring(4,6)}-${val.substring(6,8)}` : val;
const handlePrint = () => vAlert('출력 기능을 준비 중입니다.');
const handleExcel = () => grid.value?.download("xlsx", "발주상세현황.xlsx", { sheetName: "발주상세" });

onMounted(() => {
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'SD', CMPYCD: authStore.CMPYCD } }).then(r => {
    empOptions.value = r.data.map((i: any) => ({
      USERID: String(i.USERID || i.userid || Object.values(i)[0]).trim(),
      USERNM: String(i.USERNM || i.usernm || Object.values(i)[1]).trim()
    }))
  })
  nextTick(() => {
    initGrid();
    fetchList();
  });
})
</script>

<style scoped>
.hsio082s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f8f9fa; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #ffffff !important; border: none !important; }
.btn-print { background-color: #6c757d !important; color: #fff !important; border: none !important; }
.btn-excel { background-color: #198754 !important; color: #fff !important; border: none !important; }

.search-bar { background-color: #f1f5f9 !important; }

:deep(.tabulator) { border: none; font-size: 12px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 700 !important; }
:deep(.tabulator-row:hover) { background-color: #eef2ff !important; }

.erp-footer { background-color: #212529 !important; }
</style>