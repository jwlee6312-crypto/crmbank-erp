<!--수입관리/수입발주등록 [디자인 원칙 13가지 완벽 준수 및 버튼 통합] -->
<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsip100u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1, 12. 상단 액션 바 (버튼 그룹 우측 상단 정렬 및 표준 색상) -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-globe-americas me-2 text-primary" style="font-size: 18px;"></i>
        수입관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">수입발주작업 (HSIP100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchDetail">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="handleSave">
          <i class="bi bi-save"></i> 저장
        </button>
        <button class="btn-erp btn-delete" @click="handleDelete" :disabled="!formData.FILENO || formData.FILENO === '0000'">
          <i class="bi bi-trash"></i> 삭제
        </button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🔍 9. 최상단 검색항목 구분 -->
      <div class="card border shadow-sm bg-light bg-opacity-50">
        <div class="card-body py-2 px-3">
          <div class="d-flex align-items-center gap-4">
            <div class="d-flex align-items-center gap-2">
              <span class="erp-label">발주부서</span>
              <div class="input-group input-group-sm" style="width: 250px;">
                <input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
                <input v-model="searchForm.DEPTNM" type="text" class="form-control" placeholder="부서 선택" />
                <button class="btn btn-outline-secondary px-2" @click="openHelp('S_DEPT')"><i class="bi bi-search"></i></button>
              </div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="erp-label">PO No</span>
              <input v-model="searchForm.FILENO" type="text" class="form-control form-control-sm fw-bold" style="width: 150px;" placeholder="PO 번호 입력" @keyup.enter="fetchDetail" />
            </div>
          </div>
        </div>
      </div>

      <!-- 🅰️ 10. 입력항목 영역 (마스터 정보) -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <colgroup>
              <col style="width: 10%"><col style="width: 23%">
              <col style="width: 10%"><col style="width: 23%">
              <col style="width: 10%"><col style="width: 24%">
            </colgroup>
            <tbody>
              <tr>
                <th class="required">발주부서</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="formData.DEPTNM" type="text" class="form-control border-start-0" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">PO No</th>
                <td><input v-model="formData.FILENO" type="text" class="form-control form-control-sm text-center fw-bold text-primary bg-light" readonly /></td>
                <th class="required">발주일자</th>
                <td><input v-model="formData.ISSYMD" type="date" class="form-control form-control-sm w-auto" /></td>
              </tr>
              <tr>
                <th class="required">거 래 처</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.CUSTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 70px;" readonly />
                    <input v-model="formData.CUSTNM" type="text" class="form-control border-start-0" placeholder="거래처 선택" @keyup.enter="handleOpenHelp('CUST')" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required">수입구분</th>
                <td>
                  <select v-model="formData.IMPTGBN" class="form-select form-select-sm">
                    <option value="">-- 선택 --</option>
                    <option v-for="opt in comboData.IMPTGBN" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">원 산 지</th>
                <td>
                  <select v-model="formData.NACD" class="form-select form-select-sm">
                    <option value="">-- 선택 --</option>
                    <option v-for="opt in comboData.NACD" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="required">선 적 항</th>
                <td>
                  <select v-model="formData.SHIPPORT" class="form-select form-select-sm">
                    <option value="">-- 선택 --</option>
                    <option v-for="opt in comboData.SHIPPORT" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">도 착 항</th>
                <td>
                  <select v-model="formData.ARVPORT" class="form-select form-select-sm">
                    <option value="">-- 선택 --</option>
                    <option v-for="opt in comboData.ARVPORT" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">통화구분</th>
                <td>
                  <select v-model="formData.CURRCD" class="form-select form-select-sm">
                    <option value="">-- 선택 --</option>
                    <option v-for="opt in comboData.CURRCD" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="required">결제조건</th>
                <td>
                  <select v-model="formData.PAYCOND" class="form-select form-select-sm">
                    <option value="">-- 선택 --</option>
                    <option v-for="opt in comboData.PAYCOND" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">가격조건</th>
                <td>
                  <select v-model="formData.PRICOND" class="form-select form-select-sm">
                    <option value="">-- 선택 --</option>
                    <option v-for="opt in comboData.PRICOND" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th class="required">적용환율</th>
                <td><input v-model="formData.FRGNRATE" type="number" step="0.01" class="form-control form-control-sm text-end fw-bold text-primary" @input="updateTotals" /></td>
              </tr>
              <tr>
                 <th class="required">결제기간</th>
                 <td><input v-model="formData.PAYTERM" type="text" class="form-control form-control-sm" /></td>
                 <th class="required">Offer No</th>
                 <td><input v-model="formData.OFFERNO" type="text" class="form-control form-control-sm" /></td>
                 <th class="required">입고일자</th>
                 <td><input v-model="formData.INYMD" type="date" class="form-control form-control-sm w-auto" /></td>
              </tr>
              <tr>
                 <th class="required">소요비용</th>
                 <td>
                    <div class="d-flex align-items-center gap-1">
                      <span class="small" style="min-width: 40px;">L/C:</span>
                      <input v-model="formData.LCAMT" type="number" class="form-control form-control-sm text-end" @input="updateTotals" />
                      <span class="small" style="min-width: 40px;">통관:</span>
                      <input v-model="formData.XTAMT" type="number" class="form-control form-control-sm text-end" @input="updateTotals" />
                    </div>
                </td>
                <th class="required">B/L</th>
                <td>
                    <div class="d-flex align-items-center gap-1">
                      <span class="small" style="min-width: 40px;">외화:</span>
                      <input v-model="formData.FRGNAMT" type="number" class="form-control form-control-sm text-end bg-light" readonly />
                      <span class="small" style="min-width: 40px;">원화:</span>
                      <input v-model="formData.WONAMT" type="number" class="form-control form-control-sm text-end bg-light" readonly />
                    </div>
                </td>
                <th class="required">합계</th>
                <td>
                    <input v-model="formData.COSTSUM" type="number" class="form-control form-control-sm text-end fw-bold text-primary bg-light" readonly />
                </td>
              </tr>
              <tr>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td colspan="5"><input v-model="formData.BIGO" type="text" class="form-control form-control-sm" placeholder="특기사항 입력" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 8. 디테일 그리드 영역 (중앙 정렬 표준 적용 및 HPIO200U 스타일 버튼) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden bg-white">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 수입 발주 품목 리스트
          </span>
          <!-- 💎 HPIO200U 스타일로 행추가 버튼 통합 -->
          <button class="btn btn-grid-row-add" @click="addGridRow">
             <i class="bi bi-plus-circle"></i> 행추가
             </button>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="mainGridRef" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 팝업 모달 -->
    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
    <ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" :astKind="String(formData.ASTKIND || '2')" @close="itemHelpVisible = false" @confirm="onSelectItem" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
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

