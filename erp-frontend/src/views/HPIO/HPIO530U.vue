<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio530u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-up-right me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">재공타계정출고 (HPIO530U)</span>
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
      <!-- 🅰️ 출고 및 주문 정보 -->
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
                <th class="required">출고부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
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
                <th class="required">출고창고</th>
                <td>
                  <select v-model="masterData.WHCD" class="form-select form-select-sm" style="width: 140px;">
                    <option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="required">주문번호</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="uiORDYM" type="month" class="form-control" />
                    <input v-model="masterData.ORDNO" type="text" class="form-control text-center" style="max-width: 60px;" @keyup.enter="openHelp('ORDER')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('ORDER')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>품목(모델)</th>
                <td>
                  <select v-model="masterData.MODELCD" class="form-select form-select-sm" style="width: 180px;">
                    <option value="0000000">전체</option>
                    <option v-for="m in modelOptions" :key="m.ITEMCD" :value="m.ITEMCD">{{ m.ITEMNM }}</option>
                  </select>
                </td>
                <th class="required">입고창고</th>
                <td>
                  <select v-model="masterData.IWHCD" class="form-select form-select-sm" style="width: 140px;">
                    <option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
                  </select>
                </td>
                <td colspan="2" class="bg-light">
                  <div class="d-flex gap-2 justify-content-center">
                    <button class="btn btn-xs btn-primary px-2" style="height: 26px;" @click="generateBom">
                      <i class="bi bi-diagram-3-fill me-1"></i> BOM전개
                    </button>
                    <button class="btn btn-xs btn-info text-white px-2" style="height: 26px;" @click="importData">
                      <i class="bi bi-download me-1"></i> 불러오기
                    </button>
                  </div>
                </td>
              </tr>
              <tr>
                <th>특기사항</th>
                <td colspan="7">
                  <input v-model="masterData.REMARK" type="text" class="form-control form-control-sm" placeholder="비고 입력" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 자재 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1 text-primary"></i> 타계정 출고 자재 내역</span>
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
        <div class="col-md-4 small">항목수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 주문번호별 BOM 전개나 불러오기 기능을 통해 출고 자재를 간편하게 구성할 수 있습니다.
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
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`
const initYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`

// 1. 상태 관리
const masterData = reactive({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  IOYM: initYM, IONO: '',
  IOYMD: initYMD,
  WHCD: '200',
  IWHCD: '100',
  ORDYM: initYM, ORDNO: '',
  MODELCD: '0000000',
  REMARK: ''
})

const whOptions = ref<any[]>([])
const modelOptions = ref<any[]>([])
const closingInfo = reactive({ CLSYMD: '', SCLSYM: '', PCLSYM: '' })

const uiIOYM = computed({ get: () => `${masterData.IOYM.substring(0, 4)}-${masterData.IOYM.substring(4, 6)}`, set: (v) => { if (v) masterData.IOYM = v.replace(/-/g, '') } })
const uiIOYMD = computed({ get: () => formatDateString(masterData.IOYMD, '-'), set: (v) => { if (v) masterData.IOYMD = v.replace(/-/g, '') } })
const uiORDYM = computed({ get: () => `${masterData.ORDYM.substring(0, 4)}-${masterData.ORDYM.substring(4, 6)}`, set: (v) => { if (v) masterData.ORDYM = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 주문번호 변경 시 모델 옵션 로드
watch(() => [masterData.ORDYM, masterData.ORDNO], async ([ym, no]) => {
  if (ym && no && String(no).length >= 3) {
    try {
        const res = await api.get('/api/prod/codes/ORDER_MODELS', { params: { CMPYCD: authStore.CMPYCD, ORDYM: ym, ORDNO: no } })
        modelOptions.value = res.data
    } catch (e) { modelOptions.value = [] }
  } else {
    modelOptions.value = []
  }
})

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns", height: "100%", selectable: true,
      columns: [
        { title: "상태", field: "UPKIND", width: 60, hozAlign: "center", vertAlign: "middle", cssClass: "text-danger fw-bold" },
        { title: "자재코드", field: "ITEMCD", width: 120, hozAlign: "center" },
        {
          title: "원(부)자재명", field: "ITEMNM", minWidth: 250,
          cellClick: (e, cell) => openHelp('GRID_ITEM', cell)
        },
        { title: "규격", field: "ITSIZE", width: 150 },
        { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
        { title: "재고수량", field: "JQTY", width: 100, hozAlign: "right", formatter: "money" },
        { title: "출고수량", field: "IOQTY", width: 120, hozAlign: "right", editor: "number", formatter: "money", cssClass: "bg-light-yellow fw-bold" },
        { title: "비고", field: "REMARK", minWidth: 150, editor: "input" },
        { title: "삭제", width: 60, hozAlign: "center", formatter: "buttonCross", cellClick: (e, cell) => cell.getRow().delete() }
      ]
    })
    grid.on("cellEdited", (cell) => {
        const row = cell.getRow()
        if (row.getData().UPKIND !== 'A') row.update({ UPKIND: 'U' })
    })
  }
}

// 3. 비즈니스 로직
const fetchWhOptions = async () => {
  try {
    const res = await api.get('/api/comm/codes/WH')
    whOptions.value = res.data
  } catch (e) {}
}

