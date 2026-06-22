<!--
	=============================================================
	프로그램명	: 품목등록 (HSBA010U)
	작성일자	: 2025.02.24
	설명        : 영업/재고 품목 마스터 관리 (HSOD100U 디자인 표준 준수 및 누락 항목 추가)
	=============================================================
-->

<template>
  <AppAlert :show="showalert" :error="showerror" :message="alertmessage" />
  <Modal v-model:visible="modalvisible" :modalProps="modalprops" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-tags-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        품목관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목등록 (HSBA010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 100px" /><col style="width: 250px" />
                <col style="width: 100px" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">재고자산</th>
                <td>
                  <select v-model="searchdata.sch_astkind" class="form-select form-select-sm" @change="handleassetchange">
                    <option v-for="opt in assetoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light border-start">품목검색</th>
                <td class="d-flex align-items-center gap-2 border-0">
                  <input v-model="searchdata.sch_itemnm" type="text" class="form-control form-control-sm" style="width: 300px;" placeholder="품목명 또는 코드" @keyup.enter="search" />
                  <button class="btn btn-sm btn-secondary px-3" @click="search"><i class="bi bi-search me-1"></i>조회</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [중간] 품목 상세 정보 (HSOD100U 8컬럼 표준 적용) -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 품목 상세 정보</span>
          <div class="d-flex gap-2 align-items-center">
            <span v-if="masterdata.actkind === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 모드</span>
            <span v-else class="badge bg-primary" style="font-size: 10px;">신규 등록</span>
          </div>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light">품목코드</th>
                <td><input v-model="masterdata.itemcd" type="text" class="form-control form-control-sm text-center fw-bold text-primary" maxlength="7" :readonly="masterdata.actkind === 'U0'" /></td>
                <th class="required bg-light border-start">품목명</th>
                <td><input v-model="masterdata.itemnm" type="text" class="form-control form-control-sm" /></td>
                <th class="required bg-light border-start">규격</th>
                <td><input v-model="masterdata.itsize" type="text" class="form-control form-control-sm" /></td>
                <th class="required bg-light border-start">단위</th>
                <td>
                  <select v-model="masterdata.unit" class="form-select form-select-sm">
                    <option v-for="opt in unitoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="required bg-light">재고자산</th>
                <td>
                  <select v-model="masterdata.astkind" class="form-select form-select-sm">
                    <option v-for="opt in assetoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light border-start">영문명</th>
                <td><input v-model="masterdata.itemenm" type="text" class="form-control form-control-sm" /></td>
                <th class="bg-light border-start">적정재고</th>
                <td><input v-model="masterdata.stock" type="number" class="form-control form-control-sm text-end" /></td>
                <th class="bg-light border-start">바코드</th>
                <td><input v-model="masterdata.barcode" type="text" class="form-control form-control-sm" /></td>
              </tr>
              <tr>
                <th class="bg-light">대분류</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterdata.agrpnm" class="form-control bg-light" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="openhelp('agrp')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light border-start">중분류</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterdata.bgrpnm" class="form-control bg-light" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="openhelp('bgrp')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light border-start">HS Code</th>
                <td><input v-model="masterdata.hscode" type="text" class="form-control form-control-sm" /></td>
                <th class="bg-light border-start">제조사</th>
                <td><input v-model="masterdata.maker" type="text" class="form-control form-control-sm" /></td>
              </tr>
              <tr>
                <th class="required bg-light">매입처</th>
                <td colspan="3">
                  <div class="input-group input-group-sm">
                    <input v-model="masterdata.in_custcd" type="text" class="form-control text-center fw-bold bg-light" style="max-width: 80px;" readonly />
                    <input v-model="masterdata.in_custnm" class="form-control bg-light" placeholder="거래처 선택" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="openhelp('cust')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light border-start">사용/세트</th>
                <td colspan="3">
                  <div class="d-flex gap-3 align-items-center h-100 ps-2">
                    <div class="form-check form-switch m-0"><input v-model="masterdata.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="swuse"> <label class="small fw-bold" for="swuse">사용</label></div>
                    <div class="form-check form-switch m-0"><input v-model="masterdata.setyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="swset"> <label class="small fw-bold" for="swset">세트</label></div>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="bg-light">매입단위</th>
                <td>
                  <select v-model="masterdata.inunit" class="form-select form-select-sm">
                    <option v-for="opt in unitoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light border-start">매입환산</th>
                <td><input v-model="masterdata.inqty" type="number" class="form-control form-control-sm text-end fw-bold" /></td>
                <th class="bg-light border-start">매출단위</th>
                <td>
                  <select v-model="masterdata.outunit" class="form-select form-select-sm">
                    <option v-for="opt in unitoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light border-start">매출환산</th>
                <td><input v-model="masterdata.outqty" type="number" class="form-control form-control-sm text-end fw-bold" /></td>
              </tr>
              <tr>
                <th class="bg-light">총중량</th>
                <td><input v-model="masterdata.g_weight" type="number" class="form-control form-control-sm text-end" /></td>
                <th class="bg-light border-start">순중량</th>
                <td><input v-model="masterdata.net_weight" type="number" class="form-control form-control-sm text-end" /></td>
                <th class="bg-light border-start">부피(CBM)</th>
                <td colspan="3"><input v-model="masterdata.cbm" type="number" class="form-control form-control-sm text-end" /></td>
              </tr>
              <tr>
                <th class="bg-light">비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td colspan="7"><input v-model="masterdata.remark" type="text" class="form-control form-control-sm" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 품목 리스트 그리드 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 품목 리스트 ({{ activeitemcount }} 건)</span>
        </div>
        <div class="card-body p-0 flex-grow-1 overflow-hidden d-flex flex-column">
          <div ref="gridelement" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
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

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

const searchdata = reactive({ sch_astkind: '120', sch_itemnm: '' })
const masterdata = reactive<any>({
  actkind: 'A0', cmpycd: authstore.cmpycd, astkind: '120', itemcd: '',
  itsize: '', unit: 'EA', itemnm: '', itemenm: '',
  inunit: 'EA', inqty: 1, outunit: 'EA', outqty: 1, setyn: 'N',
  agrpcd: '', agrpnm: '', bgrpcd: '', bgrpnm: '',
  stock: 0, qtypnt: 0, vatyn: 'Y', sotaxyn: 'N', udogyn: 'N',
  barcode: '', hscode: '', remark: '', useyn: 'Y',
  in_custcd: '', in_custnm: '', maker: '', updemp: authstore.userid,
  g_weight: 0, net_weight: 0, cbm: 0
})

const assetoptions = ref<any[]>([]); const unitoptions = ref<any[]>([])
const gridelement = ref<HTMLElement | null>(null); let grid: Tabulator | null = null; const activeitemcount = ref(0)

const normalizekeys = (obj: any) => {
  const n: any = {};
  if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

const initgrid = () => {
  if (!gridelement.value) return
  grid = new Tabulator(gridelement.value, {
    layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 20,
    paginationButtonCount: 5, paginationSizeSelector: [20, 50, 100],
    placeholder: "데이터가 없습니다.", columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
    columns: [
      { title: "코드", field: "itemcd", width: 100, cssClass: "fw-bold text-primary" },
      { title: "품목명", field: "itemnm", minWidth: 200, hozAlign: "left" },
      { title: "규격", field: "itsize", width: 180, hozAlign: "left" },
      { title: "단위", field: "unit", width: 80 },
      { title: "대분류", field: "agrpnm", width: 150, hozAlign: "left" },
      { title: "중분류", field: "bgrpnm", width: 150, hozAlign: "left" },
      { title: "사용", field: "useyn", width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
    ]
  })
  grid.on("rowClick", (e, row) => {
    const data = normalizekeys(row.getData());
    Object.assign(masterdata, data);
    masterdata.in_custnm = data.custnm || data.in_custnm || '';
    masterdata.actkind = 'U0';
  })
}

async function fetchoptions() {
  try {
    const p1 = api.get('/api/hs00/hs00_000s_str', { params: { gubun: 'e0', cmpycd: authstore.cmpycd, gbncd: '100' } })
    const p2 = api.get('/api/hs00/hs00_000s_str', { params: { gubun: 'U0', cmpycd: authstore.cmpycd, gbncd: '', code: '' } })
    const [r1, r2] = await Promise.all([p1, p2])
    assetoptions.value = r1.data.map((i: any) => normalizekeys(i)).map((n: any) => ({ codecd: n.code || n.codecd, codenm: n.cdnm || n.codenm }))
    unitoptions.value = r2.data.map((i: any) => normalizekeys(i)).map((n: any) => ({ codecd: n.unit, codenm: n.unitnm }))
  } catch (e) { console.error('코드 로드 실패', e) }
}

async function search() {
  try {
    const res = await api.post('/api/hsba/hsba_010u_str', {
      actkind: 'S0', cmpycd: authstore.cmpycd, astkind: searchdata.sch_astkind, itemnm: searchdata.sch_itemnm || '',
      itemcd: '', itsize: '', unit: '', itemenm: '', inunit: '', inqty: 0, outunit: '', outqty: 0, setyn: '',
      agrpcd: '', bgrpcd: '', imprice: 0, omprice: 0, stock: 0, qtypnt: 0, vatyn: '', sotaxyn: '', udogyn: '',
      barcode: '', hscode: '', remark: '', useyn: '', in_custcd: '', maker: '', updemp: authstore.userid
    })
    const processed = (res.data || []).map((i: any) => normalizekeys(i));
    if (grid) { grid.setData(processed); activeitemcount.value = processed.length }
  } catch (e) { valerterror('조회 실패') }
}

async function save() {
  if (!masterdata.itemnm || !masterdata.itsize) return valerterror('품목명과 규격은 필수입니다.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const res = await api.post('/api/hsba/hsba_010u_str', { ...masterdata, userid: authstore.userid })
    const resdata = normalizekeys(res.data?.[0]);
    if (resdata.result === 'N' || resdata.erryn === 'Y') { valerterror(resdata.msg || '저장 실패') }
    else { valert('저장되었습니다.'); search(); initialize() }
  } catch (e) { valerterror('저장 중 오류 발생') }
}

function initialize() {
  const currentasset = searchdata.sch_astkind; resetform(masterdata)
  Object.assign(masterdata, {
    actkind: 'A0', cmpycd: authstore.cmpycd, unit: 'EA', astkind: currentasset,
    setyn: 'N', useyn: 'Y', stock: 0, qtypnt: 0, imprice: 0, omprice: 0,
    vatyn: 'Y', sotaxyn: 'N', udogyn: 'N', inunit: 'EA', inqty: 1, outunit: 'EA', outqty: 1,
    updemp: authstore.userid, in_custcd: '', in_custnm: '', agrpcd: '', agrpnm: '', bgrpcd: '', bgrpnm: '', maker: '',
    g_weight: 0, net_weight: 0, cbm: 0
  })
}

const handleassetchange = () => { initialize(); search(); }

const modalvisible = ref(false); const modalprops = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openhelp(type: string) {
  let config: any = {}
  if (type === 'agrp') {
    config = {
      title: '대분류 선택',
      path: '/api/hs00/hs00_000s_str',
      data: { gubun: 'g0', gbncd: masterdata.astkind },
      field: 'agrpnm',
      columns: [{ title: '코드', field: 'agrpcd', width: 80 }, { title: '분류명', field: 'agrpnm', width: 180 }]
    }
  } else if (type === 'bgrp') {
    config = {
      title: '중분류 선택',
      path: '/api/hs00/hs00_000s_str',
      data: { gubun: 'g1', gbncd: masterdata.astkind, code: masterdata.agrpcd },
      field: 'bgrpnm',
      columns: [{ title: '코드', field: 'bgrpcd', width: 80 }, { title: '분류명', field: 'bgrpnm', width: 180 }]
    }
  } else if (type === 'cust') {
    config = {
      title: '거래처 선택',
      path: '/api/ha00/ha00_00p_str',
      data: { gubun: 'c4' },
      field: 'custnm',
      columns: [{ title: '코드', field: 'custcd', width: 100 }, { title: '거래처명', field: 'custnm', width: 200 }]
    }
  }

  if (!config.path) return
  Object.assign(modalprops, {
    title: config.title, path: config.path, defaultField: config.field,
    data: { ...config.data, cmpycd: authstore.cmpycd }, columns: config.columns,
    onConfirm: (d: any) => {
      const n = normalizekeys(d);
      if (type === 'agrp') { masterdata.agrpcd = n.agrpcd || n.code || ''; masterdata.agrpnm = n.agrpnm || n.cdnm || ''; }
      else if (type === 'bgrp') { masterdata.bgrpcd = n.bgrpcd || n.code || ''; masterdata.bgrpnm = n.bgrpnm || n.cdnm || ''; }
      else if (type === 'cust') { masterdata.in_custcd = n.custcd || n.code || ''; masterdata.in_custnm = n.custnm || n.cdnm || ''; }
    }
  })
  modalvisible.value = true
}

onMounted(async () => { await fetchoptions(); nextTick(() => { initgrid(); search() }) })
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
