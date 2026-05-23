<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba820u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-cash-stack me-2 text-primary" style="font-size: 18px;"></i>
        기초자료 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">미수금 등록 (HSBA820U)</span>
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
                <th style="width: 100px;">연 월</th>
                <td style="width: 250px;">
                  <div class="d-flex align-items-center gap-2">
                    <select v-model="searchData.YY" class="form-select form-select-sm" style="width: 100px;">
                      <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
                    </select>
                    <select v-model="searchData.MM" class="form-select form-select-sm" style="width: 80px;">
                      <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
                    </select>
                  </div>
                </td>
                <th style="width: 100px;">판매부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 250px;">
                    <input v-model="searchData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('SEARCH_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('SEARCH_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 미수금 정보 입력 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 미수금 정보 입력</span>
          <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark ms-2">수정 모드</span>
          <span v-else class="badge bg-primary ms-2">신규 등록</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required" style="width: 100px;">연 월</th>
                <td style="width: 200px;">
                  <div class="d-flex align-items-center gap-1">
                    <select v-model="masterData.YY" class="form-select form-select-sm" style="width: 90px;" :disabled="masterData.ACTKIND === 'U0'">
                      <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
                    </select>
                    <select v-model="masterData.MM" class="form-select form-select-sm" style="width: 70px;" :disabled="masterData.ACTKIND === 'U0'">
                      <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
                    </select>
                  </div>
                </td>
                <th class="required" style="width: 100px;">판매부서</th>
                <td style="width: 250px;">
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.DEPTNM" type="text" class="form-control bg-light" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('MASTER_DEPT')" :disabled="masterData.ACTKIND === 'U0'"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required" style="width: 100px;">거 래 처</th>
                <td style="width: 280px;">
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')" :disabled="masterData.ACTKIND === 'U0'"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required" style="width: 100px;">미 수 금</th>
                <td>
                  <input v-model="masterData.AMT" type="text" class="form-control form-control-sm text-end fw-bold" @input="formatInput('AMT')" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 거래처별 미수금 내역</span>
          <div class="small text-muted">행 클릭 시 상세 정보를 수정할 수 있습니다.</div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick, watch } from 'vue'
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
const currentYear = now.getFullYear()
const currentMonth = String(now.getMonth() + 1).padStart(2, '0')

// 1. 상태 관리
const searchData = reactive({
  YY: String(currentYear),
  MM: currentMonth,
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM
})

const masterData = reactive<any>({
  ACTKIND: 'A0',
  CMPYCD: authStore.CMPYCD,
  YY: String(currentYear),
  MM: currentMonth,
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  CUSTCD: '',
  CUSTNM: '',
  AMT: '0'
})

const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)
const totalMisuSum = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      { title: "거래처", field: "CUSTNM", minWidth: 200, cssClass: "fw-bold" },
      { title: "전월잔액", field: "BNONAMT", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "매출액", field: "SPYAMT", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "부가세", field: "VATAMT", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      {
        title: "합계", field: "TOTAL", width: 120, hozAlign: "right",
        formatter: (cell) => {
            const data = cell.getData();
            return new Intl.NumberFormat().format(Number(data.SPYAMT || 0) + Number(data.VATAMT || 0));
        }
      },
      { title: "지급액", field: "IMAMT", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미수금", field: "AMT", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary fw-bold" }
    ]
  })

  grid.value.on("rowClick", (e, row) => {
    const data = row.getData()
    Object.assign(masterData, data)
    masterData.YY = searchData.YY
    masterData.MM = searchData.MM
    masterData.DEPTCD = searchData.DEPTCD
    masterData.DEPTNM = searchData.DEPTNM
    masterData.AMT = formatNumber(data.AMT)
    masterData.ACTKIND = 'U0'
  })
}

