<!--
	=============================================================
	프로그램명	: 선적통지입력 [디자인 표준 통합]
	작성일자	: 2025.02.24
	설명        : HSOD100U 디자인 패턴 이식 및 레이아웃 최적화
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-ship-fill me-2 text-primary" style="font-size: 18px;"></i>
        수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">선적통지입력 (HSIP110U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchPoList">조회</button>
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
                <th class="text-center bg-light">발주부서</th>
                <td>
                  <div class="input-group input-group-sm w-75">
                    <input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT_search')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">발주일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchForm.inymdfr" v-model:todt="searchForm.inymdto" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📊 3. 메인 분할 작업 영역 -->
      <div class="d-flex flex-row flex-grow-1 overflow-hidden gap-2" style="min-height: 0;">
        <!-- 3-1. 좌측: PO 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 280px; min-width: 280px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">선적 대상 PO</div>
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
                  <col style="width: 100px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="bg-light text-center">PO No.</th>
                    <td><input v-model="formData.fileno" class="form-control bg-light fw-bold text-primary" readonly /></td>
                    <th class="required bg-light text-center">선적차수</th>
                    <td>
                      <select v-model="formData.shipseq" class="form-select" @change="fetchDetail">
                        <option value="10">1차 선적</option><option value="20">2차 선적</option>
                        <option value="30">3차 선적</option><option value="40">4차 선적</option>
                        <option value="50">5차 선적</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center">B/L No.</th>
                    <td><input v-model="formData.blno" class="form-control" /></td>
                    <th class="required bg-light text-center">보관장소</th>
                    <td>
                      <select v-model="formData.storseat" class="form-select">
                        <option v-for="opt in seatOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center">선적일자</th>
                    <td><input v-model="formData.shipymd" type="date" class="form-control" /></td>
                    <th class="required bg-light text-center">도착일자</th>
                    <td><input v-model="formData.arvymd" type="date" class="form-control" /></td>
                    <th class="required bg-light text-center">적용환율</th>
                    <td><input v-model="formData.frgnrate" type="number" step="0.01" class="form-control text-end fw-bold text-primary" @input="calcWonAmt" /></td>
                    <th class="bg-light"></th><td></td>
                  </tr>
                  <tr>
                    <th class="bg-light text-center">거 래 처</th>
                    <td colspan="3">
                      <div class="d-flex gap-1">
                        <input v-model="formData.custcd" class="form-control bg-light" style="max-width: 80px;" readonly />
                        <input v-model="formData.custnm" class="form-control bg-light" readonly />
                      </div>
                    </td>
                    <th class="required bg-light text-center">원 산 지</th>
                    <td><input v-model="formData.nacd" class="form-control bg-light" readonly /></td>
                    <th class="required bg-light text-center">통화구분</th>
                    <td><input v-model="formData.currcd" class="form-control bg-light" readonly /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>선적 품목 명세</span>
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

const searchForm = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  inymdfr: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  inymdto: new Date().toISOString().substring(0, 10)
})

const formData = reactive<any>({
  fileno: '', shipseq: '10', blno: '',
  shipymd: new Date().toISOString().substring(0, 10),
  arvymd: new Date().toISOString().substring(0, 10),
  storseat: '100', frgnrate: 1350.00, custnm: '', custcd: '',
  nacd: '', currcd: '', shipport: '', arvport: '',
  paycond: '', pricond: '', payterm: ''
})

const seatOptions = ref<any[]>([])
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null

async function fetchPoList() {
  try {
    const res = await api.post('/api/hsip/HSIP_110U_STR', { ...searchForm, actkind: 'S1', cmpycd: authStore.cmpycd })
    poGrid?.setData(res.data || [])
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('PO 조회 실패') }
}

async function fetchDetail() {
  if (!formData.fileno) return
  try {
    const res = await api.post('/api/hsip/HSIP_110U_STR', { fileno: formData.fileno, shipseq: formData.shipseq, actkind: 'S0' })
    if (res.data && res.data[0]) {
      Object.assign(formData, res.data[0])
      const resItems = await api.post('/api/hsip/HSIP_111U_STR', { fileno: formData.fileno, shipseq: formData.shipseq, actkind: 'S0' })
      itemGrid?.setData(resItems.data || [])
    }
  } catch (e) { vAlertError('상세 조회 실패') }
}

