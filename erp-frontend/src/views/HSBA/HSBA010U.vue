<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba010u-wrapper d-flex flex-column h-100 bg-white p-0 overflow-hidden">
    <!-- 🚀 1. 상단 액션 바 (고정) -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-tags-fill me-2 text-primary"></i>
        기본정보 > 품목관리 > <span class="text-primary fw-bolder">품목등록 (HSBA010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-y-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-1 bg-light-subtle">
          <div class="d-flex align-items-center gap-3">
            <div class="input-group input-group-sm flex-nowrap" style="width: 250px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">재고자산</span>
              <select v-model="searchData.SCH_ASTKIND" class="form-select border-0 bg-white" @change="handleAssetChange">
                <option v-for="opt in assetOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
              </select>
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width: 350px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">품목검색</span>
              <input v-model="searchData.SCH_ITEMNM" type="text" class="form-control border-0 bg-white" placeholder="품목명 또는 코드" @keyup.enter="search" />
              <button class="btn btn-dark btn-sm" @click="search"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 🅱️ 품목 상세 정보 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f8f9fa;">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1"></i> 품목 상세 정보</span>
          <div class="d-flex gap-2 align-items-center">
            <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 중</span>
            <span v-else class="badge bg-primary" style="font-size: 10px;">신규 등록</span>
          </div>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 75px;" /><col />
              <col style="width: 75px;" /><col />
              <col style="width: 75px;" /><col />
              <col style="width: 75px;" /><col />
              <col style="width: 75px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">품목코드</th>
                <td><input v-model="masterData.ITEMCD" type="text" class="form-control form-control-sm text-center fw-bold text-primary" maxlength="7" :readonly="masterData.ACTKIND === 'U0'" /></td>
                <th class="required">품목명</th>
                <td><input v-model="masterData.ITEMNM" type="text" class="form-control form-control-sm" /></td>
                <th class="required">규격</th>
                <td><input v-model="masterData.ITSIZE" type="text" class="form-control form-control-sm" /></td>
                <th class="required">단위</th>
                <td>
                  <select v-model="masterData.UNIT" class="form-select form-select-sm">
                    <option v-for="opt in unitOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">재고자산</th>
                <td>
                  <select v-model="masterData.ASTKIND" class="form-select form-select-sm">
                    <option v-for="opt in assetOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>영문명</th>
                <td><input v-model="masterData.ITEMENM" type="text" class="form-control form-control-sm" /></td>
                <th>적정재고</th>
                <td><input v-model="masterData.STOCK" type="number" class="form-control form-control-sm text-end" /></td>
                <th>대분류</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.AGRPNM" type="text" class="form-control bg-light" readonly />
                    <button class="btn btn-outline-secondary btn-sm px-1" @click="openHelp('AGRP')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>중분류</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.BGRPNM" type="text" class="form-control bg-light" readonly />
                    <button class="btn btn-outline-secondary btn-sm px-1" @click="openHelp('BGRP')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>바코드</th>
                <td><input v-model="masterData.BARCODE" type="text" class="form-control form-control-sm" /></td>
              </tr>
              <tr>
                <th>HS Code</th>
                <td><input v-model="masterData.HSCODE" type="text" class="form-control form-control-sm" /></td>
                <th>제조사</th>
                <td><input v-model="masterData.MAKER" type="text" class="form-control form-control-sm" /></td>
                <th class="required">매입처</th>
                <td colspan="3">
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.IN_CUSTCD" type="text" class="form-control text-center fw-bold" style="max-width: 80px;" placeholder="코드" readonly />
                    <input v-model="masterData.IN_CUSTNM" type="text" class="form-control bg-light" placeholder="거래처명을 선택하세요" readonly />
                    <button class="btn btn-outline-secondary btn-sm px-1" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>사용/세트</th>
                <td>
                  <div class="d-flex gap-2">
                    <div class="form-check form-switch m-0"><input v-model="masterData.USEYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="swUse"> <label class="small fw-bold" for="swUse">사용</label></div>
                    <div class="form-check form-switch m-0"><input v-model="masterData.SETYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="swSet"> <label class="small fw-bold" for="swSet">세트</label></div>
                  </div>
                </td>
              </tr>
              <tr>
                <th>매입단위</th>
                <td>
                  <select v-model="masterData.INUNIT" class="form-select form-select-sm">
                    <option v-for="opt in unitOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>매입환산</th>
                <td><input v-model="masterData.INQTY" type="number" class="form-control form-control-sm text-end fw-bold" /></td>
                <th>매출단위</th>
                <td>
                  <select v-model="masterData.OUTUNIT" class="form-select form-select-sm">
                    <option v-for="opt in unitOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>매출환산</th>
                <td><input v-model="masterData.OUTQTY" type="number" class="form-control form-control-sm text-end fw-bold" /></td>
                <th>비고</th>
                <td colspan="1"><input v-model="masterData.REMARK" type="text" class="form-control form-control-sm w-100" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 품목 리스트 -->
      <div class="card border shadow-sm erp-main-grid overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 품목 리스트</span>
          <span class="text-muted" style="font-size: 11px;">※ 행 클릭 시 상단에 정보가 로드됩니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement" style="height: 100%;"></div>
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

const searchData = reactive({ SCH_ASTKIND: '120', SCH_ITEMNM: '' })
const masterData = reactive<any>({
  ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, ASTKIND: '120', ITEMCD: '',
  ITSIZE: '', UNIT: 'EA', ITEMNM: '', ITEMENM: '',
  INUNIT: 'EA', INQTY: 1, OUTUNIT: 'EA', OUTQTY: 1, SETYN: 'N',
  AGRPCD: '', AGRPNM: '', BGRPCD: '', BGRPNM: '',
  STOCK: 0, QTYPNT: 0, VATYN: 'Y', SOTAXYN: 'N', UDOGYN: 'N',
  BARCODE: '', HSCODE: '', REMARK: '', USEYN: 'Y',
  IN_CUSTCD: '', IN_CUSTNM: '', MAKER: '', UPDEMP: authStore.USERID
})

const assetOptions = ref<any[]>([]); const unitOptions = ref<any[]>([])
const gridElement = ref<HTMLElement | null>(null); const grid = ref<Tabulator | null>(null); const activeItemCount = ref(0)

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 15,
    paginationButtonCount: 5, paginationSizeSelector: [15, 30, 50],
    placeholder: "데이터가 없습니다.", columnDefaults: { headerSort: false },
    columns: [
      { title: "코드", field: "ITEMCD", width: 100, hozAlign: "center", cssClass: "fw-bold text-primary" },
      { title: "품목명", field: "ITEMNM", minWidth: 200 },
      { title: "규격", field: "ITSIZE", width: 180 },
      { title: "단위", field: "UNIT", width: 100, hozAlign: "center" },
      { title: "대분류", field: "AGRPNM", width: 200 },
      { title: "중분류", field: "BGRPNM", width: 200 },
      { title: "사용", field: "USEYN", width: 100, hozAlign: "center", formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
    ]
  })
  grid.value.on("rowClick", (e, row) => {
    const data = row.getData();
    const cleanedData: any = {};
    Object.keys(data).forEach(key => {
      cleanedData[key] = typeof data[key] === 'string' ? data[key].trim() : data[key];
    });
    Object.assign(masterData, cleanedData);
    masterData.IN_CUSTNM = cleanedData.CUSTNM || cleanedData.IN_CUSTNM || '';
    masterData.ACTKIND = 'U0';
  })
}

