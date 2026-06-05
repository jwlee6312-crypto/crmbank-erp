<!--
	=============================================================
	프로그램명	: 입고정산취소 (HSIO120U)
	작성일자	: 2025.02.24
	설명        : 완료된 매입 정산 내역을 선택하여 취소 처리 (ASP 패턴 기반 순차 저장 로직 및 소문자 통일)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-arrow-counterclockwise me-2 text-danger" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">입고정산취소 (HSIO120U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchCustList">조회</button>
        <button class="btn-erp btn-save" @click="save">정산취소</button>
      </div>
    </div>

    <!-- 🔍 2. 최상단 검색 필터 영역 -->
    <div class="p-2 pb-0 flex-shrink-0 bg-light">
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" /><col style="width: 40%" />
              <col style="width: 10%" /><col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">입고부서</th>
                <td>
                  <div class="input-group input-group-sm w-75">
                    <input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT_search')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">정산일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchForm.ioymdfr" v-model:todt="searchForm.ioymdto" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 📊 3. 메인 작업 영역 -->
    <div class="d-flex flex-row flex-grow-1 overflow-hidden p-2 gap-2 bg-light">
      <!-- 3-1. 좌측: 정산 거래처 목록 -->
      <div class="card border shadow-sm d-flex flex-column" style="width: 300px; min-width: 300px;">
        <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">매입 거래처</div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="poGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

      <!-- 3-2. 우측: 정산 상세 및 취소 명세 -->
      <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
            <span class="fw-bold small text-dark d-flex align-items-center">
              <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 취소 대상 정산 명세
            </span>
            <button class="btn btn-xs btn-outline-secondary px-2" style="height: 22px; font-size: 11px;" @click="toggleAllRows">전체선택</button>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="itemGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
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

// [1] 데이터 모델링 (소문자 통일)
const searchForm = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  ioymdfr: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  ioymdto: new Date().toISOString().substring(0, 10)
})

const formData = reactive<any>({ custcd: '', custnm: '' })
const clsInfo = reactive({ sclsym: '', pclsym: '', wclsym: '' })
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null

const loadClsInfo = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } });
    if (res.data?.length) {
        const d = res.data[0];
        Object.assign(clsInfo, { pclsym: d.pclsym, sclsym: d.sclsym, wclsym: d.wclsym });
    }
  } catch (e) {}
}

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_120U_STR', {
      actkind: 'S1', cmpycd: authStore.cmpycd, iogbn: '100',
      ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
      ioymdto: searchForm.ioymdto.replace(/-/g, ''),
      deptcd: searchForm.deptcd
    });
    poGrid?.setData(res.data || []); itemGrid?.clearData();
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('거래처 조회 실패') }
}

async function fetchDetail(cust: any) {
  formData.custcd = cust.custcd; formData.custnm = cust.custnm;
  try {
    const res = await api.post('/api/hsio/HSIO_120U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, iogbn: '100',
      ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
      ioymdto: searchForm.ymdto.replace(/-/g, ''),
      custcd: cust.custcd, deptcd: searchForm.deptcd
    })
    itemGrid?.setData(res.data || [])
  } catch (e) { vAlertError('상세 조회 실패') }
}

/**
 * 🚀 정산 취소 저장 로직 (ASP 패턴: 선택 항목별 d0 순차 호출 및 소문자 통일)
 */
async function save() {
  const items = itemGrid?.getData().filter((r: any) => r.procyn === 'y' || r.procyn === 'Y') || []
  if (items.length === 0) return vAlertError('취소할 항목을 선택하세요.')

  if (!confirm('정산자료를 취소하시겠습니까?')) return

  try {
    const ioymdfr = searchForm.ioymdfr.replace(/-/g, '')
    const ioymdto = searchForm.ioymdto.replace(/-/g, '')

    for (const item of items) {
        const params = {
            actkind: 'd0',
            cmpycd: authStore.cmpycd,
            iogbn: '100',
            ioymdfr: ioymdfr,
            ioymdto: ioymdto,
            custcd: formData.custcd,
            deptcd: searchForm.deptcd,
            jsanym: item.jsanym,
            jsanno: item.jsanno,
            updemp: authStore.userid
        }
        const res = await api.post('/api/hsio/HSIO_120U_STR', params)
        const resData = res.data?.[0]

        // 에러 체크: jsanym이 '000000'이면 에러 메시지 반환
        if (resData && (resData.jsanym === '000000' || resData.JSANYM === '000000')) {
            throw new Error(resData.jsanno || resData.JSANNO || '취소 중 업무 오류가 발생했습니다.')
        }
    }
    vAlert('정산 취소가 완료되었습니다.');
    fetchCustList(); initialize();
  } catch (e: any) { vAlertError(e.message || '취소 처리 실패') }
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().procyn === 'y' || r.getData().procyn === 'Y')
  rows.forEach(r => r.update({ procyn: allSelected ? 'n' : 'y' }))
}

function initialize() {
  resetForm(searchForm); searchForm.deptcd = authStore.deptcd; searchForm.deptnm = authStore.deptnm;
  poGrid?.clearData(); itemGrid?.clearData();
}

function openHelp(type: string) {
  if (type === 'DEPT_search') {
    openCommonHelp('DEPT', (d) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm });
  }
}

onMounted(async () => {
  await loadClsInfo();

  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [{ title: '매입 거래처명', field: 'custnm', cssClass: 'fw-bold text-dark', hozAlign: 'left' }]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row.getData()))
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        { title: '선택', field: 'procyn', hozAlign: 'center', width: 60, formatter: 'tickCross', editor: true },
        { title: '정산일', field: 'jsanymd', width: 110, formatter: (c) => formatDate(c.getValue()) },
        { title: '사업장', field: 'unitnm', width: 120, hozAlign: 'left' },
        { title: '유형', field: 'vattypenm', width: 100 },
        { title: '공급가', field: 'spyamt', hozAlign: 'right', width: 120, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '부가세', field: 'vatamt', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 0 } },
        { title: '합계', field: 'jsansum', hozAlign: 'right', width: 130, formatter: 'money', cssClass: 'fw-bold text-primary', formatterParams: { precision: 0 },
          mutatorData: (v, d) => Number(d.spyamt || 0) + Number(d.vatamt || 0)
        }
      ]
    })
    itemGrid.on("cellEdited", (cell) => {
        if (cell.getField() === 'procyn') {
            const d = cell.getData();
            if (d.procyn === 'y' || d.procyn === 'Y') {
                if (d.slipymd && d.slipymd > '00000000') { vAlertError('전표 발행된 자료입니다.'); cell.setValue('n'); return; }
                const ioym = d.jsanymd?.replace(/-/g, '').substring(0, 6) || '';
                if (clsInfo.sclsym && ioym <= clsInfo.sclsym) { vAlertError('영업 마감된 월입니다.'); cell.setValue('n'); return; }
            }
        }
    })
  }
  fetchCustList()
})

const formatDate = (val: any) => val && val.length === 8 ? `${val.substring(0,4)}-${val.substring(4,6)}-${val.substring(6,8)}` : val;
</script>

<style scoped>
.main-content-wrapper { padding-bottom: 0px !important; }
.grid-container-right { border-bottom: 3px solid #005a9f !important; }
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
