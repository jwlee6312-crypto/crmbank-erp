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
        { title: '규격', field: 'ITSIZE', width: 200, hozAlign: "left" },
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
.hsip110u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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
