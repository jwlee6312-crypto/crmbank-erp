<!--
	=============================================================
	프로그램명	: 수입전표발행 [디자인 원칙 13가지 완벽 준수]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 전표 발행 기능 및 표준 UI 디자인 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip140u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1, 12. 상단 액션 바: 버튼 그룹 우측 상단 정렬 및 표준 색상 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-post-fill me-2 text-primary" style="font-size: 18px;"></i>
				수입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입전표발행 (HSIP140U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchUnissuedList">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="handleGenerateSlip">
					<i class="bi bi-file-earmark-check"></i> 전표 발행
				</button>
			</div>
		</div>

		<!-- 🔍 9. 최상단 검색 항목 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-light bg-opacity-50">
				<div class="card-body py-2 px-3">
					<div class="d-flex align-items-center gap-4">
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">발생부서</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
								<input v-model="searchForm.DEPTNM" type="text" class="form-control" placeholder="부서 선택" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">발발생일자</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.IOYMDFR" type="date" class="form-control form-control-sm" style="width: 140px;" />
								<span class="text-muted mx-1">~</span>
								<input v-model="searchForm.IOYMDTO" type="date" class="form-control form-control-sm" style="width: 140px;" />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 3. 중앙: 발행 대상 그리드 (상하좌우 중앙 정렬 표준) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-list-check me-2 text-primary"></i> 전표 발행 대기 목록
					</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<!-- 💰 10. 하단: 발행 정보 입력 영역 (표준 폼 적용) -->
			<div class="card border shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-secondary">
					<i class="bi bi-pencil-square me-1"></i> 전표 발행 설정
				</div>
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
								<th>대상합계</th>
								<td><input :value="formatNumber(targetTotalAmt)" class="form-control form-control-sm text-end bg-light fw-bold text-primary" readonly /></td>
								<th>부가세유형</th>
								<td>
									<select v-model="slipForm.VATTYPE" class="form-select form-select-sm">
										<option v-for="opt in vatOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
									</select>
								</td>
								<th>부가세금액</th>
								<td><input v-model="slipForm.VATAMT" type="number" class="form-control form-control-sm text-end" /></td>
								<th class="required">지불일자</th>
								<td><input v-model="slipForm.PAYYMD" type="date" class="form-control form-control-sm" /></td>
							</tr>
							<tr>
								<th class="required">지불계정</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="payment.ACCTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="payment.ACCTNM" type="text" class="form-control" placeholder="계정 선택" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('ACCT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>지불금액</th>
								<td colspan="5"><input v-model="payment.AMT" type="number" class="form-control form-control-sm text-end fw-bold text-danger" style="width: 200px;" /></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 13. 모든 변수명 대문자 고수
const searchForm = reactive({
	DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
	IOYMDFR: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
	IOYMDTO: new Date().toISOString().substring(0, 10)
})

const slipForm = reactive({ VATTYPE: '000', VATAMT: 0, PAYYMD: new Date().toISOString().substring(0, 10) })
const payment = reactive({ ACCTCD: '', ACCTNM: '', AMT: 0 })
const vatOptions = ref<any[]>([{ CODE: '000', CDNM: '해당없음' }])
const targetTotalAmt = ref(0)

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchUnissuedList = async () => {
	try {
		const res = await api.post('/api/hsip/HSIP_140U_STR', { ...searchForm, ACTKIND: 'S0', CMPYCD: authStore.CMPYCD })
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const handleGenerateSlip = async () => {
	const selected = mainGrid?.getSelectedData()
	if (!selected || selected.length === 0) return vAlertError('발행할 항목을 선택하세요.')
	if (!payment.ACCTCD) return vAlertError('지불계정을 선택하세요.')

	if (!confirm('전표를 발행하시겠습니까?')) return
	try {
		await api.post('/api/hsip/HSIP_140U_STR', {
			...searchForm, ...slipForm, ...payment,
			ACTKIND: 'A0', ITEMS: selected, UPDEMP: authStore.USERID
		})
		vAlert('전표가 발행되었습니다.')
		fetchUnissuedList()
	} catch (e) { vAlertError('발행 실패') }
}

const toggleAllRows = () => {
	if (!mainGrid) return
	const rows = mainGrid.getRows()
	const allSelected = mainGrid.getSelectedRows().length === rows.length
	if (allSelected) mainGrid.deselectRow()
	else mainGrid.selectRow()
}

const formatNumber = (val: any) => Number(val || 0).toLocaleString()

const initialize = () => {
	resetForm(searchForm);
	resetForm(slipForm);
	payment.ACCTCD = ''; payment.ACCTNM = ''; payment.AMT = 0;
	mainGrid?.clearData(); targetTotalAmt.value = 0;
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM',
			data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
			columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
			onConfirm: (d: any) => { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM }
		})
	} else if (type === 'ACCT') {
		Object.assign(modalProps, {
			title: '계정 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM',
			data: { GUBUN: 'A0', CMPYCD: authStore.CMPYCD },
			columns: [{ title: '코드', field: 'CODE', width: 100 }, { title: '계정명', field: 'CDNM', width: 200 }],
			onConfirm: (d: any) => { payment.ACCTCD = d.CODE; payment.ACCTNM = d.CDNM }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
			columns: [
				{
					title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 60, hozAlign: "center",
					headerClick: () => toggleAllRows() // 💎 헤더 클릭 시 전체 토글
				},
				{ title: "PO No.", field: "FILENO", width: 150, cssClass: "fw-bold" },
				{ title: "비용종류", field: "COSTNM", width: 150 },
				{ title: "발생일", field: "PUBYMD", width: 110 },
				{ title: "상세 적요", field: "BIGO", minWidth: 250, widthGrow: 1, hozAlign: "left" },
				{ title: "비용(원화)", field: "COSTAMT", hozAlign: "right", width: 140, formatter: "money", formatterParams: { precision: 0 } }
			]
		})
		mainGrid.on('rowSelectionChanged', (data) => {
			targetTotalAmt.value = data.reduce((acc, cur: any) => acc + (Number(cur.COSTAMT) || 0), 0)
			payment.AMT = targetTotalAmt.value + (Number(slipForm.VATAMT) || 0)
		})
	}
})
</script>

<style scoped>
/* 🎨 폰트 선명도 보정 및 전역 스타일 */
.hsip140u-wrapper {
  height: 100%;
  overflow: hidden;
  font-family: 'Pretendard', sans-serif;
  background-color: #f4f7fa !important;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

.erp-header { background-color: #ffffff !important; }

/* 🎨 원칙 1-4: 시스템 공통 버튼 색상 표준 적용 */
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

/* 🎨 원칙 5, 6: 그리드 타이틀 색상 및 중앙 정렬 표준 */
:deep(.tabulator) {
  border: 1px solid #dee2e6;
  font-size: 13px;
  color: #212529 !important;
  font-family: 'Pretendard', sans-serif !important;
}
:deep(.tabulator-header) {
  background-color: #f8f9fa !important;
  border-bottom: 2px solid #dee2e6 !important;
}
:deep(.tabulator-col-title) {
  color: #6c757d !important;
  font-weight: 800;
  text-align: center !important;
  cursor: pointer;
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
