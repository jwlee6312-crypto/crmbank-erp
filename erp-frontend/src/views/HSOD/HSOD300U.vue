<!-- 구매관리/입고관리/입고확정 -->
<!--
	=============================================================
	Program     : 입고확정
	Program ID  : HEPC040U
	Create-Date : 25.05.29
	Creator     : 이현준
	Modify-Date : 25.12.18
	Modifier    : 이현준
	History     : 세트상품 입고시 거래처 체크 여부 수정
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
		</colgroup>
		<tbody>
			<tr>
				<th>입고일자</th>
				<td>
					<div class="d-flex gap-2 align-items-center w-100">
						<DateForm v-model:fromdt="form_01.fromdt" v-model:todt="form_01.todt" />
					</div>
				</td>
				<th>확정구분</th>
				<td>
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
				<th class="required-label">입고부서</th>
				<td>
					<div class="d-flex gap-2">
						<input
							v-model="form_02.deptnm"
							class="form-control form-control-sm w-100"
							maxlength="30"
							readonly
						/>
					</div>
				</td>
				<th class="required-label">입고번호</th>
				<td>
					<input
						v-model="form_02.iono"
						class="form-control form-control-sm w-100"
						maxlength="12"
						readonly
					/>
				</td>
				<th class="required-label">입고일자</th>
				<td>
					<input
						v-model="form_02.ioymd"
						type="date"
						class="form-control form-control-sm w-100"
						maxlength="8"
					/>
				</td>
				<th class="required-label">입고유형</th>
				<td>
					<input v-model="form_02.iotypenm" class="form-control form-control-sm w-100" readonly />
				</td>
			</tr>
			<tr>
				<th class="required-label">매입거래처</th>
				<td>
					<div class="d-flex gap-2">
						<input
							v-model="form_02.custnm"
							class="form-control form-control-sm w-100"
							maxlength="100"
							readonly
						/>
					</div>
				</td>
				<th class="required-label">입고창고</th>
				<td>
					<select v-model="form_02.whcd" class="form-select form-select-sm">
						<option v-for="item in whcdData" :key="item.codecd" :value="item.codecd">
							{{ item.codenm }}
						</option>
					</select>
				</td>
				<th class="required-label">입고금액</th>
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
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef2" class="tabulator-instance flex-grow-1"></div>
        </div>
	</div>
</template>
<script setup lang="ts">
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import DateForm from '@/components/DateForm.vue'
import { getDate } from '@/composables/useDate'
import type { RowComponent } from 'tabulator-tables'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { computed, onMounted, reactive, ref } from 'vue'
import { useSearch } from '@/composables/useSearch'
import { useSave } from '@/composables/useSave'
import { useFormReset } from '@/composables/useFormReset'
import type { SelectData } from '@/composables/useFetchSelectData'
import { fetchSelectDataList } from '@/composables/useFetchSelectData'
import { formatNumber } from '@/composables/useFormatUtils'

