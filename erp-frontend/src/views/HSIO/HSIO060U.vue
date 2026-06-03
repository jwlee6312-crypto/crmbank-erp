<!--
	=============================================================
	프로그램명	: 입고처리 (HSIO060U)
	작성일자	: 2025.02.24
	설명        : 발주 내역을 기반으로 입고 처리 (ASP 패턴 기반 순차 저장 로직 적용 및 소문자 통일)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        입고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">입고처리 (HSIO060U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchCustList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 🔍 [상단] 조회 필터 영역 (HSOD100U 디자인 패턴 적용) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" />
              <col style="width: 40%" />
              <col style="width: 10%" />
              <col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">입고부서</th>
                <td>
                  <div class="input-group input-group-sm w-75">
                    <input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT_search')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">발주일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchForm.ymdfr" v-model:todt="searchForm.ymdto" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📊 3. 메인 작업 영역 -->
      <div class="d-flex flex-row flex-grow-1 overflow-hidden gap-2" style="min-height: 0;">
        <!-- ⬅️ 좌측: 발주 거래처 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 280px; min-width: 280px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">발주 거래처</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="poGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ➡️ 우측: 상세 정보 및 품목 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
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
                    <th class="bg-light text-center">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                    <td>
                      <div class="d-flex gap-1">
                        <input v-model="formData.custcd" class="form-control bg-light fw-bold" style="max-width: 80px;" readonly />
                        <input v-model="formData.custnm" class="form-control bg-light" readonly />
                      </div>
                    </td>
                    <th class="required bg-light text-center">입고창고</th>
                    <td>
                      <select v-model="formData.whcd" class="form-select">
                        <option v-for="opt in whOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center">입고일자</th>
                    <td><input v-model="formData.ioymd" type="date" class="form-control" /></td>
                  </tr>
                  <tr>
                    <th class="bg-light text-center">비고</th>
                    <td colspan="5"><input v-model="formData.remark" class="form-control" placeholder="특기사항 입력" /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
              <span class="fw-bold small text-dark d-flex align-items-center">
                <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 입고 대상 품목 명세
              </span>
              <button class="btn btn-xs btn-outline-secondary px-2" style="height: 24px; font-size: 11px;" @click="toggleAllRows">전체선택</button>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="itemGridRef" class="tabulator-instance flex-grow-1"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp: openCommonHelp } = useCommonHelp()

const searchForm = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  ymdfr: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  ymdto: new Date().toISOString().substring(0, 10)
})

const formData = reactive<any>({
  custcd: '', custnm: '', whcd: '100',
  ioymd: new Date().toISOString().substring(0, 10),
  remark: ''
})

const whOptions = ref<any[]>([])
const poGridRef = ref<HTMLDivElement | null>(null);
const itemGridRef = ref<HTMLDivElement | null>(null);
let poGrid: Tabulator | null = null;
let itemGrid: Tabulator | null = null

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_060U_STR', {
      ...searchForm, iogbn: '100', actkind: 's1',
      ioymdfr: searchForm.ymdfr.replace(/-/g, ''),
      ioymdto: searchForm.ymdto.replace(/-/g, '')
    })
    poGrid?.setData(res.data || [])
    itemGrid?.clearData();
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('거래처 조회 실패') }
}

async function fetchDetail(cust: any) {
  formData.custcd = cust.custcd;
  formData.custnm = cust.custnm;
  try {
    const res = await api.post('/api/hsio/HSIO_060U_STR', {
      ...searchForm, iogbn: '100', actkind: 's0',
      custcd: cust.custcd,
      ioymdfr: searchForm.ymdfr.replace(/-/g, ''),
      ioymdto: searchForm.ymdto.replace(/-/g, '')
    })
    const detailData = (res.data || []).map((i: any) => ({ ...i, ioqty: i.janqty, procyn: 'y' }))
    itemGrid?.setData(detailData)
  } catch (e) { vAlertError('상세 조회 실패') }
}

/**
 * 🚀 저장 로직 (ASP 루프 패턴 이식 - 소문자 통일)
 */
