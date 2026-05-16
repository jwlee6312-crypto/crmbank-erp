<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio300u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam-fill me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">생산실적등록 (HPIO300U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchProcesses">
          <i class="bi bi-search"></i> 조회
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border-0 shadow-sm overflow-hidden" style="border-radius: 8px;">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">생산라인</th>
                <td>
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 200px;" @change="onLineChange">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
                </td>
                <th class="required">지시일자</th>
                <td>
                  <input v-model="uiORDYMD" type="date" class="form-control form-control-sm" style="width: 150px;" @change="onFilterChange" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 작업 영역 -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 공정 목록 -->
        <div class="card border-0 shadow-sm d-flex flex-column" style="width: 180px; border-radius: 8px;">
          <div class="card-header bg-light py-2 px-3 border-bottom fw-bold small text-dark text-center">
            생산공정
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-auto">
            <ul class="list-group list-group-flush process-list">
              <li
                v-for="prog in processList"
                :key="prog.PROGCD"
                class="list-group-item list-group-item-action text-center py-2 px-1 small"
                :class="{ active: selectedProg.PROGCD === prog.PROGCD }"
                @click="onProcessSelect(prog)"
              >
                {{ prog.PROGNM }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 우측 영역 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <!-- 우측 상단: 생산실적 그리드 -->
          <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark">
                <i class="bi bi-list-check me-1 text-primary"></i> 제품 생산 실적
                <span v-if="selectedProg.PROGNM" class="text-primary ms-2">[{{ selectedProg.PROGNM }}]</span>
              </span>
              <div class="btn-group gap-1">
                <button class="btn btn-erp btn-save py-0" style="height: 28px;" @click="savePerformance">
                  <i class="bi bi-save me-1"></i> 실적저장
                </button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
              <div ref="perfGridElement" style="height: 100%;"></div>
            </div>
          </div>

          <!-- 우측 하단: 투입자재 그리드 -->
          <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark">
                <i class="bi bi-box-arrow-in-right me-1 text-success"></i> 투입 자재 상세
                <span v-if="selectedProduct.ITEMNM" class="text-success ms-2">[{{ selectedProduct.ITEMNM }}]</span>
              </span>
              <div class="btn-group gap-2">
                <button class="btn btn-sm btn-outline-primary px-3" style="height: 28px; font-size: 12px; font-weight: 600;" @click="addMaterialRow">
                  <i class="bi bi-plus-circle me-1"></i> 행추가
                </button>
                <button class="btn btn-erp btn-save py-0" style="height: 28px; background-color: #2ec4b6 !important;" @click="saveMaterials">
                  <i class="bi bi-check2-circle me-1"></i> 자재저장
                </button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
              <div ref="matGridElement" style="height: 100%;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">실적건수: <span class="fw-bold text-info">{{ perfItemCount }}</span> 건</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 제품을 클릭하면 하단에 투입 자재 내역이 표시됩니다.
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
  ORDYMD: initYMD
})

const lineOptions = ref<any[]>([])
const processList = ref<any[]>([])
const selectedProg = reactive({ PROGCD: '', PROGNM: '' })
const selectedProduct = reactive<any>({})
const closingInfo = reactive({ CLSYMD: '', SCLSYM: '', PCLSYM: '', WCLSYM: '' })

const uiORDYMD = computed({
  get: () => formatDateString(searchData.ORDYMD, '-'),
  set: (v) => { if (v) searchData.ORDYMD = v.replace(/-/g, '') }
})

const perfGridElement = ref<HTMLElement | null>(null)
const matGridElement = ref<HTMLElement | null>(null)
let perfGrid: Tabulator | null = null
let matGrid: Tabulator | null = null
const perfItemCount = ref(0)

// 🏭 라인 목록 조회
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', {
      params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y', CODE: '' }
    })
    lineOptions.value = res.data.map((i: any) => ({ LINECD: i.LINECD, LINENM: i.LINENM }))
  } catch (e) { console.error(e) }
}

// ⚙️ 공정 목록 조회
const fetchProcesses = async () => {
  if (!searchData.LINECD) return
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', {
      params: { GUBUN: 'G0', CMPYCD: authStore.CMPYCD, GBNCD: searchData.LINECD, CODE: '' }
    })
    processList.value = res.data.map((i: any) => ({ PROGCD: i.PROGCD, PROGNM: i.PROGNM }))
    perfGrid?.clearData()
    matGrid?.clearData()
    selectedProg.PROGCD = ''; selectedProg.PROGNM = ''
    selectedProduct.ITEMCD = ''; selectedProduct.ITEMNM = ''
  } catch (e) { console.error(e) }
}

