<!--
	=============================================================
	프로그램명	: 통관등록 및 입고처리 (HSIP120U)
	작성일자	: 2025.02.24
	설명        : 수입 통관 마스터/상세 관리 (HSOD100U 디자인 표준 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-box-seam me-2 text-primary" style="font-size: 18px;"></i>
        수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        통관관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">통관등록 (HSIP120U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchPoList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
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
                <th class="text-center bg-light">조회기간</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchForm.fromdt" v-model:todt="searchForm.todt" />
                </td>
                <th class="text-center bg-light">관리부서</th>
                <td>
                  <div class="input-group input-group-sm w-75">
                    <input v-model="searchForm.deptcd" class="form-control bg-light text-center" style="max-width: 70px;" readonly />
                    <input v-model="searchForm.deptnm" class="form-control" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('S_DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 투-그리드 레이아웃 영역 -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 통관 대상 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">통관 대상 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column" style="min-height: 0;">
            <div ref="poGridRef" class="tabulator-full-height" />
          </div>
        </div>

        <!-- ➡️ 우측: 마스터 상세 폼 + 품목 그리드 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

          <!-- 상세 마스터 정보 폼 -->
          <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup>
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                  <col style="width: 110px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light text-center">파일번호</th>
                    <td><input v-model="formData.fileno" class="form-control bg-light fw-bold text-primary" readonly /></td>
                    <th class="required bg-light text-center">선적차수</th>
                    <td><input v-model="formData.shipseqnm" class="form-control bg-light text-center" readonly /></td>
                    <th class="required bg-light text-center">통관차수</th>
                    <td>
                      <select v-model="formData.passseq" class="form-select" @change="fetchDetail">
                        <option value="10">1차 통관</option><option value="20">2차 통관</option>
                        <option value="30">3차 통관</option><option value="40">4차 통관</option>
                        <option value="50">5차 통관</option>
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center">통관일자</th>
                    <td><input v-model="formData.passymd" type="date" class="form-control" /></td>
                    <th class="required bg-light text-center">세 관 명</th>
                    <td>
                      <select v-model="formData.taxorg" class="form-select">
                        <option value="">-- 선택 --</option>
                        <option v-for="opt in taxOrgOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center">면장번호</th>
                    <td><input v-model="formData.passno" class="form-control" /></td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center">입고창고</th>
                    <td>
                      <select v-model="formData.whcd" class="form-select">
                        <option value="">-- 선택 --</option>
                        <option v-for="opt in whOptions" :key="opt.whcd" :value="opt.whcd">{{ opt.whnm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light text-center">입고부서</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="formData.ideptcd" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                        <input v-model="formData.ideptnm" class="form-control" placeholder="부서 선택" readonly />
                        <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light text-center">적용환율</th>
                    <td><input v-model="formData.frgnrate" type="number" step="0.01" class="form-control text-end fw-bold text-primary" /></td>
                  </tr>
                  <tr>
                    <th class="bg-light text-center">거 래 처</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="formData.custcd" class="form-control bg-light text-center" style="max-width: 80px;" readonly />
                        <input v-model="formData.custnm" class="form-control bg-light" readonly />
                      </div>
                    </td>
                    <th class="bg-light text-center">선 적 일</th>
                    <td><input v-model="formData.shipymd" class="form-control bg-light text-center" readonly /></td>
                    <th class="bg-light text-center">도 착 일</th>
                    <td><input v-model="formData.arvymd" class="form-control bg-light text-center" readonly /></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 상세 그리드 영역 -->
          <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
              <span class="fw-bold small text-dark d-flex align-items-center">
                <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 통관 품목 명세
              </span>
              <div class="d-flex gap-1">
                <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
                <button class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold" @click="deleteSelectedRows" style="font-size: 11px;">- 행삭제</button>
              </div>
            </div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column" style="min-height: 0;">
              <div ref="itemGridRef" class="tabulator-full-height" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
// import { useCommonHelp } from '@/composables/useCommonHelp'
import { getDate, formatDateToInput } from '@/composables/useDate'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'

const authStore = useAuthStore()
const { firstDay, today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
// const { modalVisible, modalProps, openHelp: openCommonHelp } = useCommonHelp()

const searchForm = reactive({
  deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  fromdt: firstDay,
  todt: today
})

const formData = reactive<any>({
  fileno: '', shipseq: '', shipseqnm: '', passseq: '10',
  passymd: today,
  taxorg: '', passno: '', whcd: '', bigo: '',
  ideptcd: authStore.deptcd, ideptnm: authStore.deptnm,
  custcd: '', custnm: '', jsanyn: 'N',
  shipymd: '', arvymd: '', currnm: '', shipportnm: '', arvportnm: '', frgnrate: 0,
  ioym: '', iono: ''
})

const taxOrgOptions = ref<any[]>([]); const whOptions = ref<any[]>([])
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null

const fetchPoList = async () => {
  try {
    const res = await api.post('/api/hsip/HSIP_122U_STR', {
        cmpycd: authStore.cmpycd, deptcd: searchForm.deptcd,
        fromdt: (searchForm.fromdt || '').replace(/-/g, ''),
        todt: (searchForm.todt || '').replace(/-/g, '')
    })
    poGrid?.setData(res.data || [])
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

const fetchDetail = async () => {
  if (!formData.fileno || !formData.shipseq) return
  try {
    const res = await api.post('/api/hsip/HSIP_120U_STR', {
          fileno: formData.fileno, shipseq: formData.shipseq, passseq: formData.passseq,
          actkind: 'S0', cmpycd: authStore.cmpycd
    })
    if (res.data?.length) {
        const data = res.data[0]
        data.passymd = formatDateToInput(data.passymd)
        data.shipymd = formatDateToInput(data.shipymd)
        data.arvymd = formatDateToInput(data.arvymd)

        Object.assign(formData, data)
        formData.shipseqnm = `${formData.shipseq.substring(0,1)}차 선적`
        const resItems = await api.post('/api/hsip/HSIP_121U_STR', {
            actkind: 'S0', cmpycd: authStore.cmpycd,
            fileno: formData.fileno, shipseq: formData.shipseq, passseq: formData.passseq,
            gqty: 0, qty: 0, amt: 0
        })
        console.log(resItems.data)
        itemGrid?.setData(resItems.data || [])
    }
  } catch (e) { vAlertError('상세 조회 실패') }
}

const save = async () => {
  const selectedItems = itemGrid?.getSelectedData() || []
  if (selectedItems.length === 0) return vAlertError('통관 처리할 품목을 선택하세요.')

  if(!formData.fileno || !formData.passymd || !formData.whcd || !formData.passno) {
    return vAlertError('필수 입력 항목(PO번호, 통관일자, 입고창고, 면장번호)을 확인하세요.')
  }

  if (!confirm('통관입고 작업을 하시겠습니까?')) return

  try {
    const clean = (val: any) => String(val || '').trim()
    const passymd = (formData.passymd || '').replace(/-/g, '')
    const ITEMGRID_DATA = itemGrid?.getData() || []

    // 🔍 수량 및 모드 판별 전처리
    for (const item of selectedItems) {
      const totalQty = Number(item.iqty || 0) + Number(item.gqty || 0)
      const isNewItem = clean(item.iorowno) === ''

      // 신규 등록인 경우 수량이 0보다 커야 함
      if (isNewItem && totalQty <= 0) {
        vAlertError(`신규 품목(${item.itemnm})은 통관량 또는 감모량을 입력해야 합니다.`)
        return
      }
      // 수정인 경우 0 허용 (서비스 단에서 삭제 처리됨)
    }

    // IONO 존재 여부로 마스터 모드 판별
    const rowWithIono = ITEMGRID_DATA.find(d => clean(d.iono) !== '')
    const isUpdate = !!rowWithIono

    const payload = {
      mst: {
        ...formData,
        actkind: isUpdate ? 'U' : 'A',
        ioym: isUpdate ? clean(rowWithIono.ioym) : (clean(formData.ioym) || passymd.substring(0, 6)),
        iono: isUpdate ? clean(rowWithIono.iono) : '',
        passymd: passymd,
        iogbn: '100', iotype: '100', cfmyn: 'Y',
        deptcd: formData.ideptcd, updemp: authStore.userid
      },
      dtl: selectedItems.map(item => ({
        ...item,
        prowno: item.srowno // 🚀 srowno를 prowno에 담아서 전달
      }))
    }

    const res = await api.post('/api/hsip/HSIP_120U_SAVE', payload)
    if (res.status === 200) { // 🚀 HTTP 상태 코드로 성공 여부 확인 (인터셉터 영향 방지)
      vAlert('정상적으로 통관 입고 처리가 완료되었습니다.')
      fetchDetail(); fetchPoList();
    }
  } catch (e: any) {
    vAlertError('저장 오류: ' + (e.response?.data?.message || e.message))
  }
}

const addRow = () => itemGrid?.addRow({ sqty: 0, price: 0, iqty: 0, gqty: 0, amt: 0 }, true);
const deleteSelectedRows = () => itemGrid?.getSelectedRows().forEach(row => row.delete());

function initialize() {
    resetForm(formData);
    Object.assign(formData, {
      passseq: '10',
      passymd: today,
      ideptcd: authStore.deptcd,
      ideptnm: authStore.deptnm
    });
    itemGrid?.clearData(); poGrid?.deselectRow();
}

function openHelp(type: string) {
  if (type === 'S_DEPT' || type === 'DEPT') {
    openCommonHelp('DEPT', (d) => {
      if (type === 'S_DEPT') { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm }
      else { formData.ideptcd = d.deptcd; formData.ideptnm = d.deptnm }
    })
  }
}

onMounted(async () => {
  api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'E2', cmpycd: authStore.cmpycd, gbncd: '306' } }).then(r => taxOrgOptions.value = r.data)
  api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'W0', cmpycd: authStore.cmpycd } }).then(r => {
    whOptions.value = r.data; if (r.data?.length) formData.whcd = r.data[0].whcd;
  })

  nextTick(() => {
    if (poGridRef.value) {
      poGrid = new Tabulator(poGridRef.value, {
        layout: 'fitColumns', height: "100%", selectable: 1,
        columnDefaults: {
          headerSort: false,
          headerHozAlign: "center",
          hozAlign: "right",
          vertAlign: "middle"
        },
        columns: [
          { title: 'PO No.', field: 'fileno', cssClass: 'fw-bold text-primary', minWidth: 100 },
          { title: '선적', field: 'shipseq', width: 80, formatter: (c) => `${c.getValue()?.substring(0,1)}차` },
          { title: "수입처", field: "custnm", hozAlign: "left", minWidth: 100 }
        ]
      })
      poGrid.on('rowClick', (e, row) => {
        const d = row.getData(); formData.fileno = d.fileno; formData.shipseq = d.shipseq; fetchDetail()
      })
    }

    if (itemGridRef.value) {
      itemGrid = new Tabulator(itemGridRef.value, {
        layout: 'fitColumns', height: "100%", selectable: true,
        columnDefaults: {
          headerSort: false,
          headerHozAlign: "center",
          hozAlign: "right", // 🚀 기본값 우측 정렬
          vertAlign: "middle",
          minWidth: 50
        },
        columns: [
          { title: '선택', width: 50, hozAlign: 'center', formatter: 'rowSelection', titleFormatter: 'rowSelection' },
          { title: '품목명', field: 'itemnm', minWidth: 180, widthGrow: 1, cssClass: 'fw-bold text-primary', hozAlign: 'left' },
          { title: '규격', field: 'itsize', width: 120, hozAlign: 'left' },
          { title: '단위', field: 'unit', width: 60 },
          { title: '선적량', field: 'sqty', hozAlign: 'right', width: 90, formatter: 'money', formatterParams: { precision: 0 } },
          { title: '단가', field: 'price', hozAlign: 'right', width: 100, formatter: 'money', formatterParams: { precision: 0 } },
          { title: '잔량', field: 'jqty', hozAlign: 'right', width: 90, cssClass: 'text-danger fw-bold', formatter: 'money', formatterParams: { precision: 0 } },
          { title: '통관량', field: 'iqty', hozAlign: 'right', width: 90, editor: 'number', cssClass: 'bg-yellow fw-bold', formatter: 'money', formatterParams: { precision: 0 } },
          { title: '감모량', field: 'gqty', hozAlign: 'right', width: 90, editor: 'number', cssClass: 'bg-yellow fw-bold', formatter: 'money', formatterParams: { precision: 0 } },
          { title: '외화금액', field: 'amt', hozAlign: 'right', width: 110, formatter: 'money', formatterParams: { precision: 0 } },
          // 🚀 내부 데이터용 숨김 컬럼 (배열 하단 배치 및 제목 제거)
          { field: 'ioym', visible: false },
          { field: 'iono', visible: false },
          { field: 'iorowno', visible: false },
          { field: 'srowno', visible: false }
        ]
      });

      // 통관량 또는 감모량 변경 시 외화금액 자동 계산
      itemGrid.on('cellEdited', (cell) => {
        const field = cell.getField();
        if (field === 'iqty' || field === 'gqty') {
          const row = cell.getRow();
          const data = row.getData();
          const iqty = Number(data.iqty || 0);
          const price = Number(data.price || 0);
          const amt = iqty * price;
          row.update({ amt: amt });
        }
      });
    }
    fetchPoList();
  })
})
</script>

<style scoped>
.tabulator-full-height { width: 100% !important; background-color: #fff; border-bottom: 3px solid #005a9f !important; }
</style>