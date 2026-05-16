<!--영업정보/출고관리/기타출고처리 [ERP 프리미엄 고밀도 표준 - 최종 완성본] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio500u-wrapper d-flex flex-column h-100 bg-light p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">무주문 출고처리 (HSIO500U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMaster">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-danger" @click="deleteMaster" :disabled="!masterData.IONO || masterData.IONO === '0000'">삭제</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 (gap-3 적용) -->
    <div class="flex-grow-1 overflow-auto p-3 d-flex flex-column gap-3">
      <!-- 🅰️ 마스터 정보 카드 -->
      <div class="card border-0 shadow-sm overflow-hidden">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
          <div class="fw-bold small text-dark"><i class="bi bi-info-circle-fill me-1 text-secondary"></i>출고 기본 정보</div>
          <div v-if="masterData.STS === 'Y'" class="badge bg-success-subtle text-success border border-success-subtle px-2">승인완료</div>
          <div v-else class="badge bg-danger-subtle text-danger border border-danger-subtle px-2">미승인</div>
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
                <th class="required">출고부서</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap" style="width: 100%;">
                    <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="masterData.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">출고번호</th>
                <td>
                  <div class="d-flex align-items-center gap-1 flex-nowrap">
                    <input v-model="uiIOYM" type="month" class="form-control text-center fw-bold" style="width: 130px;" />
                    <input v-model="masterData.IONO" type="text" class="form-control text-center fw-bold text-primary bg-light" placeholder="0000" style="width: 60px;" readonly />
                  </div>
                </td>
                <th class="required">출고일자</th>
                <td><input v-model="uiIOYMD" type="date" class="form-control" /></td>
              </tr>
              <tr>
                <th class="required">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 70px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control border-start-0" placeholder="거래처 선택" @keyup.enter="handleOpenHelp('CUST')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">출고창고</th>
                <td>
                  <select v-model="masterData.WHCD" class="form-select">
                    <option v-for="opt in whOptions" :key="opt.WHCD" :value="opt.WHCD">{{ opt.WHNM }}</option>
                  </select>
                </td>
                <th>출고유형</th>
                <td>
                  <select v-model="masterData.IOTYPE" class="form-select">
                    <option v-for="opt in typeOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>배&nbsp;&nbsp;송&nbsp;&nbsp;처</th>
                <td colspan="3">
                  <AddressPopupForm
                    v-model:postno="addrData.POSTNO"
                    v-model:address="addrData.ADDR1"
                    v-model:d_address="addrData.ADDR2"
                    :active="!!masterData.CUSTCD"
                    @open-address="handleOpenHelp('ADDR')"
                  />
                </td>
                <th>특기사항</th>
                <td><input v-model="masterData.REMARK" type="text" class="form-control" placeholder="출고 관련 메모 입력" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 디테일 품목 리스트 카드 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1 text-secondary"></i> 출고 품목 리스트</span>
          <button class="btn btn-xs btn-primary fw-bold" @click="addRow" :disabled="masterData.STS === 'Y'"><i class="bi bi-plus-lg me-1"></i> 행추가</button>
        </div>
        <div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
      <div class="row align-items-center">
        <div class="col-md-3 small opacity-75">출고 품목수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="fs-5 ms-2 fw-light">총 합계금액: <span class="fw-bold text-info ms-2">{{ formatNumber(sumTotal) }}</span> 원</span>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
    <ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" :astKind="'2'" @close="itemHelpVisible = false" @confirm="onSelectItem" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import ItemHelpModal from '@/components/ItemHelpModal.vue'
import AddressPopupForm from '@/components/AddressPopupForm.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const itemHelpVisible = ref(false)
const currentTargetRow = ref<any>(null)

