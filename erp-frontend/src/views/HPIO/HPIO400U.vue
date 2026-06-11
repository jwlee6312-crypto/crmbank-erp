<!--
	=============================================================
	프로그램명	: 제품입고(자가생산) (HPIO400U)
	작성일자	: 2025.02.24
	설명        : 생산 공정별 완료 항목의 입고 처리 관리 (HPIO210U 표준 패턴 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-arrow-in-down me-2 text-primary" style="font-size: 18px;"></i>
        생산정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">제품입고(자가생산) (HPIO400U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchProcesses">조회</button>
        <button class="btn-erp btn-save" @click="saveReceipt" :disabled="!selectedProg.progcd">입고저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 25%" />
                <col style="width: 10%" /><col style="width: 30%" />
                <col style="width: 10%" /><col style="width: 15%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light required">생산라인</th>
                <td>
                  <select v-model="searchForm.linecd" class="form-select form-select-sm" @change="onLineChange">
                    <option value="">라인 선택</option>
                    <option v-for="opt in lineOptions" :key="opt.linecd" :value="opt.linecd">
                      [{{ opt.linecd }}] {{ opt.linenm }}
                    </option>
                  </select>
                </td>
                <th class="text-center bg-light required">생산일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <input v-model="proymdfr_f" type="date" class="form-control form-control-sm" style="width: 140px;" />
                  <span class="px-1 opacity-50">~</span>
                  <input v-model="proymdto_f" type="date" class="form-control form-control-sm" style="width: 140px;" />
                </td>
                <th class="text-center bg-light required">입고일자</th>
                <td>
                  <input v-model="inymd_f" type="date" class="form-control form-control-sm" />
                </td>
              </tr>
              <tr>
                <th class="text-center bg-light required">입고창고</th>
                <td>
                  <select v-model="searchForm.whcd" class="form-select form-select-sm">
                    <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
                  </select>
                </td>
                <td colspan="4" class="text-muted small ps-3">
                  <i class="bi bi-info-circle me-1"></i> 생산 공정별 실적 완료 건에 대해 창고 입고 처리를 수행합니다.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 투-그리드 레이아웃 영역 -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 공정 목록 (grid1) -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 250px; min-width: 250px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark text-center">공정 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ➡️ 우측: 입고 대상 그리드 (grid2) -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark">
                <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>입고 대상 내역
                <span v-if="selectedProg.prognm" class="badge bg-primary-subtle text-primary border border-primary-subtle ms-2">{{ selectedProg.prognm }}</span>
              </span>
              <div class="small text-muted">※ 항목 선택 후 '입고저장'을 클릭하세요.</div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="tableRef2" class="tabulator-instance flex-grow-1"></div>
            </div>
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
import { getDate } from '@/composables/useDate'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { today, firstDay } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// [1] 데이터 모델링
const initymd = today.replace(/-/g, '')
const initfrymd = firstDay.replace(/-/g, '')

const searchForm = reactive({
  linecd: '010',
  proymdfr: initfrymd,
  proymdto: initymd,
  whcd: '200',
  inymd: initymd
})

const lineOptions = ref<any[]>([]); const whOptions = ref<any[]>([])
const selectedProg = reactive({ progcd: '', prognm: '' })
const closingInfo = reactive({ clsymd: '', sclsym: '' })

// 포맷팅 헬퍼 (ui 접두어 제거)
const proymdfr_f = computed({ get: () => formatDate(searchForm.proymdfr), set: (v) => { if(v) searchForm.proymdfr = v.replace(/-/g, '') } })
const proymdto_f = computed({ get: () => formatDate(searchForm.proymdto), set: (v) => { if(v) searchForm.proymdto = v.replace(/-/g, '') } })
const inymd_f = computed({ get: () => formatDate(searchForm.inymd), set: (v) => { if(v) searchForm.inymd = v.replace(/-/g, '') } })

const tableRef1 = ref<HTMLDivElement | null>(null)
const tableRef2 = ref<HTMLDivElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// [2] 그리드 초기화
const initGrids = () => {
  // Left: 공정 목록
  grid1 = new Tabulator(tableRef1.value!, {
    layout: "fitColumns", height: "100%", placeholder: "라인 선택", selectable: 1,
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
      { title: "공정명", field: "cdnm", hozAlign: "left", headerSort: false },
      { title: "코드", field: "code", hozAlign: "center", width: 80, cssClass: "fw-bold text-primary", headerSort: false }
    ],
  });
  grid1.on("rowClick", (e, row) => onProcessSelect(row.getData()));

  // Right: 입고 대상
  grid2 = new Tabulator(tableRef2.value!, {
    layout: "fitColumns", height: "100%", placeholder: "공정을 선택하세요.", selectable: true,
    columnDefaults: { headerHozAlign: 'center', headerSort: false, vertAlign: "middle" },
    columns: [
      { title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false },
      { title: "생산일자", field: "proymd", width: 110, hozAlign: "center", formatter: (c) => formatDate(c.getValue()) },
      { title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 1, cssClass: "fw-bold" },
      { title: "규격", field: "itsize", width: 150 },
      { title: "단위", field: "unit", width: 70, hozAlign: "center" },
      { title: "생산량", field: "prdqty", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미입고", field: "ninqty", width: 90, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-danger" },
      { title: "입고량", field: "inqty", width: 100, hozAlign: "right", editor: "number", cssClass: "bg-light-yellow fw-bold" },
      { title: "작업처", field: "work_info", width: 150, hozAlign: "left" }
    ]
  });
}

