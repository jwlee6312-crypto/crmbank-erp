<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsba010u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-tags-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        품목관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목등록 (HSBA010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
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
                <th style="width: 100px;">재고자산</th>
                <td style="width: 200px;">
                  <select v-model="searchData.SCH_ASTKIND" class="form-select form-select-sm" @change="initialize">
                    <option v-for="opt in assetOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th style="width: 100px;">품&nbsp;목&nbsp;명</th>
                <td>
                  <input v-model="searchData.SCH_ITEMNM" type="text" class="form-control form-control-sm w-50" placeholder="품목명 검색" @keyup.enter="search" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 마스터 정보 입력 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 품목 상세 정보</span>
          <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark ms-2">수정 모드</span>
          <span v-else class="badge bg-primary ms-2">신규 모드</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">품목코드</th>
                <td><input v-model="masterData.ITEMCD" type="text" class="form-control form-control-sm text-center fw-bold" maxlength="7" :readonly="masterData.ACTKIND === 'U0'" /></td>
                <th class="required">품&nbsp;목&nbsp;명</th>
                <td><input v-model="masterData.ITEMNM" type="text" class="form-control form-control-sm" maxlength="50" /></td>
                <th class="required">규&nbsp;&nbsp;&nbsp;&nbsp;격</th>
                <td><input v-model="masterData.ITSIZE" type="text" class="form-control form-control-sm" maxlength="30" /></td>
                <th class="required">단&nbsp;&nbsp;&nbsp;&nbsp;위</th>
                <td>
                  <select v-model="masterData.UNIT" class="form-select form-select-sm">
                    <option v-for="opt in unitOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="required">재고자산</th>
                <td>
                  <select v-model="masterData.ASTKIND" class="form-select form-select-sm">
                    <option v-for="opt in assetOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>영&nbsp;문&nbsp;명</th>
                <td><input v-model="masterData.ITEMENM" type="text" class="form-control form-control-sm" maxlength="50" /></td>
                <th>세트상품</th>
                <td>
                  <div class="form-check form-check-inline mb-0">
                    <input v-model="masterData.SETYN" class="form-check-input" type="checkbox" id="setYn" true-value="Y" false-value="N">
                    <label class="form-check-label small" for="setYn">세트</label>
                  </div>
                </td>
                <th>사용여부</th>
                <td>
                  <div class="form-check form-check-inline mb-0">
                    <input v-model="masterData.USEYN" class="form-check-input" type="checkbox" id="useYn010" true-value="Y" false-value="N">
                    <label class="form-check-label small" for="useYn010">사용</label>
                  </div>
                </td>
              </tr>
              <tr>
                <th>대&nbsp;분&nbsp;류</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.AGRPNM" type="text" class="form-control" placeholder="선택" @keyup.enter="openHelp('AGRP')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('AGRP')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>중&nbsp;분&nbsp;류</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.BGRPNM" type="text" class="form-control" placeholder="선택" @keyup.enter="openHelp('BGRP')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('BGRP')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>적정재고</th>
                <td><input v-model="masterData.STOCK" type="text" class="form-control form-control-sm text-end" /></td>
                <th>수량소수점</th>
                <td>
                  <select v-model="masterData.QTYPNT" class="form-select form-select-sm">
                    <option value="0">0자리</option>
                    <option value="1">1자리</option>
                    <option value="2">2자리</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>바&nbsp;코&nbsp;드</th>
                <td><input v-model="masterData.BARCODE" type="text" class="form-control form-control-sm" maxlength="20" /></td>
                <th>HS Code</th>
                <td><input v-model="masterData.HSCODE" type="text" class="form-control form-control-sm" maxlength="20" /></td>
                <th>제&nbsp;조&nbsp;사</th>
                <td><input v-model="masterData.MAKER" type="text" class="form-control form-control-sm" maxlength="20" /></td>
                <th>총&nbsp;중&nbsp;량</th>
                <td><input v-model="masterData.G_WEIGHT" type="text" class="form-control form-control-sm text-end" /></td>
              </tr>
              <tr>
                <th>매입단위</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.INUNITNM" type="text" class="form-control" placeholder="선택" @keyup.enter="openHelp('INUNIT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('INUNIT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>매입환산</th>
                <td><input v-model="masterData.INQTY" type="text" class="form-control form-control-sm text-end" /></td>
                <th>매입단가</th>
                <td><input v-model="masterData.IMPRICE" type="text" class="form-control form-control-sm text-end" /></td>
                <th>순&nbsp;중&nbsp;량</th>
                <td><input v-model="masterData.NET_WEIGHT" type="text" class="form-control form-control-sm text-end" /></td>
              </tr>
              <tr>
                <th>매출단위</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.OUTUNITNM" type="text" class="form-control" placeholder="선택" @keyup.enter="openHelp('OUTUNIT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('OUTUNIT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>매출환산</th>
                <td><input v-model="masterData.OUTQTY" type="text" class="form-control form-control-sm text-end" /></td>
                <th>매출단가</th>
                <td><input v-model="masterData.OMPRICE" type="text" class="form-control form-control-sm text-end" /></td>
                <th>부피(CBM)</th>
                <td><input v-model="masterData.CBM" type="text" class="form-control form-control-sm text-end" /></td>
              </tr>
              <tr>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td colspan="3"><input v-model="masterData.REMARK" type="text" class="form-control form-control-sm w-100" maxlength="50" /></td>
                <th>LOCATION</th>
                <td colspan="3">
                  <div class="d-flex align-items-center gap-1 small">
                    존<select v-model="masterData.JONECD" class="form-select form-select-xs" style="width:60px"><option value="">선택</option><option v-for="o in joneOptions" :key="o.CODECD" :value="o.CODECD">{{o.CODENM}}</option></select>
                    행<select v-model="masterData.ROWCD" class="form-select form-select-xs" style="width:60px"><option value="">선택</option><option v-for="o in rowOptions" :key="o.CODECD" :value="o.CODECD">{{o.CODENM}}</option></select>
                    열<select v-model="masterData.COLCD" class="form-select form-select-xs" style="width:60px"><option value="">선택</option><option v-for="o in colOptions" :key="o.CODECD" :value="o.CODECD">{{o.CODENM}}</option></select>
                    단<select v-model="masterData.DANCD" class="form-select form-select-xs" style="width:60px"><option value="">선택</option><option v-for="o in danOptions" :key="o.CODECD" :value="o.CODECD">{{o.CODENM}}</option></select>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 품목 목록</span>
          <div class="d-flex align-items-center gap-2 small text-muted">
            <i class="bi bi-info-circle me-1"></i> 행 클릭 시 수정 가능
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-6 small">
          조회 건수: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건
        </div>
        <div class="col-md-6 text-end text-white-50 x-small">
          ※ 신규 품목코드는 동일 품명/규격 존재 시 중복 등록되지 않습니다.
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
const searchData = reactive({
  SCH_ASTKIND: '120',
  SCH_ITEMNM: ''
})

const masterData = reactive<any>({
  ACTKIND: 'A0', CMPYCD: authStore.CMPYCD,
  ITEMCD: '', ITEMNM: '', ITEMENM: '', ITSIZE: '', UNIT: 'EA', ASTKIND: '120',
  SETYN: 'N', USEYN: 'Y', AGRPCD: '', AGRPNM: '', BGRPCD: '', BGRPNM: '',
  STOCK: '0', QTYPNT: '0', BARCODE: '', HSCODE: '', MAKER: '',
  G_WEIGHT: '0', NET_WEIGHT: '0', CBM: '0',
  INUNIT: 'EA', INUNITNM: 'EA', INQTY: '1', IMPRICE: '0',
  OUTUNIT: 'EA', OUTUNITNM: 'EA', OUTQTY: '1', OMPRICE: '0',
  REMARK: '', JONECD: '', ROWCD: '', COLCD: '', DANCD: ''
})

const assetOptions = ref<any[]>([])
const unitOptions = ref<any[]>([])
const joneOptions = ref<any[]>([])
const rowOptions = ref<any[]>([])
const colOptions = ref<any[]>([])
const danOptions = ref<any[]>([])

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
      { title: "품목코드", field: "ITEMCD", width: 100, hozAlign: "center", cssClass: "fw-bold" },
      { title: "품목명", field: "ITEMNM", minWidth: 200 },
      { title: "규격", field: "ITSIZE", width: 150 },
      { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
      { title: "대분류", field: "AGRPNM", width: 100 },
      { title: "중분류", field: "BGRPNM", width: 100 },
      { title: "입고단가", field: "IMPRICE", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 2 } },
      { title: "적정재고", field: "STOCK", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      {
        title: "사용", field: "USEYN", width: 60, hozAlign: "center",
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
async function fetchOptions() {
  try {
    const p1 = api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '100' } })
    const p2 = api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'U0', CMPYCD: authStore.CMPYCD } })
    const p3 = api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '810' } })
    const p4 = api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '820' } })
    const p5 = api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '830' } })
    const p6 = api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E0', CMPYCD: authStore.CMPYCD, GBNCD: '840' } })

    const [r1, r2, r3, r4, r5, r6] = await Promise.all([p1, p2, p3, p4, p5, p6])

    assetOptions.value = r1.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
    unitOptions.value = r2.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
    joneOptions.value = r3.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
    rowOptions.value = r4.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
    colOptions.value = r5.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
    danOptions.value = r6.data.map((i: any) => ({ CODECD: Object.values(i)[0], CODENM: Object.values(i)[1] }))
  } catch (e) { console.error('옵션 로드 실패') }
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_010U_STR', {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      ASTKIND: searchData.SCH_ASTKIND,
      ITEMNM: searchData.SCH_ITEMNM,
      LIMITOFFSET: 0,
      LIMITROWS: 9999
    })
    if (grid.value) {
      grid.value.setData(res.data)
      activeItemCount.value = res.data.length
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.ITEMNM || !masterData.ITSIZE || !masterData.UNIT) {
    return vAlertError('품목명, 규격, 단위는 필수 입력 항목입니다.')
  }

  const confirmMsg = masterData.ACTKIND === 'A0' ? '신규 품목을 등록하시겠습니까?' : '품목 정보를 수정하시겠습니까?'
  if (!confirm(confirmMsg)) return

  try {
    const res = await api.post('/api/hsba/HSBA_010U_STR', {
      ...masterData,
      USERID: authStore.USERID
    })
    if (res.data?.[0]?.RESULT === '0000000') {
      vAlertError(res.data[0].MSG || '저장에 실패했습니다.')
    } else {
      vAlert('성공적으로 저장되었습니다.')
      search()
      initialize()
    }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  const currentAsset = searchData.SCH_ASTKIND
  resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'A0', CMPYCD: authStore.CMPYCD,
    UNIT: 'EA', ASTKIND: currentAsset, SETYN: 'N', USEYN: 'Y',
    STOCK: '0', QTYPNT: '0', INQTY: '1', IMPRICE: '0', OUTQTY: '1', OMPRICE: '0',
    NET_WEIGHT: '0', G_WEIGHT: '0', CBM: '0'
  })
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  let config: any = {}
  if (type === 'AGRP') {
    config = { title: '대분류 선택', path: '/api/ha00/HA00_00P_STR', data: { GUBUN: 'G0', CODEGBN: masterData.ASTKIND }, field: 'CDNM', columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '분류명', field: 'CDNM', width: 180 }] }
  } else if (type === 'BGRP') {
    config = { title: '중분류 선택', path: '/api/ha00/HA00_00P_STR', data: { GUBUN: 'G1', CODEGBN: masterData.ASTKIND, CODEGBN_1: masterData.AGRPCD }, field: 'CDNM', columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '분류명', field: 'CDNM', width: 180 }] }
  } else if (type.includes('UNIT')) {
    config = { title: '단위 선택', path: '/api/ha00/HA00_00P_STR', data: { GUBUN: 'U0' }, field: 'CDNM', columns: [{ title: '코드', field: 'CODE', width: 80 }, { title: '단위명', field: 'CDNM', width: 180 }] }
  }

  if (!config.path) return

  Object.assign(modalProps, {
    title: config.title, path: config.path, defaultField: config.field,
    data: { ...config.data, CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 },
    columns: config.columns,
    onConfirm: (data: any) => {
      if (type === 'AGRP') { masterData.AGRPCD = data.CODE; masterData.AGRPNM = data.CDNM }
      else if (type === 'BGRP') { masterData.BGRPCD = data.CODE; masterData.BGRPNM = data.CDNM }
      else if (type === 'INUNIT') { masterData.INUNIT = data.CODE; masterData.INUNITNM = data.CDNM }
      else if (type === 'OUTUNIT') { masterData.OUTUNIT = data.CODE; masterData.OUTUNITNM = data.CDNM }
    }
  })
  modalVisible.value = true
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => {
    initGrid()
    search()
  })
})
</script>

<style scoped>
.hsba010u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.erp-table-full { width: 100%; border-collapse: collapse; table-layout: auto !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 1% !important; white-space: nowrap !important; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11px; padding: 6px 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
.form-select-xs { padding-top: 0.1rem; padding-bottom: 0.1rem; padding-left: 0.2rem; font-size: 0.75rem; }
</style>
