<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ?ì‚°?´ë ¥ê´€ë¦?(HAFA010U)
	?‘ì„±?¼ì	: 2025.02.24
	?¤ëª…        : ê³ ì •?ì‚° ì·¨ë“, ë³€?? ì²˜ë¶„ ???´ë ¥ ê´€ë¦?(ê¸ˆì•¡ ì½¤ë§ˆ ë°??ì—… ?ë™ ë§¤í•‘ ?ìš©)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- ?? 1. ?ë‹¨ ?¡ì…˜ ë°?-->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-pci-card me-2 text-primary" style="font-size: 18px;"></i>
        ê³ ì •?ì‚° <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        ?´ë ¥ê´€ë¦?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">?ì‚°?´ë ¥ê´€ë¦?(HAFA010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">ì´ˆê¸°??/button>
        <button class="btn-erp btn-search" @click="search">ì¡°íšŒ</button>
        <button class="btn-erp btn-save" @click="save">?€??/button>
        <button class="btn-erp btn-delete" @click="deleteData" :disabled="formData.actkind !== 'U'">?? œ</button>
      </div>
    </div>

    <!-- ?’¡ 2. ë©”ì¸ ì»¨í…ì¸??ì—­ -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [?ë‹¨] ì¡°íšŒ ?„í„° ?ì—­ -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 10%" /><col style="width: 40%" />
              <col style="width: 10%" /><col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">ê³„ì •ê³¼ëª©</th>
                <td>
                  <div class="input-group input-group-sm" style="width: 300px;">
                    <input v-model="searchForm.acctcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 65px;" readonly />
                    <input v-model="searchForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT', 'search')" placeholder="ê³„ì • ? íƒ" />
                    <button class="btn btn-outline-secondary" @click="openHelp('ACCT', 'search')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light">ë°œìƒ?¼ì</th>
                <td>
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="searchForm.fromdt" type="date" class="form-control form-control-sm" style="width: 140px;" />
                    <span class="text-muted small fw-bold">~</span>
                    <input v-model="searchForm.todt" type="date" class="form-control form-control-sm" style="width: 140px;" />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- ?“ ?ì„¸ ?•ë³´ ?…ë ¥ ??-->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-primary"></i>?ì‚° ?ì„¸ ?•ë³´ [{{ formData.actkind === 'A' ? '? ê·œ' : '?˜ì •' }}]</div>
          <div class="d-flex align-items-center gap-3">
            <div v-if="formData.actkind === 'U'" class="badge bg-primary-subtle text-primary border border-primary-subtle px-2" style="font-size: 10px;">?˜ì •ëª¨ë“œ</div>
            <div class="form-check form-switch m-0">
              <input v-model="formData.useyn" class="form-check-input" type="checkbox" id="useynCheck010" true-value="N" false-value="Y">
              <label class="form-check-label text-danger fw-bold small" for="useynCheck010">?? œ</label>
            </div>
          </div>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-full">
            <colgroup>
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
              <col style="width: 110px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light">ì²˜ë¦¬êµ¬ë¶„</th>
                <td>
                  <select v-model="formData.proctype" class="form-select form-select-sm">
                    <option v-for="item in proctypeOptions" :key="item.codecd" :value="item.codecd">{{ item.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light">?ì‚°ì½”ë“œ</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.asetcd" type="text" class="form-control text-center bg-light fw-bold" readonly />
                    <button class="btn btn-outline-secondary" @click="openHelp('ASET')" :disabled="formData.proctype === '010' || !formData.acctcd">
                      <i class="bi bi-search"></i>
                    </button>
                  </div>
                </td>
                <th class="required bg-light">?ì‚°ëª?/th>
                <td><input v-model="formData.asetnm" type="text" class="form-control form-control-sm" placeholder="?ì‚° ëª…ì¹­" /></td>
              </tr>
              <tr>
                <th class="required bg-light">ê³„ì •ê³¼ëª©</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.acctcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 65px;" readonly />
                    <input v-model="formData.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT', 'form')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('ACCT', 'form')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="required bg-light">ë°œìƒ??/th>
                <td><input v-model="formData.procymd" type="date" class="form-control form-control-sm" /></td>
                <th class="required bg-light">?˜ëŸ‰/ê¸ˆì•¡</th>
                <td>
                  <div class="d-flex gap-1 align-items-center">
                    <input v-model="formData.procqty" type="number" class="form-control form-control-sm text-end" style="width: 70px;" />
                    <input v-model="formattedProcamt" type="text" class="form-control form-control-sm text-end fw-bold text-primary" placeholder="0" @input="e => e.target.value = e.target.value.replace(/[^0-9,]/g, '')" />
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required bg-light">?ê°ë°©ë²•</th>
                <td>
                  <select v-model="formData.dprstype" class="form-select form-select-sm">
                    <option v-for="item in dprstypeOptions" :key="item.codecd" :value="item.codecd">{{ item.codenm }}</option>
                  </select>
                </td>
                <th class="required bg-light">?´ìš©?°ìˆ˜</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.legalyy" type="number" class="form-control form-control-sm text-end" />
                    <button class="btn btn-outline-secondary" @click="openHelp('rate')"><i class="bi bi-list-ul"></i></button>
                  </div>
                </td>
                <th class="required bg-light">?ê°??/th>
                <td>
                  <div class="d-flex gap-1 align-items-center px-1">
                    <input v-model="formData.asetrate" type="number" step="0.001" class="form-control form-control-sm text-end fw-bold" style="width: 100px;" />
                    <span class="small text-muted ms-1">ê°€ê°?</span>
                    <input v-model="formData.GAGAMyy" type="number" class="form-control form-control-sm text-end" style="width: 50px;" />
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required bg-light">ë¹„ìš©êµ¬ë¶„</th>
                <td>
                  <select v-model="formData.costtype" class="form-select form-select-sm">
                    <option v-for="item in costtypeOptions" :key="item.codecd" :value="item.codecd">{{ item.codenm }}</option>
                  </select>
                </td>
                <th class="required bg-light">ë³´ìœ ë¶€??/th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="formData.deptcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 65px;" readonly />
                    <input v-model="formData.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" />
                    <button class="btn btn-outline-secondary" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light">????/th>
                <td><input v-model="formData.remark" type="text" class="form-control form-control-sm" placeholder="ì°¸ì¡° ?¬í•­ ?…ë ¥" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [?˜ë‹¨] ê·¸ë¦¬???ì—­ -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <div class="fw-bold small text-dark"><i class="bi bi-list-ul me-2 text-primary"></i>?ì‚° ë³€???´ì—­ ë¦¬ìŠ¤??/div>
          <span class="text-muted small" style="font-size: 11px;">??ª© ?´ë¦­ ???˜ì • ëª¨ë“œë¡??„í™˜?©ë‹ˆ??</span>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const today = new Date().toISOString().slice(0, 10)
const firstDay = today.slice(0, 8) + '01'

const searchForm = reactive({ acctcd: '', acctnm: '', fromdt: firstDay, todt: today })
const formData = reactive({ actkind: 'A', ym: '', rowno: '', proctype: '010', asetcd: '', asetnm: '', acctcd: '', acctnm: '', procymd: today, procqty: 0, procamt: 0, dprstype: '010', legalyy: 0, GAGAMyy: 0, asetrate: 0, costtype: '010', deptcd: '', deptnm: '', remark: '', useyn: 'Y' })

const proctypeOptions = ref<any[]>([]); const dprstypeOptions = ref<any[]>([]); const costtypeOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

// ?°ì´???¤ë? ?Œë¬¸?ë¡œ ?•ê·œ??
const normalizeKeys = (data: any): any => {
  if (!data) return data;
  if (Array.isArray(data)) return data.map(item => normalizeKeys(item));
  if (typeof data !== 'object' || data instanceof Date) return data;
  const normalized: any = {};
  Object.keys(data).forEach(key => { normalized[key.toLowerCase().trim()] = normalizeKeys(data[key]); });
  return normalized;
};

// ê³µí†µ ì½”ë“œ ?œì? ë§¤í•‘ (codecd, codenm)
const mapToStandard = (data: any[]) => {
  return normalizeKeys(data || []).map((i: any) => ({
    codecd: i.codecd || i.code || '',
    codenm: i.codenm || i.cdnm || ''
  }));
};

// ê¸ˆì•¡ ì²œë‹¨??ì½¤ë§ˆ ?¬ë§·??Computed
const formattedProcamt = computed({
  get: () => {
    if (formData.procamt === undefined || formData.procamt === null || formData.procamt === '') return '';
    return formData.procamt.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  },
  set: (val: string) => {
    const num = Number(val.replace(/,/g, ''));
    formData.procamt = isNaN(num) ? 0 : num;
  }
});

function initialize() {
	Object.assign(formData, { actkind: 'A', ym: '', rowno: '', proctype: '010', asetcd: '', asetnm: '', acctcd: searchForm.acctcd, acctnm: searchForm.acctnm, procymd: today, procqty: 0, procamt: 0, dprstype: '010', legalyy: 0, GAGAMyy: 0, asetrate: 0, costtype: '010', deptcd: '', deptnm: '', remark: '', useyn: 'Y' })
}

const fetchOptions = async () => {
	try {
		const [resP, resD, resC] = await Promise.all([
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: ' ', gbncd: '260' }),
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: ' ', gbncd: '250' }),
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', cmpycd: ' ', gbncd: '110' })
		]);
		proctypeOptions.value = mapToStandard(resP.data);
		dprstypeOptions.value = mapToStandard(resD.data);
		costtypeOptions.value = mapToStandard(resC.data);
	} catch (e) { console.error('ì½”ë“œ ë¡œë“œ ?¤íŒ¨:', e) }
}

