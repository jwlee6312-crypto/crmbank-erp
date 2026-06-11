<template>
  <AppAlert :show="showalert" :error="showerror" :message="alertmessage" />

  <!-- 💡 근본 해결: 최외각 wrapper에 overflow-hidden 적용 -->
  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-truck me-2 text-primary"></i>
        기본정보 > <span class="text-primary fw-bolder">거래 배송처 등록 (hsba130u)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-y-auto p-2 d-flex flex-column gap-2 bg-light">
      <!-- 🅰️ 조회 조건 영역 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-1 bg-white">
          <div class="d-flex align-items-center gap-3 px-2">
            <div class="input-group input-group-sm flex-nowrap" style="width: 350px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">거래처 명</span>
              <input v-model="searchdata.scustnm" type="text" class="form-control border-0 bg-light" placeholder="거래처명 입력" @keyup.enter="search" />
              <button class="btn btn-dark btn-sm" @click="search"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 🅱️ 마스터 정보 입력 영역 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 배송처 정보 입력</span>
          <div class="d-flex gap-2 align-items-center">
            <span v-if="masterdata.actkind === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 모드 ({{ masterdata.custcd }} - {{ masterdata.trancd }})</span>
            <span v-else class="badge bg-primary" style="font-size: 10px;">신규 등록</span>
          </div>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <tbody>
              <tr>
                <th class="required" style="width: 70px;">거 래 처</th>
                <td style="width: 220px;">
                  <div class="input-group input-group-sm">
                    <input v-model="masterdata.custcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
                    <input v-model="masterdata.custnm" type="text" class="form-control bg-light" placeholder="거래처 선택" readonly />
                    <button class="btn btn-outline-secondary" @click="openhelp('cust')" :disabled="masterdata.actkind === 'U0'"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required" style="width: 70px;">주&nbsp;&nbsp;&nbsp;&nbsp;소</th>
                <td style="width: 320px;">
                  <AddressPopupForm v-model:postno="masterdata.postno" v-model:address="masterdata.address" v-model:d_address="masterdata.address_detail" active />
                </td>
                <th style="width: 70px;">지&nbsp;&nbsp;&nbsp;&nbsp;역</th>
                <td style="width: 120px;">
                  <select v-model="masterdata.area" class="form-select form-select-sm">
                    <option v-for="opt in areaoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th style="width: 70px;">담 당 자</th>
                <td style="width: 110px;">
                  <input v-model="masterdata.damdang" type="text" class="form-control form-control-sm" maxlength="30" />
                </td>
                <th style="width: 70px;">연 락 처</th>
                <td style="width: 300px;">
                  <div class="d-flex gap-1">
                    <input v-model="masterdata.telno" type="text" class="form-control form-control-sm" placeholder="전화" maxlength="30" />
                    <input v-model="masterdata.email" type="text" class="form-control form-control-sm" placeholder="이메일" maxlength="50" style="ime-mode:inactive" />
                  </div>
                </td>
                <th style="width: 50px;">사용</th>
                <td style="width: 60px;">
                  <div class="form-check form-switch m-0 d-flex justify-content-center">
                    <input v-model="masterdata.useyn" class="form-check-input" type="checkbox" id="useyn130" true-value="y" false-value="n">
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 데이터 그리드 영역 -->
      <div class="card border shadow-sm erp-main-grid overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 배송처 목록 ({{ activeitemcount }} 건)</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridelement" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
    <Modal v-model:visible="modalvisible" :modalProps="modalprops" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import AddressPopupForm from '@/components/AddressPopupForm.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import type { ModalProps } from '@/types/modal'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

// 1. 상태 관리 (모든 키와 값 소문자 원칙)
const searchdata = reactive({ scustnm: '', scustcd: '' })
const masterdata = reactive<any>({
  actkind: 'A0', cmpycd: authstore.cmpycd, custcd: '', custnm: '', trancd: '',
  area: '000', postno: '', address: '', address_detail: '', damdang: '', telno: '', email: '', useyn: 'Y'
})

const areaoptions = ref<any[]>([])
const gridelement = ref<HTMLElement | null>(null); let grid: Tabulator | null = null; const activeitemcount = ref(0)