// 13. 모든 변수명 대문자 고수
const searchForm = reactive({ DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, FILENO: '' })
const formData = reactive<any>({
  ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, FILENO: '', DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  ISSYMD: new Date().toISOString().substring(0, 10), CUSTCD: '', CUSTNM: '', NACD: '',
  CURRCD: '', FRGNRATE: 1350, PRICOND: '', PAYCOND: '', IMPTGBN: '',
  SHIPPORT: '', ARVPORT: '', BIGO: '', WONAMT: 0, FRGNAMT: 0, LCAMT: 0, XTAMT: 0, COSTSUM: 0, ASTKIND: '2',
  PAYTERM: '', OFFERNO: '', INYMD: new Date().toISOString().substring(0, 10)
})

const comboData = reactive<any>({
  IMPTGBN: [], NACD: [], SHIPPORT: [], ARVPORT: [], CURRCD: [], PAYCOND: [], PRICOND: []
})

const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null

// 6. 그리드: 좌우 상하 무조건 중앙 위치, 최소폭 100
const initGrid = () => {
  if (!mainGridRef.value) return
  mainGrid = new Tabulator(mainGridRef.value, {
    layout: "fitColumns", height: "100%", placeholder: "발주 품목이 없습니다.",
    columnDefaults: { headerHozAlign: 'center', headerSort: false },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      { title: "품목코드", field: "ITEMCD", width: 150, hozAlign: "center"},
      {
        title: "품목명",
        field: "ITEMNM",
        minWidth: 100,
        widthGrow: 1,
        cssClass: "fw-bold",
        formatter: (cell) => `
            <div class="d-flex justify-content-between align-items-center w-100">
                <span>${cell.getValue() || ''}</span>
                <i class="bi bi-search text-primary ms-1 cursor-pointer"></i>
            </div>
        `,
        cellClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "규격", field: "ITSIZE", width: 200,
        bottomCalc: "count", bottomCalcFormatter: (c) => "총 " + c.getValue() + "건"
      },
      { title: "단위", field: "UNITNM", width: 60, hozAlign: "center" },
      { title: "수량", field: "QTY", width: 150, hozAlign: "right", editor: "number", cssClass: "bg-yellow fw-bold",
        bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
      },
      { title: "단가", field: "PRICE", width: 150, hozAlign: "right", editor: "number" },
      { title: "금액", field: "AMT", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 },
        bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 }
      },
      { title: "", width: 60, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>",
       cellClick: (e, c) => { c.getRow().delete(); updateTotals()
       } }
    ]
  });

  mainGrid.on("cellEdited", (cell) => {
    const row = cell.getRow(); const d = row.getData()
    if (['QTY', 'PRICE'].includes(cell.getField())) {
      const amt = (Number(d.QTY) || 0) * (Number(d.PRICE) || 0)
      row.update({ AMT: amt })
    }
    updateTotals()
  })
}

