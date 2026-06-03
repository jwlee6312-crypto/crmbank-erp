<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
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
              <select v-model="searchData.SCH_astkind" class="form-select border-0 bg-white" @change="handleAssetChange">
                <option v-for="opt in assetOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
              </select>
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width: 350px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">품목검색</span>
              <input v-model="searchData.SCH_itemnm" type="text" class="form-control border-0 bg-white" placeholder="품목명 또는 코드" @keyup.enter="search" />
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
            <span v-if="masterData.actkind === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 중</span>
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
                <td><input v-model="masterData.itemcd" type="text" class="form-control form-control-sm text-center fw-bold text-primary" maxlength="7" :readonly="masterData.actkind === 'U0'" /></td>
                <th class="required">품목명</th>
                <td><input v-model="masterData.itemnm" type="text" class="form-control form-control-sm" /></td>
                <th class="required">규격</th>
                <td><input v-model="masterData.itsize" type="text" class="form-control form-control-sm" /></td>
                <th class="required">단위</th>
                <td>
                  <select v-model="masterData.unit" class="form-select form-select-sm">
                    <option v-for="opt in unitOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="required">재고자산</th>
                <td>
                  <select v-model="masterData.astkind" class="form-select form-select-sm">
                    <option v-for="opt in assetOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>영문명</th>
                <td><input v-model="masterData.itemenm" type="text" class="form-control form-control-sm" /></td>
                <th>적정재고</th>
                <td><input v-model="masterData.stock" type="number" class="form-control form-control-sm text-end" /></td>
                <th>대분류</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.Agrpnm" type="text" class="form-control bg-light" readonly />
                    <button class="btn btn-outline-secondary btn-sm px-1" @click="openHelp('AGRP')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>중분류</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.Bgrpnm" type="text" class="form-control bg-light" readonly />
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
                    <input v-model="masterData.IN_custcd" type="text" class="form-control text-center fw-bold" style="max-width: 80px;" placeholder="코드" readonly />
                    <input v-model="masterData.IN_custnm" type="text" class="form-control bg-light" placeholder="거래처명을 선택하세요" readonly />
                    <button class="btn btn-outline-secondary btn-sm px-1" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>사용/세트</th>
                <td>
                  <div class="d-flex gap-2">
                    <div class="form-check form-switch m-0"><input v-model="masterData.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="swUse"> <label class="small fw-bold" for="swUse">사용</label></div>
                    <div class="form-check form-switch m-0"><input v-model="masterData.SETYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="swSet"> <label class="small fw-bold" for="swSet">세트</label></div>
                  </div>
                </td>
              </tr>
              <tr>
                <th>매입단위</th>
                <td>
                  <select v-model="masterData.INunit" class="form-select form-select-sm">
                    <option v-for="opt in unitOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th>매입환산</th>
                <td><input v-model="masterData.inqty" type="number" class="form-control form-control-sm text-end fw-bold" /></td>
                <th>매출단위</th>
                <td>
                  <select v-model="masterData.OUTunit" class="form-select form-select-sm">
                    <option v-for="opt in unitOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th>매출환산</th>
                <td><input v-model="masterData.OUtqty" type="number" class="form-control form-control-sm text-end fw-bold" /></td>
                <th>비고</th>
                <td colspan="1"><input v-model="masterData.remark" type="text" class="form-control form-control-sm w-100" /></td>
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
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridElement" class="tabulator-instance flex-grow-1"></div>
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

const searchData = reactive({ SCH_astkind: '120', SCH_itemnm: '' })
const masterData = reactive<any>({
  actkind: 'A0', cmpycd: authStore.cmpycd, astkind: '120', itemcd: '',
  itsize: '', unit: 'EA', itemnm: '', itemenm: '',
  INunit: 'EA', inqty: 1, OUTunit: 'EA', OUtqty: 1, SETYN: 'N',
  agrpcd: '', Agrpnm: '', bgrpcd: '', Bgrpnm: '',
  stock: 0, QTYPNT: 0, VATYN: 'Y', SOTAXYN: 'N', UDOGYN: 'N',
  BARCODE: '', HSCODE: '', remark: '', useyn: 'Y',
  IN_custcd: '', IN_custnm: '', MAKER: '', updemp: authStore.userid
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
      { title: "코드", field: "itemcd", width: 100, hozAlign: "center", cssClass: "fw-bold text-primary" },
      { title: "품목명", field: "itemnm", minWidth: 200 },
      { title: "규격", field: "itsize", width: 180 },
      { title: "단위", field: "unit", width: 100, hozAlign: "center" },
      { title: "대분류", field: "Agrpnm", width: 200 },
      { title: "중분류", field: "Bgrpnm", width: 200 },
      { title: "사용", field: "useyn", width: 100, hozAlign: "center", formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
    ]
  })
  grid.value.on("rowClick", (e, row) => {
    const data = row.getData();
    const cleanedData: any = {};
    Object.keys(data).forEach(key => {
      cleanedData[key] = typeof data[key] === 'string' ? data[key].trim() : data[key];
    });
    Object.assign(masterData, cleanedData);
    masterData.IN_custnm = cleanedData.custnm || cleanedData.IN_custnm || '';
    masterData.actkind = 'U0';
  })
}

