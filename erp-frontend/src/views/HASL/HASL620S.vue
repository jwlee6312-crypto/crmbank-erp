<!--
	=============================================================
	프로그램명	: 증감잔액명세서-거래처
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 특정 계정과목에 대해 거래처별 이월액, 증가액, 감소액 및 기말 잔액을 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-person-lines-fill me-2 text-primary" style="font-size: 18px;"></i>
				장부관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">증감잔액명세서-거래처 (HASL620S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<!-- 계정과목 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>계정과목</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
								<input v-model="searchForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT')" placeholder="계정명 입력" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('ACCT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<!-- 거래처 범위 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>거&nbsp;래&nbsp;처</span>
							<div class="d-flex align-items-center gap-1">
								<div class="input-group input-group-sm" style="width: 220px;">
									<input v-model="searchForm.custcdFR" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchForm.custnmFR" type="text" class="form-control" @keydown.enter="openHelp('CUSTFR')" placeholder="시작 거래처" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('CUSTFR')"><i class="bi bi-search"></i></button>
								</div>
								<span>~</span>
								<div class="input-group input-group-sm" style="width: 220px;">
									<input v-model="searchForm.custcdTO" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchForm.custnmTO" type="text" class="form-control" @keydown.enter="openHelp('CUSTTO')" placeholder="종료 거래처" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('CUSTTO')"><i class="bi bi-search"></i></button>
								</div>
							</div>
						</div>
						<!-- 회계일자 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>회계일자</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.frymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
								<span>~</span>
								<input v-model="searchForm.toymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
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
import { useRouter } from 'vue-router'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const now = new Date()
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const today = now.toISOString().substring(0, 10)

// 🔍 검색 조건
const searchForm = reactive({
	acctcd: '',
	acctnm: '',
	custgbn: '', // 계정에 따른 거래처 구분
	custcdFR: '',
	custnmFR: '',
	custcdTO: '',
	custnmTO: '',
	frymd: firstDay,
	toymd: today
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.acctcd) {
		vAlertError('계정과목을 선택해 주십시오.')
		return
	}
	if (!searchForm.frymd || !searchForm.toymd) {
		vAlertError('회계일자를 선택해 주십시오.')
		return
	}

	try {
		const res = await api.post('/api/hasl/HASL_620S_STR', {
			cmpycd: authStore.cmpycd,
			frymd: searchForm.frymd.replace(/-/g, ''),
			toymd: searchForm.toymd.replace(/-/g, ''),
			acctcd: searchForm.acctcd,
			custcdFR: searchForm.custcdFR,
			custcdTO: searchForm.custcdTO,
			gubun: '2' // ASP 파라미터 "2"
		})

		const data = (res.data || []).map((row: any) => ({
			...row,
			custcd: row.col0 || row.custcd,
			custnm: row.col1 || row.custnm,
			Bjanamt: Number(row.col2 || row.Bjanamt || 0),
			dbamt: Number(row.col3 || row.dbamt || 0),
			cramt: Number(row.col4 || row.cramt || 0),
			Cjanamt: Number(row.col5 || row.Cjanamt || 0)
		}))

		mainGrid?.setData(data)
		if (data.length > 0) vAlert('조회되었습니다.')
		else vAlert('데이터가 존재하지 않습니다.')
	} catch (e) {
		vAlertError('조회 중 오류 발생')
	}
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: 'ACCT' | 'CUSTFR' | 'CUSTTO') {
	if (type === 'ACCT') {
		Object.assign(modalProps, {
			title: '계정과목 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'acctnm',
			data: { gubun: 'A0', cmpycd: authStore.cmpycd, search: searchForm.acctnm },
			columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 180 }, { title: '거래처구분', field: 'TYPESUB', visible: false }],
			onConfirm: (d: any) => {
				searchForm.acctcd = d.acctcd
				searchForm.acctnm = d.acctnm
				searchForm.custgbn = d.TYPESUB || ''
				// 계정 변경 시 거래처 초기화
				searchForm.custcdFR = ''
				searchForm.custnmFR = ''
				searchForm.custcdTO = ''
				searchForm.custnmTO = ''
			}
		})
	} else {
		if (!searchForm.acctcd) {
			vAlertError('계정과목을 먼저 선택해 주십시오.')
			return
		}
		const isFr = type === 'CUSTFR'
		Object.assign(modalProps, {
			title: '거래처 선택', path: '/api/ha00/HA00_03P_STR', defaultField: 'col1',
			data: { custgbn: searchForm.custgbn || '010', cmpycd: authStore.cmpycd, search: isFr ? searchForm.custnmFR : searchForm.custnmTO },
			columns: [{ title: '코드', field: 'col0', width: 100 }, { title: '거래처명', field: 'col1', width: 250 }],
			onConfirm: (d: any) => {
				if (isFr) {
					searchForm.custcdFR = d.col0
					searchForm.custnmFR = d.col1
				} else {
					searchForm.custcdTO = d.col0
					searchForm.custnmTO = d.col1
				}
			}
		})
	}
	modalVisible.value = true
}

const print = () => {
	if (!searchForm.acctcd) return vAlertError('계정과목을 선택하세요.')
	const params = `acctcd=${searchForm.acctcd}&acctnm=${searchForm.acctnm}&custcdFR=${searchForm.custcdFR}&custnmFR=${searchForm.custnmFR}&custcdTO=${searchForm.custcdTO}&custnmTO=${searchForm.custnmTO}&frymd=${searchForm.frymd.replace(/-/g, '')}&toymd=${searchForm.toymd.replace(/-/g, '')}&PRTGU=1`
	window.open(`/api/hasl/HASL_620P?${params}`, 'CustomerStatementPrint', 'width=800,height=800,scrollbars=yes')
}

const goDetail = (custCd: string, custNm: string) => {
	if (!custCd) return
	router.push({
		path: '/HASL/HASL550S',
		query: {
			frymd: searchForm.frymd,
			toymd: searchForm.toymd,
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
				{ title: "거래처코드", field: "custcd", width: 100, hozAlign: "center" },
				{
					title: "거 래 처 명", field: "custnm", widthGrow: 2,
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</span>`,
					cellClick: (e, cell) => {
						const d = cell.getData()
						goDetail(d.custcd, d.custnm)
					}
				},
				{
					title: "이월액", field: "Bjanamt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "증가액", field: "dbamt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "감소액", field: "cramt", width: 130, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "잔액", field: "Cjanamt", width: 130, hozAlign: "right",
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
