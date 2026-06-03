<!--기본정보/사업장관리 [ERP 프리미엄 고밀도 표준 - 3열 5행 & 간격 확보] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-2 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-geo-alt-fill me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">사업장 관리 (HABA030U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 (섹션 간 확실한 간격 확보: gap-3) -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-3 p-3">

			<!-- 🔍 2. 검색 바 섹션 (표준 패턴) -->
			<div class="card border-0 shadow-sm flex-shrink-0">
				<div class="card-body p-2 bg-white rounded">
					<div class="d-flex align-items-center gap-3">
						<div class="input-group input-group-sm flex-nowrap" style="width: 420px;">
							<span class="input-group-text bg-light fw-bold px-3 border-0">사업장명 검색</span>
							<input v-model="searchForm.unitnm" class="form-control border-light-subtle" placeholder="사업장명 입력 후 엔터..." @keyup.enter="fetchList" />
							<button class="btn btn-dark px-3" @click="fetchList"><i class="bi bi-search"></i></button>
						</div>
						<div class="small text-muted border-start ps-3"><i class="bi bi-info-circle me-1"></i> 사업장명을 입력하여 필터링 조회가 가능합니다.</div>
					</div>
				</div>
			</div>

			<!-- 💡 3. 상세 입수정 영역 (3열 5행 구조 가이드 준수) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>사업장 상세 정보 등록</div>
					<div v-if="formData.actkind === 'U0'" class="badge bg-primary text-white px-2">수정 중</div>
					<div v-else class="badge bg-success text-white px-2">신규 등록</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<!-- 1행 -->
							<tr>
								<th class="required">사업장코드</th>
								<td><input v-model="formData.taxunit" type="text" class="form-control fw-bold text-primary text-center" maxlength="3" :disabled="formData.actkind === 'U0'" /></td>
								<th class="required">사업장명칭</th>
								<td><input v-model="formData.unitnm" type="text" class="form-control" /></td>
								<th class="required">사업자번호</th>
								<td><input v-model="formData.saupno" type="text" class="form-control text-center" maxlength="10" /></td>
							</tr>
							<!-- 2행 -->
							<tr>
								<th>대표자성명</th>
								<td><input v-model="formData.bossnm" type="text" class="form-control" /></td>
								<th>우편번호</th>
								<td>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="formData.postno" type="text" class="form-control bg-light text-center fw-bold" style="max-width: 80px;" readonly />
										<button class="btn btn-outline-secondary px-2" @click="execPostcode"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>사용여부</th>
								<td>
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYn030">
										<label class="form-check-label ms-2 small fw-bold" for="useYn030">{{ formData.useyn === 'Y' ? '사용 중' : '중지' }}</label>
									</div>
								</td>
							</tr>
							<!-- 3행 -->
							<tr>
								<th>주소(기본)</th>
								<td colspan="3"><input v-model="formData.address" type="text" class="form-control" placeholder="기본 주소를 입력하세요" /></td>
								<th>정렬순서</th>
								<td><input v-model="formData.dspord" type="number" class="form-control text-end" /></td>
							</tr>
							<!-- 4행 (확장 공간) -->
							<tr>
								<th>법인번호</th>
								<td><input v-model="formData.legalno" type="text" class="form-control text-center" maxlength="13" /></td>
								<th>업    태</th>
								<td><input v-model="formData.uptae" type="text" class="form-control" /></td>
								<th>종    목</th>
								<td><input v-model="formData.upjong" type="text" class="form-control" /></td>
							</tr>
							<!-- 5행 (확장 공간) -->
							<tr>
								<th>전화번호</th>
								<td><input v-model="formData.telno" type="text" class="form-control text-center" /></td>
								<th>팩스번호</th>
								<td><input v-model="formData.FAXNO" type="text" class="form-control text-center" /></td>
								<td colspan="2" class="bg-light-subtle"></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 4. 하단 그리드 영역 (전체 폭 복구 & 자동 높이 확보) -->
			<div class="card border-0 shadow-sm flex-grow-1 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-table me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">사업장 전체 목록</span>
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

const searchForm = reactive({ unitnm: '' })
const formData = reactive({
	actkind: 'SR', taxunit: '', unitnm: '', saupno: '', bossnm: '', postno: '', address: '', dspord: '1', useyn: 'Y',
	legalno: '', uptae: '', upjong: '', telno: '', FAXNO: '', cmpycd: authStore.cmpycd, userid: authStore.userid
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

async function fetchList() {
	try {
		const res = await api.post('/api/haba/HABA_030U_STR', { actkind: 'SR', cmpycd: authStore.cmpycd, unitnm: searchForm.unitnm })
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

async function save() {
	if (!formData.taxunit || !formData.unitnm) return vAlertError('사업장코드와 명칭은 필수입니다.')
	try {
		const act = formData.actkind === 'SR' ? 'A0' : 'U0';
		await api.post('/api/haba/HABA_030U_STR', { ...formData, actkind: act, updemp: authStore.userid })
		vAlert('저장되었습니다.')
		fetchList()
	} catch (e) { vAlertError('저장 실패') }
}

function initialize() {
	resetForm(formData); formData.actkind = 'SR'; formData.useyn = 'Y'; formData.cmpycd = authStore.cmpycd;
}

function execPostcode() { /* 우편번호 로직 */ }

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center' },
			columns: [
				{ title: '코드', field: 'taxunit', hozAlign: 'center', width: 80, cssClass: 'fw-bold text-primary border-end' },
				{ title: '사업장명', field: 'unitnm', minWidth: 150, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '사업자번호', field: 'saupno', hozAlign: 'center', width: 120 },
				{ title: '대표자', field: 'bossnm', hozAlign: 'center', width: 100 },
				{ title: '주소', field: 'address', minWidth: 300, widthGrow: 1 },
				{ title: '사용', field: 'useyn', hozAlign: 'center', width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		mainGrid.on('rowClick', (e, row) => { Object.assign(formData, row.getData()); formData.actkind = 'U0' })
	}
	fetchList()
})
</script>

