<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpba130u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-gear-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">라인관리 (HPBA130U)</span>
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
                <th class="required">라인코드</th>
                <td>
                  <input v-model="formData.LINECD" type="text" class="form-control form-control-sm" style="width: 100px;" maxlength="3" :readonly="formData.ACTKIND === 'U'" />
                </td>
                <th class="required">라 인 명</th>
                <td>
                  <input v-model="formData.LINENM" type="text" class="form-control form-control-sm" placeholder="라인명 입력" />
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

      <!-- 🅲 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-task me-1 text-primary"></i> 생산 라인 목록</span>
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
        <div class="col-md-4 small">라인 개수: <span class="fw-bold text-info">{{ itemCount }}</span> 개</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 생산 라인 정보를 등록하고 부서와 연동하여 관리합니다.
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
  ACTKIND: 'A',
  LINECD: '',
  LINENM: '',
  DEPTCD: '',
  DEPTNM: '',
  BIGO: '',
  DSPORD: 0,
  USEYN: 'Y'
})

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "데이터가 없습니다.",
      selectable: 1,
      columns: [
        { title: "라인코드", field: "LINECD", width: 100, hozAlign: "center" },
        { title: "라 인 명", field: "LINENM", minWidth: 200, cssClass: "fw-bold" },
        { title: "부 서 명", field: "DEPTNM", width: 200 },
        { title: "비 고", field: "BIGO", minWidth: 200 },
        { title: "순서", field: "DSPORD", width: 80, hozAlign: "center" },
        { title: "사용", field: "USEYN", width: 80, hozAlign: "center", formatter: "tickCross" }
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
const fetchList = async () => {
  try {
    const res = await api.post('/api/hpba/HPBA_130U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD
    })
    grid?.setData(res.data)
    itemCount.value = res.data.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const saveData = async () => {
  if (!formData.LINECD || !formData.LINENM || !formData.DEPTCD) {
    return vAlertError('라인코드, 라인명, 부서는 필수 입력 사항입니다.')
  }

  const msg = formData.ACTKIND === 'A' ? '새 라인을 등록하시겠습니까?' : '수정된 정보를 저장하시겠습니까?'
  if (!confirm(msg)) return

  try {
    await api.post('/api/hpba/HPBA_130U_STR', {
      ...formData,
      CMPYCD: authStore.CMPYCD,
      USERID: authStore.USERID
    })
    vAlert('정상적으로 처리되었습니다.')
    fetchList()
    initialize()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

const initialize = () => {
  resetForm(formData)
  Object.assign(formData, { ACTKIND: 'A', USEYN: 'Y', DSPORD: 0 })
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

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(() => {
  nextTick(() => { initGrid(); fetchList(); })
})
</script>

<style scoped>
.hpba130u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
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
