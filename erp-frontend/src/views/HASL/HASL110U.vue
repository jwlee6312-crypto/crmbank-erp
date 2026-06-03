<!--
	=============================================================
	프로그램명	: 경리전표입력
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 경리 부서용 회계 전표 입력 및 상세 분개 관리 (부가세, 어음, 접대비 등 상세 제어)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journal-check me-2 text-primary" style="font-size: 18px;"></i>
				전표관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">경리전표입력 (HASL110U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-plus-lg"></i> 신규
				</button>
				<button class="btn-erp btn-search" @click="search('S')">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-check-lg"></i> 저장
				</button>
				<button v-if="masterForm.SLIPNO" class="btn-erp btn-delete" @click="deleteData">
					<i class="bi bi-trash"></i> 삭제
				</button>
				<button v-if="masterForm.SLIPNO" class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
				</button>
			</div>
		</div>

		<!-- 🔍 마스터 정보 및 검색 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 12%;" /><col style="width: 28%;" />
						<col style="width: 12%;" /><col style="width: 28%;" />
						<col style="width: 10%;" /><col style="width: 10%;" />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">전표번호</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.slipymd" type="date" class="form-control form-control-sm" />
									<input v-model="masterForm.SLIPNO" type="text" class="form-control form-control-sm text-center" style="width: 50px;" placeholder="000" maxlength="3" @blur="formatSlipNo" />
									<div class="btn-group btn-group-sm ms-1 shadow-sm">
										<button class="btn btn-outline-secondary px-1" title="이전" @click="search('P')"><i class="bi bi-chevron-left"></i></button>
										<button class="btn btn-outline-secondary px-1" title="다음" @click="search('N')"><i class="bi bi-chevron-right"></i></button>
									</div>
								</div>
							</td>
							<th class="text-center border-end border-start">전표 가져오기</th>
							<td class="bg-white">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.search_KEY" type="text" class="form-control" placeholder="전표 적요 검색..." @keydown.enter="openHelp('IMPORT_SLIP')" />
									<button class="btn btn-outline-primary px-2" @click="openHelp('IMPORT_SLIP')"><i class="bi bi-list-ul"></i></button>
								</div>
							</td>
							<th class="text-center border-end border-start">발행인</th>
							<td class="bg-white text-center fw-bold small text-secondary">{{ masterForm.EMPNM }}</td>
						</tr>
						<tr>
							<th class="text-center border-end border-top">발행부서</th>
							<td class="bg-white border-top">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="masterForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center border-end border-top border-start">회계일자</th>
							<td class="bg-white border-top">
								<input v-model="masterForm.acctymD" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center border-end border-top border-start">상태</th>
							<td class="bg-white text-center px-1 border-top">
								<span :class="masterForm.SLIPGU === '020' ? 'badge bg-success' : 'badge bg-primary'" style="font-size: 10px;">
									{{ masterForm.SLIPGU === '020' ? '미확정' : '확정' }}
								</span>
							</td>
						</tr>
						<tr>
							<th class="text-center border-end bg-secondary-subtle border-top">거래내역</th>
							<td colspan="5" class="bg-white p-0 border-top">
								<textarea v-model="masterForm.business" class="form-control form-control-sm border-0 rounded-0" rows="1" placeholder="전체 거래 내역을 입력하십시오."></textarea>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 분개 내역 그리드 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-2 bg-light d-flex justify-content-between align-items-center border-bottom">
					<span class="fw-bold small text-secondary"><i class="bi bi-grid-3x3-gap me-1"></i> 분개 상세 내역</span>
					<div class="btn-group btn-group-sm">
						<button class="btn btn-primary py-0 px-2" @click="addRow"><i class="bi bi-plus"></i> 추가</button>
						<button class="btn btn-danger py-0 px-2" @click="deleteRow"><i class="bi bi-dash"></i> 삭제</button>
					</div>
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
				<!-- 하단 합계 바 -->
				<div class="card-footer p-0 border-top bg-dark text-white fw-bold">
					<table class="table table-sm table-dark table-bordered mb-0 text-end fw-bold" style="font-size: 12px; table-layout: fixed;">
						<tr>
							<td class="text-center" style="width: 35%;">합 계</td>
							<td style="width: 20%;" class="text-info">{{ formatMoney(totalDebit) }}</td>
							<td style="width: 20%;" class="text-warning">{{ formatMoney(totalCredit) }}</td>
							<td style="width: 25%;" :class="{'text-success': balance === 0, 'text-danger': balance !== 0}">
								{{ balance === 0 ? '일치' : '차액: ' + formatMoney(balance) }}
							</td>
						</tr>
					</table>
				</div>
			</div>
		</div>

		<!-- 📝 상세 관리항목 (선택된 행 기반) -->
		<div class="p-2 pt-0 flex-shrink-0" v-if="selectedRow">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-dark text-white d-flex justify-content-between align-items-center">
					<span class="small fw-bold"><i class="bi bi-pencil-square me-1"></i> [{{ selectedRow.acctnm || '계정미선택' }}] 상세 관리항목</span>
					<span class="badge" :class="selectedRow.DBCR === 'D' ? 'bg-primary' : 'bg-danger'">{{ selectedRow.DBCR === 'D' ? '차변' : '대변' }}</span>
				</div>
				<div class="card-body p-2 bg-light-subtle">
					<div class="row g-2">
						<!-- 1. 기본 관리 (관리번호, 거래처, 예산, 프로젝트) -->
						<div class="col-md-3" v-if="selectedRow.TYPEMGT > '000' || ['050','060','070'].includes(selectedRow.typeacct)">
							<label class="erp-label-sm">{{ selectedRow.TITMGT || '관리번호' }}</label>
							<div class="input-group input-group-sm">
								<input v-model="selectedRow.mgtno" type="text" class="form-control" />
								<button class="btn btn-outline-secondary" @click="openHelp('MGT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="col-md-3" v-if="selectedRow.TYPESUB > '000'">
							<label class="erp-label-sm">{{ selectedRow.TITSUB || '보조/거래처' }}</label>
							<div class="input-group input-group-sm">
								<input v-model="selectedRow.SUBNM" type="text" class="form-control" />
								<button class="btn btn-outline-secondary" @click="openHelp('SUB')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="col-md-3" v-if="selectedRow.TYPEBUGT > '000'">
							<label class="erp-label-sm">예산코드</label>
							<div class="input-group input-group-sm">
								<input v-model="selectedRow.bugtnm" type="text" class="form-control" />
								<button class="btn btn-outline-secondary" @click="openHelp('BUGT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="col-md-3" v-if="selectedRow.acctcd?.startsWith('4') || selectedRow.acctcd?.startsWith('5')">
							<label class="erp-label-sm">프로젝트</label>
							<div class="input-group input-group-sm">
								<input v-model="selectedRow.PRJNM" type="text" class="form-control" />
								<button class="btn btn-outline-secondary" @click="openHelp('PRJ')"><i class="bi bi-search"></i></button>
							</div>
						</div>

						<!-- 2. 부가세 상세 (Type 090) -->
						<template v-if="selectedRow.typeacct === '090'">
							<div class="col-md-2">
								<label class="erp-label-sm">사업장/세무유형</label>
								<div class="d-flex gap-1">
									<select v-model="selectedRow.docno1" class="form-select form-select-sm">
										<option v-for="opt in bizPlaceOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
									</select>
									<select v-model="selectedRow.docno3" class="form-select form-select-sm">
										<option v-for="opt in vatTypeOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
									</select>
								</div>
							</div>
							<div class="col-md-2">
								<label class="erp-label-sm">거래처/발행일</label>
								<div class="input-group input-group-sm">
									<input v-model="selectedRow.docno2NM" type="text" class="form-control" placeholder="거래처" @keydown.enter="openHelp('VAT_CUST')" />
									<input v-model="selectedRow.docno6" type="date" class="form-control" />
								</div>
							</div>
							<div class="col-md-2">
								<label class="erp-label-sm">공급가액</label>
								<input v-model="selectedRow.docno8" type="number" class="form-control form-control-sm text-end" />
							</div>
							<div class="col-md-2">
								<label class="erp-label-sm">부가세</label>
								<input v-model="selectedRow.docno9" type="number" class="form-control form-control-sm text-end" />
							</div>
						</template>

						<!-- 3. 접대비 상세 (Type 100) -->
						<template v-if="selectedRow.typeacct === '100'">
							<div class="col-md-2">
								<label class="erp-label-sm">접대일자/유형</label>
								<div class="d-flex gap-1">
									<input v-model="selectedRow.docno6" type="date" class="form-control form-control-sm" />
									<select v-model="selectedRow.docno3" class="form-select form-select-sm" style="width: 80px;">
										<option value="1">법인</option><option value="2">개인</option>
									</select>
								</div>
							</div>
							<div class="col-md-3">
								<label class="erp-label-sm">상호/사업자번호</label>
								<div class="input-group input-group-sm shadow-sm">
									<input v-model="selectedRow.docno4" type="text" class="form-control" placeholder="상호" />
									<input v-model="selectedRow.docno2" type="text" class="form-control" placeholder="사업자번호" />
								</div>
							</div>
							<div class="col-md-3">
								<label class="erp-label-sm">접대상대/목적</label>
								<div class="input-group input-group-sm shadow-sm">
									<input v-model="selectedRow.docno5" type="text" class="form-control" placeholder="상대방" />
									<input v-model="selectedRow.docno7" type="text" class="form-control" placeholder="목적" />
								</div>
							</div>
						</template>

						<!-- 4. 어음 상세 (Type 050, 060, 070) -->
						<template v-if="['050','060','070'].includes(selectedRow.typeacct)">
							<div class="col-md-2">
								<label class="erp-label-sm">발행일/만기일</label>
								<div class="input-group input-group-sm shadow-sm">
									<input v-model="selectedRow.docno6" type="date" class="form-control" />
									<input v-model="selectedRow.docno7" type="date" class="form-control" />
								</div>
							</div>
							<div class="col-md-3">
								<label class="erp-label-sm">은행/발행인</label>
								<div class="input-group input-group-sm shadow-sm">
									<input v-model="selectedRow.docno4" type="text" class="form-control" placeholder="은행" />
									<input v-model="selectedRow.docno5" type="text" class="form-control" placeholder="발행인" />
								</div>
							</div>
						</template>

						<!-- 5. 선급비용 / 미불 (Type 110, CRS 010) -->
						<template v-if="selectedRow.typeacct === '110' || selectedRow.TYPECRS === '010'">
							<div class="col-md-3" v-if="selectedRow.DBCR === 'D' || (selectedRow.typeacct === '110' && selectedRow.DBCR === 'C')">
								<label class="erp-label-sm">원천/발생전표번호</label>
								<div class="input-group input-group-sm">
									<input v-model="selectedRow.PSLIPNO" type="text" class="form-control shadow-sm" />
									<button class="btn btn-outline-secondary" @click="openHelp('SLIP_LINK')"><i class="bi bi-search"></i></button>
								</div>
							</div>
							<div class="col-md-2" v-if="selectedRow.TYPECRS === '010' && selectedRow.DBCR === 'C'">
								<label class="erp-label-sm">지불일자</label>
								<input v-model="selectedRow.PAyyMD" type="date" class="form-control form-control-sm shadow-sm" />
							</div>
						</template>
					</div>
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
import { useFormReset } from '@/composables/useFormReset'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const today = new Date().toISOString().substring(0, 10)

