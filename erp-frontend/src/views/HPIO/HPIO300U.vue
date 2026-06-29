<!--
	=============================================================
	프로그램명	: 생산실적등록 (HPIO300U)
	작성일자	: 2025.02.24
	설명        : 생산 공정별 실적 등록 및 투입 자재 상세 관리 (HPIO210U 표준 패턴 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam-fill me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">생산실적등록 (HPIO300U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchProcesses">조회</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 23%" />
                <col style="width: 10%" /><col style="width: 23%" />
                <col style="width: 10%" /><col style="width: 24%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light required">생산라인</th>
                <td>
                  <select v-model="searchForm.linecd" class="form-select form-select-sm" @change="onLineChange">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.linecd" :value="opt.linecd">
                      [{{ opt.linecd }}] {{ opt.linenm }}
                    </option>
                  </select>
                </td>
                <th class="text-center bg-light required">지시일자</th>
                <td>
                  <input v-model="ordymd_f" type="date" class="form-control form-control-sm" @change="onFilterChange" />
                </td>
                <th class="bg-light"></th><td></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 3단 레이아웃 영역 -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 공정 목록 (grid1) -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 280px; min-width: 280px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark text-center">생산공정</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ➡️ 우측: 실적 및 자재 (Vertical Split) -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

          <!-- 우측 상단: 제품 생산 실적 (grid2) -->
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark">
                <i class="bi bi-list-check me-2 text-primary"></i>제품 생산 실적
                <span v-if="selectedProg.prognm" class="badge bg-primary-subtle text-primary border border-primary-subtle ms-2">{{ selectedProg.prognm }}</span>
              </span>
              <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="savePerformance" style="font-size: 11px;">실적저장</button>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1"></div>
            </div>
          </div>

          <!-- 우측 하단: 투입 자재 상세 (grid3) -->
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark">
                <i class="bi bi-box-arrow-in-right me-2 text-success"></i>투입 자재 상세
                <span v-if="selectedProduct.itemnm" class="badge bg-success-subtle text-success border border-success-subtle ms-2">{{ selectedProduct.itemnm }}</span>
              </span>
              <div class="d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addMaterialRow" :disabled="!selectedProduct.itemcd" style="font-size: 11px;">+ 자재추가</button>
                <button class="btn btn-sm btn-outline-success py-0 px-2 fw-bold" @click="saveMaterials" :disabled="!selectedProduct.itemcd" style="font-size: 11px;">자재저장</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef3" class="tabulator-instance flex-grow-1"></div>
            </div>
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
const searchForm = reactive({ linecd: '010', ordymd: initymd })
const lineOptions = ref<any[]>([])
const selectedProg = reactive({ progcd: '', prognm: '' })
const selectedProduct = reactive<any>({ itemcd: '', itemnm: '' })
const closingInfo = reactive({ clsymd: '' })

const ordymd_f = computed({
  get: () => searchForm.ordymd && searchForm.ordymd.length === 8 ? `${searchForm.ordymd.substring(0, 4)}-${searchForm.ordymd.substring(4, 6)}-${searchForm.ordymd.substring(6, 8)}` : '',
  set: (v) => { if (v) searchForm.ordymd = v.replace(/-/g, '') }
})

const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
const tableRef3 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null
let grid3: Tabulator | null = null

// [2] 그리드 초기화
const initGrids = () => {
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%", placeholder: "라인 선택", selectable: 1,
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
      { title: "공정명", field: "cdnm", hozAlign: "left", headerSort: false },
      { title: "코드", field: "code", hozAlign: "center", width: 80, cssClass: "fw-bold text-primary", headerSort: false }
    ],
  });
  grid1.on("rowClick", (e, row) => onProcessSelect(row.getData()));

  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", placeholder: "공정을 선택하세요.", selectable: true,
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
      { title: "제품명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary',
        cellClick: (e, cell) => { if(Number(cell.getData().prdqty) !== 0) fetchMaterialData(cell.getData()) }
      },
      { title: "생산일자", field: "proymd", width: 110, editor: "input", formatter: (c) => formatDate(c.getValue()) },
      { title: "지시량", field: "ordqty", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "금회생산", field: "prdqty", width: 90, hozAlign: "right", editor: "number", cssClass: "bg-light-yellow fw-bold",
        cellEdited: (cell) => { const d = cell.getData(); if (d._state === 'EXIST') cell.getRow().update({ _status: '수정' }); }
      },
      { title: "불량", field: "errqty", width: 70, hozAlign: "right", editor: "number",
        cellEdited: (cell) => { const d = cell.getData(); if (d._state === 'EXIST') cell.getRow().update({ _status: '수정' }); }
      },
      { title: "불량원인", field: "woninnm", width: 130, cellClick: (e, cell) => handleOpenHelp('REASON', cell.getRow()) },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  });

  grid3 = new Tabulator(tableRef3.value!, {
    layout: "fitColumns", height: "100%", placeholder: "제품을 선택하세요.", selectable: true,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">신규</span>';
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>';
          return '';
      }},
      { title: "투입자재명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-success', cellClick: (e, cell) => handleOpenHelp('MATERIAL', cell.getRow()) },
      { title: "재고", field: "jaego", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "투입량", field: "inqty", width: 100, hozAlign: "right", editor: "number", cssClass: "bg-light-green fw-bold" },
      { title: "전공정", field: "prognm", width: 130, cellClick: (e, cell) => handleOpenHelp('befprog', cell.getRow()) },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  });
}

