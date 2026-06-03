<!--	=============================================================
	프로그램명	: 매입부가세관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 사업장별 매입 부가세 내역 조회 및 관리 (전표 연동 포함)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi- receipt me-2 text-primary" style="font-size: 18px;"></i>
				세무관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">매입부가세관리 (HATX010U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-new" @click="handleNew">
					<i class="bi bi-plus-lg"></i> 신규
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>사업장</span>
							<select v-model="searchForm.taxunit" class="form-select form-select-sm" style="width: 150px;">
								<option v-for="opt in taxUnitOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
							</select>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>유형</span>
							<select v-model="searchForm.gubun" class="form-select form-select-sm" style="width: 120px;">
								<option value="000">전체</option>
								<option v-for="opt in taxTypeOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
							</select>
						</div>
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>발행일</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.frymd" type="date" class="form-control form-control-sm" style="width: 135px;" />
								<span>~</span>
								<input v-model="searchForm.toymd" type="date" class="form-control form-control-sm" style="width: 135px;" />
							</div>
						</div>
					</div>
				</div>
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

	<!-- 📝 상세 입력 팝업 (HATX010I 역할) -->
	<Modal v-model:visible="detailModalVisible" :title="'매입부가세 상세정보'" size="xl">
		<template #body>
			<div class="p-1 small">
				<div class="row g-2 mb-3">
					<div class="col-md-3">
						<div class="d-flex align-items-center">
							<span class="erp-label-form required">사업장</span>
							<select v-model="formData.taxunit" class="form-select form-select-sm">
								<option v-for="opt in taxUnitOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
							</select>
						</div>
					</div>
					<div class="col-md-3">
						<div class="d-flex align-items-center">
							<span class="erp-label-form required">거래처</span>
							<div class="input-group input-group-sm">
								<input v-model="formData.custcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
								<input v-model="formData.custnm" type="text" class="form-control" @keydown.enter="openHelp('CUST')" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
							</div>
						</div>
					</div>
					<div class="col-md-3">
						<div class="d-flex align-items-center">
							<span class="erp-label-form">사업자번호</span>
							<input v-model="formData.custno" type="text" class="form-control form-control-sm bg-light" readonly />
						</div>
					</div>
					<div class="col-md-3 text-end">
						<div v-if="formData.SSLIPNO" class="badge bg-info p-2 w-100">전표: {{ formData.SSLIPNO }}</div>
					</div>

					<div class="col-md-3">
						<div class="d-flex align-items-center">
							<span class="erp-label-form required">발행일</span>
							<input v-model="formData.pubymd" type="date" class="form-control form-control-sm" />
						</div>
					</div>
					<div class="col-md-3">
						<div class="d-flex align-items-center">
							<span class="erp-label-form required">유형</span>
							<select v-model="formData.TAXTYPE_VAL" class="form-select form-select-sm" @change="handleTaxTypeChange">
								<option value="0">선택</option>
								<option v-for="opt in taxTypeOptions" :key="opt.code" :value="opt.code + '|' + opt.name">{{ opt.name }}</option>
							</select>
						</div>
					</div>
					<div class="col-md-3">
						<div class="d-flex align-items-center">
							<span class="erp-label-form">공급가</span>
							<input v-model="formData.supyamt" type="text" class="form-control form-control-sm text-end bg-light" readonly />
						</div>
					</div>
					<div class="col-md-3">
						<div class="d-flex align-items-center">
							<span class="erp-label-form">부가세</span>
							<input v-model="formData.vatamt" type="text" class="form-control form-control-sm text-end bg-light" readonly />
						</div>
					</div>

					<div class="col-md-3">
						<div class="d-flex align-items-center">
							<span class="erp-label-form">대표자</span>
							<input v-model="formData.bossnm" type="text" class="form-control form-control-sm bg-light" readonly />
						</div>
					</div>
					<div class="col-md-6">
						<div class="d-flex align-items-center">
							<span class="erp-label-form required">적요</span>
							<input v-model="formData.descnm" type="text" class="form-control form-control-sm" maxlength="50" />
						</div>
					</div>
					<div class="col-md-3">
						<div class="d-flex align-items-center">
							<span class="erp-label-form">합계</span>
							<input v-model="formData.amtsum" type="text" class="form-control form-control-sm text-end bg-light fw-bold" readonly />
						</div>
					</div>

					<div class="col-md-6">
						<div class="d-flex align-items-center">
							<span class="erp-label-form">업태/종목</span>
							<div class="d-flex gap-1 flex-grow-1">
								<input v-model="formData.custtype" type="text" class="form-control form-control-sm" placeholder="업태" />
								<input v-model="formData.custkind" type="text" class="form-control form-control-sm" placeholder="종목" />
							</div>
						</div>
					</div>
					<div class="col-md-6">
						<div class="d-flex align-items-center">
							<span class="erp-label-form">주소</span>
							<input v-model="formData.ADRS" type="text" class="form-control form-control-sm" />
						</div>
					</div>

					<div v-if="showNonDeductible" class="col-12">
						<div class="d-flex align-items-center bg-warning-subtle p-2 rounded">
							<span class="erp-label-form fw-bold text-danger">불공제사유</span>
							<select v-model="formData.BGONGCD" class="form-select form-select-sm">
								<option value="01">필요적기재사항누락</option>
								<option value="02">사업과 직접 관련 없는 지출</option>
								<option value="03">비영업용 소형승용차 구입·유지 및 임차</option>
								<option value="04">접대비 및 이와 유사한 비용 관련</option>
								<option value="05">면세사업 관련</option>
								<option value="06">토지의 자본적 지출 관련</option>
								<option value="07">사업자등록 전 매입세액</option>
								<option value="08">금거래계좌 미사용 관련 매입세액</option>
							</select>
						</div>
					</div>
				</div>

				<!-- 📋 품목 세부 내역 -->
				<div class="table-responsive mb-3 border rounded">
					<table class="table table-sm table-bordered mb-0 align-middle">
						<thead class="table-light text-center">
							<tr style="font-size: 11px;">
								<th style="width: 120px;">일자</th>
								<th>품명</th>
								<th style="width: 60px;">수량</th>
								<th style="width: 90px;">단가</th>
								<th style="width: 100px;">공급가</th>
								<th style="width: 100px;">세액</th>
								<th>비고</th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="(item, idx) in formData.items" :key="idx">
								<td><input v-model="item.ymD" type="date" class="form-control form-control-sm border-0 px-1" /></td>
								<td class="d-flex align-items-center gap-1">
									<input v-model="item.itemnm" type="text" class="form-control form-control-sm border-0 px-1" />
									<button class="btn btn-xs btn-outline-secondary p-0" @click="openHelp('ITEM', idx)" style="width:20px;height:20px;"><i class="bi bi-search" style="font-size:10px;"></i></button>
								</td>
								<td><input v-model="item.QTY" type="number" class="form-control form-control-sm border-0 px-1 text-end" @input="calculateItem(idx)" /></td>
								<td><input v-model="item.price" type="number" class="form-control form-control-sm border-0 px-1 text-end" @input="calculateItem(idx)" /></td>
								<td><input v-model="item.AMT" type="text" class="form-control form-control-sm border-0 px-1 text-end bg-light" readonly /></td>
								<td><input v-model="item.VAT" type="text" class="form-control form-control-sm border-0 px-1 text-end bg-light" readonly /></td>
								<td><input v-model="item.bigo" type="text" class="form-control form-control-sm border-0 px-1" /></td>
							</tr>
						</tbody>
					</table>
				</div>

				<!-- 💳 전표 생성 정보 -->
				<div class="card erp-slip-card border-info-subtle mb-0 shadow-none">
					<div class="card-header bg-info-subtle p-2 d-flex justify-content-between align-items-center shadow-none border-info-subtle">
						<div class="form-check mb-0">
							<input v-model="formData.SLIPYN" class="form-check-input border-primary" type="checkbox" id="slipCheck" :disabled="!!formData.SSLIPNO" true-value="Y" false-value="N">
							<label class="form-check-label fw-bold text-primary" for="slipCheck">전표 생성 정보</label>
						</div>
						<div class="form-check mb-0">
							<input v-model="formData.useyn" class="form-check-input" type="checkbox" id="receiptCheck" true-value="Y" false-value="N">
							<label class="form-check-label fw-bold text-success" for="receiptCheck">접수확인</label>
						</div>
					</div>
					<div v-if="formData.SLIPYN === 'Y'" class="card-body p-3 bg-white border border-top-0 border-info-subtle rounded-bottom">
						<div class="row g-2 small">
							<div class="col-md-4">
								<div class="d-flex align-items-center">
									<span class="erp-label-form required">발행부서</span>
									<div class="input-group input-group-sm">
										<input v-model="formData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="formData.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</div>
							<div class="col-md-4">
								<div class="d-flex align-items-center">
									<span class="erp-label-form required">매입계정</span>
									<div class="input-group input-group-sm">
										<input v-model="formData.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
										<input v-model="formData.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT_PURCHASE')" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('ACCT_PURCHASE')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</div>
							<div class="col-12 mt-2">
								<table class="table table-sm table-bordered mb-0 bg-light">
									<thead class="text-center bg-secondary-subtle">
										<tr>
											<th>지불계정</th><th>지불액</th><th>관리번호</th><th>발행일</th><th>만기일</th>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td>
												<select v-model="formData.CPAYCNDT1" class="form-select form-select-sm" @change="handlePaymentChange(1)">
													<option value="000">선택</option>
													<option v-for="opt in paymentOptions" :key="opt.VAL" :value="opt.VAL">{{ opt.name }}</option>
												</select>
											</td>
											<td><input v-model="formData.CInamt1" type="number" class="form-control form-control-sm text-end" /></td>
											<td>
												<div class="input-group input-group-sm">
													<input v-model="formData.Cmgtno1" type="text" class="form-control" @keydown.enter="openHelp('MGT', 1)" />
													<button class="btn btn-outline-secondary px-2" @click="openHelp('MGT', 1)"><i class="bi bi-search"></i></button>
												</div>
											</td>
											<td><input v-model="formData.Cstdymd1" type="date" class="form-control form-control-sm" /></td>
											<td><input v-model="formData.Cendymd1" type="date" class="form-control form-control-sm" /></td>
										</tr>
										<tr>
											<td>
												<select v-model="formData.CPAYCNDT2" class="form-select form-select-sm" @change="handlePaymentChange(2)">
													<option value="000">선택</option>
													<option v-for="opt in paymentOptions" :key="opt.VAL" :value="opt.VAL">{{ opt.name }}</option>
												</select>
											</td>
											<td><input v-model="formData.CInamt2" type="number" class="form-control form-control-sm text-end" /></td>
											<td>
												<div class="input-group input-group-sm">
													<input v-model="formData.Cmgtno2" type="text" class="form-control" @keydown.enter="openHelp('MGT', 2)" />
													<button class="btn btn-outline-secondary px-2" @click="openHelp('MGT', 2)"><i class="bi bi-search"></i></button>
												</div>
											</td>
											<td><input v-model="formData.Cstdymd2" type="date" class="form-control form-control-sm" /></td>
											<td><input v-model="formData.Cendymd2" type="date" class="form-control form-control-sm" /></td>
										</tr>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
			</div>
		</template>
		<template #footer>
			<div class="d-flex justify-content-between w-100">
				<div class="btn-group gap-1">
					<button v-if="formData.SSLIPNO" class="btn btn-sm btn-print" @click="handleSlipPrint">
						<i class="bi bi-printer"></i> 전표인쇄
					</button>
					<button v-if="formData.SSLIPNO" class="btn btn-sm btn-danger" @click="handleSlipDelete">
						<i class="bi bi-trash"></i> 전표삭제
					</button>
				</div>
				<div class="btn-group gap-1">
					<button class="btn btn-sm btn-primary" @click="save">
						<i class="bi bi-check-lg"></i> 저장
					</button>
					<button v-if="formData.taxno" class="btn btn-sm btn-danger" @click="handleTaxDelete">
						<i class="bi bi-trash"></i> 삭제
					</button>
					<button class="btn btn-sm btn-secondary" @click="detailModalVisible = false">
						닫기
					</button>
				</div>
			</div>
		</template>
	</Modal>

	<!-- 팝업 공통 모달 -->
	<Modal v-model:visible="commonModalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const today = new Date().toISOString().slice(0, 10)
