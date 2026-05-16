<!--
	=============================================================
	프로그램명	  : 제조원가명세서
    프로그램 ID	: HFMF221S
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 제조원가명세서 (FMF2210R_STR 연결)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hfmf221s-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small"><i class="bi bi-file-earmark-ruled me-2 text-primary"></i>원가관리 >> 제조원가명세서</div>
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
						<label class="small fw-bold me-2 required-label">마감년월:</label>
						<input v-model="searchForm.YM" type="month" class="form-control form-control-sm d-inline-block w-auto" @change="handleSearch" />
					</div>
				</div>
			</div>
		</div>

		<!-- 3. 메인 그리드 -->
		<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary mt-1">
			<div class="card-header bg-white py-1 px-2 fw-bold small border-bottom">
				<span>제조원가 명세 (전월누계 / 당월 / 당월누계)</span>
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
const searchForm = reactive({ YM: new Date().toISOString().substring(0, 7) })
const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null

const handleSearch = async () => {
	try {
		const ym = searchForm.YM.replace('-', '')
		const { data } = await api.post('/api/hfmf/FMF2210R_STR', {
            YM: ym,
            ACTKIND: 'S0'
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
	mainGrid?.download('xlsx', `제조원가명세서_${searchForm.YM}.xlsx`)
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
            height: '100%',
			columnDefaults: { minWidth: 100 },
			columns: [
				{
                    title: '계정과목', field: 'ACCTNM', widthGrow: 3, hozAlign: 'left', frozen: true,
                    formatter: (cell) => {
                        const row = cell.getData();
                        const acct = row.ACCT;
                        const acctnm = row.ACCTNM;

                        // 계층형 표시 로직
                        if (acct === '415-0000') return `<b>Ⅱ. ${acctnm}</b>`;
                        if (acct === '418-0000') return `<b>Ⅲ. ${acctnm}</b>`;
                        if (acct === '419-0000') return `<b>Ⅳ. ${acctnm}</b>`;
                        if (acct === '411-0000') return `&nbsp;&nbsp;&nbsp;&nbsp;(1) ${acctnm}`;
                        if (acct === '412-0000') return `&nbsp;&nbsp;&nbsp;&nbsp;(2) ${acctnm}`;
                        if (acct === '413-0000') return `&nbsp;&nbsp;&nbsp;&nbsp;(3) ${acctnm}`;
                        if (acct === '414-0000' || acct === '416-0000' || acct === '417-0000') return `&nbsp;&nbsp;&nbsp;&nbsp;${acctnm}`;

                        return `&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${acctnm}`;
                    }
                },
				{ title: '전월누계', field: 'BEFOSUM', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '당월', field: 'CURRAMT', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'bg-light fw-bold' },
				{ title: '당월누계', field: 'CURRSUM', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } }
			]
		})
	}
	handleSearch()
})
</script>

<style scoped>
.hfmf221s-wrapper { height: calc(100vh - 110px); }
.tabulator-full-height { height: 100% !important; min-height: 400px; }
.required-label::before { content: '* '; color: red; }
</style>
