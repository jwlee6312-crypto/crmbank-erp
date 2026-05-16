<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio400u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-in-down me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">제품입고(자가생산) (HPIO400U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchProcesses">조회</button>
        <button class="btn-erp btn-save" @click="saveReceipt" :disabled="!selectedProg.PROGCD">입고저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden">
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
                <th class="required">생산일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiPROYMDFR" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiPROYMDTO" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th class="required">입고창고</th>
                <td>
                  <select v-model="searchData.WHCD" class="form-select form-select-sm" style="width: 180px;">
                    <option v-for="opt in whOptions" :key="opt.WHCD" :value="opt.WHCD">{{ opt.WHNM }}</option>
                  </select>
                </td>
                <th class="required">입고일자</th>
                <td>
                  <input v-model="uiINYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 작업 영역 -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 공정 목록 -->
        <div class="card border shadow-sm d-flex flex-column" style="width: 200px;">
          <div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark text-center">
            공정
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-auto">
            <ul class="list-group list-group-flush process-list">
              <li v-for="prog in processList" :key="prog.PROGCD"
                  class="list-group-item list-group-item-action py-2 text-center small"
                  :class="{ active: selectedProg.PROGCD === prog.PROGCD }"
                  @click="onProcessSelect(prog)">
                {{ prog.PROGNM }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 우측: 입고 대상 목록 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark">
                <i class="bi bi-list-check me-1"></i> 입고 대상 내역
                <span v-if="selectedProg.PROGNM" class="text-primary ms-2">[{{ selectedProg.PROGNM }}]</span>
              </span>
              <div class="small text-muted">※ 생산 완료된 항목을 선택하여 입고 처리하세요.</div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
              <div ref="gridElement" style="height: 100%;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">조회건수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 미입고량 범위 내에서 입고수량을 수정할 수 있습니다.
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
const initFRYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  LINECD: '010',
  PROYMDFR: initFRYMD,
  PROYMDTO: initYMD,
  WHCD: '200',
  INYMD: initYMD
})

const lineOptions = ref<any[]>([])
const processList = ref<any[]>([])
const whOptions = ref<any[]>([])
const selectedProg = reactive({ PROGCD: '', PROGNM: '' })
const closingInfo = reactive({ CLSYMD: '', SCLSYM: '', PCLSYM: '' })

const uiPROYMDFR = computed({ get: () => formatDateString(searchData.PROYMDFR, '-'), set: (v) => { if(v) searchData.PROYMDFR = v.replace(/-/g, '') } })
const uiPROYMDTO = computed({ get: () => formatDateString(searchData.PROYMDTO, '-'), set: (v) => { if(v) searchData.PROYMDTO = v.replace(/-/g, '') } })
const uiINYMD = computed({ get: () => formatDateString(searchData.INYMD, '-'), set: (v) => { if(v) searchData.INYMD = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 라인 로드
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data.map((i: any) => ({ LINECD: i.CODE, LINENM: i.CDNM }))
  } catch (e) {}
}

// 🏭 창고 로드 (HS00_000S_STR 'W0' 적용)
const fetchWhOptions = async () => {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })
    whOptions.value = res.data.map((i: any) => ({ WHCD: i.WHCD, WHNM: i.WHNM }))
  } catch (e) {}
}

const fetchProcesses = async () => {
  if (!searchData.LINECD) return
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'G2', CMPYCD: authStore.CMPYCD, LINECD: searchData.LINECD } })
    processList.value = res.data.map((i: any) => ({ PROGCD: i.CODE, PROGNM: i.CDNM }))
    grid?.clearData(); itemCount.value = 0; selectedProg.PROGCD = '';
  } catch (e) {}
}

const onLineChange = () => fetchProcesses()

const onProcessSelect = (prog: any) => {
  selectedProg.PROGCD = prog.PROGCD
  selectedProg.PROGNM = prog.PROGNM
  fetchGridData()
}

