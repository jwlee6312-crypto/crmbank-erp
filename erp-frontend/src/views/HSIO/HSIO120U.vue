<!--
	=============================================================
	프로그램명	: 입고정산취소 (Purchase Settlement Cancellation)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] HSIO550U(레이아웃) + HSOD100U(스타일/정렬) 표준 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio120u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 (표준 버튼 배치) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-arrow-counterclockwise me-2 text-danger" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">입고정산취소</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchCustList">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">정산취소</button>
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
							<span class="fw-bold small text-dark" style="min-width: 60px;">정산일자</span>
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
			<!-- 🅰️ 좌측: 정산 거래처 목록 -->
			<div class="card border shadow-sm d-flex flex-column" style="width: 280px; min-width: 280px;">
				<div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 매입 거래처</span>
				</div>
				<div class="flex-grow-1 overflow-auto bg-white p-0">
					<div ref="poGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<!-- 🅱️ 우측: 정산 상세 및 취소 그리드 -->
			<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
				<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
					<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
						<span class="fw-bold small text-dark d-flex align-items-center">
							<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 취소 대상 정산 명세
						</span>
						<button class="btn btn-xs btn-outline-secondary px-2" style="height: 28px; font-size: 11px;" @click="toggleAllRows">전체선택</button>
					</div>
					<div class="card-body p-0 flex-grow-1 bg-white">
						<div ref="itemGridRef" style="height: 100%;"></div>
					</div>
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

const clsInfo = reactive({ PCLSYM: '', SCLSYM: '', WCLSYM: '' })
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null
const activeItemCount = ref(0)

const totalSummary = computed(() => {
  const items = itemGrid?.getData().filter((r: any) => r.PROCYN === 'Y') || []
  return items.reduce((acc, cur: any) => acc + (Number(cur.JSANSUM) || 0), 0)
})

async function loadClsInfo() {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } });
    if (res.data?.length) {
        const d = res.data[0];
        Object.assign(clsInfo, { PCLSYM: d.PCLSYM, SCLSYM: d.SCLSYM, WCLSYM: d.WCLSYM });
    }
  } catch (e) {}
}

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_120U_STR', {
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
    const res = await api.post('/api/hsio/HSIO_120U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOGBN: '100',
      IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.IOYMDTO.replace(/-/g, ''),
      CUSTCD: cust.CUSTCD, DEPTCD: searchForm.DEPTCD
    })
    itemGrid?.setData(res.data.data || [])
  } catch (e) { vAlertError('정산 내역 조회 실패') }
}

async function handleSave() {
  const items = itemGrid?.getData().filter((r: any) => r.PROCYN === 'Y')
  if (!items || items.length === 0) return vAlertError('취소할 항목을 선택하세요.')
  if (!confirm('정산자료를 취소하시겠습니까?')) return

  try {
    for (const item of items) {
        await api.post('/api/hsio/HSIO_120U_STR', {
            ...item, ACTKIND: 'D0', CMPYCD: authStore.CMPYCD, UPDEMP: authStore.USERID
        })
    }
    vAlert('정산 취소가 완료되었습니다.'); fetchCustList();
  } catch (e) { vAlertError('취소 처리 실패') }
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().PROCYN === 'Y')
  rows.forEach(r => r.update({ PROCYN: allSelected ? 'N' : 'Y' }))
}

function initialize() {
  resetForm(searchForm); searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
  poGrid?.clearData(); itemGrid?.clearData();
}

onMounted(async () => {
  await loadClsInfo();

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
      columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 80 },
      columns: [
        { title: '선택', field: 'PROCYN', hozAlign: 'center', width: 80, formatter: 'tickCross', editor: true,
          cellClick: (e, cell) => {
              const d = cell.getData();
              if (d.SLIPYMD > '00000000') { vAlertError('전표 발행된 자료입니다.'); cell.setValue('N'); return; }
              const ym = d.JSANYMD?.substring(0, 6) || '';
              if (clsInfo.SCLSYM && ym <= clsInfo.SCLSYM) { vAlertError('영업 마감된 자료입니다.'); cell.setValue('N'); return; }
          }
        },
        { title: '발행일', field: 'JSANYMD', width: 150, hozAlign: 'center', formatter: (c) => formatDate(c.getValue()) },
        { title: '정산부서', field: 'DEPTNM', minWidth: 100, widthGrow: 1,hozAlign: 'left'  },
        { title: '사업장', field: 'UNITNM', width: 150 },
        { title: '유형', field: 'VATTYPENM', width: 150 },
        { title: '공급가', field: 'SPYAMT', hozAlign: 'right', width: 150, formatter: 'money' },
        { title: '부가세', field: 'VATAMT', hozAlign: 'right', width: 150, formatter: 'money' },
        { title: '합계', field: 'JSANSUM', hozAlign: 'right', width: 150, formatter: 'money', cssClass: 'fw-bold' }
      ]
    })
    itemGrid.on('rowSelectionChanged', (data) => { activeItemCount.value = data.length; })
  }
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(2,4)}-${val.substring(4,6)}-${val.substring(6,8)}` : val;
const openHelp = (type: string) => { /* 팝업 로직 */ }
const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })
</script>

<style scoped>
.hsio120u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

/* 🚀 입력 필드 글자 크기 및 높이 최적화 (표준) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12px; padding: 6px 10px !important; color: #495057;
  white-space: nowrap;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }

:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>

