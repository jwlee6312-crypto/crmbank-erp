<!--
	=============================================================
	프로그램명	: 외주가공생산실적 (HPIO350U)
	작성일자	: 2025.02.24
	설명        : 외주 생산 제품 실적 및 투입 자재 상세 관리 (HPIO210U 표준 패턴 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-journal-check me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">외주가공생산실적 (HPIO350U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMaster">조회</button>
        <button class="btn-erp btn-save" @click="saveAll">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 및 마스터 정보 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 20%" />
                <col style="width: 10%" /><col style="width: 20%" />
                <col style="width: 10%" /><col style="width: 15%" />
                <col style="width: 10%" /><col style="width: 15%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light required">생산라인</th>
                <td>
                  <select v-model="searchForm.linecd" class="form-select form-select-sm" @change="onLineChange">
                    <option v-for="opt in lineOptions" :key="opt.code" :value="opt.code">[{{ opt.code }}] {{ opt.cdnm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light required">거 래 처</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="searchForm.custcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchForm.custnm" type="text" class="form-control" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light required">지시일자</th>
                <td><input v-model="ordymd_f" type="date" class="form-control form-control-sm" /></td>
                <th class="text-center bg-light required">입고일자</th>
                <td><input v-model="proymd_f" type="date" class="form-control form-control-sm" /></td>
              </tr>
              <tr>
                <th class="text-center bg-light required">출고창고</th>
                <td>
                  <select v-model="masterInfo.whcd" class="form-select form-select-sm">
                    <option v-for="opt in whOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light required">입고창고</th>
                <td>
                  <select v-model="masterInfo.iwhcd" class="form-select form-select-sm">
                    <option v-for="opt in whOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light required">입고공정</th>
                <td colspan="3">
                  <select v-model="masterInfo.progcd" class="form-select form-select-sm" style="max-width: 250px;">
                    <option v-for="opt in progOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 2단 그리드 레이아웃 영역 -->
      <div class="d-flex flex-column gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- 상단: 외주 생산 제품 (grid1) -->
        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
            <span class="fw-bold small text-dark"><i class="bi bi-box-seam me-2 text-primary"></i>외주 생산 제품 리스트</span>
            <div class="d-flex gap-1">
              <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow('PROD')" style="font-size: 11px;">+ 제품추가</button>
              <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteRows('PROD')" style="font-size: 11px;">- 삭제</button>
            </div>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- 하단: 투입 자재 상세 (grid2) -->
        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
            <span class="fw-bold small text-dark">
              <i class="bi bi-tools me-2 text-success"></i>투입 자재 상세
              <span v-if="selectedProduct.itemnm" class="badge bg-success-subtle text-success border border-success-subtle ms-2">{{ selectedProduct.itemnm }}</span>
              <span v-else class="ms-2 text-danger opacity-75" style="font-size: 11px;">(상단 리스트에서 제품을 선택해야 자재를 추가할 수 있습니다.)</span>
            </span>
            <div class="d-flex gap-1">
              <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow('MAT')" :disabled="!selectedProduct.itemcd" style="font-size: 11px;">+ 자재추가</button>
              <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteRows('MAT')" :disabled="!selectedProduct.itemcd" style="font-size: 11px;">- 삭제</button>
            </div>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef2" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { getDate } from '@/composables/useDate'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

// [1] 데이터 모델링
const initymd = today.replace(/-/g, '')
const searchForm = reactive({ linecd: '010', custcd: '', custnm: '', ordymd: initymd, proymd: initymd })
const masterInfo = reactive({ whcd: '300', iwhcd: '200', progcd: '' })

const ordymd_f = computed({ get: () => formatDate(searchForm.ordymd), set: (v) => { if (v) searchForm.ordymd = v.replace(/-/g, '') } })
const proymd_f = computed({ get: () => formatDate(searchForm.proymd), set: (v) => { if (v) searchForm.proymd = v.replace(/-/g, '') } })

const lineOptions = ref<any[]>([]); const progOptions = ref<any[]>([]); const whOptions = ref<any[]>([])
const selectedProduct = reactive<any>({ itemcd: '', itemnm: '' })
const closingInfo = reactive({ clsymd: '' })

const tableRef1 = ref<HTMLDivElement | null>(null); const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null; let grid2: Tabulator | null = null

// [2] 그리드 초기화
const initGrids = () => {
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%", placeholder: "조회 조건을 선택하세요.", selectable: 1,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", frozen: true },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">신규</span>';
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>';
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>';
          return '';
      }, frozen: true },
      { title: "제품명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', cellClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 60, hozAlign: "center" },
      { title: "지시량", field: "ordqty", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "생산량", field: "prdqty", width: 150, hozAlign: "right", editor: "number", cssClass: "bg-light-yellow fw-bold",
        cellEdited: (cell) => { const d = cell.getData(); if (d._state === 'EXIST') cell.getRow().update({ _status: '수정' }); calcTotal(cell.getRow()); }
      },
      { title: "가공단가", field: "outprice", width: 150, hozAlign: "right", editor: "number",
        cellEdited: (cell) => { const d = cell.getData(); if (d._state === 'EXIST') cell.getRow().update({ _status: '수정' }); calcTotal(cell.getRow()); }
      },
      { title: "합계금액", field: "outtot", width: 150, hozAlign: "right", formatter: "money", cssClass: "fw-bold text-primary" },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ],
  });
  grid1.on("rowClick", (e, row) => fetchDetails(row.getData()));

  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", placeholder: "제품을 선택하세요.", selectable: true,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">신규</span>';
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>';
          return '';
      }},
      { title: "투입자재", field: "mitemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-success', cellClick: (e, cell) => handleOpenHelp('MAT', cell.getRow()) },
      { title: "규격", field: "mitsize", width: 150 },
      { title: "단위", field: "munit", width: 70, hozAlign: "center" },
      { title: "소요량", field: "soqty", width: 90, hozAlign: "right", formatter: "money" },
      { title: "투입량", field: "inqty", width: 100, hozAlign: "right", editor: "number", cssClass: "bg-light-green fw-bold" },
      { title: "출고공정", field: "bprognm", width: 150, cellClick: (e, cell) => handleOpenHelp('befprog', cell.getRow()) },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  });
}

