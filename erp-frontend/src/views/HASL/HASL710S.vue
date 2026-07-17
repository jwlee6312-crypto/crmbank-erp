<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ë¶€?œë³„ ë¹„ìš©ëª…ì„¸??
	?‘ì„±?¼ì	: 2025.02.24
	?‘ì„±??    : AI Assistant
	?¤ëª…        : ë¶€?œë³„ ?”ë³„ ë¹„ìš© ë°œìƒ ?´ì—­??12ê°œì›”ë¶?ì¡°íšŒ (ë¶€???ì—… ?Œë¼ë¯¸í„° ?˜ì •)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-diagram-3 me-2 text-primary" style="font-size: 18px;"></i>
				?¥ë?ê´€ë¦?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">ë¶€?œë³„ ë¹„ìš©ëª…ì„¸??(HASL710S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> ì¡°íšŒ
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> ?¸ì‡„
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> ?‘ì?
				</button>
			</div>
		</div>

		<!-- ?” ê²€??ì¡°ê±´ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<!-- ë¶€??-->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>ë¶€&nbsp;&nbsp;&nbsp;&nbsp;??/span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
								<input v-model="searchForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" placeholder="ë¶€?œëª… ?…ë ¥" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<!-- ê¸°ì??°ì›” -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>ê¸°ì??°ì›”</span>
							<div class="d-flex align-items-center gap-1">
								<select v-model="searchForm.yyyy" class="form-select form-select-sm" style="width: 90px;">
									<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}??/option>
								</select>
								<select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 70px;">
									<option v-for="month in monthOptions" :key="month" :value="month">{{ month }}</option>
								</select>
								<span>??/span>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- ?“Š ê·¸ë¦¬???ì—­ -->
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
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = String(now.getMonth() + 1).padStart(2, '0')

// ê²€??ì¡°ê±´
const searchForm = reactive({
	deptcd: '',
	deptnm: '',
	yyyy: String(currentYear),
    mm: currentMonth
})

const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

// ?”ë³„ ?¤ë” ?•ë³´ë¥??´ì„ ë°°ì—´
const monthHeaders = ref<string[]>([])

