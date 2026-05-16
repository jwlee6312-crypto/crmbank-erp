<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio160u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-percent me-2 text-success" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매입할인 등록 (HSIO160U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="saveData">저장</button>
        <button class="btn-erp btn-delete" @click="deleteData">삭제</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 및 배부 설정 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">정산연월</th>
                <td>
                  <input v-model="uiJSANYM" type="month" class="form-control form-control-sm" style="width: 150px;" />
                </td>
                <th class="required">매&nbsp;&nbsp;입&nbsp;&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 250px;">
                    <input v-model="searchData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="searchData.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">정산일자</th>
                <td>
                  <input v-model="uiJSANYMD" type="date" class="form-control form-control-sm" style="width: 150px;" />
                </td>
              </tr>
              <tr>
                <th class="bg-light-blue">할인금액</th>
                <td>
                  <input v-model.number="inputData.DIVSUM" type="number" class="form-control form-control-sm text-end fw-bold text-primary" style="width: 150px;" @input="applyAllocation" />
                </td>
                <th class="bg-light-blue">배부기준</th>
                <td colspan="3">
                  <div class="d-flex gap-3 align-items-center mt-1">
                    <div class="form-check form-check-inline">
                      <input class="form-check-input" type="radio" v-model="inputData.DIVGBN" value="AMT" id="gbnAmt" @change="applyAllocation">
                      <label class="form-check-label small fw-bold" for="gbnAmt">금액기준</label>
                    </div>
                    <div class="form-check form-check-inline">
                      <input class="form-check-input" type="radio" v-model="inputData.DIVGBN" value="QTY" id="gbnQty" @change="applyAllocation">
                      <label class="form-check-label small fw-bold" for="gbnQty">수량기준</label>
                    </div>
                    <span class="text-muted small ms-3"><i class="bi bi-info-circle me-1"></i> 할인금액 입력 시 선택된 항목들에 자동 배부됩니다.</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-check me-1"></i> 매입 내역</span>
          <div class="small text-muted">전표 발행된 자료는 수정/삭제가 불가능합니다.</div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 합계 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="d-flex justify-content-between align-items-center w-100">
        <div class="small">선택건수: <span class="fw-bold text-info">{{ selectedCount }}</span> 건</div>
        <div class="d-flex gap-4">
          <div class="small">선택수량 합계: <span class="fw-bold text-white">{{ formatNumber(totals.qty) }}</span></div>
          <div class="small">선택공급가 합계: <span class="fw-bold text-white">{{ formatNumber(totals.amt) }}</span></div>
          <div class="small">할인금액 합계: <span class="fw-bold text-warning">{{ formatNumber(totals.hal) }}</span></div>
        </div>
      </div>
    </div>

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
const initYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`

// 1. 상태 관리
const searchData = reactive({
  JSANYM: initYM,
  CUSTCD: '',
  CUSTNM: '',
  JSANYMD: initYMD
})

const inputData = reactive({
  DIVSUM: 0,
  DIVGBN: 'AMT'
})

const uiJSANYM = computed({
  get: () => searchData.JSANYM ? `${searchData.JSANYM.substring(0, 4)}-${searchData.JSANYM.substring(4, 6)}` : '',
  set: (v) => searchData.JSANYM = v.replace(/-/g, '')
})
const uiJSANYMD = computed({
  get: () => formatDateString(searchData.JSANYMD, '-'),
  set: (v) => searchData.JSANYMD = v.replace(/-/g, '')
})

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const selectedCount = ref(0)
const totals = reactive({ qty: 0, amt: 0, hal: 0 })

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      selectable: true,
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 80, hozAlign: "center", headerSort: false },
        { title: "품목명", field: "ITEMNM", minWidth: 100, widthGrow: 1 },
        { title: "규격", field: "ITEMSIZE", width: 200 },
        { title: "수량", field: "IOQTY", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "공급가", field: "IOAMT", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "할인금액", field: "HALAMT", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary fw-bold" },
        { title: "입고부서", field: "DEPTNM", width: 250 },
        { title: "전표발행", field: "SLIPYMD", width: 150, hozAlign: "center", formatter: (c) => c.getValue() > "00000000" ? "발행" : "" },
      ],
    })

    grid.on("rowSelectionChanged", () => {
      calculateTotals()
      applyAllocation()
    })
  }
}

// 3. 로직 함수
const calculateTotals = () => {
  const selectedRows = grid?.getSelectedData() || []
  selectedCount.value = selectedRows.length
  totals.qty = selectedRows.reduce((acc, row) => acc + Number(row.IOQTY || 0), 0)
  totals.amt = selectedRows.reduce((acc, row) => acc + Number(row.IOAMT || 0), 0)
}

// ASP의 Div_Total_Calc 구현
const applyAllocation = () => {
  const selectedRows = grid?.getSelectedRows() || []
  if (selectedRows.length === 0 || inputData.DIVSUM <= 0) {
    grid?.getData().forEach(row => grid?.updateData([{ id: row.id, HALAMT: 0 }]))
    totals.hal = 0
    return
  }

  const divSum = inputData.DIVSUM
  const isAmtBasis = inputData.DIVGBN === 'AMT'
  const totalBasis = isAmtBasis ? totals.amt : totals.qty

  if (totalBasis === 0) return

  let allocatedSum = 0
  const updates: any[] = []

  selectedRows.forEach((row, idx) => {
    const data = row.getData()
    const basisValue = isAmtBasis ? Number(data.IOAMT) : Number(data.IOQTY)

    // 비례 배분 계산
    let halAmt = Math.round(divSum * (basisValue / totalBasis))

    // 마지막 행에서 단수 차이 보정
    if (idx === selectedRows.length - 1) {
      halAmt = divSum - allocatedSum
    }

    allocatedSum += halAmt
    updates.push({ ...data, HALAMT: halAmt })
  })

  // 선택되지 않은 행들은 0으로 초기화
  grid?.getData().forEach(row => {
    if (!selectedRows.find(r => r.getData().IOROWNO === row.IOROWNO)) {
      updates.push({ ...row, HALAMT: 0 })
    }
  })

  grid?.updateData(updates)
  totals.hal = allocatedSum
}

const fetchList = async () => {
  if (!searchData.CUSTCD) return vAlertError('매입처를 선택하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_160U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      JSANYM: searchData.JSANYM,
      CUSTCD: searchData.CUSTCD
    })

    // Tabulator는 고유 ID가 필요하므로 IOROWNO 등을 활용하거나 인덱스 부여
    const dataWithId = res.data.map((item: any, idx: number) => ({ ...item, id: idx }))
    grid?.setData(dataWithId)

    // 기존에 할인이 등록된 행(HALAMT > 0) 자동 선택
    nextTick(() => {
      grid?.getRows().forEach(row => {
        if (Number(row.getData().HALAMT) > 0) {
          row.select()
        }
      })
    })

    vAlert('조회되었습니다.')
  } catch (e) {
    vAlertError('조회 중 오류가 발생했습니다.')
  }
}

const saveData = async () => {
  const selectedData = grid?.getSelectedData() || []
  if (selectedData.length === 0) return vAlertError('저장할 항목을 선택하세요.')
  if (totals.hal !== inputData.DIVSUM) return vAlertError('배부된 할인금액 합계가 입력한 할인금액과 일치하지 않습니다.')

  // 전표 발행 체크
  if (selectedData.some(item => item.SLIPYMD > "00000000")) {
    return vAlertError('전표가 발행된 자료는 수정할 수 없습니다.')
  }

  if (!confirm('매입할인 정보를 저장하시겠습니까?')) return

  try {
    // ASP 로직처럼 루프를 돌며 개별 저장하거나, 백엔드에서 리스트 처리가 가능하면 일괄 전송
    for (const item of selectedData) {
      const halRate = inputData.DIVGBN === 'AMT'
                      ? (Number(item.IOAMT) / totals.amt)
                      : (Number(item.IOQTY) / totals.qty)

      await api.post('/api/hsio/HSIO_160U_STR', {
        ACTKIND: 'A0',
        CMPYCD: authStore.CMPYCD,
        USERID: authStore.USERID,
        JSANYM: searchData.JSANYM,
        JSANYMD: searchData.JSANYMD,
        CUSTCD: searchData.CUSTCD,
        DIVGBN: inputData.DIVGBN,
        // 개별 아이템 정보
        JSANNO: item.JSANNO,
        IOYM: item.IOYM,
        IONO: item.IONO,
        IOROWNO: item.IOROWNO,
        DEPTCD: item.DEPTCD,
        ITEMCD: item.ITEMCD,
        IOQTY: item.IOQTY,
        IOAMT: item.IOAMT,
        HALAMT: item.HALAMT,
        HALRATE: halRate.toFixed(8)
      })
    }
    vAlert('정상적으로 저장되었습니다.')
    fetchList()
  } catch (e) {
    vAlertError('저장 중 오류가 발생했습니다.')
  }
}

const deleteData = async () => {
  const selectedData = grid?.getSelectedData() || []
  if (selectedData.length === 0) return vAlertError('삭제할 항목을 선택하세요.')
  if (selectedData.some(item => item.SLIPYMD > "00000000")) return vAlertError('전표가 발행된 자료는 삭제할 수 없습니다.')

  if (!confirm('선택한 항목의 할인 정보를 삭제하시겠습니까?')) return

  try {
    for (const item of selectedData) {
      await api.post('/api/hsio/HSIO_160U_STR', {
        ACTKIND: 'D0',
        CMPYCD: authStore.CMPYCD,
        JSANYM: item.JSANYM,
        JSANNO: item.JSANNO,
        IOYM: item.IOYM,
        IONO: item.IONO,
        IOROWNO: item.IOROWNO
      })
    }
    vAlert('삭제되었습니다.')
    fetchList()
  } catch (e) {
    vAlertError('삭제 실패')
  }
}

const initialize = () => {
  searchData.JSANYM = initYM
  searchData.CUSTCD = ''
  searchData.CUSTNM = ''
  searchData.JSANYMD = initYMD
  inputData.DIVSUM = 0
  grid?.clearData()
  calculateTotals()
}

// 4. 도움창
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  Object.assign(modalProps, {
    title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CUSTNM',
    data: { GUBUN: 'C4', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
    columns: [{ title: '코드', field: 'CUSTCD', width: 80 }, { title: '거래처명', field: 'CUSTNM', width: 200 }],
    onConfirm: (data: any) => {
      searchData.CUSTCD = data.CUSTCD
      searchData.CUSTNM = data.CUSTNM
      fetchList()
    }
  })
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(() => {
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsio160u-wrapper { height: 100%; overflow: hidden; }
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