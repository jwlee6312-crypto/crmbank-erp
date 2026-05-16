<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpba030u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-folder2-open me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">자재 중분류 관리 (HPBA030U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchLargeGroups">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="saveData">
          <i class="bi bi-save"></i> 저장
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 입력 폼 영역 -->
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
                  <select v-model="formData.ASTKIND" class="form-select form-select-sm" style="width: 150px;" @change="onAstKindChange">
                    <option v-for="opt in astOptions" :key="opt.CODE" :value="opt.CODE">
                      [{{ opt.CODE }}] {{ opt.CDNM }}
                    </option>
                  </select>
                </td>
                <th class="required">대분류코드</th>
                <td>
                  <select v-model="formData.AGRPCD" class="form-select form-select-sm" style="width: 150px;" @change="onLargeGroupChange">
                    <option value="">대분류 선택</option>
                    <option v-for="opt in largeGroupOptions" :key="opt.CODE" :value="opt.CODE">
                      [{{ opt.CODE }}] {{ opt.CDNM }}
                    </option>
                  </select>
                </td>
                <th class="required">중분류코드</th>
                <td>
                  <input v-model="formData.BGRPCD" type="text" class="form-control form-control-sm" style="width: 80px;" maxlength="3" :readonly="formData.ACTKIND === 'U0'" />
                </td>
                <th class="required">중분류명</th>
                <td>
                  <input v-model="formData.BGRPNM" type="text" class="form-control form-control-sm" placeholder="중분류명 입력" />
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

      <!-- 🅱️ 메인 레이아웃 (좌: 대분류목록, 우: 중분류목록) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 대분류 목록 (선택용) -->
        <div class="card border-0 shadow-sm d-flex flex-column" style="width: 300px; border-radius: 8px;">
          <div class="card-header bg-light py-2 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-folder-fill me-1 text-primary"></i> 대분류 목록
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
            <div ref="largeGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <!-- 우측: 상세 중분류 목록 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark">
                <i class="bi bi-list-ul me-1 text-primary"></i> 상세 중분류 리스트
                <span v-if="selectedLargeGroup.CDNM" class="badge bg-info ms-2 px-3">{{ selectedLargeGroup.CDNM }}</span>
              </span>
              <span class="text-muted small">목록을 클릭하면 수정 모드로 전환됩니다.</span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
              <div ref="middleGridElement" style="height: 100%;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">중분류 개수: <span class="fw-bold text-info">{{ itemCount }}</span> 개</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 대분류를 선택한 후 해당 분류에 속한 중분류 정보를 관리하십시오.
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
  BGRPCD: '',
  BGRPNM: '',
  USEYN: 'Y'
})

const selectedLargeGroup = reactive({ CODE: '', CDNM: '' })
const astOptions = ref<any[]>([])
const largeGroupOptions = ref<any[]>([])

const largeGridElement = ref<HTMLElement | null>(null)
const middleGridElement = ref<HTMLElement | null>(null)
let largeGrid: Tabulator | null = null
let middleGrid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 기초 코드 로드
const fetchAstOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '100' } })
    astOptions.value = res.data
    if (astOptions.value.length > 0) {
        formData.ASTKIND = astOptions.value[0].CODE
        fetchLargeGroups()
    }
  } catch (e) {}
}

const onAstKindChange = () => {
    fetchLargeGroups()
    resetInputForm()
    middleGrid?.clearData()
}

const onLargeGroupChange = () => {
    if (formData.AGRPCD) {
        const selected = largeGroupOptions.value.find(o => o.CODE === formData.AGRPCD)
        if (selected) {
            Object.assign(selectedLargeGroup, { CODE: selected.CODE, CDNM: selected.CDNM })
            fetchMiddleGroups(selected.CODE)
        }
    }
}

// 2. 그리드 초기화
const initGrids = () => {
  if (largeGridElement.value) {
    largeGrid = new Tabulator(largeGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "데이터가 없습니다.",
      selectable: 1,
      columns: [
        { title: "대분류명", field: "CDNM", minWidth: 200, cssClass: "fw-bold" }
      ],
    })

    largeGrid.on("rowClick", (e, row) => {
        const data = row.getData()
        Object.assign(selectedLargeGroup, { CODE: data.CODE, CDNM: data.CDNM })
        formData.AGRPCD = data.CODE
        formData.AGRPNM = data.CDNM
        fetchMiddleGroups(data.CODE)
        resetInputForm()
    })
  }

  if (middleGridElement.value) {
    middleGrid = new Tabulator(middleGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "대분류를 선택하세요.",
      selectable: 1,
      columns: [
        { title: "코드", field: "BGRPCD", width: 100, hozAlign: "center" },
        { title: "중 분 류 명", field: "BGRPNM", minWidth: 250, cssClass: "fw-bold" },
        { title: "사용", field: "USEYN", width: 100, hozAlign: "center", formatter: "tickCross" }
      ],
    })

    middleGrid.on("rowClick", (e, row) => {
        const data = row.getData()
        Object.assign(formData, data)
        formData.ACTKIND = 'U0'
    })
  }
}

// 3. 비즈니스 로직
const fetchLargeGroups = async () => {
  if (!formData.ASTKIND) return
  try {
    const res = await api.post('/api/hpba/HPBA_030U_STR', {
      ACTKIND: 'S1',
      CMPYCD: authStore.CMPYCD,
      ASTKIND: formData.ASTKIND
    })
    largeGroupOptions.value = res.data
    largeGrid?.setData(res.data)
  } catch (e) { vAlertError('대분류 조회 실패') }
}

const fetchMiddleGroups = async (largeGroupCd: string) => {
  try {
    const res = await api.post('/api/hpba/HPBA_030U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      ASTKIND: formData.ASTKIND,
      AGRPCD: largeGroupCd
    })
    middleGrid?.setData(res.data)
    itemCount.value = res.data.length
  } catch (e) { vAlertError('중분류 조회 실패') }
}

const saveData = async () => {
  if (!formData.ASTKIND || !formData.AGRPCD || !formData.BGRPCD || !formData.BGRPNM) {
    return vAlertError('재고자산, 대분류, 중분류코드, 중분류명은 필수 입력 사항입니다.')
  }

  const msg = formData.ACTKIND === 'A0' ? '새 중분류를 등록하시겠습니까?' : '중분류 정보를 수정하시겠습니까?'
  if (!confirm(msg)) return

  try {
    await api.post('/api/hpba/HPBA_030U_STR', {
      ...formData,
      CMPYCD: authStore.CMPYCD,
      USERID: authStore.USERID
    })
    vAlert('정상적으로 처리되었습니다.')
    fetchMiddleGroups(formData.AGRPCD)
    resetInputForm()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const resetInputForm = () => {
    formData.ACTKIND = 'A0'
    formData.BGRPCD = ''
    formData.BGRPNM = ''
    formData.USEYN = 'Y'
}

const initialize = () => {
  resetForm(formData)
  if (astOptions.value.length > 0) formData.ASTKIND = astOptions.value[0].CODE
  resetInputForm()
  Object.assign(selectedLargeGroup, { CODE: '', CDNM: '' })
  largeGrid?.clearData()
  middleGrid?.clearData()
  itemCount.value = 0
  fetchLargeGroups()
}

onMounted(async () => {
  await fetchAstOptions()
  nextTick(() => { initGrids(); })
})
</script>

<style scoped>
.hpba030u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
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