async function save() {
  const items = itemGrid?.getData().filter((r: any) => r.procyn === 'y')
  if (!items || items.length === 0) return vAlertError('입고 항목을 선택하세요.')
  if (!confirm('입고작업을 하시겠습니까?')) return

  try {
    // 🚀 Step 1. 입고 MASTER 저장 (HSIO_060U_STR)
    const masterParams = {
        actkind: 'a0',
        cmpycd: authStore.cmpycd,
        iogbn: '100',
        ioymdfr: searchForm.ymdfr.replace(/-/g, ''),
        ioymdto: searchForm.ymdto.replace(/-/g, ''),
        deptcd: searchForm.deptcd,
        custcd: formData.custcd,
        ioym: '',
        iono: '',
        ioymd: formData.ioymd.replace(/-/g, ''),
        iotype: '100',
        whcd: formData.whcd,
        addres: '',
        remark: formData.remark || '',
        cfmyn: 'y',
        updemp: authStore.userid
    }
    const mRes = await api.post('/api/hsio/HSIO_060U_STR', masterParams)
    const mstData = mRes.data?.[0]

    // ASP 에러 체크: ioym이 '000000'이면 에러
    if (mstData && mstData.ioym === '000000') {
      throw new Error(mstData.iono || '마스터 저장 오류')
    }

    const keyIoym = mstData?.ioym;
    const keyIono = mstData?.iono;

    // 🚀 Step 2. 입고 상세 내역 루프 저장 (HSIO_061U_STR)
    for (const item of items) {
        // ASP 로직: 단가 비례 금액 계산
        const balQty = Number(item.balqty) || 1
        const inQty = Number(item.ioqty) || 0
        const ioAmt = Math.round((Number(item.balamt) / balQty) * inQty)
        const ioVat = Math.round((Number(item.balvat) / balQty) * inQty)

        const detailParams = {
            ...item,
            actkind: 'a0',
            cmpycd: authStore.cmpycd,
            iogbn: '100',
            ioym: keyIoym,
            iono: keyIono,
            iorowno: '',
            deptcd: item.deptcd || searchForm.deptcd,
            custcd: formData.custcd,
            whcd: formData.whcd,
            ioymd: formData.ioymd.replace(/-/g, ''),
            iotype: '100',
            ioqty: inQty,
            ioamt: ioAmt,
            iovat: ioVat,
            cfmyn: 'y',
            updemp: authStore.userid
        }
        const resDtl = await api.post('/api/hsio/HSIO_061U_STR', detailParams)
        if (resDtl.data?.[0]?.ioym === '000000') {
            throw new Error(resDtl.data[0].iono || '상세 저장 중 오류 발생')
        }
    }
    vAlert('정상적으로 처리되었습니다.');
    fetchCustList();
    initialize();
  } catch (e: any) { vAlertError(e.message || '저장 실패') }
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().procyn === 'y')
  rows.forEach(r => r.update({ procyn: allSelected ? 'n' : 'y' }))
}

function initialize() {
  resetForm(formData);
  formData.ioymd = new Date().toISOString().substring(0, 10);
  formData.whcd = '100';
  poGrid?.clearData(); itemGrid?.clearData();
}

function openHelp(type: string) {
  if (type === 'DEPT_search') {
    openCommonHelp('DEPT', (d) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm });
  }
}

onMounted(async () => {
  api.get('/api/comm/HA00_00P_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
     .then(r => whOptions.value = r.data.map((i:any)=>({code: i.code || i.whcd, cdnm: i.cdnm || i.whnm})));

  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [{ title: '발주 거래처', field: 'custnm', cssClass: 'fw-bold text-dark', hozAlign: 'left' }]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row.getData()))
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnCalcs: "table",
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        { title: '선택', field: 'procyn', hozAlign: 'center', width: 60, formatter: 'tickCross', editor: true, headerClick: () => toggleAllRows() },
        { title: '발주일', field: 'balymd', width: 110, formatter: (c) => formatDate(c.getValue()) },
        { title: '품목명', field: 'itemnm', minWidth: 180, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
        { title: '규격', field: 'itsize', width: 120, hozAlign: 'left' },
        { title: '단위', field: 'unit', width: 60 },
        { title: '미입고', field: 'janqty', hozAlign: 'right', width: 100, cssClass: 'text-danger fw-bold', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '입고량', field: 'ioqty', hozAlign: 'right', width: 100, editor: 'number', cssClass: 'bg-yellow fw-bold', formatter: 'money', formatterParams: { precision: 0 }, bottomCalc: "sum" }
      ]
    })
  }
  fetchCustList()
})

const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(0,4)}-${val.substring(4,6)}-${val.substring(6,8)}` : val;
</script>

<style scoped>
.main-content-wrapper { padding-bottom: 0vh !important; }
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