async function fetchOptions() {
  try {
    const p1 = api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '100' } })
    const p2 = api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'U0', CMPYCD: authStore.CMPYCD, GBNCD: '', CODE: '' } })
    const [r1, r2] = await Promise.all([p1, p2])
    assetOptions.value = r1.data.map((i: any) => ({ CODECD: String(i.CODE || i.CODECD || '').trim(), CODENM: String(i.CDNM || i.CODENM || '').trim() }))
    unitOptions.value = r2.data.map((i: any) => ({ CODECD: String(i.UNIT || '').trim(), CODENM: String(i.UNITNM || '').trim() }))
  } catch (e) { console.error('코드 로드 실패', e) }
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_010U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, ASTKIND: searchData.SCH_ASTKIND, ITEMNM: searchData.SCH_ITEMNM || '',
      ITEMCD: '', ITSIZE: '', UNIT: '', ITEMENM: '', INUNIT: '', INQTY: 0, OUTUNIT: '', OUTQTY: 0, SETYN: '',
      AGRPCD: '', BGRPCD: '', IMPRICE: 0, OMPRICE: 0, STOCK: 0, QTYPNT: 0, VATYN: '', SOTAXYN: '', UDOGYN: '',
      BARCODE: '', HSCODE: '', REMARK: '', USEYN: '', IN_CUSTCD: '', MAKER: '', UPDEMP: authStore.USERID
    })
    if (grid.value) { grid.value.setData(res.data || []); activeItemCount.value = res.data.length }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.ITEMNM || !masterData.ITSIZE) return vAlertError('품목명과 규격은 필수입니다.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const res = await api.post('/api/hsba/HSBA_010U_STR', { ...masterData, USERID: authStore.USERID })
    if (res.data?.[0]?.RESULT === '0000000') { vAlertError(res.data[0].MSG || '저장 실패') }
    else { vAlert('저장되었습니다.'); search(); initialize() }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  const currentAsset = searchData.SCH_ASTKIND; resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, UNIT: 'EA', ASTKIND: currentAsset,
    SETYN: 'N', USEYN: 'Y', STOCK: 0, QTYPNT: 0, IMPRICE: 0, OMPRICE: 0,
    VATYN: 'Y', SOTAXYN: 'N', UDOGYN: 'N', INUNIT: 'EA', INQTY: 1, OUTUNIT: 'EA', OUTQTY: 1,
    UPDEMP: authStore.USERID, IN_CUSTCD: '', IN_CUSTNM: '', AGRPCD: '', AGRPNM: '', BGRPCD: '', BGRPNM: '', MAKER: ''
  })
}

