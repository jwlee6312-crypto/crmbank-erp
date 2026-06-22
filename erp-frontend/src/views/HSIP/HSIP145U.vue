<!--
	=============================================================
	프로그램명	: 수입제비용 정산입력 [디자인 표준 통합]
	작성일자	: 2025.02.24
	설명        : HSOD100U 디자인 패턴 이식 및 ASP 패턴 기반 순차 저장 로직 적용
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
        수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">수입제비용정산입력 (HSIP145U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchDetail">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 🔍 [상단] 조회 필터 영역 (ASP 요구 파라미터 포함) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" /><col style="width: 35%" />
              <col style="width: 10%" /><col style="width: 45%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">발생일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchForm.IOYMDFR" v-model:todt="searchForm.IOYMDTO" />
                </td>
                <th class="text-center bg-light">PO No</th>
                <td>
                  <div class="input-group input-group-sm w-50">
                    <input v-model="formData.FILENO" type="text" class="form-control fw-bold text-primary" placeholder="PO 번호 입력" @keyup.enter="fetchDetail" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('PO')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 정산 정보 마스터 폼 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col style="width: 23%">
              <col style="width: 100px;" /><col style="width: 23%">
              <col style="width: 100px;" /><col style="width: 24%">
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center">정산일자</th>
                <td><input v-model="formData.PUBYMD" type="date" class="form-control" /></td>
                <th class="bg-light text-center">진행상태</th>
                <td><input :value="formData.JSANYN === 'Y' ? '정산완료' : '미정산'" class="form-control bg-light text-center fw-bold" readonly /></td>
                <th class="bg-light text-center">합계금액</th>
                <td><input :value="formatNumber(totalCostAmt)" class="form-control bg-light text-end text-danger fw-bold" readonly /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📊 3. 상세 품목 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 수입비용 정산 대상 목록
          </span>
          <span class="text-muted" style="font-size: 11px;">※ 정산할 항목을 선택 후 저장하세요.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
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
import DateForm from '@/components/DateForm.vue'

const authStore = useAuthStore()
const { firstDay, today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp: openCommonHelp } = useCommonHelp()

const searchForm = reactive({
  IOYMDFR: firstDay,
  IOYMDTO: today
})

const formData = reactive<any>({
  FILENO: '', PUBYMD: today, JSANYN: 'N'
})

const totalCostAmt = ref(0)
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchDetail = async () => {
  if (!formData.FILENO) return vAlertError('PO No를 입력하세요.')
  try {
    const params = {
      FILENO: formData.FILENO,
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.IOYMDTO.replace(/-/g, '')
    }
    const res = await api.post('/api/hsip/HSIP_145U_STR', params)
    if (res.data) {
      mainGrid?.setData(res.data)
      if (res.data.length > 0) formData.JSANYN = res.data[0].JSANYN || 'N'
    }
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

/**
 * 🚀 저장 로직 (ASP 루프 패턴 완벽 이식)
 */
const save = async () => {
  const selected = mainGrid?.getData().filter((r: any) => r.PROCYN === 'Y') || []
  if (selected.length === 0) return vAlertError('정산할 항목을 선택하세요.')

  if (!confirm('정산 처리를 진행하시겠습니까?')) return

  try {
    // 🔄 선택된 각 항목에 대해 순차적으로 프로시저 호출
    for (const item of selected) {
      // 💡 COSTCD에 따른 slipkind 분기 (ASP 로직)
      const slipKind = item.COSTCD === '200' ? '031' : '030'

      const baseParams = {
        CMPYCD: authStore.CMPYCD,
        COSTCD: item.COSTCD,
        IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
        IOYMDTO: searchForm.IOYMDTO.replace(/-/g, ''),
        DEPTCD: item.DEPTCD || authStore.deptcd,
        FILENO: formData.FILENO,
        DOCNO: item.DOCNO,
        CROWNO: item.ROWNO || item.crowno,
        jsanymD: formData.PUBYMD.replace(/-/g, ''),
        spyamt: item.costamt || item.costamt,
        vatamt: item.vatamt || 0,
        CUSTCD: item.CUSTCD,
        TAXUNIT: item.TAXUNIT || '10',
        vattype: item.vattype || '10',
        SLIPYMD: formData.PUBYMD.replace(/-/g, ''),
        slipkind: slipKind,
        HDEPTCD: item.DEPTCD || authStore.deptcd,
        BUSINESS: formData.FILENO + "-" + (item.BIGO || ''),
        UPDEMP: authStore.USERID
      }

      // 🚀 Step 1: 전표번호 채번 (A0)
      const resA = await api.post('/api/hsip/HSIP_145U_STR', { ...baseParams, ACTKIND: 'A0', slipno: '' })
      const slipNo = resA.data?.[0]?.slipno || resA.data?.[0]?.slipno

      // 🚀 Step 2: 정산 저장 (U0)
      const resU = await api.post('/api/hsip/HSIP_145U_STR', { ...baseParams, ACTKIND: 'U0', slipno: slipNo })

      const resData = resU.data?.[0]
      if (resData && (resData.FILENO === '00000000' || resData.fileno === '00000000')) {
         throw new Error(resData.BIGO || resData.bigo || '저장 중 업무 오류가 발생했습니다.')
      }
    }

    vAlert('정상적으로 정산 작업이 완료되었습니다.')
    fetchDetail()
  } catch (e: any) {
    console.error('Settlement error:', e)
    vAlertError(e.message || '저장 실패')
  }
}

const formatNumber = (val: any) => Number(val || 0).toLocaleString()

const initialize = () => {
  resetForm(formData);
  formData.PUBYMD = today
  formData.JSANYN = 'N';
  mainGrid?.clearData();
  totalCostAmt.value = 0;
}

function openHelp(type: string) {
  if (type === 'PO') {
    openCommonHelp('COMMON', (d: any) => { formData.FILENO = d.fileno; fetchDetail() }, { gubun: 'F0', cmpycd: authStore.CMPYCD, gbncd: '1' })
  }
}

onMounted(() => {
  if (mainGridRef.value) {
    mainGrid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnCalcs: "table",
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        { title: "선택", field: "PROCYN", width: 60, formatter: "tickCross", editor: true, hozAlign: "center" },
        { title: "비용코드", field: "COSTCD", width: 100 },
        { title: "비용명칭", field: "COSTNM", minWidth: 200, widthGrow: 1, cssClass: "fw-bold", hozAlign: "left" },
        { title: "발생일자", field: "PUBYMD", width: 110, formatter: (c) => {
            const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
        }},
        { title: "비용금액", field: "costamt", width: 130, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
        { title: "부가세", field: "vatamt", width: 110, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum" },
        { title: "적요", field: "BIGO", minWidth: 200, widthGrow: 1, hozAlign: "left" }
      ]
    })
    mainGrid.on('cellEdited', () => {
      totalCostAmt.value = mainGrid?.getData().filter((r:any) => r.PROCYN === 'Y').reduce((acc, cur:any) => acc + (Number(cur.costamt) || 0), 0) || 0
    })
  }
})
</script>

<style scoped>
.main-content-wrapper { padding-bottom: 0vh !important; }
.grid-container-right { border-bottom: 3px solid #005a9f !important; }
.erp-table-dense th, .erp-table-dense td {
  height: 32px !important; padding: 0 8px !important; font-size: 12px; vertical-align: middle; border: 1px solid #dee2e6;
}
.erp-table-dense .form-control, .erp-table-dense .form-select, .erp-table-dense .btn {
  height: 26px !important; font-size: 12px !important; border-radius: 2px;
}
.erp-table-dense th { font-weight: 600; color: #495057; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
