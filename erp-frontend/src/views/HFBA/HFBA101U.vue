<!--
	=============================================================
	프로그램명	  : 코드관리
    프로그램 ID	: HFBA101U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 공통 코드 및 원가 기준 코드 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<Modal v-model:visible="showModal" :modalProps="modalProps" />

	<div class="hfba101u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-gear-fill me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">코드관리</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="handleResetForm">초기화</button>
				<button class="btn-erp btn-search" @click="fetchGroups">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
				<button class="btn-erp btn-danger" @click="handleDelete" :disabled="detailForm.mode === 'N'">삭제</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm overflow-hidden">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 10%"><col style="width: 23%">
							<col style="width: 10%"><col style="width: 23%">
							<col style="width: 10%"><col style="width: 24%">
						</colgroup>
						<tbody>
							<tr>
								<th>코드구분</th>
								<td>
									<select v-model="detailForm.CDKD" class="form-select form-select-sm" @change="onCdkdComboChange">
										<option v-for="opt in cdkdOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
									</select>
								</td>
								<th class="required">코드</th>
								<td><input v-model="detailForm.CODE" class="form-control form-control-sm" maxlength="10" :readonly="detailForm.mode === 'U'" /></td>
								<th class="required">코드명</th>
								<td><input v-model="detailForm.CDNM" class="form-control form-control-sm" maxlength="30" /></td>
							</tr>
							<tr>
								<th>비고</th>
								<td><input v-model="detailForm.REMARK" class="form-control form-control-sm" maxlength="50" /></td>
								<th>출현순서</th>
								<td><input v-model="detailForm.DISPORD" type="number" class="form-control form-control-sm" /></td>
								<th>사용여부</th>
								<td>
									<div class="form-check form-switch mt-1">
										<input v-model="detailForm.USEYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useynSwitch">
										<label class="form-check-label small" for="useynSwitch">{{ detailForm.USEYN === 'Y' ? '사용' : '미사용' }}</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<div class="row g-2 flex-grow-1 overflow-hidden">
				<!-- 좌측 그리드: 코드 그룹 -->
				<div class="col-md-3 d-flex flex-column">
					<div class="card border shadow-sm h-100 overflow-hidden">
						<div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark">코드 그룹</div>
						<div class="card-body p-0 flex-grow-1 bg-white">
							<div ref="leftGridRef" style="height: 100%;"></div>
						</div>
					</div>
				</div>
				<!-- 우측 그리드: 상세 코드 목록 -->
				<div class="col-md-9 d-flex flex-column">
					<div class="card border shadow-sm h-100 overflow-hidden border-top border-3 border-primary">
						<div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">상세 코드 목록</div>
						<div class="card-body p-0 flex-grow-1 bg-white">
							<div ref="mainGridRef" style="height: 100%;"></div>
						</div>
					</div>
				</div>
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

const cdkdOptions = ref<any[]>([])
const detailForm = reactive({
	CDKD: '0000',
    CODE: '',
    CDNM: '',
    REMARK: '',
    DISPORD: 0,
    USEYN: 'Y',
    mode: 'N'
})

const leftGridRef = ref<HTMLElement | null>(null)
const mainGridRef = ref<HTMLElement | null>(null)
let leftGrid: Tabulator | null = null
let mainGrid: Tabulator | null = null

const showModal = ref(false)
const modalProps = ref<any>({})

// 상단 폼 초기화 (현재 CDKD는 유지)
const handleResetForm = () => {
	const currentCdkd = detailForm.CDKD
	Object.assign(detailForm, {
		CDKD: currentCdkd,
		CODE: '',
		CDNM: '',
		REMARK: '',
		DISPORD: 0,
		USEYN: 'Y',
		mode: 'N'
	})
	mainGrid?.deselectRow()
}

const loadOptions = async () => {
	try {
		const { data } = await api.get('/api/hp00/HP00_000S_STR', {
            params: { GUBUN: 'E2', CMPYCD: authStore.CMPYCD, GBNCD: '0000', CODE: '' }
        })
		cdkdOptions.value = data
	} catch (e) {
        vAlertError('코드그룹 옵션 로드 실패')
    }
}

// 1. 좌측 그리드 조회 (cdkd='0000')
const fetchGroups = async () => {
	try {
		const { data } = await api.post('/api/hfba/FBA1010U_STR', {
            ACTKIND: 'S0',
            CMPYCD: authStore.CMPYCD,
            CDKD: '0000',
            CODE: '', CDNM: '', REMARK: '', DISPORD: '0', USEYN: 'Y', USERID: authStore.USERID
        })
		leftGrid?.setData(data)
		if (data?.length > 0) {
            nextTick(() => {
                leftGrid?.selectRow(leftGrid.getRows()[0])
            })
        }
	} catch (e) {
        vAlertError('그룹 조회 실패')
    }
}

