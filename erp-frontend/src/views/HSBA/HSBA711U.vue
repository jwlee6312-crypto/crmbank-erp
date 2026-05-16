<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba711u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-wallet2 me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">입금계정 등록 (HSBA711U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 입금계정 정보 입력 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 입금계정 정보 입력</span>
          <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark">수정 모드</span>
          <span v-else class="badge bg-primary">신규 등록</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required" style="width: 100px;">입금유형</th>
                <td style="width: 150px;">
                  <input v-model="masterData.IMGBN" type="text" class="form-control form-control-sm text-center fw-bold" maxlength="3" :readonly="masterData.ACTKIND === 'U0'" />
                </td>
                <th class="required" style="width: 100px;">입금유형명</th>
                <td style="width: 250px;">
                  <input v-model="masterData.IMGBNNM" type="text" class="form-control form-control-sm" maxlength="20" />
                </td>
                <th style="width: 100px;">사용여부</th>
                <td>
                  <div class="form-check form-check-inline mb-0">
                    <input v-model="masterData.USEYN" class="form-check-input" type="checkbox" id="useYn711" true-value="Y" false-value="N">
                    <label class="form-check-label small" for="useYn711">사용</label>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required">차변계정</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.DACCTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="masterData.DACCTNM" type="text" class="form-control" placeholder="계정 선택" @keyup.enter="openHelp('DACCT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('DACCT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">대변계정</th>
                <td colspan="3">
                  <div class="input-group input-group-sm" style="width: 350px;">
                    <input v-model="masterData.CACCTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="masterData.CACCTNM" type="text" class="form-control" placeholder="계정 선택" @keyup.enter="openHelp('CACCT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('CACCT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 입금계정 목록</span>
          <div class="small text-muted">행 클릭 시 상세 정보를 수정할 수 있습니다.</div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-12 small">
          조회 건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건
        </div>
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
const masterData = reactive<any>({
  ACTKIND: 'A0',
  CMPYCD: authStore.CMPYCD,
  IMGBN: '',
  IMGBNNM: '',
  DACCTCD: '',
  DACCTNM: '',
  CACCTCD: '',
  CACCTNM: '',
  USEYN: 'Y'
})

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      { title: "유형", field: "IMGBN", width: 80, hozAlign: "center", cssClass: "fw-bold" },
      { title: "입금유형명", field: "IMGBNNM", width: 200 },
      { title: "차변코드", field: "DACCTCD", width: 100, hozAlign: "center" },
      { title: "차변계정명", field: "DACCTNM", width: 200 },
      { title: "대변코드", field: "CACCTCD", width: 100, hozAlign: "center" },
      { title: "대변계정명", field: "CACCTNM", minWidth: 200 },
      {
        title: "사용", field: "USEYN", width: 80, hozAlign: "center",
        formatter: (c) => c.getValue() === 'Y' ? '<i class="bi bi-check-lg text-success"></i>' : '<i class="bi bi-x-lg text-danger"></i>'
      }
    ]
  })

  grid.value.on("rowClick", (e, row) => {
    const data = row.getData()
    Object.assign(masterData, data)
    masterData.ACTKIND = 'U0'
  })
}

// 3. 기능 구현
async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_711U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD
    })
    if (grid.value) {
      grid.value.setData(res.data)
      activeItemCount.value = res.data.length
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.IMGBN || !masterData.IMGBNNM) {
    return vAlertError('입금유형과 유형명은 필수입니다.')
  }
  if (!masterData.DACCTCD || !masterData.CACCTCD) {
    return vAlertError('차변 및 대변 계정코드를 모두 선택하세요.')
  }

  const actionText = masterData.ACTKIND === 'A0' ? '등록' : '수정'
  if (!confirm(`입금계정 정보를 ${actionText}하시겠습니까?`)) return

  try {
    const res = await api.post('/api/hsba/HSBA_711U_STR', {
      ...masterData,
      USERID: authStore.USERID
    })
    vAlert('정상적으로 저장되었습니다.')
    initialize()
    search()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'A0',
    CMPYCD: authStore.CMPYCD,
    USEYN: 'Y'
  })
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let acctType = type === 'DACCT' ? '11' : '111' // Based on ASP openWindow1 gbn params

  Object.assign(modalProps, {
    title: '계정코드 선택',
    path: '/api/comm/HELP_ACCTCD_LTD',
    defaultField: 'ACCTNM',
    data: { CMPYCD: authStore.CMPYCD, ACCT: acctType },
    columns: [
      { title: '코드', field: 'ACCTCD', width: 100 },
      { title: '계정명', field: 'ACCTNM', minWidth: 200 }
    ],
    onConfirm: (data: any) => {
      if (type === 'DACCT') {
        masterData.DACCTCD = data.ACCTCD
        masterData.DACCTNM = data.ACCTNM
      } else {
        masterData.CACCTCD = data.ACCTCD
        masterData.CACCTNM = data.ACCTNM
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
.hsba711u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px 15px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 8px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
