<!--	=============================================================
	프로그램명	: 예산현황
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 부서별 예산 대비 실적/잔액 현황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 (표준 규격) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-grid-3x3-gap me-2 text-primary" style="font-size: 18px;"></i>
				예산관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">예산현황 (HABG210S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
			</div>
		</div>

		<!-- 🔍 검색 영역 (표준 레이아웃) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3">
						<div class="d-flex align-items-center">
							<span class="erp-label">예산년월</span>
							<input v-model="searchForm.BUGTym" type="month" class="form-control" style="width: 130px;" @change="search" />
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label">예산부서</span>
							<div class="input-group" style="width: 250px;">
								<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
								<input v-model="searchForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" placeholder="부서 선택" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 그리드 영역 (표준 통합CSS 적용) -->
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
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const searchForm = reactive({
	BUGTym: new Date().toISOString().slice(0, 7),
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || ''
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.deptcd) return vAlertError('부서를 선택하세요.')

	try {
		const res = await api.post('/api/habg/HABG_210S_STR', {
			cmpycd: authStore.cmpycd,
			BUGTym: searchForm.BUGTym.replace('-', ''),
			deptcd: searchForm.deptcd
		})

		const data = (res.data || []).map((row: any) => ({
			bugtcd: row.col0,
			bugtnm: row.col1,
			bugtamt: Number(row.col2 || 0),
			rsltamt: Number(row.col3 || 0),
			balamt: Number(row.col4 || 0),
			rate: Number(row.col2) > 0 ? (Number(row.col3) / Number(row.col2) * 100).toFixed(1) : '0.0'
		}))

		mainGrid?.setData(data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const excel = () => {
	mainGrid?.download("xlsx", `예산현황_${searchForm.deptnm}_${searchForm.BUGTym}.xlsx`)
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
    Object.assign(modalProps, {
        title: '부서 선택', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: searchForm.deptnm },
        columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
        onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search() }
    })
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columns: [
				{ title: "예산코드", field: "bugtcd", width: 100, hozAlign: "center" },
				{ title: "예산명", field: "bugtnm", width: 200 },
				{ title: "예산금액", field: "bugtamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "실적금액", field: "rsltamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "잔액", field: "balamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
				{ title: "집행율(%)", field: "rate", width: 100, hozAlign: "right" }
			]
		})
	}
	if (searchForm.deptcd) search()
})
</script>
