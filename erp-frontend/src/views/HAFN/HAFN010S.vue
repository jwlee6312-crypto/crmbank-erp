<template>
	<!-- 💡 HASL030S 스타일: 소문자 컴포넌트 태그 및 변수명 사용 -->
	<app_alert :show="show_alert" :error="show_error" :message="alert_message" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-cash-stack me-2 text-primary" style="font-size: 18px;"></i>
				자금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">자금현황 (hafn010s)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
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
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">기준일자</th>
							<td class="bg-white">
								<div class="d-flex align-items-center gap-2">
									<input v-model="searchform.ymd" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
									<span class="small fw-bold text-secondary">현재</span>
									<select v-model="searchform.selgbn" class="form-select form-select-sm ms-3" style="max-width: 120px;" @change="search">
										<option value="mm">당월기준</option>
										<option value="dd">당일기준</option>
									</select>
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
					<div ref="maingridref" class="tabulator-instance flex-grow-1"></div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
// 💡 HASL030S 스타일: Vue 핵심 함수 별칭 사용
import { ref as _ref, reactive as _reactive, onMounted as _on_mounted } from 'vue'
import { TabulatorFull as tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts as use_alerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore as use_auth_store } from '@/stores/authStore'
import { useFormReset as use_form_reset } from '@/composables/useFormReset'
import { useRouter as use_router } from 'vue-router'
import { addDynamicRoute as add_dynamic_route } from '@/router/dynamicRoute'
import app_alert from '@/components/AppAlert.vue'

const authstore = use_auth_store()
const router = use_router()
const { showAlert: show_alert, showError: show_error, alertMessage: alert_message, vAlert: v_alert, vAlertError: v_alert_error } = use_alerts()
const { resetForm: reset_form } = use_form_reset()

const today = new Date().toISOString().substring(0, 10)

const searchform = _reactive({
	ymd: today,
	selgbn: 'mm'
})

const maingridref = _ref<HTMLDivElement | null>(null)
let maingrid: tabulator | null = null

const get_columns = () => {
	const prefix = searchform.selgbn === 'mm' ? '월' : '일'
	const prev_prefix = searchform.selgbn === 'mm' ? '전월' : '전일'

	return [
		{ title: "과목", field: "acctnm", minWidth: 180,
			formatter: (cell) => `<span class="text-primary cursor-pointer text-decoration-underline fw-bold">${cell.getValue()}</span>`,
			cellClick: (e, cell) => go_acct_ledger(cell.getData())
		},
		{ title: "보조과목(거래처)", field: "custnm", width: 300,
			formatter: (cell) => `<span class="text-success cursor-pointer text-decoration-underline">${cell.getValue()}</span>`,
			cellClick: (e, cell) => go_cust_ledger(cell.getData())
		},
		{ title: `${prev_prefix}잔액`, field: "bjamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 200 },
		{ title: `당${prefix}증가`, field: "icamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 200 },
		{ title: `당${prefix}감소`, field: "dcamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 200 },
		{ title: `당${prefix}잔액`, field: "janamt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 200,
			cssClass: "fw-bold bg-light-subtle"
		}
	]
}

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_010S_STR', {
			cmpycd: authstore.cmpycd,
			ymd: searchform.ymd.replace(/-/g, ''),
			selgbn: searchform.selgbn
		})

		const data = (res.data || []).map((row: any) => ({
			acctcd: row.acctcd,
			acctnm: row.acctnm,
			custcd: row.custcd,
			custnm: row.custnm,
			bjamt: Number(row.bjamt || 0),
			icamt: Number(row.dbamt || 0),
			dcamt: Number(row.cramt || 0),
			janamt: Number(row.janamt || 0)
		}))

		maingrid?.setColumns(get_columns())
		maingrid?.setData(data)
		v_alert('조회되었습니다.')
	} catch (e) { v_alert_error('조회 실패') }
}

const initialize = () => {
	reset_form(searchform)
	searchform.ymd = today
	searchform.selgbn = 'mm'
	maingrid?.clearData()
}

const excel = () => maingrid?.download("xlsx", `자금현황_${searchform.ymd}.xlsx`)
const print = () => {
	const params = `ymd=${searchform.ymd}&selgbn=${searchform.selgbn}`
	window.open(`/api/hafn/HAFN_010P?${params}`, 'Print', 'width=1000,height=800')
}

/**
 * 🚀 HASL030S 표준 링크 방식 적용
 */
const go_acct_ledger = (row: any) => {
	if (!row.acctcd) return

	const pgmid = 'HASL540S'
	add_dynamic_route(pgmid, '계정별원장', 'HASL')

	const frymd = `${searchform.ymd.substring(0, 7)}-01`.replace(/-/g, '')
	const toymd = searchform.ymd.replace(/-/g, '')

	router.push({
		path: `/${pgmid}`,
		query: {
			acctcd: row.acctcd.trim(),
			frymd: frymd,
			toymd: toymd
		}
	})
}

const go_cust_ledger = (row: any) => {
	if (!row.custcd) return

	const pgmid = 'HASL550S'
	add_dynamic_route(pgmid, '거래처원장', 'HASL')

	const frymd = `${searchform.ymd.substring(0, 7)}-01`.replace(/-/g, '')
	const toymd = searchform.ymd.replace(/-/g, '')

	router.push({
		path: `/${pgmid}`,
		query: {
			acctcd: row.acctcd ? row.acctcd.trim() : '',
			custcd: row.custcd.trim(),
			frymd: frymd,
			toymd: toymd
		}
	})
}

_on_mounted(() => {
	if (maingridref.value) {
		maingrid = new tabulator(maingridref.value, {
			layout: 'fitColumns',
			height: '100%',
			groupBy: "acctnm",
			groupHeader: (value, count, data) => {
				const sumjan = data.reduce((acc, curr) => acc + curr.janamt, 0)
				return `
					<div class="d-flex justify-content-between w-100 pe-4">
						<span class="fw-bold text-dark">${value} 계</span>
						<div class="small fw-bold">잔액: ${sumjan.toLocaleString()}</div>
					</div>
				`
			},
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: get_columns()
		})
	}
})
</script>

<style scoped>
.cursor-pointer { cursor: pointer; }
:deep(.tabulator-group) { background-color: #e9ecef !important; border-top: 1px solid #dee2e6 !important; }
:deep(.bg-light-subtle) { background-color: #f8f9fa !important; }
</style>