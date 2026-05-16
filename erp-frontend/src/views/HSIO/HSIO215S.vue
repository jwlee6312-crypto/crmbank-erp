<!--
	=============================================================
	프로그램명	: 입고의뢰서 출력 (Goods Receipt Request Print)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] HSIO550U(레이아웃) + HSOD100U(스타일/정렬) 표준 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio215s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 (표준 버튼 배치 및 색상) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-printer-fill me-2 text-primary" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				입고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">입고의뢰서 출력</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchCustList">조회</button>
				<button class="btn-erp btn-print" @click="handlePrint">인쇄</button>
			</div>
		</div>

		<!-- 🔍 2. 최상단 검색 조건 (ASP 기준 항목 구성) -->
		<div class="p-2 pb-0">
			<div class="card border shadow-sm">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;"><col />
							<col style="width: 100px;"><col />
							<col style="width: 100px;"><col />
							<col style="width: 100px;"><col />
						</colgroup>
						<tbody>
							<tr>
								<th>입고창고</th>
								<td>
									<select v-model="searchForm.WHCD" class="form-select form-select-sm">
										<option value="000">전체</option>
										<option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
									</select>
								</td>
								<th class="required">입고일자</th>
								<td>
									<div class="d-flex align-items-center gap-1">
										<input v-model="searchForm.OUTYMDFR" type="date" class="form-control form-control-sm" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.OUTYMDTO" type="date" class="form-control form-control-sm" />
									</div>
								</td>
								<th>거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="searchForm.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.CUSTNM" type="text" class="form-control" placeholder="거래처 검색" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>확정여부</th>
								<td>
									<select v-model="searchForm.SLIPYN" class="form-select form-select-sm">
										<option value="Y">확정</option>
										<option value="N">미확정</option>
									</select>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📊 3. 메인 작업 영역 (좌우 분할 레이아웃) -->
		<div class="d-flex flex-row flex-grow-1 overflow-hidden p-2 gap-2">
			<!-- 🅰️ 좌측: 입고 거래처 및 번호 목록 -->
			<div class="card border shadow-sm d-flex flex-column" style="width: 320px; min-width: 320px;">
				<div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-list-check me-1"></i> 입고증 대상 목록</span>
				</div>
				<div class="flex-grow-1 overflow-auto bg-white p-0">
					<div ref="poGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<!-- 🅱️ 우측: 입고 상세 품목 그리드 -->
			<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
				<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
					<!-- ✅ 그리드 타이틀 bg-white 및 상하 중앙 정렬 -->
					<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
						<span class="fw-bold small text-dark d-flex align-items-center">
							<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 입고의뢰 상세 내역
						</span>
						<span v-if="selectedInfo" class="badge bg-primary-subtle text-primary">번호: {{ selectedInfo }}</span>
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
				<div class="col-md-3 small">건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="fs-5 ms-2 fw-light">총 합계 금액: <span class="fw-bold text-white ms-2">{{ formatNumber(totalSummary) }}</span></span>
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

const now = new Date()
const searchForm = reactive<any>({
  WHCD: '000',
  OUTYMDFR: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  OUTYMDTO: now.toISOString().substring(0, 10),
  CUSTCD: '', CUSTNM: '',
  SLIPYN: 'N'
})

const selectedInfo = ref('');
const whOptions = ref<any[]>([]);
const poGridRef = ref<HTMLDivElement | null>(null);
const itemGridRef = ref<HTMLDivElement | null>(null);
let poGrid: Tabulator | null = null;
let itemGrid: Tabulator | null = null;
const activeItemCount = ref(0);

const totalSummary = computed(() => {
  const items = itemGrid?.getData() || []
  return items.reduce((acc, cur: any) => acc + (Number(cur.JSANAMT) + Number(cur.JSANVAT) || 0), 0)
})

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_215S_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, IOGBN: '100',
      OUTYMDFR: searchForm.OUTYMDFR.replace(/-/g, ''),
      OUTYMDTO: searchForm.OUTYMDTO.replace(/-/g, ''),
      WHCD: searchForm.WHCD, CUSTCD: searchForm.CUSTCD, SLIPYN: searchForm.SLIPYN
    });
    poGrid?.setData(res.data.data || []);
    itemGrid?.clearData();
    selectedInfo.value = '';
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetail(row: any) {
  const d = row.getData();
  selectedInfo.value = `${d.IOYM}-${d.IONO}`;
  try {
    const res = await api.post('/api/hsio/HSIO_215S_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOGBN: '100',
      WHCD: searchForm.WHCD, CUSTCD: d.CUSTCD, IOYM: d.IOYM, IONO: d.IONO,
      OUTYMDFR: searchForm.OUTYMDFR.replace(/-/g, ''),
      OUTYMDTO: searchForm.OUTYMDTO.replace(/-/g, '')
    })
    itemGrid?.setData(res.data.data || [])
  } catch (e) { vAlertError('상세 내역 조회 실패') }
}

function initialize() {
  resetForm(searchForm);
  searchForm.WHCD = '000'; searchForm.SLIPYN = 'N';
  searchForm.OUTYMDFR = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
  searchForm.OUTYMDTO = now.toISOString().substring(0, 10);
  poGrid?.clearData(); itemGrid?.clearData(); selectedInfo.value = '';
}

const handlePrint = () => { vAlert('인쇄 기능을 준비 중입니다.') }
const openHelp = (type: string) => { /* 팝업 구현 */ }
const formatNumber = (val: any) => Number(val || 0).toLocaleString()

onMounted(async () => {
  api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })
     .then(r => whOptions.value = r.data.map((i:any)=>({CODE: i.CODE || i.WHCD, CDNM: i.CDNM || i.WHNM})));

  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columns: [
        { title: '거래처', field: 'CUSTNM', minWidth: 150, widthGrow: 1, cssClass: 'fw-bold' },
        { title: '입고번호', field: 'IONO_DISP', width: 120, hozAlign: 'center', mutatorData: (v, d) => `${d.IOYM}-${d.IONO}` }
      ]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row))
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: 'center' },
      columns: [
        { title: '품목명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
        { title: '규격', field: 'ITSIZE', width: 150 },
        { title: '단위', field: 'UNIT', width: 60, hozAlign: 'center' },
        { title: '수량', field: 'IOQTY', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '공급가', field: 'JSANAMT', hozAlign: 'right', width: 120, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '부가세', field: 'JSANVAT', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '합계', field: 'SUM_AMT', hozAlign: 'right', width: 130, formatter: 'money', cssClass: 'fw-bold bg-light',
          mutatorData: (v, d) => Number(d.JSANAMT || 0) + Number(d.JSANVAT || 0) }
      ]
    })
    itemGrid.on('dataLoaded', (data) => activeItemCount.value = data.length)
  }
})

const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })
</script>

<style scoped>
.hsio215s-wrapper { height: 100%; overflow: hidden; }
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
