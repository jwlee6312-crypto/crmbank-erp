<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio560s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-x-circle-fill me-2 text-danger" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-danger fw-bolder">출고취소 (HSIO560S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-save" @click="saveCancel" :disabled="activeItemCount === 0">취소처리</button>
      </div>
    </div>

    <!-- 🔍 2. 최상단 조회 조건 바 (ASP 재현) -->
    <div class="p-2 pb-0">
      <div class="card border shadow-sm bg-light bg-opacity-50">
        <div class="card-body py-2 px-3">
          <div class="d-flex align-items-center gap-4">
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-dark" style="min-width: 60px;">출고창고</span>
              <select v-model="searchForm.WHCD" class="form-select form-select-sm" style="width: 150px;">
                <option v-for="opt in whOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
              </select>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-dark" style="min-width: 60px;">출고일자</span>
              <div class="d-flex align-items-center gap-1">
                <input v-model="searchForm.OUTYMDFR" type="date" class="form-control form-control-sm" style="width: 140px;" />
                <span class="text-muted">~</span>
                <input v-model="searchForm.OUTYMDTO" type="date" class="form-control form-control-sm" style="width: 140px;" />
              </div>
            </div>
            <button class="btn btn-sm btn-dark px-4 fw-bold ms-auto" @click="fetchCustList">
              <i class="bi bi-search me-1"></i>조회
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 💡 3. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex gap-2">

      <!-- 🅰️ 좌측: 출고 완료 상호 목록 (S1 액션) -->
      <div class="card border shadow-sm d-flex flex-column" style="width: 320px; min-width: 320px;">
        <div class="card-header bg-light py-1 px-3 border-bottom">
          <span class="fw-bold small text-dark"><i class="bi bi-list-check me-1"></i> 출고 완료 상호</span>
        </div>
        <div class="flex-grow-1 overflow-auto bg-white p-0">
          <table class="table table-hover mb-0" style="table-layout: fixed; font-size: 12px;">
            <thead class="table-light sticky-top">
              <tr>
                <th style="width: 70%; text-align: center;">상호</th>
                <th style="width: 30%; text-align: center;">출고일</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(cust, idx) in custList" :key="idx"
                  class="clickable"
                  :class="{ 'table-danger': selectedCust?.IONO === cust.IONO }"
                  @click="selectCust(cust)">
                <td class="text-truncate px-2">{{ idx + 1 }}. {{ cust.CUSTNM }}</td>
                <td class="text-center px-1">{{ formatDateDot(cust.IOYMD) }}</td>
              </tr>
              <tr v-if="custList.length === 0">
                <td colspan="2" class="text-center p-4 text-muted italic">데이터가 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 우측: 선택 내역 및 품목 그리드 (S0 액션) -->
      <div class="d-flex flex-column flex-grow-1 gap-2 overflow-hidden">

        <!-- 마스터 정보 -->
        <div class="card border shadow-sm">
          <div class="card-body p-0">
            <table class="erp-table-full">
              <tbody>
                <tr>
                  <th>거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                  <td style="width: 35%;">
                    <input :value="masterData.CUSTNM" type="text" class="form-control form-control-sm bg-light" readonly />
                  </td>
                  <th>창&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                  <td style="width: 20%;">
                    <input :value="masterData.WHNM" type="text" class="form-control form-control-sm bg-light" readonly />
                  </td>
                  <th>출고일자</th>
                  <td>
                    <input :value="formatDateDash(masterData.IOYMD)" type="text" class="form-control form-control-sm bg-light text-center" readonly />
                  </td>
                </tr>
                <tr>
                  <th>배&nbsp;&nbsp;송&nbsp;&nbsp;처</th>
                  <td colspan="3">
                    <input :value="masterData.ADDRES" type="text" class="form-control form-control-sm bg-light" readonly />
                  </td>
                  <th>배송담당</th>
                  <td>
                    <input :value="masterData.TRNEMPNM" type="text" class="form-control form-control-sm bg-light text-center" readonly />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 품목 그리드 -->
        <div class="card border shadow-sm flex-grow-1 overflow-hidden bg-white">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
            <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 출고 품목 상세 내역</span>
            <span class="badge bg-danger text-white px-2">취소 항목 선택</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="gridElement" style="height: 100%;"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-3 small">취소 선택: <span class="fw-bold text-warning">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="me-4 small opacity-75">선택 총 수량: <span class="fw-bold text-info ms-1">{{ formatNumber(qtySum) }}</span></span>
          <span class="fs-6 fw-light text-danger italic">※ 정산 완료된 자료는 취소할 수 없습니다.</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date();
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`;
const initYMD = `${initYM}${String(now.getDate()).padStart(2, '0')}`;

const searchForm = reactive({
  WHCD: '100',
  OUTYMDFR: `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`,
  OUTYMDTO: `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
})

const masterData = reactive<any>({
  CUSTCD: '', CUSTNM: '', WHCD: '', WHNM: '', IOYMD: '', ADDRES: '', TRNEMPNM: '', IOYM: '', IONO: '',
  CLSYMD: '', SCLSYM: ''
})

const gridElement = ref<HTMLElement | null>(null);
const grid = ref<Tabulator | null>(null);
const activeItemCount = ref(0);
const qtySum = ref(0);

