<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio200u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-tags-fill me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">LOT부여작업 (HPIO200U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchList">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="saveData">
          <i class="bi bi-save"></i> 저장
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
                  <!-- 💎 팝업에서 콤보박스로 변경 -->
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 200px;" @change="onLineChange">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
                </td>
                <th class="required">LOT일자</th>
                <td>
                  <input v-model="uiLOTYMD" type="date" class="form-control form-control-sm" style="width: 150px;" />
                </td>
                <th>주문번호</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="uiORDYM" type="month" class="form-control" />
                    <input v-model="searchData.ORDNO" type="text" class="form-control text-center" placeholder="번호" maxlength="4" />
                    <button class="btn btn-outline-secondary" @click="openHelp('ORDER')"><i class="bi bi-search"></i></button>
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
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap me-1 text-primary"></i> LOT 부여 내역</span>
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
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`

const searchData = reactive({ LINECD: '', LOTYMD: initYMD, ORDYM: initYM, ORDNO: '' })
const lineOptions = ref<any[]>([])
const uiLOTYMD = computed({ get: () => formatDateString(searchData.LOTYMD, '-'), set: (v) => { if (v) searchData.LOTYMD = v.replace(/-/g, '') } })
const uiORDYM = computed({ get: () => searchData.ORDYM ? `${searchData.ORDYM.substring(0, 4)}-${searchData.ORDYM.substring(4, 6)}` : '', set: (v) => { if (v) searchData.ORDYM = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 라인 옵션 로드 (콤보박스용)
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data
  } catch (e) {}
}

const onLineChange = () => { if (searchData.LINECD) fetchList() }

const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns", height: "100%", selectable: true,
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 50, hozAlign: "center", headerSort: false },
        { title: "제품명", field: "ITEMNM", minWidth: 250, cellClick: (e, cell) => { if (!searchData.LINECD) return vAlertError('생산라인을 먼저 선택하세요.'); openHelp('ITEM', cell) } },
        { title: "규격", field: "ITSIZE", width: 150, hozAlign: "center" },
        { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
        { title: "LOT SIZE", field: "LOTSIZE", width: 120, hozAlign: "right", editor: "number" },
        { title: "투입일자", field: "STYMD", width: 150, hozAlign: "center", editor: "input", formatter: (c) => formatDateString(c.getValue(), '-') },
        { title: "완료일자", field: "TOYMD", width: 150, hozAlign: "center", editor: "input", formatter: (c) => formatDateString(c.getValue(), '-') },
        { title: "비고", field: "BIGO", minWidth: 150, editor: "input" },
        { title: "삭제", field: "USEYN", width: 80, hozAlign: "center", formatter: "tickCross", editor: true }
      ],
    })
    grid.on("cellEdited", (cell) => cell.getRow().select())
  }
}

const fetchList = async () => {
  if (!searchData.LINECD) return
  try {
    const res = await api.post('/api/hpio/HPIO_200U_STR', { ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, LINECD: searchData.LINECD, LOTYMD: searchData.LOTYMD })
    grid?.setData(res.data)
    itemCount.value = res.data.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const addRow = () => {
  if (!searchData.LINECD) return vAlertError('생산라인을 먼저 선택하세요.')
  grid?.addRow({ PROCGBN: 'A', LOTNO: '000', ITEMCD: '', ITEMNM: '', ITSIZE: '', UNIT: '', LOTSIZE: 0, STYMD: searchData.LOTYMD, TOYMD: '', BIGO: '', USEYN: 'Y' }, true)
}

const saveData = async () => {
  const selectedData = grid?.getSelectedData() || []
  if (selectedData.length === 0) return vAlertError('저장할 항목을 선택하세요.')
  if (!confirm('자료를 저장하시겠습니까?')) return

  try {
    for (const item of selectedData) {
      const actkind = item.PROCGBN === 'A' ? 'A0' : 'U0'
      await api.post('/api/hpio/HPIO_200U_STR', {
        ACTKIND: actkind, CMPYCD: authStore.CMPYCD, USERID: authStore.USERID,
        LINECD: searchData.LINECD, LOTYMD: searchData.LOTYMD, LOTNO: item.LOTNO,
        ITEMCD: item.ITEMCD, ITSIZE: item.ITSIZE, UNIT: item.UNIT, LOTSIZE: item.LOTSIZE,
        STYMD: item.STYMD?.replace(/-/g, ''), TOYMD: item.TOYMD?.replace(/-/g, ''),
        BIGO: item.BIGO, USEYN: item.USEYN, ORDYM: searchData.ORDYM, ORDNO: searchData.ORDNO
      })
    }
    vAlert('정상적으로 처리되었습니다.'); fetchList()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const initialize = () => { resetForm(searchData); Object.assign(searchData, { LINECD: '', LOTYMD: initYMD, ORDYM: initYM, ORDNO: '' }); grid?.clearData(); itemCount.value = 0 }

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell?: any) {
  let config: any = {}
  if (type === 'ORDER') {
    config = { title: '주문번호 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ORDNO', data: { GUBUN: 'ORDER', CMPYCD: authStore.CMPYCD }, columns: [{ title: '주문연월', field: 'ORDYM', width: 100 }, { title: '순번', field: 'ORDNO', width: 80 }, { title: '거래처', field: 'CUSTNM', width: 200 }], onConfirm: (data: any) => { searchData.ORDYM = data.ORDYM; searchData.ORDNO = data.ORDNO } }
  } else if (type === 'ITEM') {
    config = { title: '제품 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM', data: { GUBUN: 'I1', CMPYCD: authStore.CMPYCD, GBNCD: searchData.LINECD }, columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '제품명', field: 'ITEMNM', width: 250 }, { title: '규격', field: 'ITSIZE', width: 120 }], onConfirm: (data: any) => { cell.getRow().update({ ITEMCD: data.ITEMCD, ITEMNM: data.ITEMNM, ITSIZE: data.ITSIZE, UNIT: data.UNIT }) } }
  }
  Object.assign(modalProps, config); modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
onMounted(() => { fetchLineOptions(); nextTick(() => initGrid()) })
</script>

<style scoped>
.hpio200u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
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

/* Tabulator 스타일 기준 */
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }
</style>
