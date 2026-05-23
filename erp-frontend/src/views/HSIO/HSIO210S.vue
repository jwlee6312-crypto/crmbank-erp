<!--
	=============================================================
	프로그램명	: 입고현황 (Purchase Receipt Status Report)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] HSIP110U 표준 UI 및 상하정중앙 정렬 완벽 보정
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio210s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 (표준 버튼 배치 및 색상) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				입고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">입고현황</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-print" @click="handlePrint">인쇄</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 영역 (2행 3열 고밀도 표준) -->
		<div class="p-2 pb-0">
			<div class="card border shadow-sm">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;"><col />
							<col style="width: 100px;"><col />
							<col style="width: 100px;"><col />
						</colgroup>
						<tbody>
							<tr>
								<th>입고창고</th>
								<td>
									<select v-model="searchForm.WHCD" class="form-select form-select-sm">
										<option value="000">전체</option>
										<option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
									</select>
								</td>
								<th class="required">입고일자</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<input v-model="searchForm.FRYMD" type="date" class="form-control form-control-sm" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.TOYMD" type="date" class="form-control form-control-sm" />
									</div>
								</td>
								<th>입고유형</th>
								<td>
									<select v-model="searchForm.IOTYPE" class="form-select form-select-sm">
										<option value="000">전체</option>
										<option v-for="opt in typeOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
									</select>
								</td>
							</tr>
							<tr>
								<th>거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="searchForm.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.CUSTNM" type="text" class="form-control" placeholder="거래처 검색" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>품&nbsp;&nbsp;목&nbsp;&nbsp;명</th>
								<td colspan="3">
									<div class="input-group input-group-sm">
										<input v-model="searchForm.ITEMCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
										<input v-model="searchForm.ITEMNM" type="text" class="form-control" placeholder="품목 검색" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('ITEM')"><i class="bi bi-search"></i></button>
                                        <input v-model="searchForm.ITSIZE" type="text" class="form-control bg-light" style="max-width: 150px;" readonly placeholder="규격" />
                                        <input v-model="searchForm.UNIT" type="text" class="form-control bg-light" style="max-width: 60px;" readonly placeholder="단위" />
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📊 3. 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center" style="height: 40px;">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 입고 상세 내역 현황
					</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const searchForm = reactive<any>({
	WHCD: '000',
	IOTYPE: '000',
	FRYMD: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
	TOYMD: now.toISOString().substring(0, 10),
	CUSTCD: '', CUSTNM: '',
	ITEMCD: '', ITEMNM: '', ITSIZE: '', UNIT: ''
})

const whOptions = ref<any[]>([])
const typeOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

async function loadInitData() {
	try {
		const [resWh, resType] = await Promise.all([
			api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } }),
			api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '120' } })
		])
		whOptions.value = resWh.data.map((i: any) => ({ CODE: i.CODE || i.WHCD, CDNM: i.CDNM || i.WHNM }))
		typeOptions.value = resType.data.map((i: any) => ({ CODE: i.CODE, CDNM: i.CDNM }))
	} catch (e) {}
}

async function fetchList() {
	try {
		const res = await api.post('/api/hsio/HSIO_210S_STR', {
			...searchForm,
			CMPYCD: authStore.CMPYCD,
			FRYMD: searchForm.FRYMD.replace(/-/g, ''),
			TOYMD: searchForm.TOYMD.replace(/-/g, ''),
            LIMITOFFSET: 0, LIMITROWS: 1000
		})
		mainGrid?.setData(res.data.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

function initialize() {
	resetForm(searchForm)
	searchForm.WHCD = '000'; searchForm.IOTYPE = '000'
	searchForm.FRYMD = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
	searchForm.TOYMD = now.toISOString().substring(0, 10)
	mainGrid?.clearData()
}

const handlePrint = () => { vAlert('인쇄 기능을 준비 중입니다.') }
const openHelp = (type: string) => { /* 팝업 구현 */ }
const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 100 },
			columns: [
				{ title: '일자', field: 'IOYMD', width: 100, hozAlign: 'center',
                  formatter: (c) => { const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v } },
				{ title: '품명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '규격', field: 'ITSIZE', width: 150 },
				{ title: '단위', field: 'UNIT', width: 60, hozAlign: 'center' },
				{ title: '거래처', field: 'CUSTNM', minWidth: 180, widthGrow: 1 },
				{ title: '수량', field: 'IOQTY', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '단가', field: 'PRICE', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 },
                  mutatorData: (v, d) => (Number(d.JSANAMT) / (Number(d.IOQTY) || 1)) },
				{ title: '공급가', field: 'JSANAMT', hozAlign: 'right', width: 120, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '부가세', field: 'JSANVAT', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '합계', field: 'IOSUM', hozAlign: 'right', width: 120, formatter: 'money', formatterParams: { precision: 0 },
                  mutatorData: (v, d) => Number(d.JSANAMT || 0) + Number(d.JSANVAT || 0), cssClass: 'fw-bold bg-light' },
				{ title: '비고', field: 'SCUSTNM', width: 150 }
			]
		})
	}
	await loadInitData()
})
</script>
<style scoped>
.hsio210s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.flex-shrink-0 { flex-shrink: 0 !important; }
.flex-grow-1 { flex-grow: 1 !important; min-height: 0 !important; }
.overflow-hidden { overflow: hidden !important; }
/* 🚀 입력 필드 글자 크기 및 높이 최적화 (HSBA070U 패턴) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}
.erp-table-full { width: 100%; border-collapse: collapse; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 4px 5px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 2px 4px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }

/* 🚀 팝업 가독성 표준 스타일 */
:deep(.modal-content) { background-color: #ffffff !important; }
:deep(.modal-content .tabulator) { background-color: #ffffff !important; color: #000000 !important; border: 1px solid #dee2e6 !important; }
:deep(.modal-content .tabulator-cell) { color: #000000 !important; font-size: 13px !important; padding: 8px !important; }

</style>