<!--
	=============================================================
	프로그램명	: 매입전표발행 (HSIO130U)
	작성일자	: 2025.02.24
	설명        : 입고 정산 내역을 기반으로 회계 전표 발행 (ASP 패턴 기반 순차 저장 로직 적용 및 소문자 통일)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-post-fill me-2 text-primary" style="font-size: 18px;"></i>
        구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        매입정산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">매입전표발행 (HSIO130U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="save">전표발행</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 -->
    <div class="p-2 pb-0 flex-shrink-0 bg-light">
      <div class="card border shadow-sm overflow-hidden">
        <div class="card-body p-2 bg-white">
          <div class="d-flex align-items-center gap-4">
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-dark" style="min-width: 60px;">입고부서</span>
              <div class="input-group input-group-sm" style="width: 250px;">
                <input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                <input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" />
                <button class="btn btn-outline-secondary px-2" @click="openHelp('S_DEPT')"><i class="bi bi-search"></i></button>
              </div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="fw-bold small text-dark" style="min-width: 60px;">정산일자</span>
              <div class="d-flex align-items-center gap-1">
                <DateForm v-model:fromdt="searchForm.ioymdfr" v-model:todt="searchForm.ioymdto" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="flex-grow-1 d-flex flex-column overflow-hidden p-2 gap-2 bg-light">
      <!-- 🅰️ 전표 발행 설정 폼 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center">전표일자</th>
                <td><input v-model="formData.pubymd" type="date" class="form-control" /></td>
                <th class="required bg-light text-center">발행부서</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.deptcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
                    <input v-model="formData.deptnm" type="text" class="form-control" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required bg-light text-center">사업장/유형</th>
                <td>
                  <div class="d-flex gap-1">
                    <select v-model="formData.taxunit" class="form-select">
                      <option v-for="opt in saOptions" :key="opt.taxunit" :value="opt.taxunit">{{ opt.unitnm }}</option>
                    </select>
                    <select v-model="formData.vattype" class="form-select">
                      <option v-for="opt in vatOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                    </select>
                  </div>
                </td>
              </tr>
              <tr class="bg-light-subtle">
                <th class="bg-light text-center">카드결제</th>
                <td>
                  <div class="form-check form-switch m-0 d-flex align-items-center justify-content-center">
                    <input v-model="formData.cardyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="cardCheck">
                    <label class="form-check-label ms-2 small fw-bold" for="cardCheck">사용</label>
                  </div>
                </td>
                <th class="bg-light text-center">카드번호</th>
                <td colspan="3">
                  <div class="input-group input-group-sm">
                    <input v-model="formData.cardno" type="text" class="form-control" placeholder="카드번호 선택" :readonly="formData.cardyn !== 'Y'" />
                    <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CARD')" :disabled="formData.cardyn !== 'Y'"><i class="bi bi-search"></i></button>
                    <input v-model="formData.cardnm" type="text" class="form-control bg-light" readonly style="flex-grow: 2;" />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 정산 내역 그리드 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 전표 발행 대상 내역
          </span>
          <button class="btn btn-xs btn-outline-secondary px-2" style="height: 24px; font-size: 11px;" @click="toggleAllRows">전체선택/해제</button>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
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

const now = new Date()
const searchForm = reactive<any>({
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm,
  ioymdfr: new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10),
  ioymdto: now.toISOString().substring(0, 10)
})

const formData = reactive<any>({
  actkind: 'A',
  cmpycd: authStore.cmpycd,
  pubymd: now.toISOString().substring(0, 10),
  taxunit: '',
  vattype: '010',
  cardyn: 'N',
  cardno: '',
  cardnm: '',
  deptcd: authStore.deptcd,
  deptnm: authStore.deptnm
})

const saOptions = ref<any[]>([]);
const vatOptions = ref<any[]>([]);
const mainGridRef = ref<HTMLDivElement | null>(null);
let grid: Tabulator | null = null;