const firstDay = today.slice(0, 8) + '01'

// --- 옵션 데이터 ---
const taxUnitOptions = ref<any[]>([])
const taxTypeOptions = ref<any[]>([])
const paymentOptions = ref<any[]>([])

const searchForm = reactive({
	taxunit: '',
	gubun: '000',
	frymd: firstDay,
	toymd: today
})

// --- 폼 데이터 ---
const formData = reactive({
	taxunit: '',
	custcd: '', custnm: '', custno: '',
	taxno: '', taxym: '',
	pubymd: today,
	TAXTYPE_VAL: '0',
	TAXTYPE: '', typenm: '',
	supyamt: '0', vatamt: '0', amtsum: '0',
	bossnm: '', descnm: '', custtype: '', custkind: '', ADRS: '',
	BGONGCD: '01',
	useyn: 'N', SLIPYN: 'N', SSLIPNO: '',
	deptcd: authStore.deptcd || '', deptnm: authStore.deptnm || '',
	acctcd: '', acctnm: '',
	CPAYCNDT1: '000', CInamt1: 0, Cmgtno1: '', Cmgtnm1: '', Cmgtgbn1: '000', Cacctcd1: '', CROWACT1: 'A', Cstdymd1: today, Cendymd1: today, CPAYTYPE1: '000', CPAyyMD1: today,
	CPAYCNDT2: '000', CInamt2: 0, Cmgtno2: '', Cmgtnm2: '', Cmgtgbn2: '000', Cacctcd2: '', CROWACT2: 'A', Cstdymd2: today, Cendymd2: today, CPAYTYPE2: '000', CPAyyMD2: today,
	items: Array.from({ length: 4 }, () => ({
		rowno: '', ymD: today, itemnm: '', QTY: 0, price: 0, AMT: '0', VAT: '0', bigo: ''
	}))
})

