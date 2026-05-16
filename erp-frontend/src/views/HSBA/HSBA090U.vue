<!--기준정보/거래처관리/거래처등록 [ERP 프리미엄 고밀도 표준 적용] -->
<template>
  <div class="hsba090u-wrapper d-flex flex-column h-100 bg-light p-0 overflow-hidden text-start">
    <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

    <!-- 🚀 1. 상단 액션 바 (표준 헤더) -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-briefcase-fill me-2 text-primary" style="font-size: 16px;"></i>
        기준정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        거래처관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">거래처 등록 (HSBA090U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button @click="initialize" class="btn-erp btn-init">신규</button>
        <button @click="fetchList" class="btn-erp btn-search">조회</button>
        <button @click="save" class="btn-erp btn-save">저장</button>
      </div>
    </div>

    <!-- 🔍 2. 상단 조회 필터 (Premium High-Density 표준 - 줄바꿈 방지) -->
    <div class="search-bar bg-white border-bottom p-2 px-3 d-flex align-items-center flex-shrink-0 gap-4 shadow-sm">
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold small text-secondary flex-shrink-0">거래처구분:</span>
        <select v-model="searchParams.CUSTGBN" class="form-select form-select-sm" style="width: 120px;">
          <option value="000">전체</option>
          <option v-for="opt in options.custGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
        </select>
      </div>

      <div class="vr text-secondary opacity-25" style="height: 18px;"></div>

      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold small text-secondary flex-shrink-0">거래상태:</span>
        <select v-model="searchParams.STATUS" class="form-select form-select-sm" style="width: 110px;">
          <option value="000">전체</option>
          <option v-for="opt in options.status" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
        </select>
      </div>

      <div class="vr text-secondary opacity-25" style="height: 18px;"></div>

      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold small text-secondary flex-shrink-0">거래처명:</span>
        <div class="input-group input-group-sm flex-nowrap" style="width: 280px;">
          <input v-model="searchParams.CUSTNM" type="text" class="form-control" placeholder="상호 또는 코드 입력..." @keyup.enter="fetchList">
          <button @click="searchParams.CUSTNM = ''" class="btn btn-outline-secondary border-start-0 bg-white px-2">
            <i class="bi bi-x-lg" style="font-size: 10px;"></i>
          </button>
        </div>
      </div>
    </div>

    <div class="flex-grow-1 overflow-auto p-3 d-flex flex-column gap-3">
      <!-- 📊 3. 상세 입력 영역 -->
      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between h-auto">
          <div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>거래처 상세 정보</div>
          <div v-if="formData.ACTKIND === 'U0'" class="badge bg-primary-subtle text-primary border border-primary-subtle px-2">수정 모드</div>
          <div v-else class="badge bg-success-subtle text-success border border-success-subtle px-2">신규 등록</div>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="table erp-table-full m-0 border-0 text-dark">
            <colgroup>
              <col style="width: 110px;"> <col>
              <col style="width: 110px;"> <col>
              <col style="width: 110px;"> <col>
              <col style="width: 110px;"> <col>
            </colgroup>
            <tbody>
              <tr>
                <th class="required">거래처코드</th>
                <td><input v-model="formData.CUSTCD" type="text" class="form-control bg-light text-center fw-bold" readonly placeholder="자동 생성" /></td>
                <th class="required">거래처상호</th>
                <td><input v-model="formData.CUSTNM" type="text" class="form-control fw-bold border-primary-subtle" /></td>
                <th>사업자번호</th>
                <td><input v-model="formData.CUSTNO" type="text" class="form-control" placeholder="000-00-00000" /></td>
                <th>거래구분</th>
                <td>
                  <select v-model="formData.CUSTGBN" class="form-select">
                    <option v-for="opt in options.custGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>대표자명</th>
                <td><input v-model="formData.BOSSNM" type="text" class="form-control" /></td>
                <th>업 태</th>
                <td><input v-model="formData.CUSTTYPE" type="text" class="form-control" /></td>
                <th>종 목</th>
                <td><input v-model="formData.CUSTKIND" type="text" class="form-control" /></td>
                <th>설립일자</th>
                <td><input v-model="formData.FNDYMD" type="date" class="form-control" /></td>
              </tr>
              <tr>
                <th>주 소</th>
                <td colspan="5">
                  <div class="d-flex gap-1">
                    <input v-model="formData.POSTNO" type="text" class="form-control text-center" style="width: 80px;" readonly />
                    <button @click="openPostcode" class="btn btn-sm btn-outline-secondary px-2"><i class="bi bi-search" style="font-size: 11px;"></i></button>
                    <input v-model="formData.ADDRESS" type="text" class="form-control flex-grow-1" readonly />
                  </div>
                </td>
                <th>전자세금</th>
                <td>
                  <select v-model="formData.ELCYN" class="form-select">
                    <option value="Y">발행</option>
                    <option value="N">미발행</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>전화번호</th>
                <td><input v-model="formData.TELNO" type="text" class="form-control" /></td>
                <th>팩스번호</th>
                <td><input v-model="formData.FAXNO" type="text" class="form-control" /></td>
                <th>단가구분(입)</th>
                <td>
                  <select v-model="formData.INPRCGBN" class="form-select">
                    <option v-for="opt in options.prcGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>단가구분(출)</th>
                <td>
                  <select v-model="formData.OUTPRCGBN" class="form-select">
                    <option v-for="opt in options.prcGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>한도금액</th>
                <td><input v-model.number="formData.HDAMT" type="number" class="form-control text-end" /></td>
                <th>결제일</th>
                <td><input v-model.number="formData.RCVDD" type="number" class="form-control text-end" placeholder="일자" /></td>
                <th>신용등급</th>
                <td>
                  <select v-model="formData.SINGRD" class="form-select">
                    <option value="A">A등급</option>
                    <option value="B">B등급</option>
                    <option value="C">C등급</option>
                  </select>
                </td>
                <th>마감구분</th>
                <td>
                  <div class="d-flex align-items-center gap-1">
                    <span class="small fw-bold text-secondary">일</span>
                    <select v-model="formData.GIGBCD" class="form-select">
                      <option v-for="opt in options.gigGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                    </select>
                  </div>
                </td>
              </tr>
              <tr>
                <th>분류(대/중)</th>
                <td colspan="3">
                  <div class="d-flex gap-1">
                    <select v-model="formData.AGRPCD" class="form-select">
                      <option value="000">대분류 선택</option>
                      <option v-for="opt in options.agrp" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                    </select>
                    <select v-model="formData.BGRPCD" class="form-select">
                      <option value="000">중분류 선택</option>
                      <option v-for="opt in options.bgrp" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                    </select>
                  </div>
                </td>
                <th>분류(소)/지역</th>
                <td colspan="3">
                  <div class="d-flex gap-1">
                    <select v-model="formData.CGRPCD" class="form-select">
                      <option value="000">소분류 선택</option>
                      <option v-for="opt in options.cgrp" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                    </select>
                    <select v-model="formData.AREA" class="form-select">
                      <option value="000">지역 선택</option>
                      <option v-for="opt in options.area" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                    </select>
                  </div>
                </td>
              </tr>
              <tr>
                <th>담 당 자</th>
                <td><input v-model="formData.CDAMDANG" type="text" class="form-control" /></td>
                <th>연락처(SMS)</th>
                <td><input v-model="formData.CTELNO" type="text" class="form-control" /></td>
                <th>전자메일</th>
                <td><input v-model="formData.CEMAIL" type="text" class="form-control" /></td>
                <th>매입매출</th>
                <td>
                  <select v-model="formData.IOGBN" class="form-select">
                    <option v-for="opt in options.ioGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>적용/유효일</th>
                <td colspan="3">
                  <div class="d-flex gap-1 align-items-center">
                    <input v-model="uiSTDYMD" type="date" class="form-control" />
                    <span class="small mx-1 text-secondary opacity-50">~</span>
                    <input v-model="uiCLSYMD" type="date" class="form-control" />
                  </div>
                </td>
                <th>상태/사용</th>
                <td colspan="3">
                  <div class="d-flex align-items-center gap-3 h-100">
                    <select v-model="formData.STATUS" class="form-select" style="width: 120px;">
                      <option v-for="opt in options.status" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                    </select>
                    <div class="form-check form-switch m-0 d-flex align-items-center">
                      <input v-model="formData.USEYN" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYn090">
                      <label class="form-check-label ms-2 small fw-bold text-dark" for="useYn090">{{ formData.USEYN === 'Y' ? '사용 중' : '중지' }}</label>
                    </div>
                  </div>
                </td>
              </tr>
              <tr>
                <th>비 고</th>
                <td colspan="7"><input v-model="formData.REMARK" type="text" class="form-control" placeholder="기타 참고사항 입력" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 📊 4. 하단 그리드 영역 -->
      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center flex-shrink-0 h-auto">
          <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1 text-secondary"></i> 거래처 목록</span>
          <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle px-2">Total: {{ activeItemCount }} 건</span>
        </div>
        <div class="card-body p-0 flex-grow-1 overflow-hidden bg-white text-dark">
          <div ref="gridElement" class="tabulator-full-height"></div>
        </div>
      </div>
    </div>

    <!-- 📊 5. 하단 정보 바 -->
    <div class="bg-dark text-white-50 px-3 py-1 d-flex justify-content-between align-items-center flex-shrink-0 shadow-lg" style="font-size: 11px;">
       <span><i class="bi bi-info-circle me-1"></i> 목록 행 클릭 시 상세 정보를 수정할 수 있습니다.</span>
       <span class="text-white fw-bold">Haion ERP Premium High-Density</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchParams = reactive({ CUSTGBN: '000', STATUS: '010', CUSTNM: '' })
