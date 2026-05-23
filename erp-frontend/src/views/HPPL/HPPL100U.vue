<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hppl100u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calendar-check me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목별생산계획 (HPPL100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="saveData">저장</button>
        <button class="btn-erp btn-excel" @click="exportExcel">엑셀</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">연&nbsp;&nbsp;월</th>
                <td>
                  <input v-model="uiYYMM" type="month" class="form-control form-control-sm" style="width: 150px;" @change="onMonthChange" />
                </td>
                <th class="required">생산라인</th>
                <td>
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 200px;" @change="onLineChange">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
                </td>
                <th class="required">생산공정</th>
                <td>
                  <select v-model="searchData.PROGCD" class="form-select form-select-sm" style="width: 200px;" @change="fetchList">
                    <option value="">공정 선택</option>
                    <option v-for="opt in progOptions" :key="opt.PROGCD" :value="opt.PROGCD">
                      [{{ opt.PROGCD }}] {{ opt.PROGNM }}
                    </option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-task me-1"></i> 생산 계획 상세</span>
          <div class="d-flex gap-3 align-items-center">
            <div class="d-flex align-items-center gap-1">
              <span class="small text-muted">전체선택</span>
              <input type="checkbox" v-model="allSelected" @change="toggleAllSelection" class="form-check-input" />
            </div>
            <div class="small text-muted">※ 우측으로 스크롤하여 1일부터 말일까지 확인하세요.</div>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative;">
          <div ref="gridElement" style="position: absolute; top:0; left:0; width:100%; height:100%;"></div>
        </div>
      </div>
    </div>
    <!-- 📊 하단 정보 바 삭제됨 -->
    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick, watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`

// 1. 상태 관리
const searchData = reactive({
  YYMM: initYM,
  LINECD: '',
  PROGCD: ''
})

const lineOptions = ref<any[]>([])
const progOptions = ref<any[]>([])

const uiYYMM = computed({
  get: () => searchData.YYMM ? `${searchData.YYMM.substring(0, 4)}-${searchData.YYMM.substring(4, 6)}` : '',
  set: (v) => { if (v) searchData.YYMM = v.replace(/-/g, '') }
})

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const lastDay = ref(0)
const allSelected = ref(false)

// 🏭 라인/공정 옵션 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', {
      params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y', CODE: '' }
    })
    lineOptions.value = res.data
  } catch (e) {
    console.error('라인 목록 조회 실패', e)
  }
}

const fetchProgOptions = async (lineCd: string) => {
  if (!lineCd) {
    progOptions.value = []
    return
  }
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', {
      params: { GUBUN: 'G0', CMPYCD: authStore.CMPYCD, GBNCD: lineCd, CODE: '' }
    })
    progOptions.value = res.data
  } catch (e) {
    console.error('공정 목록 조회 실패', e)
  }
}

const onLineChange = () => {
  searchData.PROGCD = ''
  fetchProgOptions(searchData.LINECD)
}

const onMonthChange = () => {
  if (grid) grid.setColumns(generateColumns())
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitData",
      height: "100%",
      placeholder: "데이터를 조회해 주세요.",
      headerSort: false,
      headerHeight: 65,
      columns: generateColumns()
    })
  }
}

/**
 * 📅 연월에 따른 동적 컬럼 생성 로직
 */
const generateColumns = () => {
  const year = parseInt(searchData.YYMM.substring(0, 4))
  const month = parseInt(searchData.YYMM.substring(4, 6))
  const lastDate = new Date(year, month, 0).getDate()
  const weekDays = ['일', '월', '화', '수', '목', '금', '토']

  lastDay.value = lastDate

  const columns: any[] = [
    // 👈 1. 선택 체크박스 (tickCross 포맷터 적용, 빈 상태 디폴트)
    {
      title: "선택",
      field: "PROCYN",
      formatter: "tickCross",
      formatterParams: { crossElement: false }, // 👈 'X' 표시 안함
      editor: true,
      width: 60,
      hozAlign: "center",
      headerHozAlign: "center",
      vertAlign: "middle",
      headerSort: false,
      frozen: true,
    },
    // 👈 2. 품목 정보 (품명 + 합계 열합치기)
    {
      title: "품목 정보",
      frozen: true,
      headerHozAlign: "center",
      columns: [
        { title: "품목명", field: "ITEMNM", width: 220, headerHozAlign: "center", vertAlign: "middle", headerSort: false },
        { title: "합계", field: "QTY_TT", width: 90, hozAlign: "right", headerHozAlign: "center", vertAlign: "middle", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-light fw-bold", headerSort: false }
      ]
    }
  ]

  const dateColumns: any[] = []
  for (let d = 1; d <= lastDate; d++) {
    const curDate = new Date(year, month - 1, d)
    const ww = weekDays[curDate.getDay()]
    const fieldName = `MM${String(d - 1).padStart(2, '0')}`

    dateColumns.push({
      title: `${d}일\n(${ww})`,
      field: fieldName,
      width: 75,
      hozAlign: "right",
      editor: "number",
      headerHozAlign: "center",
      vertAlign: "middle",
      headerSort: false,
      formatter: (cell: any) => {
        const val = Number(cell.getValue() || 0)
        return val === 0 ? "" : new Intl.NumberFormat().format(val)
      },
      cellEdited: (cell: any) => {
        const row = cell.getRow()
        const data = row.getData()
        let sum = 0
        for (let i = 0; i < lastDay.value; i++) {
          sum += Number(data[`MM${String(i).padStart(2, '0')}`] || 0)
        }
        row.update({ QTY_TT: sum, PROCYN: true })
      }
    })
  }

  // 👈 3. 생산계획 (일자별 그룹 열합치기)
  columns.push({
    title: `${year}년 ${month}월 생산계획`,
    headerHozAlign: "center",
    columns: dateColumns
  })

  return columns
}

// 3. 비즈니스 로직
const fetchList = async () => {
  if (!searchData.LINECD || !searchData.PROGCD) return

  try {
    const res = await api.post('/api/hppl/HPPL_100U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      YYMM: searchData.YYMM,
      LINECD: searchData.LINECD,
      PROGCD: searchData.PROGCD
    })

    const mappedData = res.data.map((item: any) => {
      const row = { ...item, PROCYN: null } // 👈 null로 초기화하여 'X' 방지
      for (let i = 0; i < lastDay.value; i++) {
        const d = i + 1
        const val = item[String(d)] ?? item[d] ?? item[String(d).padStart(2, '0')] ?? item[i + 5] ?? 0
        row[`MM${String(i).padStart(2, '0')}`] = Number(val)
      }
      return row
    })

    grid?.setData(mappedData)
    allSelected.value = false
    vAlert('조회되었습니다.')
  } catch (e) {
    vAlertError('조회 처리 중 오류 발생')
  }
}

const saveData = async () => {
  const allData = grid?.getData() || []
  const selectedData = allData.filter((r: any) => r.PROCYN === true) // 👈 체크된 항목만 필터링

  if (selectedData.length === 0) return vAlertError('저장할 항목을 선택하세요.')

  if (!confirm('생산계획을 저장하시겠습니까?')) return

  try {
    for (const row of selectedData) {
      for (let d = 0; d < lastDay.value; d++) {
        const dayStr = String(d + 1).padStart(2, '0')
        const qty = Number(row[`MM${String(d).padStart(2, '0')}`] || 0)

        await api.post('/api/hppl/HPPL_100U_STR', {
          ACTKIND: 'A0',
          CMPYCD: authStore.CMPYCD,
          USERID: authStore.USERID,
          LINECD: searchData.LINECD,
          PROGCD: searchData.PROGCD,
          YYMMDD: searchData.YYMM + dayStr,
          ITEMCD: row.ITEMCD,
          ITSIZE: row.ITSIZE,
          UNIT: row.UNIT,
          PLNQTY: qty
        })
      }
    }
    vAlert('정상적으로 저장되었습니다.')
    fetchList()
  } catch (e) {
    vAlertError('저장 중 오류 발생')
  }
}

const toggleAllSelection = () => {
  if (!grid) return
  const data = grid.getData()
  // 👈 true 또는 null로 업데이트하여 'X' 방지
  grid.updateData(data.map(i => ({ ...i, PROCYN: allSelected.value ? true : null })))
}

const initialize = () => {
  searchData.YYMM = initYM
  searchData.LINECD = ''
  searchData.PROGCD = ''
  progOptions.value = []
  grid?.clearData()
  allSelected.value = false
  if (grid) grid.setColumns(generateColumns())
}

const exportExcel = () => {
  grid?.download("xlsx", `생산계획_${searchData.YYMM}.xlsx`, { title: "품목별 생산계획" })
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

onMounted(() => {
  fetchLineOptions()
  nextTick(() => { initGrid() })
})
</script>

<style scoped>
.hppl100u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #0056b3 !important; color: #fff !important; border: none !important; }
.btn-excel { background-color: #198754 !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; border: 1px solid #dee2e6; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 8px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* Tabulator 스타일 정밀 보정 */
:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col) { background-color: #f8f9fa !important; border-right: 1px solid #dee2e6 !important; }
:deep(.tabulator-col-title) {
  line-height: 1.3 !important;
  text-align: center !important;
  white-space: pre-wrap !important;
}
:deep(.tabulator-cell) { border-right: 1px solid #eee !important; border-bottom: 1px solid #eee !important; }

/* 💎 모든 헤더 내용 및 데이터 셀 정중앙 정렬 강제 적용 */
:deep(.tabulator-col-content) {
  padding: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 100% !important;
}
:deep(.tabulator-col-title-holder) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  width: 100% !important;
}
:deep(.tabulator-cell) {
  display: flex !important;
  align-items: center !important;
}
/* 체크박스 정중앙 */
:deep(.tabulator-cell.tabulator-selectable) {
  justify-content: center !important;
}

/* 입력 필드 글자 크기 및 높이 최적화 */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}
</style>
