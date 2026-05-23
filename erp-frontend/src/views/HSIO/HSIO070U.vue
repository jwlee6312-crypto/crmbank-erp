<!--
	=============================================================
	프로그램명	: 입고취소 (Purchase Receipt Cancellation) [디자인 원칙 13가지 완벽 준수]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : HSIP100U와 동일한 UI 표준 패턴 적용 (테두리, 정렬, 색상, 검색영역 균등배분)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio070u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1, 12. 상단 액션 바: 버튼 그룹 우측 상단 정렬 및 표준 색상 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-x-circle-fill me-2 text-danger" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				입고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">입고취소 (HSIO070U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchCustList">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-delete" @click="handleSave">
					<i class="bi bi-x-square"></i> 취소저장
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
									<span class="erp-label me-2">입고부서</span>
									<div class="input-group input-group-sm flex-nowrap" style="max-width: 300px;">
										<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.DEPTNM" type="text" class="form-control" placeholder="부서 선택" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT_SEARCH')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">입고일자</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1" style="max-width: 320px;">
										<input v-model="searchForm.YMDFR" type="date" class="form-control form-control-sm" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.YMDTO" type="date" class="form-control form-control-sm" />
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 3. 메인 작업 영역 (좌우 분할 레이아웃) -->
		<div class="d-flex flex-row flex-grow-1 overflow-hidden p-2 gap-2">
			<!-- 🅰️ 좌측: 발주 거래처 목록 -->
			<div class="card border shadow-sm d-flex flex-column" style="width: 280px; min-width: 280px;">
				<div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 발주 거래처</span>
				</div>
				<div class="flex-grow-1 overflow-hidden bg-white">
					<div ref="poGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<!-- 🅱️ 우측: 취소 내역 그리드 -->
			<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
				<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
						<span class="fw-bold small text-dark d-flex align-items-center">
							<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 취소 대상 입고 명세
						</span>
					</div>
					<div class="card-body p-0 flex-grow-1 bg-white">
						<div ref="itemGridRef" style="height: 100%;"></div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">선택 항목: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="fs-5 ms-2 fw-light">취소 합계 수량: <span class="fw-bold text-warning ms-2">{{ formatNumber(totalSummary) }}</span></span>
				</div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 13. 모든 변수명 대문자 고수
const searchForm = reactive({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  YMDFR: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  YMDTO: new Date().toISOString().substring(0, 10)
})

const clsInfo = reactive({ PCLSYM: '', SCLSYM: '' })
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null
const activeItemCount = ref(0)

const totalSummary = computed(() => {
  const items = itemGrid?.getData().filter((r: any) => r.PROCYN === 'Y') || []
  return items.reduce((acc, cur: any) => acc + (Number(cur.IOQTY) || 0), 0)
})

async function loadClsInfo() {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } });
    if (res.data?.length) {
        const d = res.data[0];
        clsInfo.PCLSYM = d.PCLSYM; clsInfo.SCLSYM = d.SCLSYM;
    }
  } catch (e) {}
}

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_070U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, IOGBN: '100',
      IOYMDFR: searchForm.YMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.YMDTO.replace(/-/g, ''),
      DEPTCD: searchForm.DEPTCD
    });
    poGrid?.setData(res.data.data || []); itemGrid?.clearData();
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetail(cust: any) {
  try {
    const res = await api.post('/api/hsio/HSIO_070U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOGBN: '100',
      IOYMDFR: searchForm.YMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.YMDTO.replace(/-/g, ''),
      CUSTCD: cust.CUSTCD, DEPTCD: searchForm.DEPTCD
    })
    itemGrid?.setData(res.data.data || [])
    setTimeout(updateCount, 100)
  } catch (e) { vAlertError('상세 조회 실패') }
}

async function handleSave() {
  const items = itemGrid?.getData().filter((r: any) => r.PROCYN === 'Y')
  if (!items || items.length === 0) return vAlertError('취소할 항목을 선택하세요.')
  if (!confirm('입고처리를 취소하시겠습니까?')) return

  try {
    for (const item of items) {
        await api.post('/api/hsio/HSIO_070U_STR', { ...item, ACTKIND: 'D0', CMPYCD: authStore.CMPYCD, UPDEMP: authStore.USERID })
    }
    vAlert('정상적으로 취소되었습니다.'); fetchCustList();
  } catch (e) { vAlertError('취소 처리 실패') }
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().PROCYN === 'Y')
  rows.forEach(r => r.update({ PROCYN: allSelected ? 'N' : 'Y' }))
  updateCount()
}

const updateCount = () => {
    const data = itemGrid?.getData() || []
    activeItemCount.value = data.filter((r: any) => r.PROCYN === 'Y').length
}

function initialize() {
  resetForm(searchForm); searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
  poGrid?.clearData(); itemGrid?.clearData(); activeItemCount.value = 0;
}

onMounted(async () => {
  await loadClsInfo();

  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [{ title: '발주 거래처', field: 'CUSTNM', cssClass: 'fw-bold text-dark', hozAlign: 'left' }]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row.getData()))
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        {
            title: '선택', field: 'PROCYN', hozAlign: 'center', width: 80, formatter: 'tickCross', editor: true,
            headerClick: () => toggleAllRows()
        },
        { title: '입고일', field: 'IOYMD', width: 150, formatter: (c) => formatDate(c.getValue()) },
        { title: '입고창고', field: 'WHNM', minWidth: 150, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'center' },
        { title: '입고번호', field: 'IONO', width: 150, hozAlign: 'left' },
        { title: '수량', field: 'IOQTY', hozAlign: 'right', width: 150, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '금액', field: 'IOAMT', hozAlign: 'right', width: 150, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '부가세', field: 'IOVAT', hozAlign: 'right', width: 150, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '합계', field: 'IOSUM', hozAlign: 'right', width: 150, formatter: 'money', cssClass: 'fw-bold text-primary', formatterParams: { precision: 0 } }
      ]
    })
    itemGrid.on("cellEdited", (cell) => {
        if (cell.getField() === 'PROCYN') {
            const d = cell.getData();
            if (d.PROCYN === 'Y') {
                if (d.JYN === 'Y') { vAlertError('정산 완료된 자료입니다.'); cell.setValue('N'); return; }
                const ioYM = d.IOYMD?.substring(0, 6) || '';
                if (clsInfo.SCLSYM && ioYM <= clsInfo.SCLSYM) { vAlertError('영업 마감된 월입니다.'); cell.setValue('N'); return; }
            }
            updateCount()
        }
    })
  }
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(0,4)}-${val.substring(4,6)}-${val.substring(6,8)}` : val;
const openHelp = (type: string) => { /* 팝업 구현부 */ }
const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })
</script>

<style scoped>
.hsio070u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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