<!--
	=============================================================
	프로그램명	: 거래처관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 거래처(매입/매출/카드사 등) 기본 정보 및 상세 설정 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-buildings me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">거래처관리 (HABA180U)</span>
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
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 80px;" /><col style="width: 150px;" />
						<col style="width: 80px;" /><col style="width: 150px;" />
						<col style="width: 80px;" /><col style="width: 200px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">종 류</th>
							<td class="bg-white border-end">
								<select v-model="searchForm.Qcustgbn" class="form-select form-select-sm">
									<option value="000">전체</option>
									<option v-for="opt in custGbnOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
								</select>
							</td>
							<th class="text-center border-end">상 태</th>
							<td class="bg-white border-end">
								<select v-model="searchForm.Qstatus" class="form-select form-select-sm">
									<option value="000">전체</option>
									<option v-for="opt in statusOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
								</select>
							</td>
							<th class="text-center border-end">거래처명</th>
							<td class="bg-white border-end px-2">
								<input v-model="searchForm.Qcustnm" type="text" class="form-control form-control-sm" placeholder="검색어 입력" @keydown.enter="search" />
							</td>
							<td class="bg-white"></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📝 상세 정보 입력 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> 거래처 상세 정보 [{{ masterForm.actkind === 'I0' ? '신규' : '수정' }}]</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">거래처코드</th>
							<td class="bg-white border-end px-2">
								<input v-model="masterForm.custcd" type="text" class="form-control form-control-sm text-center fw-bold" maxlength="7" :readonly="masterForm.actkind === 'U0'" />
							</td>
							<th class="text-center bg-light-subtle border-end">상호(명칭)</th>
							<td class="bg-white border-end px-2">
								<input v-model="masterForm.custnm" type="text" class="form-control form-control-sm" maxlength="50" />
							</td>
							<th class="text-center bg-light-subtle border-end">사업자번호</th>
							<td class="bg-white px-2">
								<div class="d-flex align-items-center gap-2">
									<input v-model="masterForm.custno" type="text" class="form-control form-control-sm text-center" maxlength="13" placeholder="- 없이 입력" />
									<span class="text-muted" style="font-size: 11px;">(개인은 주민번호)</span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">종 류</th>
							<td class="bg-white border-end border-top px-2">
								<select v-model="masterForm.custgbn" class="form-select form-select-sm">
									<option v-for="opt in custGbnOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
								</select>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">대 표 자</th>
							<td class="bg-white border-end border-top px-2">
								<input v-model="masterForm.bossnm" type="text" class="form-control form-control-sm" maxlength="30" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">법인번호</th>
							<td class="bg-white border-top px-2">
								<input v-model="masterForm.legalno" type="text" class="form-control form-control-sm" maxlength="14" placeholder="- 없이 입력" />
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">주 소</th>
							<td colspan="5" class="bg-white border-top px-2 py-1">
								<div class="d-flex gap-1 mb-1">
									<input v-model="masterForm.postno" type="text" class="form-control form-control-sm text-center bg-light" style="max-width: 80px;" readonly />
									<button class="btn btn-sm btn-outline-secondary px-2" @click="handleAddressSearch"><i class="bi bi-search"></i> 우편번호</button>
									<input v-model="masterForm.address" type="text" class="form-control form-control-sm" placeholder="기본 주소" />
								</div>
								<input v-model="masterForm.address_DET" type="text" class="form-control form-control-sm" placeholder="상세 주소" />
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">업태 / 종목</th>
							<td colspan="3" class="bg-white border-end border-top px-2">
								<div class="d-flex gap-2">
									<input v-model="masterForm.custtype" type="text" class="form-control form-control-sm" placeholder="업태" />
									<input v-model="masterForm.custkind" type="text" class="form-control form-control-sm" placeholder="종목" />
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">연락처</th>
							<td class="bg-white border-top px-2">
								<div class="d-flex gap-2">
									<input v-model="masterForm.telno" type="text" class="form-control form-control-sm" placeholder="전화번호" />
									<input v-model="masterForm.FAXNO" type="text" class="form-control form-control-sm" placeholder="FAX" />
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">금융정보</th>
							<td colspan="3" class="bg-white border-end border-top px-2">
								<div class="d-flex gap-2">
									<div class="input-group input-group-sm" style="width: 40%;">
										<span class="input-group-text">은행</span>
										<input v-model="masterForm.banknm" type="text" class="form-control" />
									</div>
									<div class="input-group input-group-sm" style="width: 60%;">
										<span class="input-group-text">계좌번호</span>
										<input v-model="masterForm.gujoa" type="text" class="form-control" />
									</div>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">기타구분</th>
							<td class="bg-white border-top px-2">
								<div class="d-flex gap-2">
									<select v-model="masterForm.iogbn" class="form-select form-select-sm">
										<option v-for="opt in ioGbnOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
									</select>
									<select v-model="masterForm.singrd" class="form-select form-select-sm" style="width: 80px;">
										<option value="A">A등급</option>
										<option value="B">B등급</option>
										<option value="C">C등급</option>
									</select>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">적용 / 유효일</th>
							<td colspan="3" class="bg-white border-end border-top px-2">
								<div class="d-flex align-items-center gap-2">
									<input v-model="masterForm.stdymd" type="date" class="form-control form-control-sm" style="width: 150px;" />
									<span class="text-muted">~</span>
									<input v-model="masterForm.clsymd" type="date" class="form-control form-control-sm" style="width: 150px;" />
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">상태 / 옵션</th>
							<td class="bg-white border-top px-3">
								<div class="d-flex align-items-center gap-4 h-100">
									<select v-model="masterForm.status" class="form-select form-select-sm" style="width: 100px;">
										<option v-for="opt in statusOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
									</select>
									<div class="form-check pt-1">
										<input v-model="masterForm.elcyn" class="form-check-input" type="checkbox" id="elcCheck" true-value="Y" false-value="N">
										<label class="form-check-label small fw-bold" for="elcCheck">전자세금계산서</label>
									</div>
									<div class="form-check pt-1">
										<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="useCheck" true-value="Y" false-value="N">
										<label class="form-check-label small fw-bold" for="useCheck">사용</label>
									</div>
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
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const today = new Date().toISOString().substring(0, 10)