// 🔍 마스터 데이터
const masterForm = reactive({
	cmpycd: authStore.cmpycd,
	deptcd: authStore.deptcd,
	deptnm: authStore.deptnm,
	slipymd: today,
	SLIPNO: '',
	EMPNM: authStore.usernm,
	acctymD: today,
	business: '',
	search_KEY: '',
	SLIPGU: '020',
	clsymd: '00000000'
})

// 📊 그리드 관련
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null
const selectedRow = ref<any>(null)

// 합계 계산
const totalDebit = ref(0)
const totalCredit = ref(0)
const balance = computed(() => totalDebit.value - totalCredit.value)

const formatMoney = (val: any) => Number(val || 0).toLocaleString()

const updateTotals = () => {
	const data = mainGrid?.getData() || []
	totalDebit.value = data.reduce((sum, r: any) => sum + (r.DBCR === 'D' ? Number(r.AMOUNT || 0) : 0), 0)
	totalCredit.value = data.reduce((sum, r: any) => sum + (r.DBCR === 'C' ? Number(r.AMOUNT || 0) : 0), 0)
}

// 🚀 데이터 조회 (S: 일반, P: 이전, N: 다음)
const search = async (kind: string = 'S') => {
	if (kind === 'S' && !masterForm.SLIPNO) return vAlert('조회할 전표번호를 입력하십시오.')
	try {
		const res = await api.post('/api/hasl/HASL_110U_STR', {
			actkind: kind,
			cmpycd: authStore.cmpycd,
			slipymd: masterForm.slipymd.replace(/-/g, ''),
			SLIPNO: masterForm.SLIPNO || '000',
			deptcd: masterForm.deptcd
		})

		const { master, details } = res.data
		if (master) {
			Object.assign(masterForm, master)
			// 날짜 포맷팅
			if (master.acctymD) masterForm.acctymD = formatYmd(master.acctymD)
			if (master.slipymd) masterForm.slipymd = formatYmd(master.slipymd)

			mainGrid?.setData(details || [])
			updateTotals()
		} else {
			vAlert(kind === 'S' ? '조회된 데이터가 없습니다.' : '이전/다음 전표가 존재하지 않습니다.')
		}
	} catch (e) { vAlertError('데이터 조회 실패') }
}

