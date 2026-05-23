<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-cart-check-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        주문관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">주문등록 관리 (HSOD100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchOrder">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-delete" @click="deleteOrder" :disabled="!masterData.ORDNO || masterData.ORDNO === '0000' || masterData.STS === 'Y'">삭제</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 (gap-3 적용) -->
    <div class="flex-grow-1 overflow-auto p-3 d-flex flex-column gap-3">
      <!-- 🅰️ 마스터 정보 카드 -->
      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
          <div class="fw-bold small text-dark"><i class="bi bi-info-circle-fill me-1 text-secondary"></i>주문 기본 정보</div>
          <div v-if="masterData.STS === 'Y'" class="badge bg-success-subtle text-success border border-success-subtle px-2">승인완료</div>
          <div v-else class="badge bg-danger-subtle text-danger border border-danger-subtle px-2">미승인</div>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">주문부서</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap" style="width: 100%;">
                    <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="masterData.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">주문번호</th>
                <td>
                  <div class="d-flex align-items-center gap-1 flex-nowrap">
                    <input v-model="uiORDYM" type="month" class="form-control text-center fw-bold" style="width: 130px;" />
                    <input v-model="masterData.ORDNO" type="text" class="form-control text-center fw-bold text-primary bg-light" placeholder="0000" style="width: 60px;" readonly />
                  </div>
                </td>
                <th class="required">주문일자</th>
                <td><input v-model="uiORDYMD" type="date" class="form-control" /></td>
              </tr>
              <tr>
                <th class="required">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 70px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control border-start-0" placeholder="거래처 선택" @keyup.enter="handleOpenHelp('CUST')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">납품일자</th>
                <td><input v-model="uiOUTYMD" type="date" class="form-control" /></td>
                <th>영업담당</th>
                <td>
                  <select v-model="masterData.ORDEMP" class="form-select">
                    <option v-for="item in empOptions" :key="item.USERID" :value="item.USERID">{{ item.USERNM }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 디테일 품목 리스트 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1 text-secondary"></i> 주문 품목 리스트</span>
          <button class="btn btn-grid-row-add" @click="addRow" :disabled="masterData.STS === 'Y'">
            <i class="bi bi-plus-circle"></i> 행추가
          </button>
        </div>
        <div class="card-body p-0 flex-grow-1 overflow-hidden bg-white" style="position: relative;">
          <div ref="gridElement" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;"></div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  <ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" :astKind="String(masterData.ASTKIND || '2')" @close="itemHelpVisible = false" @confirm="onSelectItem" />
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

const masterData = reactive<any>({
  ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, ORDYM: initYM, ORDNO: '0000', DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, CUSTCD: '', CUSTNM: '', ORDYMD: initYMD, OUTYMD: initYMD, STS: 'N', ORDEMP: authStore.USER_ID, ORDKIND: '100', ASTKIND: '2'
})

const uiORDYM = computed({ get: () => masterData.ORDYM ? `${masterData.ORDYM.substring(0, 4)}-${masterData.ORDYM.substring(4, 6)}` : '', set: (v) => masterData.ORDYM = v.replace('-', '') })
const uiORDYMD = computed({ get: () => masterData.ORDYMD ? `${masterData.ORDYMD.substring(0, 4)}-${masterData.ORDYMD.substring(4, 6)}-${masterData.ORDYMD.substring(6, 8)}` : '', set: (v) => masterData.ORDYMD = v.replace(/-/g, '') })
const uiOUTYMD = computed({ get: () => masterData.OUTYMD ? `${masterData.OUTYMD.substring(0, 4)}-${masterData.OUTYMD.substring(4, 6)}-${masterData.OUTYMD.substring(6, 8)}` : '', set: (v) => masterData.OUTYMD = v.replace(/-/g, '') })

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
      { title: "수량", field: "ORDQTY", width: 80, hozAlign: "right", editor: "number" },
      { title: "단가", field: "PRICE", width: 100, hozAlign: "right", editor: "number" },
      { title: "금액", field: "AMTSUM", width: 120, hozAlign: "right", formatter: "money", bottomCalc: "sum" },
      { title: "", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>", cellClick: (e, c) => { c.getRow().delete(); updateTotals() } }
    ]
  });
}

const updateTotals = () => { if (!grid.value) return; const data = grid.value.getData(); activeItemCount.value = data.length; sumTotal.value = data.reduce((acc, i) => acc + (Number(i.AMTSUM) || 0), 0) }

const handleOpenHelp = (type: string, target?: any) => {
  if (type === 'CUST') openHelp('CUST', (d) => { masterData.CUSTCD = d.CUSTCD; masterData.CUSTNM = d.CUSTNM });
  else if (type === 'DEPT') openHelp('DEPT', (d) => { masterData.DEPTCD = d.DEPTCD; masterData.DEPTNM = d.DEPTNM });
  else if (type === 'ITEM') { currentTargetRow.value = target; itemHelpVisible.value = true; }
}

const onSelectItem = (d: any) => {
  if (!currentTargetRow.value) return
  currentTargetRow.value.update({ ITEMCD: d.ITEMCD, ITEMNM: d.ITEMNM, ITSIZE: d.ITSIZE, UNIT: d.UNIT, PRICE: d.OUTCOST || 0 });
  itemHelpVisible.value = false; updateTotals();
}

function addRow() { grid.value?.addRow({ ORDQTY: 0, PRICE: 0, AMTSUM: 0 }) }
function initialize() { resetForm(masterData); grid.value?.clearData(); updateTotals(); }
const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

async function fetchOrder() { if (!masterData.ORDYM || !masterData.ORDNO) return vAlertError('주문번호 입력 필수'); try { const res = await api.post('/api/hsod/HSOD_100U_STR', { ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, ORDYM: masterData.ORDYM, ORDNO: masterData.ORDNO }); if (res.data?.length) { Object.assign(masterData, res.data[0]); fetchDetail() } } catch (e) { vAlertError('조회 실패') } }
async function fetchDetail() { try { const res = await api.post('/api/hsod/HSOD_101U_STR', { ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, ORDYM: masterData.ORDYM, ORDNO: masterData.ORDNO }); if (grid.value) { grid.value.setData(res.data); setTimeout(updateTotals, 100) } } catch (e) { vAlertError('상세 로드 실패') } }

onMounted(() => {
  api.post('/api/ha00/HA00_00P_STR', { GUBUN: 'SD', CMPYCD: authStore.CMPYCD, GBNCD: '' }).then(r => empOptions.value = r.data);
  nextTick(() => initGrid());
})
</script>

<style scoped>
/* 🎨 전역 스타일(global.css)이 적용되므로 특수 레이아웃만 남깁니다. */
</style>
