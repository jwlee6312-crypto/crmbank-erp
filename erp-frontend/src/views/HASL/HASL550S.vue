<!--
	=============================================================
	프로그램명	: 보조원장-거래처
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 특정 계정과목 및 거래처에 대한 상세 전표 내역 및 잔액 조회
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
				<span class="text-primary fw-bolder">보조원장-거래처 (HASL550S)</span>
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
						<!-- 거래처 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>거&nbsp;래&nbsp;처</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.custcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
								<input v-model="searchForm.custnm" type="text" class="form-control" @keydown.enter="openHelp('CUST')" placeholder="거래처명 입력" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
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
import { useRouter, useRoute } from 'vue-router'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const now = new Date()
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const today = now.toISOString().substring(0, 10)

// 🔍 검색 조건
const searchForm = reactive({
	acctcd: (route.query.acctcd as string) || '',
	acctnm: '',
	custcd: (route.query.custcd as string) || '',
	custnm: '',
	custgbn: '', // 계정에 따른 거래처 구분
	frymd: (route.query.frymd as string) || firstDay,
	toymd: (route.query.toymd as string) || today
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.acctcd) {
		vAlertError('계정과목을 선택해 주십시오.')
		return
	}
	if (!searchForm.custcd) {
		vAlertError('거래처를 선택해 주십시오.')
		return
	}

	try {
		const res = await api.post('/api/hasl/HASL_550S_STR', {
			cmpycd: authStore.cmpycd,
			acctcd: searchForm.acctcd,
			custcd: searchForm.custcd,
			frymd: searchForm.frymd.replace(/-/g, ''),
			toymd: searchForm.toymd.replace(/-/g, '')
		})

		const rawData = res.data || []
		const processedData: any[] = []

		if (rawData.length > 0) {
			// 첫 번째 행: 전기이월/시작잔액
			let carryRow = rawData[0]
			let dbMmTot = Number(carryRow.col8 || 0)
			let crMmTot = Number(carryRow.COL9 || 0)
			let dbcr = carryRow.col10 // 'D' or 'C'
			let janAmt = dbcr === 'D' ? (dbMmTot - crMmTot) : (crMmTot - dbMmTot)

			processedData.push({
				acctymD: '',
				descnm: carryRow.col1,
				SLIPNO: '',
				dbamt: dbMmTot,
				cramt: crMmTot,
				janamt: janAmt,
				IS_S.mmARY: true
			})

			let i = 1
			while (i < rawData.length) {
				let currentYm = String(rawData[i].col0).substring(0, 6)
				let dbMmAmt = 0
				let crMmAmt = 0

				while (i < rawData.length && String(rawData[i].col0).substring(0, 6) === currentYm) {
					const row = rawData[i]
					const db = Number(row.col8 || 0)
					const cr = Number(row.COL9 || 0)

					if (dbcr === 'D') janAmt += (db - cr)
					else janAmt += (cr - db)

					// 세부내역(TEMP) 조립
					let details: string[] = []
					if (row.col4) details.push(String(row.col4).trim()) // deptnm
					if (row.col3) details.push(String(row.col3).trim()) // custnm
					if (row.col7) details.push(String(row.col7).trim()) // mgtno
					if (row.col5) details.push(String(row.col5).trim()) // bugtnm
					if (row.col6) details.push(String(row.col6).trim()) // PRJNM

					// 관리항목 12-17
					for (let idx = 12; idx <= 17; idx++) {
						const val = row[`COL${idx}`]
						if (val && String(val).trim().length > 0 && val !== '00000000') details.push(String(val).trim())
					}
					if (Number(row.col18) !== 0) details.push(new Intl.NumberFormat().format(row.col18))
					if (Number(row.col19) !== 0) details.push(new Intl.NumberFormat().format(row.col19))

					// 외화
					if (row.col20 === 'Y') {
						if (row.col21) details.push(String(row.col21).trim())
						if (Number(row.col22) !== 0) details.push(new Intl.NumberFormat().format(row.col22))
						if (Number(row.col23) !== 0) details.push(new Intl.NumberFormat().format(row.col23))
					}

					processedData.push({
						acctymD: row.col0,
						descnm: row.col1 + (row.col11 && String(row.col11).trim() !== '' ? ` (${row.col11})` : ''),
						SLIPNO: row.col2,
						dbamt: db,
						cramt: cr,
						janamt: janAmt,
						DETAIL_STR: details.join(' | '),
						IS_DATA: true
					})

					dbMmAmt += db
					crMmAmt += cr
					i++
				}

				// 월계
				processedData.push({
					acctymD: '',
					descnm: '월   계',
					dbamt: dbMmAmt,
					cramt: crMmAmt,
					janamt: null,
					IS_MONTHLY: true
				})

				dbMmTot += dbMmAmt
				crMmTot += crMmAmt

				// 누계
				processedData.push({
					acctymD: '',
					descnm: '누   계',
					dbamt: dbMmTot,
					cramt: crMmTot,
					janamt: janAmt,
					IS_TOTAL: true
				})
			}
		}

		mainGrid?.setData(processedData)
		if (processedData.length > 0) vAlert('조회되었습니다.')
		else vAlert('데이터가 존재하지 않습니다.')
	} catch (e) {
		vAlertError('조회 중 오류 발생')
	}
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: 'ACCT' | 'CUST') {
	if (type === 'ACCT') {
		Object.assign(modalProps, {
			title: '계정과목 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'acctnm',
			data: { gubun: 'A0', cmpycd: authStore.cmpycd, search: searchForm.acctnm },
			columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 180 }, { title: '구분', field: 'TYPESUB', width: 50, visible: false }],
			onConfirm: (d: any) => {
				searchForm.acctcd = d.acctcd
				searchForm.acctnm = d.acctnm
				searchForm.custgbn = d.TYPESUB || ''
				searchForm.custcd = ''
				searchForm.custnm = ''
			}
		})
	} else {
		Object.assign(modalProps, {
			title: '거래처 선택', path: '/api/ha00/HA00_03P_STR', defaultField: 'col1',
			data: { custgbn: searchForm.custgbn || '010', cmpycd: authStore.cmpycd, search: searchForm.custnm },
			columns: [{ title: '코드', field: 'col0', width: 100 }, { title: '거래처명', field: 'col1', width: 250 }],
			onConfirm: (d: any) => {
				searchForm.custcd = d.col0
				searchForm.custnm = d.col1
				search()
			}
		})
	}
	modalVisible.value = true
}

