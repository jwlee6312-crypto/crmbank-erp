<!--기본정보/시스템관리/중분류등록 [ERP 프리미엄 표준 - 계층형 조회 및 등록] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba030u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-diagram-3-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        중분류등록 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">중분류 관리 (HSBA030U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchLargeList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">

      <!-- 🅰️ 조회 및 입력 영역 (압축 배치) -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 80px;" /><col style="width: 100px;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="bg-light-subtle">재고자산</th>
                <td>
                  <select v-model="searchForm.ASTKIND" class="form-select form-select-sm" @change="fetchLargeList">
                    <option v-for="opt in assetOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">대분류</th>
                <td>
                  <select v-model="formData.AGRPCD" class="form-select form-select-sm" @change="fetchMiddleList">
                    <option value="">-- 대분류 선택 --</option>
                    <option v-for="opt in largeOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">중분류코드</th>
                <td><input v-model="formData.BGRPCD" type="text" class="form-control form-control-sm text-center fw-bold" maxlength="3" :disabled="formData.ACTKIND === 'U0'" placeholder="3자리" /></td>
                <th class="required">중분류명</th>
                <td><input v-model="formData.BGRPNM" type="text" class="form-control form-control-sm" maxlength="50" placeholder="분류 명칭" /></td>
                <th>사용</th>
                <td>
                  <div class="form-check form-switch m-0 d-flex justify-content-center">
                    <input v-model="formData.USEYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useYn030">
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역 (2단 분할) -->
      <div class="flex-grow-1 overflow-hidden d-flex gap-2">
        <!-- 3. 좌측: 대분류 리스트 -->
        <div class="card border shadow-sm flex-shrink-0 d-flex flex-column" style="width: 320px;">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
            <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 대분류 리스트</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
            <div ref="largeGridRef"></div>
          </div>
        </div>

        <!-- 4. 우측: 중분류 리스트 -->
        <div class="card border shadow-sm flex-grow-1 d-flex flex-column">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
            <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 중분류 리스트</span>
            <span v-if="formData.AGRPCD" class="text-primary small fw-bold">선택된 대분류: {{ selectedLargeNm }}</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
            <div ref="middleGridRef"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 5. 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-1 px-4 shadow-lg flex-shrink-0">
      <div class="d-flex justify-content-between align-items-center" style="font-size: 11px;">
        <div>대분류: <span class="text-warning fw-bold">{{ largeOptions.length }}</span>건 | 중분류: <span class="text-info fw-bold">{{ middleItemCount }}</span>건</div>
        <div class="text-white-50">※ 좌측 대분류를 클릭하면 우측 중분류 정보가 조회됩니다.</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick, computed } from 'vue'
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
const searchForm = reactive({ ASTKIND: '120' })
const formData = reactive({
  ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, USERID: authStore.USER_ID,
  ASTKIND: '120', AGRPCD: '', BGRPCD: '', AGRPNM: '', BGRPNM: '', USEYN: 'Y'
})

const assetOptions = ref<any[]>([])
const largeOptions = ref<any[]>([])
const middleItemCount = ref(0)
const selectedLargeNm = ref('')

const largeGridRef = ref<HTMLElement | null>(null); let largeGrid: Tabulator | null = null
const middleGridRef = ref<HTMLElement | null>(null); let middleGrid: Tabulator | null = null

