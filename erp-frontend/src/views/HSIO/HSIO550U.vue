<!--영업관리/출고관리/주문출고처리 [ERP 프리미엄 고밀도 표준 - 최종 복구본] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio550u-wrapper d-flex flex-column h-100 bg-light p-0">
    <!-- 🚀 1. 상단 액션 바 (표준 버튼 색상 적용) -->
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
    <div class="search-bar bg-white border-bottom p-2 px-3 d-flex align-items-center flex-shrink-0 gap-4">
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold small text-secondary">출고창고:</span>
        <select v-model="searchParam.WHCD" class="form-select form-select-sm" style="width: 120px;">
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

    <!-- 💡 3. 메인 분할 영역 (Split Layout - gap-3 적용) -->
    <div class="flex-grow-1 d-flex flex-row gap-3 p-3 overflow-hidden">

      <!-- 🅰️ 좌측: 거래처 목록 (표준 헤더 적용) -->
      <div class="card border-0 shadow-sm d-flex flex-column flex-shrink-0" style="width: 320px;">
        <div class="card-header bg-white text-dark fw-bold border-bottom py-2 ps-3 h-auto" style="font-size: 13px;">
          <i class="bi bi-list-ul me-1 text-secondary"></i> 상 호
        </div>
        <div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
          <div ref="custGridRef" class="tabulator-full-height"></div>
        </div>
      </div>

      <!-- 🅱️ 우측: 상세 정보 및 주문 내역 (Detail Area) -->
      <div class="d-flex flex-column flex-grow-1 gap-3 overflow-hidden">
        <!-- 마스터 정보 카드 -->
        <div class="card border-0 shadow-sm flex-shrink-0 overflow-hidden">
          <div class="card-header bg-white text-dark fw-bold border-bottom py-2 ps-3 h-auto" style="font-size: 13px;">
            <i class="bi bi-pencil-square me-1 text-secondary"></i> 출고 상세 정보
          </div>
          <div class="card-body p-0 bg-white">
            <table class="erp-table-full border-0">
              <colgroup>
                <col style="width: 1%;"><col />
                <col style="width: 1%;"><col />
                <col style="width: 1%;"><col />
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
                      <option v-for="opt in whOptions" :key="opt.WHCD" :value="opt.WHCD">{{ opt.WHNM }}</option>
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
        <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
          <div class="card-header bg-white text-dark fw-bold border-bottom py-2 ps-3 h-auto">
            <span class="small"><i class="bi bi-list-check me-1 text-secondary"></i> 주문 상세 내역</span>
          </div>
          <div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
            <div ref="detailGridRef" class="tabulator-full-height"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
      <div class="row align-items-center">
        <div class="col-md-3 small opacity-75">선택 품목: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="fs-5 ms-2 fw-light">총 출고합계: <span class="fw-bold text-info ms-2">{{ formatNumber(sumTotal) }}</span> 원</span>
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
const activeItemCount = ref(0); const sumTotal = ref(0);

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
        { title: "주문일", field: "ORDYMD", width: 100, hozAlign: "center" },
        { title: "납품일", field: "OUTYMD", width: 100, hozAlign: "center" },
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
    setTimeout(updateTotals, 100);
  } catch (e) { vAlertError('상세 로드 실패') }
}

const updateTotals = () => { if (!detailGrid) return; const data = detailGrid.getData(); activeItemCount.value = data.length; sumTotal.value = data.reduce((acc, i) => acc + (Number(i.IOQTY) * (Number(i.PRICE) || 0)), 0) }
const formatNumber = (v: any) => new Intl.NumberFormat().format(Number(v) || 0)

function handleOpenHelp(type: string) {
    if (type === 'TRAN') openHelp('CUST', (d) => { masterData.TRANCD = d.CUSTCD; masterData.TRANNM = d.CUSTNM });
}

function initialize() {
  resetForm(masterData); masterData.IOYMD = initYMD;
  custGrid?.clearData(); detailGrid?.clearData(); updateTotals();
}

onMounted(() => {
  api.post('/api/hs00/HS00_000S_STR', { GUBUN: 'W0', CMPYCD: authStore.CMPYCD }).then(r => { whOptions.value = r.data.map((i:any)=>({CODECD:i.CODE||i.CODECD, CODENM:i.CDNM||i.CODENM})) });
  nextTick(() => { initGrids(); fetchCustList(); });
})
</script>

<style scoped>
.hsio550u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }

/* 💎 표준 버튼 팔레트 */
.btn-erp { padding: 4px 18px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

/* 💎 프리미엄 테이블 표준 (지능형 라벨) */
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  width: 1%; white-space: nowrap;
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12px; padding: 8px 15px !important; color: #475569;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }

.bg-yellow { background-color: #fffde7 !important; }
.required::after { content: ' *'; color: #ef4444; }

/* 💎 Tabulator 표준 디자인 */
:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 800 !important; color: #334155 !important; }
:deep(.tabulator-row.tabulator-selected) { background-color: #f0f7ff !important; border-left: 4px solid #005a9f !important; }
</style>
