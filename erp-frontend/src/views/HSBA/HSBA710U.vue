<!--기본정보/재무관리/입금계정등록 [ERP 프리미엄 표준] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba710u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-wallet2 me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        입금계정 등록 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">입금계정 관리 (HSBA710U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-1 bg-light-subtle">
          <div class="d-flex align-items-center gap-3">
            <div class="input-group input-group-sm flex-nowrap" style="width: 350px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">입금유형명</span>
              <input v-model="searchForm.IMGBNNM" type="text" class="form-control border-0 bg-white" placeholder="유형명 검색" @keyup.enter="search" />
              <button class="btn btn-dark btn-sm" @click="search"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 🅱️ 상세 정보 입력 (10열 고밀도 배치) -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f8f9fa;">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1"></i> 유형 정보 및 계정 설정</span>
          <div class="d-flex gap-2 align-items-center">
            <span v-if="formData.ACTKIND === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 중</span>
            <span v-else class="badge bg-primary" style="font-size: 10px;">신규 등록</span>
          </div>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 85px;" /><col />
              <col style="width: 85px;" /><col />
              <col style="width: 85px;" /><col />
              <col style="width: 85px;" /><col />
              <col style="width: 80px;" /><col style="width: 80px;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">입금유형</th>
                <td><input v-model="formData.IMGBN" type="text" class="form-control form-control-sm text-center fw-bold text-primary" maxlength="3" :readonly="formData.ACTKIND === 'U0'" placeholder="CODE" /></td>
                <th class="required">유형명</th>
                <td><input v-model="formData.IMGBNNM" type="text" class="form-control form-control-sm" maxlength="50" /></td>
                <th class="required">차변계정</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.DACCTCD" type="text" class="form-control bg-light text-center" style="max-width: 60px;" readonly />
                    <input v-model="formData.DACCTNM" type="text" class="form-control" placeholder="계정 검색" @keyup.enter="openAccountHelp('D')" />
                    <button class="btn btn-outline-secondary btn-sm px-1" @click="openAccountHelp('D')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">대변계정</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.CACCTCD" type="text" class="form-control bg-light text-center" style="max-width: 60px;" readonly />
                    <input v-model="formData.CACCTNM" type="text" class="form-control" placeholder="계정 검색" @keyup.enter="openAccountHelp('C')" />
                    <button class="btn btn-outline-secondary btn-sm px-1" @click="openAccountHelp('C')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-danger">삭제여부</th>
                <td class="text-center">
                  <div class="form-check d-inline-block">
                    <input v-model="deleteCheck" class="form-check-input" type="checkbox" id="delCheck710">
                    <label class="form-check-label small text-danger fw-bold" for="delCheck710">삭제</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 (15행 페이징 처리) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 입금유형 리스트</span>
          <span class="text-muted" style="font-size: 11px;">※ 행 클릭 시 상단에 정보가 로드됩니다. (페이지당 15행)</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement"></div>
        </div>
      </div>
    </div>

    <!-- 📊 3. 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-1 px-4 shadow-lg flex-shrink-0">
      <div class="d-flex justify-content-between align-items-center" style="font-size: 11px;">
        <div>조회 건수: <span class="fw-bold text-warning">{{ activeItemCount }}</span> 건</div>
        <div class="text-white-50">※ 설정된 입금 계정은 자동 전표 생성 시 참조됩니다.</div>
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
const searchForm = reactive({ IMGBNNM: '' })
const formData = reactive({
  ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, USERID: authStore.USER_ID,
  IMGBN: '', IMGBNNM: '', DACCTCD: '', DACCTNM: '', CACCTCD: '', CACCTNM: '', USEYN: 'Y'
})

const gridElement = ref<HTMLElement | null>(null); const grid = ref<Tabulator | null>(null); const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 15,
    placeholder: "조회된 데이터가 없습니다.", columnDefaults: { headerSort: false, headerHozAlign: "center" },
    columns: [
      { title: "유형코드", field: "IMGBN", width: 100, hozAlign: "center", cssClass: "fw-bold bg-light" },
      { title: "유형명칭", field: "IMGBNNM", width: 180, cssClass: "text-primary fw-bold" },
      { title: "차변코드", field: "DACCTCD", width: 90, hozAlign: "center" },
      { title: "차변계정명", field: "DACCTNM", widthGrow: 1 },
      { title: "대변코드", field: "CACCTCD", width: 90, hozAlign: "center" },
      { title: "대변계정명", field: "CACCTNM", widthGrow: 1 },
      { title: "사용", field: "USEYN", width: 70, hozAlign: "center", formatter: (c) => c.getValue() === 'Y' ? '<span class="text-success fw-bold">O</span>' : '<span class="text-danger">X</span>' }
    ]
  })
  grid.value.on("rowClick", (e, row) => {
    const data = row.getData()
    Object.assign(formData, data)
    formData.ACTKIND = 'U0'
    deleteCheck.value = (data.USEYN === 'N')
  })
}

// 3. 기능 구현
async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_710U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IMGBN: '', IMGBNNM: searchForm.IMGBNNM
    })
    if (grid.value) {
      grid.value.setData(res.data || [])
      activeItemCount.value = (res.data || []).length
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!formData.IMGBN || !formData.IMGBNNM) return vAlertError('입금유형 코드와 명칭은 필수입니다.')
  if (!formData.DACCTCD || !formData.CACCTCD) return vAlertError('차변/대변 계정을 모두 선택하십시오.')

  if (!confirm('설정 정보를 저장하시겠습니까?')) return

  // ASP 로직 반영: 삭제 체크 시 USEYN을 N으로, 아니면 Y로 전송
  const finalAct = formData.ACTKIND
  const finalUseYn = deleteCheck.value ? 'N' : 'Y'

  try {
    await api.post('/api/hsba/HSBA_710U_STR', { ...formData, ACTKIND: finalAct, USEYN: finalUseYn })
    vAlert('성공적으로 저장되었습니다.')
    search()
    initialize()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(formData)
  Object.assign(formData, {
    ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, USERID: authStore.USER_ID, USEYN: 'Y'
  })
  deleteCheck.value = false
}

// 4. 도움창(모달) 연동
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openAccountHelp(mode: 'D' | 'C') {
  const gbn = mode === 'D' ? '11' : '111' // ASP 코드 규격 적용

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
.hsba710u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 4px 5px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 2px 4px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
:deep(.tabulator-footer) { background-color: #fff !important; border-top: 1px solid #dee2e6 !important; padding: 2px !important; font-size: 11px; }
</style>
