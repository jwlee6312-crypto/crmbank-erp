<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba060u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-currency-dollar me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        품목별단가관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목별단가관리 (HSBA060U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-search me-1"></i> 조회 조건</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th style="width: 100px;">매입/매출</th>
                <td style="width: 200px;">
                  <select v-model="searchData.IOGBN" class="form-select form-select-sm" @change="onIogbnChange">
                    <option v-for="opt in iogbnOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th style="width: 100px;">단가구분</th>
                <td style="width: 200px;">
                  <select v-model="searchData.PRCGBN" class="form-select form-select-sm">
                    <option v-for="opt in prcgbnOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <td></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 품목별 단가 목록</span>
          <div class="d-flex align-items-center gap-3">
            <div class="form-check form-check-inline mb-0 small">
              <input class="form-check-input" type="checkbox" id="checkAllRows" v-model="allSelected" @change="toggleAllSelection">
              <label class="form-check-label text-muted" for="checkAllRows">전체선택</label>
            </div>
            <div class="form-check form-check-inline mb-0 small border-start ps-3">
              <input class="form-check-input" type="checkbox" id="checkAllStd" v-model="allStd" @change="toggleAllStd">
              <label class="form-check-label text-muted" for="checkAllStd">기본단가 일괄선택</label>
            </div>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-6 small">
          총 품목 수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건
        </div>
        <div class="col-md-6 text-end text-white-50 x-small">
          ※ 선택된 항목에 대해서만 단가 및 기본여부가 저장됩니다.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 1. 상태 관리
const searchData = reactive({
  IOGBN: '100',
  PRCGBN: '100'
})

const iogbnOptions = ref<any[]>([])
const prcgbnOptions = ref<any[]>([])

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)
const allSelected = ref(true)
const allStd = ref(false)

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
        title: "선택", field: "PROCYN", width: 50, hozAlign: "center",
        formatter: "tickCross", editor: true,
      },
      { title: "품목코드", field: "ITEMCD", width: 100, hozAlign: "center", cssClass: "fw-bold" },
      { title: "품목명", field: "ITEMNM", minWidth: 250 },
      { title: "규격", field: "ITSIZE", width: 150 },
      { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
      {
        title: "단가", field: "PRICE", width: 120, hozAlign: "right",
        editor: "number", formatter: "money", formatterParams: { precision: 2 }
      },
      {
        title: "기본단가", field: "STDYN", width: 90, hozAlign: "center",
        formatter: "tickCross", editor: true
      },
      { title: "비고", field: "REMARK", minWidth: 200, editor: "input" }
    ]
  })
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    // 매입/매출 구분 로드 (210)
    const resIo = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '210' } })
    iogbnOptions.value = resIo.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
    if (iogbnOptions.value.length) searchData.IOGBN = iogbnOptions.value[0].CODECD

    await updatePrcGbnOptions()
  } catch (e) { console.error('옵션 로드 실패') }
}

async function updatePrcGbnOptions() {
  try {
    // 단가구분 로드 (200) - 원본 ASP는 A01.asp에서 처리
    const resPrc = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '200' } })
    prcgbnOptions.value = resPrc.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
    if (prcgbnOptions.value.length) searchData.PRCGBN = prcgbnOptions.value[0].CODECD
  } catch (e) { console.error('단가구분 로드 실패') }
}

async function onIogbnChange() {
  await updatePrcGbnOptions()
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_060U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      IOGBN: searchData.IOGBN,
      PRCGBN: searchData.PRCGBN,
      LIMITOFFSET: 0,
      LIMITROWS: 9999
    })
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => ({
        ...i,
        PROCYN: true,
        STDYN: i.STDYN === 'Y',
        PRICE: Number(i.PRICE) || 0
      })))
      activeItemCount.value = res.data.length
      allSelected.value = true
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  const selectedRows = grid.value?.getData().filter((i: any) => i.PROCYN)
  if (!selectedRows || selectedRows.length === 0) {
    return vAlertError('처리할 대상이 없습니다.')
  }

  if (!confirm('품목별 단가 정보를 저장하시겠습니까?')) return

  try {
    // ASP 로직을 따라 각 행에 대해 저장 수행 (실제 프로젝트 API 구조에 맞춰 조정 필요)
    // 여기서는 루프를 돌며 개별 혹은 일괄 API 호출을 가정함
    for (const row of selectedRows) {
      await api.post('/api/hsba/HSBA_060U_STR', {
        ACTKIND: 'A0',
        CMPYCD: authStore.CMPYCD,
        IOGBN: searchData.IOGBN,
        ITEMCD: row.ITEMCD,
        PRCGBN: searchData.PRCGBN,
        PRICE: row.PRICE,
        STDYN: row.STDYN ? 'Y' : 'N',
        REMARK: row.REMARK,
        USEYN: 'Y',
        USERID: authStore.USERID
      })
    }
    vAlert('정상적으로 작업이 완료되었습니다.')
    search()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  if (grid.value) grid.value.clearData()
  activeItemCount.value = 0
}

const toggleAllSelection = () => {
  if (!grid.value) return
  const data = grid.value.getData()
  grid.value.updateData(data.map(i => ({ ...i, PROCYN: allSelected.value })))
}

const toggleAllStd = () => {
  if (!grid.value) return
  const data = grid.value.getData()
  grid.value.updateData(data.map(i => ({ ...i, STDYN: allStd.value })))
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => {
    initGrid()
    search()
  })
})
</script>

<style scoped>
.hsba060u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px 15px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 8px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
