<!--	=============================================================
	프로그램명	: 배부작업
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 해당 월의 부문별/품목별 비용 배부 실행 (컴팩트 디자인 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container p-2">
		<!-- 🟢 상단 타이틀 및 버튼 (타이트한 배치) -->
		<div class="d-flex justify-content-between align-items-center mb-1 border-bottom pb-1">
			<div class="fw-bold small d-flex align-items-center">
				<i class="bi bi-chevron-right me-1 text-primary"></i>
				<span>관리손익</span>
				<i class="bi bi-chevron-double-right mx-1 opacity-50"></i>
				<span class="text-primary">배부작업 (HAPL100U)</span>
			</div>
			<div class="d-flex gap-1">
				<button class="btn btn-sm btn-primary shadow-sm px-4" @click="handleExecute">
					<i class="bi bi-play-fill"></i> 배부실행
				</button>
			</div>
		</div>

		<!-- 🔍 1. 작업 대상 설정 영역 (기존 테이블 스타일 레이아웃) -->
		<div class="border shadow-sm mb-2 bg-white overflow-hidden">
			<div class="row g-0 border-bottom border-secondary-subtle bg-light-beige">
				<div class="col-12 p-2 fw-bold small text-dark">
					<i class="bi bi-calendar-check me-1"></i> 배부 작업 설정
				</div>
			</div>
			<div class="row g-0">
				<div class="col-md-6 d-flex border-end border-bottom border-secondary-subtle">
					<div class="input-label bg-light-beige border-end p-1 px-3 fw-bold small text-center d-flex align-items-center justify-content-center" style="width: 120px;">기준연월</div>
					<div class="p-1 d-flex align-items-center gap-1 flex-grow-1">
						<select v-model="form.yy" class="form-select form-select-sm border-0 bg-transparent" style="width: 100px;">
							<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
						</select>
						<select v-model="form.mm" class="form-select form-select-sm border-0 bg-transparent" style="width: 80px;">
							<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
						</select>
					</div>
				</div>
				<div class="col-md-6 d-flex border-bottom border-secondary-subtle bg-light-yellow">
					<div class="p-2 small text-muted italic">※ 대상 연월을 선택한 후 [배부실행] 버튼을 클릭하세요.</div>
				</div>
			</div>

			<!-- 📘 안내 메시지 영역 (짜임새 있는 배치) -->
			<div class="row g-0">
				<div class="col-12 p-3 bg-white">
					<div class="border rounded p-2 bg-light shadow-inner">
						<div class="d-flex align-items-start mb-1 small">
							<i class="bi bi-info-circle-fill text-primary me-2 mt-1"></i>
							<span class="fw-bold">1. 당월 마감작업이 완료되었으면 배부작업을 하시기 바랍니다.</span>
						</div>
						<div class="d-flex align-items-start small">
							<i class="bi bi-info-circle-fill text-primary me-2 mt-1"></i>
							<span>2. <span class="text-primary fw-bold" style="cursor:pointer;" @click="goToHaba100">[기본정보 > 마감/결제라인관리]</span>의 마감정보와 작업월의 말일자가 일치해야 만 배부작업이 가능합니다.</span>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- ⚠️ 2. 차액 발생 시 그리드 영역 -->
		<div v-if="showErrorGrid" class="flex-grow-1 overflow-hidden d-flex flex-column border shadow-sm mt-1">
			<div class="bg-danger text-white p-1 px-3 small fw-bold d-flex justify-content-between">
				<span><i class="bi bi-exclamation-triangle-fill me-1"></i> 배부 전/후 금액 불일치 내역</span>
				<span class="opacity-75">수정 후 재작업 필요</span>
			</div>
			<div ref="errorGridRef" class="compact-grid flex-grow-1"></div>
		</div>

		<!-- 빈 공간 채우기용 (그리드가 없을 때) -->
		<div v-else class="flex-grow-1 bg-light-beige opacity-25 d-flex align-items-center justify-content-center">
			<i class="bi bi-calculator opacity-25" style="font-size: 100px;"></i>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useTabStore } from '@/stores/tabStore'

const authStore = useAuthStore()
const tabStore = useTabStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')

