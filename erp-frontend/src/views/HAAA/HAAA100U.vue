<template>
  <AppAlert :show="showalert" :error="showerror" :message="alertmessage" />
  <div class="erp-container d-flex flex-column h-100 bg-white">
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-people-fill me-2 text-primary" style="font-size: 18px;"></i>
        시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">사용자 관리 (haaa100u)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
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
                <th class="bg-light text-center" style="width: 120px;">검색어</th>
                <td>
                  <input v-model="searchform.qnm" type="text" class="form-control form-control-sm" placeholder="ID 또는 성명" @keydown.enter="search" style="max-width: 300px;" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center">사용자 ID</th>
                <td><input v-model="masterdata.userid" type="text" class="form-control form-control-sm fw-bold" :readonly="masterdata.actkind === 'U0'" /></td>
                <th class="required bg-light text-center">성 명</th>
                <td><input v-model="masterdata.usernm" type="text" class="form-control form-control-sm" /></td>
                <th class="bg-light text-center">사용여부</th>
                <td>
                  <div class="form-check form-switch m-0 d-flex justify-content-center">
                    <input v-model="masterdata.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N">
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridelement" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useAlerts } from '@/composables/useAlerts'
import { useFormReset } from '@/composables/useFormReset'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

const searchform = reactive({ qnm: '' })
const masterdata = reactive<any>({
  actkind: 'A0', cmpycd: authstore.cmpycd, userid: '', usernm: '', useyn: 'Y'
})

const gridelement = ref<HTMLDivElement | null>(null); let grid: Tabulator | null = null

const normalizekeys = (obj: any) => {
  const n: any = {}; if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

const search = async () => {
  try {
    const res = await api.post('/api/haaa/haaa_100u_str', { actkind: 'S0', cmpycd: authstore.cmpycd, usernm: searchform.qnm })
    const processed = (res.data || []).map((i: any) => normalizekeys(i));
    grid?.setData(processed)
  } catch (e) { valerterror('조회 실패') }
}

const save = async () => {
  if (!masterdata.userid || !masterdata.usernm) return valerterror('필수 항목을 입력하세요.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const res = await api.post('/api/haaa/haaa_100u_str', { ...masterdata })
    const resdata = normalizekeys(res.data?.[0]);
    if (resdata.result === 'Y') { valert('저장되었습니다.'); search(); initialize(); }
  } catch (e) { valerterror('저장 실패') }
}

const initialize = () => {
  resetform(masterdata); Object.assign(masterdata, { actkind: 'A0', cmpycd: authstore.cmpycd, useyn: 'Y' })
}

onMounted(() => {
  if (gridelement.value) {
    grid = new Tabulator(gridelement.value, {
      layout: "fitColumns", height: "100%", selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: "ID", field: "userid", width: 120 },
        { title: "사용자명", field: "usernm", minWidth: 200, hozAlign: "left" },
        { title: "사용", field: "useyn", width: 100, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
      ]
    });
    grid.on("rowClick", (e, row) => {
      Object.assign(masterdata, normalizekeys(row.getData()));
      masterdata.actkind = 'U0';
    });
  }
  nextTick(search);
})
</script>