async function fetchOptions() {
  try {
    const p1 = api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '100' } })
    const p2 = api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'U0', cmpycd: authStore.cmpycd, gbncd: '', CODE: '' } })
    const [r1, r2] = await Promise.all([p1, p2])
    assetOptions.value = r1.data.map((i: any) => ({ codecd: String(i.CODE || i.codecd || '').trim(), codenm: String(i.cdnm || i.codenm || '').trim() }))
    unitOptions.value = r2.data.map((i: any) => ({ codecd: String(i.unit || '').trim(), codenm: String(i.unitnm || '').trim() }))
  } catch (e) { console.error('코드 로드 실패', e) }
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_010U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, astkind: searchData.SCH_astkind, itemnm: searchData.SCH_itemnm || '',
      itemcd: '', itsize: '', unit: '', itemenm: '', INunit: '', inqty: 0, OUTunit: '', OUtqty: 0, SETYN: '',
      agrpcd: '', bgrpcd: '', IMprice: 0, OMprice: 0, stock: 0, QTYPNT: 0, VATYN: '', SOTAXYN: '', UDOGYN: '',
      BARCODE: '', HSCODE: '', remark: '', useyn: '', IN_custcd: '', MAKER: '', updemp: authStore.userid
    })
    if (grid.value) { grid.value.setData(res.data || []); activeItemCount.value = res.data.length }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.itemnm || !masterData.itsize) return vAlertError('품목명과 규격은 필수입니다.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const res = await api.post('/api/hsba/HSBA_010U_STR', { ...masterData, userid: authStore.userid })
    if (res.data?.[0]?.RESULT === '0000000') { vAlertError(res.data[0].MSG || '저장 실패') }
    else { vAlert('저장되었습니다.'); search(); initialize() }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  const currentAsset = searchData.SCH_astkind; resetForm(masterData)
  Object.assign(masterData, {
    actkind: 'A0', cmpycd: authStore.cmpycd, unit: 'EA', astkind: currentAsset,
    SETYN: 'N', useyn: 'Y', stock: 0, QTYPNT: 0, IMprice: 0, OMprice: 0,
    VATYN: 'Y', SOTAXYN: 'N', UDOGYN: 'N', INunit: 'EA', inqty: 1, OUTunit: 'EA', OUtqty: 1,
    updemp: authStore.userid, IN_custcd: '', IN_custnm: '', agrpcd: '', Agrpnm: '', bgrpcd: '', Bgrpnm: '', MAKER: ''
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
      data: { gubun: 'G0', gbncd: masterData.astkind },
      field: 'Agrpnm',
      columns: [{ title: '코드', field: 'agrpcd', width: 80, headerHozAlign: 'center', hozAlign: 'center' }, { title: '분류명', field: 'Agrpnm', width: 180 }]
    }
  } else if (type === 'BGRP') {
    config = {
      title: '중분류 선택',
      path: '/api/hs00/HS00_000S_STR',
      data: { gubun: 'G1', gbncd: masterData.astkind, CODE: masterData.agrpcd },
      field: 'Bgrpnm',
      columns: [{ title: '코드', field: 'bgrpcd', width: 80, headerHozAlign: 'center', hozAlign: 'center' }, { title: '분류명', field: 'Bgrpnm', width: 180 }]
    }
  } else if (type === 'CUST') {
    config = {
      title: '거래처 선택',
      path: '/api/ha00/HA00_00P_STR',
      data: { gubun: 'C4' },
      field: 'custnm',
      columns: [{ title: '코드', field: 'custcd', width: 100, headerHozAlign: 'center', hozAlign: 'center' }, { title: '거래처명', field: 'custnm', width: 200 }]
    }
  }

  if (!config.path) return
  Object.assign(modalProps, {
    title: config.title, path: config.path, defaultField: config.field,
    data: { ...config.data, cmpycd: authStore.cmpycd }, columns: config.columns,
    onConfirm: (data: any) => {
      if (type === 'AGRP') { masterData.agrpcd = (data.agrpcd || data.CODE || '').trim(); masterData.Agrpnm = (data.Agrpnm || data.cdnm || '').trim(); }
      else if (type === 'BGRP') { masterData.bgrpcd = (data.bgrpcd || data.CODE || '').trim(); masterData.Bgrpnm = (data.Bgrpnm || data.cdnm || '').trim(); }
      else if (type === 'CUST') { masterData.IN_custcd = (data.custcd || data.CODE || '').trim(); masterData.IN_custnm = (data.custnm || data.cdnm || '').trim(); }
    }
  })
  modalVisible.value = true
}

onMounted(async () => { await fetchOptions(); nextTick(() => { initGrid(); search() }) })
</script>
