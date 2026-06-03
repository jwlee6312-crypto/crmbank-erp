<!--
	=============================================================
	프로그램명	: 통관입고입력 [디자인 표준 통합]
	작성일자	: 2025.02.24
	설명        : HSOD100U 디자인 패턴 이식 및 하단 공백/짤림 해결
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-in-down me-2 text-primary" style="font-size: 18px;"></i>
        수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">통관입고입력 (HSIP120U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchPoList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-print" @click="print">인쇄</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 🔍 [상단] 조회 필터 영역 (HSOD100U 디자인 패턴) -->
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
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('S_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">선적일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchForm.inymdFR" v-model:todt="searchForm.inymdTO" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📊 3. 메인 작업 영역 -->
      <div class="d-flex flex-row flex-grow-1 overflow-hidden gap-2" style="min-height: 0;">
        <!-- 3-1. 좌측: PO 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 280px; min-width: 280px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">통관 대상 PO</div>
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
                    <th class="bg-light text-center">PO No.</th>
                    <td><input v-model="formData.fileno" class="form-control bg-light fw-bold text-primary" readonly /></td>
                    <th class="bg-light text-center">선적차수</th>
                    <td><input v-model="formData.shipseqnm" class="form-control bg-light text-center" readonly /></td>
                    <th class="required bg-light text-center">통관차수</th>
                    <td>
                      <select v-model="formData.passseq" class="form-select" @change="fetchDetail">
                        <option value="10">1차 통관</option><option value="20">2차 통관</option>
                        <option value="30">3차 통관</option><option value="40">4차 통관</option>
                        <option value="50">5차 통관</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center">통관일자</th>
                    <td><input v-model="formData.passymd" type="date" class="form-control" /></td>
                    <th class="required bg-light text-center">세 관 명</th>
                    <td>
                      <select v-model="formData.taxorg" class="form-select">
                        <option value="">-- 선택 --</option>
                        <option v-for="opt in taxOrgOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center">면장번호</th>
                    <td><input v-model="formData.passno" class="form-control" /></td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center">입고창고</th>
                    <td>
                      <select v-model="formData.whcd" class="form-select">
                        <option value="">-- 선택 --</option>
                        <option v-for="opt in whOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center">입고부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="formData.ideptcd" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                        <input v-model="formData.ideptnm" class="form-control" placeholder="부서 선택" />
                        <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light text-center">적용환율</th>
                    <td><input v-model="formData.frgnrate" type="number" step="0.01" class="form-control text-end fw-bold text-primary" /></td>
                  </tr>
                  <tr>
                    <th class="bg-light text-center">거 래 처</th>
                    <td><input v-model="formData.custnm" class="form-control bg-light" readonly /></td>
                    <th class="bg-light text-center">선 적 일</th>
                    <td><input v-model="formData.shipymd" class="form-control bg-light text-center" readonly /></td>
                    <th class="bg-light text-center">도 착 일</th>
                    <td><input v-model="formData.arvymd" class="form-control bg-light text-center" readonly /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>통관 품목 명세</span>
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
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp: openCommonHelp } = useCommonHelp()

const searchForm = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  inymdFR: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  inymdTO: new Date().toISOString().substring(0, 10)
})

const formData = reactive<any>({
  fileno: '', shipseq: '', shipseqnm: '', passseq: '10',
  passymd: new Date().toISOString().substring(0, 10),
  taxorg: '', passno: '', whcd: '', bigo: '',
  ideptcd: authStore.deptcd, ideptnm: authStore.deptnm,
  custcd: '', custnm: '', jsanyn: 'N',
  shipymd: '', arvymd: '', currnm: '', shipportnm: '', arvportnm: '', frgnrate: 0
})

const taxOrgOptions = ref<any[]>([]); const whOptions = ref<any[]>([])
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null

const fetchPoList = async () => {
  try {
    const res = await api.post('/api/hsip/HSIP_120U_STR', { ...searchForm, actkind: 'S1', cmpycd: authStore.cmpycd })
    poGrid?.setData(res.data || [])
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('PO 조회 실패') }
}

