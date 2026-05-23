<!--영업관리/출고관리/주문출고처리 [ERP 프리미엄 고밀도 표준 적용] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-truck me-2 text-primary" style="font-size: 16px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">주문출고처리 (HSIO550U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchCustList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 -->
    <div class="search-bar bg-white border-bottom p-2 px-3 d-flex align-items-center flex-shrink-0 gap-4 shadow-sm">
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold small text-secondary">출고창고:</span>
        <select v-model="searchParam.WHCD" class="form-select form-select-sm" style="width: 120px;">
          <option value="">전체</option>
          <option v-for="opt in whOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
        </select>
      </div>
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold small text-secondary">▶ 납품일자:</span>
        <div class="d-flex align-items-center gap-1 flex-nowrap">
          <input v-model="searchParam.FRYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
          <span class="text-muted">~</span>
          <input v-model="searchParam.TOYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
        </div>
      </div>
    </div>

    <!-- 💡 3. 메인 분할 영역 -->
    <div class="flex-grow-1 d-flex flex-row gap-3 p-3 overflow-hidden">

      <!-- 🅰️ 좌측: 거래처 목록 -->
      <div class="card border shadow-sm d-flex flex-column flex-shrink-0" style="width: 320px;">
        <div class="card-header bg-white text-dark fw-bold border-bottom py-1 ps-3 h-auto" style="font-size: 13px;">
          <i class="bi bi-list-ul me-1 text-secondary"></i> 미출고 거래처
        </div>
        <div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
          <div ref="custGridRef" style="height: 100%;"></div>
        </div>
      </div>

      <!-- 🅱️ 우측: 상세 정보 및 주문 내역 -->
      <div class="d-flex flex-column flex-grow-1 gap-3 overflow-hidden">
        <!-- 마스터 정보 카드 -->
        <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
          <div class="card-header bg-white text-dark fw-bold border-bottom py-1 ps-3 h-auto" style="font-size: 13px;">
            <i class="bi bi-pencil-square me-1 text-secondary"></i> 출고 상세 정보
          </div>
          <div class="card-body p-0 bg-white">
            <table class="erp-table-full border-0">
              <colgroup>
                <col style="width: 110px;" /><col />
                <col style="width: 110px;" /><col />
                <col style="width: 110px;" /><col />
              </colgroup>
              <tbody>
                <tr>
                  <th class="required">거 래 처</th>
                  <td>
                    <div class="input-group input-group-sm flex-nowrap">
                      <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 65px;" readonly />
                      <input v-model="masterData.CUSTNM" type="text" class="form-control border-start-0" readonly />
                    </div>
                  </td>
                  <th class="required">출고창고</th>
                  <td>
                    <select v-model="masterData.WHCD" class="form-select">
                      <option value="">선택</option>
                      <option v-for="opt in whOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                    </select>
                  </td>
                  <th class="required">출고일자</th>
                  <td><input v-model="uiIOYMD" type="date" class="form-control" /></td>
                </tr>
                <tr>
                  <th>배 송 처</th>
                  <td><input v-model="masterData.ADDRESS" type="text" class="form-control" placeholder="배송지 정보" /></td>
                  <th>배송방법</th>
                  <td>
                    <div class="input-group input-group-sm flex-nowrap">
                        <input v-model="masterData.TRANCD" type="text" class="form-control bg-light text-center" style="max-width: 50px;" readonly />
                        <input v-model="masterData.TRANNM" type="text" class="form-control border-start-0" />
                        <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('TRAN')"><i class="bi bi-search"></i></button>
                    </div>
                  </td>
                  <td colspan="2" class="bg-light-subtle"></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 주문 상세 내역 그리드 -->
        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
          <div class="card-header bg-white text-dark fw-bold border-bottom py-1 ps-3 h-auto d-flex justify-content-between align-items-center">
            <span class="small"><i class="bi bi-list-check me-1 text-secondary"></i> 주문 상세 내역</span>
            <div class="small text-muted">※ 출고수량을 입력 후 저장하세요.</div>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative;">
            <div ref="detailGridRef" style="position: absolute; top:0; left:0; width:100%; height:100%;"></div>
          </div>
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
import { useCommonHelp } from '@/composables/useCommonHelp'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const now = new Date()
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const initYMD = now.toISOString().substring(0, 10)

const searchParam = reactive({ WHCD: '', FRYMD: firstDay, TOYMD: initYMD, DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM })
const masterData = reactive<any>({
  ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOYMD: initYMD, WHCD: '', CUSTCD: '', CUSTNM: '', ADDRESS: '', TRANCD: '', TRANNM: '', STS: 'N'
})

const uiIOYMD = computed({ get: () => masterData.IOYMD ? `${masterData.IOYMD.substring(0, 4)}-${masterData.IOYMD.substring(4, 6)}-${masterData.IOYMD.substring(6, 8)}` : '', set: (v) => masterData.IOYMD = v.replace(/-/g, '') })

