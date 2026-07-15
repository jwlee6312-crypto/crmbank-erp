<!--	=============================================================
	프로그램명	: 매입장
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 사업장별 매입 세금계산서 상세 내역 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journal-text me-2 text-primary" style="font-size: 18px;"></i>
				세무관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">매입장 (HATX030S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex flex-column gap-2 small">
						<div class="d-flex align-items-center flex-wrap gap-3">
							<div class="d-flex align-items-center">
								<span class="erp-label"><i class="bi bi-dot"></i>사업장</span>
								<select v-model="searchForm.SAUPCD" class="form-select form-select-sm" style="width: 150px;">
									<option v-for="opt in taxUnitOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
								</select>
							</div>
							<div class="d-flex align-items-center">
								<span class="erp-label"><i class="bi bi-dot"></i>유형</span>
								<div class="d-flex align-items-center gap-1">
									<select v-model="searchForm.gubun1" class="form-select form-select-sm" style="width: 110px;">
										<option v-for="opt in taxTypeOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
									</select>
									<span>~</span>
									<select v-model="searchForm.gubun2" class="form-select form-select-sm" style="width: 110px;">
										<option v-for="opt in taxTypeOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
									</select>
								</div>
							</div>
							<div class="d-flex align-items-center">
								<span class="erp-label"><i class="bi bi-dot"></i>발행일</span>
								<div class="d-flex align-items-center gap-1">
									<input v-model="searchForm.stdymd" type="date" class="form-control form-control-sm" style="width: 135px;" />
									<span>~</span>
									<input v-model="searchForm.endymd" type="date" class="form-control form-control-sm" style="width: 135px;" />
								</div>
							</div>
						</div>
						<div class="d-flex align-items-center flex-wrap gap-3">
							<div class="d-flex align-items-center">
								<span class="erp-label"><i class="bi bi-dot"></i>거래처</span>
								<div class="d-flex align-items-center gap-1">
									<div class="input-group input-group-sm" style="width: 200px;">
										<input v-model="searchForm.Fcustcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.Fcustnm" type="text" class="form-control" @keydown.enter="openHelp('CUST', 'F')" placeholder="시작 거래처" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('CUST', 'F')"><i class="bi bi-search"></i></button>
									</div>
									<span>-</span>
									<div class="input-group input-group-sm" style="width: 200px;">
										<input v-model="searchForm.Tcustcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.Tcustnm" type="text" class="form-control" @keydown.enter="openHelp('CUST', 'T')" placeholder="종료 거래처" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('CUST', 'T')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</div>
							<div class="d-flex align-items-center">
								<span class="erp-label"><i class="bi bi-dot"></i>종류</span>
								<select v-model="searchForm.elcyn" class="form-select form-select-sm" style="width: 130px;">
									<option value="0">전체</option>
									<option value="N">일반세금계산서</option>
									<option value="Y">전자세금계산서</option>
								</select>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column" style="min-height: 0;">
                  <div ref="mainGridRef" class="tabulator-full-height" />
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
import * as XLSX from 'xlsx'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useTabStore } from '@/stores/tabStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const tabStore = useTabStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const today = new Date().toISOString().slice(0, 10)
const firstDay = today.slice(0, 8) + '01'

const taxUnitOptions = ref<any[]>([])
const taxTypeOptions = ref<any[]>([])

const searchForm = reactive({
	SAUPCD: '',
	gubun1: '',
	gubun2: '',
	stdymd: firstDay,
	endymd: today,
	Fcustcd: '',
	Fcustnm: '',
	Tcustcd: '',
	Tcustnm: '',
	elcyn: '0'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const fetchOptions = async () => {
	try {
		const [resU, resT] = await Promise.all([
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd, search: ' ' }),
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '120' })
		])
		taxUnitOptions.value = resU.data || []
		if (taxUnitOptions.value.length > 0) searchForm.SAUPCD = taxUnitOptions.value[0].code

		taxTypeOptions.value = resT.data || []
		if (taxTypeOptions.value.length > 0) {
			searchForm.gubun1 = taxTypeOptions.value[0].code
			searchForm.gubun2 = taxTypeOptions.value[taxTypeOptions.value.length - 1].code
		}
	} catch (e) { console.error(e) }
}

