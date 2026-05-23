<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 💡 Haion ERP 표준 레이아웃: HSBA070U 패턴 적용 -->
  <div class="hsba090u-wrapper d-flex flex-column h-100 bg-white p-0 overflow-hidden text-start">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-briefcase-fill me-2 text-primary"></i>
        기준정보 > 거래처관리 > <span class="text-primary fw-bolder">거래처 등록 (HSBA090U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 d-flex flex-column gap-2 p-2 overflow-hidden">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-1 bg-light-subtle">
          <div class="d-flex align-items-center gap-3">
            <div class="input-group input-group-sm flex-nowrap" style="width: 180px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">구분</span>
              <select v-model="searchParams.CUSTGBN" class="form-select border-0 bg-white" @change="fetchList">
                <option value="000">전체</option>
                <option v-for="opt in options.custGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
              </select>
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width: 180px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">상태</span>
              <select v-model="searchParams.STATUS" class="form-select border-0 bg-white" @change="fetchList">
                <option value="000">전체</option>
                <option v-for="opt in options.status" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
              </select>
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width: 300px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">거래처명</span>
              <input v-model="searchParams.CUSTNM" type="text" class="form-control border-0 bg-white" placeholder="거래처명 입력..." @keyup.enter="fetchList" />
              <button class="btn btn-dark btn-sm" @click="fetchList"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 🅱️ 상세 입력 폼 영역: 5열 레이아웃 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f8f9fa;">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1"></i> 거래처 상세 정보</span>
          <div class="d-flex gap-2">
            <span v-if="formData.ACTKIND === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 모드</span>
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
                <td><input v-model="formData.CUSTCD" type="text" class="form-control form-control-sm text-center fw-bold text-primary" placeholder="자동 생성" readonly /></td>
                <th class="required">거래처상호</th>
                <td><input v-model="formData.CUSTNM" type="text" class="form-control form-control-sm fw-bold border-primary-subtle" maxlength="50" /></td>
                <th>사업자번호</th>
                <td><input v-model="formData.CUSTNO" type="text" class="form-control form-control-sm" maxlength="13" placeholder="- 없이" /></td>
                <th>거래구분</th>
                <td>
                  <select v-model="formData.CUSTGBN" class="form-select form-select-sm">
                    <option v-for="opt in options.custGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>대표자명</th>
                <td><input v-model="formData.BOSSNM" type="text" class="form-control form-control-sm" maxlength="30" /></td>
              </tr>
              <tr>
                <th>업&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;태</th>
                <td><input v-model="formData.CUSTTYPE" type="text" class="form-control form-control-sm" maxlength="50" /></td>
                <th>종&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;목</th>
                <td><input v-model="formData.CUSTKIND" type="text" class="form-control form-control-sm" maxlength="50" /></td>
                <th>설립일자</th>
                <td><input v-model="formData.FNDYMD" type="date" class="form-control form-control-sm" /></td>
                <th>전화번호</th>
                <td><input v-model="formData.TELNO" type="text" class="form-control form-control-sm" maxlength="30" /></td>
                <th>팩스번호</th>
                <td><input v-model="formData.FAXNO" type="text" class="form-control form-control-sm" maxlength="30" /></td>
              </tr>
              <tr>
                <th>주&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;소</th>
                <td colspan="7">
                  <div class="d-flex gap-1">
                    <input v-model="formData.POSTNO" type="text" class="form-control form-control-sm text-center" style="width: 70px;" readonly />
                    <button class="btn btn-outline-secondary btn-sm px-2" @click="openPostcode"><i class="bi bi-search"></i></button>
                    <input v-model="formData.ADDRESS" type="text" class="form-control form-control-sm flex-grow-1" />
                  </div>
                </td>
                <th>전자세금</th>
                <td>
                  <select v-model="formData.ELCYN" class="form-select form-select-sm">
                    <option value="Y">발행</option>
                    <option value="N">미발행</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>단가(입)</th>
                <td>
                  <select v-model="formData.INPRCGBN" class="form-select form-select-sm">
                    <option v-for="opt in options.prcGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>단가(출)</th>
                <td>
                  <select v-model="formData.OUTPRCGBN" class="form-select form-select-sm">
                    <option v-for="opt in options.prcGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>한도금액</th>
                <td><input v-model.number="formData.HDAMT" type="number" class="form-control form-control-sm text-end" /></td>
                <th>결제일</th>
                <td><input v-model.number="formData.RCVDD" type="number" class="form-control form-control-sm text-end" placeholder="일자" /></td>
                <th>마감구분</th>
                <td>
                  <div class="d-flex gap-1 align-items-center">
                    <span class="small">일</span>
                    <select v-model="formData.GIGBCD" class="form-select form-select-sm flex-grow-1">
                      <option v-for="opt in options.gigGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                    </select>
                  </div>
                </td>
              </tr>
              <tr>
                <th>대분류</th>
                <td>
                  <select v-model="formData.AGRPCD" class="form-select form-select-sm">
                    <option value="000">선택</option>
                    <option v-for="opt in options.agrp" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>중분류</th>
                <td>
                  <select v-model="formData.BGRPCD" class="form-select form-select-sm">
                    <option value="000">선택</option>
                    <option v-for="opt in options.bgrp" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>소분류</th>
                <td>
                  <select v-model="formData.CGRPCD" class="form-select form-select-sm">
                    <option value="000">선택</option>
                    <option v-for="opt in options.cgrp" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>지&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;역</th>
                <td>
                  <select v-model="formData.AREA" class="form-select form-select-sm">
                    <option value="000">선택</option>
                    <option v-for="opt in options.area" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>담 당 자</th>
                <td><input v-model="formData.CDAMDANG" type="text" class="form-control form-control-sm" /></td>
              </tr>
              <tr>
                <th>신용등급</th>
                <td>
                  <select v-model="formData.SINGRD" class="form-select form-select-sm">
                    <option value="A">A등급</option><option value="B">B등급</option><option value="C">C등급</option>
                  </select>
                </td>
                <th>적용일자</th>
                <td><input v-model="uiSTDYMD" type="date" class="form-control form-control-sm" /></td>
                <th>유효일자</th>
                <td><input v-model="uiCLSYMD" type="date" class="form-control form-control-sm" /></td>
                <th>상&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;태</th>
                <td>
                  <select v-model="formData.STATUS" class="form-select form-select-sm">
                    <option v-for="opt in options.status" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>사용여부</th>
                <td>
                  <div class="form-check form-switch m-0 d-flex align-items-center">
                    <input v-model="formData.USEYN" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYn090">
                    <label class="form-check-label ms-2 small fw-bold text-dark" for="useYn090">{{ formData.USEYN === 'Y' ? '사용' : '중지' }}</label>
                  </div>
                </td>
              </tr>
              <tr>
                <th>매입매출</th>
                <td>
                  <select v-model="formData.IOGBN" class="form-select form-select-sm">
                    <option v-for="opt in options.ioGbn" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
                  </select>
                </td>
                <th>연락처(SMS)</th>
                <td><input v-model="formData.CTELNO" type="text" class="form-control form-control-sm" /></td>
                <th>전자메일</th>
                <td><input v-model="formData.CEMAIL" type="text" class="form-control form-control-sm" /></td>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td colspan="3"><input v-model="formData.REMARK" type="text" class="form-control form-control-sm w-100" placeholder="기타 참고사항 입력" /></td>
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

// 1. 상태 관리
const searchParams = reactive({ CUSTGBN: '000', STATUS: '010', CUSTNM: '' })
const activeItemCount = ref(0)
const gridElement = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

const formData = reactive<any>({
  ACTKIND: 'A0', CUSTCD: '', CUSTNM: '', CUSTNO: '', JUMINNO: '', LEGALNO: '', CUSTGBN: '010',
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

// 2. 기능 구현
async function fetchOptions() {
  const getOptHS = async (gbn: string, cd = '') => {
    try {
      const res = await api.post('/api/hs00/HS00_000S_STR', { GUBUN: gbn, CMPYCD: authStore.CMPYCD, GBNCD: cd })
      return (res.data || []).map((i: any) => ({ CODECD: String(i.CODE || i.CODECD || '').trim(), CODENM: String(i.CDNM || i.CODENM || '').trim() }))
    } catch (e) { return [] }
  }
  const getOptHA = async (gbn: string, cd = '') => {
    try {
      const res = await api.post('/api/ha00/HA00_00P_STR', { GUBUN: gbn, CMPYCD: authStore.CMPYCD, GBNCD: cd })
      return (res.data || []).map((i: any) => ({ CODECD: String(i.CODE || i.CODECD || '').trim(), CODENM: String(i.CDNM || i.CODENM || '').trim() }))
    } catch (e) { return [] }
  }

  const [o1, o2, o3, o4, o5, o6, o7, o8, o9] = await Promise.all([
    getOptHA('E0', '270'), getOptHA('E0', '280'), getOptHS('E0', '200'),
    getOptHS('E0', '305'), getOptHS('E0', '400'), getOptHS('E0', '410'),
    getOptHS('E0', '420'), getOptHS('AR'), getOptHS('E0', '340')
  ])
  options.custGbn = o1; options.status = o2; options.prcGbn = o3; options.gigGbn = o4;
  options.agrp = o5; options.bgrp = o6; options.cgrp = o7; options.area = o8; options.ioGbn = o9;
}

const initGrid = () => {
  if (!gridElement.value) return
  grid = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 20,
    columnDefaults: { vertAlign: "middle", headerHozAlign: "center", headerSort: false },
    columns: [
      { title: "코드", field: "CUSTCD", width: 90, hozAlign: "center", cssClass: "fw-bold text-primary" },
      { title: "거래처명 (상호)", field: "CUSTNM", minWidth: 250, hozAlign: "left" },
      { title: "사업자번호", field: "CUSTNO", width: 130, hozAlign: "center" },
      { title: "대표자", field: "BOSSNM", width: 100, hozAlign: "center" },
      { title: "연락처", field: "TELNO", width: 130, hozAlign: "left" },
      { title: "상태", field: "STATUSNM", width: 80, hozAlign: "center" },
      { title: "사용", field: "USEYN", width: 60, hozAlign: "center", formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
    ]
  })
  grid.on("rowClick", (e, row) => {
    const data = row.getData()
    const cleaned: any = {}
    Object.keys(data).forEach(k => cleaned[k] = typeof data[k] === 'string' ? data[k].trim() : data[k])
    Object.assign(formData, cleaned); formData.ACTKIND = 'U0'
  })
}

async function fetchList() {
  try {
    const res = await api.post('/api/hsba/HSBA_090U_STR', {
      ACTKIND: 'S0', CMPYCD: authStore.CMPYCD,
      CUSTNM: searchParams.CUSTNM, CUSTGBN: searchParams.CUSTGBN === '000' ? '' : searchParams.CUSTGBN, STATUS: searchParams.STATUS === '000' ? '' : searchParams.STATUS
    })
    grid?.setData(res.data); activeItemCount.value = res.data.length;
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!formData.CUSTNM) return vAlertError('상호를 입력하세요.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const res = await api.post('/api/hsba/HSBA_090U_STR', { ...formData, CMPYCD: authStore.CMPYCD, UPDEMP: authStore.USERID })
    if (res.data?.[0]?.ERRYN === 'Y') vAlertError(res.data[0].MSG)
    else { vAlert('정상적으로 저장되었습니다.'); fetchList(); initialize() }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(formData);
  Object.assign(formData, {
    ACTKIND: 'A0', CUSTCD: '', CUSTNM: '', CUSTNO: '', JUMINNO: '', LEGALNO: '', CUSTGBN: '010',
    BOSSNM: '', CUSTTYPE: '', CUSTKIND: '', POSTNO: '', ADDRESS: '', TELNO: '', FAXNO: '',
    INPRCGBN: '200', OUTPRCGBN: '200', HDAMT: 0, RCVDD: 0, GIGBCD: '305',
    AGRPCD: '000', BGRPCD: '000', CGRPCD: '000', AREA: '000', CDAMDANG: '', CTELNO: '', CEMAIL: '',
    REMARK: '', STATUS: '010', BANKNM: '', GUJOA: '', STDYMD: new Date().toISOString().substring(0, 10).replace(/-/g, ''), CLSYMD: '99991231',
    SINGRD: 'A', ELCYN: 'Y', USEYN: 'Y', IOGBN: '010', FNDYMD: ''
  });
}

function openPostcode() {
  // @ts-ignore
  new daum.Postcode({ oncomplete: (data: any) => { formData.POSTNO = data.zonecode; formData.ADDRESS = data.roadAddress } }).open()
}

const formatDate = (v: string) => v && v.length === 8 ? `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}` : v

onMounted(async () => { await fetchOptions(); nextTick(() => { initGrid(); initialize(); fetchList() }) })
</script>

<style scoped>
.hsba090u-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
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

/* 🚀 팝업 및 그리드 가독성 스타일 */
:deep(.tabulator-cell) { font-size: 13px !important; }
</style>
