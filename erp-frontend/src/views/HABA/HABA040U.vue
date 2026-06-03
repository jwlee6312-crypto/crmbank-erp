<!--
	=============================================================
	프로그램명	: 보조과목관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 계정과목의 보조부유형이 기타인 코드 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-collection me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">보조과목관리 (HABA040U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-plus-lg"></i> 신규
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-check-lg"></i> 저장
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 250px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">보조과목 명</th>
							<td class="bg-white border-end px-2">
								<input v-model="searchForm.SUBNM_H" type="text" class="form-control form-control-sm" placeholder="검색어 입력" @keydown.enter="search" />
							</td>
							<td class="bg-white px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> 보조과목명을 입력하여 검색하실 수 있습니다.
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📝 상세 정보 입력 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> 보조과목 상세 정보 [{{ masterForm.actkind === 'A0' ? '신규' : '수정' }}]</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 30%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">보조과목</th>
							<td class="bg-white border-end px-2">
								<input v-model="masterForm.SUBCD" type="text" class="form-control form-control-sm text-center fw-bold" maxlength="5" :readonly="masterForm.actkind === 'U0'" placeholder="코드 입력" />
							</td>
							<th class="text-center bg-light-subtle border-end">보조과목명</th>
							<td colspan="3" class="bg-white px-2">
								<input v-model="masterForm.SUBNM" type="text" class="form-control form-control-sm" maxlength="30" />
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
							<td class="bg-white border-end border-top px-2">
								<input v-model="masterForm.remark" type="text" class="form-control form-control-sm" maxlength="50" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">출현순서</th>
							<td class="bg-white border-end border-top px-2">
								<input v-model="masterForm.dspord" type="text" class="form-control form-control-sm text-center" maxlength="3" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">사용여부</th>
							<td class="bg-white border-top px-3">
								<div class="form-check pt-1">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useCheck" true-value="Y" false-value="N">
									<label class="form-check-label small fw-bold" for="useCheck">사용</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<div class="mt-2 ps-1 text-primary small fw-bold">
				&nbsp;☞계정과목의 보조부유형이 기타인 코드를 등록하여 관리한다.
			</div>
		</div>

		<!-- 📊 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 🔍 검색 데이터
const searchForm = reactive({
	SUBNM_H: ''
})

// 📝 마스터 데이터
const masterForm = reactive({
	actkind: 'A0',
	SUBCD: '',
	SUBNM: '',
	remark: '',
	dspord: '',
	useyn: 'Y'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_040U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			SUBNM_H: searchForm.SUBNM_H
		})

		const processedData = (res.data || []).map((r: any) => ({
			SUBCD: r.SUBCD || r.col0,
			SUBNM: r.SUBNM || r.col1,
			remark: r.bigo || r.col2,
			dspord: r.dspord || r.col3,
			useyn: r.useyn || r.col4
		}))

		mainGrid?.setData(processedData)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.SUBCD) return vAlert('보조과목 코드를 입력하십시오.')
	if (!masterForm.SUBNM) return vAlert('보조과목명을 입력하십시오.')
	if (!masterForm.dspord) return vAlert('출현순서를 입력하십시오.')

	try {
		const payload = {
			...masterForm,
			cmpycd: authStore.cmpycd,
			userid: authStore.userid
		}

		const res = await api.post('/api/haba/HABA_040U_STR', payload)

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].RET_MSG)
		} else {
			vAlert('저장되었습니다.')
			search()
			initialize()
		}
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.actkind = 'A0'
	masterForm.useyn = 'Y'
}

const excel = () => mainGrid?.download("xlsx", `보조과목관리_${new Date().toISOString().substring(0, 10)}.xlsx`)

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "보조과목", field: "SUBCD", width: 100, hozAlign: "center" },
				{ title: "보조과목 명", field: "SUBNM", minWidth: 200 },
				{ title: "비고", field: "remark", minWidth: 300 },
				{ title: "출현순서", field: "dspord", width: 100, hozAlign: "center" },
				{ title: "사용", field: "useyn", width: 80, hozAlign: "center", formatter: "tickCross" }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				Object.assign(masterForm, d)
				masterForm.actkind = 'U0'
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
