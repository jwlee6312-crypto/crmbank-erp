<!--
	=============================================================
	프로그램명	: 수입비용입력 [디자인 원칙 13가지 완벽 준수]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 콤보박스(선적/통관차수) 적용 및 표준 UI 보정
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip130u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1, 12. 상단 액션 바: 버튼 그룹 우측 상단 정렬 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-cash-coin me-2 text-primary" style="font-size: 18px;"></i>
				수입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입비용입력 (HSIP130U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchDetail">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="handleSave">
					<i class="bi bi-save"></i> 저장
				</button>
				<button class="btn-erp btn-delete" @click="handleDelete" :disabled="!formData.DOCNO">
					<i class="bi bi-trash"></i> 삭제
				</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
			<!-- 🔍 9. 최상단 검색항목 구분 -->
			<div class="card border shadow-sm bg-light bg-opacity-50">
				<div class="card-body py-2 px-3">
					<div class="d-flex align-items-center gap-4">
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">발생부서</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
								<input v-model="formData.DEPTNM" type="text" class="form-control" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">PO No</span>
							<div class="input-group input-group-sm" style="width: 200px;">
								<input v-model="formData.FILENO" type="text" class="form-control fw-bold text-primary" placeholder="PO 번호" @keyup.enter="fetchDetail" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('PO')"><i class="bi bi-search"></i></button>
							</div>
						</div>
					</div>
				</div>
			</div>

			<!-- 🅰️ 10. 입력항목 영역 (마스터) -->
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
								<th class="required">비용번호</th>
								<td><input v-model="formData.DOCNO" type="text" class="form-control form-control-sm bg-light fw-bold" readonly placeholder="자동부여" /></td>
								<th class="required">발생일자</th>
								<td><input v-model="formData.PUBYMD" type="date" class="form-control form-control-sm w-auto" /></td>
								<th>특기사항</th>
								<td><input v-model="formData.REMARK" type="text" class="form-control form-control-sm" placeholder="비용 관련 참고사항" /></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 🅱️ 8. 디테일 그리드 영역 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 수입 제비용 상세 내역
					</span>
					<button class="btn btn-sm btn-outline-primary px-3" style="height: 28px; font-size: 12px; font-weight: 600;" @click="addRow">
						<i class="bi bi-plus-circle me-1"></i> 행추가
					</button>
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

const formData = reactive<any>({
	ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, FILENO: '',
    PUBYMD: new Date().toISOString().substring(0, 10), REMARK: '',
	DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, DOCNO: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchDetail = async () => {
    if (!formData.FILENO) return vAlertError('PO No를 입력하세요.')
	try {
		const res = await api.post('/api/hsip/HSIP_130U_STR', { ...formData, ACTKIND: 'S0' })
		if (res.data?.length > 0) {
            Object.assign(formData, res.data[0])
            mainGrid?.setData(res.data)
            vAlert('조회되었습니다.')
        } else {
            vAlertError('조회 결과가 없습니다.')
        }
	} catch (e) { vAlertError('조회 실패') }
}

const handleSave = async () => {
    if (!formData.FILENO) return vAlertError('PO No를 입력하세요.')
    const items = mainGrid?.getData();
    if (!items?.length) return vAlertError('저장할 항목이 없습니다.')

    try {
        const act = formData.DOCNO ? 'U0' : 'A0'
        await api.post('/api/hsip/HSIP_130U_STR', { ...formData, ACTKIND: act, ITEMS: items, UPDEMP: authStore.USERID })
        vAlert('저장되었습니다.')
        fetchDetail()
    } catch (e) { vAlertError('저장 실패') }
}

const handleDelete = async () => {
    if (!formData.DOCNO) return
    if (!confirm('정말 삭제하시겠습니까?')) return
    try {
        await api.post('/api/hsip/HSIP_130U_STR', { ...formData, ACTKIND: 'D0' })
        vAlert('삭제되었습니다.')
        initialize()
    } catch (e) { vAlertError('삭제 실패') }
}

const addRow = () => { mainGrid?.addRow({ COSTCD: '', COSTNM: '', COSTAMT: 0, SHIPSEQ: '10', PASSSEQ: '10', BIGO: '' }, true) }

const initialize = () => {
    resetForm(formData);
    Object.assign(formData, {
        ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
        PUBYMD: new Date().toISOString().substring(0, 10),
        DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM
    });
    mainGrid?.clearData();
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell?: any) {
    if (type === 'DEPT') {
        Object.assign(modalProps, {
            title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM',
            data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
            columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
            onConfirm: (d: any) => { formData.DEPTCD = d.DEPTCD; formData.DEPTNM = d.DEPTNM }
        })
    } else if (type === 'PO') {
        Object.assign(modalProps, {
            title: 'PO 선택', path: '/api/hs00/HS00_000S_STR', defaultField: 'FILENO',
            data: { GUBUN: 'F0', CMPYCD: authStore.CMPYCD, GBNCD: '1' },
            columns: [
                { title: 'PO No', field: 'FILENO', width: 120 },
                { title: '거래처', field: 'CUSTNM', minWidth: 200, widthGrow: 1 },
                { title: '발주일자', field: 'ISSYMD', width: 120 }
            ],
            onConfirm: (d: any) => { formData.FILENO = d.FILENO; fetchDetail() }
        })
    } else if (type === 'COST') {
        Object.assign(modalProps, {
            title: '비용항목 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM',
            data: { GUBUN: 'C2', CMPYCD: authStore.CMPYCD, GBNCD: 'IP' },
            columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '비용명', field: 'CDNM', width: 180 }],
            onConfirm: (d: any) => { cell.getRow().update({ COSTCD: d.CODE, COSTNM: d.CDNM }) }
        })
    }
    modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
			columns: [
				{ title: 'No', formatter: 'rownum', width: 100 },
                {
                    title: '비용코드', field: 'COSTCD', width: 120,
                    formatter: (cell) => {
                        return `<div class='d-flex align-items-center justify-content-between w-100 px-2 flex-nowrap'>
                                    <span>${cell.getValue() || ''}</span>
                                    <i class='bi bi-search text-primary cursor-pointer' style='font-size: 13px;'></i>
                                </div>`
                    },
                    cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) openHelp('COST', cell) }
                },
                { title: '비용 명칭', field: 'COSTNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
				{ title: '비용(원화)', field: 'COSTAMT', hozAlign: 'right', width: 200, editor: 'number', formatter: 'money', formatterParams: { precision: 0 } },
				{
                    title: '선적차수', field: 'SHIPSEQ', width: 200,
                    editor: "select",
                    editorParams: {
                        values: { "10": "1차", "20": "2차", "30": "3차", "40": "4차", "50": "5차" }
                    },
                    formatter: "lookup",
                    formatterParams: { "10": "1차", "20": "2차", "30": "3차", "40": "4차", "50": "5차" }
                },
				{
                    title: '통관차수', field: 'PASSSEQ', width: 200,
                    editor: "select",
                    editorParams: (cell: any) => {
                        // 💎 선적차수가 선택된 경우에만 콤보가 나타나거나 해당 차수에 맞는 목록 제공 (여기서는 1~5차 고정 예시)
                        return { values: { "10": "1차", "20": "2차", "30": "3차", "40": "4차", "50": "5차" } }
                    },
                    formatter: "lookup",
                    formatterParams: { "10": "1차", "20": "2차", "30": "3차", "40": "4차", "50": "5차" }
                },
				{ title: '상세 적요', field: 'BIGO', minWidth: 200, widthGrow: 1, editor: 'input', hozAlign: 'left' },
                {
                    title: "삭제", width: 100,
                    formatter: () => "<i class='bi bi-trash text-danger cursor-pointer' style='font-size: 16px;'></i>",
                    cellClick: (e, c) => { c.getRow().delete() }
                }
			]
		})
	}
})
</script>

<style scoped>
.hsip130u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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
