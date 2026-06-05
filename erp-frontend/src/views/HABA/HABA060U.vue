<!--기본정보/조직도관리 [ERP 프리미엄 고밀도 표준 적용] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
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
							<input v-model="searchForm.deptnm_s" class="form-control border-light-subtle" placeholder="부서명을 입력하고 엔터..." @keyup.enter="fetchList" />
							<button class="btn btn-dark px-3" @click="fetchList"><i class="bi bi-search"></i></button>
						</div>
					</div>
				</div>
			</div>

			<!-- 💡 3. 상세 입수정 영역 (3열 배치) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>부서 상세 정보 관리</div>
					<div v-if="formData.actkind === 'U0'" class="badge bg-primary text-white px-2">수정 중</div>
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
								<td><input v-model="formData.deptcd" type="text" class="form-control fw-bold text-primary text-center" maxlength="5" :disabled="formData.actkind === 'U0'"/></td>
								<th class="required">부서명칭</th>
								<td><input v-model="formData.deptnm" type="text" class="form-control" /></td>
								<th>영문명칭</th>
								<td><input v-model="formData.deptenm" type="text" class="form-control" /></td>
							</tr>
							<tr>
								<th>상위부서</th>
								<td>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="formData.updept" type="text" class="form-control bg-light text-center fw-bold" style="max-width: 60px;" readonly />
										<input v-model="formData.updeptnm" type="text" class="form-control border-start-0" placeholder="상위부서 검색" @keyup.enter="openDeptPopup" />
										<button class="btn btn-outline-secondary px-2" @click="openDeptPopup"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>사업장</th>
								<td>
									<select v-model="formData.taxunit" class="form-select">
										<option value="">-- 선택 --</option>
										<option v-for="opt in saOptions" :key="opt.taxunit" :value="opt.taxunit">{{ opt.unitnm }}</option>
									</select>
								</td>
								<th>비용구분</th>
								<td>
									<select v-model="formData.costgbn" class="form-select">
										<option value="">-- 선택 --</option>
										<option value="100">관리비용</option>
										<option value="110">제조비용</option>
									</select>
								</td>
							</tr>
							<tr>
								<th class="required">적용일자</th>
								<td><input v-model="formData.deptopn" type="date" class="form-control" /></td>
								<th>유효일자</th>
								<td><input v-model="formData.deptcld" type="date" class="form-control" /></td>
								<th>사용여부</th>
								<td class="bg-light-subtle text-center">
									<div class="form-check form-switch m-0 d-inline-block">
										<input v-model="formData.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYn060">
										<label class="form-check-label ms-2 small fw-bold" for="useYn060">{{ formData.useyn === 'Y' ? '사용 중' : '중지' }}</label>
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
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                    <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
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

const searchForm = reactive({ deptnm_s: '' })
const formData = reactive({
	actkind: 'S0', cmpycd: authStore.cmpycd, deptcd: '', deptnm: '', deptenm: '', deptsnm: '', updept: '', updeptnm: '',
	deptdum: 'N', taxunit: '', costgbn: '', DEPTEMP: '', deptempnm: '',
	deptopn: new Date().toISOString().substring(0, 10), deptcld: '', dspord: '1', useyn: 'Y'
})

const saOptions = ref<any[]>([])
const costOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

async function fetchCodes() {
	try {
		// 💡 구형 API 정리 및 표준 프로시저 호출로 변경
		const [resSa] = await Promise.all([
			api.get('/api/ha00/HA00_00P_STR', { params: { gubun: 'SA', cmpycd: authStore.cmpycd } })
		])

		const normalize = (data: any[]) => (data || []).map(i => ({
			taxunit: String(i.taxunit || Object.values(i)[0]).trim(),
			unitnm: String(i.unitnm || Object.values(i)[1]).trim()
		}))
		saOptions.value = normalize(resSa.data)
	} catch (e) { console.error('코드 로드 실패') }
}

async function fetchList() {
	try {
		const res = await api.post('/api/haba/HABA_060U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			deptnm: searchForm.deptnm_s
		})
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

async function save() {
	if (!formData.deptcd || !formData.deptnm) return vAlertError('부서코드와 부서명칭은 필수입니다.')
	try {
		const act = formData.actkind === 'S0' ? 'A0' : 'U0';
		await api.post('/api/haba/HABA_060U_STR', { ...formData, actkind: act, updemp: authStore.userid })
		vAlert('저장이 완료되었습니다.')
		fetchList()
	} catch (e) { vAlertError('저장 실패') }
}

function initialize() {
	resetForm(formData); formData.actkind = 'S0'; formData.useyn = 'Y'; formData.cmpycd = authStore.cmpycd;
}

function openDeptPopup() { /* 팝업 로직 */ }

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '표시할 데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center' },
			columns: [
				{ title: '부서코드', field: 'deptcd', hozAlign: 'center', width: 100, cssClass: 'fw-bold text-primary border-end' },
				{ title: '부서명칭', field: 'deptnm', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '상위부서', field: 'updeptnm', minWidth: 150, widthGrow: 1 },
				{ title: '적용일', field: 'deptopn', hozAlign: 'center', width: 120 },
				{ title: '사용', field: 'useyn', hozAlign: 'center', width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		mainGrid.on('rowClick', (e, row) => { Object.assign(formData, row.getData()); formData.actkind = 'U0' })
	}
	await fetchCodes(); fetchList()
})
</script>
