<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-check2-all me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        주문관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">주문승인 (HSOD120U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="searchMaster">조회</button>
        <button class="btn-erp btn-save" @click="processApproval">
          {{ searchData.SELGBN === 'N' ? '승인처리' : '승인취소' }}
        </button>
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
                <th class="required">주문일자</th>
                <td style="width: 350px;">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="uiIOYMDFR" type="date" class="form-control form-control-sm" />
                    <span>~</span>
                    <input v-model="uiIOYMDTO" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th class="required">조회대상</th>
                <td style="width: 150px;">
                  <select v-model="searchData.SELGBN" class="form-select form-select-sm" @change="searchMaster">
                    <option value="N">미승인</option>
                    <option value="Y">승인</option>
                  </select>
                </td>
                <th>주문서종류</th>
                <td>
                  <select v-model="searchData.ORDKIND" class="form-select form-select-sm" style="width: 150px;" @change="searchMaster">
                    <option value="">전체</option>
                    <option v-for="opt in ordKindOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 마스터 그리드 (주문 목록) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="min-height: 250px;">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-list-check me-1"></i> 주문 내역</span>
          <div class="d-flex gap-2 align-items-center">
            <span class="small text-muted">전체선택</span>
            <input type="checkbox" v-model="allSelected" @change="toggleAllSelection" class="form-check-input" />
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="masterGridElement" style="height: 100%;"></div>
        </div>
      </div>

      <!-- Ⓒ 디테일 정보 영역 (주문 상세) -->
      <div class="card border shadow-sm overflow-hidden d-flex flex-column" style="height: 300px;">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-box-seam me-1"></i> 품목 상세 정보</span>
          <div v-if="selectedOrderInfo" class="small text-muted">
            <span class="me-3">배송처: {{ selectedOrderInfo.ADDRESS }}</span>
            <span>특기사항: {{ selectedOrderInfo.REMARK }}</span>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="detailGridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">선택 주문: <span class="fw-bold text-info">{{ selectedCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="fs-6 fw-light opacity-75">상세 합계: <span class="fw-bold text-white ms-2">{{ formatNumber(detailSum) }}</span></span>
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
  IOYMDFR: initFRYMD,
  IOYMDTO: initYMD,
  SELGBN: 'N',
  ORDKIND: ''
})

const uiIOYMDFR = computed({ get: () => formatDateString(searchData.IOYMDFR, '-'), set: (v) => searchData.IOYMDFR = v.replace(/-/g, '') })
const uiIOYMDTO = computed({ get: () => formatDateString(searchData.IOYMDTO, '-'), set: (v) => searchData.IOYMDTO = v.replace(/-/g, '') })

const masterGridElement = ref<HTMLElement | null>(null)
const detailGridElement = ref<HTMLElement | null>(null)
let masterGrid: Tabulator | null = null
let detailGrid: Tabulator | null = null

const selectedOrderInfo = ref<any>(null)
const activeItemCount = ref(0)
const selectedCount = ref(0)
const detailSum = ref(0)
const allSelected = ref(false)
const ordKindOptions = ref<any[]>([])

// 2. 그리드 초기화
const initGrids = () => {
  if (masterGridElement.value) {
    masterGrid = new Tabulator(masterGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      columnDefaults: { headerSort: false },
      columns: [
        {
          title: "선택", field: "PROCYN", width: 50, hozAlign: "center",
          formatter: "tickCross", editor: true,
          cellClick: (e, cell) => {
              const data = cell.getData();
              if (data.OUTYN === 'Y') {
                  vAlertError("출고처리된 주문서입니다. 선택할 수 없습니다.");
                  cell.setValue(false);
              }
          },
          cellEdited: () => updateSelectedCount()
        },
        { title: "주문번호", field: "ORD_FULL", width: 120, hozAlign: "center", cssClass: "fw-bold text-primary cursor-pointer" },
        { title: "주문일자", field: "ORDYMD_FMT", width: 100, hozAlign: "center" },
        { title: "주문구분", field: "ORDKINDNM", width: 100, hozAlign: "center" },
        { title: "거래처", field: "CUSTNM", minWidth: 200 },
        { title: "출고일자", field: "OUTYMD_FMT", width: 120, editor: "date", cssClass: "bg-light-yellow" },
        { title: "주문금액", field: "ORDAMT", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        {
            title: "영업부서", field: "DEPTNM", width: 150,
            formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>",
            cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) openHelp('DEPT', cell.getRow()) }
        },
        {
            title: "영업담당", field: "OEMPNM", width: 120,
            formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>",
            cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) openHelp('EMP', cell.getRow()) }
        }
      ]
    })

    masterGrid.on("rowClick", (e, row) => {
        const data = row.getData()
        fetchDetails(data)
    })
  }

  if (detailGridElement.value) {
    detailGrid = new Tabulator(detailGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "주문을 선택하세요.",
      columnDefaults: { headerSort: false },
      columns: [
        { title: "No", field: "OROWNO", width: 50, hozAlign: "center" },
        { title: "품명", field: "ITEMNM", minWidth: 200 },
        { title: "규격", field: "ITSIZE", width: 150 },
        { title: "단위", field: "UNIT", width: 60, hozAlign: "center" },
        { title: "수량", field: "ORDQTY", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 2 } },
        { title: "공급가", field: "ORDAMT", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "부가세", field: "ORDVAT", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "합계", field: "SUMAMT", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" }
      ]
    })
  }
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '220' } })
    ordKindOptions.value = res.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
  } catch (e) { console.error('옵션 로드 실패') }
}

