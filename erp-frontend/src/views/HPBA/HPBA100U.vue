<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpba100u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-gear-wide-connected me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">공정관리 (HPBA100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchLines">
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
                <th class="required">공정코드</th>
                <td>
                  <input v-model="formData.PROGCD" type="text" class="form-control form-control-sm" style="width: 100px;" maxlength="3" :readonly="formData.ACTKIND === 'U0'" />
                </td>
                <th class="required">공 정 명</th>
                <td>
                  <input v-model="formData.PROGNM" type="text" class="form-control form-control-sm" placeholder="공정명 입력" />
                </td>
                <th class="required">부&nbsp;&nbsp;&nbsp;&nbsp;서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="formData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
              <tr>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td>
                  <input v-model="formData.BIGO" type="text" class="form-control form-control-sm" placeholder="비고 입력" />
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

      <!-- 🅱️ 메인 레이아웃 (좌: 라인목록, 우: 공정목록) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 생산라인 목록 -->
        <div class="card border-0 shadow-sm d-flex flex-column" style="width: 300px; border-radius: 8px;">
          <div class="card-header bg-light py-2 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-list-columns me-1 text-primary"></i> 생산 라인
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
            <div ref="lineGridElement" style="height: 100%;"></div>
          </div>
        </div>

        <!-- 우측: 공정 목록 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark">
                <i class="bi bi-list-task me-1 text-primary"></i> 공정 목록
                <span v-if="selectedLine.LINENM" class="badge bg-info ms-2 px-3">{{ selectedLine.LINENM }}</span>
              </span>
              <span class="text-muted small">목록을 클릭하면 수정 모드로 전환됩니다.</span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
              <div ref="procGridElement" style="height: 100%;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">공정 개수: <span class="fw-bold text-info">{{ itemCount }}</span> 개</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 생산라인을 먼저 선택한 후 해당 라인에 소속된 공정 정보를 관리하십시오.
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
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

// 1. 상태 관리
const formData = reactive({
  ACTKIND: 'A0',
  LINECD: '',
  PROGCD: '',
  PROGNM: '',
  DEPTCD: '',
  DEPTNM: '',
  BIGO: '',
  DSPORD: 0,
  USEYN: 'Y'
})

const selectedLine = reactive({ LINECD: '', LINENM: '' })

const lineGridElement = ref<HTMLElement | null>(null)
const procGridElement = ref<HTMLElement | null>(null)
let lineGrid: Tabulator | null = null
let procGrid: Tabulator | null = null
const itemCount = ref(0)

// 2. 그리드 초기화
const initGrids = () => {
  if (lineGridElement.value) {
    lineGrid = new Tabulator(lineGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "데이터가 없습니다.",
      selectable: 1,
      columns: [
        { title: "라인명", field: "LINENM", minWidth: 150, cssClass: "fw-bold" },
        { title: "부서", field: "DEPTNM", width: 120 }
      ],
    })

    lineGrid.on("rowClick", (e, row) => {
        const data = row.getData()
        Object.assign(selectedLine, { LINECD: data.LINECD, LINENM: data.LINENM })
        formData.LINECD = data.LINECD
        fetchProcesses(data.LINECD)
        resetInputForm()
    })
  }

  if (procGridElement.value) {
    procGrid = new Tabulator(procGridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "라인을 선택하세요.",
      selectable: 1,
      columns: [
        { title: "코드", field: "PROGCD", width: 80, hozAlign: "center" },
        { title: "공 정 명", field: "PROGNM", minWidth: 180, cssClass: "fw-bold" },
        { title: "부 서 명", field: "DEPTNM", width: 150 },
        { title: "비 고", field: "BIGO", minWidth: 150 },
        { title: "순서", field: "DSPORD", width: 70, hozAlign: "center" },
        { title: "사용", field: "USEYN", width: 70, hozAlign: "center", formatter: "tickCross" }
      ],
    })

    procGrid.on("rowClick", (e, row) => {
        const data = row.getData()
        Object.assign(formData, data)
        formData.ACTKIND = 'U0'
    })
  }
}

// 3. 비즈니스 로직
const fetchLines = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineGrid?.setData(res.data)
  } catch (e) { vAlertError('라인 조회 실패') }
}

const fetchProcesses = async (linecd: string) => {
  try {
    const res = await api.post('/api/hpba/HPBA_100U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      LINECD: linecd
    })
    procGrid?.setData(res.data)
    itemCount.value = res.data.length
  } catch (e) { vAlertError('공정 조회 실패') }
}

const saveData = async () => {
  if (!formData.LINECD) return vAlertError('먼저 생산라인을 선택하세요.')
  if (!formData.PROGCD || !formData.PROGNM || !formData.DEPTCD) {
    return vAlertError('공정코드, 공정명, 부서는 필수 입력 사항입니다.')
  }

  const msg = formData.ACTKIND === 'A0' ? '새 공정을 등록하시겠습니까?' : '공정 정보를 수정하시겠습니까?'
  if (!confirm(msg)) return

  try {
    await api.post('/api/hpba/HPBA_100U_STR', {
      ...formData,
      CMPYCD: authStore.CMPYCD,
      USERID: authStore.USERID
    })
    vAlert('정상적으로 처리되었습니다.')
    fetchProcesses(formData.LINECD)
    resetInputForm()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const resetInputForm = () => {
    formData.ACTKIND = 'A0'
    formData.PROGCD = ''
    formData.PROGNM = ''
    formData.DEPTCD = ''
    formData.DEPTNM = ''
    formData.BIGO = ''
    formData.DSPORD = 0
    formData.USEYN = 'Y'
}

const initialize = () => {
  resetForm(formData)
  Object.assign(formData, { ACTKIND: 'A0', USEYN: 'Y', DSPORD: 0 })
  Object.assign(selectedLine, { LINECD: '', LINENM: '' })
  lineGrid?.clearData()
  procGrid?.clearData()
  itemCount.value = 0
  fetchLines()
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'DEPT') {
    Object.assign(modalProps, {
      title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM',
      data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '부서명', field: 'CDNM', width: 200 }],
      onConfirm: (data: any) => { formData.DEPTCD = data.CODE; formData.DEPTNM = data.CDNM }
    })
    modalVisible.value = true
  }
}

onMounted(() => {
  nextTick(() => { initGrids(); fetchLines(); })
})
</script>

<style scoped>
.hpba100u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
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
