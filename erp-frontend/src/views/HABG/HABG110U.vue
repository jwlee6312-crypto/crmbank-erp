<!--
	=============================================================
	프로그램명	: 추가/조정신청
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 예산 추가, 조기, 이월, 전용 신청 및 내역 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 (표준 규격) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-pencil-square me-2 text-primary" style="font-size: 18px;"></i>
				예산관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">추가/조정신청 (HABG110U)</span>
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
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3">
						<div class="d-flex align-items-center">
							<span class="erp-label">예산부서</span>
							<div class="input-group" style="width: 250px;">
								<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 65px;" readonly />
								<input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" @input="searchForm.deptcd = ''" />
								<button class="btn" @click="openHelp('DEPT_S')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label">신청연월</span>
							<div class="d-flex gap-1">
								<select v-model="searchForm.bugtyy" class="form-select" style="width: 100px;">
									<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
								</select>
								<select v-model="searchForm.bugtmm" class="form-select" style="width: 80px;">
									<option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ String(m).padStart(2, '0') }}월</option>
								</select>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📝 입력 폼 영역 (표준 erp-table-full 적용) -->
		<div class="p-2 flex-shrink-0">
			<div class="card border shadow-sm">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;" />
							<col style="width: 30%;" />
							<col style="width: 100px;" />
							<col style="width: auto;" />
							<col style="width: 100px;" />
							<col style="width: 150px;" />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">조정구분</th>
								<td>
									<select v-model="inputForm.dockind" class="form-select">
										<option value="000">선택하세요</option>
										<option v-for="opt in dockindOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
									</select>
								</td>
								<th>예산부서</th>
								<td>
									<div class="d-flex gap-2 align-items-center px-2">
										<span class="badge bg-secondary">{{ inputForm.deptcd }}</span>
										<span class="fw-bold small">{{ inputForm.deptnm }}</span>
									</div>
								</td>
								<th>삭제여부</th>
								<td>
									<div class="form-check form-switch ms-2">
										<input v-model="inputForm.useyn" class="form-check-input" type="checkbox" true-value="N" false-value="Y" id="chkDelete">
										<label class="form-check-label text-danger fw-bold small" for="chkDelete">삭제</label>
									</div>
								</td>
							</tr>
							<tr>
								<th class="required">예산코드</th>
								<td colspan="3">
									<div class="d-flex align-items-center gap-2 px-1">
										<div class="input-group" style="width: 250px;">
											<input v-model="inputForm.bugtcd_fr" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
											<input v-model="inputForm.bugtnm_fr" type="text" class="form-control" placeholder="FROM 예산코드" />
											<button class="btn" @click="openHelp('BUGT_FR')"><i class="bi bi-search"></i></button>
										</div>
										<template v-if="inputForm.dockind === '050'">
											<i class="bi bi-arrow-right text-primary"></i>
											<div class="input-group" style="width: 250px;">
												<input v-model="inputForm.bugtcd_to" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
												<input v-model="inputForm.bugtnm_to" type="text" class="form-control" placeholder="TO 예산코드" />
												<button class="btn" @click="openHelp('BUGT_TO')"><i class="bi bi-search"></i></button>
											</div>
										</template>
									</div>
								</td>
								<th class="required">신청액</th>
								<td>
									<input v-model="inputForm.reqamt" type="number" class="form-control text-end fw-bold text-primary" placeholder="0" />
								</td>
							</tr>
							<tr>
								<th>조정연월</th>
								<td colspan="3">
									<div class="d-flex align-items-center gap-2 px-1">
										<div class="d-flex gap-1">
											<select v-model="inputForm.bugtyy_fr" class="form-select" :disabled="!isFromPeriodEnabled" style="width: 100px;">
												<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
											</select>
											<select v-model="inputForm.bugtmm_fr" class="form-select" :disabled="!isFromPeriodEnabled" style="width: 90px;">
												<option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
											</select>
										</div>
										<template v-if="inputForm.dockind === '040'">
											<i class="bi bi-arrow-right text-primary"></i>
											<div class="d-flex gap-1">
												<select v-model="inputForm.bugtyy_to" class="form-select" style="width: 100px;">
													<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
												</select>
												<select v-model="inputForm.bugtmm_to" class="form-select" style="width: 90px;">
													<option v-for="opt in periodOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
												</select>
											</div>
										</template>
									</div>
								</td>
								<th>신청연월</th>
								<td>
									<div class="d-flex gap-1 align-items-center px-2">
										<span class="fw-bold small text-primary">{{ inputForm.bugtyy }}-{{ inputForm.bugtmm }}</span>
									</div>
								</td>
							</tr>
							<tr>
								<th>적요</th>
								<td colspan="5">
									<input v-model="inputForm.bigo" type="text" class="form-control" placeholder="신청 사유 및 상세 내역을 입력하십시오." />
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📊 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-3 bg-white border-bottom d-flex justify-content-between align-items-center">
					<span class="small fw-bold text-dark"><i class="bi bi-list-ul me-1 text-primary"></i>신청 내역 리스트</span>
					<span v-if="inputForm.rstyn === 'Y'" class="badge bg-danger" style="font-size: 10px;">배정완료 (수정불가)</span>
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = String(new Date().getMonth() + 1).padStart(2, '0')
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear + 1 - i))