const onLineChange = () => {
  fetchProcesses()
}

const onFilterChange = () => {
  if (selectedProg.PROGCD) fetchPerfData()
}

// 공정 선택
const onProcessSelect = (prog: any) => {
  selectedProg.PROGCD = prog.PROGCD
  selectedProg.PROGNM = prog.PROGNM
  fetchPerfData()
}

// 실적 데이터 조회 (S0)
const fetchPerfData = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_300U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      LINECD: searchData.LINECD,
      PROGCD: selectedProg.PROGCD,
      ORDYMD: searchData.ORDYMD,
      PROYMD: '', EQUPCD: '000', PRODCD: '', WORKJO: '000', WKGBN: '100'
    })
    perfGrid?.setData(res.data)
    perfItemCount.value = res.data.length
    matGrid?.clearData()
    Object.keys(selectedProduct).forEach(key => delete selectedProduct[key])
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('실적 조회 실패') }
}

// 자재 데이터 조회 (S0)
const fetchMaterialData = async (product: any) => {
  Object.assign(selectedProduct, product)
  try {
    const res = await api.post('/api/hpio/HPIO_301U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      LINECD: searchData.LINECD,
      PROGCD: selectedProg.PROGCD,
      PROYMD: product.PROYMD,
      ORDYMD: product.ORDYMD,
      ITEMCD: product.ITEMCD,
      WHCD: product.WHCD || '300',
      LOTYMD: product.LOTYMD,
      LOTNO: product.LOTNO,
      ORDYM: product.ORDYM,
      ORDNO: product.ORDNO,
      PRODCD: product.PRODCD,
      EQUPCD: '000', WORKJO: '000', WKGBN: '100'
    })
    matGrid?.setData(res.data)
  } catch (e) { vAlertError('자재 상세 조회 실패') }
}

