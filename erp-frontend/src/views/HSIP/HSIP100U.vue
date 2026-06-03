<!--
	=============================================================
	프로그램명	: 수입발주작업 (HSIP100U)
	작성일자	: 2025.02.24
	설명        : 수입 발주 마스터/상세 관리 (HSOD100U 표준 구조 및 소문자 원칙 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-globe-americas me-2 text-primary" style="font-size: 18px;"></i>
        수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">수입발주작업 (HSIP100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-delete" @click="handleFullDelete" :disabled="!formData.fileno || formData.fileno === '0000'">전체삭제</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 40%" />
                <col style="width: 10%" /><col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">발주부서</th>
                <td>
                  <div class="input-group input-group-sm w-50">
                    <input v-model="searchForm.deptnm" class="form-control" readonly />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT_SCH')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">PO No</th>
                <td>
                  <input v-model="searchForm.fileno" class="form-control form-control-sm" placeholder="PO 번호 입력" @keyup.enter="search" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 투-그리드 레이아웃 영역 -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 발주 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">발주 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ➡️ 우측: 마스터 상세 폼 + 품목 상세 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

          <!-- 상세 마스터 정보 폼 -->
          <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup>
                  <col style="width: 100px;" /><col />
                  <col style="width: 100px;" /><col />
                  <col style="width: 100px;" /><col />
                  <col style="width: 100px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light">발주부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="formData.deptnm" class="form-control" readonly />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light text-center">PO No</th>
                    <td><input v-model="formData.fileno" class="form-control bg-light text-primary fw-bold text-center" readonly placeholder="자동생성" /></td>
                    <th class="required bg-light text-center">발주일자</th>
                    <td><input v-model="formData.issymd" type="date" class="form-control" /></td>
                    <th class="required bg-light text-center">수입구분</th>
                    <td>
                      <select v-model="formData.imptgbn" class="form-select">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.imptgbn" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="formData.custnm" class="form-control" readonly />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light text-center">원&nbsp;&nbsp;산&nbsp;&nbsp;지</th>
                    <td>
                      <select v-model="formData.nacd" class="form-select">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.nacd" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center">선&nbsp;&nbsp;적&nbsp;&nbsp;항</th>
                    <td>
                      <select v-model="formData.shipport" class="form-select">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.shipport" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center">도&nbsp;&nbsp;착&nbsp;&nbsp;항</th>
                    <td>
                      <select v-model="formData.arvport" class="form-select">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.arvport" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center">결제조건</th>
                    <td>
                      <select v-model="formData.paycond" class="form-select">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.paycond" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center">가격조건</th>
                    <td>
                      <select v-model="formData.pricond" class="form-select">
                        <option value="">선택</option>
                        <option v-for="opt in comboData.pricond" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center">통화/환율</th>
                    <td colspan="3">
                      <div class="d-flex gap-1">
                        <select v-model="formData.currcd" class="form-select" style="width: 100px;">
                          <option value="">선택</option>
                          <option v-for="opt in comboData.currcd" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                        </select>
                        <input v-model="formData.frgnrate" type="number" class="form-control text-end" step="0.01" @input="updateTotals" />
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <th class="bg-light text-center">소요비용</th>
                    <td>
                      <div class="d-flex gap-1 align-items-center">
                        <span class="small">L/C:</span>
                        <input v-model="formData.lcamt" type="number" class="form-control text-end" @input="updateTotals" />
                        <span class="small">통관:</span>
                        <input v-model="formData.xtamt" type="number" class="form-control text-end" @input="updateTotals" />
                      </div>
                    </td>
                    <th class="bg-light text-center">B/L 합계</th>
                    <td colspan="3">
                      <div class="d-flex gap-1 align-items-center">
                        <span class="small">외화:</span>
                        <input :value="formData.frgnamt?.toLocaleString()" class="form-control text-end bg-light" readonly />
                        <span class="small">원화:</span>
                        <input :value="formData.wonamt?.toLocaleString()" class="form-control text-end bg-light" readonly />
                      </div>
                    </td>
                    <th class="bg-light text-center">총합계</th>
                    <td><input :value="formData.costsum?.toLocaleString()" class="form-control text-end bg-light fw-bold text-primary" readonly /></td>
                  </tr>
                  <tr>
                    <th class="bg-light text-center">특기사항</th>
                    <td colspan="7"><input v-model="formData.bigo" class="form-control" placeholder="비고 입력" /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 상세 품목 그리드 영역 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>수입 발주 품목 리스트</span>
              <div class="d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addGridRow" style="font-size: 11px;">+ 행추가</button>
                <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick, computed, onUnmounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { getDate } from '@/composables/useDate'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'
import { useSearchStore } from '@/stores/useSearchStore'
import { useRoute } from 'vue-router'

const authStore = useAuthStore()
const searchStore = useSearchStore()
const route = useRoute()
const { today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

// [1] 데이터 모델링 (HSOD100U 기준 소문자 원칙)
const searchForm = reactive({ deptcd: authStore.deptcd, deptnm: authStore.deptnm, fileno: '' })
const formData = reactive<any>({
  actkind: 's0', cmpycd: authStore.cmpycd, fileno: '', deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  issymd: today, custcd: '', custnm: '', nacd: '',
  currcd: '', frgnrate: 1350, pricond: '', paycond: '', imptgbn: '',
  shipport: '', arvport: '', bigo: '', wonamt: 0, frgnamt: 0, lcamt: 0, xtamt: 0, costsum: 0
})

const comboData = reactive<any>({ imptgbn: [], nacd: [], shipport: [], arvport: [], currcd: [], paycond: [], pricond: [] })

const tableRef1 = ref<HTMLElement | null>(null)
const mainGridRef = ref<HTMLElement | null>(null)
let grid1: Tabulator | null = null
let mainGrid: Tabulator | null = null

// [2] 그리드 초기화
const initGrids = () => {
  if (!tableRef1.value || !mainGridRef.value) return

  grid1 = new Tabulator(tableRef1.value, {
    data: [], // 🚀 초기 데이터 설정
    layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
      { title: "발주일자", field: "issymd", hozAlign: "center", width: 100, formatter: (c) => {
          const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
      }},
      { title: "PO No", field: "fileno", hozAlign: "center", width: 130, cssClass: "fw-bold text-primary", headerSort: false }
    ],
  });
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData().fileno));

  mainGrid = new Tabulator(mainGridRef.value, {
    data: [], // 🚀 초기 데이터 설정
    layout: "fitColumns", height: "100%", placeholder: "발주 품목이 없습니다.", selectable: true,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "선택", width: 40, hozAlign: "center", formatter: "rowSelection", titleFormatter: "rowSelection" },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">신규</span>';
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>';
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>';
          return '';
      }},
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary',
        cellClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow())
      },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unitnm", width: 70, hozAlign: "center" },
      { title: "수량", field: "qty", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "단가", field: "price", width: 110, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "금액", field: "amt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>",
        cellClick: (e, cell) => handleRowAction(cell.getRow())
      }
    ]
  });
}

