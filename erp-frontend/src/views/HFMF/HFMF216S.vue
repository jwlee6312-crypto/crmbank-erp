<!--
	=============================================================
	프로그램명	  : 제품별 제조원가 현황
    프로그램 ID	: HFMF216S
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 제품별 제조원가 현황 (FMF2160R_STR 연결)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hfmf216s-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small"><i class="bi bi-file-earmark-bar-graph me-2 text-primary"></i>원가관리 >> 제품별 제조원가 현황</div>
			<div class="btn-group shadow-sm">
				<button class="btn btn-sm btn-dark px-3" @click="handleSearch">조회</button>
				<button class="btn btn-sm btn-outline-success px-3" @click="handleExport">엑셀저장</button>
			</div>
		</div>

		<!-- 2. 검색 조건 영역 -->
		<div class="card shadow-sm border-0 mb-1 flex-shrink-0">
			<div class="card-body p-2 px-3">
				<div class="row g-3 align-items-center">
					<div class="col-auto">
						<label class="small fw-bold me-2 required-label">조회년월:</label>
						<input v-model="searchForm.YM" type="month" class="form-control form-control-sm d-inline-block w-auto" @change="handleSearch" />
					</div>
					<div class="col-auto ms-4 border-start ps-4">
						<label class="small fw-bold me-3">제품구분:</label>
						<div class="form-check form-check-inline">
							<input v-model="searchForm.JASANCD" class="form-check-input" type="radio" value="200" id="js200" @change="handleSearch">
							<label class="form-check-label small" for="js200">제품</label>
						</div>
						<div class="form-check form-check-inline">
							<input v-model="searchForm.JASANCD" class="form-check-input" type="radio" value="210" id="js210" @change="handleSearch">
							<label class="form-check-label small" for="js210">반제품</label>
						</div>
					</div>
					<div class="col-auto ms-4 border-start ps-4">
						<label class="small fw-bold me-3">조회구분:</label>
						<div class="form-check form-check-inline">
							<input v-model="searchForm.MMGBN" class="form-check-input" type="radio" value="M" id="mmM" @change="handleSearch">
							<label class="form-check-label small" for="mmM">월계</label>
						</div>
						<div class="form-check form-check-inline">
							<input v-model="searchForm.MMGBN" class="form-check-input" type="radio" value="N" id="mmN" @change="handleSearch">
							<label class="form-check-label small" for="mmN">누계</label>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 3. 메인 그리드 -->
		<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary mt-1">
			<div class="card-header bg-white py-1 px-2 fw-bold small border-bottom">
				<span>제품별 제조원가 상세 현황</span>
			</div>
			<div class="card-body p-0 flex-grow-1 bg-white">
				<div ref="mainGridRef" class="tabulator-full-height" />
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

// 상태 관리
const searchForm = reactive({
	YM: new Date().toISOString().substring(0, 7),
	JASANCD: '200',
	MMGBN: 'M'
})

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null

const handleSearch = async () => {
	try {
		const ym = searchForm.YM.replace('-', '')
		const { data } = await api.post('/api/hfmf/FMF2160R_STR', {
			YM: ym,
			JASANCD: searchForm.JASANCD,
			MMGBN: searchForm.MMGBN,
			ACTKIND: 'S0',
			COSTCD: '10000'
		})

		if (data.data) {
			mainGrid?.setData(data.data)
			vAlert('조회되었습니다.')
		}
	} catch (e) {
        vAlertError('조회 실패')
    }
}

const handleExport = () => {
	mainGrid?.download('xlsx', `제품별제조원가현황_${searchForm.YM}.xlsx`)
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
            height: '100%',
			columnDefaults: { minWidth: 100 },
			columns: [
				{ title: '코드', field: 'ITEMCD', width: 100, hozAlign: 'center', frozen: true },
				{ title: '품목명', field: 'ITEMNM', widthGrow: 2, hozAlign: 'left', frozen: true },
				{ title: '단위', field: 'UNIT', width: 70, hozAlign: 'center' },
				{ title: '생산량', field: 'PROQTY', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '재공품', field: 'JEGONGCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 2 } },
				{ title: '원재료', field: 'MATLCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '부품', field: 'BUMATLCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '계', field: 'JTOT_AMT', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 2 }, cssClass: 'bg-light' },
				{ title: '노무비', field: 'MANCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 2 } },
				{ title: '제조경비', field: 'EXPCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 2 } },
				{ title: '제조원가', field: 'PRICE', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 2 }, cssClass: 'fw-bold text-primary bg-info-subtle' }
			]
		})
	}
	handleSearch()
})
</script>

<style scoped>
.hfmf216s-wrapper { height: calc(100vh - 110px); }
.tabulator-full-height { height: 100% !important; min-height: 400px; }
.required-label::before { content: '* '; color: red; }
</style>
