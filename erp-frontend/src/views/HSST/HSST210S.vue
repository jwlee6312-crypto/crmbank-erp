<!--
	=============================================================
	프로그램명	: 월별 채권 잔액 현황 [디자인 원칙 13가지 + 검색영역 단일행 균등배분]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 거래처별 최근 6개월 판매/수금/잔액 추이 현황 (그리드 렌더링 보정 완료)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsst210s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-range-fill me-2 text-primary" style="font-size: 18px;"></i>
				매출분석 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">월별 채권 잔액 현황 (HSST210S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchData">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-excel" @click="handleExcel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
			</div>
		</div>

		<!-- 🔍 9. 최상단 검색 영역 (33.3% 균등 배분) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 33.3%;" /><col style="width: 33.3%;" /><col style="width: 33.4%;" />
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
									<span class="erp-label me-2">기준일자</span>
									<input v-model="searchForm.YMD" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
									<span class="text-muted small ms-1">현재</span>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">거 래 처</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1">
										<input v-model="searchForm.CUSTNMFR" type="text" class="form-control form-control-sm" placeholder="시작" @keyup.enter="openHelp('CUST_FR')" />
										<button class="btn btn-sm btn-outline-secondary px-1" @click="openHelp('CUST_FR')"><i class="bi bi-search"></i></button>
										<span class="text-muted">~</span>
										<input v-model="searchForm.CUSTNMTO" type="text" class="form-control form-control-sm" placeholder="종료" @keyup.enter="openHelp('CUST_TO')" />
										<button class="btn btn-sm btn-outline-secondary px-1" @click="openHelp('CUST_TO')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 6. 중앙 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-body p-0 flex-grow-1 bg-white d-flex flex-column">
					<div ref="mainGridRef" style="width: 100%; flex: 1 1 0%;"></div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">조회 업체: <span class="fw-bold text-info">{{ rowCount }}</span> 개</div>
				<div class="col-md-9 text-end opacity-75 small">※ 거래처별 3행(판매/수금/잔액) 단위로 집계됩니다.</div>
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

const today = new Date().toISOString().substring(0, 10);
const searchForm = reactive({
	DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
	YMD: today, CUSTCDFR: '', CUSTNMFR: '', CUSTCDTO: '', CUSTNMTO: ''
})

const rowCount = ref(0)
const mainGridRef = ref<HTMLDivElement | null>(null);
let mainGrid: Tabulator | null = null

const getMonthHeaders = (baseDate: string) => {
	const date = new Date(baseDate);
	const headers = [];
	for (let i = 5; i >= 0; i--) {
		const d = new Date(date.getFullYear(), date.getMonth() - i, 1);
		headers.push({ year: d.getFullYear(), month: String(d.getMonth() + 1).padStart(2, '0') });
	}
	return headers;
}

const getColumnsConfig = (baseDate: string) => {
	const months = getMonthHeaders(baseDate);
	const columns: any[] = [
		{ title: "거래처 / 담당자", field: "CUSTNM", minWidth: 200, widthGrow: 2, hozAlign: "left", cssClass: "fw-bold border-end", frozen: true,
		  formatter: (cell: any) => {
			  const d = cell.getData();
			  if (d.GUBUN_TYPE === '1') return (d.CUSTCD || '') + ' ' + (d.CUSTNM || '');
			  if (d.GUBUN_TYPE === '2') return d.CUSTNM || '';
			  return d.DAMDANG || '';
		  }
		},
		{ title: "구분", field: "GUBUN_NM", width: 150, hozAlign: "center", cssClass: "bg-light border-end", frozen: true }
	];
	months.forEach((m, idx) => {
		columns.push({
			title: `${m.year}년 ${m.month}월`, field: `AMT_M${idx + 1}`,
			hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 }
		});
	});
	columns.push({ title: "입금요청/회전", field: "REQ_INFO", width: 150, hozAlign: "right", formatter: "money" });
	return columns;
}

const fetchData = async () => {
	if (!searchForm.DEPTCD) return vAlertError('부서를 선택하세요.');
	try {
		const res = await api.post('/api/hsst/HSST_210S_STR', {
			...searchForm, CMPYCD: authStore.CMPYCD, YMD: searchForm.YMD.replace(/-/g, '')
		})
		const data = res.data || []
		mainGrid?.setColumns(getColumnsConfig(searchForm.YMD)); // 날짜 변경 대응
		mainGrid?.setData(data)
		rowCount.value = Math.ceil(data.length / 3);
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm);
	searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
	searchForm.YMD = today;
	mainGrid?.clearData(); rowCount.value = 0;
	mainGrid?.setColumns(getColumnsConfig(today));
}

const handleExcel = () => mainGrid?.download("xlsx", "월별채권현황.xlsx")

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM',
			data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
			columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
			onConfirm: (d: any) => { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM }
		})
	} else if (type.startsWith('CUST')) {
		Object.assign(modalProps, {
			title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CUSTNM',
			data: { GUBUN: 'C0', CMPYCD: authStore.CMPYCD },
			columns: [{ title: '코드', field: 'CUSTCD', width: 100 }, { title: '거래처명', field: 'CUSTNM', width: 200 }],
			onConfirm: (d: any) => {
				if (type === 'CUST_FR') { searchForm.CUSTCDFR = d.CUSTCD; searchForm.CUSTNMFR = d.CUSTNM }
				else { searchForm.CUSTCDTO = d.CUSTCD; searchForm.CUSTNMTO = d.CUSTNM }
			}
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: getColumnsConfig(searchForm.YMD),
			placeholder: "조회된 데이터가 없습니다."
		})
	}
})
</script>

<style scoped>
/* 🎨 폰트 선명도 보정 및 전역 스타일 */
.hsst210s-wrapper {
  height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif;
  background-color: #f4f7fa !important; -webkit-font-smoothing: antialiased;
}
.erp-header { background-color: #ffffff !important; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 4px; border: none; }
.btn-init { background-color: #ffffff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #ffffff !important; }
.btn-excel { background-color: #1d6f42 !important; color: #ffffff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f1f3f5; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 8px !important; color: #212529; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 8px 4px !important; background-color: #fff; vertical-align: middle; }
.erp-label { font-weight: 700; font-size: 12px; color: #212529; min-width: 60px; text-align: right; white-space: nowrap; }

:deep(.tabulator) { border: 1px solid #dee2e6; font-size: 13px; color: #212529 !important; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { color: #6c757d !important; font-weight: 800; text-align: center !important; }
:deep(.tabulator-cell) { display: flex !important; align-items: center !important; justify-content: center !important; border-right: 1px solid #eee !important; }
.erp-footer { background-color: #212529 !important; min-height: 50px; }
</style>