const yearOptions = Array.from({ length: 10 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const form = reactive({
	yy: String(currentYear),
    mm: currentMonth
})

const showErrorGrid = ref(false)
const errorGridRef = ref<HTMLDivElement | null>(null)
let errorGrid: Tabulator | null = null

const handleExecute = async () => {
	if (!confirm(`${form.yy}년 ${form.mm}월의 배부작업을 하시겠습니까?`)) return

	try {
		showErrorGrid.value = false

		// 1. 마감 정보 체크
		const resClose = await api.post('/api/haba/HABA_100U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd
		})

		const clsymd = resClose.data?.[0]?.clsymd || '00000000'
		const lastDay = new Date(Number(form.yy), Number(form.mm), 0).getDate()
		const targetLastYmd = `${form.yy}${form.mm}${String(lastDay).padStart(2, '0')}`

		if (targetLastYmd > clsymd) {
			return vAlertError('해당월의 마감 작업을 하신 후 작업하시기 바랍니다.')
		} else if (targetLastYmd < clsymd) {
			return vAlertError('과거 자료는 배부작업을 할 수 없습니다.\n마감정보를 작업월의 마지막 일자로 수정하신 후 작업하시기 바랍니다.')
		}

		const resExec = await api.post('/api/hapl/HAPL_100U_STR', {
			actkind: 'A',
			cmpycd: authStore.cmpycd,
			STDym: form.yy + form.mm,
			userid: authStore.userid
		})

		const rawData = resExec.data || []
		if (rawData.length > 0) {
			vAlertError('배부전과 배부후 금액이 다릅니다.\n계정별 배부기준과 배부적수를 확인 하시기 바랍니다.')
			showErrorGrid.value = true

			const mappedData = rawData.map((row: any) => ({
				acctcd: row.col0,
				acctnm: row.col1,
				BEF_AMT: Number(row.col2 || 0),
				AFT_AMT: Number(row.col3 || 0),
				DIFF_AMT: Number(row.col4 || 0)
			}))

			await nextTick()
			initErrorGrid(mappedData)
		} else {
			await api.post('/api/hapl/HAPL_100U_STR', {
				actkind: 'C',
				cmpycd: authStore.cmpycd,
				STDym: form.yy + form.mm,
				userid: authStore.userid
			})
			vAlert('배부작업이 정상으로 되었습니다.')
		}
	} catch (e) {
		vAlertError('배부 작업 중 오류가 발생했습니다.')
	}
}

const initErrorGrid = (data: any[]) => {
	if (!errorGridRef.value) return

	errorGrid = new Tabulator(errorGridRef.value, {
		data: data,
		layout: 'fitColumns',
		height: '100%',
		columnDefaults: { headerSort: false, vertAlign: "middle" },
		columns: [
			{ title: "계정", field: "acctcd", width: 90, hozAlign: "center", headerHozAlign: "center" },
			{ title: "계정명", field: "acctnm", widthGrow: 2, headerHozAlign: "center" },
			{ title: "배부전금액", field: "BEF_AMT", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, headerHozAlign: "center" },
			{ title: "배부후금액", field: "AFT_AMT", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, headerHozAlign: "center" },
			{ title: "차 액", field: "DIFF_AMT", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-danger fw-bold", headerHozAlign: "center" }
		]
	})
}

const goToHaba100 = () => {
	tabStore.addTab({ name: '마감/결제라인관리', path: '/HABA/HABA100U' })
}
</script>

<style scoped>
/* 🎨 기존 시스템 테마 재현 */
.bg-light-beige { background-color: #f1ede3 !important; }
.bg-light-yellow { background-color: #fffde7 !important; }
.shadow-inner { box-shadow: inset 0 1px 3px rgba(0,0,0,0.05); }

.erp-container {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background-color: #fff;
}

.input-label {
	color: #333;
	font-size: 12px;
}

/* Tabulator 디자인 (고밀도) */
:deep(.tabulator) {
	font-size: 12px;
	border: none !important;
}
:deep(.tabulator-header) {
	background-color: #7d7d9a !important; /* 보라빛 도는 클래식 헤더 색상 */
	color: white !important;
	font-weight: bold;
}
:deep(.tabulator-header .tabulator-col) {
	background-color: transparent !important;
	border-right: 1px solid #ffffff44 !important;
}
:deep(.tabulator-row) {
	min-height: 28px !important;
}
:deep(.tabulator-cell) {
	padding: 4px 8px !important;
	border-right: 1px solid #eee !important;
}
:deep(.tabulator-row:hover) {
	background-color: #dfd9bd !important;
}

.form-select-sm {
	font-size: 12px;
	padding: 2px 5px;
}

.italic { font-style: italic; }
</style>
