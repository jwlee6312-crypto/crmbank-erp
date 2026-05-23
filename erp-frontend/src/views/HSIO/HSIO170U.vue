<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio170u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-medical me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매입할인전표 발행 (HSIO170U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="saveData">전표발행</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 및 발행 설정 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">정산연월</th>
                <td>
                  <input v-model="uiJSANYM" type="month" class="form-control form-control-sm" style="width: 150px;" @change="fetchList" />
                </td>
                <th class="required">전표일자</th>
                <td>
                  <input v-model="uiSLIPYMD" type="date" class="form-control form-control-sm" style="width: 150px;" />
                </td>
                <th class="required">발행부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 250px;">
                    <input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="formData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
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
          <span class="fw-bold small text-dark"><i class="bi bi-list-check me-1"></i> 매입할인 내역</span>
          <div class="small text-muted">발행할 항목을 선택한 후 [전표발행] 버튼을 클릭하세요.</div>
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
        <div class="small">매입할인액 합계: <span class="fw-bold text-warning" style="font-size: 16px;">{{ formatNumber(totalHalAmt) }}</span> 원</div>
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

// 1. 상태 관리
const searchData = reactive({ JSANYM: initYM })
const formData = reactive({
  SLIPYMD: initYMD,
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  SLIPNO: ''
})
const closingInfo = reactive({ CLSYMD: '', SCLSYM: '' })
const autoSlipInfo = ref('N')

const uiJSANYM = computed({
  get: () => searchData.JSANYM ? `${searchData.JSANYM.substring(0, 4)}-${searchData.JSANYM.substring(4, 6)}` : '',
  set: (v) => searchData.JSANYM = v.replace(/-/g, '')
})
const uiSLIPYMD = computed({
  get: () => formatDateString(formData.SLIPYMD, '-'),
  set: (v) => formData.SLIPYMD = v.replace(/-/g, '')
})

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
      selectable: true,
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 80, hozAlign: "center", headerSort: false },
        { title: "거래처", field: "CUSTNM", minWidth: 100, widthGrow: 1 },
        { title: "매입부서", field: "DEPTNM", width: 300 },
        { title: "매입할인액", field: "HALAMT", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold text-primary" },
      ],
    })

    grid.on("rowSelectionChanged", () => {
      const selectedData = grid?.getSelectedData() || []
      selectedCount.value = selectedData.length
      totalHalAmt.value = selectedData.reduce((acc, row) => acc + Number(row.HALAMT || 0), 0)
    })
  }
}

// 3. 비즈니스 로직
const fetchList = async () => {
  if (!searchData.JSANYM) return vAlertError('정산연월을 선택하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_170U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      JSANYM: searchData.JSANYM
    })
    grid?.setData(res.data)
    vAlert('조회되었습니다.')
  } catch (e) {
    vAlertError('조회 중 오류가 발생했습니다.')
  }
}

const saveData = async () => {
  const selectedData = grid?.getSelectedData() || []
  if (selectedData.length === 0) return vAlertError('전표발행 대상을 선택하시기 바랍니다.')

  // 마감 체크
  if (formData.SLIPYMD <= closingInfo.CLSYMD) {
    return vAlertError("회계정보가 마감이 되었습니다. 해당 전표일자로 작업할 수 없습니다.")
  }
  if (formData.SLIPYMD.substring(0, 6) <= closingInfo.SCLSYM) {
    return vAlertError("영업정보가 마감이 되었습니다. 해당 전표일자로 작업할 수 없습니다.")
  }

  if (!confirm("전표를 발행 하시겠습니까?")) return

  try {
    // 1. 전표 마스터 생성 (HASL_010U_STR)
    const acctYmd = autoSlipInfo.value === 'Y' ? formData.SLIPYMD : ''
    const business = `${searchData.JSANYM.substring(0, 4)}년 ${searchData.JSANYM.substring(4, 6)}월 매입할인 건`

    const masterRes = await api.post('/api/hasl/HASL_010U_STR', {
      ACTKIND: 'A',
      CMPYCD: authStore.CMPYCD,
      SLIPYMD: formData.SLIPYMD,
      SLIPNO: '',
      ACCTYMD: acctYmd,
      DEPTCD: formData.DEPTCD,
      EMPNM: authStore.USERNM,
      SLIPKIND: '030', // 매입할인전표
      BUSINESS: business,
      USERID: authStore.USERID
    })

    const slipNo = masterRes.data?.[0]?.SLIPNO || masterRes.data?.[0]?.RTN_VAL
    if (!slipNo) throw new Error('전표번호 생성 실패')

    // 2. 디테일 업데이트 (HSIO_170U_STR 'U0')
    for (const item of selectedData) {
      const res = await api.post('/api/hsio/HSIO_170U_STR', {
        ACTKIND: 'U0',
        CMPYCD: authStore.CMPYCD,
        JSANYM: searchData.JSANYM,
        CUSTCD: item.CUSTCD,
        HALAMT: item.HALAMT,
        DEPTCD: formData.DEPTCD,
        SLIPYMD: formData.SLIPYMD,
        SLIPNO: slipNo,
        USERID: authStore.USERID
      })
      if (res.data?.[0]?.RTN_CD === 'Y' || res.data?.[0]?.ERRYN === 'Y') {
         vAlertError(res.data[0].RTN_MSG || '처리 중 오류 발생')
         return
      }
    }

    // 3. 자동 전표 확정 (HASL_020U_STR)
    if (autoSlipInfo.value === 'Y') {
      await api.post('/api/hasl/HASL_020U_STR', {
        ACTKIND: 'A0',
        CMPYCD: authStore.CMPYCD,
        SLIPYMD: formData.SLIPYMD,
        ACCTYMD: acctYmd,
        SLIPNO: slipNo,
        DEPTCD: formData.DEPTCD,
        SLIPKIND: '030',
        SLIPYN: 'N',
        COFMYN: 'Y',
        USERNM: authStore.USERNM,
        USERID: authStore.USERID
      })
    }

    vAlert('정상적으로 발행되었습니다.')

    // 4. 인쇄 팝업 호출
    const printUrl = `../HASL/HASL_SLIP_PRINT.asp?SLIPGU=010&SLIPYMD=${formData.SLIPYMD}&SLIPNO=${slipNo}&DEPTCD=${formData.DEPTCD}`
    window.open(printUrl, '전표인쇄', 'left=10,top=10,width=700,height=650,scrollbars=yes')

    fetchList()
  } catch (e) {
    vAlertError('전표 발행 처리 실패')
  }
}

const initialize = () => {
  searchData.JSANYM = initYM
  formData.SLIPYMD = initYMD
  grid?.clearData()
  totalHalAmt.value = 0
  selectedCount.value = 0
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
      formData.DEPTCD = data.DEPTCD
      formData.DEPTNM = data.DEPTNM
    }
  })
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  // 마감 정보 조회
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      closingInfo.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(r.data[0])[1]).trim()
    }
  })
  // 자동전표 설정 조회
  api.post('/api/ha00/HA00_010S_STR', { CMPYCD: authStore.CMPYCD, GUBUN: 'P1' }).then(r => {
    if (r.data?.length) autoSlipInfo.value = r.data[0].SLIPYN || 'N'
  })

  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsio170u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

/* 🚀 입력 필드 글자 크기 및 높이 최적화 (표준) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12px; padding: 6px 10px !important; color: #495057;
  white-space: nowrap;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }

:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>

