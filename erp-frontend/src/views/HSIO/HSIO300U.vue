<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hsio300u-wrapper d-flex flex-column h-100 bg-white p-0">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-cash-coin me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        입금관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">입금등록 (HSIO300U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">
          <i class="bi bi-arrow-clockwise"></i> 초기화
        </button>
        <button class="btn-erp btn-search" @click="fetchOrder">
          <i class="bi bi-search"></i> 조회
        </button>
        <button class="btn-erp btn-save" @click="save">
          <i class="bi bi-save"></i> 저장
        </button>
        <button class="btn-erp btn-delete" @click="deleteOrder" :disabled="!masterData.IMNO || masterData.IMNO === '0000'">
          <i class="bi bi-trash"></i> 삭제
        </button>
      </div>
    </div>

    <!-- 🔍 2. 최상단 검색 조건 영역 -->
    <div class="p-2 pb-0 flex-shrink-0">
      <div class="card border shadow-sm overflow-hidden">
        <table class="erp-table-full" style="table-layout: fixed;">
          <colgroup>
            <col style="width: 50%;" />
            <col style="width: 50%;" />
          </colgroup>
          <tbody>
            <tr>
              <td>
                <div class="d-flex align-items-center px-2">
                  <span class="erp-label me-2">입금부서</span>
                  <div class="input-group input-group-sm flex-nowrap" style="max-width: 300px;">
                    <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
                    <input v-model="masterData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </div>
              </td>
              <td>
                <div class="d-flex align-items-center px-2">
                  <span class="erp-label me-2">입금번호</span>
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="uiIMYM" type="month" class="form-control form-control-sm text-center fw-bold" style="width: 130px;" :readonly="masterData.IMNO !== '0000'" />
                    <input v-model="masterData.IMNO" type="text" class="form-control form-control-sm text-center fw-bold text-primary bg-light" placeholder="0000" style="width: 70px;" readonly />
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 💡 3. 입력 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 마스터 정보 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <colgroup>
              <col style="width: 100px;" />
              <col style="width: 25%" />
              <col style="width: 100px;" />
              <col style="width: 20%" />
              <col style="width: 100px;" />
              <col style="width: auto;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">입금일자</th>
                <td><input v-model="uiIMYMD" type="date" class="form-control form-control-sm" @change="onImymdChange" /></td>
                <th class="required">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm flex-nowrap">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 70px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control form-control-sm bg-light" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>여신잔액/기한</th>
                <td>
                  <div class="d-flex gap-1">
                    <input :value="formatNumber(masterData.JANAMT)" class="form-control form-control-sm text-end bg-light fw-bold text-primary" readonly style="width: 120px;" />
                    <input v-model="masterData.RCVDD" class="form-control form-control-sm text-center bg-light" readonly style="max-width: 100px;" />
                  </div>
                </td>
              </tr>
              <tr>
                <th>적&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;요</th>
                <td colspan="5">
                  <input v-model="masterData.REMARK" type="text" class="form-control form-control-sm" placeholder="입금 관련 특기사항 입력" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 디테일 그리드 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
          <span class="fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 입금 상세 내역
          </span>
          <button class="btn btn-sm btn-outline-primary px-3" style="height: 28px; font-size: 12px; font-weight: 600;" @click="addRow">
            <i class="bi bi-plus-circle me-1"></i> 행추가
          </button>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%; width: 100%;"></div>
        </div>
      </div>
    </div>

    <!-- 📊 하단 요약 바 -->
    <div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
      <div class="row align-items-center w-100">
        <div class="col-md-3 small">조회 건수: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
        <div class="col-md-9 text-end">
          <span class="me-4 small opacity-75">어음 합계: <span class="fw-bold text-warning ms-1">{{ formatNumber(billTotal) }}</span></span>
          <span class="fs-5 ms-2 fw-light">총 입금액: <span class="fw-bold text-white ms-2">{{ formatNumber(amtTotal) }}</span> 원</span>
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
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const now = new Date()
const initYM = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`
const initYMD = `${initYM}${String(now.getDate()).padStart(2, '0')}`

const masterData = reactive<any>({
  ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IMYM: initYM, IMNO: '0000',
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, CUSTCD: '', CUSTNM: '',
  IMYMD: initYMD, REMARK: '', JANAMT: 0, RCVDD: '', CLSYMD: '', SCLSYM: ''
})

const uiIMYM = computed({ get: () => masterData.IMYM ? `${masterData.IMYM.substring(0, 4)}-${masterData.IMYM.substring(4, 6)}` : '', set: (v) => masterData.IMYM = v.replace('-', '') })
const uiIMYMD = computed({ get: () => masterData.IMYMD ? `${masterData.IMYMD.substring(0, 4)}-${masterData.IMYMD.substring(4, 6)}-${masterData.IMYMD.substring(6, 8)}` : '', set: (v) => masterData.IMYMD = v.replace(/-/g, '') })

const onImymdChange = () => { if (masterData.IMYMD) masterData.IMYM = masterData.IMYMD.substring(0, 6) }

const gridElement = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)
const activeItemCount = ref(0)
const amtTotal = ref(0)
const billTotal = ref(0)

const imTypeOptions = ref<any[]>([])
const billGbnOptions = ref<any[]>([])

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns",
    height: "100%",
    placeholder: "등록된 입금 내역이 없습니다.",
    columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
    columns: [
      { title: "No", formatter: "rownum", width: 40 },
      {
        title: "상태", field: "UPKIND", width: 60,
        formatter: (cell) => {
          const v = cell.getValue();
          if (v === 'A') return '<i class="bi bi-plus-circle-fill text-primary"></i>';
          if (v === 'U') return '<i class="bi bi-pencil-square text-warning"></i>';
          if (v === 'D') return '<i class="bi bi-dash-circle-fill text-danger"></i>';
          return '';
        }
      },
      {
        title: "입금유형", field: "IMTYPE", widthGrow: 3, minWidth: 150, editor: "list",
        editorParams: { values: () => imTypeOptions.value.reduce((acc, opt) => ({ ...acc, [opt.CODECD]: opt.CODENM }), {}) },
        formatter: (cell) => {
          const val = cell.getValue();
          const opt = imTypeOptions.value.find(o => String(o.CODECD).trim() === String(val || '').trim());
          return opt ? opt.CODENM : val;
        }
      },
      { title: "입금액", field: "IMAMT", width: 120, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 }, cssClass: "bg-yellow fw-bold" },
      {
        title: "관리번호", field: "MGTNO", widthGrow: 2, minWidth: 150, editor: "input",
        formatter: (cell) => {
            const val = cell.getValue() || '';
            return `<div class='d-flex align-items-center justify-content-between w-100 px-1 flex-nowrap'><span>${val}</span><i class='bi bi-search text-primary cursor-pointer'></i></div>`;
        },
        cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) openHelp('MGT', cell.getRow()) }
      },
      { title: "어음액면가", field: "BILLAMT", width: 120, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 } },
      {
        title: "어음종류", field: "BILLGBN", widthGrow: 1, minWidth: 100, editor: "list",
        editorParams: { values: () => billGbnOptions.value.reduce((acc, opt) => ({ ...acc, [opt.CODECD]: opt.CODENM }), {}) },
        formatter: (cell) => {
          const val = cell.getValue();
          const opt = billGbnOptions.value.find(o => String(o.CODECD).trim() === String(val || '').trim());
          return opt ? opt.CODENM : val;
        }
      },
      { title: "발행인", field: "PUBMAN", widthGrow: 1, minWidth: 100, editor: "input" },
      { title: "만기일", field: "ENDYMD", width: 110, editor: "input" },
      { title: "발행은행", field: "PUBBANK", widthGrow: 1, minWidth: 120, editor: "input" },
      {
        title: "삭제", width: 60,
        formatter: () => "<i class='bi bi-trash text-danger cursor-pointer' style='font-size:16px;'></i>",
        cellClick: (e, c) => {
          const row = c.getRow();
          if (row.getData().UPKIND === 'A') row.delete();
          else {
            const cur = row.getData().UPKIND;
            row.update({ UPKIND: cur === 'D' ? 'U' : 'D' });
            row.getElement().style.opacity = row.getData().UPKIND === 'D' ? '0.5' : '1';
          }
          updateTotals();
        }
      }
    ]
  });

  grid.value.on("cellEdited", (cell: any) => {
    const row = cell.getRow();
    if (row.getData().UPKIND !== 'A') row.update({ UPKIND: 'U' });
    updateTotals();
  });
}

const updateTotals = () => {
  if (!grid.value) return;
  const data = grid.value.getData();
  const activeData = data.filter((i: any) => i.UPKIND !== 'D');
  activeItemCount.value = activeData.length;
  amtTotal.value = activeData.reduce((acc, cur) => acc + (Number(cur.IMAMT) || 0), 0);
  billTotal.value = activeData.reduce((acc, cur) => acc + (Number(cur.BILLAMT) || 0), 0);
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

async function fetchOrder() {
  if (!masterData.IMYM || !masterData.IMNO) return vAlertError('입금번호 입력 필수');
  try {
    const res = await api.post('/api/hsio/HSIO_300U_STR', { ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IMYM: masterData.IMYM, IMNO: masterData.IMNO, DEPTCD: masterData.DEPTCD });
    if (res.data?.length) {
      Object.assign(masterData, res.data[0]);
      fetchDetail();
      vAlert('조회되었습니다.');
    } else {
      vAlert('조회된 데이터가 없습니다.');
    }
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetail() {
  try {
    const res = await api.post('/api/hsio/HSIO_300U_STR', { ACTKIND: 'S1', CMPYCD: authStore.CMPYCD, IMYM: masterData.IMYM, IMNO: masterData.IMNO, DEPTCD: masterData.DEPTCD });
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => ({ ...i, UPKIND: 'U' })));
      setTimeout(updateTotals, 100);
    }
  } catch (e) { vAlertError('상세 로드 실패') }
}

async function save() {
  if (!masterData.CUSTCD) return vAlertError('거래처를 선택하세요.');
  if (amtTotal.value <= 0) return vAlertError('입금액을 입력하세요.');
  try {
    const masterKind = (masterData.IMNO === '0000' || !masterData.IMNO) ? 'A0' : 'U0';
    const mRes = await api.post('/api/hsio/HSIO_300U_STR', { ...masterData, ACTKIND: masterKind });
    if (mRes.data?.length) {
      const newImNo = mRes.data[0].IMNO || masterData.IMNO;
      const details = grid.value!.getData();
      for (const item of details) {
        if (!item.IMTYPE) continue;
        await api.post('/api/hsio/HSIO_300U_STR', { ...item, ACTKIND: item.UPKIND + '1', CMPYCD: authStore.CMPYCD, IMYM: masterData.IMYM, IMNO: newImNo, DEPTCD: masterData.DEPTCD, CUSTCD: masterData.CUSTCD, IMYMD: masterData.IMYMD, REMARK: masterData.REMARK });
      }
      vAlert('저장완료'); masterData.IMNO = newImNo; fetchOrder();
    }
  } catch (e) { vAlertError('저장 실패') }
}

async function deleteOrder() {
  if (!confirm('정말로 삭제하시겠습니까?')) return;
  try {
    await api.post('/api/hsio/HSIO_300U_STR', { ACTKIND: 'D0', CMPYCD: authStore.CMPYCD, IMYM: masterData.IMYM, IMNO: masterData.IMNO, DEPTCD: masterData.DEPTCD });
    vAlert('삭제되었습니다.'); initialize();
  } catch (e) { vAlertError('삭제 실패') }
}

function initialize() {
  resetForm(masterData);
  Object.assign(masterData, { ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IMYM: initYM, IMNO: '0000', DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, IMYMD: initYMD, JANAMT: 0 });
  if (grid.value) grid.value.clearData();
  updateTotals();
}

function addRow() {
  if (grid.value) {
    grid.value.addRow({ UPKIND: 'A', IMTYPE: '', IMAMT: 0, MGTNO: '', BILLAMT: 0, BILLGBN: '000', PUBYMD: '', PUBMAN: '', ENDYMD: '', PUBBANK: '' });
    updateTotals();
  }
}

function openHelp(type: string, target?: any) {
  if (type === 'CUST') {
    Object.assign(modalProps, { title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'CUSTNM', large: true, data: { GUBUN: 'C4', CMPYCD: authStore.CMPYCD, LIMITOFFSET: 0, LIMITROWS: 20 }, columns: [{ title: '코드', field: 'CUSTCD', width: 70 }, { title: '거래처명', field: 'CUSTNM', width: 180 }, { title: '사업자번호', field: 'CUSTNO', width: 110 }], onConfirm: (data: any) => { masterData.CUSTCD = data.CUSTCD; masterData.CUSTNM = data.CUSTNM; api.post('/api/ha00/HA00_010S_STR', { GUBUN: 'C1', CMPYCD: authStore.CMPYCD, CODE: data.CUSTCD }).then(r => { if (r.data?.length) { masterData.JANAMT = r.data[0].JANAMT; masterData.RCVDD = r.data[0].RCVDD; } }) } }); modalVisible.value = true;
  } else if (type === 'DEPT') {
    Object.assign(modalProps, { title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'DEPTNM', data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD }, columns: [{ title: '코드', field: 'DEPTCD', width: 80 }, { title: '부서명', field: 'DEPTNM', width: 180 }], onConfirm: (data: any) => { masterData.DEPTCD = data.DEPTCD; masterData.DEPTNM = data.DEPTNM } }); modalVisible.value = true;
  } else if (type === 'MGT' && target) {
    const rowData = target.getData();
    const imType = String(rowData.IMTYPE || '').trim();
    let title = '관리번호 선택'; let gubun = 'M0'; let gbncd = ''; let code = ''; let columns: any[] = []; let onConfirm = (data: any) => {};
    if (imType === '300') {
        title = '카드번호 선택'; gbncd = '040'; code = '1110';
        columns = [{ title: '카드번호', field: 'MGTNO', width: 150 }, { title: '카드명', field: 'MGTNM', width: 200 }];
        onConfirm = (d: any) => target.update({ MGTNO: d.MGTNO, MGTNM: d.MGTNM });
    } else if (imType === '200' || imType === '600') {
        title = '계좌번호 선택'; gbncd = '010'; code = '1120';
        columns = [{ title: '계좌번호', field: 'GUJANO', width: 150 }, { title: '은행명', field: 'BANKNM', width: 200 }];
        onConfirm = (d: any) => target.update({ MGTNO: d.GUJANO, MGTNM: d.BANKNM });
    } else if (imType === '210') {
        title = '계좌번호 선택'; gbncd = '010'; code = '1145';
        columns = [{ title: '계좌번호', field: 'GUJANO', width: 150 }, { title: '은행명', field: 'BANKNM', width: 200 }];
        onConfirm = (d: any) => target.update({ MGTNO: d.GUJANO, MGTNM: d.BANKNM });
    } else if (imType === '500' || imType === '510') {
        title = '전표 선택'; gubun = 'P1'; gbncd = (imType === '500' ? '2110' : '2125'); code = masterData.CUSTCD;
        columns = [{ title: '전표번호', field: 'slipno', width: 150 }];
        onConfirm = (d: any) => target.update({ MGTNO: d.slipno, MGTNM: d.slipno });
    } else { return vAlertError('관리번호를 입력하는 유형이 아닙니다.'); }
    Object.assign(modalProps, { title, path: '/api/ha00/HA00_00P_STR', defaultField: columns[0].field, large: true, data: { GUBUN: gubun, CMPYCD: authStore.CMPYCD, GBNCD: gbncd, CODE: code }, columns, onConfirm: (d: any) => onConfirm(d) });
    modalVisible.value = true;
  }
}

async function loadComboCodes(gbn: string, gbnCd: string) { try { const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: gbn, CMPYCD: authStore.CMPYCD, GBNCD: gbnCd } }); return res.data.map((item: any) => ({ CODECD: String(item.CODE || item.CODECD || Object.values(item)[0]).trim(), CODENM: String(item.CDNM || item.CODENM || Object.values(item)[1]).trim() })) } catch (e) { return [] } }
async function loadComboImgbn(gbn: string, gbnCd: string) { try { const res = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: gbn, CMPYCD: authStore.CMPYCD, GBNCD: gbnCd } }); return res.data.map((item: any) => ({ CODECD: String(item.IMGBN || item.imgbn || '').trim(), CODENM: String(item.IMGBNNM || item.imgbnnm || '').trim() })) } catch (e) { return [] } }

const formatNumber = (val: any) => new Intl.NumberFormat().format(Number(val) || 0)

onMounted(async () => {
  imTypeOptions.value = await loadComboImgbn('I2', '');
  billGbnOptions.value = await loadComboCodes('E0', '150');
  nextTick(() => { initGrid(); updateTotals(); });
})
</script>
<style scoped>
.hsio300u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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