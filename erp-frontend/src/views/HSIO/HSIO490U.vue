<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-arrow-left-right me-2 text-primary" style="font-size: 18px;"></i>
        영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매출반품 (HSIO490U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMaster('S')">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-danger" @click="deleteMaster" :disabled="!masterData.IONO || masterData.IONO === '0000'">삭제</button>
      </div>
    </div>

    <!-- 🔍 2. 최상단 검색 바 (ASP 재현) -->
    <div class="p-2 pb-0">
      <div class="card border shadow-sm bg-light bg-opacity-50">
        <div class="card-body py-2 px-3">
          <div class="d-flex align-items-center gap-4">
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-dark" style="min-width: 60px;">판매부서</span>
              <div class="input-group input-group-sm" style="width: 220px;">
                <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                <input v-model="masterData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
                <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
              </div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-dark" style="min-width: 60px;">입고번호</span>
              <div class="d-flex align-items-center gap-1">
                <input v-model="uiIOYM" type="month" class="form-control form-control-sm text-center fw-bold" style="width: 130px;" />
                <input v-model="masterData.IONO" type="text" class="form-control form-control-sm text-center fw-bold text-primary" placeholder="0000" style="width: 60px;" />
                <div class="btn-group btn-group-sm ms-1">
                  <button class="btn btn-outline-secondary" @click="fetchMaster('P')"><i class="bi bi-chevron-left"></i></button>
                  <button class="btn btn-outline-secondary" @click="fetchMaster('N')"><i class="bi bi-chevron-right"></i></button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 💡 3. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
      <!-- 🅰️ 마스터 정보 (ASP 디자인 철저 준수) -->
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-0">
          <table class="erp-table-full">
            <tbody>
              <!-- 1행: 판매부서 | 입고번호 | 입고일자 -->
              <tr>
                <th class="required">판매부서</th>
                <td style="width: 25%;">
                  <div class="input-group input-group-sm w-100">
                    <input v-model="masterData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.DEPTNM" type="text" class="form-control bg-light" readonly />
                  </div>
                </td>
                <th class="required">입고번호</th>
                <td style="width: 25%;">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="uiIOYM" type="month" class="form-control form-control-sm text-center bg-light" style="width: 120px;" readonly />
                    <input v-model="masterData.IONO" type="text" class="form-control form-control-sm text-center bg-light" style="width: 60px;" readonly />
                  </div>
                </td>
                <th class="required">입고일자</th>
                <td>
                  <input v-model="uiIOYMD" type="date" class="form-control form-control-sm w-100" @change="setYM" />
                </td>
              </tr>
              <!-- 2행: 거래처 | 입고창고 | 영업담당 -->
              <tr>
                <th class="required">거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
                <td>
                  <div class="input-group input-group-sm w-100">
                    <input v-model="masterData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 80px;" readonly />
                    <input v-model="masterData.CUSTNM" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="handleOpenHelp('CUST')" />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th>입고창고</th>
                <td>
                  <select v-model="masterData.WHCD" class="form-select form-select-sm w-100">
                    <option v-for="opt in whOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>영업담당</th>
                <td>
                  <select v-model="masterData.USERID" class="form-select form-select-sm w-100">
                    <option v-for="opt in empOptions" :key="opt.USERID" :value="opt.USERID">{{ opt.USERNM }}</option>
                  </select>
                </td>
              </tr>
              <!-- 3행: 배송처 | 배송담당 -->
              <tr>
                <th class="required">배&nbsp;&nbsp;송&nbsp;&nbsp;처</th>
                <td colspan="3">
                  <div class="input-group input-group-sm w-100">
                    <input v-model="masterData.ADDRESS" type="text" class="form-control" placeholder="배송지를 입력하거나 선택하세요." />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('ADDR')"><i class="bi bi-search"></i></button>
                    <input type="hidden" v-model="masterData.TRANCD" />
                    <input type="hidden" v-model="masterData.AREA" />
                  </div>
                </td>
                <th>배송담당</th>
                <td colspan="3">
                  <select v-model="masterData.TRNEMP" class="form-select form-select-sm w-100">
                    <option value="">-- 선택 --</option>
                    <option v-for="opt in empOptions" :key="opt.USERID" :value="opt.USERID">{{ opt.USERNM }}</option>
                  </select>
                </td>
              </tr>
              <!-- 4행: 입고내역 -->
              <tr>
                <th>입고내역</th>
                <td colspan="5">
                  <textarea v-model="masterData.REMARK" rows="1" class="form-control form-control-sm w-100" style="resize: none;" placeholder="반품 사유 및 입고 내역 입력"></textarea>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 디테일 그리드 (ASP 그리드 재현) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 반품 품목 리스트</span>
          <button class="btn-grid-row-add" @click="addRow" :disabled="masterData.STS === 'Y'">행추가</button>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white">
          <div ref="gridElement" style="height: 100%;"></div>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
    <ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" :astKind="String(masterData.ASTKIND || '2')" @close="itemHelpVisible = false" @confirm="onSelectItem" />
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

const masterData = reactive<any>({
  ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOYM: initYM, IONO: '0000', DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  CUSTCD: '', CUSTNM: '', IOYMD: initYMD, WHCD: '', USERID: authStore.USERID, USERNM: authStore.USERNM,
  TRANCD: '', ADDRESS: '', TRNEMP: '', REMARK: '', STS: 'N', CLSYMD: '', SCLSYM: '', PRICEGBN: '1', ASTKIND: '2'
})

const uiIOYM = computed({ get: () => masterData.IOYM ? `${masterData.IOYM.substring(0, 4)}-${masterData.IOYM.substring(4, 6)}` : '', set: (v) => masterData.IOYM = v.replace('-', '') })
const uiIOYMD = computed({ get: () => masterData.IOYMD ? `${masterData.IOYMD.substring(0, 4)}-${masterData.IOYMD.substring(4, 6)}-${masterData.IOYMD.substring(6, 8)}` : '', set: (v) => masterData.IOYMD = v.replace(/-/g, '') })

const gridElement = ref<HTMLElement | null>(null);
const grid = ref<Tabulator | null>(null);
const activeItemCount = ref(0);
const totals = ref({ amt: 0, vat: 0, sum: 0 });

const empOptions = ref<any[]>([]);
const whOptions = ref<any[]>([]);

const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", placeholder: "등록된 품목이 없습니다.",
    columnDefaults: { minWidth: 100, headerSort: false },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      {
        title: "상태", field: "UPKIND", width: 50, hozAlign: "center",
        formatter: (cell) => {
          const v = cell.getValue();
          if (v === 'A') return '<i class="bi bi-plus-circle-fill text-primary"></i>';
          if (v === 'U') return '<i class="bi bi-pencil-square text-warning"></i>';
          if (v === 'D') return '<i class="bi bi-dash-circle-fill text-danger"></i>';
          return '';
        }
      },
      { title: "품목코드", field: "ITEMCD", width: 120, formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>", cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) handleOpenHelp('ITEM', cell.getRow()) } },
      { title: "품목명", field: "ITEMNM", minWidth: 200, widthGrow: 1 },
      { title: "규격", field: "ITSIZE", width: 150 },
      { title: "단위", field: "UNIT", width: 80, hozAlign: "center" },
      { title: "수량", field: "IOQTY", width: 100, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { thousand: ',', precision: 0 } },
      { title: "단가", field: "PRICE", width: 100, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { thousand: ',', precision: 0 } },
      { title: "금액", field: "IOAMT", width: 110, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { thousand: ',', precision: 0 } },
      { title: "부가세", field: "IOVAT", width: 100, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { thousand: ',', precision: 0 } },
      { title: "합계", field: "AMTSUM", width: 120, hozAlign: "right", cssClass: "fw-bold", formatter: "money", formatterParams: { thousand: ',', precision: 0 } },
      { title: "", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>", cellClick: (e, c) => { if (masterData.STS === 'Y') return; const row = c.getRow(); if (row.getData().UPKIND === 'A') row.delete(); else { row.update({ UPKIND: 'D' }); row.getElement().style.opacity = '0.5' }; updateTotals() } }
    ],
    cellEdited: (cell) => {
      const field = cell.getField(); const row = cell.getRow(); const data = row.getData();
      let qty = Number(data.IOQTY) || 0; let price = Number(data.PRICE) || 0;
      let amt = Number(data.IOAMT) || 0; let vat = Number(data.IOVAT) || 0;
      const vatYn = String(data.VATYN || 'Y').trim();

      if (['IOQTY', 'PRICE'].includes(field)) {
        if (masterData.PRICEGBN === '1') {
          amt = Math.floor(qty * price);
          vat = vatYn === 'Y' ? Math.floor(amt * 0.1) : 0;
        } else {
          const sum = Math.floor(qty * price);
          vat = vatYn === 'Y' ? Math.floor((sum / 110) * 10) : 0;
          amt = sum - vat;
        }
      } else if (field === 'IOAMT') {
        vat = vatYn === 'Y' ? Math.floor(amt * 0.1) : 0;
      }
      row.update({ IOAMT: amt, IOVAT: vat, AMTSUM: amt + vat });
      if (data.UPKIND !== 'A') row.update({ UPKIND: 'U' });
      updateTotals();
    }
  });
}

