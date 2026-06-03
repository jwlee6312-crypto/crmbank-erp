<!--
	=============================================================
	프로그램명	: 예산일괄배정
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 부서 범위 및 기간 범위를 지정하여 예산을 일괄 배정 또는 취소
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 (표준 규격) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-stack me-2 text-primary" style="font-size: 18px;"></i>
				예산관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">예산일괄배정 (HABG060U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-play-fill"></i> 작업실행
				</button>
			</div>
		</div>

		<!-- 🔍 작업 조건 영역 (통합CSS 표준 적용) -->
		<div class="p-4 flex-grow-1 bg-white overflow-auto">
			<div class="card border shadow-sm mx-auto" style="max-width: 800px;">
				<div class="card-header bg-white py-2 px-3 border-bottom">
					<h6 class="mb-0 fw-bold small text-dark"><i class="bi bi-gear-fill me-2 text-primary"></i>배정 작업 조건 설정</h6>
				</div>
				<div class="card-body p-4">
					<div class="alert alert-info py-2 px-3 mb-4 small d-flex align-items-center border-0 shadow-sm" style="background-color: #f0f9ff; color: #0369a1;">
						<i class="bi bi-info-circle-fill me-2 fs-5"></i>
						<span>조정금액을 일괄 배정합니다. 추가/조기/이월/전용 금액이 있으면 배정작업을 할 수가 없습니다.</span>
					</div>

					<div class="row g-4">
						<!-- 예산연월 범위 -->
						<div class="col-12">
							<div class="d-flex align-items-center">
								<span class="erp-label">예산연월</span>
								<div class="d-flex align-items-center gap-2 flex-grow-1">
									<div class="d-flex gap-1 flex-grow-1">
										<select v-model="form.bugtyyF" class="form-select">
											<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
										</select>
										<select v-model="form.bugtmmF" class="form-select">
											<option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
										</select>
									</div>
									<span class="text-muted fw-bold">~</span>
									<div class="d-flex gap-1 flex-grow-1">
										<select v-model="form.bugtyyT" class="form-select">
											<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
										</select>
										<select v-model="form.bugtmmT" class="form-select">
											<option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
										</select>
									</div>
								</div>
							</div>
						</div>

						<!-- 예산부서 범위 -->
						<div class="col-12">
							<div class="d-flex align-items-center">
								<span class="erp-label">예산부서</span>
								<div class="d-flex align-items-center gap-2 flex-grow-1">
									<div class="input-group flex-grow-1">
										<input v-model="form.deptcdF" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
										<input v-model="form.deptnmF" type="text" class="form-control" @keydown.enter="openHelp('DEPTF')" placeholder="시작 부서" />
										<button class="btn" @click="openHelp('DEPTF')"><i class="bi bi-search"></i></button>
									</div>
									<span class="text-muted fw-bold">~</span>
									<div class="input-group flex-grow-1">
										<input v-model="form.deptcdT" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
										<input v-model="form.deptnmT" type="text" class="form-control" @keydown.enter="openHelp('DEPTT')" placeholder="종료 부서" />
										<button class="btn" @click="openHelp('DEPTT')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</div>
						</div>

						<!-- 작업방법 -->
						<div class="col-12">
							<div class="d-flex align-items-center">
								<span class="erp-label">작업방법</span>
								<select v-model="form.wkgbn" class="form-select flex-grow-1 fw-bold">
									<option value="Y">일괄배정 작업을 합니다.</option>
									<option value="N">일괄배정 취소작업을 합니다.</option>
								</select>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 7 }, (_, i) => String(currentYear + 1 - i))

const bgType = ref('010')

const form = reactive({
	bugtyyF: String(currentYear + 1),
	bugtmmF: '01',
	bugtyyT: String(currentYear + 1),
	bugtmmT: String(new Date().getMonth() + 1).padStart(2, '0'),
	deptcdF: '',
	deptnmF: '',
	deptcdT: '',
	deptnmT: '',
	wkgbn: 'Y'
})

const periodOptions = computed(() => {
	if (bgType.value === '020') {
		return [
			{ value: '01', text: '01 분기' }, { value: '04', text: '02 분기' },
			{ value: '07', text: '03 분기' }, { value: '10', text: '04 분기' }
		]
	} else if (bgType.value === '030') {
		return [{ value: '01', text: '상반기' }, { value: '07', text: '하반기' }]
	} else if (bgType.value === '040') {
		return [{ value: '01', text: '전체' }]
	}
	return Array.from({ length: 12 }, (_, i) => ({
		value: String(i + 1).padStart(2, '0'),
		text: `${String(i + 1).padStart(2, '0')} 월`
	}))
})

const save = async () => {
	if (!form.deptcdF || !form.deptcdT) return vAlertError('예산부서 범위를 지정해 주십시오.')

	const msg = form.wkgbn === 'Y' ? '배정 작업을 하시겠습니까?' : '배정 취소작업을 하시겠습니까?'
	if (!confirm(msg)) return

	try {
		const res = await api.post('/api/habg/HABG_060U_STR', {
			cmpycd: authStore.cmpycd,
			BUGTymF: `${form.bugtyyF}${form.bugtmmF}`,
			BUGTymT: `${form.bugtyyT}${form.bugtmmT}`,
			deptcdF: form.deptcdF,
			deptcdT: form.deptcdT,
			wkgbn: form.wkgbn
		})

		if (res.data?.[0]?.col0 === '000') {
			vAlert('작업이 완료되었습니다.')
		} else {
			vAlertError(res.data?.[0]?.col1 || '작업 중 오류가 발생했습니다.')
		}
	} catch (e) { vAlertError('서버 통신 오류') }
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: 'DEPTF' | 'DEPTT') {
	const searchVal = type === 'DEPTF' ? form.deptnmF : form.deptnmT
    Object.assign(modalProps, {
        title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
        data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: searchVal },
        columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
        onConfirm: (d: any) => {
			if (type === 'DEPTF') { form.deptcdF = d.deptcd; form.deptnmF = d.deptnm }
			else { form.deptcdT = d.deptcd; form.deptnmT = d.deptnm }
		}
    })
	modalVisible.value = true
}

const fetchBgType = async () => {
	try {
		const res = await api.post('/api/habg/GET_BGTYPE', { cmpycd: authStore.cmpycd })
		bgType.value = res.data || '010'
	} catch (e) { bgType.value = '010' }
}

onMounted(() => {
	fetchBgType()
	form.deptcdF = authStore.deptcd || ''
	form.deptnmF = authStore.deptnm || ''
	form.deptcdT = authStore.deptcd || ''
	form.deptnmT = authStore.deptnm || ''
})
</script>
