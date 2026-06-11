<!--
	=============================================================
	프로그램명	: 보조원장-거래처
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 특정 계정과목 및 거래처에 대한 상세 전표 내역 및 잔액 조회 (HASL030S 표준화 및 SQL 알리아스 적용)
	=============================================================
-->

<template>
	<app_alert :show="show_alert" :error="show_error" :message="alert_message" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-person-lines-fill me-2 text-primary" style="font-size: 18px;"></i>
				장부관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">보조원장-거래처 (HASL550S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-2 bg-light">
					<div class="d-flex align-items-center flex-wrap gap-3 small">
						<!-- 계정과목 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>계정과목</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchform.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
								<input v-model="searchform.acctnm" type="text" class="form-control" @keydown.enter="open_help('ACCT')" placeholder="계정명 입력" />
								<button class="btn btn-outline-secondary px-2" @click="open_help('ACCT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<!-- 거래처 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>거&nbsp;래&nbsp;처</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchform.custcd" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
								<input v-model="searchform.custnm" type="text" class="form-control" @keydown.enter="open_help('CUST')" placeholder="거래처명 입력" />
								<button class="btn btn-outline-secondary px-2" @click="open_help('CUST')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<!-- 회계일자 -->
						<div class="d-flex align-items-center">
							<span class="erp-label"><i class="bi bi-dot"></i>회계일자</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchform.frymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
								<span>~</span>
								<input v-model="searchform.toymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
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
                  <div ref="maingridref" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>

	<modal_component v-model:visible="modal_visible" :modalProps="modal_props" />
</template>

<script setup lang="ts">
import { ref as _ref, reactive as _reactive, onMounted as _on_mounted } from 'vue'
import { TabulatorFull as tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts as use_alerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore as use_auth_store } from '@/stores/authStore'
import { useRouter as use_router, useRoute as use_route } from 'vue-router'
import app_alert from '@/components/AppAlert.vue'
import modal_component from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authstore = use_auth_store()
const router = use_router()
const route = use_route()
const { showAlert: show_alert, showError: show_error, alertMessage: alert_message, vAlert: v_alert, vAlertError: v_alert_error } = use_alerts()

// 💡 날짜 포맷팅 함수 (20260601 -> 2026-06-01)
const format_date = (val: any) => {
	if (!val || typeof val !== 'string' || val.length !== 8) return val
	return `${val.substring(0, 4)}-${val.substring(4, 6)}-${val.substring(6, 8)}`
}

const now = new Date()
const first_day = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
const today = now.toISOString().substring(0, 10)

const searchform = _reactive({
	acctcd: (route.query.acctcd as string) || '',
	acctnm: (route.query.acctnm as string) || '',
	custcd: (route.query.custcd as string) || '',
	custnm: (route.query.custnm as string) || '',
	custgbn: '',
	frymd: format_date(route.query.frymd) || first_day,
	toymd: format_date(route.query.toymd) || today
})

const maingridref = _ref<HTMLDivElement | null>(null)
let maingrid: tabulator | null = null