const activeItemCount = ref(0)
const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

const formData = reactive<any>({
  ACTKIND: 'I0', CUSTCD: '', CUSTNM: '', CUSTNO: '', JUMINNO: '', LEGALNO: '', CUSTGBN: '010',
  BOSSNM: '', CUSTTYPE: '', CUSTKIND: '', POSTNO: '', ADDRESS: '', TELNO: '', FAXNO: '',
  INPRCGBN: '200', OUTPRCGBN: '200', HDAMT: 0, RCVDD: 0, GIGBCD: '305',
  AGRPCD: '000', BGRPCD: '000', CGRPCD: '000', AREA: '000', CDAMDANG: '', CTELNO: '', CEMAIL: '',
  REMARK: '', STATUS: '010', BANKNM: '', GUJOA: '', STDYMD: '', CLSYMD: '99991231',
  SINGRD: 'A', ELCYN: 'Y', USEYN: 'Y', IOGBN: '010', FNDYMD: ''
})

const options = reactive<any>({
  custGbn: [], status: [], prcGbn: [], gigGbn: [], agrp: [], bgrp: [], cgrp: [], area: [], ioGbn: []
})

const uiSTDYMD = computed({ get: () => formatDate(formData.STDYMD), set: (v) => formData.STDYMD = v.replace(/-/g, '') })
const uiCLSYMD = computed({ get: () => formatDate(formData.CLSYMD), set: (v) => formData.CLSYMD = v.replace(/-/g, '') })

