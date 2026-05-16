<!--기본정보/조직도관리 [ERP 프리미엄 고밀도 표준 적용] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="haba060u-wrapper d-flex flex-column h-100 bg-light p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-2 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-diagram-3-fill me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">조직도 관리 (HABA060U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 (gap-3 적용) -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-3 p-3">

			<!-- 🔍 2. 검색 바 섹션 -->
			<div class="card border-0 shadow-sm flex-shrink-0">
				<div class="card-body p-2 bg-white rounded">
					<div class="d-flex align-items-center gap-3">
						<div class="input-group input-group-sm flex-nowrap" style="width: 420px;">
							<span class="input-group-text bg-light fw-bold px-3 border-0">부서명 검색</span>
							<input v-model="searchForm.DEPTNM_S" class="form-control border-light-subtle" placeholder="부서명을 입력하고 엔터..." @keyup.enter="fetchList" />
							<button class="btn btn-dark px-3" @click="fetchList"><i class="bi bi-search"></i></button>
						</div>
					</div>
				</div>
			</div>

			<!-- 💡 3. 상세 입수정 영역 (3열 배치) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>부서 상세 정보 관리</div>
					<div v-if="formData.ACTKIND === 'U0'" class="badge bg-primary text-white px-2">수정 중</div>
					<div v-else class="badge bg-success text-white px-2">신규 등록</div>
				</div>
				<div class="card-body p-0 bg-white border-bottom">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">부서코드</th>
								<td><input v-model="formData.DEPTCD" type="text" class="form-control fw-bold text-primary text-center" maxlength="5" :disabled="formData.ACTKIND === 'U0'"/></td>
								<th class="required">부서명칭</th>
								<td><input v-model="formData.DEPTNM" type="text" class="form-control" /></td>
								<th>영문명칭</th>
								<td><input v-model="formData.DEPTENM" type="text" class="form-control" /></td>
							</tr>
							<tr>
								<th>상위부서</th>
								<td>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="formData.UPDEPT" type="text" class="form-control bg-light text-center fw-bold" style="max-width: 60px;" readonly />
										<input v-model="formData.UPDEPTNM" type="text" class="form-control border-start-0" placeholder="상위부서 검색" @keyup.enter="openDeptPopup" />
										<button class="btn btn-outline-secondary px-2" @click="openDeptPopup"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>사업장</th>
								<td>
									<select v-model="formData.TAXUNIT" class="form-select">
										<option value="">-- 선택 --</option>
										<option v-for="opt in saOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
									</select>
								</td>
								<th>비용구분</th>
								<td>
									<select v-model="formData.COSTGBN" class="form-select">
										<option value="">-- 선택 --</option>
										<option v-for="opt in costOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
									</select>
								</td>
							</tr>
							<tr>
								<th class="required">적용일자</th>
								<td><input v-model="formData.DEPTOPN" type="date" class="form-control" /></td>
								<th>유효일자</th>
								<td><input v-model="formData.DEPTCLD" type="date" class="form-control" /></td>
								<th>사용여부</th>
								<td class="bg-light-subtle text-center">
									<div class="form-check form-switch m-0 d-inline-block">
										<input v-model="formData.USEYN" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYn060">
										<label class="form-check-label ms-2 small fw-bold" for="useYn060">{{ formData.USEYN === 'Y' ? '사용 중' : '중지' }}</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 4. 하단 그리드 영역 (전체 폭 복구) -->
			<div class="card border-0 shadow-sm flex-grow-1 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-table me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">부서 목록</span>
				</div>
				<div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
					<div ref="mainGridRef" class="tabulator-full-height"></div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchForm = reactive({ DEPTNM_S: '' })
const formData = reactive({
	ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, DEPTCD: '', DEPTNM: '', DEPTENM: '', DEPTSNM: '', UPDEPT: '', UPDEPTNM: '',
	DEPTDUM: 'N', TAXUNIT: '', COSTGBN: '', DEPTEMP: '', DEPTEMPNM: '',
	DEPTOPN: new Date().toISOString().substring(0, 10), DEPTCLD: '', DSPORD: '1', USEYN: 'Y'
})

const saOptions = ref<any[]>([])
const costOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

async function fetchCodes() {
	try {
		// 💡 구형 API 정리 및 표준 프로시저 호출로 변경
		const [resSa, resCost] = await Promise.all([
			api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: '030', CMPYCD: authStore.CMPYCD } }),
			api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: '310', CMPYCD: authStore.CMPYCD } })
		])

		const normalize = (data: any[]) => (data || []).map(i => ({
			CODECD: String(i.CODECD || i.UNITCD || Object.values(i)[0]).trim(),
			CODENM: String(i.CODENM || i.UNITNM || Object.values(i)[1]).trim()
		}))

		saOptions.value = normalize(resSa.data)
		costOptions.value = normalize(resCost.data)
	} catch (e) { console.error('코드 로드 실패') }
}

