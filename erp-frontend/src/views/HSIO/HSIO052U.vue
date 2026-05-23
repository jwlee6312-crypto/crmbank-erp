<!--구매정보/발주관리/일반발주등록 [ERP 프리미엄 고밀도 표준 - HSOD100U 참조] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio052u-wrapper d-flex flex-column h-100 bg-light p-0 overflow-hidden">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-cart-plus-fill me-2 text-primary" style="font-size: 16px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        발주관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">일반발주등록 (HSIO052U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchOrder">조회</button>
        <button class="btn-erp btn-save" @click="handleSave">저장</button>
        <button class="btn-erp btn-danger" @click="handleDelete" :disabled="!formData.BALNO || formData.BALNO === '0000'">삭제</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 -->
    <div class="search-bar bg-white border-bottom p-2 px-3 d-flex align-items-center flex-shrink-0 gap-4">
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold small text-secondary">발주부서:</span>
        <div class="input-group input-group-sm flex-nowrap" style="width: 250px;">
          <input v-model="searchParam.DEPTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
          <input v-model="searchParam.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" />
          <button class="btn btn-dark" @click="handleOpenHelp('S_DEPT')"><i class="bi bi-search"></i></button>
        </div>
      </div>
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold small text-secondary">발주번호:</span>
        <div class="d-flex align-items-center gap-1 flex-nowrap">
          <input v-model="searchParam.BALYM" type="month" class="form-control form-control-sm text-center" style="width: 130px;" />
          <input v-model="searchParam.BALNO" type="text" class="form-control form-control-sm text-center fw-bold text-primary" placeholder="0000" style="width: 60px;" @keyup.enter="fetchOrder" />
          <div class="btn-group btn-group-sm ms-1">
            <button class="btn btn-outline-secondary py-0 px-2" @click="moveNo(-1)"><i class="bi bi-chevron-left"></i></button>
            <button class="btn btn-outline-secondary py-0 px-2" @click="moveNo(1)"><i class="bi bi-chevron-right"></i></button>
          </div>
        </div>
      </div>
    </div>

    <!-- 💡 3. 메인 컨텐츠 영역 (gap-3 적용) -->
    <div class="flex-grow-1 overflow-auto p-3 d-flex flex-column gap-3">
      <!-- 🅰️ 마스터 정보 -->
      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
          <i class="bi bi-pencil-square me-2 text-secondary"></i>
          <span class="fw-bold small text-dark">발주 마스터 정보</span>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 100px;"><col />
              <col style="width: 100px;"><col />
              <col style="width: 100px;"><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">발주부서</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap">
                    <input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="formData.DEPTNM" type="text" class="form-control border-start-0" @keyup.enter="handleOpenHelp('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">발주번호</th>
                <td>
                  <div class="d-flex align-items-center gap-1 flex-nowrap">
                    <input v-model="uiBALYM" type="month" class="form-control text-center fw-bold bg-light" readonly style="width: 130px;" />
                    <input v-model="formData.BALNO" type="text" class="form-control text-center fw-bold text-primary bg-light" style="width: 60px;" readonly />
                  </div>
                </td>
                <th class="required">발주일자</th>
                <td><input v-model="uiBALYMD" type="date" class="form-control" /></td>
              </tr>
              <tr>
                <th class="required">거 래 처</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap">
                    <input v-model="formData.CUSTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 65px;" readonly />
                    <input v-model="formData.CUSTNM" type="text" class="form-control border-start-0" @keyup.enter="handleOpenHelp('CUST')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>주문번호</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap">
                    <input v-model="formData.ORDYM" type="month" class="form-control text-center bg-light" readonly />
                    <input v-model="formData.ORDNO" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('ORDER')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">입고일자</th>
                <td><input v-model="uiREQYMD" type="date" class="form-control" /></td>
              </tr>
              <tr>
                <th class="required">담 당 자</th>
                <td>
                  <select v-model="formData.USERID" class="form-select">
                    <option v-for="item in empOptions" :key="item.USERID" :value="item.USERID">{{ item.USERNM }}</option>
                  </select>
                </td>
                <th>특기사항</th>
                <td colspan="3"><input v-model="formData.REMARK" type="text" class="form-control" placeholder="기타 특이사항 입력" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 디테일 정보 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between h-auto">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1 text-secondary"></i> 발주 품목 상세 리스트</span>
          <button class="btn btn-grid-row-add" @click="addGridRow">
            <i class="bi bi-plus-circle"></i> 행추가
          </button>
        </div>
        <div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
          <div ref="mainGridRef" class="tabulator-full-height"></div>
        </div>
      </div>
    </div>

    <!-- 📊 4. 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
      <div class="row align-items-center">
        <div class="col-md-3 small opacity-75">선택 품목: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="me-4 small opacity-75">총 발주수량: <span class="fw-bold text-white ms-1">{{ formatNumber(totalSummary.qty) }}</span></span>
          <span class="fs-5 ms-2 fw-light">총 발주금액: <span class="fw-bold text-info ms-2">{{ formatNumber(totalSummary.sum) }}</span> 원</span>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
    <ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" :astKind="'2'" @close="itemHelpVisible = false" @confirm="onSelectItem" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import ItemHelpModal from '@/components/ItemHelpModal.vue'
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
const initYM = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
const initYMD = `${initYM.replace('-', '')}${String(now.getDate()).padStart(2, '0')}`;

const searchParam = reactive({ DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, BALYM: initYM, BALNO: '' })
const formData = reactive<any>({
  BALYM: initYM.replace('-', ''), BALNO: '0000',
  BALYMD: initYMD, REQYMD: initYMD,
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  CUSTCD: '', CUSTNM: '', USERID: String(authStore.USER_ID || '').trim(),
  ORDYM: '', ORDNO: '', REMARK: '', ASTKIND: '2'
})

const uiBALYM = computed({ get: () => formData.BALYM ? `${formData.BALYM.substring(0, 4)}-${formData.BALYM.substring(4, 6)}` : '', set: (v) => formData.BALYM = v.replace('-', '') })
const uiBALYMD = computed({ get: () => formData.BALYMD ? `${formData.BALYMD.substring(0, 4)}-${formData.BALYMD.substring(4, 6)}-${formData.BALYMD.substring(6, 8)}` : '', set: (v) => formData.BALYMD = v.replace(/-/g, '') })
const uiREQYMD = computed({ get: () => formData.REQYMD ? `${formData.REQYMD.substring(0, 4)}-${formData.REQYMD.substring(4, 6)}-${formData.REQYMD.substring(6, 8)}` : '', set: (v) => formData.REQYMD = v.replace(/-/g, '') })

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null
const activeItemCount = ref(0)
const empOptions = ref<any[]>([])

const totalSummary = computed(() => {
  const items = mainGrid?.getData() || []
  return items.reduce((acc, cur: any) => ({
    qty: acc.qty + (Number(cur.BALQTY) || 0),
    sum: acc.sum + (Number(cur.BALAMT || 0) + Number(cur.BALVAT || 0))
  }), { qty: 0, sum: 0 })
})


const initGrid = () => {
  if (!mainGridRef.value) return
  mainGrid = new Tabulator(mainGridRef.value, {
    layout: "fitColumns", height: "100%", placeholder: "발주 품목이 없습니다.",
    columnDefaults: { headerHozAlign: 'center', headerSort: false },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      { title: "품목코드", field: "ITEMCD", width: 150, hozAlign: "center"},
      {
        title: "품목명",
        field: "ITEMNM",
        minWidth: 100,
        widthGrow: 1,
        cssClass: "fw-bold",
        formatter: (cell) => `
        <div class="d-flex justify-content-between align-items-center w-100">
            <span>${cell.getValue() || ''}</span>
            <i class="bi bi-search text-primary ms-1 cursor-pointer"></i>
        </div>
        `,
        cellClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "단위", field: "UNITNM", width: 60, hozAlign: "center" },
      { title: "발주수량", field: "BALQTY", width: 90, hozAlign: "right", editor: "number", cssClass: "bg-yellow fw-bold",
        bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
      },
      { title: "단가", field: "PRICE", width: 100, hozAlign: "right", editor: "number" },
      { title: "공급가액", field: "BALAMT", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
        bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
      },
      { title: "부가세", field: "BALVAT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
        bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
      },
      { title: "합계", field: "AMTSUM", width: 120, hozAlign: "right",
        formatter: "money", formatterParams: { precision: 0 },
        cssClass: "fw-bold text-primary",
        bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
      },
      { title: "", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>", cellClick: (e, c) => { c.getRow().delete() } }
    ]
  });

  mainGrid.on("cellEdited", (cell: any) => {
    const field = cell.getField()
    if (field === 'BALQTY' || field === 'PRICE') {
      const row = cell.getRow(), d = row.getData()
      const amt = Math.floor((Number(d.BALQTY) || 0) * (Number(d.PRICE) || 0))
      const vat = Math.floor(amt * 0.1)
      // 💡 합계 필드까지 업데이트해야 bottomCalc가 갱신됨
      row.update({
        BALAMT: amt,
        BALVAT: vat,
        AMTSUM: amt + vat
      })
    }
  });
}

function handleOpenHelp(type: string, target?: any) {
  if (type === 'CUST') openHelp('CUST', (d) => { formData.CUSTCD = d.CUSTCD; formData.CUSTNM = d.CUSTNM });
  else if (type === 'DEPT' || type === 'S_DEPT') openHelp('DEPT', (d) => { if(type==='S_DEPT'){ searchParam.DEPTCD=d.DEPTCD; searchParam.DEPTNM=d.DEPTNM } else { formData.DEPTCD = d.DEPTCD; formData.DEPTNM = d.DEPTNM } });
  else if (type === 'ITEM') { currentTargetRow.value = target; itemHelpVisible.value = true; }
  else if (type === 'ORDER') {
    Object.assign(modalProps, { title: '주문 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ORDNO', large: true, data: { GUBUN: 'OR', CMPYCD: authStore.CMPYCD }, columns: [{ title: '주문월', field: 'ORDYM', width: 100 }, { title: '번호', field: 'ORDNO', width: 80 }, { title: '거래처', field: 'CUSTNM', width: 200 }], onConfirm: (d: any) => { formData.ORDYM = d.ORDYM; formData.ORDNO = d.ORDNO; formData.CUSTCD = d.CUSTCD; formData.CUSTNM = d.CUSTNM; } });
    modalVisible.value = true;
  }
}

const onSelectItem = (d: any) => {
  if (!currentTargetRow.value) return
  currentTargetRow.value.update({ ITEMCD: d.ITEMCD, ITEMNM: d.ITEMNM, ITSIZE: d.ITSIZE, UNITNM: d.UNITNM, PRICE: d.INCOST || 0 });
  itemHelpVisible.value = false; updateItemCount();
}

const updateItemCount = () => { activeItemCount.value = mainGrid?.getData().length || 0; }
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

async function fetchOrder() {
  if (!searchParam.BALYM || !searchParam.BALNO) return vAlertError('조회할 발주번호를 입력하세요.');
  try {
    const res = await api.post('/api/hsio/HSIO_052U_STR', { ACTKIND: 'S', ...searchParam, CMPYCD: authStore.CMPYCD });
    if (res.data?.length) {
      Object.assign(formData, res.data[0]);
      mainGrid?.setData(res.data[0].ITEMS || []);
      updateItemCount();
      vAlert('조회되었습니다.');
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function handleSave() {
  const items = mainGrid?.getData();
  if (!items?.length) return vAlertError('발주 품목이 없습니다.')
  try {
    const act = (formData.BALNO === '0000' || !formData.BALNO) ? 'A' : 'U';
    await api.post('/api/hsio/HSIO_052U_STR', { ...formData, ACTKIND: act, ITEMS: items, UPDEMP: authStore.USER_ID });
    vAlert('저장되었습니다.'); fetchOrder();
  } catch (e) { vAlertError('저장 실패') }
}

async function handleDelete() {
  if (!confirm('발주 정보를 삭제하시겠습니까?')) return;
  try {
    await api.post('/api/hsio/HSIO_052U_STR', { ACTKIND: 'D', ...formData });
    vAlert('삭제되었습니다.'); initialize();
  } catch (e) { vAlertError('삭제 실패') }
}

function initialize() {
  resetForm(formData); formData.BALNO = '0000'; formData.BALYMD = initYMD; mainGrid?.clearData(); updateItemCount();
}

function moveNo(step: number) {
  if (!searchParam.BALNO) searchParam.BALNO = '0001';
  else searchParam.BALNO = String(Math.max(1, parseInt(searchParam.BALNO) + step)).padStart(4, '0');
  fetchOrder();
}

const addGridRow = () => { mainGrid?.addRow({ BALQTY: 0, PRICE: 0, BALAMT: 0, BALVAT: 0 }); updateItemCount(); }

onMounted(() => {
  api.post('/api/ha00/HA00_00P_STR', { GUBUN: 'SD', CMPYCD: authStore.CMPYCD, GBNCD: '' }).then(r => empOptions.value = r.data);
  nextTick(() => initGrid());
})
</script>

<style scoped>
.hsio052u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.flex-shrink-0 { flex-shrink: 0 !important; }
.flex-grow-1 { flex-grow: 1 !important; min-height: 0 !important; }
.overflow-hidden { overflow: hidden !important; }
/* 🚀 입력 필드 글자 크기 및 높이 최적화 (HSBA070U 패턴) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}
.erp-table-full { width: 100%; border-collapse: collapse; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 4px 5px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 2px 4px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }

/* 🚀 팝업 가독성 표준 스타일 */
:deep(.modal-content) { background-color: #ffffff !important; }
:deep(.modal-content .tabulator) { background-color: #ffffff !important; color: #000000 !important; border: 1px solid #dee2e6 !important; }
:deep(.modal-content .tabulator-cell) { color: #000000 !important; font-size: 13px !important; padding: 8px !important; }

</style>
