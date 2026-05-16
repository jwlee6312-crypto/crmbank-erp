<!--
	=============================================================
	프로그램명	  : 공정별제조비용 조정
    프로그램 ID	: HFMF207U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 공정별제조비용 조정 (FMF2070U_STR 연결 - 전수조사 규격 반영)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<Modal v-model:visible="showModal" :modalProps="modalProps" />

	<div class="hfmf207u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-sliders me-2 text-primary" style="font-size: 18px;"></i>
				원가관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">공정제조비용관리 (HFMF207U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="handleResetForm">초기화</button>
				<button class="btn-erp btn-search" @click="handleSearch">조회</button>
				<button class="btn-erp btn-danger" @click="handleDelete" :disabled="!selectedRowCount">삭제</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
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
						<label class="small fw-bold me-2 required-label">계정과목:</label>
						<select v-model="searchForm.ACCT" class="form-select form-select-sm d-inline-block w-auto" style="min-width: 200px;" @change="handleSearch">
							<option v-for="opt in acctOptions" :key="opt.ACCT" :value="opt.ACCT">{{ opt.ACCTNM }}</option>
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
				<span>공정별 비용 조정 목록</span>
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
import Modal from '@/components/Modal.vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const clsInfo = reactive({ WCLSYM: '' })
const acctOptions = ref<any[]>([])
const searchForm = reactive({
	YM: new Date().toISOString().substring(0, 7),
	ACCT: ''
})

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null
const selectedRowCount = ref(0)
const showModal = ref(false)
const modalProps = ref<any>({})

const logSsms = (act: string, row: any = {}) => {
    const ym = searchForm.YM.replace('-', '')
    console.log(`📋 [SSMS Query] EXEC FMF2070U_STR '${authStore.CMPYCD}', '${act}', '${ym}', '10000', '${searchForm.ACCT}', '${row.LINECD || ''}', '${row.ADSTAMT || '0'}', '${authStore.USERID}'`)
}

const loadInitData = async () => {
	try {
		const [cls, accts] = await Promise.all([
			api.get('/api/comm/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }),
			api.post('/api/hfba/SELECT_ACCT_LIST', { CMPYCD: authStore.CMPYCD })
		])
        if (cls.data?.length > 0) {
            clsInfo.WCLSYM = cls.data[0].WCLSYM || cls.data[0].CODECD || ''
        }
		acctOptions.value = (accts.data || []).map((item: any) => ({
			ACCT: String(item.ACCT || item.acct || '').trim(),
			ACCTNM: String(item.ACCTNM || item.acctnm || '').trim()
		}))
		if (acctOptions.value.length > 0) searchForm.ACCT = acctOptions.value[0].ACCT
	} catch (e) {
        vAlertError('기초 데이터 로드 실패')
    }
}

const handleResetForm = () => {
    mainGrid?.clearData()
    selectedRowCount.value = 0
}

const handleSearch = async () => {
	if (!searchForm.ACCT) return
	try {
		const ym = searchForm.YM.replace('-', '')
        logSsms('S0')
		const { data } = await api.post('/api/hfmf/FMF2070U_STR', {
			CMPYCD: String(authStore.CMPYCD),
            ACTKIND: 'S0',
            YM: String(ym),
			COSTCD: '10000',
            ACCT: String(searchForm.ACCT),
			LINECD: '',
            ADSTAMT: '0',
            USERID: String(authStore.USERID)
		})
		mainGrid?.setData(data)
        selectedRowCount.value = 0
	} catch (e) {
        vAlertError('조회 실패')
    }
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
			await api.post('/api/hfmf/FMF2070U_STR', {
				CMPYCD: String(authStore.CMPYCD),
				ACTKIND: 'A0',
                YM: String(ym),
				COSTCD: '10000',
				ACCT: String(searchForm.ACCT),
				LINECD: String(row.LINECD || ''),
				ADSTAMT: String(row.ADSTAMT || '0'),
                USERID: String(authStore.USERID)
			})
		}
		vAlert('저장되었습니다.')
		handleSearch()
	} catch (e) {
        vAlertError('저장 실패')
    }
}

const handleDelete = async () => {
    const ym = searchForm.YM.replace('-', '')
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')

	const selectedData = mainGrid?.getSelectedData() || []
	if (!confirm('선택한 조정 정보를 삭제하시겠습니까?')) return

	try {
		for (const row of selectedData) {
            logSsms('D0', row)
			await api.post('/api/hfmf/FMF2070U_STR', {
				CMPYCD: String(authStore.CMPYCD),
				ACTKIND: 'D0',
                YM: String(ym),
				COSTCD: '10000',
				ACCT: String(searchForm.ACCT),
				LINECD: String(row.LINECD || ''),
				ADSTAMT: '0',
                USERID: String(authStore.USERID)
			})
		}
		vAlert('삭제되었습니다.')
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
			selectable: true,
			columnDefaults: { minWidth: 100, headerSort: false },
			columns: [
				{ title: '', field: '_sel', width: 40, formatter: 'rowSelection', titleFormatter: 'rowSelection' },
				{ title: '공정코드', field: 'LINECD', hozAlign: 'center', width: 120 },
				{ title: '공정명', field: 'LINENM', widthGrow: 2, hozAlign: 'left' },
				{
					title: '금액', field: 'DIRCAMT_SUM',
					hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
                    mutatorData: (value, data) => (Number(data.DIRCAMT || data.dircamt || 0) + Number(data.IDIRCAMT || data.idircamt || 0))
				},
				{
					title: '조정액 <i class="bi bi-pencil"></i>', field: 'ADSTAMT',
					hozAlign: 'right', editor: 'number', formatter: 'money', formatterParams: { precision: 0 }
				},
				{
					title: '합계', field: 'TOT_AMT',
					hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
                    cssClass: 'fw-bold text-primary',
                    mutatorData: (value, data) => (Number(data.DIRCAMT || data.dircamt || 0) + Number(data.IDIRCAMT || data.idircamt || 0) + Number(data.ADSTAMT || data.adstamt || 0))
				}
			]
		})

		mainGrid.on('cellEdited', (cell) => cell.getRow().select())
        mainGrid.on('rowSelectionChanged', (data) => selectedRowCount.value = data.length)
	}
	handleSearch()
})
</script>

<style scoped>
.hfmf207u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; border: none; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 8px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required-label::before { content: '* '; color: red; }
</style>
