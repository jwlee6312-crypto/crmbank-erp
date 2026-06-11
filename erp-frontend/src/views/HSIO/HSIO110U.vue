<!--
	=============================================================
	프로그램명	: 매입정산 (HSIO110U)
	작성일자	: 2025.02.24
	설명        : 입고 내역을 기반으로 매입 정산 처리 (ASP 패턴 기반 순차 저장 로직 적용 및 소문자 통일)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매입정산 (HSIO110U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchCustList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 🔍 2. 최상단 검색 필터 영역 -->
    <div class="p-2 pb-0 flex-shrink-0 bg-light">
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-2 bg-white">
          <div class="d-flex align-items-center gap-4">
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-dark" style="min-width: 60px;">입고부서</span>
              <div class="input-group input-group-sm" style="width: 250px;">
                <input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                <input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" />
                <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT_search')"><i class="bi bi-search"></i></button>
              </div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-dark" style="min-width: 60px;">입고일자</span>
              <div class="d-flex align-items-center gap-1">
                <DateForm v-model:fromdt="searchForm.ioymdfr" v-model:todt="searchForm.ioymdto" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 3. 메인 작업 영역 -->
    <div class="d-flex flex-row flex-grow-1 overflow-hidden p-2 gap-2 bg-light">
      <!-- 3-1. 좌측: 매입 거래처 목록 -->
      <div class="card border shadow-sm d-flex flex-column" style="width: 280px; min-width: 280px;">
        <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">매입 거래처</div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="poGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

      <!-- 3-2. 우측: 상세 정보 및 품목 그리드 -->
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
                    <div class="input-group input-group-sm">
                      <input v-model="formData.custcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
                      <input v-model="formData.custnm" type="text" class="form-control bg-light" readonly />
                    </div>
                  </td>
                  <th class="bg-light text-center">입고부서</th>
                  <td>
                    <div class="input-group input-group-sm">
                      <input v-model="formData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                      <input v-model="formData.deptnm" type="text" class="form-control bg-light" readonly />
                    </div>
                  </td>
                  <th class="required bg-light text-center">사업장/유형</th>
                  <td>
                    <div class="d-flex gap-1">
                      <select v-model="formData.taxunit" class="form-select">
                        <option v-for="opt in saOptions" :key="opt.taxunit" :value="opt.taxunit">{{ opt.unitnm }}</option>
                      </select>
                      <select v-model="formData.vattype" class="form-select">
                        <option v-for="opt in vatOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                      </select>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th class="required bg-light text-center">발&nbsp;행&nbsp;일</th>
                  <td><input v-model="formData.pubymd" type="date" class="form-control" /></td>
                  <th class="bg-light text-center">공급가액</th>
                  <td><input :value="formatNumber(totals.amt)" class="form-control text-end bg-light fw-bold" readonly /></td>
                  <th class="bg-light text-center">총&nbsp;&nbsp;합&nbsp;&nbsp;계</th>
                  <td><input :value="formatNumber(totals.sum)" class="form-control text-end bg-warning-subtle fw-bold text-dark" readonly /></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
            <span class="fw-bold small text-dark d-flex align-items-center">
              <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 정산 대상 입고 명세
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

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
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

const now = new Date()
const searchForm = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  ioymdfr: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  ioymdto: now.toISOString().substring(0, 10)
})

const formData = reactive<any>({
  actkind: 'S0', cmpycd: authStore.cmpycd, custcd: '', custnm: '', deptcd: '', deptnm: '',
  taxunit: '', vattype: '010', pubymd: now.toISOString().substring(0, 10)
})

const saOptions = ref<any[]>([]); const vatOptions = ref<any[]>([]);
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null

const totals = computed(() => {
  const items = itemGrid?.getData().filter((r: any) => r.procyn === 'Y' || r.procyn === 'Y') || []
  const amt = items.reduce((acc, cur: any) => acc + (Number(cur.jsanamt || cur.ioamt) || 0), 0)
  const vat = items.reduce((acc, cur: any) => acc + (Number(cur.jsanvat || cur.iovat) || 0), 0)
  return { amt, vat, sum: amt + vat }
})

async function fetchOptions() {
  try {
    const [resSa, resVat] = await Promise.all([
      api.post('/api/ha00/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd }),
      api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '120' })
    ])
    saOptions.value = resSa.data; vatOptions.value = resVat.data;
    if (saOptions.value.length > 0) formData.taxunit = saOptions.value[0].taxunit;
  } catch (e) {}
}

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_110U_STR', {
      actkind: 'S1', cmpycd: authStore.cmpycd, iogbn: '100',
      ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
      ioymdto: searchForm.ioymdto.replace(/-/g, ''),
      deptcd: searchForm.deptcd
    });
    poGrid?.setData(res.data || []); itemGrid?.clearData(); vAlert('조회되었습니다.')
  } catch (e) { vAlertError('목록 조회 실패') }
}

