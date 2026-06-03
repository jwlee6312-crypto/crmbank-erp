<!--
	=============================================================
	프로그램명	: 품목별 표준공정도
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 품목별로 적용되는 생산 공정 순서 및 표준 생산 능력 관리 (상하단 폰트 12px 통일 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 (표준 규격 및 함수명 통일) -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-diagram-2-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목별 표준공정도 (HPBA200U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save" :disabled="!selectedItem.itemcd">저장</button>
        <button class="btn-erp btn-excel" @click="excel">엑셀</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 (표준 erp-table-full 및 80px 레이블 규격) -->
      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <colgroup>
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">생산라인</th>
                <td>
                  <select v-model="searchData.linecd" class="form-select" style="width: 150px;" @change="search">
                    <option v-for="opt in lineOptions" :key="opt.linecd" :value="opt.linecd">
                      [{{ opt.linecd }}] {{ opt.linenm }}
                    </option>
                  </select>
                </td>
                <th class="required">재고자산</th>
                <td>
                  <select v-model="searchData.astkind" class="form-select" style="width: 120px;" @change="search">
                    <option value="200">제품</option>
                    <option value="210">반제품</option>
                  </select>
                </td>
                <td class="text-muted small px-3">
                  <i class="bi bi-info-circle me-1 text-primary"></i> 라인과 자산구분을 선택하여 품목별 표준 공정을 구성하십시오.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 메인 레이아웃 (좌: 품목목록, 우: 공정설정) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 품목 목록 -->
        <div class="card border-0 shadow-sm d-flex flex-column" style="width: 350px; border-radius: 8px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-box-seam me-2 text-primary"></i> 대상 품목 리스트
          </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="itemGridElement" class="tabulator-instance flex-grow-1"></div>
            </div>
        </div>

        <!-- 우측: 공정 설정 -->
        <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center flex-shrink-0">
            <span class="fw-bold small text-dark">
              <i class="bi bi-gear-wide-connected me-2 text-primary"></i> 표준 공정 설정
              <span v-if="selectedItem.itemnm" class="badge bg-info ms-2 px-3 text-white" style="font-size: 10px;">{{ selectedItem.itemnm }} ({{ selectedItem.itemcd }})</span>
            </span>
            <div class="small text-muted" style="font-size: 11px;">※ 공정별 순서 및 생산 능력을 등록하십시오.</div>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="procGridElement" class="tabulator-instance flex-grow-1"></div>
          </div>
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

const searchData = reactive({ linecd: '010', astkind: '200' })
const lineOptions = ref<any[]>([])
const selectedItem = reactive({ itemcd: '', itemnm: '', itsize: '', unit: '' })

const itemGridElement = ref<HTMLElement | null>(null); const procGridElement = ref<HTMLElement | null>(null)
let itemGrid: Tabulator | null = null; let procGrid: Tabulator | null = null
const itemCount = ref(0)

const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y' } })
    lineOptions.value = res.data
  } catch (e) {}
}

const initGrids = () => {
  if (itemGridElement.value) {
    itemGrid = new Tabulator(itemGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "데이터가 없습니다.", selectable: 1,
      columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
      columns: [
        { title: "코드", field: "itemcd", width: 90, hozAlign: "center", cssClass: "fw-bold text-primary" },
        { title: "품목명", field: "itemnm", minWidth: 150, cssClass: "fw-bold" },
        { title: "단위", field: "unit", width: 60, hozAlign: "center" }
      ],
    })
    itemGrid.on("rowClick", (e, row) => { const d = row.getData(); Object.assign(selectedItem, d); fetchProcesses(d.itemcd) })
  }

  if (procGridElement.value) {
    procGrid = new Tabulator(procGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "품목을 선택하세요.",
      columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
      columns: [
        { title: "선택", field: "useyn", width: 60, hozAlign: "center", formatter: "tickCross", editor: true, formatterParams: { crossElement: false } },
        { title: "공정", field: "progcd", width: 80, hozAlign: "center" },
        { title: "공 정 명", field: "prognm", minWidth: 180, widthGrow: 1, cssClass: "fw-bold text-primary" },
        { title: "순서", field: "dspord", width: 70, hozAlign: "center", editor: "number" },
        { title: "가동율(%)", field: "GADrate", width: 100, hozAlign: "right", editor: "number" },
        { title: "양품율(%)", field: "JUNGrate", width: 100, hozAlign: "right", editor: "number" },
        { title: "표준시간", field: "STDWORKHH", width: 100, hozAlign: "right", editor: "number" },
        { title: "일가동시간", field: "GADTMDD", width: 100, hozAlign: "right", editor: "number" },
        { title: "일생산량", field: "PQTYDD", width: 100, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 } }
      ],
    })
  }
}

async function search() {
  try {
    const res = await api.post('/api/hpba/HPBA_200U_STR', { actkind: 'S1', cmpycd: authStore.cmpycd, linecd: searchData.linecd, astkind: searchData.astkind })
    itemGrid?.setData(res.data); itemCount.value = res.data.length
    Object.assign(selectedItem, { itemcd: '', itemnm: '', itsize: '', unit: '' }); procGrid?.clearData()
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('품목 조회 실패') }
}

async function fetchProcesses(itemcd: string) {
  try {
    const res = await api.post('/api/hpba/HPBA_200U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, itemcd: itemcd, linecd: searchData.linecd, astkind: searchData.astkind })
    procGrid?.setData(res.data)
  } catch (e) { vAlertError('공정 정보 조회 실패') }
}

async function save() {
  if (!selectedItem.itemcd) return vAlertError('저장할 품목을 선택하세요.')
  const data = procGrid?.getData() || []
  if (!confirm('현재 공정 설정을 저장하시겠습니까?')) return
  try {
    for (const row of data) {
      const actkind = (row.useyn === 'Y' || row.useyn === true) ? 'A0' : 'D0'
      await api.post('/api/hpba/HPBA_200U_STR', {
        actkind: actkind, cmpycd: authStore.cmpycd, userid: authStore.userid, itemcd: selectedItem.itemcd, linecd: searchData.linecd, astkind: searchData.astkind,
        progcd: row.progcd, itsize: selectedItem.itsize, unit: selectedItem.unit, dspord: row.dspord || 0, GADrate: row.GADrate || 0, JUNGrate: row.JUNGrate || 0,
        STDWORKHH: row.STDWORKHH || 0, GADTMDD: row.GADTMDD || 0, PQTYDD: row.PQTYDD || 0, CAPAHH: 0, PQTYTT: 0
      })
    }
    vAlert('정상적으로 저장되었습니다.'); fetchProcesses(selectedItem.itemcd)
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(searchData); Object.assign(searchData, { linecd: '010', astkind: '200' });
  Object.assign(selectedItem, { itemcd: '', itemnm: '', itsize: '', unit: '' });
  itemGrid?.clearData(); procGrid?.clearData(); itemCount.value = 0; search()
}

function excel() { procGrid?.download("xlsx", `표준공정도_${selectedItem.itemnm || '품목별'}.xlsx`) }

onMounted(() => { fetchLineOptions(); nextTick(() => { initGrids(); search(); }) })
</script>