// 🔍 검색 데이터
const searchForm = reactive({
	Qcustgbn: '000',
	Qstatus: '010',
	Qcustnm: ''
})

// 📝 마스터 데이터
const masterForm = reactive({
	actkind: 'I0',
	custcd: '',
	custnm: '',
	custno: '',
	custgbn: '010',
	bossnm: '',
	legalno: '',
	postno: '',
	address: '',
	address_DET: '',
	custtype: '',
	custkind: '',
	telno: '',
	FAXNO: '',
	banknm: '',
	gujoa: '',
	stdymd: today,
	clsymd: '',
	singrd: 'A',
	elcyn: 'N',
	iogbn: '010',
	status: '010',
	useyn: 'Y'
})

// 옵션 데이터
const custGbnOptions = ref<any[]>([])
const statusOptions = ref<any[]>([])
const ioGbnOptions = ref<any[]>([])

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

// 🚀 초기 정보 로드
const loadInitData = async () => {
	try {
		// 거래처 종류 (270)
		const resGbn = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '270' })
		custGbnOptions.value = (resGbn.data || []).map((r: any) => ({ CD: r.col0, NM: r.col1 }))

		// 상태 (280)
		const resStatus = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '280' })
		statusOptions.value = (resStatus.data || []).map((r: any) => ({ CD: r.col0, NM: r.col1 }))

		// 입출구분 (340)
		const resIo = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '340' })
		ioGbnOptions.value = (resIo.data || []).map((r: any) => ({ CD: r.col0, NM: r.col1 }))
	} catch (e) { console.error('초기 데이터 로드 실패') }
}

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_180U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			Qcustgbn: searchForm.Qcustgbn,
			Qstatus: searchForm.Qstatus,
			Qcustnm: searchForm.Qcustnm
		})

		const processedData = (res.data || []).map((r: any) => {
			// 날짜 포맷팅 (yyyymmDD -> yyyyymm-DD)
			const fmtDate = (v: string) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v

			return {
				...r,
				custcd: r.col0,
				custno_RAW: r.col1,
				custnm: r.col2,
				custgbn: r.col3,
				bossnm: r.col4,
				legalno: r.col6,
				custkind: r.col7,
				custtype: r.col8,
				telno: r.COL9,
				FAXNO: r.col10,
				postno: r.col11,
				address: r.col12,
				fndymd: fmtDate(r.col14),
				stdymd: fmtDate(r.col15),
				clsymd: fmtDate(r.col16),
				singrd: r.col17,
				useyn: r.col18,
				status: r.col19,
				iogbn: r.col20,
				gujoa: r.col21,
				banknm: r.col22,
				iogbn_NM: r.col23,
				elcyn: r.col24,
				// 사업자번호 포맷팅 (ASP 로직 반영)
				custno: r.col3 === '010'
					? `${r.col1.substring(0,3)}-${r.col1.substring(3,5)}-${r.col1.substring(5)}`
					: `${r.col1.substring(0,6)}-${r.col1.substring(6)}`
			}
		})

		mainGrid?.setData(processedData)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