async function fetchDetail(row: any) {
  const d = row.getData();
  formData.custcd = d.custcd; formData.custnm = d.custnm; formData.deptcd = d.deptcd; formData.deptnm = d.deptnm;
  try {
    const res = await api.post('/api/hsio/HSIO_110U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, iogbn: '100',
      custcd: d.custcd, deptcd: searchForm.deptcd,
      ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
      ioymdto: searchForm.ioymdto.replace(/-/g, '')
    })
    itemGrid?.setData(res.data || [])
  } catch (e) { vAlertError('상세 조회 실패') }
}

/**
 * 🚀 저장 로직 (ASP 패턴: 마스터 채번 -> 상세 루프)
 */
async function save() {
  const items = itemGrid?.getData().filter((r: any) => r.procyn === 'Y' || r.procyn === 'Y') || []
  if (!items.length) return vAlertError('정산 처리할 품목을 선택하세요.')
  if (!formData.taxunit) return vAlertError('사업장을 선택하세요.')

  if (!confirm('정산 작업을 하시겠습니까?')) return

  try {
    // 🚀 Step 1. 정산 MASTER 생성 (actkind: 'A0')
    const masterParams = {
      actkind: 'A0',
      cmpycd: authStore.cmpycd,
      iogbn: '100',
      ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
      ioymdto: searchForm.ioymdto.replace(/-/g, ''),
      custcd: formData.custcd,
      deptcd: formData.deptcd,
      ioym: '', iono: '', iorowno: '',
      taxunit: formData.taxunit,
      vattype: formData.vattype,
      jsanym: '', jsanno: '',
      jsanymd: formData.pubymd.replace(/-/g, ''),
      jsanamt: totals.value.amt,
      jsanvat: totals.value.vat,
      updemp: authStore.userid
    }

    const resMst = await api.post('/api/hsio/HSIO_110U_STR', masterParams)
    const mstData = resMst.data?.[0]

    const keyJsanym = mstData?.jsanym;
    const keyJsanno = mstData?.jsanno;

    if (!keyJsanym || keyJsanym === '000000') {
        throw new Error(keyJsanno || '마스터 정산 처리 중 오류가 발생했습니다.')
    }

    // 🚀 Step 2. 상세 내역 루프 저장 (actkind: 'U0')
    for (const item of items) {
      const detailParams = {
        actkind: 'U0',
        cmpycd: authStore.cmpycd,
        iogbn: '100',
        ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
        ioymdto: searchForm.ioymdto.replace(/-/g, ''),
        custcd: formData.custcd,
        deptcd: formData.deptcd,
        ioym: item.ioym,
        iono: item.iono,
        iorowno: item.iorowno,
        taxunit: formData.taxunit,
        vattype: formData.vattype,
        jsanym: keyJsanym,
        jsanno: keyJsanno,
        jsanymd: formData.pubymd.replace(/-/g, ''),
        jsanamt: String(item.jsanamt || item.ioamt || '0').replace(/,/g, ''),
        jsanvat: String(item.jsanvat || item.iovat || '0').replace(/,/g, ''),
        updemp: authStore.userid
      }

      const resDtl = await api.post('/api/hsio/HSIO_110U_STR', detailParams)
      if (resDtl.data?.[0]?.jsanym === '000000') {
         throw new Error(resDtl.data[0].jsanno || '상세 정산 처리 중 오류 발생')
      }
    }

    vAlert('정상으로 정산되었습니다.')
    fetchCustList(); initialize();
  } catch (e: any) { vAlertError(e.message || '정산 실패') }
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().procyn === 'Y' || r.getData().procyn === 'Y')
  rows.forEach(r => r.update({ procyn: allSelected ? 'N' : 'Y' }))
}

function initialize() {
  resetForm(formData); poGrid?.clearData(); itemGrid?.clearData();
  Object.assign(formData, { cmpycd: authStore.cmpycd, pubymd: now.toISOString().substring(0, 10), vattype: '010' });
}

function openHelp(type: string) {
  if (type === 'DEPT_search') openCommonHelp('DEPT', (d) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm });
}

onMounted(async () => {
  await fetchOptions();
  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [{ title: '매입 거래처', field: 'custnm', cssClass: 'fw-bold text-dark', hozAlign: 'left' }]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row))
  }
  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 80 },
      columns: [
        { title: '선택', field: 'procyn', hozAlign: 'center', width: 60, formatter: 'tickCross', editor: true },
        { title: '입고일', field: 'ioymd', width: 110, formatter: (c) => formatDate(c.getValue()) },
        { title: '품목명', field: 'itemnm', minWidth: 180, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
        { title: '규격', field: 'itsize', width: 120, hozAlign: 'left' },
        { title: '단위', field: 'unit', width: 60 },
        { title: '수량', field: 'ioqty', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '공급가', field: 'ioamt', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '부가세', field: 'iovat', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } }
      ]
    })
  }
  fetchCustList()
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(0,4)}-${val.substring(4,6)}-${val.substring(6,8)}` : val;
</script>

<style scoped>
.main-content-wrapper { padding-bottom: 0px !important; }
.grid-container-right { border-bottom: 3px solid #005a9f !important; }
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