const formatSlipNo = () => {
	if (masterForm.SLIPNO) masterForm.SLIPNO = masterForm.SLIPNO.padStart(3, '0')
}

const formatYmd = (v: string) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v

// 💾 저장 / 삭제 처리
const save = async () => {
	if (balance.value !== 0) return vAlert('차/대변 합계 금액이 일치하지 않습니다.')
	if (!masterForm.business) return vAlert('거래내역을 입력하십시오.')

	const acctYmdRaw = masterForm.acctymD.replace(/-/g, '')
	if (acctYmdRaw <= masterForm.clsymd) return vAlert('회계 마감된 일자입니다.')

	try {
		const payload = {
			actkind: masterForm.SLIPNO ? 'U' : 'A',
			MASTER: { ...masterForm, acctymD: acctYmdRaw, slipymd: masterForm.slipymd.replace(/-/g, '') },
			DETAILS: mainGrid?.getData().map((d: any) => ({
				...d,
				docno6: d.docno6?.replace(/-/g, ''),
				docno7: d.docno7?.replace(/-/g, ''),
				PAyyMD: d.PAyyMD?.replace(/-/g, '')
			}))
		}
		const res = await api.post('/api/hasl/HASL_110U_SAVE', payload)
		vAlert('저장되었습니다.')
		masterForm.SLIPNO = res.data.SLIPNO
		search('S')
	} catch (e) { vAlertError('저장 실패') }
}

