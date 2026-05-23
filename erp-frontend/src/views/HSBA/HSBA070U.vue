<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 💡 Haion ERP 표준 레이아웃: 하단 공백 없이 꽉 채우는 구조 -->
  <div class="hsba070u-wrapper d-flex flex-column h-100 bg-white p-0 overflow-hidden text-start">
    <!-- 🚀 1. 상단 액션 바: 고정 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-briefcase-fill me-2 text-primary"></i>
        기본정보 > <span class="text-primary fw-bolder">거래처등록 (HSBA070U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역: flex-grow-1 및 overflow-hidden으로 레이아웃 고정 -->
    <div class="flex-grow-1 d-flex flex-column gap-2 p-2 overflow-hidden">
      <!-- 🅰️ 조회 조건 영역: 고정 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-1 bg-light-subtle">
          <div class="d-flex align-items-center gap-3">
            <div class="input-group input-group-sm flex-nowrap" style="width: 180px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">종류</span>
              <select v-model="searchParams.QCUSTGBN" class="form-select border-0 bg-white" @change="search">
                <option value="000">전체</option>
                <option v-for="opt in options.custGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
              </select>
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width: 180px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">상태</span>
              <select v-model="searchParams.QSTATUS" class="form-select border-0 bg-white" @change="search">
                <option v-for="opt in options.status" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
              </select>
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width: 300px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">거래처명</span>
              <input v-model="searchParams.QCUSTNM" type="text" class="form-control border-0 bg-white" placeholder="거래처명 입력..." @keyup.enter="search" />
              <button class="btn btn-dark btn-sm" @click="search"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 🅱️ 상세 입력 폼 영역: 고정 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f8f9fa;">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1"></i> 거래처 상세 정보</span>
          <div class="d-flex gap-2">
            <span v-if="masterData.ACTKIND === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 모드</span>
            <span v-else class="badge bg-primary" style="font-size: 10px;">신규 등록</span>
          </div>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">거래처코드</th>
                <td><input v-model="masterData.CUSTCD" type="text" class="form-control form-control-sm text-center fw-bold text-primary" maxlength="7" :readonly="masterData.ACTKIND === 'U0'" /></td>
                <th>외부코드</th>
                <td><input v-model="masterData.OUTCUSTCD" type="text" class="form-control form-control-sm" maxlength="7" /></td>
                <th class="required">상&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;호</th>
                <td><input v-model="masterData.CUSTNM" type="text" class="form-control form-control-sm fw-bold border-primary-subtle" maxlength="50" /></td>
                <th>사업자번호</th>
                <td><input v-model="masterData.CUSTNO" type="text" class="form-control form-control-sm" maxlength="13" placeholder="- 없이" /></td>
                <th>법인번호</th>
                <td><input v-model="masterData.LEGALNO" type="text" class="form-control form-control-sm" maxlength="14" placeholder="- 없이" /></td>
              </tr>
              <tr>
                <th>종&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;류</th>
                <td>
                  <select v-model="masterData.CUSTGBN" class="form-select form-select-sm" @change="handleCustGbnChange">
                    <option v-for="opt in options.custGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>대&nbsp;&nbsp;표&nbsp;&nbsp;자</th>
                <td><input v-model="masterData.BOSSNM" type="text" class="form-control form-control-sm" maxlength="30" /></td>
                <th>업&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;태</th>
                <td><input v-model="masterData.CUSTTYPE" type="text" class="form-control form-control-sm" maxlength="50" /></td>
                <th>종&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;목</th>
                <td><input v-model="masterData.CUSTKIND" type="text" class="form-control form-control-sm" maxlength="50" /></td>
                <th>연&nbsp;락&nbsp;처</th>
                <td><input v-model="masterData.TELNO" type="text" class="form-control form-control-sm" maxlength="30" /></td>
              </tr>
              <tr>
                <th>주&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;소</th>
                <td colspan="7">
                  <div class="d-flex gap-1">
                    <input v-model="masterData.POSTNO" type="text" class="form-control form-control-sm text-center" style="width: 70px;" readonly />
                    <button class="btn btn-outline-secondary btn-sm px-2" @click="openPostcode"><i class="bi bi-search"></i></button>
                    <input v-model="masterData.ADDRESS" type="text" class="form-control form-control-sm flex-grow-1" />
                  </div>
                </td>
                <th>FAX번호</th>
                <td><input v-model="masterData.FAXNO" type="text" class="form-control form-control-sm" maxlength="30" /></td>
              </tr>
              <tr>
                <th>매입단가</th>
                <td>
                  <select v-model="masterData.INPRCGBN" class="form-select form-select-sm">
                    <option value="000">선택</option>
                    <option v-for="opt in options.inPrcGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>매출단가</th>
                <td>
                  <select v-model="masterData.OUTPRCGBN" class="form-select form-select-sm">
                    <option value="000">선택</option>
                    <option v-for="opt in options.outPrcGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>여신한도액</th>
                <td><input v-model="masterData.HDAMT" type="number" class="form-control form-control-sm text-end" /></td>
                <th>여신기한</th>
                <td>
                  <div class="d-flex gap-1 align-items-center">
                    <input v-model="masterData.RCVDD" type="number" class="form-control form-control-sm text-end" style="width: 40px;" />
                    <span class="small">일</span>
                    <select v-model="masterData.GIGBCD" class="form-select form-select-sm flex-grow-1">
                      <option v-for="opt in options.gigGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                    </select>
                  </div>
                </td>
                <th>대&nbsp;분&nbsp;류</th>
                <td>
                  <select v-model="masterData.AGRPCD" class="form-select form-select-sm">
                    <option value="000">선택</option>
                    <option v-for="opt in options.agrp" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>중&nbsp;분&nbsp;류</th>
                <td>
                  <select v-model="masterData.BGRPCD" class="form-select form-select-sm">
                    <option value="000">선택</option>
                    <option v-for="opt in options.bgrp" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>소&nbsp;분&nbsp;류</th>
                <td>
                  <select v-model="masterData.CGRPCD" class="form-select form-select-sm">
                    <option value="000">선택</option>
                    <option v-for="opt in options.cgrp" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>담&nbsp;당&nbsp;자</th>
                <td><input v-model="masterData.CDAMDANG" type="text" class="form-control form-control-sm" maxlength="30" /></td>
                <th>연락처(SMS)</th>
                <td><input v-model="masterData.CTELNO" type="text" class="form-control form-control-sm" maxlength="30" /></td>
                <th>전자메일</th>
                <td><input v-model="masterData.CEMAIL" type="text" class="form-control form-control-sm" maxlength="50" /></td>
              </tr>
              <tr>
                <th>신용등급</th>
                <td>
                  <select v-model="masterData.SINGRD" class="form-select form-select-sm">
                    <option value="A">A등급</option><option value="B">B등급</option><option value="C">C등급</option>
                  </select>
                </td>
                <th>적용일자</th>
                <td><input v-model="uiStdYmd" type="date" class="form-control form-control-sm" /></td>
                <th>전자여부</th>
                <td>
                  <div class="form-check form-switch m-0"><input v-model="masterData.ELCYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N"> <span class="small fw-bold">전자계산서</span></div>
                </td>
                <th>상&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;태</th>
                <td>
                  <select v-model="masterData.STATUS" class="form-select form-select-sm">
                    <option v-for="opt in options.status" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>사용여부</th>
                <td>
                  <div class="form-check form-switch m-0"><input v-model="masterData.USEYN" class="form-check-input" type="checkbox" true-value="Y" false-value="N"> <span class="small fw-bold">사용</span></div>
                </td>
              </tr>
              <tr>
                <th>입금은행</th>
                <td><input v-model="masterData.BANKNM" type="text" class="form-control form-control-sm" maxlength="30" /></td>
                <th>입금통장</th>
                <td><input v-model="masterData.GUJOA" type="text" class="form-control form-control-sm" maxlength="30" /></td>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td colspan="5"><input v-model="masterData.REMARK" type="text" class="form-control form-control-sm w-100" maxlength="50" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 거래처 목록 그리드: flex-grow-1로 하단 공백 없이 채움 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 거래처 목록 ({{ activeItemCount }} 건)</span>
          <span class="text-muted" style="font-size: 11px;">※ 행 클릭 시 상세 정보가 로드됩니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden" style="min-height: 0; position: relative;">
          <div ref="gridElement" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;"></div>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 1. 상태 관리
const searchParams = reactive({ QCUSTGBN: '000', QSTATUS: '010', QCUSTNM: '' })
const masterData = reactive<any>({
  ACTKIND: 'I0', CMPYCD: authStore.CMPYCD, CUSTCD: '', CUSTNO: '', CUSTNM: '', CUSTGBN: '010',
  BOSSNM: '', LEGALNO: '', CUSTKIND: '', CUSTTYPE: '', TELNO: '', FAXNO: '', POSTNO: '', ADDRESS: '',
  IOGBN: '010', INPRCGBN: '190', OUTPRCGBN: '200', HDAMT: 0, RCVDD: 0, GIGBCD: '305',
  AGRPCD: '000', BGRPCD: '000', CGRPCD: '000', AREA: '000', CDAMDANG: '', CTELNO: '', CEMAIL: '',
  REMARK: '', STATUS: '010', BANKNM: '', GUJOA: '', FNDYMD: '', STDYMD: '', CLSYMD: '99991231',
  SINGRD: 'A', ELCYN: 'Y', USEYN: 'Y', OUTCUSTCD: '', AGENTYN: 'N', UPDEMP: authStore.USERID
})

const uiStdYmd = computed({
  get: () => masterData.STDYMD ? `${masterData.STDYMD.substring(0, 4)}-${masterData.STDYMD.substring(4, 6)}-${masterData.STDYMD.substring(6, 8)}` : '',
  set: (v) => masterData.STDYMD = v.replace(/-/g, '')
})

const options = reactive<any>({ custGbn: [], status: [], inPrcGbn: [], outPrcGbn: [], gigGbn: [], agrp: [], bgrp: [], cgrp: [], area: [], ioGbn: [] })
const gridElement = ref<HTMLElement | null>(null); const grid = ref<Tabulator | null>(null); const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 10,
    columnDefaults: { headerSort: false, headerHozAlign: "center" },
    columns: [
      { title: "코드", field: "CUSTCD", width: 90, hozAlign: "center", cssClass: "fw-bold text-primary" },
      { title: "거래처상호", field: "CUSTNM", minWidth: 250, hozAlign: "left" },
      { title: "사업자번호", field: "CUSTNO", width: 130, hozAlign: "center" },
      { title: "대표자", field: "BOSSNM", width: 100 },
      { title: "연락처", field: "TELNO", width: 130, hozAlign: "left" },
      { title: "상태", field: "STATUSNM", width: 80 },
      { title: "외부CD", field: "OUTCUSTCD", width: 90 },
      { title: "사용", field: "USEYN", width: 60, hozAlign: "center", formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
    ]
  })
  grid.value.on("rowClick", (e, row) => {
    const data = row.getData()
    const cleaned: any = {}
    Object.keys(data).forEach(k => cleaned[k] = typeof data[k] === 'string' ? data[k].trim() : data[k])
    Object.assign(masterData, cleaned); masterData.ACTKIND = 'U0'
  })
}

