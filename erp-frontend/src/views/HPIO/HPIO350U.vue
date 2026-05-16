<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio350u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-journal-check me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">외주가공생산실적 (HPIO350U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchMaster">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="saveAll">
          <i class="bi bi-save"></i> 전체저장
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-3">
      <!-- 🅰️ 조회 및 마스터 정보 -->
      <div class="card border-0 shadow-sm overflow-hidden" style="border-radius: 8px;">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <colgroup>
              <col style="width: 100px;"><col>
              <col style="width: 100px;"><col>
              <col style="width: 100px;"><col>
              <col style="width: 100px;"><col>
            </colgroup>
            <tbody>
              <tr>
                <th class="required">생산라인</th>
                <td>
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 200px;" @change="onLineChange">
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">{{ opt.LINENM }}</option>
                  </select>
                </td>
                <th class="required">거 래 처</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="searchData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.CUSTNM" type="text" class="form-control" placeholder="외주업체 선택" @keyup.enter="openHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">지시일자</th>
                <td>
                  <input v-model="uiORDYMD" type="date" class="form-control form-control-sm" style="width: 150px;" />
                </td>
                <th class="required">입고일자</th>
                <td>
                  <input v-model="uiPROYMD" type="date" class="form-control form-control-sm" style="width: 150px;" />
                </td>
              </tr>
              <tr>
                <th class="required">출고창고</th>
                <td>
                  <select v-model="masterInfo.WHCD" class="form-select form-select-sm" style="width: 200px;">
                    <option v-for="opt in whOptions" :key="opt.WHCD" :value="opt.WHCD">{{ opt.WHNM }}</option>
                  </select>
                </td>
                <th class="required">입고창고</th>
                <td>
                  <select v-model="masterInfo.IWHCD" class="form-select form-select-sm" style="width: 220px;">
                    <option v-for="opt in whOptions" :key="opt.WHCD" :value="opt.WHCD">{{ opt.WHNM }}</option>
                  </select>
                </td>
                <th class="required">입고공정</th>
                <td colspan="3">
                  <select v-model="masterInfo.PROGCD" class="form-select form-select-sm" style="width: 250px;">
                    <option v-for="opt in progOptions" :key="opt.PROGCD" :value="opt.PROGCD">{{ opt.PROGNM }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 제품 실적 그리드 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px; min-height: 250px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-box-seam me-1 text-primary"></i> 외주 생산 제품 리스트</span>
          <div class="btn-group gap-1">
            <button class="btn btn-sm btn-outline-primary px-3" style="height: 28px; font-size: 12px; font-weight: 600;" @click="addProductRow">
              <i class="bi bi-plus-circle me-1"></i> 행추가
            </button>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="prodGridElement" style="height: 100%;"></div>
        </div>
      </div>

      <!-- 🅲 투입 자재 그리드 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px; min-height: 250px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark">
            <i class="bi bi-tools me-1 text-success"></i> 투입 자재 상세
            <span v-if="selectedProduct.ITEMNM" class="text-success ms-2">[{{ selectedProduct.ITEMNM }}]</span>
          </span>
          <div class="btn-group gap-2">
            <button class="btn btn-sm btn-outline-primary px-3" style="height: 28px; font-size: 12px; font-weight: 600;" @click="addMaterialRow" :disabled="!selectedProduct.ITEMCD">
              <i class="bi bi-plus-circle me-1"></i> 행추가
            </button>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="matGridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
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

// 1. 상태 관리
const searchData = reactive({
  LINECD: '010',
  CUSTCD: '', CUSTNM: '',
  ORDYMD: initYMD,
  PROYMD: initYMD
})

const masterInfo = reactive({
    WHCD: '300',
    IWHCD: '200',
    PROGCD: ''
})

const lineOptions = ref<any[]>([])
const progOptions = ref<any[]>([])
const whOptions = ref<any[]>([])
const closingInfo = reactive({ CLSYMD: '', SCLSYM: '', PCLSYM: '' })

const uiORDYMD = computed({ get: () => formatDateString(searchData.ORDYMD, '-'), set: (v) => { if (v) searchData.ORDYMD = v.replace(/-/g, '') } })
const uiPROYMD = computed({ get: () => formatDateString(searchData.PROYMD, '-'), set: (v) => { if (v) searchData.PROYMD = v.replace(/-/g, '') } })

const selectedProduct = reactive<any>({ ITEMCD: '', ITEMNM: '' })

const prodGridElement = ref<HTMLElement | null>(null)
const matGridElement = ref<HTMLElement | null>(null)
let prodGrid: Tabulator | null = null
let matGrid: Tabulator | null = null

// 🏭 기초 코드 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data.map((i: any) => ({ LINECD: i.CODE, LINENM: i.CDNM }))

    if (lineOptions.value.length > 0) {
      if (!searchData.LINECD) searchData.LINECD = lineOptions.value[0].LINECD
      await fetchProgOptions(searchData.LINECD)
    } else if (searchData.LINECD) {
       await fetchProgOptions(searchData.LINECD)
    }
  } catch (e) {}
}

