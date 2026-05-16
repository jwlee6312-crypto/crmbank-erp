<!--
	=============================================================
	프로그램명	: 수입B/L번호 등록
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] HSOD100U 표준 UI 적용 및 상하정중앙 정렬 보정
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip155u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-check me-2 text-primary" style="font-size: 18px;"></i>
				수입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입B/L번호등록</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm overflow-hidden">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 10%"><col style="width: 23%">
							<col style="width: 10%"><col style="width: 23%">
							<col style="width: 10%"><col style="width: 24%">
						</colgroup>
						<tbody>
							<tr>
								<th>PO No</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.FILENO" type="text" class="form-control fw-bold text-primary" placeholder="PO 번호 입력" />
										<button class="btn btn-outline-secondary px-2" @click="fetchList"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>B/L번호</th>
								<td><input v-model="formData.BLNO" type="text" class="form-control form-control-sm" /></td>
								<th>비고</th>
								<td><input v-model="formData.REMARK" type="text" class="form-control form-control-sm" /></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> B/L 등록 대상 리스트
					</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>
	</div>
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

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const formData = reactive<any>({ FILENO: '', BLNO: '', REMARK: '' })
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchList = async () => { /* 조회 */ }
const handleSave = async () => { /* 저장 */ }
const initialize = () => { resetForm(formData); mainGrid?.clearData(); }

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 100 },
			columns: [
				{ title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 50, hozAlign: "center" },
				{ title: 'PO No', field: 'FILENO', width: 150 },
				{ title: '거래처', field: 'CUSTNM', minWidth: 200, widthGrow: 1 },
				{ title: '선적일자', field: 'SHIPYMD', hozAlign: 'center', width: 110 },
				{ title: 'B/L번호', field: 'BLNO', width: 150 },
				{ title: '발주금액', field: 'BALAMT', hozAlign: 'right', formatter: 'money' }
			]
		})
	}
})
</script>

<style scoped>
.hsip155u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #ffffff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #ffffff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #ffffff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; border: 1px solid #dee2e6; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; font-size: 12.5px; }

:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; }
:deep(.tabulator-cell[data-field="BALAMT"]) { justify-content: flex-end; }
</style>
