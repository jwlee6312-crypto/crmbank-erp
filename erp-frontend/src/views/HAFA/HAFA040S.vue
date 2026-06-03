<!--	=============================================================
	프로그램명	: 고정자산대장
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 계정과목별 고정자산 마스터 및 상각/처분 현황 조회 (디자인 표준화 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 (표준 규격 및 함수명 통일) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-book me-2 text-primary" style="font-size: 18px;"></i>
				고정자산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">고정자산대장 (HAFA040S)</span>
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
								<th>기준연월</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<select v-model="searchForm.yy" class="form-select" style="width: 100px;">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
										</select>
										<select v-model="searchForm.mm" class="form-select" style="width: 80px;">
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
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

async function search() {
	if (!searchForm.acctcd) return vAlertError('계정과목을 선택하세요.')
	try {
		const res = await api.post('/api/hafa/HAFA_040S_STR', { cmpycd: authStore.cmpycd, baseym: searchForm.yy + searchForm.mm, acctcd: searchForm.acctcd })
		const data = (res.data || []).map((row: any) => {
			const pAmt = Number(row.col6 || 0); const bAmt = Number(row.col18 || 0); const sSum = Number(row.col21 || 0)
			return { acctcd: row.col0, acctnm: row.col1, asetcd: row.col2, asetnm: row.col3, pchymd: row.col4, pchymd_disp: row.col4 ? `${row.col4.slice(2,4)}.${row.col4.slice(4,6)}.${row.col4.slice(6,8)}` : '', pchqty: row.col5, pchamt: pAmt, dprstypeNM: row.col8, legalyy: row.COL9, asetrate: row.col10, deptnm: row.col12, costtypeNM: row.col14, dspymd: (row.col15 && row.col15 > '00000000') ? `${row.col15.slice(2,4)}.${row.col15.slice(4,6)}.${row.col15.slice(6,8)}` : '', DSPQTY: row.col16, dspamt: Number(row.col17 || 0), baseamt: bAmt, incramt: Number(row.col19 || 0), dprsamt: Number(row.col20 || 0), dprssum: sSum, janamt: bAmt === 0 ? pAmt - sSum : bAmt - sSum }
		})
		mainGrid?.setData(data); vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

function excel() { mainGrid?.download("xlsx", `고정자산대장_${searchForm.acctnm}_${searchForm.yy}${searchForm.mm}.xlsx`) }
function print() { const params = `yy=${searchForm.yy}.mm=${searchForm.mm}&acctcd=${searchForm.acctcd}&acctnm=${searchForm.acctnm}&PRTGU=1`; window.open(`/api/hafa/HAFA_040P?${params}`, 'FixedAssetLedger', 'width=1000,height=800,scrollbars=yes') }

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
	if (type === 'ACCT') {
		Object.assign(modalProps, { title: '계정과목 선택', path: '/api/ha00/HELP_ACCTCD_LTD_ETC_STR', data: { acctgbn: '020', search: searchForm.acctnm }, columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 200 }],
			onConfirm: (d: any) => { searchForm.acctcd = d.acctcd; searchForm.acctnm = d.acctnm; search() }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "자산정보", columns: [
					{ title: "코드", field: "asetcd", width: 70, hozAlign: "center" },
					{ title: "자산명", field: "asetnm", width: 150, cssClass: "text-primary fw-bold" },
					{ title: "취득일", field: "pchymd_disp", width: 80, hozAlign: "center" },
					{ title: "수량", field: "pchqty", width: 60, hozAlign: "center" },
					{ title: "취득가액", field: "pchamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				]},
				{ title: "상각정보", columns: [
					{ title: "방법", field: "dprstypeNM", width: 80, hozAlign: "center" },
					{ title: "연수", field: "legalyy", width: 60, hozAlign: "center" },
					{ title: "율", field: "asetrate", width: 60, hozAlign: "center" },
					{ title: "기초가액", field: "baseamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				]},
				{ title: "변동 및 잔액", columns: [
					{ title: "당기증가", field: "incramt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
					{ title: "당기상각", field: "dprsamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
					{ title: "상각누계", field: "dprssum", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
					{ title: "미상각잔액", field: "janamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", cssClass: "fw-bold text-primary" },
				]},
				{ title: "기타", columns: [
					{ title: "보유부서", field: "deptnm", width: 120 },
					{ title: "처분일", field: "dspymd", width: 80, hozAlign: "center", cssClass: "text-danger" },
					{ title: "처분금액", field: "dspamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				]}
			]
		})
	}
})
</script>
