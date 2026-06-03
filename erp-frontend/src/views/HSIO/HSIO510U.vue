<!--영업정보/매출관리/매출정산 [ERP 프리미엄 고밀도 표준 적용] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매출정산 (HSIO510U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchCustomerList">조회</button>
        <button class="btn-erp btn-save" @click="saveSettlement" :disabled="!selectedCust.custcd">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" /><col style="width: 23%" />
              <col style="width: 10%" /><col style="width: 23%" />
              <col style="width: 10%" /><col style="width: 24%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center">판매부서</th>
                <td>
                  <div class="input-group input-group-sm w-75">
                    <input v-model="searchData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.deptnm" type="text" class="form-control" placeholder="부서 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('SEARCH_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required bg-light text-center">판매일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchData.ioymdfr" v-model:todt="searchData.ioymdto" />
                </td>
                <th class="bg-light text-center">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm w-75">
                    <input v-model="searchData.custcd" type="text" class="form-control text-center bg-light" readonly />
                    <input v-model="searchData.custnm" type="text" class="form-control" placeholder="거래처 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('SEARCH_CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 메인 작업 영역 -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden" style="min-height: 0;">

        <!-- 좌측: 거래처 목록 -->
        <div class="card border shadow-sm d-flex flex-column flex-shrink-0" style="width: 280px;">
          <div class="card-header bg-white text-dark fw-bold border-bottom py-1 ps-3 h-auto" style="font-size: 13px;">
            <i class="bi bi-list-ul me-1 text-secondary"></i> 정산 대상 거래처
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="custGridElement" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- 우측: 상세 정보 및 품목 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden" style="min-height: 0;">

          <!-- 정산 등록 정보 -->
          <div class="card border shadow-sm overflow-hidden flex-shrink-0">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup>
                  <col style="width: 100px;" /><col />
                  <col style="width: 100px;" /><col />
                  <col style="width: 100px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="bg-light text-center">거 래 처</th>
                    <td>
                      <input v-model="selectedCust.custnm" type="text" class="form-control bg-light fw-bold" readonly />
                    </td>
                    <th class="required bg-light text-center">판매부서</th>
                    <td>
                      <div class="input-group input-group-sm flex-nowrap">
                        <input v-model="masterData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                        <input v-model="masterData.deptnm" type="text" class="form-control" readonly />
                        <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('MASTER_DEPT')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light text-center">발 행 일</th>
                    <td><input v-model="masterData.pubymd" type="date" class="form-control" /></td>
                  </tr>
                  <tr>
                    <th class="bg-light text-center">사 업 장</th>
                    <td>
                      <select v-model="masterData.taxunit" class="form-select">
                        <option v-for="opt in taxUnitOptions" :key="opt.taxunit" :value="opt.taxunit">{{ opt.unitnm }}</option>
                      </select>
                    </td>
                    <th class="bg-light text-center">유&nbsp;&nbsp;&nbsp;&nbsp;형</th>
                    <td>
                      <select v-model="masterData.vattype" class="form-select">
                        <option v-for="opt in vatTypeOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                      </select>
                    </td>
                    <td colspan="2" class="bg-light-subtle px-3 text-muted small text-center">
                      <i class="bi bi-info-circle me-1"></i> 정산 대상을 선택 후 저장하십시오.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 정산 품목 내역 -->
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center"><i class="bi bi-grid-3x3-gap-fill me-1 text-primary"></i> 정산 품목 내역</span>
              <button class="btn btn-xs btn-outline-secondary px-2" style="height: 24px; font-size: 11px;" @click="toggleAllSelection">전체선택</button>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                <div ref="itemGridElement" class="tabulator-instance flex-grow-1"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'
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
const initymd = now.toISOString().substring(0, 10)
const initfrymd = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)

// [1] 데이터 모델링 (소문자 통일)
const searchData = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  ioymdfr: initfrymd, ioymdto: initymd, custcd: '', custnm: ''
})

const masterData = reactive<any>({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  taxunit: '', vattype: '010', pubymd: initymd,
  splamt: 0, vatamt: 0, pricegbn: '1', clsymd: '', sclsym: ''
})

