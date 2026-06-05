<!--
	=============================================================
	프로그램명	: 매출전표취소 (HSIO541U)
	작성일자	: 2025.02.24
	설명        : 발행된 매출 전표 조회 및 취소 처리 (ASP 패턴 기반 순차 저장 로직 및 소문자 통일)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-x me-2 text-danger" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매출전표취소 (HSIO541U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-danger" @click="deleteSlips">전표삭제</button>
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
                    <input v-model="searchdata.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchdata.deptnm" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('SEARCH_DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('SEARCH_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">발행일자</th>
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

      <!-- 🅱️ 데이터 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 전표 발행 내역</span>
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

// 1. 상태 관리 (소문자 통일)
const searchdata = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  ioymdfr: initfrymd, ioymdto: initymd
})

const closinginfo = reactive({ clsymd: '', sclsym: '' })
const autoslip = ref('N')

const ioymdfr = computed({ get: () => formatDate(searchdata.ioymdfr, '-'), set: (v) => searchdata.ioymdfr = v.replace(/-/g, '') })
const ioymdto = computed({ get: () => formatDate(searchdata.ioymdto, '-'), set: (v) => searchdata.ioymdto = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeitemcount = ref(0)
const allSelected = ref(false)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "조회된 내역이 없습니다.",
    columnDefaults: { headerSort: false },
    columns: [
      {
        title: "선택", field: "selected", width: 60, hozAlign: "center",
        formatter: "tickCross", editor: true,
        cellEdited: (cell) => {
            if (cell.getValue()) {
                const canCancel = checkCanCancel(cell.getData())
                if (canCancel !== true) {
                    vAlertError(canCancel)
                    cell.setValue(false)
                }
            }
            updateTotals()
        }
      },
      { title: "전표번호", field: "slip_full", width: 200, hozAlign: "center", cssClass: "fw-bold" },
      {
        title: "더존전송", field: "sendyn", width: 120, hozAlign: "center",
        formatter: (c) => (c.getValue() === 'Y' || c.getData().sendyn === 'Y') ? '<span class="badge bg-success">전송완료</span>' : '<span class="badge bg-secondary">미전송</span>'
      },
      { title: "발행부서", field: "deptnm", width: 250 },
      { title: "거래처", field: "custnm", minWidth: 200 },
      { title: "유형", field: "vattypenm", width: 150 },
      { title: "공급가", field: "spyamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "부가세", field: "vatamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "합계", field: "jsansum", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary" }
    ]
  })
}

const checkCanCancel = (row: any) => {
    const slipymd = row.slipymd || row.SLIPYMD || ''
    const sendyn = row.sendyn || row.SENDYN || 'N'
    const cfmyn = row.cfmyn || row.CFMYN || 'N'

    if (cfmyn === 'Y' || sendyn === 'Y') return "전송완료된 전표입니다. 취소할 수 없습니다."
    if (slipymd === '00000000' || !slipymd) return "전표가 발행되지 않은 자료입니다."

    const slipym = slipymd.substring(0, 6)
    if (closinginfo.sclsym && slipym <= closinginfo.sclsym) return "영업정보가 마감된 월입니다. 취소할 수 없습니다."
    if (closinginfo.clsymd && slipymd <= closinginfo.clsymd) return "회계정보가 마감된 일자입니다. 취소할 수 없습니다."
    return true
}

const updateTotals = () => {
  if (!grid.value) return
  const data = grid.value.getData().filter((i: any) => i.selected)
  activeitemcount.value = data.length
}

const toggleAllSelection = () => {
  if (!grid.value) return
  const data = grid.value.getData()
  grid.value.updateData(data.map(i => {
      const can = checkCanCancel(i) === true
      return { ...i, selected: allSelected.value && can }
  }))
  updateTotals()
}

