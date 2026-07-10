<!--
	=============================================================
	프로그램명	: 제조원가 수식편집 (HFBA202U)
	작성일자	: 2025.02.24
	설명        : 제조원가 보고서 구성을 위한 계정 및 항목별 수식 관리
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-gear-fill me-2 text-primary" style="font-size: 18px;"></i>
        원가관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        기준정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">제조원가 수식편집 (HFBA202U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="handleSearch">조회</button>
        <button class="btn-erp btn-save" @click="saveAll">전체저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 조건 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-2 bg-white d-flex align-items-center gap-4">
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-muted">적용년도</span>
              <select v-model="searchForm.yyyy" class="form-select form-select-sm" style="width: 100px;">
                <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
              </select>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-muted">제표구분</span>
              <select v-model="searchForm.gubun" class="form-select form-select-sm" style="width: 160px;">
                <option value="030">030. 제조원가명세서</option>
                <option value="010">010. 대차대조표(BS)</option>
                <option value="020">020. 손익계산서(IS)</option>
              </select>
            </div>
            <div class="ms-auto"><span class="badge bg-light text-dark border small">Standard V1.0</span></div>
        </div>
      </div>

      <!-- [하단] 좌우 분할 그리드 -->
      <div class="flex-grow-1 d-flex gap-2 overflow-hidden">

        <!-- 좌측: 보고서 라인 항목 (40%) -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden" style="flex: 4;">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
            <span class="fw-bold small"><i class="bi bi-list-columns-reverse me-2 text-primary"></i>출력 라인 정의</span>
            <div class="btn-group btn-group-sm">
              <button class="btn btn-outline-primary py-0" style="height:22px" @click="addMasterRow">추가</button>
              <button class="btn btn-outline-danger py-0" style="height:22px" @click="deleteMasterRow">삭제</button>
            </div>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white">
            <div ref="masterGridRef" class="tabulator-instance h-100"></div>
          </div>
        </div>

        <!-- 우측: 세부 수식 편집 (60%) -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden" style="flex: 6;">
          <div class="card-header bg-white p-0 border-bottom">
            <ul class="nav nav-tabs erp-nav-tabs px-3 border-0">
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'A1' }" @click="activeTab = 'A1'">1. 계정합산</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'A2' }" @click="activeTab = 'A2'">2. 항목집계</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'A3' }" @click="activeTab = 'A3'">3. 차감항목</a>
              </li>
            </ul>
          </div>

          <div class="card-body p-0 flex-grow-1 d-flex flex-column bg-white overflow-hidden">
            <!-- 선택된 항목 명칭 바 -->
            <div class="px-3 py-2 border-bottom bg-light d-flex justify-content-between align-items-center">
              <div class="fw-bold text-primary small">
                <i class="bi bi-chevron-right me-1"></i>
                {{ selectedItem.prtacctnm ? `[${selectedItem.prtseq}] ${selectedItem.prtacctnm} - 세부 수식` : '항목을 선택하세요' }}
              </div>
              <div class="btn-group btn-group-sm">
                <button class="btn btn-outline-primary py-0" @click="addSubRow"><i class="bi bi-plus-lg"></i> 행추가</button>
                <button class="btn btn-outline-danger py-0" @click="deleteSubRow"><i class="bi bi-trash"></i></button>
              </div>
            </div>

            <!-- 탭별 그리드 전환 -->
            <div class="flex-grow-1 relative">
              <div v-show="activeTab === 'A1'" ref="gridA1Ref" class="tabulator-instance h-100"></div>
              <div v-show="activeTab === 'A2'" ref="gridA2Ref" class="tabulator-instance h-100"></div>
              <div v-show="activeTab === 'A3'" ref="gridA3Ref" class="tabulator-instance h-100"></div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const authStore = useAuthStore()

const yearOptions = ref<string[]>([])
const searchForm = reactive({ yyyy: new Date().getFullYear().toString(), gubun: '030' })
const activeTab = ref('A1')
const selectedItem = reactive({ prtseq: '', prtacctnm: '', prtacct: '' })

const masterGridRef = ref<HTMLElement | null>(null)
const gridA1Ref = ref<HTMLElement | null>(null)
const gridA2Ref = ref<HTMLElement | null>(null)
const gridA3Ref = ref<HTMLElement | null>(null)

