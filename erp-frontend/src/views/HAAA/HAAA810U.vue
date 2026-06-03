<!--관리정보/시스템관리/분류코드관리 [ERP 프리미엄 고밀도 표준 - 1열 배치] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 shadow-sm sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-folder-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">분류코드 관리 (HAAA810U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
				<button v-if="formData.actkind === 'U0'" class="btn-erp btn-delete" @click="deleteData">삭제</button>
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
							<select v-model="searchForm.upmucd" class="form-select border-light-subtle" @change="search">
								<option v-for="opt in upmuOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
							</select>
						</div>
					</div>
				</div>
			</div>

			<!-- 💡 3. 상세 입수정 영역 (한 줄 배치) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>분류 상세 정보</div>
					<div v-if="formData.actkind === 'U0'" class="badge bg-primary text-white px-2">수정 중</div>
					<div v-else class="badge bg-success text-white px-2">신규 등록</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">업    무</th>
								<td>
									<select v-model="formData.upmucd" class="form-select bg-light" disabled>
										<option v-for="opt in upmuOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
									</select>
								</td>
								<th class="required">분류코드</th>
								<td><input v-model="formData.grpcd" type="text" class="form-control fw-bold text-primary text-center" maxlength="3" :disabled="formData.actkind === 'U0'" /></td>
								<th class="required">분 류 명</th>
								<td><input v-model="formData.grpnm" type="text" class="form-control" placeholder="분류명칭" /></td>
								<th class="required">정렬순서</th>
								<td><input v-model="formData.dspord" type="number" class="form-control text-end" /></td>
								<th>사용여부</th>
								<td>
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYn810">
										<label class="form-check-label ms-2 small fw-bold" for="useYn810">{{ formData.useyn === 'Y' ? '사용' : '중지' }}</label>
									</div>
								</td>
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
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                    <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
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

const searchForm = reactive({ upmucd: '' })
const formData = reactive({
	actkind: 'S0', upmucd: '', grpcd: '', grpnm: '', dspord: '1', useyn: 'Y', cmpycd: authStore.cmpycd, userid: authStore.user_id
})

const upmuOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

async function fetchUpmu() {
	try {
		const res = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '320', cmpycd: authStore.cmpycd })
		if (res.data) {
			upmuOptions.value = res.data.map((i: any) => ({ codecd: String(i.codecd || i.code).trim(), codenm: String(i.codenm || i.cdnm).trim() }))
			if (upmuOptions.value.length > 0) {
				searchForm.upmucd = upmuOptions.value[0].codecd;
				formData.upmucd = upmuOptions.value[0].codecd;
				// await fetchSearchGrpcd(); // HAAA810U doesn't have fetchSearchGrpcd
			}
		}
	} catch (e) { console.error('업무코드 로드 실패') }
}

async function search() {
	if (!searchForm.upmucd) return
	try {
		const res = await api.post('/api/haaa/HAAA_810U_STR', { actkind: 'S0', upmucd: searchForm.upmucd, cmpycd: authStore.cmpycd })
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

async function save() {
	if (!formData.grpcd || !formData.grpnm) return vAlertError('필수값 확인')
	try {
		const act = formData.actkind === 'S0' ? 'A0' : 'U0';
		await api.post('/api/haaa/HAAA_810U_STR', { ...formData, actkind: act, cmpycd: authStore.cmpycd })
		vAlert('저장되었습니다.')
		search()
	} catch (e) { vAlertError('저장 실패') }
}

async function deleteData() {
	if (!confirm('삭제하시겠습니까?')) return
	try {
		await api.post('/api/haaa/HAAA_810U_STR', { ...formData, actkind: 'D0', cmpycd: authStore.cmpycd })
		vAlert('삭제되었습니다.')
		search(); initialize()
	} catch (e) { vAlertError('삭제 실패') }
}

function initialize() {
	resetForm(formData); formData.upmucd = searchForm.upmucd; formData.actkind = 'S0'; formData.useyn = 'Y'; formData.dspord = '1'; formData.cmpycd = authStore.cmpycd;
}

watch(() => searchForm.upmucd, (newVal) => { formData.upmucd = newVal })

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center' },
			columns: [
				{ title: '분류코드', field: 'grpcd', hozAlign: 'center', width: 110, cssClass: 'fw-bold text-primary border-end' },
				{ title: '분류 명칭', field: 'grpnm', minWidth: 250, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '순서', field: 'dspord', hozAlign: 'center', width: 100 },
				{ title: '사용', field: 'useyn', hozAlign: 'center', width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		mainGrid.on('rowClick', (e, row) => { Object.assign(formData, row.getData()); formData.actkind = 'U0' })
	}
	await fetchUpmu()
})
</script>

