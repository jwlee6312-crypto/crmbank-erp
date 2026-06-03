<!--	=============================================================
	프로그램명	: 감가상각계산
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 해당 월의 변동자산에 대한 감가상각 계산 처리 (디자인 표준화 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 (표준 규격 및 함수명 통일) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
				고정자산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">감가상각계산 (HAFA090U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-play-circle-fill"></i> 계산실행
				</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-4 d-flex justify-content-center align-items-start">
			<div class="card border shadow-sm bg-white overflow-hidden" style="max-width: 600px; width: 100%; border-radius: 8px;">
				<div class="card-header bg-white py-2 px-3 border-bottom text-center">
					<h6 class="fw-bold mb-0 text-dark small"><i class="bi bi-gear-fill me-2 text-primary"></i>감가상각 계산 조건 설정</h6>
				</div>
				<div class="card-body p-0 bg-white">
					<!-- 표준 erp-table-full을 이용한 조건 입력 -->
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">대상연도</th>
								<td class="px-3">
									<select v-model="form.yy" class="form-select" style="width: 150px;">
										<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년도</option>
									</select>
								</td>
							</tr>
							<tr>
								<th class="required">대 상 월</th>
								<td class="px-3">
									<select v-model="form.mm" class="form-select" style="width: 120px;">
										<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
									</select>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="card-body p-4">
					<div class="alert alert-info border-0 shadow-sm small py-3 px-4 m-0" style="background-color: #f0f9ff; color: #0369a1;">
						<div class="d-flex mb-2">
							<i class="bi bi-info-circle-fill me-2 fs-5"></i>
							<span>해당 월의 변동자산에 대한 감가상각을 일괄 계산합니다.</span>
						</div>
						<div class="d-flex">
							<i class="bi bi-info-circle-fill me-2 fs-5"></i>
							<span>12월 작업 시에는 차기연도 감가상각 기초 데이터를 함께 생성합니다.</span>
						</div>
					</div>
				</div>
				<div class="card-footer p-3 bg-light text-center border-top">
					<p class="text-muted mb-0" style="font-size: 11px;">※ 계산 작업은 취소할 수 없으므로 대상 연월을 반드시 확인하십시오.</p>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')

const yearOptions = Array.from({ length: 5 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const form = reactive({
	yy: String(currentYear),
    mm: currentMonth
})

async function save() {
	if (!confirm(`${form.yy}년 ${form.mm}월의 감가상각 계산작업을 진행하시겠습니까?`)) return

	try {
		const res = await api.post('/api/hafa/HAFA_090U_STR', {
			cmpycd: authStore.cmpycd,
			baseym: form.yy + form.mm
		})

		if (res.data && res.data[0]) {
			const result = res.data[0]
			const code = result.col0 || result.COL_0
			const msg = result.col1 || result.COL_1

			if (code === 'Y') {
				vAlert(msg || '감가상각 계산이 완료되었습니다.')
			} else {
				vAlertError(msg || '계산 처리 중 오류가 발생했습니다.')
			}
		} else {
			vAlert('정상적으로 작업이 완료되었습니다.')
		}
	} catch (e) {
		vAlertError('서버 통신 중 오류가 발생했습니다.')
	}
}
</script>