let grids: any = { master: null, A1: null, A2: null, A3: null }

const initGrids = () => {
  const baseCfg: any = { layout: 'fitColumns', height: '100%', placeholder: "데이터가 없습니다." }

  // 1. 마스터 그리드 (소문자 필드 매핑)
  grids.master = new Tabulator(masterGridRef.value!, {
    ...baseCfg,
    columns: [
      { title: "순번", field: "prtseq", width: 60, hozAlign: "center", editor: "input" },
      { title: "항목명칭", field: "prtacctnm", hozAlign: "left", editor: "input", minWidth: 200 },
      { title: "대표계정", field: "prtacct", width: 120, hozAlign: "center", editor: "input" },
      { title: "D/C", field: "dc", width: 80, hozAlign: "center", editor: "list", editorParams: { values: ["D", "C"] } },
      { title: "표시", field: "dspyn", width: 80, hozAlign: "center", editor: "list", editorParams: { values: ["Y", "N"] } },
      { title: "수정", field: "_status", width: 80, hozAlign: "center", formatter: (c:any) => c.getValue() === 'u' ? '📝' : '' }
    ]
  })
  grids.master.on("rowClick", (e, row) => {
    const d = row.getData()
    selectedItem.prtseq = String(d.prtseq || '')
    selectedItem.prtacctnm = String(d.prtacctnm || '')
    selectedItem.prtacct = String(d.prtacct || '')
    fetchSubData(selectedItem.prtseq)
  })
  grids.master.on("cellEdited", (cell) => cell.getRow().update({ _status: 'u' }))

  // 2. 서브 그리드 공통 설정
  const subCols = (isA1: boolean) => {
    const cols = [
      { title: "SEQ", field: "calseq", width: 50, hozAlign: "center", editor: "input" },
      { title: "대상코드", field: "calacct", width: 100, editor: "input" },
      { title: "대상계정/항목명", field: "calacctnm", hozAlign: "left", editor: "input", minWidth: 200 },
      { title: "가감", field: "calgagam", width: 100, hozAlign: "center", editor: "list", editorParams: { values: ["+", "-"] } },
      { title: "비고", field: "bigo", width: 150, editor: "input" },
      { title: "U", field: "_status", width: 100, formatter: (c:any) => c.getValue() === 'u' ? '✔' : '' }
    ]
    if(isA1) cols.splice(3, 0, { title: "D/C", field: "dc", width: 60, editor: "input" }, { title: "로직", field: "calcaulm", width: 60, editor: "input" })
    return cols
  }

  grids.A1 = new Tabulator(gridA1Ref.value!, { ...baseCfg, columns: subCols(true) })
  grids.A2 = new Tabulator(gridA2Ref.value!, { ...baseCfg, columns: subCols(false) })
  grids.A3 = new Tabulator(gridA3Ref.value!, { ...baseCfg, columns: subCols(false) })

  const setU = (cell:any) => cell.getRow().update({ _status: 'u' })
  grids.A1.on("cellEdited", setU); grids.A2.on("cellEdited", setU); grids.A3.on("cellEdited", setU)
}

const handleSearch = async () => {
  try {
    const { data } = await api.post('/api/hfba/FBA2020U_SEL', {
      actkind: 'S0', cmpycd: authStore.cmpycd, yyyy: searchForm.yyyy, gubun: searchForm.gubun, prtseq: ''
    })
    // 수신 데이터 키 소문자 변환
    grids.master.setData(data.map((i:any) => {
        const item:any = {};
        for(let key in i) item[key.toLowerCase()] = i[key];
        return {...item, _status: ''};
    }))
    clearSub()
  } catch (e) { vAlertError('조회 오류') }
}

const fetchSubData = async (prtseq: string) => {
  const load = (gbn: string) => api.post('/api/hfba/FBA2020U_SEL', {
    actkind: gbn, cmpycd: authStore.cmpycd, yyyy: searchForm.yyyy, gubun: searchForm.gubun, prtseq
  })
  try {
    const [r1, r2, r3] = await Promise.all([load('S1'), load('S2'), load('S3')])
    const formatter = (list:any[]) => list.map((i:any) => {
        const item:any = {};
        for(let key in i) item[key.toLowerCase()] = i[key];
        return {...item, _status: ''};
    })
    grids.A1.setData(formatter(r1.data))
    grids.A2.setData(formatter(r2.data))
    grids.A3.setData(formatter(r3.data))
  } catch (e) { vAlertError('상세 조회 실패') }
}

