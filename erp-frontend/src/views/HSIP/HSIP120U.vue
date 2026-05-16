<!--
	=============================================================
	프로그램명	: 통관입고입력 [디자인 원칙 13가지 완벽 준수]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 전체선택 버튼 제거 및 그리드 헤더 클릭 통합, 표준 UI 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsip120u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1, 12. 상단 액션 바: 버튼 그룹 우측 상단 정렬 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-box-arrow-in-down me-2 text-primary" style="font-size: 18px;"></i>
				수입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">통관입고입력 (HSIP120U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchPoList">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="handleSave">
					<i class="bi bi-save"></i> 저장
				</button>
				<button class="btn-erp btn-print" @click="handlePrint">
					<i class="bi bi-printer"></i> 인쇄
				</button>
			</div>
		</div>

		<!-- 🔍 9. 최상단 검색 항목 영역 -->
		<div class="p-2 pb-0">
			<div class="card border shadow-sm bg-light bg-opacity-50">
				<div class="card-body py-2 px-3">
					<div class="d-flex align-items-center gap-4">
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">발주부서</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
								<input v-model="searchForm.DEPTNM" type="text" class="form-control" placeholder="부서 선택" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('S_DEPT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">선적일자</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.INYMDFR" type="date" class="form-control form-control-sm" style="width: 140px;" />
								<span class="text-muted mx-1">~</span>
								<input v-model="searchForm.INYMDTO" type="date" class="form-control form-control-sm" style="width: 140px;" />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 3. 메인 작업 영역 -->
		<div class="d-flex flex-row flex-grow-1 overflow-hidden p-2 gap-2">
			<!-- 3-1. 좌측: PO 목록 -->
			<div class="card border shadow-sm d-flex flex-column" style="width: 280px; min-width: 280px;">
				<div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-card-checklist me-1"></i> 통관 대상 PO</span>
				</div>
				<div class="flex-grow-1 overflow-hidden bg-white">
					<div ref="poGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<!-- 3-2. 우측: 상세 정보 및 품목 그리드 -->
			<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
				<!-- 🅰️ 10. 입력항목 영역 -->
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
									<th>PO No.</th>
									<td><input v-model="formData.FILENO" type="text" class="form-control form-control-sm bg-light fw-bold text-primary" readonly /></td>
									<th>선적차수</th>
									<td><input v-model="formData.SHIPSEQNM" type="text" class="form-control form-control-sm bg-light text-center" readonly /></td>
									<th class="required">통관차수</th>
									<td>
										<select v-model="formData.PASSSEQ" class="form-select form-select-sm" @change="fetchDetail">
											<option value="10">1차 통관</option><option value="20">2차 통관</option>
											<option value="30">3차 통관</option><option value="40">4차 통관</option>
											<option value="50">5차 통관</option>
										</select>
									</td>
								</tr>
								<tr>
									<th class="required">통관일자</th>
									<td><input v-model="formData.PASSYMD" type="date" class="form-control form-control-sm" /></td>
									<th class="required">세 관 명</th>
									<td>
										<select v-model="formData.TAXORG" class="form-select form-select-sm">
											<option value="">-- 선택 --</option>
											<option v-for="opt in taxOrgOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
										</select>
									</td>
									<th class="required">면장번호</th>
									<td><input v-model="formData.PASSNO" type="text" class="form-control form-control-sm" /></td>
								</tr>
								<tr>
									<th class="required">입고창고</th>
									<td>
										<select v-model="formData.WHCD" class="form-select form-select-sm">
											<option value="">-- 선택 --</option>
											<option v-for="opt in whOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
										</select>
									</td>
									<th class="required">입고부서</th>
									<td>
										<div class="input-group input-group-sm">
											<input v-model="formData.IDEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
											<input v-model="formData.IDEPTNM" type="text" class="form-control" placeholder="부서 선택" />
											<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
										</div>
									</td>
									<th class="required">적용환율</th>
									<td><input v-model="formData.FRGNRATE" type="number" step="0.01" class="form-control form-control-sm text-end fw-bold text-primary" /></td>
								</tr>
								<tr>
									<th>거 래 처</th>
									<td><input v-model="formData.CUSTNM" type="text" class="form-control form-control-sm bg-light" readonly /></td>
									<th>선 적 일</th>
									<td><input v-model="formData.SHIPYMD" type="text" class="form-control form-control-sm bg-light text-center" readonly /></td>
									<th>도 착 일</th>
									<td><input v-model="formData.ARVYMD" type="text" class="form-control form-control-sm bg-light text-center" readonly /></td>
								</tr>
								<tr>
									<th>통화구분</th>
									<td><input v-model="formData.CURRNM" type="text" class="form-control form-control-sm bg-light text-center" readonly /></td>
									<th>선 적 항</th>
									<td><input v-model="formData.SHIPPORTNM" type="text" class="form-control form-control-sm bg-light" readonly /></td>
									<th>도 착 항</th>
									<td><input v-model="formData.ARVPORTNM" type="text" class="form-control form-control-sm bg-light" readonly /></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>

				<!-- 🅱️ 8. 디테일 그리드 영역 -->
				<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
						<span class="fw-bold small text-dark d-flex align-items-center">
							<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 통관 품목 명세
						</span>
					</div>
					<div class="card-body p-0 flex-grow-1 bg-white">
						<div ref="itemGridRef" style="height: 100%;"></div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">항목: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="fs-5 ms-2 fw-light">통관 총액(외화): <span class="fw-bold text-warning ms-2">{{ formatNumber(totalSummaryAmt, 2) }}</span></span>
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
	DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
	INYMDFR: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
	INYMDTO: new Date().toISOString().substring(0, 10)
})