const calcRow = (row: any) => {
  const d = row.getData();
  const amt = Math.floor(Number(d.qty || 0) * Number(d.price || 0));
  row.update({ amt: amt });
  if (d._state === 'EXIST' && d._status !== '삭제') row.update({ _status: '수정' });
  updateTotals();
}

const updateTotals = () => {
    const data = mainGrid?.getData().filter(i => i._status !== '삭제') || []
    const frgnAmt = data.reduce((acc, cur: any) => acc + (Number(cur.amt) || 0), 0)
    formData.frgnamt = frgnAmt
    formData.wonamt = Math.floor(frgnAmt * formData.frgnrate)
    formData.costsum = (Number(formData.lcamt) || 0) + (Number(formData.xtamt) || 0) + (Number(formData.wonamt) || 0)
}

const handleOpenHelp = (type: string, target?: any) => {
  if (type === 'CUST') openHelp('CUST', (d) => { formData.custcd = d.custcd; formData.custnm = d.custnm });
  else if (type === 'DEPT') openHelp('DEPT', (d) => { formData.deptcd = d.deptcd; formData.deptnm = d.deptnm });
  else if (type === 'DEPT_SCH') openHelp('DEPT', (d) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm });
  else if (type === 'ITEM') {
    openHelp('ITEM', (d) => {
      target.update({ itemcd: d.itemcd, itemnm: d.itemnm, itsize: d.itsize, unitnm: d.unit, price: d.outcost || 0, qty: 1, amt: d.outcost || 0, _status: '입력', _state: 'NEW' });
      updateTotals();
    });
  }
}

