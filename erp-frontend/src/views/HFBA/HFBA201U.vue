<!--
	=============================================================
	프로그램명	  : 제조원가 계정관리
    프로그램 ID	: HFBA201U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 제조원가 계정등록 (FBA2010U_STR 연결)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<Modal v-model:visible="showModal" :modalProps="modalProps" />

	<div class="hfba201u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small"><i class="bi bi-journal-check me-2 text-primary"></i>기본정보 >> 계정관리</div>
			<div class="btn-group shadow-sm">
				<button class="btn btn-sm btn-outline-secondary px-3" @click="handleReset">초기화</button>
				<button class="btn btn-sm btn-dark px-3" @click="handleSearch">조회</button>
				<button class="btn btn-sm btn-primary px-4 fw-bold" @click="handleSave"><i class="bi bi-save me-1"></i>저장</button>
				<button class="btn btn-sm btn-danger px-3" @click="handleDelete" :disabled="detailForm.mode === 'N'">삭제</button>
			</div>
		</div>

		<!-- 2. 상세 정보 입력 (상단 폼) -->
		<div class="card shadow-sm border-0 mb-1 flex-shrink-0">
			<div class="card-body p-2">
				<table class="table table-sm table-bordered form-table mb-0 small">
					<colgroup>
						<col style="width: 10%"><col style="width: 23%"><col style="width: 10%"><col style="width: 23%"><col style="width: 10%"><col style="width: 24%">
					</colgroup>
					<tbody>
						<tr>
							<th class="bg-light text-end pe-2 required-label">계정코드</th>
							<td><input v-model="detailForm.ACCT" class="form-control form-control-sm" maxlength="8" :readonly="detailForm.mode === 'U'" /></td>
							<th class="bg-light text-end pe-2 required-label">계정과목명</th>
							<td><input v-model="detailForm.ACCTNM" class="form-control form-control-sm" maxlength="50" /></td>
							<th class="bg-light text-end pe-2">외주가공</th>
							<td>
								<div class="form-check form-switch mt-1">
									<input v-model="detailForm.ACNTGBN" class="form-check-input" type="checkbox" true-value="1010" false-value="1000" id="acntGbnSwitch">
									<label class="form-check-label" for="acntGbnSwitch">{{ detailForm.ACNTGBN === '1010' ? '대상' : '일반' }}</label>
								</div>
							</td>
						</tr>
						<tr>
							<th class="bg-light text-end pe-2">비고</th>
							<td colspan="3"><input v-model="detailForm.BIGO" class="form-control form-control-sm" maxlength="50" /></td>
							<th class="bg-light text-end pe-2">사용여부</th>
							<td>
								<div class="form-check form-switch mt-1">
									<input v-model="detailForm.USEYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useynSwitch">
									<label class="form-check-label" for="useynSwitch">{{ detailForm.USEYN === 'Y' ? '사용' : '미사용' }}</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 3. 메인 그리드 -->
		<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary">
			<div class="card-header bg-white py-1 px-2 fw-bold small border-bottom">계정 목록</div>
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
const detailForm = reactive({
	ACCT: '',
	ACCT_O: '',
	ACCTNM: '',
	BIGO: '',
	ACNTGBN: '1000',
	USEYN: 'Y',
	mode: 'N'
})

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null
const showModal = ref(false)
const modalProps = ref<any>({})

// 초기화
const handleReset = () => {
	Object.assign(detailForm, {
		ACCT: '',
		ACCT_O: '',
		ACCTNM: '',
		BIGO: '',
		ACNTGBN: '1000',
		USEYN: 'Y',
		mode: 'N'
	})
	mainGrid?.deselectRow()
}

// 조회 (ACTKIND: S0)
const handleSearch = async () => {
	try {
		const { data } = await api.post('/api/hfba/FBA2010U_STR', {
            CMPYCD: authStore.CMPYCD,
            ACTKIND: 'S0',
            ACCT: '', ACCT_O: '', ACCTNM: '', BIGO: '', USEYN: 'Y', ACNTGBN: '', USERID: authStore.USERID
        })
		mainGrid?.setData(data)
	} catch (e) {
		vAlertError('조회 실패')
	}
}

// 저장 (ACTKIND: A0)
const handleSave = async () => {
	if (!detailForm.ACCT || !detailForm.ACCTNM) return vAlertError('계정코드와 과목명을 입력하세요.')
	if (!confirm('저장하시겠습니까?')) return
	try {
        const act = detailForm.mode === 'U' ? 'U0' : 'A0'
		await api.post('/api/hfba/FBA2010U_STR', {
			CMPYCD: authStore.CMPYCD,
            ACTKIND: act,
            ACCT: String(detailForm.ACCT || ''),
            ACCT_O: String(detailForm.ACCT_O || ''),
            ACCTNM: String(detailForm.ACCTNM || ''),
            BIGO: String(detailForm.BIGO || ''),
            USEYN: String(detailForm.USEYN || 'Y'),
            ACNTGBN: String(detailForm.ACNTGBN || '1000'),
            USERID: authStore.USERID
		})
		vAlert('성공적으로 처리되었습니다.')
		handleSearch()
		handleReset()
	} catch (e) {
		vAlertError('저장 실패')
	}
}

// 삭제 (ACTKIND: D0)
const handleDelete = async () => {
	if (detailForm.mode === 'N') return
	if (!confirm('정말 삭제하시겠습니까?')) return
	try {
		await api.post('/api/hfba/FBA2010U_STR', {
			CMPYCD: authStore.CMPYCD,
            ACTKIND: 'D0',
            ACCT: String(detailForm.ACCT || ''),
            ACCT_O: String(detailForm.ACCT_O || ''),
            ACCTNM: '', BIGO: '', USEYN: 'Y', ACNTGBN: '', USERID: authStore.USERID
		})
		vAlert('삭제되었습니다.')
		handleSearch()
		handleReset()
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
				{ title: '', field: '_sel', width: 40, hozAlign: 'center', formatter: 'rowSelection', titleFormatter: 'rowSelection' },
				{ title: '계정코드', field: 'ACCT', hozAlign: 'center' },
				{ title: '계정 명', field: 'ACCTNM', widthGrow: 2, hozAlign: 'left' },
				{ title: '사용', field: 'USEYN', hozAlign: 'center', formatter: (cell) => cell.getValue() === 'Y' ? '사용' : '미사용' },
				{ title: '비고', field: 'BIGO', widthGrow: 1.5, hozAlign: 'left' }
			]
		})
		mainGrid.on('rowSelected', (row) => {
			const data = row.getData()
			Object.assign(detailForm, {
				ACCT: data.ACCT,
				ACCT_O: data.ACCT,
				ACCTNM: data.ACCTNM,
				BIGO: data.BIGO || '',
				ACNTGBN: data.ACNTGBN || '1000',
				USEYN: data.USEYN || 'Y',
				mode: 'U'
			})
		})
	}
	handleSearch()
})
</script>

<style scoped>
.hfba201u-wrapper { height: calc(100vh - 110px); }
.tabulator-full-height { height: 100% !important; min-height: 400px; }
.required-label::before { content: '* '; color: red; }
.form-table th { background-color: #f8f9fa; font-weight: bold; }
</style>
