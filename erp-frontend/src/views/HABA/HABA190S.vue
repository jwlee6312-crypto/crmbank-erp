<!--
	=============================================================프로그램명	: 거래처현황
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 등록된 모든 거래처의 상세 현황 및 관리 정보를 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-card-list me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">거래처현황 (HABA190S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-excel" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
				<button class="btn-erp btn-print" @click="print">
					<i class="bi bi-printer"></i> 인쇄
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 150px;" />
						<col style="width: 100px;" /><col style="width: 250px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">구 분</th>
							<td class="bg-white border-end">
								<select v-model="searchForm.iogbn" class="form-select form-select-sm" @change="search">
									<option value="000">전체</option>
									<option v-for="opt in ioGbnOptions" :key="opt.CD" :value="opt.CD">{{ opt.NM }}</option>
								</select>
							</td>
							<th class="text-center border-end">거래처명</th>
							<td class="bg-white border-end px-2">
								<input v-model="searchForm.custnm" type="text" class="form-control form-control-sm" placeholder="거래처명 검색" @keydown.enter="search" />
							</td>
							<td class="bg-white px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> 거래처명을 클릭하면 상세 정보 관리 화면으로 이동합니다.
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
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 🔍 검색 데이터
const searchForm = reactive({
	iogbn: '000',
	custnm: ''
})

const ioGbnOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

// 🚀 초기 정보 로드
const loadInitData = async () => {
	try {
		// 입출구분 (340)
		const resIo = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '340' })
		ioGbnOptions.value = (resIo.data || []).map((r: any) => ({ CD: r.col0, NM: r.col1 }))
	} catch (e) {
		console.error('초기 데이터 로드 실패')
	}
}

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_190S_STR', {
			cmpycd: authStore.cmpycd,
			iogbn: searchForm.iogbn,
			custnm: searchForm.custnm
		})

		const processedData = (res.data || []).map((r: any) => {
			// 사업자번호 포맷팅 (10자리: 000-00-00000, 13자리: 000000-0000000)
			let formattedNo = r.custno || ''
			if (formattedNo.length === 10) {
				formattedNo = `${formattedNo.substring(0, 3)}-${formattedNo.substring(3, 5)}-${formattedNo.substring(5)}`
			} else if (formattedNo.length === 13) {
				formattedNo = `${formattedNo.substring(0, 6)}-${formattedNo.substring(6)}`
			}

			return {
				...r,
				custno_F: formattedNo,
				BANK_ACC: `${r.banknm || ''} ${r.gujoa || ''}`.trim()
			}
		})

		mainGrid?.setData(processedData)
		vAlert('조회되었습니다.')
	} catch (e) {
		vAlertError('조회 중 오류 발생')
	}
}

const initialize = () => {
	resetForm(searchForm)
	searchForm.iogbn = '000'
	mainGrid?.clearData()
}

const excel = () => mainGrid?.download("xlsx", `거래처현황_${new Date().toISOString().substring(0, 10)}.xlsx`)
const print = () => window.open(`/api/haba/HABA_190P?iogbn=${searchForm.iogbn}&custnm=${searchForm.custnm}`)

// 상세 이동 로직
const goDetail = (row: any) => {
	router.push({
		path: '/HABA/HABA180U',
		query: {
			custgbn: row.custgbn,
			status: row.status,
			custnm: row.custnm
		}
	})
}

onMounted(() => {
	loadInitData()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{
					title: "거래처", field: "custcd", width: 70, hozAlign: "center",
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer fw-bold">${cell.getValue()}</span>`,
					cellClick: (e, cell) => goDetail(cell.getData())
				},
				{
					title: "거래처 명", field: "custnm", minWidth: 200,
					formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</span>`,
					cellClick: (e, cell) => goDetail(cell.getData())
				},
				{ title: "구분", field: "iogbnNM", width: 80, hozAlign: "center" },
				{ title: "종류", field: "custgbnM", width: 80, hozAlign: "center" },
				{ title: "사업자번호", field: "custno_F", width: 110, hozAlign: "center" },
				{ title: "대표자", field: "bossnm", width: 80, hozAlign: "center" },
				{ title: "업태", field: "custtype", width: 120 },
				{ title: "종목", field: "custkind", width: 120 },
				{ title: "연락처", field: "telno", width: 110, hozAlign: "center" },
				{ title: "매입단가", field: "inprcgbnM", width: 80, hozAlign: "center" },
				{ title: "매출단가", field: "outprcgbnM", width: 80, hozAlign: "center" },
				{ title: "여신한도", field: "hdamt", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "여신기한", field: "rcvdd", width: 80, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "은행/계좌", field: "BANK_ACC", width: 150 },
				{ title: "상태", field: "statusNM", width: 70, hozAlign: "center" }
			]
		})
	}
})
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.cursor-pointer { cursor: pointer; }
</style>