const selectedCust = reactive({ custcd: '', custnm: '', def_vattype: '010' })

// [2] 그리드 관련
const custGridElement = ref<HTMLElement | null>(null)
const itemGridElement = ref<HTMLElement | null>(null)
let custGrid: Tabulator | null = null
let itemGrid: Tabulator | null = null

const allSelected = ref(true)
const taxUnitOptions = ref<any[]>([])
const vatTypeOptions = ref<any[]>([])

const initGrids = () => {
  if (custGridElement.value) {
    custGrid = new Tabulator(custGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
      columns: [{ title: "거래처 상호", field: "custnm", headerSort: false, cssClass: "fw-bold text-primary" }],
    })
    custGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      selectedCust.custcd = data.custcd; selectedCust.custnm = data.custnm;
      selectedCust.def_vattype = data.def_vattype || '010'
      masterData.vattype = selectedCust.def_vattype
      fetchSettlementItems()
    })
  }

  if (itemGridElement.value) {
    itemGrid = new Tabulator(itemGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "거래처를 선택하세요.",
      columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: "middle" },
      columns: [
        { title: "선택", field: "procyn", width: 60, hozAlign: "center", formatter: "tickCross", editor: true },
        { title: "출고일", field: "salsymd", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
        { title: "품목명", field: "itemnm", minWidth: 180, widthGrow: 1, cssClass: "fw-bold" },
        { title: "규격", field: "itsize", width: 120 },
        { title: "단위", field: "unit", width: 60, hozAlign: "center" },
        { title: "수량", field: "ioqty", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "정산수량", field: "jioqty", width: 80, hozAlign: "right", editor: "number", cssClass: "bg-yellow" },
        { title: "단가", field: "price", width: 100, hozAlign: "right", editor: "number", formatter: "money" },
        { title: "공급가", field: "ioamt", width: 110, hozAlign: "right", editor: "number", formatter: "money" },
        { title: "부가세", field: "iovat", width: 100, hozAlign: "right", editor: "number", formatter: "money" }
      ]
    });
    itemGrid.on("cellEdited", (cell) => {
        if (['jioqty', 'price', 'ioamt', 'iovat', 'procyn'].includes(cell.getField())) {
            if (['jioqty', 'price'].includes(cell.getField())) calcRow(cell.getRow());
            else if (cell.getField() === 'ioamt') calcRow(cell.getRow(), 'AMT');
            updateTotals();
        }
    });
  }
}

// [3] 로직 및 계산
const calcRow = (row: Tabulator.RowComponent, type: string = 'QTY') => {
  const d = row.getData(); const prcGbn = masterData.pricegbn;
  let amt = Number(d.ioamt) || 0; let vat = Number(d.iovat) || 0;
  if (type === 'QTY') {
    if (prcGbn === '1') {
        amt = Math.round(Number(d.jioqty) * Number(d.price));
        vat = Math.round(amt * 0.1);
    } else {
        amt = Math.round(Number(d.jioqty) * Number(d.price) * (10 / 11));
        vat = Math.round(Number(d.jioqty) * Number(d.price) * (1 / 11));
    }
  } else if (type === 'AMT') {
    vat = Math.floor(amt * 0.1);
    row.update({ price: Math.floor((prcGbn === '1' ? amt : amt + vat) / (Number(d.jioqty) || 1)) });
  }
  row.update({ ioamt: amt, iovat: vat });
}

const updateTotals = () => {
  const data = itemGrid?.getData().filter((i: any) => i.procyn === 'y' || i.procyn === true) || []
  masterData.splamt = data.reduce((acc, cur) => acc + (Number(cur.ioamt) || 0), 0)
  masterData.vatamt = data.reduce((acc, cur) => acc + (Number(cur.iovat) || 0), 0)
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelectedValue = rows.every(r => r.getData().procyn === 'y')
  rows.forEach(r => r.update({ procyn: allSelectedValue ? 'n' : 'y' }))
  updateTotals()
}

