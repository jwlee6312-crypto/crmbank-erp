<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpba900u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-code-square me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">코드관리 (HPBA900U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchGroups">
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
            </colgroup>
            <tbody>
              <tr>
                <th class="required">코드구분</th>
                <td>
                  <select v-model="formData.CDGBN" class="form-select form-select-sm" style="width: 180px;">
                    <option v-for="opt in groupOptions" :key="opt.CODE" :value="opt.CODE">
                      [{{ opt.CODE }}] {{ opt.CDNM }}
                    </option>
                  </select>
                </td>
                <th class="required">코&nbsp;&nbsp;&nbsp;&nbsp;드</th>
                <td>
                  <input v-model="formData.CODE" type="text" class="form-control form-control-sm" style="width: 100px;" maxlength="10" :readonly="formData.ACTKIND === 'U0'" />
                </td>
                <th class="required">코&nbsp;드&nbsp;명</th>
                <td>
                  <input v-model="formData.CDNM" type="text" class="form-control form-control-sm" placeholder="코드명 입력" />
                </td>
              </tr>
              <tr>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td>
                  <input v-model="formData.REMARK" type="text" class="form-control form-control-sm" placeholder="설명 입력" />
                </td>
                <th>출현순서</th>
                <td>
                  <input v-model="formData.DSPORD" type="number" class="form-control form-control-sm text-end" style="width: 80px;" />
                </td>
                <th>사용여부</th>
                <td>
                  <div class="form-check form-switch pt-1">
                    <input v-model="formData.USEYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N">
                    <label class="form-check-label small ms-1">{{ formData.USEYN === 'Y' ? '사용함' : '미사용' }}</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 메인 레이아웃 (좌: 그룹목록, 우: 코드목록) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 코드 그룹 목록 -->
        <div class="card border-0 shadow-sm d-flex flex-column" style="width: 300px; border-radius: 8px;">
          <div class="card-header bg-light py-2 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-folder2-open me-1 text-primary"></i> 코드 그룹
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
            <div ref="groupGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <!-- 우측: 상세 코드 목록 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark">
                <i class="bi bi-list-ul me-1 text-primary"></i> 상세 코드 리스트
                <span v-if="selectedGroup.CDNM" class="badge bg-info ms-2 px-3">{{ selectedGroup.CDNM }}</span>
              </span>
              <span class="text-muted small">목록을 클릭하면 수정 모드로 전환됩니다.</span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
              <div ref="codeGridElement" style="height: 100%;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">코드 개수: <span class="fw-bold text-info">{{ itemCount }}</span> 개</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 시스템 전반에서 사용되는 기초 공통 코드를 관리하는 화면입니다.
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
  CDGBN: '010',
  CODE: '',
  CDNM: '',
  REMARK: '',
  DSPORD: 0,
  USEYN: 'Y'
})

const selectedGroup = reactive({ CODE: '010', CDNM: '' })
const groupOptions = ref<any[]>([])

const groupGridElement = ref<HTMLElement | null>(null)
const codeGridElement = ref<HTMLElement | null>(null)
let groupGrid: Tabulator | null = null
let codeGrid: Tabulator | null = null
const itemCount = ref(0)

// 2. 그리드 초기화
const initGrids = () => {
  if (groupGridElement.value) {
    groupGrid = new Tabulator(groupGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "데이터가 없습니다.",
      selectable: 1,
      columns: [
        { title: "코드 그룹명", field: "CDNM", minWidth: 200, cssClass: "fw-bold" }
      ],
    })

    groupGrid.on("rowClick", (e, row) => {
        const data = row.getData()
        Object.assign(selectedGroup, { CODE: data.CODE, CDNM: data.CDNM })
        formData.CDGBN = data.CODE
        fetchCodes(data.CODE)
        resetInputForm(data.CODE)
    })
  }

  if (codeGridElement.value) {
    codeGrid = new Tabulator(codeGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "그룹을 선택하세요.",
      selectable: 1,
      columns: [
        { title: "코드", field: "CODE", width: 100, hozAlign: "center" },
        { title: "코 드 명", field: "CDNM", minWidth: 200, cssClass: "fw-bold" },
        { title: "비 고", field: "REMARK", minWidth: 200 },
        { title: "순서", field: "DSPORD", width: 80, hozAlign: "center" },
        { title: "사용", field: "USEYN", width: 80, hozAlign: "center", formatter: "tickCross" }
      ],
    })

    codeGrid.on("rowClick", (e, row) => {
        const data = row.getData()
        Object.assign(formData, data)
        formData.ACTKIND = 'U0'
    })
  }
}

// 3. 비즈니스 로직
const fetchGroups = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '010' } })
    groupOptions.value = res.data
    groupGrid?.setData(res.data)
  } catch (e) { vAlertError('그룹 조회 실패') }
}

const fetchCodes = async (groupCd: string) => {
  try {
    const res = await api.post('/api/hpba/HPBA_900U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      CDGBN: groupCd
    })
    codeGrid?.setData(res.data)
    itemCount.value = res.data.length
  } catch (e) { vAlertError('코드 조회 실패') }
}

const saveData = async () => {
  if (!formData.CODE || !formData.CDNM) {
    return vAlertError('코드와 코드명은 필수 입력 사항입니다.')
  }

  const msg = formData.ACTKIND === 'A0' ? '새 코드를 등록하시겠습니까?' : '코드 정보를 수정하시겠습니까?'
  if (!confirm(msg)) return

  try {
    await api.post('/api/hpba/HPBA_900U_STR', {
      ...formData,
      CMPYCD: authStore.CMPYCD,
      USERID: authStore.USERID
    })
    vAlert('정상적으로 처리되었습니다.')
    fetchCodes(formData.CDGBN)
    resetInputForm(formData.CDGBN)
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const resetInputForm = (groupCd: string) => {
    formData.ACTKIND = 'A0'
    formData.CDGBN = groupCd
    formData.CODE = ''
    formData.CDNM = ''
    formData.REMARK = ''
    formData.DSPORD = 0
    formData.USEYN = 'Y'
}

const initialize = () => {
  resetForm(formData)
  Object.assign(formData, { ACTKIND: 'A0', CDGBN: '010', USEYN: 'Y', DSPORD: 0 })
  Object.assign(selectedGroup, { CODE: '010', CDNM: '' })
  groupGrid?.clearData()
  codeGrid?.clearData()
  itemCount.value = 0
  fetchGroups()
}

onMounted(() => {
  nextTick(() => { initGrids(); fetchGroups(); })
})
</script>

<style scoped>
.hpba900u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
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
