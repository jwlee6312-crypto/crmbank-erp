<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hpcl110u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-currency-dollar me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">제조원가등록(수작업) (HPCL110U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-2">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchList">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="saveData">
          <i class="bi bi-save"></i> 저장
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border-0 shadow-sm overflow-hidden" style="border-radius: 8px;">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required">입고연월</th>
                <td>
                  <div class="d-flex align-items-center gap-2" style="width: 250px;">
                    <select v-model="searchData.YY" class="form-select form-select-sm">
                      <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}년</option>
                    </select>
                    <select v-model="searchData.MM" class="form-select form-select-sm">
                      <option v-for="month in monthOptions" :key="month" :value="month">{{ month }}월</option>
                    </select>
                  </div>
                </td>
                <td class="text-muted small">
                  <i class="bi bi-info-circle me-1"></i> 생산 월마감(HPCL100U) 작업이 완료된 월에 대해서만 원가 등록이 가능합니다.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅲 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column" style="border-radius: 8px;">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-check me-1 text-primary"></i> 제품별 제조원가 리스트</span>
          <div class="small text-muted">※ 단가 또는 금액을 수정하면 자동으로 계산되어 반영됩니다.</div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 정보 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center w-100">
        <div class="col-md-4 small">제품건수: <span class="fw-bold text-info">{{ itemCount }}</span> 건</div>
        <div class="col-md-8 text-end text-muted small">
          현재 생산마감월: <span class="fw-bold text-warning">{{ closingInfo.PCLSYM }}</span> |
          원가마감월: <span class="fw-bold text-success">{{ closingInfo.WCLSYM }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()

// 1. 상태 관리
const searchData = reactive({
  YY: String(now.getFullYear()),
  MM: String(now.getMonth() + 1).padStart(2, '0')
})

const yearOptions = ref<string[]>([])
const monthOptions = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
const closingInfo = reactive({ CLSYMD: '', SCLSYM: '', PCLSYM: '', WCLSYM: '' })

const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
const itemCount = ref(0)

const generateYearOptions = () => {
    const currentYear = new Date().getFullYear()
    for (let i = 0; i < 5; i++) {
        yearOptions.value.push(String(currentYear - i))
    }
}

// 2. 그리드 초기화
const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      placeholder: "조회된 데이터가 없습니다.",
      selectable: true,
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 50, hozAlign: "center", headerSort: false },
        { title: "제품코드", field: "ITEMCD", width: 120, hozAlign: "center" },
        { title: "제품명", field: "ITEMNM", minWidth: 200, cssClass: "fw-bold" },
        { title: "규격", field: "ITSIZE", width: 150 },
        { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
        { title: "수량", field: "INQTY", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: (c:any)=>c.getData().QTYPNT||0 } },
        {
          title: "단 가", field: "PRICE", width: 120, hozAlign: "right",
          editor: "number", formatter: "money", formatterParams: { precision: 2 },
          cssClass: "bg-light-yellow"
        },
        {
          title: "금 액", field: "INAMT", width: 130, hozAlign: "right",
          editor: "number", formatter: "money", formatterParams: { precision: 0 },
          cssClass: "bg-light-blue fw-bold"
        }
      ],
    })

    grid.on("cellEdited", (cell) => {
        const field = cell.getField()
        const data = cell.getData()
        const qty = Number(data.INQTY || 0)

        if (field === 'PRICE') {
            const price = Number(data.PRICE || 0)
            cell.getRow().update({ INAMT: Math.round(qty * price) })
        } else if (field === 'INAMT') {
            const amt = Number(data.INAMT || 0)
            if (qty !== 0) {
                cell.getRow().update({ PRICE: Number((amt / qty).toFixed(2)) })
            }
        }
        cell.getRow().select()
    })
  }
}

// 3. 비즈니스 로직
const fetchClosingStatus = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } })
    if (res.data?.length) {
      closingInfo.CLSYMD = String(Object.values(res.data[0])[0]).trim()
      closingInfo.SCLSYM = String(Object.values(res.data[0])[1]).trim()
      closingInfo.PCLSYM = String(Object.values(res.data[0])[2]).trim()
      closingInfo.WCLSYM = String(Object.values(res.data[0])[3]).trim()

      // 마감월 기준으로 초기 세팅
      if (closingInfo.PCLSYM.length === 6) {
          searchData.YY = closingInfo.PCLSYM.substring(0, 4)
          searchData.MM = closingInfo.PCLSYM.substring(4, 6)
      }
    }
  } catch (e) {}
}

const fetchList = async () => {
  try {
    const res = await api.post('/api/hpcl/HPCL_110U_STR', {
      ACTKIND: 'S',
      CMPYCD: authStore.CMPYCD,
      YM: searchData.YY + searchData.MM
    })

    const mapped = res.data.map((item: any) => ({
        ...item,
        PRICE: Number(item.INQTY) !== 0 ? Number((Number(item.INAMT) / Number(item.INQTY)).toFixed(2)) : 0
    }))

    grid?.setData(mapped)
    itemCount.value = mapped.length
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const saveData = async () => {
  const ym = searchData.YY + searchData.MM

  // 1. 생산 마감 체크
  if (ym > closingInfo.PCLSYM) {
      return vAlertError(`생산정보 마감작업(${closingInfo.PCLSYM}) 후 작업하시기 바랍니다.`)
  }

  const selected = grid?.getSelectedData() || []
  if (selected.length === 0) return vAlertError('저장할 항목을 선택하세요.')

  if (!confirm('선택한 품목의 제조원가를 저장하시겠습니까?')) return

  try {
    for (const item of selected) {
      await api.post('/api/hpcl/HPCL_110U_STR', {
        ACTKIND: 'U0',
        CMPYCD: authStore.CMPYCD,
        YM: ym,
        ITEMCD: item.ITEMCD,
        INQTY: item.INQTY,
        INAMT: item.INAMT,
        USERID: authStore.USERID
      })
    }
    vAlert('정상적으로 저장되었습니다.')
    fetchList()
  } catch (e) { vAlertError('저장 처리 중 오류 발생') }
}

const initialize = () => {
  generateYearOptions()
  fetchClosingStatus()
  grid?.clearData()
  itemCount.value = 0
}

const exportExcel = () => {
  grid?.download("xlsx", `제조원가등록_${searchData.YY}${searchData.MM}.xlsx`, { title: "제품 제조원가 등록(수작업)" })
}

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)
const formatDateString = (v: any, sep: string) => v && String(v).length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : (v || '')

onMounted(() => {
  generateYearOptions()
  fetchClosingStatus()
  nextTick(() => initGrid())
})
</script>

<style scoped>
.hpcl110u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; background-color: #f4f7fa !important; }
.erp-header { background-color: #ffffff !important; }

.btn-erp { padding: 5px 14px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; border: none; }
.btn-init { background-color: #f8f9fa !important; color: #495057 !important; border: 1px solid #ced4da !important; }
.btn-init:hover { background-color: #e9ecef !important; }
.btn-search { background-color: #4361ee !important; color: #fff !important; }
.btn-save { background-color: #2ec4b6 !important; color: #fff !important; }
.btn-excel { background-color: #107c41 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 11.5px; padding: 10px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 12px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }

:deep(.tabulator) { border: none; font-size: 12.5px; border-radius: 0 0 8px 8px; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; font-weight: 700; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; color: #333; }
:deep(.tabulator-row.tabulator-selected) { background-color: #eef2ff !important; }

.bg-light-yellow { background-color: #fffde7 !important; }
.bg-light-blue { background-color: #e0f2fe !important; }
</style>
