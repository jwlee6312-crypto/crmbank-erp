<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-graph-up-arrow me-2 text-primary" style="font-size: 18px;"></i>
        매출계획 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목그룹별판매계획 (HSPL110U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-outline-secondary" @click="print('Print')">인쇄</button>
        <button class="btn-erp btn-outline-success" @click="print('Excel')">엑셀</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <tbody>
              <tr>
                <th class="required" style="width: 100px;">연&nbsp;&nbsp;&nbsp;&nbsp;도</th>
                <td style="width: 150px;">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="searchData.yyyy" type="number" class="form-control form-control-sm text-center fw-bold" style="width: 80px;" />
                    <span class="small">년</span>
                  </div>
                </td>
                <th class="required">영업부서</th>
                <td style="width: 250px;">
                  <div class="input-group input-group-sm">
                    <input v-model="searchData.deptcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="searchData.deptnm" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">영업사원</th>
                <td style="width: 220px;">
                  <div class="input-group input-group-sm">
                    <input v-model="searchData.userid" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="searchData.usernm" type="text" class="form-control border-start-0" placeholder="사원 선택" @keyup.enter="handleOpenHelp('EMP')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('EMP')"><i class="bi bi-search"></i></button>
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
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 그룹별 월간 계획</span>
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
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
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
import { useCommonHelp } from '@/composables/useCommonHelp'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const now = new Date()

// 1. 상태 관리
const searchData = reactive({
  yyyy: now.getFullYear(),
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  userid: authStore.userid,
  usernm: authStore.usernm
})

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const allSelected = ref(false)

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
        title: "선택", field: "procyn", width: 60, hozAlign: "center",
        formatter: "tickCross",
        formatterParams: { crossElement: false },
        editor: true
      },
      { title: "구분", field: "astkindNM", width: 120, hozAlign: "center" },
      { title: "대분류", field: "Agrpnm", width: 140 },
      { title: "중분류", field: "Bgrpnm", width: 140 },
      {
        title: "합계", field: "PLAnsum", width: 120, hozAlign: "right",
        cssClass: "bg-light-blue fw-bold",
        formatter: (cell) => Number(cell.getValue() || 0).toLocaleString()
      },
      ...Array.from({ length: 12 }, (_, i) => {
        const month = String(i + 1).padStart(2, '0')
        return {
          title: `${month}월`,
          field: .mm${month}`,
          width: 90,
          hozAlign: "right",
          editor: "number",
          formatter: "money",
          formatterParams: { precision: 0 }
        }
      })
    ]
  })

  grid.value.on("cellEdited", (cell: any) => {
    const row = cell.getRow()
    const data = row.getData()
    if (!data.procyn) row.update({ procyn: true })

    let rowSum = 0
    for(let i=1; i<=12; i++) {
        rowSum += Number(data[.mm${String(i).padStart(2, '0')}`]) || 0
    }
    row.update({ PLAnsum: rowSum })
  })
}

const toggleAllSelection = () => {
  if (!grid.value) return
  const data = grid.value.getData()
  grid.value.updateData(data.map(i => ({ ...i, procyn: allSelected.value ? true : null })))
}

// 3. 기능 구현
async function search() {
  if (!searchData.deptcd || !searchData.userid) return vAlertError('영업부서와 사원을 선택하세요.')
  try {
    const res = await api.post('/api/hspl/HSPL_110U_STR', {
      actkind: 'S0',
      cmpycd: authStore.cmpycd,
      yyyy: searchData.yyyy,
      deptcd: searchData.deptcd,
      userid: searchData.userid
    })
    if (grid.value) {
      const mapped = res.data.map((i: any) => {
          let rowSum = 0
          for(let m=1; m<=12; m++) rowSum += Number(i[.mm${String(m).padStart(2, '0')}`]) || 0
          return { ...i, procyn: null, PLAnsum: rowSum }
      })
      grid.value.setData(mapped)
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  const selected = grid.value?.getData().filter((i: any) => i.procyn === true)
  if (!selected || selected.length === 0) return vAlertError('저장할 대상을 선택하세요.')

  if (!confirm('선택한 품목 그룹의 판매계획을 저장하시겠습니까?')) return

  try {
    for (const item of selected) {
      await api.post('/api/hspl/HSPL_110U_STR', {
        ...item,
        actkind: 'A0',
        cmpycd: authStore.cmpycd,
        yyyy: searchData.yyyy,
        deptcd: searchData.deptcd,
        userid: searchData.userid,
        UPD_USER: authStore.userid
      })
    }
    vAlert('정상적으로 저장되었습니다.')
    search()
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(searchData)
  Object.assign(searchData, {
    yyyy: now.getFullYear(),
    deptcd: authStore.deptcd,
    deptnm: authStore.deptnm,
    userid: authStore.userid,
    usernm: authStore.usernm
  })
  if (grid.value) grid.value.clearData()
  allSelected.value = false
}

function print(type: string) {
    vAlert(`${type} 기능은 준비 중입니다.`)
}

// 팝업 핸들러 (표준화)
function handleOpenHelp(type: string) {
  if (type === 'DEPT') {
    openHelp('DEPT', (data: any) => {
      searchData.deptcd = data.deptcd;
      searchData.deptnm = data.deptnm;
    });
  } else if (type === 'EMP') {
    openHelp('EMP', (data: any) => {
      searchData.userid = data.userid;
      searchData.usernm = data.usernm;
    });
  }
}

onMounted(() => {
  nextTick(() => initGrid())
})
</script>
