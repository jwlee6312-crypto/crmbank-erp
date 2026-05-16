<!--
	=============================================================
	프로그램명	  : 부문/작업장 배부기준 등록
    프로그램 ID	: HFBA104U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 부문/작업장 배부기준 등록 (FBA1040U_STR 연결)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<Modal v-model:visible="showModal" :modalProps="modalProps" />

	<div class="hfba104u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small"><i class="bi bi-diagram-3-fill me-2 text-primary"></i>원가관리 >> 배부기준관리</div>
			<div class="btn-group shadow-sm">
				<button class="btn btn-sm btn-outline-secondary px-3" @click="handleReset">초기화</button>
				<button class="btn btn-sm btn-dark px-3" @click="handleSearch">조회</button>
				<button class="btn btn-sm btn-danger px-3" @click="handleDelete" :disabled="detailForm.mode !== 'U'">삭제</button>
				<button class="btn btn-sm btn-primary px-4 fw-bold" @click="handleSave"><i class="bi bi-save me-1"></i>저장</button>
			</div>
		</div>

		<!-- 2. 검색 및 입력 폼 -->
		<div class="card shadow-sm border-0 mb-1 flex-shrink-0">
			<div class="card-body p-2 px-3">
				<div class="row g-3 align-items-center mb-2">
					<div class="col-auto">
						<label class="small fw-bold me-2 required-label">마감년월:</label>
						<input v-model="searchForm.YM" type="month" class="form-control form-control-sm d-inline-block w-auto" @change="handleSearch" />
					</div>
					<div class="col-auto ms-auto">
						<div v-if="clsInfo.WCLSYM" class="badge bg-danger p-2">마감월: {{ clsInfo.WCLSYM }}</div>
					</div>
				</div>
				<table class="table table-sm table-bordered form-table mb-0 small">
					<colgroup>
						<col style="width: 10%"><col style="width: 23%"><col style="width: 10%"><col style="width: 23%"><col style="width: 10%"><col style="width: 24%">
					</colgroup>
					<tbody>
						<tr>
							<th class="bg-light text-end pe-2 required-label">계정과목</th>
							<td>
								<select v-model="detailForm.ACCT" class="form-select form-select-sm" @change="onAcctChange">
									<option v-for="opt in acctOptions" :key="opt.ACCT" :value="opt.ACCT">{{ opt.ACCTNM }}</option>
								</select>
							</td>
							<th class="bg-light text-end pe-2 required-label">공정배부</th>
							<td>
								<select v-model="detailForm.DIVIDE1" class="form-select form-select-sm">
									<option v-for="opt in divide1Options" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
								</select>
							</td>
							<th class="bg-light text-end pe-2 required-label">품목배부</th>
							<td>
								<select v-model="detailForm.DIVIDE2" class="form-select form-select-sm">
									<option v-for="opt in divide2Options" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
								</select>
							</td>
						</tr>
						<tr>
							<th class="bg-light text-end pe-2">비고</th>
							<td colspan="5"><input v-model="detailForm.BIGO" class="form-control form-control-sm" maxlength="50" /></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 3. 메인 그리드 -->
		<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary">
			<div class="card-header bg-white py-1 px-2 fw-bold small border-bottom d-flex justify-content-between align-items-center">
				<span>배부기준 목록</span>
			</div>
			<div class="card-body p-0 flex-grow-1 bg-white">
				<div ref="mainGridRef" class="tabulator-full-height" />
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

// 상태 관리
const clsInfo = reactive({ WCLSYM: '' })
const acctOptions = ref<any[]>([])
const divide1Options = ref<any[]>([])
const divide2Options = ref<any[]>([])

const searchForm = reactive({ YM: new Date().toISOString().substring(0, 7) })
const detailForm = reactive({
	ACCT: '',
    ACCTNM: '',
    DIVIDE1: '',
    DIVIDE2: '',
    BIGO: '',
    mode: 'N'
})

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null
const showModal = ref(false)
const modalProps = ref<any>({})

const onAcctChange = () => {
    const selected = acctOptions.value.find(o => o.ACCT === detailForm.ACCT)
    if (selected) detailForm.ACCTNM = selected.ACCTNM
}

