<!--
	=============================================================
	프로그램명	: 배송자별 상차현황 (Loading Status by Delivery) [디자인 원칙 13가지 완벽 준수]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 배송담당자별/일자별 상차 내역 및 집계 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio660s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1, 11, 12. 상단 액션 바: 버튼 그룹 우측 상단 정렬 및 표준 색상 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-truck me-2 text-primary" style="font-size: 18px;"></i>
				영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				출고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">배송자별 상차현황 (HSIO660S)</span>
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
						<col style="width: 50%;" />
						<col style="width: 50%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">배송담당</span>
									<div class="input-group input-group-sm flex-nowrap" style="max-width: 300px;">
										<input v-model="searchForm.TRNEMP" type="text" class="form-control text-center bg-white" style="max-width: 80px;" readonly />
										<input v-model="searchForm.TRNEMPNM" type="text" class="form-control" placeholder="담당자 선택" @keyup.enter="openHelp('TRNEMP')" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('TRNEMP')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">출고일자</span>
									<div class="d-flex align-items-center gap-1" style="max-width: 200px;">
										<input v-model="searchForm.OUTYMD" type="date" class="form-control form-control-sm" />
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 6. 중앙 그리드 영역 (상하좌우 중앙 정렬 표준) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 상차 명세 리스트
					</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">조회 품목: <span class="fw-bold text-info">{{ rowCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="fs-5 ms-2 fw-light">총 상차수량: <span class="fw-bold text-warning ms-2">{{ formatNumber(totalQty) }}</span></span>
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
	TRNEMP: authStore.USERID,
	TRNEMPNM: authStore.USERNM,
	OUTYMD: new Date().toISOString().substring(0, 10)
})

const rowCount = ref(0)
const totalQty = ref(0)
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchData = async () => {
	try {
		const res = await api.post('/api/hsio/HSIO_660S_STR', {
			...searchForm,
			CMPYCD: authStore.CMPYCD,
			OUTYMD: searchForm.OUTYMD.replace(/-/g, '')
		})
		const data = res.data || []
		mainGrid?.setData(data)
		rowCount.value = data.length
		totalQty.value = data.reduce((acc: number, cur: any) => acc + (Number(cur.QTY) || 0), 0)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm);
	searchForm.TRNEMP = authStore.USERID;
	searchForm.TRNEMPNM = authStore.USERNM;
	searchForm.OUTYMD = new Date().toISOString().substring(0, 10);
	mainGrid?.clearData(); rowCount.value = 0; totalQty.value = 0;
}

const handleExcel = () => mainGrid?.download("xlsx", "배송자별상차현황.xlsx")
const handlePrint = () => vAlert('인쇄 기능을 준비 중입니다.')

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'TRNEMP') {
		Object.assign(modalProps, {
			title: '배송담당자 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM',
			data: { GUBUN: 'U1', CMPYCD: authStore.CMPYCD },
			columns: [{ title: '코드', field: 'CODE', width: 100 }, { title: '성명', field: 'CDNM', width: 200 }],
			onConfirm: (d: any) => { searchForm.TRNEMP = d.CODE; searchForm.TRNEMPNM = d.CDNM }
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
				{ title: "품목명", field: "ITEMNM", minWidth: 250, widthGrow: 2, hozAlign: "left", cssClass: "fw-bold" },
				{ title: "규격", field: "ITSIZE", width: 150, hozAlign: "left" },
				{ title: "단위", field: "UNIT", width: 80 },
				{ title: "배송지역", field: "AREANM", width: 180 },
				{ title: "수량", field: "QTY", hozAlign: "right", width: 110, formatter: "money", formatterParams: { precision: (c:any)=>c.getData().QTYPNT || 0 } },
				{ title: "비고", field: "REMARK", minWidth: 200, hozAlign: "left" }
			],
			groupBy: "ITEMNM", // ASP의 소계 로직을 위해 품목별 그룹화 적용
			groupHeader: function(value, count, data, group){
				return value + " (합계: " + data.reduce((a, b) => a + Number(b.QTY), 0).toLocaleString() + " 건)";
			},
		})
	}
})
</script>

<style scoped>
/* 🎨 폰트 선명도 보정 및 전역 스타일 */
.hsio660s-wrapper {
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
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }

/* 🎨 원칙 9: 폼 라벨 스타일 */
.erp-label { font-weight: 700; font-size: 12px; color: #212529; min-width: 80px; text-align: right; white-space: nowrap; }

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

/* 그룹화 헤더 스타일 */
:deep(.tabulator-group) {
  background-color: #f1f5f9 !important;
  font-weight: 800 !important;
  color: #005a9f !important;
}
</style>
