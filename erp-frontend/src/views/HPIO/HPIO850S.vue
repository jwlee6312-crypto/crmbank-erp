<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio850s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-check me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">출고의뢰서 관리 (HPIO850S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchList">
          <i class="bi bi-search"></i> 조회
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
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
                <th class="required">출고일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiOUTYMDFR" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiOUTYMDTO" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th class="required">생산라인</th>
                <td>
                  <!-- 💎 팝업에서 콤보박스로 변경 -->
                  <select v-model="searchData.LINECD" class="form-select form-select-sm" style="width: 180px;" @change="fetchList">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.LINECD" :value="opt.LINECD">
                      [{{ opt.LINECD }}] {{ opt.LINENM }}
                    </option>
                  </select>
                </td>
                <th class="required">조회구분</th>
                <td>
                  <select v-model="searchData.SLIPYN" class="form-select form-select-sm" style="width: 120px;" @change="fetchList">
                    <option value="N">미결건</option>
                    <option value="Y">완료건</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 메인 레이아웃 (좌: 목록, 우: 상세) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 의뢰 목록 -->
        <div class="card border-0 shadow-sm d-flex flex-column" style="width: 350px; border-radius: 8px;">
          <div class="card-header bg-light py-2 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-list-ol me-1 text-primary"></i> 의뢰 목록
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-auto">
            <div class="list-group list-group-flush">
              <div v-if="requestList.length === 0" class="p-5 text-center text-muted small">조회된 내역이 없습니다.</div>
              <button
                v-for="(item, idx) in requestList"
                :key="idx"
                class="list-group-item list-group-item-action p-3"
                :class="{ active: selectedItem?.IOYM === item.IOYM && selectedItem?.IONO === item.IONO }"
                @click="fetchDetail(item)"
              >
                <div class="d-flex justify-content-between align-items-center mb-1">
                  <span class="fw-bold text-truncate text-primary">{{ item.CUSTNM }}</span>
                  <span class="badge bg-secondary opacity-75">{{ item.IOYM }}-{{ item.IONO }}</span>
                </div>
                <div class="small text-dark opacity-75">
                  <i class="bi bi-gear-fill me-1"></i> {{ item.PROGNM }}
                </div>
              </button>
            </div>
          </div>
        </div>

        <!-- 우측: 상세 정보 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <!-- 상세 마스터 폼 -->
          <div class="card border-0 shadow-sm overflow-hidden" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark"><i class="bi bi-info-circle me-1 text-primary"></i> 의뢰 상세 정보</span>
              <div v-if="masterData.IONO" class="btn-group">
                <button class="btn btn-sm btn-outline-dark px-3" style="height: 28px; font-size: 12px;" @click="printWindow">
                  <i class="bi bi-printer me-1"></i> 의뢰서 인쇄
                </button>
              </div>
            </div>
            <div class="card-body p-0">
              <table class="erp-table-full">
                <colgroup>
                  <col style="width: 90px;"><col>
                  <col style="width: 90px;"><col>
                  <col style="width: 90px;"><col>
                  <col style="width: 90px;"><col>
                </colgroup>
                <tbody>
                  <tr>
                    <th>생산제품</th>
                    <td>
                      <div class="d-flex gap-1">
                        <span class="badge bg-light text-dark border p-2">{{ masterData.ITEMCD }}</span>
                        <span class="fw-bold align-self-center text-primary">{{ masterData.ITEMNM }}</span>
                      </div>
                    </td>
                    <th>규격/단위</th>
                    <td>{{ masterData.ITSIZE }} / {{ masterData.UNIT }}</td>
                    <th>생산량</th>
                    <td class="text-end fw-bold text-success">{{ formatNumber(masterData.PROQTY) }}</td>
                    <th>진행상태</th>
                    <td>
                      <span :class="masterData.CNFM === 'Y' ? 'text-success' : 'text-danger'" class="fw-bold">
                        <i :class="masterData.CNFM === 'Y' ? 'bi bi-check-circle-fill' : 'bi bi-clock-history'"></i>
                        {{ masterData.CNFM === 'Y' ? ' 승인' : ' 미승인' }}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <th>출고번호</th>
                    <td>{{ masterData.IOYM }}-{{ masterData.IONO }}</td>
                    <th>출고일자</th>
                    <td>{{ formatDateString(masterData.IOYMD, '-') }}</td>
                    <th>출고창고</th>
                    <td>{{ masterData.WHNM }}</td>
                    <th>입고창고</th>
                    <td>{{ masterData.IWHNM }}</td>
                  </tr>
                  <tr>
                    <th>주문번호</th>
                    <td>{{ masterData.ORDYM }}-{{ masterData.ORDNO }}</td>
                    <th>생산일자</th>
                    <td>{{ formatDateString(masterData.PROYMD, '-') }}</td>
                    <th>담당자</th>
                    <td colspan="3">{{ masterData.USERNM }} ({{ masterData.USERID }})</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 자재 그리드 -->
          <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom">
              <span class="fw-bold small text-dark"><i class="bi bi-box-fill me-1 text-success"></i> 투입 자재 상세</span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
              <div ref="gridElement" style="height: 100%;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">의뢰건수: <span class="fw-bold text-info">{{ requestList.length }}</span> 건</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 목록에서 항목을 선택하면 상세 품목 및 자재 투입 내역을 확인할 수 있습니다.
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
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