const fetchProgOptions = async (lineCd: string) => {
  if (!lineCd) { progOptions.value = []; return; }
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'G0', CMPYCD: authStore.CMPYCD, GBNCD: lineCd } })
    progOptions.value = res.data.map((i: any) => ({ PROGCD: i.CODE, PROGNM: i.CDNM }))

    if (progOptions.value.length > 0 && !masterInfo.PROGCD) {
        masterInfo.PROGCD = progOptions.value[0].PROGCD
    }
  } catch (e) {}
}

const fetchWhOptions = async () => {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })
    whOptions.value = res.data.map((i: any) => ({ WHCD: i.WHCD, WHNM: i.WHNM }))
  } catch (e) {}
}

const onLineChange = () => {
    masterInfo.PROGCD = ''
    fetchProgOptions(searchData.LINECD)
}

// 2. 그리드 초기화
const initGrids = () => {
  if (prodGridElement.value) {
    prodGrid = new Tabulator(prodGridElement.value, {
      layout: "fitColumns", height: "100%", selectable: 1,
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false },
        { title: "제품명", field: "ITEMNM", minWidth: 200, cellClick: (e, cell) => { if(!cell.getValue()) openHelp('GRID_ITEM', cell) } },
        { title: "규격", field: "ITSIZE", width: 120 },
        { title: "단위", field: "UNIT", width: 60, hozAlign: "center" },
        { title: "지시량", field: "ORDQTY", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "총생산", field: "TOTQTY", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "생산량", field: "PRDQTY", width: 100, hozAlign: "right", editor: "number", cssClass: "bg-light-yellow fw-bold" },
        { title: "가공단가", field: "OUTPRICE", width: 100, hozAlign: "right", editor: "number", formatter: "money" },
        { title: "가공금액", field: "OUTAMT", width: 110, hozAlign: "right", formatter: "money" },
        { title: "부가세", field: "OUTVAT", width: 90, hozAlign: "right", formatter: "money" },
        { title: "합계", field: "OUTTOT", width: 110, hozAlign: "right", formatter: "money", cssClass: "fw-bold text-primary" },
        { title: "상태", field: "USEYN", width: 60, hozAlign: "center", formatter: "tickCross", editor: true }
      ]
    })
    prodGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      if (data.ITEMCD) fetchDetails(data)
    })
    prodGrid.on("cellEdited", (cell) => {
        const field = cell.getField()
        if (field === 'PRDQTY' || field === 'OUTPRICE') {
            const data = cell.getData()
            const qty = Number(data.PRDQTY || 0)
            const price = Number(data.OUTPRICE || 0)
            const amt = Math.floor(qty * price)
            const vat = Math.floor(amt * 0.1)
            cell.getRow().update({ OUTAMT: amt, OUTVAT: vat, OUTTOT: amt + vat })
        }
        if (cell.getRow().getData().PROCGBN !== 'A') cell.getRow().update({ PROCGBN: 'U' })
    })
  }

  if (matGridElement.value) {
    matGrid = new Tabulator(matGridElement.value, {
      layout: "fitColumns", height: "100%", selectable: true,
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false },
        { title: "투입자재", field: "MITEMNM", minWidth: 200, cellClick: (e, cell) => { if(!cell.getValue()) openHelp('GRID_MAT', cell) } },
        { title: "규격", field: "MITSIZE", width: 150 },
        { title: "단위", field: "MUNIT", width: 80, hozAlign: "center" },
        { title: "소요량", field: "SOQTY", width: 100, hozAlign: "right", formatter: "money" },
        { title: "투입량", field: "INQTY", width: 100, hozAlign: "right", editor: "number", cssClass: "bg-light-green fw-bold" },
        { title: "재공출고공정", field: "BPROGNM", width: 180, cellClick: (e, cell) => openHelp('GRID_PROG', cell) },
        { title: "상태", field: "USEYN", width: 60, hozAlign: "center", formatter: "tickCross", editor: true }
      ]
    })
    matGrid.on("cellEdited", (cell) => {
        if (cell.getRow().getData().PROCGBN !== 'A') cell.getRow().update({ PROCGBN: 'U' })
    })
  }
}

