<!--수입관리/수입단가추이현황-->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-1 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 13px;">
				<i class="bi bi-graph-up-arrow me-2 text-primary"></i>
				수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입단가추이현황</span>
			</div>
			<div class="btn-group-erp pe-2">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchTrendList">조회</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 -->
		<table class="erp-table-full border-bottom" style="table-layout: fixed;">
			<colgroup>
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
			</colgroup>
			<tbody>
				<tr>
					<th class="required border-end text-nowrap">수입부서</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.deptcd" type="text" class="form-control bg-light" style="max-width: 60px;" readonly />
							<input v-model="searchForm.deptnm" type="text" class="form-control" />
							<button class="btn btn-outline-secondary px-2" @click="openDeptPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="required border-end text-nowrap">수입연도</th>
					<td>
						<select v-model="searchForm.yy" class="form-select form-select-sm ms-1" style="width: 120px;">
							<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
						</select>
					</td>
					<td colspan="4" class="bg-light text-center small text-muted">
						※ 수입 연도별 월별 단가 및 금액 추이를 분석합니다.
					</td>
				</tr>
			</tbody>
		</table>

		<!-- 📊 3. 추이 분석 그리드 -->
		<div class="flex-grow-1 overflow-hidden bg-white d-flex flex-column">
			<div class="grid-title py-1 px-3 border-bottom fw-bold small text-primary d-flex justify-content-between align-items-center bg-light">
				<span><i class="bi bi-table me-1"></i> 월별 품목 수입 추이 데이터</span>
				<span class="badge bg-primary-subtle text-primary border border-primary-subtle px-2">단위: 원화(KRW)</span>
			</div>
			<div ref="mainGridRef" class="tabulator-full-height border-top" />
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const currentYear = new Date().getFullYear()
const searchForm = reactive({
	deptcd: authStore.deptcd, deptnm: authStore.deptnm,
	yy: currentYear.toString()
})

const yearOptions = ref<string[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const initYearOptions = () => {
	const years = []
	for (let i = 0; i < 6; i++) { years.push((currentYear - i).toString()) }
	yearOptions.value = years
}

async function fetchTrendList() {
	try {
		// 🛠️ [경로 수정] /api/hsip/list -> /api/hsip/HSIP_210S_STR (POST)
		const res = await api.post('/api/hsip/HSIP_210S_STR', {
			...searchForm,
			cmpycd: authStore.cmpycd
		})
		mainGrid?.setData(res.data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const openDeptPopup = () => { vAlert('부서 팝업 준비 중') }

function initialize() {
	resetForm(searchForm)
	searchForm.yy = currentYear.toString()
	mainGrid?.clearData()
}

onMounted(async () => {
	initYearOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', minWidth: 70 },
			columns: [
				{ title: '품목 명칭', field: 'itemnm', minWidth: 250, widthGrow: 1, cssClass: 'fw-bold text-dark border-end' },
				{ title: '합 계', field: 'amtsum', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'bg-info-subtle fw-bold' },
				{ title: '1월', field: 'amt01', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '2월', field: 'amt02', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '3월', field: 'amt03', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '4월', field: 'amt04', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '5월', field: 'amt05', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '6월', field: 'amt06', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '7월', field: 'amt07', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '8월', field: 'amt08', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '9월', field: 'amt09', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '10월', field: 'amt10', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '11월', field: 'amt11', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '12월', field: 'amt12', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } }
			]
		})
	}
	fetchTrendList()
})
</script>