const formData = reactive<any>({
	FILENO: '', SHIPSEQ: '', SHIPSEQNM: '', PASSSEQ: '10',
    PASSYMD: new Date().toISOString().substring(0, 10),
	TAXORG: '', PASSNO: '', WHCD: '', BIGO: '',
    IDEPTCD: authStore.DEPTCD, IDEPTNM: authStore.DEPTNM,
	CUSTCD: '', CUSTNM: '', JSANYN: 'N',
    SHIPYMD: '', ARVYMD: '', CURRNM: '', SHIPPORTNM: '', ARVPORTNM: '', FRGNRATE: 0
})

const taxOrgOptions = ref<any[]>([]); const whOptions = ref<any[]>([])
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null
const activeItemCount = ref(0)
const totalSummaryAmt = ref(0)

const fetchPoList = async () => {
	try {
		const res = await api.post('/api/hsip/HSIP_120U_STR', { ...searchForm, ACTKIND: 'S1', CMPYCD: authStore.CMPYCD })
		poGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('PO 조회 실패') }
}

const fetchDetail = async () => {
	if (!formData.FILENO || !formData.SHIPSEQ) return
	try {
		const res = await api.post('/api/hsip/HSIP_120U_STR', {
            FILENO: formData.FILENO, SHIPSEQ: formData.SHIPSEQ, PASSSEQ: formData.PASSSEQ, ACTKIND: 'S0', CMPYCD: authStore.CMPYCD
        })
		if (res.data && res.data[0]) {
			Object.assign(formData, res.data[0])
            formData.SHIPSEQNM = `${formData.SHIPSEQ.substring(0,1)}차 선적`

            const resItems = await api.post('/api/hsip/HSIP_121U_STR', {
                FILENO: formData.FILENO, SHIPSEQ: formData.SHIPSEQ, PASSSEQ: formData.PASSSEQ, ACTKIND: 'S0', CMPYCD: authStore.CMPYCD
            })
            itemGrid?.setData(resItems.data || [])
            setTimeout(updateTotals, 100)
		}
	} catch (e) { vAlertError('상세 조회 실패') }
}

const handleSave = async () => {
    if (formData.JSANYN === 'Y') return vAlertError('정산된 자료입니다. 수정할 수 없습니다.')
	const items = itemGrid?.getData().filter((r: any) => r.PROCYN === 'Y')
	if (!items || items.length === 0) return vAlertError('통관 처리할 품목을 선택하세요.')

    if (!confirm('통관입고 작업을 하시겠습니까?')) return
	try {
		await api.post('/api/hsip/HSIP_120U_STR', { ...formData, ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, ITEMS: items, UPDEMP: authStore.USERID })
		vAlert('저장되었습니다.'); fetchDetail()
	} catch (e) { vAlertError('저장 실패') }
}

const updateTotals = () => {
    const data = itemGrid?.getData() || []
    const selected = data.filter((r: any) => r.PROCYN === 'Y')
    activeItemCount.value = selected.length
    totalSummaryAmt.value = selected.reduce((acc, cur: any) => acc + (Number(cur.AMT) || 0), 0)
}

const toggleAllRows = () => {
    const rows = itemGrid?.getRows(); if (!rows) return
    const allSelected = rows.every(r => r.getData().PROCYN === 'Y')
    rows.forEach(r => r.update({ PROCYN: allSelected ? 'N' : 'Y' }))
    updateTotals()
}

const formatNumber = (val: any, prec = 0) => new Intl.NumberFormat(undefined, { minimumFractionDigits: prec, maximumFractionDigits: prec }).format(Number(val) || 0)
const handlePrint = () => { vAlert('인쇄 기능을 준비 중입니다.') }