const searchForm = reactive({
	bugtyy: String(currentYear),
	bugtmm: currentMonth,
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || ''
})

const inputForm = reactive({
	actkind: 'A0',
	bugtno: '',
	dockind: '000',
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || '',
	bugtyy: String(currentYear),
	bugtmm: currentMonth,
	reqamt: 0,
	bugtcd_fr: '',
	bugtnm_fr: '',
	bugtcd_to: '',
	bugtnm_to: '',
	bugtyy_fr: String(currentYear),
	bugtmm_fr: currentMonth,
	bugtyy_to: String(currentYear),
	bugtmm_to: currentMonth,
	bigo: '',
	useyn: 'Y',
	rstyn: 'N'
})

const bgType = ref('010')
const dockindOptions = ref<any[]>([])

const isFromPeriodEnabled = computed(() => ['030'].includes(inputForm.dockind))

const periodOptions = computed(() => {
	if (bgType.value === '020') {
		return [{ value: '01', text: '1분기' }, { value: '04', text: '2분기' }, { value: '07', text: '3분기' }, { value: '10', text: '4분기' }]
	} else if (bgType.value === '030') {
		return [{ value: '01', text: '상반기' }, { value: '07', text: '하반기' }]
	} else if (bgType.value === '040') {
		return [{ value: '01', text: '전체' }]
	}
	return Array.from({ length: 12 }, (_, i) => ({ value: String(i + 1).padStart(2, '0'), text: `${i + 1}월` }))
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.deptcd) return vAlertError('예산부서를 선택하세요.')
	try {
		const res = await api.post('/api/habg/HABG_110U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			BUGTym: `${searchForm.bugtyy}${searchForm.bugtmm}`,
			deptcd: searchForm.deptcd
		})
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const save = async () => {
	if (inputForm.rstyn === 'Y') return vAlertError('배정된 자료입니다. 수정할 수 없습니다.')
	if (inputForm.dockind === '000') return vAlertError('구분을 선택해 주십시오.')
	if (!inputForm.bugtcd_fr) return vAlertError('예산코드를 선택해 주십시오.')
	if (Number(inputForm.reqamt) <= 0) return vAlertError('신청액을 입력해 주십시오.')

	try {
		await api.post('/api/habg/HABG_110U_STR', {
			...inputForm,
			cmpycd: authStore.cmpycd,
			BUGTym: `${inputForm.bugtyy}${inputForm.bugtmm}`,
			bugtym_fr: `${inputForm.bugtyy_fr}${inputForm.bugtmm_fr}`,
			bugtym_to: `${inputForm.bugtyy_to}${inputForm.bugtmm_to}`
		})
		vAlert('저장되었습니다.')
		search()
		initialize()
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
	Object.assign(inputForm, {
		actkind: 'A0', bugtno: '', dockind: '000', reqamt: 0,
		bugtcd_fr: '', bugtnm_fr: '', bugtcd_to: '', bugtnm_to: '',
		bigo: '', useyn: 'Y', rstyn: 'N',
		bugtyy: searchForm.bugtyy, bugtmm: searchForm.bugtmm,
		bugtyy_fr: searchForm.bugtyy, bugtmm_fr: searchForm.bugtmm,
		bugtyy_to: searchForm.bugtyy, bugtmm_to: searchForm.bugtmm
	})
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type.startsWith('DEPT')) {
        Object.assign(modalProps, {
            title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
            data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: searchForm.deptnm },
            columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
            onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search() }
        })
	} else {
		Object.assign(modalProps, {
			title: '예산코드 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'bugtnm',
			data: { gubun: 'B1', cmpycd: authStore.cmpycd, search: type === 'BUGT_FR' ? inputForm.bugtnm_fr : inputForm.bugtnm_to },
			columns: [{ title: '코드', field: 'bugtcd', width: 80 }, { title: '예산명', field: 'bugtnm', width: 180 }],
			onConfirm: (d: any) => {
				if (type === 'BUGT_FR') { inputForm.bugtcd_fr = d.bugtcd; inputForm.bugtnm_fr = d.bugtnm }
				else { inputForm.bugtcd_to = d.bugtcd; inputForm.bugtnm_to = d.bugtnm }
			}
		})
	}
	modalVisible.value = true
}

