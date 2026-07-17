<!--
	=============================================================
	?�로그램�?: 증감?�액명세??거래�?
	?�성?�자	: 2025.02.24
	?�성??    : AI Assistant
	?�명        : ?�정 계정과목???�??거래처별 ?�월?? 증�??? 감소??�?기말 ?�액??조회 (HA00_00P_STR ?�식)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- ?? ?�단 ?�션 �?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-person-lines-fill me-2 text-primary" style="font-size: 18px;"></i>
				?��?관�?<i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">증감?�액명세??거래�?(HASL620S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> ?�쇄
				</button>
			</div>
		</div>

		<!-- ?�� 검??조건 ?�역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<!-- 계정과목 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>계정과목</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
								<input v-model="searchForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT')" placeholder="계정�??�력" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('ACCT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<!-- 거래�?범위 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>�?nbsp;??nbsp;�?/span>
							<div class="d-flex align-items-center gap-1">
								<div class="input-group input-group-sm" style="width: 220px;">
									<input v-model="searchForm.custcdfr" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchForm.custnmfr" type="text" class="form-control" @keydown.enter="openHelp('CUSTFR')" placeholder="?�작 거래�? />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('CUSTFR')"><i class="bi bi-search"></i></button>
								</div>
								<span>~</span>
								<div class="input-group input-group-sm" style="width: 220px;">
									<input v-model="searchForm.custcdto" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchForm.custnmto" type="text" class="form-control" @keydown.enter="openHelp('CUSTTO')" placeholder="종료 거래�? />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('CUSTTO')"><i class="bi bi-search"></i></button>
								</div>
							</div>
						</div>
						<!-- ?�계?�자 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>?�계?�자</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.fromdt" type="date" class="form-control form-control-sm" style="width: 140px;" />
								<span>~</span>
								<input v-model="searchForm.todt" type="date" class="form-control form-control-sm" style="width: 140px;" />
							</div>
						</div>
					</div>
				</div>
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
import { useRouter } from 'vue-router'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const now = new Date()
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const today = now.toISOString().substring(0, 10)

// ?�� 검??조건
const searchForm = reactive({
	acctcd: '',
	acctnm: '',
	custgbn: '', // 계정???�른 거래�?구분
	custcdfr: '',
	custnmfr: '',
	custcdto: '',
	custnmto: '',
	fromdt: firstDay,
	todt: today
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.acctcd) {
		vAlertError('계정과목???�택??주십?�오.')
		return
	}
	if (!searchForm.fromdt || !searchForm.todt) {
		vAlertError('?�계?�자�??�택??주십?�오.')
		return
	}

	try {
		const res = await api.post('/api/hasl/HASL_620S_STR', {
			cmpycd: authStore.cmpycd,
			fromdt: searchForm.fromdt.replace(/-/g, ''),
			todt: searchForm.todt.replace(/-/g, ''),
			acctcd: searchForm.acctcd,
			custcdfr: searchForm.custcdfr,
			custcdto: searchForm.custcdto,
			gubun: '2' // ASP ?�라미터 "2"
		})

		const data = (res.data || []).map((row: any) => {
            const item = Object.fromEntries(Object.entries(row).map(([k, v]) => [k.toLowerCase(), v]))
            return {
                ...item,
                custcd: item.col0 || item.custcd,
                custnm: item.col1 || item.custnm,
                bjanamt: Number(item.col2 || item.bjanamt || 0),
                dbamt: Number(item.col3 || item.dbamt || 0),
                cramt: Number(item.col4 || item.cramt || 0),
                cjanamt: Number(item.col5 || item.cjanamt || 0)
            }
        })

		mainGrid?.setData(data)
		if (data.length > 0) vAlert('조회?�었?�니??')
		else vAlert('?�이?��? 존재?��? ?�습?�다.')
	} catch (e) {
		vAlertError('조회 �??�류 발생')
	}
}

// ?�업 ?�정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: 'ACCT' | 'CUSTFR' | 'CUSTTO') {
	if (type === 'ACCT') {
		Object.assign(modalProps, {
			title: '계정과목 ?�택', path: '/api/ha00/HA00_00P_STR', defaultField: 'acctnm',
			data: { gubun: 'A0', cmpycd: authStore.cmpycd, gbncd: '', code: searchForm.acctnm },
			columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정�?, field: 'acctnm', width: 180 }, { title: '거래처구�?, field: 'typesub', visible: false }],
			onConfirm: (d: any) => {
                const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
				searchForm.acctcd = item.acctcd
				searchForm.acctnm = item.acctnm
				searchForm.custgbn = item.typesub || ''
				// 계정 변�???거래�?초기??
				searchForm.custcdfr = ''; searchForm.custnmfr = ''; searchForm.custcdto = ''; searchForm.custnmto = ''
			}
		})
	} else {
		if (!searchForm.acctcd) {
			vAlertError('계정과목??먼�? ?�택??주십?�오.')
			return
		}
		const isFr = type === 'CUSTFR'
		Object.assign(modalProps, {
			title: '거래�??�택', path: '/api/ha00/HA00_00P_STR', defaultField: 'custnm',
			data: { gubun: 'c4', cmpycd: authStore.cmpycd, gbncd: '', code: isFr ? searchForm.custnmfr : searchForm.custnmto },
			columns: [{ title: '코드', field: 'custcd', width: 100 }, { title: '거래처명', field: 'custnm', width: 250 }],
			onConfirm: (d: any) => {
                const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
				if (isFr) {
					searchForm.custcdfr = item.custcd
					searchForm.custnmfr = item.custnm
				} else {
					searchForm.custcdto = item.custcd
					searchForm.custnmto = item.custnm
				}
			}
		})
	}
	modalVisible.value = true
}

const print = () => {
	if (!searchForm.acctcd) return vAlertError('계정과목???�택?�세??')
	const params = `acctcd=${searchForm.acctcd}&acctnm=${searchForm.acctnm}&custcdfr=${searchForm.custcdfr}&custnmfr=${searchForm.custnmfr}&custcdto=${searchForm.custcdto}&custnmto=${searchForm.custnmto}&fromdt=${searchForm.fromdt.replace(/-/g, '')}&todt=${searchForm.todt.replace(/-/g, '')}&PRTGU=1`
	window.open(`/api/hasl/HASL_620P?${params}`, 'CustomerStatementPrint', 'width=800,height=800,scrollbars=yes')
}

const goDetail = (custCd: string, custNm: string) => {
	if (!custCd) return
	router.push({
		path: '/HASL/HASL550S',
		query: {
			fromdt: searchForm.fromdt,
			todt: searchForm.todt,
			acctcd: searchForm.acctcd,
			custcd: custCd,
			custnm: custNm
		}
	})
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "거래처코??, field: "custcd", width: 100, hozAlign: "center" },
				{
					title: "�???�?�?, field: "custnm", widthGrow: 2,
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</span>`,
					cellClick: (e, cell) => {
						const d = cell.getData()
						goDetail(d.custcd, d.custnm)
					}
				},
				{
					title: "?�월??, field: "bjanamt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "증�???, field: "dbamt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "감소??, field: "cramt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "?�액", field: "cjanamt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				}
			]
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; }
:deep(.tabulator-footer .tabulator-calcs) { background-color: #f0f7ff !important; font-weight: bold; }
</style>
