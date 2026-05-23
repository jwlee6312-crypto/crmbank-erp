<!--수입관리/비용배부작업-->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip160u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-2 px-3 sticky-top">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
				수입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">비용배부작업 (HSIP160U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchPoList">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">배부 실행/취소</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 -->
		<table class="erp-table-full border-bottom" style="table-layout: fixed;">
			<colgroup>
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
			</colgroup>
			<tbody>
				<tr>
					<th class="required border-end">발주부서</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.DEPTCD" type="text" class="form-control bg-light text-center" style="max-width: 60px;" readonly />
							<input v-model="searchForm.DEPTNM" type="text" class="form-control" />
							<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="required border-end">통관일자</th>
					<td>
						<div class="d-flex align-items-center gap-1 ms-1">
							<input v-model="searchForm.FRYMD" type="date" class="form-control form-control-sm" />
							<span class="text-muted">~</span>
							<input v-model="searchForm.TOYMD" type="date" class="form-control form-control-sm" />
						</div>
					</td>
					<th class="border-end">배부여부</th>
					<td>
						<div class="d-flex gap-3 ms-1">
							<div class="form-check m-0">
								<input v-model="searchForm.DIVYN" class="form-check-input" type="radio" value="N" id="divN" />
								<label class="form-check-label small fw-bold" for="divN">미배부</label>
							</div>
							<div class="form-check m-0">
								<input v-model="searchForm.DIVYN" class="form-check-input" type="radio" value="Y" id="divY" />
								<label class="form-check-label small fw-bold" for="divY">배부완료</label>
							</div>
						</div>
					</td>
				</tr>
			</tbody>
		</table>

		<!-- 📊 3. 메인 작업 영역 -->
		<div class="d-flex flex-column flex-grow-1 overflow-hidden">
			<div class="bg-light border-bottom" style="height: 250px;">
				<div class="grid-title py-1 px-3 border-bottom bg-white fw-bold small text-secondary d-flex justify-content-between">
					<span><i class="bi bi-card-list me-1"></i> 배부 대상 PO 목록</span>
					<button class="btn btn-xs btn-outline-primary" @click="toggleSelectAll">전체선택</button>
				</div>
				<div ref="poGridRef" class="tabulator-full-height" />
			</div>

			<div class="flex-grow-1 bg-white overflow-hidden d-flex flex-column">
				<div class="grid-title py-1 px-3 border-bottom fw-bold small text-primary bg-light">
					<i class="bi bi-box-seam me-1"></i> 선택된 PO의 품목별 비용 배부 상세 내역
				</div>
				<div ref="detailGridRef" class="tabulator-full-height" />
			</div>
		</div>
	</div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
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
	DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
	FRYMD: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
	TOYMD: new Date().toISOString().substring(0, 10),
	DIVYN: 'N'
})

const poGridRef = ref<HTMLDivElement | null>(null); const detailGridRef = ref<HTMLDivElement | null>(null);
let poGrid: Tabulator | null = null; let detailGrid: Tabulator | null = null;

async function fetchPoList() {
	if (!searchForm.DEPTCD) return vAlertError('발주부서를 선택하세요.')
	try {
		const res = await api.post('/api/hsip/HSIP_160U_STR', {
            ...searchForm,
            FRYMD: searchForm.FRYMD.replace(/-/g, ''),
            TOYMD: searchForm.TOYMD.replace(/-/g, ''),
            ACTKIND: 'S0',
            CMPYCD: authStore.CMPYCD
        })
		poGrid?.setData(res.data); detailGrid?.clearData(); vAlert('조회되었습니다.')
	} catch (e) { vAlertError('PO 조회 실패') }
}

async function fetchDetail(row: any) {
	const d = row.getData()
	try {
		const res = await api.post('/api/hsip/HSIP_161S_STR', {
            CMPYCD: authStore.CMPYCD, FILENO: d.FILENO, SHIPSEQ: d.SHIPSEQ, PASSSEQ: d.PASSSEQ, DIVYN: searchForm.DIVYN
        })
		detailGrid?.setData(res.data)
	} catch (e) { vAlertError('상세 내역 로드 실패') }
}

