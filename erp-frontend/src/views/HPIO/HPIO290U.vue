<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio290u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-pencil-square me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">외주(임)가공계약등록 (HPIO290U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchMaster">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="saveAll">
          <i class="bi bi-save"></i> 전체저ar
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-3">
      <!-- 🅰️ 계약 마스터 정보 -->
      <div class="card border-0 shadow-sm overflow-hidden" style="border-radius: 8px;">
        <div class="card-header bg-light py-2 px-3 border-bottom fw-bold small text-dark">
          <i class="bi bi-file-earmark-text me-1 text-primary"></i> 계약 기본 정보
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <colgroup>
              <col style="width: 100px;"><col>
              <col style="width: 100px;"><col>
              <col style="width: 100px;"><col>
            </colgroup>
            <tbody>
              <tr>
                <th class="required">품의부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">품의번호</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 180px;">
                    <input v-model="uiPUMYM" type="month" class="form-control form-control-sm" style="width: 120px;" />
                    <input v-model="masterData.PUMNO" type="text" class="form-control form-control-sm text-center bg-light" style="width: 50px;" readonly />
                  </div>
                </td>
                <th class="required">품의일자</th>
                <td>
                  <input v-model="uiPUMYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
              </tr>
              <tr>
                <th class="required">거 래 처</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">계약기간</th>
                <td colspan="3">
                  <div class="d-flex align-items-center gap-1" style="width: 300px;">
                    <input v-model="uiFRYMD" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiTOYMD" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
              </tr>
              <tr>
                <th>특기사항</th>
                <td colspan="5">
                  <textarea v-model="masterData.REMARK" class="form-control form-control-sm" rows="1"></textarea>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 대상 제품 그리드 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px; min-height: 250px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-box-seam me-1 text-primary"></i> 계약 대상 제품</span>
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

      <!-- 🅲 소요 자재 그리드 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px; min-height: 250px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark">
            <i class="bi bi-tools me-1 text-success"></i> 소요 자재 내역
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

    <!-- 📊 하단 상태 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-6 small">
          <i class="bi bi-info-circle me-1"></i> 제품을 클릭하면 하단의 소요 자재 현황을 볼 수 있습니다.
        </div>
        <div class="col-md-6 text-end small opacity-75">
          외주가공 계약정보를 등록/수정하는 화면입니다.
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
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`
const initYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`

// 1. 상태 관리
const masterData = reactive({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  PUMYM: initYM, PUMNO: '000',
  PUMYMD: initYMD, FRYMD: initYMD, TOYMD: initYMD,
  CUSTCD: '', CUSTNM: '', REMARK: ''
})

const uiPUMYM = computed({ get: () => `${masterData.PUMYM.substring(0, 4)}-${masterData.PUMYM.substring(4, 6)}`, set: (v) => { if (v) masterData.PUMYM = v.replace(/-/g, '') } })
const uiPUMYMD = computed({ get: () => formatDateString(masterData.PUMYMD, '-'), set: (v) => { if (v) masterData.PUMYMD = v.replace(/-/g, '') } })
const uiFRYMD = computed({ get: () => formatDateString(masterData.FRYMD, '-'), set: (v) => { if (v) masterData.FRYMD = v.replace(/-/g, '') } })
const uiTOYMD = computed({ get: () => formatDateString(masterData.TOYMD, '-'), set: (v) => { if (v) masterData.TOYMD = v.replace(/-/g, '') } })

const selectedProduct = reactive<any>({ ITEMCD: '', ITEMNM: '' })

const prodGridElement = ref<HTMLElement | null>(null)
const matGridElement = ref<HTMLElement | null>(null)
let prodGrid: Tabulator | null = null
let matGrid: Tabulator | null = null

// 2. 그리드 초기화
const initGrids = () => {
  if (prodGridElement.value) {
    prodGrid = new Tabulator(prodGridElement.value, {
      layout: "fitColumns", height: "100%", selectable: 1,
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false },
        {
          title: "제품명", field: "ITEMNM", minWidth: 250,
          cellClick: (e, cell) => { openHelp('ITEM', cell) }
        },
        { title: "규격", field: "ITSIZE", width: 150 },
        { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
        { title: "계약단가", field: "PRICE", width: 120, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 2 } },
        { title: "비고", field: "REMARK", minWidth: 200, editor: "input" },
        { title: "삭제", field: "USEYN", width: 70, hozAlign: "center", formatter: "tickCross", editor: true }
      ]
    })
    prodGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      if (data.ITEMCD) fetchMaterials(data)
    })
  }

  if (matGridElement.value) {
    matGrid = new Tabulator(matGridElement.value, {
      layout: "fitColumns", height: "100%", selectable: true,
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false },
        {
          title: "소요자재", field: "MITEMNM", minWidth: 250,
          cellClick: (e, cell) => { openHelp('MATERIAL', cell) }
        },
        { title: "규격", field: "MITSIZE", width: 150 },
        { title: "단위", field: "MUNIT", width: 80, hozAlign: "center" },
        { title: "소요량", field: "SOQTY", width: 120, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 2 } },
        { title: "비고", field: "REMARK", minWidth: 200, editor: "input" },
        { title: "삭제", field: "USEYN", width: 70, hozAlign: "center", formatter: "tickCross", editor: true }
      ]
    })
  }
}