const fetchMonthHeaders = async () => {
	try {
		const res = await api.post('/api/hasl/HA00_150S_STR', {
			cmpycd: authStore.cmpycd,
			yyyymm: `${searchForm.yyyy}${searchForm.mm}`
		})
		if (res.data && res.data.length > 0) {
			const row = res.data[0]
			monthHeaders.value = Array.from({ length: 12 }, (_, i) => {
				const val = row['COL' + i] || Object.values(row)[i]
				return val ? String(val).substring(4, 6) + '?? : `${i + 1}??
			})
		} else {
			monthHeaders.value = monthOptions.map(m => `${m}??)
		}
	} catch (e) {
		monthHeaders.value = monthOptions.map(m => `${m}??)
	}
}

const search = async () => {
	if (!searchForm.deptcd) {
		vAlertError('ë¶€?œë? ? íƒ??ì£¼ì‹­?œì˜¤.')
		return
	}

	try {
		// 1. ?¤ë” ë¨¼ì? ê°±ì‹ 
		await fetchMonthHeaders()

		// 2. ?°ì´??ì¡°íšŒ
		const res = await api.post('/api/hasl/HASL_710S_STR', {
			cmpycd: authStore.cmpycd,
			deptcd: searchForm.deptcd,
			yyyymm: `${searchForm.yyyy}${searchForm.mm}`
		})

		const rawData = res.data || []
		const processedData: any[] = []

		if (rawData.length > 0) {
			let i = 0
			let totalSum = Array(13).fill(0) // ?©ê³„ + 12ê°œì›”ë¶?

			while (i < rawData.length) {
				const groupKey = String(rawData[i].col2 || '').substring(0, 2)
				let groupSum = Array(13).fill(0)

				while (i < rawData.length && String(rawData[i].col2 || '').substring(0, 2) === groupKey) {
					const row = rawData[i]
					let rowMonthlySum = 0
					const monthlyValues = []

					for (let m = 1; m <= 12; m++) {
						const val = Number(row['COL' + (m + 2)] || 0)
						monthlyValues.push(val)
						rowMonthlySum += val
						groupSum[m] += val
						totalSum[m] += val
					}

					groupSum[0] += rowMonthlySum
					totalSum[0] += rowMonthlySum

					processedData.push({
						acctcd: row.col0,
						acctnm: row.col1,
						total: rowMonthlySum,
						...Object.fromEntries(monthlyValues.map((v, idx) => [`M${idx + 1}`, v])),
						is_data: true
					})
					i++
				}

				// ?Œê³„ ì¶”ê?
				processedData.push({
					acctcd: '',
					acctnm: '??  ê³?,
					total: groupSum[0],
					...Object.fromEntries(groupSum.slice(1).map((v, idx) => [`M${idx + 1}`, v])),
					IS_SUBtotal: true
				})
			}

			// ?©ê³„ ì¶”ê?
			processedData.push({
				acctcd: '',
				acctnm: '??  ê³?,
				total: totalSum[0],
				...Object.fromEntries(totalSum.slice(1).map((v, idx) => [`M${idx + 1}`, v])),
				is_total: true
			})
		}

		updateGridColumns()
		mainGrid?.setData(processedData)
		vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
	} catch (e) {
		vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ')
	}
}

const updateGridColumns = () => {
	const baseColumns = [
		{ title: "ê³„ì •", field: "acctcd", width: 80, hozAlign: "center", headerSort: false },
		{ title: "ê³„ì •ê³¼ëª© ëª?, field: "acctnm", width: 200, headerSort: false },
		{
			title: "??ê³?, field: "total", width: 120, hozAlign: "right",
			formatter: "money", formatterParams: { precision: 0 }, headerSort: false,
			cssClass: "fw-bold text-primary"
		}
	]

	const dynamicColumns = monthHeaders.value.map((label, idx) => ({
		title: label, field: `M${idx + 1}`, width: 100, hozAlign: "right",
		formatter: "money", formatterParams: { precision: 0 }, headerSort: false
	}))

	mainGrid?.setColumns([...baseColumns, ...dynamicColumns])
}

const excel = () => {
	mainGrid?.download("xlsx", `ë¶€?œë³„ë¹„ìš©ëª…ì„¸??${searchForm.yyyy}${searchForm.mm}.xlsx`)
}

const print = () => {
	if (!searchForm.deptcd) return vAlertError('ë¶€?œë? ë¨¼ì? ? íƒ?˜ì„¸??')
	const params = `deptcd=${searchForm.deptcd}&deptnm=${searchForm.deptnm}&ymTO=${searchForm.yyyy}${searchForm.mm}&PRTGU=1`
	window.open(`/api/hasl/HASL_710P?${params}`, 'ExpensePrint', 'width=800,height=800,scrollbars=yes')
}

// ?ì—… ?¤ì •
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	Object.assign(modalProps, {
		title: 'ë¶€??? íƒ',
		path: '/api/ha00/HA00_00P_STR',
		defaultField: 'deptnm',
		data: {
			gubun: 'D0',
			cmpycd: authStore.cmpycd,
			gbncd: '',
			code: searchForm.deptnm
		},
		columns: [
			{ title: 'ì½”ë“œ', field: 'deptcd', width: 80 },
			{ title: 'ë¶€?œëª…', field: 'deptnm', width: 180 }
		],
		onConfirm: (d: any) => {
			const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
			searchForm.deptcd = item.deptcd
			searchForm.deptnm = item.deptnm
			search()
		}
	})
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [], // ì´ˆê¸° ì»¬ëŸ¼?€ ë¹„ì›Œ??(ì¡°íšŒ ???™ì  ?¤ì •)
			rowFormatter: (row) => {
				const d = row.getData()
				if (d.IS_SUBtotal) row.getElement().style.backgroundColor = "#fcfcfc"
				if (d.is_total) {
					row.getElement().style.backgroundColor = "#f0f7ff"
					row.getElement().style.fontWeight = "bold"
				}
			}
		})
	}
	// ì´ˆê¸° ?¤ë” ?¤ì •
	fetchMonthHeaders().then(() => updateGridColumns())
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; }
</style>
