<!--
	=============================================================
	프로그램명	: 외주(임)가공계약등록 (HPIO290U)
	작성일자	: 2025.02.24
	설명        : 외주 가공 마스터 정보 및 제품/자재 소요량 관리 (HPIO210U 표준 패턴 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-pencil-square me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">외주(임)가공계약등록 (HPIO290U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMaster">조회</button>
        <button class="btn-erp btn-save" @click="saveAll">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">
      <!-- 🅰️ 계약 마스터 정보 폼 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-file-earmark-text me-2 text-primary"></i>계약 기본 정보</span>
        </div>
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
                <th class="required bg-light text-center">품의부서</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.deptnm" type="text" class="form-control" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required bg-light text-center">품의번호</th>
                <td>
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="pumym_f" type="month" class="form-control" style="max-width: 120px;" />
                    <input v-model="masterData.pumno" type="text" class="form-control text-center bg-light fw-bold text-primary" style="width: 60px;" readonly placeholder="번호" />
                  </div>
                </td>
                <th class="required bg-light text-center">품의일자</th>
                <td><input v-model="pumymd_f" type="date" class="form-control" /></td>
                <th class="bg-light"></th><td></td>
              </tr>
              <tr>
                <th class="required bg-light text-center">거 래 처</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterData.custcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterData.custnm" type="text" class="form-control" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required bg-light text-center">계약기간</th>
                <td colspan="3">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="fromdt_f" type="date" class="form-control" />
                    <span class="px-1 opacity-50">~</span>
                    <input v-model="todt_f" type="date" class="form-control" />
                  </div>
                </td>
                <th class="bg-light text-center">특기사항</th>
                <td><input v-model="masterData.remark" class="form-control" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 대상 제품 및 소요 자재 (하단 2단 레이아웃) -->
      <div class="d-flex flex-column gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
        <!-- 상단: 대상 제품 그리드 -->
        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
            <span class="fw-bold small text-dark"><i class="bi bi-box-seam me-2 text-primary"></i>계약 대상 제품 리스트</span>
            <div class="d-flex gap-1">
              <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow('PROD')" style="font-size: 11px;">+ 제품추가</button>
              <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteRows('PROD')" style="font-size: 11px;">- 삭제</button>
            </div>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="prodTableRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- 하단: 소요 자재 그리드 -->
        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
            <span class="fw-bold small text-dark">
              <i class="bi bi-tools me-2 text-success"></i>소요 자재 내역
              <span v-if="selectedProduct.itemnm" class="badge bg-success-subtle text-success border border-success-subtle ms-2">{{ selectedProduct.itemnm }}</span>
            </span>
            <div class="d-flex gap-1">
              <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow('MAT')" :disabled="!selectedProduct.itemcd" style="font-size: 11px;">+ 자재추가</button>
              <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteRows('MAT')" :disabled="!selectedProduct.itemcd" style="font-size: 11px;">- 삭제</button>
            </div>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="matTableRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
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

const authStore = useAuthStore()
const { today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

// [1] 데이터 모델링
const masterData = reactive<any>({
  actkind: 'S0', cmpycd: authStore.cmpycd,
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  pumym: today.replace(/-/g, '').substring(0, 6), pumno: '000',
  pumymd: today.replace(/-/g, ''), fromdt: today.replace(/-/g, ''), todt: today.replace(/-/g, ''),
  custcd: '', custnm: '', remark: ''
})

const selectedProduct = reactive<any>({ itemcd: '', itemnm: '' })

// 포맷팅 헬퍼
const pumym_f = computed({ get: () => masterData.pumym ? `${masterData.pumym.substring(0, 4)}-${masterData.pumym.substring(4, 6)}` : '', set: (v) => { if (v) masterData.pumym = v.replace(/-/g, '') } })
const pumymd_f = computed({ get: () => formatDate(masterData.pumymd), set: (v) => { if (v) masterData.pumymd = v.replace(/-/g, '') } })
const fromdt_f = computed({ get: () => formatDate(masterData.fromdt), set: (v) => { if (v) masterData.fromdt = v.replace(/-/g, '') } })
const todt_f = computed({ get: () => formatDate(masterData.todt), set: (v) => { if (v) masterData.todt = v.replace(/-/g, '') } })

const prodTableRef = ref<HTMLDivElement | null>(null)
const matTableRef = ref<HTMLDivElement | null>(null)
let prodGrid: Tabulator | null = null
let matGrid: Tabulator | null = null

// [2] 그리드 초기화
const initGrids = () => {
  prodGrid = new Tabulator(prodTableRef.value!, {
    layout: "fitColumns", height: "100%", placeholder: "데이터 없음", selectable: 1,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">신규</span>';
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>';
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>';
          return '';
      }},
      { title: "제품명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', cellClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "계약단가", field: "price", width: 120, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 },
        cellEdited: (cell) => { const d = cell.getData(); if (d._state === 'EXIST') cell.getRow().update({ _status: '수정' }); }
      },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ],
  });
  prodGrid.on("rowClick", (e, row) => fetchMaterials(row.getData()));

  matGrid = new Tabulator(matTableRef.value!, {
    layout: "fitColumns", height: "100%", placeholder: "제품을 선택하세요.", selectable: true,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
      { title: "상태", field: "_status", width: 60, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">신규</span>';
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>';
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>';
          return '';
      }},
      { title: "자재명", field: "mitemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-success', cellClick: (e, cell) => handleOpenHelp('MAT', cell.getRow()) },
      { title: "규격", field: "mitsize", width: 150 },
      { title: "단위", field: "munit", width: 70, hozAlign: "center" },
      { title: "소요량", field: "soqty", width: 100, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 2 },
        cellEdited: (cell) => { const d = cell.getData(); if (d._state === 'EXIST') cell.getRow().update({ _status: '수정' }); }
      },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  });
}