async function fetchOptions() {
  try {
    const [resSa, resVat] = await Promise.all([
      api.post('/api/comm/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd }),
      api.post('/api/comm/HA00_00P_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '120' })
    ])
    saOptions.value = resSa.data;
    vatOptions.value = resVat.data;
    if (saOptions.value.length > 0) formData.taxunit = saOptions.value[0].taxunit;
  } catch (e) {}
}

const handleOpenHelp = (type: string) => {
  if (type === 'S_DEPT') {
    openCommonHelp('DEPT', (d) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm; });
  } else if (type === 'DEPT') {
    openCommonHelp('DEPT', (d) => { formData.deptcd = d.deptcd; formData.deptnm = d.deptnm; });
  } else if (type === 'CARD') {
    openCommonHelp('EMP', (d) => { formData.cardno = d.userid; formData.cardnm = d.usernm; });
  }
}

async function fetchList() {
  try {
    const res = await api.post('/api/hsio/HSIO_130U_STR', {
      actkind: 'S0',
      cmpycd: authStore.cmpycd,
      iogbn: '100',
      ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
      ioymdto: searchForm.ioymdto.replace(/-/g, ''),
      deptcd: searchForm.deptcd
    });
    grid?.setData(res.data.map((i: any) => ({ ...i, procyn: false })));
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

/**
 * 🚀 저장 로직 (ASP 패턴 기반 순차 저장 - 소문자 통일)
 */
async function save() {
  const items = grid?.getData().filter((r: any) => r.procyn === true)
  if (!items || items.length === 0) return vAlertError('전표 발행할 항목을 선택하세요.')
  if (formData.cardyn === 'Y' && !formData.cardno) return vAlertError('카드번호를 입력하세요.')
  if (!confirm('선택한 항목들에 대해 매입전표를 발행하시겠습니까?')) return

  try {
    // 1. 자동전표 설정 체크 (ASP: HA00_010S_STR 'P1')
    const resset = await api.post('/api/comm/HA00_010S_STR', { cmpycd: authStore.cmpycd, gbn: 'p1' })
    const autoslip = resset.data?.[0]?.slipyn || 'n'
    const slipymd = formData.pubymd.replace(/-/g, '')
    const acctymd = (autoslip === 'y' || autoslip === 'Y') ? slipymd : ''
    const business = slipymd.substring(0, 4) + "년 " + slipymd.substring(4, 6) + "월 매입 건"

    // 2. 전표 MASTER 생성 (ASP: HASL_010U_STR)
    const resmst = await api.post('/api/hasl/HASL_010U_STR', {
        actkind: 'a',
        cmpycd: authStore.cmpycd,
        slipymd: slipymd,
        slipno: '',
        acctymd: acctymd,
        deptcd: formData.deptcd,
        empnm: authStore.usernm,
        slipkind: '030',
        business: business,
        updemp: authStore.userid
    })

    const slipno = resmst.data?.[0]?.slipno
    if (!slipno || slipno === '000000') throw new Error('전표 마스터 생성 실패')

    // 3. 정산 내역 루프 돌며 전표번호 업데이트 (ASP: HSIO_130U_STR)
    for (const item of items) {
        const detailparams = {
            actkind: 'u0',
            cmpycd: authStore.cmpycd,
            iogbn: '100',
            ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
            ioymdto: searchForm.ioymdto.replace(/-/g, ''),
            udeptcd: item.udeptcd || item.deptcd || formData.deptcd,
            jsanym: item.jsanym,
            jsanno: item.jsanno,
            jsanymd: (item.jsanymd || '').replace(/-/g, ''),
            spyamt: String(item.jsanamt || item.spyamt || '0').replace(/,/g, ''),
            vatamt: String(item.jsanvat || item.vatamt || '0').replace(/,/g, ''),
            custcd: item.custcd,
            taxunit: formData.taxunit,
            vattype: formData.vattype,
            slipymd: slipymd,
            slipno: slipno,
            cardyn: formData.cardyn,
            cardno: formData.cardno,
            updemp: authStore.userid
        }
        const resdetail = await api.post('/api/hsio/HSIO_130U_STR', detailparams)
        const resdata = resdetail.data?.[0]
        if (resdata && (resdata.status === 'y' || resdata.erryn === 'y' || resdata.status === 'Y' || resdata.erryn === 'Y')) {
            throw new Error(resdata.msg || '상세 내역 저장 중 오류 발생')
        }
    }

    vAlert('정상적으로 전표 발행이 완료되었습니다.')
    fetchList(); initialize();
  } catch (e: any) {
    vAlertError(e.message || '오류 발생')
  }
}

const toggleAllRows = () => {
  const rows = grid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().procyn === true)
  rows.forEach(r => r.update({ procyn: allSelected ? false : true }))
}

function initialize() {
  resetForm(formData);
  formData.pubymd = now.toISOString().substring(0, 10);
  formData.deptcd = authStore.deptcd; formData.deptnm = authStore.deptnm;
  grid?.clearData();
}

onMounted(async () => {
  await fetchOptions();
  if (mainGridRef.value) {
    grid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: '선택', field: 'procyn', hozAlign: 'center', width: 60, formatter: 'tickCross', editor: true },
        { title: '정산일', field: 'jsanymd', width: 110, hozAlign: 'center', formatter: (c) => {
            const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
        }},
        { title: '거래처', field: 'custnm', minWidth: 150, widthGrow: 1, hozAlign: 'left', cssClass: 'fw-bold' },
        { title: '품목명', field: 'itemnm', minWidth: 150, widthGrow: 1, hozAlign: 'left' },
        { title: '수량', field: 'jsanqty', width: 80, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '공급가', field: 'jsanamt', width: 110, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '부가세', field: 'jsanvat', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '합계', field: 'jsansum', width: 120, hozAlign: 'right', formatter: 'money', cssClass: 'bg-light fw-bold',
          mutatorData: (v, d) => Number(d.jsanamt || 0) + Number(d.jsanvat || 0)
        },
        { title: '사업장', field: 'unitnm', width: 120 },
        { title: '유형', field: 'vattypenm', width: 100 }
      ]
    })
  }
  nextTick(() => fetchList())
})
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
