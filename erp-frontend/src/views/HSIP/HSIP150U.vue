<!--
	=============================================================
	프로그램명	: 수입L/C번호 등록
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] PO 팝업 전용 API 적용 및 백엔드 POST 통신 일치화
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip150u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-text me-2 text-primary" style="font-size: 18px;"></i>
				수입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입L/C번호등록 (HSIP150U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
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
										<input v-model="formData.FILENO" type="text" class="form-control fw-bold text-primary" placeholder="PO 번호 입력" @keyup.enter="fetchList" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('PO')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>L/C번호</th>
								<td><input v-model="formData.LCNO" type="text" class="form-control form-control-sm" /></td>
								<th>비고</th>
								<td><input v-model="formData.REMARK" type="text" class="form-control form-control-sm" /></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 🅱️ 디테일 그리드 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> L/C 등록 대상 리스트
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

const formData = reactive<any>({ FILENO: '', LCNO: '', REMARK: '' })
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchList = async () => {
    if (!formData.FILENO) return vAlertError('PO No를 입력하세요.');
	try {
		const res = await api.post('/api/hsip/HSIP_150U_STR', {
            ...formData,
            ACTKIND: 'S0',
            CMPYCD: authStore.CMPYCD
        })
		if (res.data) {
            mainGrid?.setData(res.data)
            if (res.data.length > 0 && !formData.LCNO) {
                formData.LCNO = res.data[0].LCNO || '';
                formData.REMARK = res.data[0].REMARK || '';
            }
            vAlert('조회되었습니다.')
        }
	} catch (e) { vAlertError('조회 실패') }
}

const handleSave = async () => {
    const selectedData = mainGrid?.getSelectedData();
    if (!selectedData || selectedData.length === 0) return vAlertError('저장할 항목을 선택하세요.');

    try {
        await api.post('/api/hsip/HSIP_150U_STR', {
            ...formData,
            ACTKIND: 'A0',
            CMPYCD: authStore.CMPYCD,
            UPDEMP: authStore.USERID,
            ITEMS: selectedData
        });
        vAlert('저장되었습니다.');
        fetchList();
    } catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
    resetForm(formData);
    mainGrid?.clearData();
}

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
            onConfirm: (d: any) => { formData.FILENO = d.FILENO; fetchList() }
        })
    }
    modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 100 },
			columns: [
				{ title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 100, hozAlign: "center" },
				{ title: 'PO No', field: 'FILENO', width: 250 },
				{ title: '거래처', field: 'CUSTNM', minWidth: 200, widthGrow: 1 },
				{ title: '발주일자', field: 'ISSYMD', hozAlign: 'center', width: 200 },
				{ title: '통화', field: 'CURRNM', hozAlign: 'center', width: 200 },
				{ title: '발주금액', field: 'BALAMT', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 2 } }
			]
		})
	}
})
</script>

<style scoped>
.hsip15u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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

