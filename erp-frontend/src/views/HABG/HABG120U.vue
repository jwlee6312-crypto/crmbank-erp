<!--
	=============================================================
	프로그램명	: 추가/조정 배정
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 예산 추가/조정 신청 내역에 대해 실제 배정 처리 및 확정
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-check-fill me-2 text-primary" style="font-size: 18px;"></i>
				예산관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">추가/조정 배정 (HABG120U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-check-lg"></i> 배정저장
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>예산부서</span>
							<div class="input-group input-group-sm" style="width: 220px;">
								<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
								<input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" @input="searchForm.deptcd = ''" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT_S')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>신청연월</span>
							<div class="d-flex gap-1">
								<select v-model="searchForm.bugtyy" class="form-select form-select-sm" style="width: 100px;">
									<option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
								</select>
								<select v-model="searchForm.bugtmm" class="form-select form-select-sm" style="width: 80px;">
									<option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ String(m).padStart(2, '0') }}월</option>
								</select>
							</div>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>검색대상</span>
							<select v-model="searchForm.rstyn" class="form-select form-select-sm" style="width: 100px;">
								<option value="N">미배정</option>
								<option value="Y">배정</option>
							</select>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📝 배정 정보 입력 영역 -->
		<div class="p-2 flex-shrink-0">
			<div class="card border shadow-sm">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;" />
							<col style="width: 20%;" />
							<col style="width: 100px;" />
							<col style="width: 20%;" />
							<col style="width: 100px;" />
							<col style="width: auto;" />
						</colgroup>
						<tbody>
							<tr>
								<th>구분</th>
								<td>
									<div class="d-flex gap-1">
										<input v-model="inputForm.dockind" type="text" class="form-control form-control-sm text-center bg-light" style="width: 50px;" readonly />
										<input v-model="inputForm.dockindNM" type="text" class="form-control form-control-sm bg-light" readonly />
									</div>
								</td>
								<th>예산부서</th>
								<td>
									<div class="d-flex gap-1 align-items-center">
										<span class="badge bg-secondary px-2 py-1">{{ inputForm.deptcd }}</span>
										<span class="fw-bold">{{ inputForm.deptnm }}</span>
									</div>
								</td>
								<th>신청연월</th>
								<td>
									<div class="d-flex gap-1 align-items-center fw-bold">
										{{ inputForm.bugtyy }}년 {{ inputForm.bugtmm }}월
									</div>
								</td>
							</tr>
							<tr>
								<th>신청액</th>
								<td>
									<input :value="formatMoney(inputForm.reqamt)" type="text" class="form-control form-control-sm text-end bg-light" readonly />
								</td>
								<th>예산잔액</th>
								<td>
									<input :value="formatMoney(inputForm.janamt)" type="text" class="form-control form-control-sm text-end bg-light fw-bold text-danger" readonly />
								</td>
								<th class="bg-yellow">배정액</th>
								<td>
									<input v-model="inputForm.bugtamt" type="number" class="form-control form-control-sm text-end fw-bold text-primary border-primary" />
								</td>
							</tr>
							<tr>
								<th class="bg-yellow">배정일</th>
								<td>
									<input v-model="inputForm.bugtymd" type="date" class="form-control form-control-sm" />
								</td>
								<th>예산코드</th>
								<td>
									<div class="small">
										<div class="text-muted">FROM: <span class="text-dark fw-bold">{{ inputForm.bugtnm_fr }}</span></div>
										<div v-if="inputForm.bugtnm_to" class="text-muted">TO: <span class="text-dark fw-bold">{{ inputForm.bugtnm_to }}</span></div>
									</div>
								</td>
								<th>조정연월</th>
								<td>
									<div class="small">
										<div class="text-muted">FROM: <span class="text-dark fw-bold">{{ formatym(inputForm.bugtym_fr) }}</span></div>
										<div v-if="inputForm.bugtym_to" class="text-muted">TO: <span class="text-dark fw-bold">{{ formatym(inputForm.bugtym_to) }}</span></div>
									</div>
								</td>
							</tr>
							<tr>
								<th>적요</th>
								<td colspan="3">
									<input v-model="inputForm.bigo" type="text" class="form-control form-control-sm bg-light" readonly />
								</td>
								<th>배정여부</th>
								<td>
									<div class="form-check mb-0">
										<input v-model="inputForm.rstyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="chkAssign">
										<label class="form-check-label fw-bold text-primary" for="chkAssign">배정확정</label>
									</div>
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
					<span class="small fw-bold text-dark"><i class="bi bi-list-check me-1"></i>신청 및 배정 내역</span>
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

const currentYear = new Date().getFullYear()
const currentMonth = String(new Date().getMonth() + 1).padStart(2, '0')
const today = new Date().toISOString().substring(0, 10)
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))

const searchForm = reactive({
	bugtyy: String(currentYear),
	bugtmm: currentMonth,
	deptcd: authStore.deptcd || '',
	deptnm: authStore.deptnm || '',
	rstyn: 'N'
})

