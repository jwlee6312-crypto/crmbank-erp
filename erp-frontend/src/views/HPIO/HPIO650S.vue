<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpio650s-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark- medical me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목별 수불현황 (HPIO650S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchList">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-excel" @click="exportExcel">
          <i class="bi bi-file-earmark-excel"></i> 엑셀
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
              <col style="width: 90px;"><col>
              <col style="width: 90px;"><col>
              <col style="width: 90px;"><col>
            </colgroup>
            <tbody>
              <tr>
                <th class="required">입출일자</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="uiFYMD" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="uiTYMD" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th class="required">창&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td>
                  <select v-model="searchData.WHCD" class="form-select form-select-sm" style="width: 150px;">
                    <option value="000">전체</option>
                    <option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
                  </select>
                </td>
                <th class="required">자&nbsp;&nbsp;&nbsp;&nbsp;재</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 450px;">
                    <input v-model="searchData.ITEMCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="searchData.ITEMNM" type="text" class="form-control" placeholder="품목 선택" @keyup.enter="openHelp('ITEM')" />
                    <input v-model="searchData.ITSIZE" type="text" class="form-control bg-light" style="max-width: 100px;" readonly />
                    <input v-model="searchData.UNIT" type="text" class="form-control bg-light text-center" style="max-width: 50px;" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('ITEM')"><i class="bi bi-search"></i></button>
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
          <span class="fw-bold small text-dark"><i class="bi bi-list-check me-1 text-primary"></i> 품목별 상세 입출고 이력</span>
          <span v-if="searchData.ITEMNM" class="badge bg-info px-3">{{ searchData.ITEMNM }} ({{ searchData.ITEMCD }})</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-3 small">조회건수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-9 text-end text-muted small">
          <i class="bi bi-info-circle me-1"></i> 적요 항목을 클릭하면 해당 데이터의 상세 등록 화면으로 이동할 수 있습니다.
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
const route = useRoute()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const initTYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initFYMD = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  FYMD: initFYMD,
  TYMD: initTYMD,
  WHCD: '000',
  ITEMCD: '', ITEMNM: '', ITSIZE: '', UNIT: '',
  ASTKIND: ''
})

const whOptions = ref<any[]>([])

const uiFYMD = computed({ get: () => formatDateString(searchData.FYMD, '-'), set: (v) => { if (v) searchData.FYMD = v.replace(/-/g, '') } })
const uiTYMD = computed({ get: () => formatDateString(searchData.TYMD, '-'), set: (v) => { if (v) searchData.TYMD = v.replace(/-/g, '') } })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

// 🏭 기초 코드 로드
const fetchWhOptions = async () => {
  try {
    const res = await api.get('/api/comm/codes/WH')
    whOptions.value = res.data
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
        { title: "일 자", field: "IOYMD", width: 110, hozAlign: "center", formatter: (c) => formatDateString(c.getValue(), '.') },
        {
          title: "적 요 (거래처/내역)", field: "CUSTNM", minWidth: 300,
          formatter: (cell) => {
              const d = cell.getData();
              let isLinkable = false;
              if (d.IOGBN === "200" && d.IOTYPE >= "300" && d.IOTYPE <= "390" && d.IOTYPE !== "380") isLinkable = true;
              else if (d.IOGBN === "200" && d.IOTYPE === "380") isLinkable = true;
              else if (d.IOGBN === "100" && d.GUBUN === "1" && d.IOTYPE <= "190") isLinkable = true;

              return isLinkable ? `<span class="text-primary text-decoration-underline cursor-pointer">${cell.getValue() || ''}</span>` : `<span>${cell.getValue() || ''}</span>`;
          },
          cellClick: (e, cell) => {
              const d = cell.getData();
              if (d.IOGBN === "200" && d.IOTYPE >= "300" && d.IOTYPE <= "390" && d.IOTYPE !== "380") {
                  router.push({ path: '/HPIO510U', query: { IOYM: d.IOYM, IONO: d.IONO, DEPTCD: d.DEPTCD } });
              } else if (d.IOGBN === "200" && d.IOTYPE === "380") {
                  router.push({ path: '/HSIO570U', query: { IOYM: d.IOYM, IONO: d.IONO, DEPTCD: d.DEPTCD } });
              } else if (d.IOGBN === "100" && d.GUBUN === "1" && d.IOTYPE <= "190") {
                  router.push({ path: '/HSIO100U', query: { IOYM: d.IOYM, IONO: d.IONO, DEPTCD: d.DEPTCD } });
              }
          },
          bottomCalc: () => "합 계", bottomCalcHozAlign: "center"
        },
        { title: "입출구분", field: "IOTYPENM", width: 100, hozAlign: "center" },
        { title: "입 고", field: "INQTY", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 }, bottomCalc: "sum" },
        { title: "출 고", field: "OUTQTY", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any) => c.getData().QTYPNT || 0 }, bottomCalc: "sum" },
        {
          title: "재 고", field: "STKQTY", width: 110, hozAlign: "right",
          formatter: (cell) => {
              const val = cell.getValue();
              return `<span class="${Number(val) < 0 ? 'text-danger' : ''} fw-bold">${formatNumber(val, cell.getData().QTYPNT)}</span>`;
          }
        },
        { title: "창 고", field: "WHNM", width: 120, hozAlign: "center" },
        { title: "비 고", field: "PKUNITNM", minWidth: 100 }
      ],
    })
  }
}