const handleAssetChange = () => { initialize(); search(); }

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let config: any = {}
  if (type === 'AGRP') {
    config = {
      title: '대분류 선택',
      path: '/api/hs00/HS00_000S_STR',
      data: { GUBUN: 'G0', GBNCD: masterData.ASTKIND },
      field: 'AGRPNM',
      columns: [{ title: '코드', field: 'AGRPCD', width: 80, headerHozAlign: 'center', hozAlign: 'center' }, { title: '분류명', field: 'AGRPNM', width: 180 }]
    }
  } else if (type === 'BGRP') {
    config = {
      title: '중분류 선택',
      path: '/api/hs00/HS00_000S_STR',
      data: { GUBUN: 'G1', GBNCD: masterData.ASTKIND, CODE: masterData.AGRPCD },
      field: 'BGRPNM',
      columns: [{ title: '코드', field: 'BGRPCD', width: 80, headerHozAlign: 'center', hozAlign: 'center' }, { title: '분류명', field: 'BGRPNM', width: 180 }]
    }
  } else if (type === 'CUST') {
    config = {
      title: '거래처 선택',
      path: '/api/ha00/HA00_00P_STR',
      data: { GUBUN: 'C4' },
      field: 'CUSTNM',
      columns: [{ title: '코드', field: 'CUSTCD', width: 100, headerHozAlign: 'center', hozAlign: 'center' }, { title: '거래처명', field: 'CUSTNM', width: 200 }]
    }
  }

  if (!config.path) return
  Object.assign(modalProps, {
    title: config.title, path: config.path, defaultField: config.field,
    data: { ...config.data, CMPYCD: authStore.CMPYCD }, columns: config.columns,
    onConfirm: (data: any) => {
      if (type === 'AGRP') { masterData.AGRPCD = (data.AGRPCD || data.CODE || '').trim(); masterData.AGRPNM = (data.AGRPNM || data.CDNM || '').trim(); }
      else if (type === 'BGRP') { masterData.BGRPCD = (data.BGRPCD || data.CODE || '').trim(); masterData.BGRPNM = (data.BGRPNM || data.CDNM || '').trim(); }
      else if (type === 'CUST') { masterData.IN_CUSTCD = (data.CUSTCD || data.CODE || '').trim(); masterData.IN_CUSTNM = (data.CUSTNM || data.CDNM || '').trim(); }
    }
  })
  modalVisible.value = true
}

onMounted(async () => { await fetchOptions(); nextTick(() => { initGrid(); search() }) })
</script>

<style scoped>
.hsba010u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

/* 💡 [Haion ERP 표준 레이아웃 CSS] */
.flex-shrink-0 { flex-shrink: 0 !important; }
.flex-grow-1 { flex-grow: 1 !important; min-height: 0 !important; }
.overflow-hidden { overflow: hidden !important; }

/* 🚀 표준 그리드 높이 고정 (다른 프로그램도 동일 적용) */
.erp-main-grid {
    height: 500px !important;
    flex-shrink: 0 !important;
}

.erp-table-full { width: 100%; border-collapse: collapse; border: 1px solid #e9ecef; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 4px 5px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 2px 4px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }

/* 🚀 팝업(Modal) 가독성 표준 스타일 */
:deep(.modal-content) { background-color: #ffffff !important; }
:deep(.modal-content .tabulator) { background-color: #ffffff !important; color: #000000 !important; border: 1px solid #dee2e6 !important; }
:deep(.modal-content .tabulator-header) { background-color: #f1f3f5 !important; color: #000000 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.modal-content .tabulator-cell) { color: #000000 !important; border-right: 1px solid #eeeeee !important; font-size: 13px !important; padding: 8px !important; }
:deep(.modal-content .tabulator-col-title) { color: #000000 !important; font-weight: 800 !important; }
:deep(.modal-content .tabulator-row:hover) { background-color: #e9ecef !important; }
:deep(.modal-content .tabulator-placeholder span) { color: #555555 !important; font-weight: bold; }
</style>
