<!--
	=============================================================
	프로그램명	: 수입전표발행 [디자인 표준 통합]
	작성일자	: 2025.02.24
	설명        : HSOD100U 디자인 패턴 이식 및 ASP 패턴 기반 순차 저장 로직 적용 (소문자 표준 통일)
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
        수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">수입전표발행 (HSIP140U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchUnissuedList">조회</button>
        <button class="btn-erp btn-save" @click="handleGenerateSlip">전표 발행</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 🔍 [상단] 조회 필터 영역 (HSOD100U 디자인 패턴) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" />
              <col style="width: 40%" />
              <col style="width: 10%" />
              <col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">발생부서</th>
                <td>
                  <div class="input-group input-group-sm w-75">
                    <input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">발생일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchForm.ioymdfr" v-model:todt="searchForm.ioymdto" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📊 3. 중앙: 발행 대상 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
          <span class="fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-list-check me-2 text-primary"></i> 전표 발행 대기 목록
          </span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

      <!-- 💰 4. 하단: 발행 정보 입력 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-secondary">
          <i class="bi bi-pencil-square me-1"></i> 전표 발행 설정
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
                <th class="bg-light text-center">대상합계</th>
                <td><input :value="formatNumber(targetTotalAmt)" class="form-control bg-light fw-bold text-primary" readonly /></td>
                <th class="bg-light text-center">부가세유형</th>
                <td>
                  <select v-model="slipForm.vattype" class="form-select">
                    <option v-for="opt in vatOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center">부가세금액</th>
                <td><input v-model.number="slipForm.vatamt" type="number" class="form-control text-end" /></td>
                <th class="required bg-light text-center">지불일자</th>
                <td><input v-model="slipForm.payymd" type="date" class="form-control" /></td>
              </tr>
              <tr>
                <th class="required bg-light text-center">지불계정</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="payment.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="payment.acctnm" type="text" class="form-control" placeholder="계정 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('ACCT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light text-center">지불금액</th>
                <td colspan="5"><input :value="formatNumber(payment.amt)" class="form-control bg-light text-end fw-bold text-danger" style="width: 200px;" readonly /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="bottom-spacer"></div>
    </div>
  </div>

  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
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

const searchForm = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  ioymdfr: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  ioymdto: new Date().toISOString().substring(0, 10)
})

