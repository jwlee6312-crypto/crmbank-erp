<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsod210u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
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
        <button class="btn-erp btn-danger" @click="deleteData" :disabled="formData.ACTKIND !== 'U'">삭제</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
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
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
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
                    <input v-model="formData.CUSTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" readonly />
                    <input v-model="formData.CUSTNM" type="text" class="form-control border-start-0" placeholder="거래처 선택" @keyup.enter="handleOpenHelp('CUST')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>부&nbsp;&nbsp;&nbsp;&nbsp;서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 250px;">
                    <input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" readonly />
                    <input v-model="formData.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
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
        <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex justify-content-between align-items-center">
          <span><i class="bi bi-list-check me-1 text-primary"></i> 처리 내역 ({{ itemCount }} 건)</span>
          <span class="text-muted" style="font-size: 11px;">※ 목록에서 행을 클릭하면 수정 모드로 전환됩니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative;">
          <div ref="gridElement" style="position: absolute; top:0; left:0; width:100%; height:100%;"></div>
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
import { useCommonHelp } from '@/composables/useCommonHelp'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const now = new Date()
const initYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initFRYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  GBN: '001',
  FRYMD: initFRYMD,
  TOYMD: initYMD
})

const formData = reactive<any>({
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
      columnDefaults: { headerSort: false, headerHozAlign: 'center' },
      columns: [
        { title: "구분", field: "GBN", width: 100, hozAlign: "center", formatter: (c) => c.getValue() === '001' ? '판매장려금' : '재고보상금' },
        { title: "거래처 상호", field: "CUSTNM", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
        { title: "등록일", field: "IOYMD", width: 120, hozAlign: "center", formatter: (c) => formatDateString(c.getValue(), '-') },
        { title: "금액", field: "IOAMT", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "부가세", field: "IOVAT", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "비고", field: "REMARK", width: 250, hozAlign: "left" },
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

// 4. 도움창 핸들러 (표준화)
const handleOpenHelp = (type: string) => {
  if (type === 'CUST') {
    openHelp('CUST', (data: any) => {
      formData.CUSTCD = data.CUSTCD;
      formData.CUSTNM = data.CUSTNM;
    });
  } else if (type === 'DEPT') {
    openHelp('DEPT', (data: any) => {
      formData.DEPTCD = data.DEPTCD;
      formData.DEPTNM = data.DEPTNM;
    });
  }
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v

onMounted(() => {
  nextTick(() => initGrid())
  fetchList()
})
</script>

<style scoped>
.hsod210u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #dc3545 !important; color: #fff !important; border: none !important; }
.form-control, .form-select { font-size: 12px !important; height: 28px !important; padding: 2px 8px !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8fafc; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 6px 10px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>