const print = () => {
	if (!searchForm.acctcd || !searchForm.custcd) return vAlertError('계정 및 거래처를 먼저 선택하세요.')
	const params = `acctcd=${searchForm.acctcd}&acctnm=${searchForm.acctnm}&custcd=${searchForm.custcd}&custnm=${searchForm.custnm}&frymd=${searchForm.frymd.replace(/-/g, '')}&toymd=${searchForm.toymd.replace(/-/g, '')}&PRTGU=1`
	window.open(`/api/hasl/HASL_550P?${params}`, 'CustomerLedgerPrint', 'width=800,height=800,scrollbars=yes')
}

const goSlipDetail = (slipNo: string) => {
	if (!slipNo || slipNo.length < 10) return
	const ymd = slipNo.substring(0, 8)
	const no = slipNo.substring(9)
	router.push({
		path: '/HASL/HASL110U',
		query: { slipymd: ymd, SLIPNO: no }
	})
}

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "일자", field: "acctymD", width: 90, hozAlign: "center",
					formatter: (cell) => {
						const v = cell.getValue()
						return v && v.length === 8 ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : ''
					}
				},
				{
					title: "적요 / 세부내역", field: "descnm", widthGrow: 2.5,
					formatter: (cell) => {
						const d = cell.getData()
						if (d.IS_DATA) {
							return `<div>${cell.getValue()}</div><div class="small text-secondary fw-normal mt-1" style="font-size: 11px;">${d.DETAIL_STR}</div>`
						}
						return cell.getValue()
					}
				},
				{
					title: "전표번호", field: "SLIPNO", width: 120, hozAlign: "center",
					formatter: (cell) => {
						const v = cell.getValue()
						return v ? `<span class="text-primary text-decoration-underline cursor-pointer">${v}</span>` : ''
					},
					cellClick: (e, cell) => {
						goSlipDetail(cell.getValue())
					}
				},
				{
					title: "차변", field: "dbamt", width: 120, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 }
				},
				{
					title: "대변", field: "cramt", width: 120, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 }
				},
				{
					title: "잔액", field: "janamt", width: 120, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 }
				}
			],
			rowFormatter: (row) => {
				const d = row.getData()
				if (d.IS_MONTHLY || d.IS_TOTAL || d.IS_S.mmARY) {
					row.getElement().style.backgroundColor = "#f8f9fa"
					row.getElement().style.fontWeight = "bold"
				}
				if (d.IS_TOTAL) {
					row.getElement().style.borderBottom = "2px solid #dee2e6"
				}
			}
		})
	}

	if (searchForm.acctcd) {
		try {
			// 계정 정보 및 거래처 구분 가져오기
			const res = await api.post('/api/haba/HABA_010T_GET', {
				cmpycd: authStore.cmpycd,
				acctcd: searchForm.acctcd
			})
			if (res.data?.length > 0) {
				searchForm.acctnm = res.data[0].acctnm
				searchForm.custgbn = res.data[0].TYPESUB

				if (searchForm.custcd) {
					// 거래처명 가져오기
					const resCust = await api.post('/api/ha00/HA00_010S_STR', {
						cmpycd: authStore.cmpycd,
						gubun: 'C0',
						TYPESUB: searchForm.custgbn,
						search: searchForm.custcd
					})
					if (resCust.data?.length > 0) {
						searchForm.custnm = resCust.data[0].col0 || resCust.data[0].custnm
						search()
					}
				}
			}
		} catch (e) {}
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; padding-top: 4px !important; padding-bottom: 4px !important; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; }
</style>
