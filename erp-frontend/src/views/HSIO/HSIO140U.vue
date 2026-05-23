<!--
	=============================================================
	프로그램명	: 매입전표취소 (Purchase Slip Cancellation)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : ASP(HSIO_140U) 소스 기반 재작성 (단일 그리드 표준 + 팝업 표준화)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio140u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-x-fill me-2 text-danger" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매입전표취소 (HSIO140U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-danger" @click="handleCancel">전표취소</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 -->
    <div class="search-bar bg-light border-bottom p-2 px-3 d-flex align-items-center shadow-sm flex-shrink-0">
      <div class="d-flex align-items-center gap-4 flex-grow-1">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary">▶ 입고부서:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchForm.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
            <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
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
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 취소 대상 매입 전표 내역</span>
          <button class="btn btn-outline-primary btn-sm px-2 py-0" style="font-size: 11px;" @click="toggleAllRows">전체선택/해제</button>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative;">
          <div ref="mainGridRef" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;"></div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const now = new Date()
const searchForm = reactive<any>({
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  IOYMDFR: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  IOYMDTO: now.toISOString().substring(0, 10)
})

// 마감일 정보
const clsInfo = reactive({ CLSYMD: '', SCLSYM: '' })
const mainGridRef = ref<HTMLDivElement | null>(null);
let grid: Tabulator | null = null;

async function fetchClosingInfo() {
  try {
    const res = await api.post('/api/hs00/HS00_000S_STR', { GUBUN: 'CL', CMPYCD: authStore.CMPYCD })
    if (res.data?.[0]) {
      clsInfo.CLSYMD = res.data[0].CLSYMD || ''
      clsInfo.SCLSYM = res.data[0].SCLSYM || ''
    }
  } catch (e) {}
}

async function fetchList() {
  try {
    const res = await api.post('/api/hsio/HSIO_140U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      IOGBN: '100',
      IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.IOYMDTO.replace(/-/g, ''),
      DEPTCD: searchForm.DEPTCD
    });
    grid?.setData(res.data.map((i: any) => ({ ...i, PROCYN: null })));
  } catch (e) { vAlertError('조회 실패') }
}

async function handleCancel() {
  const items = grid?.getData().filter((r: any) => r.PROCYN === true)
  if (!items || items.length === 0) return vAlertError('취소할 전표를 선택하세요.')

  for (const item of items) {
    if (item.CFMYN === 'Y') return vAlertError(`확정처리된 전표입니다. 취소할 수 없습니다. [전표:${item.SLIPYMD}-${item.SLIPNO}]`);
    const slipYm = String(item.JSANYMD || '').substring(0, 6);
    if (clsInfo.SCLSYM && slipYm <= clsInfo.SCLSYM) return vAlertError(`영업정보가 마감되었습니다. [전표:${item.SLIPYMD}-${item.SLIPNO}]`);
    if (item.JSANYMD <= clsInfo.CLSYMD) return vAlertError(`회계정보가 마감되었습니다. [전표:${item.SLIPYMD}-${item.SLIPNO}]`);
  }

  if (!confirm('선택한 전표들을 취소하시겠습니까?')) return

  try {
    const res = await api.post('/api/hsio/HSIO_140U_STR', {
        ACTKIND: 'D0',
        CMPYCD: authStore.CMPYCD,
        ITEMS: items,
        USERID: authStore.USERID
    })
    if (res.data?.[0]?.ERRYN === 'N') {
        vAlert('정상적으로 취소되었습니다.');
        fetchList();
    } else {
        vAlertError(res.data?.[0]?.MSG || '취소 실패');
    }
  } catch (e) { vAlertError('오류 발생') }
}

const handleOpenHelp = (type: string) => {
  if (type === 'DEPT') {
    openHelp('DEPT', (d) => {
      searchForm.DEPTCD = d.DEPTCD;
      searchForm.DEPTNM = d.DEPTNM;
    });
  }
}

const toggleAllRows = () => {
  const rows = grid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().PROCYN === true)
  rows.forEach(r => r.update({ PROCYN: allSelected ? null : true }))
}

function initialize() {
  resetForm(searchForm);
  searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
  searchForm.IOYMDFR = new Date().toISOString().substring(0, 7) + '-01';
  searchForm.IOYMDTO = now.toISOString().substring(0, 10);
  grid?.clearData();
}

onMounted(async () => {
  await fetchClosingInfo();
  if (mainGridRef.value) {
    grid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center" },
      columns: [
        { title: '선택', field: 'PROCYN', hozAlign: 'center', width: 60, formatter: 'tickCross', formatterParams: { crossElement: false }, editor: true, cellClick: (e, cell) => cell.setValue(cell.getValue() === true ? null : true) },
        { title: '전표번호', field: 'SLIPNO_FULL', width: 150, hozAlign: 'center', cssClass: 'fw-bold text-primary cursor-pointer',
          mutatorData: (v, d) => d.SLIPYMD && d.SLIPNO ? `${d.SLIPYMD}-${d.SLIPNO}` : '',
          cellClick: (e, cell) => {
            const d = cell.getRow().getData();
            window.open(`../HASL/HASL_SLIP_PRINT.asp?SLIPGU=010&SLIPYMD=${d.SLIPYMD}&SLIPNO=${d.SLIPNO}&DEPTCD=${d.UDEPTCD}`, '전표인쇄', 'left=10,top=10,width=700,height=650,scrollbars=yes');
          }
        },
        { title: '발행부서', field: 'DEPTNM', width: 140, hozAlign: 'left' },
        { title: '거래처', field: 'CUSTNM', minWidth: 150, widthGrow: 1, hozAlign: 'left', cssClass: 'fw-bold' },
        { title: '유형', field: 'VATTYPENM', width: 100 },
        { title: '공급가', field: 'SPYAMT', width: 110, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '부가세', field: 'VATAMT', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '합계', field: 'JSANSUM', width: 120, hozAlign: 'right', formatter: 'money', cssClass: 'bg-light fw-bold', mutatorData: (v, d) => Number(d.SPYAMT || 0) + Number(d.VATAMT || 0) }
      ]
    })
  }
  nextTick(() => fetchList())
})
</script>

<style scoped>
.hsio140u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #dc3545 !important; color: #fff !important; border: none !important; }
.form-control, .form-select { font-size: 12px !important; height: 28px !important; padding: 2px 8px !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8fafc; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 6px 10px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>
