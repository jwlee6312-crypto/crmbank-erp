<!--
	=============================================================
	프로그램명	: 법인카드관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 법인카드 정보 상세 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-credit-card me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">법인카드 (HABA140U)</span>
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
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 250px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">발 행 처</th>
							<td class="bg-white border-end px-2">
								<div class="input-group input-group-sm">
									<input v-model="searchForm.bankcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchForm.bankcd_t" type="text" class="form-control" placeholder="발행처 선택" @keydown.enter="openHelp('search_BANK')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('search_BANK')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<td class="bg-white px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> 검색하실 발행처를 선택해 주십시요.
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
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> 법인카드 상세 정보 [{{ masterForm.actkind === 'I1' ? '신규' : '수정' }}]</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">카드번호</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.CARDNO" type="text" class="form-control form-control-sm" maxlength="19" :readonly="masterForm.actkind === 'U1'" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">발 행 처</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.bankcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="masterForm.bankcd_t" type="text" class="form-control" @keydown.enter="openHelp('BANK')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('BANK')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">카 드 명</th>
							<td class="bg-white border-top px-2 py-1">
								<input v-model="masterForm.CARDNM" type="text" class="form-control form-control-sm" maxlength="50" />
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">발 행 일</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterForm.stdymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">유효년월</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.sndymd_yy" type="text" class="form-control form-control-sm text-center" maxlength="4" style="width: 60px;" placeholder="yyyy" />
									<span class="small">년</span>
									<select v-model="masterForm.sndymd.mm" class="form-select form-select-sm" style="width: 70px;">
										<option value="00">선택</option>
										<option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ String(m).padStart(2, '0') }}</option>
									</select>
									<span class="small">월</span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">결 제 일</th>
							<td class="bg-white border-top px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<select v-model="masterForm.CHKDD" class="form-select form-select-sm" style="width: 80px;">
										<option value="00">선택</option>
										<option v-for="d in 31" :key="d" :value="String(d).padStart(2, '0')">{{ String(d).padStart(2, '0') }}</option>
									</select>
									<span class="small">일</span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">소 유 자</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterForm.soname" type="text" class="form-control form-control-sm" maxlength="20" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">결제계좌</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.gujano" type="text" class="form-control" @keydown.enter="openHelp('GUJA')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('GUJA')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">사용여부</th>
							<td class="bg-white border-top px-3">
								<div class="form-check pt-1">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useCheck" true-value="Y" false-value="N">
									<label class="form-check-label small fw-bold" for="useCheck">사용</label>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
							<td colspan="5" class="bg-white border-top px-2 py-1">
								<input v-model="masterForm.remark" type="text" class="form-control form-control-sm" maxlength="50" />
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
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 🔍 검색 데이터
const searchForm = reactive({
	bankcd: '',
	bankcd_t: ''
})

