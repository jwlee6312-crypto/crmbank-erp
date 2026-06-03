<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi- arrow-left-right me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        재고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">창고별 수불현황 (HSIO650S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-outline-secondary" @click="print('Print')">인쇄</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-search me-1"></i> 조회 조건</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required" style="width: 100px;">입출일자</th>
                <td style="width: 320px;">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="uifymd" type="date" class="form-control form-control-sm" />
                    <span>~</span>
                    <input v-model="uitymd" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th class="required" style="width: 100px;">창&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td style="width: 180px;">
                  <select v-model="searchData.whcd" class="form-select form-select-sm">
                    <option value="000">전체</option>
                    <option v-for="opt in whOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="required" style="width: 100px;">품&nbsp;&nbsp;&nbsp;&nbsp;목</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 450px;">
                    <input v-model="searchData.itemcd" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="searchData.itemnm" type="text" class="form-control" placeholder="품목 선택" @keyup.enter="openHelp('ITEM')" />
                    <input v-model="searchData.itsize" type="text" class="form-control bg-light text-center" style="max-width: 100px;" readonly />
                    <input v-model="searchData.unit" type="text" class="form-control bg-light text-center" style="max-width: 60px;" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('ITEM')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 품목별 상세 수불 이력</span>
          <div class="small text-muted">출고번호 또는 적요를 클릭하면 원본 전표로 이동합니다.</div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridElement" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const initymd = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initfrymd = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  fymd: (route.query.fymd as string) || initfrymd,
  tymd: (route.query.tymd as string) || initymd,
  whcd: (route.query.whcd as string) || '000',
  astkind: (route.query.astkind as string) || '120',
  itemcd: (route.query.itemcd as string) || '',
  itemnm: '',
  itsize: '',
  unit: ''
})

const uifymd = computed({ get: () => formatDateString(searchData.fymd, '-'), set: (v) => searchData.fymd = v.replace(/-/g, '') })
const uitymd = computed({ get: () => formatDateString(searchData.tymd, '-'), set: (v) => searchData.tymd = v.replace(/-/g, '') })

const totals = reactive({ IN: 0, OUT: 0, stock: 0 })
const whOptions = ref<any[]>([])
const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "조회된 데이터가 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "출고번호", field: "IO_FULL", width: 130, hozAlign: "center",
        formatter: (cell) => {
            const val = cell.getValue();
            return val ? `<span class="text-primary text-decoration-underline cursor-pointer">${val}</span>` : '';
        },
        cellClick: (e, cell) => navigateToOrigin(cell.getData())
      },
      {
        title: "적요 (거래처/유형)", field: "remark", minWidth: 250,
        formatter: (cell) => `<span class="text-primary cursor-pointer">${cell.getValue()}</span>`,
        cellClick: (e, cell) => navigateToOrigin(cell.getData())
      },
      { title: "입출구분", field: "IOtypenm", width: 100, hozAlign: "center" },
      { title: "입고", field: "inqty", width: 100, hozAlign: "right", formatter: (c) => formatQty(c) },
      { title: "출고", field: "OUtqty", width: 100, hozAlign: "right", formatter: (c) => formatQty(c) },
      {
        title: "재고", field: "stkqty", width: 110, hozAlign: "right",
        formatter: (cell) => {
          const val = Number(cell.getValue()) || 0;
          const formatted = new Intl.NumberFormat().format(val);
          return val < 0 ? `<span class="text-danger fw-bold">${formatted}</span>` : formatted;
        }
      },
      { title: "입출창고", field: "whnm", width: 120 },
      { title: "입출부서", field: "deptnm", width: 120 }
    ]
  })
}

const formatQty = (cell: any) => {
    const data = cell.getData();
    const pnt = Number(data.QTYPNT) || 0;
    return new Intl.NumberFormat(undefined, { minimumFractionDigits: pnt, maximumFractionDigits: pnt }).format(Number(cell.getValue()) || 0);
}

// 3. 기능 구현
async function fetchWhOptions() {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } })
    whOptions.value = res.data.map((i: any) => ({ codecd: Object.values(i)[0], codenm: Object.values(i)[1] }))
  } catch (e) { console.error('창고 옵션 로드 실패') }
}

