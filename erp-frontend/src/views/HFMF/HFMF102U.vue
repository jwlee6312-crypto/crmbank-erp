<!--
	=============================================================
	프로그램명	: 품목별 배부적수 등록
    프로그램 ID	: HFMF102U
	작성일자	: 25.02.24
	작성자	    : AI Assistant
    설명        : 품목별 배부적수 등록 (Compact & Solid Design 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<!-- 💡 시스템 표준 고도화 팝업 적용 -->
	<ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" astKind="2" @close="itemHelpVisible = false" @confirm="onSelectItem" />

	<div class="hfmf102u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 (Compact) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13.5px;">
				<i class="bi bi-box-seam me-2 text-primary" style="font-size: 16px;"></i>
				원가관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">품목별 배부적수 등록 (HFMF102U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="handleSearch">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 영역 (Slim) -->
		<div class="p-2 bg-light border-bottom shadow-inner-soft">
			<div class="card border-0 bg-transparent">
				<div class="card-body p-1 px-2">
					<div class="row g-3 align-items-center">
						<div class="col-auto d-flex align-items-center">
							<label class="compact-label me-2">년월</label>
							<input v-model="searchForm.YM" type="month" class="form-control form-control-sm text-center fw-bold" style="width: 130px;" @change="handleSearch" />
						</div>
						<div class="col-auto d-flex align-items-center">
							<label class="compact-label mx-2 ms-3">배부기준</label>
							<select v-model="searchForm.DIVSTD" class="form-select form-select-sm fw-500" style="width: 160px;" @change="handleSearch">
								<option v-for="opt in divideOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
							</select>
						</div>
						<div class="col-auto ms-auto">
							<div v-if="clsInfo.WCLSYM" class="badge bg-danger-subtle text-danger border border-danger-subtle px-2 py-1" style="font-size: 11px;">
								<i class="bi bi-lock-fill me-1"></i>마감월: {{ clsInfo.WCLSYM }}
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 3. 메인 콘텐츠 (좌우 분할, 탄탄한 그리드) -->
		<div class="flex-grow-1 d-flex overflow-hidden p-2 gap-2">
			<!-- 좌측: 작업장 목록 (Compact) -->
			<div class="col-workshop d-flex flex-column shadow-sm border rounded-1 bg-white overflow-hidden">
				<div class="grid-header-compact px-2 py-1 border-bottom bg-light d-flex align-items-center">
					<i class="bi bi-list-task me-2 text-secondary"></i>
					<span class="fw-bold smaller-text">작업장 목록</span>
				</div>
				<div class="flex-grow-1">
					<div ref="leftGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<!-- 우측: 품목 배부적수 목록 (Solid) -->
			<div class="flex-grow-1 d-flex flex-column shadow-sm border rounded-1 bg-white overflow-hidden border-top-primary">
				<div class="grid-header-compact px-3 py-1 border-bottom bg-white d-flex align-items-center justify-content-between">
					<div class="d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>
						<span v-if="selectedLineNm" class="fw-bold smaller-text text-primary">[작업장: {{ selectedLineNm }}] 품목 상세</span>
						<span v-else class="fw-bold smaller-text">품목 목록</span>
					</div>
					<!-- 💡 시스템 표준 행추가 버튼 -->
					<button class="btn btn-grid-row-add" @click="addRow" :disabled="!selectedLineCd">
						<i class="bi bi-plus-circle"></i> 행추가
					</button>
				</div>
				<div class="flex-grow-1">
					<div ref="mainGridRef" style="height: 100%;"></div>
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
import ItemHelpModal from '@/components/ItemHelpModal.vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

// 1. 상태 관리
const clsInfo = reactive({ WCLSYM: '' })
const divideOptions = ref<any[]>([])
const searchForm = reactive({
	YM: new Date().toISOString().substring(0, 7),
	DIVSTD: ''
})

const selectedLineCd = ref('')
const selectedLineNm = ref('')
const itemHelpVisible = ref(false)
const currentTargetRow = ref<any>(null)

const leftGridRef = ref<HTMLElement | null>(null)
const mainGridRef = ref<HTMLElement | null>(null)
let leftGrid: Tabulator | null = null
let mainGrid: Tabulator | null = null

// 2. 초기 데이터 로드 (콤보박스 및 마감정보)
const loadInitData = async () => {
	try {
		const [cls, divOpts] = await Promise.all([
			api.get('/api/comm/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }),
			api.post('/api/hfba/SELECT_DIVIDE_LIST', { CMPYCD: String(authStore.CMPYCD), CDKD: '1040' })
		])

		if (cls.data?.length > 0) {
			clsInfo.WCLSYM = cls.data[0].WCLSYM || cls.data[0].CODECD || ''
		}

		if (Array.isArray(divOpts.data)) {
			divideOptions.value = divOpts.data.map((item: any) => ({
				CODE: String(item.CODE || item.code || '').trim(),
				CDNM: String(item.CDNM || item.cdnm || '').trim()
			}))
		}

		if (divideOptions.value.length > 0) {
			searchForm.DIVSTD = divideOptions.value[0].CODE
		}
	} catch (e) { console.error('기초 데이터 로드 실패') }
}

// 3. 기능 로직
const logSsms = (act: string, ym: string, itemcd: string = '', divrate: string = '0') => {
    console.log(`📋 [SSMS Query] EXEC FMF1020U_STR '${authStore.CMPYCD}', '${act}', '${ym}', '${itemcd}', '${divrate}'`)
}

const handleSearch = async () => {
    if(!searchForm.DIVSTD) return;
	try {
		const ym = searchForm.YM.replace('-', '')
        logSsms('S1', ym, '', searchForm.DIVSTD)
		const { data } = await api.post('/api/hfmf/FMF1020U_STR', {
			CMPYCD: String(authStore.CMPYCD),
            ACTKIND: 'S1',
			YYMM: String(ym),
            ITEMCD: '',
            DIVRATE: String(searchForm.DIVSTD)
		})
		leftGrid?.setData(data)
		mainGrid?.clearData()
		selectedLineCd.value = ''; selectedLineNm.value = ''

		if (Array.isArray(data) && data.length > 0) {
			nextTick(() => leftGrid?.selectRow(leftGrid.getRows()[0]))
		}
	} catch (e) { vAlertError('작업장 조회 실패') }
}

const selectDetails = async (linecd: string) => {
	try {
		const ym = searchForm.YM.replace('-', '')
        logSsms('S0', ym, linecd, searchForm.DIVSTD)
		const { data } = await api.post('/api/hfmf/FMF1020U_STR', {
			CMPYCD: String(authStore.CMPYCD),
            ACTKIND: 'S0',
			YYMM: String(ym),
			ITEMCD: String(linecd),
            DIVRATE: String(searchForm.DIVSTD)
		})
		mainGrid?.setData(data)
	} catch (e) { vAlertError('상세 조회 실패') }
}

const addRow = () => {
	if (!selectedLineCd.value) return vAlertError('작업장을 먼저 선택하세요.')
	mainGrid?.addRow({ ITEMCD: '', ITEMNM: '', RATE: '0', JUKSU: '0' }, true)
	const rows = mainGrid?.getRows()
	if (rows && rows.length > 0) {
		currentTargetRow.value = rows[0]
		itemHelpVisible.value = true
	}
}

const onSelectItem = (data: any) => {
	if (currentTargetRow.value) {
		currentTargetRow.value.update({
			ITEMCD: String(data.ITEMCD || '').trim(),
			ITEMNM: String(data.ITEMNM || '').trim(),
			UNIT: String(data.UNITNM || '').trim()
		})
		currentTargetRow.value.select()
	}
	itemHelpVisible.value = false
}

const handleSave = async () => {
	const ym = searchForm.YM.replace('-', '')
	if (clsInfo.WCLSYM && clsInfo.WCLSYM >= ym) return vAlertError('원가마감 되었습니다. 마감 취소 후 작업하세요.')

	const selectedData = mainGrid?.getSelectedData() || []
	if (selectedData.length === 0) return vAlertError('저장할 행을 선택하세요.')

	if (!confirm('저장하시겠습니까?')) return

	try {
		for (const row of selectedData) {
			const payload = {
				CMPYCD: String(authStore.CMPYCD),
                ACTKIND: 'A0',
                YYMM: String(ym),
                ITEMCD: String(row.ITEMCD || ''),
                DIVRATE: String(row.RATE || '0')
			}
            logSsms('A0', ym, payload.ITEMCD, payload.DIVRATE)
			await api.post('/api/hfmf/FMF1020U_STR', payload)
		}
		vAlert('성공적으로 저장되었습니다.')
		await selectDetails(selectedLineCd.value)
	} catch (e) { vAlertError('저장 중 오류 발생') }
}

// 4. 그리드 초기화 (Compact & Solid Style)
onMounted(async () => {
	await loadInitData()

	if (leftGridRef.value) {
		leftGrid = new Tabulator(leftGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columnDefaults: { headerSort: false },
			columns: [
				{ title: '작업장', field: 'LINENM', hozAlign: 'left',
                  formatter: (cell) => `<span class="small-text fw-600">${cell.getData().LINECD || ''}-${cell.getValue() || ''}</span>`
                }
			]
		})
		leftGrid.on('rowSelected', (row) => {
			const data = row.getData()
			selectedLineCd.value = data.LINECD
			selectedLineNm.value = data.LINENM
			selectDetails(data.LINECD)
		})
	}

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			columnDefaults: { headerHozAlign: 'center', headerSort: false },
			columns: [
				{ title: '', field: '_sel', width: 35, formatter: 'rowSelection', titleFormatter: 'rowSelection' },
				{
					title: '코드', field: 'ITEMCD', width: 100,
					formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>",
					cellClick: (e, cell) => {
						if ((e.target as HTMLElement).classList.contains('bi-search')) {
							currentTargetRow.value = cell.getRow()
							itemHelpVisible.value = true
						}
					}
				},
				{ title: '품목명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-700 text-dark' },
				{ title: '단위', field: 'UNIT', width: 60, hozAlign: 'center' },
				{ title: '생산량', field: 'PROQTY', width: 80, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '배부적수', field: 'JUKSU', width: 90, hozAlign: 'right', editor: 'number', formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'bg-yellow' },
				{ title: '배부율(%)', field: 'RATE', width: 90, hozAlign: 'right', editor: 'number', formatter: 'money', formatterParams: { precision: 4 }, cssClass: 'bg-yellow' },
				{ title: '비고', field: 'REMARK', minWidth: 150, hozAlign: 'left', editor: 'input' }
			]
		})
		mainGrid.on('cellEdited', (cell) => cell.getRow().select())
	}
	handleSearch()
})
</script>

<script lang="ts">
export default { name: 'HFMF102U' }
</script>

<style scoped>
.hfmf102u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; border: none; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; }

.compact-label { font-size: 12px; font-weight: 700; color: #475569; white-space: nowrap; }
.grid-header-compact { min-height: 32px; background-color: #f8fafc; }
.smaller-text { font-size: 11.5px; }
.small-text { font-size: 12px; }
.fw-600 { font-weight: 600; }
.fw-700 { font-weight: 700; }
.fw-500 { font-weight: 500; }

.col-workshop { width: 220px; flex-shrink: 0; }
.border-top-primary { border-top: 3px solid #005a9f !important; }
.shadow-inner-soft { box-shadow: inset 0 2px 4px rgba(0,0,0,0.03); }
.bg-yellow { background-color: #fffde7 !important; }

:deep(.tabulator) { border: none !important; font-size: 11.5px !important; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 1.5px solid #cbd5e1 !important; }
:deep(.tabulator-col-title) { font-weight: 800 !important; color: #334155 !important; }
:deep(.tabulator-row) { border-bottom: 1px solid #f1f5f9 !important; min-height: 28px !important; }
:deep(.tabulator-cell) { padding: 3px 6px !important; vertical-align: middle !important; }
</style>