const { firstDay, today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { searchStart } = useSearch()
const { saveBody } = useSave()

interface Form_01 {
	fromdt: string
	todt: string
	schcfmyn: string
}

const form_01: Form_01 = {
	fromdt: firstDay,
	todt: today,
	schcfmyn: 'N',
}

interface Form_02 {
	deptcd: string
	deptnm: string
	iono: string
	ioymd: string
	iotype: string
	iotypenm: string
	custcd: string
	custnm: string
	whcd: string
	sumamt: number
	remark: string
	cfmyn: string
}

const form_02 = reactive<Form_02>({
	deptcd: '',
	deptnm: '',
	iono: '',
	ioymd: today,
	iotype: '',
	iotypenm: '',
	custcd: '',
	custnm: '',
	whcd: '100',
	sumamt: 0,
	remark: '',
	cfmyn: 'Y',
})

const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let tableInstance1: Tabulator | null = null
let tableInstance2: Tabulator | null = null

const formattedSum = computed(() => formatNumber(form_02.sumamt))

const whcdData = ref<SelectData[]>([])

const loadSelectData = async () => {
	whcdData.value = await fetchSelectDataList('/code', '030')
}

onMounted(() => {
	loadSelectData()
	if (!tableRef1.value) return

	tableInstance1 = new Tabulator(tableRef1.value, {
		placeholder: 'No data',
		layout: 'fitColumns',
		data: [],
		columns: [
			{ title: '입고번호', field: 'iono', hozAlign: 'center', headerSort: false },
			{ title: '입고일', field: 'ioymd', hozAlign: 'center', headerSort: false },
			{ title: '거래처', field: 'custnm', hozAlign: 'center', headerSort: false },
			{ title: '입고유형', field: 'iotypenm', hozAlign: 'center', headerSort: false },
			{ title: '담당자', field: 'usernm', hozAlign: 'center', headerSort: false },
			{ title: '입고창고', field: 'whnm', hozAlign: 'center', headerSort: false },
			{ title: '확정여부', field: 'cfmyn', hozAlign: 'center', headerSort: false },
		],
	})

	if (!tableRef2.value) return

	tableInstance2 = new Tabulator(tableRef2.value, {
		placeholder: 'No data',
		layout: 'fitColumns',
		data: [],
		columns: [
			{ title: '품목명', field: 'itemnm', hozAlign: 'center', headerSort: false },
			{ title: '규격', field: 'itsize', hozAlign: 'center', headerSort: false },
			{ title: '단위', field: 'unit', hozAlign: 'center', headerSort: false },
			{
				title: '수량',
				field: 'ioqty',
				hozAlign: 'center',
				headerSort: false,
				formatter: 'money',
				formatterParams: {
					thousand: ',',
					precision: 0,
				},
			},
			{
				title: '금액',
				field: 'ioamt',
				hozAlign: 'right',
				headerSort: false,
				formatter: 'money',
				formatterParams: {
					thousand: ',',
					precision: 0,
				},
			},
			{
				title: '부가세',
				field: 'iovat',
				hozAlign: 'right',
				headerSort: false,
				formatter: 'money',
				formatterParams: {
					thousand: ',',
					precision: 0,
				},
			},
			{
				title: '합계',
				field: 'totamt',
				hozAlign: 'right',
				headerSort: false,
				formatter: 'money',
				formatterParams: {
					thousand: ',',
					precision: 0,
				},
			},
		],
	})

	tableInstance1.on('rowClick', onClickRef1)
})

async function onClickRef1(e: UIEvent, row: RowComponent) {
	const rowData = row.getData() as Form_02
	Object.assign(form_02, rowData)

	const path = '/purch-order-in-confirm/search-right'

	const param = {
		iono: rowData.iono,
	}
	try {
		const { data, message } = await searchStart(path, param)
		if (tableInstance2) {
			await tableInstance2.setData(data)
			vAlert(message)
		}
	} catch (error) {
		console.log(error)
		const message =
			error?.response?.data?.message || error?.message || '입고확정 상세 품목 조회에 실패했습니다.'
		vAlertError(message)
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

async function search() {
	if (!tableInstance1) return
	const path = '/purch-order-in-confirm/search-left'
	const params = {
		fromdt: form_01.fromdt.replace(/-/g, ''),
		todt: form_01.todt.replace(/-/g, ''),
		schcfmyn: form_01.schcfmyn,
	}

	try {
		const { data, message } = await searchStart(path, params)
		await tableInstance1.setData(data)
		return vAlert(message)
	} catch (error) {
		console.log(error)
		const message =
			error?.response?.data?.message ||
			error?.message ||
			'입고확정 대상 거래처 조회에 실패했습니다.'
		return vAlertError(message)
	}
}

async function save() {
	for (const key in form_02) {
		const typedKey = key as keyof Form_02

		if (typedKey === 'sumamt') continue

		if (typedKey === 'iotype') {
			if (form_02[typedKey] === '200') {
				return vAlertError('이관 입고 시, 수정할 수 없습니다.')
			}
		}

		if (!form_02[typedKey] || form_02[typedKey].trim() === '') {
			if (typedKey === 'deptcd' || typedKey === 'deptnm') {
				return vAlertError('입고부서가 비어있습니다.')
			} else if (typedKey === 'iono') {
				return vAlertError('입고번호가 비어있습니다.')
			} else if (typedKey === 'ioymd') {
				return vAlertError('입고일자를 선택해주세요')
			} else if (typedKey === 'custcd' || typedKey === 'custnm') {
				if (form_02.iotype === '390') {
					continue
				}
				return vAlertError('거래처를 선택해주세요.')
			} else if (typedKey === 'whcd') {
				return vAlertError('입고창고를 선택해주세요.')
			}
		}
	}

	const path = '/purch-order-in-confirm/confirm'
	const payload = {
		...form_02,
	}

	try {
		const { message } = await saveBody(path, payload)
		await search()
		resetForm(form_02)
		tableInstance2?.clearData()
		return vAlert(message)
	} catch (error) {
		console.log(error)
		const message =
			error?.response?.data?.message || error?.message || '입고 상태를 저장하는 데 실패했습니다.'
		return vAlertError(message)
	}
}
</script>
<style scoped></style>
