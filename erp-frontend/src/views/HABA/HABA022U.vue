<!--
	=============================================================
	프로그램명	: 재무제표 차감수식
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 재무제표 충당금 계정의 차감 연산 수식 설정 및 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi- calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">재무제표 차감수식 (HABA022U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-plus-lg"></i> 신규
				</button>
				<button class="btn-erp btn-search" @click="fetchLeftGrid">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-check-lg"></i> 저장
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 150px;" />
						<col style="width: 100px;" /><col style="width: 200px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">적용연도</th>
							<td class="bg-white border-end px-2">
								<select v-model="searchForm.yyyy" class="form-select form-select-sm" @change="fetchLeftGrid">
									<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
								</select>
							</td>
							<th class="text-center border-end">재무제표</th>
							<td class="bg-white border-end">
								<select v-model="searchForm.gubun" class="form-select form-select-sm" @change="fetchLeftGrid">
									<option v-for="opt in statementOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
								</select>
							</td>
							<td class="bg-white px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> 집계계정을 선택하신 후 수식을 편집하십시오. (충당금계정만 가능)
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📝 수식 편집 폼 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom d-flex justify-content-between">
					<span class="small fw-bold text-secondary">
						<i class="bi bi-pencil-square me-1"></i> 수식 상세 설정 [{{ masterForm.actkind === 'I1' ? '신규' : '수정' }}]
					</span>
					<span v-if="masterForm.upacct" class="badge bg-secondary">집계대상: {{ masterForm.upacct_t }}</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">계정과목</th>
							<td class="bg-white border-end px-2">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="masterForm.acctcd_t" type="text" class="form-control" placeholder="과목 선택" @keydown.enter="openHelp('ACCT')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('ACCT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">연산대상</th>
							<td class="bg-white border-end border-top px-2">
								<select v-model="masterForm.caltype" class="form-select form-select-sm">
									<option value="000">선택</option>
									<option v-for="opt in calcTypeOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
								</select>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">연산수식</th>
							<td class="bg-white border-top px-2">
								<select v-model="masterForm.calgagam" class="form-select form-select-sm">
									<option value="000">선택</option>
									<option v-for="opt in formulaOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
								</select>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">사용여부</th>
							<td colspan="5" class="bg-white border-top px-3">
								<div class="form-check form-check-inline">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useCheck" true-value="Y" false-value="N">
									<label class="form-check-label fw-bold" for="useCheck">사용</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 듀얼 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex gap-2">
			<!-- 좌측: 집계계정 리스트 (40%) -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white" style="flex: 4;">
				<div class="card-header py-1 px-2 bg-secondary text-white small fw-bold">
					<i class="bi bi-folder-fill me-1"></i> 집계계정 목록
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="leftGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
			<!-- 우측: 차감 수식 리스트 (60%) -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white" style="flex: 6;">
				<div class="card-header py-1 px-2 bg-info text-white small fw-bold">
					<i class="bi bi-list-stars me-1"></i> 설정된 차감수식
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="rightGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 🔍 검색 데이터
const searchForm = reactive({
	yyyy: new Date().getFullYear().toString(),
	gubun: '010'
})

// 📝 마스터 데이터 (수식 상세)
const masterForm = reactive({
	actkind: 'I1',
	yyyy: '',
	gubun: '',
	upacct: '',
	upacct_t: '',
	acctcd: '',
	acctcd_t: '',
	caltype: '000',
	calgagam: '000',
	useyn: 'Y',
	updyn: 'Y',
	rstyn: 'N'
})

// 옵션 데이터
const yearOptions = ref<string[]>([])
const statementOptions = ref<any[]>([])
const calcTypeOptions = ref<any[]>([])
const formulaOptions = ref<any[]>([])

const leftGridRef = ref<HTMLDivElement | null>(null)
const rightGridRef = ref<HTMLDivElement | null>(null)
let leftGrid: Tabulator | null = null
let rightGrid: Tabulator | null = null

// 🚀 초기 정보 로드
const loadInitData = async () => {
	try {
		// 적용연도 목록 (HABA810T 기준)
		const resYear = await api.post('/api/ha00/HA00_010S_STR', { gubun: 'Y0', cmpycd: authStore.cmpycd })
		yearOptions.value = (resYear.data || []).map((r: any) => r.col0)
		if (yearOptions.value.length > 0) searchForm.yyyy = yearOptions.value[0]

		// 재무제표 구분 (070)
		const resGbn = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '070' })
		statementOptions.value = (resGbn.data || []).map((r: any) => ({ CD: r.col0, NM: r.col1 }))

		// 연산대상 (080)
		const resType = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '080' })
		calcTypeOptions.value = (resType.data || []).map((r: any) => ({ CD: r.col0, NM: r.col1 }))

		// 연산수식 (090)
		const resFormula = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '090' })
		formulaOptions.value = (resFormula.data || []).map((r: any) => ({ CD: r.col0, NM: r.col1 }))
	} catch (e) { console.error('초기 데이터 로드 실패') }
}

// 좌측 그리드 조회 (집계계정)
const fetchLeftGrid = async () => {
	try {
		const res = await api.post('/api/haba/HABA_022U_STR', {
			actkind: 'S3',
			cmpycd: authStore.cmpycd,
			yyyy: searchForm.yyyy,
			gubun: searchForm.gubun
		})

		const processed = (res.data || []).map((r: any) => ({
			gubun: r.col0,
			upacct: r.col1,
			acctnm: r.col2,
			gubun_NM: r.col3,
			updyn: r.col4,
			rstyn: r.col5 // 'Y'일 때만 등록 가능 (충당금계정)
		}))

		leftGrid?.setData(processed)
		rightGrid?.clearData()
		initialize()
	} catch (e) { vAlertError('좌측 목록 조회 실패') }
}

