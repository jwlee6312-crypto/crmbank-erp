<!--수입관리/수입발주현황-->
<!--
	=============================================================
	프로그램명	  : 수입발주현황 (시스템 최종 디자인 표준 모델)
    프로그램 ID	: HSIP200S
	작성일자	    : 25.02.24
	작성자	      : AI Assistant (Full-Expansion Layout)
    설명         : 명칭 가독폭 130px 고정, 4열 25% 균등배분, 텍스트 줄바꿈 절대금지(No-Wrap)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip200s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-1">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 13px;">
				<i class="bi bi-file-earmark-bar-graph-fill me-2 text-primary"></i>
				수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입발주현황</span>
			</div>
			<div class="btn-group-erp pe-2">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
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
					<th class="required border-end text-nowrap text-center">발주부서</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.DEPTCD" type="text" class="form-control bg-light" style="max-width: 60px;" readonly />
							<input v-model="searchForm.DEPTNM" type="text" class="form-control" />
							<button class="btn btn-outline-secondary px-2" @click="openDeptPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="border-end text-nowrap text-center">PO No</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.FILENO" type="text" class="form-control border-primary-subtle" placeholder="PO 번호 입력" @keyup.enter="fetchList" />
							<button class="btn btn-outline-secondary px-2" @click="openPoPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<td colspan="4" class="bg-light text-center small text-muted">※ 부서와 PO 번호로 수입 진행 현황을 조회합니다.</td>
				</tr>
			</tbody>
		</table>

		<!-- 📊 3. 하단 그리드 -->
		<div class="flex-grow-1 overflow-hidden bg-white">
			<div class="grid-title py-1 px-3 border-bottom fw-bold small text-primary bg-light">
				<i class="bi bi-list-check me-1"></i> 수입 발주별 진행 현황 리스트
			</div>
			<div ref="mainGridRef" class="tabulator-full-height border-top" />
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchForm = reactive({
	DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
	FILENO: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

async function fetchList() {
	if (!searchForm.DEPTCD) return vAlertError('발주부서를 선택하세요.')
	try {
		const res = await api.post('/api/hsip/HSIP_200S_STR', {
			...searchForm,
			CMPYCD: authStore.CMPYCD,
            ACTKIND: 'S0'
		})
		mainGrid?.setData(res.data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

function initialize() {
	resetForm(searchForm)
	searchForm.DEPTCD = authStore.DEPTCD
	searchForm.DEPTNM = authStore.DEPTNM
	mainGrid?.clearData()
}

const openDeptPopup = () => { vAlert('부서 팝업 준비 중') }
const openPoPopup = () => { vAlert('PO 팝업 준비 중') }

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			selectable: 1,
			placeholder: '표시할 데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', minWidth: 100 },
			columns: [
				{ title: '품목코드', field: 'ITEMCD', hozAlign: 'center', width: 110, cssClass: 'fw-bold text-primary border-end' },
				{ title: '품목 명칭 (규격)', field: 'ITEMNM', minWidth: 250, widthGrow: 10, cssClass: 'fw-bold' },
				{ title: '규격', field: 'ITSIZE', width: 120 },
				{ title: '단위', field: 'UNIT', hozAlign: 'center', width: 70 },
				{ title: '발주량', field: 'BALQTY', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '선적량', field: 'SHIPQTY', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'text-success' },
				{ title: '미선적량', field: 'UNSHIPQTY', hozAlign: 'right', width: 110, formatter: (c) => (Number(c.getData().BALQTY) - Number(c.getData().SHIPQTY)).toLocaleString() },
				{ title: '통관량', field: 'PASSQTY', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'text-primary' },
				{ title: '미입고량', field: 'UNPASSQTY', hozAlign: 'right', width: 110, formatter: (c) => (Number(c.getData().BALQTY) - Number(c.getData().PASSQTY)).toLocaleString(), cssClass: 'text-danger fw-bold' }
			]
		})
	}
	fetchList()
})
</script>

<style scoped>
.hsip200s-wrapper { height: 100%; overflow: hidden; padding: 0 !important; }
.tabulator-full-height { height: 100% !important; border: none; }
.btn-group-erp { display: flex; gap: 4px; }
.btn-erp { padding: 4px 15px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; border: 1px solid #ced4da; transition: all 0.2s; }
.btn-init { background-color: #fff; color: #6c757d; }
.btn-init:hover { background-color: #f8f9fa; border-color: #6c757d; }
.btn-search { background-color: #2d3748; color: #fff; border: none; }

.erp-table-full {
	table-layout: fixed;
	border-collapse: collapse !important;
	width: 100%;
	border-bottom: 1px solid #dee2e6 !important;
}
.erp-table-full th {
	background-color: #f1f5f9; border-right: 1px solid #dee2e6 !important;
	text-align: center; font-weight: 800; font-size: 12.5px; color: #334155;
	vertical-align: middle; padding: 8px 10px !important;
	white-space: nowrap !important;
}
.erp-table-full td {
	border: 1px solid #dee2e6 !important;
	padding: 4px 8px !important;
	vertical-align: middle;
	white-space: nowrap !important;
	font-size: 12.5px;
	overflow: hidden;
}
.erp-table-full th.required::after { content: ' *'; color: #ef4444; }

.form-control-sm, .form-select-sm { border-radius: 2px; border: 1px solid #cbd5e1; font-size: 12.5px; height: 28px; width: 100%; }
.form-control-sm:focus { border-color: #005a9f; box-shadow: 0 0 0 1px rgba(0, 90, 159, 0.1); }

:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; }

:deep(.tabulator-row.tabulator-selected) { background-color: #f0f7ff !important; border-left: 4px solid #005a9f !important; }
.grid-title { background-color: #f8fafc; font-size: 12px; }
</style>
