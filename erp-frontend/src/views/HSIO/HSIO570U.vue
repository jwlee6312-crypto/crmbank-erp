<!--
	=============================================================
	프로그램명	: 타계정출고 (HSIO570U)
	작성일자	: 2025.02.24
	설명        : 타계정 출고 마스터/상세 관리 (HSOD100U 표준 구조 및 소문자 원칙 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-right me-2 text-primary" style="font-size: 18px;"></i>
        구매관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        출고관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">타계정출고 (HSIO570U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-delete" @click="handleFullDelete" :disabled="!masterData.iono || masterData.iono === '0000'">전체삭제</button>
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
                <th class="text-center bg-light">출고일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchParam.fromdt" v-model:todt="searchParam.todt" />
                </td>
                <th class="text-center bg-light">출고부서</th>
                <td>
                  <div class="input-group input-group-sm w-50">
                    <input v-model="searchParam.deptnm" class="form-control" readonly />
                    <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT_SCH')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 투-그리드 레이아웃 -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 목록 그리드 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">출고 목록</div>
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
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light">출고부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="masterData.deptnm" class="form-control" readonly />
                        <button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light">출고번호</th>
                    <td><input :value="displayIoNo" class="form-control bg-light text-primary fw-bold text-center" readonly placeholder="자동생성" /></td>
                    <th class="required bg-light">출고일자</th>
                    <td><input v-model="masterData.ioymd" type="date" class="form-control" /></td>
                    <th class="required bg-light">출고창고</th>
                    <td>
                      <select v-model="masterData.whcd" class="form-select">
                        <option value="">-- 선택 --</option>
                        <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="bg-light">특기사항</th>
                    <td colspan="7"><input v-model="masterData.remark" class="form-control" /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 상세 품목 그리드 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>출고 품목 리스트</span>
              <div class="d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
                <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { getDate } from '@/composables/useDate'

const authStore = useAuthStore()
const { firstDay, today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const searchParam = reactive({ fromdt: firstDay, todt: today, deptcd: authStore.deptcd, deptnm: authStore.deptnm })
const masterData = reactive<any>({
  actkind: 's0',
  ioym: today.replace(/-/g, '').substring(0, 6), iono: '0000',
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  ioymd: today, whcd: '', remark: '', sts: 'n'
})

const displayIoNo = computed(() => (!masterData.iono || masterData.iono === '0000') ? '' : `${masterData.ioym}-${masterData.iono}`)
watch(() => masterData.ioymd, (nv) => { if (nv) masterData.ioym = nv.replace(/-/g, '').substring(0, 6) })

const closingInfo = reactive({ sclsym: '' })
const whOptions = ref<any[]>([])

const tableRef1 = ref<HTMLDivElement | null>(null); const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null; let grid2: Tabulator | null = null

const isClosed = computed(() => {
  if (!closingInfo.sclsym || !masterData.ioymd) return false
  return masterData.ioymd.replace(/-/g, '').substring(0, 6) <= closingInfo.sclsym
})

const initGrids = () => {
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
      { title: "출고일자", field: "ioymd", hozAlign: "center", width: 100, formatter: (c) => {
          const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
      }},
      { title: "출고번호", field: "iono_full", hozAlign: "center", width: 110, cssClass: "fw-bold text-primary", headerSort: false }
    ],
  });
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()));

  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", placeholder: "품목 없음", selectable: true,
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
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', cellClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "규격", field: "itsize", width: 120 },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "수량", field: "ioqty", width: 100, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "단가", field: "price", width: 110, hozAlign: "right", editor: "number", cellEdited: (cell) => calcRow(cell.getRow()) },
      { title: "금액", field: "ioamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "유형", field: "iotypenm", width: 120, cellClick: (e, cell) => handleOpenHelp('IOTYPE', cell.getRow()) },
      { title: "사용부서", field: "usedeptnm", width: 120, cellClick: (e, cell) => handleOpenHelp('IDEPT', cell.getRow()) },
      { title: "삭제", width: 40, hozAlign: "center", formatter: (c) => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ]
  });
}

const calcRow = (row: any) => {
  const d = row.getData(); const amt = Math.floor(Number(d.ioqty || 0) * Number(d.price || 0));
  row.update({ ioamt: amt });
  if (d._state === 'EXIST' && d._status !== '삭제') row.update({ _status: '수정' });
}

async function search() {
  try {
    const res = await api.post('/api/hsio/HSIO_570U_STR', {
        actkind: 's1', gubun: '200',
        deptcd: searchParam.deptcd,
        frymd: searchParam.fromdt.replace(/-/g, ''), toymd: searchParam.todt.replace(/-/g, '')
    });
    grid1?.setData(res.data.map((i: any) => ({ ...i, iono_full: `${i.ioym}-${i.iono}` })));
    vAlert('조회되었습니다.');
  } catch (e) { vAlertError('조회 실패'); }
}