const custList = ref<any[]>([]);
const selectedCust = ref<any>(null);
const whOptions = ref<any[]>([]);

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "상호 목록을 먼저 조회하세요.",
    selectable: true,
    columnDefaults: { minWidth: 100, headerSort: false },
    columns: [
      { title: "", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center" },
      { title: "품목", field: "ITEMNM", minWidth: 200, widthGrow: 1 },
      { title: "규격", field: "ITSIZE", width: 150 },
      { title: "단위", field: "UNIT", width: 70, hozAlign: "center" },
      { title: "수량", field: "IOQTY", width: 100, hozAlign: "right", formatter: (c) => formatNumber(c.getValue()) },
      { title: "금액", field: "JSANAMT", width: 110, hozAlign: "right", formatter: (c) => formatNumber(c.getValue()) },
      { title: "부가세", field: "JSANVAT", width: 100, hozAlign: "right", formatter: (c) => formatNumber(c.getValue()) },
      { title: "합계", field: "IOSUM", width: 120, hozAlign: "right", cssClass: "fw-bold", formatter: (c) => formatNumber(c.getValue()) }
    ],
    rowSelectionChanged: (data) => {
       activeItemCount.value = data.length;
       qtySum.value = data.reduce((acc, cur) => acc + (Number(cur.IOQTY) || 0), 0);
    }
  });
}

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_560U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, IOGBN: '200',
      OUTYMDFR: searchForm.OUTYMDFR.replace(/-/g, ''),
      OUTYMDTO: searchForm.OUTYMDTO.replace(/-/g, ''),
      WHCD: searchForm.WHCD
    });
    custList.value = res.data || [];
    if (grid.value) grid.value.clearData();
    selectedCust.value = null;
  } catch (e) { vAlertError('목록 조회 실패'); }
}

async function selectCust(cust: any) {
  selectedCust.value = cust;
  Object.assign(masterData, cust);
  fetchDetail(cust);
}

async function fetchDetail(cust: any) {
  try {
    const res = await api.post('/api/hsio/HSIO_560U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOGBN: '200',
      OUTYMDFR: searchForm.OUTYMDFR.replace(/-/g, ''),
      OUTYMDTO: searchForm.OUTYMDTO.replace(/-/g, ''),
      CUSTCD: cust.CUSTCD, WHCD: cust.WHCD, IOYM: cust.IOYM, IONO: cust.IONO
    });
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => ({
        ...i,
        IOSUM: (Number(i.JSANAMT) || 0) + (Number(i.JSANVAT) || 0)
      })));
    }
  } catch (e) { vAlertError('상세 내역 로드 실패'); }
}

async function saveCancel() {
  const items = grid.value?.getSelectedData();
  if (!items || items.length === 0) return vAlertError('취소할 품목을 선택하세요.');

  // 정산 체크 (ASP 로직)
  if (items.some((i: any) => i.JYN === 'Y')) return vAlertError('정산 완료된 자료입니다. 취소할 수 없습니다.');

  // 마감 체크 (ASP 로직)
  const ioYmd = String(masterData.IOYMD);
  if (ioYmd <= masterData.CLSYMD) return vAlertError('회계 마감이 되었습니다. 해당 일자로 작업할 수 없습니다.');
  if (ioYmd.substring(0,6) <= masterData.SCLSYM) return vAlertError('영업 마감이 되었습니다. 해당 일자로 작업할 수 없습니다.');

  if (!confirm("출고처리를 취소하시겠습니까?")) return;

  try {
    for (const item of items) {
      await api.post('/api/hsio/HSIO_560U_STR', {
        ACTKIND: 'D0', CMPYCD: authStore.CMPYCD, IOGBN: '200',
        OUTYMDFR: searchForm.OUTYMDFR.replace(/-/g, ''),
        OUTYMDTO: searchForm.OUTYMDTO.replace(/-/g, ''),
        WHCD: masterData.WHCD, CUSTCD: masterData.CUSTCD,
        IOYM: masterData.IOYM, IONO: masterData.IONO, IOROWNO: item.IOROWNO,
        UPDEMP: authStore.USERID
      });
    }
    vAlert('정상으로 취소되었습니다.');
    fetchCustList();
  } catch (e) { vAlertError('취소 처리 실패'); }
}

function initialize() {
  custList.value = []; selectedCust.value = null;
  resetForm(masterData);
  if (grid.value) grid.value.clearData();
}

async function loadComboCodes(gbn: string, gbnCd: string) {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: gbn, CMPYCD: authStore.CMPYCD, GBNCD: gbnCd, LIMITOFFSET: 0, LIMITROWS: 9999 } });
    return res.data.map((item: any) => ({ CODECD: String(item.CODE || item.CODECD || item.WHCD || Object.values(item)[0]).trim(), CODENM: String(item.CDNM || item.CODENM || item.WHNM || Object.values(item)[1]).trim() }))
  } catch (e) { return [] }
}

function formatNumber(val: any) { return new Intl.NumberFormat().format(Number(val) || 0) }
function formatDateDot(val: any) { return val && val.length === 8 ? `${val.substring(0,4)}.${val.substring(4,6)}.${val.substring(6,8)}` : val; }
function formatDateDash(val: any) { return val && val.length === 8 ? `${val.substring(0,4)}-${val.substring(4,6)}-${val.substring(6,8)}` : val; }

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => { if (r.data?.length) { masterData.CLSYMD = String(Object.values(r.data[0])[0]).trim(); masterData.SCLSYM = String(Object.values(r.data[0])[1]).trim(); } })
  whOptions.value = await loadComboCodes('W0', '');
  nextTick(initGrid);
})
</script>

<style scoped>
.hsio560s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-save { background-color: #d32f2f !important; color: #fff !important; border: none !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.cust-list .list-group-item { cursor: pointer; transition: background 0.15s; border-left: 3px solid transparent; }
.cust-list .list-group-item:hover { background-color: #f8f9fa; }
.cust-list .list-group-item.active { border-left: 3px solid #dc3545; background-color: #fff5f5; }
.erp-table-full th { width: 12% !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px !important; color: #444; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px !important; vertical-align: middle; }
.extra-small { font-size: 10px; }
.italic { font-style: italic; }
</style>