// 3. 우측 그리드 조회 (Step 3)
const fetchDetails = async (cdkd: string) => {
    if (!cdkd) return
	try {
		const { data } = await api.post('/api/hfba/FBA1010U_STR', {
            ACTKIND: 'S0',
            CMPYCD: authStore.CMPYCD,
            CDKD: String(cdkd || ''),
            CODE: '', CDNM: '', REMARK: '', DISPORD: '0', USEYN: 'Y', USERID: authStore.USERID
        })
		mainGrid?.setData(data)
	} catch (e) {
        vAlertError('상세 조회 실패')
    }
}

const onCdkdComboChange = () => {
    handleResetForm()
    fetchDetails(detailForm.CDKD)
}

const handleSave = async () => {
	if (!detailForm.CODE || !detailForm.CDNM) return vAlertError('필수 항목을 입력하세요.')
	if (!confirm('저장하시겠습니까?')) return
	try {
        const act = detailForm.mode === 'U' ? 'U0' : 'A0'
		await api.post('/api/hfba/FBA1010U_STR', {
            ACTKIND: act,
            CMPYCD: authStore.CMPYCD,
            CDKD: String(detailForm.CDKD || ''),
            CODE: String(detailForm.CODE || ''),
            CDNM: String(detailForm.CDNM || ''),
            REMARK: String(detailForm.REMARK || ''),
            DISPORD: String(detailForm.DISPORD || '0'),
            USEYN: String(detailForm.USEYN || 'Y'),
            USERID: authStore.USERID
        })
		vAlert('저장되었습니다.')
        await fetchDetails(detailForm.CDKD)
	} catch (e) {
        vAlertError('저장 실패')
    }
}

const handleDelete = async () => {
	if (detailForm.mode === 'N') return
	if (!confirm('정말 삭제하시겠습니까?')) return
	try {
		await api.post('/api/hfba/FBA1010U_STR', {
            ACTKIND: 'D0',
            CMPYCD: authStore.CMPYCD,
            CDKD: String(detailForm.CDKD || ''),
            CODE: String(detailForm.CODE || ''),
            CDNM: '', REMARK: '', DISPORD: '0', USEYN: 'Y', USERID: authStore.USERID
        })
        vAlert('삭제되었습니다.')
        handleResetForm()
        await fetchDetails(detailForm.CDKD)
	} catch (e) {
        vAlertError('삭제 실패')
    }
}

onMounted(async () => {
	await loadOptions()

	// 좌측 그리드 설정
	if (leftGridRef.value) {
		leftGrid = new Tabulator(leftGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			selectable: 1,
            columnDefaults: { headerSort: false },
			columns: [
                { title: '코드', field: 'CODE', hozAlign: 'center', width: 80 },
                { title: '코드그룹명', field: 'CDNM', hozAlign: 'left' }
            ]
		})

		// 2. 좌측 그리드 클릭/선택 -> 우측 상세 조회 (Step 2)
		leftGrid.on('rowClick', (e, row) => {
            const data = row.getData()
            if (data.CODE) {
                detailForm.CDKD = data.CODE
                handleResetForm()
                fetchDetails(data.CODE)
            }
        })

        leftGrid.on('rowSelected', (row) => {
            const data = row.getData()
            if (data.CODE) {
                detailForm.CDKD = data.CODE
                fetchDetails(data.CODE)
            }
        })
	}

	// 우측 그리드 설정
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			selectable: 1,
            columnDefaults: { headerSort: false },
			columns: [
				{ title: '코드', field: 'CODE', width: 100, hozAlign: 'center' },
				{ title: '코드명', field: 'CDNM', width: 250, hozAlign: 'left' },
				{ title: '순서', field: 'DISPORD', width: 80, hozAlign: 'center' },
				{
					title: '사용', field: 'USEYN', width: 80, hozAlign: 'center',
					formatter: (cell) => cell.getValue() === 'Y' ? '사용' : '미사용'
				},
				{ title: '비고', field: 'REMARK', widthGrow: 2, hozAlign: 'left' }
			]
		})

		// 4. 우측 그리드 클릭 -> 상단 입력 폼 세팅 (Step 4)
		mainGrid.on('rowClick', (e, row) => {
			const data = row.getData()
			Object.assign(detailForm, {
                CDKD: detailForm.CDKD,
                CODE: data.CODE,
                CDNM: data.CDNM,
                REMARK: data.REMARK,
                DISPORD: data.DISPORD,
                USEYN: data.USEYN,
                mode: 'U'
            })
		})
	}

	// 초기 실행: 좌측 그리드 그룹 목록 로드
	fetchGroups()
})
</script>

<style scoped>
.hfba101u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 4px; border: none; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; border: 1px solid #dee2e6; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; font-size: 12.5px; }
.required::after { content: ' *'; color: #dc3545; }
</style>