const fetchGridData = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_400U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      LINECD: searchData.LINECD, PROGCD: selectedProg.PROGCD,
      PROYMDFR: searchData.PROYMDFR, PROYMDTO: searchData.PROYMDTO
    })
    const mapped = res.data.map((item: any) => ({
      ...item,
      NINQTY: Number(item.PRDQTY || 0) - Number(item.INQTY || 0),
      WORK_INFO: item.PRODCD === '100' ? item.WORKNM : item.CUSTNM
    }))
    grid?.setData(mapped)
    itemCount.value = mapped.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns", height: "100%", selectable: true, headerHeight: 60,
      columns: [
          { title: "", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", vertAlign: "middle", headerSort: false },
          { title: "생산일자", field: "PROYMD", width: 120, hozAlign: "center", vertAlign: "middle", formatter: (c) => formatDateString(c.getValue(), '-') },
          { title: "품목명", field: "ITEMNM", minWidth: 200, vertAlign: "middle", cssClass: "fw-bold" },
          { title: "규격", field: "ITSIZE", width: 120, vertAlign: "middle" },
          { title: "단위", field: "UNIT", width: 70, hozAlign: "center", vertAlign: "middle" },
          { title: "생산량", field: "PRDQTY", width: 90, hozAlign: "right", vertAlign: "middle", formatter: "money", formatterParams: { precision: 0 } },
          { title: "미입고", field: "NINQTY", width: 90, hozAlign: "right", vertAlign: "middle", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-danger" },
          { title: "입고량", field: "INQTY", width: 90, hozAlign: "right", vertAlign: "middle", editor: "number", cssClass: "bg-light-yellow fw-bold" },
          { title: "비고", field: "WORK_INFO", width: 150, vertAlign: "middle" }
      ]
    })
  }
}

// 3. 저장 로직
const saveReceipt = async () => {
  const selected = grid?.getSelectedData() || []
  if (selected.length === 0) return vAlertError('입고 처리할 항목을 선택하세요.')

  const inYmd = searchData.INYMD.replace(/-/g, '')
  if (inYmd <= closingInfo.CLSYMD) return vAlertError('회계 마감된 일자입니다.')
  if (inYmd.substring(0,6) <= closingInfo.SCLSYM) return vAlertError('영업 마감된 월입니다.')

  if (!confirm('선택한 내역을 입고 처리하시겠습니까?')) return

  try {
    const masterRes = await api.post('/api/hsio/HSIO_060U_STR', {
      ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, IOGBN: '100',
      PROYMDFR: searchData.PROYMDFR, PROYMDTO: searchData.PROYMDTO,
      DEPTCD: '21000', WHCD: searchData.WHCD, JINYMD: inYmd,
      REMARK: `생산입고-${searchData.INYMD}`, CFMYN: 'Y', USERID: authStore.USERID
    })

    const ioYm = masterRes.data[0].IOYM; const ioNo = masterRes.data[0].IONO

    for (const item of selected) {
      const detailRes = await api.post('/api/hsio/HSIO_061U_STR', {
        ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, IOGBN: '100',
        IOYM: ioYm, IONO: ioNo, DEPTCD: '21000', WHCD: searchData.WHCD,
        JINYMD: inYmd, ITEMCD: item.ITEMCD, UNIT: item.UNIT, ITSIZE: item.ITSIZE,
        INQTY: item.INQTY, USERID: authStore.USERID, ORDYM: item.ORDYM, ORDNO: item.ORDNO
      })

      const ioRowNo = detailRes.data[0].IOROWNO

      await api.post('/api/hpio/HPIO_400U_STR', {
        ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, LINECD: searchData.LINECD, PROGCD: selectedProg.PROGCD,
        PROYMD: item.PROYMD, JINYMD: inYmd, ORDYMD: item.ORDYMD, DEPTCD: '21000', WHCD: searchData.WHCD,
        ITEMCD: item.ITEMCD, INQTY: item.INQTY, IOYM: ioYm, IONO: ioNo, IOROWNO: ioRowNo,
        ORDYM: item.ORDYM, ORDNO: item.ORDNO, PRODCD: item.PRODCD
      })
    }
    vAlert('정상적으로 입고 처리되었습니다.')
    fetchGridData()
  } catch (e) { vAlertError('입고 처리 중 오류 발생') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { LINECD: '010', PROYMDFR: initFRYMD, PROYMDTO: initYMD, WHCD: '200', INYMD: initYMD })
  fetchProcesses()
}

const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      closingInfo.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(r.data[0])[1]).trim()
      closingInfo.PCLSYM = String(Object.values(r.data[0])[2]).trim()
    }
  })
  fetchLineOptions(); fetchWhOptions(); fetchProcesses(); nextTick(() => initGrid())
})
</script>

<style scoped>
.hpio400u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #0056b3 !important; color: #fff !important; border: none !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; border: 1px solid #dee2e6; }
.erp-table-full th { width: 90px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 6px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
.process-list .list-group-item { cursor: pointer; border-left: 4px solid transparent; }
.process-list .list-group-item.active { background-color: #e7f1ff; color: #0056b3; border-left-color: #0056b3; font-weight: bold; }
:deep(.tabulator) { border: none; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; }
.bg-light-yellow { background-color: #fffde7 !important; }
</style>
