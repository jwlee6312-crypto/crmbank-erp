<!--
	=============================================================
	프로그램명	: 매입정산 (Purchase Settlement)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [표준 필드 적용] TAXUNIT, UNITNM / CODECD, CODENM
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio110u-wrapper d-flex flex-column h-100 bg-white p-0">
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				매입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">매입정산 (HSIO110U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchCustList">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
			</div>
		</div>

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
							<span class="fw-bold small text-dark" style="min-width: 60px;">입고일자</span>
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

		<div class="d-flex flex-row flex-grow-1 overflow-hidden p-2 gap-2">
			<div class="card border shadow-sm d-flex flex-column" style="width: 280px; min-width: 280px;">
				<div class="card-header bg-light py-1 px-3 border-bottom">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 매입 거래처 목록</span>
				</div>
				<div class="flex-grow-1 overflow-auto bg-white p-0">
					<div ref="poGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
				<div class="card border shadow-sm overflow-hidden">
					<div class="card-body p-0">
						<table class="erp-table-full">
							<colgroup>
								<col style="width: 100px;"><col />
								<col style="width: 100px;"><col />
								<col style="width: 100px;"><col />
							</colgroup>
							<tbody>
								<tr>
									<th>거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
									<td>
										<div class="input-group input-group-sm">
											<input v-model="formData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
											<input v-model="formData.CUSTNM" type="text" class="form-control bg-light" readonly />
										</div>
									</td>
									<th>입고부서</th>
									<td>
										<div class="input-group input-group-sm">
											<input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
											<input v-model="formData.DEPTNM" type="text" class="form-control bg-light" readonly />
										</div>
									</td>
									<th>사업장/유형</th>
									<td>
										<div class="d-flex gap-1">
											<select v-model="formData.TAXUNIT" class="form-select form-select-sm" style="width: 50%;">
												<option value="">-- 사업장 --</option>
												<option v-for="opt in saOptions" :key="opt.TAXUNIT" :value="opt.TAXUNIT">{{ opt.UNITNM }}</option>
											</select>
											<select v-model="formData.VATTYPE" class="form-select form-select-sm" style="width: 50%;">
												<option value="">-- 부가세 --</option>
												<option v-for="opt in vatOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
											</select>
										</div>
									</td>
								</tr>
								<tr>
									<th>발&nbsp;행&nbsp;일</th>
									<td><input v-model="formData.PUBYMD" type="date" class="form-control form-control-sm" /></td>
									<th>공급가액</th>
									<td><input :value="formatNumber(totals.amt)" class="form-control form-control-sm text-end bg-light fw-bold" readonly /></td>
									<th>총&nbsp;&nbsp;합&nbsp;&nbsp;계</th>
									<td><input :value="formatNumber(totals.sum)" class="form-control form-control-sm text-end bg-warning-subtle fw-bold text-dark" readonly /></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>

				<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
					<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
						<span class="fw-bold small text-dark d-flex align-items-center">
							<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 정산 대상 입고 명세
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

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const searchForm = reactive({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  IOYMDFR: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  IOYMDTO: now.toISOString().substring(0, 10)
})

const formData = reactive<any>({
  ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, CUSTCD: '', CUSTNM: '', DEPTCD: '', DEPTNM: '',
  TAXUNIT: '', VATTYPE: '010', PUBYMD: now.toISOString().substring(0, 10)
})

const saOptions = ref<any[]>([]); const vatOptions = ref<any[]>([]);
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null

const totals = computed(() => {
  const items = itemGrid?.getData().filter((r: any) => r.PROCYN === 'Y') || []
  const amt = items.reduce((acc, cur: any) => acc + (Number(cur.JSANAMT) || 0), 0)
  const vat = items.reduce((acc, cur: any) => acc + (Number(cur.JSANVAT) || 0), 0)
  return { amt, vat, sum: amt + vat }
})

