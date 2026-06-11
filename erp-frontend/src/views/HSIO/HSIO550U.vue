<!--
	=============================================================
	프로그램명	: 주문출고처리 (HSIO550U)
	작성일자	: 2025.02.24
	설명        : 발주 내역을 기반으로 출고 처리 (창고 로직 표준화 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-truck me-2 text-primary" style="font-size: 16px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">주문출고처리 (HSIO550U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchCustList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 -->
    <div class="search-bar bg-white border-bottom p-2 px-3 d-flex align-items-center flex-shrink-0 gap-4 shadow-sm">
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold small text-secondary">출고창고:</span>
        <select v-model="searchParam.whcd" class="form-select form-select-sm" style="width: 120px;">
          <option value="">전체</option>
          <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
        </select>
      </div>
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold small text-secondary">▶ 납품일자:</span>
        <div class="d-flex align-items-center gap-1 flex-nowrap">
          <DateForm v-model:fromdt="searchParam.frymd" v-model:todt="searchParam.toymd" />
        </div>
      </div>
    </div>

    <!-- 💡 3. 메인 분할 영역 -->
    <div class="flex-grow-1 d-flex flex-row gap-2 p-2 overflow-hidden bg-light">

      <!-- 🅰️ 좌측: 거래처 목록 -->
      <div class="card border shadow-sm d-flex flex-column flex-shrink-0" style="width: 320px;">
        <div class="card-header bg-white text-dark fw-bold border-bottom py-1 ps-3 h-auto" style="font-size: 13px;">
          <i class="bi bi-list-ul me-1 text-secondary"></i> 미출고 거래처
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="custGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

      <!-- 🅱️ 우측: 상세 정보 및 주문 내역 -->
      <div class="d-flex flex-column flex-grow-1 gap-2 overflow-hidden">
        <!-- 마스터 정보 카드 -->
        <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
          <div class="card-body p-0 bg-white">
            <table class="erp-table-dense w-100">
              <colgroup>
                <col style="width: 100px;" /><col />
                <col style="width: 100px;" /><col />
                <col style="width: 100px;" /><col />
              </colgroup>
              <tbody>
                <tr>
                  <th class="required bg-light text-center">거 래 처</th>
                  <td>
                    <div class="input-group input-group-sm flex-nowrap">
                      <input v-model="masterData.custcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 65px;" readonly />
                      <input v-model="masterData.custnm" type="text" class="form-control border-start-0" readonly />
                    </div>
                  </td>
                  <th class="required bg-light text-center">출고창고</th>
                  <td>
                    <select v-model="masterData.whcd" class="form-select">
                      <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
                    </select>
                  </td>
                  <th class="required bg-light text-center">출고일자</th>
                  <td><input v-model="masterData.ioymd" type="date" class="form-control" /></td>
                </tr>
                <tr>
                  <th class="bg-light text-center">배 송 처</th>
                  <td><input v-model="masterData.address" class="form-control" placeholder="배송지 정보" /></td>
                  <th class="bg-light text-center">배송방법</th>
                  <td>
                    <div class="input-group input-group-sm flex-nowrap">
                        <input v-model="masterData.trancd" type="text" class="form-control bg-light text-center" style="max-width: 50px;" readonly />
                        <input v-model="masterData.trannm" type="text" class="form-control border-start-0" readonly />
                        <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('TRAN')"><i class="bi bi-search"></i></button>
                    </div>
                  </td>
                  <th class="bg-light text-center">특기사항</th>
                  <td><input v-model="masterData.remark" class="form-control" placeholder="비고" /></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 주문 상세 내역 그리드 -->
        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center flex-shrink-0">
            <span class="fw-bold small text-dark"><i class="bi bi-list-check me-1 text-primary"></i> 주문 상세 내역</span>
            <button class="btn btn-xs btn-outline-secondary px-2" style="height: 24px; font-size: 11px;" @click="toggleAllRows">전체선택</button>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="detailGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
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
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const initymd = now.toISOString().substring(0, 10)

// [1] 데이터 모델링
const searchParam = reactive({ whcd: '', frymd: firstDay, toymd: initymd })
const masterData = reactive<any>({
  cmpycd: authStore.cmpycd, ioymd: initymd, whcd: '', custcd: '', custnm: '',
  address: '', trancd: '', trannm: '', remark: '', trnemp: '', area: ''
})

const custGridRef = ref<HTMLElement | null>(null); let custGrid: Tabulator | null = null;
const detailGridRef = ref<HTMLElement | null>(null); let detailGrid: Tabulator | null = null;
const whOptions = ref<any[]>([])

// [2] 그리드 초기화
const initGrids = () => {
  if (custGridRef.value) {
    custGrid = new Tabulator(custGridRef.value, {
      layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
      columns: [{ title: "미출고 거래처명", field: "custnm", widthGrow: 1, cssClass: "fw-bold text-primary" }]
    });
    custGrid.on("rowClick", (e, row) => fetchDetails(row.getData()));
  }

  if (detailGridRef.value) {
    detailGrid = new Tabulator(detailGridRef.value, {
      layout: "fitColumns", height: "100%", placeholder: "주문 내역이 없습니다.",
      columnCalcs: "table",
      columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
      columns: [
        { title: "선택", field: "procyn", hozAlign: "center", width: 50, formatter: "tickCross", editor: true },
        { title: "주문부서", field: "deptnm", width: 140, hozAlign: "left" },
        { title: "주문일", field: "ordymd", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
        { title: "납품일", field: "outymd", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
        { title: "품 목", field: "itemnm", minWidth: 150, widthGrow: 1, cssClass: 'fw-bold', hozAlign: "left" },
        { title: "규격", field: "itsize", width: 120, hozAlign: "left" },
        { title: "미출고", field: "janqty", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
        { title: "출고수량", field: "ioqty", width: 90, hozAlign: "right", editor: "number", cssClass: "bg-yellow fw-bold", bottomCalc: "sum" }
      ]
    });
  }
}

// [3] 로직 처리
const fetchWhOptions = async () => {
  try {
    const resWh = await api.get('/api/hs00/HS00_000S_STR', {
      params: { gubun: 'W0', cmpycd: authStore.cmpycd }
    })
    whOptions.value = (resWh.data || []).map((i: any) => ({
      whcd: String(i.whcd || '').trim(),
      whnm: String(i.whnm || '').trim()
    }))

    if (whOptions.value.length > 0) {
      masterData.whcd = whOptions.value[0].whcd
    }
  } catch (e) {
    whOptions.value = []
  }
}

const fetchCustList = async () => {
  try {
    const res = await api.post('/api/hsio/HSIO_550U_STR', {
        actkind: 'S0', cmpycd: authStore.cmpycd, iogbn: '200',
        ioymdfr: searchParam.frymd.replace(/-/g, ''), ioymdto: searchParam.toymd.replace(/-/g, '')
    });
    custGrid?.setData(res.data || []);
  } catch (e) { vAlertError('조회 실패') }
}

const fetchDetails = async (row: any) => {
  masterData.custcd = row.custcd; masterData.custnm = row.custnm;
  try {
    const res = await api.post('/api/hsio/HSIO_550U_STR', {
        actkind: 'S1', cmpycd: authStore.cmpycd, custcd: row.custcd, whcd: searchParam.whcd,
        iogbn: '200', ioymdfr: searchParam.frymd.replace(/-/g, ''), ioymdto: searchParam.toymd.replace(/-/g, '')
    });
    const detailData = (res.data || []).map((i: any) => ({ ...i, ioqty: i.janqty, procyn: 'Y' }));
    detailGrid?.setData(detailData);
  } catch (e) { vAlertError('상세 조회 실패') }
}

const save = async () => {
  const items = detailGrid?.getData().filter((r: any) => r.procyn === 'Y' && Number(r.ioqty) > 0) || [];
  if (!items.length) return vAlertError('출고 수량을 입력하세요.');
  if (!masterData.whcd) return vAlertError('출고 창고를 선택하세요.');

  const ioymd = masterData.ioymd.replace(/-/g, '');

  try {
    const resCls = await api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'cl', cmpycd: authStore.cmpycd } });
    if (resCls.data?.length > 0) {
        const sclsym = resCls.data[0].sclsym;
        if (sclsym && ioymd.substring(0, 6) <= sclsym) {
            throw new Error(`영업정보가 마감되었습니다. (${sclsym})`);
        }
    }

    if (!confirm('출고 작업을 진행하시겠습니까?')) return

    const detailItems = items.map((item: any) => {
        const ordQty = Number(item.ordqty) || 1;
        const outQty = Number(item.ioqty) || 0;
        const outAmt = Math.round((Number(item.ordamt) / ordQty) * outQty);
        const outVat = Math.round((Number(item.ordvat) / ordQty) * outQty);
        return { ...item, outqty: outQty, outamt: outAmt, outvat: outVat };
    });

    const totalAmtSum = detailItems.reduce((acc, cur) => acc + cur.outamt, 0);

    const masterParams = {
        actkind: 'A0', cmpycd: authStore.cmpycd, iogbn: '200',
        ioymdfr: searchParam.frymd.replace(/-/g, ''), ioymdto: searchParam.toymd.replace(/-/g, ''),
        custcd: masterData.custcd, deptcd: authStore.deptcd, ioym: '', iono: '', ioymd: ioymd,
        iotype: '100', whcd: masterData.whcd, area: masterData.area || '', userid: authStore.userid,
        trnemp: masterData.trnemp || '', trancd: masterData.trancd || '', addres: masterData.address || '',
        remark: masterData.remark || '', cfmyn: 'Y', amtsum: totalAmtSum, updemp: authStore.userid
    }
    const resMst = await api.post('/api/hsio/HSIO_550U_STR', masterParams);
    const mstData = resMst.data?.[0];

    if (mstData && (mstData.ioym === '000000')) {
        throw new Error(mstData.iono || '마스터 저장 오류');
    }

    const keyioym = mstData?.ioym; const keyiono = mstData?.iono;

    for (const item of detailItems) {
        const detailParams = {
            actkind: 'A0', cmpycd: authStore.cmpycd, iogbn: '200', ioym: keyioym, iono: keyiono,
            iorowno: '', deptcd: item.deptcd || authStore.deptcd, custcd: masterData.custcd,
            whcd: masterData.whcd, area: masterData.area || '', userid: item.ordemp || authStore.userid,
            ioymd: ioymd, iotype: '100', itemcd: item.itemcd, itsize: item.itsize, unit: item.unit,
            ioqty: item.outqty, ioamt: item.outamt, iovat: item.outvat, balym: item.ordym, balno: item.ordno,
            browno: item.orowno, cfmyn: 'Y', updemp: authStore.userid
        }
        const resDtl = await api.post('/api/hsio/HSIO_551U_STR', detailParams);
        if (resDtl.data?.[0]?.ioym === '000000') {
            throw new Error(resDtl.data[0].iono || '상세 저장 오류');
        }
    }

    vAlert('정상적으로 처리되었습니다.');
    fetchCustList(); initialize();
  } catch (e: any) { vAlertError(e.message || '저장 실패'); }
}

const toggleAllRows = () => {
  const rows = detailGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().procyn === 'Y')
  rows.forEach(r => r.update({ procyn: allSelected ? 'N' : 'Y' }))
}

