<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio520u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam me-2 text-primary" style="font-size: 18px;"></i>
        재고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">재공품 타계정출고 (HPIO520U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchMaster">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="saveData">
          <i class="bi bi-save"></i> 저장
        </button>
        <button v-if="masterData.IONO" class="btn-erp btn-delete" @click="deleteData">
          <i class="bi bi-trash"></i> 삭제
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-3">
      <!-- 🅰️ 출고 마스터 정보 -->
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
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="masterData.LINECD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.LINENM" type="text" class="form-control" placeholder="라인 선택" @keyup.enter="openHelp('LINE')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('LINE')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">출고공정</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="masterData.PROGCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.PROGNM" type="text" class="form-control" placeholder="공정 선택" @keyup.enter="openHelp('PROG')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('PROG')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">출고번호</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 180px;">
                    <input v-model="uiIOYM" type="month" class="form-control form-control-sm" style="width: 120px;" />
                    <input v-model="masterData.IONO" type="text" class="form-control form-control-sm text-center bg-light" style="width: 50px;" readonly />
                  </div>
                </td>
                <th class="required">출고일자</th>
                <td>
                  <input v-model="uiIOYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
              </tr>
              <tr>
                <th>특기사항</th>
                <td colspan="7">
                  <input v-model="masterData.REMARK" type="text" class="form-control form-control-sm" placeholder="기타 특이사항 입력" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 재공품 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1 text-primary"></i> 재공품 출고 상세 내역</span>
          <div class="btn-group gap-1">
            <button class="btn btn-sm btn-outline-primary px-3" style="height: 28px; font-size: 12px; font-weight: 600;" @click="addRow">
              <i class="bi bi-plus-circle me-1"></i> 행추가
            </button>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-3 small">항목수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-3 small text-center">총 수량: <span class="fw-bold text-warning">{{ formatNumber(totalQty) }}</span></div>
        <div class="col-md-3 small text-center">총 금액: <span class="fw-bold text-success">{{ formatNumber(totalAmt) }}</span></div>
        <div class="col-md-3 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 재공품 상태에서 샘플이나 폐기 등으로 출고할 때 사용하는 화면입니다.
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
  LINECD: '010', LINENM: '통합라인',
  PROGCD: '', PROGNM: '',
  IOYM: initYM, IONO: '',
  IOYMD: initYMD,
  REMARK: '', SLIPNO: ''
})

const closingInfo = reactive({ CLSYMD: '', SCLSYM: '' })

const uiIOYM = computed({ get: () => `${masterData.IOYM.substring(0, 4)}-${masterData.IOYM.substring(4, 6)}`, set: (v) => { if (v) masterData.IOYM = v.replace(/-/g, '') } })
const uiIOYMD = computed({ get: () => formatDateString(masterData.IOYMD, '-'), set: (v) => { if (v) masterData.IOYMD = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)
const totalQty = ref(0)
const totalAmt = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns", height: "100%", selectable: true,
      columns: [
        { title: "상태", field: "UPKIND", width: 60, hozAlign: "center", vertAlign: "middle", cssClass: "text-danger fw-bold" },
        { title: "코드", field: "ITEMCD", width: 100, hozAlign: "center" },
        {
          title: "재공품명", field: "ITEMNM", minWidth: 200,
          cellClick: (e, cell) => openHelp('GRID_ITEM', cell)
        },
        {
          title: "유 형", field: "IOTYPENM", width: 120,
          cellClick: (e, cell) => openHelp('GRID_IOTYPE', cell)
        },
        { title: "규격", field: "ITSIZE", width: 150 },
        { title: "단위", field: "UNIT", width: 70, hozAlign: "center" },
        { title: "수량", field: "IOQTY", width: 100, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: (c:any)=>c.getData().QTYPNT||0 }, cssClass: "bg-light-yellow fw-bold" },
        { title: "금액", field: "IOAMT", width: 120, hozAlign: "right", editor: "number", formatter: "money", cssClass: "bg-light-yellow" },
        {
          title: "사용부서", field: "IDEPTNM", width: 120,
          cellClick: (e, cell) => openHelp('GRID_DEPT', cell)
        },
        {
          title: "거래처", field: "SCUSTNM", width: 150,
          cellClick: (e, cell) => openHelp('GRID_CUST', cell)
        },
        { title: "삭제", width: 60, hozAlign: "center", formatter: "buttonCross", cellClick: (e, cell) => cell.getRow().delete() }
      ]
    })
    grid.on("cellEdited", (cell) => {
        const row = cell.getRow()
        if (row.getData().UPKIND !== 'A') row.update({ UPKIND: 'U' })
        updateTotals()
    })
    grid.on("rowDeleted", () => updateTotals())
    grid.on("dataLoaded", () => updateTotals())
  }
}

const updateTotals = () => {
    const data = grid?.getData() || []
    itemCount.value = data.length
    totalQty.value = data.reduce((acc, cur) => acc + Number(cur.IOQTY || 0), 0)
    totalAmt.value = data.reduce((acc, cur) => acc + Number(cur.IOAMT || 0), 0)
}

// 3. 비즈니스 로직
const fetchMaster = async () => {
  if (!masterData.LINECD || !masterData.IOYM || !masterData.IONO) return vAlertError('조회 조건을 입력하세요.')
  try {
    const res = await api.post('/api/hpio/HPIO_520U_STR', {
      ACTKIND: 'S', CMPYCD: authStore.CMPYCD,
      IOYM: masterData.IOYM, IONO: masterData.IONO, LINECD: masterData.LINECD
    })
    if (res.data && res.data.length > 0) {
      Object.assign(masterData, res.data[0])
      fetchDetails()
      vAlert('조회되었습니다.')
    } else {
      vAlert('조회된 데이터가 없습니다.')
    }
  } catch (e) { vAlertError('마스터 조회 실패') }
}