const search = async () => {
	if (!searchForm.SAUPCD) return vAlertError('사업장을 선택하세요.')
	if (searchForm.gubun1 > searchForm.gubun2) return vAlertError('유형 범위를 확인하세요.')

	try {
		const res = await api.post('/api/hatx/HATX_030S_STR', {
			cmpycd: authStore.cmpycd,
			SAUPCD: searchForm.SAUPCD,
			gubun1: searchForm.gubun1,
			gubun2: searchForm.gubun2,
			stdymd: searchForm.stdymd.replace(/-/g, ''),
			endymd: searchForm.endymd.replace(/-/g, ''),
			Fcustcd: searchForm.Fcustcd,
			Tcustcd: searchForm.Tcustcd,
			elcyn: searchForm.elcyn
		})

		const data = (res.data || []).map((row: any) => ({
			pubymd: row.col0,
			custno: row.col1,
			custnm: row.col2,
			descnm: row.col3,
			supyamt: Number(row.col4 || 0),
			vatamt: Number(row.col5 || 0),
			totamt: Number(row.col6 || 0),
			TAX_REF: row.col7, // taxym(6) + '-' + taxno(4)
			SLIP_REF: row.col8, // slipymd(8) + '-' + slipno(3) + '-' + srowno(3)
			typenm: row.COL9,
			elcyn: row.elcyn,
			slipgu: row.slipgu,
			deptcd: row.deptcd
		}))

		mainGrid?.setData(data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const excel = () => {
	mainGrid?.download("xlsx", `매입장_${searchForm.stdymd}_${searchForm.endymd}.xlsx`)
}

const print = () => {
	const params = `taxunit=${searchForm.SAUPCD}&gubun1=${searchForm.gubun1}&gubun2=${searchForm.gubun2}&stdymd=${searchForm.stdymd}&endymd=${searchForm.endymd}&Fcustcd=${searchForm.Fcustcd}&Tcustcd=${searchForm.Tcustcd}&PRTGU=1`
	window.open(`/api/hatx/HATX_030P?${params}`, 'PurchaseRegister', 'width=1000,height=800,scrollbars=yes')
}

// --- 팝업 설정 ---
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, target?: string) {
	if (type === 'CUST') {
		Object.assign(modalProps, {
			title: '거래처 선택', path: '/api/ha00/HA00_010S_STR',
			data: { gubun: 'C0', search_gubun: '010', cmpycd: authStore.cmpycd, search: '' },
			columns: [{ title: '코드', field: 'custcd', width: 80 }, { title: '거래처명', field: 'custnm', width: 180 }],
			onConfirm: (d: any) => {
				if (target === 'F') { searchForm.Fcustcd = d.custcd; searchForm.Fcustnm = d.custnm }
				else { searchForm.Tcustcd = d.custcd; searchForm.Tcustnm = d.custnm }
			}
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (typeof window !== 'undefined') (window as any).XLSX = XLSX
	fetchOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: {
				headerSort: false,
				hozAlign: 'right', // 🚀 기본값 우측 정렬
				vertAlign: "middle"
			},
			columns: [
				{
					title: "발행일", field: "pubymd", width: 90, hozAlign: "center", frozen: true,
					formatter: (cell) => { const v = cell.getValue(); return v ? `${v.slice(0,4)}-${v.slice(4,6)}-${v.slice(6,8)}` : '' }
				},
				{ title: "사업자번호", field: "custno", width: 110, hozAlign: "center", frozen: true },
				{ title: "상호", field: "custnm", width: 150, frozen: true },
				{ title: "유형", field: "typenm", width: 80, hozAlign: "center" },
				{ title: "전자", field: "elcyn", width: 50, hozAlign: "center" },
				{ title: "적요", field: "descnm", widthGrow: 1.5 },
				{
					title: "공급가", field: "supyamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "부가세", field: "vatamt", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "합계", field: "totamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 },
					cssClass: "fw-bold"
				},
				{
					title: "번호", field: "TAX_REF", width: 110, hozAlign: "center",
					formatter: (cell) => `<span class="text-primary text-decoration-underline" style="cursor:pointer;">${cell.getValue()}</span>`,
					cellClick: (e, cell) => {
						const d = cell.getData()
						if (!d.TAX_REF) return
						const ym = d.TAX_REF.substring(0, 6)
						const no = d.TAX_REF.substring(7, 11)
						tabStore.addTab({
							name: '매입부가세관리',
							path: '/HATX/HATX010U',
							params: { taxym: ym, taxno: no }
						})
					}
				},
				{
					title: "전표번호", field: "SLIP_REF", width: 150, hozAlign: "center",
					formatter: (cell) => cell.getValue() ? `<span class="text-primary text-decoration-underline" style="cursor:pointer;">${cell.getValue()}</span>` : '',
					cellClick: (e, cell) => {
						const d = cell.getData()
						if (!d.SLIP_REF) return
						const parts = d.SLIP_REF.split('-')
						const path = d.slipgu === '010' ? '/HASL/HASL010U' : '/HASL/HASL110U'
						tabStore.addTab({
							name: d.slipgu === '010' ? '회계전표관리' : '일반전표관리',
							path: path,
							params: { slipymd: parts[0], slipno: parts[1], deptcd: d.deptcd }
						})
					}
				}
			]
		})
	}
})
</script>

<style scoped>
.tabulator-full-height { width: 100% !important; background-color: #fff; border-bottom: 3px solid #005a9f !important; }
</style>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; font-size: 12px; }
:deep(.tabulator-footer .tabulator-calcs) { background-color: #f0f7ff !important; font-weight: bold; font-size: 12px; }
</style>
