<!--
	=============================================================
	프로그램명	: 매입할인현황 (Purchase Discount Status)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] ERP 고밀도 표준 및 상하정중앙 정렬 완벽 보정
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio220s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 (표준 버튼 배치 및 색상) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-percent me-2 text-primary" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">매입할인현황</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-print" @click="handlePrint">인쇄</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 영역 (고밀도 표준 적용) -->
		<div class="p-2 pb-0">
			<div class="card border shadow-sm bg-light bg-opacity-50">
				<div class="card-body py-2 px-3">
					<table class="erp-table-form">
						<tbody>
							<tr>
								<th class="border-0 bg-transparent text-end pr-2" style="width: 1%; white-space: nowrap;">할인일자</th>
								<td class="border-0 bg-transparent" style="width: auto;">
									<div class="d-flex align-items-center gap-1">
										<input v-model="searchForm.FRYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
										<span class="text-muted mx-1">~</span>
										<input v-model="searchForm.TOYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📊 3. 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<!-- ✅ 그리드 타이틀 상하 정중앙 배치 -->
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px; min-height: 40px;">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 거래처별 할인 요약 내역
					</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 (선택 정보만 노출) -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">거래처수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 개</div>
				<div class="col-md-9 text-end">
					<span class="me-4 small opacity-75">총 공급가: <span class="fw-bold text-info ms-1">{{ formatNumber(totals.amt) }}</span></span>
					<span class="fs-5 ms-2 fw-light">총 할인금액: <span class="fw-bold text-warning ms-2">{{ formatNumber(totals.hal) }}</span></span>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
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

const now = new Date()
const searchForm = reactive({
  FRYMD: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  TOYMD: now.toISOString().substring(0, 10)
})

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null
const activeItemCount = ref(0)
const totals = reactive({ amt: 0, hal: 0 })

async function fetchList() {
  try {
    const res = await api.post('/api/hsio/HSIO_220S_STR', {
      CMPYCD: authStore.CMPYCD,
      FRYMD: searchForm.FRYMD.replace(/-/g, ''),
      TOYMD: searchForm.TOYMD.replace(/-/g, '')
    })
    const data = res.data.data || []
    mainGrid?.setData(data)
    activeItemCount.value = data.length
    totals.amt = data.reduce((acc: number, cur: any) => acc + (Number(cur.SPYAMT) || 0), 0)
    totals.hal = data.reduce((acc: number, cur: any) => acc + (Number(cur.HALAMT) || 0), 0)
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

function initialize() {
  resetForm(searchForm);
  searchForm.FRYMD = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
  searchForm.TOYMD = now.toISOString().substring(0, 10);
  mainGrid?.clearData(); totals.amt = 0; totals.hal = 0; activeItemCount.value = 0;
}

const handlePrint = () => { vAlert('인쇄 기능을 준비 중입니다.') }
const formatNumber = (val: any) => Number(val || 0).toLocaleString()

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 100 },
			columns: [
				{ title: '거래처', field: 'CUSTNM', minWidth: 200, widthGrow: 2, cssClass: 'fw-bold text-dark' },
				{ title: '품목수', field: 'ITMCNT', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '공급가', field: 'SPYAMT', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '할인금액', field: 'HALAMT', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'text-danger fw-bold' }
			]
		})
	}
    fetchList()
})
</script>

<style scoped>
.hsio220s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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

