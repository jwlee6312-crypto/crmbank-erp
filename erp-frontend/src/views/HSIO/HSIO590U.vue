<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매출정산(일괄) (HSIO590U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
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
                <th class="required">판매일자</th>
                <td style="width: 300px;">
                  <div class="d-flex align-items-center gap-1" style="width: 260px;">
                    <input v-model="fromdt" type="date" class="form-control form-control-sm" />
                    <span class="px-1">~</span>
                    <input v-model="todt" type="date" class="form-control form-control-sm" />
                  </div>
                </td>
                <th>영업사원</th>
                <td>
                  <select v-model="searchData.salsemp" class="form-select form-select-sm" style="width: 150px;">
                    <option value="000">전체</option>
                    <option v-for="opt in empOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 발행 설정 (등록 영역) - 시각적 차별화 -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 정산 발행 정보 (등록용)</span>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <tr>
                <th class="required bg-light-primary">사업장</th>
                <td style="width: 250px;">
                  <select v-model="registerData.taxunit" class="form-select form-select-sm" style="width: 220px;">
                    <option v-for="opt in taxUnitOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="required bg-light-primary">유형</th>
                <td style="width: 300px;">
                  <select v-model="registerData.vattype" class="form-select form-select-sm" style="width: 150px;">
                    <option v-for="opt in vatTypeOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="required bg-light-primary">발행일</th>
                <td>
                  <input v-model="pubymd" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 (여백 없이 꽉 채움) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 정산 대상 내역</span>
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
const initfromdt = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}01`

// 1. 상태 관리
const searchData = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  fromdt: initfromdt, todt: initymd, salsemp: authStore.userid
})

const registerData = reactive({
  taxunit: '100', vattype: '010', pubymd: initymd,
  splamt: 0, vatamt: 0, sumamt: 0,
  clsymd: '', sclsym: ''
})

const fromdt = computed({ get: () => formatDate(searchData.fromdt, '-'), set: (v) => searchData.fromdt = v.replace(/-/g, '') })
const todt = computed({ get: () => formatDate(searchData.todt, '-'), set: (v) => searchData.todt = v.replace(/-/g, '') })
const pubymd = computed({ get: () => formatDate(registerData.pubymd, '-'), set: (v) => registerData.pubymd = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)
const allSelected = ref(true)

const empOptions = ref<any[]>([])
const taxUnitOptions = ref<any[]>([])
const vatTypeOptions = ref<any[]>([])

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "조회된 내역이 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "선택", field: "procyn", width: 50, hozAlign: "center",
        formatter: "tickCross", editor: true, cellEdited: () => updateTotals()
      },
      { title: "거래처", field: "custnm", minWidth: 200 },
      { title: "판매부서", field: "deptnm", width: 150 },
      { title: "수량", field: "ioqty", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "공급가", field: "ioamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "부가세", field: "iovat", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "합계", field: "iosum", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold" }
    ]
  })
}

const updateTotals = () => {
  if (!grid.value) return
  const data = grid.value.getData().filter((i: any) => i.procyn)
  activeItemCount.value = data.length
  registerData.splamt = data.reduce((acc, cur) => acc + (Number(cur.ioamt) || 0), 0)
  registerData.vatamt = data.reduce((acc, cur) => acc + (Number(cur.iovat) || 0), 0)
  registerData.sumamt = registerData.splamt + registerData.vatamt
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
    const res = await api.post('/api/hsio/HSIO_590U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, iogbn: '200',
      fromdt: searchData.fromdt, todt: searchData.todt,
      deptcd: searchData.deptcd, saleuserid: searchData.salsemp === '000' ? '' : searchData.salsemp
    })
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => {
        const ioqty = (Number(i.qty || i.qty) || 0) - (Number(i.jqty || i.Jqty) || 0)
        const ioamt = (Number(i.amt || i.amt) || 0) - (Number(i.jamt || i.jamt) || 0)
        const iovat = (Number(i.vat || i.vat) || 0) - (Number(i.jvat || i.Jvat) || 0)
        return { ...i, procyn: true, ioqty, ioamt, iovat, iosum: ioamt + iovat }
      }))
      allSelected.value = true
      updateTotals()
    }
  } catch (e) { vAlertError('조회 실패') }
}

/**
 * 🚀 저장 처리 (ASP 로직 반영)
 */
async function save() {
  const selected = grid.value?.getData().filter((i: any) => i.procyn)
  if (!selected || selected.length === 0) return vAlertError('정산할 자료를 선택해 주십시오.')

  // 마감여부 check (ASP 로직 반영)
  const jsanymd = registerData.pubymd
  if (registerData.clsymd && registerData.clsymd >= jsanymd) {
    return vAlertError('회계정보가 마감이 되었습니다. 해당 정산일자로 작업할 수 없습니다.')
  }
  if (registerData.sclsym && registerData.sclsym >= jsanymd.substring(0, 6)) {
    return vAlertError('영업정보가 마감이 되었습니다. 해당 정산일자로 작업할 수 없습니다.')
  }

  if (confirm('정산 작업을 진행하시겠습니까?')) {
    try {
      const fromdt = searchData.fromdt
      const todt = searchData.todt
      const salsemp = searchData.salsemp === '000' ? '' : searchData.salsemp

      for (const item of selected) {
        await api.post('/api/hsio/HSIO_590U_STR', {
          actkind: 'U0',
          cmpycd: authStore.cmpycd,
          iogbn: '200',
          fromdt: fromdt,
          todt: todt,
          deptcd: item.deptcd,
          custcd: item.custcd,
          salsemp: salsemp,
          taxunit: registerData.taxunit,
          vattype: registerData.vattype,
          jsanymd: jsanymd,
          ioamt: item.ioamt,
          iovat: item.iovat,
          userid: authStore.userid
        })
      }
      vAlert('정상으로 작업이 되었습니다.')
      search()
    } catch (e) {
      vAlertError('저장 중 오류 발생')
    }
  }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, { deptcd: authStore.deptcd, deptnm: authStore.deptnm, fromdt: initfromdt, todt: initymd, salsemp: authStore.userid })
  Object.assign(registerData, { taxunit: '100', vattype: '010', pubymd: initymd, splamt: 0, vatamt: 0, sumamt: 0 })
  if (grid.value) grid.value.clearData()
  updateTotals()
}

// 4. 팝업 및 기초정보
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
  if (type === 'SEARCH_DEPT') {
    Object.assign(modalProps, {
      title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm', large: false,
      data: { gubun: 'D0', cmpycd: authStore.cmpycd },
      columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
      onConfirm: (data: any) => { searchData.deptcd = data.deptcd; searchData.deptnm = data.deptnm }
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
  api.get('/api/ha00/HA00_00P_STR', { params: { gubun: 'SD', cmpycd: authStore.cmpycd } }).then(r => {
    if (r.data) empOptions.value = r.data.map((i: any) => ({ codecd: i.userid, codenm: i.usernm }))
  })
  api.post('/api/ha00/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd }).then(r => { taxUnitOptions.value = (r.data || []).map((i:any)=>({codecd:String(i.taxunit||Object.values(i)[0]).trim(), codenm:String(i.unitnm||Object.values(i)[1]).trim()})); if(taxUnitOptions.value.length) registerData.taxunit = taxUnitOptions.value[0].codecd; });
  api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '130', cmpycd: authStore.cmpycd }).then(r => vatTypeOptions.value = (r.data || []).map((i:any)=>({codecd:String(i.codecd||Object.values(i)[0]).trim(), codenm:String(i.codenm||Object.values(i)[1]).trim()})));

  nextTick(() => initGrid())
})
</script>
