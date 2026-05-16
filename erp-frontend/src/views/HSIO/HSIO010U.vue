<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio010u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-pencil-square me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        구매관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">구매요청 등록 (HSIO010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchRequest">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-danger" @click="deleteRequest" :disabled="!masterData.REQNO || masterData.REQNO === '0000' || masterData.STS === 'Y'">삭제</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 -->
    <div class="search-bar bg-light border-bottom p-2 px-3 d-flex align-items-center shadow-sm">
      <div class="d-flex align-items-center gap-4 flex-grow-1">
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary">요청부서:</span>
          <div class="input-group input-group-sm" style="width: 250px;">
            <input v-model="searchParam.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
            <input v-model="searchParam.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="fetchRequest" />
            <button class="btn btn-outline-secondary" @click="openHelp('S_DEPT')"><i class="bi bi-search"></i></button>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="fw-bold small text-secondary">요청번호:</span>
          <div class="d-flex align-items-center gap-1">
            <input v-model="searchParam.REQYM" type="month" class="form-control form-control-sm text-center" style="width: 130px;" />
            <input v-model="searchParam.REQNO" type="text" class="form-control form-control-sm text-center" placeholder="0000" style="width: 60px;" @keyup.enter="fetchRequest" />
          </div>
        </div>
      </div>
    </div>

    <!-- 💡 3. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 d-flex flex-column overflow-hidden p-3 gap-3">
      <!-- 🅰️ 마스터 정보 -->
      <div class="card border-0 shadow-sm overflow-hidden">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-info-circle-fill me-1 text-secondary"></i>요청 기본 정보</span>
          <div v-if="masterData.STS === 'Y'" class="badge bg-success-subtle text-success border border-success-subtle px-2">승인완료</div>
          <div v-else class="badge bg-danger-subtle text-danger border border-danger-subtle px-2">미승인</div>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">요청부서</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap">
                    <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="masterData.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="openHelp('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">요청번호</th>
                <td>
                  <div class="d-flex align-items-center gap-1 flex-nowrap">
                    <input v-model="uiREQYM" type="month" class="form-control text-center fw-bold bg-light" readonly style="width: 130px;" />
                    <input v-model="masterData.REQNO" type="text" class="form-control text-center fw-bold text-primary bg-light" placeholder="0000" style="width: 60px;" readonly />
                  </div>
                </td>
                <th class="required">요청일자</th>
                <td><input v-model="uiREQYMD" type="date" class="form-control" /></td>
              </tr>
              <tr>
                <th class="required">입고요청일</th>
                <td><input v-model="uiINYMD" type="date" class="form-control" /></td>
                <th>요청담당</th>
                <td>
                  <select v-model="masterData.USERID" class="form-select">
                    <option v-for="item in empOptions" :key="item.USERID" :value="item.USERID">{{ item.USERNM }}</option>
                  </select>
                </td>
                <th>특기사항</th>
                <td><input v-model="masterData.REMARK" type="text" class="form-control" maxlength="100" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 디테일 정보 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1 text-secondary"></i> 요청 품목 리스트</span>
          <button class="btn btn-grid-row-add" @click="addRow" :disabled="masterData.STS === 'Y'">
            <i class="bi bi-plus-circle"></i> 행추가
          </button>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
      <div class="row align-items-center">
        <div class="col-md-3 small">선택 품목: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="fs-5 ms-2 fw-light">총 합계금액: <span class="fw-bold text-white ms-2">{{ formatNumber(sumTotal) }}</span> 원</span>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
    <ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" astKind="2" @close="itemHelpVisible = false" @confirm="onSelectItem" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import ItemHelpModal from '@/components/ItemHelpModal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const itemHelpVisible = ref(false)
const currentTargetRow = ref<any>(null)

const now = new Date();
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`;
const initYMD = `${initYM}${String(now.getDate()).padStart(2, '0')}`;

const searchParam = reactive({ DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, REQYM: `${initYM.substring(0, 4)}-${initYM.substring(4, 6)}`, REQNO: '' })
const masterData = reactive<any>({
  ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, REQYM: initYM, REQNO: '0000', DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, REQYMD: initYMD, INYMD: initYMD, STS: 'N', USERID: String(authStore.USERID || '').trim(), REMARK: '', ASGBN: 'N'
})

const uiREQYM = computed({ get: () => masterData.REQYM ? `${masterData.REQYM.substring(0, 4)}-${masterData.REQYM.substring(4, 6)}` : '', set: (v) => masterData.REQYM = v.replace('-', '') })
const uiREQYMD = computed({ get: () => masterData.REQYMD ? `${masterData.REQYMD.substring(0, 4)}-${masterData.ORDYMD.substring(4, 6)}-${masterData.ORDYMD.substring(6, 8)}` : '', set: (v) => masterData.REQYMD = v.replace(/-/g, '') })
const uiINYMD = computed({ get: () => masterData.INYMD ? `${masterData.INYMD.substring(0, 4)}-${masterData.INYMD.substring(4, 6)}-${masterData.INYMD.substring(6, 8)}` : '', set: (v) => masterData.INYMD = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null);
const grid = ref<Tabulator | null>(null);
const activeItemCount = ref(0); const sumTotal = ref(0);
const empOptions = ref<any[]>([])

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "등록된 품목이 없습니다.",
    columnDefaults: { headerHozAlign: 'center', headerSort: false },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      { title: "품목코드", field: "ITEMCD", width: 120, formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>", cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) handleOpenHelp('ITEM', cell.getRow()) } },
      { title: "품목명", field: "ITEMNM", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
      { title: "규격", field: "ITSIZE", width: 150 },
      { title: "단위", field: "UNITNM", width: 80, hozAlign: "center" },
      { title: "수량", field: "REQQTY", width: 100, hozAlign: "right", editor: "number", cssClass: "bg-yellow fw-bold" },
      { title: "단가", field: "PRICE", width: 100, hozAlign: "right", editor: "number" },
      { title: "금액", field: "REQAMT", width: 120, hozAlign: "right", formatter: "money", bottomCalc: "sum" },
      { title: "", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>", cellClick: (e, c) => { c.getRow().delete(); updateTotals() } }
    ]
  });
}

const updateTotals = () => { if (!grid.value) return; const data = grid.value.getData(); activeItemCount.value = data.length; sumTotal.value = data.reduce((acc, i) => acc + (Number(i.REQAMT) || 0), 0) }

const handleOpenHelp = (type: string, target?: any) => {
  if (type === 'S_DEPT') openHelp('DEPT', (d) => { searchParam.DEPTCD = d.DEPTCD; searchParam.DEPTNM = d.DEPTNM });
  else if (type === 'DEPT') openHelp('DEPT', (d) => { masterData.DEPTCD = d.DEPTCD; masterData.DEPTNM = d.DEPTNM });
  else if (type === 'ITEM') { currentTargetRow.value = target; itemHelpVisible.value = true; }
}

const onSelectItem = (d: any) => {
  if (!currentTargetRow.value) return
  currentTargetRow.value.update({ ITEMCD: d.ITEMCD, ITEMNM: d.ITEMNM, ITSIZE: d.ITSIZE, UNIT: d.UNIT, PRICE: d.IMPRICE || 0 });
  itemHelpVisible.value = false; updateTotals();
}

function addRow() { grid.value?.addRow({ REQQTY: 0, PRICE: 0, REQAMT: 0 }) }
function initialize() { resetForm(masterData); grid.value?.clearData(); updateTotals(); }
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

async function fetchRequest() { if (!searchParam.REQYM || !searchParam.REQNO) return vAlertError('요청번호 입력 필수'); try { const res = await api.post('/api/hsio/HSIO_010U_STR', { ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, REQYM: searchParam.REQYM.replace('-', ''), REQNO: searchParam.REQNO }); if (res.data?.length) { Object.assign(masterData, res.data[0]); fetchDetail() } } catch (e) { vAlertError('조회 실패') } }
async function fetchDetail() { try { const res = await api.post('/api/hsio/HSIO_011U_STR', { ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, REQYM: masterData.REQYM, REQNO: masterData.REQNO }); if (grid.value) { grid.value.setData(res.data); setTimeout(updateTotals, 100) } } catch (e) { vAlertError('상세 로드 실패') } }
async function save() { /* ... */ }
async function deleteRequest() { /* ... */ }

onMounted(() => {
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'SD', CMPYCD: authStore.CMPYCD } }).then(r => empOptions.value = r.data);
  nextTick(() => initGrid());
})
</script>

<style scoped>
.hsio010u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 8px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 10px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
.bg-yellow { background-color: #fffde7 !important; }
</style>