const calcTotal = (row: any) => {
  const d = row.getData();
  const amt = Math.floor(Number(d.prdqty || 0) * Number(d.outprice || 0));
  row.update({ outamt: amt, outvat: Math.floor(amt * 0.1), outtot: Math.floor(amt * 1.1) });
}

// [3] 비즈니스 로직
const fetchInitCodes = async () => {
  try {
    const [line, wh] = await Promise.all([
      api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y', code: '' } }),
      api.post('/api/hs00/HS00_000S_STR', { gubun: 'W0', cmpycd: authStore.cmpycd, gbncd: '', code: '', codenm: '', etcval: '' })
    ]);
    lineOptions.value = line.data;
    whOptions.value = wh.data;
    if (lineOptions.value.length > 0) { searchForm.linecd = lineOptions.value[0].code; onLineChange(); }
  } catch (e) {}
}

const onLineChange = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'G0', cmpycd: authStore.cmpycd, gbncd: searchForm.linecd, code: '' } });
    progOptions.value = res.data;
    if (progOptions.value.length > 0) masterInfo.progcd = progOptions.value[0].code;
  } catch (e) {}
}

const fetchMaster = async () => {
  if (!searchForm.custcd) return vAlertError('거래처를 선택하세요.');
  try {
    const res = await api.post('/api/hpio/HPIO_350U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, linecd: searchForm.linecd, custcd: searchForm.custcd, ordymd: searchForm.ordymd, proymd: searchForm.proymd });
    grid1?.setData(res.data.map((i: any) => ({ ...i, _state: 'EXIST', _status: '' })));
    grid2?.clearData(); selectedProduct.itemcd = ''; selectedProduct.itemnm = '';
    vAlert('조회되었습니다.');
  } catch (e) { vAlertError('조회 실패'); }
}

const fetchDetails = async (row: any) => {
  selectedProduct.itemcd = row.itemcd; selectedProduct.itemnm = row.itemnm;
  if (!row.itemcd) { grid2?.clearData(); return; }

  // 💡 신규 제품 행인 경우 서버 조회를 하지 않고 그리드만 초기화 (자재 추가는 가능하도록 유지)
  if (row._status === '입력') {
    grid2?.clearData();
    return;
  }

  try {
    const res = await api.post('/api/hpio/HPIO_351U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, linecd: searchForm.linecd, itemcd: row.itemcd, custcd: searchForm.custcd, ordymd: searchForm.ordymd, proymd: searchForm.proymd });
    grid2?.setData(res.data.map((i: any) => ({ ...i, _state: 'EXIST', _status: '' })));
  } catch (e) {}
}

const saveData = async () => {
  if (searchForm.proymd <= closingInfo.clsymd) return vAlertError('회계 마감된 일자입니다.');
  const prods = grid1?.getData().filter((r: any) => r._status) || [];
  const mats = grid2?.getData().filter((r: any) => r._status) || [];
  if (!prods.length && !mats.length) return vAlertError('저장할 변경 내용이 없습니다.');
  if (!confirm('변경된 정보를 저장하시겠습니까?')) return

  try {
    // 🚀 [Seed-Model] 루프 저장 루틴 (개별 행 무결성 체크)
    for (const p of prods) {
      const actkind = p._status === '입력' ? 'A0' : (p._status === '삭제' ? 'D0' : 'U0')
      const resP = await api.post('/api/hpio/HPIO_350U_STR', { ...p, actkind, cmpycd: authStore.cmpycd, linecd: searchForm.linecd, custcd: searchForm.custcd, ordymd: searchForm.ordymd, proymd: searchForm.proymd, progcd: masterInfo.progcd, whcd: masterInfo.whcd, prodcd: '200', userid: authStore.userid });
      const pValues = resP.data?.[0]?.returnkeyvalue || Object.values(resP.data?.[0] || {});
      if (pValues[0] === '000000') throw new Error(String(pValues[1] || '제품 실적 저장 중 오류 발생'));
    }
    for (const m of mats) {
      const actkind = m._status === '입력' ? 'A0' : (m._status === '삭제' ? 'D0' : 'U0')
      const resM = await api.post('/api/hpio/HPIO_351U_STR', { ...m, actkind, cmpycd: authStore.cmpycd, linecd: searchForm.linecd, itemcd: selectedProduct.itemcd, custcd: searchForm.custcd, ordymd: searchForm.ordymd, proymd: searchForm.proymd, progcd: masterInfo.progcd, whcd: masterInfo.whcd, prodcd: '200', userid: authStore.userid });
      const mValues = resM.data?.[0]?.returnkeyvalue || Object.values(resM.data?.[0] || {});
      if (mValues[0] === '000000') throw new Error(String(mValues[1] || '자재 소모 저장 중 오류 발생'));
    }
    vAlert('저장되었습니다.'); fetchMaster();
  } catch (e) { vAlertError('저장 실패'); }
}

const handleOpenHelp = (type: string, target?: any) => {
  const props: any = {
    title: '',
    path: '',
    data: { cmpycd: authStore.cmpycd },
    columns: [],
    onConfirm: () => {}
  }

  if (type === 'CUST') {
    props.title = '외주처 선택'
    props.path = '/api/ha00/HA00_00P_STR'
    props.data.gubun = 'C7'
    props.columns = [
      { title: '코드', field: 'custcd', width: 80, hozAlign: 'center' },
      { title: '거래처명', field: 'custnm', width: 200 }
    ]
    props.onConfirm = (d: any) => { searchForm.custcd = d.custcd; searchForm.custnm = d.custnm }
  }
  else if (type === 'ITEM') {
    props.title = '제품 선택'
    props.path = '/api/hp00/HP00_000S_STR'
    props.data.gubun = 'I0'
    props.data.gbncd = 'A'
    props.columns = [
      { title: '자산구분', field: 'astkindnm', width: 100 },
      { title: '코드', field: 'itemcd', width: 100, hozAlign: 'center' },
      { title: '제품명', field: 'itemnm', width: 200 },
      { title: '규격', field: 'itsize', width: 150 },
      { title: '단위', field: 'unit', width: 80, hozAlign: 'center' }
    ]
    props.onConfirm = (d: any) => {
      target.update({ itemcd: d.itemcd, itemnm: d.itemnm, itsize: d.itsize, unit: d.unit, _status: '입력', _state: 'NEW' });
      // 💡 품목 선택 시 현재 선택된 제품 정보(selectedProduct)를 즉시 업데이트하여 버튼 활성화
      selectedProduct.itemcd = d.itemcd;
      selectedProduct.itemnm = d.itemnm;
    }
  }
  else if (type === 'MAT') {
    props.title = '자재 선택'
    props.path = '/api/hp00/HP00_000S_STR'
    props.data.gubun = 'I0'
    props.data.gbncd = 'A'
    props.columns = [
      { title: '자산구분', field: 'astkindnm', width: 100 },
      { title: '코드', field: 'itemcd', width: 100, hozAlign: 'center' },
      { title: '자재명', field: 'itemnm', width: 200 },
      { title: '규격', field: 'itsize', width: 150 },
      { title: '단위', field: 'unit', width: 80, hozAlign: 'center' }
    ]
    props.onConfirm = (d: any) => target.update({ mitemcd: d.itemcd, mitemnm: d.itemnm, mitsize: d.itsize, munit: d.unit, mastkind: d.astkind, _status: '입력', _state: 'NEW' })
  }
  else if (type === 'befprog') {
    props.title = '출고공정 선택'
    props.path = '/api/hp00/HP00_000S_STR'
    props.data.gubun = 'G0'
    props.data.gbncd = searchForm.linecd
    props.columns = [
      { title: '코드', field: 'progcd', width: 100, hozAlign: 'center' },
      { title: '공정명', field: 'cdnm', width: 200 }
    ]
    props.onConfirm = (d: any) => target.update({ befprog: d.progcd, bprognm: d.cdnm })
  }

  Object.assign(modalProps, props)
  modalVisible.value = true
}

const handleRowAction = (row: any) => {
  const d = row.getData();
  if (d._state === 'NEW') row.delete();
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' });
}

const addRow = (type: string) => {
  if (type === 'PROD') grid1?.addRow({ prdqty: 0, outprice: 0, outtot: 0, _status: '입력', _state: 'NEW', useyn: 'Y' }, true);
  else grid2?.addRow({ inqty: 0, _status: '입력', _state: 'NEW', useyn: 'Y' }, true);
}

const deleteRows = (type: string) => {
  const g = type === 'PROD' ? grid1 : grid2;
  g?.getSelectedRows().forEach(r => handleRowAction(r));
}

const initialize = () => {
  resetForm(searchForm); Object.assign(searchForm, { linecd: '010', ordymd: initymd, proymd: initymd });
  grid1?.clearData(); grid2?.clearData(); selectedProduct.itemcd = ''; selectedProduct.itemnm = '';
  fetchInitCodes();
}

const formatDate = (v: any) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v;
onMounted(() => {
  fetchInitCodes();
  api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => { if(r.data?.length) closingInfo.clsymd = r.data[0].clsymd })
  nextTick(initGrids);
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
.grid-container-right { border-bottom: 3px solid #005a9f !important; }
</style>