async function fetchList() {
	try {
		const res = await api.post('/api/haba/HABA_060U_STR', {
			ACTKIND: 'S0',
			CMPYCD: authStore.CMPYCD,
			DEPTNM: searchForm.DEPTNM_S
		})
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

async function save() {
	if (!formData.DEPTCD || !formData.DEPTNM) return vAlertError('부서코드와 부서명칭은 필수입니다.')
	try {
		const act = formData.ACTKIND === 'S0' ? 'A0' : 'U0';
		await api.post('/api/haba/HABA_060U_STR', { ...formData, ACTKIND: act, UPDEMP: authStore.USERID })
		vAlert('저장이 완료되었습니다.')
		fetchList()
	} catch (e) { vAlertError('저장 실패') }
}

function initialize() {
	resetForm(formData); formData.ACTKIND = 'S0'; formData.USEYN = 'Y'; formData.CMPYCD = authStore.CMPYCD;
}

function openDeptPopup() { /* 팝업 로직 */ }

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '표시할 데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center' },
			columns: [
				{ title: '부서코드', field: 'DEPTCD', hozAlign: 'center', width: 100, cssClass: 'fw-bold text-primary border-end' },
				{ title: '부서명칭', field: 'DEPTNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '상위부서', field: 'UPDEPTNM', minWidth: 150, widthGrow: 1 },
				{ title: '적용일', field: 'DEPTOPN', hozAlign: 'center', width: 120 },
				{ title: '사용', field: 'USEYN', hozAlign: 'center', width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		mainGrid.on('rowClick', (e, row) => { Object.assign(formData, row.getData()); formData.ACTKIND = 'U0' })
	}
	await fetchCodes(); fetchList()
})
</script>

<style scoped>
.haba060u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }
.btn-group-erp { display: flex; gap: 6px; }
.btn-erp { padding: 4px 18px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff; color: #6c757d; border: 1px solid #6c757d; }
.btn-init:hover { background-color: #f8f9fa; }
.btn-search { background-color: #2d3748; color: #fff; border: none; }
.btn-save { background-color: #005a9f; color: #fff; border: none; }
.erp-table-full { table-layout: fixed !important; border-collapse: collapse !important; width: 100%; border-style: hidden; }
.erp-table-full th { background-color: #f8fafc; border: 1px solid #e2e8f0 !important; text-align: center; font-weight: 800; font-size: 12px; color: #475569; vertical-align: middle; padding: 8px 12px !important; white-space: nowrap; }
.erp-table-full td { vertical-align: middle; padding: 6px 10px !important; border: 1px solid #e2e8f0 !important; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
.form-control, .form-select { border-radius: 4px; border: 1px solid #cbd5e1; font-size: 13px; height: 32px; width: 100%; font-weight: 500; }
.form-control:focus, .form-select:focus { border-color: #005a9f; box-shadow: 0 0 0 2px rgba(0, 90, 159, 0.1); }
:deep(.tabulator) { border: none; font-size: 12.5px; background-color: transparent !important; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 800; color: #334155; }
:deep(.tabulator-row.tabulator-selected) { background-color: #f0f7ff !important; border-left: 4px solid #005a9f !important; }
:deep(.tabulator-cell) { border-right: 1px solid #f1f5f9 !important; padding: 8px 4px !important; }
</style>
