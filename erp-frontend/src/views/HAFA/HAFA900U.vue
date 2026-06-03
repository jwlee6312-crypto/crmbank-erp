<!--	=============================================================
	프로그램명	: 감가상각계정과목 설정
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 고정자산 감가상각 처리를 위한 계정 과목(자산, 충당금, 비용) 매핑 관리 (디자인 표준화 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 (표준 규격 및 함수명 통일) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-gear-wide-connected me-2 text-primary" style="font-size: 18px;"></i>
				고정자산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">감가상각계정과목 (HAFA900U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 📝 2. 설정 입력 영역 (표준 erp-table-full 및 고밀도 레이아웃 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-primary"></i>감가상각 계정 설정</div>
					<div class="form-check form-switch m-0">
						<input v-model="formData.useyn" class="form-check-input" type="checkbox" id="useynCheck900" true-value="N" false-value="Y">
						<label class="form-check-label text-danger fw-bold small" for="useynCheck900">데이터 삭제</label>
					</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">자산계정</th>
								<td>
									<div class="input-group">
										<input v-model="formData.acctcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" readonly />
										<input v-model="formData.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ASSET')" placeholder="자산 계정 선택" />
										<button class="btn" @click="openHelp('ASSET')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required">대변계정</th>
								<td>
									<div class="input-group">
										<input v-model="formData.Cacctcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" readonly />
										<input v-model="formData.Cacctnm" type="text" class="form-control" @keydown.enter="openHelp('CREDIT')" placeholder="충당금 또는 본계정 선택" />
										<button class="btn" @click="openHelp('CREDIT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
							<tr>
								<th>판관비용 계정</th>
								<td>
									<div class="input-group">
										<input v-model="formData.Sacctcd" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
										<input v-model="formData.Sacctnm" type="text" class="form-control" @keydown.enter="openHelp('ADMIN')" placeholder="판매비와관리비 감가상각비 계정" />
										<button class="btn" @click="openHelp('ADMIN')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>제조비용 계정</th>
								<td>
									<div class="input-group">
										<input v-model="formData.Macctcd" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
										<input v-model="formData.Macctnm" type="text" class="form-control" @keydown.enter="openHelp('MANU')" placeholder="제조원가 감가상각비 계정" />
										<button class="btn" @click="openHelp('MANU')"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📊 3. 그리드 영역 (표준 통합CSS 적용) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
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
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const formData = reactive({ actkind: 'A0', acctcd: '', acctnm: '', Cacctcd: '', Cacctnm: '', Sacctcd: '', Sacctnm: '', Macctcd: '', Macctnm: '', useyn: 'Y' })
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

function initialize() {
	Object.assign(formData, { actkind: 'A0', acctcd: '', acctnm: '', Cacctcd: '', Cacctnm: '', Sacctcd: '', Sacctnm: '', Macctcd: '', Macctnm: '', useyn: 'Y' })
}

async function search() {
	try {
		const res = await api.post('/api/hafa/HAFA_900U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd })
		const data = (res.data || []).map((row: any) => ({ acctcd: row.col0, acctnm: row.col1, Cacctcd: row.col2, Cacctnm: row.col3, Macctcd: row.col4, Macctnm: row.col5, Sacctcd: row.col6, Sacctnm: row.col7, useyn: row.col8 }))
		mainGrid?.setData(data); vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

async function save() {
	if (!formData.acctcd) return vAlertError('자산계정을 선택하세요.')
	if (!formData.Cacctcd) return vAlertError('대변계정을 선택하세요.')
	try {
		await api.post('/api/hafa/HAFA_900U_STR', { ...formData, cmpycd: authStore.cmpycd, userid: authStore.userid })
		vAlert('정상적으로 처리되었습니다.'); search(); initialize()
	} catch (e) { vAlertError('저장 중 오류 발생') }
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
	if (type === 'ASSET') {
		Object.assign(modalProps, { title: '자산계정 선택', path: '/api/ha00/HELP_acctcd_LTD_ETC_STR', data: { acctgbn: '020', search: formData.acctnm }, columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 200 }],
			onConfirm: (d: any) => { formData.acctcd = d.acctcd; formData.acctnm = d.acctnm }
		})
	} else {
		let title = ''; let fieldPrefix = '';
		if (type === 'CREDIT') { title = '대변계정 선택'; fieldPrefix = 'C'; }
		else if (type === 'ADMIN') { title = '판관비 감가상각계정 선택'; fieldPrefix = 'S'; }
		else if (type === 'MANU') { title = '제조비용 감가상각계정 선택'; fieldPrefix = 'M'; }
		Object.assign(modalProps, { title: title, path: '/api/ha00/HELP_acctcd_LTD_STR', data: { search: (formData as any)[fieldPrefix + 'acctnm'] }, columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 200 }],
			onConfirm: (d: any) => { (formData as any)[fieldPrefix + 'acctcd'] = d.acctcd; (formData as any)[fieldPrefix + 'acctnm'] = d.acctnm }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "자산계정", field: "acctnm", width: 150, cssClass: "fw-bold" },
				{ title: "충당금/본계정", field: "Cacctnm", width: 150, cssClass: "text-primary fw-bold" },
				{ title: "판관비계정", field: "Sacctnm", width: 180 },
				{ title: "제조비용계정", field: "Macctnm", width: 180 },
				{ title: "삭제", field: "useyn", width: 60, hozAlign: "center", formatter: (c) => c.getValue() === 'N' ? 'V' : '' }
			]
		})
		mainGrid.on("rowClick", (e, row) => { Object.assign(formData, row.getData()); formData.actkind = 'U0' })
	}
	search()
})
</script>