async function search() {
	if (!searchForm.acctcd) return vAlertError('ê³„ì •ê³¼ëª©??? íƒ?˜ì„¸??')
	try {
		const res = await api.post('/api/hafa/HAFA_010U_STR', { actkind: 'S', cmpycd: authStore.cmpycd, fromdt: searchForm.fromdt.replace(/-/g, ''), todt: searchForm.todt.replace(/-/g, ''), acctcd: searchForm.acctcd })
    const data = normalizeKeys(res.data || []).map((r: any) => ({
      ...r,
      procymd: r.procymd ? `${r.procymd.slice(0,4)}-${r.procymd.slice(4,6)}-${r.procymd.slice(6,8)}` : ''
    }));
		mainGrid?.setData(data);
    vAlert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??');
    initialize();
	} catch (e) { vAlertError('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

async function save() {
	if (!formData.acctcd) return vAlertError('ê³„ì •ê³¼ëª©??? íƒ?˜ì„¸??')
	if (formData.proctype === '010' && !formData.asetnm) return vAlertError('?ì‚°ëª…ì„ ?…ë ¥?˜ì„¸??')
	if (formData.proctype !== '010' && !formData.asetcd) return vAlertError('?ì‚°ì½”ë“œë¥?? íƒ?˜ì„¸??')
	try {
		const res = await api.post('/api/hafa/HAFA_010U_STR', { ...formData, cmpycd: authStore.cmpycd, procymd: formData.procymd.replace(/-/g, ''), userid: authStore.userid })
		if (res.data && res.data[0]?.col0 === 'Y') { vAlertError(res.data[0].col1) } else { vAlert('?€?¥ë˜?ˆìŠµ?ˆë‹¤.'); search() }
	} catch (e) { vAlertError('?€??ì¤??¤ë¥˜ ë°œìƒ') }
}

async function deleteData() {
	if (!confirm('?•ë§ ?? œ?˜ì‹œê² ìŠµ?ˆê¹Œ?')) return
	try {
		await api.post('/api/hafa/HAFA_010U_STR', { ...formData, actkind: 'D', cmpycd: authStore.cmpycd, userid: authStore.userid })
		vAlert('?? œ?˜ì—ˆ?µë‹ˆ??'); search()
	} catch (e) { vAlertError('?? œ ì¤??¤ë¥˜ ë°œìƒ') }
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string, target?: string) {
	if (type === 'ACCT') {
		Object.assign(modalProps, { title: 'ê³„ì •ê³¼ëª© ? íƒ', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'A8', cmpycd: authStore.cmpycd, gbncd: '020', code: '' }, columns: [{ title: 'ì½”ë“œ', field: 'acctcd', width: 80 }, { title: 'ê³„ì •ëª?, field: 'acctnm', width: 200 }],
			onConfirm: (rawData: any) => {
        const d = normalizeKeys(rawData);
        if (target === 'search') { searchForm.acctcd = d.acctcd; searchForm.acctnm = d.acctnm; search() } else { formData.acctcd = d.acctcd; formData.acctnm = d.acctnm }
      }
		})
	} else if (type === 'ASET') {
		if (!formData.acctcd) {
			vAlertError('ê³„ì •ê³¼ëª©??ë¨¼ì? ? íƒ??ì£¼ì„¸??');
			return;
		}
		Object.assign(modalProps, { title: '?ì‚° ? íƒ', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'J0', cmpycd: authStore.cmpycd, gbncd: formData.acctcd, code: formData.asetnm }, columns: [{ title: 'ì½”ë“œ', field: 'asetcd', width: 100 }, { title: '?ì‚°ëª?, field: 'asetnm', width: 200 }],
			onConfirm: (rawData: any) => {
        const d = normalizeKeys(rawData);
        Object.assign(formData, {
          asetcd: d.asetcd,
          asetnm: d.asetnm,
          legalyy: d.legalyy,
          deptcd: d.deptcd,
          deptnm: d.deptnm,
          costtype: d.costtype,
          dprstype: d.dprstype,
          asetrate: d.asetrate,
          procqty: d.pchqty,
          procamt: d.pchamt
        })
      }
		})
	} else if (type === 'DEPT') {
		Object.assign(modalProps, { title: 'ë¶€??? íƒ', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'D0', cmpycd: authStore.cmpycd, code: formData.deptnm }, columns: [{ title: 'ì½”ë“œ', field: 'deptcd', width: 80 }, { title: 'ë¶€?œëª…', field: 'deptnm', width: 180 }],
			onConfirm: (rawData: any) => {
        const d = normalizeKeys(rawData);
        formData.deptcd = d.deptcd; formData.deptnm = d.deptnm;
      }
		})
	} else if (type === 'rate') {
		Object.assign(modalProps, { title: '?ê°??? íƒ', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'J1', cmpycd: authStore.cmpycd, gbncd: formData.dprstype }, columns: [{ title: '?´ìš©?°ìˆ˜', field: 'legalyy', width: 100 }, { title: '?ê°??, field: 'asetrate', width: 100 }],
			onConfirm: (rawData: any) => {
        const d = normalizeKeys(rawData);
        formData.legalyy = d.legalyy; formData.asetrate = d.rate95;
      }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	fetchOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "ë°œìƒ??, field: "procymd", width: 120, hozAlign: "center" },
				{ title: "êµ¬ë¶„", field: "procnm", width: 100, hozAlign: "center", cssClass: "text-primary fw-bold" },
				{ title: "?ì‚°ëª?, field: "asetnm", minWidth: 200, cssClass: "fw-bold" },
				{ title: "ê¸ˆì•¡", field: "procamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "?ê°ë°©ë²•", field: "dprstypenm", width: 150, hozAlign: "center" },
				{ title: "?´ìš©?°ìˆ˜", field: "legalyy", width: 150, hozAlign: "center" },
				{ title: "?ê°??, field: "asetrate", width: 150, hozAlign: "center" },
				{ title: "ë³´ìœ ë¶€??, field: "deptnm", width: 200 },
                { title: "?íƒœ", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">?¬ìš©</b>' : '<span class="text-danger">?? œ</span>';
                  }
                }
			]
		})
		mainGrid.on("rowClick", (e, row) => {
      const d = normalizeKeys(row.getData());
      Object.assign(formData, d);
      formData.actkind = 'U'
    })
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
