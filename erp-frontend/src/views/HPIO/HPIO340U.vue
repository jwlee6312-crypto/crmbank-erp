<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio340u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-right me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">외주가공 자재출고 (HPIO340U)</span>
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
        <button v-if="masterData.IONO !== '0000'" class="btn-erp btn-delete" @click="deleteData">
          <i class="bi bi-trash"></i> 삭제
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
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
                <th class="required">품의번호</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="uiPUMYM" type="month" class="form-control" />
                    <input v-model="masterData.PUMNO" type="text" class="form-control text-center" style="max-width: 60px;" @keyup.enter="openHelp('PUM')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('PUM')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">생산라인</th>
                <td>
                  <select v-model="masterData.LINECD" class="form-select form-select-sm" style="width: 180px;" @change="onLineChange">
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
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
                <th class="required">거 래 처</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control" @keyup.enter="openHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">생산공정</th>
                <td>
                  <select v-model="masterData.PROGCD" class="form-select form-select-sm" style="width: 180px;">
                    <option v-for="opt in progOptions" :key="opt.PROGCD" :value="opt.PROGCD">
                      [{{ opt.PROGCD }}] {{ opt.PROGNM }}
                    </option>
                  </select>
                </td>
                <th class="required">출고창고</th>
                <td>
                  <select v-model="masterData.WHCD" class="form-select form-select-sm" style="width: 180px;">
                    <option v-for="opt in whOptions" :key="opt.WHCD" :value="opt.WHCD">{{ opt.WHNM }}</option>
                  </select>
                </td>
                <th class="required">입고창고</th>
                <td>
                  <select v-model="masterData.IWHCD" class="form-select form-select-sm" style="width: 180px;">
                    <option v-for="opt in whOptions" :key="opt.WHCD" :value="opt.WHCD">{{ opt.WHNM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="required">생산제품</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="masterData.HITEMCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="masterData.HITEMNM" type="text" class="form-control" placeholder="제품 선택" @keyup.enter="openHelp('HITEM')" />
                    <input v-model="masterData.HITSIZE" type="text" class="form-control bg-light" style="max-width: 100px;" readonly />
                    <input v-model="masterData.HUNIT" type="text" class="form-control bg-light text-center" style="max-width: 50px;" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('HITEM')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>가공단가</th>
                <td>
                  <input v-model="masterData.PRICE" type="number" class="form-control form-control-sm text-end" style="width: 200px;" />
                </td>
                <th class="required">생 산 량</th>
                <td>
                  <input v-model="masterData.PROQTY" type="number" class="form-control form-control-sm text-end" style="width: 180px;" @change="onProQtyChange" />
                </td>
                <th class="required">입고일자</th>
                <td>
                  <input v-model="uiPROYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 투입 자재 그리드 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-task me-1 text-primary"></i> 투입 자재 내역</span>
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
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`
const initYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`

// 1. 상태 관리
const masterData = reactive({
  PUMYM: initYM, PUMNO: '',
  LINECD: '010', LINENM: '',
  IOYM: initYM, IONO: '0000',
  IOYMD: initYMD,
  CUSTCD: '', CUSTNM: '',
  PROGCD: '', PROGNM: '',
  WHCD: '300',
  IWHCD: '100',
  HITEMCD: '', HITEMNM: '', HITSIZE: '', HUNIT: '', HASTKIND: '',
  PRICE: 0, PROQTY: 0, PROYMD: initYMD, REMARK: '',
  INYM: '', INNO: ''
})

const lineOptions = ref<any[]>([])
const progOptions = ref<any[]>([])
const whOptions = ref<any[]>([])
const closingInfo = reactive({ CLSYMD: '', SCLSYM: '', PCLSYM: '' })

const uiPUMYM = computed({ get: () => `${masterData.PUMYM.substring(0, 4)}-${masterData.PUMYM.substring(4, 6)}`, set: (v) => { if (v) masterData.PUMYM = v.replace(/-/g, '') } })
const uiIOYM = computed({ get: () => `${masterData.IOYM.substring(0, 4)}-${masterData.IOYM.substring(4, 6)}`, set: (v) => { if (v) masterData.IOYM = v.replace(/-/g, '') } })
const uiIOYMD = computed({ get: () => formatDateString(masterData.IOYMD, '-'), set: (v) => { if (v) masterData.IOYMD = v.replace(/-/g, '') } })
const uiPROYMD = computed({ get: () => formatDateString(masterData.PROYMD, '-'), set: (v) => { if (v) masterData.PROYMD = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 기초 코드 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data.map((i: any) => ({ LINECD: i.CODE, LINENM: i.CDNM }))

    // 강제로 초기값을 적용하고 공정을 가져오도록 수정
    if (lineOptions.value.length > 0) {
       const initialLine = masterData.LINECD || lineOptions.value[0].LINECD;
       masterData.LINECD = initialLine;
       await fetchProgOptions(initialLine);
    }
  } catch (e) {}
}

const fetchProgOptions = async (lineCd: string) => {
  if (!lineCd) { progOptions.value = []; return; }
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'G3', CMPYCD: authStore.CMPYCD, GBNCD: lineCd } })
    progOptions.value = res.data.map((i: any) => ({ PROGCD: i.CODE, PROGNM: i.CDNM }))

    if (progOptions.value.length > 0 && !masterData.PROGCD) {
       masterData.PROGCD = progOptions.value[0].PROGCD
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
    masterData.PROGCD = ''
    fetchProgOptions(masterData.LINECD)
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns", height: "100%", selectable: true,
      columns: [
        { title: "상태", field: "UPKIND", width: 60, hozAlign: "center", vertAlign: "middle", cssClass: "text-danger fw-bold" },
        { title: "자재코드", field: "ITEMCD", width: 120 },
        {
          title: "자재명", field: "ITEMNM", minWidth: 250,
          cellClick: (e, cell) => openHelp('GRID_ITEM', cell)
        },
        { title: "규격", field: "ITSIZE", width: 150 },
        { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
        { title: "출고수량", field: "IOQTY", width: 120, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-light-yellow fw-bold" },
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
const fetchMaster = async () => {
  if (!masterData.LINECD) return vAlertError('생산라인을 선택하세요.')
  try {
    const res = await api.post('/api/hpio/HPIO_340U_STR', {
      ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOGBN: '200',
      IOYM: masterData.IOYM, IONO: masterData.IONO, LINECD: masterData.LINECD
    })
    if (res.data && res.data.length > 0) {
      Object.assign(masterData, res.data[0])
      fetchProgOptions(masterData.LINECD)
      fetchDetails()
      vAlert('조회되었습니다.')
    } else {
      vAlert('조회된 자료가 없습니다.')
    }
  } catch (e) { vAlertError('조회 실패') }
}

const fetchDetails = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_341U_STR', {
      ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOGBN: '200',
      IOYM: masterData.IOYM, IONO: masterData.IONO
    })
    grid?.setData(res.data)
    itemCount.value = res.data.length
  } catch (e) {}
}

const onProQtyChange = () => {
  if (masterData.PUMNO && masterData.PROQTY > 0) {
    fetchBomRequirement()
  }
}

const fetchBomRequirement = async () => {
    try {
        const res = await api.post('/api/hpio/HPIO_341U_STR', {
            ACTKIND: 'B', CMPYCD: authStore.CMPYCD, IOGBN: '200',
            IOYM: masterData.IOYM, IONO: masterData.IONO,
            PUMYM: masterData.PUMYM, PUMNO: masterData.PUMNO,
            HITEMCD: masterData.HITEMCD, PROQTY: masterData.PROQTY
        })
        const mapped = res.data.map((item: any) => ({ ...item, UPKIND: 'A' }))
        grid?.setData(mapped)
        itemCount.value = mapped.length
    } catch (e) {}
}

const saveData = async () => {
  if (masterData.IOYMD <= closingInfo.CLSYMD) return vAlertError('회계 마감된 일자입니다.')
  if (masterData.IOYMD.substring(0, 6) <= closingInfo.SCLSYM) return vAlertError('영업 마감된 월입니다.')

  const details = grid?.getData() || []
  if (details.length === 0) return vAlertError('출고할 자재 항목이 없습니다.')

  if (!confirm('저장하시겠습니까?')) return

  try {
    const actkind = masterData.IONO === '0000' ? 'A' : 'U'
    const resM = await api.post('/api/hpio/HPIO_340U_STR', {
      ...masterData, ACTKIND: actkind, CMPYCD: authStore.CMPYCD, USERID: authStore.USERID
    })

    const newIono = resM.data[0].IONO
    const newInno = resM.data[0].INNO

    for (const item of details) {
        if (!item.ITEMCD) continue
        await api.post('/api/hpio/HPIO_341U_STR', {
            ...item, ACTKIND: item.UPKIND || 'U', CMPYCD: authStore.CMPYCD,
            IOGBN: '200', IOYM: masterData.IOYM, IONO: newIono, INNO: newInno,
            LINECD: masterData.LINECD, PROGCD: masterData.PROGCD, USERID: authStore.USERID
        })
    }
    vAlert('정상적으로 저장되었습니다.')
    masterData.IONO = newIono
    fetchMaster()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const deleteData = async () => {
    if (!confirm('현재 출고 내역 전체를 삭제하시겠습니까?')) return
    try {
        await api.post('/api/hpio/HPIO_340U_STR', { ...masterData, ACTKIND: 'D', CMPYCD: authStore.CMPYCD })
        vAlert('삭제되었습니다.')
        initialize()
    } catch (e) { vAlertError('삭제 실패') }
}

const addRow = () => { grid?.addRow({ UPKIND: 'A', ITEMCD: '', ITEMNM: '', IOQTY: 0 }, true) }

const initialize = () => {
  resetForm(masterData)
  Object.assign(masterData, { PUMYM: initYM, PUMNO: '', IOYM: initYM, IONO: '0000', IOYMD: initYMD, PROYMD: initYMD, LINECD: '010' })
  grid?.clearData(); itemCount.value = 0
  fetchLineOptions()
  fetchWhOptions()
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, cell?: any) {
  let config: any = {}
  if (type === 'PUM') {
    config = {
        title: '외주품의 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'PUMNO',
        data: { GUBUN: 'OUT_PUM', CMPYCD: authStore.CMPYCD },
        columns: [{ title: '품의월', field: 'PUMYM', width: 80 }, { title: '번호', field: 'PUMNO', width: 60 }, { title: '거래처', field: 'CUSTNM', width: 150 }, { title: '제품명', field: 'ITEMNM', width: 200 }],
        onConfirm: (data: any) => {
            masterData.PUMYM = data.PUMYM; masterData.PUMNO = data.PUMNO;
            masterData.CUSTCD = data.CUSTCD; masterData.CUSTNM = data.CUSTNM;
            masterData.HITEMCD = data.ITEMCD; masterData.HITEMNM = data.ITEMNM;
            masterData.PRICE = data.PRICE; fetchBomRequirement();
        }
    }
  } else if (type === 'CUST') {
    config = { title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: '010', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 100 }, { title: '거래처명', field: 'CDNM', width: 200 }], onConfirm: (data: any) => { masterData.CUSTCD = data.CODE; masterData.CUSTNM = data.CDNM } }
  } else if (type === 'HITEM') {
    config = { title: '제품 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM', data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '제품명', field: 'ITEMNM', width: 250 }], onConfirm: (data: any) => { masterData.HITEMCD = data.ITEMCD; masterData.HITEMNM = data.ITEMNM; masterData.HITSIZE = data.ITSIZE; masterData.HUNIT = data.UNIT } }
  } else if (type === 'GRID_ITEM') {
    config = { title: '자재 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM', data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '자재명', field: 'ITEMNM', width: 250 }], onConfirm: (data: any) => { cell.getRow().update({ ITEMCD: data.ITEMCD, ITEMNM: data.ITEMNM, ITSIZE: data.ITSIZE, UNIT: data.UNIT }) } }
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
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hpio340u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }
.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-save { background-color: #2ec4b6 !important; color: #fff !important; }
.btn-delete { background-color: #ef4444 !important; color: #fff !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 90px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 8px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }
.bg-light-yellow { background-color: #fffde7 !important; }
</style>
