<!--
	=============================================================
	프로그램명	: 외부매입전표출력 (External Purchase Slip Print)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : ASP(HSIO_142S) 로직을 Vue 표준으로 재작성
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio142s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-printer-fill me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">외부매입전표출력 (HSIO142S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 -->
    <div class="search-bar bg-light border-bottom p-2 px-3 d-flex align-items-center shadow-sm flex-shrink-0">
      <div class="d-flex align-items-center gap-4 flex-grow-1">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary">▶ 입고부서:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchForm.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="fetchList" />
            <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary">▶ 발행일자:</span>
          <div class="d-flex align-items-center gap-1">
            <input v-model="searchForm.IOYMDFR" type="date" class="form-control form-control-sm" style="width: 140px;" />
            <span class="text-muted mx-1">~</span>
            <input v-model="searchForm.IOYMDTO" type="date" class="form-control form-control-sm" style="width: 140px;" />
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 3. 메인 그리드 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2">
      <div class="card border shadow-sm h-100 d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 전표 발행 내역</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative;">
          <div ref="mainGridRef" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;"></div>
        </div>
      </div>
    </div>
    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const searchForm = reactive<any>({
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  IOYMDFR: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  IOYMDTO: now.toISOString().substring(0, 10)
})

const mainGridRef = ref<HTMLDivElement | null>(null);
let grid: Tabulator | null = null;
const activeItemCount = ref(0)

async function fetchList() {
  try {
    // ASP 소스에서 HSIO_141U_STR 'S0'를 사용하여 조회하고 있음
    const res = await api.post('/api/hsio/HSIO_141U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      IOGBN: '100',
      IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.IOYMDTO.replace(/-/g, ''),
      DEPTCD: searchForm.DEPTCD
    });
    grid?.setData(res.data || []);
    activeItemCount.value = res.data?.length || 0;
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

function initialize() {
  resetForm(searchForm);
  searchForm.DEPTCD = authStore.DEPTCD;
  searchForm.DEPTNM = authStore.DEPTNM;
  searchForm.IOYMDFR = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
  searchForm.IOYMDTO = now.toISOString().substring(0, 10);
  grid?.clearData();
  activeItemCount.value = 0;
}

// 출력 함수 (ASP 기존 출력 페이지 호출)
function slipPrint(type: 'SLIP' | 'PUM', data: any) {
  const slipGu = '010';
  const url = type === 'SLIP'
    ? `../HASL/HASL_SLIP_PRINT_OUT.asp?SLIPGU=${slipGu}&SLIPYMD=${data.SLIPYMD}&SLIPNO=${data.SLIPNO}&DEPTCD=${data.DEPTCD}`
    : `../HSIR/HSIR_SLIP_PRINT.asp?SLIPGU=${slipGu}&SLIPYMD=${data.SLIPYMD}&SLIPNO=${data.SLIPNO}&DEPTCD=${data.DEPTCD}`;

  window.open(url, '전표인쇄', 'left=10,top=10,width=800,height=700,scrollbars=yes');
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'DEPT') {
    Object.assign(modalProps, {
      title: '부서 선택',
      path: '/api/ha00/HA00_00P_STR',
      data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 200 }],
      onConfirm: (d: any) => { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM }
    });
  }
  modalVisible.value = true;
}

onMounted(() => {
  if (mainGridRef.value) {
    grid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center" },
      columns: [
        { title: '전표번호', field: 'SLIPNO_FULL', width: 140, hozAlign: 'center', cssClass: 'fw-bold text-primary cursor-pointer',
          mutatorData: (v, d) => d.SLIPYMD && d.SLIPNO ? `${d.SLIPYMD}-${d.SLIPNO}` : '',
          cellClick: (e, cell) => slipPrint('SLIP', cell.getRow().getData())
        },
        { title: '발행부서', field: 'DEPTNM', width: 130, hozAlign: 'left' },
        { title: '거래처', field: 'CUSTNM', minWidth: 200, widthGrow: 1, hozAlign: 'left', cssClass: 'fw-bold' },
        { title: '유형', field: 'VATTYPENM', width: 100 },
        { title: '공급가', field: 'SPYAMT', width: 110, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '부가세', field: 'VATAMT', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '합계', field: 'JSANSUM', width: 120, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'bg-light fw-bold',
          mutatorData: (v, d) => Number(d.SPYAMT || 0) + Number(d.VATAMT || 0)
        },
        { title: '회계전표', width: 100, hozAlign: 'center',
          formatter: () => `<button class="btn btn-xs btn-outline-dark px-2">인쇄</button>`,
          cellClick: (e, cell) => slipPrint('SLIP', cell.getRow().getData())
        },
        { title: '지출결의', width: 100, hozAlign: 'center',
          formatter: () => `<button class="btn btn-xs btn-outline-primary px-2">인쇄</button>`,
          cellClick: (e, cell) => slipPrint('PUM', cell.getRow().getData())
        }
      ]
    })
  }
  nextTick(() => fetchList())
})

const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(4,6)}-${val.substring(6,8)}` : val;
</script>

<style scoped>
.hsio142s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }

.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}

:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
:deep(.tabulator-cell) { font-size: 12px !important; }

.btn-xs { padding: 1px 5px; font-size: 11px; }
</style>
