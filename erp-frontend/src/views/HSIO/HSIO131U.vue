<!--
	=============================================================
	프로그램명	: 외부매입전표처리 (External Purchase Slip Issue)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [표준화] ASP(HSIO_131U) 기반 재작성 - 단일 그리드, 요약바 제거, 팝업 표준화
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio131u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-diff-fill me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">외부매입전표처리 (HSIO131U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="handleSave">전표발행</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 -->
    <div class="search-bar bg-light border-bottom p-2 px-3 d-flex align-items-center shadow-sm flex-shrink-0">
      <div class="d-flex align-items-center gap-4 flex-grow-1">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary">▶ 입고부서:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchForm.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('S_DEPT')" />
            <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('S_DEPT')"><i class="bi bi-search"></i></button>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary">▶ 정산일자:</span>
          <div class="d-flex align-items-center gap-1">
            <input v-model="searchForm.IOYMDFR" type="date" class="form-control form-control-sm" style="width: 140px;" />
            <span class="text-muted mx-1">~</span>
            <input v-model="searchForm.IOYMDTO" type="date" class="form-control form-control-sm" style="width: 140px;" />
          </div>
        </div>
      </div>
    </div>

    <!-- 💡 3. 메인 작업 영역 -->
    <div class="flex-grow-1 d-flex flex-column overflow-hidden p-2 gap-2">
      <!-- 전표 발행 마스터 정보 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">전표일자</th>
                <td><input v-model="formData.SLIPYMD" type="date" class="form-control form-control-sm" /></td>
                <th class="required">발행부서</th>
                <td colspan="3">
                  <div class="input-group input-group-sm" style="width: 300px;">
                    <input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="formData.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 정산 내역 그리드 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 전표 발행 대상 내역 (외부)</span>
          <button class="btn btn-outline-primary btn-sm px-2 py-0" style="font-size: 11px;" @click="toggleAllRows">전체선택/해제</button>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative;">
          <div ref="mainGridRef" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;"></div>
        </div>
      </div>

      <!-- 카드 결제 및 비고 정보 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr class="bg-light-subtle">
                <th>카드결제</th>
                <td>
                  <div class="form-check form-switch m-0 d-flex align-items-center">
                    <input v-model="formData.CARDYN" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="cardCheck131">
                    <label class="form-check-label ms-2 small fw-bold" for="cardCheck131">카드로 결제</label>
                  </div>
                </td>
                <th>카드번호</th>
                <td colspan="3">
                  <div class="input-group input-group-sm">
                    <input v-model="formData.CARDNO" type="text" class="form-control" placeholder="카드번호 선택" :readonly="formData.CARDYN !== 'Y'" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CARD')" :disabled="formData.CARDYN !== 'Y'"><i class="bi bi-search"></i></button>
                    <input v-model="formData.CARDNM" type="text" class="form-control bg-light" readonly style="flex-grow: 2;" />
                  </div>
                </td>
              </tr>
              <tr>
                <th>특기사항</th>
                <td colspan="5"><input v-model="formData.REMARK" type="text" class="form-control form-control-sm" placeholder="전표 비고 내용 입력" /></td>
              </tr>
            </tbody>
          </table>
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
import { useCommonHelp } from '@/composables/useCommonHelp'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const now = new Date()
const searchForm = reactive<any>({
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  IOYMDFR: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  IOYMDTO: now.toISOString().substring(0, 10)
})

const formData = reactive<any>({
  ACTKIND: 'A0',
  CMPYCD: authStore.CMPYCD,
  SLIPYMD: now.toISOString().substring(0, 10),
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  CARDYN: 'N',
  CARDNO: '',
  CARDNM: '',
  REMARK: '',
  PCLSYM: '',
  SCLSYM: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null);
let grid: Tabulator | null = null;
const activeItemCount = ref(0)

const totals = computed(() => {
  const items = grid?.getData().filter((r: any) => r.PROCYN === true) || []
  const amt = items.reduce((acc, cur: any) => acc + (Number(cur.SPYAMT) || 0), 0)
  const vat = items.reduce((acc, cur: any) => acc + (Number(cur.VATAMT) || 0), 0)
  return { amt, vat, sum: amt + vat }
})

async function fetchClosingInfo() {
	try {
		const res = await api.post('/api/hs00/HS00_000S_STR', { GUBUN: 'CL', CMPYCD: authStore.CMPYCD })
		if (res.data && res.data.length > 0) {
			formData.PCLSYM = res.data[0].PCLSYM || ''
			formData.SCLSYM = res.data[0].SCLSYM || ''
		}
	} catch (e) {}
}

const handleOpenHelp = (type: string) => {
	if (type === 'S_DEPT') {
		openHelp('DEPT', (d) => {
			searchForm.DEPTCD = d.DEPTCD;
			searchForm.DEPTNM = d.DEPTNM;
		});
	} else if (type === 'DEPT') {
		openHelp('DEPT', (d) => {
			formData.DEPTCD = d.DEPTCD;
			formData.DEPTNM = d.DEPTNM;
		});
	} else if (type === 'CARD') {
		openHelp('EMP', (d) => {
			formData.CARDNO = d.USERID;
			formData.CARDNM = d.USERNM;
		});
	}
}

async function fetchList() {
  try {
    const res = await api.post('/api/hsio/HSIO_131U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.IOYMDTO.replace(/-/g, ''),
      DEPTCD: searchForm.DEPTCD
    });
    grid?.setData(res.data.map((i: any) => ({ ...i, PROCYN: null })));
    activeItemCount.value = 0;
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('목록 조회 실패') }
}

