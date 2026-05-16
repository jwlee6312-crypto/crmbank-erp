<!--
	=============================================================
	프로그램명	  : 제조원가계산작업
    프로그램 ID	: HFMF211U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 제조원가계산작업 (FMF2110U_STR 연결 - 전수조사 규격 반영)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hfmf211u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
				원가관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">제조원가계산작업 (HFMF211U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="handleExecute">계산실행</button>
				<button class="btn-erp btn-danger" @click="handleCancel">계산취소</button>
			</div>
		</div>

		<!-- 2. 처리 대상 및 조건 -->
		<div class="card shadow-sm border-0 mb-1 flex-shrink-0">
			<div class="card-header bg-white py-1 fw-bold small border-bottom">☞ 처리대상</div>
			<div class="card-body p-3">
				<table class="erp-table-full">
					<colgroup>
						<col style="width: 180px"><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="bg-light text-center">1.최종원가작업년월</th>
							<td>
								<input :value="clsInfo.CLOSE_YYMM" class="form-control form-control-sm bg-light text-center fw-bold" readonly style="width: 120px;" />
							</td>
						</tr>
						<tr>
							<th class="bg-light text-center required">2.제조원가작업년월</th>
							<td>
								<div class="d-flex align-items-center gap-2">
									<input v-model="searchForm.YM" type="month" class="form-control form-control-sm d-inline-block w-auto" @change="handleSearch" />
                                    <span class="text-danger small fw-bold">※ 공정별 배부작업 후 원가작업을 진행하세요.</span>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

        <!-- 3. 도움말 및 안내 -->
        <div class="alert alert-info py-2 px-3 mb-1 small d-flex flex-column gap-1 border-0 shadow-sm">
            <div class="fw-bold text-primary"><i class="bi bi-info-circle-fill me-2"></i>작업 안내</div>
            <div class="ps-4">1. 재료비 생성 → 2. 노무비/제조경비 배부 → 3. 외주가공 → 4. 재공품 원가 생성 → 5. 제품별 원가 생성 (순차 자동 처리)</div>
            <div class="ps-4">※ 재작업 시 기존 내용을 취소(D0)한 후 다시 작업하십시오.</div>
        </div>

		<!-- 4. 결과 그리드 -->
		<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary">
			<div class="card-header bg-white py-1 px-2 fw-bold small border-bottom">
				<span>공정별 배부 전/후 원가 결과</span>
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
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const clsInfo = reactive({ CLOSE_YYMM: '', CHECK_YYMM: '', NEXT_YYMM: '', WCLSYM: '' })
const searchForm = reactive({ YM: new Date().toISOString().substring(0, 7) })

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null

const logSsms = (act: string, ym: string, jobord: string = '') => {
    console.log(`📋 [SSMS Query] EXEC FMF2110U_STR '${authStore.CMPYCD}', '${act}', '${ym}', '${jobord}', '${authStore.USERID}', '', ''`)
}

// 💡 프로시저 응답 결과 처리 헬퍼 (위치 기반 매핑)
const parseProcResult = (data: any) => {
    if (!data) return { status: 'N', message: '응답 데이터가 없습니다.', values: [] };
    const values = Object.values(data);
    return {
        status: (values[0] || 'N').toString().toUpperCase(),
        message: (values[1] || '').toString(),
        values: values
    };
}

