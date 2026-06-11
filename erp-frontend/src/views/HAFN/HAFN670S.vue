<!--
	=============================================================
	프로그램명	: 미지급금현황 (HAFN670S)
	작성일자	: 2025.02.24
	설명        : 거래처별 미지급금 발생, 지불, 잔액 현황 조회 (HSOD100U 표준 디자인 및 소문자 원칙 적용)
	=============================================================
-->

<template>
  <app_alert :show="show_alert" :error="show_error" :message="alert_message" />
  <modal_component v-model:visible="modal_visible" :modalProps="modal_props" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
        자금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">미지급금현황 (HAFN670S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-print" @click="print">인쇄</button>
        <button class="btn-erp btn-excel" @click="excel">엑셀</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 (HSOD100U 표준화) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" /><col style="width: 25%" />
              <col style="width: 10%" /><col style="width: 55%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light border-end">기준일자</th>
                <td class="border-end px-2">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="search_form.ymd" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
                    <span class="small fw-bold text-secondary">현재</span>
                  </div>
                </td>
                <th class="text-center bg-light border-end">거 래 처</th>
                <td>
                  <div class="d-flex align-items-center gap-1 px-1">
                    <div class="input-group input-group-sm" style="width: 220px;">
                      <input v-model="search_form.custcdfr" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                      <input v-model="search_form.custnmfr" type="text" class="form-control" placeholder="시작 거래처" @keydown.enter="open_help('CUSTFR')" />
                      <button class="btn btn-outline-secondary px-2" @click="open_help('CUSTFR')"><i class="bi bi-search"></i></button>
                    </div>
                    <span class="text-muted">~</span>
                    <div class="input-group input-group-sm" style="width: 220px;">
                      <input v-model="search_form.custcdto" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                      <input v-model="search_form.custnmto" type="text" class="form-control" placeholder="종료 거래처" @keydown.enter="open_help('CUSTTO')" />
                      <button class="btn btn-outline-secondary px-2" @click="open_help('CUSTTO')"><i class="bi bi-search"></i></button>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom text-secondary">
          <span class="fw-bold small"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 거래처별 미지급금 내역</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="main_grid_ref" class="tabulator-instance flex-grow-1"></div>
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
import { useFormReset as use_form_reset } from '@/composables/useFormReset'
import { useRouter as use_router } from 'vue-router'
import { addDynamicRoute as add_dynamic_route } from '@/router/dynamicRoute'
import app_alert from '@/components/AppAlert.vue'
import modal_component from '@/components/Modal.vue'
import type { ModalProps as modal_props_type } from '@/types/modal'

const auth_store = use_auth_store()
const router = use_router()
const { showAlert: show_alert, showError: show_error, alertMessage: alert_message, vAlert: v_alert, vAlertError: v_alert_error } = use_alerts()
const { resetForm: reset_form } = use_form_reset()

const today = new Date().toISOString().substring(0, 10)

// 🔍 검색 데이터 (소문자 원칙)
const search_form = _reactive({
	ymd: today,
	custcdfr: '',
	custnmfr: '',
	custcdto: '',
	custnmto: ''
})

const main_grid_ref = _ref<HTMLDivElement | null>(null)
let maingrid: tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/hafn/HAFN_670S_STR', {
			cmpycd: auth_store.cmpycd,
			ymd: search_form.ymd.replace(/-/g, ''),
			custcdfr: search_form.custcdfr,
			custcdto: search_form.custcdto
		})

		const data = (res.data || []).map((row: any) => {
            const item = Object.fromEntries(Object.entries(row).map(([k, v]) => [k.toLowerCase(), v]))
            return {
                ...item,
                upyamt: Number(item.upyamt || 0),
                payamt: Number(item.payamt || 0),
                janamt: Number(item.janamt || 0),
                bank_info: `${item.gujoa || ''} ${item.banknm || ''}`.trim()
            }
        })

		maingrid?.setData(data)
		v_alert('조회되었습니다.')
	} catch (e) { v_alert_error('조회 실패') }
}

const initialize = () => {
	reset_form(search_form)
	search_form.ymd = today
	maingrid?.clearData()
}

const excel = () => maingrid?.download("xlsx", `미지급금현황_${today}.xlsx`)

const print = () => {
	const params = `ymd=${search_form.ymd}&custcdfr=${search_form.custcdfr}&custcdto=${search_form.custcdto}`
	window.open(`/api/hafn/HAFN_670P?${params}`, 'UnpaidStatusPrint', 'width=1000,height=800,scrollbars=yes')
}

// 상세 이동 (HAFN650S로 이동)
const go_detail = (row: any) => {
    const pgmid = 'HAFN650S'
    add_dynamic_route(pgmid, '미지급금상세현황', 'HAFN')
	router.push({
		path: `/${pgmid}`,
		query: { ymd: search_form.ymd.replace(/-/g, ''), custcd: row.custcd, custnm: row.custnm }
	})
}

// 팝업 설정
const modal_visible = _ref(false)
const modal_props = _reactive<modal_props_type>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function open_help(type: string) {
	const is_fr = type === 'CUSTFR'
	Object.assign(modal_props, {
		title: '거래처 선택', path: '/api/ha00/HA00_00P_STR',
		data: { gubun: 'c4', cmpycd: auth_store.cmpycd, gbncd: '', code: is_fr ? search_form.custnmfr : search_form.custnmto },
		columns: [{ title: '코드', field: 'custcd', width: 80 }, { title: '거래처명', field: 'custnm', width: 180 }],
		onConfirm: (d: any) => {
            const item = Object.fromEntries(Object.entries(d).map(([k, v]) => [k.toLowerCase(), v]))
			if (is_fr) { search_form.custcdfr = item.custcd; search_form.custnmfr = item.custnm }
			else { search_form.custcdto = item.custcd; search_form.custnmto = item.custnm }
		}
	})
	modal_visible.value = true
}

_on_mounted(() => {
	if (main_grid_ref.value) {
		maingrid = new tabulator(main_grid_ref.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "거래처", field: "custcd", width: 100, hozAlign: "center",
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer fw-bold">${cell.getValue()}</span>`,
					cellClick: (e, cell) => go_detail(cell.getData())
				},
				{
					title: "거래처명", field: "custnm", width: 300,
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</span>`,
					cellClick: (e, cell) => go_detail(cell.getData())
				},
				{ title: "입금은행/계좌", field: "bank_info", minWidth: 300 },
				{
					title: "발생액", field: "upyamt", width: 150, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "지불액", field: "payamt", width: 150, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
				},
				{
					title: "잔 액", field: "janamt", width: 150, hozAlign: "right",
					formatter: "money", formatterParams: { precision: 0 },
					bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 },
					cssClass: "fw-bold text-danger"
				}
			]
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.cursor-pointer { cursor: pointer; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