// 📝 마스터 데이터
const masterForm = reactive({
	actkind: 'I1',
	CARDNO: '',
	bankcd: '',
	bankcd_t: '',
	CARDNM: '',
	stdymd: '',
	sndymd_yy: '',
	sndymd.mm: '00',
	CHKDD: '00',
	soname: '',
	gujano: '',
	remark: '',
	useyn: 'Y'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const formatYmd = (v: string) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_140U_STR', {
			actkind: searchForm.bankcd_t ? 'SR' : 'S1',
			cmpycd: authStore.cmpycd,
			bankcd: searchForm.bankcd
		})

		const processedData = (res.data || []).map((r: any) => ({
			CARDNO: r.col0,
			bankcd: r.col1,
			banknm: r.col2,
			CARDNM: r.col3,
			stdymd: formatYmd(r.col4),
			sndymd_yy: r.col5,
			sndymd.mm: r.col6,
			CHKDD: r.col7,
			soname: r.col8,
			gujano: r.COL9,
			remark: r.col10,
			useyn: r.col11
		}))

		mainGrid?.setData(processedData)
		if (processedData.length > 0) vAlert('조회되었습니다.')
		else vAlert('데이터가 존재하지 않습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.CARDNO) return vAlert('카드번호를 정확히 입력하세요.')
	if (!masterForm.bankcd) return vAlert('발행처를 기재해 주십시요.')
	if (!masterForm.CARDNM) return vAlert('카드명을 기재해 주십시요.')
	if (!masterForm.stdymd) return vAlert('발행일자를 기재해 주십시요.')
	if (!masterForm.sndymd_yy || isNaN(Number(masterForm.sndymd_yy))) return vAlert('유효년도를 숫자로 기재해 주십시요.')
	if (masterForm.sndymd.mm === '00') return vAlert('유효월을 기재해 주십시요.')
	if (masterForm.CHKDD === '00') return vAlert('결제일을 기재해 주십시요.')
	if (!masterForm.soname) return vAlert('소유자를 기재해 주십시요.')
	if (!masterForm.gujano) return vAlert('결제계좌를 기재해 주십시요.')

	try {
		const payload = {
			...masterForm,
			cmpycd: authStore.cmpycd,
			userid: authStore.userid,
			stdymd: masterForm.stdymd.replace(/-/g, ''),
			sndymd: masterForm.sndymd_yy + masterForm.sndymd.mm
		}

		const res = await api.post('/api/haba/HABA_140U_STR', payload)

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].RET_MSG)
		} else {
			vAlert('정상으로 작업이 되었습니다.')
			search()
			initialize()
		}
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.actkind = 'I1'
	masterForm.sndymd.mm = '00'
	masterForm.CHKDD = '00'
	masterForm.useyn = 'Y'
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'search_BANK') {
		Object.assign(modalProps, {
			title: '발행처 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'C0', cmpycd: authStore.cmpycd, search: searchForm.bankcd_t, custgbn: '020' },
			columns: [{ title: '코드', field: 'custcd', width: 80 }, { title: '거래처명', field: 'custnm', width: 180 }],
			onConfirm: (d: any) => { searchForm.bankcd = d.custcd; searchForm.bankcd_t = d.custnm }
		})
	} else if (type === 'BANK') {
		Object.assign(modalProps, {
			title: '발행처 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'C0', cmpycd: authStore.cmpycd, search: masterForm.bankcd_t, custgbn: '020' },
			columns: [{ title: '코드', field: 'custcd', width: 80 }, { title: '거래처명', field: 'custnm', width: 180 }],
			onConfirm: (d: any) => { masterForm.bankcd = d.custcd; masterForm.bankcd_t = d.custnm }
		})
	} else if (type === 'GUJA') {
		// mgtgbn='010'&acctcd='1120'
		Object.assign(modalProps, {
			title: '결제계좌 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'M0', cmpycd: authStore.cmpycd, search: masterForm.gujano, mgtgbn: '010', acctcd: '1120' },
			columns: [{ title: '계좌번호', field: 'mgtno', width: 150 }, { title: '관리항목명', field: 'mgtnm', width: 150 }],
			onConfirm: (d: any) => { masterForm.gujano = d.mgtno }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "카드번호", field: "CARDNO", width: 150 },
				{ title: "발행처", field: "banknm", width: 150 },
				{ title: "발행일", field: "stdymd", width: 100, hozAlign: "center" },
				{
					title: "유효년월", field: "sndymd_yy", width: 100, hozAlign: "center",
					formatter: (c) => {
						const d = c.getData()
						return `${d.sndymd_yy}-${d.sndymd.mm}`
					}
				},
				{ title: "결제일", field: "CHKDD", width: 80, hozAlign: "center", formatter: (c) => c.getValue() + "일" },
				{ title: "소유자", field: "soname", width: 100 },
				{ title: "결제계좌", field: "gujano", width: 150 },
				{ title: "비고", field: "remark", minWidth: 150 },
				{ title: "사용", field: "useyn", width: 60, hozAlign: "center", formatter: "tickCross" }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				Object.assign(masterForm, d)
				masterForm.actkind = 'U1'
				masterForm.bankcd_t = d.banknm
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
