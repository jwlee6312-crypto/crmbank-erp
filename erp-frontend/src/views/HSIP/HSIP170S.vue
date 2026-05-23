<!--수입관리/수입원가계산서-->
<!--
	=============================================================
	프로그램명	  : 수입원가계산서 (시스템 최종 표준 모델)
    프로그램 ID	: HSIP170S
	작성일자	    : 25.02.24
	작성자	      : AI Assistant (Full-Expansion Layout)
    설명         : 년월 입력 방식을 문자 6자리(YYYYMM) 체계로 변경
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip170s-wrapper d-flex flex-column h-100 bg-white p-0 overflow-auto">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-1 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 13px;">
				<i class="bi bi-file-earmark-ruled-fill me-2 text-primary" style="font-size: 18px;"></i>
				수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입원가계산서 (HSIP170S)</span>
			</div>
			<div class="btn-group-erp pe-2">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchData">조회</button>
				<button class="btn-erp btn-print" @click="handlePrint">인쇄</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 -->
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
							<input v-model="searchForm.FILENO" type="text" class="form-control border-primary-subtle fw-bold" placeholder="PO 번호 입력" @keyup.enter="fetchData" />
							<button class="btn btn-outline-secondary px-2" @click="openPoPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="required border-end text-nowrap text-center">선적차수</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.SHIPSEQ" type="text" class="form-control text-center" style="max-width: 60px;" />
							<button class="btn btn-outline-secondary px-2" @click="openShipPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="required border-end text-nowrap text-center">통관차수</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.PASSSEQ" type="text" class="form-control text-center" style="max-width: 60px;" />
							<button class="btn btn-outline-secondary px-2" @click="openPassPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="border-end text-nowrap text-center">품    목</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.ITEMCD" type="text" class="form-control bg-light" style="max-width: 80px;" readonly />
							<input v-model="searchForm.ITEMNM" type="text" class="form-control" />
							<button class="btn btn-outline-secondary px-2" @click="openItemPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
				</tr>
			</tbody>
		</table>

		<!-- 📊 3. 메인 작업 영역 -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column bg-white">
			<div class="flex-grow-1 d-flex flex-column overflow-hidden">
				<div class="grid-title py-1 px-3 border-bottom fw-bold small text-primary bg-light">
					<i class="bi bi-box-seam-fill me-1"></i> 품목별 수입비용 배부 상세 현황
				</div>
				<div ref="mainGridRef" class="tabulator-full-height border-top" />
			</div>

			<div class="bg-light border-top" style="height: 200px;">
				<div class="grid-title py-1 px-3 border-bottom bg-white fw-bold small text-secondary">
					<i class="bi bi-list-stars me-1"></i> 수입 비용종류별 요약 (Summary)
				</div>
				<div ref="summaryGridRef" class="tabulator-full-height" />
			</div>
		</div>

		<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchForm = reactive({
	FILENO: '', SHIPSEQ: '10', PASSSEQ: '10', ITEMCD: '', ITEMNM: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null); const summaryGridRef = ref<HTMLDivElement | null>(null);
let mainGrid: Tabulator | null = null; let summaryGrid: Tabulator | null = null;

async function fetchData() {
	if (!searchForm.FILENO) {
		return vAlertError('PO No를 입력하세요.');
	}
	try {
		const res = await api.post('/api/hsip/HSIP_161S_STR', {
			...searchForm,
			CMPYCD: authStore.CMPYCD,
			ACTKIND: 'S0'
		})
		if (res.data) {
			mainGrid?.setData(res.data.filter((i:any) => i.GBN !== 'SUM') || [])
			summaryGrid?.setData(res.data.filter((i:any) => i.GBN === 'SUM') || [])
			vAlert('조회되었습니다.')
		}
	} catch (e) { vAlertError('조회 실패') }
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	const commonProps = { path: '/api/hs00/HS00_000S_STR', CMPYCD: authStore.CMPYCD };
	if (type === 'PO') {
		Object.assign(modalProps, {
			title: 'PO 선택', ...commonProps,
			data: { GUBUN: 'F0', CMPYCD: authStore.CMPYCD, GBNCD: '1' },
			columns: [
				{ title: 'PO No', field: 'FILENO', width: 120 },
				{ title: '거래처', field: 'CUSTNM', minWidth: 200, widthGrow: 1 },
				{ title: '발주일자', field: 'ISSYMD', width: 120, formatter: (c:any) => '발주일:' + c.getValue() }
			],
			onConfirm: (d: any) => { searchForm.FILENO = d.FILENO; fetchData() }
		})
	} else if (type === 'ITEM') {
		Object.assign(modalProps, {
			title: '품목 선택', path: '/api/hs00/HS00_000S_STR', defaultField: 'ITEMNM', large: true,
			data: { GUBUN: 'I1', CMPYCD: authStore.CMPYCD, GBNCD: '1' },
			columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '품목명', field: 'ITEMNM', width: 200 }, { title: '규격', field: 'ITSIZE', width: 150 }],
			onConfirm: (d: any) => { searchForm.ITEMCD = d.ITEMCD; searchForm.ITEMNM = d.ITEMNM }
		})
	}
	modalVisible.value = true
}

const openPoPopup = () => openHelp('PO')
const openShipPopup = () => { vAlert('선적차수를 직접 입력하거나 PO 조회 후 확인하세요.') }
const openPassPopup = () => { vAlert('통관차수를 직접 입력하거나 PO 조회 후 확인하세요.') }
const openItemPopup = () => openHelp('ITEM')
const handlePrint = () => { vAlert('인쇄 기능을 준비 중입니다.') }

function initialize() {
	resetForm(searchForm); mainGrid?.clearData(); summaryGrid?.clearData()
	searchForm.SHIPSEQ = '10'; searchForm.PASSSEQ = '10'
}

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			placeholder: '표시할 데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', minWidth: 100 },
			columns: [
				{ title: '품목명칭', field: 'ITEMNM', minWidth: 250, widthGrow: 10, cssClass: 'fw-bold text-dark' },
				{ title: '규격', field: 'ITSIZE', width: 250 },
				{ title: '단위', field: 'UNIT', hozAlign: 'center', width: 100 },
				{ title: '입고수량', field: 'INQTY', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '비용종류', field: 'COSTNM', width: 250, cssClass: 'border-start text-primary' },
				{ title: '단가(KRW)', field: 'PRICE', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 2 } },
				{ title: '배부금액', field: 'COSTAMT', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'fw-bold' }
			]
		})
	}
	if (summaryGridRef.value) {
		summaryGrid = new Tabulator(summaryGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', minWidth: 100 },
			columns: [
				{ title: '비용종류', field: 'COSTNM', widthGrow: 1 },
				{ title: '배부기준', field: 'DIVNM', width: 350 },
				{ title: '총액', field: 'COSTAMT', hozAlign: 'right', width: 350, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'text-danger fw-bold' }
			]
		})
	}
})
</script>

<style scoped>
.hsip170s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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
