<!--	=============================================================
	프로그램명	: 배부적수관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 부서별 배부 기준에 따른 배부적수 및 배부율 관리 (표준 디자인 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
        관리손익 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">배부적수관리 (HAPL030U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-search" @click="fetchLeftList">조회</button>
        <button class="btn-erp btn-save" @click="handleBatchSave">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 (gap-3 적용) -->
    <div class="flex-grow-1 overflow-auto p-3 d-flex flex-column gap-3">

      <!-- 🔍 검색 조건 카드 -->
      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
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
                <th>배부기준</th>
                <td>
                  <select v-model="searchForm.divcd" class="form-select" style="max-width: 250px;" @change="handleSearchChange">
                    <option v-for="opt in divOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📂 3. 데이터 영역 (좌측 리스트 + 우측 그리드) -->
      <div class="flex-grow-1 d-flex gap-3 overflow-hidden">
        <!-- 좌측: 부서 목록 카드 -->
        <div class="card border-0 shadow-sm d-flex flex-column flex-shrink-0" style="width: 280px;">
          <div class="card-header bg-white py-2 px-3 border-bottom">
            <div class="fw-bold small text-dark"><i class="bi bi-list-ul me-1 text-secondary"></i>비용 부서 목록</div>
          </div>
          <div class="card-body p-0 overflow-auto">
            <div class="list-group list-group-flush small">
              <button v-for="dept in leftDeptList" :key="dept.deptcd"
                class="list-group-item list-group-item-action border-0 d-flex align-items-center"
                :class="{ 'active': selectedDeptCD === dept.deptcd }"
                @click="selectDept(dept)">
                <span v-for="n in (dept.LEV - 1)" :key="n" class="ms-2"></span>
                <i v-if="dept.LEV > 1" class="bi bi-arrow-return-right me-1 opacity-50"></i>
                {{ dept.deptnm }}
              </button>
            </div>
          </div>
        </div>

        <!-- 우측: 배부적수 상세 카드 -->
        <div class="card border-0 shadow-sm flex-grow-1 d-flex flex-column overflow-hidden">
          <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-3">
              <span class="fw-bold small text-dark"><i class="bi bi-gear-fill me-1 text-secondary"></i>적수 설정</span>
              <div v-if="selectedDeptCD" class="d-flex gap-1 align-items-center ms-2">
                <span class="badge bg-primary-subtle text-primary border border-primary-subtle">{{ selectedDeptNM }}</span>
                <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">구성비1: {{ gRates.rate1 }}%</span>
                <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">구성비2: {{ gRates.rate2 }}%</span>
                <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">구성비3: {{ gRates.rate3 }}%</span>
              </div>
            </div>
            <button v-if="selectedDeptCD" class="btn btn-grid-row-add" @click="handleGenerateFactors">
              <i class="bi bi-magic"></i> 적수생성
            </button>
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
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')
const yearOptions = Array.from({ length: 10 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({
  yy: String(currentYear),
  mm: currentMonth,
  divcd: ''
})

const divOptions = ref<any[]>([])
const leftDeptList = ref<any[]>([])
const selectedDeptCD = ref('')
const selectedDeptNM = ref('')
const gRates = reactive({ rate1: 0, rate2: 0, rate3: 0 })

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const fetchOptions = async () => {
  try {
    const res = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'SB', cmpycd: authStore.cmpycd, search: '100' })
    divOptions.value = res.data || []
    if (divOptions.value.length > 0) searchForm.divcd = divOptions.value[0].code
  } catch (e) { console.error(e) }
}

const handleSearchChange = () => fetchLeftList()

const fetchLeftList = async () => {
  try {
    const res = await api.post('/api/hapl/HAPL_030U_STR', { actkind: 'S1', cmpycd: authStore.cmpycd, STDym: searchForm.yy + searchForm.mm })
    leftDeptList.value = res.data || []
    selectedDeptCD.value = ''; mainGrid?.setData([])
  } catch (e) { vAlertError('조회 실패') }
}

const selectDept = async (dept: any) => {
  selectedDeptCD.value = dept.deptcd
  selectedDeptNM.value = dept.deptnm
  try {
    const resCfg = await api.post('/api/hapl/HAPL_010U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, divcd: searchForm.divcd, divgbn: '100' })
    if (resCfg.data?.[0]) { const c = resCfg.data[0]; gRates.rate1 = c.rate1; gRates.rate2 = c.rate2; gRates.rate3 = c.rate3 }
    const res = await api.post('/api/hapl/HAPL_030U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, STDym: searchForm.yy + searchForm.mm, divcd: searchForm.divcd, deptcd: dept.deptcd })
    mainGrid?.setData(res.data || [])
  } catch (e) { vAlertError('상세 로드 실패') }
}

const handleGenerateFactors = async () => {
  if (!confirm("배부적수를 생성 하시겠습니까?")) return
  try {
    await api.post('/api/hapl/HAPL_030U_STR', { actkind: 'DR', cmpycd: authStore.cmpycd, STDym: searchForm.yy + searchForm.mm, divcd: searchForm.divcd, deptcd: selectedDeptCD.value, userid: authStore.userid })
    vAlert('정상적으로 생성되었습니다.'); selectDept({ deptcd: selectedDeptCD.value, deptnm: selectedDeptNM.value })
  } catch (e) { vAlertError('생성 오류') }
}

const handleBatchSave = async () => {
  const data = mainGrid?.getData() || []
  if (!data.length) return
  try {
    await api.post('/api/hapl/HAPL_030U_SAVE_BATCH', { cmpycd: authStore.cmpycd, STDym: searchForm.yy + searchForm.mm, divcd: searchForm.divcd, deptcd: selectedDeptCD.value, items: data, userid: authStore.userid })
    vAlert('저장되었습니다.'); selectDept({ deptcd: selectedDeptCD.value, deptnm: selectedDeptNM.value })
  } catch (e) { vAlertError('저장 실패') }
}

onMounted(() => {
  fetchOptions()
  if (mainGridRef.value) {
    mainGrid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
      columns: [
        { title: "부서", field: "deptnm", widthGrow: 1.5, cssClass: 'fw-bold' },
        { title: "배부적수 1", field: "divrate1", width: 110, hozAlign: "right", editor: "number", formatter: "money" },
        { title: "배부율 1", field: "rate1", width: 80, hozAlign: "right", formatter: (c) => c.getValue() + '%' },
        { title: "배부적수 2", field: "divrate2", width: 110, hozAlign: "right", editor: "number", formatter: "money" },
        { title: "배부율 2", field: "rate2", width: 80, hozAlign: "right", formatter: (c) => c.getValue() + '%' },
        { title: "배부적수 3", field: "divrate3", width: 110, hozAlign: "right", editor: "number", formatter: "money" },
        { title: "배부율 3", field: "rate3", width: 80, hozAlign: "right", formatter: (c) => c.getValue() + '%' }
      ]
    })
  }
  fetchLeftList()
})
</script>

<style scoped></style>
