<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio870u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-shield-check me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">외주가공생산실적승인 (HPIO870U)</span>
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
                  <div class="input-group input-group-sm" style="width: 200px;">
                    <input v-model="searchData.LINECD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.LINENM" type="text" class="form-control" placeholder="라인 선택" @keyup.enter="openHelp('LINE')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('LINE')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">조회구분</th>
                <td>
                  <select v-model="searchData.SLIPYN" class="form-select form-select-sm" style="width: 140px;" @change="fetchList">
                    <option value="N">미승인건</option>
                    <option value="Y">승인완료건</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 메인 레이아웃 (좌: 목록, 우: 상세 및 승인) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden">
        <!-- 좌측: 실적 목록 -->
        <div class="card border-0 shadow-sm d-flex flex-column" style="width: 350px; border-radius: 8px;">
          <div class="card-header bg-light py-2 px-3 border-bottom fw-bold small text-dark">
            <i class="bi bi-list-check me-1 text-primary"></i> 실적 대기 목록
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-auto">
            <div class="list-group list-group-flush">
              <div v-if="resultList.length === 0" class="p-5 text-center text-muted small">조회된 내역이 없습니다.</div>
              <button
                v-for="(item, idx) in resultList"
                :key="idx"
                class="list-group-item list-group-item-action p-3"
                :class="{ active: selectedItem?.IOYM === item.IOYM && selectedItem?.IONO === item.IONO }"
                @click="fetchDetail(item)"
              >
                <div class="d-flex justify-content-between align-items-center mb-1">
                  <span class="fw-bold text-truncate">{{ item.CUSTNM }}</span>
                  <span class="badge bg-secondary opacity-75">{{ item.IOYM }}-{{ item.IONO }}</span>
                </div>
                <div class="d-flex justify-content-between small opacity-75">
                  <span><i class="bi bi-gear me-1"></i> {{ item.PROGNM }}</span>
                  <span :class="item.SLIPYN === 'Y' ? 'text-success fw-bold' : 'text-danger fw-bold'">
                    {{ item.SLIPYN === 'Y' ? '승인완료' : '미승인' }}
                  </span>
                </div>
              </button>
            </div>
          </div>
        </div>

        <!-- 우측: 상세 정보 및 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <!-- 상세 마스터 정보 -->
          <div class="card border-0 shadow-sm overflow-hidden" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark"><i class="bi bi-info-circle me-1 text-primary"></i> 실적 상세 및 승인 정보</span>
              <div v-if="masterData.IONO" class="btn-group gap-2">
                <button v-if="searchData.SLIPYN === 'N'" class="btn btn-sm btn-success px-3" style="height: 28px; font-size: 12px; font-weight: 600;" @click="handleApproval('U0')">
                  <i class="bi bi-check2-square me-1"></i> 승인처리
                </button>
                <button v-if="searchData.SLIPYN === 'Y'" class="btn btn-sm btn-danger px-3" style="height: 28px; font-size: 12px; font-weight: 600;" @click="handleApproval('D0')">
                  <i class="bi bi-x-square me-1"></i> 승인취소
                </button>
                <button class="btn btn-sm btn-outline-dark px-3" style="height: 28px; font-size: 12px;" @click="printWindow">
                  <i class="bi bi-printer me-1"></i> 인쇄
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
                    <th>제품코드</th>
                    <td>
                      <div class="d-flex gap-1">
                        <span class="badge bg-light text-dark border p-2">{{ masterData.ITEMCD }}</span>
                        <span class="fw-bold align-self-center">{{ masterData.ITEMNM }}</span>
                      </div>
                    </td>
                    <th>규격/단위</th>
                    <td>{{ masterData.ITSIZE }} / {{ masterData.UNIT }}</td>
                    <th>생산량</th>
                    <td class="text-end fw-bold text-primary">{{ formatNumber(masterData.PROQTY) }}</td>
                    <th>진행상태</th>
                    <td>
                      <span :class="masterData.CNFM === 'Y' ? 'text-success' : 'text-danger'" class="fw-bold fs-6">
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
                    <th>생산공정</th>
                    <td>{{ masterData.PROGNM }}</td>
                    <th>출고공정</th>
                    <td>{{ masterData.BEFPROGNM }}</td>
                  </tr>
                  <tr>
                    <th>주문번호</th>
                    <td>{{ masterData.ORDYM }}-{{ masterData.ORDNO }}</td>
                    <th>생산일자</th>
                    <td>
                        <input v-model="uiPROYMD" type="date" class="form-control form-control-sm border-primary" style="width: 130px; height: 24px;" />
                    </td>
                    <th>담당자</th>
                    <td>
                        <div class="input-group input-group-sm">
                            <input v-model="masterData.USERNM" type="text" class="form-control border-0 bg-transparent" readonly />
                            <button class="btn btn-xs btn-link" @click="openHelp('USER')"><i class="bi bi-search"></i></button>
                        </div>
                    </td>
                    <th>비고</th>
                    <td>
                        <input v-model="masterData.REMARK" type="text" class="form-control form-control-sm" style="height: 24px;" />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 그리드 영역 -->
          <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
            <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap me-1 text-primary"></i> 생산 재공품 상세</span>
              <span class="text-danger small fw-bold">※ 승인처리 시 외주비가 정산에 반영됩니다.</span>
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
        <div class="col-md-4 small">조회건수: <span class="fw-bold text-info">{{ resultList.length }}</span> 건</div>
        <div class="col-md-8 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 미승인 상태에서는 외주비 지급이 불가능합니다. 검토 후 승인처리 하세요.
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