async function save() {
  const items = itemGrid?.getData().filter((r: any) => r.procyn === 'Y') || []
  if (items.length === 0) return vAlertError('선적 처리할 품목을 선택하세요.')

  if (!confirm('선적정보를 등록 하시겠습니까?')) return

  try {
    // 🚀 1. 마스터 저장 (HSIP_110U_STR 직접 호출)
    // actkind: A0 (신규), U0 (수정)
    const masterActkind = formData.imymd ? 'U0' : 'A0' // 임의의 필드로 신규/수정 판단 (로직에 맞게 조정 가능)

    const masterParams = {
      ...formData,
      actkind: masterActkind,
      cmpycd: authStore.cmpycd,
      updemp: authStore.userid,
      shipymd: formData.shipymd.replace(/-/g, ''),
      arvymd: formData.arvymd.replace(/-/g, '')
    }

    const resMst = await api.post('/api/hsip/HSIP_110U_STR', masterParams)
    const mstData = resMst.data?.[0]

    // 업무 에러 체크 ('000000' 이면 오류)
    if (mstData && mstData.fileno === '000000') {
      throw new Error(mstData.shipseq || '마스터 저장 오류')
    }

    const keyFileno = mstData?.fileno || formData.fileno
    const keyShipseq = mstData?.shipseq || formData.shipseq

    // 🚀 2. 상세 내역 루프 저장 (HSIP_111U_STR 직접 호출)
    for (const item of items) {
      const detailParams = {
        ...item,
        actkind: 'A1', // 상세 저장용 액션코드
        cmpycd: authStore.cmpycd,
        updemp: authStore.userid,
        fileno: keyFileno,
        shipseq: keyShipseq
      }

      const resDtl = await api.post('/api/hsip/HSIP_111U_STR', detailParams)
      const dtlData = resDtl.data?.[0]
      if (dtlData && dtlData.fileno === '000000') {
          throw new Error(dtlData.shipseq || '상세 저장 오류')
      }
    }

    vAlert('성공적으로 저장되었습니다.')
    fetchPoList()
    initialize()

  } catch (e: any) {
    console.error('저장 중 오류 발생:', e)
    vAlertError(e.response?.data?.message || e.message || '저장 처리 중 오류가 발생했습니다.')
  }
}

const calcWonAmt = () => {
  const rows = itemGrid?.getRows()
  rows?.forEach(row => {
    const d = row.getData()
    row.update({ wonamt: Math.floor((Number(d.amt) || 0) * formData.frgnrate) })
  })
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().procyn === 'Y')
  rows.forEach(r => r.update({ procyn: allSelected ? 'N' : 'Y' }))
}

function initialize() {
  resetForm(formData); formData.shipseq = '10'; itemGrid?.clearData(); poGrid?.deselectRow()
}

function openHelp(type: string) {
  if (type === 'DEPT_search') {
    openCommonHelp('DEPT', (d) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm });
  }
}

onMounted(async () => {
  try {

      const resSeat = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '330' } })
      seatOptions.value = resSeat.data.map((i: any) => ({ code: i.codecd || i.code, cdnm: i.codenm || i.cdnm }))
  } catch (e) {}

  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: "100%", selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: 'PO No.', field: 'fileno', cssClass: 'fw-bold text-primary', minWidth: 150 },
        { title: '상태', field: 'shipyn', width: 80, formatter: (c) => c.getValue() === 'Y' ? '완료' : '미완' }
      ]
    })
    poGrid.on('rowClick', (e, row) => {
      formData.fileno = row.getData().fileno; fetchDetail()
    })
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: "100%",
      columnCalcs: "table",
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        {
          title: '선택', field: 'procyn', hozAlign: 'center', width: 50, formatter: 'tickCross', editor: true,
          headerClick: (e, column) => toggleAllRows(), bottomCalc: () => "합계"
        },
        { title: '품목명', field: 'itemnm', minWidth: 180, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
        { title: '규격', field: 'itsize', width: 120, hozAlign: 'left' },
        { title: '단위', field: 'unit', width: 60 },
        { title: '발주량', field: 'pqty', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 }, bottomCalc: "sum" },
        { title: '단가', field: 'price', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 2 } },
        { title: '잔량', field: 'jqty', hozAlign: 'right', width: 90, cssClass: 'text-danger fw-bold', bottomCalc: "sum" },
        { title: '선적량', field: 'sqty', hozAlign: 'right', width: 90, editor: 'number', cssClass: 'bg-yellow fw-bold', bottomCalc: "sum" },
        { title: '외화금액', field: 'amt', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 2 }, bottomCalc: "sum" },
        { title: '원화금액', field: 'wonamt', hozAlign: 'right', width: 120, formatter: 'money', formatterParams: { precision: 0 }, bottomCalc: "sum" }
      ]
    })
  }
  fetchPoList()
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