// [4] 데이터 페칭
async function fetchCustomerList() {
  try {
    const res = await api.post('/api/hsio/HSIO_510U_STR', {
        actkind: 'S1', cmpycd: authStore.cmpycd, iogbn: '200',
        ioymdfr: searchData.ioymdfr.replace(/-/g, ''), ioymdto: searchData.ioymdto.replace(/-/g, ''),
        deptcd: searchData.deptcd, custcd: searchData.custcd
    })
    custGrid?.setData(res.data); itemGrid?.clearData(); selectedCust.custcd = ''
  } catch (e) { vAlertError('거래처 목록 조회 실패') }
}

async function fetchSettlementItems() {
  try {
    const res = await api.post('/api/hsio/HSIO_510U_STR', {
        actkind: 'S0', cmpycd: authStore.cmpycd, iogbn: '200',
        ioymdfr: searchData.ioymdfr.replace(/-/g, ''), ioymdto: searchData.ioymdto.replace(/-/g, ''),
        deptcd: searchData.deptcd, custcd: selectedCust.custcd
    })
    const data = (res.data || []).map((i: any) => {
      const ioQty = (Number(i.jsanqty) || 0) - (Number(i.jqty) || 0);
      const ioAmt = (Number(i.jsanamt) || 0) - (Number(i.jamt) || 0);
      const ioVat = (Number(i.jsanvat) || 0) - (Number(i.jvat) || 0);
      let price = ioQty !== 0 ? (masterData.pricegbn === '1' ? ioAmt / ioQty : (ioAmt + ioVat) / ioQty) : 0
      return { ...i, procyn: 'y', ioqty: ioQty, jioqty: ioQty, price: Math.round(price), ioamt: ioAmt, iovat: ioVat }
    })
    itemGrid?.setData(data); updateTotals()
  } catch (e) { vAlertError('정산 품목 조회 실패') }
}

/**
 * 🚀 저장 로직 (ASP 패턴: 마스터 채번 -> 상세 루프)
 */
async function saveSettlement() {
  if (!masterData.deptcd || !masterData.pubymd) return vAlertError('부서 및 발행일은 필수입니다.')
  const details = itemGrid?.getData().filter((i: any) => i.procyn === 'y' || i.procyn === true)
  if (!details || details.length === 0) return vAlertError('정산할 품목을 선택하세요.')

  if (!confirm('정산 작업을 진행하시겠습니까?')) return

  try {
    const ioymdfr = searchData.ioymdfr.replace(/-/g, '')
    const ioymdto = searchData.ioymdto.replace(/-/g, '')
    const jsanymd = masterData.pubymd.replace(/-/g, '')

    // 🚀 Step 1. 정산 MASTER 생성 (actkind: 'a0')
    const masterParams = {
        actkind: 'a0', cmpycd: authStore.cmpycd, iogbn: '200',
        ioymdfr: ioymdfr, ioymdto: ioymdto,
        custcd: selectedCust.custcd, deptcd: masterData.deptcd,
        ioym: '', iono: '', iorowno: '',
        taxunit: masterData.taxunit, vattype: masterData.vattype,
        jsanym: '', jsanno: '', jsanymd: jsanymd,
        jsanqty: 0,
        jsanamt: String(masterData.splamt || '0').replace(/,/g, ''),
        jsanvat: String(masterData.vatamt || '0').replace(/,/g, ''),
        updemp: authStore.userid
    }

    const mRes = await api.post('/api/hsio/HSIO_510U_STR', masterParams)
    const mstData = mRes.data?.[0]

    // ASP 에러 체크: '000000'이면 에러
    if (mstData && (mstData.jsanym === '000000' || mstData.JSANYM === '000000')) {
      throw new Error(mstData.jsanno || mstData.JSANNO || '마스터 정산 오류')
    }

    const keyJsanym = mstData?.jsanym || mstData?.JSANYM
    const keyJsanno = mstData?.jsanno || mstData?.JSANNO

    // 🚀 Step 2. 상세 내역 루프 저장 (actkind: 'u0')
    for (const item of details) {
      const detailParams = {
        actkind: 'u0', cmpycd: authStore.cmpycd, iogbn: '200',
        ioymdfr: ioymdfr, ioymdto: ioymdto,
        custcd: selectedCust.custcd, deptcd: masterData.deptcd,
        ioym: item.ioym, iono: item.iono, iorowno: item.iorowno,
        taxunit: masterData.taxunit, vattype: masterData.vattype,
        jsanym: keyJsanym, jsanno: keyJsanno, jsanymd: jsanymd,
        jsanqty: item.jioqty,
        jsanamt: String(item.ioamt || '0').replace(/,/g, ''),
        jsanvat: String(item.iovat || '0').replace(/,/g, ''),
        updemp: authStore.userid
      }
      const dRes = await api.post('/api/hsio/HSIO_510U_STR', detailParams)
      if (dRes.data?.[0]?.jsanym === '000000' || dRes.data?.[0]?.JSANYM === '000000') {
        throw new Error(dRes.data[0].jsanno || dRes.data[0].JSANNO || '상세 정산 오류')
      }
    }

    vAlert('정상적으로 작업이 되었습니다.')
    fetchCustomerList(); initialize();
  } catch (e: any) { vAlertError(e.message || '저장 실패') }
}