// 3. 비즈니스 로직
const fetchList = async () => {
  if (!searchData.ITEMCD) return vAlertError('품목을 선택하세요.')

  try {
    const res = await api.post('/api/hpio/HPIO_650S_STR', {
      CMPYCD: authStore.CMPYCD,
      WHCD: searchData.WHCD,
      FYMD: searchData.FYMD,
      TYMD: searchData.TYMD,
      ASTKIND: searchData.ASTKIND || '',
      ITEMCD: searchData.ITEMCD
    })

    // 누적 재고 계산 (Running Total)
    let runningStock = 0;
    const mapped = res.data.map((item: any) => {
        runningStock = runningStock + Number(item.INQTY || 0) - Number(item.OUTQTY || 0);
        return { ...item, STKQTY: runningStock };
    });

    grid?.setData(mapped)
    itemCount.value = mapped.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 처리 중 오류 발생') }
}

const initialize = () => {
  resetForm(searchData)
  Object.assign(searchData, { FYMD: initFYMD, TYMD: initTYMD, WHCD: '000', ITEMCD: '', ITEMNM: '', ITSIZE: '', UNIT: '' })
  grid?.clearData()
  itemCount.value = 0
}

const exportExcel = () => {
  grid?.download("xlsx", `품목수불_${searchData.ITEMNM}_${searchData.TYMD}.xlsx`, { title: "품목별 수불 현황" })
}

// 4. 도움창 (Modal)
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'ITEM') {
    Object.assign(modalProps, {
      title: '품목 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'ITEMNM',
      data: { GUBUN: 'I0', CMPYCD: authStore.CMPYCD, codegbn: 'B' },
      columns: [{ title: '코드', field: 'ITEMCD', width: 100 }, { title: '품목명', field: 'ITEMNM', width: 250 }, { title: '규격', field: 'ITSIZE', width: 120 }],
      onConfirm: (data: any) => {
          searchData.ITEMCD = data.ITEMCD;
          searchData.ITEMNM = data.ITEMNM;
          searchData.ITSIZE = data.ITSIZE;
          searchData.UNIT = data.UNIT;
          searchData.ASTKIND = data.ASTKIND;
          fetchList();
      }
    })
  }
  modalVisible.value = true
}

const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')
const formatNumber = (val: any, precision: any = 0) => new Intl.NumberFormat('ko-KR', { minimumFractionDigits: precision, maximumFractionDigits: precision }).format(Number(val) || 0)

onMounted(async () => {
  await fetchWhOptions()

  // 외부 파라미터 처리 (HPIO640S 등에서 넘어온 경우)
  if (route.query.ITEMCD) {
      Object.assign(searchData, {
          ITEMCD: String(route.query.ITEMCD),
          ASTKIND: String(route.query.ASTKIND || ''),
          WHCD: String(route.query.WHCD || '000'),
          FYMD: String(route.query.FYMD),
          TYMD: String(route.query.TYMD)
      })
      // 품목명칭 보완을 위한 API 호출이 필요할 수 있으나, 조회 로직에서 ITEMCD로 처리 가능
  }

  nextTick(() => { initGrid(); if(searchData.ITEMCD) fetchList(); })
})
</script>

<style scoped>
.hpio650s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-search:hover { background-color: #374fc7 !important; transform: translateY(-1px); }
.btn-excel { background-color: #107c41 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

/* Tabulator 스타일 */
:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-footer) { background-color: #f8f9fa !important; border-top: 2px solid #dee2e6 !important; font-weight: 800; }
.cursor-pointer { cursor: pointer; }
</style>
