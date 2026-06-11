<!--수입관리/수입원가계산서-->
<!--
	=============================================================
	프로그램명	  : 수입원가계산서 (시스템 최종 표준 모델)
    프로그램 ID	: HSIP170S
	작성일자	    : 25.02.24
	작성자	      : AI Assistant (Full-Expansion Layout)
    설명         : 년월 입력 방식을 문자 6자리(yyyymm) 체계로 변경
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-1 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 13px;">
				<i class="bi bi-file-earmark-ruled-fill me-2 text-primary" style="font-size: 18px;"></i>
				수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">수입원가계산서 (HSIP170S)</span>
			</div>
			<div class="btn-group-erp pe-2">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-print" @click="print">인쇄</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 -->
		<table class="erp-table-full border-bottom" style="table-layout: fixed;">
			<colgroup>
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
				<col style="width: 130px;" /> <col />
			</colgroup>
			<tbody>
				<tr>
					<th class="required border-end text-nowrap text-center">PO No</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.fileno" type="text" class="form-control border-primary-subtle fw-bold" placeholder="PO 번호 입력" @keyup.enter="search" />
							<button class="btn btn-outline-secondary px-2" @click="openPoPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="required border-end text-nowrap text-center">선적차수</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.shipseq" type="text" class="form-control text-center" style="max-width: 60px;" />
							<button class="btn btn-outline-secondary px-2" @click="openShipPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="required border-end text-nowrap text-center">통관차수</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.passseq" type="text" class="form-control text-center" style="max-width: 60px;" />
							<button class="btn btn-outline-secondary px-2" @click="openPassPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
					<th class="border-end text-nowrap text-center">품    목</th>
					<td>
						<div class="input-group input-group-sm flex-nowrap ms-1">
							<input v-model="searchForm.itemcd" type="text" class="form-control bg-light" style="max-width: 80px;" readonly />
							<input v-model="searchForm.itemnm" type="text" class="form-control" />
							<button class="btn btn-outline-secondary px-2" @click="openItemPopup"><i class="bi bi-search"></i></button>
						</div>
					</td>
				</tr>
			</tbody>
		</table>

		<!-- 📊 3. 메인 작업 영역 -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column bg-white">
			<div class="flex-grow-1 d-flex flex-column overflow-hidden">
				<div class="grid-title py-1 px-3 border-bottom fw-bold small text-primary bg-light">
					<i class="bi bi-box-seam-fill me-1"></i> 품목별 수입비용 배부 상세 현황
				</div>
				<div ref="mainGridRef" class="tabulator-full-height border-top" />
			</div>

			<div class="bg-light border-top" style="height: 200px;">
				<div class="grid-title py-1 px-3 border-bottom bg-white fw-bold small text-secondary">
					<i class="bi bi-list-stars me-1"></i> 수입 비용종류별 요약 (Summary)
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                    <div ref="summaryGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>

		<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchForm = reactive({
	fileno: '', shipseq: '10', passseq: '10', itemcd: '', itemnm: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null); const summaryGridRef = ref<HTMLDivElement | null>(null);
let mainGrid: Tabulator | null = null; let summaryGrid: Tabulator | null = null;

async function search() {
	if (!searchForm.fileno) {
		return vAlertError('PO No를 입력하세요.');
	}
	try {
		const res = await api.post('/api/hsip/HSIP_161S_STR', {
			...searchForm,
			cmpycd: authStore.cmpycd,
			actkind: 'S0'
		})
		if (res.data) {
			mainGrid?.setData(res.data.filter((i:any) => i.gbn !== 'sum') || [])
			summaryGrid?.setData(res.data.filter((i:any) => i.gbn === 'sum') || [])
			vAlert('조회되었습니다.')
		}
	} catch (e) { vAlertError('조회 실패') }
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	const commonProps = { path: '/api/hs00/HS00_000S_STR', cmpycd: authStore.cmpycd };
	if (type === 'PO') {
		Object.assign(modalProps, {
			title: 'PO 선택', ...commonProps,
			data: { gubun: 'F0', cmpycd: authStore.cmpycd, gbncd: '1' },
			columns: [
				{ title: 'PO No', field: 'fileno', width: 120 },
				{ title: '거래처', field: 'custnm', minWidth: 200, widthGrow: 1 },
				{ title: '발주일자', field: 'issymd', width: 120, formatter: (c:any) => '발주일:' + c.getValue() }
			],
			onConfirm: (d: any) => { searchForm.fileno = d.fileno; search() }
		})
	} else if (type === 'ITEM') {
		Object.assign(modalProps, {
			title: '품목 선택', path: '/api/hs00/HS00_000S_STR', defaultField: 'itemnm', large: true,
			data: { gubun: 'I1', cmpycd: authStore.cmpycd, gbncd: '1' },
			columns: [{ title: '코드', field: 'itemcd', width: 100 }, { title: '품목명', field: 'itemnm', width: 200 }, { title: '규격', field: 'itsize', width: 150 }],
			onConfirm: (d: any) => { searchForm.itemcd = d.itemcd; searchForm.itemnm = d.itemnm }
		})
	}
	modalVisible.value = true
}

const openPoPopup = () => openHelp('PO')
const openShipPopup = () => { vAlert('선적차수를 직접 입력하거나 PO 조회 후 확인하세요.') }
const openPassPopup = () => { vAlert('통관차수를 직접 입력하거나 PO 조회 후 확인하세요.') }
const openItemPopup = () => openHelp('ITEM')
const print = () => { vAlert('인쇄 기능을 준비 중입니다.') }

function initialize() {
	resetForm(searchForm); mainGrid?.clearData(); summaryGrid?.clearData()
	searchForm.shipseq = '10'; searchForm.passseq = '10'
}

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			placeholder: '표시할 데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', minWidth: 100 },
			columns: [
				{ title: '품목명칭', field: 'itemnm', minWidth: 250, widthGrow: 10, cssClass: 'fw-bold text-dark' },
				{ title: '규격', field: 'itsize', width: 250 },
				{ title: '단위', field: 'unit', hozAlign: 'center', width: 100 },
				{ title: '입고수량', field: 'inqty', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '비용종류', field: 'costnm', width: 250, cssClass: 'border-start text-primary' },
				{ title: '단가(KRW)', field: 'price', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 2 } },
				{ title: '배부금액', field: 'costamt', hozAlign: 'right', width: 200, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'fw-bold' }
			]
		})
	}
	if (summaryGridRef.value) {
		summaryGrid = new Tabulator(summaryGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', minWidth: 100 },
			columns: [
				{ title: '비용종류', field: 'costnm', widthGrow: 1 },
				{ title: '배부기준', field: 'divnm', width: 350 },
				{ title: '총액', field: 'costamt', hozAlign: 'right', width: 350, formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'text-danger fw-bold' }
			]
		})
	}
})
</script>