function initialize() {
	resetForm(formData);
    Object.assign(formData, { PASSSEQ: '10', PASSYMD: new Date().toISOString().substring(0, 10), IDEPTCD: authStore.DEPTCD, IDEPTNM: authStore.DEPTNM });
    itemGrid?.clearData(); poGrid?.deselectRow(); totalSummaryAmt.value = 0; activeItemCount.value = 0;
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
    const commonProps = { path: '/api/ha00/HA00_00P_STR', CMPYCD: authStore.CMPYCD };
    if (type === 'S_DEPT' || type === 'DEPT') {
        Object.assign(modalProps, {
            title: '부서 선택', ...commonProps, data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
            columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 200 }],
            onConfirm: (d: any) => {
                if (type === 'S_DEPT') { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM }
                else { formData.IDEPTCD = d.DEPTCD; formData.IDEPTNM = d.DEPTNM }
            }
        })
    }
    modalVisible.value = true
}

const loadCombos = async () => {
    const fetchCombo = async (gbn: string, gbncd: string = '') => {
        try {
            const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: gbn, CMPYCD: authStore.CMPYCD, GBNCD: gbncd, CODE: '' } })
            return res.data.map((i: any) => ({ CODECD: String(i.CODECD || i.CODE || '').trim(), CODENM: String(i.CODENM || i.CDNM || '').trim() }))
        } catch (e) { return [] }
    }
    taxOrgOptions.value = await fetchCombo('E2', '306')
    whOptions.value = await fetchCombo('W0', '')
    if (whOptions.value.length > 0) formData.WHCD = whOptions.value[0].CODECD
}

onMounted(() => {
    loadCombos()
	if (poGridRef.value) {
		poGrid = new Tabulator(poGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
            columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
			columns: [
				{ title: 'PO No.', field: 'FILENO', cssClass: 'fw-bold text-primary', minWidth: 150 },
				{ title: '선적', field: 'SHIPSEQ', width: 80, formatter: (c) => `${c.getValue().substring(0,1)}차` }
			]
		})
		poGrid.on('rowClick', (e, row) => {
			const d = row.getData(); formData.FILENO = d.FILENO; formData.SHIPSEQ = d.SHIPSEQ; fetchDetail()
		})
	}

	if (itemGridRef.value) {
        itemGrid = new Tabulator(itemGridRef.value, {
            layout: 'fitColumns',
            height: '100%',
            columnDefaults: {
                headerSort: false,
                headerHozAlign: "center",
                hozAlign: "center",
                vertAlign: "middle",
                minWidth: 100
            },
            columns: [
                { title: '선택', field: 'PROCYN', hozAlign: 'center', width: 60, formatter: 'tickCross', editor: true, headerClick: () => toggleAllRows() },
                { title: '품목명', field: 'ITEMNM', minWidth: 200, widthGrow: 2, cssClass: 'fw-bold', hozAlign: 'left' },
                { title: '규격', field: 'ITSIZE', width: 150, hozAlign: 'left' },
                { title: '단위', field: 'UNIT', width: 80 },
                { title: '선적량', field: 'SQTY', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
                { title: '단가', field: 'PRICE', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 2 } },
                { title: '잔량', field: 'JQTY', hozAlign: 'right', width: 100, cssClass: 'text-danger fw-bold' },
                { title: '통관량', field: 'IQTY', hozAlign: 'right', width: 100, editor: 'number', cssClass: 'bg-yellow fw-bold', formatter: 'money', formatterParams: { precision: 0 } },
                { title: '감모량', field: 'GQTY', hozAlign: 'right', width: 100, editor: 'number', cssClass: 'bg-yellow fw-bold', formatter: 'money', formatterParams: { precision: 0 } },
                { title: '외화금액', field: 'AMT', hozAlign: 'right', width: 120, formatter: 'money', formatterParams: { precision: 2 } }
            ]
        }); // <--- 세미콜론 및 괄호 닫기 확인

        itemGrid.on("cellEdited", (cell) => {
            if (cell.getField() === 'PROCYN') {
                updateTotals();
            }
        });
    } // <--- if문 닫기
	fetchPoList();
})
</script>

<style scoped>
.hsip120u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }

.erp-header { background-color: #ffffff !important; }

/* 💎 개별 파일의 스타일을 삭제하여 global.css 표준 디자인이 적용되도록 함 */
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }

erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  width: 1%; white-space: nowrap;
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12.5px; padding: 10px 15px !important; color: #495057;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 10px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
.bg-yellow { background-color: #fffde7 !important; }

</style>