const normalizekeys = (obj: any) => {
  const n: any = {};
  if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

// 2. 그리드 초기화
const initgrid = () => {
  if (!gridelement.value) return
  grid = new Tabulator(gridelement.value, {
    layout: "fitColumns", height: "100%", placeholder: "조회된 데이터가 없습니다.",
    pagination: "local", paginationSize: 20, paginationButtonCount: 3,
    columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
    columns: [
      { title: "거래처코드", field: "custcd", width: 100 },
      { title: "상호", field: "custnm", minWidth: 200, hozAlign: "left", cssClass: "fw-bold text-primary" },
      { title: "순번", field: "trancd", width: 60 },
      { title: "담당자", field: "damdang", width: 120 },
      { title: "주소", field: "address", minWidth: 300, hozAlign: "left" },
      { title: "사용", field: "useyn", width: 60, formatter: (c) => c.getValue() === 'Y' ? 'o' : 'x' }
    ]
  })

  grid.on("rowClick", (e, row) => {
    const data = normalizekeys(row.getData());
    Object.assign(masterdata, {
      actkind: 'U0', cmpycd: data.cmpycd, custcd: data.custcd, custnm: data.custnm,
      trancd: data.trancd, area: data.area || '000', postno: data.postno,
      damdang: data.damdang, telno: data.telno, email: data.email, useyn: data.useyn || 'Y'
    });
    if (data.address && data.address.includes('  ')) {
      const parts = data.address.split('  '); masterdata.address = parts[0]; masterdata.address_detail = parts[1];
    } else {
      masterdata.address = data.address; masterdata.address_detail = '';
    }
  })
}

// 3. 기능 구현
async function fetchoptions() {
  try {
    const res = await api.post('/api/hs00/hs00_000s_str', { gubun: 'ar', cmpycd: authstore.cmpycd });
    areaoptions.value = res.data.map((i: any) => {
      const n = normalizekeys(i);
      return { codecd: String(n.code || n.codecd || '').trim(), codenm: String(n.cdnm || n.codenm || '').trim() }
    })
    if (areaoptions.value.length === 0) areaoptions.value = [{ codecd: '000', codenm: '없음' }]
  } catch (e) {}
}

async function search() {
  try {
    const res = await api.post('/api/hsba/hsba_130u_str', {
      actkind: 'S0', cmpycd: authstore.cmpycd, custcd: searchdata.scustcd || '', custnm: searchdata.scustnm || '',
      trancd: '', area: '', postno: '', address: '', damdang: '', telno: '', email: '', useyn: '', userid: authstore.userid
    })
    const processed = (res.data || []).map((i: any) => normalizekeys(i));
    grid?.setData(processed); activeitemcount.value = processed.length
  } catch (e) { valerterror('조회 실패') }
}

async function save() {
  if (!masterdata.custcd) return valerterror('거래처를 선택해 주십시요.')
  if (!masterdata.address) return valerterror('주소를 입력해 주십시요.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const payload = { ...masterdata, address: `${masterdata.address}  ${masterdata.address_detail}`.trim(), userid: authstore.userid }
    const res = await api.post('/api/hsba/hsba_130u_str', payload)
    const resdata = normalizekeys(res.data?.[0]);
    if (resdata.result === 'N' || resdata.erryn === 'Y') valerterror(resdata.msg || '저장 실패')
    else { valert('성공적으로 저장되었습니다.'); search(); initialize() }
  } catch (e) { valerterror('저장 중 오류 발생') }
}

function initialize() {
  resetform(masterdata);
  Object.assign(masterdata, { actkind: 'A0', cmpycd: authstore.cmpycd, area: '000', useyn: 'Y' })
}

const modalvisible = ref(false); const modalprops = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openhelp(type: string) {
  if (type === 'cust') {
    Object.assign(modalprops, { title: '거래처 선택', path: '/api/ha00/ha00_00p_str', defaultField: 'custnm', large: true, data: { gubun: 'c4', cmpycd: authstore.cmpycd }, columns: [{ title: '코드', field: 'custcd', width: 80 }, { title: '거래처명', field: 'custnm', width: 200 }, { title: '사업자번호', field: 'custno', width: 120 }, { title: '주소', field: 'address', minWidth: 300 }], onConfirm: (d: any) => {
      const n = normalizekeys(d);
      masterdata.custcd = n.custcd; masterdata.custnm = n.custnm
    } })
    modalvisible.value = true
  }
}

onMounted(async () => { await fetchoptions(); nextTick(() => { initgrid(); search() }) })
</script>
