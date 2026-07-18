<!--
	=============================================================
	프로그램명	: 수입전표취소 [디자인 표준 통합]
	작성일자	: 2025.02.24
	설명        : HSOD100U 디자인 패턴 이식 및 ASP 패턴 기반 순차 취소 로직 적용
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-x-fill me-2 text-danger" style="font-size: 18px;"></i>
        수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">수입전표취소 (HSIP150U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="save">전표 취소</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 🔍 [상단] 조회 필터 영역 (HSOD100U 디자인 패턴) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" />
              <col style="width: 40%" />
              <col style="width: 10%" />
              <col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">발생부서</th>
                <td>
                  <div class="input-group input-group-sm w-75">
                    <input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">발생일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchForm.IOYMDFR" v-model:todt="searchForm.IOYMDTO" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📊 3. 발행 전표 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 취소 대상 수입 전표 목록
          </span>
          <div class="d-flex align-items-center gap-2">
            <div class="form-check form-switch m-0 d-flex align-items-center">
              <input v-model="autoSlip" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="autoSlipSwitch">
              <label class="form-check-label ms-2 small fw-bold text-secondary" for="autoSlipSwitch">자동 전표 연동</label>
            </div>
            <span class="text-muted" style="font-size: 11px;">※ 행 선택 후 '전표 취소' 버튼을 누르세요.</span>
          </div>
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
import { ref, reactive, onMounted } from 'vue'
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
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  IOYMDFR: firstDay,
  IOYMDTO: today
})

const autoSlip = ref('N')
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchList = async () => {
  try {
    const params = {
      ACTKIND: 'S0',
      CMPYCD: authStore.CMPYCD,
      deptcd: searchForm.deptcd,
      IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.IOYMDTO.replace(/-/g, '')
    }
    const res = await api.post('/api/hsip/HSIP_150U_STR', params)
    mainGrid?.setData(res.data || [])
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

/**
 * 🚀 전표 취소 로직 (ASP 루프 패턴 완벽 이식)
 */
const save = async () => {
  const selected = mainGrid?.getSelectedData()
  if (!selected || selected.length === 0) return vAlertError('취소할 항목을 선택하세요.')

  if (!confirm('선택한 항목들의 전표를 취소하시겠습니까?')) return

  try {
    // 🔄 선택된 각 항목에 대해 순차적으로 프로시저 호출 (ASP 패턴)
    for (const item of selected) {
      const slipYmd = item.slipymd || item.slipymd
      const slipNo = item.slipno || item.slipno
      const uDeptCd = item.udeptcd || item.deptcd || searchForm.deptcd

      // 🚀 Step 1. 자동 전표 승인 취소 (ASP: HASL_020U_STR)
      if (autoSlip.value === 'Y') {
        await api.post('/api/hasl/HASL_020U_STR', {
          ACTKIND: 'A0',
          CMPYCD: authStore.CMPYCD,
          slipymd: slipYmd,
          ACCTYMD: slipYmd,
          slipno: slipNo,
          deptcd: uDeptCd,
          slipkind: '031',
          slipyn: 'Y',
          COFMYN: 'N', // 승인 취소
          UPDEMP: authStore.USERID
        })
      }

      // 🚀 Step 2. 수입전표 취소 처리 (ASP: HSIP_150U_STR 'D0')
      const params = {
        ACTKIND: 'D0',
        CMPYCD: authStore.CMPYCD,
        IOYMDFR: searchForm.IOYMDFR.replace(/-/g, ''),
        IOYMDTO: searchForm.IOYMDTO.replace(/-/g, ''),
        deptcd: uDeptCd,
        slipymd: slipYmd,
        slipno: slipNo,
        UPDEMP: authStore.USERID
      }

      const res = await api.post('/api/hsip/HSIP_150U_STR', params)
      const resData = res.data?.[0]

      // ASP: if RTRIM(rs(0)) = "Y" then (에러 발생 시 중단)
      if (resData && (resData.ERRYN === 'Y' || resData.STATUS === 'Y')) {
        throw new Error(resData.MSG || '취소 처리 중 업무 오류가 발생했습니다.')
      }
    }

    vAlert('정상적으로 전표 취소 작업이 완료되었습니다.')
    fetchList()
  } catch (e: any) {
    console.error('Cancellation error:', e)
    vAlertError(e.message || '취소 실패')
  }
}

const initialize = () => {
  resetForm(searchForm);
  mainGrid?.clearData();
}

function openHelp(type: string) {
  if (type === 'DEPT') {
    openCommonHelp('DEPT', (d) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm })
  }
}

onMounted(async () => {
  // 전표 환경 설정 체크 (ASP: HA00_010S_STR 'P1')
  try {
    const resSet = await api.post('/api/ha00/HA00_010S_STR', { CMPYCD: authStore.CMPYCD, gbn: 'P1' })
    if (resSet.data?.length > 0) autoSlip.value = resSet.data[0].slipyn || 'N'
  } catch (e) {}

  if (mainGridRef.value) {
    mainGrid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: true,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 60, hozAlign: "center" },
        { title: "전표일자", field: "slipymd", width: 110, formatter: (c) => {
            const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
        }},
        { title: "전표번호", field: "slipno", width: 100, cssClass: "fw-bold text-primary" },
        { title: "부서명", field: "deptnm", width: 120 },
        { title: "비용종류", field: "COSTNM", width: 150 },
        { title: "PO No.", field: "FILENO", width: 180 },
        { title: "정산일", field: "jsanymD", width: 110, formatter: (c) => {
            const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
        }},
        { title: "비용금액", field: "spyamt", hozAlign: "right", width: 130, formatter: "money", formatterParams: { precision: 0 } },
        { title: "상세 적요", field: "BIGO", minWidth: 200, widthGrow: 1, hozAlign: "left" }
      ]
    })
  }
  fetchList()
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