async function fetchOptions() {
  const getOptHS = async (gbn: string, cd = '') => {
    try {
      const res = await api.post('/api/hs00/HS00_000S_STR', {
        GUBUN: gbn, CMPYCD: authStore.CMPYCD, GBNCD: cd, CODE: '', CODENM: '', ETCVAL: ''
      })
      return (res.data || []).map((i: any) => {
        const c = String(i.CODECD || i.codecd || Object.values(i)[0] || '').trim()
        const n = String(i.CODENM || i.codenm || Object.values(i)[1] || '').trim()
        return { CODECD: c, CODENM: n ? `[${c}] ${n}` : c }
      })
    } catch (e) { return [] }
  }

  const getOptHA = async (gbn: string, cd = '') => {
    try {
      const res = await api.post('/api/ha00/HA00_00P_STR', {
        GUBUN: gbn, CMPYCD: authStore.CMPYCD, GBNCD: cd, CODE: '', REMARK: ''
      })
      return (res.data || []).map((i: any) => {
        const c = String(i.CODECD || i.codecd || '').trim()
        const n = String(i.CODENM || i.codenm || '').trim()
        return { CODECD: c, CODENM: n ? `[${c}] ${n}` : c }
      })
    } catch (e) { return [] }
  }

  options.custGbn = await getOptHA('E0', '270')
  options.status = await getOptHA('E0', '280')
  options.prcGbn = await getOptHS('E0', '200')
  options.gigGbn = await getOptHS('E0', '305')
  options.agrp = await getOptHS('E0', '400')
  options.bgrp = await getOptHS('E0', '410')
  options.cgrp = await getOptHS('E0', '420')
  options.area = await getOptHS('AR')
  options.ioGbn = await getOptHS('E0', '340')
}