// 3. 기능 구현
async function search() {
  if (!searchdata.deptcd) return vAlertError('판매부서를 선택하세요.')
  try {
    const res = await api.post('/api/hsio/HSIO_541U_STR', {
      actkind: 's0', cmpycd: authStore.cmpycd, gubun: '200',
      ioymdfr: searchdata.ioymdfr, ioymdto: searchdata.ioymdto,
      deptcd: searchdata.deptcd
    })
    if (grid.value) {
      const mappedData = res.data.map((i: any) => {
          const slipymd = i.slipymd || i.SLIPYMD || ''
          const slipno = i.slipno || i.slipno || ''
          return {
              ...i,
              slipymd,
              slipno,
              selected: false,
              slip_full: slipymd > '00000000' ? `${formatDate(slipymd, '-')}-${slipno}` : '',
              jsansum: Number(i.spyamt || i.SPYAMT || 0) + Number(i.vatamt || i.VATAMT || 0),
              udeptcd: i.udeptcd || i.UDEPTCD || i.deptcd || i.DEPTCD,
              sendyn: i.sendyn || i.SENDYN || 'N'
          }
      })
      grid.value.setData(mappedData)
      allSelected.value = false
      updateTotals()
    }
  } catch (e) { vAlertError('조회 실패') }
}

/**
 * 🚀 전표 삭제 처리 (ASP 로직 반영 + 소문자 통일)
 */
async function deleteSlips() {
  const selected = grid.value?.getData().filter((i: any) => i.selected)
  if (!selected || selected.length === 0) return vAlertError('취소할 전표를 선택하세요.')

  if (!confirm('선택한 전표를 삭제하시겠습니까?')) return

  try {
    for (const item of selected) {
      const slipymd = (item.slipymd || '').replace(/-/g, '')
      const slipno = item.slipno
      const udeptcd = item.udeptcd

      // 1. 자동전표 여부 확인 후 확정 취소 (ASP: HASL_020U_STR 'A0')
      if (autoslip.value === 'Y') {
          await api.post('/api/hasl/HASL_020U_STR', {
              actkind: 'a0',
              cmpycd: authStore.cmpycd,
              slipymd: slipymd,
              acctymd: slipymd,
              slipno: slipno,
              deptcd: udeptcd,
              slipkind: '040',
              slipyn: 'y',
              cofmyn: 'n',
              empnm: authStore.usernm,
              updemp: authStore.userid
          })
      }

      // 2. 정산 내역 전표 정보 삭제 (ASP: HSIO_541U_STR 'D0')
      const res = await api.post('/api/hsio/HSIO_541U_STR', {
        actkind: 'd0',
        cmpycd: authStore.cmpycd,
        gubun: '200',
        ioymdfr: searchdata.ioymdfr,
        ioymdto: searchdata.ioymdto,
        udeptcd: udeptcd,
        slipymd: slipymd,
        slipno: slipno,
        userid: authStore.userid
      })

      const resData = res.data?.[0]
      if (resData && (resData.result === 'y' || resData.erryn === 'y' || resData.RESULT === 'Y' || resData.ERRYN === 'Y')) {
          throw new Error(resData.msg || resData.MSG || '전표 삭제 중 업무 오류 발생')
      }
    }
    vAlert('정상적으로 작업이 되었습니다.')
    search()
  } catch (e: any) {
      vAlertError(e.message || '전표 삭제 중 오류 발생')
  }
}

function initialize() {
  resetForm(searchdata)
  Object.assign(searchdata, { deptcd: authStore.deptcd, deptnm: authStore.deptnm, ioymdfr: initfrymd, ioymdto: initymd })
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
      data: { gubun: 'd0', cmpycd: authStore.cmpycd },
      columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
      onConfirm: (data: any) => { searchdata.deptcd = data.deptcd; searchdata.deptnm = data.deptnm }
    })
    modalVisible.value = true
  }
}

const formatDate = (v: any, sep: string) => v && v.length === 8 ? `${v.substring(0, 4)}${sep}${v.substring(4, 6)}${sep}${v.substring(6, 8)}` : v

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'cl', cmpycd: authStore.cmpycd } }).then(r => {
    if (r.data?.length) {
        const d = r.data[0]
        closinginfo.clsymd = String(d.clsymd || d.CLSYMD || Object.values(d)[0]).trim()
        closinginfo.sclsym = String(d.sclsym || d.SCLSYM || Object.values(d)[1]).trim()
    }
  })
  api.get('/api/ha00/HA00_010S_STR', { params: { gubun: 'p1', cmpycd: authStore.cmpycd } }).then(r => {
    if (r.data?.length) {
        const d = r.data[0]
        autoslip.value = (d.slipyn || d.slipyn || 'N').toUpperCase()
    }
  })
  nextTick(() => initGrid())
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
