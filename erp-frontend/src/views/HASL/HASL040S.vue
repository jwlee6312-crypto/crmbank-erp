<!--
	=============================================================
	프로그램명	: 자동분개전표
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 활동별 자동 분개 유형 및 내역 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi- lightning-charge me-2 text-primary" style="font-size: 18px;"></i>
				전표관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">자동분개전표 (HASL040S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
			</div>
		</div>

		<!-- 🔍 검색 항목 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 30%;" />
						<col style="width: 50%;" />
						<col style="width: 20%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">검색대상</span>
									<select v-model="searchForm.searchGBN" class="form-select form-select-sm">
										<option value="100">분개유형</option>
										<option value="200">분개전표</option>
									</select>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">활&nbsp;&nbsp;&nbsp;&nbsp;동</span>
									<input v-model="searchForm.KEYWORD" type="text" class="form-control form-control-sm"
										placeholder="예: 상품을 구입하고 현금 지불" @keydown.enter="search" ref="keywordInput" />
								</div>
							</td>
							<td>
								<div class="px-2 text-muted small">
									(검색어를 입력하고 조회를 누르세요)
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 중앙 리스트/그리드 영역 -->
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
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const keywordInput = ref<HTMLInputElement | null>(null)

const searchForm = reactive({
	searchGBN: '100',
	KEYWORD: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/hasl/HASL_040S_STR', {
			cmpycd: authStore.cmpycd,
			searchGBN: searchForm.searchGBN,
			KEYWORD: searchForm.KEYWORD
		})

		const data = res.data || []
		mainGrid?.setData(data)

		if (data.length === 0) {
			vAlert('데이터가 존재하지 않습니다.')
		} else {
			vAlert('조회되었습니다.')
		}
	} catch (e) {
		vAlertError('조회 중 오류가 발생했습니다.')
	}
}

const initialize = () => {
	searchForm.searchGBN = '100'
	searchForm.KEYWORD = ''
	mainGrid?.clearData()
	keywordInput.value?.focus()
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: "JURNCD",
			groupHeader: (value, count, data: any) => {
				const firstRow = data[0]
				const costGbn = firstRow.COSTGBNM ? `[${firstRow.COSTGBNM}] ` : ''
				const remark = firstRow.remark ? `<div class='text-muted fw-normal small mt-1'>설명: ${firstRow.remark.replace(/\n/g, '<br>')}</div>` : ''

				return `
					<div class='d-flex flex-column w-100 py-1'>
						<div class='d-flex justify-content-between align-items-center'>
							<span class='text-primary fw-bold cursor-pointer' onclick='window.dispatchEvent(new CustomEvent("go-detail", {detail: {jurncd: "${value}", searchgbn: "${searchForm.searchGBN}"}}))'>
								<i class="bi bi-check2-circle me-1"></i> ${costGbn}${firstRow.JURNNM}
							</span>
							<span class='badge bg-secondary opacity-75'>${count} lines</span>
						</div>
						${remark}
					</div>
				`
			},
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: [
				{ title: "No", field: "JURNNO", width: 60 },
				{
					title: "구분", field: "DBCR", width: 100,
					formatter: (cell) => {
						const val = cell.getValue()
						return val === 'D' ? '<span class="text-primary fw-bold">차변</span>' : '<span class="text-danger fw-bold">대변</span>'
					}
				},
				{ title: "계정코드", field: "acctcd", width: 120 },
				{ title: "계정과목명", field: "acctnm", hozAlign: "left", minWidth: 200 },
				{ title: "JURNCD", field: "JURNCD", visible: false }
			],
			placeholder: "조회된 데이터가 없습니다."
		})

		// 그룹 헤더 클릭 이벤트를 처리하기 위한 리스너
		window.addEventListener("go-detail", ((e: CustomEvent) => {
			router.push({
				path: '/HASL/HASL050U',
				query: { JURNCD: e.detail.jurncd, searchGBN: e.detail.searchgbn }
			})
		}) as EventListener)
	}

	keywordInput.value?.focus()
})
</script>

<style scoped>
.erp-label {
	min-width: 70px;
	font-weight: 500;
	color: #495057;
}

:deep(.tabulator-group) {
	background-color: #f8f9fa !important;
	border-top: 1px solid #dee2e6 !important;
}

:deep(.tabulator-group-header) {
	padding: 8px 12px !important;
}

:deep(.tabulator-group span) {
	display: inline-block;
}
</style>