const fetchDetails = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_521U_STR', {
      ACTKIND: 'S', CMPYCD: authStore.CMPYCD,
      IOYM: masterData.IOYM, IONO: masterData.IONO,
      LINECD: masterData.LINECD, PROGCD: masterData.PROGCD
    })
    grid?.setData(res.data)
  } catch (e) {}
}

const saveData = async () => {
  const ioYmd = masterData.IOYMD.replace(/-/g, '')
  if (ioYmd <= closingInfo.CLSYMD) return vAlertError('회계 마감된 일자입니다.')
  if (ioYmd.substring(0, 6) <= closingInfo.SCLSYM) return vAlertError('영업 마감된 월입니다.')
  if (masterData.SLIPNO > '000') return vAlertError('전표가 발행되어 수정할 수 없습니다.')
  if (!masterData.PROGCD) return vAlertError('출고공정을 선택하세요.')

  const details = grid?.getData() || []
  if (details.length === 0) return vAlertError('출고할 항목이 없습니다.')

  if (!confirm('저장하시겠습니까?')) return

  try {
    const actkind = !masterData.IONO ? 'A' : 'U'
    const resM = await api.post('/api/hpio/HPIO_520U_STR', {
      ...masterData, ACTKIND: actkind, CMPYCD: authStore.CMPYCD, USERID: authStore.USERID,
      IOYMD: ioYmd
    })

    const newIono = resM.data[0].OUTNO

    for (const item of details) {
        if (!item.ITEMCD) continue
        await api.post('/api/hpio/HPIO_521U_STR', {
            ...item, ACTKIND: item.UPKIND || 'U', CMPYCD: authStore.CMPYCD, USERID: authStore.USERID,
            IOYM: masterData.IOYM, IONO: newIono,
            LINECD: masterData.LINECD, PROGCD: masterData.PROGCD, IOYMD: ioYmd
        })
    }
    vAlert('정상적으로 저장되었습니다.')
    masterData.IONO = newIono
    fetchMaster()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const deleteData = async () => {
    if (masterData.SLIPNO > '000') return vAlertError('전표가 발행되어 삭제할 수 없습니다.')
    if (!confirm('현재 출고 내역 전체를 삭제하시겠습니까?')) return
    try {
        await api.post('/api/hpio/HPIO_520U_STR', { ...masterData, ACTKIND: 'D', CMPYCD: authStore.CMPYCD })
        vAlert('삭제되었습니다.')
        initialize()
    } catch (e) { vAlertError('삭제 실패') }
}

const addRow = () => {
  if (!masterData.LINECD) return vAlertError('생산라인을 먼저 선택하세요.')
  grid?.addRow({ UPKIND: 'A', ITEMCD: '', ITEMNM: '', IOQTY: 0, IOAMT: 0 }, true)
}

const initialize = () => {
  resetForm(masterData)
  Object.assign(masterData, {
      LINECD: '010', LINENM: '통합라인',
      IOYM: initYM, IONO: '', IOYMD: initYMD,
      PROGCD: '', PROGNM: '', REMARK: ''
  })
  grid?.clearData()
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell?: any) {
  let config: any = {}
  if (type === 'LINE') {
    config = { title: '라인 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '라인명', field: 'CDNM', width: 150 }], onConfirm: (data: any) => { masterData.LINECD = data.CODE; masterData.LINENM = data.CDNM } }
  } else if (type === 'PROG') {
    config = { title: '공정 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: 'I8', LINECD: masterData.LINECD, CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '공정명', field: 'CDNM', width: 150 }], onConfirm: (data: any) => { masterData.PROGCD = data.CODE; masterData.PROGNM = data.CDNM } }
  } else if (type === 'GRID_ITEM') {
    config = { title: '품목 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM', data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD, codegbn: '210' }, columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '품목명', field: 'ITEMNM', width: 250 }], onConfirm: (data: any) => { cell.getRow().update({ ITEMCD: data.ITEMCD, ITEMNM: data.ITEMNM, ITSIZE: data.ITSIZE, UNIT: data.UNIT, ASTKIND: data.ASTKIND, QTYPNT: data.QTYPNT }) } }
  } else if (type === 'GRID_IOTYPE') {
    config = { title: '출고유형 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: 'IOTYPE', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '유형명', field: 'CDNM', width: 150 }], onConfirm: (data: any) => { cell.getRow().update({ IOTYPE: data.CODE, IOTYPENM: data.CDNM }) } }
  } else if (type === 'GRID_DEPT') {
    config = { title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '부서명', field: 'CDNM', width: 150 }], onConfirm: (data: any) => { cell.getRow().update({ IDEPTCD: data.CODE, IDEPTNM: data.CDNM }) } }
  } else if (type === 'GRID_CUST') {
    config = { title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: '010', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 100 }, { title: '거래처명', field: 'CDNM', width: 200 }], onConfirm: (data: any) => { cell.getRow().update({ SCUSTCD: data.CODE, SCUSTNM: data.CDNM }) } }
  }
  Object.assign(modalProps, config); modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      closingInfo.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(r.data[0])[1]).trim()
    }
  })
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hpio520u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-save { background-color: #2ec4b6 !important; color: #fff !important; }
.btn-delete { background-color: #ef4444 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 95px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }

.bg-light-yellow { background-color: #fffde7 !important; }
</style>
