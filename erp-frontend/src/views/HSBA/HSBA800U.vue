<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba800u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-database-fill-add me-2 text-primary" style="font-size: 18px;"></i>
        기초자료 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">재고금액등록 (HSBA800U)</span>
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
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <select v-model="searchData.YY" class="form-select form-select-sm" style="width: 100px;">
                      <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
                    </select>
                    <select v-model="searchData.MM" class="form-select form-select-sm" style="width: 80px;">
                      <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
                    </select>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 마스터 정보 입력 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 기초재고 정보 입력</span>
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
                <th class="required" style="width: 100px;">품 목</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 500px;">
                    <input v-model="masterData.ITEMCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="masterData.ITEMNM" type="text" class="form-control" placeholder="품목 선택" @keyup.enter="openHelp('ITEM')" />
                    <input v-model="masterData.ITSIZE" type="text" class="form-control bg-light" style="max-width: 120px;" readonly />
                    <input v-model="masterData.UNIT" type="text" class="form-control bg-light text-center" style="max-width: 60px;" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('ITEM')" :disabled="masterData.ACTKIND === 'U0'"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">기초재고수량</th>
                <td>
                  <input v-model="masterData.QTY" type="text" class="form-control form-control-sm text-end" @input="formatInput('QTY')" />
                </td>
                <th class="required">기초재고금액</th>
                <td>
                  <input v-model="masterData.AMT" type="text" class="form-control form-control-sm text-end" style="width: 150px;" @input="formatInput('AMT')" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 기초재고 목록</span>
          <div class="small text-muted">행 클릭 시 상세 정보를 수정할 수 있습니다.</div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-12 small">
          조회 건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건
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
  MM: currentMonth
})

const masterData = reactive<any>({
  ACTKIND: 'A0',
  CMPYCD: authStore.CMPYCD,
  YY: String(currentYear),
  MM: currentMonth,
  ITEMCD: '',
  ITEMNM: '',
  ITSIZE: '',
  UNIT: '',
  QTY: '0',
  AMT: '0'
})

const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      { title: "품목", field: "ITEMNM", minWidth: 250, cssClass: "fw-bold" },
      { title: "규격", field: "ITSIZE", width: 150 },
      { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
      { title: "기초재고수량", field: "QTY", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      {
        title: "단가", field: "PRICE", width: 120, hozAlign: "right",
        formatter: (cell) => {
            const data = cell.getData();
            const qty = Number(data.QTY) || 0;
            const amt = Number(data.AMT) || 0;
            const price = qty !== 0 ? Math.floor(amt / qty) : 0;
            return new Intl.NumberFormat().format(price);
        }
      },
      { title: "재고금액", field: "AMT", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } }
    ]
  })

  grid.value.on("rowClick", (e, row) => {
    const data = row.getData()
    Object.assign(masterData, data)
    masterData.YY = searchData.YY
    masterData.MM = searchData.MM
    masterData.QTY = formatNumber(data.QTY)
    masterData.AMT = formatNumber(data.AMT)
    masterData.ACTKIND = 'U0'
  })
}

// 3. 기능 구현
async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_800U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      YY: searchData.YY,
      MM: searchData.MM,
      ITEMCD: '',
      QTY: 0,
      AMT: 0
    })
    if (grid.value) {
      grid.value.setData(res.data)
      activeItemCount.value = res.data.length
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.ITEMCD) return vAlertError('품목을 선택해 주십시요.')
  if (Number(masterData.QTY.replace(/,/g, '')) === 0) return vAlertError('수량을 입력해 주십시요.')
  if (Number(masterData.AMT.replace(/,/g, '')) === 0) return vAlertError('금액을 입력해 주십시요.')

  const confirmMsg = masterData.ACTKIND === 'A0' ? '기초재고를 등록하시겠습니까?' : '기초재고 정보를 수정하시겠습니까?'
  if (!confirm(confirmMsg)) return

  try {
    const payload = {
      ...masterData,
      QTY: masterData.QTY.replace(/,/g, ''),
      AMT: masterData.AMT.replace(/,/g, ''),
      USERID: authStore.USERID
    }
    await api.post('/api/hsba/HSBA_800U_STR', payload)
    vAlert('성공적으로 저장되었습니다.')
    search()
    initialize()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'A0',
    CMPYCD: authStore.CMPYCD,
    YY: searchData.YY,
    MM: searchData.MM,
    QTY: '0',
    AMT: '0'
  })
}

const formatInput = (field: string) => {
  let val = masterData[field].replace(/[^0-9]/g, '')
  masterData[field] = val.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'ITEM') {
    Object.assign(modalProps, {
      title: '품목 선택',
      path: '/api/hs00/HS00_000S_STR',
      defaultField: 'ITEMNM',
      large: true,
      data: { GUBUN: 'I1', CMPYCD: authStore.CMPYCD, GBNCD: '2', LIMITOFFSET: 0, LIMITROWS: 9999 },
      columns: [
        { title: '코드', field: 'ITEMCD', width: 100 },
        { title: '품목명', field: 'ITEMNM', minWidth: 200 },
        { title: '규격', field: 'ITSIZE', width: 150 },
        { title: '단위', field: 'UNITNM', width: 80 }
      ],
      onConfirm: (data: any) => {
        masterData.ITEMCD = data.ITEMCD
        masterData.ITEMNM = data.ITEMNM
        masterData.ITSIZE = data.ITSIZE
        masterData.UNIT = data.UNITNM
      }
    })
    modalVisible.value = true
  }
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(() => {
  nextTick(() => {
    initGrid()
    search()
  })
})

// Update masterData YM when searchData changes
watch(() => searchData.YY, (val) => { if (masterData.ACTKIND === 'A0') masterData.YY = val })
watch(() => searchData.MM, (val) => { if (masterData.ACTKIND === 'A0') masterData.MM = val })

</script>

<style scoped>
.hsba800u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px 15px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 8px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