// 2. 그리드 초기화
const initGrids = () => {
  if (perfGridElement.value) {
    perfGrid = new Tabulator(perfGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      selectable: true,
      columns: [
        { title: "", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", vertAlign: "middle", headerSort: false, frozen: true },
        {
          title: "제품명", field: "ITEMNM",  vertAlign: "middle", frozen: true,
          cellClick: (e, cell) => { if(cell.getData().PRDQTY !== 0) fetchMaterialData(cell.getData()) }
        },
        { title: "규격", field: "ITSIZE", width: 150, vertAlign: "middle", frozen: true },
        { title: "단위", field: "UNIT", width: 70, hozAlign: "center", vertAlign: "middle", frozen: true },
        { title: "생산일자", field: "PROYMD", width: 110, editor: "input", formatter: (c) => formatDateString(c.getValue() || initYMD, '-'), vertAlign: "middle" },
        { title: "지시량", field: "ORDQTY", width: 100, hozAlign: "right", vertAlign: "middle", formatter: "money", formatterParams: { precision: 0 } },
        { title: "총생산", field: "TOTQTY", width: 100, hozAlign: "right", vertAlign: "middle", formatter: "money", formatterParams: { precision: 0 } },
        { title: "금회생산", field: "PRDQTY", width: 100, hozAlign: "right", editor: "number", vertAlign: "middle", cssClass: "bg-light-yellow fw-bold" },
        { title: "작업시간", field: "WORKMM", width: 100, hozAlign: "right", editor: "number", vertAlign: "middle" },
        { title: "불량", field: "ERRQTY", width: 100, hozAlign: "right", editor: "number", vertAlign: "middle" },
        {
          title: "불량원인", field: "WONINNM", width: 200, editor: "input", vertAlign: "middle",
          cellClick: (e, cell) => openHelp('REASON', cell)
        },
        { title: "상태", field: "USEYN", width: 100, hozAlign: "center", vertAlign: "middle", formatter: "tickCross", editor: true }
      ]
    })
  }

  if (matGridElement.value) {
    matGrid = new Tabulator(matGridElement.value, {
      layout: "fitColumns", height: "100%", selectable: true,
      columns: [
        { title: "", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", vertAlign: "middle", headerSort: false, frozen: true },
        {
          title: "투입자재명", field: "ITEMNM", vertAlign: "middle",
          cellClick: (e, cell) => openHelp('MATERIAL', cell)
        },
        { title: "규격", field: "MITSIZE", width: 200, vertAlign: "middle" },
        { title: "단위", field: "MUNIT", width: 70, hozAlign: "center", vertAlign: "middle" },
        { title: "재고", field: "JAEGO", width: 100, hozAlign: "right", vertAlign: "middle", formatter: "money", formatterParams: { precision: 0 } },
        { title: "소요량", field: "SOQTY", width: 100, hozAlign: "right", vertAlign: "middle", formatter: "money", formatterParams: { precision: 0 } },
        { title: "투입량", field: "INQTY", width: 100, hozAlign: "right", editor: "number", vertAlign: "middle", cssClass: "bg-light-green fw-bold" },
        {
          title: "전공정", field: "PROGNM", width: 200, vertAlign: "middle",
          cellClick: (e, cell) => openHelp('BEFPROG', cell)
        },
        { title: "상태", field: "USEYN", width: 100, hozAlign: "center", vertAlign: "middle", formatter: "tickCross", editor: true }
      ]
    })
  }
}

// 실적 저장
const savePerformance = async () => {
  const selected = perfGrid?.getSelectedData() || []
  if (selected.length === 0) return vAlertError('저장할 실적 항목을 선택하세요.')

  // 마감 체크 및 유효성 검사
  for (const item of selected) {
      if (!item.PROYMD) item.PROYMD = searchData.ORDYMD
      const proYmd = item.PROYMD.replace(/-/g, '')
      if (proYmd <= closingInfo.CLSYMD) return vAlertError(`회계마감일(${closingInfo.CLSYMD}) 이전으로는 저장 불가합니다.`)
      if (item.PRDQTY <= 0 && item.USEYN !== 'N') return vAlertError('생산량을 입력하세요.')
  }

  if (!confirm('제품 생산 실적을 저장하시겠습니까?')) return

  try {
    for (const item of selected) {
      const actkind = !item.PRDQTY_O || item.PRDQTY_O === 0 ? 'A0' : 'U0'
      const godQty = Number(item.PRDQTY || 0) - Number(item.ERRQTY || 0)

      await api.post('/api/hpio/HPIO_300U_STR', {
        ACTKIND: actkind, CMPYCD: authStore.CMPYCD, USERID: authStore.USERID,
        LINECD: searchData.LINECD, PROGCD: selectedProg.PROGCD,
        PROYMD: item.PROYMD.replace(/-/g, ''), ORDYMD: searchData.ORDYMD,
        ITEMCD: item.ITEMCD, ITSIZE: item.ITSIZE, UNIT: item.UNIT,
        PRDQTY: item.PRDQTY, GODQTY: godQty, ERRQTY: item.ERRQTY,
        WONINCD: item.WONINCD, LOTYMD: item.LOTYMD, LOTNO: item.LOTNO,
        WORKMM: item.WORKMM, ORDYM: item.ORDYM, ORDNO: item.ORDNO,
        USEYN: item.USEYN || 'Y', WHCD: item.WHCD || '300',
        EQUPCD: '000', PRODCD: item.PRODCD || '100', WORKJO: '000', WKGBN: '100'
      })
    }
    vAlert('실적이 저장되었습니다.')
    fetchPerfData()
  } catch (e) { vAlertError('실적 저장 실패') }
}

// 자재 저장
const saveMaterials = async () => {
  const selected = matGrid?.getSelectedData() || []
  if (selected.length === 0) return vAlertError('저장할 자재 항목을 선택하세요.')
  if (!selectedProduct.ITEMCD) return vAlertError('먼저 상단에서 제품을 선택하세요.')

  if (!confirm('투입 자재 상세 내역을 저장하시겠습니까?')) return

  try {
    for (const item of selected) {
      const actkind = item.PROCGBN === 'A' ? 'A0' : 'U0'
      await api.post('/api/hpio/HPIO_301U_STR', {
        ACTKIND: actkind, CMPYCD: authStore.CMPYCD, USERID: authStore.USERID,
        LINECD: searchData.LINECD, PROGCD: selectedProg.PROGCD,
        PROYMD: selectedProduct.PROYMD, ORDYMD: selectedProduct.ORDYMD,
        ITEMCD: selectedProduct.ITEMCD, MITEMCD: item.MITEMCD,
        MITSIZE: item.MITSIZE, MUNIT: item.MUNIT,
        SOQTY: item.SOQTY, INQTY: item.INQTY,
        BEFPROG: item.BEFPROG, MASTKIND: item.ASTKIND || '210',
        USEYN: item.USEYN === 'N' ? 'N' : 'Y',
        LOTYMD: selectedProduct.LOTYMD, LOTNO: selectedProduct.LOTNO,
        ORDYM: selectedProduct.ORDYM, ORDNO: selectedProduct.ORDNO,
        WHCD: selectedProduct.WHCD || '300', EQUPCD: '000', PRODCD: selectedProduct.PRODCD || '100', WORKJO: '000', WKGBN: '100'
      })
    }
    vAlert('자재 정보가 저장되었습니다.')
    fetchMaterialData(selectedProduct)
  } catch (e) { vAlertError('자재 저장 실패') }
}

const addMaterialRow = () => {
  if (!selectedProduct.ITEMCD) return vAlertError('먼저 상단에서 제품을 선택하세요.')
  matGrid?.addRow({ PROCGBN: 'A', MITEMCD: '', ITEMNM: '', SOQTY: 0, INQTY: 0, USEYN: 'Y' }, true)
}

const initialize = () => {
  searchData.LINECD = '010'
  searchData.ORDYMD = initYMD
  processList.value = []
  perfGrid?.clearData()
  matGrid?.clearData()
  selectedProg.PROGCD = ''; selectedProg.PROGNM = ''
  Object.keys(selectedProduct).forEach(key => delete selectedProduct[key])
  fetchLineOptions()
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell: any) {
  let config: any = {}
  if (type === 'REASON') {
    config = {
      title: '불량원인 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM',
      data: { GUBUN: 'E0', GBNCD: '210', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '원인명', field: 'CDNM', width: 200 }],
      onConfirm: (data: any) => { cell.getRow().update({ WONINCD: data.CODE, WONINNM: data.CDNM }) }
    }
  } else if (type === 'MATERIAL') {
    config = {
      title: '자재 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM',
      data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '자재명', field: 'ITEMNM', width: 250 }],
      onConfirm: (data: any) => { cell.getRow().update({ MITEMCD: data.ITEMCD, ITEMNM: data.ITEMNM, MITSIZE: data.ITSIZE, MUNIT: data.UNIT, ASTKIND: data.ASTKIND }) }
    }
  } else if (type === 'BEFPROG') {
    config = {
        title: '전공정 선택', path: '/api/hp00/HP00_000S_STR', defaultField: 'PROGNM',
        data: { GUBUN: 'G0', LINECD: searchData.LINECD, CMPYCD: authStore.CMPYCD },
        columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '공정명', field: 'CDNM', width: 200 }],
        onConfirm: (data: any) => { cell.getRow().update({ BEFPROG: data.CODE, PROGNM: data.CDNM }) }
    }
  }

  Object.assign(modalProps, config)
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(async () => {
  // 마감 정보 조회
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      closingInfo.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(r.data[0])[1]).trim()
      closingInfo.PCLSYM = String(Object.values(r.data[0])[2]).trim()
      closingInfo.WCLSYM = String(Object.values(r.data[0])[3]).trim()
    }
  })
  fetchLineOptions()
  nextTick(() => { initGrids() })
})
</script>

<style scoped>
.hpio300u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

/* 메인 버튼 스타일 통일 */
.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }
.btn-save { background-color: #2ec4b6 !important; color: #fff !important; }
.btn-save:hover { background-color: #28afa3 !important; transform: translateY(-1px); }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* 공정 목록 스타일 */
.process-list .list-group-item { cursor: pointer; font-size: 12.5px; border-left: 4px solid transparent; transition: all 0.2s; }
.process-list .list-group-item:hover { background-color: #f8f9fa; }
.process-list .list-group-item.active { background-color: #eef2ff; color: #4361ee; border-left-color: #4361ee; font-weight: bold; }

/* Tabulator 스타일 기준 */
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }

.bg-light-yellow { background-color: #fffde7 !important; }
.bg-light-green { background-color: #f1f8e9 !important; }

/* 스크롤바 커스텀 */
.scrollbar-custom::-webkit-scrollbar { width: 8px; height: 8px; }
.scrollbar-custom::-webkit-scrollbar-thumb { background: #ccc; border-radius: 4px; }
</style>
