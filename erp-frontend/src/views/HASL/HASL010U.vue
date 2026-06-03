<!--
	=============================================================
	프로그램명	: 현업전표등록
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 현업 부서용 회계 전표 입력 및 상세 분개 관리 (ASP 로직 완전 이관)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 (표준 규격) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journal-plus me-2 text-primary" style="font-size: 18px;"></i>
				전표관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">현업전표등록 (HASL010U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-plus-lg"></i> 신규
				</button>
				<div class="btn-group btn-group-sm ms-2 shadow-sm">
					<button class="btn btn-outline-secondary px-2" @click="search('P')" title="이전 전표"><i class="bi bi-chevron-left"></i></button>
					<button class="btn-erp btn-search" @click="search('S')">조회</button>
					<button class="btn btn-outline-secondary px-2" @click="search('N')" title="다음 전표"><i class="bi bi-chevron-right"></i></button>
				</div>
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

		<!-- 🔍 마스터 정보 영역 (표준 erp-table-full 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full">
					<colgroup>
						<col style="width: 100px;" /><col />
						<col style="width: 100px;" /><col />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="required">발행부서</th>
							<td>
								<div class="input-group">
									<input v-model="masterForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="masterForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" />
									<button class="btn" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="required">전표번호</th>
							<td>
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.slipymd" type="date" class="form-control" />
									<input v-model="masterForm.SLIPNO" type="text" class="form-control text-center" style="width: 60px;" placeholder="000" maxlength="3" @blur="formatSlipNo" />
								</div>
							</td>
							<th>발행인/회계일</th>
							<td>
								<div class="d-flex gap-1 align-items-center">
									<span class="fw-bold small text-secondary">{{ masterForm.EMPNM }}</span>
									<input v-model="masterForm.acctymD" type="text" class="form-control bg-light text-center" readonly style="width: 100px;" />
								</div>
							</td>
						</tr>
						<tr>
							<th>전표가져오기</th>
							<td>
								<div class="input-group">
									<input v-model="masterForm.search_business" type="text" class="form-control" placeholder="거래내역 검색..." @keydown.enter="openHelp('IMPORT')" />
									<button class="btn" @click="openHelp('IMPORT')"><i class="bi bi-list-ul"></i></button>
								</div>
							</td>
							<th class="required">거래내역</th>
							<td colspan="3">
								<input v-model="masterForm.business" type="text" class="form-control fw-bold" placeholder="전체 거래 내역을 입력하십시오." />
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 분개 내역 그리드 (표준 통합CSS 적용) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-2 bg-light d-flex justify-content-between align-items-center border-bottom text-secondary">
					<span class="fw-bold small"><i class="bi bi-list-columns me-1"></i> 분개 상세 내역</span>
					<button class="btn-erp btn-save" style="height: 22px; padding: 0 8px;" @click="addRow"><i class="bi bi-plus"></i> 행추가</button>
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
				<!-- 하단 합계 영역 (표준 다크 테마) -->
				<div class="card-footer p-0 border-top bg-dark text-white">
					<table class="table table-sm table-dark table-bordered mb-0 text-end fw-bold" style="font-size: 11px; table-layout: fixed;">
						<tbody>
							<tr>
								<td class="text-center" style="width: 35%;">차/대변 합계</td>
								<td style="width: 20%;" class="text-info">{{ formatMoney(totalDebit) }}</td>
								<td style="width: 20%;" class="text-warning">{{ formatMoney(totalCredit) }}</td>
								<td style="width: 25%;" :class="{'text-success': balance === 0, 'text-danger': balance !== 0}">차액: {{ formatMoney(balance) }}</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📝 관리 항목 상세 입력 영역 (표준 그리드 폰트 및 여백) -->
		<div class="p-2 pt-0 flex-shrink-0" v-if="selectedRow">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-secondary text-white d-flex justify-content-between align-items-center">
					<span class="small fw-bold"><i class="bi bi-gear-fill me-1"></i> [{{ selectedRow.acctnm || '계정미선택' }}] 상세 관리항목</span>
					<span class="badge" :class="selectedRow.DBCR === 'D' ? 'bg-primary' : 'bg-danger'">{{ selectedRow.DBCR === 'D' ? '차변' : '대변' }}</span>
				</div>
				<div class="card-body p-2 bg-light-subtle">
					<div class="row g-2">
						<!-- 1. 기본 관리 -->
						<div class="col-md-3" v-if="selectedRow.TYPEMGT > '000' || ['050','060','070'].includes(selectedRow.typeacct)">
							<div class="d-flex align-items-center">
								<span class="erp-label" style="width: 70px !important; min-width: 70px !important;">{{ selectedRow.TITMGT || '관리번호' }}</span>
								<div class="input-group">
									<input v-model="selectedRow.mgtno" type="text" class="form-control" />
									<button class="btn" @click="openHelp('MGT')"><i class="bi bi-search"></i></button>
								</div>
							</div>
						</div>
						<div class="col-md-3" v-if="selectedRow.TYPESUB > '000'">
							<div class="d-flex align-items-center">
								<span class="erp-label" style="width: 70px !important; min-width: 70px !important;">{{ selectedRow.TITSUB || '거래처' }}</span>
								<div class="input-group">
									<input v-model="selectedRow.SUBNM" type="text" class="form-control" />
									<button class="btn" @click="openHelp('SUB')"><i class="bi bi-search"></i></button>
								</div>
							</div>
						</div>
						<div class="col-md-2" v-if="selectedRow.TYPEBUGT > '000'">
							<div class="d-flex align-items-center">
								<span class="erp-label" style="width: 60px !important; min-width: 60px !important;">예산</span>
								<div class="input-group">
									<input v-model="selectedRow.bugtnm" type="text" class="form-control" />
									<button class="btn" @click="openHelp('BUGT')"><i class="bi bi-search"></i></button>
								</div>
							</div>
						</div>
						<div class="col-md-2" v-if="selectedRow.acctcd?.startsWith('4') || selectedRow.acctcd?.startsWith('5')">
							<div class="d-flex align-items-center">
								<span class="erp-label" style="width: 60px !important; min-width: 60px !important;">프로젝트</span>
								<div class="input-group">
									<input v-model="selectedRow.PRJNM" type="text" class="form-control" />
									<button class="btn" @click="openHelp('PRJ')"><i class="bi bi-search"></i></button>
								</div>
							</div>
						</div>

						<!-- 2. 외화 -->
						<template v-if="selectedRow.FRGNYN === 'Y'">
							<div class="col-md-2">
								<div class="d-flex align-items-center">
									<span class="erp-label" style="width: 60px !important; min-width: 60px !important;">환율</span>
									<div class="input-group">
										<select v-model="selectedRow.FRGNKIND" class="form-select" style="max-width: 70px;">
											<option value="USD">USD</option><option value="JPY">JPY</option><option value="EUR">EUR</option>
										</select>
										<input v-model="selectedRow.frgnrate" type="number" class="form-control text-end" step="0.01" />
									</div>
								</div>
							</div>
						</template>

						<!-- 3. 부가세 상세 -->
						<template v-if="selectedRow.typeacct === '090'">
							<div class="col-md-3">
								<div class="d-flex align-items-center">
									<span class="erp-label" style="width: 70px !important; min-width: 70px !important;">세무유형</span>
									<div class="d-flex gap-1 flex-grow-1">
										<select v-model="selectedRow.docno1" class="form-select">
											<option v-for="opt in bizPlaceOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
										</select>
										<select v-model="selectedRow.docno3" class="form-select">
											<option v-for="opt in vatTypeOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
										</select>
									</div>
								</div>
							</div>
							<div class="col-md-2">
								<div class="d-flex align-items-center">
									<span class="erp-label" style="width: 60px !important; min-width: 60px !important;">발행일</span>
									<input v-model="selectedRow.docno6" type="date" class="form-control" />
								</div>
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
	business: '',
	search_business: '',
	acctymD: '',
	SLIPGU: '010',
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
const formatYmd = (v: string) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v

const updateTotals = () => {
	const data = mainGrid?.getData() || []
	totalDebit.value = data.reduce((sum, r: any) => sum + (r.DBCR === 'D' ? Number(r.AMOUNT || 0) : 0), 0)
	totalCredit.value = data.reduce((sum, r: any) => sum + (r.DBCR === 'C' ? Number(r.AMOUNT || 0) : 0), 0)
}

const search = async (kind: string = 'S') => {
	if (kind === 'S' && !masterForm.SLIPNO) return vAlert('조회할 전표번호를 입력하십시오.')
	try {
		const resMaster = await api.post('/api/hasl/HASL_010U_STR', {
			actkind: kind,
			cmpycd: authStore.cmpycd,
			slipymd: masterForm.slipymd.replace(/-/g, ''),
			SLIPNO: masterForm.SLIPNO || '000',
			deptcd: masterForm.deptcd
		})

		const master = resMaster.data[0]
		if (master) {
			if (kind === 'X') {
				Object.assign(masterForm, master)
				masterForm.SLIPNO = ''
				masterForm.slipymd = today
				masterForm.acctymD = today
				masterForm.EMPNM = authStore.usernm
			} else {
				Object.assign(masterForm, master)
				if (master.acctymD) masterForm.acctymD = formatYmd(master.acctymD)
				if (master.slipymd) masterForm.slipymd = formatYmd(master.slipymd)
			}

			const resDetails = await api.post('/api/hasl/HASL_011U_STR', {
				actkind: 'S',
				cmpycd: authStore.cmpycd,
				slipymd: master.slipymd,
				SLIPNO: master.SLIPNO
			})

			const detailData = resDetails.data || []
			if (kind === 'X') {
				detailData.forEach((d: any) => { d.srowno = ''; d.upkind = 'A' })
			}
			mainGrid?.setData(detailData)
			updateTotals()

			if (detailData.length > 0) {
				setTimeout(() => { mainGrid?.getRows()[0].select() }, 100)
			}
		} else {
			vAlert(kind === 'S' ? '조회된 데이터가 없습니다.' : '이전/다음 데이터가 없습니다.')
		}
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (balance.value !== 0) return vAlert('차/대변 합계 금액이 일치하지 않습니다.')
	if (!masterForm.business || masterForm.business.length < 5) return vAlert('거래내역을 정확히 입력하십시오.')

	try {
		const payload = {
			actkind: masterForm.SLIPNO ? 'U' : 'A',
			MASTER: { ...masterForm, slipymd: masterForm.slipymd.replace(/-/g, '') },
			DETAILS: mainGrid?.getData().map(d => {
				const row = { ...d }
				row.docno6 = row.docno6?.replace(/-/g, '')
				row.docno7 = row.docno7?.replace(/-/g, '')
				row.PAyyMD = row.PAyyMD?.replace(/-/g, '')
				if (row.DBCR === 'D') { row.dbamt = row.AMOUNT; row.cramt = 0 }
				else { row.dbamt = 0; row.cramt = row.AMOUNT }
				row.custcd = row.SUBCD
				row.SSLIPNO = row.PSLIPNO
				row.deptcd = row.USEdeptcd || masterForm.deptcd
				if (row.DBCR === 'D' && row.typeacct === '050') row.custcd = row.docno9
				return row
			})
		}
		const res = await api.post('/api/hasl/HASL_010U_SAVE', payload)
		vAlert('저장되었습니다.')
		masterForm.SLIPNO = res.data.SLIPNO
		search('S')
	} catch (e) { vAlertError('저장 실패') }
}

const deleteData = async () => {
	if (!confirm('정말 삭제하시겠습니까?')) return
	if (masterForm.SLIPGU !== '010') return vAlert('현업 전표가 아니므로 삭제할 수 없습니다.')
	try {
		await api.post('/api/hasl/HASL_010U_SAVE', { actkind: 'D', MASTER: masterForm })
		vAlert('삭제되었습니다.')
		initialize()
	} catch (e) { vAlertError('삭제 실패') }
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.deptcd = authStore.deptcd
	masterForm.deptnm = authStore.deptnm
	masterForm.slipymd = today
	masterForm.EMPNM = authStore.usernm
	masterForm.SLIPGU = '010'
	mainGrid?.clearData()
	selectedRow.value = null
	totalDebit.value = 0
	totalCredit.value = 0
}

const formatSlipNo = () => { if (masterForm.SLIPNO) masterForm.SLIPNO = masterForm.SLIPNO.padStart(3, '0') }

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
	}
	modalVisible.value = true
}

