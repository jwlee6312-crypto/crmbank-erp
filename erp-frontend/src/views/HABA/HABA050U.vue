<!--
	=============================================================
	프로그램명	: 금융기관관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 금융기관(은행) 코드 및 기본 정보 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 (표준 규격) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-bank me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">금융기관관리 (HABA050U)</span>
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

		<!-- 🔍 검색 조건 영역 (표준 erp-table-full 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full">
					<colgroup>
						<col style="width: 80px;" /><col style="width: 250px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th>은 행 명</th>
							<td class="bg-white">
								<input v-model="searchForm.banknm_h" type="text" class="form-control" placeholder="은행명 입력" @keydown.enter="search" />
							</td>
							<td class="bg-white"></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📝 상세 정보 입력 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-3 bg-white border-bottom d-flex align-items-center">
					<i class="bi bi-pencil-square me-2 text-primary"></i>
					<span class="fw-bold small text-dark">은행 상세 정보 [{{ masterForm.actkind === 'A0' ? '신규등록' : '정보수정' }}]</span>
				</div>
				<table class="erp-table-full">
					<colgroup>
						<col style="width: 80px;" /><col style="width: 20%;" />
						<col style="width: 80px;" /><col style="width: 30%;" />
						<col style="width: 80px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="required">은행코드</th>
							<td class="bg-white">
								<input v-model="masterForm.bankcd" type="text" class="form-control text-center fw-bold" maxlength="5" :readonly="masterForm.actkind === 'U0'" placeholder="코드" />
							</td>
							<th class="required">은 행 명</th>
							<td class="bg-white">
								<input v-model="masterForm.banknm" type="text" class="form-control" maxlength="30" />
							</td>
							<th>비 고</th>
							<td class="bg-white">
								<input v-model="masterForm.remark" type="text" class="form-control" maxlength="50" />
							</td>
						</tr>
						<tr>
							<th class="required">출현순서</th>
							<td class="bg-white">
								<input v-model="masterForm.dspord" type="text" class="form-control text-center" maxlength="3" />
							</td>
							<th>사용여부</th>
							<td colspan="3" class="bg-white">
								<div class="form-check form-switch ms-2">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useCheck" true-value="Y" false-value="N">
									<label class="form-check-label small fw-bold" for="useCheck">사용</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 그리드 영역 (표준 통합CSS 적용) -->
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

const searchForm = reactive({ banknm_h: '' })
const masterForm = reactive({ actkind: 'A0', bankcd: '', banknm: '', remark: '', dspord: '', useyn: 'Y' })

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_050U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, banknm: searchForm.banknm_h })
		const processedData = (res.data || []).map((r: any) => ({ bankcd: r.bankcd, banknm: r.banknm, remark: r.bigo, dspord: r.dspord, useyn: r.useyn }))
		mainGrid?.setData(processedData)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.bankcd) return vAlert('은행코드를 입력하십시오.')
	if (!masterForm.banknm) return vAlert('은행명을 입력하십시오.')
	if (!masterForm.dspord) return vAlert('출현순서를 입력하십시오.')
	try {
		await api.post('/api/haba/HABA_050U_STR', { ...masterForm, cmpycd: authStore.cmpycd, userid: authStore.userid })
		vAlert('저장되었습니다.')
		search(); initialize()
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => { resetForm(masterForm); masterForm.actkind = 'A0'; masterForm.useyn = 'Y' }
const excel = () => mainGrid?.download("xlsx", `금융기관관리_${new Date().toISOString().substring(0, 10)}.xlsx`)

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columns: [
				{ title: "은행코드", field: "bankcd", width: 100, hozAlign: "center" },
				{ title: "은행명", field: "banknm", minWidth: 200, cssClass: "fw-bold" },
				{ title: "비고", field: "remark", minWidth: 300 },
				{ title: "순서", field: "dspord", width: 80, hozAlign: "center" },
				{ title: "사용", field: "useyn", width: 80, hozAlign: "center", formatter: "tickCross" }
			],
			rowClick: (e, row) => { Object.assign(masterForm, row.getData()); masterForm.actkind = 'U0' }
		})
	}
})
</script>
