<!--
	=============================================================
	프로그램명	  : 원가마감정보등록
    프로그램 ID	: HFBA301U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 원가마감정보등록 (FBA3010U_STR 연결)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<Modal v-model:visible="showModal" :modalProps="modalProps" />

	<div class="hfba301u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small"><i class="bi bi-calendar-check me-2 text-primary"></i>원가관리 >> 원가마감정보등록</div>
			<div class="btn-group shadow-sm">
				<button class="btn btn-sm btn-dark px-3" @click="handleSearch">조회</button>
				<button class="btn btn-sm btn-primary px-4 fw-bold" @click="handleSave"><i class="bi bi-save me-1"></i>저장</button>
			</div>
		</div>

		<!-- 2. 검색 조건 영역 -->
		<div class="card shadow-sm border-0 mb-1 flex-shrink-0">
			<div class="card-body p-2 px-3">
				<div class="row g-3 align-items-center">
					<div class="col-auto">
						<label class="small fw-bold me-2 required-label">적용년도:</label>
						<select v-model="searchForm.year" class="form-select form-select-sm d-inline-block w-auto" @change="handleSearch">
							<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
						</select>
					</div>
					<div class="col-auto ms-auto">
						<div class="alert alert-warning py-1 px-3 mb-0 small border-0 shadow-sm">
							<i class="bi bi-info-circle-fill me-2"></i>원가마감월 체크 후 저장처리 => 수불부원가생성 => 영업매출원가 확인할 것.
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 3. 메인 그리드 -->
		<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary mt-1">
			<div class="card-header bg-white py-1 px-2 fw-bold small border-bottom">
				<span>연간 원가 마감 상태 목록</span>
			</div>
			<div class="card-body p-0 flex-grow-1 bg-white">
				<div ref="mainGridRef" class="tabulator-full-height" />
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

// 상태 관리
const yearOptions = ref<string[]>([])
const searchForm = reactive({
	year: new Date().getFullYear().toString()
})

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null
const showModal = ref(false)
const modalProps = ref<any>({})

const handleSearch = async () => {
	try {
		const yymm = searchForm.year + "01"
		const { data } = await api.post('/api/hfba/FBA3010U_STR', {
			yymm: yymm,
			actkind: 'S0'
		})

		if (data.data) {
			mainGrid?.setData(data.data)
			nextTick(() => {
				mainGrid?.getRows().forEach(row => {
					if (row.getData().YN === 'Y') row.select()
				})
			})
			vAlert('조회되었습니다.')
		}
	} catch (e) { vAlertError('조회 실패') }
}

const handleSave = async () => {
	const allRows = mainGrid?.getRows() || []
	if (allRows.length === 0) return

	if (!confirm('원가 마감 정보를 저장하시겠습니까?')) return

	try {
		for (const row of allRows) {
			const d = row.getData()
			const isSelected = row.isSelected()
			const actkind = isSelected ? 'A0' : 'D0'

			await api.post('/api/hfba/FBA3010U_STR', {
				yymm: searchForm.year + d.MM,
				procyn: isSelected ? 'Y' : 'N',
				remark: d.REMARK || '',
				actkind: actkind
			})
		}

		vAlert('성공적으로 저장되었습니다.')
		handleSearch()
	} catch (e) { vAlertError('저장 중 오류 발생') }
}

onMounted(() => {
	const curYear = new Date().getFullYear()
	for (let i = 0; i < 5; i++) yearOptions.value.push((curYear - i).toString())

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			columnDefaults: { minWidth: 100 },
			columns: [
				{
					title: '마감확정', field: '_sel', width: 100, hozAlign: 'center',
					formatter: 'rowSelection', titleFormatter: 'rowSelection', headerSort: false
				},
				{
					title: '마감년월', field: 'MM', width: 150, hozAlign: 'center',
					formatter: (cell) => `${searchForm.year}-${cell.getValue()}`
				},
				{ title: '비고', field: 'REMARK', widthGrow: 3, hozAlign: 'left', editor: 'input' }
			]
		})
	}
	handleSearch()
})
</script>

<style scoped>
.hfba301u-wrapper { height: calc(100vh - 110px); }
.tabulator-full-height { height: 100% !important; min-height: 400px; }
.required-label::before { content: '* '; color: red; }
</style>
