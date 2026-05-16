<!--
	=============================================================
	프로그램명	: 선적통지입력 [디자인 원칙 13가지 완벽 준수]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
    설명        : 전체선택 버튼 제거 및 그리드 헤더 클릭 기능 개선 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip110u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-ship-fill me-2 text-primary" style="font-size: 18px;"></i>
				수입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">선적통지입력 (HSIP110U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchPoList">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="handleSave">
					<i class="bi bi-save"></i> 저장
				</button>
			</div>
		</div>

		<!-- 🔍 2. 최상단 검색 조건 -->
		<div class="p-2 pb-0">
			<div class="card border shadow-sm bg-light bg-opacity-50">
				<div class="card-body py-2 px-3">
					<div class="d-flex align-items-center gap-4">
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">발주부서</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
								<input v-model="searchForm.DEPTNM" type="text" class="form-control" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT_SEARCH')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">발주일자</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.INYMDFR" type="date" class="form-control form-control-sm" style="width: 140px;" />
								<span class="text-muted mx-1">~</span>
								<input v-model="searchForm.INYMDTO" type="date" class="form-control form-control-sm" style="width: 140px;" />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 3. 메인 분할 작업 영역 -->
		<div class="d-flex flex-row flex-grow-1 overflow-hidden p-2 gap-2">
			<!-- 3-1. 좌측: PO 목록 -->
			<div class="card border shadow-sm d-flex flex-column" style="width: 280px; min-width: 280px;">
				<div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 선적 대상 PO</span>
				</div>
				<div class="flex-grow-1 overflow-hidden bg-white">
					<div ref="poGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<!-- 3-2. 우측: 상세 정보 및 품목 그리드 -->
			<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
				<div class="card border shadow-sm overflow-hidden">
					<div class="card-body p-0">
						<table class="erp-table-full">
							<colgroup>
								<col style="width: 10%"><col style="width: 15%">
								<col style="width: 10%"><col style="width: 15%">
								<col style="width: 10%"><col style="width: 15%">
								<col style="width: 10%"><col style="width: 15%">
							</colgroup>
							<tbody>
								<tr>
									<th>PO No.</th>
									<td><input v-model="formData.FILENO" type="text" class="form-control form-control-sm bg-light fw-bold text-primary" readonly /></td>
									<th class="required">선적차수</th>
									<td>
										<select v-model="formData.SHIPSEQ" class="form-select form-select-sm" @change="fetchDetail">
											<option value="10">1차 선적</option><option value="20">2차 선적</option>
											<option value="30">3차 선적</option><option value="40">4차 선적</option>
											<option value="50">5차 선적</option>
										</select>
									</td>
									<th class="required">B/L No.</th>
									<td><input v-model="formData.BLNO" type="text" class="form-control form-control-sm" /></td>
									<th class="required">보관장소</th>
									<td>
										<select v-model="formData.STORSEAT" class="form-select form-select-sm">
											<option v-for="opt in seatOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
										</select>
									</td>
								</tr>
								<tr>
									<th class="required">선적일자</th>
									<td><input v-model="formData.SHIPYMD" type="date" class="form-control form-control-sm" /></td>
									<th class="required">도착일자</th>
									<td><input v-model="formData.ARVYMD" type="date" class="form-control form-control-sm" /></td>
									<th class="required">적용환율</th>
									<td><input v-model="formData.FRGNRATE" type="number" step="0.01" class="form-control form-control-sm text-end fw-bold text-primary" @input="calcWonAmt" /></td>
									<th></th>
									<td></td>
								</tr>
								<tr>
 									<th>거 래 처</th>
 									<td colspan="3">
										<div class="d-flex gap-1">
											<input v-model="formData.CUSTCD" type="text" class="form-control form-control-sm bg-light" style="width: 80px;" readonly />
											<input v-model="formData.CUSTNM" type="text" class="form-control form-control-sm bg-light" readonly />
										</div>
 									</td>
                                    <th class="required">원 산 지</th>
                                    <td><input v-model="formData.NACD" type="text" class="form-control form-control-sm bg-light" readonly /></td>
                                    <th class="required">통화구분</th>
                                    <td><input v-model="formData.CURRCD" type="text" class="form-control form-control-sm bg-light" readonly /></td>
                                </tr>
                                <tr>
                                    <th class="required">선 적 항</th>
                                    <td><input v-model="formData.SHIPPORT" type="text" class="form-control form-control-sm bg-light" readonly /></td>
                                    <th class="required">도 착 항</th>
                                    <td><input v-model="formData.ARVPORT" type="text" class="form-control form-control-sm bg-light" readonly /></td>
                                    <th class="required">결제조건</th>
                                    <td><input v-model="formData.PAYCOND" type="text" class="form-control form-control-sm bg-light" readonly /></td>
                                    <th class="required">가격조건</th>
                                    <td><input v-model="formData.PRICOND" type="text" class="form-control form-control-sm bg-light" readonly /></td>
                                </tr>
							</tbody>
						</table>
					</div>
				</div>

				<!-- 그리드 영역 -->
				<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
						<span class="fw-bold small text-dark d-flex align-items-center">
							<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 선적 품목 명세
						</span>
                        <!-- 💎 전체선택 버튼 제거 (그리드 헤더 클릭으로 통합) -->
					</div>
					<div class="card-body p-0 flex-grow-1 bg-white">
						<div ref="itemGridRef" style="height: 100%;"></div>
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
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchForm = reactive({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  INYMDFR: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  INYMDTO: new Date().toISOString().substring(0, 10)
})