const handleReset = () => {
	Object.assign(detailForm, {
        ACCT: acctOptions.value[0]?.ACCT || '',
        ACCTNM: acctOptions.value[0]?.ACCTNM || '',
        DIVIDE1: divide1Options.value[0]?.CODE || '',
        DIVIDE2: divide2Options.value[0]?.CODE || '',
        BIGO: '',
        mode: 'N'
    })
	mainGrid?.deselectRow()
}

const loadInitData = async () => {
	try {
		const [cls, accts, d1, d2] = await Promise.all([
			api.post('/api/hfba/FBA3010U_STR', { CMPYCD: authStore.CMPYCD, ACTKIND: 'S1', YYMM: '', YN: 'N', REMARK: '', UPDEMP: authStore.USERID }),
			api.post('/api/hfba/SELECT_ACCT_LIST', { CMPYCD: authStore.CMPYCD }),
			api.post('/api/hfba/SELECT_DIVIDE_LIST', { CMPYCD: authStore.CMPYCD, CDKD: '1030' }), // 공정배부
			api.post('/api/hfba/SELECT_DIVIDE_LIST', { CMPYCD: authStore.CMPYCD, CDKD: '1040' })  // 품목배부
		])

		const clsRows = cls.data
		if (Array.isArray(clsRows) && clsRows.length > 0) {
			clsInfo.WCLSYM = clsRows[0].WCLSYM || clsRows[0].wclsym || ''
		}

		acctOptions.value = (accts.data || []).map((item: any) => ({
			ACCT: String(item.ACCT || item.acct || '').trim(),
			ACCTNM: String(item.ACCTNM || item.acctnm || '').trim()
		}))

		divide1Options.value = (d1.data || []).map((item: any) => ({
			CODE: String(item.CODE || item.code || '').trim(),
			CDNM: String(item.CDNM || item.cdnm || '').trim()
		}))

		divide2Options.value = (d2.data || []).map((item: any) => ({
			CODE: String(item.CODE || item.code || '').trim(),
			CDNM: String(item.CDNM || item.cdnm || '').trim()
		}))

		if (acctOptions.value.length > 0 && !detailForm.ACCT) {
            detailForm.ACCT = acctOptions.value[0].ACCT
            detailForm.ACCTNM = acctOptions.value[0].ACCTNM
        }
		if (divide1Options.value.length > 0 && !detailForm.DIVIDE1) detailForm.DIVIDE1 = divide1Options.value[0].CODE
		if (divide2Options.value.length > 0 && !detailForm.DIVIDE2) detailForm.DIVIDE2 = divide2Options.value[0].CODE

	} catch (e) {
		vAlertError('기초 데이터 로드 실패')
	}
}

const handleSearch = async () => {
	try {
		const ym = searchForm.YM.replace('-', '')
		const payload = {
			CMPYCD: authStore.CMPYCD,
            ACTKIND: 'S0',
            YM: ym,
            ACCT: '', ACCTNM: '',
            DIVIDE1: '', DIVIDE2: '', DIVIDE3: '', DIVIDE4: '',
            DIVIDE5: '', DIVIDE6: '', DIVIDE7: '',
            BIGO: '', USERID: authStore.USERID
        }

		console.log(`📋 [SSMS Query] EXEC FBA1040U_STR '${payload.CMPYCD}', '${payload.ACTKIND}', '${payload.YM}', '${payload.ACCT}', '${payload.ACCTNM}', '${payload.DIVIDE1}', '${payload.DIVIDE2}', '${payload.DIVIDE3}', '${payload.DIVIDE4}', '${payload.DIVIDE5}', '${payload.DIVIDE6}', '${payload.DIVIDE7}', '${payload.BIGO}', '${payload.USERID}'`)

		const { data } = await api.post('/api/hfba/FBA1040U_STR', payload)
		mainGrid?.setData(data)
	} catch (e) {
        vAlertError('조회 실패')
    }
}

