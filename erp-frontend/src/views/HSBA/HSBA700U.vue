<!--기본정보/재무관리/재고자산계정등록 [ERP 프리미엄 표준] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba700u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-calculator-fill me-2 text-primary"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        재고자산계정 등록 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">재고자산 계정관리 (HSBA700U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="fetchList">초기화(새로고침)</button>
        <button class="btn-erp btn-save" @click="handleBulkSave">일괄 저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 안내 문구 -->
      <div class="alert alert-primary py-1 px-3 mb-0 d-flex align-items-center shadow-sm" style="font-size: 12px; border-left: 4px solid #005a9f;">
        <i class="bi bi-info-circle-fill me-2 text-primary"></i>
        재고자산별 매입/매출 계정코드를 설정하십시오. 돋보기 아이콘을 클릭하여 계정을 검색할 수 있습니다.
      </div>

      <!-- 🅱️ 데이터 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement"></div>
        </div>
      </div>
    </div>

    <!-- 📊 3. 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-1 px-4 shadow-lg flex-shrink-0">
      <div class="d-flex justify-content-between align-items-center" style="font-size: 11px;">
        <div>설정 가능 대상: <span class="fw-bold text-warning">{{ activeItemCount }}</span> 유형</div>
        <div class="text-white-50">※ 계정 정보가 모두 누락된 항목은 저장 시 '삭제' 처리됩니다.</div>
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
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

// 1. 상태 관리
const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)

// 2. 그리드 컬럼 정의
const accountSearchIcon = () => "<i class='bi bi-search text-primary' style='cursor:pointer;'></i>"

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "데이터를 조회 중입니다...",
    columnHeaderVertAlign: "middle",
    columnDefaults: { headerSort: false },
    columns: [
      { title: "재고자산 유형", field: "CDNM", width: 150, hozAlign: "center", cssClass: "fw-bold bg-light", frozen: true },
      { title: "코드", field: "CODE", visible: false },

      // 매입(Purchase) 그룹
      {
        title: "매입 (Purchase)",
        headerHozAlign: "center",
        columns: [
          { title: "차변계정", field: "IMACCTNM", widthGrow: 1,
            formatter: (cell) => cell.getData().IMACCTCD ? `[${cell.getData().IMACCTCD}] ${cell.getValue()}` : ""
          },
          { title: " ", field: "searchIM", width: 35, hozAlign: "center", formatter: accountSearchIcon,
            cellClick: (e, cell) => openAccountHelp('IM', cell)
          },
          { title: "대변계정", field: "IPACCTNM", widthGrow: 1,
            formatter: (cell) => cell.getData().IPACCTCD ? `[${cell.getData().IPACCTCD}] ${cell.getValue()}` : ""
          },
          { title: " ", field: "searchIP", width: 35, hozAlign: "center", formatter: accountSearchIcon,
            cellClick: (e, cell) => openAccountHelp('IP', cell)
          },
          { title: "선급부가세", field: "IVACCTNM", widthGrow: 1,
            formatter: (cell) => cell.getData().IVACCTCD ? `[${cell.getData().IVACCTCD}] ${cell.getValue()}` : ""
          },
          { title: " ", field: "searchIV", width: 35, hozAlign: "center", formatter: accountSearchIcon,
            cellClick: (e, cell) => openAccountHelp('IV', cell)
          },
        ]
      },

      // 매출(Sales) 그룹
      {
        title: "매출 (Sales)",
        headerHozAlign: "center",
        columns: [
          { title: "차변계정", field: "OMACCTNM", widthGrow: 1,
            formatter: (cell) => cell.getData().OMACCTCD ? `[${cell.getData().OMACCTCD}] ${cell.getValue()}` : ""
          },
          { title: " ", field: "searchOM", width: 35, hozAlign: "center", formatter: accountSearchIcon,
            cellClick: (e, cell) => openAccountHelp('OM', cell)
          },
          { title: "대변계정", field: "OPACCTNM", widthGrow: 1,
            formatter: (cell) => cell.getData().OPACCTCD ? `[${cell.getData().OPACCTCD}] ${cell.getValue()}` : ""
          },
          { title: " ", field: "searchOP", width: 35, hozAlign: "center", formatter: accountSearchIcon,
            cellClick: (e, cell) => openAccountHelp('OP', cell)
          },
          { title: "예수부가세", field: "OVACCTNM", widthGrow: 1,
            formatter: (cell) => cell.getData().OVACCTCD ? `[${cell.getData().OVACCTCD}] ${cell.getValue()}` : ""
          },
          { title: " ", field: "searchOV", width: 35, hozAlign: "center", formatter: accountSearchIcon,
            cellClick: (e, cell) => openAccountHelp('OV', cell)
          },
        ]
      }
    ]
  })
}