const handleRowAction = (row: any) => {
  const d = row.getData();
  if (d._state === 'NEW') row.delete();
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' });
  updateTotals();
}

async function search() {
  try {
    const res = await api.post('/api/hsip/HSIP_100U_STR', { ...searchForm, actkind: 's1' });
    const list = res.data?.data || res.data || [];
    grid1?.setData(list); vAlert('조회되었습니다.');
  } catch (e) { vAlertError('조회 실패'); }
}

async function fetchDetail(fileNo: string) {
  try {
    const res = await api.post('/api/hsip/HSIP_100U_STR', { fileno: fileNo, actkind: 's0' });
    if (res.data?.data?.length) {
      Object.assign(formData, res.data.data[0]);
      // 품목 상세 조회
      const itemRes = await api.post('/api/hsip/HSIP_101U_STR', { fileno: fileNo, actkind: 's0' });
      // itemRes.data.data를 사용하여 그리드 세팅
      mainGrid?.setData((itemRes.data.data || []).map((i: any) => ({ ...i, _state: 'EXIST', _status: '' })));
      updateTotals();
    }
  } catch (e) { vAlertError('상세 로드 실패'); }
}

async function save() {
  if (!formData.custcd) return vAlertError('거래처를 선택하세요.');
  const details = mainGrid?.getData().filter(r => r._status) || [];
  if (!details.length && formData.fileno === '0000') return vAlertError('품목을 추가하세요.');

  try {
    const mst = { ...formData, actkind: formData.fileno === '0000' || !formData.fileno ? 'a0' : 'u0', issymd: formData.issymd.replace(/-/g, ''), userid: authStore.userid };
    const resM = await api.post('/api/hsip/HSIP_100U_STR', mst);
    const mstData = resM.data?.data?.[0] || resM.data?.[0];
    if (mstData) {
      const newFileNo = mstData.fileno;
      for (const item of details) {
        await api.post('/api/hsip/HSIP_101U_STR', { ...item, actkind: item._status === '입력' ? 'a0' : (item._status === '삭제' ? 'd0' : 'u0'), fileno: newFileNo, userid: authStore.userid });
      }
      vAlert('저장되었습니다.'); search(); fetchDetail(newFileNo);
    }
  } catch (e) { vAlertError('저장 실패'); }
}

function initialize() {
  resetForm(formData);
  Object.assign(formData, { cmpycd: authStore.cmpycd, deptcd: authStore.deptcd, deptnm: authStore.deptnm, fileno: '0000', issymd: today, frgnrate: 1350 });
  if (grid1) grid1.setData([]);
  if (mainGrid) mainGrid.setData([]);
  updateTotals();
}

async function handleFullDelete() {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  try {
    await api.post('/api/hsip/HSIP_100U_STR', { fileno: formData.fileno, actkind: 'd0' });
    vAlert('삭제되었습니다.'); initialize(); search();
  } catch (e) { vAlertError('삭제 실패'); }
}

function addGridRow() { mainGrid?.addRow({ qty: 0, price: 0, amt: 0, _status: '입력', _state: 'NEW' }, true); }
function deleteSelectedRows() { mainGrid?.getSelectedRows().forEach(row => handleRowAction(row)); }

onMounted(async () => {
    nextTick(initGrids);

    // 공통 콤보 로드 로직 (수입관리 표준 HS00_000S_STR 사용)
    const loadCombo = async (gbncd: string, target: string) => {
        try {
            const res = await api.post('/api/hs00/HS00_000S_STR', {
                gubun: 'E2', cmpycd: authStore.cmpycd, gbncd: gbncd, code: '', codenm: '', etcval: ''
            });
            const list = res.data?.data || res.data || [];
            comboData[target] = list.map((i: any) => ({
                code: i.code || i.CODE || i.codecd || i.CODECD || '',
                cdnm: i.codenm || i.CODENM || i.cdnm || i.CDNM || ''
            }));
        } catch (e) { console.error(`Combo load error (${target}):`, e); }
    };

    await Promise.all([
        loadCombo('311', 'imptgbn'),
        loadCombo('305', 'nacd'),
        loadCombo('308', 'shipport'),
        loadCombo('309', 'arvport'),
        loadCombo('310', 'currcd'),
        loadCombo('312', 'paycond'),
        loadCombo('314', 'pricond')
    ]);

    initialize();
})

onUnmounted(() => { searchStore.removeTab(route.name as string) });
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
