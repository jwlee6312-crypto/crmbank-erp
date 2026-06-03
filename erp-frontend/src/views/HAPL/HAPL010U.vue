<!--	=============================================================
	프로그램명	: 배부기준관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 부문별/품목별 배부기준 및 구성비 관리 (표준 디자인 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-diagram-3-fill me-2 text-primary" style="font-size: 18px;"></i>
        관리손익 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">배부기준관리 (HAPL010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="resetForm">신규</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
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
                <th>기준구분</th>
                <td>
                  <select v-model="searchForm.divgbn" class="form-select" style="max-width: 200px;" @change="handleSearchChange">
                    <option value="100">부문별 배부</option>
                    <option value="200">품목별 배부</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📝 입력 정보 카드 -->
      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header bg-white py-2 px-3 border-bottom">
          <div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1 text-secondary"></i>배부기준 설정</div>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">기준구분</th>
                <td>
                  <select v-model="formData.divgbn" class="form-select">
                    <option value="100">부문별 배부</option>
                    <option value="200">품목별 배부</option>
                  </select>
                </td>
                <th class="required">배부기준</th>
                <td>
                  <input v-model="formData.divcd" type="text" class="form-control text-center fw-bold" maxlength="3" :readonly="formData.actkind === 'U0'" />
                </td>
                <th class="required">배부기준명</th>
                <td>
                  <input v-model="formData.divnm" type="text" class="form-control" />
                </td>
              </tr>
              <tr>
                <th class="required">구성비 1 (%)</th>
                <td>
                  <input v-model="formData.rate1" type="number" class="form-control text-end" />
                </td>
                <th>구성비 2 (%)</th>
                <td>
                  <input v-model="formData.rate2" type="number" class="form-control text-end" :disabled="formData.divgbn === '200'" />
                </td>
                <th>구성비 3 (%)</th>
                <td>
                  <input v-model="formData.rate3" type="number" class="form-control text-end" :disabled="formData.divgbn === '200'" />
                </td>
              </tr>
              <tr>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td>
                  <input v-model="formData.remark" type="text" class="form-control" maxlength="30" />
                </td>
                <th class="required">출현순서</th>
                <td>
                  <input v-model="formData.dspord" type="text" class="form-control text-center" maxlength="3" />
                </td>
                <th>사용여부</th>
                <td>
                  <div class="form-check mb-0">
                    <input v-model="formData.useyn" class="form-check-input" type="checkbox" id="useynCheckHapl" true-value="Y" false-value="N">
                    <label class="form-check-label small fw-bold text-primary" for="useynCheckHapl">사용</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📊 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const searchForm = reactive({
  divgbn: '100'
})

const formData = reactive({
  actkind: 'A0', divgbn: '100', divcd: '', divnm: '', rate1: 0, rate2: 0, rate3: 0, remark: '', dspord: '', useyn: 'Y'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const resetForm = () => {
  Object.assign(formData, { actkind: 'A0', divgbn: searchForm.divgbn, divcd: '', divnm: '', rate1: 0, rate2: 0, rate3: 0, remark: '', dspord: '', useyn: 'Y' })
}

const handleSearchChange = () => { search(); resetForm() }

const search = async () => {
  try {
    const res = await api.post('/api/hapl/HAPL_010U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, divgbn: searchForm.divgbn })
    const data = (res.data || []).map((row: any) => ({
      divcd: row.divcd, divnm: row.divnm, rate1: Number(row.Srate || 0), rate2: Number(row.Mrate || 0), rate3: Number(row.Erate || 0), remark: row.remark, dspord: row.dspord, useyn: row.useyn, divgbn: searchForm.divgbn
    }))
    mainGrid?.setData(data)
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 오류') }
}

const save = async () => {
  if (!formData.divcd || !formData.divnm) return vAlertError('필수 항목을 입력하세요.')
  const rateSum = Number(formData.rate1) + Number(formData.rate2) + Number(formData.rate3)
  if (rateSum !== 100) return vAlertError('구성비의 합은 100이어야 합니다.')
  try {
    await api.post('/api/hapl/HAPL_010U_STR', { ...formData, cmpycd: authStore.cmpycd, userid: authStore.userid })
    vAlert('저장되었습니다.'); search(); resetForm();
  } catch (e) { vAlertError('저장 오류') }
}

onMounted(() => {
  if (mainGridRef.value) {
    mainGrid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
      columns: [
        { title: "배부기준", field: "divcd", width: 90, hozAlign: "center" },
        { title: "배부기준 명", field: "divnm", widthGrow: 2, cssClass: 'fw-bold text-primary' },
        { title: "구성비 1", field: "rate1", width: 90, hozAlign: "right", formatter: (c) => c.getValue() + '%' },
        { title: "구성비 2", field: "rate2", width: 90, hozAlign: "right", formatter: (c) => c.getValue() + '%' },
        { title: "구성비 3", field: "rate3", width: 90, hozAlign: "right", formatter: (c) => c.getValue() + '%' },
        { title: "비고", field: "remark", widthGrow: 1.5 },
        { title: "순서", field: "dspord", width: 60, hozAlign: "center" },
        { title: "사용", field: "useyn", width: 60, hozAlign: "center", formatter: "tickCross" }
      ]
    })
    mainGrid.on("rowClick", (e, row) => { Object.assign(formData, row.getData()); formData.actkind = 'U0' })
  }
  search()
})

watch(() => formData.divgbn, (newVal) => { if (newVal === '200') { formData.rate2 = 0; formData.rate3 = 0 } })
</script>