// 우측 그리드 조회 (차감수식 상세)
const fetchRightGrid = async (row: any) => {
	try {
		const res = await api.post('/api/haba/HABA_022U_STR', {
			actkind: 'SR',
			cmpycd: authStore.cmpycd,
			yyyy: searchForm.yyyy,
			gubun: row.gubun,
			upacct: row.upacct
		})

		const processed = (res.data || []).map((r: any) => ({
			acctcd: r.col0,
			acctnm: r.col1,
			caltype_nm: r.col2,
			caltype: r.col3,
			calgagam_nm: r.col4,
			calgagam: r.col5,
			useyn: r.col6,
			gubun: r.col7,
			upacct: r.col8,
			upacct_nm: r.COL9
		}))

		rightGrid?.setData(processed)
	} catch (e) { vAlertError('수식 내역 조회 실패') }
}

const save = async () => {
	if (masterForm.updyn !== 'Y') return vAlert('수정할 수 없습니다.')
	if (masterForm.rstyn !== 'Y') return vAlert('충당금계정만 등록 가능합니다.')
	if (!masterForm.upacct) return vAlert('집계계정을 선택해 주십시요.')
	if (!masterForm.acctcd) return vAlert('계정코드를 선택해 주십시요.')
	if (masterForm.caltype === '000') return vAlert('연산대상을 선택해 주십시요.')
	if (masterForm.calgagam === '000') return vAlert('연산수식을 선택해 주십시요.')

	try {
		masterForm.yyyy = searchForm.yyyy
		masterForm.gubun = searchForm.gubun

		const res = await api.post('/api/haba/HABA_022U_SAVE', {
			...masterForm,
			cmpycd: authStore.cmpycd,
			userid: authStore.userid
		})

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].ret_msg)
		} else {
			vAlert('저장되었습니다.')
			fetchRightGrid({ gubun: masterForm.gubun, upacct: masterForm.upacct })
			initialize()
		}
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
	const currentUpAcct = masterForm.upacct
	const currentUpAcctT = masterForm.upacct_t
	const currentRstyn = masterForm.rstyn
	const currentUpdyn = masterForm.updyn

	resetForm(masterForm)
	masterForm.actkind = 'I1'
	masterForm.upacct = currentUpAcct
	masterForm.upacct_t = currentUpAcctT
	masterForm.rstyn = currentRstyn
	masterForm.updyn = currentUpdyn
	masterForm.useyn = 'Y'
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'ACCT') {
		if (!masterForm.gubun) return vAlert('재무제표 종류가 확인되지 않습니다.')
		Object.assign(modalProps, {
			title: '계정과목 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'A0', cmpycd: authStore.cmpycd, code: masterForm.acctcd_t },
			columns: [{ title: '코드', field: 'col0', width: 80 }, { title: '계정명', field: 'col1', width: 180 }],
			onConfirm: (d: any) => { masterForm.acctcd = d.col0; masterForm.acctcd_t = d.col1 }
		})
		modalVisible.value = true
	}
}

onMounted(() => {
	loadInitData()

	// 📊 좌측 그리드 초기화 (집계계정)
	if (leftGridRef.value) {
		leftGrid = new Tabulator(leftGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "코드", field: "upacct", width: 90, hozAlign: "center" },
				{
					title: "계정명", field: "acctnm",
					formatter: (cell) => {
						const code = String(cell.getData().upacct)
						let indent = 0
						if (code.endsWith("000000")) indent = 0
						else if (code.endsWith("00000")) indent = 10
						else if (code.endsWith("0000")) indent = 20
						else if (code.endsWith("00")) indent = 30
						else indent = 35

						const isRst = cell.getData().rstyn === 'Y'
						const style = isRst ? 'color: blue; font-weight: bold;' : ''
						const icon = indent > 0 ? `<i class="bi bi-dot me-1"></i>` : ""
						return `<div style="padding-left: ${indent}px; ${style}">${icon}${cell.getValue()}</div>`
					}
				},
				{ title: "구분", field: "gubun_NM", width: 70, hozAlign: "center" }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				masterForm.upacct = d.upacct
				masterForm.upacct_t = d.acctnm
				masterForm.gubun = d.gubun
				masterForm.updyn = d.updyn
				masterForm.rstyn = d.rstyn
				fetchRightGrid(d)
				initialize()
			}
		})
	}

	// 📊 우측 그리드 초기화 (수식내역)
	if (rightGridRef.value) {
		rightGrid = new Tabulator(rightGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "계정코드", field: "acctcd", width: 90, hozAlign: "center" },
				{ title: "계정과목명", field: "acctnm", width: 180 },
				{ title: "연산대상", field: "caltype_nm", width: 100, hozAlign: "center" },
				{ title: "연산수식", field: "calgagam_nm", width: 100, hozAlign: "center" },
				{ title: "사용", field: "useyn", width: 60, hozAlign: "center", formatter: "tickCross" }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				Object.assign(masterForm, d)
				masterForm.actkind = 'U1'
				masterForm.acctcd_t = d.acctnm
			}
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.bg-light-subtle { background-color: #f8f9fa !important; }
:deep(.tabulator-row) { cursor: pointer; }
:deep(.tabulator-row:hover) { background-color: #f0f7ff !important; }
</style>