<!--
	=============================================================
	프로그램명	: 입고현황 (Purchase Receipt Status)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [디자인정상화] 라벨 겹침 및 글자 일그러짐 해결, 고밀도 표준 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio200s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-clipboard-data me-2 text-primary" style="font-size: 18px;"></i>
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

		<!-- 🔍 2. 검색 조건 영역 (라벨 폭 고정으로 일그러짐 방지) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;"><col style="width: 23%">
							<col style="width: 100px;"><col style="width: 23%">
							<col style="width: 100px;"><col style="width: 24%">
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
								<th>입고일자</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<input v-model="searchForm.FRYMD" type="date" class="form-control form-control-sm" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.TOYMD" type="date" class="form-control form-control-sm" />
									</div>
								</td>
								<th>거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="searchForm.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.CUSTNM" type="text" class="form-control" placeholder="거래처 검색" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
							<tr>
								<th>입고유형</th>
								<td>
									<select v-model="searchForm.IOTYPE" class="form-select form-select-sm">
										<option value="000">전체</option>
										<option v-for="opt in typeOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
									</select>
								</td>
								<th>주문번호</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<input v-model="searchForm.ORDYM" type="month" class="form-control form-control-sm" style="width: 130px;" />
										<input v-model="searchForm.ORDNO" type="text" class="form-control form-control-sm" style="width: 60px;" placeholder="0000" />
									</div>
								</td>
								<th>담&nbsp;&nbsp;당&nbsp;&nbsp;자</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="searchForm.SCH_USERID" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.SCH_USERNM" type="text" class="form-control" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('USER')"><i class="bi bi-search"></i></button>
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
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 입고 현황 상세 목록
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
import { ref, reactive, onMounted } from 'vue'
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
	ORDYM: '', ORDNO: '',
	SCH_USERID: '', SCH_USERNM: ''
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
		const res = await api.post('/api/hsio/HSIO_200S_STR', {
			...searchForm,
			CMPYCD: authStore.CMPYCD,
			FRYMD: searchForm.FRYMD.replace(/-/g, ''),
			TOYMD: searchForm.TOYMD.replace(/-/g, ''),
			ORDYM: searchForm.ORDYM.replace(/-/g, '')
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
const openHelp = (type: string) => { /* 팝업 로직 */ }
const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 100 },
			columns: [
				{ title: '입고번호', field: 'IONO_FULL', width: 130, hozAlign: 'center', cssClass: 'fw-bold text-primary',
                  mutatorData: (v, d) => `${d.IOYM || ''}-${d.IONO || ''}` },
				{ title: '거래처', field: 'CUSTNM', minWidth: 180, widthGrow: 1 },
				{ title: '입고유형', field: 'IOTYPENM', width: 100, hozAlign: 'center' },
				{ title: '입고일자', field: 'IOYMD', width: 100, hozAlign: 'center',
                  formatter: (c) => { const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v } },
				{ title: '품명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '규격', field: 'ITSIZE', width: 150 },
				{ title: '단위', field: 'UNIT', width: 60, hozAlign: 'center' },
				{ title: '수량', field: 'QTY', hozAlign: 'right', width: 80, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '단가', field: 'PRICE', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '공급가', field: 'SPYAMT', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '부가세', field: 'VATAMT', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '합계', field: 'TOTAMT', hozAlign: 'right', width: 120, formatter: 'money', formatterParams: { precision: 0 },
                  mutatorData: (v, d) => Number(d.SPYAMT || 0) + Number(d.VATAMT || 0), cssClass: 'fw-bold bg-light' }
			]
		})
	}
	await loadInitData()
})
</script>

<style scoped>
.hsio200s-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }

.erp-header { background-color: #ffffff !important; }

/* 💎 개별 파일의 스타일을 삭제하여 global.css 표준 디자인이 적용되도록 함 */
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }

erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  width: 1%; white-space: nowrap;
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12.5px; padding: 10px 15px !important; color: #495057;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 10px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
.bg-yellow { background-color: #fffde7 !important; }
</style>
