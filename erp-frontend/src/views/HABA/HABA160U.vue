<!--
	=============================================================
	프로그램명	: 지급어음기초자료
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 지급어음의 기초 정보를 등록하고 관리하는 화면
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-ruled me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">지급어음기초자료 (HABA160U)</span>
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
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
				<button v-if="masterForm.actkind === 'U1'" class="btn-erp btn-delete" @click="deleteData">
					<i class="bi bi-trash"></i> 삭제
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>어음번호</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.billno" type="text" class="form-control form-control-sm" style="width: 150px;" maxlength="14" @keydown.enter="search" />
								<span>~</span>
								<input v-model="searchForm.billno_TO" type="text" class="form-control form-control-sm" style="width: 150px;" maxlength="14" @keydown.enter="search" />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📝 입력 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> 어음 상세 정보 [{{ masterForm.actkind === 'I1' ? '신규' : '수정' }}]</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">어음번호</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.billno" type="text" class="form-control form-control-sm" maxlength="14" :readonly="masterForm.actkind === 'U1'" />
							</td>
							<th class="text-center bg-light-subtle border-end">발행은행</th>
							<td class="bg-white border-end px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.bankcd" type="text" class="form-control text-center bg-light" style="max-width: 65px;" readonly />
									<input v-model="masterForm.banknm" type="text" class="form-control" @keydown.enter="openHelp('BANK')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('BANK')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end">발행인</th>
							<td class="bg-white px-2 py-1">
								<input v-model="masterForm.issuman" type="text" class="form-control form-control-sm" maxlength="20" />
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end">발행일자</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.stdymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end">만기일자</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.endymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end">금 액</th>
							<td class="bg-white px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.billamt" type="number" class="form-control form-control-sm text-end" step="0" />
									<span>(원)</span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end">어음유형</th>
							<td class="bg-white border-end px-2 py-1">
								<select v-model="masterForm.billtype" class="form-select form-select-sm">
									<option v-for="opt in billTypeOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
								</select>
							</td>
							<th class="text-center bg-light-subtle border-end">지급거래처</th>
							<td class="bg-white border-end px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.custcd" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
									<input v-model="masterForm.custnm" type="text" class="form-control" @keydown.enter="openHelp('CUST')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end">사용여부</th>
							<td class="bg-white px-2 py-1">
								<div class="form-check form-check-inline mb-0">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useYnCheck" true-value="Y" false-value="N" />
									<label class="form-check-label" for="useYnCheck">사용</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
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

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import * as XLSX from 'xlsx'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 🔍 검색 조건
const searchForm = reactive({
	billno: '',
	billno_TO: ''
})

// 📝 마스터 데이터
const masterForm = reactive({
	actkind: 'I1',
	billno: '',
	bankcd: '',
	banknm: '',
	issuman: '',
	stdymd: '',
	endymd: '',
	billamt: 0,
	billtype: '',
	custcd: '',
	custnm: '',
	useyn: 'Y'
})

const billTypeOptions = ref<{ value: string; text: string }[]>([])

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const fetchOptions = async () => {
	try {
		// 어음유형 (170)
		const resType = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: ' ', search: '170' })
		billTypeOptions.value = resType.data?.map((i: any) => ({ value: i.col0, text: i.col1 })) || []
		if (billTypeOptions.value.length > 0) masterForm.billtype = billTypeOptions.value[0].value
	} catch (e) { console.error('Options Load Failed', e) }
}

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_160U_STR', {
			actkind: 'S1',
			cmpycd: authStore.cmpycd,
			BILLGU: '100',
			billno: searchForm.billno,
			billno_TO: searchForm.billno_TO
		})
		mainGrid?.setData(res.data || [])
		if (res.data?.length > 0) vAlert('조회되었습니다.')
		else vAlert('데이터가 존재하지 않습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.billno) return vAlert('어음번호를 입력하세요.')
	if (!masterForm.banknm) return vAlert('발행은행을 입력하세요.')
	if (!masterForm.issuman) return vAlert('발행인을 입력하세요.')
	if (!masterForm.stdymd) return vAlert('발행일자를 입력하세요.')
	if (!masterForm.endymd) return vAlert('만기일자를 입력하세요.')
	if (!masterForm.billamt) return vAlert('어음금액을 입력하세요.')
	if (!masterForm.custcd) return vAlert('지급거래처를 선택하세요.')

	try {
		const payload = {
			actkind: masterForm.actkind,
			cmpycd: authStore.cmpycd,
			BILLGU: '100',
			billno: masterForm.billno,
			billno_TO: '',
			issuman: masterForm.issuman,
			bankcd: masterForm.bankcd,
			stdymd: masterForm.stdymd.replace(/-/g, ''),
			endymd: masterForm.endymd.replace(/-/g, ''),
			billamt: masterForm.billamt,
			custcd: masterForm.custcd,
			billtype: masterForm.billtype,
			useyn: masterForm.useyn,
			userid: authStore.userid
		}

		const res = await api.post('/api/haba/HABA_160U_STR', payload)
		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].ret_msg)
		} else {
			vAlert('정상으로 저장 되었습니다.')
			search()
			initialize()
		}
	} catch (e) { vAlertError('저장 실패') }
}

