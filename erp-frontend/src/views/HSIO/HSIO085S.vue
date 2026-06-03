<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
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
        <button class="btn-erp btn-print" @click="print">인쇄</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 (HSIO020U 균형 배치 적용) -->
    <div class="search-bar bg-light border-bottom p-2 px-3 d-flex align-items-center shadow-sm">
      <div class="d-flex align-items-center gap-4 flex-grow-1">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 요청부서:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="fetchList" />
            <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 요청일자:</span>
          <div class="d-flex align-items-center gap-1">
            <input v-model="searchForm.frymd" type="date" class="form-control form-control-sm" style="width: 140px;" @change="fetchList" />
            <span class="text-muted mx-1">~</span>
            <input v-model="searchForm.toymd" type="date" class="form-control form-control-sm" style="width: 140px;" @change="fetchList" />
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary" style="white-space: nowrap;">▶ 거래처:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchForm.custcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchForm.custnm" type="text" class="form-control" placeholder="거래처 검색" @keyup.enter="fetchList" />
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
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="poGridRef" class="tabulator-instance flex-grow-1"></div>
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
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="itemGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
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
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  frymd: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  toymd: now.toISOString().substring(0, 10),
  custcd: '', custnm: ''
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
      cmpycd: authStore.cmpycd,
      frymd: searchForm.frymd.replace(/-/g, ''),
      toymd: searchForm.toymd.replace(/-/g, '')
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
  selectedCustNm.value = d.custnm;
  try {
    const res = await api.post('/api/hsio/HSIO_086S_STR', {
      cmpycd: authStore.cmpycd,
      deptcd: d.deptcd,
      custcd: d.custcd,
      frymd: searchForm.frymd.replace(/-/g, ''),
      toymd: searchForm.toymd.replace(/-/g, '')
    })
    const data = res.data.data || []
    itemGrid?.setData(data)
    activeItemCount.value = data.length
    totals.qty = data.reduce((acc: number, cur: any) => acc + (Number(cur.reqqty) || 0), 0)
    totals.amt = data.reduce((acc: number, cur: any) => acc + (Number(cur.reqamt) || 0), 0)
  } catch (e) { vAlertError('상세 조회 실패') }
}

function initialize() {
  resetForm(searchForm)
  searchForm.deptcd = authStore.deptcd; searchForm.deptnm = authStore.deptnm
  searchForm.frymd = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
  searchForm.toymd = now.toISOString().substring(0, 10)
  poGrid?.clearData(); itemGrid?.clearData();
  selectedCustNm.value = ''; activeItemCount.value = 0; totals.qty = 0; totals.amt = 0;
}

const print = () => { vAlert('인쇄 기능을 준비 중입니다.') }
const formatNumber = (val: any) => Number(val || 0).toLocaleString()
const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(4,6)}-${val.substring(6,8)}` : val;

function openHelp(type: string) {
  const commonProps = { path: '/api/ha00/HA00_00P_STR', cmpycd: authStore.cmpycd };
  if (type === 'DEPT') {
    Object.assign(modalProps, {
      title: '부서 선택', ...commonProps,
      data: { gubun: 'D0', cmpycd: authStore.cmpycd },
      columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 200 }],
      onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm }
    });
  } else if (type === 'CUST') {
    Object.assign(modalProps, {
      title: '거래처 선택', ...commonProps,
      data: { gubun: 'C0', cmpycd: authStore.cmpycd },
      columns: [{ title: '코드', field: 'custcd', width: 100 }, { title: '거래처명', field: 'custnm', width: 200 }],
      onConfirm: (d: any) => { searchForm.custcd = d.custcd; searchForm.custnm = d.custnm }
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
        { title: '거래처', field: 'custnm', minWidth: 150, widthGrow: 1, cssClass: 'fw-bold text-dark' },
        { title: '요청수량', field: 'reqqty', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } }
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
        { title: '요청일', field: 'reqymd', width: 90, hozAlign: 'center', formatter: (c) => formatDate(c.getValue()) },
        { title: '품명', field: 'itemnm', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
        { title: '규격', field: 'itsize', width: 150 },
        { title: '단위', field: 'unit', width: 60, hozAlign: 'center' },
        { title: '요청량', field: 'reqqty', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '단가', field: 'IMprice', hozAlign: 'right', width: 100, formatter: 'money' },
        { title: '금액', field: 'reqamt', hozAlign: 'right', width: 120, formatter: 'money' },
        { title: '발주량', field: 'balqty', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'text-primary' },
        { title: '미발주량', field: 'janqty', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'text-danger fw-bold',
          mutatorData: (v, d) => Number(d.reqqty || 0) - Number(d.balqty || 0) }
      ]
    })
  }
  nextTick(() => { fetchList() })
})
</script>
