<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsst100s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-journal-text me-2 text-primary" style="font-size: 18px;"></i>
        매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매출처원장 (HSST100S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-outline-secondary" @click="print('Print')">인쇄</button>
        <button class="btn-erp btn-outline-success" @click="print('Excel')">엑셀</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required" style="width: 100px;">판매부서</th>
                <td style="width: 250px;">
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="searchData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required" style="width: 100px;">판매일자</th>
                <td style="width: 300px;">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="uiFRYMD" type="date" class="form-control form-control-sm" />
                    <span>~</span>
                    <input v-model="uiTOYMD" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th style="width: 100px;">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 300px;">
                    <input v-model="searchData.CUSTCDFR" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="searchData.CUSTNMFR" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 거래처별 원장 내역</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-12 small text-center opacity-75">
          <i class="bi bi-info-circle me-1"></i> 거래처명을 클릭하면 상세 거래 내역(HSST110S)으로 이동합니다.
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
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
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const initYMD = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
const initFRYMD = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`

// 1. 상태 관리
const searchData = reactive({
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  FRYMD: initFRYMD.replace(/-/g, ''),
  TOYMD: initYMD.replace(/-/g, ''),
  CUSTCDFR: '',
  CUSTNMFR: '',
  SELGBN: '1'
})

const uiFRYMD = computed({ get: () => formatDateString(searchData.FRYMD, '-'), set: (v) => searchData.FRYMD = v.replace(/-/g, '') })
const uiTOYMD = computed({ get: () => formatDateString(searchData.TOYMD, '-'), set: (v) => searchData.TOYMD = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)

// 2. 그리드 초기화 (다단 헤더 적용)
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "거래처", field: "CUSTNM", minWidth: 180, cssClass: "fw-bold border-end",
        formatter: (cell) => `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue()}</span>`,
        cellClick: (e, cell) => navigateToDetail(cell.getData())
      },
      {
        title: "이월액", field: "BASEAMT", width: 110, hozAlign: "right",
        formatter: "money", formatterParams: { precision: 0 },
        bottomCalc: "sum", bottomCalcFormatter: "money"
      },
      // 판매현황 그룹
      {
        title: "판매현황(VAT포함)",
        columns: [
          { title: "공급가", field: "SPYAMT", width: 100, hozAlign: "right", formatter: "money", bottomCalc: "sum", bottomCalcFormatter: "money" },
          { title: "부가세", field: "VATAMT", width: 90, hozAlign: "right", formatter: "money", bottomCalc: "sum", bottomCalcFormatter: "money" },
          {
            title: "매출계", field: "SALES_TOTAL", width: 110, hozAlign: "right",
            formatter: (cell) => formatNumber(Number(cell.getData().SPYAMT || 0) + Number(cell.getData().VATAMT || 0)),
            cssClass: "bg-light border-end", bottomCalc: "sum", bottomCalcFormatter: "money"
          },
        ]
      },
      // 입금현황 그룹
      {
        title: "입 금 현 황",
        columns: [
          { title: "현금", field: "CASHAMT", width: 90, hozAlign: "right", formatter: "money", bottomCalc: "sum", bottomCalcFormatter: "money" },
          { title: "예금", field: "BANKAMT", width: 90, hozAlign: "right", formatter: "money", bottomCalc: "sum", bottomCalcFormatter: "money" },
          { title: "어음/카드", field: "BILLAMT", width: 95, hozAlign: "right", formatter: "money", bottomCalc: "sum", bottomCalcFormatter: "money" },
          { title: "기타", field: "ETCAMT", width: 90, hozAlign: "right", formatter: "money", bottomCalc: "sum", bottomCalcFormatter: "money" },
          {
            title: "입금계", field: "DEPOSIT_TOTAL", width: 110, hozAlign: "right",
            formatter: (cell) => {
              const d = cell.getData();
              return formatNumber(Number(d.CASHAMT || 0) + Number(d.BANKAMT || 0) + Number(d.BILLAMT || 0) + Number(d.ETCAMT || 0));
            },
            cssClass: "bg-light border-end", bottomCalc: "sum", bottomCalcFormatter: "money"
          },
        ]
      },
      {
        title: "잔액", field: "BALANCE", width: 120, hozAlign: "right",
        formatter: (cell) => {
          const d = cell.getData();
          const totalSales = Number(d.SPYAMT || 0) + Number(d.VATAMT || 0);
          const totalDeposit = Number(d.CASHAMT || 0) + Number(d.BANKAMT || 0) + Number(d.BILLAMT || 0) + Number(d.ETCAMT || 0);
          return formatNumber(Number(d.BASEAMT || 0) + totalSales - totalDeposit);
        },
        cssClass: "bg-light fw-bold text-danger",
        bottomCalc: "sum", bottomCalcFormatter: "money"
      }
    ]
  })
}

// 3. 기능 구현
async function search() {
  if (!searchData.DEPTCD) return vAlertError('판매부서를 선택해 주십시요.')
  try {
    const res = await api.post('/api/hsst/HSST_100S_STR', {
      CMPYCD: authStore.CMPYCD,
      SELGBN: searchData.SELGBN,
      DEPTCD: searchData.DEPTCD,
      CUSTCDFR: searchData.CUSTCDFR,
      CUSTCDTO: searchData.CUSTCDFR, // ASP 로직상 시작/종료를 동일하게 사용하거나 범위 지정
      FRYMD: searchData.FRYMD,
      TOYMD: searchData.TOYMD
    })
    if (grid.value) {
      grid.value.setData(res.data)
      activeItemCount.value = res.data.length
    }
  } catch (e) { vAlertError('조회 실패') }
}

const navigateToDetail = (row: any) => {
    // 상세 원장(HSST110S)으로 이동하는 로직
    router.push({
        name: '매출처상세원장', // 라우터에 정의된 이름
        query: {
            CUSTCD: row.CUSTCD,
            CUSTNM: row.CUSTNM,
            FRYMD: searchData.FRYMD,
            TOYMD: searchData.TOYMD,
            DEPTCD: searchData.DEPTCD
        }
    })
}

function initialize() {
  resetForm(searchData)
  searchData.FRYMD = initFRYMD.replace(/-/g, '')
  searchData.TOYMD = initYMD.replace(/-/g, '')
  searchData.DEPTCD = authStore.DEPTCD
  searchData.DEPTNM = authStore.DEPTNM
  grid.value?.clearData()
}

function print(type: string) {
  // 인쇄 또는 엑셀 내보내기 로직 (기존 ASP 팝업 호출 방식 유지 가능)
  const url = `HSST_100P.asp?SELGBN=1&DEPTCD=${searchData.DEPTCD}&CUSTCDFR=${searchData.CUSTCDFR}&FRYMD=${searchData.FRYMD}&TOYMD=${searchData.TOYMD}&PRTGU=${type}`
  window.open(url, 'print', 'width=700,height=650,scrollbars=yes')
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'DEPT') {
    Object.assign(modalProps, {
      title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM',
      data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
      columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
      onConfirm: (data: any) => { searchData.DEPTCD = data.DEPTCD; searchData.DEPTNM = data.DEPTNM }
    })
  } else if (type === 'CUST') {
    Object.assign(modalProps, {
      title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CUSTNM',
      data: { GUBUN: 'C4', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
      columns: [{ title: '코드', field: 'CUSTCD', width: 80 }, { title: '거래처명', field: 'CUSTNM', width: 200 }],
      onConfirm: (data: any) => { searchData.CUSTCDFR = data.CUSTCD; searchData.CUSTNMFR = data.CUSTNM }
    })
  }
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(() => {
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsst100s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px 15px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 8px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* Tabulator 다단 헤더 스타일 조정 */
:deep(.tabulator-col-group) { border-right: 1px solid #dee2e6 !important; border-bottom: 1px solid #dee2e6 !important; }
:deep(.tabulator-col) { border-right: 1px solid #dee2e6 !important; }
</style>