const updateTotals = () => { if (!grid.value) return; const data = grid.value.getData(); const active = data.filter((i: any) => i.UPKIND !== 'D'); activeItemCount.value = active.length; totals.value = active.reduce((acc, cur) => { acc.amt += (Number(cur.IOAMT) || 0); acc.vat += (Number(cur.IOVAT) || 0); acc.sum += (Number(cur.AMTSUM) || 0); return acc }, { amt: 0, vat: 0, sum: 0 }) }

async function fetchMaster(gbn: string) {
  if (!masterData.DEPTCD || !masterData.IOYM) return vAlertError('판매부서와 입고연월 필수');
  try {
    const res = await api.post('/api/hsio/HSIO_490U_STR', { ...masterData, ACTKIND: gbn, CMPYCD: authStore.CMPYCD, IOGBN: '200' });
    if (res.data?.length) {
      Object.assign(masterData, res.data[0]);
      fetchDetail();
    } else { if (gbn === 'S') vAlertError('데이터가 없습니다.'); else masterData.IONO = '0000'; }
  } catch (e) { vAlertError('조회 실패'); }
}

async function fetchDetail() {
  try {
    const res = await api.post('/api/hsio/HSIO_491U_STR', { ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOGBN: '200', IOYM: masterData.IOYM, IONO: masterData.IONO });
    if (grid.value) {
      grid.value.setData(res.data.map((i: any) => ({ ...i, IOQTY: Number(i.IOQTY)*-1, IOAMT: Number(i.IOAMT)*-1, IOVAT: Number(i.IOVAT)*-1, AMTSUM: (Number(i.IOAMT)+Number(i.IOVAT))*-1, UPKIND: 'U' })));
      setTimeout(updateTotals, 100);
    }
  } catch (e) { vAlertError('상세 로드 실패'); }
}

