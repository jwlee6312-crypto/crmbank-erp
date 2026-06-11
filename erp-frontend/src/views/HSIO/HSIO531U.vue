<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-ruled me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">외부매출전표 (HSIO531U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="issueSlip">전표발행</button>
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
                <th class="required">판매부서</th>
                <td style="width: 250px;">
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="searchData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchData.deptnm" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('SEARCH_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('SEARCH_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">정산일자</th>
                <td style="width: 300px;">
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="ioymdfr" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="ioymdto" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <td></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 전표 발행 설정 (등록 영역) - 시각적 차별화 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 전표 발행 정보 (등록용)</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required bg-light-primary">전표일자</th>
                <td style="width: 250px;">
                  <input v-model="uislipymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
                <th class="required bg-light-primary">발행부서</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 220px;">
                    <input v-model="registerData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="registerData.deptnm" type="text" class="form-control" placeholder="발행부서 선택" @keyup.enter="openHelp('REG_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('REG_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 정산 내역</span>
          <div class="d-flex gap-2 align-items-center">
            <span class="small text-muted">전체선택</span>
            <input type="checkbox" v-model="allSelected" @change="toggleAllSelection" class="form-check-input" />
          </div>
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
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const initymd = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
const initfrymd = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  ioymdfr: initfrymd, ioymdto: initymd, salsemp: '000'
})

const registerData = reactive({
  slipymd: initymd, deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  clsymd: '', sclsym: ''
})

const sumData = reactive({ spysum: 0, vatsum: 0, totsum: 0 })

const ioymdfr = computed({ get: () => formatDate(searchData.ioymdfr, '-'), set: (v) => searchData.ioymdfr = v.replace(/-/g, '') })
const ioymdto = computed({ get: () => formatDate(searchData.ioymdto, '-'), set: (v) => searchData.ioymdto = v.replace(/-/g, '') })
const uislipymd = computed({ get: () => formatDate(registerData.slipymd, '-'), set: (v) => registerData.slipymd = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)
const allSelected = ref(false)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "조회된 내역이 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "선택", field: "procyn", width: 60, hozAlign: "center",
        formatter: "tickCross", editor: true, cellEdited: () => updateTotals()
      },
      { title: "발행일", field: "jsanymd", width: 150, hozAlign: "center", formatter: (c) => formatDate(c.getValue(), '-') },
      { title: "부서", field: "deptnm", width: 250 },
      { title: "거래처", field: "custnm", minWidth: 200 },
      { title: "사업장", field: "unitnm", width: 200 },
      { title: "유형", field: "vattypenm", width: 150 },
      { title: "공급가", field: "spyamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "부가세", field: "vatamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "합계", field: "jsansum", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" }
    ]
  })
}

const updateTotals = () => {
  if (!grid.value) return
  const data = grid.value.getData().filter((i: any) => i.procyn)
  activeItemCount.value = data.length
  sumData.spysum = data.reduce((acc, cur) => acc + (Number(cur.spyamt) || 0), 0)
  sumData.vatsum = data.reduce((acc, cur) => acc + (Number(cur.vatamt) || 0), 0)
  sumData.totsum = sumData.spysum + sumData.vatsum
}

const toggleAllSelection = () => {
  if (!grid.value) return
  const data = grid.value.getData()
  grid.value.updateData(data.map(i => ({ ...i, procyn: allSelected.value })))
  updateTotals()
}

// 3. 기능 구현
async function search() {
  if (!searchData.deptcd) return vAlertError('판매부서를 선택하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_531U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, gubun: '200',
      ioymdfr: searchData.ioymdfr, ioymdto: searchData.ioymdto,
      deptcd: searchData.deptcd, salsemp: searchData.salsemp === '000' ? '' : searchData.salsemp
    })
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => ({
        ...i,
        jsanymd: i.jsanymd || i.jsanymd,
        procyn: false,
        jsansum: Number(i.spyamt || 0) + Number(i.vatamt || 0)
      })))
      allSelected.value = false
      updateTotals()
    }
  } catch (e) { vAlertError('조회 실패') }
}

/**
 * 🚀 전표 발행 처리 (ASP 로직 반영 + 소문자 통일)
 */