// [3] 비즈니스 로직
async function fetchMaster() {
  if (!masterData.deptcd) return vAlertError('부서를 선택하세요.');
  try {
    const res = await api.post('/api/hpio/HPIO_290U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, pumym: masterData.pumym, pumno: masterData.pumno, deptcd: masterData.deptcd, pumgbn: '200' });
    if (res.data?.length) {
      Object.assign(masterData, res.data[0]);
      fetchProducts();
    } else vAlert('데이터가 없습니다.');
  } catch (e) { vAlertError('조회 실패'); }
}

async function fetchProducts() {
  try {
    const res = await api.post('/api/hpio/HPIO_291U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, pumym: masterData.pumym, pumno: masterData.pumno });
    prodGrid?.setData(res.data.map((i: any) => ({ ...i, _state: 'EXIST', _status: '' })));
    matGrid?.clearData(); selectedProduct.itemcd = ''; selectedProduct.itemnm = '';
  } catch (e) {}
}

async function fetchMaterials(prod: any) {
  selectedProduct.itemcd = prod.itemcd; selectedProduct.itemnm = prod.itemnm;
  if (!prod.itemcd || prod._status === '입력') { matGrid?.clearData(); return; }
  try {
    const res = await api.post('/api/hpio/HPIO_292U_STR', { actkind: 'S', cmpycd: authStore.cmpycd, pumym: masterData.pumym, pumno: masterData.pumno, itemcd: prod.itemcd });
    matGrid?.setData(res.data.map((i: any) => ({ ...i, _state: 'EXIST', _status: '' })));
  } catch (e) {}
}

async function saveAll() {
  if (!masterData.custcd) return vAlertError('거래처를 선택하세요.');
  const prods = prodGrid?.getData().filter((r: any) => r._status) || [];
  const mats = matGrid?.getData().filter((r: any) => r._status) || [];
  if (!prods.length && !mats.length && masterData.pumno !== '000') return vAlertError('저장할 변경 내용이 없습니다.');

  if (!confirm('변경된 모든 정보를 저장하시겠습니까?')) return

  try {
    const mst = { ...masterData, actkind: masterData.pumno === '000' ? 'A0' : 'U0', userid: authStore.userid, pumgbn: '200' };
    // 🚀 [Seed-Model] Step 1. 마스터 저장 실행
    const resM = await api.post('/api/hpio/HPIO_290U_STR', mst);
    const mstData = resM.data?.[0];

    // 🚀 [Seed-Model] Step 2. 무결성 키 추출 (0번: 상태/년월, 1번: 메시지/번호)
    const rowValues = mstData?.returnkeyvalue || Object.values(mstData || {});
    const key1 = (mstData?.pumym || rowValues[0] || '').toString().trim();
    const key2 = (mstData?.pumno || rowValues[1] || '').toString().trim();

    // 🚀 [Seed-Model] Step 3. 에러 판별
    if (key1 === '000000') {
        throw new Error(key2 || '업무 규칙 위반으로 저장할 수 없습니다.');
    }

    if (!key1 || !key2) throw new Error('실적번호 수신 실패 (Data Integrity Error)');

    // 🚀 [Seed-Model] Step 4. 상세 내역 연결 (A0 루프)
    const newYm = key1;
    const newNo = key2;

    for (const p of prods) {
      const pAct = p._status === '입력' ? 'A0' : (p._status === '삭제' ? 'D0' : 'U0');
      const resP = await api.post('/api/hpio/HPIO_291U_STR', { ...p, actkind: pAct, cmpycd: authStore.cmpycd, pumym: newYm, pumno: newNo, userid: authStore.userid });
      const pValues = resP.data?.[0]?.returnkeyvalue || Object.values(resP.data?.[0] || {});
      if (pValues[0] === '000000') throw new Error(String(pValues[1] || '제품 실적 저장 중 오류 발생'));
    }
    for (const m of mats) {
      const mAct = m._status === '입력' ? 'A' : (m._status === '삭제' ? 'D' : 'U');
      const resM = await api.post('/api/hpio/HPIO_292U_STR', { ...m, actkind: mAct, cmpycd: authStore.cmpycd, pumym: newYm, pumno: newNo, itemcd: selectedProduct.itemcd, userid: authStore.userid });
      const mValues = resM.data?.[0]?.returnkeyvalue || Object.values(resM.data?.[0] || {});
      if (mValues[0] === '000000') throw new Error(String(mValues[1] || '자재 소모 저장 중 오류 발생'));
    }
    vAlert('저장되었습니다.');
    masterData.pumym = newYm; masterData.pumno = newNo;
    fetchMaster();
  } catch (e) { vAlertError('저장 실패'); }
}

const handleOpenHelp = (type: string, row: any) => {
  if (type === 'DEPT') openHelp('DEPT', (d) => { masterData.deptcd = d.deptcd; masterData.deptnm = d.deptnm });
  else if (type === 'CUST') openHelp('CUST', (d) => { masterData.custcd = d.custcd; masterData.custnm = d.custnm });
  else if (type === 'ITEM') openHelp('ITEM', (d) => row.update({ itemcd: d.itemcd, itemnm: d.itemnm, itsize: d.itsize, unit: d.unit, _status: '입력', _state: 'NEW' }), { codegbn: 'B' });
  else if (type === 'MAT') openHelp('ITEM', (d) => row.update({ mitemcd: d.itemcd, mitemnm: d.itemnm, mitsize: d.itsize, munit: d.unit, _status: '입력', _state: 'NEW' }));
}

const handleRowAction = (row: any) => {
  const d = row.getData();
  if (d._state === 'NEW') row.delete();
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' });
}

const addRow = (type: string) => {
  if (type === 'PROD') prodGrid?.addRow({ price: 0, _status: '입력', _state: 'NEW' }, true);
  else matGrid?.addRow({ soqty: 0, _status: '입력', _state: 'NEW' }, true);
}
const deleteRows = (type: string) => {
  const g = type === 'PROD' ? prodGrid : matGrid;
  g?.getSelectedRows().forEach(r => handleRowAction(r));
}

const initialize = () => {
  resetForm(masterData);
  Object.assign(masterData, { cmpycd: authStore.cmpycd, pumym: today.replace(/-/g, '').substring(0, 6), pumno: '000', pumymd: today.replace(/-/g, ''), fromdt: today.replace(/-/g, ''), todt: today.replace(/-/g, ''), deptcd: authStore.deptcd, deptnm: authStore.deptnm });
  prodGrid?.clearData(); matGrid?.clearData();
  selectedProduct.itemcd = ''; selectedProduct.itemnm = '';
}

const formatDate = (v: any) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v;
onMounted(() => { nextTick(initGrids); })
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
.grid-container-right { border-bottom: 3px solid #005a9f !important; }
</style>
