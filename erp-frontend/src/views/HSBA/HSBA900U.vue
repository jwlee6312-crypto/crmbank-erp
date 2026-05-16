<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba900u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-code-square me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        코드정보 등록 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">코드정보 등록 (HSBA900U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchGroups">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 코드 입력 정보 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1"></i> 코드 정보 입력</span>
          <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark">수정 모드</span>
          <span v-else class="badge bg-primary">신규 모드</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">코드구분</th>
                <td style="width: 200px;">
                  <select v-model="masterData.CDGBN" class="form-select form-select-sm" :disabled="masterData.ACTKIND === 'U0'">
                    <option v-for="opt in groupOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">코&nbsp;&nbsp;&nbsp;&nbsp;드</th>
                <td style="width: 150px;">
                  <input v-model="masterData.CODE" type="text" class="form-control form-control-sm text-center fw-bold" maxlength="3" :readonly="masterData.ACTKIND === 'U0'" />
                </td>
                <th class="required">코&nbsp;드&nbsp;명</th>
                <td>
                  <input v-model="masterData.CDNM" type="text" class="form-control form-control-sm" maxlength="30" />
                </td>
              </tr>
              <tr>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td>
                  <input v-model="masterData.REMARK" type="text" class="form-control form-control-sm" maxlength="50" />
                </td>
                <th>출현순서</th>
                <td>
                  <input v-model="masterData.DSPORD" type="text" class="form-control form-control-sm text-center" maxlength="3" />
                </td>
                <th>사용여부</th>
                <td>
                  <div class="form-check form-check-inline mb-0">
                    <input v-model="masterData.USEYN" class="form-check-input" type="checkbox" id="useYn900" true-value="Y" false-value="N">
                    <label class="form-check-label small" for="useYn900">사용</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 하단 목록 영역 (좌: 코드 그룹, 우: 코드 상세) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 코드 그룹 목록 -->
        <div class="card border shadow-sm d-flex flex-column" style="width: 300px;">
          <div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-folder-fill me-1"></i> 코드 그룹
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="groupGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <!-- 우측: 선택된 그룹의 코드 상세 목록 -->
        <div class="card border shadow-sm flex-grow-1 d-flex flex-column">
          <div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark d-flex justify-content-between">
            <span><i class="bi bi-list-check me-1"></i> 상세 코드 목록</span>
            <span v-if="selectedGroupName" class="text-primary fw-bold">[{{ selectedGroupName }}]</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="codeGridElement" style="height: 100%;"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-6 small">
          조회된 상세 코드: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건
        </div>
        <div class="col-md-6 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 코드 그룹을 선택하면 상세 목록이 표시됩니다.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick, watch } from 'vue'
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
const masterData = reactive<any>({
  ACTKIND: 'A0',
  CMPYCD: authStore.CMPYCD,
  CDGBN: '010',
  CODE: '',
  CDNM: '',
  REMARK: '',
  DSPORD: '',
  USEYN: 'Y'
})

const groupOptions = ref<any[]>([])
const selectedGroupName = ref('')
const activeItemCount = ref(0)

const groupGridElement = ref<HTMLElement | null>(null)
const codeGridElement = ref<HTMLElement | null>(null)
let groupGrid: Tabulator | null = null
let codeGrid: Tabulator | null = null