// 3. 기능 구현
async function fetchOptions() {
  const getHS = (gbn: string, cd: string) => api.post('/api/hs00/HS00_000S_STR', { GUBUN: gbn, GBNCD: cd, CMPYCD: authStore.CMPYCD })
  const getHA = (gbn: string, cd: string) => api.post('/api/ha00/HA00_00P_STR', { GUBUN: gbn, GBNCD: cd, CMPYCD: authStore.CMPYCD })

  try {
    const [o1, o2, o3, o4, o5, o6, o7, o8, o9] = await Promise.all([
      getHA('E0', '270'), getHA('E0', '280'), getHS('E0', '190'), getHS('E0', '200'),
      getHS('E0', '305'), getHS('E0', '400'), getHS('E0', '410'), getHS('E0', '420'),
      getHA('E0', '340')
    ])
    const map = (r: any) => r.data.map((i: any) => ({ CODECD: String(i.CODE || i.CODECD || '').trim(), CODENM: String(i.CDNM || i.CODENM || '').trim() }))
    options.custGbn = map(o1); options.status = map(o2); options.inPrcGbn = map(o3); options.outPrcGbn = map(o4)
    options.gigGbn = map(o5); options.agrp = map(o6); options.bgrp = map(o7); options.cgrp = map(o8); options.ioGbn = map(o9)
  } catch (e) {}
}