const fetchMaster = async () => {
  if (!masterData.IOYM || !masterData.IONO) return vAlertError('출고번호를 입력하세요.')
  try {
    const res = await api.post('/api/hpio/HPIO_530U_STR', {
      ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOGBN: '200',
      IOYM: masterData.IOYM, IONO: masterData.IONO
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
    const res = await api.post('/api/hpio/HPIO_501U_STR', { // Detail은 501U 공동사용
      ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOGBN: '200',
      IOYM: masterData.IOYM, IONO: masterData.IONO
    })
    grid?.setData(res.data)
    itemCount.value = res.data.length
  } catch (e) {}
}

const generateBom = async () => {
    if (!masterData.ORDYM || !masterData.ORDNO) return vAlertError('주문번호를 입력하세요.')
    if (!confirm('BOM 전개를 실행하시겠습니까?')) return
    try {
        await api.post('/api/hpio/HPIO_530U_STR', {
            ACTKIND: 'G', CMPYCD: authStore.CMPYCD,
            ORDYM: masterData.ORDYM, ORDNO: masterData.ORDNO
        })
        vAlert('BOM 전개가 완료되었습니다.')
        importData()
    } catch (e) { vAlertError('BOM 전개 실패') }
}

const importData = async () => {
    if (!masterData.ORDYM || !masterData.ORDNO) return vAlertError('주문번호를 입력하세요.')
    try {
        const res = await api.post('/api/hpio/HPIO_530S_STR', {
            CMPYCD: authStore.CMPYCD,
            ORDYM: masterData.ORDYM, ORDNO: masterData.ORDNO,
            MODELCD: masterData.MODELCD, WHCD: masterData.WHCD
        })
        const mapped = res.data.map((i: any) => ({ ...i, UPKIND: 'A', IOQTY: i.REQQTY }))
        grid?.setData(mapped)
        itemCount.value = mapped.length
        vAlert('데이터를 불러왔습니다.')
    } catch (e) { vAlertError('데이터 로드 실패') }
}

const saveData = async () => {
  const ioYmd = masterData.IOYMD.replace(/-/g, '')
  if (ioYmd.substring(0, 6) <= closingInfo.PCLSYM) return vAlertError('생산 마감된 월입니다.')
  if (ioYmd.substring(0, 6) <= closingInfo.SCLSYM) return vAlertError('영업 마감된 월입니다.')

  const details = grid?.getData() || []
  if (details.length === 0) return vAlertError('항목이 없습니다.')

  if (!confirm('저장하시겠습니까?')) return

  try {
    const actkind = !masterData.IONO ? 'A' : 'U'
    const resM = await api.post('/api/hpio/HPIO_500U_STR', { // Save Logic은 500U 공동사용
      ...masterData, ACTKIND: actkind, CMPYCD: authStore.CMPYCD, USERID: authStore.USERID,
      IOGBN: '200', PRODCD: '100', LINECD: '010', PROGCD: '888'
    })

    const newIono = resM.data[0].IONO
    const newInno = resM.data[0].INNO

    for (const item of details) {
        if (!item.ITEMCD) continue
        await api.post('/api/hpio/HPIO_501U_STR', {
            ...item, ACTKIND: item.UPKIND || 'U', CMPYCD: authStore.CMPYCD, USERID: authStore.USERID,
            IOGBN: '200', IOYM: masterData.IOYM, IONO: newIono, INNO: newInno,
            DEPTCD: masterData.DEPTCD, WHCD: masterData.WHCD, IWHCD: masterData.IWHCD, IOYMD: ioYmd,
            LINECD: '010', PROGCD: '888'
        })
    }
    vAlert('정상적으로 저장되었습니다.')
    masterData.IONO = newIono
    fetchMaster()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const deleteData = async () => {
    if (!confirm('삭제하시겠습니까?')) return
    try {
        await api.post('/api/hpio/HPIO_500U_STR', { ...masterData, ACTKIND: 'D', CMPYCD: authStore.CMPYCD })
        vAlert('삭제되었습니다.')
        initialize()
    } catch (e) { vAlertError('삭제 실패') }
}

const addRow = () => { grid?.addRow({ UPKIND: 'A', ITEMCD: '', ITEMNM: '', IOQTY: 0 }, true) }

const initialize = () => {
  resetForm(masterData)
  Object.assign(masterData, {
      DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
      IOYM: initYM, IONO: '', IOYMD: initYMD,
      WHCD: '200', IWHCD: '100',
      ORDYM: initYM, ORDNO: '', MODELCD: '0000000'
  })
  grid?.clearData(); itemCount.value = 0
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell?: any) {
  let config: any = {}
  if (type === 'DEPT') {
    config = { title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '부서명', field: 'CDNM', width: 150 }], onConfirm: (data: any) => { masterData.DEPTCD = data.CODE; masterData.DEPTNM = data.CDNM } }
  } else if (type === 'ORDER') {
    config = { title: '주문 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ORDNO', data: { GUBUN: 'ORDER', CMPYCD: authStore.CMPYCD }, columns: [{ title: '연월', field: 'ORDYM', width: 80 }, { title: '번호', field: 'ORDNO', width: 60 }, { title: '거래처', field: 'CUSTNM', width: 150 }], onConfirm: (data: any) => { masterData.ORDYM = data.ORDYM; masterData.ORDNO = data.ORDNO } }
  } else if (type === 'GRID_ITEM') {
    config = { title: '자재 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM', data: { GUBUN: 'I1', CMPYCD: authStore.CMPYCD, iogbn: 'I' }, columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '자재명', field: 'ITEMNM', width: 250 }], onConfirm: (data: any) => { cell.getRow().update({ ITEMCD: data.ITEMCD, ITEMNM: data.ITEMNM, ITSIZE: data.ITSIZE, UNIT: data.UNIT, QTYPNT: data.QTYPNT }) } }
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
      closingInfo.PCLSYM = String(Object.values(r.data[0])[2]).trim()
    }
  })
  fetchWhOptions()
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hpio530u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-save { background-color: #2ec4b6 !important; color: #fff !important; }
.btn-delete { background-color: #ef4444 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }

.bg-light-yellow { background-color: #fffde7 !important; }
</style>