const deleteData = async () => {
	if (masterForm.actkind !== 'U1' || !masterForm.billno) return

	if (!confirm('정말로 삭제하시겠습니까?')) return

	try {
		const res = await api.post('/api/haba/HABA_160U_STR', {
			actkind: 'D1',
			cmpycd: authStore.cmpycd,
			BILLGU: '100',
			billno: masterForm.billno,
			userid: authStore.userid
		})

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].ret_msg)
		} else {
			vAlert('삭제되었습니다.')
			search()
			initialize()
		}
	} catch (e) { vAlertError('삭제 중 오류 발생') }
}

const excel = () => {
	const today = new Date().toISOString().substring(0, 10)
	mainGrid?.download("xlsx", `지급어음기초자료_${today}.xlsx`)
}

const print = () => {
	const params = `billno=${searchForm.billno}&billno_TO=${searchForm.billno_TO}`
	window.open(`/api/haba/HABA_160P?${params}`, 'NotesPrint', 'width=800,height=800,scrollbars=yes')
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.actkind = 'I1'
	masterForm.useyn = 'Y'
	if (billTypeOptions.value.length > 0) masterForm.billtype = billTypeOptions.value[0].value
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: 'BANK' | 'CUST') {
	const isBank = type === 'BANK'
	Object.assign(modalProps, {
		title: isBank ? '발행은행 선택' : '거래처 선택',
		path: '/api/ha00/HA00_03P_STR',
		data: { custgbn: isBank ? '020' : '010', cmpycd: authStore.cmpycd, search: isBank ? masterForm.banknm : masterForm.custnm },
		columns: [
			{ title: '코드', field: 'col0', width: 100, hozAlign: 'center' },
			{ title: isBank ? '은행명' : '거래처명', field: 'col1', width: 250 }
		],
		onConfirm: (d: any) => {
			if (isBank) {
				masterForm.bankcd = d.col0
				masterForm.banknm = d.col1
			} else {
				masterForm.custcd = d.col0
				masterForm.custnm = d.col1
			}
		}
	})
	modalVisible.value = true
}

const formatDate = (val: string) => {
	if (!val || val.length !== 8) return ''
	return `${val.substring(0, 4)}-${val.substring(4, 6)}-${val.substring(6, 8)}`
}

onMounted(async () => {
	if (typeof window !== 'undefined') (window as any).XLSX = XLSX
	await fetchOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "어음번호", field: "col0", width: 130, hozAlign: "center" },
				{ title: "발행은행", field: "col11", width: 150 },
				{ title: "발행인", field: "col1", width: 120 },
				{ title: "발행일", field: "col3", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
				{ title: "만기일", field: "col4", width: 100, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
				{ title: "금 액", field: "col5", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "유형", field: "col10", width: 100, hozAlign: "center" },
				{ title: "지급거래처", field: "COL9", minWidth: 200 },
				{
					title: "사용",
					field: "col8",
					width: 60,
					hozAlign: "center",
					formatter: (cell) => cell.getValue() === 'Y' ? '✔️' : '❌'
				}
			],
			rowClick: (e, row) => {
				const d = row.getData()
				masterForm.actkind = 'U1'
				masterForm.billno = d.col0
				masterForm.issuman = d.col1
				masterForm.bankcd = d.col2
				masterForm.banknm = d.col11
				masterForm.stdymd = formatDate(d.col3)
				masterForm.endymd = formatDate(d.col4)
				masterForm.billamt = Number(d.col5)
				masterForm.billtype = d.col7
				masterForm.custnm = d.COL9
				masterForm.custcd = d.col6
				masterForm.useyn = d.col8
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