async function search() {
  try {
    const res = await api.post('/api/hsba/HSBA_070U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, CUSTGBN: searchParams.QCUSTGBN === '000' ? '000' : searchParams.QCUSTGBN,
      STATUS: searchParams.QSTATUS, CUSTNM: searchParams.QCUSTNM, UPDEMP: authStore.USERID
    })
    grid.value?.setData(res.data); activeItemCount.value = res.data.length
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.CUSTNM) return vAlertError('상호는 필수입니다.')
  if (masterData.CUSTGBN === '010' && !masterData.CUSTNO) return vAlertError('사업자번호를 입력하세요.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const res = await api.post('/api/hsba/HSBA_070U_STR', { ...masterData, ACTKIND: masterData.ACTKIND === 'U0' ? 'U0' : 'I0' })
    if (res.data?.[0]?.RESULT === 'Y') { vAlertError(res.data[0].MSG || '저장 실패') }
    else { vAlert('정상 처리되었습니다.'); search(); initialize() }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(masterData)
  Object.assign(masterData, {
    ACTKIND: 'A0', CMPYCD: authStore.CMPYCD, CUSTGBN: '010', IOGBN: '010', INPRCGBN: '190', OUTPRCGBN: '200',
    GIGBCD: '305', STDYMD: new Date().toISOString().substring(0, 10).replace(/-/g, ''), CLSYMD: '99991231',
    SINGRD: 'A', ELCYN: 'Y', USEYN: 'Y', AGENTYN: 'N', UPDEMP: authStore.USERID, OUTCUSTCD: '', AREA: '000'
  })
}

