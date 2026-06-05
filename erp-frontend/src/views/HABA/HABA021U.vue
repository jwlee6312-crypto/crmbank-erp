<!--	=============================================================
	프로그램명	: 재무제표 수식편집
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 재무제표 항목별 집계 수식(계정과목 가감) 설정 및 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 (표준) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
				장부관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">재무제표 수식편집 (HABA021U)</span>
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

		<!-- 🔍 검색 조건 영역 (표준 erp-table-full) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 150px;" />
						<col style="width: 100px;" /><col style="width: 200px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th>적용연도</th>
							<td class="bg-white">
								<select v-model="searchForm.yyyy" class="form-select" @change="fetchLeftGrid">
									<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
								</select>
							</td>
							<th>재무제표</th>
							<td class="bg-white">
								<select v-model="searchForm.gubun" class="form-select" @change="fetchLeftGrid">
									<option v-for="opt in statementOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
								</select>
							</td>
							<td class="bg-white px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> 좌측 항목 선택 시 상세 수식이 조회됩니다.
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📝 수식 상세 입력 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom d-flex justify-content-between align-items-center">
					<span class="fw-bold small text-dark">
						<i class="bi bi-pencil-square me-1 text-primary"></i> 수식 편집
						<span v-if="masterForm.upacct" class="ms-2 text-primary fw-bolder">[{{ masterForm.upacct_t }}]</span>
					</span>
					<span class="badge bg-secondary" style="font-size: 10px;">{{ masterForm.actkind === 'I1' ? '신규등록' : '정보수정' }}</span>
				</div>
				<table class="erp-table-full">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="required">계정과목</th>
							<td>
								<div class="input-group">
									<input v-model="masterForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="masterForm.acctcd_t" type="text" class="form-control" placeholder="계정 선택" @keydown.enter="openHelp('ACCT')" />
									<button class="btn btn-outline-secondary" @click="openHelp('ACCT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="required">연산대상</th>
							<td>
								<select v-model="masterForm.caltype" class="form-select">
									<option value="000">선택하세요</option>
									<option v-for="opt in calcTypeOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
								</select>
							</td>
							<th class="required">연산수식</th>
							<td>
								<select v-model="masterForm.calgagam" class="form-select">
									<option value="000">선택하세요</option>
									<option v-for="opt in formulaOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
								</select>
							</td>
						</tr>
						<tr>
							<th>사용여부</th>
							<td colspan="5">
								<div class="form-check form-switch ms-2">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useCheck" true-value="Y" false-value="N">
									<label class="form-check-label small fw-bold" for="useCheck">수식 사용</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 그리드 영역 (좌/우 분할) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex gap-2">
			<!-- 좌측: 집계계정 리스트 -->
			<div class="card border shadow-sm d-flex flex-column bg-white" style="flex: 4;">
				<div class="card-header py-1 px-2 bg-white border-bottom fw-bold small text-dark">
					<i class="bi bi-folder-fill me-1 text-warning"></i> 재무제표 집계항목
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="leftGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
			<!-- 우측: 구성 수식 리스트 -->
			<div class="card border shadow-sm d-flex flex-column bg-white" style="flex: 6;">
				<div class="card-header py-1 px-2 bg-white border-bottom fw-bold small text-dark">
					<i class="bi bi-list-stars me-1 text-info"></i> 세부 구성 수식 리스트
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

const currentYear = new Date().getFullYear()
// 적용연도 옵션: 현재년도 기준 +5년 ~ -5년
const yearOptions = Array.from({ length: 11 }, (_, i) => (currentYear + 5 - i).toString())

const searchForm = reactive({ yyyy: currentYear.toString(), gubun: '010' })
const masterForm = reactive({ actkind: 'I1', yyyy: '', gubun: '', upacct: '', upacct_t: '', acctcd: '', acctcd_t: '', arowno: '', caltype: '000', calgagam: '000', useyn: 'Y', updyn: 'Y' })

const statementOptions = ref<any[]>([]); const calcTypeOptions = ref<any[]>([]); const formulaOptions = ref<any[]>([])
const leftGridRef = ref<HTMLDivElement | null>(null); const rightGridRef = ref<HTMLDivElement | null>(null)
let leftGrid: Tabulator | null = null; let rightGrid: Tabulator | null = null

const loadInitData = async () => {
	try {
		const resGbn = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '070' })
		statementOptions.value = (resGbn.data || []).map((r: any) => ({ codecd: r.codecd, codenm: r.codenm }))
		const resType = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '080' })
		calcTypeOptions.value = (resType.data || []).map((r: any) => ({ codecd: r.codecd, codenm: r.codenm }))
		const resFormula = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '090' })
		formulaOptions.value = (resFormula.data || []).map((r: any) => ({ codecd: r.codecd, codenm: r.codenm }))
	} catch (e) { console.error('초기 데이터 로드 실패') }
}

