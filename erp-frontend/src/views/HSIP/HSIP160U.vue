<!--
	=============================================================
	프로그램명	: 수입입고작업 (HSIP160U)
	작성일자	: 2025.02.24
	설명        : 수입 통관 물품 입고 처리 (ASP 패턴 기반 순차 저장 및 소문자 통일)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam-fill me-2 text-primary" style="font-size: 18px;"></i>
        수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        입고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">수입입고작업 (HSIP160U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchPoList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 🔍 [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" /><col style="width: 23%" />
              <col style="width: 10%" /><col style="width: 23%" />
              <col style="width: 10%" /><col style="width: 24%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">입고부서</th>
                <td>
                  <div class="input-group input-group-sm w-75">
                    <input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('S_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">통관일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchForm.ymdfr" v-model:todt="searchForm.ymdto" />
                </td>
                <th class="text-center bg-light">분할여부</th>
                <td>
                  <select v-model="searchForm.divyn" class="form-select form-select-sm w-50">
                    <option value="N">미분할</option>
                    <option value="Y">분할</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📊 3. 메인 작업 영역 -->
      <div class="d-flex flex-row flex-grow-1 overflow-hidden gap-2" style="min-height: 0;">
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 380px; min-width: 380px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex justify-content-between align-items-center">
            <span>통관 완료 목록</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="poGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup>
                  <col style="width: 100px;" /><col />
                  <col style="width: 100px;" /><col />
                  <col style="width: 100px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="bg-light text-center">PO No.</th>
                    <td><input v-model="formData.fileno" class="form-control bg-light fw-bold text-primary" readonly /></td>
                    <th class="bg-light text-center">면장번호</th>
                    <td><input v-model="formData.passno" class="form-control bg-light text-center" readonly /></td>
                    <th class="bg-light text-center">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                    <td><input v-model="formData.custnm" class="form-control bg-light" readonly /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
              <span class="fw-bold small text-dark d-flex align-items-center">
                <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 입고 예정 품목 명세 (참조용)
              </span>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="itemGridRef" class="tabulator-instance flex-grow-1"></div>
            </div>
          </div>
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
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp: openCommonHelp } = useCommonHelp()

const searchForm = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  ymdfr: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  ymdto: new Date().toISOString().substring(0, 10),
  divyn: 'N'
})

const formData = reactive<any>({
  fileno: '', passno: '', custnm: '', custcd: '', shipseq: '', passseq: ''
})

const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null

const fetchPoList = async () => {
  try {
    const res = await api.post('/api/hsip/HSIP_160U_STR', {
      actkind: 'S1', cmpycd: authStore.cmpycd,
      deptcd: searchForm.deptcd,
      frymd: searchForm.ymdfr.replace(/-/g, ''),
      toymd: searchForm.ymdto.replace(/-/g, ''),
      divyn: searchForm.divyn
    })
    poGrid?.setData(res.data || [])
    itemGrid?.clearData();
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const fetchDetail = async (row: any) => {
  Object.assign(formData, row)
  try {
    const res = await api.post('/api/hsip/HSIP_161S_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, fileno: row.fileno,
      shipseq: row.shipseq, passseq: row.passseq
    })
    itemGrid?.setData(res.data || [])
  } catch (e) { vAlertError('상세 조회 실패') }
}

/**
 * 🚀 저장 로직 (ASP 패턴: 목록에서 선택된 행들을 순차적으로 입고처리)
 */
const save = async () => {
  const selected = poGrid?.getSelectedData() || []
  if (selected.length === 0) return vAlertError('입고 처리할 항목을 선택하세요.')

  if (!confirm('입고 등록을 진행하시겠습니까?')) return

  try {
    for (const item of selected) {
      const params = {
        actkind: 'U0', // ASP 로직에 따라 입고 확정은 u0
        cmpycd: authStore.cmpycd,
        deptcd: searchForm.deptcd,
        frymd: searchForm.ymdfr.replace(/-/g, ''),
        toymd: searchForm.ymdto.replace(/-/g, ''),
        divyn: searchForm.divyn,
        fileno: item.fileno,
        shipseq: item.shipseq,
        passseq: item.passseq,
        updemp: authStore.userid
      }

      const res = await api.post('/api/hsip/HSIP_160U_STR', params)
      const resData = res.data?.[0]

      // ASP 에러 체크 패턴 적용
      if (resData && (resData.status === 'Y' || resData.erryn === 'Y' || resData.status === 'Y' || resData.erryn === 'Y')) {
        throw new Error(resData.msg || '입고 처리 중 업무 오류가 발생했습니다.')
      }
    }

    vAlert('정상적으로 작업이 되었습니다.')
    fetchPoList(); initialize();
  } catch (e: any) {
    vAlertError(e.message || '저장 실패')
  }
}

function initialize() {
  resetForm(formData); poGrid?.clearData(); itemGrid?.clearData();
}

function openHelp(type: string) {
  if (type === 'S_DEPT') {
    openCommonHelp('DEPT', (d) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm });
  }
}

onMounted(() => {
  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: true,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 60 },
        { title: 'PO No.', field: 'fileno', cssClass: 'fw-bold text-primary', width: 140 },
        { title: '면장번호', field: 'passno', width: 100 },
        { title: '거 래 처', field: 'custnm', minWidth: 100, hozAlign: "left" }
      ]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row.getData()))
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        { title: '품목명', field: 'itemnm', minWidth: 180, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
        { title: '규격', field: 'itsize', width: 120, hozAlign: 'left' },
        { title: '단위', field: 'unit', width: 60 },
        { title: '통관량', field: 'iqty', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } }
      ]
    })
  }
  fetchPoList()
})
</script>

<style scoped>
.main-content-wrapper { padding-bottom: 0vh !important; }
.grid-container-left, .grid-container-right { border-bottom: 3px solid #005a9f !important; }
.erp-table-dense th, .erp-table-dense td {
  height: 32px !important; padding: 0 8px !important; font-size: 12px; vertical-align: middle; border: 1px solid #dee2e6;
}
.erp-table-dense .form-control, .erp-table-dense .form-select, .erp-table-dense .btn {
  height: 26px !important; font-size: 12px !important; border-radius: 2px;
}
.erp-table-dense th { font-weight: 600; color: #495057; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.empty-row-style) { opacity: 0.6; }
</style>