// [3] 비즈니스 로직
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y', code: '' } })
    lineOptions.value = res.data.map((i: any) => ({ linecd: i.code || i.code, linenm: i.cdnm }));
  } catch (e) { console.error(e) }
}

const fetchProcesses = async () => {
  if (!searchForm.linecd) { vAlertError('생산라인을 먼저 선택하세요.'); return; }
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'G0', cmpycd: authStore.cmpycd, gbncd: searchForm.linecd, code: '' } })
    grid1?.setData(res.data)
    grid2?.clearData(); grid3?.clearData()
    selectedProg.progcd = ''; selectedProg.prognm = '';
    selectedProduct.itemcd = ''; selectedProduct.itemnm = '';
  } catch (e) { console.error(e) }
}

const onProcessSelect = (prog: any) => {
  selectedProg.progcd = prog.code; selectedProg.prognm = prog.cdnm
  fetchPerfData()
}

const fetchPerfData = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_300U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, linecd: searchForm.linecd, progcd: selectedProg.progcd, ordymd: searchForm.ordymd
    })
    grid2?.setData(res.data.map((i: any) => ({ ...i, _state: 'EXIST', _status: '', prdqty_o: i.prdqty || 0 })))
    grid3?.clearData(); selectedProduct.itemcd = ''; selectedProduct.itemnm = ''
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('실적 조회 실패') }
}

const fetchMaterialData = async (product: any) => {
  Object.assign(selectedProduct, product)
  try {
    const res = await api.post('/api/hpio/HPIO_301U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, linecd: searchForm.linecd, progcd: selectedProg.progcd, proymd: product.proymd, itemcd: product.itemcd
    })
    grid3?.setData(res.data.map((i: any) => ({ ...i, _state: 'EXIST', _status: '' })))
  } catch (e) { vAlertError('자재 조회 실패') }
}

const savePerformance = async () => {
  const details = grid2?.getData().filter((r: any) => r._status) || []
  if (details.length === 0) return vAlertError('저장할 실적 변경 항목이 없습니다.')
  if (!confirm('제품 생산 실적을 저장하시겠습니까?')) return

  try {
    for (const item of details) {
      const actkind = !item.prdqty_o || Number(item.prdqty_o) === 0 ? 'A0' : (item._status === '삭제' ? 'D0' : 'U0')
      await api.post('/api/hpio/HPIO_300U_STR', {
        ...item, actkind, cmpycd: authStore.cmpycd, linecd: searchForm.linecd, progcd: selectedProg.progcd, userid: authStore.userid
      })
    }
    vAlert('실적이 저장되었습니다.'); fetchPerfData()
  } catch (e) { vAlertError('실적 저장 실패') }
}

const saveMaterials = async () => {
  const details = grid3?.getData().filter((r: any) => r._status) || []
  if (details.length === 0) return vAlertError('저장할 자재 변경 항목이 없습니다.')
  if (!confirm('투입 자재 상세 내역을 저장하시겠습니까?')) return

  try {
    for (const item of details) {
      const actkind = item._status === '입력' ? 'A' : (item._status === '삭제' ? 'D' : 'U')
      await api.post('/api/hpio/HPIO_301U_STR', {
        ...item, actkind, cmpycd: authStore.cmpycd, linecd: searchForm.linecd, progcd: selectedProg.progcd, userid: authStore.userid
      })
    }
    vAlert('자재 정보가 저장되었습니다.'); fetchMaterialData(selectedProduct)
  } catch (e) { vAlertError('자재 저장 실패') }
}

const handleOpenHelp = (type: string, row: any) => {
  if (type === 'REASON') {
    openHelp('REASON', (d) => row.update({ wonincd: d.code, woninnm: d.cdnm }), { gbncd: '210' })
  } else if (type === 'MATERIAL') {
    openHelp('ITEM', (d) => row.update({ mitemcd: d.itemcd, itemnm: d.itemnm, mitsize: d.itsize, munit: d.unit, _status: '입력', _state: 'NEW' }))
  } else if (type === 'befprog') {
    openHelp('befprog', (d) => row.update({ befprog: d.progcd, prognm: d.prognm }), { gbncd: searchForm.linecd })
  }
}

const handleRowAction = (row: any) => {
  const d = row.getData();
  if (d._state === 'NEW') row.delete();
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' });
}

const addMaterialRow = () => {
  if (!selectedProduct.itemcd) return vAlertError('먼저 제품을 선택하세요.');
  grid3?.addRow({ inqty: 0, _status: '입력', _state: 'NEW', useyn: 'Y' }, true);
}

const onLineChange = () => fetchProcesses()
const onFilterChange = () => { if (selectedProg.progcd) fetchPerfData() }

const initialize = () => {
  resetForm(searchForm);
  Object.assign(searchForm, { linecd: '010', ordymd: initymd });
  grid1?.clearData(); grid2?.clearData(); grid3?.clearData();
  selectedProg.progcd = ''; selectedProg.prognm = '';
  selectedProduct.itemcd = ''; selectedProduct.itemnm = '';
}

const formatDate = (v: any) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v;
onMounted(() => {
  fetchLineOptions();
  api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => { if(r.data?.length) closingInfo.clsymd = r.data[0].clsymd })
  nextTick(initGrids);
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
.grid-container-left, .grid-container-right { border-bottom: 3px solid #005a9f !important; }
</style>
