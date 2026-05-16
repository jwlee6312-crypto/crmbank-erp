<!--
	=============================================================
	프로그램명	: 매출 총이익 명세서 (Statement of Gross Sales Profit) [디자인 원칙 13가지 완벽 준수]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 부서별/품목별 매출액, 원가, 이익 및 이익률 현황 조회 (당월/누계)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsst340s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1, 11, 12. 상단 액션 바: 버튼 그룹 우측 상단 정렬 및 표준 색상 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-graph-up-arrow me-2 text-primary" style="font-size: 18px;"></i>
				매출분석 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">매출 총이익 명세서 (HSST340S)</span>
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
				<button class="btn-erp btn-print" @click="handlePrint">
					<i class="bi bi-printer"></i> 인쇄
				</button>
			</div>
		</div>

		<!-- 🔍 9. 최상단 검색 항목 영역 (단일행 균등 배분 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 33.3%;" />
						<col style="width: 33.3%;" />
						<col style="width: 33.4%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">매출부서</span>
									<div class="input-group input-group-sm flex-nowrap" style="max-width: 300px;">
										<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.DEPTNM" type="text" class="form-control" placeholder="부서 선택" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">조회연월</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1" style="max-width: 250px;">
										<select v-model="searchForm.YY" class="form-select form-select-sm">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
										</select>
										<select v-model="searchForm.MM" class="form-select form-select-sm">
											<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
										</select>
										<span class="text-muted small ms-1">현재</span>
									</div>
								</div>
							</td>
							<td><!-- 균등 배분을 위한 빈 영역 --></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 6, 8. 중앙 그리드 영역 (상하좌우 중앙 정렬 표준) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
			<div class="row align-items-center w-100">
				<div class="col-md-2 small">조회 품목: <span class="fw-bold text-info">{{ rowCount }}</span> 건</div>
				<div class="col-md-10 text-end">
					<span class="me-4 small opacity-75">당월 매출합계: <span class="fw-bold text-white ms-1">{{ formatNumber(totals.AMTSUM) }}</span></span>
					<span class="me-4 small opacity-75">당월 이익합계: <span class="fw-bold text-info ms-1">{{ formatNumber(totals.PROFSUM) }}</span></span>
					<span class="fs-5 ms-2 fw-light">누계 매출액: <span class="fw-bold text-warning ms-2">{{ formatNumber(totals.AMTSUM_T) }}</span> 원</span>
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
import { useRouter } from 'vue-router'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date();
const currentYY = String(now.getFullYear());
const currentMM = String(now.getMonth() + 1).padStart(2, '0');

// 13. 모든 변수명 대문자 고수
const searchForm = reactive({
	DEPTCD: authStore.DEPTCD,
	DEPTNM: authStore.DEPTNM,
	YY: currentYY,
	MM: currentMM
})

const yearOptions = ref<string[]>(Array.from({ length: 5 }, (_, i) => String(now.getFullYear() - i)));
const monthOptions = ref<string[]>(Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0')));

const rowCount = ref(0)
const totals = reactive({ AMTSUM: 0, COSTSUM: 0, PROFSUM: 0, AMTSUM_T: 0, COSTSUM_T: 0, PROFSUM_T: 0 })
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchData = async () => {
	try {
		const res = await api.post('/api/hsst/HSST_340S_STR', {
			...searchForm,
			CMPYCD: authStore.CMPYCD,
			YM: searchForm.YY + searchForm.MM
		})
		const data = res.data || []
		mainGrid?.setData(data)
		rowCount.value = data.length

		totals.AMTSUM = data.reduce((acc: number, cur: any) => acc + (Number(cur.SALSAMT) || 0), 0)
		totals.COSTSUM = data.reduce((acc: number, cur: any) => acc + (Number(cur.SALSCOST) || 0), 0)
		totals.PROFSUM = totals.AMTSUM - totals.COSTSUM

		totals.AMTSUM_T = data.reduce((acc: number, cur: any) => acc + (Number(cur.SALSAMT_T) || 0), 0)
		totals.COSTSUM_T = data.reduce((acc: number, cur: any) => acc + (Number(cur.SALSCOST_T) || 0), 0)
		totals.PROFSUM_T = totals.AMTSUM_T - totals.COSTSUM_T

		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm);
	searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
	searchForm.YY = currentYY; searchForm.MM = currentMM;
	mainGrid?.clearData(); rowCount.value = 0;
	Object.assign(totals, { AMTSUM: 0, COSTSUM: 0, PROFSUM: 0, AMTSUM_T: 0, COSTSUM_T: 0, PROFSUM_T: 0 });
}

const handleExcel = () => mainGrid?.download("xlsx", "매출총이익명세서.xlsx")
const handlePrint = () => vAlert('인쇄 기능을 준비 중입니다.')

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
	}
	modalVisible.value = true
}

