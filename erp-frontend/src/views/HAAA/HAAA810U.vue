<!--관리정보/시스템관리/분류코드관리 [ERP 프리미엄 고밀도 표준 - 3열 배치] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="haaa810u-wrapper d-flex flex-column h-100 bg-light p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 shadow-sm sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-folder-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">분류코드 관리 (HAAA810U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
				<button v-if="formData.ACTKIND === 'U0'" class="btn-erp btn-delete" @click="handleDelete">삭제</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 (gap-3 적용) -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-3 p-3">

			<!-- 🔍 2. 검색 바 섹션 -->
			<div class="card border-0 shadow-sm flex-shrink-0">
				<div class="card-body p-2 bg-white rounded">
					<div class="d-flex align-items-center gap-3">
						<div class="input-group input-group-sm flex-nowrap" style="width: 400px;">
							<span class="input-group-text bg-light fw-bold px-3 border-0">업무(대분류) 선택</span>
							<select v-model="searchForm.UPMUCD" class="form-select border-light-subtle" @change="fetchList">
								<option v-for="opt in upmuOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
							</select>
						</div>
					</div>
				</div>
			</div>

			<!-- 💡 3. 상세 입수정 영역 (3열 배치) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>분류 상세 정보</div>
					<div v-if="formData.ACTKIND === 'U0'" class="badge bg-primary text-white px-2">수정 중</div>
					<div v-else class="badge bg-success text-white px-2">신규 등록</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">업    무</th>
								<td>
									<select v-model="formData.UPMUCD" class="form-select bg-light" disabled>
										<option v-for="opt in upmuOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
									</select>
								</td>
								<th class="required">분류코드</th>
								<td><input v-model="formData.GRPCD" type="text" class="form-control fw-bold text-primary text-center" maxlength="3" :disabled="formData.ACTKIND === 'U0'" /></td>
								<th class="required">분 류 명</th>
								<td><input v-model="formData.GRPNM" type="text" class="form-control" placeholder="분류명칭" /></td>
							</tr>
							<tr>
								<th class="required">정렬순서</th>
								<td><input v-model="formData.DSPORD" type="number" class="form-control text-end" /></td>
								<th>사용여부</th>
								<td>
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.USEYN" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYn810">
										<label class="form-check-label ms-2 small fw-bold" for="useYn810">{{ formData.USEYN === 'Y' ? '사용' : '중지' }}</label>
									</div>
								</td>
								<td colspan="2" class="bg-light"></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 4. 하단 그리드 영역 -->
			<div class="card border-0 shadow-sm flex-grow-1 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-table me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">메뉴 그룹 목록</span>
				</div>
				<div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
					<div ref="mainGridRef" class="tabulator-full-height"></div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchForm = reactive({ UPMUCD: '' })
const formData = reactive({
	ACTKIND: 'S0', UPMUCD: '', GRPCD: '', GRPNM: '', DSPORD: '1', USEYN: 'Y', CMPYCD: authStore.CMPYCD, USERID: authStore.USER_ID
})

const upmuOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

async function fetchUpmu() {
	try {
		const res = await api.post('/api/ha00/HA00_00P_STR', { GUBUN: 'E0', GBNCD: '320', CMPYCD: authStore.CMPYCD })
		if (res.data) {
			upmuOptions.value = res.data.map((i: any) => ({ CODECD: String(i.CODECD || i.CODE).trim(), CODENM: String(i.CODENM || i.CDNM).trim() }))
			if (upmuOptions.value.length > 0) {
				searchForm.UPMUCD = upmuOptions.value[0].CODECD;
				formData.UPMUCD = upmuOptions.value[0].CODECD;
				await fetchSearchGrpcd();
			}
		}
	} catch (e) { console.error('업무코드 로드 실패') }
}

async function fetchList() {
	if (!searchForm.UPMUCD) return
	try {
		const res = await api.post('/api/haaa/HAAA_810U_STR', { ACTKIND: 'S0', UPMUCD: searchForm.UPMUCD, CMPYCD: authStore.CMPYCD })
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

async function save() {
	if (!formData.GRPCD || !formData.GRPNM) return vAlertError('필수값 확인')
	try {
		const act = formData.ACTKIND === 'S0' ? 'A0' : 'U0';
		await api.post('/api/haaa/HAAA_810U_STR', { ...formData, ACTKIND: act, CMPYCD: authStore.CMPYCD })
		vAlert('저장되었습니다.')
		fetchList()
	} catch (e) { vAlertError('저장 실패') }
}

async function handleDelete() {
	if (!confirm('삭제하시겠습니까?')) return
	try {
		await api.post('/api/haaa/HAAA_810U_STR', { ...formData, ACTKIND: 'D0', CMPYCD: authStore.CMPYCD })
		vAlert('삭제되었습니다.')
		fetchList(); initialize()
	} catch (e) { vAlertError('삭제 실패') }
}

function initialize() {
	resetForm(formData); formData.UPMUCD = searchForm.UPMUCD; formData.ACTKIND = 'S0'; formData.USEYN = 'Y'; formData.DSPORD = '1'; formData.CMPYCD = authStore.CMPYCD;
}

watch(() => searchForm.UPMUCD, (newVal) => { formData.UPMUCD = newVal })

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center' },
			columns: [
				{ title: '분류코드', field: 'GRPCD', hozAlign: 'center', width: 110, cssClass: 'fw-bold text-primary border-end' },
				{ title: '분류 명칭', field: 'GRPNM', minWidth: 250, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '순서', field: 'DSPORD', hozAlign: 'center', width: 100 },
				{ title: '사용', field: 'USEYN', hozAlign: 'center', width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		mainGrid.on('rowClick', (e, row) => { Object.assign(formData, row.getData()); formData.ACTKIND = 'U0' })
	}
	await fetchUpmu()
})
</script>

<style scoped>
.haaa810u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }
.btn-group-erp { display: flex; gap: 6px; }
.btn-erp { padding: 4px 18px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff; color: #6c757d; border: 1px solid #6c757d; }
.btn-init:hover { background-color: #f8f9fa; }
.btn-search { background-color: #2d3748; color: #fff; border: none; }
.btn-save { background-color: #005a9f; color: #fff; border: none; }
.btn-delete { background-color: #d32f2f; color: #fff; border: none; }
.erp-table-full { table-layout: fixed; border-collapse: collapse !important; width: 100%; border-style: hidden; }
.erp-table-full th { background-color: #f8fafc; border: 1px solid #e2e8f0 !important; text-align: center; font-weight: 800; font-size: 12px; color: #475569; vertical-align: middle; padding: 10px 15px !important; white-space: nowrap; }
.erp-table-full td { vertical-align: middle; padding: 8px 12px !important; border: 1px solid #e2e8f0 !important; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
.form-control, .form-select { border-radius: 4px; border: 1px solid #cbd5e1; font-size: 13px; height: 32px; width: 100%; font-weight: 500; }
:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 800; color: #334155; }
:deep(.tabulator-row.tabulator-selected) { background-color: #f0f7ff !important; border-left: 4px solid #005a9f !important; }
</style>