// 2. 그리드 초기화
const initGrids = () => {
  // 좌측: 코드 그룹 그리드
  if (groupGridElement.value) {
    groupGrid = new Tabulator(groupGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "그룹 데이터 없음",
      columns: [
        { title: "코드 그룹", field: "CDNM", headerSort: false, formatter: (cell) => `<span class="cursor-pointer text-primary fw-bold">${cell.getValue()}</span>` }
      ]
    })

    groupGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      masterData.CDGBN = data.CODE
      selectedGroupName.value = data.CDNM
      masterData.ACTKIND = 'A0'
      masterData.CODE = ''
      masterData.CDNM = ''
      masterData.REMARK = ''
      masterData.DSPORD = ''
      masterData.USEYN = 'Y'
      fetchCodes(data.CODE)
    })
  }

  // 우측: 상세 코드 그리드
  if (codeGridElement.value) {
    codeGrid = new Tabulator(codeGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "그룹을 선택하세요",
      columnDefaults: { headerSort: false },
      columns: [
        { title: "코드", field: "CODE", width: 80, hozAlign: "center", cssClass: "fw-bold" },
        { title: "코드 명", field: "CDNM", width: 250 },
        { title: "비고", field: "REMARK", minWidth: 200 },
        { title: "순서", field: "DSPORD", width: 80, hozAlign: "center" },
        {
          title: "사용", field: "USEYN", width: 80, hozAlign: "center",
          formatter: (c) => c.getValue() === 'Y' ? '<i class="bi bi-check-lg text-success"></i>' : '<i class="bi bi-x-lg text-danger"></i>'
        }
      ]
    })

    codeGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      Object.assign(masterData, data)
      masterData.CDGBN = data.CDGBN || masterData.CDGBN // Ensure group code is maintained
      masterData.ACTKIND = 'U0'
    })
  }
}

// 3. 기능 구현
async function fetchGroupOptions() {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', {
      params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '010' }
    })
    groupOptions.value = res.data.map((i: any) => ({
      CODECD: String(Object.values(i)[0]).trim(),
      CODENM: String(Object.values(i)[1]).trim()
    }))
  } catch (e) { console.error('코드구분 로드 실패') }
}

async function fetchGroups() {
  try {
    const res = await api.post('/api/hsba/HSBA_900U_STR', {
      ACTKIND: 'S1',
      CMPYCD: authStore.CMPYCD,
      CDGBN: '010'
    })
    groupGrid?.setData(res.data)
  } catch (e) { vAlertError('그룹 목록 조회 실패') }
}

async function fetchCodes(cdgbn: string) {
  try {
    const res = await api.post('/api/hsba/HSBA_900U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      CDGBN: cdgbn
    })
    codeGrid?.setData(res.data)
    activeItemCount.value = res.data.length
  } catch (e) { vAlertError('상세 코드 조회 실패') }
}

async function save() {
  if (!masterData.CODE || !masterData.CDNM) {
    return vAlertError('코드와 코드명은 필수입니다.')
  }

  const confirmMsg = masterData.ACTKIND === 'A0' ? '신규 등록하시겠습니까?' : '수정하시겠습니까?'
  if (!confirm(confirmMsg)) return

  try {
    const res = await api.post('/api/hsba/HSBA_900U_STR', {
      ...masterData,
      USERID: authStore.USERID
    })
    vAlert('정상적으로 처리되었습니다.')
    fetchCodes(masterData.CDGBN)
    // Keep group selection but reset form for next entry if A0
    if (masterData.ACTKIND === 'A0') {
      const currentGbn = masterData.CDGBN
      initialize()
      masterData.CDGBN = currentGbn
    } else {
        masterData.ACTKIND = 'A0' // Reset to new after update
        masterData.CODE = ''
        masterData.CDNM = ''
        masterData.REMARK = ''
        masterData.DSPORD = ''
    }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  const currentGbn = masterData.CDGBN
  resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'A0',
    CMPYCD: authStore.CMPYCD,
    CDGBN: currentGbn,
    USEYN: 'Y'
  })
}

onMounted(async () => {
  await fetchGroupOptions()
  nextTick(() => {
    initGrids()
    fetchGroups()
  })
})

// Watch for group change in select to update the right side automatically if not in update mode
watch(() => masterData.CDGBN, (newVal) => {
    if (masterData.ACTKIND === 'A0') {
        const group = groupOptions.value.find(o => o.CODECD === newVal)
        selectedGroupName.value = group ? group.CODENM : ''
        fetchCodes(newVal)
    }
})

</script>

<style scoped>
.hsba900u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px 15px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 8px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
