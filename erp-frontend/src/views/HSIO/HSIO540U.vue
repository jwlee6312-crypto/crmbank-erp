<!--
	=============================================================
	프로그램명	: 매출전표취소 [디자인 원칙 13가지 + 검색영역 단일행 균등배분]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 발행된 매출 전표 조회 및 취소 처리 (디자인 표준 완벽 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio540u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1, 12. 상단 액션 바: 버튼 그룹 우측 상단 정렬 및 표준 색상 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-x-fill me-2 text-danger" style="font-size: 18px;"></i>
				영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">매출전표취소 (HSIO540U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchIssuedList">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-delete" @click="handleCancelSlip">
					<i class="bi bi-x-circle"></i> 전표 취소
				</button>
			</div>
		</div>

		<!-- 🔍 9. 최상단 검색 항목 영역 (단일행 균등 배분 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 50%;" />
						<col style="width: 50%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">판매부서</span>
									<div class="input-group input-group-sm flex-nowrap" style="max-width: 300px;">
										<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.DEPTNM" type="text" class="form-control" placeholder="부서 선택" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">발행일자</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1" style="max-width: 320px;">
										<input v-model="searchForm.IOYMDFR" type="date" class="form-control form-control-sm" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.IOYMDTO" type="date" class="form-control form-control-sm" />
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 3. 중앙: 발행 대상 그리드 (상하좌우 중앙 정렬 표준) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-list-check me-2 text-primary"></i> 전표 취소 대상 목록
					</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">선택 전표: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="fs-5 ms-2 fw-light">취소 합계액: <span class="fw-bold text-warning ms-2">{{ formatNumber(totals.SUM) }}</span> 원</span>
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

// 13. 모든 변수명 대문자 고수
const searchForm = reactive({
	DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
	IOYMDFR: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
	IOYMDTO: new Date().toISOString().substring(0, 10)
})

const closingInfo = reactive({ CLSYMD: '', SCLSYM: '' })
const activeItemCount = ref(0)
const totals = reactive({ SUM: 0 })

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchIssuedList = async () => {
	try {
		const res = await api.post('/api/hsio/HSIO_540U_STR', {
			...searchForm,
			ACTKIND: 'S0',
			CMPYCD: authStore.CMPYCD,
			IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
			IOYMDTO: searchForm.IOYMDTO.replace(/-/g, '')
		})
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const handleCancelSlip = async () => {
	const selected = mainGrid?.getSelectedData()
	if (!selected || selected.length === 0) return vAlertError('취소할 전표를 선택하세요.')

	if (!confirm('선택한 전표를 삭제(취소) 하시겠습니까?')) return
	try {
		for (const item of selected) {
			await api.post('/api/hsio/HSIO_540U_STR', {
				...item,
				ACTKIND: 'D',
				CMPYCD: authStore.CMPYCD,
				IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
				IOYMDTO: searchForm.IOYMDTO.replace(/-/g, ''),
				UPDEMP: authStore.USERID
			})
		}
		vAlert('정상적으로 처리되었습니다.')
		fetchIssuedList()
	} catch (e) { vAlertError('취소 실패') }
}

const toggleAllRows = () => {
	if (!mainGrid) return
	const rows = mainGrid.getRows()
	const allSelected = mainGrid.getSelectedRows().length === rows.length
	if (allSelected) mainGrid.deselectRow()
	else mainGrid.selectRow()
}

const initialize = () => {
	resetForm(searchForm);
	searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
	searchForm.IOYMDFR = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10);
	searchForm.IOYMDTO = new Date().toISOString().substring(0, 10);
	mainGrid?.clearData(); activeItemCount.value = 0; totals.SUM = 0;
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM',
			data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
			columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
			onConfirm: (d: any) => { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM }
		})
	}
	modalVisible.value = true
}

onMounted(async () => {
	api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
		if (r.data?.length) {
			closingInfo.CLSYMD = String(r.data[0].CLSYMD || '');
			closingInfo.SCLSYM = String(r.data[0].SCLSYM || '');
		}
	});

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
			columns: [
				{
					title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 60, hozAlign: "center",
					headerClick: () => toggleAllRows()
				},
				{ title: "전표번호", field: "SLIP_FULL", width: 160, cssClass: "fw-bold text-primary",
				  formatter: (cell) => {
					  const d = cell.getData();
					  return d.SLIPYMD && d.SLIPNO ? `${d.SLIPYMD}-${d.SLIPNO}` : '';
				  }
				},
				{ title: "발행부서", field: "DEPTNM", width: 150 },
				{ title: "거래처", field: "CUSTNM", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
				{ title: "유형", field: "VATTYPENM", width: 120 },
				{ title: "공급가액", field: "SPYAMT", hozAlign: "right", width: 130, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "부가세", field: "VATAMT", hozAlign: "right", width: 110, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "합계금액", field: "JSANSUM", hozAlign: "right", width: 140, formatter: "money", cssClass: "text-danger fw-bold",
				  mutatorData: (v,d) => Number(d.SPYAMT||0) + Number(d.VATAMT||0)
				}
			]
		})
		mainGrid.on('rowSelectionChanged', (data) => {
			activeItemCount.value = data.length
			totals.SUM = data.reduce((acc, cur: any) => acc + (Number(cur.SPYAMT||0) + Number(cur.VATAMT||0)), 0)
		})
	}
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
</script>

<style scoped>
.hsio540u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.flex-shrink-0 { flex-shrink: 0 !important; }
.flex-grow-1 { flex-grow: 1 !important; min-height: 0 !important; }
.overflow-hidden { overflow: hidden !important; }
/* 🚀 입력 필드 글자 크기 및 높이 최적화 (HSBA070U 패턴) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}
.erp-table-full { width: 100%; border-collapse: collapse; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 4px 5px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 2px 4px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }

/* 🚀 팝업 가독성 표준 스타일 */
:deep(.modal-content) { background-color: #ffffff !important; }
:deep(.modal-content .tabulator) { background-color: #ffffff !important; color: #000000 !important; border: 1px solid #dee2e6 !important; }
:deep(.modal-content .tabulator-cell) { color: #000000 !important; font-size: 13px !important; padding: 8px !important; }

</style>
