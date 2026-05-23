<!--수입관리/품목원가등록-->
<!--
	=============================================================
	프로그램명	  : 품목원가등록 (시스템 최종 표준 모델)
    프로그램 ID	: HSIP180U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant (Full-Expansion Layout)
    설명         : 명칭 가독폭 130px 고정, 4열 25% 균등배분, 텍스트 줄바꿈 절대금지(No-Wrap)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip180u-wrapper d-flex flex-column h-100 bg-white p-0 overflow-auto">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-1 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 13px;">
				<i class="bi bi-box-seam me-2 text-primary"></i>
				수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">품목원가등록</span>
			</div>
			<div class="btn-group-erp pe-2">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">원가 저장</button>
				<button class="btn-erp btn-print" @click="handlePrint">품목원가인쇄</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 및 상세 헤더 (25% 균등 배분) -->
		<table class="erp-table-full border-bottom" style="table-layout: fixed;">
			<colgroup>
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
			</colgroup>
			<tbody>
				<tr>
					<th class="required border-end text-nowrap text-center">PO No</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.FILENO" type="text" class="form-control border-primary-subtle fw-bold" placeholder="PO 번호 입력" />
							<button class="btn btn-outline-secondary px-2" @click="openPoPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="required border-end text-nowrap text-center">선적차수</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.SHIPSEQ" type="text" class="form-control text-center" style="max-width: 60px;" readonly />
							<button class="btn btn-outline-secondary px-2" @click="openShipPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="required border-end text-nowrap text-center">통관차수</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.PASSSEQ" type="text" class="form-control text-center" style="max-width: 60px;" readonly />
							<button class="btn btn-outline-secondary px-2" @click="openPassPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<td colspan="2" class="bg-light text-center small text-muted">※ 전표 마감 여부를 확인 후 작업하십시오.</td>
				</tr>
			</tbody>
		</table>

		<!-- 📊 3. 하단 원가 리스트 그리드 (품목명 무한 확장) -->
		<div class="flex-grow-1 overflow-hidden bg-white d-flex flex-column">
			<div class="grid-title py-1 px-3 border-bottom fw-bold small text-primary bg-light">
				<i class="bi bi-currency-dollar me-1"></i> 품목별 최종 수입 원가 등록 현황
			</div>
			<div ref="mainGridRef" class="tabulator-full-height border-top" />

			<!-- 💰 4. 하단 요약 합계 -->
			<div class="erp-grid-summary d-flex border-top bg-light py-2 px-3 fw-bold text-dark shadow-sm">
				<div class="flex-grow-1 text-center border-end">전체 원가 합계 (Total Cost)</div>
				<div style="width: 200px;" class="text-end px-3 border-start text-danger">총 원가금액: {{ formatNumber(totalSummaryAmt) }}</div>
				<div style="width: 50px;"></div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchForm = reactive({
	FILENO: '', SHIPSEQ: '', PASSSEQ: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null;
const totalSummaryAmt = ref(0);

async function fetchList() {
	if (!searchForm.FILENO || !searchForm.SHIPSEQ || !searchForm.PASSSEQ) {
		return vAlertError('조회 조건을 모두 입력하세요.');
	}
	try {
		const res = await api.get('/api/hsip/HSIP180U_ItemCost/list', {
			params: { ...searchForm, CMPYCD: authStore.CMPYCD }
		})
		mainGrid?.setData(res.data); vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

async function handleSave() {
	const items = mainGrid?.getData().filter((r: any) => r.PROCYN === 'Y')
	if (!items || items.length === 0) return vAlertError('저장할 항목을 선택하세요.')

	try {
		await api.post('/api/hsip/HSIP180U_ItemCost/save', { header: searchForm, items: items, userId: authStore.USER_ID })
		vAlert('원가 등록이 완료되었습니다.'); fetchList()
	} catch (e) { vAlertError('저장 실패') }
}

const openPoPopup = () => { vAlert('PO 팝업 준비 중') }
const openShipPopup = () => { vAlert('선적차수 팝업 준비 중') }
const openPassPopup = () => { vAlert('통관차수 팝업 준비 중') }
const handlePrint = () => { vAlert('품목원계산서 출력 시작') }

function initialize() {
	resetForm(searchForm); mainGrid?.clearData(); totalSummaryAmt.value = 0
}

const formatNumber = (val: number) => Number(val || 0).toLocaleString()

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			placeholder: '데이터가 존재하지 않습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', minWidth: 100 },
			columns: [
				{ title: '선택', formatter: 'rowSelection', titleFormatter: 'rowSelection', hozAlign: 'center', headerHozAlign: 'center', width: 100 },
				{ title: '품목코드', field: 'ITEMCD', width: 150, cssClass: 'bg-light' },
				// 💡 품목명: widthGrow를 부여하여 남은 모든 우측 공간 점유 (여백 제거)
				{ title: '품목 명칭 (규격)', field: 'ITEMNM', minWidth: 250, widthGrow: 10, cssClass: 'fw-bold border-start' },
				{ title: '입고일자', field: 'IOYMD', hozAlign: 'center', width: 150, formatter: (c) => {
					const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}.${v.substring(4,6)}.${v.substring(6,8)}` : v
				}},
				{ title: '입고수량', field: 'IOQTY', hozAlign: 'right', width: 150, formatter: 'money' },
				{ title: '최종 수입원가 (KRW)', field: 'IOAMT', hozAlign: 'right', width: 200, editor: 'number', formatter: 'money', cssClass: 'bg-yellow fw-bold text-danger' }
			]
		})

		mainGrid.on('rowSelectionChanged', () => {
			const selected = mainGrid?.getSelectedData() || []
			totalSummaryAmt.value = selected.reduce((acc, cur: any) => acc + (Number(cur.IOAMT) || 0), 0)
		})
	}
})
</script>
<style scoped>
.hsip180u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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