const handleSave = async () => {
	const ym = searchForm.YM.replace('-', '')
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')
	if (!detailForm.ACCT || !detailForm.DIVIDE1 || !detailForm.DIVIDE2) return vAlertError('필수 항목을 선택하세요.')

	if (!confirm('저장하시겠습니까?')) return
	try {
		const payload = {
            CMPYCD: authStore.CMPYCD,
            ACTKIND: 'A0', // 💡 수정은 삭제 후 등록이므로 항상 A0
            YM: ym,
            ACCT: String(detailForm.ACCT || ''),
            ACCTNM: String(detailForm.ACCTNM || ''),
            DIVIDE1: String(detailForm.DIVIDE1 || ''),
            DIVIDE2: String(detailForm.DIVIDE2 || ''),
            DIVIDE3: '', DIVIDE4: '', DIVIDE5: '', DIVIDE6: '', DIVIDE7: '',
            BIGO: String(detailForm.BIGO || ''),
            USERID: authStore.USERID
        }

		console.log(`📋 [SSMS Query] EXEC FBA1040U_STR '${payload.CMPYCD}', '${payload.ACTKIND}', '${payload.YM}', '${payload.ACCT}', '${payload.ACCTNM}', '${payload.DIVIDE1}', '${payload.DIVIDE2}', '${payload.DIVIDE3}', '${payload.DIVIDE4}', '${payload.DIVIDE5}', '${payload.DIVIDE6}', '${payload.DIVIDE7}', '${payload.BIGO}', '${payload.USERID}'`)

		await api.post('/api/hfba/FBA1040U_STR', payload)
		vAlert('저장되었습니다.')
		handleSearch()
	} catch (e) {
        vAlertError('저장 실패')
    }
}

const handleDelete = async () => {
	const ym = searchForm.YM.replace('-', '')
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')
    if (!confirm('정말 삭제하시겠습니까?')) return
    try {
        const payload = {
            CMPYCD: authStore.CMPYCD,
            ACTKIND: 'D0',
            YM: ym,
            ACCT: String(detailForm.ACCT || ''),
            ACCTNM: '', DIVIDE1: '', DIVIDE2: '', DIVIDE3: '', DIVIDE4: '',
            DIVIDE5: '', DIVIDE6: '', DIVIDE7: '', BIGO: '',
            USERID: authStore.USERID
        }

		console.log(`📋 [SSMS Query] EXEC FBA1040U_STR '${payload.CMPYCD}', '${payload.ACTKIND}', '${payload.YM}', '${payload.ACCT}', '${payload.ACCTNM}', '${payload.DIVIDE1}', '${payload.DIVIDE2}', '${payload.DIVIDE3}', '${payload.DIVIDE4}', '${payload.DIVIDE5}', '${payload.DIVIDE6}', '${payload.DIVIDE7}', '${payload.BIGO}', '${payload.USERID}'`)

        await api.post('/api/hfba/FBA1040U_STR', payload)
        vAlert('삭제되었습니다.')
        handleReset()
        handleSearch()
    } catch (e) {
        vAlertError('삭제 실패')
    }
}

onMounted(async () => {
	await loadInitData()

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			selectable: 1,
			columnDefaults: { minWidth: 100, headerSort: false },
			columns: [
				{ title: '', field: '_sel', width: 40, hozAlign: 'center', formatter: 'rowSelection', titleFormatter: 'rowSelection' },
				{ title: '계정코드', field: 'ACCT', width: 100, hozAlign: 'center' },
				{ title: '계정과목명', field: 'ACCTNM', widthGrow: 2, hozAlign: 'left' },
				{ title: '공정배부', field: 'DIVIDE1', width: 100, hozAlign: 'center' },
				{ title: '공정배부기준', field: 'DIVIDE_NM1', widthGrow: 1.5, hozAlign: 'left' },
				{ title: '품목배부', field: 'DIVIDE2', width: 100, hozAlign: 'center' },
				{ title: '품목배부기준', field: 'DIVIDE_NM2', widthGrow: 1.5, hozAlign: 'left' },
				{ title: '비고', field: 'BIGO', widthGrow: 2, hozAlign: 'left' }
			]
		})
		mainGrid.on('rowSelected', (row) => {
			const data = row.getData()
			Object.assign(detailForm, {
				ACCT: data.ACCT,
                ACCTNM: data.ACCTNM,
                DIVIDE1: data.DIVIDE1,
                DIVIDE2: data.DIVIDE2,
                BIGO: data.BIGO || '',
                mode: 'U'
			})
		})
	}
	handleSearch()
})
</script>

<style scoped>
.hfba104u-wrapper { height: calc(100vh - 110px); }
.tabulator-full-height { height: 100% !important; min-height: 400px; }
.required-label::before { content: '* '; color: red; }
.form-table th { background-color: #f8f9fa; font-weight: bold; }
</style>