const deleteData = async () => {
	if (!confirm('정말 삭제하시겠습니까?')) return
	try {
		await api.post('/api/hasl/HASL_110U_SAVE', { actkind: 'D', MASTER: masterForm })
		vAlert('삭제되었습니다.')
		initialize()
	} catch (e) { vAlertError('삭제 실패') }
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.deptcd = authStore.deptcd
	masterForm.deptnm = authStore.deptnm
	masterForm.slipymd = today
	masterForm.acctymD = today
	masterForm.EMPNM = authStore.usernm
	masterForm.SLIPGU = '020'
	mainGrid?.clearData()
	selectedRow.value = null
	totalDebit.value = 0
	totalCredit.value = 0
}

const addRow = () => {
	const lastRow = mainGrid?.getData().slice(-1)[0]
	mainGrid?.addRow({
		DBCR: lastRow ? (lastRow.DBCR === 'D' ? 'C' : 'D') : 'D',
		remark: masterForm.business,
		USEdeptcd: masterForm.deptcd,
		USEdeptnm: masterForm.deptnm,
		AMOUNT: 0,
		upkind: 'A'
	})
}

const deleteRow = () => {
	const selectedRows = mainGrid?.getSelectedRows()
	if (selectedRows && selectedRows.length > 0) {
		selectedRows.forEach(row => row.delete())
	} else {
		vAlert('행을 먼저 선택하십시오.')
	}
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell?: any) {
	if (type === 'ACCT') {
		Object.assign(modalProps, {
			title: '계정과목 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'A0', cmpycd: authStore.cmpycd, search: '' },
			columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 180 }],
			onConfirm: (d: any) => {
				const row = cell.getRow().getData()
				Object.assign(row, d)
				cell.getRow().update(row)
				selectedRow.value = row
			}
		})
	} else if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: '' },
			columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { masterForm.deptcd = d.deptcd; masterForm.deptnm = d.deptnm }
		})
	} else if (type === 'IMPORT_SLIP') {
		Object.assign(modalProps, {
			title: '기존 전표 가져오기', path: '/api/hasl/HASL_110U_search',
			data: { cmpycd: authStore.cmpycd, search: masterForm.search_KEY },
			columns: [{ title: '일자', field: 'slipymd', width: 100 }, { title: '번호', field: 'SLIPNO', width: 60 }, { title: '적요', field: 'business', width: 250 }],
			onConfirm: (d: any) => {
				masterForm.slipymd = formatYmd(d.slipymd)
				masterForm.SLIPNO = d.SLIPNO
				search('S')
			}
		})
	}
	modalVisible.value = true
}