async function search() {
  if (!searchData.itemcd) return vAlertError('품목을 선택해 주십시요.')
  try {
    const res = await api.post('/api/hsio/HSIO_650S_STR', {
      cmpycd: authStore.cmpycd,
      whcd: searchData.whcd,
      fymd: searchData.fymd,
      tymd: searchData.tymd,
      astkind: searchData.astkind,
      itemcd: searchData.itemcd
    })
    if (grid.value) {
      // 행별 누적 재고 계산 (ASP의 stkqty = stkqty + inqty - OUtqty 로직)
      let currentStock = 0;
      let totalIn = 0;
      let totalOut = 0;

      const processedData = res.data.map((i: any) => {
          const inQty = Number(i.inqty) || 0;
          const outQty = Number(i.OUtqty) || 0;
          currentStock = currentStock + inQty - outQty;
          totalIn += inQty;
          totalOut += outQty;

          return {
              ...i,
              IO_FULL: i.ioymd === '00000000' ? '' : `${i.ioym}-${i.iono}`,
              remark: `${i.custnm || ''} / ${i.IOtypenm || ''}`,
              stkqty: currentStock
          }
      })

      grid.value.setData(processedData)
      activeItemCount.value = processedData.length
      totals.IN = totalIn;
      totals.OUT = totalOut;
      totals.stock = currentStock;
    }
  } catch (e) { vAlertError('조회 실패') }
}

const navigateToOrigin = (row: any) => {
    const { iogbn, gubun, IOTYPE, ioym, iono, deptcd, OUtqty } = row;
    if (!ioym || !iono) return;

    let routeName = '';
    // ASP 소스의 복잡한 분기 로직 이식
    if (iogbn === "200" && gubun === "1" && IOTYPE === "100" && Number(OUtqty) > 0) routeName = 'HSIO500U';
    else if (iogbn === "200" && gubun === "2" && IOTYPE === "100" && Number(OUtqty) > 0) routeName = 'HSOD200S';
    else if (iogbn === "200" && gubun === "1" && IOTYPE === "100" && Number(OUtqty) < 0) routeName = 'HSIO490U';
    else if (iogbn === "200" && Number(IOTYPE) >= 300 && Number(IOTYPE) < 390) routeName = 'HSIO570U';
    else if (iogbn === "200" && IOTYPE === "390") routeName = 'HSIO730U';
    else if (IOTYPE === "390") routeName = 'HSIO720U';
    else if (iogbn === "200" && IOTYPE === "200") routeName = 'HSIO580U';
    else if (iogbn === "100" && gubun === "1" && IOTYPE === "100") routeName = 'HSIO100U';
    else if (iogbn === "100" && gubun === "2" && IOTYPE === "100") routeName = 'HSOD300S';
    else if (iogbn === "100" && gubun === "1" && IOTYPE === "120") routeName = 'HSIO250U';

    if (routeName) {
        router.push({
            name: routeName,
            query: { ioym, iono, deptcd, actkind: 'S' }
        });
    }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { fymd: initfrymd, tymd: initymd, whcd: '000', astkind: '120', itemcd: '', itemnm: '', itsize: '', unit: '' })
  grid.value?.clearData()
  activeItemCount.value = 0
  totals.IN = 0; totals.OUT = 0; totals.stock = 0;
}

function print(type: string) {
    vAlert(`${type} 기능은 준비 중입니다.`)
}

// 4. 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'ITEM') {
    Object.assign(modalProps, {
      title: '품목 선택',
      path: '/api/hs00/HS00_000S_STR',
      defaultField: 'itemnm',
      large: true,
      data: { gubun: 'I1', cmpycd: authStore.cmpycd, gbncd: '2', LIMITOFFSET: 0, LIMITROWS: 9999 },
      columns: [
        { title: '코드', field: 'itemcd', width: 100 },
        { title: '품목명', field: 'itemnm', minWidth: 200 },
        { title: '규격', field: 'itsize', width: 150 },
        { title: '단위', field: 'unitnm', width: 80 }
      ],
      onConfirm: (data: any) => {
        searchData.itemcd = data.itemcd
        searchData.itemnm = data.itemnm
        searchData.itsize = data.itsize
        searchData.unit = data.unitnm
        search() // 품목 선택 시 자동 조회
      }
    })
    modalVisible.value = true
  }
}

const formatDateString = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  await fetchWhOptions()
  nextTick(() => {
      initGrid()
      if (searchData.itemcd) {
          // HSIO640S에서 넘어온 경우 자동 조회
          api.get('/api/ha00/HA00_010S_STR', { params: { gubun: 'I0', cmpycd: authStore.cmpycd, CODE: searchData.itemcd } }).then(r => {
              if (r.data?.length) {
                  searchData.itemnm = r.data[0].itemnm;
                  searchData.itsize = r.data[0].itsize;
                  searchData.unit = r.data[0].unit;
                  search();
              }
          })
      }
  })
})
</script>