// [3] 비즈니스 로직
const fetchLineOptions = async () => {
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'L0', cmpycd: authStore.cmpycd, gbncd: 'Y', code: '' } })
    lineOptions.value = res.data.map((i: any) => ({ code: i.code || i.code, cdnm: i.cdnm }));
  } catch (e) {}
}

const fetchWhOptions = async () => {
  try {
    const res = await api.post('/api/hs00/HS00_000S_STR', { gubun: 'W0', cmpycd: authStore.cmpycd, gbncd: '', code: '', codenm: '', etcval: '' })
    whOptions.value = res.data.map((i: any) => ({ whcd: i.whcd, whnm: i.whnm }));
  } catch (e) {}
}

const fetchProcesses = async () => {
  if (!searchForm.linecd) { vAlertError('라인을 선택하세요.'); return; }
  try {
    const res = await api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'G2', cmpycd: authStore.cmpycd, gbncd: searchForm.linecd, code: '' } })
    grid1?.setData(res.data)
    grid2?.clearData(); selectedProg.progcd = ''; selectedProg.prognm = '';
  } catch (e) {}
}

const onProcessSelect = (prog: any) => {
  selectedProg.progcd = prog.code; selectedProg.prognm = prog.cdnm
  fetchGridData()
}

const fetchGridData = async () => {
  try {
    const res = await api.post('/api/hpio/HPIO_400U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, linecd: searchForm.linecd, progcd: selectedProg.progcd, proymdFR: searchForm.proymdfr, proymdTO: searchForm.proymdto
    })
    const mapped = res.data.map((item: any) => ({
      ...item,
      ninqty: Number(item.prdqty || 0) - Number(item.inqty || 0),
      work_info: item.prodcd === '100' ? item.worknm : item.custnm
    }))
    grid2?.setData(mapped)
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const saveReceipt = async () => {
  const selected = grid2?.getSelectedData() || []
  if (selected.length === 0) return vAlertError('입고 처리할 항목을 선택하세요.')

  const inYmd = searchForm.inymd.replace(/-/g, '')
  if (inYmd <= closingInfo.clsymd) return vAlertError('회계 마감된 일자입니다.')
  if (inYmd.substring(0, 6) <= closingInfo.sclsym) return vAlertError('영업 마감된 월입니다.')

  if (!confirm('선택한 내역을 입고 처리하시겠습니까?')) return

  try {
    const masterRes = await api.post('/api/hsio/HSIO_060U_STR', {
      actkind: 'A0', cmpycd: authStore.cmpycd, iogbn: '100', proymdFR: searchForm.proymdfr, proymdTO: searchForm.proymdto,
      deptcd: '21000', whcd: searchForm.whcd, Jinymd: inYmd, remark: `생산입고-${searchForm.inymd}`, cfmyn: 'Y', userid: authStore.userid
    })

    const ioYm = masterRes.data[0].ioym; const ioNo = masterRes.data[0].iono

    for (const item of selected) {
      const detailRes = await api.post('/api/hsio/HSIO_061U_STR', {
        actkind: 'A0', cmpycd: authStore.cmpycd, iogbn: '100', ioym: ioYm, iono: ioNo, deptcd: '21000', whcd: searchForm.whcd,
        Jinymd: inYmd, itemcd: item.itemcd, unit: item.unit, itsize: item.itsize, inqty: item.inqty, userid: authStore.userid, ordym: item.ordym, ordno: item.ordno
      })
      const ioRowNo = detailRes.data[0].iorowno

      await api.post('/api/hpio/HPIO_400U_STR', {
        actkind: 'A0', cmpycd: authStore.cmpycd, linecd: searchForm.linecd, progcd: selectedProg.progcd,
        proymd: item.proymd, Jinymd: inYmd, ordymd: item.ordymd, deptcd: '21000', whcd: searchForm.whcd,
        itemcd: item.itemcd, inqty: item.inqty, ioym: ioYm, iono: ioNo, iorowno: ioRowNo, ordym: item.ordym, ordno: item.ordno, PRODCD: item.prodcd
      })
    }
    vAlert('정상적으로 입고 처리되었습니다.'); fetchGridData()
  } catch (e) { vAlertError('입고 처리 중 오류 발생') }
}

const onLineChange = () => fetchProcesses()
const initialize = () => {
  resetForm(searchForm);
  Object.assign(searchForm, { linecd: '010', proymdfr: initfrymd, proymdto: initymd, whcd: '200', inymd: initymd });
  grid1?.clearData(); grid2?.clearData();
  selectedProg.progcd = ''; selectedProg.prognm = '';
  fetchProcesses();
}

const formatDate = (v: any) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v;

onMounted(async () => {
  api.get('/api/hp00/HP00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => {
    if (r.data?.length) {
      closingInfo.clsymd = r.data[0].clsymd
      closingInfo.sclsym = r.data[0].sclsym
    }
  })
  fetchLineOptions(); fetchWhOptions(); fetchProcesses();
  nextTick(initGrids);
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
.grid-container-left, .grid-container-right { border-bottom: 3px solid #005a9f !important; }
</style>