const print = () => window.open(`/api/hasl/HASL_SLIP_PRINT?slipymd=${masterForm.slipymd.replace(/-/g, '')}&SLIPNO=${masterForm.SLIPNO}&deptcd=${masterForm.deptcd}`)

onMounted(async () => {
	// 마감 정보 로드
	const resCls = await api.post('/api/ha00/HA00_010S_STR', { gubun: 'P1', cmpycd: authStore.cmpycd })
	if (resCls.data) masterForm.clsymd = resCls.data.clsymd || '00000000'

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			selectable: 1,
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "No", field: "srowno", width: 40, hozAlign: "center" },
				{
					title: "구분", field: "DBCR", width: 70, editor: "list",
					editorParams: { values: { "D": "차변", "C": "대변" } },
					hozAlign: "center",
					formatter: (cell) => cell.getValue() === 'D' ? '<span class="text-primary fw-bold">차변</span>' : '<span class="text-danger fw-bold">대변</span>'
				},
				{
					title: "계정과목", field: "acctnm", width: 200,
					cellClick: (e, cell) => openHelp('ACCT', cell),
					cssClass: "bg-warning-subtle cursor-pointer"
				},
				{ title: "적요", field: "remark", minWidth: 220, editor: "input" },
				{ title: "사용부서", field: "USEdeptnm", width: 120 },
				{
					title: "금액", field: "AMOUNT", width: 120, hozAlign: "right",
					editor: "input", formatter: "money", formatterParams: { precision: 0 }
				}
			],
			rowClick: (e, row) => {
				selectedRow.value = row.getData()
				const fmt = (f: string) => { if(selectedRow.value[f]) selectedRow.value[f] = formatYmd(selectedRow.value[f]) }
				['docno6', 'docno7', 'PAyyMD'].forEach(fmt)
			}
		})
		mainGrid.on("dataChanged", updateTotals)
	}
})

// 옵션 데이터
const bizPlaceOptions = [{ CD: '100', NM: '본사' }, { CD: '200', NM: '지점' }]
const vatTypeOptions = [{ CD: '11', NM: '과세' }, { CD: '12', NM: '영세' }, { CD: '13', NM: '면세' }]
</script>

<style scoped>
.erp-label-sm { display: block; font-size: 11px; color: #555; margin-bottom: 2px; font-weight: 600; }
.bg-warning-subtle { background-color: #fffcf0 !important; }
.cursor-pointer { cursor: pointer; }
</style>
