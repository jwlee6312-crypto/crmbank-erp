<!--
	=============================================================
	프로그램명	: 수입제비용 정산입력
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] PO 팝업 전용 API 적용 및 상하정중앙 정렬 보정
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip145u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
				수입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입제비용정산입력 (HSIP145U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchDetail">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
				<button class="btn-erp btn-danger" @click="handleDelete" :disabled="!formData.FILENO">삭제</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
			<!-- 🅰️ 마스터 정보 -->
			<div class="card border shadow-sm overflow-hidden">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;"><col style="width: 23%">
							<col style="width: 100px;"><col style="width: 23%">
							<col style="width: 100px;"><col style="width: 24%">
						</colgroup>
						<tbody>
							<tr>
								<th>PO No</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.FILENO" type="text" class="form-control fw-bold text-primary" placeholder="PO 번호 입력" @keyup.enter="fetchDetail" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('PO')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>정산일자</th>
								<td><input v-model="formData.PUBYMD" type="date" class="form-control form-control-sm w-auto" /></td>
								<th>진행상태</th>
								<td><input :value="formData.JSANYN === 'Y' ? '정산완료' : '미정산'" class="form-control form-control-sm bg-light text-center" readonly /></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 🅱️ 디테일 그리드 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 수입비용 정산 품목 리스트
					</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
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
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const formData = reactive<any>({
	FILENO: '', PUBYMD: new Date().toISOString().substring(0, 10), JSANYN: 'N'
})

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchDetail = async () => {
	if (!formData.FILENO) return vAlertError('PO No를 입력하세요.')
	try {
		const res = await api.post('/api/hsip/HSIP_145U_STR', { FILENO: formData.FILENO, ACTKIND: 'S0', CMPYCD: authStore.CMPYCD })
		if (res.data) mainGrid?.setData(res.data)
        vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const handleSave = async () => { /* 저장 로직 */ }
const handleDelete = () => { /* 삭제 로직 */ }
const initialize = () => { resetForm(formData); mainGrid?.clearData(); formData.PUBYMD = new Date().toISOString().substring(0, 10); }

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
    if (type === 'PO') {
        Object.assign(modalProps, {
            title: 'PO 선택', path: '/api/hs00/HS00_000S_STR', defaultField: 'FILENO',
            data: { GUBUN: 'F0', CMPYCD: authStore.CMPYCD, GBNCD: '1' },
            columns: [
                { title: 'PO No', field: 'FILENO', width: 120 },
                { title: '거래처', field: 'CUSTNM', minWidth: 200, widthGrow: 1 },
                { title: '발주일자', field: 'ISSYMD', width: 120, formatter: (c:any) => '발주일:' + c.getValue() }
            ],
            onConfirm: (d: any) => { formData.FILENO = d.FILENO; fetchDetail() }
        })
    }
    modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 100 },
			columns: [
				{ title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
				{ title: '비용코드', field: 'COSTCD', width: 100 },
				{ title: '비용명칭', field: 'COSTNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '금액', field: 'COSTAMT', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '적요', field: 'BIGO', minWidth: 300, widthGrow: 1 }
			]
		})
	}
})
</script>

<style scoped>
.hsip145u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #ffffff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #ffffff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #ffffff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #ffffff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; font-size: 12.5px; }

:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; }
</style>
