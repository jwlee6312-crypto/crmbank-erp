<!--
	=============================================================
	프로그램명	  : 제조경비 배부명세서
    프로그램 ID	: HFMF215S
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 제조경비 배부명세서 (FMF2150R_STR 연결 - 부서팝업 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<!-- 💡 세련된 부서 도움창 적용 -->
	<DeptHelpModal :visible="deptHelpVisible" :cmpycd="authStore.CMPYCD" @close="deptHelpVisible = false" @confirm="onSelectDept" />

	<div class="hfmf215s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-spreadsheet me-2 text-primary" style="font-size: 18px;"></i>
				원가관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">제조경비 배부명세서 (HFMF215S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="handleSearch">조회</button>
				<button class="btn-erp btn-outline-success px-3" style="border: 1px solid #198754 !important;" @click="handleExport">Excel</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm overflow-hidden">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px"><col style="width: 250px">
							<col style="width: 100px"><col>
						</colgroup>
						<tbody>
							<tr>
								<th>마감년월</th>
								<td>
									<input v-model="searchForm.YM" type="month" class="form-control form-control-sm w-75" @change="handleSearch" />
								</td>
								<th>부서선택</th>
								<td>
									<div class="input-group input-group-sm w-50">
										<input v-model="searchForm.DEPTCD" class="form-control text-center bg-light" style="max-width: 100px;" placeholder="코드" readonly />
										<input v-model="searchForm.DEPTNM" class="form-control bg-light" placeholder="전체 (선택 안할 시)" readonly @click="openDeptHelp" />
										<button class="btn btn-outline-secondary" type="button" @click="openDeptHelp"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 🅱️ 데이터 그리드 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden border-top border-3 border-primary">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 공정별 배부 명세</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" class="tabulator-full-height" />
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import AppAlert from '@/components/AppAlert.vue'
import DeptHelpModal from '@/components/DeptHelpModal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlertError } = useAlerts()

const searchForm = reactive({
	YM: new Date().toISOString().substring(0, 7),
	DEPTCD: '',
	DEPTNM: ''
})

const deptHelpVisible = ref(false)
const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null

const openDeptHelp = () => { deptHelpVisible.value = true }
const onSelectDept = (data: any) => {
	searchForm.DEPTCD = String(data.DEPTCD || '').trim()
	searchForm.DEPTNM = String(data.DEPTNM || '').trim()
	deptHelpVisible.value = false
	handleSearch()
}

const handleSearch = async () => {
	try {
		const ym = searchForm.YM.replace('-', '')
		console.log(`📋 [SSMS Query] EXEC FMF2150R_STR '${authStore.CMPYCD}', '${ym}', '${searchForm.DEPTCD}'`)

		// 💡 LIMIT 파라미터 삭제 및 3개 규격 준수
		const { data } = await api.post('/api/hfmf/FMF2150R_STR', {
			CMPYCD: String(authStore.CMPYCD),
			YYMM: String(ym),
			DEPTCD: String(searchForm.DEPTCD)
		})

		mainGrid?.setData(data)
	} catch (e) {
		vAlertError('데이터 조회 실패')
	}
}

const handleExport = () => {
	mainGrid?.download('xlsx', `제조경비배부명세서_${searchForm.YM}.xlsx`)
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerHozAlign: 'center', headerSort: false },
			columns: [
				{ title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
				{ title: '계정과목', field: 'ACCTNM', width: 200, frozen: true, hozAlign: 'left' },
				{ title: '가공공정', field: 'AMT1', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '조립공정', field: 'AMT2', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '포장공정', field: 'AMT3', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '공정4', field: 'AMT4', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '공정5', field: 'AMT5', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '공정6', field: 'AMT6', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '공정7', field: 'AMT7', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '합계', field: 'SUMAMT', width: 120, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'fw-bold text-primary bg-light' }
			]
		})
	}
	handleSearch()
})
</script>

<style scoped>
.hfmf215s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; border: none; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
</style>
