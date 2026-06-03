<!--
	=============================================================
	프로그램명	: 그룹별판매계획대실적 (Purchase Plan vs Performance by Group)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [표준화] 팝업 표준(useCommonHelp) 적용 및 하단 요약바 제거
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-graph-up me-2 text-primary" style="font-size: 18px;"></i>
        매출계획 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">그룹별판매계획대실적 (HSPL210S)</span>
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
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required" style="width: 100px;">연&nbsp;&nbsp;&nbsp;&nbsp;도</th>
                <td style="width: 150px;">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="searchData.yyyy" type="number" class="form-control form-control-sm text-center fw-bold" style="width: 80px;" />
                    <span class="small">년</span>
                  </div>
                </td>
                <th class="required">영업부서</th>
                <td style="width: 250px;">
                  <div class="input-group input-group-sm">
                    <input v-model="searchData.deptcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="searchData.deptnm" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">영업사원</th>
                <td style="width: 220px;">
                  <div class="input-group input-group-sm">
                    <input v-model="searchData.userid" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="searchData.usernm" type="text" class="form-control border-start-0" placeholder="사원 선택" @keyup.enter="handleOpenHelp('EMP')" />
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
          <span class="fw-bold small text-dark"><i class="bi bi-table me-1"></i> 그룹별 월간 실적 대비표</span>
          <span class="small text-muted">※ 각 그룹별로 [계획 / 실적 / 달성율] 순으로 표시됩니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridElement" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
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
  yyyy: new Date().getFullYear(),
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  userid: authStore.userid,
  usernm: authStore.usernm
})

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "자산", field: "astkindNM", width: 80, hozAlign: "center",
        formatter: (cell) => cell.getData().TYPE === '계획' ? cell.getValue() : ''
      },
      {
        title: "대분류", field: "Agrpnm", width: 120,
        formatter: (cell) => cell.getData().TYPE === '계획' ? cell.getValue() : ''
      },
      {
        title: "중분류", field: "Bgrpnm", width: 120,
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
          field: .mm${month}`,
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
  if (!searchData.deptcd || !searchData.userid) return vAlertError('영업부서와 사원을 선택하세요.')
  try {
    const res = await api.post('/api/hspl/HSPL_210S_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, yyyy: searchData.yyyy,
      deptcd: searchData.deptcd, userid: searchData.userid
    })

    if (grid.value) {
      const displayData: any[] = []
      let pTotals = Array(13).fill(0); let sTotals = Array(13).fill(0)

      res.data.forEach((item: any) => {
          const plans = Array.from({ length: 12 }, (_, i) => Number(item[`PL${String(i + 1).padStart(2, '0')}`]) || 0)
          const sales = Array.from({ length: 12 }, (_, i) => Number(item[`SL${String(i + 1).padStart(2, '0')}`]) || 0)
          const planSum = plans.reduce((a, b) => a + b, 0); const saleSum = sales.reduce((a, b) => a + b, 0)

          const pRow: any = { astkindNM: item.astkindNM, Agrpnm: item.Agrpnm, Bgrpnm: item.Bgrpnm, TYPE: '계획', TOTAL: planSum }
          plans.forEach((v, idx) => { pRow[.mm${String(idx+1).padStart(2, '0')}`] = v; pTotals[idx+1] += v }); pTotals[0] += planSum; displayData.push(pRow)

          const sRow: any = { astkindNM: item.astkindNM, Agrpnm: item.Agrpnm, Bgrpnm: item.Bgrpnm, TYPE: '실적', TOTAL: saleSum }
          sales.forEach((v, idx) => { sRow[.mm${String(idx+1).padStart(2, '0')}`] = v; sTotals[idx+1] += v }); sTotals[0] += saleSum; displayData.push(sRow)

          const rRow: any = { astkindNM: item.astkindNM, Agrpnm: item.Agrpnm, Bgrpnm: item.Bgrpnm, TYPE: '달성율', TOTAL: planSum !== 0 ? (saleSum / planSum * 100) : 0 }
          plans.forEach((p, idx) => { const s = sales[idx]; rRow[.mm${String(idx+1).padStart(2, '0')}`] = p !== 0 ? (s / p * 100) : 0 }); displayData.push(rRow)
      })

      if (displayData.length > 0) {
          const tpRow: any = { astkindNM: '합 계', Agrpnm: '', Bgrpnm: '', TYPE: '계획', TOTAL: pTotals[0], IS_TOTAL: true }
          pTotals.slice(1).forEach((v, i) => tpRow[.mm${String(i+1).padStart(2, '0')}`] = v); displayData.push(tpRow)

          const tsRow: any = { astkindNM: '', Agrpnm: '', Bgrpnm: '', TYPE: '실적', TOTAL: sTotals[0], IS_TOTAL: true }
          sTotals.slice(1).forEach((v, i) => tsRow[.mm${String(i+1).padStart(2, '0')}`] = v); displayData.push(tsRow)

          const trRow: any = { astkindNM: '', Agrpnm: '', Bgrpnm: '', TYPE: '달성율', TOTAL: pTotals[0] !== 0 ? (sTotals[0] / pTotals[0] * 100) : 0, IS_TOTAL: true }
          pTotals.slice(1).forEach((p, i) => { const s = sTotals[i+1]; trRow[.mm${String(i+1).padStart(2, '0')}`] = p !== 0 ? (s / p * 100) : 0 }); displayData.push(trRow)
      }
      grid.value.setData(displayData)
      vAlert('조회되었습니다.')
    }
  } catch (e) { vAlertError('조회 실패') }
}

function handleOpenHelp(type: string) {
  if (type === 'DEPT') openHelp('DEPT', (d) => { searchData.deptcd = d.deptcd; searchData.deptnm = d.deptnm });
  else if (type === 'EMP') openHelp('EMP', (d) => { searchData.userid = d.userid; searchData.usernm = d.usernm });
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, {
    yyyy: new Date().getFullYear(), deptcd: authStore.deptcd, deptnm: authStore.deptnm,
    userid: authStore.userid, usernm: authStore.usernm
  })
  grid?.clearData()
}

function print(type: string) { vAlert(`${type} 기능은 준비 중입니다.`) }
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(() => { nextTick(() => initGrid()) })
</script>
