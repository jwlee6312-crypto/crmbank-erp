<!--	=============================================================
	프로그램명	: 감가상각비 집계표
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 부서별/계정과목별 감가상각비 발생 현황 집계 조회 (디자인 표준화 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 (표준 규격 및 함수명 통일) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
				고정자산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">감가상각비 집계표 (HAFA130S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-print" @click="print">인쇄</button>
				<button class="btn-erp btn-excel" @click="excel">엑셀</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 영역 (표준 erp-table-full 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-0 bg-light">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 80px;" /><col style="width: 250px;" />
							<col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">기준연월</th>
								<td class="bg-white">
									<div class="d-flex align-items-center gap-1 px-1">
										<select v-model="searchForm.yy" class="form-select" style="width: 100px;">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
										</select>
										<select v-model="searchForm.mm" class="form-select" style="width: 80px;">
											<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
										</select>
										<span class="small fw-bold ms-2 text-secondary">분 감가상각 집계</span>
									</div>
								</td>
								<td class="bg-white"></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📊 3. 그리드 영역 (표준 통합CSS 적용) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({ yy: String(currentYear),.mm: currentMonth })
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

async function search() {
	try {
		const res = await api.post('/api/hafa/HAFA_130S_STR', { cmpycd: authStore.cmpycd, baseym: searchForm.yy + searchForm.mm })
		mainGrid?.setData(res.data || []); vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

function excel() { mainGrid?.download("xlsx", `감가상각비집계표_${searchForm.yy}${searchForm.mm}.xlsx`) }
function print() { const params = `yy=${searchForm.yy}.mm=${searchForm.mm}&PRTGU=1`; window.open(`/api/hafa/HAFA_130P?${params}`, 'DepreciationSummary', 'width=1000,height=800,scrollbars=yes') }

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "부서코드", field: "deptcd", width: 100, hozAlign: "center" },
				{ title: "부서명", field: "deptnm", width: 150 },
				{ title: "계정코드", field: "acctcd", width: 100, hozAlign: "center" },
				{ title: "계정과목명", field: "acctnm", width: 180, cssClass: "fw-bold" },
				{ title: "기초가액", field: "baseamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "당기증가", field: "incramt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "당기상각액", field: "dprsamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", cssClass: "text-primary fw-bold" },
				{ title: "상각누계액", field: "dprssum", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "미상각잔액", field: "janamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", cssClass: "fw-bold" }
			]
		})
	}
})
</script>