const initGrid = () => {
  if (gridElement.value) {
    grid = new Tabulator(gridElement.value, {
      layout: "fitColumns",
      height: "100%",
      pagination: "local",
      paginationSize: 15,
      rowHeight: 32,
      columnDefaults: { vertAlign: "middle", headerHozAlign: "center", headerSort: false },
      columns: [
        { title: "코드", field: "CUSTCD", width: 90, hozAlign: "center", cssClass: "fw-bold text-primary" },
        { title: "거래처명 (상호)", field: "CUSTNM", minWidth: 200, widthGrow: 1, cssClass: "fw-bold text-dark text-start" },
        { title: "사업자번호", field: "CUSTNO", width: 130, hozAlign: "center" },
        { title: "대표자", field: "BOSSNM", width: 100, hozAlign: "center" },
        { title: "연락처", field: "TELNO", width: 130, hozAlign: "start" },
        { title: "상태", field: "STATUSNM", width: 80, hozAlign: "center" },
        { title: "사용", field: "USEYN", width: 60, hozAlign: "center", formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
      ]
    })
    grid.on("rowClick", (e, row) => {
        Object.assign(formData, row.getData());
        formData.ACTKIND = 'U0';
    })
  }
}

async function fetchList() {
  try {
    const res = await api.post('/api/hsba/HSBA_090U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      CUSTNM: searchParams.CUSTNM, CUSTGBN: searchParams.CUSTGBN === '000' ? '' : searchParams.CUSTGBN, STATUS: searchParams.STATUS === '000' ? '' : searchParams.STATUS
    })
    if (grid) {
      grid.setData(res.data);
      activeItemCount.value = res.data.length;
    }
  } catch (e) { vAlertError('조회 오류') }
}

async function save() {
  if (!formData.CUSTNM) return vAlertError('상호를 입력하세요.')
  try {
    const res = await api.post('/api/hsba/HSBA_090U_STR', { ...formData, CMPYCD: authStore.CMPYCD, UPDEMP: authStore.USERID })
    if (res.data?.[0]?.ERRYN === 'Y') vAlertError(res.data[0].MSG)
    else { vAlert('저장되었습니다.'); fetchList(); initialize() }
  } catch (e) { vAlertError('저장 실패') }
}

function initialize() {
  resetForm(formData);
  Object.assign(formData, {
    ACTKIND: 'I0', CUSTCD: '', CUSTNM: '', CUSTNO: '', JUMINNO: '', LEGALNO: '', CUSTGBN: '010',
    BOSSNM: '', CUSTTYPE: '', CUSTKIND: '', POSTNO: '', ADDRESS: '', TELNO: '', FAXNO: '',
    INPRCGBN: '200', OUTPRCGBN: '200', HDAMT: 0, RCVDD: 0, GIGBCD: '305',
    AGRPCD: '000', BGRPCD: '000', CGRPCD: '000', AREA: '000', CDAMDANG: '', CTELNO: '', CEMAIL: '',
    REMARK: '', STATUS: '010', BANKNM: '', GUJOA: '', STDYMD: '', CLSYMD: '99991231',
    SINGRD: 'A', ELCYN: 'Y', USEYN: 'Y', IOGBN: '010', FNDYMD: ''
  });
}

