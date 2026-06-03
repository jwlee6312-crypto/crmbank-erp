<!--
	=============================================================
	프로그램명	: 계정과목관리 (HABA010U)
	작성일자	: 2025.02.24
	설명        : 계정과목 등록 및 속성 설정 (소문자 원칙 및 인터셉터 표준화 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-diagram-3-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">계정과목관리 (HABA010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">신규</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-3 d-flex flex-column gap-3">
      <!-- 조회 조건 -->
      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <tbody>
              <tr>
                <th style="width: 120px;">재무제표</th>
                <td>
                  <select v-model="searchForm.gubun" class="form-select" style="max-width: 200px;" @change="search">
                    <option v-for="opt in statementOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                  </select>
                </td>
                <th style="width: 120px;">계 정 명</th>
                <td>
                  <input v-model="searchForm.acctnm" type="text" class="form-control" placeholder="계정명 검색" @keydown.enter="search" style="max-width: 300px;" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 상세 정보 -->
      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <tbody>
              <tr>
                <th class="required" style="width: 110px;">계정과목</th>
                <td><input v-model="masterForm.acctcd" type="text" class="form-control text-center fw-bold" maxlength="7" :readonly="masterForm.actkind === 'u1'" /></td>
                <th class="required" style="width: 110px;">계 정 명</th>
                <td><input v-model="masterForm.acctnm" type="text" class="form-control" maxlength="30" /></td>
                <th style="width: 110px;">상위계정</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterForm.upacct" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 70px;" readonly />
                    <input v-model="masterForm.upacct_t" type="text" class="form-control" placeholder="선택" @keydown.enter="openHelp('upacct')" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('upacct')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
              <tr>
                <th>차대구분</th>
                <td>
                  <select v-model="masterForm.typedc" class="form-select">
                    <option value="D">차변</option>
                    <option value="C">대변</option>
                  </select>
                </td>
                <th>전표발생</th>
                <td>
                  <select v-model="masterForm.slipyn" class="form-select">
                    <option value="Y">발행</option>
                    <option value="N">미발행</option>
                  </select>
                </td>
                <th>사용여부</th>
                <td>
                  <select v-model="masterForm.useyn" class="form-select">
                    <option value="Y">사용</option>
                    <option value="N">미사용</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 그리드 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
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
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp: commonOpenHelp } = useCommonHelp()

const searchForm = reactive({ gubun: '010', acctnm: '' })
const masterForm = reactive({
  actkind: 'i1', gubun: '010', acctcd: '', acctnm: '', upacct: '', upacct_t: '', typedc: 'D', slipyn: 'Y', useyn: 'Y'
})

const statementOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const search = async () => {
  try {
    const res = await api.post('/api/haba/HABA_010U_STR', { actkind: 's2', cmpycd: authStore.cmpycd, gubun: searchForm.gubun, acctnm: searchForm.acctnm });
    mainGrid?.setData(res.data);
  } catch (e) { vAlertError('조회 실패') }
}

const save = async () => {
  if (!masterForm.acctcd || !masterForm.acctnm) return vAlertError('필수 항목을 입력하세요.');
  try {
    const res = await api.post('/api/haba/HABA_010U_STR', { ...masterForm, cmpycd: authStore.cmpycd });
    if (res.data[0].result === 'y' || res.data[0].result === 'Y') { vAlert('저장되었습니다.'); search(); }
  } catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
  resetForm(masterForm);
  Object.assign(masterForm, { actkind: 'i1', gubun: searchForm.gubun, typedc: 'D', slipyn: 'Y', useyn: 'Y' });
}

function openHelp(type: string) {
  if (type === 'upacct') {
    commonOpenHelp('ACCT', (d) => { masterData.upacct = d.acctcd; masterData.upacct_t = d.acctnm; });
  }
}

onMounted(async () => {
  api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '070' }).then(r => {
    statementOptions.value = (r.data || []).filter((i: any) => i.code <= '030');
  });

  mainGrid = new Tabulator(mainGridRef.value!, {
    layout: "fitColumns", height: "100%", selectable: 1,
    columns: [
      { title: "코드", field: "acctcd", width: 100, hozAlign: "center" },
      { title: "계정과목명", field: "acctnm", minWidth: 200 },
      { title: "차대", field: "typedc", width: 80, hozAlign: "center" },
      { title: "전표", field: "slipyn", width: 80, hozAlign: "center", formatter: "tickCross" }
    ]
  });
  mainGrid.on("rowClick", (e, row) => { Object.assign(masterForm, row.getData()); masterForm.actkind = 'u1'; });
  search();
})
</script>
