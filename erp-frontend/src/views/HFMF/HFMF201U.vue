<!--
	=============================================================
	프로그램명	  : 생산실적집계
    프로그램 ID	: HFMF201U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 공통 마감 정보 조회 및 집계 처리 (결과값 위치 기반 매핑 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hfmf201u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-stack me-2 text-primary" style="font-size: 18px;"></i>
				원가관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">생산실적집계 (HFMF201U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="handleExecute">집계실행</button>
				<button class="btn-erp btn-danger" @click="handleCancel">집계취소</button>
			</div>
		</div>

		<!-- 2. 처리 대상 및 조건 -->
		<div class="card shadow-sm border-0 mb-1 flex-shrink-0">
			<div class="card-header bg-white py-1 fw-bold small border-bottom">☞ 처리대상</div>
			<div class="card-body p-3">
				<table class="erp-table-full">
					<colgroup>
						<col style="width: 150px"><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="bg-light text-center">1.회계마감</th>
							<td>
								<div class="d-flex align-items-center gap-2">
									<input :value="clsInfo.WCLSYM" class="form-control form-control-sm bg-light text-center fw-bold" readonly style="width: 120px;" />
									<span class="text-danger small fw-bold">※ 생산시스템 마감 후 처리할 것.</span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="bg-light text-center required">2.연월</th>
							<td>
								<input v-model="searchForm.YM" type="text" maxlength="6" class="form-control form-control-sm text-center fw-bold" style="width: 120px;" placeholder="YYYYMM" />
							</td>
						</tr>
						<tr>
							<th class="bg-light text-center">3.집계대상</th>
							<td>
								<div class="d-flex gap-4 mt-1">
									<div class="form-check">
										<input v-model="targetForm.useyn1" class="form-check-input" type="checkbox" id="chk1" true-value="Y" false-value="N">
										<label class="form-check-label fw-bold" for="chk1">생산실적</label>
									</div>
									<div class="form-check">
										<input v-model="targetForm.useyn2" class="form-check-input" type="checkbox" id="chk2" true-value="Y" false-value="N">
										<label class="form-check-label fw-bold" for="chk2">품목별 배부적수</label>
									</div>
									<div class="form-check">
										<input v-model="targetForm.useyn3" class="form-check-input" type="checkbox" id="chk3" true-value="Y" false-value="N">
										<label class="form-check-label fw-bold" for="chk3">작업장 배부적수</label>
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
				<div class="row g-4">
					<div class="col-md-6">
						<label class="small fw-bold mb-2 text-primary"><i class="bi bi-clock-history me-1"></i>작업시간</label>
						<div class="input-group input-group-sm mb-2">
							<span class="input-group-text bg-light" style="width: 100px;">시작시간</span>
							<input :value="resultInfo.amt_bf" class="form-control text-end bg-light" readonly />
						</div>
						<div class="input-group input-group-sm">
							<span class="input-group-text bg-light" style="width: 100px;">종료시간</span>
							<input :value="resultInfo.amt_af" class="form-control text-end bg-light" readonly />
						</div>
					</div>
					<div class="col-md-6">
						<label class="small fw-bold mb-2 text-success"><i class="bi bi-graph-up me-1"></i>생산량</label>
						<div class="input-group input-group-sm mb-2">
							<span class="input-group-text bg-light" style="width: 100px;">집계전</span>
							<input :value="resultInfo.qty_bf" class="form-control text-end bg-light" readonly />
						</div>
						<div class="input-group input-group-sm">
							<span class="input-group-text bg-light" style="width: 100px;">집계후</span>
							<input :value="resultInfo.qty_af" class="form-control text-end bg-light" readonly />
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`

const clsInfo = reactive({ WCLSYM: '' })
const searchForm = reactive({ YM: initYM })
const targetForm = reactive({ useyn1: 'Y', useyn2: 'Y', useyn3: 'Y' })
const resultInfo = reactive({ amt_bf: '0', amt_af: '0', qty_bf: '0', qty_af: '0' })

const loadClsInfo = async () => {
	try {
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

const logSsms = (actkind: string, ym: string, colgbn: string) => {
    console.log(`📋 [SSMS Query] EXEC FMF2010U_STR '${authStore.CMPYCD}', '${actkind}', '${ym}', '${colgbn}', '${authStore.USERID}'`)
}

// 💡 프로시저 응답 결과 처리 헬퍼 (위치 기반 매핑: 0:상태, 1:메시지, 2~5:통계값)
const parseProcResult = (data: any) => {
    if (!data) return { status: 'N', message: '응답 데이터가 없습니다.', values: [] };

    // 컬럼명이 없을 때 발생하는 중복 키 문제를 피하기 위해 모든 값을 배열로 추출
    const values = Object.values(data);
    console.log('📋 Proc Result Values:', values);

    return {
        status: (values[0] || 'N').toString().toUpperCase(),
        message: (values[1] || '').toString(),
        values: values
    };
}

// 집계 실행 (A0)
const handleExecute = async () => {
	const ym = searchForm.YM
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')
	if (targetForm.useyn1 === 'N' && targetForm.useyn2 === 'N' && targetForm.useyn3 === 'N') {
		return vAlertError('적어도 하나 이상의 집계대상을 선택하세요.')
	}

	if (!confirm('생산실적집계 작업을 하시겠습니까?')) return

	try {
		// 1. 사전 체크 (ACTKIND: C0)
		logSsms('C0', ym, '2000')
		const checkRes = await api.post('/api/hfmf/FMF2010U_STR', {
			CMPYCD: String(authStore.CMPYCD),
			ACTKIND: 'C0',
			YM: String(ym),
			COLGBN: '2000',
            USERID: String(authStore.USERID)
		})

        const checkResult = parseProcResult(checkRes.data[0]);
		if (checkResult.status !== 'Y') {
            // 💡 "생산마감작업 후 작업하시기 바랍니다." 등의 메시지 출력
			return vAlertError(checkResult.message || '사전 체크 실패');
		}

		// 2. 생산실적 집계 (COLGBN: 2000)
		if (targetForm.useyn1 === 'Y') {
			logSsms('A0', ym, '2000')
			const res = await api.post('/api/hfmf/FMF2010U_STR', {
				CMPYCD: String(authStore.CMPYCD),
				ACTKIND: 'A0',
				YM: String(ym),
				COLGBN: '2000',
                USERID: String(authStore.USERID)
			})
			const execResult = parseProcResult(res.data[0]);
            if (execResult.status === 'Y') {
                const v = execResult.values;
			    Object.assign(resultInfo, {
                    // 💡 지시하신 순서대로 매핑: 2:amt_bf, 3:amt_af, 4:qty_bf, 5:qty_af
                    amt_bf: String(v[2] || '0'),
                    amt_af: String(v[3] || '0'),
                    qty_bf: String(v[4] || '0'),
                    qty_af: String(v[5] || '0')
                })
                vAlert(execResult.message || '생산실적 집계 완료');
            } else {
                return vAlertError(execResult.message);
            }
		}

		// 3. 품목별 배부적수 생성 (COLGBN: 9000)
		if (targetForm.useyn2 === 'Y') {
			logSsms('A0', ym, '9000')
			const res = await api.post('/api/hfmf/FMF2010U_STR', {
				CMPYCD: String(authStore.CMPYCD),
				ACTKIND: 'A0',
				YM: String(ym),
				COLGBN: '9000',
                USERID: String(authStore.USERID)
			})
            const resData = parseProcResult(res.data[0]);
            if (resData.status !== 'Y') return vAlertError(resData.message);
			vAlert(resData.message || '품목별 배부적수 생성 완료');
		}

		// 4. 작업장 배부적수 생성 (COLGBN: 8000)
		if (targetForm.useyn3 === 'Y') {
			logSsms('A0', ym, '8000')
			const res = await api.post('/api/hfmf/FMF2010U_STR', {
				CMPYCD: String(authStore.CMPYCD),
				ACTKIND: 'A0',
				YM: String(ym),
				COLGBN: '8000',
                USERID: String(authStore.USERID)
			})
            const resData = parseProcResult(res.data[0]);
            if (resData.status !== 'Y') return vAlertError(resData.message);
			vAlert(resData.message || '작업장 배부적수 생성 완료');
		}

		vAlert('전체 작업이 정상적으로 완료되었습니다.')
	} catch (e) {
        vAlertError('작업 중 오류 발생')
    }
}

// 집계 취소 (D0)
const handleCancel = async () => {
	const ym = searchForm.YM
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')

	if (!confirm('집계 작업을 취소하시겠습니까?')) return

	try {
		if (targetForm.useyn1 === 'Y') {
			logSsms('D0', ym, '2000')
			await api.post('/api/hfmf/FMF2010U_STR', {
				CMPYCD: String(authStore.CMPYCD),
				ACTKIND: 'D0',
				YM: String(ym),
				COLGBN: '2000',
                USERID: String(authStore.USERID)
			})
		}
		if (targetForm.useyn2 === 'Y') {
			logSsms('D0', ym, '9000')
			await api.post('/api/hfmf/FMF2010U_STR', {
				CMPYCD: String(authStore.CMPYCD),
				ACTKIND: 'D0',
				YM: String(ym),
				COLGBN: '9000',
                USERID: String(authStore.USERID)
			})
		}
		if (targetForm.useyn3 === 'Y') {
			logSsms('D0', ym, '8000')
			await api.post('/api/hfmf/FMF2010U_STR', {
				CMPYCD: String(authStore.CMPYCD),
				ACTKIND: 'D0',
				YM: String(ym),
				COLGBN: '8000',
                USERID: String(authStore.USERID)
			})
		}
		vAlert('정상적으로 취소되었습니다.')
		Object.assign(resultInfo, { amt_bf: '0', amt_af: '0', qty_bf: '0', qty_af: '0' })
	} catch (e) {
        vAlertError('취소 중 오류 발생')
    }
}

function initialize() {
    resetForm(searchForm);
    searchForm.YM = initYM;
    Object.assign(resultInfo, { amt_bf: '0', amt_af: '0', qty_bf: '0', qty_af: '0' });
}

onMounted(() => {
	loadClsInfo()
})
</script>

<style scoped>
.hfmf201u-wrapper { height: 100%; overflow: hidden; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 8px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