const handleCustGbnChange = () => { if(masterData.CUSTGBN === '030') masterData.ELCYN = 'N'; }

function openPostcode() {
  // @ts-ignore
  new daum.Postcode({ oncomplete: (data: any) => { masterData.POSTNO = data.zonecode; masterData.ADDRESS = data.roadAddress; } }).open()
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
  let config: any = {}
  if (type === 'AGRP') config = { title: '대분류 선택', path: '/api/hs00/HS00_000S_STR', data: { GUBUN: 'G0' }, field: 'AGRPNM', columns: [{ title: '코드', field: 'AGRPCD', width: 80 }, { title: '분류명', field: 'AGRPNM', width: 180 }] }
  else if (type === 'BGRP') config = { title: '중분류 선택', path: '/api/hs00/HS00_000S_STR', data: { GUBUN: 'G1', CODE: masterData.AGRPCD }, field: 'BGRPNM', columns: [{ title: '코드', field: 'BGRPCD', width: 80 }, { title: '분류명', field: 'BGRPNM', width: 180 }] }
  else if (type === 'CUST') config = { title: '거래처 선택', path: '/api/ha00/HA00_00P_STR', data: { GUBUN: 'C4' }, field: 'CUSTNM', columns: [{ title: '코드', field: 'CUSTCD', width: 100 }, { title: '거래처명', field: 'CUSTNM', width: 200 }] }
  if (!config.path) return
  Object.assign(modalProps, { title: config.title, path: config.path, defaultField: config.field, data: { ...config.data, CMPYCD: authStore.CMPYCD }, columns: config.columns, onConfirm: (data: any) => {
    if (type === 'AGRP') { masterData.AGRPCD = (data.AGRPCD || data.CODE || '').trim(); masterData.AGRPNM = (data.AGRPNM || data.CDNM || '').trim(); }
    else if (type === 'BGRP') { masterData.BGRPCD = (data.BGRPCD || data.CODE || '').trim(); masterData.BGRPNM = (data.BGRPNM || data.CDNM || '').trim(); }
    else if (type === 'CUST') { masterData.IN_CUSTCD = (data.CUSTCD || data.CODE || '').trim(); masterData.IN_CUSTNM = (data.CUSTNM || data.CDNM || '').trim(); }
  }})
  modalVisible.value = true
}

onMounted(async () => { await fetchOptions(); nextTick(() => { initGrid(); search() }) })
</script>

<style scoped>
.hsba070u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 14px; border-radius: 4px; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }

.flex-shrink-0 { flex-shrink: 0 !important; }
.flex-grow-1 { flex-grow: 1 !important; min-height: 0 !important; }
.overflow-hidden { overflow: hidden !important; }

.erp-table-full { width: 100%; border-collapse: collapse; border: 1px solid #dee2e6; }
.erp-table-full th { background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 800; font-size: 11px; padding: 4px 5px !important; color: #495057; white-space: nowrap; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 2px 4px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; font-size: 12px; }
:deep(.tabulator-col-title) { font-weight: 800; color: #334155; }

/* 🚀 팝업 가독성 표준 스타일 */
:deep(.modal-content) { background-color: #ffffff !important; }
:deep(.modal-content .tabulator) { background-color: #ffffff !important; color: #000000 !important; border: 1px solid #dee2e6 !important; }
:deep(.modal-content .tabulator-cell) { color: #000000 !important; font-size: 13px !important; padding: 8px !important; }
</style>