const custGridRef = ref<HTMLElement | null>(null); let custGrid: Tabulator | null = null;
const detailGridRef = ref<HTMLElement | null>(null); let detailGrid: Tabulator | null = null;
const whOptions = ref<any[]>([])

const initGrids = () => {
  if (custGridRef.value) {
    custGrid = new Tabulator(custGridRef.value, {
      layout: "fitColumns", height: "100%", placeholder: "건수: 0",
      columns: [{ title: "상 호", field: "CUSTNM", widthGrow: 1, cssClass: "fw-bold text-primary" }]
    });
    custGrid.on("rowClick", (e, row) => {
        const d = row.getData();
        masterData.CUSTCD = d.CUSTCD;
        masterData.CUSTNM = d.CUSTNM;
        fetchDetails(d.CUSTCD);
    });
  }

  if (detailGridRef.value) {
    detailGrid = new Tabulator(detailGridRef.value, {
      layout: "fitColumns", height: "100%", placeholder: "데이터가 존재하지 않습니다.",
      columnDefaults: { headerHozAlign: 'center', headerSort: false },
      columns: [
        { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
        { title: "주문부서", field: "DEPTNM", width: 120 },
        { title: "주문일", field: "ORDYMD", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
        { title: "납품일", field: "OUTYMD", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
        { title: "품 목", field: "ITEMNM", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
        { title: "규격", field: "ITSIZE", width: 150 },
        { title: "단위", field: "UNITNM", width: 70 },
        { title: "미출고", field: "JANQTY", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "출고수량", field: "IOQTY", width: 90, hozAlign: "right", editor: "number", cssClass: "bg-yellow fw-bold" }
      ]
    });
  }
}

const fetchCustList = async () => {
  try {
    const res = await api.post('/api/hsio/HSIO_550U_STR', {
        ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, WHCD: searchParam.WHCD,
        FRYMD: searchParam.FRYMD.replace(/-/g, ''), TOYMD: searchParam.TOYMD.replace(/-/g, '')
    });
    custGrid?.setData(res.data || []);
  } catch (e) { vAlertError('조회 실패') }
}

const fetchDetails = async (custCd: string) => {
  try {
    const res = await api.post('/api/hsio/HSIO_550U_STR', {
        ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, CUSTCD: custCd, WHCD: searchParam.WHCD,
        FRYMD: searchParam.FRYMD.replace(/-/g, ''), TOYMD: searchParam.TOYMD.replace(/-/g, '')
    });
    detailGrid?.setData(res.data || []);
  } catch (e) { vAlertError('상세 로드 실패') }
}

function handleOpenHelp(type: string) {
    if (type === 'TRAN') openHelp('CUST', (d) => { masterData.TRANCD = d.CUSTCD; masterData.TRANNM = d.CUSTNM });
}

function initialize() {
  resetForm(masterData); masterData.IOYMD = initYMD;
  custGrid?.clearData(); detailGrid?.clearData();
}

const formatDate = (v: any) => v && v.length === 8 ? `${v.substring(2,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;

const save = async () => {
  const items = detailGrid?.getData().filter((r: any) => Number(r.IOQTY) > 0);
  if (!items || items.length === 0) return vAlertError('출고 수량을 입력하세요.');
  if (!masterData.WHCD) return vAlertError('출고 창고를 선택하세요.');

  if (!confirm('저장하시겠습니까?')) return;

  try {
    const res = await api.post('/api/hsio/HSIO_550U_STR', {
      ...masterData,
      ACTKIND: 'A0',
      ITEMS: items,
      IOYMD: masterData.IOYMD.replace(/-/g, ''),
      USERID: authStore.USERID
    });
    if (res.data?.[0]?.ERRYN === 'N') {
      vAlert('저장되었습니다.');
      fetchCustList();
      detailGrid?.clearData();
    } else {
      vAlertError(res.data?.[0]?.MSG || '저장 실패');
    }
  } catch (e) { vAlertError('저장 처리 중 오류 발생'); }
}

onMounted(() => {
  api.post('/api/hs00/HS00_000S_STR', { GUBUN: 'W0', CMPYCD: authStore.CMPYCD }).then(r => {
    whOptions.value = (r.data || []).map((i:any)=>({
      CODECD: String(i.WHCD || i.CODECD || i.CODE || '').trim(),
      CODENM: String(i.WHNM || i.CODENM || i.CDNM || '').trim()
    }))
  });
  nextTick(() => { initGrids(); fetchCustList(); });
})
</script>

<style scoped>
/* 🎨 전역 표준(global.css)이 적용되므로, 특수 케이스가 아니면 비워둡니다. */
</style>
