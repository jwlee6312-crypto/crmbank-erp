<!--
	=============================================================
	프로그램명	: 자재이관출고 (HPIO500U)
	작성일자	: 2025.02.24
	설명        : 창고 간 자재 이관 출고 및 입고 자동 연계 관리 (HPIO210U 표준 패턴 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-arrow-left-right me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">자재이관출고 (HPIO500U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMaster">조회</button>
        <button class="btn-erp btn-save" @click="saveData">저장</button>
        <button class="btn-erp btn-delete" @click="deleteData" :disabled="!masterData.iono">전체삭제</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 🅰️ 출고 마스터 정보 폼 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-file-earmark-text me-2 text-primary"></i>이관 마스터 정보</span>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center">출고부서</th>
                <td>
                  <select v-model="masterData.deptcd" class="form-select form-select-sm">
                    <option v-for="opt in deptOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                  </select>
                </td>
                <th class="required bg-light text-center">출고번호</th>
                <td>
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="ioym_f" type="month" class="form-control" style="max-width: 120px;" />
                    <input v-model="masterData.iono" type="text" class="form-control text-center bg-light fw-bold text-primary" style="width: 60px;" readonly placeholder="번호" />
                  </div>
                </td>
                <th class="required bg-light text-center">출고일자</th>
                <td><input v-model="ioymd_f" type="date" class="form-control" /></td>
              </tr>
              <tr>
                <th class="required bg-light text-center">출고창고</th>
                <td>
                  <select v-model="masterData.whcd" class="form-select form-select-sm">
                    <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
                  </select>
                </td>
                <th class="required bg-light text-center">입고창고</th>
                <td>
                  <select v-model="masterData.iwhcd" class="form-select form-select-sm">
                    <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center">특기사항</th>
                <td><input v-model="masterData.remark" class="form-control" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 자재 상세 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>이관 자재 상세 내역</span>
          <div class="d-flex gap-1">
            <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
            <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="tableRef" class="tabulator-instance flex-grow-1"></div>
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
  deptcd: authStore.deptcd,
  ioym: today.replace(/-/g, '').substring(0, 6), iono: '',
  ioymd: today.replace(/-/g, ''),
  whcd: '200', iwhcd: '100',
  remark: ''
})

const deptOptions = ref<any[]>([]); const whOptions = ref<any[]>([])
const closingInfo = reactive({ clsymd: '', sclsym: '' })

// 포맷팅 헬퍼
const ioym_f = computed({ get: () => masterData.ioym ? `${masterData.ioym.substring(0, 4)}-${masterData.ioym.substring(4, 6)}` : '', set: (v) => { if (v) masterData.ioym = v.replace(/-/g, '') } })
const ioymd_f = computed({ get: () => formatDate(masterData.ioymd), set: (v) => { if (v) masterData.ioymd = v.replace(/-/g, '') } })

const tableRef = ref<HTMLDivElement | null>(null)
let grid: Tabulator | null = null

// [2] 그리드 초기화
const initGrids = () => {
  grid = new Tabulator(tableRef.value!, {
    layout: "fitColumns", height: "100%", placeholder: "데이터 없음", selectable: true,
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
      { title: "자재코드", field: "itemcd", width: 120, hozAlign: "center" },
      { title: "자재명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: 'fw-bold text-primary', cellClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
      { title: "규격", field: "itsize", width: 180 },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "출고수량", field: "ioqty", width: 150, hozAlign: "right", editor: "number", cssClass: "bg-light-yellow fw-bold",
        cellEdited: (cell) => { const d = cell.getData(); if (d._state === 'EXIST') cell.getRow().update({ _status: '수정' }); }
      },
      { title: "비고", field: "remark", minWidth: 200, editor: "input",
        cellEdited: (cell) => { const d = cell.getData(); if (d._state === 'EXIST') cell.getRow().update({ _status: '수정' }); }
      },
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger'></i>", cellClick: (e, cell) => handleRowAction(cell.getRow()) }
    ],
  });
}

// [3] 비즈니스 로직
const fetchDeptOptions = async () => {
  try {
    const res = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'D0', cmpycd: authStore.cmpycd });
    deptOptions.value = res.data.map((i: any) => ({ code: i.code || i.code, cdnm: i.cdnm }));
  } catch (e) {}
}

const fetchWhOptions = async () => {
  try {
    const res = await api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } });
    whOptions.value = res.data.map((i: any) => ({ whcd: i.whcd, whnm: i.whnm }));
  } catch (e) {}
}

async function fetchMaster() {
  if (!masterData.deptcd || !masterData.ioym || !masterData.iono) return vAlertError('조회 조건을 확인하세요.');
  try {
    const res = await api.post('/api/hpio/HPIO_500U_STR', { actkind: 'S', cmpycd: authStore.cmpycd, iogbn: '200', ioym: masterData.ioym, iono: masterData.iono, deptcd: masterData.deptcd });
    if (res.data?.length) {
      Object.assign(masterData, res.data[0]);
      fetchDetails();
    } else vAlert('데이터가 없습니다.');
  } catch (e) { vAlertError('조회 실패'); }
}

async function fetchDetails() {
  try {
    const res = await api.post('/api/hpio/HPIO_501U_STR', { actkind: 'S', cmpycd: authStore.cmpycd, iogbn: '200', ioym: masterData.ioym, iono: masterData.iono });
    grid?.setData(res.data.map((i: any) => ({ ...i, _state: 'EXIST', _status: '' })));
  } catch (e) {}
}

