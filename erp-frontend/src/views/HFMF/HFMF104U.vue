<!--
	=============================================================
	프로그램명	  : 제조비용 등록
    프로그램 ID	: HFMF104U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 제조비용 등록 (FMF1040U_STR 연결)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hfmf104u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small"><i class="bi bi-cash-stack me-2 text-primary"></i>원가관리 >> 제조비용관리</div>
			<div class="btn-group shadow-sm">
				<button class="btn btn-sm btn-outline-secondary px-3" @click="handleReset">초기화</button>
				<button class="btn btn-sm btn-dark px-3" @click="handleSearch">조회</button>
				<button class="btn btn-sm btn-danger px-3" @click="handleDelete" :disabled="detailForm.mode !== 'U'">삭제</button>
				<button class="btn btn-sm btn-primary px-4 fw-bold" @click="handleSave"><i class="bi bi-save me-1"></i>저장</button>
			</div>
		</div>

		<!-- 2. 입력 및 수정 폼 -->
		<div class="card shadow-sm border-0 mb-1 flex-shrink-0">
			<div class="card-body p-2 px-3">
				<table class="table table-sm table-bordered form-table mb-0 small">
					<colgroup>
						<col style="width: 10%"><col style="width: 23%"><col style="width: 10%"><col style="width: 23%"><col style="width: 10%"><col style="width: 24%">
					</colgroup>
					<tbody>
						<tr>
							<th class="bg-light text-end pe-2 required-label">년월</th>
							<td><input v-model="searchForm.YM" type="month" class="form-control form-control-sm" @change="handleSearch" /></td>
							<th class="bg-light text-end pe-2">계정과목</th>
							<td>
								<input v-model="detailForm.ACCTNM" class="form-control form-control-sm bg-light" readonly placeholder="그리드에서 선택하세요" />
							</td>
							<th class="bg-light text-end pe-2 required-label">비용금액</th>
							<td><input v-model="detailForm.COSTAMT" type="number" class="form-control form-control-sm text-end fw-bold text-primary" placeholder="0" /></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 3. 메인 그리드 -->
		<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary">
			<div class="card-header bg-white py-1 px-2 fw-bold small border-bottom">제조비용 목록 (행 클릭 시 상단에 표시됩니다)</div>
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

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const searchForm = reactive({ YM: new Date().toISOString().substring(0, 7) })
const detailForm = reactive({ ACCT: '', ACCTNM: '', COSTAMT: 0, mode: 'N' })

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null

const handleReset = () => {
    Object.assign(detailForm, { ACCT: '', ACCTNM: '', COSTAMT: 0, mode: 'N' });
    mainGrid?.deselectRow();
}

const handleSearch = async () => {
	try {
		const ym = searchForm.YM.replace('-', '')
		const payload = {
			CMPYCD: authStore.CMPYCD,
            ACTKIND: 'S0',
            YM: ym,
            COSTCD: '10000',
			ACCT: '',
			COSTAMT: '0',
			USERID: authStore.USERID
        }

		console.log(`📋 [SSMS Query] EXEC FMF1040U_STR '${payload.CMPYCD}', '${payload.ACTKIND}', '${payload.YM}', '${payload.COSTCD}', '${payload.ACCT}', '${payload.COSTAMT}', '${payload.USERID}'`)

		const { data } = await api.post('/api/hfmf/FMF1040U_STR', payload)
		mainGrid?.setData(data)
        handleReset()
	} catch (e) {
        vAlertError('조회 실패')
    }
}

const handleSave = async () => {
	if (!detailForm.ACCT) return vAlertError('항목을 그리드에서 선택하세요.')
    if (!confirm('저장하시겠습니까?')) return

	try {
		const payload = {
			CMPYCD: authStore.CMPYCD,
			ACTKIND: 'A0',
			YM: searchForm.YM.replace('-', ''),
			COSTCD: '10000',
			ACCT: String(detailForm.ACCT || ''),
			COSTAMT: String(detailForm.COSTAMT || '0'),
			USERID: authStore.USERID
		}

		console.log(`📋 [SSMS Query] EXEC FMF1040U_STR '${payload.CMPYCD}', '${payload.ACTKIND}', '${payload.YM}', '${payload.COSTCD}', '${payload.ACCT}', '${payload.COSTAMT}', '${payload.USERID}'`)

		await api.post('/api/hfmf/FMF1040U_STR', payload)
		vAlert('저장되었습니다.')
		handleSearch()
	} catch (e) {
        vAlertError('저장 실패')
    }
}

const handleDelete = async () => {
	if (!detailForm.ACCT) return vAlertError('삭제할 항목을 그리드에서 선택하세요.')
    if (!confirm('정말 삭제하시겠습니까?')) return
    try {
        const payload = {
            CMPYCD: authStore.CMPYCD,
            ACTKIND: 'D0',
            YM: searchForm.YM.replace('-', ''),
            COSTCD: '10000',
            ACCT: String(detailForm.ACCT || ''),
            COSTAMT: '0',
            USERID: authStore.USERID
        }

		console.log(`📋 [SSMS Query] EXEC FMF1040U_STR '${payload.CMPYCD}', '${payload.ACTKIND}', '${payload.YM}', '${payload.COSTCD}', '${payload.ACCT}', '${payload.COSTAMT}', '${payload.USERID}'`)

        await api.post('/api/hfmf/FMF1040U_STR', payload)
        vAlert('삭제되었습니다.')
        handleSearch()
    } catch (e) {
        vAlertError('삭제 실패')
    }
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			selectable: 1,
			columnDefaults: { minWidth: 100, headerSort: false },
			columns: [
				{ title: '계정코드', field: 'ACCT', hozAlign: 'center', width: 120 },
				{ title: '계정과목명', field: 'ACCTNM', widthGrow: 2, hozAlign: 'left' },
				{ title: '비용', field: 'DIRCAMT', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '비고', field: 'BIGO', widthGrow: 3, hozAlign: 'left' }
			]
		})

		mainGrid.on('rowClick', (e, row) => {
			const data = row.getData()
			detailForm.ACCT = String(data.ACCT || data.acct || '').trim()
			detailForm.ACCTNM = String(data.ACCTNM || data.acctnm || '').trim()
			detailForm.COSTAMT = Number(data.DIRCAMT || data.dircamt || 0)
			detailForm.mode = 'U'

			mainGrid?.deselectRow()
			row.select()
		})
	}
	handleSearch()
})
</script>

<style scoped>
.hfmf104u-wrapper { height: calc(100vh - 110px); }
.tabulator-full-height { height: 100% !important; min-height: 400px; }
.form-table th { background-color: #f8f9fa; font-weight: bold; }
</style>
