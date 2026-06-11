<template>
  <AppAlert :show="showalert" :error="showerror" :message="alertmessage" />
  <Modal v-model:visible="modalvisible" :modalProps="modalprops" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-list-check me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">재무제표 항목설정 (haba021u)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize">신규</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <div class="flex-grow-1 overflow-hidden p-3 d-flex flex-column gap-3 bg-light">
      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <tbody>
              <tr>
                <th class="bg-light text-center" style="width: 100px;">연도</th>
                <td style="width: 150px;"><input v-model="searchform.yyyy" type="number" class="form-control form-control-sm text-center" @change="search" /></td>
                <th class="bg-light text-center" style="width: 100px;">재무제표</th>
                <td>
                  <select v-model="searchform.gubun" class="form-select form-select-sm" style="max-width: 200px;" @change="fetchgroups">
                    <option v-for="opt in statementoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center" style="width: 100px;">항목그룹</th>
                <td>
                  <select v-model="searchform.grpcd" class="form-select form-select-sm" style="max-width: 200px;" @change="search">
                    <option value="">전체</option>
                    <option v-for="opt in groupoptions" :key="opt.grpcd" :value="opt.grpcd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <tbody>
              <tr>
                <th class="required bg-light text-center" style="width: 100px;">계정과목</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterdata.acctcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" readonly />
                    <input v-model="masterdata.acctcd_t" type="text" class="form-control" placeholder="선택" @keydown.enter="openhelp('acct')" />
                    <button class="btn btn-outline-secondary px-2" @click="openhelp('acct')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light text-center" style="width: 100px;">계산타입</th>
                <td>
                  <select v-model="masterdata.caltype" class="form-select form-select-sm">
                    <option v-for="opt in calctypeoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center" style="width: 100px;">가감</th>
                <td>
                  <select v-model="masterdata.calgagam" class="form-select form-select-sm">
                    <option value="000">해당없음</option>
                    <option value="100">더하기(+)</option>
                    <option value="200">빼기(-)</option>
                  </select>
                </td>
                <th class="bg-light text-center" style="width: 100px;">사용</th>
                <td>
                  <div class="form-check form-switch m-0 d-flex justify-content-center">
                    <input v-model="masterdata.useyn" class="form-check-input" type="checkbox" true-value="y" false-value="n">
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="maingridelement" class="tabulator-instance flex-grow-1"></div>
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
import { useCommonHelp } from '@/composables/useCommonHelp'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()
const { modalVisible: modalvisible, modalProps: modalprops, openHelp: commonopenhelp } = useCommonHelp()

const searchform = reactive({ yyyy: new Date().getFullYear(), gubun: '010', grpcd: '' })
const masterdata = reactive<any>({
  actkind: 'i1', cmpycd: authstore.cmpycd, yyyy: new Date().getFullYear(), gubun: '010', grpcd: '',
  acctcd: '', acctcd_t: '', arowno: '', caltype: '000', calgagam: '000', useyn: 'Y'
})

const statementoptions = ref<any[]>([]); const groupoptions = ref<any[]>([]); const calctypeoptions = ref<any[]>([])
const maingridelement = ref<HTMLDivElement | null>(null); let maingrid: Tabulator | null = null

const normalizekeys = (obj: any) => {
  const n: any = {}; if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

const fetchgroups = async () => {
  try {
    const res = await api.post('/api/haba/haba_020u_str', { actkind: 'S2', cmpycd: authstore.cmpycd, gubun: searchform.gubun })
    groupoptions.value = (res.data || []).map((i: any) => normalizekeys(i));
    search()
  } catch (e) { console.error('그룹 로드 실패') }
}

const search = async () => {
  try {
    const res = await api.post('/api/haba/haba_021u_str', {
        actkind: 's3', cmpycd: authstore.cmpycd, yyyy: searchform.yyyy, gubun: searchform.gubun, grpcd: searchform.grpcd
    });
    const processed = (res.data || []).map((i: any) => normalizekeys(i));
    maingrid?.setData(processed);
  } catch (e) { valerterror('조회 실패') }
}

const save = async () => {
  if (!masterdata.acctcd) return valerterror('계정과목을 선택하세요.');
  if (!confirm('저장하시겠습니까?')) return
  try {
    const payload = { ...masterdata, yyyy: searchform.yyyy, gubun: searchform.gubun, grpcd: searchform.grpcd }
    const res = await api.post('/api/haba/haba_021u_str', payload);
    const resdata = normalizekeys(res.data?.[0]);
    if (resdata.result === 'Y') { valert('저장되었습니다.'); search(); }
  } catch (e) { valerterror('저장 실패') }
}

const initialize = () => {
  const curyyyy = searchform.yyyy; const curgubun = searchform.gubun; const curgrp = searchform.grpcd
  resetform(masterdata);
  Object.assign(masterdata, { actkind: 'i1', cmpycd: authstore.cmpycd, yyyy: curyyyy, gubun: curgubun, grpcd: curgrp, useyn: 'Y', caltype: '000', calgagam: '000' });
}

function openhelp(type: string) {
  if (type === 'acct') {
    commonopenhelp('ACCT', (d) => {
        const n = normalizekeys(d);
        masterdata.acctcd = n.acctcd; masterdata.acctcd_t = n.acctnm;
    });
  }
}

onMounted(async () => {
  // 초기 옵션 로드
  const p1 = api.post('/api/ha00/ha00_00p_str', { gubun: 'e0', gbncd: '070' })
  const p2 = api.post('/api/ha00/ha00_00p_str', { gubun: 'e0', gbncd: '090' })
  const [r1, r2] = await Promise.all([p1, p2])

  statementoptions.value = (r1.data || []).map((i: any) => normalizekeys(i)).filter((n: any) => n.codecd <= '030')
  calctypeoptions.value = (r2.data || []).map((i: any) => normalizekeys(i))

  if (maingridelement.value) {
    maingrid = new Tabulator(maingridelement.value, {
      layout: "fitColumns", height: "100%", selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: "코드", field: "acctcd", width: 100 },
        { title: "계정과목명", field: "acctnm", minWidth: 200, hozAlign: "left", cssClass: "fw-bold text-primary" },
        { title: "타입", field: "caltypenm", width: 120 },
        { title: "가감", field: "calgagamnm", width: 100 },
        { title: "사용", field: "useyn", width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
      ]
    });
    maingrid.on("rowClick", (e, row) => {
      const d = normalizekeys(row.getData());
      Object.assign(masterdata, d);
      masterdata.acctcd_t = d.acctnm;
      masterdata.actkind = 'u1';
    });
  }

  await fetchgroups();
})
</script>
