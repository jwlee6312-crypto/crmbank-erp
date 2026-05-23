<!--
	=============================================================
	프로그램명	: 반품현황 (Return Status) [디자인 원칙 13가지 + 검색영역 단일행 균등배분]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 반품 내역 조회 및 상세 화면 이동 (검색 영역 균등 배분)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio470s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-arrow-return-left me-2 text-primary" style="font-size: 18px;"></i>
				영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				출고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">반품현황 (HSIO470S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize"><i class="bi bi-arrow-clockwise"></i> 초기화</button>
				<button class="btn-erp btn-search" @click="fetchData"><i class="bi bi-search"></i> 조회</button>
				<button class="btn-erp btn-excel" @click="handleExcel"><i class="bi bi-file-earmark-excel"></i> 엑셀</button>
			</div>
		</div>

		<!-- 🔍 검색 영역 (단일행 25% 균등 배분 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 25%;" />
						<col style="width: 25%;" />
						<col style="width: 25%;" />
						<col style="width: 25%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">판매부서</span>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.DEPTNM" type="text" class="form-control" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">반품일자</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1">
										<input v-model="searchForm.FRYMD" type="date" class="form-control form-control-sm" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.TOYMD" type="date" class="form-control form-control-sm" />
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">거 래 처</span>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="searchForm.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('CUST')" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">반품품목</span>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="searchForm.ITEMNM" type="text" class="form-control" placeholder="품목명 입력/선택" @keyup.enter="openHelp('ITEM')" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('ITEM')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>

		<!-- 📊 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">조회 건수: <span class="fw-bold text-info">{{ rowCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="me-4 small opacity-75">총 수량: <span class="fw-bold text-white ms-1">{{ formatNumber(totals.QTY) }}</span></span>
					<span class="me-4 small opacity-75">공급가액: <span class="fw-bold text-info ms-1">{{ formatNumber(totals.AMT) }}</span></span>
					<span class="fs-5 ms-2 fw-light">총 합계액: <span class="fw-bold text-warning ms-2">{{ formatNumber(totals.SUM) }}</span> 원</span>
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
import { useRouter } from 'vue-router'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date();
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
const today = now.toISOString().substring(0, 10);

// 13. 모든 변수명 대문자 고수
const searchForm = reactive({
	DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
	FRYMD: firstDay,
	TOYMD: today,
	CUSTCD: '', CUSTNM: '', ITEMCD: '', ITEMNM: ''
})

const rowCount = ref(0);
const totals = reactive({ QTY: 0, AMT: 0, SUM: 0 })
const mainGridRef = ref<HTMLDivElement | null>(null);
let mainGrid: Tabulator | null = null

const fetchData = async () => {
	try {
		const res = await api.post('/api/hsio/HSIO_470S_STR', {
			...searchForm,
			CMPYCD: authStore.CMPYCD,
			FRYMD: searchForm.FRYMD.replace(/-/g, ''),
			TOYMD: searchForm.TOYMD.replace(/-/g, '')
		})
		const data = res.data || []
		mainGrid?.setData(data)
		rowCount.value = data.length
		totals.QTY = data.reduce((acc: number, cur: any) => acc + (Number(cur.QTY) || 0), 0)
		totals.AMT = data.reduce((acc: number, cur: any) => acc + (Number(cur.AMT) || 0), 0)
		totals.SUM = data.reduce((acc: number, cur: any) => acc + (Number(cur.AMT || 0) + Number(cur.VAT || 0)), 0)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm);
	searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
	searchForm.FRYMD = firstDay;
	searchForm.TOYMD = today;
	mainGrid?.clearData();
	rowCount.value = 0;
	totals.QTY = 0; totals.AMT = 0; totals.SUM = 0;
}

const handleExcel = () => mainGrid?.download("xlsx", "반품현황.xlsx")

const modalVisible = ref(false);
const modalProps = reactive<any>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM',
			data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
			columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
			onConfirm: (d: any) => { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM }
		})
	} else if (type === 'CUST') {
		Object.assign(modalProps, {
			title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CUSTNM',
			data: { GUBUN: 'C0', CMPYCD: authStore.CMPYCD },
			columns: [{ title: '코드', field: 'CUSTCD', width: 100 }, { title: '거래처명', field: 'CUSTNM', width: 200 }],
			onConfirm: (d: any) => { searchForm.CUSTCD = d.CUSTCD; searchForm.CUSTNM = d.CUSTNM }
		})
	} else if (type === 'ITEM') {
		Object.assign(modalProps, {
			title: '품목 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM',
			data: { GUBUN: 'I1', CMPYCD: authStore.CMPYCD },
			columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '품목명', field: 'ITEMNM', width: 200 }],
			onConfirm: (d: any) => { searchForm.ITEMCD = d.ITEMCD; searchForm.ITEMNM = d.ITEMNM }
		})
	}
	modalVisible.value = true
}

const formatNumber = (val: any) => Number(val || 0).toLocaleString()

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
			columns: [
				{
					title: "출고번호", field: "IONO_FULL", width: 140, cssClass: "fw-bold text-primary cursor-pointer",
					formatter: (cell) => `${cell.getData().IOYM}-${cell.getData().IONO}`,
					cellClick: (e, cell) => {
						const d = cell.getData();
						router.push({ path: '/HSIO/HSIO490U', query: { IOYM: d.IOYM, IONO: d.IONO, DEPTCD: d.DEPTCD } });
					}
				},
				{ title: "거래처명", field: "CUSTNM", minWidth: 180, hozAlign: "left", cssClass: "fw-bold" },
				{ title: "품목명", field: "ITEMNM", minWidth: 200, widthGrow: 2, hozAlign: "left" },
				{ title: "규격", field: "ITSIZE", width: 150, hozAlign: "left" },
				{ title: "단위", field: "UNIT", width: 70 },
				{ title: "수량", field: "QTY", hozAlign: "right", width: 90, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "공급가", field: "AMT", hozAlign: "right", width: 120, formatter: "money" },
				{ title: "부가세", field: "VAT", hozAlign: "right", width: 110, formatter: "money" },
				{ title: "합계액", field: "AMTSUM", hozAlign: "right", width: 130, formatter: "money", cssClass: "text-primary fw-bold", mutatorData: (v,d) => Number(d.AMT||0) + Number(d.VAT||0) },
				{ title: "매출일자", field: "SALSYMD", width: 110, formatter: (c) => { const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v } }
			]
		})
	}
})
</script>

<style scoped>
.hsio470s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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