async function save() {
  const ioYmd = String(masterData.IOYMD).replace(/-/g, '');
  if (ioYmd <= masterData.CLSYMD) return vAlertError(`회계 마감(${masterData.CLSYMD}) 이전으로는 등록할 수 없습니다.`);
  if (ioYmd.substring(0, 6) <= masterData.SCLSYM) return vAlertError(`영업 마감(${masterData.SCLSYM}) 이전으로는 등록할 수 없습니다.`);
  if (!masterData.CUSTCD) return vAlertError('거래처를 선택하세요.');

  try {
    const act = (masterData.IONO === '0000' || !masterData.IONO) ? 'A' : 'U';
    const mRes = await api.post('/api/hsio/HSIO_490U_STR', { ...masterData, ACTKIND: act, IOGBN: '200', IOTYPE: '100', SUMTOT: totals.value.sum, UPDEMP: authStore.USERID });
    if (mRes.data?.length) {
      const ioYM = mRes.data[0].IOYM || masterData.IOYM;
      const ioNo = mRes.data[0].IONO || masterData.IONO;
      const ino = mRes.data[0].INO || '';
      const gridData = grid.value!.getData();
      for (const item of gridData) {
        if (!item.ITEMCD) continue;
        await api.post('/api/hsio/HSIO_491U_STR', { ...item, ACTKIND: item.UPKIND, CMPYCD: authStore.CMPYCD, IOGBN: '200', IOTYPE: '100', IOYM: ioYM, IONO: ioNo, IOYMD: ioYmd, INO: ino, UPDEMP: authStore.USERID });
      }
      vAlert('정상으로 작업이 되었습니다.'); fetchMaster('S');
    }
  } catch (e) { vAlertError('저장 실패'); }
}