const now = new Date()
const initYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`

// 1. 상태 관리
const searchData = reactive({
  OUTYMDFR: initYMD,
  OUTYMDTO: initYMD,
  LINECD: '010',
  LINENM: '통합라인',
  SLIPYN: 'N'
})

const lineOptions = ref<any[]>([])
const uiOUTYMDFR = computed({ get: () => formatDateString(searchData.OUTYMDFR, '-'), set: (v) => { if(v) searchData.OUTYMDFR = v.replace(/-/g, '') } })
const uiOUTYMDTO = computed({ get: () => formatDateString(searchData.OUTYMDTO, '-'), set: (v) => { if(v) searchData.OUTYMDTO = v.replace(/-/g, '') } })

const requestList = ref<any[]>([])
const selectedItem = ref<any>(null)
const masterData = reactive<any>({})

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

// 🏭 라인 목록 로드 (콤보박스용)
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD, GBNCD: 'Y' } })
    lineOptions.value = res.data
  } catch (e) {}
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      columns: [
        { title: "자재코드", field: "ITEMCD", width: 120, hozAlign: "center" },
        { title: "투입자재명", field: "ITEMNM", minWidth: 250, cssClass: "fw-bold" },
        { title: "규격", field: "ITSIZE", width: 150 },
        { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
        { title: "출고수량", field: "QTY", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary fw-bold" },
        { title: "공급거래처", field: "CUSTNM", width: 200 }
      ],
    })
  }
}

// 3. 비즈니스 로직
const fetchList = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_850S_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      OUTYMDFR: searchData.OUTYMDFR,
      OUTYMDTO: searchData.OUTYMDTO,
      LINECD: searchData.LINECD,
      SLIPYN: searchData.SLIPYN
    })
    requestList.value = res.data
    selectedItem.value = null
    Object.keys(masterData).forEach(key => delete masterData[key])
    grid?.clearData()
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('목록 조회 실패') }
}

const fetchDetail = async (item: any) => {
  selectedItem.value = item
  try {
    // 1) 마스터 정보 조회 (S1)
    const resM = await api.post('/api/hpio/HPIO_850S_STR', {
      ACTKIND: 'S1',
      CMPYCD: authStore.CMPYCD,
      OUTYMDFR: searchData.OUTYMDFR,
      OUTYMDTO: searchData.OUTYMDTO,
      LINECD: searchData.LINECD,
      SLIPYN: searchData.SLIPYN,
      IOYM: item.IOYM,
      IONO: item.IONO,
      CUSTCD: item.CUSTCD
    })
    if (resM.data && resM.data.length > 0) {
      Object.assign(masterData, resM.data[0])
    }

    // 2) 자재 목록 조회 (S2)
    const resG = await api.post('/api/hpio/HPIO_850S_STR', {
      ACTKIND: 'S2',
      CMPYCD: authStore.CMPYCD,
      OUTYMDFR: searchData.OUTYMDFR,
      OUTYMDTO: searchData.OUTYMDTO,
      LINECD: searchData.LINECD,
      SLIPYN: searchData.SLIPYN,
      IOYM: item.IOYM,
      IONO: item.IONO,
      CUSTCD: item.CUSTCD
    })
    grid?.setData(resG.data)
  } catch (e) { vAlertError('상세 정보 조회 실패') }
}

const printWindow = () => {
  if (!masterData.IONO) return
  const url = `../HPIO/HPIO_REQOUT_PRINT.asp?PRTGU=Print&IOYM=${masterData.IOYM}&IONO=${masterData.IONO}&CUSTCD=${masterData.CUSTCD}`
  window.open(url, '의뢰서인쇄', 'width=700,height=600,scrollbars=yes')
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { OUTYMDFR: initYMD, OUTYMDTO: initYMD, LINECD: '010', LINENM: '통합라인', SLIPYN: 'N' })
  requestList.value = []
  selectedItem.value = null
  Object.keys(masterData).forEach(key => delete masterData[key])
  grid?.clearData()
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

// (라인 콤보박스 처리로 openHelp('LINE') 제거 가능)

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(() => {
  fetchLineOptions();
  nextTick(() => { initGrid(); fetchList(); })
})
</script>

<style scoped>
.hpio850s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; font-size: 12.5px; color: #333; }
.required::after { content: ' *'; color: #dc3545; }

/* 리스트 그룹 커스텀 */
.list-group-item { border: none; border-bottom: 1px solid #f1f3f5; transition: all 0.2s; cursor: pointer; }
.list-group-item:hover { background-color: #f8f9fa; padding-left: 1.5rem; }
.list-group-item.active { background-color: #eef2ff; color: #4361ee; border-left: 4px solid #4361ee; z-index: 1; }

/* Tabulator 스타일 */
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
</style>