async function issueSlip() {
  const selected = grid.value?.getData().filter((i: any) => i.procyn)
  if (!selected || selected.length === 0) return vAlertError('전표발행 대상을 선택하시기 바랍니다.')

  // 마감 체크
  if (registerData.slipymd <= registerData.clsymd) return vAlertError('회계정보가 마감이 되었습니다. 해당 전표일자로 작업할 수 없습니다.')
  if (registerData.sclsym && registerData.sclsym >= registerData.slipymd.substring(0, 6)) return vAlertError('영업정보가 마감이 되었습니다. 해당 전표일자로 작업할 수 없습니다.')

  // 데이터 검증
  for (const item of selected) {
    if (!item.custcd) return vAlertError('거래처코드가 없는 항목이 있습니다.')
    if (Number(item.jsansum) === 0) return vAlertError('합계금액이 0인 항목은 발행할 수 없습니다.')
  }

  if (!confirm('전표를 발행 하시겠습니까?')) return

  try {
    const slipymd = registerData.slipymd
    const business = `${slipymd.substring(0, 4)}년 ${slipymd.substring(4, 6)}월 매출 건`
    const ioymdfr = searchData.ioymdfr
    const ioymdto = searchData.ioymdto

    // 1. 전표 마스터 발행 (A0) - ASP 패턴: A0로 먼저 번호를 땀
    const mRes = await api.post('/api/hsio/HSIO_531U_STR', {
      actkind: 'A0',
      cmpycd: authStore.cmpycd,
      gubun: '200',
      ioymdfr: ioymdfr,
      ioymdto: ioymdto,
      deptcd: '', // 마스터 발행 시에는 빈값
      jsanym: '',
      jsanno: '',
      jsanymd: '',
      spyamt: '0',
      vatamt: '0',
      custcd: '',
      taxunit: '',
      vattype: '',
      slipymd: slipymd,
      slipno: '',
      slipkind: '040',
      deptcd: registerData.deptcd,
      business: business,
      userid: authStore.userid
    })

    const resM = mRes.data?.[0]
    const slipno = resM?.slipno || resM?.slipno

    if (slipno) {
      // 2. 전표 상세 매핑 (U0) - 루프 처리
      for (const item of selected) {
        const dRes = await api.post('/api/hsio/HSIO_531U_STR', {
          actkind: 'U0',
          cmpycd: authStore.cmpycd,
          gubun: '200',
          ioymdfr: ioymdfr,
          ioymdto: ioymdto,
          deptcd: item.deptcd || item.deptcd,
          jsanym: item.jsanym,
          jsanno: item.jsanno,
          jsanymd: (item.jsanymd || item.jsanymd || '').replace(/-/g, ''),
          spyamt: String(item.spyamt || '0').replace(/,/g, ''),
          vatamt: String(item.vatamt || '0').replace(/,/g, ''),
          custcd: item.custcd,
          taxunit: item.taxunit,
          vattype: item.vattype,
          slipymd: slipymd,
          slipno: slipno,
          slipkind: '040',
          deptcd: registerData.deptcd,
          business: business,
          userid: authStore.userid
        })
        const resD = dRes.data?.[0]
        if (resD && (resD.result === 'Y' || resD.erryn === 'Y' || resD.RESULT === 'Y' || resD.ERRYN === 'Y')) {
          throw new Error(resD.msg || resD.MSG || '전표 상세 저장 중 업무 오류 발생')
        }
      }

      vAlert('정상적으로 발행되었습니다.')

      // 전표 인쇄 팝업 (ASP 로직 반영)
      const printUrl = `../HASL/HASL_SLIP_PRINT_OUT.asp?slipgu=010&SLIPYMD=${slipymd}&slipno=${slipno}&DEPTCD=${registerData.deptcd}`
      window.open(printUrl, '전표인쇄', 'left=10,top=10,width=700,height=650,scrollbars=yes')

      search()
    } else {
      vAlertError('전표 마스터 생성 실패')
    }
  } catch (e: any) {
    vAlertError(e.message || '전표 발행 실패')
  }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { deptcd: authStore.deptcd, deptnm: authStore.deptnm, ioymdfr: initfrymd, ioymdto: initymd, salsemp: '000' })
  Object.assign(registerData, { slipymd: initymd, deptcd: authStore.deptcd, deptnm: authStore.deptnm })
  if (grid.value) grid.value.clearData()
  updateTotals()
}

// 4. 팝업 및 기초정보
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type.includes('DEPT')) {
    Object.assign(modalProps, {
      title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm', large: false,
      data: { gubun: 'D0', cmpycd: authStore.cmpycd },
      columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
      onConfirm: (data: any) => {
        if (type === 'SEARCH_DEPT') { searchData.deptcd = data.deptcd; searchData.deptnm = data.deptnm }
        else { registerData.deptcd = data.deptcd; registerData.deptnm = data.deptnm }
      }
    })
    modalVisible.value = true
  }
}

const formatDate = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'cl', cmpycd: authStore.cmpycd } }).then(r => {
    if (r.data?.length) {
      const d = r.data[0]
      registerData.clsymd = String(d.clsymd || d.CLSYMD || Object.values(d)[0]).trim()
      registerData.sclsym = String(d.sclsym || d.SCLSYM || Object.values(d)[1]).trim()
    }
  })
  nextTick(() => initGrid())
})
</script>