async function fetchOptions() {
  try {
    const [resSa, resVat] = await Promise.all([
      api.post('/api/ha00/HA00_00P_STR', { GUBUN: 'SA', CMPYCD: authStore.CMPYCD }),
      api.post('/api/ha00/HA00_00P_STR', { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '120' })
    ])

    // 사업장: TAXUNIT, UNITNM 필드 고정
    saOptions.value = (resSa.data || []).map((i: any) => ({
      TAXUNIT: String(i.TAXUNIT || i.CODECD || i.CODE || '').trim(),
      UNITNM: String(i.UNITNM || i.CODENM || i.CDNM || '').trim()
    }))

    // 부가세 유형: CODECD, CODENM 필드 고정
    vatOptions.value = (resVat.data || []).map((i: any) => ({
      CODECD: String(i.CODECD || i.CODE || i.TAXUNIT || '').trim(),
      CODENM: String(i.CODENM || i.CDNM || i.UNITNM || '').trim()
    }))
  } catch (e) { console.error('기초 코드 로드 실패') }
}

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_110U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, IOGBN: '100',
      IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.IOYMDTO.replace(/-/g, ''),
      DEPTCD: searchForm.DEPTCD
    });
    poGrid?.setData(res.data || []); itemGrid?.clearData(); vAlert('조회되었습니다.')
  } catch (e) { vAlertError('목록 조회 실패') }
}

async function fetchDetail(row: any) {
  const d = row.getData();
  formData.CUSTCD = d.CUSTCD; formData.CUSTNM = d.CUSTNM; formData.DEPTCD = d.DEPTCD; formData.DEPTNM = d.DEPTNM;
  try {
    const res = await api.post('/api/hsio/HSIO_110U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOGBN: '100',
      CUSTCD: d.CUSTCD, DEPTCD: searchForm.DEPTCD,
      IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.IOYMDTO.replace(/-/g, '')
    })
    itemGrid?.setData(res.data || [])
  } catch (e) { vAlertError('상세 조회 실패') }
}

async function handleSave() {
  const items = itemGrid?.getData().filter((r: any) => r.PROCYN === 'Y')
  if (!items || items.length === 0) return vAlertError('정산 처리할 품목을 선택하세요.')
  if (!confirm('정산 작업을 하시겠습니까?')) return
  try {
    await api.post('/api/hsio/HSIO_110U_STR', { ...formData, ACTKIND: 'A0', ITEMS: items })
    vAlert('정산 처리가 완료되었습니다.'); fetchCustList();
  } catch (e) { vAlertError('저장 실패') }
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().PROCYN === 'Y')
  rows.forEach(r => r.update({ PROCYN: allSelected ? 'N' : 'Y' }))
}

function initialize() {
  resetForm(searchForm); poGrid?.clearData(); itemGrid?.clearData();
  searchForm.IOYMDFR = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
  searchForm.IOYMDTO = now.toISOString().substring(0, 10);
}

onMounted(async () => {
  await fetchOptions();
  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columns: [{ title: '매입 거래처', field: 'CUSTNM', cssClass: 'fw-bold text-dark' }]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row))
  }
  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 80 },
      columns: [
        { title: '선택', field: 'PROCYN', hozAlign: 'center', width: 80, formatter: 'tickCross', editor: true },
        { title: '입고일', field: 'SALSYMD', width: 150, hozAlign: 'center', formatter: (c) => formatDate(c.getValue()) },
        { title: '품목명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
        { title: '규격', field: 'ITSIZE', width: 200 },
        { title: '단위', field: 'UNIT', width: 60, hozAlign: 'center' },
        { title: '수량', field: 'JSANQTY', hozAlign: 'right', width: 150, formatter: 'money' },
        { title: '공급가', field: 'JSANAMT', hozAlign: 'right', width: 150, formatter: 'money' },
        { title: '부가세', field: 'JSANVAT', hozAlign: 'right', width: 150, formatter: 'money' }
      ]
    })
  }
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(4,6)}-${val.substring(6,8)}` : val;
const openHelp = (type: string) => { /* 팝업 로직 */ }
</script>

<style scoped>
.hsio110u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.form-control, .form-select { font-size: 12px !important; height: 28px !important; padding: 2px 8px !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8fafc; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 12px; padding: 6px 10px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>