const search = async () => {
	if (!searchform.acctcd) return v_alert_error('계정과목을 선택해 주십시오.')
	if (!searchform.custcd) return v_alert_error('거래처를 선택해 주십시오.')

	try {
		const res = await api.post('/api/hasl/HASL_550S_STR', {
			cmpycd: authstore.cmpycd,
			acctcd: searchform.acctcd,
			custcd: searchform.custcd,
			frymd: searchform.frymd.replace(/-/g, ''),
			toymd: searchform.toymd.replace(/-/g, '')
		})

		// 💡 소문자 정규화: SQL 알리아스(ACCTYMD, DESCNM, dbamt, cramt 등)를 소문자로 변환하여 일관된 접근 지원
		const raw_data = (res.data || []).map((item: any) => {
			return Object.fromEntries(
				Object.entries(item).map(([k, v]) => [k.toLowerCase(), v])
			)
		})

		const processed_data: any[] = []

		if (raw_data.length > 0) {
			// 첫 번째 행 (전기이월/시작잔액 행)
			let carry_row = raw_data[0]
			let db_mm_tot = Number(carry_row.dbamt || 0)
			let cr_mm_tot = Number(carry_row.cramt || 0)
			let dbcr = carry_row.dbcr // 'D' or 'C'
			let jan_amt = dbcr === 'D' ? (db_mm_tot - cr_mm_tot) : (cr_mm_tot - db_mm_tot)

			processed_data.push({
				acctymd: '',
				descnm: carry_row.descnm,
				slipno: '',
				dbamt: db_mm_tot,
				cramt: cr_mm_tot,
				janamt: jan_amt,
				is_summary: true
			})

			let i = 1
			while (i < raw_data.length) {
				let current_ym = String(raw_data[i].acctymd).substring(0, 6)
				let db_mm_amt = 0; let cr_mm_amt = 0;

				while (i < raw_data.length && String(raw_data[i].acctymd).substring(0, 6) === current_ym) {
					const row = raw_data[i]
					const db = Number(row.dbamt || 0)
					const cr = Number(row.cramt || 0)

					if (dbcr === 'D') jan_amt += (db - cr)
					else jan_amt += (cr - db)

					// 💡 알리아스 기반 세부내역 조립
					let details: string[] = []
					if (row.deptnm) details.push(String(row.deptnm).trim())
					if (row.subnm) details.push(String(row.subnm).trim())
					if (row.mgtno) details.push(String(row.mgtno).trim())
					if (row.bugtnm) details.push(String(row.bugtnm).trim())
					if (row.prjnm) details.push(String(row.prjnm).trim())
					if (row.sname) details.push(String(row.sname).trim())
					if (row.issubank) details.push('은행: ' + String(row.issubank).trim())
					if (row.issuman) details.push('발행: ' + String(row.issuman).trim())

					// 서비스료, 봉사료
					if (Number(row.srvamt || 0) !== 0) details.push('서비스료: ' + new Intl.NumberFormat().format(row.srvamt))
					if (Number(row.bongamt || 0) !== 0) details.push('봉사료: ' + new Intl.NumberFormat().format(row.bongamt))

					// 외화 정보
					if (row.frgnyn === 'Y') {
						if (row.frgnnm) details.push(String(row.frgnnm).trim())
						if (Number(row.frgnrate || 0) !== 0) details.push('환율: ' + new Intl.NumberFormat().format(row.frgnrate))
						if (Number(row.frgnamt || 0) !== 0) details.push('외화: ' + new Intl.NumberFormat().format(row.frgnamt))
					}

					processed_data.push({
						acctymd: row.acctymd,
						descnm: row.descnm + (row.sslipno && String(row.sslipno).trim() !== '' ? ` (${row.sslipno})` : ''),
						slipno: row.slipno,
						dbamt: db,
						cramt: cr,
						janamt: jan_amt,
						detail_str: details.join(' | '),
						is_data: true
					})

					db_mm_amt += db
					cr_mm_amt += cr
					i++
				}

				// 월계
				processed_data.push({
					acctymd: '', descnm: '월   계', dbamt: db_mm_amt, cramt: cr_mm_amt, janamt: null, is_monthly: true
				})

				db_mm_tot += db_mm_amt
				cr_mm_tot += cr_mm_amt

				// 누계
				processed_data.push({
					acctymd: '', descnm: '누   계', dbamt: db_mm_tot, cramt: cr_mm_tot, janamt: jan_amt, is_total: true
				})
			}
		}

		maingrid?.setData(processed_data)
		if (processed_data.length > 0) v_alert('조회되었습니다.')
		else v_alert('데이터가 존재하지 않습니다.')
	} catch (e) {
		v_alert_error('조회 중 오류 발생')
	}
}