// 3. 비즈니스 로직
const fetchMaster = async () => {
  if (!masterData.DEPTCD) return vAlertError('부서를 선택하세요.')
  try {
    const res = await api.post('/api/hpio/HPIO_290U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      PUMYM: masterData.PUMYM, PUMNO: masterData.PUMNO,
      DEPTCD: masterData.DEPTCD, PUMGBN: '200'
    })

    if (res.data && res.data.length > 0) {
      Object.assign(masterData, res.data[0])
      fetchProducts()
    } else {
      vAlert('조회된 데이터가 없습니다.')
    }
  } catch (e) { vAlertError('마스터 조회 실패') }
}

const fetchProducts = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_291U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      PUMYM: masterData.PUMYM, PUMNO: masterData.PUMNO
    })
    prodGrid?.setData(res.data)
    matGrid?.clearData()
    selectedProduct.ITEMCD = ''; selectedProduct.ITEMNM = ''
  } catch (e) {}
}

const fetchMaterials = async (product: any) => {
  selectedProduct.ITEMCD = product.ITEMCD
  selectedProduct.ITEMNM = product.ITEMNM
  try {
    const res = await api.post('/api/hpio/HPIO_292U_STR', {
      ACTKIND: 'S', CMPYCD: authStore.CMPYCD,
      PUMYM: masterData.PUMYM, PUMNO: masterData.PUMNO, ITEMCD: product.ITEMCD
    })
    matGrid?.setData(res.data)
  } catch (e) {}
}

const addProductRow = () => {
  prodGrid?.addRow({ PROCGBN: 'A0', ITEMCD: '', ITEMNM: '', PRICE: 0, USEYN: 'Y' }, true)
}

const addMaterialRow = () => {
  if (!selectedProduct.ITEMCD) return vAlertError('먼저 제품을 선택하세요.')
  matGrid?.addRow({ PROCGBN: 'A', MITEMCD: '', MITEMNM: '', SOQTY: 0, USEYN: 'Y' }, true)
}

const saveAll = async () => {
  if (!masterData.CUSTCD) return vAlertError('거래처를 선택하세요.')
  if (!confirm('현재 정보를 저장하시겠습니까?')) return

  try {
    // 1. 마스터 저장 (A0/U0)
    const actkind = masterData.PUMNO === '000' ? 'A0' : 'U0'
    const resM = await api.post('/api/hpio/HPIO_290U_STR', {
      ...masterData, ACTKIND: actkind, CMPYCD: authStore.CMPYCD, USERID: authStore.USERID, PUMGBN: '200'
    })

    const newPumYm = resM.data[0].PUMYM; const newPumNo = resM.data[0].PUMNO

    // 2. 제품 정보 저장
    const prodData = prodGrid?.getSelectedData() || []
    for (const p of prodData) {
      const pKind = p.PROCGBN || 'U0'
      await api.post('/api/hpio/HPIO_291U_STR', {
        ...p, ACTKIND: pKind, CMPYCD: authStore.CMPYCD, PUMYM: newPumYm, PUMNO: newPumNo, USERID: authStore.USERID
      })
    }

    // 3. 현재 선택된 제품의 자재 저장 (있을 경우)
    if (selectedProduct.ITEMCD) {
      const matData = matGrid?.getSelectedData() || []
      for (const m of matData) {
        await api.post('/api/hpio/HPIO_292U_STR', {
          ...m, ACTKIND: m.PROCGBN || 'U', CMPYCD: authStore.CMPYCD, PUMYM: newPumYm, PUMNO: newPumNo, ITEMCD: selectedProduct.ITEMCD, USERID: authStore.USERID
        })
      }
    }

    vAlert('정상적으로 저장되었습니다.')
    masterData.PUMYM = newPumYm; masterData.PUMNO = newPumNo
    fetchMaster()
  } catch (e) { vAlertError('저장 처리 중 오류 발생') }
}

const initialize = () => {
  resetForm(masterData)
  Object.assign(masterData, { PUMYM: initYM, PUMNO: '000', PUMYMD: initYMD, FRYMD: initYMD, TOYMD: initYMD, DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM })
  prodGrid?.clearData(); matGrid?.clearData()
  selectedProduct.ITEMCD = ''; selectedProduct.ITEMNM = ''
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell?: any) {
  let config: any = {}
  if (type === 'DEPT') {
    config = { title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '부서명', field: 'CDNM', width: 200 }], onConfirm: (data: any) => { masterData.DEPTCD = data.CODE; masterData.DEPTNM = data.CDNM } }
  } else if (type === 'CUST') {
    config = { title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: '010', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 100 }, { title: '거래처명', field: 'CDNM', width: 250 }], onConfirm: (data: any) => { masterData.CUSTCD = data.CODE; masterData.CUSTNM = data.CDNM } }
  } else if (type === 'ITEM') {
    config = { title: '제품 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM', data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD, SELGBN: '200' }, columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '제품명', field: 'ITEMNM', width: 250 }], onConfirm: (data: any) => { cell.getRow().update({ ITEMCD: data.ITEMCD, ITEMNM: data.ITEMNM, ITSIZE: data.ITSIZE, UNIT: data.UNIT }) } }
  } else if (type === 'MATERIAL') {
    config = { title: '자재 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM', data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '자재명', field: 'ITEMNM', width: 250 }], onConfirm: (data: any) => { cell.getRow().update({ MITEMCD: data.ITEMCD, MITEMNM: data.ITEMNM, MITSIZE: data.ITSIZE, MUNIT: data.UNIT }) } }
  }
  Object.assign(modalProps, config); modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(() => { nextTick(() => initGrids()) })
</script>

<style scoped>
.hpio290u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-save { background-color: #2ec4b6 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 110px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }
</style>
