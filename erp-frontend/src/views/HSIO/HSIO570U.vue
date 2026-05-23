<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio570u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-right me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        재고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">타계정출고 (HSIO570U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMaster">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-danger" @click="deleteOrder" :disabled="!masterData.IONO || masterData.IONO === '0000'">삭제</button>
        <button class="btn-erp btn-outline-secondary" @click="print('Print')" :disabled="!masterData.IONO || masterData.IONO === '0000'">인수증</button>
        <button class="btn-erp btn-outline-secondary" @click="print('Print1')" :disabled="!masterData.IONO || masterData.IONO === '0000'">출고의뢰</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-search me-1"></i> 조회 조건</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">출고부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="searchData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="handleOpenHelp('SEARCH_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('SEARCH_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">출고번호</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 200px;">
                    <input v-model="uiSEARCH_YM" type="month" class="form-control form-control-sm text-center fw-bold" style="width: 130px;" />
                    <input v-model="searchData.IONO" type="text" class="form-control form-control-sm text-center fw-bold text-primary" placeholder="0000" style="width: 60px;" maxlength="4" />
                  </div>
                </td>
                <td class="text-end pe-3">
                  <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary" @click="movePrevNext('P')"><i class="bi bi-chevron-left"></i></button>
                    <button class="btn btn-outline-primary" @click="movePrevNext('N')"><i class="bi bi-chevron-right"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 마스터 정보 영역 (등록 영역) -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 출고 기본 정보 (등록용)</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">출고부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.DEPTNM" type="text" class="form-control" @keyup.enter="handleOpenHelp('MASTER_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('MASTER_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">출고번호</th>
                <td>
                  <div class="d-flex align-items-center gap-1" style="width: 200px;">
                    <input v-model="uiIOYM" type="month" class="form-control form-control-sm text-center fw-bold bg-light" style="width: 130px;" readonly />
                    <input v-model="masterData.IONO" type="text" class="form-control form-control-sm text-center fw-bold text-primary bg-light" style="width: 60px;" readonly />
                  </div>
                </td>
                <th class="required">출고일자</th>
                <td>
                  <input v-model="uiIOYMD" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
                <th class="required">출고창고</th>
                <td>
                  <select v-model="masterData.WHCD" class="form-select form-select-sm" style="width: 120px;">
                    <option v-for="opt in whOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>특기사항</th>
                <td colspan="7">
                  <input v-model="masterData.REMARK" type="text" class="form-control form-control-sm w-100" placeholder="타계정출고 특기사항 입력" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 출고 품목 상세</span>
          <button class="btn btn-xs btn-primary fw-bold" @click="addRow">
            <i class="bi bi-plus-lg me-1"></i> 행추가
          </button>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="me-4 small opacity-75">총 수량: <span class="fw-bold text-info ms-1">{{ formatNumber(qtyTotal) }}</span></span>
          <span class="fs-5 ms-2 fw-light">총 금액: <span class="fw-bold text-white ms-2">{{ formatNumber(amtTotal) }}</span></span>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
    <ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" :astKind="String(masterData.ASTKIND || '2')" @close="itemHelpVisible = false" @confirm="onSelectItem" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import ItemHelpModal from '@/components/ItemHelpModal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const itemHelpVisible = ref(false)
const currentTargetRow = ref<any>(null)

const now = new Date()
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`
const initYMD = `${initYM}${String(now.getDate()).padStart(2, '0')}`

// 1. 상태 관리
const searchData = reactive({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  IOYM: initYM, IONO: '0000', ACTKIND: 'S'
})

const masterData = reactive<any>({
  ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOYM: initYM, IONO: '0000',
  IOYMD: initYMD, DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  WHCD: '', REMARK: '', INNO: '', SLIPYMD: '', SLIPNO: '', CLSYMD: '', SCLSYM: '', ASTKIND: '2'
})

const uiSEARCH_YM = computed({ get: () => formatDate(searchData.IOYM, 'YM'), set: (v) => searchData.IOYM = v.replace(/-/g, '') })
const uiIOYM = computed({ get: () => formatDate(masterData.IOYM, 'YM'), set: (v) => masterData.IOYM = v.replace(/-/g, '') })
const uiIOYMD = computed({ get: () => formatDate(masterData.IOYMD, 'YMD'), set: (v) => masterData.IOYMD = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)
const qtyTotal = ref(0)
const amtTotal = ref(0)

const whOptions = ref<any[]>([])

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "등록된 품목이 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      {
        title: "상태", field: "UPKIND", width: 50, hozAlign: "center",
        formatter: (cell) => {
          const v = cell.getValue();
          if (v === 'A') return '<i class="bi bi-plus-circle-fill text-primary"></i>';
          if (v === 'U') return '<i class="bi bi-pencil-square text-warning"></i>';
          if (v === 'D') return '<i class="bi bi-dash-circle-fill text-danger"></i>';
          return '';
        }
      },
      {
        title: "품목코드", field: "ITEMCD", width: 100,
        formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>",
        cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) handleOpenHelp('ITEM', cell.getRow()) }
      },
      { title: "품목명", field: "ITEMNM", minWidth: 200,
        formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>",
        cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) handleOpenHelp('ITEM', cell.getRow()) }
      },
      { title: "규격", field: "ITSIZE", width: 120 },
      { title: "단위", field: "UNIT", width: 60, hozAlign: "center" },
      { title: "수량", field: "IOQTY", width: 100, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 } },
      { title: "금액", field: "IOAMT", width: 120, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 } },
      {
        title: "유형", field: "IOTYPENM", width: 150,
        formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>",
        cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) handleOpenHelp('IOTYPE', cell.getRow()) }
      },
      {
        title: "사용부서", field: "IDEPTNM", width: 130,
        formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>",
        cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) handleOpenHelp('IDEPT', cell.getRow()) }
      },
      {
        title: "거래처", field: "SCUSTNM", width: 180,
        formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>",
        cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) handleOpenHelp('SCUST', cell.getRow()) }
      },
      {
        title: "", width: 40, hozAlign: "center",
        formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>",
        cellClick: (e, c) => {
          const row = c.getRow();
          if (row.getData().UPKIND === 'A') row.delete();
          else {
            row.update({ UPKIND: row.getData().UPKIND === 'D' ? 'U' : 'D' });
            const isD = row.getData().UPKIND === 'D';
            row.getElement().style.textDecoration = isD ? 'line-through' : 'none';
            row.getElement().style.opacity = isD ? '0.5' : '1';
          };
          updateTotals()
        }
      }
    ]
  })

  grid.value.on("cellEdited", (cell: any) => {
    const row = cell.getRow();
    if (row.getData().UPKIND !== 'A') row.update({ UPKIND: 'U' });
    updateTotals();
  })
}

const updateTotals = () => {
  if (!grid.value) return
  const data = grid.value.getData().filter((i: any) => i.UPKIND !== 'D')
  activeItemCount.value = data.length
  qtyTotal.value = data.reduce((acc, cur) => acc + (Number(cur.IOQTY) || 0), 0)
  amtTotal.value = data.reduce((acc, cur) => acc + (Number(cur.IOAMT) || 0), 0)
}

// 3. 기능 구현
async function fetchMaster() {
  if (!searchData.DEPTCD || !searchData.IOYM || !searchData.IONO) return vAlertError('조회 조건을 확인하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_570U_STR', {
      ACTKIND: 'S', CMPYCD: authStore.CMPYCD, GUBUN: '200',
      IOYM: searchData.IOYM, IONO: searchData.IONO, DEPTCD: searchData.DEPTCD
    })
    if (res.data?.length) {
      Object.assign(masterData, res.data[0])
      fetchDetail()
    } else {
      vAlert('조회된 데이터가 없습니다.')
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetail() {
  try {
    const res = await api.post('/api/hsio/HSIO_571U_STR', {
      ACTKIND: 'S', CMPYCD: authStore.CMPYCD, GUBUN: '200',
      IOYM: masterData.IOYM, IONO: masterData.IONO
    })
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => ({
        ...i,
        UPKIND: 'U',
        IDEPTCD: i.USEDEPT, IDEPTNM: i.USEDEPTNM,
        SCUSTCD: i.CUSTCD, SCUSTNM: i.CUSTNM,
        IOAMT: i.IOAMT_T
      })))
      setTimeout(updateTotals, 100)
    }
  } catch (e) { vAlertError('상세 로드 실패') }
}

async function save() {
  if (!masterData.DEPTCD || !masterData.IOYMD || !masterData.WHCD) return vAlertError('필수 정보를 입력하세요.')
  if (masterData.IOYMD <= masterData.CLSYMD) return vAlertError('회계 마감된 일자입니다.')
  if (masterData.SLIPNO > '000') return vAlertError('전표가 발행된 건은 수정할 수 없습니다.')

  const details = grid.value?.getData().filter((i: any) => i.ITEMCD)
  if (!details || details.length === 0) return vAlertError('품목을 추가해 주십시오.')

  try {
    const actKind = (masterData.IONO === '0000' || !masterData.IONO) ? 'A' : 'U'
    // 1. 마스터 저장
    const mRes = await api.post('/api/hsio/HSIO_570U_STR', {
      ...masterData, ACTKIND: actKind, GUBUN: '200', USERID: authStore.USERID
    })

    if (mRes.data?.length) {
      const newIoYm = mRes.data[0].IOYM || masterData.IOYM
      const newIoNo = mRes.data[0].IONO || masterData.IONO

      // 2. 상세 저장
      for (const item of details) {
        await api.post('/api/hsio/HSIO_571U_STR', {
          ...item,
          ACTKIND: item.UPKIND || 'A',
          CMPYCD: authStore.CMPYCD, GUBUN: '200',
          IOYM: newIoYm, IONO: newIoNo, DEPTCD: masterData.DEPTCD,
          WHCD: masterData.WHCD, IOYMD: masterData.IOYMD,
          USERID: authStore.USERID
        })
      }
      vAlert('저장완료')
      searchData.IOYM = newIoYm
      searchData.IONO = newIoNo
      fetchMaster()
    }
  } catch (e) { vAlertError('저장 실패') }
}

async function deleteOrder() {
  if (!confirm('정말로 삭제하시겠습니까?')) return
  try {
    await api.post('/api/hsio/HSIO_570U_STR', {
      ACTKIND: 'D', CMPYCD: authStore.CMPYCD, GUBUN: '200',
      IOYM: masterData.IOYM, IONO: masterData.IONO, DEPTCD: masterData.DEPTCD
    })
    vAlert('삭제되었습니다.')
    initialize()
  } catch (e) { vAlertError('삭제 실패') }
}

function movePrevNext(gbn: string) {
  searchData.ACTKIND = gbn
  fetchMaster()
}

function print(type: string) {
  // 인쇄 로직 구현
}

function initialize() {
  const clsy = masterData.CLSYMD; const sclsy = masterData.SCLSYM
  resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOYM: initYM, IONO: '0000',
    IOYMD: initYMD, DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
    CLSYMD: clsy, SCLSYM: sclsy, ASTKIND: '2'
  })
  searchData.IONO = '0000'
  if (grid.value) grid.value.clearData()
  updateTotals()
}

function addRow() {
  if (grid.value) {
    grid.value.addRow({ UPKIND: 'A', ITEMCD: '', ITEMNM: '', ITSIZE: '', UNIT: '', IOQTY: 0, IOAMT: 0, IOTYPE: '', IOTYPENM: '', IDEPTCD: '', IDEPTNM: '', SCUSTCD: '', SCUSTNM: '' })
    updateTotals()
  }
}

// 4. 팝업 및 기초정보
function handleOpenHelp(type: string, target?: any) {
  if (type === 'SEARCH_DEPT') {
    openHelp('DEPT', (d) => { searchData.DEPTCD = d.DEPTCD; searchData.DEPTNM = d.DEPTNM });
  } else if (type === 'MASTER_DEPT') {
    openHelp('DEPT', (d) => { masterData.DEPTCD = d.DEPTCD; masterData.DEPTNM = d.DEPTNM });
  } else if (type === 'ITEM') {
    currentTargetRow.value = target;
    itemHelpVisible.value = true;
  } else if (type === 'IOTYPE') {
    Object.assign(modalProps, {
      title: '유형 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CDNM',
      data: { GUBUN: 'E0', GBNCD: '130', CMPYCD: authStore.CMPYCD },
      columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '유형명', field: 'CDNM', width: 180 }],
      onConfirm: (d: any) => { target.update({ IOTYPE: d.CODE, IOTYPENM: d.CDNM }) }
    });
    modalVisible.value = true;
  } else if (type === 'IDEPT') {
    openHelp('DEPT', (d) => { target.update({ IDEPTCD: d.DEPTCD, IDEPTNM: d.DEPTNM }) });
  } else if (type === 'SCUST') {
    openHelp('CUST', (d) => { target.update({ SCUSTCD: d.CUSTCD, SCUSTNM: d.CUSTNM }) });
  }
}

const onSelectItem = (d: any) => {
  if (!currentTargetRow.value) return
  currentTargetRow.value.update({ ITEMCD: d.ITEMCD, ITEMNM: d.ITEMNM, ITSIZE: d.ITSIZE, UNIT: d.UNIT || d.UNITNM, IOAMT: d.OUTCOST || d.PRICE || 0 });
  itemHelpVisible.value = false; updateTotals();
}

const formatDate = (v: any, type: string) => {
  if (!v || v.length < 6) return v
  if (type === 'YM') return `${v.substring(0, 4)}-${v.substring(4, 6)}`
  if (type === 'YMD' && v.length === 8) return `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}`
  return v
}
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => {
    if (r.data?.length) {
      masterData.CLSYMD = String(Object.values(r.data[0])[0]).trim()
      masterData.SCLSYM = String(Object.values(r.data[0])[1]).trim()
    }
  })
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } }).then(r => whOptions.value = r.data.map((i: any) => ({ CODECD: i.CODE, CODENM: i.CDNM })))
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hsio570u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

/* 🚀 입력 필드 글자 크기 및 높이 최적화 (표준) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12px; padding: 6px 10px !important; color: #495057;
  white-space: nowrap;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }

:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }
</style>

