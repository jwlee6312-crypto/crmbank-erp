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
							<span class="erp-label">발생일자</span>
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
					title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 100, hozAlign: "center",
					headerClick: () => toggleAllRows() // 💎 헤더 클릭 시 전체 토글
				},
				{ title: "PO No.", field: "FILENO", width: 250, cssClass: "fw-bold" },
				{ title: "비용종류", field: "COSTNM", width: 250 },
				{ title: "발생일", field: "PUBYMD", width: 200 },
				{ title: "상세 적요", field: "BIGO", minWidth: 250, widthGrow: 1, hozAlign: "left" },
				{ title: "비용(원화)", field: "COSTAMT", hozAlign: "right", width: 200, formatter: "money", formatterParams: { precision: 0 } }
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
.hsip140u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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
