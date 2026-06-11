<!--
	=============================================================
	프로그램명	: 감가상각계정과목 설정
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 고정자산 감가상각 처리를 위한 계정 과목 매핑 관리 (표준 디자인 및 팝업 이식)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-gear-wide-connected me-2 text-primary" style="font-size: 18px;"></i>
				고정자산 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				기초관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">감가상각계정과목 (HAFA900U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- 📝 설정 입력 영역 -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-primary"></i>감가상각 계정 설정 [{{ formData.actkind === 'A0' ? '신규' : '수정' }}]</div>
					<div class="form-check form-switch m-0">
						<input v-model="formData.useyn" class="form-check-input" type="checkbox" id="useynCheck900" true-value="N" false-value="Y">
						<label class="form-check-label text-danger fw-bold small" for="useynCheck900">삭제</label>
					</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light">자산계정</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.acctcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" readonly />
										<input v-model="formData.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ASSET')" placeholder="자산 계정 선택" />
										<button class="btn btn-outline-secondary" @click="openHelp('ASSET')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required bg-light">대변계정</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.cacctcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" readonly />
										<input v-model="formData.cacctnm" type="text" class="form-control" @keydown.enter="openHelp('CREDIT')" placeholder="충당금 또는 본계정 선택" />
										<button class="btn btn-outline-secondary" @click="openHelp('CREDIT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
							<tr>
								<th class="bg-light">판관비용 계정</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.sacctcd" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
										<input v-model="formData.sacctnm" type="text" class="form-control" @keydown.enter="openHelp('ADMIN')" placeholder="판매비와관리비 감가상각비 계정" />
										<button class="btn btn-outline-secondary" @click="openHelp('ADMIN')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="bg-light">제조비용 계정</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.macctcd" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
										<input v-model="formData.macctnm" type="text" class="form-control" @keydown.enter="openHelp('MANU')" placeholder="제조원가 감가상각비 계정" />
										<button class="btn btn-outline-secondary" @click="openHelp('MANU')"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 3. 그리드 영역 -->
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

const formData = reactive({ actkind: 'A0', acctcd: '', acctnm: '', cacctcd: '', cacctnm: '', sacctcd: '', sacctnm: '', macctcd: '', macctnm: '', useyn: 'Y' })
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

// 데이터 키를 소문자로 정규화
const normalizeKeys = (data: any): any => {
  if (!data) return data;
  if (Array.isArray(data)) return data.map(item => normalizeKeys(item));
  if (typeof data !== 'object' || data instanceof Date) return data;
  const normalized: any = {};
  Object.keys(data).forEach(key => { normalized[key.toLowerCase().trim()] = normalizeKeys(data[key]); });
  return normalized;
};

function initialize() {
	Object.assign(formData, { actkind: 'A0', acctcd: '', acctnm: '', cacctcd: '', cacctnm: '', sacctcd: '', sacctnm: '', macctcd: '', macctnm: '', useyn: 'Y' })
}

async function search() {
	try {
		const res = await api.post('/api/hafa/HAFA_900U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd })

		const data = normalizeKeys(res.data || []).map((row: any) => (
		{   acctcd: row.acctcd,
		    acctnm: row.acctnm,
		    cacctcd: row.cacctcd,
		    cacctnm: row.cacctnm,
		    macctcd: row.macctcd,
		    macctnm: row.macctnm,
		    sacctcd: row.sacctcd,
		    sacctnm: row.sacctnm,
		    useyn: row.useyn }))
		mainGrid?.setData(data); vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

async function save() {
	if (!formData.acctcd) return vAlertError('자산계정을 선택하세요.')
	if (!formData.cacctcd) return vAlertError('대변계정을 선택하세요.')
	try {
		await api.post('/api/hafa/HAFA_900U_STR',
		{ ...formData, cmpycd: authStore.cmpycd, userid: authStore.userid })
		vAlert('정상적으로 처리되었습니다.'); search(); initialize()
	} catch (e) { vAlertError('저장 중 오류 발생') }
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
    const common_data = { gubun: 'A8', cmpycd: authStore.cmpycd, gbncd: '020', code: '' };
	if (type === 'ASSET') {
		Object.assign(modalProps, { title: '자산계정 선택', path: '/api/ha00/HA00_00P_STR', data: common_data, columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 200 }],
			onConfirm: (rawData: any) => {
                const d = normalizeKeys(rawData);
                formData.acctcd = d.acctcd || d.code;
                formData.acctnm = d.acctnm || d.cdnm || d.name
            }
		})
	} else {
		let title = ''; let fieldPrefix = '';
		if (type === 'CREDIT') { title = '대변계정 선택'; fieldPrefix = 'C'; }
		else if (type === 'ADMIN') { title = '판관비 감가상각계정 선택'; fieldPrefix = 'S'; }
		else if (type === 'MANU') { title = '제조비용 감가상각계정 선택'; fieldPrefix = 'M'; }
		Object.assign(modalProps, { title: title, path: '/api/ha00/HA00_00P_STR', data: common_data, columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 200 }],
			onConfirm: (rawData: any) => {
                const d = normalizeKeys(rawData);
                (formData as any)[fieldPrefix + 'acctcd'] = d.acctcd || d.code;
                (formData as any)[fieldPrefix + 'acctnm'] = d.acctnm || d.cdnm || d.name
            }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "자산계정", field: "acctnm", widthGrow: 1, cssClass: "fw-bold" },
				{ title: "충당금/본계정", field: "cacctnm", widthGrow: 1, cssClass: "text-primary fw-bold" },
				{ title: "판관비계정", field: "sacctnm", widthGrow: 1 },
				{ title: "제조비용계정", field: "macctnm", widthGrow: 1 },
				{ title: "삭제", field: "useyn", widthGrow: 1, hozAlign: "center", formatter: (c: any) => c.getValue() === 'N' ? 'V' : '' }
			]
		})
		mainGrid.on("rowClick", (e, row) => {
            const d = normalizeKeys(row.getData());
            Object.assign(formData, d);
            formData.actkind = 'U0'
        })
	}
	search()
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
