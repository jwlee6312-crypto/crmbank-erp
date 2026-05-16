<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio085s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        구매관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">구매요청현황 (HSIO085S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-print" @click="handlePrint">인쇄</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 (HSIO020U 균형 배치 적용) -->
    <div class="search-bar bg-light border-bottom p-2 px-3 d-flex align-items-center shadow-sm">
      <div class="d-flex align-items-center gap-4 flex-grow-1">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 요청부서:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchForm.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="fetchList" />
            <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 요청일자:</span>
          <div class="d-flex align-items-center gap-1">
            <input v-model="searchForm.FRYMD" type="date" class="form-control form-control-sm" style="width: 140px;" @change="fetchList" />
            <span class="text-muted mx-1">~</span>
            <input v-model="searchForm.TOYMD" type="date" class="form-control form-control-sm" style="width: 140px;" @change="fetchList" />
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 거래처:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchForm.CUSTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchForm.CUSTNM" type="text" class="form-control" placeholder="거래처 검색" @keyup.enter="fetchList" />
            <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 3. 메인 컨텐츠 영역 (좌우 분할) -->
    <div class="flex-grow-1 d-flex flex-row overflow-hidden p-2 gap-2">
      <!-- 🅰️ 좌측: 구매요청 요약 목록 -->
      <div class="card border shadow-sm d-flex flex-column" style="width: 350px; min-width: 350px;">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1 text-primary"></i> 거래처별 요약</span>
        </div>
        <div class="flex-grow-1 bg-white">
          <div ref="poGridRef" style="height: 100%;"></div>
        </div>
      </div>

      <!-- 🅱️ 우측: 품목 상세 그리드 -->
      <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
            <span class="fw-bold small text-dark d-flex align-items-center">
              <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 구매요청 품목 상세 내역
              <span v-if="selectedCustNm" class="ms-3 badge bg-primary-subtle text-primary border border-primary-subtle fw-medium">거래처: {{ selectedCustNm }}</span>
            </span>
          </div>
          <div class="flex-grow-1 bg-white">
            <div ref="itemGridRef" style="height: 100%;"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 4. 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">조회 품목: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="me-4 small opacity-75">총 요청수량: <span class="fw-bold text-white ms-1">{{ formatNumber(totals.qty) }}</span></span>
          <span class="fs-5 ms-2 fw-light">총 요청금액: <span class="fw-bold text-white ms-2">{{ formatNumber(totals.amt) }}</span> 원</span>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
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
  FRYMD: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  TOYMD: now.toISOString().substring(0, 10),
  CUSTCD: '', CUSTNM: ''
})

const selectedCustNm = ref('');
const poGridRef = ref<HTMLDivElement | null>(null);
const itemGridRef = ref<HTMLDivElement | null>(null);
let poGrid: Tabulator | null = null;
let itemGrid: Tabulator | null = null;
const activeItemCount = ref(0)
const totals = reactive({ qty: 0, amt: 0 })

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

async function fetchList() {
  try {
    const res = await api.post('/api/hsio/HSIO_085S_STR', {
      ...searchForm,
      CMPYCD: authStore.CMPYCD,
      FRYMD: searchForm.FRYMD.replace(/-/g, ''),
      TOYMD: searchForm.TOYMD.replace(/-/g, '')
    })
    poGrid?.setData(res.data.data || [])
    itemGrid?.clearData();
    selectedCustNm.value = '';
    activeItemCount.value = 0;
    totals.qty = 0; totals.amt = 0;
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetail(row: any) {
  const d = row.getData();
  selectedCustNm.value = d.CUSTNM;
  try {
    const res = await api.post('/api/hsio/HSIO_086S_STR', {
      CMPYCD: authStore.CMPYCD,
      DEPTCD: d.DEPTCD,
      CUSTCD: d.CUSTCD,
      FRYMD: searchForm.FRYMD.replace(/-/g, ''),
      TOYMD: searchForm.TOYMD.replace(/-/g, '')
    })
    const data = res.data.data || []
    itemGrid?.setData(data)
    activeItemCount.value = data.length
    totals.qty = data.reduce((acc: number, cur: any) => acc + (Number(cur.REQQTY) || 0), 0)
    totals.amt = data.reduce((acc: number, cur: any) => acc + (Number(cur.REQAMT) || 0), 0)
  } catch (e) { vAlertError('상세 조회 실패') }
}

function initialize() {
  resetForm(searchForm)
  searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM
  searchForm.FRYMD = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
  searchForm.TOYMD = now.toISOString().substring(0, 10)
  poGrid?.clearData(); itemGrid?.clearData();
  selectedCustNm.value = ''; activeItemCount.value = 0; totals.qty = 0; totals.amt = 0;
}

const handlePrint = () => { vAlert('인쇄 기능을 준비 중입니다.') }
const formatNumber = (val: any) => Number(val || 0).toLocaleString()
const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(4,6)}-${val.substring(6,8)}` : val;

function openHelp(type: string) {
  const commonProps = { path: '/api/ha00/HA00_00P_STR', CMPYCD: authStore.CMPYCD };
  if (type === 'DEPT') {
    Object.assign(modalProps, {
      title: '부서 선택', ...commonProps,
      data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 200 }],
      onConfirm: (d: any) => { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM }
    });
  } else if (type === 'CUST') {
    Object.assign(modalProps, {
      title: '거래처 선택', ...commonProps,
      data: { GUBUN: 'C0', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '코드', field: 'CUSTCD', width: 100 }, { title: '거래처명', field: 'CUSTNM', width: 200 }],
      onConfirm: (d: any) => { searchForm.CUSTCD = d.CUSTCD; searchForm.CUSTNM = d.CUSTNM }
    });
  }
  modalVisible.value = true;
}

onMounted(() => {
  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      placeholder: "데이터가 없습니다.",
      columns: [
        { title: '거래처', field: 'CUSTNM', minWidth: 150, widthGrow: 1, cssClass: 'fw-bold text-dark' },
        { title: '요청수량', field: 'REQQTY', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } }
      ]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row))
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      placeholder: "거래처를 선택하세요.",
      columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 100 },
      columns: [
        { title: '요청일', field: 'REQYMD', width: 90, hozAlign: 'center', formatter: (c) => formatDate(c.getValue()) },
        { title: '품명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
        { title: '규격', field: 'ITSIZE', width: 150 },
        { title: '단위', field: 'UNIT', width: 60, hozAlign: 'center' },
        { title: '요청량', field: 'REQQTY', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '단가', field: 'IMPRICE', hozAlign: 'right', width: 100, formatter: 'money' },
        { title: '금액', field: 'REQAMT', hozAlign: 'right', width: 120, formatter: 'money' },
        { title: '발주량', field: 'BALQTY', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'text-primary' },
        { title: '미발주량', field: 'JANQTY', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'text-danger fw-bold',
          mutatorData: (v, d) => Number(d.REQQTY || 0) - Number(d.BALQTY || 0) }
      ]
    })
  }
  nextTick(() => { fetchList() })
})
</script>

<style scoped>
.hsio085s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f8f9fa; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-print { background-color: #6c757d !important; color: #fff !important; border: none !important; }

.search-bar { background-color: #f1f5f9 !important; }

:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 700 !important; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }

.erp-footer { background-color: #212529 !important; }
</style>