function openPostcode() {
  // @ts-ignore
  if (window.daum && window.daum.Postcode) {
    new window.daum.Postcode({
      oncomplete: (data: any) => {
        formData.POSTNO = data.zonecode;
        formData.ADDRESS = data.roadAddress
      }
    }).open()
  } else {
    vAlertError('우편번호 서비스를 호출할 수 없습니다.')
  }
}

const getToday = () => new Date().toISOString().split('T')[0].replace(/-/g, '')
const formatDate = (v: string) => v && v.length === 8 ? `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}` : v

onMounted(async () => {
  await fetchOptions()
  nextTick(() => { initGrid(); initialize(); fetchList() })
})
</script>

<style scoped>
.hsba090u-wrapper { height: 100%; overflow: hidden; }

/* 🚀 ERP 프리미엄 고밀도 표준 헤더 */
.erp-header { background-color: #fff; border-bottom: 1px solid #dee2e6; height: 48px; z-index: 100; }
.btn-group-erp { display: flex; gap: 4px; }
.btn-erp { padding: 4px 18px; border-radius: 4px; font-size: 13px; font-weight: 700; height: 32px; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #4b5563 !important; border: 1px solid #d1d5db !important; }
.btn-init:hover { background-color: #f9fafb !important; }
.btn-search { background-color: #374151 !important; color: #fff !important; border: none !important; }
.btn-search:hover { background-color: #1f2937 !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-save:hover { background-color: #004a85 !important; }

/* 📊 상단 조회바 (Premium High-Density 표준) */
.search-bar { background-color: #fff; border-bottom: 1px solid #dee2e6; }
.search-label { font-size: 12px; font-weight: 700; color: #475569; margin-right: 8px; white-space: nowrap; }

/* 💡 줄바꿈 방지 핵심 설정 */
.input-group.flex-nowrap {
  display: flex !important;
  flex-wrap: nowrap !important;
}
.input-group > .form-control {
  flex: 1 1 auto;
  width: 1%;
}

/* 📊 테이블 및 입력 폼 (표준 그리드 폰트 12.5px) */
.erp-table-full { table-layout: fixed !important; border-collapse: collapse !important; width: 100%; border: 1px solid #e2e8f0; }
.erp-table-full th { background-color: #f8fafc; border: 1px solid #e2e8f0 !important; text-align: center; font-weight: 800; font-size: 12.5px; color: #475569; padding: 10px 15px !important; white-space: nowrap; vertical-align: middle; }
.erp-table-full td { vertical-align: middle; padding: 6px 10px !important; border: 1px solid #e2e8f0 !important; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }

.form-control, .form-select { border-radius: 4px; border: 1px solid #cbd5e1; font-size: 13px; height: 30px; width: 100%; transition: all 0.15s; color: #334155; }
.form-control:focus, .form-select:focus { border-color: #005a9f; box-shadow: 0 0 0 2px rgba(0, 90, 159, 0.1); outline: none; }

/* 💡 Tabulator 그리드 스타일 보정 */
:deep(.tabulator) { border: none; font-size: 13px; background-color: transparent !important; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #cbd5e1 !important; }
:deep(.tabulator-col-title) { text-align: center !important; font-weight: 800; color: #334155; }
:deep(.tabulator-row) { border-bottom: 1px solid #f1f5f9 !important; }
:deep(.tabulator-row.tabulator-selected) { background-color: #e2e8f0 !important; }
.tabulator-full-height { height: 100% !important; }
</style>
