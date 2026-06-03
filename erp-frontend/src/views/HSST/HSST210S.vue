<!--
	=============================================================
	프로그램명	: 월별 채권 잔액 현황 [디자인 원칙 13가지 + 검색영역 단일행 균등배분]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 거래처별 최근 6개월 판매/수금/잔액 추이 현황 (그리드 렌더링 보정 완료)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-range-fill me-2 text-primary" style="font-size: 18px;"></i>
				매출분석 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">월별 채권 잔액 현황 (HSST210S)</span>
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
			</div>
		</div>

		<!-- 🔍 9. 최상단 검색 영역 (33.3% 균등 배분) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 33.3%;" /><col style="width: 33.3%;" /><col style="width: 33.4%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">판매부서</span>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.deptnm" type="text" class="form-control" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">기준일자</span>
									<input v-model="searchForm.ymD" type="date" class="form-control form-control-sm" style="max-width: 150px;" />
									<span class="text-muted small ms-1">현재</span>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">거 래 처</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1">
										<input v-model="searchForm.custnmFR" type="text" class="form-control form-control-sm" placeholder="시작" @keyup.enter="openHelp('CUST_FR')" />
										<button class="btn btn-sm btn-outline-secondary px-1" @click="openHelp('CUST_FR')"><i class="bi bi-search"></i></button>
										<span class="text-muted">~</span>
										<input v-model="searchForm.custnmTO" type="text" class="form-control form-control-sm" placeholder="종료" @keyup.enter="openHelp('CUST_TO')" />
										<button class="btn btn-sm btn-outline-secondary px-1" @click="openHelp('CUST_TO')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 6. 중앙 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>

	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const today = new Date().toISOString().substring(0, 10);
const searchForm = reactive({
	deptcd: authStore.deptcd, deptnm: authStore.deptnm,
	ymD: today, custcdFR: '', custnmFR: '', custcdTO: '', custnmTO: ''
})

const rowCount = ref(0)
const mainGridRef = ref<HTMLDivElement | null>(null);
let mainGrid: Tabulator | null = null

const getMonthHeaders = (baseDate: string) => {
	const date = new Date(baseDate);
	const headers = [];
	for (let i = 5; i >= 0; i--) {
		const d = new Date(date.getFullYear(), date.getMonth() - i, 1);
		headers.push({ year: d.getFullYear(), month: String(d.getMonth() + 1).padStart(2, '0') });
	}
	return headers;
}

const getColumnsConfig = (baseDate: string) => {
	const months = getMonthHeaders(baseDate);
	const columns: any[] = [
		{ title: "거래처 / 담당자", field: "custnm", minWidth: 200, widthGrow: 2, hozAlign: "left", cssClass: "fw-bold border-end", frozen: true,
		  formatter: (cell: any) => {
			  const d = cell.getData();
			  if (d.gubun_TYPE === '1') return (d.custcd || '') + ' ' + (d.custnm || '');
			  if (d.gubun_TYPE === '2') return d.custnm || '';
			  return d.DAMDANG || '';
		  }
		},
		{ title: "구분", field: "gubun_NM", width: 150, hozAlign: "center", cssClass: "bg-light border-end", frozen: true }
	];
	months.forEach((m, idx) => {
		columns.push({
			title: `${m.year}년 ${m.month}월`, field: `AMT_M${idx + 1}`,
			hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 }
		});
	});
	columns.push({ title: "입금요청/회전", field: "REQ_INFO", width: 150, hozAlign: "right", formatter: "money" });
	return columns;
}

const search = async () => {
	if (!searchForm.deptcd) return vAlertError('부서를 선택하세요.');
	try {
		const res = await api.post('/api/hsst/HSST_210S_STR', {
			...searchForm, cmpycd: authStore.cmpycd, ymD: searchForm.ymD.replace(/-/g, '')
		})
		const data = res.data || []
		mainGrid?.setColumns(getColumnsConfig(searchForm.ymD)); // 날짜 변경 대응
		mainGrid?.setData(data)
		rowCount.value = Math.ceil(data.length / 3);
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm);
	searchForm.deptcd = authStore.deptcd; searchForm.deptnm = authStore.deptnm;
	searchForm.ymD = today;
	mainGrid?.clearData(); rowCount.value = 0;
	mainGrid?.setColumns(getColumnsConfig(today));
}

const excel = () => mainGrid?.download("xlsx", "월별채권현황.xlsx")

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
			data: { gubun: 'D0', cmpycd: authStore.cmpycd },
			columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm }
		})
	} else if (type.startsWith('CUST')) {
		Object.assign(modalProps, {
			title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'custnm',
			data: { gubun: 'C0', cmpycd: authStore.cmpycd },
			columns: [{ title: '코드', field: 'custcd', width: 100 }, { title: '거래처명', field: 'custnm', width: 200 }],
			onConfirm: (d: any) => {
				if (type === 'CUST_FR') { searchForm.custcdFR = d.custcd; searchForm.custnmFR = d.custnm }
				else { searchForm.custcdTO = d.custcd; searchForm.custnmTO = d.custnm }
			}
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: getColumnsConfig(searchForm.ymD),
			placeholder: "조회된 데이터가 없습니다."
		})
	}
})
</script>