async function handleSave() {
  const items = grid?.getData().filter((r: any) => r.PROCYN === true)
  if (!items || items.length === 0) return vAlertError('발행할 항목을 선택하세요.')

  const slipYm = formData.SLIPYMD.replace(/-/g, '').substring(0, 6);
  if (formData.PCLSYM && slipYm <= formData.PCLSYM) return vAlertError('생산정보가 마감이 되었습니다.');
  if (formData.SCLSYM && slipYm <= formData.SCLSYM) return vAlertError('영업정보가 마감이 되었습니다.');

  if (formData.CARDYN === 'Y' && !formData.CARDNO) return vAlertError('카드번호를 입력하세요.')

  if (!confirm('외부 매입전표를 발행하시겠습니까?')) return

  try {
    const res = await api.post('/api/hsio/HSIO_131U_STR', {
        ...formData,
        ACTKIND: 'A0',
        ITEMS: items,
        SLIPYMD: formData.SLIPYMD.replace(/-/g, ''),
        USERID: authStore.USERID
    })
    if (res.data?.[0]?.ERRYN === 'N') {
        vAlert('전표 발행이 완료되었습니다.');
        fetchList();
    } else {
        vAlertError(res.data?.[0]?.MSG || '발행 실패');
    }
  } catch (e) { vAlertError('발행 처리 중 오류 발생') }
}

const toggleAllRows = () => {
  const rows = grid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().PROCYN === true)
  rows.forEach(r => r.update({ PROCYN: allSelected ? null : true }))
  activeItemCount.value = grid?.getData().filter((r: any) => r.PROCYN === true).length || 0;
}

function initialize() {
  resetForm(searchForm);
  searchForm.DEPTCD = authStore.DEPTCD; searchForm.DEPTNM = authStore.DEPTNM;
  searchForm.IOYMDFR = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
  searchForm.IOYMDTO = now.toISOString().substring(0, 10);
  formData.SLIPYMD = now.toISOString().substring(0, 10);
  formData.DEPTCD = authStore.DEPTCD; formData.DEPTNM = authStore.DEPTNM;
  grid?.clearData(); activeItemCount.value = 0;
}

onMounted(async () => {
  await fetchClosingInfo();
  if (mainGridRef.value) {
    grid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center" },
      columns: [
        { title: '선택', field: 'PROCYN', hozAlign: 'center', width: 60, formatter: 'tickCross', formatterParams: { crossElement: false }, editor: true,
          cellClick: (e, cell) => {
            cell.setValue(cell.getValue() === true ? null : true);
            activeItemCount.value = grid?.getData().filter((r: any) => r.PROCYN === true).length || 0;
          }
        },
        { title: '정산일', field: 'JSANYMD', width: 100, hozAlign: 'center', formatter: (c) => c.getValue()?.replace(/(\d{4})(\d{2})(\d{2})/, '$1-$2-$3') },
        { title: '부서', field: 'DEPTNM', width: 120, hozAlign: 'left' },
        { title: '거래처', field: 'CUSTNM', minWidth: 150, widthGrow: 1, hozAlign: 'left', cssClass: 'fw-bold' },
        { title: '항목/품명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, hozAlign: 'left' },
        { title: '사업장', field: 'UNITNM', width: 120 },
        { title: '유형', field: 'VATTYPENM', width: 100 },
        { title: '공급가', field: 'SPYAMT', width: 110, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '부가세', field: 'VATAMT', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } }
      ]
    })
  }
  nextTick(() => fetchList())
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
</script>

<style scoped>
.hsio131u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.form-control, .form-select { font-size: 12px !important; height: 28px !important; padding: 2px 8px !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8fafc; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 6px 10px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>