function deleteMaster() {
  if (!confirm("정말 삭제하시겠습니까?")) return;
  api.post('/api/hsio/HSIO_490U_STR', { ...masterData, ACTKIND: 'D', IOGBN: '200' }).then(() => { vAlert('삭제완료'); initialize(); });
}

function initialize() {
  const clsy = masterData.CLSYMD; const sclsy = masterData.SCLSYM; resetForm(masterData);
  Object.assign(masterData, { ACTKIND: 'S', CMPYCD: authStore.CMPYCD, IOYM: initYM, IONO: '0000', DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM, IOYMD: initYMD, STS: 'N', USERID: authStore.USERID, USERNM: authStore.USERNM, CLSYMD: clsy, SCLSYM: sclsy, PRICEGBN: '1', ASTKIND: '2' });
  if (grid.value) grid.value.clearData(); updateTotals();
}

function handleOpenHelp(type: string, target?: any) {
  if (type === 'DEPT') {
    openHelp('DEPT', (d) => { masterData.DEPTCD = d.DEPTCD; masterData.DEPTNM = d.DEPTNM });
  } else if (type === 'CUST') {
    openHelp('CUST', (d) => { masterData.CUSTCD = d.CUSTCD; masterData.CUSTNM = d.CUSTNM });
  } else if (type === 'ITEM') {
    if (!masterData.CUSTCD) return vAlertError('거래처를 먼저 선택하세요.');
    currentTargetRow.value = target;
    itemHelpVisible.value = true;
  } else if (type === 'ADDR') {
    if (!masterData.CUSTCD) return vAlertError('거래처 선택 필수');
    Object.assign(modalProps, {
      title: '배송지 선택',
      path: '/api/hs00/HS00_000S_STR',
      defaultField: 'CUSTNM',
      data: { GUBUN: 'T0', CMPYCD: authStore.CMPYCD, CODE: masterData.CUSTCD },
      columns: [{ title: '배송지명', field: 'CUSTNM' }, { title: '주소', field: 'ADDRESS' }],
      onConfirm: (d: any) => { masterData.ADDRESS = d.ADDRESS; masterData.TRANCD = d.TRANCD; masterData.AREA = d.AREA }
    });
    modalVisible.value = true;
  }
}

const onSelectItem = (d: any) => {
  if (!currentTargetRow.value) return
  currentTargetRow.value.update({
    ITEMCD: d.ITEMCD,
    ITEMNM: d.ITEMNM,
    ITSIZE: d.ITSIZE,
    UNIT: d.UNIT || d.UNITNM,
    PRICE: d.OUTCOST || d.PRICE || 0,
    VATYN: d.VATYN || 'Y',
    UPKIND: 'A'
  });
  itemHelpVisible.value = false; updateTotals();
}

function addRow() { grid.value?.addRow({ IOQTY: 0, PRICE: 0, IOAMT: 0, IOVAT: 0, AMTSUM: 0, UPKIND: 'A' }) }
function formatNumber(val: any) { return new Intl.NumberFormat().format(Number(val) || 0) }
function setYM() { if (masterData.IOYMD) masterData.IOYM = masterData.IOYMD.substring(0, 7).replace('-', ''); }

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { GUBUN: 'CL', CMPYCD: authStore.CMPYCD } }).then(r => { if (r.data?.length) { masterData.CLSYMD = String(Object.values(r.data[0])[0]).trim(); masterData.SCLSYM = String(Object.values(r.data[0])[1]).trim(); } })
  whOptions.value = (await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })).data.map((i: any) => ({ CODECD: i.WHCD || i.CODECD || Object.values(i)[0], CODENM: i.WHNM || i.CODENM || Object.values(i)[1] }));
  api.get('/api/ha00/HA00_00P_STR', { params: { GUBUN: 'SD', CMPYCD: authStore.CMPYCD } }).then(r => { if (r.data) empOptions.value = r.data.map((i: any) => ({ USERID: i.USERID, USERNM: i.USERNM })) });
  nextTick(initGrid);
})
</script>
