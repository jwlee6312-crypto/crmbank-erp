<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsod210u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-gift-fill me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">장려 및 재고보상금 관리 (HSOD210U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="saveData">저장</button>
        <button class="btn-erp btn-delete" @click="deleteData" :disabled="formData.ACTKIND !== 'U'">삭제</button>
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
                <th>구&nbsp;&nbsp;&nbsp;&nbsp;분</th>
                <td>
                  <select v-model="searchData.GBN" class="form-select form-select-sm" style="width: 150px;">
                    <option value="001">판매장려금</option>
                    <option value="002">재고보상금</option>
                  </select>
                </td>
                <th class="required">등록일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiFRYMD" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiTOYMD" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 입력 폼 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">구&nbsp;&nbsp;&nbsp;&nbsp;분</th>
                <td>
                  <select v-model="formData.GBN" class="form-select form-select-sm" style="width: 150px;">
                    <option value="001">판매장려금</option>
                    <option value="002">재고보상금</option>
                  </select>
                </td>
                <th class="required">거&nbsp;래&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 250px;">
                    <input v-model="formData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="formData.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="openHelp('FORM_CUST')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('FORM_CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>부&nbsp;&nbsp;&nbsp;&nbsp;서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 250px;">
                    <input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="formData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('FORM_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('FORM_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required">등록일자</th>
                <td>
                  <input v-model="uiIOYMD" type="date" class="form-control form-control-sm" style="width: 150px;" />
                </td>
                <th class="required">금&nbsp;&nbsp;&nbsp;&nbsp;액</th>
                <td>
                  <input v-model.number="formData.IOAMT" type="number" class="form-control form-control-sm text-end" style="width: 150px;" @input="calculateVat" />
                </td>
                <th>부&nbsp;가&nbsp;세</th>
                <td>
                  <input v-model.number="formData.IOVAT" type="number" class="form-control form-control-sm text-end" style="width: 150px;" />
                </td>
              </tr>
              <tr>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td colspan="5">
                  <input v-model="formData.REMARK" type="text" class="form-control form-control-sm" maxlength="100" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark">
          <i class="bi bi-list-check me-1"></i> 처리 내역
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">조회건수: <span class="fw-bold text-white">{{ itemCount }}</span> 건</div>
        <div class="col-md-9 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 목록에서 항목을 클릭하면 수정 모드로 전환됩니다.
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
  GBN: '001',
  FRYMD: initFRYMD,
  TOYMD: initYMD
})

const formData = reactive({
  ACTKIND: 'A',
  GBN: '001',
  CUSTCD: '', CUSTNM: '',
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  IOYMD: initYMD,
  IOAMT: 0,
  IOVAT: 0,
  REMARK: '',
  IOYM: '', IONO: '' // 수정용 키값
})

const uiFRYMD = computed({ get: () => formatDateString(searchData.FRYMD, '-'), set: (v) => searchData.FRYMD = v.replace(/-/g, '') })
const uiTOYMD = computed({ get: () => formatDateString(searchData.TOYMD, '-'), set: (v) => searchData.TOYMD = v.replace(/-/g, '') })
const uiIOYMD = computed({ get: () => formatDateString(formData.IOYMD, '-'), set: (v) => formData.IOYMD = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      columns: [
        { title: "구분", field: "GBN", width: 100, hozAlign: "center", formatter: (c) => c.getValue() === '001' ? '판매장려금' : '재고보상금' },
        { title: "거래처 상호", field: "CUSTNM", minwidth: 250 },
        { title: "등록일", field: "IOYMD", width: 150, hozAlign: "center", formatter: (c) => formatDateString(c.getValue(), '-') },
        { title: "금액", field: "IOAMT", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "부가세", field: "IOVAT", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "비고", field: "REMARK",width: 300, hozAlign: "left" },
      ],
    })

    grid.on("rowClick", (e, row) => {
      const data = row.getData()
      Object.assign(formData, data)
      formData.ACTKIND = 'U'
    })
  }
}

// 3. 비즈니스 로직
const calculateVat = () => {
  formData.IOVAT = Math.floor(Number(formData.IOAMT || 0) * 0.1)
}

const fetchList = async () => {
  try {
    const res = await api.post('/api/hsod/HSOD_210U_STR', {
      ACTKIND: 'S',
      CMPYCD: authStore.CMPYCD,
      GBN: searchData.GBN,
      FRYMD: searchData.FRYMD,
      TOYMD: searchData.TOYMD
    })
    grid?.setData(res.data)
    itemCount.value = res.data.length
    vAlert('조회되었습니다.')
  } catch (e) {
    vAlertError('조회 중 오류가 발생했습니다.')
  }
}

const saveData = async () => {
  if (!formData.CUSTCD) return vAlertError('거래처를 선택하세요.')
  if (!formData.IOAMT) return vAlertError('금액을 입력하세요.')

  if (!confirm('자료를 저장하시겠습니까?')) return

  try {
    const res = await api.post('/api/hsod/HSOD_210U_STR', {
      ...formData,
      CMPYCD: authStore.CMPYCD,
      USERID: authStore.USERID
    })

    if (res.data?.[0]?.ERRYN === 'Y' || (res.data?.[0]?.RTN_CD && res.data[0].RTN_CD !== '000000')) {
      vAlertError(res.data[0].RTN_MSG || '저장 중 오류 발생')
    } else {
      vAlert('저장되었습니다.')
      fetchList()
      initialize()
    }
  } catch (e) {
    vAlertError('저장 실패')
  }
}

const deleteData = async () => {
  if (formData.ACTKIND !== 'U') return
  if (!confirm('해당 자료를 삭제하시겠습니까?')) return

  try {
    await api.post('/api/hsod/HSOD_210U_STR', {
      ACTKIND: 'D',
      CMPYCD: authStore.CMPYCD,
      IOYM: formData.IOYM,
      IONO: formData.IONO
    })
    vAlert('삭제되었습니다.')
    fetchList()
    initialize()
  } catch (e) {
    vAlertError('삭제 실패')
  }
}

const initialize = () => {
  resetForm(formData)
  Object.assign(formData, {
    ACTKIND: 'A',
    GBN: searchData.GBN,
    DEPTCD: authStore.DEPTCD,
    DEPTNM: authStore.DEPTNM,
    IOYMD: initYMD,
    IOAMT: 0,
    IOVAT: 0
  })
}

// 4. 도움창 관련
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let gubun = 'C4', title = '거래처 선택', field = 'CUSTNM'
  if (type.includes('DEPT')) { gubun = 'D0'; title = '부서 선택'; field = 'DEPTNM' }

  Object.assign(modalProps, {
    title, path: '/api/ha00/HA00_00P_STR', defaultField: field,
    data: { GUBUN: gubun, CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
    columns: gubun === 'D0'
      ? [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }]
      : [{ title: '코드', field: 'CUSTCD', width: 70 }, { title: '거래처명', field: 'CUSTNM', width: 180 }],
    onConfirm: (data: any) => {
      if (type === 'FORM_DEPT') { formData.DEPTCD = data.DEPTCD; formData.DEPTNM = data.DEPTNM }
      else if (type === 'FORM_CUST') { formData.CUSTCD = data.CUSTCD; formData.CUSTNM = data.CUSTNM }
    }
  })
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v

onMounted(() => {
  nextTick(() => initGrid())
  fetchList()
})
</script>

<style scoped>
.hsip100u-wrapper { height: 100%; overflow: hidden; }
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
