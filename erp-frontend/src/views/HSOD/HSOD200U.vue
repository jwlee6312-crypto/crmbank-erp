<!--영업관리/출고관리/출고확정-->
<!--
	=============================================================
	프로그램명	  : 출고확정
  프로그램 ID	: HESB030U
	작성일자	    : 25.05.14
	작성자	      : 박현목
	수정일자     : 25.10.28
	수정자	      : 이현준
	수정 내용    : 자잘한 수정
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<div class="mb-2 btn-line">
		<button @click="initialize">초기화</button>
		<button @click="search">조회</button>
		<button @click="save">저장</button>
	</div>
	<table class="table shadow-sm" style="table-layout: fixed">
		<colgroup>
			<col style="width: 10%" />
			<col />
			<col style="width: 10%" />
			<col />
			<col style="width: 10%" />
			<col />
		</colgroup>
		<tbody>
			<tr>
				<th>출고일자</th>
				<td>
					<DateForm v-model:fromdt="form_01.fromdt" v-model:todt="form_01.todt" />
				</td>
				<th>확정구분</th>
				<td colspan="3">
					<select v-model="form_01.schcfmyn" class="form-select form-select-sm w-25">
						<option value="N">미확정</option>
						<option value="Y">확정</option>
					</select>
				</td>
			</tr>
		</tbody>
	</table>
	<table class="table shadow-sm" style="table-layout: fixed">
		<colgroup>
			<col style="width: 10%" />
			<col />
			<col style="width: 10%" />
			<col />
			<col style="width: 10%" />
			<col />
		</colgroup>
		<tbody>
			<tr>
				<th class="required-label">출고부서</th>
				<td class="d-flex gap-2">
					<input
						v-model="form_02.deptnm"
						class="form-control form-control-sm w-100"
						maxlength="30"
						readonly
					/>
				</td>
				<th class="required-label">출고번호</th>
				<td>
					<input
						v-model="form_02.iono"
						class="form-control form-control-sm w-100"
						maxlength="12"
						readonly
					/>
				</td>
				<th class="required-label">출고일자</th>
				<td>
					<input
						v-model="form_02.ioymd"
						class="form-control form-control-sm w-100"
						maxlength="8"
						readonly
					/>
				</td>
				<th class="required-label">출고유형</th>
				<td>
					<input v-model="form_02.iotypenm" class="form-control form-control-sm w-100" readonly />
				</td>
			</tr>
			<tr>
				<th class="required-label">거래처</th>
				<td class="d-flex gap-2">
					<input
						v-model="form_02.custnm"
						class="form-control form-control-sm w-100"
						maxlength="100"
						readonly
					/>
				</td>
				<th class="required-label">출고창고</th>
				<td>
					<input
						v-model="form_02.whnm"
						class="form-control form-control-sm w-100"
						maxlength="3"
						readonly
					/>
				</td>
				<th class="required-label">출고금액</th>
				<td>
					<input
						:value="formattedSum"
						class="form-control form-control-sm w-100 text-end"
						maxlength="15"
						readonly
					/>
				</td>
				<th class="required-label">확정여부</th>
				<td>
					<input
						v-model="form_02.cfmyn"
						true-value="Y"
						false-value="N"
						type="checkbox"
						class="form-check-input checkbox"
						maxlength="1"
					/>
				</td>
			</tr>
			<tr>
				<th>특이사항</th>
				<td colspan="7">
					<input
						v-model="form_02.remark"
						class="form-control form-control-sm w-100"
						maxlength="100"
						readonly
					/>
				</td>
			</tr>
		</tbody>
	</table>
	<div class="two-grid-half tabulator-ref">
		<div ref="tableRef1" />
		<div ref="tableRef2" />
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { type RowComponent, TabulatorFull as Tabulator } from 'tabulator-tables'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import DateForm from '@/components/DateForm.vue'
import { useSearch } from '@/composables/useSearch'
import { useSave } from '@/composables/useSave'
import { useRoute } from 'vue-router'
import { useSearchStore } from '@/stores/useSearchStore'
import { useFormReset } from '@/composables/useFormReset'
import { formatNumber } from '@/composables/useFormatUtils'
import { getDate } from '@/composables/useDate'

const route = useRoute()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { searchStart } = useSearch()
const { saveBody } = useSave()
const searchStore = useSearchStore()
const { resetForm } = useFormReset()
const { today, firstDay } = getDate()

// Grid
const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let tableInstance1: Tabulator | null = null
let tableInstance2: Tabulator | null = null

interface Form_01 {
	fromdt: string
	todt: string
	schcfmyn: string
}

const form_01 = reactive(<Form_01>{
	fromdt: firstDay,
	todt: today,
	schcfmyn: 'N',
})

interface Form_02 {
	deptcd: string
	deptnm: string
	iono: string
	ioymd: string
	custcd: string
	custnm: string
	whcd: string
	sumamt: number
	remark: string
	cfmyn: string
	iotype: string
	iotypenm: string
	whnm: string
}

const form_02 = reactive<Form_02>({
	deptcd: '',
	deptnm: '',
	iono: '',
	ioymd: today,
	custcd: '',
	custnm: '',
	whcd: '100',
	sumamt: 0,
	remark: '',
	cfmyn: 'N',
	iotype: '',
	iotypenm: '',
	whnm: '',
})

const formattedSum = computed(() => formatNumber(form_02.sumamt))

