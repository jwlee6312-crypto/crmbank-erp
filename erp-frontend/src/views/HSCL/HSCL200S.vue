<template><AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi- journals me-2 text-primary" style="font-size: 18px;"></i>
        마감관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">수불부 (HSCL200S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-outline-secondary" @click="print('Print')">인쇄</button>
        <button class="btn-erp btn-outline-success" @click="print('Excel')">엑셀</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required" style="width: 100px;">조회연월</th>
                <td style="width: 350px;">
                  <div class="d-flex align-items-center gap-2">
                    <select v-model="searchData.yy" class="form-select form-select-sm" style="width: 100px;">
                      <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
                    </select>
                    <select v-model="searchData.mm" class="form-select form-select-sm" style="width: 80px;">
                      <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
                    </select>
                    <span>~</span>
                    <select v-model="searchData.tomm" class="form-select form-select-sm" style="width: 80px;">
                      <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
                    </select>
                  </div>
                </td>
                <th class="required" style="width: 100px;">재고자산</th>
                <td>
                  <select v-model="searchData.astkind" class="form-select form-select-sm" style="width: 150px;">
                    <option v-for="opt in assetOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 품목별 월간 수불 현황</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridElement" class="tabulator-instance flex-grow-1"></div>
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
const currentYear = now.getFullYear()

// 1. 상태 관리
const searchData = reactive({
  yy: String(currentYear),
  mm: '01',
  tomm: String(now.getMonth() + 1).padStart(2, '0'),
  astkind: '120'
})

const closingInfo = reactive({
  clsymd: '', sclsym: '', wclsym: '',
  sclsym_fmt: '', wclsym_fmt: ''
})

const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))
const assetOptions = ref<any[]>([])

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)

// 2. 그리드 초기화 (다단 헤더 정의)
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      { title: "품목명", field: "itemnm", minWidth: 150, frozen: true, cssClass: "fw-bold border-end" },
      { title: "규격", field: "itsize", width: 100, frozen: true, borderRight: true },
      // 이월 그룹
      {
        title: "이 월",
        columns: [
          { title: "수량", field: "bsqty", width: 60, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "bsprice", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "bsamt", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
        ]
      },
      // 입고 그룹
      {
        title: "입 고",
        columns: [
          { title: "수량", field: "inqty", width: 60, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "inprice", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "inamt", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
        ]
      },
      // 출고 그룹
      {
        title: "출 고",
        columns: [
          { title: "수량", field: "outqty", width: 60, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "outprice", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "outamt", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
        ]
      },
      // 타계정 그룹
      {
        title: "타계정",
        columns: [
          { title: "수량", field: "outtqty", width: 60, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "outtprice", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "outtamt", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, bottomCalc: "sum", bottomCalcFormatter: "money" },
        ]
      },
      // 재고 그룹
      {
        title: "재 고",
        columns: [
          { title: "수량", field: "stkqty", width: 60, hozAlign: "right", formatter: (c) => formatQty(c) },
          { title: "단가", field: "stkprice", width: 70, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
          { title: "금액", field: "stkamt", width: 95, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-light-yellow fw-bold", bottomCalc: "sum", bottomCalcFormatter: "money" },
        ]
      }
    ]
  })
}

const formatQty = (cell: any) => {
  const data = cell.getData();
  const pnt = Number(data.qtypnt) || 0;
  const val = Number(cell.getValue()) || 0;
  return new Intl.NumberFormat(undefined, { minimumFractionDigits: pnt, maximumFractionDigits: pnt }).format(val);
}

// 3. 기능 구현
async function fetchOptions() {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '140' } })
    assetOptions.value = res.data.map((i: any) => ({ codecd: Object.values(i)[0], codenm: Object.values(i)[1] }))

    const resCl = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } })
    if (resCl.data?.length) {
      const data = resCl.data[0]
      closingInfo.clsymd = data.clsymd;
      closingInfo.sclsym = data.sclsym;
      closingInfo.wclsym = data.wclsym;
      closingInfo.sclsym_fmt = formatDateString(data.sclsym, 'ym');
      closingInfo.wclsym_fmt = formatDateString(data.wclsym, 'ym');

      searchData.yy = data.sclsym.substring(0, 4);
      searchData.mm = data.sclsym.substring(4, 6);
      searchData.T.mm = data.sclsym.substring(4, 6);
    }
  } catch (e) { console.error('기초 정보 로드 실패') }
}

async function search() {
  // ASP 유효성 체크 로직 이식
  if (searchData.mm > searchData.tomm) return vAlertError('조회년월을 확인하십시오. (시작월 > 종료월)')
  if (`${searchData.yy}${searchData.tomm}` > closingInfo.sclsym) return vAlertError('조회년월을 확인하십시오. (종료월 > 시스템 마감월)')

  try {
    const res = await api.post('/api/hscl/HSCL_200S_STR', {
      cmpycd: authStore.cmpycd,
      yymm_fr: `${searchData.yy}${searchData.mm}`,
      yymm_to: `${searchData.yy}${searchData.tomm}`,
      astkind: searchData.astkind
    })
    if (grid.value) {
      const mappedData = res.data.map((i: any) => ({
        ...i,
        bsprice: i.bsqty !== 0 ? (Number(i.bsamt) / Number(i.bsqty)) : 0,
        inprice: i.inqty !== 0 ? (Number(i.inamt) / Number(i.inqty)) : 0,
        outprice: i.outqty !== 0 ? (Number(i.outamt) / Number(i.outqty)) : 0,
        outtprice: i.outtqty !== 0 ? (Number(i.outtamt) / Number(i.outtqty)) : 0,
        stkprice: i.stkqty !== 0 ? (Number(i.stkamt) / Number(i.stkqty)) : 0,
      }))
      grid.value.setData(mappedData)
      activeItemCount.value = mappedData.length
    }
  } catch (e) { vAlertError('조회 실패') }
}

function initialize() {
  searchData.astkind = '120'
  if (closingInfo.sclsym) {
    searchData.yy = closingInfo.sclsym.substring(0, 4)
    searchData.mm = closingInfo.sclsym.substring(4, 6)
    searchData.tomm = closingInfo.sclsym.substring(4, 6)
  }
  grid.value?.clearData()
}

function print(type: string) {
  vAlert(`${type} 기능은 현재 준비 중입니다.`)
}

const formatDateString = (v: any, type: string) => {
    if (!v || v.length < 6) return v;
    if (type === 'ym') return `${v.substring(0, 4)}-${v.substring(4, 6)}`;
    return v;
}
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchOptions()
  nextTick(() => initGrid())
})
</script>

