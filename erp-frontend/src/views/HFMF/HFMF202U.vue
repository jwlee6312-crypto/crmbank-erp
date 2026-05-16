<!--
	=============================================================
	프로그램명	  : 공정별배부작업 (Legacy MS-SQL 이관)
    프로그램 ID	: HFMF202U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : ASP 파일을 통합하여 Vue로 이관 (MS-SQL 프로시저 호출)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hfmf202u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small"><i class="bi bi-diagram-2 me-2 text-primary"></i>원가관리 >> 공정별배부작업</div>
			<div class="btn-group shadow-sm">
				<button class="btn btn-sm btn-primary px-4 fw-bold" @click="handleExecute"><i class="bi bi-play-fill me-1"></i>배부실행</button>
				<button class="btn btn-sm btn-danger px-4 fw-bold" @click="handleCancel"><i class="bi bi-trash-fill me-1"></i>배부취소</button>
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
							<th class="bg-light text-center">1.최종배부작업년월</th>
							<td>
								<input :value="dvdYm" class="form-control form-control-sm bg-light" readonly style="width: 120px;" />
							</td>
						</tr>
						<tr>
							<th class="bg-light text-center">2.배부 작업 년월</th>
							<td>
								<div class="d-flex align-items-center gap-2">
									<input v-model="searchForm.ym" type="month" class="form-control form-control-sm d-inline-block w-auto" />
									<span class="text-primary small fw-bold">※ 원가마감 전 작업을 완료하세요.</span>
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
				<div class="row g-4">
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
					<div class="col-md-6">
						<label class="small fw-bold mb-2 text-success"><i class="bi bi-calculator me-1"></i>공정배부 금액</label>
						<div class="input-group input-group-sm mb-2">
							<span class="input-group-text bg-light" style="width: 120px;">배부전합계</span>
							<input :value="resultInfo.amt_bf" class="form-control text-end bg-light" readonly />
						</div>
						<div class="input-group input-group-sm">
							<span class="input-group-text bg-light" style="width: 120px;">배부후합계</span>
							<input :value="resultInfo.amt_af" class="form-control text-end bg-light" readonly />
						</div>
					</div>
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

const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const dvdYm = ref('')
const clsWclsym = ref('')
const searchForm = reactive({ ym: new Date().toISOString().substring(0, 7) })
const resultInfo = reactive({ startdt: '-', enddt: '-', amt_bf: '0', amt_af: '0' })

const loadInitData = async () => {
	try {
		const { data } = await api.post('/the/basicInfo/fmf/callFmf2020uStr', { actkind: 'S0' })
		if (data.data && data.data[0]) {
			dvdYm.value = data.data[0].DVDYM
			clsWclsym.value = data.data[0].WCLSYM
		}
	} catch (e) {}
}

// 배부 실행 (A1)
const handleExecute = async () => {
	const ym = searchForm.ym.replace('-', '')
	if (clsWclsym.value >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')

	if (!confirm('작업장 배부작업을 하시겠습니까?')) return

	try {
		resultInfo.startdt = new Date().toLocaleTimeString()
		const res = await api.post('/the/basicInfo/fmf/callFmf2020uStr', {
			ym,
			actkind: 'A1',
			val: ''
		})

		const d = res.data.data[0]
		if (d.RESULT === 'N') {
			vAlertError(d.MESSAGE)
		} else {
			Object.assign(resultInfo, {
				amt_bf: d.AMT_BF,
				amt_af: d.AMT_AF,
				enddt: new Date().toLocaleTimeString()
			})
			vAlert(d.MESSAGE || '배부작업이 완료되었습니다.')
		}
	} catch (e) { vAlertError('작업 중 오류 발생') }
}

// 배부 취소 (D0)
const handleCancel = async () => {
	const ym = searchForm.ym.replace('-', '')
	if (clsWclsym.value >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')

	if (!confirm('작업장 배부을 취소 하시겠습니까?')) return

	try {
		const res = await api.post('/the/basicInfo/fmf/callFmf2020uStr', { ym, actkind: 'D0' })
		vAlert(res.data.data[0].MESSAGE || '취소되었습니다.')
		Object.assign(resultInfo, { startdt: '-', enddt: '-', amt_bf: '0', amt_af: '0' })
	} catch (e) { vAlertError('취소 중 오류 발생') }
}

onMounted(() => {
	loadInitData()
})
</script>

<style scoped>
.hfmf202u-wrapper { height: calc(100vh - 110px); }
.form-table th { background-color: #f8f9fa; font-weight: bold; }
</style>
