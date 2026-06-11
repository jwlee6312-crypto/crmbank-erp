<!--
	=============================================================
	프로그램명	: 수입비용입력 [디자인 표준 통합]
	작성일자	: 2025.02.24
	설명        : HSOD100U 디자인 패턴 이식 및 ASP 패턴 기반 순차 저장 로직 적용
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-cash-coin me-2 text-primary" style="font-size: 18px;"></i>
        수입관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">수입비용입력 (HSIP130U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchDetail">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button class="btn-erp btn-delete" @click="deleteData" :disabled="!formData.docno">삭제</button>
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
                    <input v-model="formData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="formData.deptnm" type="text" class="form-control" placeholder="부서 선택" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">PO No</th>
                <td>
                  <div class="input-group input-group-sm w-50">
                    <input v-model="formData.fileno" type="text" class="form-control fw-bold text-primary" placeholder="PO 번호" @keyup.enter="fetchDetail" />
                    <button class="btn btn-outline-secondary px-2" @click="openHelp('PO')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 상세 정보 마스터 폼 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center">비용번호</th>
                <td><input v-model="formData.docno" class="form-control bg-light fw-bold" readonly placeholder="자동부여" /></td>
                <th class="required bg-light text-center">발생일자</th>
                <td><input v-model="formData.pubymd" type="date" class="form-control" /></td>
                <th class="bg-light text-center">특기사항</th>
                <td><input v-model="formData.remark" class="form-control" placeholder="비용 관련 참고사항" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📊 3. 상세 품목 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark d-flex align-items-center">
            <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 수입 제비용 상세 내역
          </span>
          <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 12px;">+ 행추가</button>
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
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp: openCommonHelp } = useCommonHelp()