const now = new Date();
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`;
const initYMD = `${initYM}${String(now.getDate()).padStart(2, '0')}`;

const masterData = reactive<any>({
  ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOYM: initYM, IONO: '0000', DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  CUSTCD: '', CUSTNM: '', IOYMD: initYMD, WHCD: '', IOTYPE: '100', ADDRESS: '', REMARK: '', STS: 'N',
  USERID: authStore.USER_ID, CLSYMD: '', SCLSYM: '', ORDKIND: '100', JANAMT: 0, RCVDD: 0, HANAMT: 0, HANDD: '', ENDYMD: ''
})

const addrData = reactive({ POSTNO: '', ADDR1: '', ADDR2: '' })
const uiIOYM = computed({ get: () => masterData.IOYM ? `${masterData.IOYM.substring(0, 4)}-${masterData.IOYM.substring(4, 6)}` : '', set: (v) => masterData.IOYM = v.replace('-', '') })
const uiIOYMD = computed({ get: () => masterData.IOYMD ? `${masterData.IOYMD.substring(0, 4)}-${masterData.IOYMD.substring(4, 6)}-${masterData.IOYMD.substring(6, 8)}` : '', set: (v) => masterData.IOYMD = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null);
const grid = ref<Tabulator | null>(null);
const activeItemCount = ref(0); const sumTotal = ref(0);
const empOptions = ref<any[]>([]); const whOptions = ref<any[]>([]); const typeOptions = ref<any[]>([])

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "등록된 품목이 없습니다.",
    columnDefaults: { headerHozAlign: 'center', headerSort: false },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      { title: "품목코드", field: "ITEMCD", width: 120, formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>", cellClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "품목명칭", field: "ITEMNM", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
      { title: "규격", field: "ITSIZE", width: 150 },
      { title: "단위", field: "UNITNM", width: 80, hozAlign: "center" },
      { title: "출고수량", field: "IOQTY", width: 90, hozAlign: "right", editor: "number", cssClass: "bg-yellow fw-bold" },
      { title: "단가", field: "PRICE", width: 100, hozAlign: "right", editor: "number" },
      { title: "금액", field: "AMTSUM", width: 120, hozAlign: "right", formatter: "money", bottomCalc: "sum", cssClass: "fw-bold" },
      { title: "", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>", cellClick: (e, c) => { c.getRow().delete(); updateTotals() } }
    ]
  });
}

const updateTotals = () => { if (!grid.value) return; const data = grid.value.getData(); activeItemCount.value = data.length; sumTotal.value = data.reduce((acc, i) => acc + (Number(i.AMTSUM) || 0), 0) }

const handleOpenHelp = (type: string, target?: any) => {
  if (type === 'CUST') openHelp('CUST', (d) => { masterData.CUSTCD = d.CUSTCD; masterData.CUSTNM = d.CUSTNM; });
  else if (type === 'DEPT') openHelp('DEPT', (d) => { masterData.DEPTCD = d.DEPTCD; masterData.DEPTNM = d.DEPTNM });
  else if (type === 'ITEM') { currentTargetRow.value = target; itemHelpVisible.value = true; }
  else if (type === 'ADDR') { openHelp('CUST', (d) => { addrData.ADDR1 = d.ADDRESS; masterData.TRANCD = d.TRANCD }); }
}

const onSelectItem = (d: any) => {
  if (!currentTargetRow.value) return
  currentTargetRow.value.update({ ITEMCD: d.ITEMCD, ITEMNM: d.ITEMNM, ITSIZE: d.ITSIZE, UNITNM: d.UNITNM, PRICE: d.OUTCOST || 0 });
  itemHelpVisible.value = false; updateTotals();
}

function addRow() { grid.value?.addRow({ IOQTY: 0, PRICE: 0, AMTSUM: 0 }) }
function initialize() { resetForm(masterData); masterData.IOYMD = initYMD; grid.value?.clearData(); updateTotals(); }
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

async function fetchMaster() { if (!masterData.IOYM || !masterData.IONO) return vAlertError('출고번호 입력 필수'); try { const res = await api.post('/api/hsio/HSIO_500U_STR', { ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOGBN: '200', IOYM: masterData.IOYM, IONO: masterData.IONO }); if (res.data?.length) { Object.assign(masterData, res.data[0]); addrData.ADDR1 = masterData.ADDRESS; fetchDetail() } } catch (e) { vAlertError('조회 실패') } }
async function fetchDetail() { try { const res = await api.post('/api/hsio/HSIO_501U_STR', { ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOGBN: '200', IOYM: masterData.IOYM, IONO: masterData.IONO }); if (grid.value) { grid.value.setData(res.data); setTimeout(updateTotals, 100) } } catch (e) { vAlertError('상세 로드 실패') } }

async function save() {
  if (!masterData.CUSTCD) return vAlertError('거래처를 선택하세요.');
  try {
    const act = (masterData.IONO === '0000' || !masterData.IONO) ? 'A' : 'U';
    await api.post('/api/hsio/HSIO_500U_STR', { ...masterData, ACTKIND: act, UPDEMP: authStore.USER_ID });
    vAlert('저장되었습니다.'); fetchMaster();
  } catch (e) { vAlertError('저장 실패') }
}

async function deleteMaster() {
  if (!confirm("정말 삭제하시겠습니까?")) return;
  try {
    await api.post('/api/hsio/HSIO_500U_STR', { ACTKIND: 'D', CMPYCD: authStore.CMPYCD, IOGBN: '200', IOYM: masterData.IOYM, IONO: masterData.IONO });
    vAlert('삭제완료'); initialize();
  } catch (e) { vAlertError('삭제 실패'); }
}

onMounted(() => {
  api.post('/api/hs00/HS00_000S_STR', { GUBUN: 'W0', CMPYCD: authStore.CMPYCD }).then(r => { whOptions.value = r.data.map((i: any) => ({ WHCD: String(i.CODE || i.CODECD || Object.values(i)[0]).trim(), WHNM: String(i.CDNM || i.CODENM || Object.values(i)[1]).trim() })) });
  api.post('/api/ha00/HA00_00P_STR', { GUBUN: 'E0', GBNCD: '330', CMPYCD: authStore.CMPYCD }).then(r => { typeOptions.value = r.data.map((i: any) => ({ CODECD: String(i.CODECD || i.code || Object.values(i)[0]).trim(), CODENM: String(i.CODENM || i.cdnm || Object.values(i)[1]).trim() })) });
  nextTick(() => initGrid());
})
</script>

<style scoped>
.hsio500u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }
.btn-erp { padding: 4px 18px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8fafc; border: 1px solid #dee2e6 !important; text-align: center; font-weight: 800; font-size: 12.5px; color: #475569; vertical-align: middle; padding: 10px 15px !important; white-space: nowrap !important; }
.erp-table-full td { vertical-align: middle; padding: 8px 12px !important; border: 1px solid #dee2e6 !important; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }

.form-control, .form-select { border-radius: 4px; border: 1px solid #cbd5e1; font-size: 13px; height: 32px; width: 100%; font-weight: 500; }
.bg-yellow { background-color: #fffde7 !important; }

:deep(.tabulator) { border: none; font-size: 12.5px; background-color: transparent !important; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 800; color: #334155; }
:deep(.tabulator-row.tabulator-selected) { background-color: #f0f7ff !important; border-left: 4px solid #005a9f !important; }
</style>
