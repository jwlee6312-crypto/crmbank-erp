<!--
	=============================================================
	프로그램명	: 품목별 월별 매출 현황 [디자인 원칙 13가지 + 검색영역 단일행 균등배분]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 품목별/월별(1월~12월) 매출 수량 및 금액 추이 현황 조회
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1, 11, 12. 상단 액션 바: 버튼 그룹 우측 상단 정렬 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar3 me-2 text-primary" style="font-size: 18px;"></i>
				매출분석 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">품목별 월별 매출 현황 (HSST520S)</span>
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

		<!-- 🔍 9. 최상단 검색 항목 영역 (단일행 균등 배분 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 33.3%;" />
						<col style="width: 33.3%;" />
						<col style="width: 33.4%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">판매부서</span>
									<div class="input-group input-group-sm flex-nowrap" style="max-width: 300px;">
										<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">조회연월</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1" style="max-width: 250px;">
										<select v-model="searchForm.yy" class="form-select form-select-sm">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
										</select>
										<select v-model="searchForm.mm" class="form-select form-select-sm">
											<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
										</select>

									</div>
								</div>
							</td>
							<td><!-- 균등 배분 공간 --></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 6, 8. 중앙 그리드 영역 (중앙 정렬 표준 적용) -->
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

const now = new Date();
const currentyy = String(now.getFullYear());
const currentmm = String(now.getMonth() + 1).padStart(2, '0');

// 13. 모든 변수명 대문자 고수
const searchForm = reactive({
	deptcd: authStore.deptcd,
	deptnm: authStore.deptnm,
	yy: currentyy,
    mm: currentmm
})

const yearOptions = ref<string[]>(Array.from({ length: 5 }, (_, i) => String(now.getFullYear() - i)));
const monthOptions = ref<string[]>(Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0')));

const rowCount = ref(0)
const totals = reactive({ wgttot: 0, amttot: 0 })
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/hsst/HSST_520S_STR', {
			...searchForm,
			cmpycd: authStore.cmpycd
		})
		const data = res.data || []
		mainGrid?.setData(data)
		rowCount.value = Math.ceil(data.length / 2) // 수량/금액 2행 1세트

		totals.wgttot = data.filter((i:any)=>i.gubun === '수량').reduce((acc: number, cur: any) => acc + (Number(cur.total) || 0), 0)
		totals.amttot = data.filter((i:any)=>i.gubun === '금액').reduce((acc: number, cur: any) => acc + (Number(cur.total) || 0), 0)

		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm);
	searchForm.deptcd = authStore.deptcd; searchForm.deptnm = authStore.deptnm;
	searchForm.yy = currentyy; searchForm.mm = currentmm;
	mainGrid?.clearData(); rowCount.value = 0;
	totals.wgttot = 0; totals.amttot = 0;
}

const excel = () => mainGrid?.download("xlsx", "품목별월별매출현황.xlsx")
const print = () => vAlert('인쇄 기능을 준비 중입니다.')

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
	}
	modalVisible.value = true
}

const formatNumber = (val: any) => Number(val || 0).toLocaleString()

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 80 },
			columns: [
				{
					title: "품목", field: "itemnm", minWidth: 200, widthGrow: 2, hozAlign: "left", cssClass: "fw-bold", frozen: true,
					formatter: (cell) => {
						const d = cell.getData();
						return d.gubun === '수량' ? `[${d.itemcd}] ${d.itemnm}` : '';
					}
				},
				{ title: "구분", field: "gubun", width: 100, frozen: true },
				{ title: "합계", field: "total", hozAlign: "right", width: 110, formatter: "money", formatterParams: { precision: (c:any) => c.getData().gubun === '수량' ? 0 : 0 }, cssClass: "bg-light fw-bold" },
				{ title: "01월", field: "m01", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "02월", field: "m02", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "03월", field: "m03", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "04월", field: "m04", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "05월", field: "m05", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "06월", field: "m06", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "07월", field: "m07", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "08월", field: "m08", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "09월", field: "m09", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "10월", field: "m10", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "11월", field: "m11", hozAlign: "right", width: 90, formatter: "money" },
				{ title: "12월", field: "m12", hozAlign: "right", width: 90, formatter: "money" }
			]
		})
	}
})
</script>
