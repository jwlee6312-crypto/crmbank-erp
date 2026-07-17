<!--
	=============================================================
	?�로그램�?: 받을?�음명세??
	?�성?�자	: 2025.02.24
	?�성??    : AI Assistant
	?�명        : 만기?�별 받을?�음???�세 ?�역 �??�액 ?�황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?�단 ?�션 �?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-text me-2 text-primary" style="font-size: 18px;"></i>
				?�금관�?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">받을?�음명세??(HAFN410S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기??
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> ?�쇄
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> ?��?
				</button>
			</div>
		</div>

		<!-- ?�� 검??조건 ?�역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">만기??/th>
							<td class="bg-white border-end">
								<div class="d-flex align-items-center gap-1">
									<input v-model="searchForm.ymd_fr" type="date" class="form-control form-control-sm" />
									<span class="text-muted">~</span>
									<input v-model="searchForm.ymd_to" type="date" class="form-control form-control-sm" />
								</div>
							</td>
							<th class="text-center border-end">??nbsp;&nbsp;??/th>
							<td class="bg-white">
								<select v-model="searchForm.billtype" class="form-select form-select-sm" style="max-width: 150px;">
									<option value="000">?�체</option>
									<option v-for="item in billTypeOptions" :key="item.codecd" :value="item.codecd">{{ item.codenm }}</option>
								</select>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- ?�� 그리???�역 -->
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
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const today = now.toISOString().substring(0, 10)

// ?�� 검??조건
const searchForm = reactive({
	ymd_fr: firstDay,
	ymd_to: today,
	billtype: '000'
})

const billTypeOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const formatYmdShort = (v: string) => v && v.length === 8 ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : v

// 초기 ?�이??로드 (?�음 ?�형 ?�션)
const loadInitData = async () => {
	try {
		const res = await api.post('/api/ha00/HA00_00P_STR', {
			gubun: 'E0',
			cmpycd: authStore.cmpycd,
			gbncd: '160'
		})
		billTypeOptions.value = res.data.map((item: any) => ({
			codecd: item.codecd,
			codenm: item.codenm
		}))
	} catch (e) {
		console.error('?�음 ?�형 코드 로드 ?�패', e)
	}
}

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_410S_STR', {
			cmpycd: authStore.cmpycd,
			billtype: searchForm.billtype,
			ymd_fr: searchForm.ymd_fr.replace(/-/g, ''),
			ymd_to: searchForm.ymd_to.replace(/-/g, '')
		})

		const data = (res.data || []).map((row: any) => {
			let bigo = row.bigo || ''
			const pcustNm = String(row.pcustnm || '').trim()

			// ASP 비고 병합 로직 반영
			if (pcustNm) {
				bigo = bigo.trim() ? `${pcustNm} / ${bigo}` : pcustNm
			}

			return {
				...row,
				dueymd: formatYmdShort(row.endymd),
				billno: row.billno,
				issubank: row.issubank,
				issuman: row.issuman,
				stdymd: formatYmdShort(row.stdymd),
				custnm: row.custnm,
				billkind_nm: row.billkind_nm,
				billtype_nm: row.billtype_nm,
				billamt: Number(row.billamt || 0),
				bigo_str: bigo
			}
		})

		mainGrid?.setData(data)
		vAlert('조회?�었?�니??')
	} catch (e) { vAlertError('조회 ?�패') }
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.ymd_fr = firstDay
	searchForm.ymd_to = today
	searchForm.billtype = '000'
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", `받을?�음명세??${today}.xlsx`)

const print = () => {
	const params = `ymd_fr=${searchForm.ymd_fr}&ymd_to=${searchForm.ymd_to}&billtype=${searchForm.billtype}`
	window.open(`/api/hafn/HAFN_410P?${params}`, 'BillStatementPrint', 'width=1000,height=800,scrollbars=yes')
}

onMounted(() => {
	loadInitData()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: "dueymd", // 만기?�별 그룹??
			groupHeader: function(value, count, data, group) {
				const sumamt = data.reduce((acc, curr) => acc + curr.billamt, 0)
				return `
					<div class="d-flex justify-content-between w-100 pe-4">
						<span class="fw-bold text-dark">만기?? ${value}</span>
						<div class="small fw-bold">
							<span class="mx-2 text-muted">?�계: ${sumamt.toLocaleString()}??(${count}�?</span>
						</div>
					</div>
				`
			},
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "?�음번호", field: "billno", width: 200, hozAlign: "center" },
				{ title: "발행?�??, field: "issubank", width: 200 },
				{ title: "발행??, field: "issuman", width: 150 },
				{ title: "발행??, field: "stdymd", hozAlign: "center", width: 150 },
				{ title: "받�?거래�?, field: "custnm", width: 250 },
				{ title: "종류", field: "billkind_nm", hozAlign: "center", width: 150 },
				{ title: "?�태", field: "billtype_nm", hozAlign: "center", width: 150 },
				{ title: "금액", field: "billamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 150,
					cssClass: "fw-bold text-primary"
				},
				{ title: "비고", field: "bigo_str", minWidth: 150 },
				{ title: "", field: "empty", visible: true }
			]
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-group) { background-color: #f0f4f8 !important; border-top: 1px solid #d1d9e6 !important; }
:deep(.tabulator-group-handle) { display: none; }
</style>