const slipForm = reactive({ vattype: '10', vatamt: 0, payymd: new Date().toISOString().substring(0, 10) })
const payment = reactive({ acctcd: '', acctnm: '', amt: 0 })
const vatOptions = ref<any[]>([])
const targetTotalAmt = ref(0)

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchUnissuedList = async () => {
  try {
    const params = {
      actkind: 's0', cmpycd: authStore.cmpycd,
      deptcd: searchForm.deptcd,
      ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
      ioymdto: searchForm.ioymdto.replace(/-/g, '')
    }
    const res = await api.post('/api/hsip/HSIP_140U_STR', params)
    mainGrid?.setData(res.data || [])
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

/**
 * 🚀 전표 발행 (ASP 로직 완벽 이식 및 소문자 통일)
 */
const handleGenerateSlip = async () => {
  const selected = mainGrid?.getSelectedData() || []
  if (selected.length === 0) return vAlertError('발행할 항목을 선택하세요.')
  if (!payment.acctcd) return vAlertError('지불계정을 선택하세요.')

  const slipymd = slipForm.payymd.replace(/-/g, '')

  try {
    // 🚀 1. 마감 체크
    const rescl = await api.get('/api/comm/HP00_000S_STR', { params: { gubun: 'cl', cmpycd: authStore.cmpycd } })
    if (rescl.data?.length > 0) {
      const cl = rescl.data[0];
      if (slipymd <= (cl.clsymd || '')) throw new Error(`회계시스템이 마감되었습니다(${cl.clsymd}).`);
      if (slipymd.substring(0, 6) <= (cl.sclsym || '')) throw new Error(`수불이 마감되었습니다(${cl.sclsym}).`);
    }

    // 🚀 2. 자동 전표 설정 체크
    let autoslip = 'n'
    const resset = await api.post('/api/comm/HA00_010S_STR', { cmpycd: authStore.cmpycd, gbn: 'p1' })
    if (resset.data?.length > 0) autoslip = resset.data[0].slipyn || 'n'
    const acctymd = autoslip === 'y' ? slipymd : ''

    if (!confirm('선택한 항목들에 대해 전표를 발행하시겠습니까?')) return

    // 🚀 3. 전표 MASTER 생성
    const business = `${slipymd.substring(0, 4)}년 ${slipymd.substring(4, 6)}월 수입비용 미착품 대체건`
    const resmst = await api.post('/api/hasl/HASL_010U_STR', {
      actkind: 'a', cmpycd: authStore.cmpycd, slipymd: slipymd, slipno: '',
      acctymd: acctymd, deptcd: searchForm.deptcd, empnm: authStore.usernm,
      slipkind: '031', business: business, updemp: authStore.userid
    })
    const slipno = resmst.data?.[0]?.slipno
    if (!slipno || slipno === '000000') throw new Error('전표 마스터 생성 실패');

    // 🚀 4. 차변(Debit) 루프: 전표 상세 생성 및 수입 데이터 업데이트
    let kk = 0; let descnm = ''
    for (const item of selected) {
      const costnm = item.costnm; const fileno = item.fileno;
      const bigo = (item.bigo || costnm) + "(" + fileno + ")"
      if (kk === 0) descnm = costnm

      // 4-1. 전표 상세(차변) 생성
      await api.post('/api/hasl/HASL_011U_STR', {
        actkind: 'a', cmpycd: authStore.cmpycd, slipymd: slipymd, slipno: slipno,
        srowno: '', acctymd: acctymd, acctcd: '1365', sdeptcd: item.deptcd || searchForm.deptcd,
        custcd: '', fileno: fileno, costamt: item.costamt, bigo: bigo, updemp: authStore.userid
      })

      // 4-2. 수입 데이터 업데이트 (연결)
      await api.post('/api/hsip/HSIP_140U_STR', {
        actkind: 'u0', cmpycd: authStore.cmpycd, qdeptcd: searchForm.deptcd,
        ioymdfr: searchForm.ioymdfr.replace(/-/g, ''), ioymdto: searchForm.ioymdto.replace(/-/g, ''),
        fileno: fileno, docno: item.docno, crowno: item.rowno,
        slipymd: slipymd, slipno: slipno, updemp: authStore.userid
      })
      kk++
    }

    // 🚀 5. 부가세 전표 생성
    if (slipForm.vattype !== '000') {
      const vatdesc = descnm + (kk > 1 ? ` 외 ${kk - 1}건` : '') + ' 부가세'
      await api.post('/api/hasl/HASL_011U_STR', {
        actkind: 'a', cmpycd: authStore.cmpycd, slipymd: slipymd, slipno: slipno, srowno: '',
        acctymd: '', acctcd: '1275', deptcd: searchForm.deptcd, custcd: selected[0].custcd,
        fileno: selected[0].fileno, costamt: slipForm.vatamt, bigo: vatdesc,
        taxunit: '10', taxtype: slipForm.vattype, supyamt: targetTotalAmt.value, updemp: authStore.userid
      })
    }

    // 🚀 6. 대변(Credit) 처리
    await api.post('/api/hasl/HASL_011U_STR', {
      actkind: 'a', cmpycd: authStore.cmpycd, slipymd: slipymd, slipno: slipno, srowno: '',
      acctymd: acctymd, acctcd: payment.acctcd, deptcd: searchForm.deptcd,
      custcd: selected[0].custcd, costamt: payment.amt, bigo: business, updemp: authStore.userid
    })

    // 🚀 7. 자동 승인 처리
    if (autoslip === 'y' || autoslip === 'Y') {
      await api.post('/api/hasl/HASL_020U_STR', {
        actkind: 'a0', cmpycd: authStore.cmpycd, slipymd: slipymd, acctymd: acctymd,
        slipno: slipno, deptcd: searchForm.deptcd, slipkind: '031',
        slipyn: 'n', cofmyn: 'y', updemp: authStore.userid
      })
    }

    vAlert('정상적으로 전표가 발행되었습니다.')
    fetchUnissuedList(); initialize();
  } catch (e: any) {
    vAlertError(e.message || '전표 발행 실패')
  }
}

const formatNumber = (val: any) => Number(val || 0).toLocaleString()

const initialize = () => {
  resetForm(searchForm);
  Object.assign(slipForm, { vattype: '10', vatamt: 0, payymd: new Date().toISOString().substring(0, 10) });
  payment.acctcd = ''; payment.acctnm = ''; payment.amt = 0;
  mainGrid?.clearData(); targetTotalAmt.value = 0;
}

function openHelp(type: string) {
  if (type === 'DEPT') {
    openCommonHelp('DEPT', (d) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm })
  } else if (type === 'ACCT') {
    openCommonHelp('ACCT', (d) => { payment.acctcd = d.code; payment.acctnm = d.cdnm })
  }
}

watch(() => [targetTotalAmt.value, slipForm.vatamt], () => {
  payment.amt = targetTotalAmt.value + (Number(slipForm.vatamt) || 0)
})

onMounted(async () => {
  try {
    const resvat = await api.get('/api/comm/HS00_000S_STR', { params: { gubun: 'e2', cmpycd: authStore.cmpycd, gbncd: '021' } })
    vatOptions.value = resvat.data
  } catch (e) {}

  if (mainGridRef.value) {
    mainGrid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: true,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 60, hozAlign: "center" },
        { title: "PO No.", field: "fileno", width: 180, cssClass: "fw-bold text-primary" },
        { title: "비용종류", field: "costnm", width: 150 },
        { title: "발생일", field: "pubymd", width: 100, formatter: (c) => {
            const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
        }},
        { title: "상세 적요", field: "bigo", minWidth: 200, widthGrow: 1, hozAlign: "left" },
        { title: "비용(원화)", field: "costamt", hozAlign: "right", width: 130, formatter: "money", formatterParams: { precision: 0 } }
      ]
    })
    mainGrid.on('rowSelectionChanged', (data) => {
      targetTotalAmt.value = data.reduce((acc, cur: any) => acc + (Number(cur.costamt) || 0), 0)
    })
  }
  fetchUnissuedList()
})
</script>

<style scoped>
.main-content-wrapper { padding-bottom: 0vh !important; }
.grid-container-right { border-bottom: 3px solid #005a9f !important; }
.erp-table-dense th, .erp-table-dense td {
  height: 32px !important; padding: 0 8px !important; font-size: 12px; vertical-align: middle; border: 1px solid #dee2e6;
}
.erp-table-dense .form-control, .erp-table-dense .form-select, .erp-table-dense .btn {
  height: 26px !important; font-size: 12px !important; border-radius: 2px;
}
.erp-table-dense th { font-weight: 600; color: #495057; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