const inputForm = reactive({
	actkind: 'U0',
	bugtno: '',
	dockind: '',
	dockindNM: '',
	deptcd: '',
	deptnm: '',
	bugtyy: '',
	bugtmm: '',
	reqamt: 0,
	janamt: 0,
	bugtamt: 0,
	bugtymd: today,
	bugtcd_fr: '',
	bugtnm_fr: '',
	bugtcd_to: '',
	bugtnm_to: '',
	bugtym_fr: '',
	bugtym_to: '',
	bigo: '',
	rstyn: 'N'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
	if (!searchForm.deptcd) return vAlertError('예산부서를 선택하세요.')
	try {
		const res = await api.post('/api/habg/HABG_120U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			BUGTym: `${searchForm.bugtyy}${searchForm.bugtmm}`,
			deptcd: searchForm.deptcd,
			rstyn: searchForm.rstyn
		})
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const save = async () => {
	if (!inputForm.bugtno) return vAlertError('배정할 내역을 목록에서 선택해 주십시오.')
	if (Number(inputForm.bugtamt) <= 0 && inputForm.rstyn === 'Y') return vAlertError('배정액을 입력해 주십시오.')

	try {
		await api.post('/api/habg/HABG_120U_STR', {
			...inputForm,
			cmpycd: authStore.cmpycd,
			BUGTym: `${inputForm.bugtyy}${inputForm.bugtmm}`,
			bugtymd: inputForm.bugtymd.replace(/-/g, '')
		})
		vAlert('배정 처리가 완료되었습니다.')
		search()
		initialize()
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
	Object.assign(inputForm, {
		actkind: 'U0', bugtno: '', dockind: '', dockindNM: '', deptcd: '', deptnm: '',
		bugtyy: '', bugtmm: '', reqamt: 0, janamt: 0, bugtamt: 0, bugtymd: today,
		bugtcd_fr: '', bugtnm_fr: '', bugtcd_to: '', bugtnm_to: '',
		bugtym_fr: '', bugtym_to: '', bigo: '', rstyn: 'N'
	})
}

// 유틸리티
const formatMoney = (val: any) => Number(val || 0).toLocaleString()
const formatym = (val: string) => (val && val.length === 6) ? `${val.substring(0, 4)}.${val.substring(4, 6)}` : val

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT_S') {
        Object.assign(modalProps, {
            title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
            data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: searchForm.deptnm },
            columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
            onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; search() }
        })
		modalVisible.value = true
	}
}

onMounted(() => {
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
				{ title: "조정구분", field: "col14", width: 80 },
				{ title: "신청액", field: "col7", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{
					title: "FROM",
					columns: [
						{ title: "예산명", field: "col5", width: 150, hozAlign: "left" },
						{
							title: "연월", field: "col6", width: 70,
							formatter: (cell) => { const v = cell.getValue(); return v ? `${v.substring(2, 4)}.${v.substring(4, 6)}` : '' }
						}
					]
				},
				{
					title: "TO",
					columns: [
						{ title: "예산명", field: "COL9", width: 150, hozAlign: "left" },
						{
							title: "연월", field: "col10", width: 70,
							formatter: (cell) => { const v = cell.getValue(); return v ? `${v.substring(2, 4)}.${v.substring(4, 6)}` : '' }
						}
					]
				},
				{ title: "적요", field: "col13", minWidth: 200, hozAlign: "left" },
				{ title: "배정", field: "col12", width: 50, formatter: "tickCross", formatterParams: { tickElement: "<i class='bi bi-check-lg text-primary fw-bold'></i>", crossElement: "" } }
			]
		})

		mainGrid.on("rowClick", (e, row) => {
			const d = row.getData();
			Object.assign(inputForm, {
				actkind: 'U0',
				bugtno: d.col0,
				deptcd: d.col1,
				deptnm: d.col2,
				dockind: d.col3,
				dockindNM: d.col14,
				bugtcd_fr: d.col4,
				bugtnm_fr: d.col5,
				bugtym_fr: d.col6,
				reqamt: Number(d.col7),
				bugtcd_to: d.col8,
				bugtnm_to: d.COL9,
				bugtym_to: d.col10,
				rstyn: d.col12,
				bigo: d.col13,
				bugtamt: d.col12 === 'Y' ? Number(d.col15) : Number(d.col7),
				bugtymd: d.col12 === 'Y' ? (d.col16 ? `${d.col16.substring(0, 4)}-${d.col16.substring(4, 6)}-${d.col16.substring(6, 8)}` : today) : today,
				janamt: Number(d.col17),
				bugtyy: d.col18.substring(0, 4),
				bugtmm: d.col18.substring(4, 6)
			})
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.erp-table-full th { background-color: #f8f9fa; text-align: center; padding: 5px; font-size: 12px; border: 1px solid #dee2e6; }
.erp-table-full td { padding: 4px; border: 1px solid #dee2e6; background-color: #fff; }
.bg-yellow { background-color: #fff9db !important; }

:deep(.tabulator-header .tabulator-col) { background-color: #f1f5f9 !important; border-right: 1px solid #dee2e6 !important; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; }
</style>