// 3. 비즈니스 로직
const fetchMaster = async () => {
  if (!searchData.LINECD || !searchData.CUSTCD) return vAlertError('라인과 거래처를 선택하세요.')
  try {
    const res = await api.post('/api/hpio/HPIO_350U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      LINECD: searchData.LINECD, CUSTCD: searchData.CUSTCD,
      ORDYMD: searchData.ORDYMD, PROYMD: searchData.PROYMD
    })
    if (res.data && res.data.length > 0) {
        Object.assign(masterInfo, {
            WHCD: res.data[0].WHCD,
            IWHCD: res.data[0].IWHCD,
            PROGCD: res.data[0].INPROGCD
        })
    }
    prodGrid?.setData(res.data)
    matGrid?.clearData()
    selectedProduct.ITEMCD = ''; selectedProduct.ITEMNM = ''
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const fetchDetails = async (product: any) => {
  selectedProduct.ITEMCD = product.ITEMCD
  selectedProduct.ITEMNM = product.ITEMNM
  try {
    const res = await api.post('/api/hpio/HPIO_351U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      LINECD: searchData.LINECD, ITEMCD: product.ITEMCD, CUSTCD: searchData.CUSTCD,
      ORDYMD: searchData.ORDYMD, PROYMD: searchData.PROYMD
    })
    matGrid?.setData(res.data)
  } catch (e) {}
}

const addProductRow = () => {
  prodGrid?.addRow({ PROCGBN: 'A', ITEMCD: '', ITEMNM: '', PRDQTY: 0, USEYN: 'Y' }, true)
}

const addMaterialRow = () => {
  if (!selectedProduct.ITEMCD) return vAlertError('제품을 먼저 선택하세요.')
  matGrid?.addRow({ PROCGBN: 'A', MITEMCD: '', MITEMNM: '', INQTY: 0, USEYN: 'Y' }, true)
}