// 3. 기능 구현
async function search() {
  if (!searchData.DEPTCD) return vAlertError('판매부서를 선택해 주십시요.')
  try {
    const res = await api.post('/api/hsba/HSBA_820U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      YY: searchData.YY,
      MM: searchData.MM,
      DEPTCD: searchData.DEPTCD,
      CUSTCD: '',
      AMT: 0
    })
    if (grid.value) {
      const mappedData = res.data.map((i: any) => {
          const bnon = Number(i.BNONAMT) || 0;
          const spy = (Number(i.SPYAMT) || 0) + (Number(i.DSCAMT) || 0) + (Number(i.RTNAMT) || 0);
          const vat = Number(i.VATAMT) || 0;
          const im = (Number(i.CASHAMT) || 0) + (Number(i.BANKAMT) || 0) + (Number(i.BILLAMT) || 0) + (Number(i.ETCAMT) || 0);
          const amt = bnon + spy + vat - im;
          return { ...i, BNONAMT: bnon, SPYAMT: spy, VATAMT: vat, IMAMT: im, AMT: amt }
      })
      grid.value.setData(mappedData)
      activeItemCount.value = mappedData.length
      totalMisuSum.value = mappedData.reduce((acc: number, cur: any) => acc + cur.AMT, 0)
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.DEPTCD) return vAlertError('판매부서를 선택해 주십시요.')
  if (!masterData.CUSTCD) return vAlertError('거래처를 선택해 주십시요.')
  if (masterData.AMT === '' || masterData.AMT === '0') return vAlertError('미수금을 입력해 주십시요.')

  const confirmMsg = masterData.ACTKIND === 'A0' ? '미수금을 등록하시겠습니까?' : '미수금 정보를 수정하시겠습니까?'
  if (!confirm(confirmMsg)) return

  try {
    const payload = {
      ...masterData,
      AMT: masterData.AMT.replace(/,/g, ''),
      USERID: authStore.USERID
    }
    await api.post('/api/hsba/HSBA_820U_STR', payload)
    vAlert('정상적으로 처리되었습니다.')
    search()
    initialize(true)
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize(keepYmDept = false) {
  const currentY = masterData.YY
  const currentM = masterData.MM
  const currentDept = masterData.DEPTCD
  const currentDeptNm = masterData.DEPTNM

  resetForm(masterData)

  if (keepYmDept) {
    Object.assign(masterData, {
      ACTKIND: 'A0',
      CMPYCD: authStore.CMPYCD,
      YY: currentY,
      MM: currentM,
      DEPTCD: currentDept,
      DEPTNM: currentDeptNm,
      AMT: '0'
    })
  } else {
    Object.assign(masterData, {
      ACTKIND: 'A0',
      CMPYCD: authStore.CMPYCD,
      YY: searchData.YY,
      MM: searchData.MM,
      DEPTCD: searchData.DEPTCD,
      DEPTNM: searchData.DEPTNM,
      AMT: '0'
    })
  }
}

const formatInput = (field: string) => {
  let val = masterData[field].replace(/[^0-9]/g, '')
  masterData[field] = val.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let gubun = 'D0', title = '부서 선택', field = 'DEPTNM'
  if (type === 'CUST') { gubun = 'C4'; title = '거래처 선택'; field = 'CUSTNM' }

  Object.assign(modalProps, {
    title, path: '/api/ha00/HA00_00P_STR', defaultField: field,
    data: { GUBUN: gubun, CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
    columns: gubun === 'D0'
      ? [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }]
      : [{ title: '코드', field: 'CUSTCD', width: 100 }, { title: '거래처명', field: 'CUSTNM', minWidth: 200 }],
    onConfirm: (data: any) => {
      if (type === 'SEARCH_DEPT') { searchData.DEPTCD = data.DEPTCD; searchData.DEPTNM = data.DEPTNM }
      else if (type === 'MASTER_DEPT') { masterData.DEPTCD = data.DEPTCD; masterData.DEPTNM = data.DEPTNM }
      else if (type === 'CUST') { masterData.CUSTCD = data.CUSTCD; masterData.CUSTNM = data.CUSTNM }
    }
  })
  modalVisible.value = true
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(() => {
  nextTick(() => {
    initGrid()
    search()
  })
})

// Sync masterData with search conditions when in new entry mode
watch(() => searchData.YY, (val) => { if (masterData.ACTKIND === 'A0') masterData.YY = val })
watch(() => searchData.MM, (val) => { if (masterData.ACTKIND === 'A0') masterData.MM = val })
watch(() => searchData.DEPTCD, (val) => { if (masterData.ACTKIND === 'A0') masterData.DEPTCD = val })
watch(() => searchData.DEPTNM, (val) => { if (masterData.ACTKIND === 'A0') masterData.DEPTNM = val })

</script>

<style scoped>
.hsba820u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px 15px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
