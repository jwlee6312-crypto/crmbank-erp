<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio180u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-x-fill me-2 text-danger" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매입할인전표 취소 (HSIO180U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-delete" @click="deleteData">전표취소</button>
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
                <th class="required">발행부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 250px;">
                    <input v-model="searchData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="searchData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">발행일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiIOYMDFR" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiIOYMDTO" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 전표 발행 내역</span>
          <div class="small text-muted">마감된 자료는 취소할 수 없습니다.</div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 합계 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="d-flex justify-content-between align-items-center w-100">
        <div class="small">선택건수: <span class="fw-bold text-info">{{ selectedCount }}</span> 건</div>
        <div class="small">할인금액 합계: <span class="fw-bold text-warning" style="font-size: 16px;">{{ formatNumber(totalHalAmt) }}</span> 원</div>
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
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  IOYMDFR: initFRYMD,
  IOYMDTO: initYMD
})

const closingInfo = reactive({ CLSYMD: '', SCLSYM: '' })
const autoSlipInfo = ref('N')

const uiIOYMDFR = computed({ get: () => formatDateString(searchData.IOYMDFR, '-'), set: (v) => searchData.IOYMDFR = v.replace(/-/g, '') })
const uiIOYMDTO = computed({ get: () => formatDateString(searchData.IOYMDTO, '-'), set: (v) => searchData.IOYMDTO = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const selectedCount = ref(0)
const totalHalAmt = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      selectable: (row) => {
        const data = row.getData()
        return checkCanCancel(data) === true
      },
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false },
        {
          title: "전표번호", field: "SLIPNO_DISP", width: 180, hozAlign: "center",
          formatter: (cell) => {
            const d = cell.getData()
            return `<a class="text-primary fw-bold text-decoration-none">${d.SLIPYMD}-${d.SLIPNO}</a>`
          },
          cellClick: (e, cell) => printSlip(cell.getData())
        },
        { title: "발행부서", field: "DEPTNM", width: 180 },
        { title: "거래처", field: "CUSTNM", width: 250 },
        { title: "할인금액", field: "HALAMT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" },
      ],
    })

    grid.on("rowSelectionChanged", () => {
      const selectedData = grid?.getSelectedData() || []
      selectedCount.value = selectedData.length
      totalHalAmt.value = selectedData.reduce((acc, row) => acc + Number(row.HALAMT || 0), 0)
    })
  }
}

// 마감 및 취소 가능 여부 체크
const checkCanCancel = (row: any) => {
  const halYm = String(row.HALYMD || '').substring(0, 6)
  if (halYm <= closingInfo.SCLSYM) return "영업정보 마감"
  if (row.HALYMD <= closingInfo.CLSYMD) return "회계정보 마감"
  if (row.CFMYN === 'Y' && autoSlipInfo.value === 'N') return "확정전표"
  return true
}

// 3. 비즈니스 로직
const fetchList = async () => {
  if (!searchData.DEPTCD) return vAlertError('발행부서를 선택하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_180U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      IOYMDFR: searchData.IOYMDFR,
      IOYMDTO: searchData.IOYMDTO,
      DEPTCD: searchData.DEPTCD
    })
    grid?.setData(res.data)
    vAlert('조회되었습니다.')
  } catch (e) {
    vAlertError('조회 실패')
  }
}

const deleteData = async () => {
  const selectedData = grid?.getSelectedData() || []
  if (selectedData.length === 0) return vAlertError('취소할 전표를 선택하세요.')

  if (!confirm('선택한 전표를 삭제(취소)하시겠습니까?')) return

  try {
    for (const item of selectedData) {
      await api.post('/api/hsio/HSIO_180U_STR', {
        ACTKIND: 'D0',
        CMPYCD: authStore.CMPYCD,
        USERID: authStore.USERID,
        IOYMDFR: searchData.IOYMDFR,
        IOYMDTO: searchData.IOYMDTO,
        UDEPTCD: item.DEPTCD,
        SLIPYMD: item.SLIPYMD,
        SLIPNO: item.SLIPNO,
        AUTOSLIP: autoSlipInfo.value
      })
    }
    vAlert('전표 취소가 완료되었습니다.')
    fetchList()
  } catch (e) {
    vAlertError('취소 처리 실패')
  }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, IOYMDFR: initFRYMD, IOYMDTO: initYMD })
  grid?.clearData()
  selectedCount.value = 0
  totalHalAmt.value = 0
}

const printSlip = (row: any) => {
  const url = `../HASL/HASL_SLIP_PRINT.asp?SLIPGU=010&SLIPYMD=${row.SLIPYMD}&SLIPNO=${row.SLIPNO}&DEPTCD=${row.DEPTCD}`
  window.open(url, '전표인쇄', 'left=10,top=10,width=700,height=650,scrollbars=yes')
}

// 4. 도움창
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  Object.assign(modalProps, {
    title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM',
    data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
    columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 200 }],
    onConfirm: (data: any) => {
      searchData.DEPTCD = data.DEPTCD
      searchData.DEPTNM = data.DEPTNM
    }
  })
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      closingInfo.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(r.data[0])[1]).trim()
    }
  })
  api.post('/api/ha00/HA00_010S_STR', { CMPYCD: authStore.CMPYCD, GUBUN: 'P1' }).then(r => {
    if (r.data?.length) autoSlipInfo.value = r.data[0].SLIPYN || 'N'
  })

  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsio180u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }

.erp-header { background-color: #ffffff !important; }

/* 💎 개별 파일의 스타일을 삭제하여 global.css 표준 디자인이 적용되도록 함 */
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }

erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  width: 1%; white-space: nowrap;
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12.5px; padding: 10px 15px !important; color: #495057;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 10px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
.bg-yellow { background-color: #fffde7 !important; }
</style>