// 팝업 설정
const modal_visible = _ref(false)
const modal_props = _reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function open_help(type: 'ACCT' | 'CUST') {
	if (type === 'ACCT') {
		Object.assign(modal_props, {
			title: '계정과목 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'acctnm',
			data: { gubun: 'A0', cmpycd: authstore.cmpycd, search: searchform.acctnm },
			columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 180 }, { title: '구분', field: 'TYPESUB', width: 50, visible: false }],
			onConfirm: (d: any) => {
				searchform.acctcd = d.acctcd; searchform.acctnm = d.acctnm; searchform.custgbn = d.TYPESUB || '';
				searchform.custcd = ''; searchform.custnm = '';
			}
		})
	} else {
		Object.assign(modal_props, {
			title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'custnm',
			data: { gubun: 'c4', cmpycd: 'coit', search: searchform.custnm },
			columns: [{ title: '코드', field: 'custcd', width: 100 }, { title: '거래처명', field: 'custnm', width: 250 }],
			onConfirm: (d: any) => { searchform.custcd = d.custcd; searchform.custnm = d.custnm; search() }
		})
	}
	modal_visible.value = true
}

const print = () => {
	if (!searchform.acctcd || !searchform.custcd) return v_alert_error('계정 및 거래처를 먼저 선택하세요.')
	const params = `acctcd=${searchform.acctcd}&acctnm=${searchform.acctnm}&custcd=${searchform.custcd}&custnm=${searchform.custnm}&frymd=${searchform.frymd.replace(/-/g, '')}&toymd=${searchform.toymd.replace(/-/g, '')}&PRTGU=1`
	window.open(`/api/hasl/HASL_550P?${params}`, 'Print', 'width=800,height=800')
}

const go_slip_detail = (slip_no: string) => {
	if (!slip_no || slip_no.length < 10) return
	const ymd = slip_no.substring(0, 8); const no = slip_no.substring(9);
	router.push({ path: '/HASL110U', query: { slipymd: ymd, slipno: no } })
}

_on_mounted(async () => {
	if (maingridref.value) {
		maingrid = new tabulator(maingridref.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "일자", field: "acctymd", width: 90, hozAlign: "center",
					formatter: (cell) => {
						const v = cell.getValue()
						return v && v.length === 8 ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : ''
					}
				},
				{
					title: "적요 / 세부내역", field: "descnm", widthGrow: 2.5,
					formatter: (cell) => {
						const d = cell.getData()
						if (d.is_data) return `<div>${cell.getValue()}</div><div class="small text-secondary fw-normal mt-1" style="font-size: 11px;">${d.detail_str}</div>`
						return cell.getValue()
					}
				},
				{
					title: "전표번호", field: "slipno", width: 120, hozAlign: "center",
					formatter: (cell) => {
						const v = cell.getValue()
						return v ? `<span class="text-primary text-decoration-underline cursor-pointer">${v}</span>` : ''
					},
					cellClick: (e, cell) => { go_slip_detail(cell.getValue()) }
				},
				{ title: "차변", field: "dbamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "대변", field: "cramt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "잔액", field: "janamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } }
			],
			rowFormatter: (row) => {
				const d = row.getData()
				if (d.is_monthly || d.is_total || d.is_summary) { row.getElement().style.backgroundColor = "#f8f9fa"; row.getElement().style.fontWeight = "bold"; }
			}
		})
	}

	if (searchform.acctcd && searchform.custcd) {
		// 💡 전달받은 명칭이 있으면 즉시 조회 실행 (404 오류 방지 및 최적화)
		if (searchform.acctnm && searchform.custnm) {
			search()
		} else {
			try {
				const res = await api.post('/api/ha00/HA00_010S_STR', { cmpycd: authstore.cmpycd, gubun: 'A0', search: '', acctcd: searchform.acctcd })
				if (res.data?.length > 0) {
					searchform.acctnm = res.data[0].col0 || res.data[0].acctnm; searchform.custgbn = res.data[0].TYPESUB
					if (searchform.custcd) {
						const res_cust = await api.post('/api/ha00/HA00_010S_STR', { cmpycd: authstore.cmpycd, gubun: 'C0', TYPESUB: searchform.custgbn, search: searchform.custcd })
						if (res_cust.data?.length > 0) { searchform.custnm = res_cust.data[0].col0 || res_cust.data[0].custnm; search() }
					}
				}
			} catch (e) {}
		}
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
:deep(.tabulator-cell) { border-right: 1px solid #dee2e6 !important; }
:deep(.tabulator-header .tabulator-col) { border-right: 1px solid #dee2e6 !important; background-color: #f8f9fa !important; }
</style>
