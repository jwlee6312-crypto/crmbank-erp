<!--	=============================================================
	프로그램명	: 부서집계수식편집
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 부서별 손익 집계를 위한 하위 부서 구성 및 가중치 설정 (HSOD 표준 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
        관리손익 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">부서집계수식 (HAPL040U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-search" @click="fetchLeftList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-3 d-flex flex-column gap-3">

      <!-- 🔍 검색 조건 카드 -->
      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 120px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th>기준연월</th>
                <td>
                  <div class="d-flex align-items-center gap-1">
                    <select v-model="searchForm.yy" class="form-select" style="width: 110px;" @change="handleSearchChange">
                      <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
                    </select>
                    <select v-model="searchForm.mm" class="form-select" style="width: 90px;" @change="handleSearchChange">
                      <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
                    </select>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📂 3. 데이터 영역 (좌측 트리 + 우측 편집) -->
      <div class="flex-grow-1 d-flex gap-3 overflow-hidden">
        <!-- 좌측: 집계 부서 목록 카드 -->
        <div class="card border-0 shadow-sm d-flex flex-column flex-shrink-0" style="width: 300px;">
          <div class="card-header bg-white py-2 px-3 border-bottom text-center">
            <div class="fw-bold small text-dark"><i class="bi bi-diagram-2 me-1 text-secondary"></i>집계 부서 목록</div>
          </div>
          <div class="card-body p-0 overflow-auto">
            <div class="list-group list-group-flush small">
              <button v-for="dept in leftDeptList" :key="dept.deptcd"
                class="list-group-item list-group-item-action border-0 d-flex align-items-center"
                :class="{ 'active': selectedDeptCD === dept.deptcd }"
                @click="selectDept(dept)">
                <span v-for="n in (dept.lev - 1)" :key="n" class="ms-2"></span>
                <i v-if="dept.lev > 1" class="bi bi-arrow-return-right me-1 opacity-50"></i>
                {{ dept.deptnm }}
              </button>
            </div>
          </div>
        </div>

        <!-- 우측: 수식 설정 카드 -->
        <div class="card border-0 shadow-sm flex-grow-1 d-flex flex-column overflow-hidden">
          <div class="card-header bg-white py-2 px-3 border-bottom">
            <div class="fw-bold small text-dark">
              <i class="bi bi-gear-fill me-1 text-secondary"></i>수식 설정
              <span v-if="selectedDeptCD" class="ms-2 text-primary">[{{ selectedDeptCD }}] {{ selectedDeptNM }}</span>
            </div>
          </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
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

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')
const yearOptions = Array.from({ length: 11 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({ yy: String(currentYear),.mm: currentMonth })
const leftDeptList = ref<any[]>([])
const selectedDeptCD = ref('')
const selectedDeptNM = ref('')

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const handleSearchChange = () => fetchLeftList()

const fetchLeftList = async () => {
  try {
    const res = await api.post('/api/hapl/HAPL_040U_STR', { actkind: 'S1', cmpycd: authStore.cmpycd, STDym: searchForm.yy + searchForm.mm })
    leftDeptList.value = (res.data || []).map((r: any) => ({ deptcd: r.deptcd, deptnm: r.deptnm, lev: Number(r.lev || 1) }))
    selectedDeptCD.value = ''; mainGrid?.setData([])
  } catch (e) { vAlertError('조회 실패') }
}

const selectDept = async (dept: any) => {
  selectedDeptCD.value = dept.deptcd; selectedDeptNM.value = dept.deptnm
  try {
    const res = await api.post('/api/hapl/HAPL_040U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, STDym: searchForm.yy + searchForm.mm, deptcd: dept.deptcd })
    mainGrid?.setData((res.data || []).map((r: any) => ({ ...r, _selected: r.useyn === 'Y' })))
  } catch (e) { vAlertError('상세 로드 실패') }
}

const save = async () => {
  if (!selectedDeptCD.value) return vAlertError('부서를 선택하세요.')
  const data = mainGrid?.getData() || []
  try {
    await api.post('/api/hapl/HAPL_040U_SAVE_BATCH', {
      cmpycd: authStore.cmpycd, STDym: searchForm.yy + searchForm.mm, deptcd: selectedDeptCD.value,
      items: data.map((it: any) => ({ Cdeptcd: it.deptcd, useyn: it._selected ? 'Y' : 'N', rate: it._selected ? '100' : '0', remark: it.remark || '' })),
      userid: authStore.userid
    })
    vAlert('저장되었습니다.'); selectDept({ deptcd: selectedDeptCD.value, deptnm: selectedDeptNM.value })
  } catch (e) { vAlertError('저장 실패') }
}

onMounted(() => {
  if (mainGridRef.value) {
    mainGrid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
      columns: [
        { title: "선택", field: "_selected", width: 60, hozAlign: "center", formatter: "tickCross", cellClick: (e, cell) => cell.setValue(!cell.getValue()) },
        { title: "부서", field: "deptnm", widthGrow: 1.5, cssClass: 'fw-bold', formatter: (cell) => {
          const row = cell.getData(); const indent = '&nbsp;'.repeat((row.lev - 1) * 3);
          const icon = row.lev > 1 ? '<i class="bi bi-arrow-return-right me-1 opacity-50"></i>' : '';
          return `<span>${indent}${icon}${cell.getValue()}</span>`
        }},
        { title: "비고", field: "remark", widthGrow: 2, editor: "input" }
      ]
    })
  }
  fetchLeftList()
})
</script>

<style scoped></style>