const loadInitData = async () => {
	try {
		const [res, cls] = await Promise.all([
			api.post('/api/hfmf/FMF2110U_STR', {
                CMPYCD: authStore.CMPYCD,
                ACTKIND: 'S0',
                YM: '', JOBORD: '', USERID: authStore.USERID, MsgYn: '', MsgText: ''
            }),
			api.get('/api/comm/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } })
		])

		if (res.data && res.data[0]) {
            const d = res.data[0];
            // 필드명 유연하게 매핑 (대소문자 및 인덱스)
            const v = Object.values(d);
			clsInfo.CLOSE_YYMM = String(d.CLOSE_YYMM || v[0] || '');
		}
        if (cls.data?.length > 0) {
            clsInfo.WCLSYM = cls.data[0].WCLSYM || cls.data[0].CODECD || '';
        }
	} catch (e) {
        console.error('기초 데이터 로드 실패', e)
    }
}

const handleSearch = async () => {
	try {
		const ym = searchForm.YM.replace('-', '')
        logSsms('S2', ym)
		const { data } = await api.post('/api/hfmf/FMF2110U_STR', {
            CMPYCD: authStore.CMPYCD,
            ACTKIND: 'S2',
            YM: ym,
            JOBORD: '',
            USERID: authStore.USERID,
            MsgYn: '',
            MsgText: ''
        })
		mainGrid?.setData(data)
	} catch (e) {
        vAlertError('조회 실패')
    }
}

// 계산 실행 (A0)
const handleExecute = async () => {
	const ym = searchForm.YM.replace('-', '')
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')

	if (!confirm('제조원가계산 작업을 시작하시겠습니까? (상당한 시간이 소요될 수 있습니다)')) return

	try {
		vAlert('원가 계산을 시작합니다...');

        const steps = [
            { val: '1', msg: '재료비 생성' },
            { val: '2', msg: '노무비/제조경비 배부' },
            { val: '3', msg: '외주가공비 집계' },
            { val: '4', msg: '재공품 원가 생성' },
            { val: '5', msg: '제품별 원가 생성' }
        ];

        for (const step of steps) {
            logSsms('A0', ym, step.val)
            const res = await api.post('/api/hfmf/FMF2110U_STR', {
                CMPYCD: String(authStore.CMPYCD),
                ACTKIND: 'A0',
                YM: String(ym),
                JOBORD: String(step.val),
                USERID: String(authStore.USERID),
                MsgYn: '',
                MsgText: ''
            });

            const result = parseProcResult(res.data[0]);
            if (result.status !== 'Y') {
                throw new Error(`${step.msg} 실패: ${result.message}`);
            }
        }

		vAlert('제조원가계산 작업이 성공적으로 완료되었습니다.');
		handleSearch();
	} catch (e: any) {
        vAlertError(e.message || '작업 중 오류 발생');
    }
}

// 계산 취소 (D0)
const handleCancel = async () => {
	const ym = searchForm.YM.replace('-', '')
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')

	if (!confirm('제조원가계산 내용을 취소하시겠습니까?')) return

	try {
        logSsms('D0', ym)
		await api.post('/api/hfmf/FMF2110U_STR', {
            CMPYCD: authStore.CMPYCD,
            ACTKIND: 'D0',
            YM: ym,
            JOBORD: '',
            USERID: authStore.USERID,
            MsgYn: '',
            MsgText: ''
        })
		vAlert('취소되었습니다.')
		mainGrid?.clearData()
	} catch (e) {
        vAlertError('취소 중 오류 발생')
    }
}

function initialize() {
    resetForm(searchForm);
    loadInitData();
    mainGrid?.clearData();
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
            height: '100%',
			columnDefaults: { minWidth: 100, headerSort: false },
			columns: [
				{ title: '공정', field: 'LINENM', hozAlign: 'center', width: 150, frozen: true },
                {
                    title: '배부 전',
                    columns: [
                        { title: '재료비', field: 'MATLCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
                        { title: '노무비', field: 'MANCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
                        { title: '제조경비', field: 'EXPCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
                        { title: '외주비', field: 'OUTCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
                        { title: '합계', field: 'TOT', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'bg-light' }
                    ]
                },
                {
                    title: '배부 후',
                    columns: [
                        { title: '재료비', field: 'BMATLCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
                        { title: '노무비', field: 'BMANCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
                        { title: '제조경비', field: 'BEXPCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
                        { title: '외주비', field: 'BOUTCOST', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
                        { title: '합계', field: 'BTOT', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'bg-info-subtle fw-bold' }
                    ]
                }
			]
		})
	}
    loadInitData().then(() => handleSearch())
})
</script>

<style scoped>
.hfmf211u-wrapper { height: calc(100vh - 110px); }
.tabulator-full-height { height: 100% !important; min-height: 350px; }
.form-table th { background-color: #f8f9fa; font-weight: bold; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; border: none; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #005a9f !important; color: #fff !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; }
</style>