const formData = reactive<any>({
  FILENO: '', SHIPSEQ: '10', BLNO: '',
  SHIPYMD: new Date().toISOString().substring(0, 10),
  ARVYMD: new Date().toISOString().substring(0, 10),
  STORSEAT: '100', FRGNRATE: 1350.00, CUSTNM: '', CUSTCD: '',
  NACD: '', CURRCD: '', SHIPPORT: '', ARVPORT: '',
  PAYCOND: '', PRICOND: '', PAYTERM: ''
})

const seatOptions = ref<any[]>([])
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null

async function fetchPoList() {
  try {
    const res = await api.post('/api/hsip/HSIP_110U_STR', { ...searchForm, ACTKIND: 'S1' })
    poGrid?.setData(res.data || [])
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('PO 조회 실패') }
}

async function fetchDetail() {
  if (!formData.FILENO) return
  try {
    const res = await api.post('/api/hsip/HSIP_110U_STR', { FILENO: formData.FILENO, SHIPSEQ: formData.SHIPSEQ, ACTKIND: 'S0' })
    if (res.data && res.data[0]) {
      Object.assign(formData, res.data[0])
      const resItems = await api.post('/api/hsip/HSIP_111U_STR', { FILENO: formData.FILENO, SHIPSEQ: formData.SHIPSEQ, ACTKIND: 'S0' })
      itemGrid?.setData(resItems.data || [])
    }
  } catch (e) { vAlertError('상세 조회 실패') }
}

async function handleSave() {
  const items = itemGrid?.getData().filter((r: any) => r.PROCYN === 'Y')
  if (!items || items.length === 0) return vAlertError('선적 처리할 품목을 선택하세요.')
  if (!confirm('선적정보를 등록 하시겠습니까?')) return
  try {
    await api.post('/api/hsip/HSIP_110U_STR', { ...formData, ACTKIND: 'A0', ITEMS: items })
    vAlert('저장되었습니다.'); fetchDetail()
  } catch (e) { vAlertError('저장 실패') }
}

const calcWonAmt = () => {
  const rows = itemGrid?.getRows()
  rows?.forEach(row => {
    const d = row.getData()
    row.update({ WONAMT: Math.floor((Number(d.AMT) || 0) * formData.FRGNRATE) })
  })
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().PROCYN === 'Y')
  rows.forEach(r => r.update({ PROCYN: allSelected ? 'N' : 'Y' }))
}

