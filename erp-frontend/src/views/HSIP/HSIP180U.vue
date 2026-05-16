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
				{ title: '선택', formatter: 'rowSelection', titleFormatter: 'rowSelection', hozAlign: 'center', headerHozAlign: 'center', width: 50 },
				{ title: '품목코드', field: 'ITEMCD', width: 110, cssClass: 'bg-light' },
				// 💡 품목명: widthGrow를 부여하여 남은 모든 우측 공간 점유 (여백 제거)
				{ title: '품목 명칭 (규격)', field: 'ITEMNM', minWidth: 250, widthGrow: 10, cssClass: 'fw-bold border-start' },
				{ title: '입고일자', field: 'IOYMD', hozAlign: 'center', width: 110, formatter: (c) => {
					const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}.${v.substring(4,6)}.${v.substring(6,8)}` : v
				}},
				{ title: '입고수량', field: 'IOQTY', hozAlign: 'right', width: 110, formatter: 'money' },
				{ title: '최종 수입원가 (KRW)', field: 'IOAMT', hozAlign: 'right', width: 160, editor: 'number', formatter: 'money', cssClass: 'bg-yellow fw-bold text-danger' }
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
.hsip180u-wrapper { height: 100%; overflow: hidden; padding: 0 !important; }
.tabulator-full-height { height: 100% !important; border: none; }
.btn-erp { padding: 4px 15px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; border: 1px solid #ced4da; transition: all 0.2s; }
.btn-init { background-color: #fff; color: #6c757d; }
.btn-init:hover { background-color: #f8f9fa; border-color: #6c757d; }
.btn-search { background-color: #2d3748; color: #fff; border: none; }
.btn-save { background-color: #005a9f; color: #fff; border: none; }
.btn-print { background-color: #f59e0b; color: #fff; border: none; }

.erp-table-full { table-layout: fixed; border-collapse: collapse !important; width: 100%; border-bottom: 1px solid #dee2e6 !important; }
.erp-table-full th { background-color: #f1f5f9; border-right: 1px solid #dee2e6 !important; text-align: center; font-weight: 800; font-size: 12.5px; color: #334155; vertical-align: middle; padding: 8px 10px !important; white-space: nowrap !important; }
.erp-table-full td { border: 1px solid #dee2e6 !important; padding: 4px 8px !important; vertical-align: middle; white-space: nowrap !important; font-size: 12.5px; overflow: hidden; }
.erp-table-full th.required::after { content: ' *'; color: #ef4444; }

.form-control-sm { border-radius: 2px; border: 1px solid #cbd5e1; font-size: 12.5px; height: 28px; width: 100%; }
.bg-yellow { background-color: #fffbeb !important; }
.grid-title { background-color: #f8fafc; font-size: 12px; }

:deep(.tabulator-col) { background-color: #f8fafc !important; font-weight: 800 !important; color: #475569 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12.5px; }
:deep(.tabulator-cell) { font-size: 12.5px; }
:deep(.tabulator-row.tabulator-selected) { background-color: #f0f7ff !important; border-left: 4px solid #005a9f !important; }
</style>