async function saveData() {
  const ioYmd = masterData.ioymd.replace(/-/g, '')
  if (ioYmd <= closingInfo.clsymd) return vAlertError('회계 마감된 일자입니다.');

  const details = grid?.getData().filter((r: any) => r._status) || [];
  if (!details.length && !masterData.iono) return vAlertError('출고할 자재를 추가하세요.');

  if (!confirm('저장하시겠습니까?')) return

  try {
    const actkind = !masterData.iono ? 'A' : 'U'
    // 🚀 [Seed-Model] Step 1. 마스터 저장
    const resM = await api.post('/api/hpio/HPIO_500U_STR', { ...masterData, actkind, cmpycd: authStore.cmpycd, iogbn: '200', linecd: '010', progcd: '888', userid: authStore.userid });
    const mstData = resM.data?.[0];

    // 🚀 [Seed-Model] Step 2. 무결성 키 추출 (0번: 상태/년월, 1번: 메시지/번호)
    const rowValues = mstData?.returnkeyvalue || Object.values(mstData || {});
    const key1 = (mstData?.ioym || rowValues[0] || '').toString().trim();
    const key2 = (mstData?.iono || rowValues[1] || '').toString().trim();

    // 🚀 [Seed-Model] Step 3. 에러 판별
    if (key1 === '000000') {
        throw new Error(key2 || '업무 규칙 위반으로 저장할 수 없습니다.');
    }

    if (!key1 || !key2) {
        throw new Error('입출 번호를 서버로부터 수신하지 못했습니다. (Data Integrity Error)');
    }

    // 🚀 [Seed-Model] Step 4. 상세 내역 연결 저장 (A0 루프)
    const newIono = key2;
    const newInno = mstData?.inno || rowValues[2] || ''; // 입고번호가 있다면 추출

    for (const item of details) {
      const act = item._status === '입력' ? 'A' : (item._status === '삭제' ? 'D' : 'U');
      const resDtl = await api.post('/api/hpio/HPIO_501U_STR', {
        ...item, actkind: act, cmpycd: authStore.cmpycd, iogbn: '200',
        ioym: key1, iono: newIono, inno: newInno,
        deptcd: masterData.deptcd, whcd: masterData.whcd, iwhcd: masterData.iwhcd,
        ioymd: masterData.ioymd, userid: authStore.userid
      });

      // 상세 저장 결과도 무결성 체크
      const dtlValues = resDtl.data?.[0]?.returnkeyvalue || Object.values(resDtl.data?.[0] || {});
      if (dtlValues[0] === '000000') {
          throw new Error(dtlValues[1] || '상세 내역 저장 중 오류 발생');
      }
    }

    vAlert('저장되었습니다.');
    masterData.iono = newIono;
    fetchMaster();
  } catch (e: any) { vAlertError(e.message || '저장 실패'); }
}

async function deleteData() {
  if (!confirm('현재 이관 내역 전체를 삭제하시겠습니까?')) return
  try {
    await api.post('/api/hpio/HPIO_500U_STR', { ...masterData, actkind: 'D', cmpycd: authStore.cmpycd, iogbn: '200', linecd: '010', progcd: '888' });
    vAlert('삭제되었습니다.'); initialize();
  } catch (e) { vAlertError('삭제 실패'); }
}

const handleOpenHelp = (type: string, target?: any) => {
  const props: any = {
    title: '',
    path: '',
    data: { cmpycd: authStore.cmpycd },
    columns: [],
    onConfirm: () => {}
  }

  if (type === 'ITEM') {
    props.title = '이관 자재 선택'
    props.path = '/api/hp00/HP00_000S_STR'
    props.data.gubun = 'I0'
    props.data.etcval = 'A' // 자재(I) 필터
    props.columns = [
      { title: '자산구분', field: 'astkindnm', width: 100, hozAlign: 'center' },
      { title: '자재코드', field: 'itemcd', width: 100, hozAlign: 'center' },
      { title: '자재명', field: 'itemnm', width: 200 },
      { title: '규격', field: 'itsize', width: 150 },
      { title: '단위', field: 'unit', width: 80, hozAlign: 'center' }
    ]
    props.onConfirm = (d: any) => target.update({
      itemcd: d.itemcd, itemnm: d.itemnm, itsize: d.itsize, unit: d.unit,
      _status: '입력', _state: 'NEW'
    })
  }

  Object.assign(modalProps, props)
  modalVisible.value = true
}

const handleRowAction = (row: any) => {
  const d = row.getData();
  if (d._state === 'NEW') row.delete();
  else row.update({ _status: d._status === '삭제' ? '' : '삭제' });
}

const addRow = () => grid?.addRow({ ioqty: 0, _status: '입력', _state: 'NEW' }, true);
const deleteSelectedRows = () => grid?.getSelectedRows().forEach(r => handleRowAction(r));

const initialize = () => {
  resetForm(masterData);
  Object.assign(masterData, { cmpycd: authStore.cmpycd, deptcd: authStore.deptcd, ioym: today.replace(/-/g, '').substring(0, 6), iono: '', ioymd: today.replace(/-/g, ''), whcd: '200', iwhcd: '100' });
  grid?.clearData();
}

const formatDate = (v: any) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v;

onMounted(async () => {
  await fetchDeptOptions(); await fetchWhOptions();
  api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => {
    if (r.data?.length) { closingInfo.clsymd = r.data[0].clsymd; closingInfo.sclsym = r.data[0].sclsym; }
  })
  nextTick(initGrids);
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
.grid-container-right { border-bottom: 3px solid #005a9f !important; }
</style>