const print = () => window.open(`/api/hasl/HASL_SLIP_PRINT?slipymd=${masterForm.slipymd.replace(/-/g, '')}&SLIPNO=${masterForm.SLIPNO}&deptcd=${masterForm.deptcd}`)

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			selectable: 1,
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "No", field: "srowno", width: 40, hozAlign: "center" },
				{
					title: "차/대", field: "DBCR", width: 70, editor: "list",
					editorParams: { values: { "D": "차변", "C": "대변" } }, hozAlign: "center",
					formatter: (cell) => cell.getValue() === 'D' ? '<span class="text-primary fw-bold">차변</span>' : '<span class="text-danger fw-bold">대변</span>'
				},
				{
					title: "계정과목", field: "acctnm", width: 200,
					cellClick: (e, cell) => openHelp('ACCT', cell),
					cssClass: "cursor-pointer"
				},
				{ title: "적요", field: "remark", minWidth: 200, editor: "input" },
				{ title: "사용부서", field: "USEdeptnm", width: 120 },
				{
					title: "금액", field: "AMOUNT", width: 120, hozAlign: "right",
					editor: "input", formatter: "money", formatterParams: { precision: 0 }
				},
				{
					title: "", width: 40, hozAlign: "center",
					formatter: () => '<i class="bi bi-trash text-danger cursor-pointer"></i>',
					cellClick: (e, cell) => { if(confirm('행을 삭제하시겠습니까?')) cell.getRow().delete() }
				}
			],
			rowClick: (e, row) => {
				selectedRow.value = row.getData()
				const formatD = (v: string) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v
				selectedRow.value.docno6 = formatD(selectedRow.value.docno6)
				selectedRow.value.docno7 = formatD(selectedRow.value.docno7)
				selectedRow.value.PAyyMD = formatD(selectedRow.value.PAyyMD)
			}
		})
		mainGrid.on("dataChanged", updateTotals)
	}
})

const bizPlaceOptions = [{ CD: '100', NM: '본사' }, { CD: '200', NM: '지점' }]
const vatTypeOptions = [{ CD: '11', NM: '과세' }, { CD: '12', NM: '영세' }, { CD: '13', NM: '면세' }, { CD: '17', NM: '카드' }]
</script>