const updateTotals = () => {
    const data = mainGrid?.getData() || []
    const frgnAmt = data.reduce((acc, cur: any) => acc + (Number(cur.AMT) || 0), 0)
    formData.FRGNAMT = frgnAmt
    formData.WONAMT = Math.floor(frgnAmt * formData.FRGNRATE)
    formData.COSTSUM = (Number(formData.LCAMT) || 0) + (Number(formData.XTAMT) || 0) + (Number(formData.WONAMT) || 0)
}

const handleOpenHelp = (type: string, target?: any) => {
  if (type === 'CUST') openHelp('CUST', (d) => { formData.CUSTCD = d.CUSTCD; formData.CUSTNM = d.CUSTNM });
  else if (type === 'DEPT') openHelp('DEPT', (d) => { formData.DEPTCD = d.DEPTCD; formData.DEPTNM = d.DEPTNM });
  else if (type === 'S_DEPT') openHelp('DEPT', (d) => { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM });
  else if (type === 'ITEM') { currentTargetRow.value = target; itemHelpVisible.value = true; }
}

async function loadCombos() {
  const fetchCombo = async (gbn: string) => {
    try {
      const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'E2', CMPYCD: authStore.CMPYCD, GBNCD: gbn } })
      return res.data.map((i: any) => ({ CODECD: String(i.CODECD || i.codecd || '').trim(), CODENM: String(i.CODENM || i.codenm || '').trim() }))
    } catch (e) { return [] }
  }
  comboData.IMPTGBN = await fetchCombo('311')
  comboData.NACD = await fetchCombo('305')
  comboData.SHIPPORT = await fetchCombo('308')
  comboData.ARVPORT = await fetchCombo('309')
  comboData.CURRCD = await fetchCombo('310')
  comboData.PAYCOND = await fetchCombo('312')
  comboData.PRICOND = await fetchCombo('314')
}