const saveAll = async () => {
  if (searchData.PROYMD <= closingInfo.CLSYMD) return vAlertError('회계 마감된 일자입니다.')

  const prodData = prodGrid?.getSelectedData() || []
  if (prodData.length === 0) return vAlertError('저장할 제품 실적이 없습니다.')

  if (!confirm('현재 입력된 실적 정보를 저장하시겠습니까?')) return

  try {
    for (const p of prodData) {
        await api.post('/api/hpio/HPIO_350U_STR', {
            ...p, ACTKIND: (p.PROCGBN || 'U') + '0', CMPYCD: authStore.CMPYCD, USERID: authStore.USERID,
            LINECD: searchData.LINECD, CUSTCD: searchData.CUSTCD,
            ORDYMD: searchData.ORDYMD, PROYMD: searchData.PROYMD,
            PROGCD: masterInfo.PROGCD, WHCD: masterInfo.WHCD,
            PRODCD: '200', WKGBN: '100', EQUPCD: '000', WORKJO: '000'
        })
    }

    if (selectedProduct.ITEMCD) {
        const matData = matGrid?.getSelectedData() || []
        for (const m of matData) {
            await api.post('/api/hpio/HPIO_351U_STR', {
                ...m, ACTKIND: (m.PROCGBN || 'U') + '0', CMPYCD: authStore.CMPYCD, USERID: authStore.USERID,
                LINECD: searchData.LINECD, ITEMCD: selectedProduct.ITEMCD, CUSTCD: searchData.CUSTCD,
                ORDYMD: searchData.ORDYMD, PROYMD: searchData.PROYMD,
                PROGCD: masterInfo.PROGCD, WHCD: masterInfo.WHCD,
                PRODCD: '200', WKGBN: '100', EQUPCD: '000', WORKJO: '000'
            })
        }
    }
    vAlert('정상적으로 저장되었습니다.')
    fetchMaster()
  } catch (e) { vAlertError('저장 처리 중 오류 발생') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { LINECD: '010', ORDYMD: initYMD, PROYMD: initYMD })
  Object.assign(masterInfo, { WHCD: '300', IWHCD: '200', PROGCD: '' })
  prodGrid?.clearData(); matGrid?.clearData()
  selectedProduct.ITEMCD = ''; selectedProduct.ITEMNM = ''
  fetchLineOptions()
  fetchWhOptions()
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell?: any) {
  let config: any = {}
  if (type === 'CUST') {
    config = { title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: '010', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 100 }, { title: '거래처명', field: 'CDNM', width: 200 }], onConfirm: (data: any) => { searchData.CUSTCD = data.CODE; searchData.CUSTNM = data.CDNM } }
  } else if (type === 'GRID_ITEM') {
    config = { title: '제품 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM', data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD, SELGBN: '200' }, columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '제품명', field: 'ITEMNM', width: 250 }], onConfirm: (data: any) => { cell.getRow().update({ ITEMCD: data.ITEMCD, ITEMNM: data.ITEMNM, ITSIZE: data.ITSIZE, UNIT: data.UNIT }) } }
  } else if (type === 'GRID_MAT') {
    config = { title: '자재 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM', data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '자재명', field: 'ITEMNM', width: 250 }], onConfirm: (data: any) => { cell.getRow().update({ MITEMCD: data.ITEMCD, MITEMNM: data.ITEMNM, MITSIZE: data.ITSIZE, MUNIT: data.UNIT, MASTKIND: data.ASTKIND }) } }
  } else if (type === 'GRID_PROG') {
    config = { title: '공정 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: 'G0', LINECD: searchData.LINECD, CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '공정명', field: 'CDNM', width: 150 }], onConfirm: (data: any) => { cell.getRow().update({ BEFPROG: data.CODE, BPROGNM: data.CDNM }) } }
  }
  if (Object.keys(config).length > 0) {
    Object.assign(modalProps, config); modalVisible.value = true
  }
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      closingInfo.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(r.data[0])[1]).trim()
      closingInfo.PCLSYM = String(Object.values(r.data[0])[2]).trim()
    }
  })
  fetchLineOptions()
  fetchWhOptions()
  nextTick(() => initGrids())
})
</script>

<style scoped>
.hpio350u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-save { background-color: #2ec4b6 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 90px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }

.bg-light-yellow { background-color: #fffde7 !important; }
.bg-light-green { background-color: #f1f8e9 !important; }
</style>
