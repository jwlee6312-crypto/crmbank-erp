<!--
	=============================================================
	프로그램명	: 증감잔액명세서
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 계정과목별 이월액, 증가액, 감소액 및 기말 잔액을 조회하는 명세서
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-list-check me-2 text-primary" style="font-size: 18px;"></i>
				장부관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">증감잔액명세서 (HASL610S)</span>
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
						<!-- 계정과목 범위 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>계정과목</span>
							<div class="d-flex align-items-center gap-1">
								<div class="input-group input-group-sm" style="width: 220px;">
									<input v-model="searchForm.acctcdfr" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchForm.acctcdfrnm" type="text" class="form-control" @keydown.enter="openHelp('FR')" placeholder="시작 계정" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('FR')"><i class="bi bi-search"></i></button>
								</div>
								<span>~</span>
								<div class="input-group input-group-sm" style="width: 220px;">
									<input v-model="searchForm.acctcdto" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchForm.acctcdtonm" type="text" class="form-control" @keydown.enter="openHelp('TO')" placeholder="종료 계정" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('TO')"><i class="bi bi-search"></i></button>
								</div>
							</div>
						</div>
						<!-- 회계일자 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>회계일자</span>
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
	acctcdfr: '',
	acctcdfrnm: '',
	acctcdto: '',
	acctcdtonm: '',
	fromdt: firstDay,
	todt: today
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.fromdt || !searchForm.todt) {
		vAlertError('회계일자를 선택해 주십시오.')
		return
	}

	try {
		const res = await api.post('/api/hasl/HASL_610S_STR', {
			cmpycd: authStore.cmpycd,
			fromdt: searchForm.fromdt.replace(/-/g, ''),
			todt: searchForm.todt.replace(/-/g, ''),
			acctcdfr: searchForm.acctcdfr,
			acctcdto: searchForm.acctcdto,
			gubun: '2' // ASP 코드의 마지막 파라미터 "2" 반영
		})

		const data = (res.data || []).map((row: any) => ({
			...row,
			acctcd: row.acctcd,
			acctnm: row.acctnm,
			bjanamt: Number(row.bjanamt || 0),
			dbamt: Number(row.dbamt || 0),
			cramt: Number(row.cramt || 0),
			cjanamt: Number(row.cjanamt || 0)
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

function openHelp(type: 'FR' | 'TO') {
	const isFr = type === 'FR'
	Object.assign(modalProps, {
		title: '계정과목 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'acctnm',
		data: { gubun: 'A0', cmpycd: authStore.cmpycd, search: isFr ? searchForm.acctcdfrnm : searchForm.acctcdtonm },
		columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 180 }],
		onConfirm: (d: any) => {
			if (isFr) {
				searchForm.acctcdfr = d.acctcd
				searchForm.acctcdfrnm = d.acctnm
			} else {
				searchForm.acctcdto = d.acctcd
				searchForm.acctcdtonm = d.acctnm
			}
		}
	})
	modalVisible.value = true
}

const print = () => {
	const params = `acctcdfr=${searchForm.acctcdfr}&acctcdfrnm=${searchForm.acctcdfrnm}&acctcdto=${searchForm.acctcdto}&acctcdtonm=${searchForm.acctcdtonm}&fromdt=${searchForm.fromdt.replace(/-/g, '')}&todt=${searchForm.todt.replace(/-/g, '')}&PRTGU=1`
	window.open(`/api/hasl/HASL_610P?${params}`, 'StatementPrint', 'width=800,height=800,scrollbars=yes')
}

// 🚀 상세 이동 (경로 수정 및 동적 라우트 등록)
const goDetail = (acctCd: string) => {
	if (!acctCd) return
    const pgmid = 'HASL540S'
    add_dynamic_route(pgmid, '보조원장', 'HASL')

	router.push({
		path: `/${pgmid}`,
		query: {
            fromdt: searchForm.fromdt.replace(/-/g, ''),
            todt: searchForm.todt.replace(/-/g, ''),
            acctcd: search_form.acctcd.trim(),
            acctnm: search_form.acctnm
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
				{ title: "계정코드", field: "acctcd", width: 150, hozAlign: "center" },
				{
					title: "계정과목 명", field: "acctnm", widthGrow: 2,
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</span>`,
					cellClick: (e, cell) => goDetail(cell.getData().acctcd)
				},
				{
					title: "이월액", field: "bjanamt", width: 200, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "증가액", field: "dbamt", width: 200, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "감소액", field: "cramt", width: 200, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "잔액", field: "cjanamt", width: 200, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				}
			],
			footerElement: '<div class="tabulator-footer-contents"></div>' // 합계 행 스타일용
		})
	}
	search()
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; }
:deep(.tabulator-footer .tabulator-calcs) { background-color: #f0f7ff !important; font-weight: bold; }
</style>
