<!--
	=============================================================
	프로그램명	: 재무제표설정 (haba020u)
	작성일자	: 2025.03.14
	설명        : 재무제표 양식 및 그룹 설정 (완전 소문자 원칙 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showalert" :error="showerror" :message="alertmessage" />
  <Modal v-model:visible="modalvisible" :modalProps="modalprops" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-list-columns-reverse me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">재무제표설정 (haba020u)</span>
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
                <th class="bg-light text-center" style="width: 120px;">재무제표</th>
                <td>
                  <select v-model="searchform.gubun" class="form-select form-select-sm" style="max-width: 200px;" @change="search">
                    <option v-for="opt in statementoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
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
                <th class="required bg-light text-center" style="width: 110px;">그룹코드</th>
                <td><input v-model="formdata.grpcd" type="text" class="form-control form-control-sm text-center fw-bold" maxlength="3" :readonly="formdata.actkind === 'u1'" /></td>
                <th class="required bg-light text-center" style="width: 110px;">그 룹 명</th>
                <td><input v-model="formdata.codenm" type="text" class="form-control form-control-sm" maxlength="30" /></td>
                <th class="bg-light text-center" style="width: 110px;">사용여부</th>
                <td>
                  <div class="form-check form-switch m-0 d-flex justify-content-center">
                    <input v-model="formdata.useyn" class="form-check-input" type="checkbox" true-value="y" false-value="n">
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

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

const searchform = reactive({ gubun: '010' })
const formdata = reactive<any>({
  actkind: 'i1', cmpycd: authstore.cmpycd, gubun: '010', grpcd: '', codenm: '', useyn: 'Y', dspord: '1'
})

const statementoptions = ref<any[]>([])
const maingridelement = ref<HTMLDivElement | null>(null)
let maingrid: Tabulator | null = null

const normalizekeys = (obj: any) => {
  const n: any = {}; if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

const search = async () => {
  try {
    const res = await api.post('/api/haba/haba_020u_str', { actkind: 'S2', cmpycd: authstore.cmpycd, gubun: searchform.gubun })
    const processed = (res.data || []).map((i: any) => normalizekeys(i));
    maingrid?.setData(processed);
  } catch (e) { valerterror('조회 실패') }
}

const save = async () => {
  if (!formdata.grpcd || !formdata.codenm) return valerterror('필수 항목을 입력하세요.');
  try {
    const res = await api.post('/api/haba/haba_020u_str', { ...formdata })
    const resdata = normalizekeys(res.data?.[0]);
    if (resdata.result === 'Y') { valert('저장되었습니다.'); search(); }
  } catch (e) { valerterror('저장 실패') }
}

const initialize = () => {
  resetform(formdata);
  Object.assign(formdata, { actkind: 'i1', cmpycd: authstore.cmpycd, gubun: searchform.gubun, useyn: 'Y', dspord: '1' });
}

onMounted(async () => {
  api.post('/api/ha00/ha00_00p_str', { gubun: 'e0', gbncd: '070' }).then(r => {
    statementoptions.value = (r.data || [])
      .map((i: any) => normalizekeys(i))
      .filter((n: any) => n.codecd <= '030')
      .map((n: any) => ({ codecd: n.codecd, codenm: n.codenm }));
  });

  if (maingridelement.value) {
    maingrid = new Tabulator(maingridelement.value, {
      layout: "fitColumns", height: "100%", selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: "코드", field: "grpcd", width: 100 },
        { title: "재무제표명", field: "codenm", minWidth: 200, hozAlign: "left", cssClass: "fw-bold text-primary" },
        { title: "순서", field: "dspord", width: 80 },
        { title: "사용", field: "useyn", width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
      ]
    });
    maingrid.on("rowClick", (e, row) => {
      Object.assign(formdata, normalizekeys(row.getData()));
      formdata.actkind = 'u1';
    });
  }
  nextTick(search);
})
</script>