async function fetchDetail(row: any) {
  const fYmd = (d: string) => d && d.length === 8 ? `${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}` : today;
  Object.assign(masterData, { ...row, ioymd: fYmd(row.ioymd) });
  try {
    const res = await api.post('/api/hsio/HSIO_571U_STR', { actkind: 's', gubun: '200', ioym: row.ioym, iono: row.iono });
    grid2?.setData(res.data.map((i: any) => ({ ...i, _state: 'EXIST', _status: '' })));
  } catch (e) { vAlertError('상세 로드 실패'); }
}

async function save() {
  if (isClosed.value) return vAlertError('마감된 월입니다.');
  if (!masterData.deptcd || !masterData.whcd) return vAlertError('부서와 창고를 선택하세요.');

  const details = grid2?.getData().filter((r: any) => r._status) || [];
  if (!details.length && masterData.iono === '0000') return vAlertError('항목을 추가하세요.');

  try {
    const mst = { ...masterData, actkind: masterData.iono === '0000' ? 'a' : 'u', ioymd: masterData.ioymd.replace(/-/g, ''), gubun: '200', userid: authStore.userid };
    const mRes = await api.post('/api/hsio/HSIO_570U_STR', mst);

    if (mRes.data?.length) {
      const ioym = mRes.data[0].ioym; const iono = mRes.data[0].iono;
      for (const item of details) {
        await api.post('/api/hsio/HSIO_571U_STR', {
          ...item, actkind: item._status === '입력' ? 'a' : (item._status === '삭제' ? 'd' : 'u'),
          gubun: '200', ioym, iono, deptcd: masterData.deptcd, whcd: masterData.whcd, ioymd: mst.ioymd, userid: authStore.userid
        });
      }
      vAlert('저장되었습니다.'); search();
    }
  } catch (e) { vAlertError('저장 실패'); }
}

const handleOpenHelp = (type: string, target?: any) => {
  if (type === 'DEPT_SCH') openHelp('DEPT', (d) => { searchParam.deptcd = d.deptcd; searchParam.deptnm = d.deptnm });
  else if (type === 'DEPT') openHelp('DEPT', (d) => { masterData.deptcd = d.deptcd; masterData.deptnm = d.deptnm });
  else if (type === 'ITEM') openHelp('ITEM', (d) => target.update({ itemcd: d.itemcd, itemnm: d.itemnm, unit: d.unit || d.unitnm, price: d.outcost || 0, ioqty: 1, ioamt: d.outcost || 0, _status: '입력', _state: 'NEW' }));
  else if (type === 'IOTYPE') openHelp('COMMON', (d) => target.update({ iotype: d.code, iotypenm: d.cdnm }), { gbncd: '130' });
  else if (type === 'IDEPT') openHelp('DEPT', (d) => target.update({ usedeptcd: d.deptcd, usedeptnm: d.deptnm }));
}

const handleRowAction = (row: any) => { const d = row.getData(); if (d._state === 'NEW') row.delete(); else row.update({ _status: d._status === '삭제' ? '' : '삭제' }); }
const deleteSelectedRows = () => { const sel = grid2?.getSelectedRows(); if (sel?.length) sel.forEach(row => handleRowAction(row)); }
const addRow = () => grid2?.addRow({ ioqty: 0, price: 0, ioamt: 0, _status: '입력', _state: 'NEW' }, true);

function initialize() {
  resetForm(masterData);
  Object.assign(masterData, { cmpycd: authStore.cmpycd, iono: '0000', ioymd: today, ioym: today.replace(/-/g, '').substring(0, 6), deptcd: authStore.deptcd, deptnm: authStore.deptnm, sts: 'n' });
  if (grid1) grid1.setData([]);
  if (grid2) grid2.setData([]);
}

async function handleFullDelete() {
  if (!confirm('정말 전체 삭제하시겠습니까?')) return;
  try {
    await api.post('/api/hsio/HSIO_570U_STR', { ...masterData, actkind: 'd', gubun: '200' });
    vAlert('삭제되었습니다.'); initialize(); search();
  } catch (e) { vAlertError('삭제 실패'); }
}

onMounted(async () => {
    nextTick(initGrids);
    // 💡 [정상화] cmpycd 파라미터 제거하여 백엔드 세션 주입 활용
    api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'W0' } }).then(r => { whOptions.value = r.data; });
    api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'CL' } }).then(r => { if(r.data?.length) closingInfo.sclsym = r.data[0].sclsym; });
    initialize();
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
