<!--	=============================================================
	프로그램명	: 월별 감가상각명세서
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 계정과목별 개별 자산의 월별 감가상각 현황 조회 (디자인 표준화 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 (표준 규격 및 함수명 통일) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-range me-2 text-primary" style="font-size: 18px;"></i>
				고정자산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">월별 감가상각명세서 (HAFA140S)</span>
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
							<col style="width: 80px;" /><col style="width: 250px;" />
							<col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">계정과목</th>
								<td>
									<div class="input-group">
										<input v-model="searchForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT')" placeholder="계정 선택" />
										<button class="btn" @click="openHelp('ACCT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required">기준연월</th>
								<td>
									<div class="d-flex align-items-center gap-1 px-1">
										<select v-model="searchForm.yy" class="form-select" style="width: 100px;" @change="updateHeader">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
										</select>
										<select v-model="searchForm.mm" class="form-select" style="width: 80px;" @change="updateHeader">
											<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
										</select>
										<span class="small fw-bold ms-1 text-secondary">현재</span>
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

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useTabStore } from '@/stores/tabStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const tabStore = useTabStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({ acctcd: '', acctnm: '', yy: String(currentYear),.mm: currentMonth })
const monthTitles = ref<string[]>(Array(12).fill(''))
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const updateHeader = async () => {
	try {
		const res = await api.post('/api/hafa/HA00_150S_STR', { cmpycd: authStore.cmpycd, baseym: searchForm.yy + searchForm.mm })
		if (res.data && res.data[0]) {
			const header = res.data[0]
			monthTitles.value = Array.from({ length: 12 }, (_, i) => {
				const val = header['COL' + i] || ''; return val ? val.substring(4, 6) + '월' : `${i + 1}월`
			})
			if (mainGrid) {
				const columns = mainGrid.getColumns()
				monthTitles.value.forEach((title, i) => { columns[i + 2].updateDefinition({ title }) })
			}
		}
	} catch (e) { console.error('Header fetch error', e) }
}

async function search() {
	if (!searchForm.acctcd) return vAlertError('계정과목을 선택하세요.')
	try {
		await updateHeader()
		const res = await api.post('/api/hafa/HAFA_140S_STR', { cmpycd: authStore.cmpycd, baseym: searchForm.yy + searchForm.mm, acctcd: searchForm.acctcd })
		const data = (res.data || []).map((row: any) => {
			const months = Array.from({ length: 12 }, (_, i) => Number(row['COL' + (i + 2)] || 0))
			return { asetcd: row.col0, asetnm: row.col1, ...Object.fromEntries(months.map((m, i) => [`M${i + 1}`, m])), TOTAL: months.reduce((a, b) => a + b, 0), acctcd: searchForm.acctcd }
		})
		mainGrid?.setData(data); vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

function excel() { mainGrid?.download("xlsx", `월별감가상각명세서_${searchForm.acctnm}_${searchForm.yy}${searchForm.mm}.xlsx`) }
function print() { const params = `yy=${searchForm.yy}.mm=${searchForm.mm}&acctcd=${searchForm.acctcd}&acctnm=${searchForm.acctnm}&PRTGU=1`; window.open(`/api/hafa/HAFA_140P?${params}`, 'MonthlyDepreciation', 'width=1000,height=800,scrollbars=yes') }

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
	if (type === 'ACCT') {
		Object.assign(modalProps, { title: '계정과목 선택', path: '/api/ha00/HELP_acctcd_LTD_ETC_STR', data: { acctgbn: '020', search: searchForm.acctnm }, columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 200 }],
			onConfirm: (d: any) => { searchForm.acctcd = d.acctcd; searchForm.acctnm = d.acctnm; search() }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		const monthCols = Array.from({ length: 12 }, (_, i) => ({ title: `${i + 1}월`, field: `M${i + 1}`, width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" }))
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "자산코드", field: "asetcd", width: 80, hozAlign: "center", frozen: true, cssClass: "text-primary fw-bold" },
				{ title: "자산명", field: "asetnm", width: 150, frozen: true, cssClass: "fw-bold" },
				...monthCols,
				{ title: "합 계", field: "TOTAL", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", cssClass: "fw-bold text-primary bg-light" }
			]
		})
	}
})
</script>