async function handleSave() {
	const selected = poGrid?.getSelectedData()
	if (!selected || selected.length === 0) return vAlertError('배부 처리할 PO를 선택하세요.')

	const msg = searchForm.DIVYN === 'N' ? '배부작업을 실행하시겠습니까?' : '배부작업을 취소하시겠습니까?'
	if (!confirm(msg)) return

	try {
        const act = searchForm.DIVYN === 'N' ? 'A0' : 'D0'
        for (const item of selected) {
            await api.post('/api/hsip/HSIP_160U_STR', {
                ...item,
                ACTKIND: act,
                CMPYCD: authStore.CMPYCD,
                UPDEMP: authStore.USERID
            })
        }
		vAlert('처리가 완료되었습니다.'); fetchPoList()
	} catch (e) { vAlertError('처리 중 오류 발생') }
}

const toggleSelectAll = () => { poGrid?.getRows().forEach(r => r.select()) }

function initialize() {
	resetForm(searchForm); poGrid?.clearData(); detailGrid?.clearData()
	Object.assign(searchForm, {
        DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
        FRYMD: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
        TOYMD: new Date().toISOString().substring(0, 10),
        DIVYN: 'N'
    })
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
    const commonProps = { path: '/api/ha00/HA00_00P_STR', CMPYCD: authStore.CMPYCD };
    if (type === 'DEPT') {
        Object.assign(modalProps, {
            title: '부서 선택', ...commonProps, data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
            columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 200 }],
            onConfirm: (d: any) => { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM }
        })
    }
    modalVisible.value = true
}

onMounted(() => {
	if (poGridRef.value) {
		poGrid = new Tabulator(poGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			placeholder: '데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', minWidth: 100 },
			columns: [
				{ title: '선택', formatter: 'rowSelection', titleFormatter: 'rowSelection', hozAlign: 'center', width: 100 },
				{ title: 'PO No.', field: 'FILENO', width: 250, cssClass: 'fw-bold text-primary' },
				{ title: '선적', field: 'SHIPSEQ', hozAlign: 'center', width: 200, formatter: (c) => `${c.getValue().substring(0,1)}차` },
				{ title: '통관', field: 'PASSSEQ', hozAlign: 'center', width: 200, formatter: (c) => `${c.getValue().substring(0,1)}차` },
				{ title: '거래처(Vender)', field: 'CUSTNM', minWidth: 300, widthGrow: 1, cssClass: 'border-start' },
				{ title: '총비용(원화)', field: 'COSTAMT', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '배부일자', field: 'DIVYMD', hozAlign: 'center', width: 200 }
			]
		})
		poGrid.on('rowClick', (e, row) => fetchDetail(row))
	}

	if (detailGridRef.value) {
		detailGrid = new Tabulator(detailGridRef.value, {
			layout: 'fitColumns', height: '100%',
			placeholder: 'PO를 선택하면 상세 품목 정보가 표시됩니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', minWidth: 80 },
			columns: [
				{ title: '품목명칭', field: 'ITEMNM', minWidth: 250, widthGrow: 2, cssClass: 'fw-bold text-dark' },
				{ title: '규격', field: 'ITSIZE', width: 250 },
				{ title: '단위', field: 'UNIT', hozAlign: 'center', width: 100 },
				{ title: '입고수량', field: 'INQTY', hozAlign: 'right', width: 150, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '비용종류', field: 'COSTNM', minWidth: 200, widthGrow: 1, cssClass: 'border-start text-primary' },
				{ title: '단가(Unit)', field: 'PRICE', hozAlign: 'right', width: 150, formatter: 'money', formatterParams: { precision: 2 } },
				{ title: '배부금액', field: 'COSTAMT', hozAlign: 'right', width: 150, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'fw-bold' },
				{ title: '배부기준', field: 'DIVNM', hozAlign: 'center', width: 200 }
			]
		})
	}
	fetchPoList()
})
</script>

<style scoped>
.hsip160u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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

