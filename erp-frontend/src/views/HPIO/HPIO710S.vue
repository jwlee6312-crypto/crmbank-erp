<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio710s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-in-down me-2 text-primary" style="font-size: 18px;"></i>
        재고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">입출고 현황 (HPIO710S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchList">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-excel" @click="exportExcel">
          <i class="bi bi-file-earmark-excel"></i> 엑셀
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
              <col style="width: 90px;"><col>
              <col style="width: 90px;"><col>
              <col style="width: 90px;"><col>
              <col style="width: 90px;"><col>
            </colgroup>
            <tbody>
              <tr>
                <th class="required">입고창고</th>
                <td>
                  <select v-model="searchData.WHCD" class="form-select form-select-sm" style="width: 150px;">
                    <option value="000">전체</option>
                    <option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
                  </select>
                </td>
                <th class="required">입고유형</th>
                <td>
                  <select v-model="searchData.IOTYPE" class="form-select form-select-sm" style="width: 150px;">
                    <option value="000">전체</option>
                    <option v-for="opt in iotypeOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
                  </select>
                </td>
                <th class="required">입고일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiFRYMD" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiTOYMD" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
              </tr>
              <tr>
                <th>거 래 처</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="searchData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>입고품목</th>
                <td colspan="5">
                  <div class="input-group input-group-sm" style="width: 450px;">
                    <input v-model="searchData.ITEMCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="searchData.ITEMNM" type="text" class="form-control" placeholder="품목 선택" @keyup.enter="openHelp('ITEM')" />
                    <input v-model="searchData.ITSIZE" type="text" class="form-control bg-light" style="max-width: 100px;" readonly />
                    <input v-model="searchData.UNIT" type="text" class="form-control bg-light text-center" style="max-width: 50px;" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('ITEM')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-check me-1 text-primary"></i> 입고 상세 내역</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-3 small">조회건수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-9 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 품목명을 클릭하면 해당 입고 등록 화면으로 이동합니다.
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
const initTYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initFRYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  WHCD: '000',
  IOTYPE: '000',
  FRYMD: initFRYMD,
  TOYMD: initTYMD,
  CUSTCD: '', CUSTNM: '',
  ITEMCD: '', ITEMNM: '', ITSIZE: '', UNIT: ''
})

const whOptions = ref<any[]>([])
const iotypeOptions = ref<any[]>([])

const uiFRYMD = computed({ get: () => formatDateString(searchData.FRYMD, '-'), set: (v) => { if (v) searchData.FRYMD = v.replace(/-/g, '') } })
const uiTOYMD = computed({ get: () => formatDateString(searchData.TOYMD, '-'), set: (v) => { if (v) searchData.TOYMD = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 기초 코드 로드
const fetchOptions = async () => {
  try {
    // 창고 코드
    const resWh = await api.get('/api/comm/codes/WH')
    whOptions.value = resWh.data

    // 입고 유형 (HS00_000S_STR 'E0', '120')
    const resIo = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '120' } })
    iotypeOptions.value = resIo.data
  } catch (e) {}
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      columns: [
        { title: "일 자", field: "IOYMD", width: 110, hozAlign: "center", formatter: (c) => formatDateString(c.getValue(), '-') },
        {
          title: "품 명", field: "ITEMNM", minWidth: 200,
          formatter: (cell) => `<span class="${cell.getData().GUBUN === '1' && cell.getData().IOTYPE === '100' ? 'text-primary text-decoration-underline cursor-pointer fw-bold' : 'fw-bold'}">${cell.getValue()}</span>`,
          cellClick: (e, cell) => {
            const d = cell.getData()
            if (d.GUBUN === '1' && d.IOTYPE === '100') {
              router.push({
                path: '/HSIO100U',
                query: { DEPTCD: d.DEPTCD, IOYM: d.IOYM, IONO: d.IONO }
              })
            }
          },
          bottomCalc: () => "합 계", bottomCalcHozAlign: "center"
        },
        { title: "규격", field: "ITSIZE", width: 150 },
        { title: "단위", field: "UNIT", width: 60, hozAlign: "center" },
        { title: "거래처", field: "CUSTNM", minWidth: 150 },
        { title: "수량", field: "IOQTY", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 }, bottomCalc: "sum" },
        { title: "단가", field: "PRICE", width: 100, hozAlign: "right", formatter: "money" },
        { title: "공급가", field: "JSANAMT", width: 110, hozAlign: "right", formatter: "money", bottomCalc: "sum" },
        { title: "부가세", field: "JSANVAT", width: 100, hozAlign: "right", formatter: "money", bottomCalc: "sum" },
        {
          title: "합 계", field: "TOT_AMT", width: 120, hozAlign: "right",
          formatter: (cell) => formatNumber(Number(cell.getData().JSANAMT || 0) + Number(cell.getData().JSANVAT || 0)),
          bottomCalc: "sum", cssClass: "text-primary fw-bold"
        }
      ],
    })
  }
}

// 3. 비즈니스 로직
const fetchList = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_710S_STR', {
      CMPYCD: authStore.CMPYCD,
      WHCD: searchData.WHCD,
      IOTYPE: searchData.IOTYPE,
      FRYMD: searchData.FRYMD,
      TOYMD: searchData.TOYMD,
      CUSTCD: searchData.CUSTCD,
      ITEMCD: searchData.ITEMCD
    })
    grid?.setData(res.data)
    itemCount.value = res.data.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 처리 중 오류 발생') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { WHCD: '000', IOTYPE: '000', FRYMD: initFRYMD, TOYMD: initTYMD })
  grid?.clearData()
  itemCount.value = 0
}

const exportExcel = () => {
  grid?.download("xlsx", `입출고현황_${searchData.TOYMD}.xlsx`, { title: "입출고 현황" })
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let config: any = {}
  if (type === 'CUST') {
    config = {
        title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM',
        data: { GUBUN: '010', CMPYCD: authStore.CMPYCD },
        columns: [{ title: '코드', field: 'CODE', width: 100 }, { title: '거래처명', field: 'CDNM', width: 250 }],
        onConfirm: (data: any) => { searchData.CUSTCD = data.CODE; searchData.CUSTNM = data.CDNM }
    }
  } else if (type === 'ITEM') {
    config = {
        title: '품목 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM',
        data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD, asetkind: '1' },
        columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '품목명', field: 'ITEMNM', width: 250 }, { title: '규격', field: 'ITSIZE', width: 120 }],
        onConfirm: (data: any) => { searchData.ITEMCD = data.ITEMCD; searchData.ITEMNM = data.ITEMNM; searchData.ITSIZE = data.ITSIZE; searchData.UNIT = data.UNIT }
    }
  }
  Object.assign(modalProps, config); modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => { initGrid(); fetchList(); })
})
</script>

<style scoped>
.hpio710s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }
.btn-excel { background-color: #107c41 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* Tabulator 스타일 */
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-footer) { background-color: #f8f9fa !important; border-top: 2px solid #dee2e6 !important; font-weight: 800; }
.cursor-pointer { cursor: pointer; }
</style>
