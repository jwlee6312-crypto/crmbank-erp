<!--
	=============================================================
	프로그램명	  : 수불부 원가 생성작업 (Legacy MS-SQL 이관)
    프로그램 ID	: HFMF301U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 공통 마감 정보 조회 및 수불부 원가 생성 (FMF3010U_STR 연결)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hfmf301u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small"><i class="bi bi-arrow-repeat me-2 text-primary"></i>원가관리 >> 수불부 원가 생성작업</div>
			<div class="btn-group shadow-sm">
				<button class="btn btn-sm btn-primary px-4 fw-bold" @click="handleExecute"><i class="bi bi-play-fill me-1"></i>생성실행</button>
			</div>
		</div>

		<!-- 2. 처리 대상 및 조건 -->
		<div class="card shadow-sm border-0 mb-1 flex-shrink-0">
			<div class="card-header bg-white py-1 fw-bold small border-bottom">☞ 처리대상</div>
			<div class="card-body p-3">
				<table class="table table-sm table-bordered form-table mb-0 small">
					<colgroup>
						<col style="width: 180px"><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="bg-light text-center">1.최종마감작업년월</th>
							<td>
								<input :value="clsInfo.WCLSYM" class="form-control form-control-sm bg-light text-center fw-bold" readonly style="width: 120px;" />
							</td>
						</tr>
						<tr>
							<th class="bg-light text-center required">2.작업 년 월</th>
							<td>
								<input v-model="searchForm.YM" type="month" class="form-control form-control-sm d-inline-block w-auto" />
							</td>
						</tr>
						<tr>
							<th class="bg-light text-center">3.생성 대상</th>
							<td>
								<div class="d-flex gap-4 mt-1">
									<div class="form-check">
										<input v-model="targetForm.useyn1" class="form-check-input" type="checkbox" id="chk1" true-value="Y" false-value="N">
										<label class="form-check-label fw-bold" for="chk1">재공품수불부</label>
									</div>
									<div class="form-check">
										<input v-model="targetForm.useyn2" class="form-check-input" type="checkbox" id="chk2" true-value="Y" false-value="N">
										<label class="form-check-label fw-bold" for="chk2">제품수불부</label>
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 3. 작업 결과 -->
		<div class="card shadow-sm border-0 flex-grow-1 border-top border-3 border-success">
			<div class="card-header bg-white py-1 fw-bold small border-bottom">☞ 작업결과</div>
			<div class="card-body p-3">
				<div class="row g-4 mb-4">
					<div class="col-md-6">
						<label class="small fw-bold mb-2 text-primary"><i class="bi bi-clock-history me-1"></i>작업시간</label>
						<div class="input-group input-group-sm mb-2">
							<span class="input-group-text bg-light" style="width: 120px;">시작시간</span>
							<input :value="resultInfo.startdt" class="form-control text-end bg-light" readonly />
						</div>
						<div class="input-group input-group-sm">
							<span class="input-group-text bg-light" style="width: 120px;">종료시간</span>
							<input :value="resultInfo.enddt" class="form-control text-end bg-light" readonly />
						</div>
					</div>
				</div>
                <div class="alert alert-warning py-2 px-3 small border-0 shadow-sm">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    품목별 제조원가를 제품 및 재공품의 수불부 입고금액을 적용하여 출고 재고금액을 생성합니다.
                </div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const clsInfo = reactive({ WCLSYM: '' })
const searchForm = reactive({ YM: new Date().toISOString().substring(0, 7) })
const targetForm = reactive({ useyn1: 'Y', useyn2: 'Y' })
const resultInfo = reactive({ startdt: '-', enddt: '-' })

const loadClsInfo = async () => {
	try {
		// 💡 시스템 표준 공통 마감 정보 조회(HP00_000S_STR 'CL')
		const res = await api.get('/api/comm/HP00_000S_STR', {
			params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD }
		})
        if (res.data?.length > 0) {
            clsInfo.WCLSYM = res.data[0].WCLSYM || res.data[0].CODECD || ''
        }
	} catch (e) {
		console.error('마감 정보 로드 실패', e)
	}
}

const handleExecute = async () => {
	const ym = searchForm.YM.replace('-', '')
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감된 자료는 수불부 원가 재성성이 불가합니다.')
	if (targetForm.useyn1 === 'N' && targetForm.useyn2 === 'N') return vAlertError('생성 대상을 선택하세요.')

	if (!confirm('수불부 원가 생성작업을 하시겠습니까?')) return

	try {
		resultInfo.startdt = new Date().toLocaleTimeString()

		// 💡 수정한 XML 규격에 맞춰 5개 파라미터 전송 (CMPYCD, ACTKIND, YM, JAGBN, USERID)
        let jagbn = '00'
        if (targetForm.useyn1 === 'Y' && targetForm.useyn2 === 'Y') jagbn = '11'
        else if (targetForm.useyn1 === 'Y') jagbn = '10'
        else if (targetForm.useyn2 === 'Y') jagbn = '01'

		await api.post('/api/hfmf/FMF3010U_STR', {
            CMPYCD: String(authStore.CMPYCD),
            ACTKIND: 'A0',
			YM: String(ym),
			JAGBN: String(jagbn),
            USERID: String(authStore.USERID)
		})

		vAlert('정상적으로 작업이 완료되었습니다.')
		resultInfo.enddt = new Date().toLocaleTimeString()

	} catch (e) {
        vAlertError('작업 중 오류 발생')
    }
}

onMounted(() => {
	loadClsInfo()
})
</script>

<style scoped>
.hfmf301u-wrapper { height: calc(100vh - 110px); }
.form-table th { background-color: #f8f9fa; font-weight: bold; }
</style>
