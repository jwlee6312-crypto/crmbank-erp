<!--
	=============================================================
	프로그램명	: 품목별 매입거래처 및 단가관리
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] HSIO550U(레이아웃) + HSOD100U(스타일/정렬) 표준 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsba065u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 (표준 버튼 배치 및 색상) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-tags-fill me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">품목별 매입거래처 및 단가관리</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
			</div>
		</div>

		<!-- 🔍 2. 최상단 검색 조건 바 (고밀도 표준 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-light bg-opacity-50">
				<div class="card-body py-2 px-3">
					<div class="d-flex align-items-center gap-4">
						<div class="d-flex align-items-center gap-2">
							<span class="fw-bold small text-dark" style="min-width: 60px;">재고자산</span>
							<select v-model="searchForm.SCH_ASTKIND" class="form-select form-select-sm" style="width: 120px;">
								<option value="000">전체</option>
								<option value="100">원재료</option>
								<option value="112">구매품</option>
								<option value="120">상품</option>
							</select>
						</div>
						<div class="d-flex align-items-center gap-2">
							<span class="fw-bold small text-dark" style="min-width: 60px;">품목명</span>
							<input v-model="searchForm.SCH_ITEMNM" type="text" class="form-control form-control-sm" style="width: 200px;" placeholder="품목코드 또는 품목명" @keyup.enter="fetchList" />
						</div>
						<div class="d-flex align-items-center gap-2">
							<span class="fw-bold small text-dark" style="min-width: 60px;">조회대상</span>
							<select v-model="searchForm.SELGBN" class="form-select form-select-sm" style="width: 100px;">
								<option value="0">전체</option>
								<option value="1">미등록</option>
							</select>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 3. 그리드 영역 (무한 확장) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px; min-height: 40px;">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 품목별 매입 정보 리스트
					</span>
					<button class="btn btn-xs btn-outline-secondary" @click="toggleAllRows">전체선택</button>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="gridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchForm = reactive({
  SCH_ASTKIND: '000',
  SCH_ITEMNM: '',
  SELGBN: '0'
})

const gridRef = ref<HTMLDivElement | null>(null)
let grid: Tabulator | null = null
const activeItemCount = ref(0)

async function fetchList() {
  try {
    const res = await api.post('/api/hsba/HSBA_065U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      SCH_ASTKIND: searchForm.SCH_ASTKIND,
      SCH_ITEMNM: searchForm.SCH_ITEMNM,
      SELGBN: searchForm.SELGBN
    })
    grid?.setData(res.data.data || [])
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

async function handleSave() {
  const selectedData = grid?.getSelectedData()
  if (!selectedData || selectedData.length === 0) return vAlertError('저장할 항목을 선택하세요.')

  if (!confirm('저장하시겠습니까?')) return

  try {
    for (const item of selectedData) {
      await api.post('/api/hsba/HSBA_065U_STR', {
        ...item,
        ACTKIND: 'A0',
        CMPYCD: authStore.CMPYCD,
        USERID: authStore.USERID
      })
    }
    vAlert('저장되었습니다.')
    fetchList()
  } catch (e) { vAlertError('저장 실패') }
}

const toggleAllRows = () => {
  const rows = grid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.isSelected())
  rows.forEach(r => allSelected ? r.deselect() : r.select())
}

function initialize() {
  resetForm(searchForm);
  searchForm.SCH_ASTKIND = '000';
  searchForm.SELGBN = '0';
  grid?.clearData();
}

onMounted(() => {
  if (gridRef.value) {
    grid = new Tabulator(gridRef.value, {
      layout: 'fitColumns',
      height: '100%',
      selectable: true,
      columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 80 },
      columns: [
        { title: '선택', formatter: 'rowSelection', titleFormatter: 'rowSelection', width: 40, hozAlign: 'center', headerHozAlign: 'center' },
        { title: '품목코드', field: 'ITEMCD', width: 100, hozAlign: 'center' },
        { title: '품목명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
        { title: '규격', field: 'ITSIZE', width: 150 },
        { title: '단위', field: 'UNIT', width: 60, hozAlign: 'center' },
        { title: '단가', field: 'PRICE', hozAlign: 'right', width: 100, editor: 'number', formatter: 'money', formatterParams: { precision: 2 } },
        {
          title: '매입거래처', field: 'CUSTNM', minWidth: 180, widthGrow: 1, editor: 'input',
          cellClick: (e, cell) => { if ((e.target as HTMLElement).tagName === 'I') openHelp('CUST', cell.getRow()) },
          formatter: (cell) => `<span>${cell.getValue() || ''}</span> <i class="bi bi-search text-primary float-end"></i>`
        },
        { title: '자동발주', field: 'AUTOYN', hozAlign: 'center', width: 80, formatter: 'tickCross', editor: true },
        { title: '비고', field: 'REMARK', minWidth: 150, widthGrow: 1, editor: 'input' }
      ]
    })
  }
})

const openHelp = (type: string, row?: any) => { /* 팝업 구현부 */ }
const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })
function formatNumber(val: any) { return Number(val || 0).toLocaleString() }
</script>

<style scoped>
.hsip100u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }

.erp-header { background-color: #ffffff !important; }

/* 💎 개별 파일의 스타일을 삭제하여 global.css 표준 디자인이 적용되도록 함 */
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }

erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  width: 1%; white-space: nowrap;
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12.5px; padding: 10px 15px !important; color: #495057;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 10px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
.bg-yellow { background-color: #fffde7 !important; }
</style>