// 2. 그리드 초기화
const initGrids = () => {
  // 대분류 그리드
  if (largeGridRef.value) {
    largeGrid = new Tabulator(largeGridRef.value, {
      layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
      columnDefaults: { headerSort: false, headerHozAlign: "center" },
      columns: [
        { title: "코드", field: "AGRPCD", width: 70, hozAlign: "center", cssClass: "fw-bold text-secondary" },
        { title: "대분류 명칭", field: "AGRPNM", minWidth: 150 }
      ]
    })
    largeGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      formData.AGRPCD = data.AGRPCD
      selectedLargeNm.value = data.AGRPNM
      formData.ACTKIND = 'A0' // 대분류 선택 시 중분류는 신규등록 모드로
      fetchMiddleList()
    })
  }

  // 중분류 그리드
  if (middleGridRef.value) {
    middleGrid = new Tabulator(middleGridRef.value, {
      layout: "fitColumns", height: "100%", placeholder: "대분류를 선택하세요.",
      columnDefaults: { headerSort: false, headerHozAlign: "center" },
      columns: [
        { title: "중분류코드", field: "BGRPCD", width: 100, hozAlign: "center", cssClass: "fw-bold text-primary border-end" },
        { title: "중분류 명칭", field: "BGRPNM", minWidth: 250 },
        { title: "사용", field: "USEYN", width: 80, hozAlign: "center", formatter: (c) => c.getValue() === 'Y' ? '<span class="text-success fw-bold">O</span>' : '<span class="text-danger">X</span>' }
      ]
    })
    middleGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      Object.assign(formData, data)
      formData.ACTKIND = 'U0'
    })
  }
}

// 3. 데이터 로드
async function fetchAssetCodes() {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '100' } })
    assetOptions.value = res.data.map((i: any) => ({ CODECD: String(i.CODE || '').trim(), CODENM: String(i.CDNM || '').trim() }))
    if (assetOptions.value.length > 0) {
      searchForm.ASTKIND = assetOptions.value[0].CODECD
      fetchLargeList()
    }
  } catch (e) { console.error('자산코드 로드 실패') }
}

async function fetchLargeList() {
  try {
    formData.ASTKIND = searchForm.ASTKIND
    const res = await api.post('/api/hsba/HSBA_030U_STR', { ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, ASTKIND: searchForm.ASTKIND })
    largeGrid?.setData(res.data || [])
    largeOptions.value = res.data.map((i: any) => ({ CODECD: i.AGRPCD, CODENM: i.AGRPNM }))

    // 초기화
    middleGrid?.setData([])
    formData.AGRPCD = ''; formData.BGRPCD = ''; formData.BGRPNM = ''; selectedLargeNm.value = ''
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchMiddleList() {
  if (!formData.AGRPCD) return
  try {
    const res = await api.post('/api/hsba/HSBA_030U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, ASTKIND: searchForm.ASTKIND, AGRPCD: formData.AGRPCD
    })
    middleGrid?.setData(res.data || [])
    middleItemCount.value = res.data.length
  } catch (e) { vAlertError('중분류 조회 실패') }
}

// 4. 저장
async function save() {
  if (!formData.AGRPCD || !formData.BGRPCD || !formData.BGRPNM) return vAlertError('대분류, 중분류 코드 및 명칭은 필수입니다.')
  if (!confirm('설정된 정보를 저장하시겠습니까?')) return

  try {
    const res = await api.post('/api/hsba/HSBA_030U_STR', { ...formData, USERID: authStore.USER_ID })
    vAlert('저장되었습니다.')
    fetchMiddleList()
    initialize()
  } catch (e) { vAlertError('저장 오류 발생') }
}

function initialize() {
  const currentLarge = formData.AGRPCD
  const currentAsset = searchForm.ASTKIND
  resetForm(formData)
  Object.assign(formData, {
    ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, USERID: authStore.USER_ID,
    ASTKIND: currentAsset, AGRPCD: currentLarge, USEYN: 'Y'
  })
}

onMounted(async () => {
  await fetchAssetCodes()
  nextTick(() => { initGrids() })
})
</script>

<style scoped>
.hsba030u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11.5px; padding: 7px 10px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

:deep(.tabulator) { border: none !important; font-size: 12.5px; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
:deep(.tabulator-row.tabulator-selected) { background-color: #e0f2fe !important; border-left: 4px solid #005a9f !important; }
:deep(.tabulator-cell) { padding: 6px 4px !important; }
</style>
