<!--
	=============================================================
	프로그램명	: 미정산현황 (Unsettled Status) [디자인 원칙 13가지 + 검색영역 단일행 균등배분]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 거래처별 출고/정산/미정산 내역 비교 현황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio680s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1, 12. 상단 액션 바: 버튼 그룹 우측 상단 정렬 및 표준 색상 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-diff me-2 text-primary" style="font-size: 18px;"></i>
				영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">미정산현황 (HSIO680S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchData">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-excel" @click="handleExcel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
			</div>
		</div>

		<!-- 🔍 9. 최상단 검색 항목 영역 (단일행 균등 배분 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 33.3%;" />
						<col style="width: 33.3%;" />
						<col style="width: 33.4%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">판매부서</span>
									<div class="input-group input-group-sm flex-nowrap" style="max-width: 300px;">
										<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.DEPTNM" type="text" class="form-control" placeholder="부서 선택" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">출고일자</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1" style="max-width: 320px;">
										<input v-model="searchForm.FYMD" type="date" class="form-control form-control-sm" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.TYMD" type="date" class="form-control form-control-sm" />
									</div>
								</div>
							</td>
							<td>
								<!-- 균등 배분을 위한 빈 영역 또는 추가 필드 가능 -->
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 6. 중앙 그리드 영역 (상하좌우 중앙 정렬 표준) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
			<div class="row align-items-center w-100">
				<div class="col-md-2 small">조회 업체: <span class="fw-bold text-info">{{ rowCount }}</span> 개</div>
				<div class="col-md-10 text-end">
					<span class="me-4 small opacity-75">출고합계: <span class="fw-bold text-white ms-1">{{ formatNumber(totals.OSUM) }}</span></span>
					<span class="me-4 small opacity-75">정산합계: <span class="fw-bold text-info ms-1">{{ formatNumber(totals.JSUM) }}</span></span>
					<span class="fs-5 ms-2 fw-light">미정산 총액: <span class="fw-bold text-warning ms-2">{{ formatNumber(totals.NSUM) }}</span></span>
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
	DEPTCD: authStore.DEPTCD,
	DEPTNM: authStore.DEPTNM,
	FYMD: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
	TYMD: new Date().toISOString().substring(0, 10)
})

const rowCount = ref(0)
const totals = reactive({ OSUM: 0, JSUM: 0, NSUM: 0 })
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchData = async () => {
	try {
		const res = await api.post('/api/hsio/HSIO_680S_STR', {
			...searchForm,
			CMPYCD: authStore.CMPYCD,
			FYMD: searchForm.FYMD.replace(/-/g, ''),
			TYMD: searchForm.TYMD.replace(/-/g, '')
		})
		const data = res.data || []
		mainGrid?.setData(data)
		rowCount.value = data.length

		totals.OSUM = data.reduce((acc: number, cur: any) => acc + (Number(cur.OAMT||0) + Number(cur.OVAT||0)), 0)
		totals.JSUM = data.reduce((acc: number, cur: any) => acc + (Number(cur.JAMT||0) + Number(cur.JVAT||0)), 0)
		totals.NSUM = data.reduce((acc: number, cur: any) => acc + (Number(cur.NAMT||0) + Number(cur.NVAT||0)), 0)

		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm);
	searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
	searchForm.FYMD = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10);
	searchForm.TYMD = new Date().toISOString().substring(0, 10);
	mainGrid?.clearData(); rowCount.value = 0;
	totals.OSUM = 0; totals.JSUM = 0; totals.NSUM = 0;
}

const handleExcel = () => mainGrid?.download("xlsx", "미정산현황.xlsx")

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
	}
	modalVisible.value = true
}

const formatNumber = (val: any) => Number(val || 0).toLocaleString()

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 80 },
			columns: [
				{ title: "거래처명", field: "CUSTNM", minWidth: 180, hozAlign: "left", cssClass: "fw-bold", frozen: true },
				{
					title: "출고 정보",
					columns: [
						{ title: "수량", field: "OQTY", hozAlign: "right", width: 80, formatter: "money", formatterParams: { precision: 0 } },
						{ title: "공급가", field: "OAMT", hozAlign: "right", width: 110, formatter: "money" },
						{ title: "부가세", field: "OVAT", hozAlign: "right", width: 100, formatter: "money" },
						{ title: "합계", field: "OSUM", hozAlign: "right", width: 120, formatter: "money", cssClass: "bg-light text-dark", mutatorData: (v,d) => Number(d.OAMT||0) + Number(d.OVAT||0) }
					]
				},
				{
					title: "정산 정보",
					columns: [
						{ title: "수량", field: "JQTY", hozAlign: "right", width: 80, formatter: "money", formatterParams: { precision: 0 } },
						{ title: "공급가", field: "JAMT", hozAlign: "right", width: 110, formatter: "money" },
						{ title: "부가세", field: "JVAT", hozAlign: "right", width: 100, formatter: "money" },
						{ title: "합계", field: "JSUM", hozAlign: "right", width: 120, formatter: "money", cssClass: "bg-light text-info", mutatorData: (v,d) => Number(d.JAMT||0) + Number(d.JVAT||0) }
					]
				},
				{
					title: "미정산 내역",
					columns: [
						{ title: "수량", field: "NQTY", hozAlign: "right", width: 80, formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-danger" },
						{ title: "공급가", field: "NAMT", hozAlign: "right", width: 110, formatter: "money", cssClass: "text-danger" },
						{ title: "부가세", field: "NVAT", hozAlign: "right", width: 100, formatter: "money", cssClass: "text-danger" },
						{ title: "합계", field: "NSUM", hozAlign: "right", width: 120, formatter: "money", cssClass: "bg-light-danger fw-bold text-danger", mutatorData: (v,d) => Number(d.NAMT||0) + Number(d.NVAT||0) }
					]
				}
			]
		})
	}
})
</script>

<style scoped>
.hsio680s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

/* 🚀 입력 필드 글자 크기 및 높이 최적화 (표준) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12px; padding: 6px 10px !important; color: #495057;
  white-space: nowrap;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }

:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>

