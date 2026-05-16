<!--
	=============================================================
	프로그램명	: 매입미정산현황 (Purchase Non-Settlement Status)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] 다단 그리드 헤더 + HSOD100U 스타일 표준 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio690s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 (표준 버튼 배치) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-x me-2 text-primary" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">매입미정산현황</span>
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
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 1%; white-space: nowrap;"><col style="width: 24%">
							<col style="width: 1%; white-space: nowrap;"><col style="width: 74%">
						</colgroup>
						<tbody>
							<tr>
								<th class="required">입고부서</th>
								<td>
									<div class="input-group input-group-sm" style="width: 250px;">
										<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.DEPTNM" type="text" class="form-control" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required">입고일자</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<input v-model="searchForm.FYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
										<span class="text-muted mx-1">~</span>
										<input v-model="searchForm.TYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📊 3. 그리드 영역 (다단 헤더 표준화) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px; min-height: 40px;">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 거래처별 입고/정산/미정산 요약 현황
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
				<div class="col-md-3 small">거래처 수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 개</div>
				<div class="col-md-9 text-end">
					<span class="me-4 small opacity-75">미정산 공급가: <span class="fw-bold text-info ms-1">{{ formatNumber(totals.amt) }}</span></span>
					<span class="fs-5 ms-2 fw-light">총 미정산 합계: <span class="fw-bold text-warning ms-2">{{ formatNumber(totals.sum) }}</span></span>
				</div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
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
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const searchForm = reactive({
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  FYMD: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  TYMD: now.toISOString().substring(0, 10)
})

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null
const activeItemCount = ref(0)
const totals = reactive({ amt: 0, sum: 0 })

async function fetchList() {
  try {
    const res = await api.post('/api/hsio/HSIO_690S_STR', {
      ...searchForm,
      CMPYCD: authStore.CMPYCD,
      FYMD: searchForm.FYMD.replace(/-/g, ''),
      TYMD: searchForm.TYMD.replace(/-/g, '')
    })
    const data = res.data.data || []
    mainGrid?.setData(data)
    activeItemCount.value = data.length
    totals.amt = data.reduce((acc: number, cur: any) => acc + (Number(cur.NAMT) || 0), 0)
    totals.sum = data.reduce((acc: number, cur: any) => acc + (Number(cur.NAMT) + Number(cur.NVAT) || 0), 0)
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

function initialize() {
  resetForm(searchForm);
  searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
  searchForm.FYMD = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
  searchForm.TYMD = now.toISOString().substring(0, 10);
  mainGrid?.clearData(); totals.amt = 0; totals.sum = 0;
}

const handlePrint = () => { vAlert('인쇄 기능을 준비 중입니다.') }
const formatNumber = (val: any) => Number(val || 0).toLocaleString()
const openHelp = (type: string) => { /* 팝업 구현 */ }
const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 80 },
			columns: [
				{ title: '거래처 명', field: 'CUSTNM', minWidth: 180, widthGrow: 1.5, cssClass: 'fw-bold text-dark', frozen: true },
				{
					title: '입고 정보',
					columns: [
						{ title: '수량', field: 'OQTY', hozAlign: 'right', width: 100, formatter: 'money' },
						{ title: '공급가', field: 'OAMT', hozAlign: 'right', width: 120, formatter: 'money' },
						{ title: '부가세', field: 'OVAT', hozAlign: 'right', width: 120, formatter: 'money' },
						{ title: '합계', field: 'OSUM', hozAlign: 'right', width: 120, formatter: 'money', mutatorData: (v, d) => Number(d.OAMT || 0) + Number(d.OVAT || 0) }
					]
				},
				{
					title: '정산 내역',
					columns: [
						{ title: '수량', field: 'JQTY', hozAlign: 'right', width: 100, formatter: 'money' },
						{ title: '공급가', field: 'JAMT', hozAlign: 'right', width: 120, formatter: 'money' },
						{ title: '부가세', field: 'JVAT', hozAlign: 'right', width: 120, formatter: 'money' },
						{ title: '합계', field: 'JSUM', hozAlign: 'right', width: 120, formatter: 'money', mutatorData: (v, d) => Number(d.JAMT || 0) + Number(d.JVAT || 0) }
					]
				},
				{
					title: '미정산 잔액',
					columns: [
						{ title: '수량', field: 'NQTY', hozAlign: 'right', width: 100, formatter: 'money', cssClass: 'text-danger' },
						{ title: '공급가', field: 'NAMT', hozAlign: 'right', width: 120, formatter: 'money', cssClass: 'text-danger' },
						{ title: '부가세', field: 'NVAT', hozAlign: 'right', width: 120, formatter: 'money', cssClass: 'text-danger' },
						{ title: '합계', field: 'NSUM', hozAlign: 'right', width: 120, formatter: 'money', cssClass: 'fw-bold text-danger bg-light',
						  mutatorData: (v, d) => Number(d.NAMT || 0) + Number(d.NVAT || 0) }
					]
				}
			]
		})
	}
    fetchList()
})
</script>

<style scoped>
.hsip100u-wrapper { height: 100%; overflow: hidden; }
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