// 3. 데이터 로드 (S0)
async function fetchList() {
  try {
    const res = await api.post('/api/hsba/HSBA_700U_STR', { ACTKIND: 'S0', CMPYCD: authStore.CMPYCD })
    if (grid.value) {
      grid.value.setData(res.data || [])
      activeItemCount.value = (res.data || []).length
    }
  } catch (e) { vAlertError('계정 설정 정보 조회 실패') }
}

// 4. 저장 (일괄 처리)
async function handleBulkSave() {
  if (!confirm('그리드에 표시된 모든 변경 사항을 저장하시겠습니까?')) return

  const data = grid.value?.getData() || []
  let successCount = 0

  try {
    for (const row of data) {
      // ASP 로직: 하나라도 계정이 있으면 저장(A0), 모두 없으면 삭제(D0)
      const hasAccount = row.IMACCTCD || row.IPACCTCD || row.IVACCTCD || row.OMACCTCD || row.OPACCTCD || row.OVACCTCD
      const act = hasAccount ? 'A0' : 'D0'

      await api.post('/api/hsba/HSBA_700U_STR', {
        ACTKIND: act,
        CMPYCD: authStore.CMPYCD,
        ASTKIND: row.CODE,
        ASTKINDNM: row.CDNM,
        IMACCTCD: row.IMACCTCD || '',
        IPACCTCD: row.IPACCTCD || '',
        IVACCTCD: row.IVACCTCD || '',
        OMACCTCD: row.OMACCTCD || '',
        OPACCTCD: row.OPACCTCD || '',
        OVACCTCD: row.OVACCTCD || '',
        USERID: authStore.USER_ID
      })
      successCount++
    }
    vAlert(`${successCount}개 유형의 계정 설정이 완료되었습니다.`)
    fetchList()
  } catch (e) { vAlertError('일괄 저장 처리 중 오류 발생') }
}

// 5. 도움창(모달) 연동
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openAccountHelp(type: string, cell: any) {
  const row = cell.getRow()

  // ASP의 HELP_ACCTCD_LTD.asp gbn 파라미터 규격 적용
  let gbn = '112'
  if (type === 'IM') gbn = '112';
  else if (type === 'IP') gbn = '210';
  else if (type === 'IV') gbn = '1118';
  else if (type === 'OM') gbn = '1114';
  else if (type === 'OP') gbn = '51';
  else if (type === 'OV') gbn = '2104';

  // HA00_00P_STR 'A0','haionnet','','112'
  Object.assign(modalProps, {
    title: '계정과목 선택',
    path: '/api/ha00/HA00_00P_STR',
    data: { GUBUN: 'A0', GBNCD: '', CODE: gbn, CMPYCD: authStore.CMPYCD },
    defaultField: 'ACCTNM',
    columns: [
      { title: '계정코드', field: 'ACCTCD', width: 100 },
      { title: '계정과목명', field: 'ACCTNM', width: 200 }
    ],
    onConfirm: (selected: any) => {
      const updateData: any = {}
      updateData[`${type}ACCTCD`] = selected.ACCTCD
      updateData[`${type}ACCTNM`] = selected.ACCTNM
      row.update(updateData)
    }
  })
  modalVisible.value = true
}

onMounted(() => {
  nextTick(() => {
    initGrid()
    fetchList()
  })
})
</script>

<style scoped>
.hsba700u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

:deep(.tabulator) { border: none !important; font-size: 12px; background: transparent !important; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col) { background-color: #f1f5f9 !important; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; text-align: center; }
:deep(.tabulator-row) { border-bottom: 1px solid #edf2f7 !important; background-color: #fff !important; }
:deep(.tabulator-cell) { padding: 6px 4px !important; vertical-align: middle !important; border-right: 1px solid #f1f5f9 !important; }

/* 매입/매출 영역 구분 강조 */
:deep(.tabulator-col[data-field="IMACCTNM"]),
:deep(.tabulator-col[data-field="OMACCTNM"]) { border-left: 2px solid #cbd5e1 !important; }
</style>