// ✅ 그리드 데이터 상태 관리
// Grid 구성
onMounted(async () => {
	if (!tableRef1.value || !tableRef2.value) return // 그리드 두개 있을때
	// ✅ 그리드 1
	tableInstance1 = new Tabulator(tableRef1.value, {
		placeholder: 'No data',
		layout: 'fitColumns',
		data: [],
		columns: [
			{
				title: '출고번호',
				field: 'iono',
				hozAlign: 'center',
			},
			{
				title: '출고일',
				field: 'ioymd',
				hozAlign: 'center',
				headerSort: false,
			},
			{
				title: '거래처',
				field: 'custnm',
				hozAlign: 'center',
				headerSort: false,
			},
			{
				title: '출고유형',
				field: 'iotypenm',
				hozAlign: 'center',
				headerSort: false,
			},
			{
				title: '출고금액',
				field: 'sumamt',
				hozAlign: 'right',
				headerSort: false,
				formatter: 'money',
				formatterParams: { thousand: ',', precision: false },
			},
			{
				title: '출고창고',
				field: 'whnm',
				hozAlign: 'center',
				headerSort: false,
			},
			{
				title: '확정여부',
				field: 'cfmyn',
				hozAlign: 'center',
				headerSort: false,
			},
		],
	})

	// ✅ 그리드 2
	tableInstance2 = new Tabulator(tableRef2.value, {
		layout: 'fitColumns',
		placeholder: 'No data',
		data: [],
		columns: [
			{
				title: '품목',
				field: 'itemnm',
				hozAlign: 'center',
				headerSort: false,
			},
			{
				title: '규격',
				field: 'itsize',
				hozAlign: 'center',
				headerSort: false,
			},
			{
				title: '단위',
				field: 'unit',
				hozAlign: 'center',
				headerSort: false,
			},
			{
				title: '출고수량',
				field: 'ioqty',
				hozAlign: 'center',
				headerSort: false,
				formatter: 'money',
				formatterParams: { thousand: ',', precision: false },
			},
			{
				title: '부가세',
				field: 'iovat',
				hozAlign: 'right',
				headerSort: false,
				formatter: 'money',
				formatterParams: { thousand: ',', precision: false },
			},
			{
				title: '합계',
				field: 'totamt',
				hozAlign: 'right',
				headerSort: false,
				formatter: 'money',
				formatterParams: { thousand: ',', precision: false },
			},
		],
	})

	tableInstance1.on('rowClick', onClickRef1) // 행클릭 이벤트
})

// ✅ 조회 버튼
async function search() {
	if (!tableInstance1) return
	const path = '/sales-order-out-confirm/search-left'
	const params = {
		fromdt: form_01.fromdt.replace(/-/g, ''),
		todt: form_01.todt.replace(/-/g, ''),
		schcfmyn: form_01.schcfmyn,
	}

	try {
		const { data, message } = await searchStart(path, params)
		await tableInstance1.setData(data)
		return vAlert(message)
	} catch (error: any) {
		console.log(error)
		const message =
			error?.response?.data?.message ||
			error?.message ||
			'출고확정 거래처 정보 조회에 실패했습니다.'
		return vAlertError(message)
	}
}

// ✅ 왼쪽 그리드 클릭 이벤트
async function onClickRef1(e: UIEvent, row: RowComponent) {
	if (!tableInstance1 || !tableInstance2) return

	const rowData = row.getData()
	const path = '/sales-order-out-confirm/search-right' // 왼쪽그리드 클릭시 조회 api
	const param = {
		iono: rowData.iono,
	}

	// 피니아 저장
	searchStore.saveTab(route.name as string, path, param) // 클릭한 데이터를 피니아에 저장

	try {
		const { data, message } = await searchStart(path, param)
		Object.assign(form_02, rowData)
		await tableInstance2.setData(data)
		tableInstance2.selectRow()
		return vAlert(message)
	} catch (error: any) {
		console.log(error)
		const message =
			error?.response?.data?.message ||
			error?.message ||
			'출고확정 상세품목 정보 조회에 실패했습니다.'
		return vAlertError(message)
	}
}

// ✅ 저장 버튼
async function save() {
	// if (form_02.custcd === '') {
	// 	vAlertError('출고 할 정보가 없습니다. ')
	// 	return
	// }

	if (form_01.schcfmyn === 'N' && form_02.cfmyn === 'N') {
		return vAlertError('확정여부를 체크해주세요.')
	}

	if (form_01.schcfmyn === 'Y' && form_02.cfmyn === 'Y') {
		return vAlertError('확정여부를 체크해제 해주세요.')
	}

	const path = '/sales-order-out-confirm/save'
	const payload = {
		...form_02,
	}

	try {
		const { message } = await saveBody(path, payload)
		await search()
		resetForm(form_02)
		tableInstance2?.clearData()
		return vAlert(message)
	} catch (error: any) {
		console.log(error)
		const message =
			error?.response?.data?.message || error?.message || '출고확정 처리에 실패했습니다.'
		return vAlertError(message)
	}
}

function initialize() {
	form_01.fromdt = firstDay
	form_01.todt = today
	form_01.schcfmyn = 'N'
	resetForm(form_02)
	form_02.ioymd = today
	form_02.whcd = '100'
	form_02.cfmyn = 'Y'

	tableInstance1?.clearData()
	tableInstance2?.clearData()

	return vAlert('초기화 완료')
}

// 탭 종료시 피니아 데이터 삭제
onUnmounted(() => {
	searchStore.removeTab(route.name as string)
})
</script>