const fetchLeftGrid = async () => {
	try {
		const res = await api.post('/api/haba/HABA_021U_STR', { actkind: 'S3', cmpycd: authStore.cmpycd, yyyy: searchForm.yyyy, gubun: searchForm.gubun })
		const processed = (res.data || []).map((r: any) => ({ gubun: r.col0, upacct: r.col1, acctnm: r.col2, gubun_NM: r.col3, updyn: r.col4, IS_IN: r.col6 === 'Y' }))
		leftGrid?.setData(processed); rightGrid?.clearData(); initialize()
	} catch (e) { vAlertError('좌측 목록 조회 실패') }
}

const fetchRightGrid = async (row: any) => {
	try {
		const res = await api.post('/api/haba/HABA_021U_STR', { actkind: 'SR', cmpycd: authStore.cmpycd, yyyy: searchForm.yyyy, gubun: row.gubun, upacct: row.upacct })
		const processed = (res.data || []).map((r: any) => ({ acctcd: r.col0, acctnm: r.col1, caltype_nm: r.col2, caltype: r.col3, calgagam_nm: r.col4, calgagam: r.col5, useyn: r.col6, gubun: r.col7, upacct: r.col8, upacct_nm: r.COL9, arowno: r.col10 }))
		rightGrid?.setData(processed)
	} catch (e) { vAlertError('수식 내역 조회 실패') }
}

const save = async () => {
	if (!masterForm.upacct) return vAlert('좌측에서 집계계정을 먼저 선택하십시오.')
	if (!masterForm.acctcd) return vAlert('계정과목을 선택하십시오.')
	if (masterForm.caltype === '000') return vAlert('연산대상을 선택하십시오.')
	if (masterForm.calgagam === '000') return vAlert('연산수식을 선택하십시오.')
	try {
		masterForm.yyyy = searchForm.yyyy; masterForm.gubun = searchForm.gubun
		const res = await api.post('/api/haba/HABA_021U_SAVE', { ...masterForm, cmpycd: authStore.cmpycd, userid: authStore.userid })
		if (res.data?.[0]?.ret_yn === 'Y') { vAlertError(res.data[0].ret_msg) } else { vAlert('저장되었습니다.'); fetchRightGrid({ gubun: masterForm.gubun, upacct: masterForm.upacct }); initialize() }
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => { const currentUpAcct = masterForm.upacct; const currentUpAcctT = masterForm.upacct_t; resetForm(masterForm); masterForm.actkind = 'I1'; masterForm.upacct = currentUpAcct; masterForm.upacct_t = currentUpAcctT; masterForm.useyn = 'Y'; masterForm.updyn = 'Y' }

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) { if (type === 'ACCT') { Object.assign(modalProps, { title: '계정과목 선택', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'A0', cmpycd: authStore.cmpycd, search: masterForm.acctcd_t }, columns: [{ title: '코드', field: 'col0', width: 80 }, { title: '계정명', field: 'col1', width: 180 }], onConfirm: (d: any) => { masterForm.acctcd = d.col0; masterForm.acctcd_t = d.col1 } }); modalVisible.value = true } }

onMounted(() => {
	loadInitData()
	if (leftGridRef.value) {
		leftGrid = new Tabulator(leftGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columns: [
				{ title: "코드", field: "upacct", width: 90, hozAlign: "center" },
				{
					title: "집계계정명", field: "acctnm",
					formatter: (cell) => {
						const code = String(cell.getData().upacct); let indent = code.endsWith("00000") ? 0 : code.endsWith("0000") ? 10 : code.endsWith("00") ? 20 : 30;
						const color = cell.getData().IS_IN ? '#0d6efd' : '#212529';
						return `<div style="padding-left: ${indent}px; color: ${color}; font-weight: ${cell.getData().IS_IN ? '800' : 'normal'}">${indent > 0 ? '· ' : ''}${cell.getValue()}</div>`
					}
				},
				{ title: "구분", field: "gubun_NM", width: 70, hozAlign: "center" }
			],
			rowClick: (e, row) => { const d = row.getData(); masterForm.upacct = d.upacct; masterForm.upacct_t = d.acctnm; masterForm.gubun = d.gubun; masterForm.updyn = d.updyn; fetchRightGrid(d); initialize() }
		})
	}
	if (rightGridRef.value) {
		rightGrid = new Tabulator(rightGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columns: [
				{ title: "계정코드", field: "acctcd", width: 90, hozAlign: "center" },
				{ title: "계정과목명", field: "acctnm", width: 180, cssClass: "fw-bold text-primary" },
				{ title: "연산대상", field: "caltype_nm", width: 100, hozAlign: "center" },
				{ title: "연산수식", field: "calgagam_nm", width: 100, hozAlign: "center" },
				{ title: "사용", field: "useyn", width: 60, hozAlign: "center", formatter: "tickCross" }
			],
			rowClick: (e, row) => { const d = row.getData(); Object.assign(masterForm, d); masterForm.actkind = 'U1'; masterForm.acctcd_t = d.acctnm }
		})
	}
})
</script>