const showNonDeductible = ref(false)
const detailModalVisible = ref(false)

// --- 그리드 ---
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const fetchOptions = async () => {
	try {
		const [resU, resT, resP] = await Promise.all([
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd, search: ' ' }),
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '120' }),
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, search: '190' })
		])
		taxUnitOptions.value = resU.data || []
		if (taxUnitOptions.value.length > 0) searchForm.taxunit = taxUnitOptions.value[0].code

		taxTypeOptions.value = resT.data || []
		paymentOptions.value = (resP.data || []).map((o: any) => ({
			VAL: `${o.code}${o.col3}${o.col2}`, // 지불수단 + 관리번호유형 + 계정과목
			name: o.name,
			mgtgbn: o.col3,
			acctcd: o.col2,
			code: o.code
		}))
	} catch (e) { console.error(e) }
}

const search = async () => {
	if (!searchForm.taxunit) return vAlertError('사업장을 선택하세요.')

	try {
		const res = await api.post('/api/hatx/HATX_010U_STR', {
			actkind: 'SR',
			cmpycd: authStore.cmpycd,
			taxunit: searchForm.taxunit,
			frymd: searchForm.frymd.replace(/-/g, ''),
			toymd: searchForm.toymd.replace(/-/g, ''),
			gubun: searchForm.gubun,
			taxkind: '100'
		})

		const data = (res.data || []).map((row: any) => ({
			pubymd: row.col0,
			custnm: row.col1,
			typenm: row.col3,
			supyamt: row.col4,
			vatamt: row.col5,
			amtsum: row.col6,
			descnm: row.col7,
			useyn: row.col8,
			cmpycd: row.COL9,
			taxkind: row.col10,
			taxym: row.col11,
			taxno: row.col12
		}))

		mainGrid?.setData(data)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const handleNew = () => {
	Object.assign(formData, {
		taxunit: searchForm.taxunit, custcd: '', custnm: '', custno: '', taxno: '', taxym: '',
		pubymd: today, TAXTYPE_VAL: '0', TAXTYPE: '', typenm: '', supyamt: '0', vatamt: '0', amtsum: '0',
		bossnm: '', descnm: '', custtype: '', custkind: '', ADRS: '', BGONGCD: '01', useyn: 'N', SLIPYN: 'N', SSLIPNO: '',
		deptcd: authStore.deptcd || '', deptnm: authStore.deptnm || '', acctcd: '', acctnm: '',
		CPAYCNDT1: '000', CInamt1: 0, CPAYCNDT2: '000', CInamt2: 0,
		items: Array.from({ length: 4 }, () => ({ rowno: '', ymD: today, itemnm: '', QTY: 0, price: 0, AMT: '0', VAT: '0', bigo: '' }))
	})
	showNonDeductible.value = false
	detailModalVisible.value = true
}

const handleTaxTypeChange = () => {
	if (formData.TAXTYPE_VAL === '0') {
		formData.TAXTYPE = ''; formData.typenm = ''; showNonDeductible.value = false
	} else {
		const [code, name] = formData.TAXTYPE_VAL.split('|')
		formData.TAXTYPE = code; formData.typenm = name
		showNonDeductible.value = code === '060' // 불공제
	}
}

const handlePaymentChange = (num: number) => {
	const val = (formData as any)['CPAYCNDT' + num]
	if (val === '000') return
	const opt = paymentOptions.value.find(o => o.VAL === val)
	if (opt) {
		(formData as any)['Cmgtgbn' + num] = opt.mgtgbn;
		(formData as any)['Cacctcd' + num] = opt.acctcd;
		(formData as any)['CPAYTYPE' + num] = opt.code;
		(formData as any)['CInamt' + num] = Number(formData.amtsum.replace(/,/g, ''));
	}
}

const calculateItem = (idx: number) => {
	const item = formData.items[idx]
	const amt = (item.QTY || 0) * (item.price || 0)
	const vat = Math.floor(amt * 0.1) // 기본 10%
	item.AMT = amt.toLocaleString()
	item.VAT = vat.toLocaleString()

	// 전체 합계 계산
	let totalSup = 0, totalVat = 0
	formData.items.forEach(it => {
		totalSup += Number(it.AMT.replace(/,/g, ''))
		totalVat += Number(it.VAT.replace(/,/g, ''))
	})
	formData.supyamt = totalSup.toLocaleString()
	formData.vatamt = totalVat.toLocaleString()
	formData.amtsum = (totalSup + totalVat).toLocaleString()
}

const save = async () => {
	if (!formData.custcd) return vAlertError('거래처를 선택하세요.')
	if (formData.TAXTYPE_VAL === '0') return vAlertError('유형을 선택하세요.')
	if (!formData.descnm.trim()) return vAlertError('적요를 입력하세요.')

	try {
		// 전표 생성 시 검증
		if (formData.SLIPYN === 'Y') {
			if (!formData.deptcd) return vAlertError('발행부서를 선택하세요.')
			if (!formData.acctcd) return vAlertError('매입계정을 선택하세요.')
			const inAmt = Number(formData.CInamt1) + Number(formData.CInamt2)
			const total = Math.abs(Number(formData.amtsum.replace(/,/g, '')))
			if (inAmt !== total) return vAlertError('매입액과 지불액이 일치하지 않습니다.')
		}

		const res = await api.post('/api/hatx/HATX_010U_STR', {
			...formData,
			actkind: formData.taxno ? 'U1' : 'I1',
			cmpycd: authStore.cmpycd,
			pubymd: formData.pubymd.replace(/-/g, ''),
			supyamt: formData.supyamt.replace(/,/g, ''),
			vatamt: formData.vatamt.replace(/,/g, ''),
			userid: authStore.userid,
			taxkind: '100',
			items: formData.items.filter(it => it.itemnm).map(it => ({
				...it,
				ymD: it.ymD.replace(/-/g, ''),
				AMT: it.AMT.replace(/,/g, ''),
				VAT: it.VAT.replace(/,/g, '')
			}))
		})

		vAlert('저장되었습니다.')
		detailModalVisible.value = false
		search()
	} catch (e) { vAlertError('저장 중 오류 발생') }
}

const handleTaxDelete = async () => {
	if (!confirm('해당 세금계산서를 삭제하시겠습니까?')) return
	try {
		await api.post('/api/hatx/HATX_010U_STR', {
			actkind: 'D1', cmpycd: authStore.cmpycd, taxkind: '100',
			taxym: formData.taxym, taxno: formData.taxno
		})
		vAlert('삭제되었습니다.')
		detailModalVisible.value = false
		search()
	} catch (e) { vAlertError('삭제 중 오류 발생') }
}

const handleSlipDelete = async () => {
	if (!confirm('해당 전표를 삭제하시겠습니까?')) return
	try {
		await api.post('/api/hatx/HATX_010U_STR', {
			SLIPACT: 'D', cmpycd: authStore.cmpycd,
			SSLIPNO: formData.SSLIPNO, deptcd: formData.deptcd, usernm: authStore.usernm, userid: authStore.userid
		})
		vAlert('전표가 삭제되었습니다.')
		detailModalVisible.value = false
		search()
	} catch (e) { vAlertError('전표 삭제 중 오류 발생') }
}

const handleSlipPrint = () => {
	if (!formData.SSLIPNO) return
	const ymd = formData.SSLIPNO.substring(0, 8)
	const no = formData.SSLIPNO.substring(9, 12)
	window.open(`/api/hasl/HASL_SLIP_PRINT?SLIPGU=010&slipymd=${ymd}&SLIPNO=${no}`, 'SlipPrint', 'width=800,height=700,scrollbars=yes')
}

const loadDetail = async (row: any) => {
	try {
		const res = await api.post('/api/hatx/HATX_010U_STR', {
			actkind: 'S2', cmpycd: row.cmpycd, taxkind: '100', taxym: row.taxym, taxno: row.taxno
		})
		if (res.data && res.data[0]) {
			const d = res.data[0]
			Object.assign(formData, {
				...d,
				pubymd: d.col12 ? `${d.col12.slice(0,4)}-${d.col12.slice(4,6)}-${d.col12.slice(6,8)}` : today,
				TAXTYPE_VAL: `${d.col13}|${d.col14}`,
				TAXTYPE: d.col13, typenm: d.col14,
				supyamt: Number(d.col15).toLocaleString(),
				vatamt: Number(d.col16).toLocaleString(),
				amtsum: (Number(d.col15) + Number(d.col16)).toLocaleString(),
				descnm: d.col17, useyn: d.col18,
				SSLIPNO: d.col19 > '00000000' ? `${d.col19}-${d.col20}-${d.col21}` : '',
				BGONGCD: d.col28 || '01', SLIPYN: 'N',
				custcd: d.col5, custnm: d.col7, custno: d.col6, bossnm: d.col8, ADRS: d.COL9, custkind: d.col10, custtype: d.col11
			})
			showNonDeductible.value = d.col13 === '060'

			// 세부 내역 로드 (S1)
			const resItems = await api.post('/api/hatx/HATX_011U_STR', {
				actkind: 'S1', cmpycd: row.cmpycd, taxkind: '100', taxym: row.taxym, taxno: row.taxno
			})
			const dbItems = resItems.data || []
			formData.items = Array.from({ length: 4 }, (_, i) => {
				const it = dbItems[i]
				return it ? {
					rowno: it.col0, itemnm: it.col1, QTY: it.col2, price: it.col3, AMT: Number(it.col4).toLocaleString(),
					VAT: Number(it.col5).toLocaleString(), bigo: it.col6,
					ymD: it.col7 ? `${it.col7.slice(0,4)}-${it.col7.slice(4,6)}-${it.col7.slice(6,8)}` : today
				} : { rowno: '', ymD: today, itemnm: '', QTY: 0, price: 0, AMT: '0', VAT: '0', bigo: '' }
			})

			detailModalVisible.value = true
		}
	} catch (e) { vAlertError('상세 정보 로드 중 오류 발생') }
}

// --- 팝업 설정 ---
const commonModalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, idx?: number) {
	if (type === 'CUST') {
		Object.assign(modalProps, {
			title: '거래처 선택', path: '/api/ha00/HELP_custcd_ALL_STR',
			data: { search: formData.custnm },
			columns: [{ title: '코드', field: 'custcd', width: 80 }, { title: '거래처명', field: 'custnm', width: 180 }, { title: '사업자번호', field: 'custno', width: 120 }],
			onConfirm: (d: any) => { formData.custcd = d.custcd; formData.custnm = d.custnm; formData.custno = d.custno; formData.bossnm = d.bossnm; formData.ADRS = d.ADRS; formData.custtype = d.custtype; formData.custkind = d.custkind }
		})
	} else if (type === 'ITEM' && idx !== undefined) {
		Object.assign(modalProps, {
			title: '품목 선택', path: '/api/ha00/HELP_ITEM_STR',
			data: { gubun: 'I1', search: formData.items[idx].itemnm },
			columns: [{ title: '코드', field: 'itemcd', width: 80 }, { title: '품목명', field: 'itemnm', width: 180 }],
			onConfirm: (d: any) => { formData.items[idx].itemnm = d.itemnm }
		})
	} else if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: formData.deptnm },
			columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { formData.deptcd = d.deptcd; formData.deptnm = d.deptnm }
		})
	} else if (type === 'ACCT_PURCHASE') {
		Object.assign(modalProps, {
			title: '계정과목 선택', path: '/api/ha00/HELP_acctcd_LTD_STR',
			data: { ACCT: '13', search: formData.acctnm }, // ASP: acct=13
			columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 180 }],
			onConfirm: (d: any) => { formData.acctcd = d.acctcd; formData.acctnm = d.acctnm }
		})
	} else if (type === 'MGT' && idx !== undefined) {
		const mgtgbn = (formData as any)['Cmgtgbn' + idx]
		const acctcd = (formData as any)['Cacctcd' + idx]
		if (!mgtgbn || mgtgbn === '000') return vAlertError('관리번호를 관리하지 않는 계정입니다.')
		Object.assign(modalProps, {
			title: '관리번호 선택', path: '/api/ha00/HELP_mgtno_STR',
			data: { mgtgbn: mgtgbn, acctcd: acctcd, search: (formData as any)['Cmgtno' + idx] },
			columns: [{ title: '번호', field: 'mgtno', width: 120 }, { title: '명칭', field: 'mgtnm', width: 180 }],
			onConfirm: (d: any) => { (formData as any)['Cmgtno' + idx] = d.mgtno }
		})
	}
	commonModalVisible.value = true
}

onMounted(() => {
	fetchOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "발행일", field: "pubymd", width: 100, hozAlign: "center",
					formatter: (cell) => {
						const val = cell.getValue();
						return val ? `${val.slice(0,4)}-${val.slice(4,6)}-${val.slice(6,8)}` : '';
					}
				},
				{ title: "상호", field: "custnm", width: 180, cssClass: "text-primary fw-bold", cellClick: (e, cell) => loadDetail(cell.getData()) },
				{ title: "유형", field: "typenm", width: 100, hozAlign: "center" },
				{ title: "공급가", field: "supyamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "부가세", field: "vatamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "합계", field: "amtsum", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" },
				{ title: "적요", field: "descnm", widthGrow: 1 },
				{ title: "접수", field: "useyn", width: 60, hozAlign: "center", formatter: "tickCross", formatterParams: { trues: "Y", falses: "N" } }
			]
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.erp-label-form { min-width: 100px; font-weight: 500; font-size: 12px; color: #555; }
.erp-label-form.required::after { content: " *"; color: red; }
.btn-xs { padding: 1px 5px; font-size: 10px; }
.erp-slip-card { border: 1px dashed #0dcaf0; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; font-size: 12px; cursor: pointer; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; font-size: 12px; }
</style>
