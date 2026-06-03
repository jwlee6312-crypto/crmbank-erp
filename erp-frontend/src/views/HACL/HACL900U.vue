<!--	=============================================================
	프로그램명	: 차기이월작업
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 회계연도 종료 후 자산/부채/자본의 잔액을 차기 회계연도로 이월 처리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-arrow-repeat me-2 text-primary" style="font-size: 18px;"></i>
				마감관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">차기이월작업 (HACL900U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-save" @click="handleExecute">
					<i class="bi bi-check-lg"></i> 저장
				</button>
			</div>
		</div>

		<div class="p-4 d-flex justify-content-center">
			<div class="card border shadow-sm bg-white overflow-hidden" style="max-width: 700px; width: 100%;">
				<div class="card-body p-4 bg-light border-bottom text-center">
					<h6 class="fw-bold mb-0 text-dark">차기이월 작업 설정</h6>
				</div>
				<div class="card-body p-4">
					<!-- 연도 및 이월 범위 설정 -->
					<div class="row g-3 align-items-center mb-4 pb-4 border-bottom">
						<div class="col-md-5 text-end">
							<span class="erp-label fw-bold"><i class="bi bi-dot"></i>대상연도 :</span>
						</div>
						<div class="col-md-7 d-flex align-items-center gap-2">
							<select v-model="form.clsyy" class="form-select form-select-sm" style="width: 120px;" @change="fetchPeriodInfo">
								<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
							</select>
							<div class="d-flex align-items-center small bg-white border rounded px-2 py-1">
								<span class="fw-bold text-primary">{{ period.fr_mm }}월</span>
								<i class="bi bi-arrow-right mx-2 text-muted"></i>
								<span class="fw-bold text-success">{{ period.to_yy }}년 {{ period.to_mm }}월</span>
							</div>
						</div>
					</div>

					<!-- 작업 방법 선택 -->
					<div class="row g-3 align-items-center mb-4">
						<div class="col-md-5 text-end">
							<span class="erp-label fw-bold"><i class="bi bi-dot"></i>작업방법 :</span>
						</div>
						<div class="col-md-7">
							<select v-model="form.wkgbn" class="form-select form-select-sm" style="max-width: 250px;">
								<option value="Y">차기이월 작업을 합니다.</option>
								<option value="N">차기이월 취소작업를 합니다.</option>
							</select>
						</div>
					</div>

					<!-- 안내 문구 -->
					<div class="alert alert-info border-0 shadow-sm small py-3 px-4 mb-0">
						<div class="d-flex mb-2">
							<i class="bi bi-info-circle-fill me-2 text-primary"></i>
							<span>회계연도의 결산월이 마감되었으면
								<span class="text-primary fw-bold" style="cursor:pointer;" @click="goToHaba100">[기본정보 > 마감/결제라인관리]</span>에서
							</span>
						</div>
						<div class="d-flex">
							<i class="bi bi-dot me-1 invisible"></i>
							<span>마감정보를 말일자로 통제한 후 차기이월 작업을 하시기 바랍니다.</span>
						</div>
					</div>
				</div>
				<div class="card-footer p-3 bg-white text-center border-top-0">
					<p class="text-muted mb-0" style="font-size: 11px;">※ 작업 실행 전 선택한 대상 연도와 이월 범위를 반드시 확인해 주세요.</p>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useTabStore } from '@/stores/tabStore'

const authStore = useAuthStore()
const tabStore = useTabStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))

const form = reactive({
	clsyy: String(currentYear - 1),
	wkgbn: 'Y'
})

const period = reactive({
	fr_mm: '',
	to_yy: '',
	to_mm: ''
})

// ASP: HACL_900U_STR 'S0' 로직
const fetchPeriodInfo = async () => {
	try {
		const res = await api.post('/api/hacl/HACL_900U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			clsyy: form.clsyy
		})

		if (res.data && res.data[0]) {
			const data = res.data[0]
			// ASP MID 로직 반영
			const frVal = String(data.col0 || '')
			const toVal = String(data.col1 || '')

			period.fr_mm = frVal.substring(4, 6)
			if (period.fr_mm === '12') {
				period.to_yy = String(Number(form.clsyy.substring(0, 4)) + 1)
			} else {
				period.to_yy = form.clsyy.substring(0, 4)
			}
			period.to_mm = toVal.substring(4, 6)
		}
	} catch (e) {
		console.error('Fetch period error', e)
	}
}

const handleExecute = async () => {
	const msg = form.wkgbn === 'Y' ? '차기이월 작업을 하시겠습니까?' : '차기이월 취소작업을 하시겠습니까?'
	if (!confirm(msg)) return

	try {
		const res = await api.post('/api/hacl/HACL_900U_STR', {
			actkind: 'A0',
			cmpycd: authStore.cmpycd,
			clsyy: form.clsyy,
			wkgbn: form.wkgbn
		})

		if (res.data && res.data[0]) {
			const result = res.data[0]
			if (result.col0 !== '000') {
				vAlertError(result.col1 || '처리 중 오류가 발생했습니다.')
			} else {
				vAlert('정상적으로 작업이 되었습니다.')
			}
		}
	} catch (e) {
		vAlertError('서버 통신 중 오류가 발생했습니다.')
	}
}

const goToHaba100 = () => {
	tabStore.addTab({ name: '마감/결제라인관리', path: '/HABA/HABA100U' })
}

onMounted(() => {
	fetchPeriodInfo()
})
</script>

<style scoped>
.erp-label { font-size: 13px; color: #555; }
.card { border-radius: 8px; }
</style>