const formData = reactive<any>({
  cmpycd: authStore.cmpycd, fileno: '',
  pubymd: new Date().toISOString().substring(0, 10), remark: '',
  deptcd: authStore.deptcd, deptnm: authStore.deptnm, docno: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchDetail = async () => {
  if (!formData.fileno) return vAlertError('PO No를 입력하세요.')
  try {
    const res = await api.post('/api/hsip/HSIP_130U_STR', { fileno: formData.fileno, actkind: 'S0', cmpycd: authStore.cmpycd })
    if (res.data && res.data.length > 0) {
      const mst = res.data[0];
      Object.assign(formData, mst)
      // 필드 매핑: DB bigo -> UI remark
      formData.remark = mst.bigo || mst.BIGO || '';
      // 날짜 포맷팅 (YYYYMMDD -> YYYY-MM-DD)
      if (formData.pubymd && formData.pubymd.length === 8) {
        formData.pubymd = `${formData.pubymd.substring(0, 4)}-${formData.pubymd.substring(4, 6)}-${formData.pubymd.substring(6, 8)}`
      }

      const resItems = await api.post('/api/hsip/HSIP_131U_STR', { fileno: formData.fileno, docno: formData.docno, actkind: 'S0', cmpycd: authStore.cmpycd })
      mainGrid?.setData(resItems.data || [])
      vAlert('조회되었습니다.')
    } else {
      mainGrid?.setData([])
      vAlertError('조회 결과가 없습니다.')
    }
  } catch (e) { vAlertError('조회 실패') }
}

const save = async () => {
  if (!formData.fileno) return vAlertError('PO No를 입력하세요.')

  // 저장 대상 필터링: 변경사항이 있는 행(state 존재) 또는 신규 행(rowno 없음)
  const items = mainGrid?.getData().filter((r: any) => r.costcd && (r.state || !r.rowno)) || [];
  if (!items.length && !formData.docno) return vAlertError('저장할 항목이 없습니다.')

  if (!confirm('저장하시겠습니까?')) return

  try {
    // 🚀 1. 수입비용 MASTER 저장 (ASP 패턴: HSIP_130U_STR)
    // 마스터 액션코드: 신규 A0, 수정 U0
    const act = formData.docno ? 'U0' : 'A0'
    const masterParams = {
      actkind: act,
      cmpycd: authStore.cmpycd,
      fileno: formData.fileno,
      docno: formData.docno || '',
      deptcd: formData.deptcd,
      pubymd: formData.pubymd.replace(/-/g, ''),
      bigo: formData.remark || '', // UI remark -> DB bigo 매핑
      updemp: authStore.userid
    }

    const resMst = await api.post('/api/hsip/HSIP_130U_STR', masterParams)
    const mstData = resMst.data?.[0]

    // 에러 체크 ('000000' 이면 오류)
    if (mstData && (mstData.fileno === '000000' || mstData.FILENO === '000000')) {
       throw new Error(mstData.docno || mstData.DOCNO || '마스터 저장 오류')
    }

    // 채번된 번호 획득 (Rs(1))
    const keyDocno = mstData?.docno || mstData?.DOCNO || formData.docno

    // 🚀 2. 수입비용 상세 내역 루프 저장 (ASP 패턴: HSIP_131U_STR)
    if (items.length > 0) {
      for (const item of items) {
        let upkind = item.state;
        if (!upkind && !item.rowno) upkind = "C"; // 신규행 판별

        if (upkind) {
          // 사용자 규칙: 상세내역 A1, U1, D1 사용
          const detailAct = upkind === 'C' ? 'A1' : (upkind === 'D' ? 'D1' : 'U1');

          const detailParams = {
            actkind: detailAct,
            cmpycd: authStore.cmpycd,
            fileno: formData.fileno,
            docno: keyDocno,
            rowno: item.rowno || '', // ASP의 BROWNO 역할
            costcd: item.costcd,
            deptcd: formData.deptcd,
            shipseq: item.shipseq || '10',
            passseq: item.passseq || '10',
            pubymd: formData.pubymd.replace(/-/g, ''),
            costamt: String(item.costamt || '0').replace(/,/g, ''),
            currcd: item.currcd || formData.currcd || '',
            frgnrate: item.frgnrate || '0',
            frgnamt: item.frgnamt || '0',
            paycust: item.paycust || '',
            mgtno: item.mgtno || '',
            bigo: item.bigo || '',
            slipymd: item.slipymd || '',
            slipno: item.slipno || '',
            srowno: item.srowno || '',
            updemp: authStore.userid
          }

          const resDtl = await api.post('/api/hsip/HSIP_131U_STR', detailParams)
          const dtlData = resDtl.data?.[0]

          if (dtlData && (dtlData.fileno === '000000' || dtlData.FILENO === '000000')) {
             throw new Error(dtlData.docno || dtlData.DOCNO || '상세 내역 저장 중 오류 발생')
          }
        }
      }
    }

    vAlert('정상적으로 작업이 되었습니다.')
    formData.docno = keyDocno
    fetchDetail() // 저장 후 재조회

  } catch (e: any) {
    console.error('Save error:', e)
    vAlertError(e.message || '저장 중 오류가 발생했습니다.')
  }
}

const deleteData = async () => {
  if (!formData.docno) return
  if (!confirm('정말 삭제하시겠습니까?')) return
  try {
    await api.post('/api/hsip/HSIP_130U_STR', { ...formData, actkind: 'D0', cmpycd: authStore.cmpycd, updemp: authStore.userid })
    vAlert('삭제되었습니다.')
    initialize()
  } catch (e) { vAlertError('삭제 실패') }
}

const addRow = () => { mainGrid?.addRow({ costcd: '', costnm: '', costamt: 0, shipseq: '10', passseq: '10', bigo: '', state: 'C' }, true) }

const initialize = () => {
  resetForm(formData);
  Object.assign(formData, {
    cmpycd: authStore.cmpycd,
    pubymd: new Date().toISOString().substring(0, 10),
    deptcd: authStore.deptcd, deptnm: authStore.deptnm,
    docno: ''
  });
  mainGrid?.clearData();
}

function openHelp(type: string, cell?: any) {
  if (type === 'DEPT') {
    openCommonHelp('DEPT', (d) => { formData.deptcd = d.deptcd; formData.deptnm = d.deptnm })
  } else if (type === 'PO') {
    openCommonHelp('COMMON', (d) => { formData.fileno = d.fileno; fetchDetail() }, { gubun: 'F0', gbncd: '1' })
  } else if (type === 'COST') {
    openCommonHelp('COMMON', (d) => {
      cell.getRow().update({
        costcd: d.code,
        costnm: d.cdnm,
        state: cell.getRow().getData().rowno ? 'U' : 'C'
      })
    }, { gubun: 'C2', gbncd: 'IP' })
  }
}

onMounted(() => {
  if (mainGridRef.value) {
    mainGrid = new Tabulator(mainGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        { title: 'No', formatter: 'rownum', width: 60 },
        {
          title: '비용코드', field: 'costcd', width: 120,
          formatter: (cell) => {
            return `<div class='d-flex align-items-center justify-content-between w-100 px-2'>
                      <span>${cell.getValue() || ''}</span>
                      <i class='bi bi-search text-primary cursor-pointer'></i>
                    </div>`
          },
          cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) openHelp('COST', cell) }
        },
        { title: '비용 명칭', field: 'costnm', minWidth: 200, widthGrow: 1.5, cssClass: 'fw-bold', hozAlign: 'left' },
        { title: '비용(원화)', field: 'costamt', hozAlign: 'right', width: 150, editor: 'number', formatter: 'money', formatterParams: { precision: 0 },
          cellEdited: (c) => { if (c.getRow().getData().rowno) c.getRow().update({ state: 'U' }) }
        },
        {
          title: '선적차수', field: 'shipseq', width: 100,
          editor: "select",
          editorParams: { values: { "10": "1차", "20": "2차", "30": "3차", "40": "4차", "50": "5차" } },
          formatter: "lookup", formatterParams: { "10": "1차", "20": "2차", "30": "3차", "40": "4차", "50": "5차" },
          cellEdited: (c) => { if (c.getRow().getData().rowno) c.getRow().update({ state: 'U' }) }
        },
        {
          title: '통관차수', field: 'passseq', width: 100,
          editor: "select",
          editorParams: { values: { "10": "1차", "20": "2차", "30": "3차", "40": "4차", "50": "5차" } },
          formatter: "lookup", formatterParams: { "10": "1차", "20": "2차", "30": "3차", "40": "4차", "50": "5차" },
          cellEdited: (c) => { if (c.getRow().getData().rowno) c.getRow().update({ state: 'U' }) }
        },
        { title: '상세 적요', field: 'bigo', minWidth: 200, widthGrow: 1.5, editor: 'input', hozAlign: 'left',
          cellEdited: (c) => { if (c.getRow().getData().rowno) c.getRow().update({ state: 'U' }) }
        },
        {
          title: "삭제", width: 60,
          formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>",
          cellClick: (e, c) => {
             const row = c.getRow();
             const data = row.getData();
             if (data.rowno) {
                if (confirm('이 항목을 즉시 삭제하시겠습니까?')) {
                   api.post('/api/hsip/HSIP_131U_STR', { ...data, actkind: 'D1', cmpycd: authStore.cmpycd, updemp: authStore.userid }).then(() => {
                      vAlert('삭제되었습니다.'); row.delete();
                   });
                }
             } else {
                row.delete();
             }
          }
        }
      ]
    })
  }
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
:deep(.empty-row-style) { opacity: 0.6; }
</style>
