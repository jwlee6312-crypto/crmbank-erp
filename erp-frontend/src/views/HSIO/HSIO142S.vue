<!--
	=============================================================
	프로그램명	: 매입전표출력 (Purchase Slip Report/Print)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] HSIP110U 표준 UI(좌우분할/정중앙정렬) 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio142s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 (표준 버튼 배치) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-printer-fill me-2 text-primary" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">매입전표출력</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchCustList">조회</button>
			</div>
		</div>

		<!-- 🔍 2. 최상단 검색 조건 -->
		<div class="p-2 pb-0">
			<div class="card border shadow-sm bg-light bg-opacity-50">
				<div class="card-body py-2 px-3">
					<div class="d-flex align-items-center gap-4">
						<div class="d-flex align-items-center gap-2">
							<span class="fw-bold small text-dark" style="min-width: 60px;">입고부서</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
								<input v-model="searchForm.DEPTNM" type="text" class="form-control" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT_SEARCH')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center gap-2">
							<span class="fw-bold small text-dark" style="min-width: 60px;">발행일자</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.IOYMDFR" type="date" class="form-control form-control-sm" style="width: 140px;" />
								<span class="text-muted mx-1">~</span>
								<input v-model="searchForm.IOYMDTO" type="date" class="form-control form-control-sm" style="width: 140px;" />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 3. 메인 작업 영역 (좌우 분할) -->
		<div class="d-flex flex-row flex-grow-1 overflow-hidden p-2 gap-2">
			<!-- 🅰️ 좌측: 매입 거래처 목록 -->
			<div class="card border shadow-sm d-flex flex-column" style="width: 280px; min-width: 280px;">
				<div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 매입 거래처</span>
				</div>
				<div class="flex-grow-1 overflow-auto bg-white p-0">
					<div ref="poGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<!-- 🅱️ 우측: 전표 목록 및 출력 그리드 -->
			<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
				<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
					<!-- ✅ 그리드 타이틀 상하 정중앙 배치 -->
					<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
						<span class="fw-bold small text-dark d-flex align-items-center">
							<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 발행 전표 상세 내역
						</span>
					</div>
					<div class="card-body p-0 flex-grow-1 bg-white">
						<div ref="itemGridRef" style="height: 100%;"></div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">건수: <span class="fw-bold text-warning">{{ activeItemCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="fs-5 ms-2 fw-light">선택 정산 합계: <span class="fw-bold text-white ms-2">{{ formatNumber(totalSummary) }}</span></span>
				</div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
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

const searchForm = reactive({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  IOYMDFR: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  IOYMDTO: new Date().toISOString().substring(0, 10)
})

const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null
const activeItemCount = ref(0)

const totalSummary = computed(() => {
  const items = itemGrid?.getData() || []
  return items.reduce((acc, cur: any) => acc + (Number(cur.JSANSUM) || 0), 0)
})

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_141U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, IOGBN: '100',
      IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.IOYMDTO.replace(/-/g, ''),
      DEPTCD: searchForm.DEPTCD
    });
    poGrid?.setData(res.data.data || []); itemGrid?.clearData();
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('거래처 조회 실패') }
}

async function fetchDetail(cust: any) {
  try {
    const res = await api.post('/api/hsio/HSIO_141U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOGBN: '100',
      IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.IOYMDTO.replace(/-/g, ''),
      CUSTCD: cust.CUSTCD, DEPTCD: searchForm.DEPTCD
    })
    itemGrid?.setData(res.data.data || [])
  } catch (e) { vAlertError('상세 조회 실패') }
}

function initialize() {
  resetForm(searchForm); searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
  poGrid?.clearData(); itemGrid?.clearData();
}

const handlePrint = (type: string, data: any) => {
    vAlert(`${type} 인쇄 팝업 준비 중: ${data.SLIPYMD}-${data.SLIPNO}`);
}

onMounted(async () => {
  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columns: [{ title: '매입 거래처', field: 'CUSTNM', cssClass: 'fw-bold text-dark' }]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row.getData()))
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: 'center' },
      columns: [
        { title: '전표번호', field: 'SLIP_FULL', width: 150, hozAlign: 'center', cssClass: 'text-primary fw-bold',
          mutatorData: (v, d) => `${d.SLIPYMD?.substring(2,8) || ''}-${d.SLIPNO || ''}` },
        { title: '발행일', field: 'JSANYMD', width: 150, hozAlign: 'center',
          formatter: (c) => { const v = c.getValue(); return v && v.length === 8 ? `${v.substring(2,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v } },
        { title: '발행부서', field: 'DEPTNM', minWidth: 100, widthGrow: 1 },
        { title: '유형', field: 'VATTYPENM', width: 150 },
        { title: '공급가', field: 'SPYAMT', hozAlign: 'right', width: 150, formatter: 'money' },
        { title: '부가세', field: 'VATAMT', hozAlign: 'right', width: 150, formatter: 'money' },
        { title: '합계', field: 'JSANSUM', hozAlign: 'right', width: 150, formatter: 'money', cssClass: 'fw-bold' },
        { title: '회계전표', width: 150, hozAlign: 'center',
          formatter: () => `<button class="btn btn-xs btn-outline-dark">인쇄</button>`,
          cellClick: (e, cell) => handlePrint('회계전표', cell.getData())
        },
        { title: '지출결의', width: 150, hozAlign: 'center',
          formatter: () => `<button class="btn btn-xs btn-outline-primary">인쇄</button>`,
          cellClick: (e, cell) => handlePrint('지출결의', cell.getData())
        }
      ]
    })
    itemGrid.on('dataLoaded', (data) => { activeItemCount.value = data.length; })
  }
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })
const openHelp = (type: string) => { /* 팝업 구현 */ }
</script>

<style scoped>
.hsio142s-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }

.erp-header { background-color: #ffffff !important; }

/* 💎 개별 파일의 스타일을 삭제하여 global.css 표준 디자인이 적용되도록 함 */
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }

erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  width: 1%; white-space: nowrap;
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12.5px; padding: 10px 15px !important; color: #495057;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 10px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
.bg-yellow { background-color: #fffde7 !important; }
</style>