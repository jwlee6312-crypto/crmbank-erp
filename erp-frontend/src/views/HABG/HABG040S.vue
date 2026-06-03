<!--
	=============================================================
	프로그램명	: 예산조정서
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 예산 연도 및 부서별 월별 예산 조정 내역 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
				예산관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">예산조정서 (HABG040S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>예산연도</span>
							<select v-model="searchForm.bugtyy" class="form-select form-select-sm" style="width: 110px;">
								<option v-for="year in yearOptions" :key="year" :value="year">{{ year }} 년</option>
							</select>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>예산부서</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 65px;" readonly />
								<input v-model="searchForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" placeholder="부서 선택" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
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
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

// 연도 옵션 설정 (ASP 로직: 현재년도+1 ~ 현재년도-3)
const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 5 }, (_, i) => String(currentYear + 1 - i))

const searchForm = reactive({
	bugtyy: String(currentYear),
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || ''
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.bugtyy) return vAlertError('예산연도를 선택하세요.')
	if (!searchForm.deptcd) return vAlertError('예산부서코드를 선택하세요.')

	try {
		const res = await api.post('/api/habg/HABG_040S_STR', {
			cmpycd: authStore.cmpycd,
			bugtyy: searchForm.bugtyy,
			deptcd: searchForm.deptcd
		})

		const data = (res.data || []).map((row: any) => {
			const m01 = Number(row.col2 || 0); const m02 = Number(row.col3 || 0);
			const m03 = Number(row.col4 || 0); const m04 = Number(row.col5 || 0);
			const m05 = Number(row.col6 || 0); const m06 = Number(row.col7 || 0);
			const m07 = Number(row.col8 || 0); const m08 = Number(row.COL9 || 0);
			const m09 = Number(row.col10 || 0); const m10 = Number(row.col11 || 0);
			const m11 = Number(row.col12 || 0); const m12 = Number(row.col13 || 0);

			return {
				bugtcd: row.col0,
				bugtnm: row.col1,
				M01: m01, M02: m02, M03: m03, M04: m04, M05: m05, M06: m06,
				M07: m07, M08: m08, M09: m09, M10: m10, M11: m11, M12: m12,
				TOTAL: m01 + m02 + m03 + m04 + m05 + m06 + m07 + m08 + m09 + m10 + m11 + m12
			}
		})

		mainGrid?.setData(data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 오류가 발생했습니다.') }
}

const initialize = () => {
	searchForm.bugtyy = String(currentYear)
	searchForm.deptcd = authStore.deptcd
	searchForm.deptnm = authStore.deptnm
	mainGrid?.clearData()
}

const print = () => {
	const params = `bugtyy=${searchForm.bugtyy}&deptcd_H=${searchForm.deptcd}&deptnm_H=${searchForm.deptnm}`
	window.open(`/api/habg/HABG_040P?${params}`, 'BudgetAdjustmentPrint', 'width=1000,height=800,scrollbars=yes')
}

const excel = () => {
	mainGrid?.download("xlsx", `예산조정서_${searchForm.bugtyy}_${searchForm.deptnm}.xlsx`)
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: 'DEPT') {
    Object.assign(modalProps, {
        title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
        data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: searchForm.deptnm },
        columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
        onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search() }
    })
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
			columns: [
				{ title: "예산코드", field: "bugtcd", width: 90, hozAlign: "center", formatter: "plaintext", frozen: true },
				{ title: "예산명", field: "bugtnm", width: 150, hozAlign: "left", formatter: "plaintext", frozen: true },
				{
					title: "상반기",
					columns: [
						{ title: "1월", field: "M01", width: 80 },
						{ title: "2월", field: "M02", width: 80 },
						{ title: "3월", field: "M03", width: 80 },
						{ title: "4월", field: "M04", width: 80 },
						{ title: "5월", field: "M05", width: 80 },
						{ title: "6월", field: "M06", width: 80 },
					]
				},
				{
					title: "하반기",
					columns: [
						{ title: "7월", field: "M07", width: 80 },
						{ title: "8월", field: "M08", width: 80 },
						{ title: "9월", field: "M09", width: 80 },
						{ title: "10월", field: "M10", width: 80 },
						{ title: "11월", field: "M11", width: 80 },
						{ title: "12월", field: "M12", width: 80 },
					]
				},
				{ title: "합계", field: "TOTAL", width: 110, cssClass: "bg-light fw-bold text-primary" }
			],
			columnCalcs: "both",
			// 하단 합계 설정
			footerFormatter: (column: any) => {
				if (column.getField() === 'TOTAL') return "Total Sum"
				return ""
			}
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 70px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; font-size: 12px; }
:deep(.tabulator-header .tabulator-col-group) { border-bottom: 1px solid #dee2e6 !important; }
:deep(.bg-light) { background-color: #f1f5f9 !important; }
</style>