async function fetchDetail() {
  if (!searchForm.FILENO) return vAlertError('PO No를 입력하세요.');
  try {
    const res = await api.post('/api/hsip/HSIP_100U_STR', { ...searchForm, ACTKIND: 'S0', CMPYCD: authStore.CMPYCD });
    if (res.data?.length) {
      Object.assign(formData, res.data[0]);
      formData.ACTKIND = 'U0';
      const itemRes = await api.post('/api/hsip/HSIP_101U_STR', { FILENO: formData.FILENO, ACTKIND: 'S0', CMPYCD: authStore.CMPYCD });
      mainGrid?.setData(itemRes.data || []);
      updateTotals();
      vAlert('조회되었습니다.');
    } else { vAlertError('조회 결과가 없습니다.'); }
  } catch (e) { vAlertError('서버 통신 오류'); }
}

async function handleSave() {
  if (!formData.CUSTCD) return vAlertError('거래처를 선택하세요.');
  const data = mainGrid?.getData();
  if (!data?.length) return vAlertError('발주 품목이 없습니다.');

  try {
    const resM = await api.post('/api/hsip/HSIP_100U_STR', { ...formData, ACTKIND: formData.FILENO ? 'U0' : 'A0', FRGNAMT: formData.FRGNAMT, WONAMT: formData.WONAMT, UPDEMP: authStore.USERID });
    if (resM.data?.[0]?.FILENO) {
      const newFileNo = resM.data[0].FILENO;
      await api.post('/api/hsip/HSIP_101U_STR', { FILENO: newFileNo, ACTKIND: 'D0', CMPYCD: authStore.CMPYCD });
      for (const item of data) {
        await api.post('/api/hsip/HSIP_101U_STR', { ...item, ACTKIND: 'A0', FILENO: newFileNo, CMPYCD: authStore.CMPYCD, UPDEMP: authStore.USERID });
      }
      vAlert('성공적으로 저장되었습니다.');
      searchForm.FILENO = newFileNo; fetchDetail();
    }
  } catch (e) { vAlertError('저장 중 오류 발생'); }
}

async function handleDelete() {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  try {
    await api.post('/api/hsip/HSIP_100U_STR', { FILENO: formData.FILENO, ACTKIND: 'D0', CMPYCD: authStore.CMPYCD });
    vAlert('삭제되었습니다.'); initialize();
  } catch (e) { vAlertError('삭제 실패'); }
}

function initialize() {
  resetForm(formData);
  Object.assign(formData, {
    ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
    ISSYMD: new Date().toISOString().substring(0, 10), FRGNRATE: 1350,
    LCAMT: 0, XTAMT: 0, FRGNAMT: 0, WONAMT: 0, COSTSUM: 0,
    INYMD: new Date().toISOString().substring(0, 10)
  });
  mainGrid?.clearData(); updateTotals();
}

const onSelectItem = (d: any) => {
  if (!currentTargetRow.value) return
  currentTargetRow.value.update({ ITEMCD: d.ITEMCD, ITEMNM: d.ITEMNM, ITSIZE: d.ITSIZE, UNIT: d.UNIT, PRICE: d.OUTCOST || 0 });
  itemHelpVisible.value = false; updateTotals();
}

const addGridRow = () => { mainGrid?.addRow({ QTY: 0, PRICE: 0, AMT: 0 });  updateTotals();}

onMounted(() => { loadCombos(); nextTick(() => { initGrid(); }); })
</script>

<style scoped>
.hsip100u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.flex-shrink-0 { flex-shrink: 0 !important; }
.flex-grow-1 { flex-grow: 1 !important; min-height: 0 !important; }
.overflow-hidden { overflow: hidden !important; }
/* 🚀 입력 필드 글자 크기 및 높이 최적화 (HSBA070U 패턴) */
.form-control, .form-select {
  font-size: 12px !important;
  height: 28px !important;
  padding: 2px 8px !important;
}
.erp-table-full { width: 100%; border-collapse: collapse; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 4px 5px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 2px 4px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }

/* 🚀 팝업 가독성 표준 스타일 */
:deep(.modal-content) { background-color: #ffffff !important; }
:deep(.modal-content .tabulator) { background-color: #ffffff !important; color: #000000 !important; border: 1px solid #dee2e6 !important; }
:deep(.modal-content .tabulator-cell) { color: #000000 !important; font-size: 13px !important; padding: 8px !important; }

</style>