function handleOpenHelp(type: string) {
  if (type === 'SEARCH_DEPT') openHelp('DEPT', (d) => { searchData.deptcd = d.deptcd; searchData.deptnm = d.deptnm });
  else if (type === 'MASTER_DEPT') openHelp('DEPT', (d) => { masterData.deptcd = d.deptcd; masterData.deptnm = d.deptnm });
  else if (type === 'SEARCH_CUST') openHelp('CUST', (d) => { searchData.custcd = d.custcd; searchData.custnm = d.custnm });
}

function initialize() {
  resetForm(searchData); Object.assign(searchData, { deptcd: authStore.deptcd, deptnm: authStore.deptnm, ioymdfr: initfrymd, ioymdto: initymd });
  Object.assign(masterData, { deptcd: authStore.deptcd, deptnm: authStore.deptnm, vattype: '010', pubymd: initymd, splamt: 0, vatamt: 0 });
  custGrid?.clearData(); itemGrid?.clearData(); selectedCust.custcd = ''
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)
const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(0,4)}-${val.substring(4,6)}-${val.substring(6,8)}` : val;

onMounted(async () => {
  api.post('/api/comm/HP00_000S_STR', { gubun: 'cl', cmpycd: authStore.cmpycd }).then(r => { if (r.data?.length) masterData.clsymd = r.data[0].clsymd });
  api.post('/api/comm/HA00_010S_STR', { cmpycd: authStore.cmpycd, gbn: 'p1' }).then(r => { if (r.data?.length) masterData.pricegbn = r.data[0].pricegbn || '1' });
  api.post('/api/comm/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd }).then(r => { taxUnitOptions.value = (r.data || []).map((i:any)=>({taxunit:String(i.taxunit||i.code).trim(), unitnm:String(i.unitnm||i.cdnm).trim()})); if(taxUnitOptions.value.length) masterData.taxunit = taxUnitOptions.value[0].taxunit; });
  api.post('/api/comm/HA00_00P_STR', { gubun: 'E0', gbncd: '120', cmpycd: authStore.cmpycd }).then(r => vatTypeOptions.value = (r.data || []).map((i:any)=>({codecd:String(i.codecd||i.code).trim(), codenm:String(i.codenm||i.cdnm).trim()})));
  nextTick(() => initGrids())
})
</script>

<style scoped>
.erp-table-dense th, .erp-table-dense td {
  height: 32px !important; padding: 0 8px !important; font-size: 12px; vertical-align: middle; border: 1px solid #dee2e6;
}
.erp-table-dense .form-control, .erp-table-dense .form-select, .erp-table-dense .btn {
  height: 26px !important; font-size: 12px !important; border-radius: 2px;
}
.erp-table-dense th { font-weight: 600; color: #495057; }
.grid-container-right { border-bottom: 3px solid #005a9f !important; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