const uiOUTYMDFR = computed({ get: () => formatDateString(searchData.OUTYMDFR, '-'), set: (v) => { if(v) searchData.OUTYMDFR = v.replace(/-/g, '') } })
const uiOUTYMDTO = computed({ get: () => formatDateString(searchData.OUTYMDTO, '-'), set: (v) => { if(v) searchData.OUTYMDTO = v.replace(/-/g, '') } })

const resultList = ref<any[]>([])
const selectedItem = ref<any>(null)
const masterData = reactive<any>({
    IOYMD: '', PROYMD: '', REMARK: '', USERNM: authStore.USER_NM, USERID: authStore.USER_ID
})
const closingInfo = reactive({ CLSYMD: '', SCLSYM: '', PCLSYM: '' })

const uiPROYMD = computed({ get: () => formatDateString(masterData.PROYMD, '-'), set: (v) => { if(v) masterData.PROYMD = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns", height: "100%", selectable: true,
      columns: [
        { title: "코드", field: "ITEMCD", width: 100, hozAlign: "center" },
        { title: "생산재공품", field: "ITEMNM", minWidth: 200, cssClass: "fw-bold" },
        { title: "규격", field: "ITSIZE", width: 120 },
        { title: "단위", field: "UNIT", width: 60, hozAlign: "center" },
        { title: "지시량", field: "QTY", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        { title: "생산량", field: "PRDQTY", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-success fw-bold" },
        { title: "가공단가", field: "PRICE", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
        {
          title: "금액", field: "AMT", width: 110, hozAlign: "right",
          formatter: (cell) => {
              const d = cell.getData();
              return formatNumber(Number(d.QTY || 0) * Number(d.PRICE || 0));
          },
          cssClass: "text-primary fw-bold"
        },
        { title: "거래처", field: "CUSTNM", width: 150 }
      ]
    })
  }
}

// 3. 비즈니스 로직
const fetchList = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_870U_STR', {
      ACTKIND: 'S1', CMPYCD: authStore.CMPYCD,
      OUTYMDFR: searchData.OUTYMDFR, OUTYMDTO: searchData.OUTYMDTO,
      LINECD: searchData.LINECD, SLIPYN: searchData.SLIPYN
    })
    resultList.value = res.data
    selectedItem.value = null; Object.assign(masterData, { USERNM: authStore.USER_NM, USERID: authStore.USER_ID }); grid?.clearData()
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('목록 조회 실패') }
}