const formatNumber = (val: any) => Number(val || 0).toLocaleString()

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 80 },
			columns: [
				{
					title: "품목명", field: "ITEMNM", minWidth: 200, widthGrow: 2, hozAlign: "left", cssClass: "fw-bold text-primary cursor-pointer", frozen: true,
					cellClick: (e, cell) => {
						const d = cell.getData();
						router.push({ path: '/HSST/HSST350S', query: { DEPTCD: searchForm.DEPTCD, YY: searchForm.YY, MM: searchForm.MM, ITEMCD: d.ITEMCD } });
					}
				},
				{
					title: "당월 실적 (Current Month)",
					columns: [
						{ title: "매출액", field: "SALSAMT", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 } },
						{ title: "매출원가", field: "SALSCOST", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 } },
						{ title: "매총이익", field: "SALSPROF", hozAlign: "right", width: 150, formatter: "money", mutatorData: (v,d) => (Number(d.SALSAMT)||0) - (Number(d.SALSCOST)||0), cssClass: "text-info fw-bold" },
						{ title: "이익률(%)", field: "PROF_RATE", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 1 }, mutatorData: (v,d) => {
							const s = Number(d.SALSAMT)||0;
							return s !== 0 ? ((s - (Number(d.SALSCOST)||0)) / s * 100) : 0;
						}}
					]
				},
				{
					title: "누계 실적 (Cumulative)",
					columns: [
						{ title: "매출액", field: "SALSAMT_T", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 } },
						{ title: "매출원가", field: "SALSCOST_T", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 } },
						{ title: "매총이익", field: "SALSPROF_T", hozAlign: "right", width: 150, formatter: "money", mutatorData: (v,d) => (Number(d.SALSAMT_T)||0) - (Number(d.SALSCOST_T)||0), cssClass: "text-warning fw-bold" },
						{ title: "이익률(%)", field: "PROF_RATE_T", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 1 }, mutatorData: (v,d) => {
							const s = Number(d.SALSAMT_T)||0;
							return s !== 0 ? ((s - (Number(d.SALSCOST_T)||0)) / s * 100) : 0;
						}}
					]
				}
			]
		})
	}
})
</script>

<style scoped>
/* 🎨 폰트 선명도 보정 및 전역 스타일 */
.hsst340s-wrapper {
  height: 100%;
  overflow: hidden;
  font-family: 'Pretendard', sans-serif;
  background-color: #f4f7fa !important;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

.erp-header { background-color: #ffffff !important; }

/* 🎨 원칙 1-4: 시스템 공통 버튼 색상 표준 적용 */
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 4px; border: none; }
.btn-init { background-color: #ffffff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #ffffff !important; }
.btn-excel { background-color: #1d6f42 !important; color: #ffffff !important; }
.btn-print { background-color: #6c757d !important; color: #ffffff !important; }

/* 🎨 원칙 10: 폼 레이블 표준 (연한 회색 배경, 검정 글자) */
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f1f3f5; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 8px !important; color: #212529; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 8px 4px !important; background-color: #fff; vertical-align: middle; }

/* 🎨 원칙 9: 폼 라벨 스타일 */
.erp-label { font-weight: 700; font-size: 12px; color: #212529; min-width: 60px; text-align: right; white-space: nowrap; }

/* 🎨 원칙 5, 6: 그리드 타이틀 색상 및 중앙 정렬 표준 */
:deep(.tabulator) {
  border: 1px solid #dee2e6;
  font-size: 13px;
  color: #212529 !important;
  font-family: 'Pretendard', sans-serif !important;
}
:deep(.tabulator-header) {
  background-color: #f8f9fa !important;
  border-bottom: 2px solid #dee2e6 !important;
}
:deep(.tabulator-col-title) {
  color: #6c757d !important;
  font-weight: 800;
  text-align: center !important;
}
:deep(.tabulator-cell) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 4px !important;
  border-right: 1px solid #eee !important;
}
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }

.erp-footer { background-color: #212529 !important; min-height: 50px; }
</style>
