<!--
	=============================================================
	프로그램명	  : 공정별제조비용 배부표
    프로그램 ID	: HFMF206S
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 계정과목별 공정 배부 현황 출력 (FMF2060R_STR 연결)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hfmf206s-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small">
				<i class="bi bi-file-earmark-bar-graph me-2 text-primary"></i>원가관리 >> 공정별제조비용 배부표
			</div>
			<div class="btn-group shadow-sm">
				<button class="btn btn-sm btn-dark px-4 fw-bold" @click="handleSearch">
					<i class="bi bi-search me-1"></i>조회
				</button>
			</div>
		</div>

		<!-- 2. 검색 조건 영역 -->
		<div class="card shadow-sm border-0 mb-1 flex-shrink-0">
			<div class="card-body p-2 px-3">
				<div class="row g-3 align-items-center">
					<div class="col-auto">
						<label class="small fw-bold me-2 required-label">조회년월:</label>
						<div class="d-inline-flex gap-1">
							<select v-model="searchForm.YYYY" class="form-select form-select-sm" style="width: 100px;">
								<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
							</select>
							<select v-model="searchForm.MM" class="form-select form-select-sm" style="width: 80px;">
								<option v-for="month in monthOptions" :key="month" :value="month">{{ month }}월</option>
							</select>
						</div>
					</div>
					<div class="col-auto ms-auto">
						<div v-if="clsInfo.WCLSYM" class="badge bg-danger p-2">마감월: {{ clsInfo.WCLSYM }}</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 3. 메인 현황판 (Custom Table) -->
		<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary mt-1">
			<div class="card-body p-0 overflow-auto bg-white">
				<table class="report-table">
					<thead>
						<tr>
							<th style="width: 15%;">계정과목</th>
							<th style="width: 7.7%;">가공공정</th>
							<th style="width: 7.7%;">조립공정</th>
							<th style="width: 7.7%;">포장공정</th>
							<th style="width: 7.7%;">공정명</th>
							<th style="width: 7.7%;">공정명</th>
							<th style="width: 7.7%;">공정명</th>
							<th style="width: 7.7%;">공정명</th>
							<th style="width: 7.7%;">공정명</th>
							<th style="width: 7.7%;">공정명</th>
							<th style="width: 7.7%;">공정명</th>
							<th style="width: 8%;">합계</th>
						</tr>
					</thead>
					<tbody>
						<tr v-if="reportData.length === 0">
							<td colspan="12" class="text-center py-5 text-muted">조회된 데이터가 없습니다.</td>
						</tr>
						<tr v-for="(row, idx) in reportData" :key="idx" class="data-row">
							<td class="text-start ps-3 fw-bold" :class="{'bg-light': row.isHeader}" v-html="formatAcctNm(row)"></td>
							<td v-for="(amt, aIdx) in row.processAmts" :key="aIdx" class="text-end pe-2">
								{{ formatNumber(amt) }}
							</td>
							<!-- 나머지 빈 칸 채우기 (총 10개 공정) -->
							<td v-for="n in (10 - row.processAmts.length)" :key="'empty-'+n" class="text-end pe-2"></td>
							<td class="text-end pe-2 fw-bold text-primary bg-primary-subtle">
								{{ formatNumber(row.totalAmt) }}
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlertError } = useAlerts()

// 년월 옵션 생성
const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({
	YYYY: String(currentYear),
	MM: String(new Date().getMonth() + 1).padStart(2, '0')
})

const clsInfo = reactive({ WCLSYM: '' })
const reportData = ref<any[]>([])

const formatNumber = (val: any) => {
	if (!val || isNaN(val)) return ''
	return Number(val).toLocaleString()
}

// ASP의 ACCTNM 포맷팅 로직 재현
const formatAcctNm = (row: any) => {
	const acct = row.ACCT || ''
	const lev = String(row.LEV || '')
	let nm = row.ACCTNM || ''

	if (lev === '2' && acct === '412-0000') return `Ⅰ. ${nm}`
	if (lev === '2' && acct === '413-0000') return `Ⅱ. ${nm}`
	if (lev === '5' && acct === '414-0000') return nm
	return `&nbsp;&nbsp;&nbsp;&nbsp;${nm}`
}

const handleSearch = async () => {
	try {
		const ym = `${searchForm.YYYY}${searchForm.MM}`

		// 📋 SSMS 실행용 로그
		console.log(`📋 [SSMS Query] EXEC FMF2060R_STR '${authStore.CMPYCD}', 'S0', '${ym}', '10000'`)

		const { data } = await api.post('/api/hfmf/FMF2060R_STR', {
			CMPYCD: authStore.CMPYCD,
			ACTKIND: 'S0',
			YM: ym,
			COSTCD: '10000'
		})

		// 💡 ASP의 그룹핑 로직(ACCT별 가로 나열) 구현
		const grouped: any[] = []
		let currentAcct = ''
		let currentRow: any = null

		data.forEach((item: any) => {
			if (item.ACCT !== currentAcct) {
				if (currentRow) grouped.push(currentRow)
				currentAcct = item.ACCT
				currentRow = {
					ACCT: item.ACCT,
					ACCTNM: item.ACCTNM,
					LEV: item.LEV,
					processAmts: [],
					totalAmt: 0
				}
			}
			const amt = Number(item.TOTAMT || 0)
			currentRow.processAmts.push(amt)
			currentRow.totalAmt += amt
		})
		if (currentRow) grouped.push(currentRow)

		reportData.value = grouped
	} catch (e) {
		vAlertError('조회 중 오류가 발생했습니다.')
	}
}

onMounted(async () => {
	// 마감 정보 로드 (표준 HP00_000S_STR 사용)
	try {
		const { data } = await api.get('/api/comm/HP00_000S_STR', {
			params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD }
		})
		if (data?.length > 0) clsInfo.WCLSYM = data[0].WCLSYM || data[0].CODECD || ''
	} catch (e) {}

	handleSearch()
})
</script>

<style scoped>
.hfmf206s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }

/* 리포트 스타일 테이블 */
.report-table { width: 100%; border-collapse: collapse; table-layout: fixed; font-size: 12.5px; border: 1px solid #ccc; }
.report-table th {
	background-color: #f1f3f5; border: 1px solid #ccc; padding: 8px 4px;
	text-align: center; font-weight: 700; color: #333;
}
.report-table td { border: 1px solid #eee; padding: 6px 8px; vertical-align: middle; }

.data-row:hover { background-color: #fdfdf0; }
.bg-light { background-color: #f8f9fa !important; }

.required-label::before { content: '* '; color: red; }
</style>