const loadCodes = async () => {
	try {
		const [resType, resDockind] = await Promise.all([
			api.post('/api/habg/GET_BGTYPE', { cmpycd: authStore.cmpycd }),
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '210' })
		])
		bgType.value = resType.data || '010'
		dockindOptions.value = resDockind.data || []
	} catch (e) {}
}

onMounted(() => {
	loadCodes()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle", hozAlign: "center" },
			columns: [
				{ title: "번호", field: "col0", width: 50 },
				{
					title: "신청일", field: "col11", width: 90,
					formatter: (cell) => {
						const v = cell.getValue();
						return v ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : '';
					}
				},
				{ title: "조정구분", field: "col15", width: 80 },
				{ title: "신청액", field: "col7", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{
					title: "FROM",
					columns: [
						{ title: "예산명", field: "col5", width: 150, hozAlign: "left" },
						{
							title: "연월", field: "col6", width: 80,
							formatter: (cell) => { const v = cell.getValue(); return v ? `${v.substring(0, 4)}.${v.substring(4, 6)}` : '' }
						}
					]
				},
				{
					title: "TO",
					columns: [
						{ title: "예산명", field: "COL9", width: 150, hozAlign: "left" },
						{
							title: "연월", field: "col10", width: 80,
							formatter: (cell) => { const v = cell.getValue(); return v ? `${v.substring(0, 4)}.${v.substring(4, 6)}` : '' }
						}
					]
				},
				{ title: "적요", field: "col13", minWidth: 200, hozAlign: "left" },
				{ title: "삭제", field: "col14", width: 50, formatter: (cell) => cell.getValue() === 'N' ? 'V' : '' }
			]
		})

		mainGrid.on("rowClick", (e, row) => {
			const d = row.getData();
			Object.assign(inputForm, {
				actkind: 'U0', bugtno: d.col0, dockind: d.col3,
				deptcd: d.col1, deptnm: d.col2, reqamt: Number(d.col7),
				bugtcd_fr: d.col4, bugtnm_fr: d.col5, bugtcd_to: d.col8, bugtnm_to: d.COL9,
				bugtyy_fr: d.col6.substring(0, 4), bugtmm_fr: d.col6.substring(4, 6),
				bugtyy_to: d.col10.substring(0, 4), bugtmm_to: d.col10.substring(4, 6),
				rstyn: d.col12, bigo: d.col13, useyn: d.col14
			})
		})
	}
})

watch(() => [searchForm.bugtyy, searchForm.bugtmm], ([y, m]) => {
	inputForm.bugtyy = y; inputForm.bugtmm = m;
	inputForm.bugtyy_fr = y; inputForm.bugtmm_fr = m;
	inputForm.bugtyy_to = y; inputForm.bugtmm_to = m;
})
</script>
