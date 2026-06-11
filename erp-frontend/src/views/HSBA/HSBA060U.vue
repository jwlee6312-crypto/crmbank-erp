<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 💡 근본 해결: 최외각 wrapper에 overflow-hidden과 flex-column 적용 -->
  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바: flex-shrink-0으로 영역 고정 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
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

    <!-- 💡 2. 메인 컨텐츠 영역: flex-grow-1 및 overflow-hidden 적용 -->
    <div class="flex-grow-1 d-flex flex-column gap-2 p-2 overflow-hidden">
      <!-- 🅰️ 조회 조건 영역: flex-shrink-0으로 높이 보존 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-search me-1"></i> 조회 조건</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <tbody>
              <tr>
                <th style="width: 100px;">매입/매출</th>
                <td style="width: 220px;">
                  <select v-model="searchData.iogbn" class="form-select form-select-sm" @change="onIogbnChange">
                    <option v-for="opt in iogbnOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th style="width: 100px;">단가구분</th>
                <td style="width: 220px;">
                  <select v-model="searchData.prcgbn" class="form-select form-select-sm">
                    <option v-for="opt in prcgbnOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <td></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역: flex-grow-1 및 min-height: 0 적용 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="min-height: 0;">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 품목별 단가 목록</span>
          <div class="d-flex align-items-center gap-3">
            <div class="form-check form-switch m-0 small"><input class="form-check-input" type="checkbox" id="checkAllRows" v-model="allSelected" @change="toggleAllSelection"> <label class="form-check-label text-muted" for="checkAllRows">전체선택</label></div>
            <div class="form-check form-switch m-0 small border-start ps-3"><input class="form-check-input" type="checkbox" id="checkAllStd" v-model="allStd" @change="toggleAllStd"> <label class="form-check-label text-muted" for="checkAllStd">기본단가 일괄</label></div>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridElement" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
    <!-- 📊 하단 정보바 삭제됨 -->
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

const searchData = reactive({ iogbn: '', prcgbn: '' })
const iogbnOptions = ref<any[]>([]); const prcgbnOptions = ref<any[]>([])
const gridElement = ref<HTMLElement | null>(null); const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0); const allSelected = ref(true); const allStd = ref(false)

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%",
    pagination: "local", paginationSize: 15,
        paginationButtonCount: 5, paginationSizeSelector: [15, 30, 50],
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false, headerHozAlign: "center" },
    columns: [
      { title: "선택", field: "procyn", width: 60, hozAlign: "center", formatter: "tickCross", editor: true },
      { title: "품목코드", field: "itemcd", width: 100, hozAlign: "center", cssClass: "fw-bold text-primary" },
      { title: "품목명", field: "itemnm", minWidth: 250, hozAlign: "left" },
      { title: "규격", field: "itsize", width: 150, hozAlign: "left" },
      { title: "단위", field: "unit", width: 80, hozAlign: "center" },
      { title: "단가", field: "price", width: 120, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 } },
      { title: "기본단가", field: "STDYN", width: 90, hozAlign: "center", formatter: "tickCross", editor: true },
      { title: "비고", field: "remark", minWidth: 200, editor: "input", hozAlign: "left" }
    ]
  })
}

async function fetchOptions() {
  try {
    const resIo = await api.post('/api/hs00/HS00_000S_STR', { gubun: 'E0', gbncd: '210' })
    iogbnOptions.value = resIo.data.map((i: any) => ({
      codecd: String(i.code || i.codecd || i.code || '').trim(),
      codenm: String(i.cdnm || i.codenm || i.cdnm || '').trim()
    }))
    if (iogbnOptions.value.length) searchData.iogbn = iogbnOptions.value[0].codecd
    await updatePrcGbnOptions()
  } catch (e) { console.error('옵션 로드 실패') }
}

async function updatePrcGbnOptions() {
  try {
    const resPrc = await api.post('/api/hs00/HS00_000S_STR', { gubun: 'E0', gbncd: '200' })
    prcgbnOptions.value = resPrc.data.map((i: any) => ({
      codecd: String(i.code || i.codecd || i.code || '').trim(),
      codenm: String(i.cdnm || i.codenm || i.cdnm || '').trim()
    }))
    if (prcgbnOptions.value.length) searchData.prcgbn = prcgbnOptions.value[0].codecd
  } catch (e) { console.error('단가구분 로드 실패') }
}

async function onIogbnChange() { await updatePrcGbnOptions() }

async function search() {
  if (!searchData.iogbn || !searchData.prcgbn) return;
  try {
    const res = await api.post('/api/hsba/HSBA_060U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, iogbn: searchData.iogbn,
      itemcd: '', prcgbn: searchData.prcgbn, price: 0, STDYN: '',
      remark: '', useyn: '', userid: authStore.userid
    })
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => ({
        ...i, procyn: true, STDYN: String(i.STDYN).trim() === 'Y', price: Number(i.price) || 0
      })))
      activeItemCount.value = res.data.length; allSelected.value = true
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  const selectedRows = grid.value?.getData().filter((i: any) => i.procyn)
  if (!selectedRows || selectedRows.length === 0) return vAlertError('처리할 대상이 없습니다.')
  if (!confirm('품목별 단가 정보를 저장하시겠습니까?')) return
  try {
    for (const row of selectedRows) {
      await api.post('/api/hsba/HSBA_060U_STR', {
        actkind: 'A0', cmpycd: authStore.cmpycd, iogbn: searchData.iogbn,
        itemcd: row.itemcd, prcgbn: searchData.prcgbn, price: row.price,
        STDYN: row.STDYN ? 'Y' : 'N', remark: row.remark, useyn: 'Y', userid: authStore.userid
      })
    }
    vAlert('정상적으로 작업이 완료되었습니다.'); search()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() { if (grid.value) grid.value.clearData(); activeItemCount.value = 0 }
const toggleAllSelection = () => { if (!grid.value) return; grid.value.updateData(grid.value.getData().map(i => ({ ...i, procyn: allSelected.value }))) }
const toggleAllStd = () => { if (!grid.value) return; grid.value.updateData(grid.value.getData().map(i => ({ ...i, STDYN: allStd.value }))) }

onMounted(async () => { await fetchOptions(); nextTick(() => { initGrid(); if (searchData.iogbn) search() }) })
</script>

