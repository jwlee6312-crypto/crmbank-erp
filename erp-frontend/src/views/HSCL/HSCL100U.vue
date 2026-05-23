<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calendar-check-fill me-2 text-primary" style="font-size: 18px;"></i>
        재고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        마감관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">월마감 작업 (HSCL100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-save" @click="executeProcess">실행</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 마감 설정 카드 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-gear-fill me-1"></i> 작업 설정</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <tbody>
              <tr>
                <th class="required" style="width: 150px;">마감 연월</th>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <select v-model="formData.YY" class="form-select form-select-sm" style="width: 100px;">
                      <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
                    </select>
                    <select v-model="formData.MM" class="form-select form-select-sm" style="width: 80px;">
                      <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
                    </select>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required">작업 방법</th>
                <td>
                  <select v-model="formData.WKGBN" class="form-select form-select-sm" style="width: 250px;">
                    <option value="Y">월마감 작업을 합니다.</option>
                    <option value="N">월마감 취소작업을 합니다.</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- ℹ️ 안내 메시지 영역 -->
      <div class="alert alert-info border-0 shadow-sm mb-0 py-2 px-3 flex-shrink-0">
        <div class="d-flex align-items-start gap-2 small">
          <i class="bi bi-info-circle-fill mt-1"></i>
          <div>
            <p class="mb-1 fw-bold text-dark">주의사항</p>
            <ul class="mb-1 ps-3">
              <li>월마감 작업을 하시면 해당 월의 입출고, 판매, 입금 자료를 입력/수정할 수 없습니다.</li>
              <li>추가 입력이 필요한 경우 반드시 월마감 취소 작업을 먼저 진행해야 합니다.</li>
              <li>원가마감이 완료된 월은 원가마감 취소 후 작업이 가능합니다.</li>
            </ul>
            <p class="mb-0 mt-2 text-primary fw-bold">※ 현재 시스템 마감 정보: [회계: {{ closingInfo.CLSYMD }}] [영업: {{ closingInfo.SCLSYM }}] [원가: {{ closingInfo.WCLSYM }}]</p>
          </div>
        </div>
      </div>

      <!-- 🅱️ 에러 내역 그리드 (재고 부족 등 오류 발생 시 표시) -->
      <div v-if="errorList.length > 0" class="card border-danger shadow-sm flex-grow-1 overflow-hidden d-flex flex-column mt-2">
        <div class="card-header bg-danger text-white py-1 px-3 d-flex align-items-center justify-content-between">
          <span class="fw-bold small"><i class="bi bi-exclamation-triangle-fill me-1"></i> 마감 오류 내역 (재고 부족 현황)</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="position: relative;">
          <div ref="gridElement" style="position: absolute; top:0; left:0; width:100%; height:100%;"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = String(now.getMonth() + 1).padStart(2, '0')

// 상태 관리
const formData = reactive({
  YY: String(currentYear),
  MM: currentMonth,
  WKGBN: 'Y'
})

const closingInfo = reactive({
  CLSYMD: '', SCLSYM: '', WCLSYM: ''
})

const errorList = ref<any[]>([])
const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

// 연도/월 옵션
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

// 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    data: errorList.value,
    columnDefaults: { headerSort: false },
    columns: [
      { title: "구분", field: "GUBUN", width: 120, hozAlign: "center" },
      { title: "에러메세지", field: "ERR_MSG", minWidth: 300 },
      { title: "품목코드", field: "ITEMCD", width: 150, hozAlign: "center" }
    ]
  })
}

// 작업 실행
async function executeProcess() {
  const ym = `${formData.YY}${formData.MM}`

  // 1. 원가마감 체크
  if (closingInfo.WCLSYM >= ym) {
    return vAlertError('해당 월은 이미 원가마감이 완료되었습니다. 원가마감 취소 후 작업하십시오.')
  }

  const actionText = formData.WKGBN === 'Y' ? '월마감' : '월마감 취소'
  if (!confirm(`${formData.YY}년 ${formData.MM}월 ${actionText} 작업을 진행하시겠습니까?`)) return

  try {
    errorList.value = []
    const actKind = formData.WKGBN === 'Y' ? 'A0' : 'D0'

    const res = await api.post('/api/hscl/HSCL_100U_STR', {
      ACTKIND: actKind,
      CMPYCD: authStore.CMPYCD,
      YM: ym,
      USERID: authStore.USERID
    })

    const result = res.data?.[0]
    const resultCode = result ? Object.values(result)[0] : ''

    if (resultCode !== '000') {
      vAlertError('마감처리 중 재고가 부족하거나 오류가 발생했습니다. 하단 내역을 확인하십시오.')
      errorList.value = res.data.map((i: any) => ({
        GUBUN: Object.values(i)[0],
        ERR_MSG: Object.values(i)[1],
        ITEMCD: Object.values(i)[2]
      }))
      await nextTick()
      initGrid()
    } else {
      vAlert('정상적으로 작업이 완료되었습니다.')
      fetchClosingInfo() // 마감 정보 갱신
    }
  } catch (e) {
    vAlertError('작업 중 통신 오류가 발생했습니다.')
  }
}

// 초기 마감 정보 조회
async function fetchClosingInfo() {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', {
      params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD }
    })
    if (res.data?.length) {
      const data = res.data[0]
      closingInfo.CLSYMD = String(data.CLSYMD || '').trim()
      closingInfo.SCLSYM = String(data.SCLSYM || '').trim()
      closingInfo.WCLSYM = String(data.WCLSYM || '').trim()

      // 초기 연월 설정 (영업마감월 기준)
      if (closingInfo.SCLSYM) {
        formData.YY = closingInfo.SCLSYM.substring(0, 4)
        formData.MM = closingInfo.SCLSYM.substring(4, 6)
      }
    }
  } catch (e) { console.error('마감 정보 로드 실패') }
}

onMounted(() => {
  fetchClosingInfo()
})
</script>

<style scoped>
</style>