const fetchDetail = async (item: any) => {
  selectedItem.value = item
  try {
    // 1) 마스터 상세 (S0)
    const resM = await api.post('/api/hpio/HPIO_870U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      OUTYMDFR: searchData.OUTYMDFR, OUTYMDTO: searchData.OUTYMDTO,
      LINECD: searchData.LINECD, SLIPYN: searchData.SLIPYN,
      IOYM: item.IOYM, IONO: item.IONO, CUSTCD: item.CUSTCD
    })
    if (resM.data && resM.data.length > 0) {
        Object.assign(masterData, resM.data[0])
    }

    // 2) 그리드 상세 (S2)
    const resG = await api.post('/api/hpio/HPIO_870U_STR', {
      ACTKIND: 'S2', CMPYCD: authStore.CMPYCD,
      OUTYMDFR: searchData.OUTYMDFR, OUTYMDTO: searchData.OUTYMDTO,
      LINECD: searchData.LINECD, SLIPYN: searchData.SLIPYN,
      IOYM: item.IOYM, IONO: item.IONO, CUSTCD: item.CUSTCD
    })
    grid?.setData(resG.data)
  } catch (e) { vAlertError('상세 정보 조회 실패') }
}

const handleApproval = async (kind: string) => {
    // 마감 체크
    const ioYmd = masterData.IOYMD.replace(/-/g, '')
    if (ioYmd <= closingInfo.CLSYMD) return vAlertError('회계 마감된 일자입니다.')
    if (ioYmd.substring(0, 6) <= closingInfo.SCLSYM) return vAlertError('영업 마감된 월입니다.')
    if (ioYmd.substring(0, 6) <= closingInfo.PCLSYM) return vAlertError('생산 마감된 월입니다.')

    const msg = kind === 'U0' ? '승인 처리하시겠습니까?' : '승인 취소하시겠습니까?'
    if (!confirm(msg)) return

    try {
        await api.post('/api/hpio/HPIO_870U_STR', {
            ACTKIND: kind,
            CMPYCD: authStore.CMPYCD,
            LINECD: searchData.LINECD,
            PROGCD: masterData.PROGCD,
            SV_SLIPYN: kind === 'D0' ? 'N' : 'Y',
            IOGBN: '200',
            PRODCD: '200',
            IOYM: masterData.IOYM.replace(/-/g, ''),
            IONO: masterData.IONO,
            CUSTCD: selectedItem.value.CUSTCD,
            USERID: masterData.USERID || authStore.USER_ID
        })
        vAlert('정상적으로 처리되었습니다.')
        fetchList()
    } catch (e) { vAlertError('처리 중 오류 발생') }
}

const printWindow = () => {
    if (!masterData.IONO) return
    const url = `../HPIO/HPIO_REQIN_PRINT.asp?PRTGU=Print&IOYM=${masterData.IOYM.replace(/-/g, '')}&IONO=${masterData.IONO}&CUSTCD=${selectedItem.value.CUSTCD}`
    window.open(url, '입고의뢰서인쇄', 'width=700,height=600,scrollbars=yes')
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { OUTYMDFR: initYMD, OUTYMDTO: initYMD, LINECD: '010', LINENM: '통합라인', SLIPYN: 'N' })
  resultList.value = []; selectedItem.value = null; Object.assign(masterData, { USERNM: authStore.USER_NM, USERID: authStore.USER_ID }); grid?.clearData()
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let config: any = {}
  if (type === 'LINE') {
    config = { title: '라인 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: 'L0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '라인명', field: 'CDNM', width: 150 }], onConfirm: (data: any) => { searchData.LINECD = data.CODE; searchData.LINENM = data.CDNM } }
  } else if (type === 'USER') {
    config = { title: '담당자 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM', data: { GUBUN: 'U1', CMPYCD: authStore.CMPYCD }, columns: [{ title: 'ID', field: 'CODE', width: 100 }, { title: '성명', field: 'CDNM', width: 150 }], onConfirm: (data: any) => { masterData.USERID = data.CODE; masterData.USERNM = data.CDNM } }
  }
  Object.assign(modalProps, config); modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      closingInfo.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(r.data[0])[1]).trim()
      closingInfo.PCLSYM = String(Object.values(r.data[0])[2]).trim()
    }
  })
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hpio870u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 95px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; font-size: 12.5px; color: #333; }
.required::after { content: ' *'; color: #dc3545; }

.list-group-item { border: none; border-bottom: 1px solid #f1f3f5; transition: all 0.2s; cursor: pointer; }
.list-group-item:hover { background-color: #f8f9fa; }
.list-group-item.active { background-color: #eef2ff; color: #4361ee; border-left: 4px solid #4361ee; z-index: 1; }

:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
</style>
