<!--
	=============================================================
	프로그램명	  : 품목단위별 배부적수관리
    프로그램 ID	: HFBA106U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 품목단위별 배부적수관리 (FBA1060U_STR 연결)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<Modal v-model:visible="showModal" :modalProps="modalProps" />

	<div class="hfba106u-wrapper bg-light text-start p-2 h-100 d-flex flex-column gap-1">
		<!-- 1. 상단 버튼 라인 -->
		<div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
			<div class="fw-bold text-dark small"><i class="bi bi-list-ol me-2 text-primary"></i>원가관리 >> 품목단위당 배부적수관리</div>
			<div class="btn-group shadow-sm">
				<button class="btn btn-sm btn-dark px-3" @click="handleSearch">조회</button>
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
					<div class="col-auto ms-3">
						<label class="small fw-bold me-2">배부기준:</label>
						<select v-model="searchForm.DIVSTD" class="form-select form-select-sm d-inline-block w-auto" style="min-width: 150px;" @change="handleSearch">
							<option v-for="opt in divideOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
						</select>
					</div>
					<div class="col-auto ms-3">
						<label class="small fw-bold me-2">제품구분:</label>
						<select v-model="searchForm.JSANGBN" class="form-select form-select-sm d-inline-block w-auto" style="min-width: 100px;" @change="handleSearch">
							<option value="200">제품</option>
							<option value="210">재공품</option>
						</select>
					</div>
					<div class="col-auto ms-auto">
						<div v-if="clsInfo.WCLSYM" class="badge bg-danger p-2">마감월: {{ clsInfo.WCLSYM }}</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 3. 메인 그리드 -->
		<div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary mt-1">
			<div class="card-header bg-white py-1 px-2 fw-bold small border-bottom">
				<span>품목별 배부적수 목록</span>
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
const divideOptions = ref<any[]>([])
const searchForm = reactive({
	YM: new Date().toISOString().substring(0, 7),
	DIVSTD: '',
	JSANGBN: '200'
})

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null
const showModal = ref(false)
const modalProps = ref<any>({})

const loadInitData = async () => {
	try {
		const [cls, divOpts] = await Promise.all([
			// 💡 FBA3010U_STR 규격 (6개 파라미터)
			api.post('/api/hfba/FBA3010U_STR', {
				CMPYCD: authStore.CMPYCD,
				ACTKIND: 'S1',
				YYMM: '',
				YN: 'N',
				REMARK: '',
				UPDEMP: authStore.USERID
			}),
			// 💡 SELECT_DIVIDE_LIST 규격 (2개 파라미터)
			api.post('/api/hfba/SELECT_DIVIDE_LIST', {
				CMPYCD: authStore.CMPYCD,
				CDKD: '1040'
			})
		])

		const clsRows = cls.data
        if (Array.isArray(clsRows) && clsRows.length > 0) {
            clsInfo.WCLSYM = clsRows[0].WCLSYM || clsRows[0].wclsym || ''
        }

		const optRows = divOpts.data
		if (Array.isArray(optRows)) {
			divideOptions.value = optRows.map((item: any) => ({
				CODE: String(item.CODE || item.code || '').trim(),
				CDNM: String(item.CDNM || item.cdnm || '').trim()
			}))
		}

		if (divideOptions.value.length > 0) searchForm.DIVSTD = divideOptions.value[0].CODE
	} catch (e) {
        vAlertError('기초 데이터 로드 실패')
    }
}

const handleSearch = async () => {
    if(!searchForm.DIVSTD) return;
	try {
		const ym = searchForm.YM.replace('-', '')
		// 💡 FBA1060U_STR 규격 (12개 파라미터 순서 준수)
		const { data } = await api.post('/api/hfba/FBA1060U_STR', {
			CMPYCD: authStore.CMPYCD,
			ACTKIND: 'S0',
			YM: ym,
			COSTCD: '10000',
			DIVSTD: String(searchForm.DIVSTD),
			JSANGBN: String(searchForm.JSANGBN),
			ITEMCD: '',
			UNIT: '',
			ITEMNM: '',
			UNITAMT: '0',
			BIGO: '',
			USERID: authStore.USERID
		})
		mainGrid?.setData(data)
	} catch (e) {
        vAlertError('조회 실패')
    }
}

const handleSave = async () => {
	const ym = searchForm.YM.replace('-', '')
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')

	const selectedData = mainGrid?.getSelectedData() || []
	if (selectedData.length === 0) return vAlertError('저장할 행을 선택해주세요.')

	if (!confirm('저장하시겠습니까?')) return

	try {
		for (const row of selectedData) {
			// 💡 FBA1060U_STR 저장 규격 (12개 파라미터 순서 준수)
			const payload = {
				CMPYCD: authStore.CMPYCD,
				ACTKIND: 'A0',
				YM: ym,
				COSTCD: '10000',
				DIVSTD: String(searchForm.DIVSTD),
				JSANGBN: String(searchForm.JSANGBN),
				ITEMCD: String(row.ITEMCD || ''),
				UNIT: String(row.UNIT || ''),
				ITEMNM: String(row.ITEMNM || ''),
				UNITAMT: String(row.UNITAMT ?? '0'),
				BIGO: String(row.BIGO || ''),
				USERID: authStore.USERID
			}
			await api.post('/api/hfba/FBA1060U_STR', payload)
		}
		vAlert('성공적으로 저장되었습니다.')
		handleSearch()
	} catch (e) {
        vAlertError('저장 중 오류 발생')
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
				{ title: '품목코드', field: 'ITEMCD', width: 120, hozAlign: 'center' },
				{ title: '품목명', field: 'ITEMNM', widthGrow: 2, hozAlign: 'left' },
				{ title: '단위', field: 'UNIT', width: 80, hozAlign: 'center' },
				{ title: '단위당적수', field: 'UNITAMT', width: 120, hozAlign: 'right', editor: 'number', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '비고', field: 'BIGO', widthGrow: 1.5, hozAlign: 'left', editor: 'input' }
			]
		})
        mainGrid.on('cellEdited', (cell) => cell.getRow().select())
	}
	handleSearch()
})
</script>

<style scoped>
.hfba106u-wrapper { height: calc(100vh - 110px); }
.tabulator-full-height { height: 100% !important; min-height: 400px; }
.required-label::before { content: '* '; color: red; }
</style>
