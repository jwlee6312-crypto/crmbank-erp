<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpba020u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-folder-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">자재 대분류 관리 (HPBA020U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchList">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="saveData">
          <i class="bi bi-save"></i> 저장
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 및 입력 영역 -->
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
                <th class="required">재고자산</th>
                <td>
                  <select v-model="formData.ASTKIND" class="form-select form-select-sm" style="width: 150px;">
                    <option v-for="opt in astOptions" :key="opt.CODE" :value="opt.CODE">
                      [{{ opt.CODE }}] {{ opt.CDNM }}
                    </option>
                  </select>
                </td>
                <th class="required">대분류코드</th>
                <td>
                  <input v-model="formData.AGRPCD" type="text" class="form-control form-control-sm" style="width: 100px;" maxlength="3" :readonly="formData.ACTKIND === 'U0'" placeholder="3자리" />
                </td>
                <th class="required">대분류명</th>
                <td>
                  <input v-model="formData.AGRPNM" type="text" class="form-control form-control-sm" placeholder="대분류명 입력" />
                </td>
                <th>사용여부</th>
                <td>
                  <div class="form-check form-switch pt-1">
                    <input v-model="formData.USEYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N">
                    <label class="form-check-label small ms-1">{{ formData.USEYN === 'Y' ? '사용' : '미사용' }}</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-task me-1 text-primary"></i> 자재 대분류 목록</span>
          <span class="text-muted small">목록을 클릭하면 수정 모드로 전환됩니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">대분류 개수: <span class="fw-bold text-info">{{ itemCount }}</span> 개</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 재고자산별로 원자재, 제품 등의 대분류 체계를 관리합니다.
        </div>
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
  ACTKIND: 'A0',
  ASTKIND: '',
  AGRPCD: '',
  AGRPNM: '',
  USEYN: 'Y'
})

const astOptions = ref<any[]>([])
const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 기초 코드 로드
const fetchAstOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '100' } })
    astOptions.value = res.data
    if (astOptions.value.length > 0) formData.ASTKIND = astOptions.value[0].CODE
  } catch (e) {}
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "데이터가 없습니다.",
      selectable: 1,
      columns: [
        { title: "대분류 코드", field: "AGRPCD", width: 150, hozAlign: "center" },
        { title: "대분류명", field: "AGRPNM", minWidth: 300, cssClass: "fw-bold" },
        { title: "사용", field: "USEYN", width: 100, hozAlign: "center", formatter: "tickCross" }
      ],
    })

    grid.on("rowClick", (e, row) => {
        const data = row.getData()
        Object.assign(formData, data)
        formData.ACTKIND = 'U0'
    })
  }
}

// 3. 비즈니스 로직
const fetchList = async () => {
  if (!formData.ASTKIND) return
  try {
    const res = await api.post('/api/hpba/HPBA_020U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      ASTKIND: formData.ASTKIND
    })
    grid?.setData(res.data)
    itemCount.value = res.data.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const saveData = async () => {
  if (!formData.ASTKIND || !formData.AGRPCD || !formData.AGRPNM) {
    return vAlertError('재고자산, 대분류코드, 대분류명은 필수 입력 사항입니다.')
  }

  const msg = formData.ACTKIND === 'A0' ? '새 대분류를 등록하시겠습니까?' : '대분류 정보를 수정하시겠습니까?'
  if (!confirm(msg)) return

  try {
    await api.post('/api/hpba/HPBA_020U_STR', {
      ...formData,
      CMPYCD: authStore.CMPYCD,
      USERID: authStore.USERID
    })
    vAlert('정상적으로 처리되었습니다.')
    fetchList()
    resetInputForm()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const resetInputForm = () => {
    formData.ACTKIND = 'A0'
    formData.AGRPCD = ''
    formData.AGRPNM = ''
    formData.USEYN = 'Y'
}

const initialize = () => {
  resetForm(formData)
  if (astOptions.value.length > 0) formData.ASTKIND = astOptions.value[0].CODE
  resetInputForm()
  grid?.clearData()
  itemCount.value = 0
}

onMounted(async () => {
  await fetchAstOptions()
  nextTick(() => { initGrid(); fetchList(); })
})
</script>

<style scoped>
.hpba020u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }
.btn-save { background-color: #2ec4b6 !important; color: #fff !important; }
.btn-save:hover { background-color: #28afa3 !important; transform: translateY(-1px); }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* Tabulator 스타일 */
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }
</style>
