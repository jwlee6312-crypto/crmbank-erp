<!--
	=============================================================
	프로그램명	: 품목별판매계획대실적 (Sale Plan vs Performance by Item)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [표준화] HSOD100U 팝업 패턴 + 고밀도 폰트(12px) + 요약바 제거
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hspl200s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-graph-up me-2 text-primary" style="font-size: 18px;"></i>
        매출계획 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목별판매계획대실적 (HSPL200S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-outline-secondary" @click="print('Print')">인쇄</button>
        <button class="btn-erp btn-outline-success" @click="print('Excel')">엑셀</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 80px;" /><col style="width: 150px;" />
              <col style="width: 80px;" /><col style="width: 280px;" />
              <col style="width: 80px;" /><col style="width: 250px;" />
              <col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">연&nbsp;&nbsp;&nbsp;&nbsp;도</th>
                <td>
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="searchData.YYYY" type="number" class="form-control form-control-sm text-center fw-bold" />
                    <span class="small">년</span>
                  </div>
                </td>
                <th class="required">영업부서</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap">
                    <input v-model="searchData.DEPTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="searchData.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">영업사원</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap">
                    <input v-model="searchData.USERID" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="searchData.USERNM" type="text" class="form-control border-start-0" placeholder="사원 선택" @keyup.enter="handleOpenHelp('EMP')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('EMP')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <td></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-table me-1"></i> 품목별 월간 실적 대비표</span>
          <span class="text-muted small">※ 각 품목별로 [계획 / 실적 / 달성율] 순으로 표시됩니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative;">
          <div ref="gridElement" style="position: absolute; top:0; left:0; width:100%; height:100%;"></div>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

// 1. 상태 관리
const searchData = reactive({
  YYYY: new Date().getFullYear(),
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  USERID: authStore.USERID,
  USERNM: authStore.USERNM
})

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false, headerHozAlign: 'center' },
    columns: [
      {
        title: "품목", field: "ITEMNM", widthGrow: 2, minWidth: 200, cssClass: "fw-bold",
        formatter: (cell) => cell.getData().TYPE === '계획' ? cell.getValue() : ''
      },
      { title: "구분", field: "TYPE", width: 70, hozAlign: "center" },
      {
        title: "합계", field: "TOTAL", width: 100, hozAlign: "right",
        formatter: (cell) => {
            const data = cell.getData();
            return data.TYPE === '달성율' ? Number(cell.getValue() || 0).toFixed(2) + '%' : formatNumber(cell.getValue());
        }
      },
      ...Array.from({ length: 12 }, (_, i) => {
        const month = String(i + 1).padStart(2, '0')
        return {
          title: `${month}월`,
          field: `MM${month}`,
          width: 85,
          hozAlign: "right",
          formatter: (cell: any) => {
            const data = cell.getData();
            const val = cell.getValue();
            return data.TYPE === '달성율' ? (Number(val) || 0).toFixed(2) + '%' : formatNumber(val);
          }
        }
      })
    ],
    rowFormatter: (row) => {
        const data = row.getData();
        if (data.TYPE === '달성율') row.getElement().style.backgroundColor = "#f9f6e7";
        if (data.IS_TOTAL) {
            row.getElement().style.backgroundColor = "#dfd9bd";
            row.getElement().style.fontWeight = "bold";
        }
    }
  })
}

// 3. 기능 구현
async function search() {
  if (!searchData.DEPTCD || !searchData.USERID) return vAlertError('영업부서와 사원을 선택하세요.')
  try {
    const res = await api.post('/api/hspl/HSPL_200S_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, YYYY: searchData.YYYY,
      DEPTCD: searchData.DEPTCD, USERID: searchData.USERID
    })

    if (grid) {
      const displayData: any[] = []
      let pTotals = Array(13).fill(0); let sTotals = Array(13).fill(0)

      res.data.forEach((item: any) => {
          const plans = Array.from({ length: 12 }, (_, i) => Number(item[`PL${String(i + 1).padStart(2, '0')}`]) || 0)
          const sales = Array.from({ length: 12 }, (_, i) => Number(item[`SL${String(i + 1).padStart(2, '0')}`]) || 0)
          const planSum = plans.reduce((a, b) => a + b, 0); const saleSum = sales.reduce((a, b) => a + b, 0)

          const pRow: any = { ITEMNM: item.ITEMNM, TYPE: '계획', TOTAL: planSum }
          plans.forEach((v, idx) => { pRow[`MM${String(idx+1).padStart(2, '0')}`] = v; pTotals[idx+1] += v }); pTotals[0] += planSum; displayData.push(pRow)

          const sRow: any = { ITEMNM: item.ITEMNM, TYPE: '실적', TOTAL: saleSum }
          sales.forEach((v, idx) => { sRow[`MM${String(idx+1).padStart(2, '0')}`] = v; sTotals[idx+1] += v }); sTotals[0] += saleSum; displayData.push(sRow)

          const rRow: any = { ITEMNM: item.ITEMNM, TYPE: '달성율', TOTAL: planSum !== 0 ? (saleSum / planSum * 100) : 0 }
          plans.forEach((p, idx) => { const s = sales[idx]; rRow[`MM${String(idx+1).padStart(2, '0')}`] = p !== 0 ? (s / p * 100) : 0 }); displayData.push(rRow)
      })

      if (displayData.length > 0) {
          const tpRow: any = { ITEMNM: '합 계', TYPE: '계획', TOTAL: pTotals[0], IS_TOTAL: true }
          pTotals.slice(1).forEach((v, i) => tpRow[`MM${String(i+1).padStart(2, '0')}`] = v); displayData.push(tpRow)

          const tsRow: any = { ITEMNM: '', TYPE: '실적', TOTAL: sTotals[0], IS_TOTAL: true }
          sTotals.slice(1).forEach((v, i) => tsRow[`MM${String(i+1).padStart(2, '0')}`] = v); displayData.push(tsRow)

          const trRow: any = { ITEMNM: '', TYPE: '달성율', TOTAL: pTotals[0] !== 0 ? (sTotals[0] / pTotals[0] * 100) : 0, IS_TOTAL: true }
          pTotals.slice(1).forEach((p, i) => { const s = sTotals[i+1]; trRow[`MM${String(i+1).padStart(2, '0')}`] = p !== 0 ? (s / p * 100) : 0 }); displayData.push(trRow)
      }
      grid.setData(displayData)
      vAlert('조회되었습니다.')
    }
  } catch (e) { vAlertError('조회 실패') }
}

function handleOpenHelp(type: string) {
  if (type === 'DEPT') {
    openHelp('DEPT', (data: any) => {
      searchData.DEPTCD = data.DEPTCD;
      searchData.DEPTNM = data.DEPTNM;
    });
  } else if (type === 'EMP') {
    openHelp('EMP', (data: any) => {
      searchData.USERID = data.USERID;
      searchData.USERNM = data.USERNM;
    });
  }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, {
    YYYY: new Date().getFullYear(),
    DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
    USERID: authStore.USERID, USERNM: authStore.USERNM
  })
  grid?.clearData()
}

function print(type: string) { vAlert(`${type} 기능은 준비 중입니다.`) }
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(() => { nextTick(() => initGrid()) })
</script>

<style scoped>
.hspl200s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11px; padding: 6px 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* 입력 필드 글자 크기 및 높이 최적화 */
.form-control, .form-select { font-size: 12px !important; height: 28px !important; padding: 2px 8px !important; }
</style>
