<!--
	=============================================================
	프로그램명	: 주문현황 (Order Status)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : ASP(HSOD_110S) 기반 주문현황 조회 화면 (CSS 표준화 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-card-list me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        주문관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">주문현황 (HSOD110S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-outline-success border" style="font-size: 12px;" @click="exportExcel">엑셀</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 -->
    <div class="search-bar bg-light border-bottom p-2 px-3 d-flex align-items-center shadow-sm flex-shrink-0">
      <div class="d-flex align-items-center gap-4 flex-grow-1">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary">▶ 주문부서:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchForm.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
            <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary">▶ 주문일자:</span>
          <div class="d-flex align-items-center gap-1">
            <input v-model="searchForm.FRYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
            <span class="text-muted mx-1">~</span>
            <input v-model="searchForm.TOYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary">▶ 거 래 처:</span>
          <div class="d-flex align-items-center gap-1">
            <div class="input-group input-group-sm" style="width: 200px;">
              <input v-model="searchForm.CUSTCDFR" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
              <input v-model="searchForm.CUSTNMFR" type="text" class="form-control" placeholder="From" @keyup.enter="handleOpenHelp('CUST_FR')" />
              <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST_FR')"><i class="bi bi-search"></i></button>
            </div>
            <span class="text-muted">~</span>
            <div class="input-group input-group-sm" style="width: 200px;">
              <input v-model="searchForm.CUSTCDTO" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
              <input v-model="searchForm.CUSTNMTO" type="text" class="form-control" placeholder="To" @keyup.enter="handleOpenHelp('CUST_TO')" />
              <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST_TO')"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 3. 메인 그리드 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2">
      <div class="card border shadow-sm h-100 d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 주문 상세 현황</span>
          <span class="text-muted small">※ 주문번호를 클릭하면 상세 등록 화면으로 이동합니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative;">
          <div ref="gridElement" style="position: absolute; top:0; left:0; width:100%; height:100%;"></div>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const now = new Date()
const searchForm = reactive<any>({
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  FRYMD: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  TOYMD: now.toISOString().substring(0, 10),
  CUSTCDFR: '', CUSTNMFR: '',
  CUSTCDTO: '', CUSTNMTO: ''
})

const gridElement = ref<HTMLDivElement | null>(null);
let grid: Tabulator | null = null;

// 1. 도움창 핸들러
const handleOpenHelp = (type: string) => {
  if (type === 'DEPT') {
    openHelp('DEPT', (d) => { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM });
  } else if (type === 'CUST_FR') {
    openHelp('CUST', (d) => { searchForm.CUSTCDFR = d.CUSTCD; searchForm.CUSTNMFR = d.CUSTNM });
  } else if (type === 'CUST_TO') {
    openHelp('CUST', (d) => { searchForm.CUSTCDTO = d.CUSTCD; searchForm.CUSTNMTO = d.CUSTNM });
  }
}

// 2. 리스트 조회
async function fetchList() {
  if (!searchForm.DEPTCD) return vAlertError('주문부서를 선택해 주십시오.');

  try {
    const res = await api.post('/api/hsod/HSOD_110S_STR', {
      CMPYCD: authStore.CMPYCD,
      DEPTCD: searchForm.DEPTCD,
      CUSTFR: searchForm.CUSTCDFR,
      CUSTTO: searchForm.CUSTCDTO,
      FRYMD: searchForm.FRYMD.replace(/-/g, ''),
      TOYMD: searchForm.TOYMD.replace(/-/g, '')
    });

    const mapped = res.data.map((i: any) => ({
      ...i,
      ORDNO_FULL: `${i.ORDYM}-${i.ORDNO}`
    }));

    grid?.setData(mapped);
    vAlert('조회되었습니다.');
  } catch (e) {
    vAlertError('조회 중 오류가 발생했습니다.');
  }
}

// 3. 초기화
function initialize() {
  resetForm(searchForm);
  searchForm.DEPTCD = authStore.DEPTCD;
  searchForm.DEPTNM = authStore.DEPTNM;
  searchForm.FRYMD = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
  searchForm.TOYMD = now.toISOString().substring(0, 10);
  grid?.clearData();
}

// 4. 엑셀 출력
function exportExcel() {
  grid?.download("xlsx", "주문현황.xlsx", { title: "주문현황" });
}

onMounted(() => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: 'fitColumns',
      height: '100%',
      placeholder: "조회된 데이터가 없습니다.",
      columnDefaults: { headerSort: false, headerHozAlign: "center" },
      columns: [
        { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
        { title: "주문일", field: "ORDYMD", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
        { title: "주문번호", field: "ORDNO_FULL", width: 120, hozAlign: "center", cssClass: "fw-bold text-primary cursor-pointer",
          cellClick: (e, cell) => {
            const d = cell.getRow().getData();
            router.push({ name: '주문등록관리', query: { ORDYM: d.ORDYM, ORDNO: d.ORDNO, DEPTCD: d.DEPTCD } });
          }
        },
        { title: "거래처", field: "CUSTNM", minWidth: 150, hozAlign: "left" },
        { title: "품목명", field: "ITEMNM", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
        { title: "규격", field: "ITSIZE", width: 120, hozAlign: "left" },
        { title: "단위", field: "UNIT", width: 60, hozAlign: "center" },
        { title: "수량", field: "ORDQTY", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
        { title: "공급가", field: "SPYAMT", width: 110, hozAlign: "right", formatter: "money", bottomCalc: "sum" },
        { title: "부가세", field: "VATAMT", width: 100, hozAlign: "right", formatter: "money", bottomCalc: "sum" },
        { title: "합계", field: "AMTSUM", width: 120, hozAlign: "right", formatter: "money", cssClass: "bg-light fw-bold",
          mutatorData: (v, d) => Number(d.SPYAMT || 0) + Number(d.VATAMT || 0),
          bottomCalc: "sum"
        },
        { title: "납품량", field: "IOQTY", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } }
      ]
    })
  }
  nextTick(() => fetchList());
})

const formatDate = (v: any) => v && v.length === 8 ? `${v.substring(2,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
</script>