const saveAll = async () => {
  if (!confirm('변경된 모든 정보를 저장하시겠습니까?')) return
  try {
    // 마스터 저장 (A0)
    const mRows = grids.master.getData().filter((r:any) => r._status === 'u')
    for (const r of mRows) {
      await api.post('/api/hfba/FBA2020U_MOD', {
        actkind: 'A0', cmpycd: authStore.cmpycd, yyyy: searchForm.yyyy, gubun: searchForm.gubun,
        prtseq: r.prtseq, prtacct: r.prtacct, prtacctnm: r.prtacctnm, dspyn: r.dspyn, dsprl: r.dsprl || 'L',
        upacct: r.upacct || '', bigo: r.bigo || '', updemp: authStore.userid
      })
    }
    // 서브 저장 (A1, A2, A3)
    const activeGrid = grids[activeTab.value]
    const sRows = activeGrid.getData().filter((r:any) => r._status === 'u')
    for (const r of sRows) {
      await api.post('/api/hfba/FBA2020U_MOD', {
        actkind: activeTab.value, cmpycd: authStore.cmpycd, yyyy: searchForm.yyyy, gubun: searchForm.gubun,
        prtseq: selectedItem.prtseq, prtacct: selectedItem.prtacct, prtacctnm: selectedItem.prtacctnm,
        calseq: r.calseq, calacct: r.calacct, calacctnm: r.calacctnm, dc: r.dc || '',
        calcaulm: r.calcaulm || '', calgagam: r.calgagam, bigo: r.bigo || '', updemp: authStore.userid
      })
    }
    vAlert('저장 완료'); handleSearch()
  } catch (e) { vAlertError('저장 실패') }
}

const addMasterRow = () => grids.master.addRow({ prtseq: '99', dspyn: 'Y', dc: 'D', _status: 'u' })
const addSubRow = () => {
  if (!selectedItem.prtseq) return vAlertError('항목을 선택하세요.')
  grids[activeTab.value].addRow({ calgagam: '+', _status: 'u' })
}

const deleteMasterRow = async () => {
  const row = grids.master.getSelectedData()[0]
  if (!row || !confirm('삭제하시겠습니까?')) return
  await api.post('/api/hfba/FBA2020U_DEL', { actkind: 'D0', cmpycd: authStore.cmpycd, yyyy: searchForm.yyyy, gubun: searchForm.gubun, prtseq: row.prtseq })
  vAlert('삭제 완료'); handleSearch()
}

const deleteSubRow = async () => {
  const grid = grids[activeTab.value]
  const row = grid.getSelectedData()[0]
  if (!row || !confirm('행을 삭제하시겠습니까?')) return
  const kind = activeTab.value === 'A1' ? 'D1' : (activeTab.value === 'A2' ? 'D2' : 'D3')
  await api.post('/api/hfba/FBA2020U_DEL', { actkind: kind, cmpycd: authStore.cmpycd, yyyy: searchForm.yyyy, gubun: searchForm.gubun, prtseq: selectedItem.prtseq, calseq: row.calseq })
  grid.deleteRow(grid.getSelectedRows()[0])
}

const clearSub = () => {
  selectedItem.prtseq = ''; selectedItem.prtacctnm = ''
  grids.A1.clearData(); grids.A2.clearData(); grids.A3.clearData()
}

const initialize = () => {
  searchForm.yyyy = new Date().getFullYear().toString()
  searchForm.gubun = '030'
  handleSearch()
}

onMounted(() => {
  const curY = new Date().getFullYear()
  for (let i = 0; i < 5; i++) yearOptions.value.push((curY - i).toString())
  nextTick(initGrids)
  handleSearch()
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; border: none !important; font-size: 13px; }
.erp-nav-tabs .nav-link { cursor: pointer; padding: 8px 16px; font-weight: 700; color: #6c757d; border: none; }
.erp-nav-tabs .nav-link.active { color: #005a9f; border-bottom: 3px solid #005a9f !important; background: transparent; }
</style>
