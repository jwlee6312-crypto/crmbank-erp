<!--
	=============================================================
	프로그램명	  : 작업장배부적수관리
    프로그램 ID	: HFMF105U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 작업장별 배부율 등록 (FMF1050U_STR 연결 - 전수조사 규격 반영)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<Modal v-model:visible="showModal" :modalProps="modalProps" />

	<div class="hfmf105u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small"><i class="bi bi-gear-wide-connected me-2 text-primary"></i>원가관리 >> 작업장배부적수관리</div>
			<div class="btn-group shadow-sm">
				<button class="btn btn-sm btn-dark px-3" @click="handleSearch">조회</button>
				<button class="btn btn-sm btn-danger px-3" @click="handleDelete" :disabled="!selectedRowCount">삭제</button>
				<button class="btn btn-sm btn-primary px-4 fw-bold" @click="handleSave"><i class="bi bi-save me-1"></i>저장</button>
			</div>
		</div>

		<!-- 2. 검색 조건 영역 -->
		<div class="card shadow-sm border-0 mb-1 flex-shrink-0">
			<div class="card-body p-2 px-3">
				<div class="row g-3 align-items-center">
					<div class="col-auto">
						<label class="small fw-bold me-2 required-label">년월:</label>
						<input v-model="searchForm.YM" type="month" class="form-control form-control-sm d-inline-block w-auto" @change="handleSearch" />
					</div>
					<div class="col-auto ms-3 border-start ps-3">
						<label class="small fw-bold me-2 required-label">공정배부기준:</label>
						<select v-model="searchForm.DIVSTD" class="form-select form-select-sm d-inline-block w-auto" style="min-width: 150px;" @change="handleSearch">
							<option v-for="opt in divideOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
						</select>
					</div>
					<div class="col-auto ms-auto">
						<div v-if="clsInfo.WCLSYM" class="badge bg-danger p-2">마감월: {{ clsInfo.WCLSYM }}</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 3. 메인 그리드 -->
		<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary">
			<div class="card-header bg-white py-1 px-2 fw-bold small border-bottom">
				<span>작업장별 배부율 목록</span>
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

const clsInfo = reactive({ WCLSYM: '' })
const divideOptions = ref<any[]>([])
const searchForm = reactive({
	YM: new Date().toISOString().substring(0, 7),
	DIVSTD: ''
})

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null
const selectedRowCount = ref(0)
const showModal = ref(false)
const modalProps = ref<any>({})

const logSsms = (act: string, row: any = {}) => {
    const ym = searchForm.YM.replace('-', '')
    console.log(`📋 [SSMS Query] EXEC FMF1050U_STR '${authStore.CMPYCD}', '${act}', '${ym}', '10000', '${row.LINECD || ''}', '${searchForm.DIVSTD}', '${row.RATE || '0'}', '${row.BIGO || ''}', '${authStore.USERID}'`)
}

const loadInitData = async () => {
	try {
		const [cls, divOpts] = await Promise.all([
			api.get('/api/comm/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }),
			api.post('/api/hfba/SELECT_DIVIDE_LIST', { CMPYCD: authStore.CMPYCD, CDKD: '1030' })
		])
        if (cls.data?.length > 0) {
            clsInfo.WCLSYM = cls.data[0].WCLSYM || cls.data[0].CODECD || ''
        }
		divideOptions.value = (divOpts.data || []).filter((opt: any) => opt.CODE !== '3010')
        if (divideOptions.value.length > 0) searchForm.DIVSTD = divideOptions.value[0].CODE
	} catch (e) { vAlertError('기초 데이터 로드 실패') }
}

const handleSearch = async () => {
    if(!searchForm.DIVSTD) return;
	try {
		const ym = searchForm.YM.replace('-', '')
        logSsms('S0')
		const { data } = await api.post('/api/hfmf/FMF1050U_STR', {
            CMPYCD: authStore.CMPYCD,
            ACTKIND: 'S0',
			YM: ym,
            COSTCD: '10000',
			LINECD: '',
            DIVSTD: String(searchForm.DIVSTD),
            RATE: '0',
            REMARK: '',
            USERID: authStore.USERID
		})
		mainGrid?.setData(data)
        selectedRowCount.value = 0
	} catch (e) { vAlertError('조회 실패') }
}

const handleSave = async () => {
	const ym = searchForm.YM.replace('-', '')
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')

	const selectedData = mainGrid?.getSelectedData() || []
	if (selectedData.length === 0) return vAlertError('저장할 행을 선택하세요.')

	if (!confirm('저장하시겠습니까?')) return

	try {
		for (const row of selectedData) {
            logSsms('A0', row)
			await api.post('/api/hfmf/FMF1050U_STR', {
				CMPYCD: authStore.CMPYCD,
                ACTKIND: 'A0',
                YM: ym,
				COSTCD: '10000',
				LINECD: String(row.LINECD || ''),
				DIVSTD: String(searchForm.DIVSTD),
				RATE: String(row.RATE || '0'),
				REMARK: String(row.BIGO || ''),
				USERID: authStore.USERID
			})
		}
		vAlert('저장되었습니다.')
		handleSearch()
	} catch (e) { vAlertError('저장 실패') }
}

const handleDelete = async () => {
    const ym = searchForm.YM.replace('-', '')
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')

	const selectedData = mainGrid?.getSelectedData() || []
	if (!confirm('선택한 자료를 삭제하시겠습니까?')) return

	try {
		for (const row of selectedData) {
            logSsms('D0', row)
			await api.post('/api/hfmf/FMF1050U_STR', {
				CMPYCD: authStore.CMPYCD,
                ACTKIND: 'D0',
                YM: ym,
				COSTCD: '10000',
				LINECD: String(row.LINECD || ''),
				DIVSTD: String(searchForm.DIVSTD),
				RATE: '0',
                REMARK: '',
				USERID: authStore.USERID
			})
		}
		vAlert('삭제되었습니다.')
		handleSearch()
	} catch (e) { vAlertError('삭제 실패') }
}

onMounted(async () => {
	await loadInitData()

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			selectable: true,
			columnDefaults: { minWidth: 100, headerSort: false },
			columns: [
				{ title: '', field: '_sel', width: 40, formatter: 'rowSelection', titleFormatter: 'rowSelection' },
				{ title: '공정코드', field: 'LINECD', hozAlign: 'center', width: 120 },
				{ title: '공정명', field: 'LINENM', widthGrow: 2, hozAlign: 'left' },
				{ title: '배부적수', field: 'PROQTY', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '적수계산배부율', field: 'JUKSU_RATE', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 2 } },
				{ title: '배부율 <i class="bi bi-pencil"></i>', field: 'RATE', hozAlign: 'right', editor: 'number', formatter: 'money', formatterParams: { precision: 2 } },
				{ title: '비고', field: 'BIGO', widthGrow: 2, hozAlign: 'left', editor: 'input' }
			]
		})
		mainGrid.on('cellEdited', (cell) => cell.getRow().select())
        mainGrid.on('rowSelectionChanged', (data) => selectedRowCount.value = data.length)
	}
	handleSearch()
})
</script>

<style scoped>
.hfmf105u-wrapper { height: calc(100vh - 110px); }
.tabulator-full-height { height: 100% !important; min-height: 400px; }
.required-label::before { content: '* '; color: red; }
.form-table th { background-color: #f8f9fa; font-weight: bold; }
</style>