async function searchMaster() {
  if (!searchData.IOYMDFR || !searchData.IOYMDTO) return vAlertError('조회 기간을 입력하세요.')
  try {
    const res = await api.post('/api/hsod/HSOD_120U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      ORDKIND: searchData.ORDKIND,
      SELGBN: searchData.SELGBN,
      IOYMDFR: searchData.IOYMDFR,
      IOYMDTO: searchData.IOYMDTO
    })
    if (masterGrid) {
      const mappedData = res.data.map((i: any) => ({
        ...i,
        PROCYN: searchData.SELGBN === 'N' && i.OUTYN !== 'Y',
        ORD_FULL: `${i.ORDYM}-${i.ORDNO}`,
        ORDYMD_FMT: formatDateString(i.ORDYMD, '-'),
        OUTYMD_FMT: formatDateString(i.OUTYMD, '-')
      }))
      masterGrid.setData(mappedData)
      allSelected.value = searchData.SELGBN === 'N'
      updateSelectedCount()
      detailGrid?.clearData()
      selectedOrderInfo.value = null
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetails(row: any) {
  try {
    const res = await api.post('/api/hsod/HSOD_120U_STR', {
      ACTKIND: 'S1',
      CMPYCD: authStore.CMPYCD,
      ORDYM: row.ORDYM,
      ORDNO: row.ORDNO
    })
    selectedOrderInfo.value = {
        ADDRESS: row.ADDRESS || '지정된 배송처 없음',
        REMARK: row.REMARK || '특기사항 없음'
    }
    if (detailGrid) {
      const mapped = res.data.map((i: any) => ({
          ...i,
          SUMAMT: Number(i.ORDAMT || 0) + Number(i.ORDVAT || 0)
      }))
      detailGrid.setData(mapped)
      detailSum.value = mapped.reduce((acc: number, cur: any) => acc + cur.SUMAMT, 0)
    }
  } catch (e) { vAlertError('상세 조회 실패') }
}

async function processApproval() {
  const selectedRows = masterGrid?.getData().filter((i: any) => i.PROCYN)
  if (!selectedRows || selectedRows.length === 0) return vAlertError('처리할 대상을 선택하세요.')

  // 승인 시 필수 항목 체크
  if (searchData.SELGBN === 'N') {
      for (const row of selectedRows) {
          if (!row.OUTYMD_FMT) return vAlertError(`[${row.ORD_FULL}] 출고일자를 입력하세요.`)
          if (!row.DEPTCD) return vAlertError(`[${row.ORD_FULL}] 영업부서를 선택하세요.`)
          if (!row.ORDEMP) return vAlertError(`[${row.ORD_FULL}] 영업담당자를 선택하세요.`)
      }
  }

  const confirmMsg = searchData.SELGBN === 'N' ? '선택한 주문을 승인처리 하시겠습니까?' : '선택한 주문의 승인을 취소하시겠습니까?'
  if (!confirm(confirmMsg)) return

  try {
    for (const row of selectedRows) {
      await api.post('/api/hsod/HSOD_120U_STR', {
        ACTKIND: 'U0',
        CMPYCD: authStore.CMPYCD,
        ORDKIND: searchData.ORDKIND,
        SELGBN: searchData.SELGBN,
        ORDYM: row.ORDYM,
        ORDNO: row.ORDNO,
        OUTYMD: row.OUTYMD_FMT.replace(/-/g, ''),
        DEPTCD: row.DEPTCD,
        WHCD: row.WHCD,
        ORDEMP: row.ORDEMP,
        USERID: authStore.USERID
      })
    }
    vAlert('정상적으로 처리되었습니다.')
    searchMaster()
  } catch (e) { vAlertError('처리 중 오류 발생') }
}

const updateSelectedCount = () => {
    if (!masterGrid) return
    selectedCount.value = masterGrid.getData().filter((i: any) => i.PROCYN).length
}

const toggleAllSelection = () => {
  if (!masterGrid) return
  const data = masterGrid.getData()
  masterGrid.updateData(data.map(i => ({ ...i, PROCYN: allSelected.value && i.OUTYN !== 'Y' })))
  updateSelectedCount()
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { IOYMDFR: initFRYMD, IOYMDTO: initYMD, SELGBN: 'N', ORDKIND: '' })
  masterGrid?.clearData()
  detailGrid?.clearData()
  selectedOrderInfo.value = null
  selectedCount.value = 0
  detailSum.value = 0
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string, row: any) {
  if (type === 'DEPT') {
    Object.assign(modalProps, {
      title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM',
      data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }],
      onConfirm: (data: any) => { row.update({ DEPTCD: data.DEPTCD, DEPTNM: data.DEPTNM }) }
    })
  } else if (type === 'EMP') {
    Object.assign(modalProps, {
      title: '사원 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'USERNM',
      data: { GUBUN: 'U1', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '코드', field: 'USERID', width: 80 }, { title: '사원명', field: 'USERNM', width: 150 }],
      onConfirm: (data: any) => { row.update({ ORDEMP: data.USERID, OEMPNM: data.USERNM }) }
    })
  }
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => initGrids())
})
</script>