// 사업자번호 유효성 검사 (ASP checkBizID 로직)
const validateBizID = (bizID: string) => {
	const id = bizID.replace(/-/g, '')
	if (id.length !== 10) return false
	const checkID = [1, 3, 7, 1, 3, 7, 1, 3, 5]
	let chkSum = 0
	for (let i = 0; i < 9; i++) chkSum += checkID[i] * Number(id.charAt(i))
	chkSum += Math.floor((checkID[8] * Number(id.charAt(8))) / 10)
	const remander = (10 - (chkSum % 10)) % 10
	return Number(id.charAt(9)) === remander
}

const save = async () => {
	if (!masterForm.custnm) return vAlert('상호를 입력하십시오.')

	// 사업자번호 체크 (ASP 로직 반영)
	if (masterForm.custgbn === '010' && masterForm.iogbn !== '040') {
		const id = masterForm.custno.replace(/-/g, '')
		if (!id) return vAlert('사업자등록번호를 입력하십시오.')
		if (!validateBizID(id)) return vAlert('정상적인 사업자등록번호가 아닙니다.')
	}

	try {
		const payload = {
			...masterForm,
			actkind: masterForm.custcd ? 'U0' : 'I0',
			cmpycd: authStore.cmpycd,
			userid: authStore.userid,
			// 날짜 특수문자 제거
			stdymd: masterForm.stdymd.replace(/-/g, ''),
			clsymd: masterForm.clsymd.replace(/-/g, ''),
			custno: masterForm.custno.replace(/-/g, ''),
			legalno: masterForm.legalno.replace(/-/g, '')
		}

		const res = await api.post('/api/haba/HABA_180U_STR', payload)

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].RET_MSG)
		} else {
			vAlert('저장되었습니다.')
			search()
			initialize()
		}
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.actkind = 'I0'
	masterForm.custgbn = '010'
	masterForm.iogbn = '010'
	masterForm.status = '010'
	masterForm.useyn = 'Y'
	masterForm.stdymd = today
}

// 주소 검색 (다음 우편번호 API)
const handleAddressSearch = () => {
	// @ts-ignore
	new window.daum.Postcode({
		oncomplete: (data: any) => {
			masterForm.postno = data.zonecode
			masterForm.address = data.roadAddress || data.jibunAddress
			masterForm.address_DET = ''
		}
	}).open()
}

const excel = () => mainGrid?.download("xlsx", `거래처현황_${today}.xlsx`)
const print = () => window.open(`/api/haba/HABA_180P?ymD=${today}`)

// 팝업 설정 (필요시)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: '' },
			columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { masterForm.deptcd = d.deptcd; masterForm.deptnm = d.deptnm }
		})
		modalVisible.value = true
	}
}

onMounted(() => {
	loadInitData()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "거래처", field: "custcd", width: 80, hozAlign: "center" },
				{ title: "상호", field: "custnm", minWidth: 200 },
				{ title: "사업자번호", field: "custno", width: 120, hozAlign: "center" },
				{ title: "대표자", field: "bossnm", width: 100, hozAlign: "center" },
				{ title: "연락처", field: "telno", width: 120 },
				{ title: "구분", field: "iogbn_NM", width: 100, hozAlign: "center" },
				{ title: "사용", field: "useyn", width: 60, hozAlign: "center", formatter: "tickCross" }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				Object.assign(masterForm, d)
				masterForm.actkind = 'U0'
				// ASP 로직상 주소는 상세주소 포함일 수 있어 필요시 분리 로직 추가
			}
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.bg-light-subtle { background-color: #f8f9fa !important; }
:deep(.tabulator-row) { cursor: pointer; }
</style>