const fetchDetail = async () => {
  if (!formData.fileno || !formData.shipseq) return
  try {
    const res = await api.post('/api/hsip/HSIP_120U_STR', {
      fileno: formData.fileno, shipseq: formData.shipseq, passseq: formData.passseq, actkind: 'S0', cmpycd: authStore.cmpycd
    })
    if (res.data && res.data[0]) {
      Object.assign(formData, res.data[0])
      formData.shipseqnm = `${formData.shipseq.substring(0,1)}차 선적`
      const resItems = await api.post('/api/hsip/HSIP_121U_STR', {
        fileno: formData.fileno, shipseq: formData.shipseq, passseq: formData.passseq, actkind: 'S0', cmpycd: authStore.cmpycd
      })
      itemGrid?.setData(resItems.data || [])
    }
  } catch (e) { vAlertError('상세 조회 실패') }
}

const save = async () => {
  if (formData.jsanyn === 'Y') return vAlertError('정산된 자료입니다. 수정할 수 없습니다.')
  const items = itemGrid?.getData().filter((r: any) => r.procyn === 'Y')
  if (!items || items.length === 0) return vAlertError('통관 처리할 품목을 선택하세요.')
  if (!confirm('통관입고 작업을 하시겠습니까?')) return
  try {
    await api.post('/api/hsip/HSIP_120U_STR', { ...formData, actkind: 'A0', cmpycd: authStore.cmpycd, items: items, updemp: authStore.userid })
    vAlert('저장되었습니다.'); fetchDetail()
  } catch (e) { vAlertError('저장 실패') }
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().procyn === 'Y')
  rows.forEach(r => r.update({ procyn: allSelected ? 'N' : 'Y' }))
}

function initialize() {
  resetForm(formData);
  Object.assign(formData, { passseq: '10', passymd: new Date().toISOString().substring(0, 10), ideptcd: authStore.deptcd, ideptnm: authStore.deptnm });
  itemGrid?.clearData(); poGrid?.deselectRow();
}

function openHelp(type: string) {
  if (type === 'S_DEPT' || type === 'DEPT') {
    openCommonHelp('DEPT', (d) => {
      if (type === 'S_DEPT') { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm }
      else { formData.ideptcd = d.deptcd; formData.ideptnm = d.deptnm }
    })
  }
}

const loadCombos = async () => {
  const fetchCombo = async (gbn: string, gbncd: string = '') => {
    try {
      const res = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: gbn, cmpycd: authStore.cmpycd, gbncd: gbncd } })
      return res.data.map((i: any) => ({ codecd: String(i.codecd || i.CODE || '').trim(), codenm: String(i.codenm || i.cdnm || '').trim() }))
    } catch (e) { return [] }
  }
  taxOrgOptions.value = await fetchCombo('E2', '306')
  whOptions.value = await fetchCombo('W0', '')
  if (whOptions.value.length > 0) formData.whcd = whOptions.value[0].codecd
}

onMounted(() => {
  loadCombos()
  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: "100%", selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: 'PO No.', field: 'fileno', cssClass: 'fw-bold text-primary', minWidth: 150 },
        { title: '선적', field: 'shipseq', width: 80, formatter: (c) => `${c.getValue().substring(0,1)}차` }
      ]
    })
    poGrid.on('rowClick', (e, row) => {
      const d = row.getData(); formData.fileno = d.fileno; formData.shipseq = d.shipseq; fetchDetail()
    })
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        { title: '선택', field: 'procyn', hozAlign: 'center', width: 50, formatter: 'tickCross', editor: true, headerClick: () => toggleAllRows() },
        { title: '품목명', field: 'itemnm', minWidth: 180, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
        { title: '규격', field: 'itsize', width: 120, hozAlign: 'left' },
        { title: '단위', field: 'unit', width: 60 },
        { title: '선적량', field: 'sqty', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '단가', field: 'price', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 2 } },
        { title: '잔량', field: 'jqty', hozAlign: 'right', width: 90, cssClass: 'text-danger fw-bold' },
        { title: '통관량', field: 'iqty', hozAlign: 'right', width: 90, editor: 'number', cssClass: 'bg-yellow fw-bold', formatter: 'money' },
        { title: '감모량', field: 'gqty', hozAlign: 'right', width: 90, editor: 'number', cssClass: 'bg-yellow fw-bold', formatter: 'money' },
        { title: '외화금액', field: 'amt', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 2 } }
      ]
    });
  }
  fetchPoList();
})
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