function initialize() {
  resetForm(formData); formData.SHIPSEQ = '10'; itemGrid?.clearData(); poGrid?.deselectRow()
}

onMounted(async () => {
  try {
      const resSeat = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E2', CMPYCD: authStore.CMPYCD, GBNCD: '313' } })
      seatOptions.value = resSeat.data.map((i: any) => ({ CODE: i.CODECD || i.CODE, CDNM: i.CODENM || i.CDNM }))
  } catch (e) {}

  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        { title: 'PO No.', field: 'FILENO', cssClass: 'fw-bold text-primary', minWidth: 150 },
        { title: '상태', field: 'SHIPYN', width: 100, formatter: (c) => c.getValue() === 'Y' ? '완료' : '미완' }
      ]
    })
    poGrid.on('rowClick', (e, row) => {
      formData.FILENO = row.getData().FILENO; fetchDetail()
    })
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        {
          title: '선택', field: 'PROCYN', hozAlign: 'center', width: 60, formatter: 'tickCross', editor: true,
          headerClick: (e, column) => toggleAllRows() // 💎 헤더 클릭 시 전체 선택/해제
        },
        { title: '품목명', field: 'ITEMNM', minWidth: 200, widthGrow: 2, cssClass: 'fw-bold', hozAlign: "left" },
        { title: '규격', field: 'ITSIZE', width: 150, hozAlign: "left" },
        { title: '단위', field: 'UNIT', width: 80 },
        { title: '발주량', field: 'PQTY', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '단가', field: 'PRICE', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 2 } },
        { title: '잔량', field: 'JQTY', hozAlign: 'right', width: 100, cssClass: 'text-danger fw-bold' },
        { title: '선적량', field: 'SQTY', hozAlign: 'right', width: 100, editor: 'number', cssClass: 'bg-yellow fw-bold' },
        { title: '외화금액', field: 'AMT', hozAlign: 'right', width: 120, formatter: 'money', formatterParams: { precision: 2 } },
        { title: '원화금액', field: 'WONAMT', hozAlign: 'right', width: 130, formatter: 'money', formatterParams: { precision: 0 } }
      ]
    })
  }
  fetchPoList()
})

const openHelp = (type: string) => { /* 팝업 로직 */ }
const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })
</script>

<style scoped>
/* 🎨 폰트 선명도 보정 및 전역 스타일 */
.hsip110u-wrapper {
  height: 100%;
  overflow: hidden;
  font-family: 'Pretendard', sans-serif;
  background-color: #f4f7fa !important;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 🎨 원칙 1-4: 시스템 공통 버튼 색상 표준 */
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 4px; border: none; }
.btn-init { background-color: #ffffff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #ffffff !important; }
.btn-save { background-color: #005a9f !important; color: #ffffff !important; }

/* 🎨 원칙 10: 폼 레이블 표준 (연한 회색 배경, 검정 글자) */
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f1f3f5; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 8px !important; color: #212529; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }

/* 🎨 원칙 9: 폼 라벨 스타일 */
.erp-label { font-weight: 700; font-size: 12px; color: #212529; min-width: 80px; }
.required::after { content: ' *'; color: #d32f2f; }
.bg-yellow { background-color: #fffde7 !important; }

/* 🎨 원칙 5, 6: 그리드 타이틀 색상 및 중앙 정렬 */
:deep(.tabulator) {
  border: 1px solid #dee2e6;
  font-size: 13px;
  color: #212529 !important;
}
:deep(.tabulator-header) {
  background-color: #f8f9fa !important;
  border-bottom: 2px solid #dee2e6 !important;
}
:deep(.tabulator-col-title) {
  color: #6c757d !important;
  font-weight: 800;
  text-align: center !important;
  cursor: pointer; /* 💎 선택 클릭 가능 표시 */
}
:deep(.tabulator-cell) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 4px !important;
  border-right: 1px solid #eee !important;
}
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }
</style>
