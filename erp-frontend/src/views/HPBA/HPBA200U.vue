<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpba200u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-diagram-2-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목별 표준공정도 (HPBA200U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchItems">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="saveData" :disabled="!selectedItem.ITEMCD">
          <i class="bi bi-save"></i> 저장
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border-0 shadow-sm overflow-hidden" style="border-radius: 8px;">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <colgroup>
              <col style="width: 100px;"><col>
              <col style="width: 100px;"><col>
              <col>
            </colgroup>
            <tbody>
              <tr>
                <th class="required">생산라인</th>
                <td>
                  <!-- 💎 팝업에서 콤보박스로 변경 -->
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 150px;" @change="fetchItems">
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
                </td>
                <th class="required">재고자산</th>
                <td>
                  <select v-model="searchData.ASTKIND" class="form-select form-select-sm" style="width: 120px;" @change="fetchItems">
                    <option value="200">제품</option>
                    <option value="210">반제품</option>
                  </select>
                </td>
                <td class="text-muted small">
                  <i class="bi bi-info-circle me-1"></i> 라인과 자산구분을 선택하여 품목별 표준 공정을 구성하십시오.
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
          <div class="card-header bg-light py-2 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-box-seam me-1 text-primary"></i> 대상 품목 리스트
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
            <div ref="itemGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <!-- 우측: 공정 설정 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark">
                <i class="bi bi-gear-wide-connected me-1 text-primary"></i> 표준 공정 정보
                <span v-if="selectedItem.ITEMNM" class="badge bg-info ms-2 px-3">{{ selectedItem.ITEMNM }} ({{ selectedItem.ITEMCD }})</span>
              </span>
              <div class="small text-muted">※ 공정을 체크하고 순서 및 생산 능력을 입력하세요.</div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
              <div ref="procGridElement" style="height: 100%;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">품목수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 품목을 선택하면 해당 라인에서 사용 가능한 모든 공정이 나열됩니다.
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
  LINECD: '010',
  ASTKIND: '200'
})

const lineOptions = ref<any[]>([])
const selectedItem = reactive({ ITEMCD: '', ITEMNM: '', ITSIZE: '', UNIT: '' })

const itemGridElement = ref<HTMLElement | null>(null)
const procGridElement = ref<HTMLElement | null>(null)
let itemGrid: Tabulator | null = null
let procGrid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 기초 코드 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data
  } catch (e) {}
}

// 2. 그리드 초기화
const initGrids = () => {
  if (itemGridElement.value) {
    itemGrid = new Tabulator(itemGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "데이터가 없습니다.",
      selectable: 1,
      columns: [
        { title: "코드", field: "ITEMCD", width: 90, hozAlign: "center" },
        { title: "품목명", field: "ITEMNM", minWidth: 150 },
        { title: "단위", field: "UNIT", width: 60, hozAlign: "center" }
      ],
    })

    itemGrid.on("rowClick", (e, row) => {
        const data = row.getData()
        Object.assign(selectedItem, data)
        fetchProcesses(data.ITEMCD)
    })
  }

  if (procGridElement.value) {
    procGrid = new Tabulator(procGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "품목을 선택하세요.",
      columns: [
        { title: "선택", field: "USEYN", width: 60, hozAlign: "center", formatter: "tickCross", editor: true, formatterParams: { crossElement: false } },
        { title: "공정", field: "PROGCD", width: 80, hozAlign: "center", cssClass: "bg-light" },
        { title: "공 정 명", field: "PROGNM", minWidth: 180, cssClass: "fw-bold bg-light" },
        { title: "순서", field: "DSPORD", width: 70, hozAlign: "center", editor: "number" },
        { title: "가동율(%)", field: "GADRATE", width: 100, hozAlign: "right", editor: "number" },
        { title: "양품율(%)", field: "JUNGRATE", width: 100, hozAlign: "right", editor: "number" },
        { title: "표준시간", field: "STDWORKHH", width: 100, hozAlign: "right", editor: "number" },
        { title: "일가동시간", field: "GADTMDD", width: 100, hozAlign: "right", editor: "number" },
        { title: "일생산량", field: "PQTYDD", width: 100, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 } }
      ],
    })
  }
}

// 3. 비즈니스 로직
const fetchItems = async () => {
  try {
    const res = await api.post('/api/hpba/HPBA_200U_STR', {
      ACTKIND: 'S1', // List of items
      CMPYCD: authStore.CMPYCD,
      LINECD: searchData.LINECD,
      ASTKIND: searchData.ASTKIND
    })
    itemGrid?.setData(res.data)
    itemCount.value = res.data.length

    // 초기화
    Object.assign(selectedItem, { ITEMCD: '', ITEMNM: '', ITSIZE: '', UNIT: '' })
    procGrid?.clearData()
  } catch (e) { vAlertError('품목 조회 실패') }
}

const fetchProcesses = async (itemcd: string) => {
  try {
    const res = await api.post('/api/hpba/HPBA_200U_STR', {
      ACTKIND: 'S0', // List of all possible processes for the item with USEYN
      CMPYCD: authStore.CMPYCD,
      ITEMCD: itemcd,
      LINECD: searchData.LINECD,
      ASTKIND: searchData.ASTKIND
    })
    procGrid?.setData(res.data)
  } catch (e) { vAlertError('공정 정보 조회 실패') }
}

const saveData = async () => {
  if (!selectedItem.ITEMCD) return vAlertError('저장할 품목을 선택하세요.')

  const data = procGrid?.getData() || []
  if (!confirm('현재 공정 설정을 저장하시겠습니까?')) return

  try {
    for (const row of data) {
      const actkind = (row.USEYN === 'Y' || row.USEYN === true) ? 'A0' : 'D0'

      await api.post('/api/hpba/HPBA_200U_STR', {
        ACTKIND: actkind,
        CMPYCD: authStore.CMPYCD,
        USERID: authStore.USERID,
        ITEMCD: selectedItem.ITEMCD,
        LINECD: searchData.LINECD,
        ASTKIND: searchData.ASTKIND,
        PROGCD: row.PROGCD,
        ITSIZE: selectedItem.ITSIZE,
        UNIT: selectedItem.UNIT,
        DSPORD: row.DSPORD || 0,
        GADRATE: row.GADRATE || 0,
        JUNGRATE: row.JUNGRATE || 0,
        STDWORKHH: row.STDWORKHH || 0,
        GADTMDD: row.GADTMDD || 0,
        PQTYDD: row.PQTYDD || 0,
        CAPAHH: 0, PQTYTT: 0
      })
    }
    vAlert('정상적으로 저장되었습니다.')
    fetchProcesses(selectedItem.ITEMCD)
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { LINECD: '010', ASTKIND: '200' })
  Object.assign(selectedItem, { ITEMCD: '', ITEMNM: '', ITSIZE: '', UNIT: '' })
  itemGrid?.clearData()
  procGrid?.clearData()
  itemCount.value = 0
  fetchItems()
}

onMounted(() => {
  fetchLineOptions()
  nextTick(() => { initGrids(); fetchItems(); })
})
</script>

<style scoped>
.hpba200u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }
.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-save { background-color: #2ec4b6 !important; color: #fff !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }
</style>
