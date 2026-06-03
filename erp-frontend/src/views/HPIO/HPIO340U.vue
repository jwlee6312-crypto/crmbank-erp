<!--
	=============================================================
	프로그램명	: 부자재투입관리 (HPIO340U)
	작성일자	: 2025.02.24
	설명        : 생산 공정별 부자재 투입 실적 관리 (HPIO210U 표준 패턴 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">부자재투입관리 (HPIO340U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchProcesses">조회</button>
        <button class="btn-erp btn-save" @click="saveData" :disabled="!selectedProg.progcd">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 30%" />
                <col style="width: 10%" /><col style="width: 50%" />
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
                <th class="text-center bg-light required">생산일자</th>
                <td>
                  <input v-model="prodymd_f" type="date" class="form-control form-control-sm" style="max-width: 150px;" @change="onDateChange" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 투-그리드 레이아웃 영역 -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 공정 목록 (grid1) -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 250px; min-width: 250px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark text-center">공정 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ➡️ 우측: 투입 내역 그리드 (grid2) -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark">
                <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>부자재 투입 상세
                <span v-if="selectedProg.prognm" class="badge bg-primary-subtle text-primary border border-primary-subtle ms-2">{{ selectedProg.prognm }}</span>
              </span>
              <div class="small text-muted">※ 수정된 항목만 저장됩니다.</div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1"></div>
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
const searchForm = reactive({ linecd: '010', prodymd: today.replace(/-/g, '') })
const lineOptions = ref<any[]>([])
const selectedProg = reactive({ progcd: '', prognm: '' })

const prodymd_f = computed({
  get: () => searchForm.prodymd && searchForm.prodymd.length === 8 ? `${searchForm.prodymd.substring(0, 4)}-${searchForm.prodymd.substring(4, 6)}-${searchForm.prodymd.substring(6, 8)}` : '',
  set: (v) => { if (v) searchForm.prodymd = v.replace(/-/g, '') }
})

const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// [2] 그리드 초기화
const initGrids = () => {
  // Left: 공정 목록
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%", placeholder: "라인 선택", selectable: 1,
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
      { title: "공정명", field: "cdnm", hozAlign: "left", headerSort: false },
      { title: "코드", field: "code", hozAlign: "center", width: 80, cssClass: "fw-bold text-primary", headerSort: false }
    ],
  });
  grid1.on("rowClick", (e, row) => onProcessSelect(row.getData()));

  // Right: 투입 내역
  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", placeholder: "공정을 선택하세요.", selectable: true,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>';
          return '';
      }},
      { title: "자재명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: "fw-bold" },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "투입량", field: "inqty", width: 120, hozAlign: "right", editor: "number", cssClass: "bg-light-yellow fw-bold",
        cellEdited: (cell) => { const d = cell.getData(); if (d._state === 'EXIST') cell.getRow().update({ _status: '수정' }); }
      },
      { title: "비고", field: "bigo", minWidth: 200, editor: "input",
        cellEdited: (cell) => { const d = cell.getData(); if (d._state === 'EXIST') cell.getRow().update({ _status: '수정' }); }
      }
    ]
  });
}

// [3] 비즈니스 로직
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y', code: '' } })
    lineOptions.value = res.data
  } catch (e) { console.error(e) }
}

const fetchProcesses = async () => {
  if (!searchForm.linecd) { vAlertError('생산라인을 먼저 선택하세요.'); return; }
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'G0', cmpycd: authStore.cmpycd, gbncd: searchForm.linecd, code: '' } })
    grid1?.setData(res.data)
    grid2?.clearData()
    selectedProg.progcd = ''; selectedProg.prognm = '';
  } catch (e) { console.error(e) }
}

const onProcessSelect = (prog: any) => {
  selectedProg.progcd = prog.code; selectedProg.prognm = prog.cdnm
  fetchGridData()
}

const fetchGridData = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_340U_STR', {
      actkind: 's0', cmpycd: authStore.cmpycd, prodymd: searchForm.prodymd, linecd: searchForm.linecd, progcd: selectedProg.progcd
    })
    grid2?.setData(res.data.map((i: any) => ({ ...i, _state: 'EXIST', _status: '' })))
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const saveData = async () => {
  const details = grid2?.getData().filter((r: any) => r._status === '수정') || []
  if (details.length === 0) return vAlertError('저장할 변경 항목이 없습니다.')
  if (!confirm('부자재 투입 내역을 저장하시겠습니까?')) return

  try {
    for (const item of details) {
      await api.post('/api/hpio/HPIO_340U_STR', {
        actkind: 'u0', cmpycd: authStore.cmpycd, prodymd: searchForm.prodymd, linecd: searchForm.linecd, progcd: selectedProg.progcd,
        itemcd: item.itemcd, inqty: item.inqty, bigo: item.bigo, userid: authStore.userid
      })
    }
    vAlert('성공적으로 저장되었습니다.'); fetchGridData()
  } catch (e) { vAlertError('저장 처리 중 오류 발생') }
}

const onLineChange = () => fetchProcesses()
const onDateChange = () => { if (selectedProg.progcd) fetchGridData() }

const initialize = () => {
  resetForm(searchForm);
  Object.assign(searchForm, { linecd: '010', prodymd: today.replace(/-/g, '') });
  grid1?.clearData(); grid2?.clearData();
  selectedProg.progcd = ''; selectedProg.prognm = '';
}

onMounted(() => {
  fetchLineOptions();
  nextTick(initGrids);
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
.grid-container-left, .grid-container-right { border-bottom: 3px solid #005a9f !important; }
</style>