function initialize() {
  resetForm(masterData); masterData.ioymd = initymd;
  if (whOptions.value.length > 0) masterData.whcd = whOptions.value[0].whcd;
  custGrid?.clearData(); detailGrid?.clearData();
}

function handleOpenHelp(type: string) {
    if (type === 'TRAN') openHelp('CUST', (d) => { masterData.trancd = d.custcd; masterData.trannm = d.custnm });
    else if (type === 'DEPT_search') openHelp('DEPT', (d) => { searchParam.deptcd = d.deptcd; searchParam.deptnm = d.deptnm });
}

const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(0,4)}-${val.substring(4,6)}-${val.substring(6,8)}` : val;

onMounted(async () => {
  await fetchWhOptions();
  nextTick(() => { initGrids(); fetchCustList(); });
})
</script>

<style scoped>
.main-content-wrapper { padding-bottom: 0px !important; }
.grid-container-left, .grid-container-right { border-bottom: 3px solid #005a9f !important; }
.erp-table-dense th, .erp-table-dense td {
  height: 32px !important; padding: 0 8px !important; font-size: 12px; vertical-align: middle; border: 1px solid #dee2e6;
}
.erp-table-dense .form-control, .erp-table-dense .form-select, .erp-table-dense .btn {
  height: 26px !important; font-size: 12px !important; border-radius: 2px;
}
.erp-table-dense th { font-weight: 600; color: #495057; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.empty-row-style) { opacity: 0.6; }
</style>
