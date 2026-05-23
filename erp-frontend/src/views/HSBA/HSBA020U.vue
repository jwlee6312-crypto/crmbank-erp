<!--기본정보/시스템관리/대분류등록 [ERP 프리미엄 표준] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba020u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-diagram-2-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        대분류등록 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">대분류 관리 (HSBA020U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 및 입력 영역 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 80px;" /><col style="width: 100px;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="bg-light-subtle">재고자산</th>
                <td>
                  <select v-model="formData.ASTKIND" class="form-select form-select-sm" @change="search">
                    <option v-for="opt in assetOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">대분류코드</th>
                <td><input v-model="formData.AGRPCD" type="text" class="form-control form-control-sm text-center fw-bold" maxlength="3" :disabled="formData.ACTKIND === 'U0'" placeholder="3자리" /></td>
                <th class="required">대분류명</th>
                <td><input v-model="formData.AGRPNM" type="text" class="form-control form-control-sm" maxlength="50" @keyup.enter="search" /></td>
                <th>사용여부</th>
                <td>
                  <div class="form-check form-switch m-0 d-flex justify-content-center">
                    <input v-model="formData.USEYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useYn020">
                    <label class="ms-2 small fw-bold" for="useYn020">{{ formData.USEYN === 'Y' ? '사용' : '중지' }}</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 (15행 페이징 처리) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 대분류 리스트</span>
          <span class="text-muted" style="font-size: 11px;">※ 행 클릭 시 상단에 정보가 로드됩니다. (페이지당 15행)</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 바 -->
    <div class="erp-footer bg-dark text-white py-1 px-4 shadow-lg flex-shrink-0">
      <div class="d-flex justify-content-between align-items-center" style="font-size: 11px;">
        <div>조회 건수: <span class="fw-bold text-warning">{{ activeItemCount }}</span> 건</div>
        <div class="text-white-50">※ 대분류 정보는 품목 마스터 및 중분류 관리에 연동됩니다.</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 1. 상태 관리
const formData = reactive({
  ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, USERID: authStore.USER_ID,
  ASTKIND: '120', AGRPCD: '', AGRPNM: '', USEYN: 'Y'
})

const assetOptions = ref<any[]>([])
const gridElement = ref<HTMLElement | null>(null); const grid = ref<Tabulator | null>(null); const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 15,
    paginationButtonCount: 5, paginationSizeSelector: [15, 30, 50],
    placeholder: "데이터가 없습니다.", columnDefaults: { headerSort: false, headerHozAlign: "center" },
    columns: [
      { title: "대분류코드", field: "AGRPCD", width: 120, hozAlign: "center", cssClass: "fw-bold text-primary border-end" },
      { title: "대분류 명칭", field: "AGRPNM", minWidth: 300 },
      { title: "사용", field: "USEYN", width: 100, hozAlign: "center", formatter: (c) => c.getValue() === 'Y' ? '<span class="text-success fw-bold">O</span>' : '<span class="text-danger">X</span>' }
    ]
  })
  grid.value.on("rowClick", (e, row) => { Object.assign(formData, row.getData()); formData.ACTKIND = 'U0' })
}

// 3. 기능 구현
async function fetchAssetCodes() {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '100' } })
    assetOptions.value = res.data.map((i: any) => ({ CODECD: String(i.CODE || i.CODECD || '').trim(), CODENM: String(i.CDNM || i.CODENM || '').trim() }))
    if (assetOptions.value.length > 0) {
      formData.ASTKIND = assetOptions.value[0].CODECD
      search()
    }
  } catch (e) { console.error('코드 로드 실패', e) }
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_020U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, ASTKIND: formData.ASTKIND, AGRPNM: formData.AGRPNM
    })
    if (grid.value) {
      grid.value.setData(res.data || []);
      activeItemCount.value = (res.data || []).length
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!formData.AGRPCD || !formData.AGRPNM) return vAlertError('대분류 코드와 명칭은 필수입니다.')
  if (!confirm('저장하시겠습니까?')) return

  try {
    const res = await api.post('/api/hsba/HSBA_020U_STR', { ...formData })
    vAlert('성공적으로 저장되었습니다.')
    search()
    initialize()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  const currentAsset = formData.ASTKIND
  resetForm(formData)
  Object.assign(formData, {
    ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, USERID: authStore.USER_ID,
    ASTKIND: currentAsset, USEYN: 'Y'
  })
}

onMounted(async () => {
  await fetchAssetCodes()
  nextTick(() => { initGrid() })
})
</script>

<style scoped>
.hsba020u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11.5px; padding: 7px 10px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
:deep(.tabulator-footer) { background-color: #fff !important; border-top: 1px solid #dee2e6 !important; padding: 2px !important; font-size: 11px; }
</style>
