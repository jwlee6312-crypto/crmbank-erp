<!--기본정보/재무관리/타계정계정과목등록 [ERP 프리미엄 표준] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba720u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-journal-check me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        타계정 계정과목 등록 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">타계정 설정 (HSBA720U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">

      <!-- 🅰️ 상세 정보 입력 (고밀도 배치) -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f8f9fa;">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1"></i> 타계정 유형 설정</span>
          <span v-if="formData.ACTKIND === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 중</span>
          <span v-else class="badge bg-primary" style="font-size: 10px;">신규 등록</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col style="width: 80px;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">유형코드</th>
                <td><input v-model="formData.TATYPE" type="text" class="form-control form-control-sm text-center fw-bold text-primary" maxlength="3" :readonly="formData.ACTKIND === 'U0'" placeholder="CODE" /></td>
                <th class="required">유형명</th>
                <td><input v-model="formData.TATYPENM" type="text" class="form-control form-control-sm" maxlength="50" /></td>
                <th class="required">차변계정</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.DACCTCD" type="text" class="form-control bg-light text-center" style="max-width: 60px;" readonly />
                    <input v-model="formData.DACCTNM" type="text" class="form-control" placeholder="계정 검색" @keyup.enter="openAccountHelp('D')" />
                    <button class="btn btn-outline-secondary px-1" @click="openAccountHelp('D')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">대변계정</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.CACCTCD" type="text" class="form-control bg-light text-center" style="max-width: 60px;" readonly />
                    <input v-model="formData.CACCTNM" type="text" class="form-control" placeholder="계정 검색" @keyup.enter="openAccountHelp('C')" />
                    <button class="btn btn-outline-secondary px-1" @click="openAccountHelp('C')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-danger">삭제여부</th>
                <td class="text-center">
                  <div class="form-check d-inline-block">
                    <input v-model="deleteCheck" class="form-check-input" type="checkbox" id="delCheck">
                    <label class="form-check-label small text-danger fw-bold" for="delCheck">삭제</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 (15행 페이징 처리) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 타계정 유형 리스트</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 바 -->
    <div class="erp-footer bg-dark text-white py-1 px-4 shadow-lg flex-shrink-0">
      <div class="d-flex justify-content-between align-items-center" style="font-size: 11px;">
        <div>등록된 유형: <span class="fw-bold text-warning">{{ activeItemCount }}</span> 건</div>
        <div class="text-white-50">※ 타계정 유형은 재고자산 수불 및 회계 전표 연동 시 사용됩니다.</div>
      </div>
    </div>

    <!-- 💡 계정 도움창 모달 -->
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
const deleteCheck = ref(false)
const formData = reactive({
  ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, USERID: authStore.USER_ID,
  TATYPE: '', TATYPENM: '', DACCTCD: '', DACCTNM: '', CACCTCD: '', CACCTNM: ''
})

const gridElement = ref<HTMLElement | null>(null); const grid = ref<Tabulator | null>(null); const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 15,
    placeholder: "조회된 데이터가 없습니다.", columnDefaults: { headerSort: false, headerHozAlign: "center" },
    columns: [
      { title: "유형코드", field: "CODE", width: 100, hozAlign: "center", cssClass: "fw-bold bg-light" },
      { title: "유형명칭", field: "CDNM", width: 200, cssClass: "text-primary fw-bold" },
      { title: "차변코드", field: "DACCTCD", width: 100, hozAlign: "center" },
      { title: "차변계정명", field: "DACCTNM", widthGrow: 1 },
      { title: "대변코드", field: "CACCTCD", width: 100, hozAlign: "center" },
      { title: "대변계정명", field: "CACCTNM", widthGrow: 1 }
    ]
  })
  grid.value.on("rowClick", (e, row) => {
    const data = row.getData()
    formData.TATYPE = data.CODE
    formData.TATYPENM = data.CDNM
    formData.DACCTCD = data.DACCTCD
    formData.DACCTNM = data.DACCTNM
    formData.CACCTCD = data.CACCTCD
    formData.CACCTNM = data.CACCTNM
    formData.ACTKIND = 'U0'
    deleteCheck.value = false
  })
}

// 3. 기능 구현
async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_720U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, USERID: authStore.USER_ID
    })
    if (grid.value) {
      grid.value.setData(res.data || [])
      activeItemCount.value = (res.data || []).length
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!formData.TATYPE || !formData.TATYPENM) return vAlertError('유형 코드와 명칭은 필수입니다.')
  if (!formData.DACCTCD || !formData.CACCTCD) return vAlertError('차변/대변 계정을 모두 선택하십시오.')

  if (!confirm('설정 정보를 저장하시겠습니까?')) return

  // ASP 삭제 로직: 체크박스 선택 시 ACTKIND를 D0로 변경
  const finalAct = deleteCheck.value ? 'D0' : formData.ACTKIND

  try {
    await api.post('/api/hsba/HSBA_720U_STR', { ...formData, ACTKIND: finalAct })
    vAlert('성공적으로 처리되었습니다.')
    search()
    initialize()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(formData)
  Object.assign(formData, {
    ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, USERID: authStore.USER_ID
  })
  deleteCheck.value = false
}

// 4. 도움창(모달) 연동
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openAccountHelp(mode: 'D' | 'C') {
  const gbn = mode === 'D' ? '62' : '112' // ASP 코드의 acct 파라미터 규격 적용

  Object.assign(modalProps, {
    title: mode === 'D' ? '차변 계정 선택' : '대변 계정 선택',
    path: '/api/ha00/HA00_00P_STR',
    data: { GUBUN: 'AC', ACCT: gbn, CMPYCD: authStore.CMPYCD },
    defaultField: 'CDNM',
    columns: [
      { title: '코드', field: 'CODE', width: 100 },
      { title: '계정명', field: 'CDNM', width: 200 }
    ],
    onConfirm: (selected: any) => {
      if (mode === 'D') {
        formData.DACCTCD = selected.CODE
        formData.DACCTNM = selected.CDNM
      } else {
        formData.CACCTCD = selected.CODE
        formData.CACCTNM = selected.CDNM
      }
    }
  })
  modalVisible.value = true
}

onMounted(() => {
  nextTick(() => {
    initGrid()
    search()
  })
})
</script>

<style scoped>
.hsba720u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
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
