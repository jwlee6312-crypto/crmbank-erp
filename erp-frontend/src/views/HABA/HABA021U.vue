<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: ?¨Î¨¥?úÌëú ?òÏãù?∏Ïßë (HABA021U)
	?ëÏÑ±?ºÏûê	: 2025.03.14
	?§Î™Ö        : ?¨Î¨¥?úÌëú Íµ¨ÏÑ± ??™© Î∞?Í≥ÑÏÇ∞ ?òÏãù ?§Ï†ï (?êÎ≥∏ ASP Íµ¨ÏÑ±??Ï∂©Ïã§???¨ÌòÑ Î∞?HSOD100U ?îÏûê???úÏ? ?ÅÏö©)
	=============================================================
-->

<template>
  <AppAlert :show="showalert" :error="showerror" :message="alertmessage" />
  <Modal v-model:visible="modalvisible" :modalProps="modalprops" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- ?? 1. ?ÅÎã® ?°ÏÖò Î∞?-->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
        Í∏∞Î≥∏?ïÎ≥¥ <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">?¨Î¨¥?úÌëú ?òÏãù?∏Ïßë (HABA021U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="frmReset">?†Í∑ú</button>
        <button class="btn-erp btn-search" @click="fetchleftgrid">Ï°∞Ìöå</button>
        <button class="btn-erp btn-save" @click="frmSubmit">?Ä??/button>
      </div>
    </div>

    <!-- ?í° 2. Î©îÏù∏ Ïª®ÌÖêÏ∏??ÅÏó≠ -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [?ÅÎã®] Search ?ÑÌÑ∞ ?ÅÏó≠ (ASP Name="Search" Ï§Ä?? -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-2 bg-white">
          <table class="erp-table-dense w-100">
            <tbody>
              <tr>
                <th class="bg-light text-center" style="width: 100px;">?ÅÏö©?∞ÎèÑ</th>
                <td style="width: 150px;">
                  <select v-model="searchform.yyyy" class="form-select form-select-sm fw-bold text-center" @change="fetchleftgrid">
                    <option v-for="y in yearoptions" :key="y" :value="y">{{ y }}??/option>
                  </select>
                </td>
                <th class="bg-light text-center border-start" style="width: 100px;">?úÎ¨¥?úÌëú</th>
                <td style="width: 200px;">
                  <select v-model="searchform.gubun" class="form-select form-select-sm" @change="fetchleftgrid">
                    <option v-for="opt in statementoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <td class="px-3">
                    <button class="btn btn-sm btn-secondary px-3" @click="fetchleftgrid"><i class="bi bi-search me-1"></i>Ï°∞Ìöå</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [Ï§ëÍ∞Ñ] frmBody ?ÖÎ†• ?ÅÏó≠ (ASP Name="frmBody" Ï§Ä?? -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 120px;" /><col style="width: 38%;" />
              <col style="width: 120px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="bg-light text-center">?úÎ¨¥?úÌëú</th>
                <td class="bg-light-yellow">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="searchform.yyyy" type="text" class="form-control form-control-sm text-center bg-light-fix fw-bold" style="max-width: 80px;" readonly />
                    <span class="small me-1">??/span>
                    <select v-model="searchform.gubun" class="form-select form-select-sm" disabled>
                      <option v-for="opt in statementoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                    </select>
                  </div>
                </td>
                <th class="bg-light text-center border-start">ÏßëÍ≥ÑÍ≥ÑÏ†ï</th>
                <td class="bg-light-yellow">
                  <div class="d-flex align-items-center gap-1">
                    <input v-model="masterdata.upacct" type="text" class="form-control form-control-sm text-center bg-light-fix fw-bold" style="max-width: 100px;" readonly />
                    <input v-model="masterdata.upacct_t" type="text" class="form-control form-control-sm bg-light-fix flex-grow-1" readonly />
                  </div>
                </td>
              </tr>
              <tr>
                <th class="bg-light text-center">Í≥ÑÏ†ïÍ≥ºÎ™©</th>
                <td class="bg-light-yellow">
                  <div class="input-group input-group-sm">
                    <input v-model="masterdata.acctcd" type="text" class="form-control text-center bg-light-fix fw-bold" style="max-width: 80px;" readonly />
                    <input v-model="masterdata.acctcd_t" type="text" class="form-control" placeholder="Í≥ÑÏ†ïÎ™??ÖÎ†• ?êÎäî Í≤Ä?? @keydown.enter="openhelp('acct')" />
                    <button class="btn btn-outline-secondary px-2" @click="openhelp('acct')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light text-center border-start">?∞ÏÇ∞?Ä??/th>
                <td class="bg-light-yellow">
                  <select v-model="masterdata.caltype" class="form-select form-select-sm">
                    <option value="000">?†ÌÉù</option>
                    <option v-for="opt in calctypeoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="bg-light text-center">?∞ÏÇ∞?òÏãù</th>
                <td class="bg-light-yellow">
                  <select v-model="masterdata.calgagam" class="form-select form-select-sm">
                    <option value="000">?†ÌÉù</option>
                    <option v-for="opt in formulaoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start">?¨Ïö©?¨Î?</th>
                <td class="bg-light-yellow">
                  <div class="form-check form-check-inline m-0 ms-2 d-flex align-items-center h-100">
                    <input v-model="masterdata.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useSwitch021">
                    <label class="form-check-label ms-2 small fw-bold" for="useSwitch021">?¨Ïö©</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [?òÎã®] Grid ?ÅÏó≠ (2Î∂ÑÌï†) -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ‚¨ÖÔ∏è Ï¢åÏ∏°: iFrame1 ( Master List ) -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden" style="width: 40%;">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
            <i class="bi bi-list-ul me-2 text-secondary"></i>
            <span class="fw-bold small text-dark">?¨Î¨¥?úÌëú ÏßëÍ≥Ñ??™©</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="leftgridref" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ?°Ô∏è ?∞Ï∏°: iFrame2 ( Detail List ) -->
        <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
            <i class="bi bi-table me-2 text-primary"></i>
            <span class="fw-bold small text-dark">?∏Î? Íµ¨ÏÑ± ??™© Î¶¨Ïä§??/span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="rightgridref" class="tabulator-instance flex-grow-1"></div>
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
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()
const { modalVisible: modalvisible, modalProps: modalprops, openHelp: commonopenhelp } = useCommonHelp()

// [1] ?∞Ïù¥??Î™®Îç∏Îß?
const yearoptions = ref<string[]>([])
const searchform = reactive({ yyyy: '', gubun: '010' })

const masterdata = reactive<any>({
  actkind: 'I1', cmpycd: authstore.cmpycd, yyyy: '', gubun: '',
  upacct: '', upacct_t: '', acctcd: '', acctcd_t: '', arowno: '',
  caltype: '000', calgagam: '000', useyn: 'Y', updyn: 'Y'
})

const statementoptions = ref<any[]>([]); const calctypeoptions = ref<any[]>([]); const formulaoptions = ref<any[]>([])
const leftgridref = ref<HTMLDivElement | null>(null); const rightgridref = ref<HTMLDivElement | null>(null)
let leftgrid: Tabulator | null = null; let rightgrid: Tabulator | null = null

const normalizekeys = (obj: any) => {
  const n: any = {}; if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

// [2] Ï£ºÏöî ?®Ïàò
const fetchleftgrid = async () => {
    if (!searchform.yyyy) return;
	try {
		const res = await api.post('/api/haba/HABA_021U_STR', { actkind: 'S3', cmpycd: authstore.cmpycd, yyyy: searchform.yyyy, gubun: searchform.gubun })
		leftgrid?.setData((res.data || []).map((r: any) => normalizekeys(r)));
        rightgrid?.clearData();
        frmReset();
	} catch (e) { valerterror('ÏßëÍ≥Ñ??™© Ï°∞Ìöå ?§Ìå®') }
}

const fetchrightgrid = async (row: any) => {
	try {
		const res = await api.post('/api/haba/HABA_021U_STR', { actkind: 'SR', cmpycd: authstore.cmpycd, yyyy: searchform.yyyy, gubun: row.gubun, upacct: row.upacct })
		rightgrid?.setData((res.data || []).map((r: any) => {
            const n = normalizekeys(r);
            return {
                ...n,
                caltype_nm: n.caltypenm,
                calgagam_nm: n.calgagamnm
            }
        }));
	} catch (e) { valerterror('?ÅÏÑ∏ ?¥Ïó≠ Ï°∞Ìöå ?§Ìå®') }
}

const frmSubmit = async () => {
	if (masterdata.updyn !== 'Y') return valerterror('?òÏ†ï?????ÜÏäµ?àÎã§.')
	if (!searchform.gubun || searchform.gubun === '0') return valerterror('?¨Î¨¥?úÌëú Ï¢ÖÎ•òÎ•??†ÌÉù?¥Ï£º??ãú??')
	if (!masterdata.upacct) return valerterror('ÏßëÍ≥ÑÍ≥ÑÏ†ï???†ÌÉù?¥Ï£º??ãú??')
	if (!masterdata.acctcd) return valerterror('Í≥ÑÏ†ïÏΩîÎìúÎ•??†ÌÉù?¥Ï£º??ãú??')
	if (masterdata.caltype === '000') return valerterror('?∞ÏÇ∞?Ä?ÅÏùÑ ?†ÌÉù??Ï£ºÏã≠?úÏöî.')
	if (masterdata.calgagam === '000') return valerterror('?∞ÏÇ∞?òÏãù???†ÌÉù??Ï£ºÏã≠?úÏöî.')

	try {
		masterdata.yyyy = searchform.yyyy; masterdata.gubun = searchform.gubun
		const res = await api.post('/api/haba/HABA_021U_SAVE', { ...masterdata, cmpycd: authstore.cmpycd, userid: authstore.userid })
        const resdata = normalizekeys(res.data?.[0]);
		if (resdata.ret_yn === 'N') { valerterror(resdata.ret_msg) }
        else { valert('?Ä?•Îêò?àÏäµ?àÎã§.'); fetchrightgrid({ gubun: masterdata.gubun, upacct: masterdata.upacct }); frmReset(); }
	} catch (e) { valerterror('?Ä???§Ìå®') }
}

const frmReset = () => {
    const curupacct = masterdata.upacct; const curupacctt = masterdata.upacct_t; const curupdyn = masterdata.updyn;
    resetform(masterdata);
    Object.assign(masterdata, {
        actkind: 'I1', cmpycd: authstore.cmpycd, useyn: 'Y',
        upacct: curupacct, upacct_t: curupacctt, updyn: curupdyn,
        caltype: '000', calgagam: '000'
    });
}

function openhelp(type: string) {
    if (type === 'acct') {
        commonopenhelp('ACCT', (d) => {
            const n = normalizekeys(d);
            masterdata.acctcd = n.acctcd; masterdata.acctcd_t = n.acctnm;
        }, { gubun: searchform.gubun });
    }
}

// [3] Ï¥àÍ∏∞??
onMounted(async () => {
    await nextTick();

    // Grids Ï¥àÍ∏∞??
    if (leftgridref.value) {
        leftgrid = new Tabulator(leftgridref.value, {
            layout: 'fitColumns', height: '100%', selectable: 1,
            columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
            columns: [
                { title: "Í≥ÑÏ†ïÏΩîÎìú", field: "upacct", width: 100, cssClass: "fw-bold text-primary border-end" },
                {
                    title: "Í≥???Î™?, field: "acctnm", hozAlign: "left", widthGrow: 1,
                    formatter: (cell) => {
                        const d = normalizekeys(cell.getData()); const code = String(d.upacct);
                        // ASP Indent Logic ?¨ÌòÑ
                        let indent = 0;
                        if (code.substring(1, 7) === "000000" || ["1990000", "2980000", "3980000", "3990000"].includes(code)) { indent = 0; }
                        else if (code.substring(2, 7) === "00000") { indent = 10; }
                        else if (code.substring(3, 7) === "0000") { indent = 20; }
                        else if (code.substring(5, 7) === "00") { indent = 25; }
                        else { indent = 30; }

                        const color = d.inyn === 'Y' ? '#0d6efd' : '#212529';
                        return `<div style="padding-left: ${indent}px; color: ${color};">${indent > 0 ? '¬∑ ' : ''}${cell.getValue()}</div>`
                    }
                },
                { title: "Íµ¨Î∂Ñ", field: "gubun_nm", width: 100 }
            ]
        });
        leftgrid.on('rowClick', (e, row) => {
            const d = normalizekeys(row.getData());
            masterdata.upacct = d.upacct; masterdata.upacct_t = d.acctnm; masterdata.updyn = d.updyn;
            fetchrightgrid(d); frmReset();
        });
    }

    if (rightgridref.value) {
        rightgrid = new Tabulator(rightgridref.value, {
            layout: 'fitColumns', height: '100%', selectable: 1,
            columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
            columns: [
                { title: "Í≥ÑÏ†ïÍ≥ºÎ™©", field: "acctcd", width: 100, cssClass: "fw-bold text-primary border-end" },
                { title: "Í≥ÑÏ†ïÎ™?, field: "acctnm", minWidth: 150, widthGrow: 1, hozAlign: "left" },
                { title: "?∞ÏÇ∞?Ä??, field: "caltype_nm", width: 100 },
                { title: "?∞ÏÇ∞?òÏãù", field: "calgagam_nm", width: 100 },
                { title: "?¨Ïö©", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">?¨Ïö©</b>' : '';
                  }
                }
            ]
        });
        rightgrid.on("rowClick", (e, row) => {
            const d = normalizekeys(row.getData()); Object.assign(masterdata, d);
            masterdata.acctcd_t = d.acctnm; masterdata.actkind = 'U1';
        });
    }

    // Ï¥àÍ∏∞ ?∞Ïù¥??Î°úÎìú
	try {
        const yRes = await api.post('/api/haba/HABA_021U_STR', { actkind: 'S4', cmpycd: authstore.cmpycd });
        if (yRes.data && yRes.data.length > 0) {
            yearoptions.value = yRes.data.map((r: any) => String(normalizekeys(r).yyyy));
            searchform.yyyy = String(normalizekeys(yRes.data[0]).yyyy);
        }

		const resgbn = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '070', cmpycd: authstore.cmpycd })
		statementoptions.value = (resgbn.data || []).map((r: any) => normalizekeys(r))
        if (statementoptions.value.length > 0) searchform.gubun = statementoptions.value[0].codecd;

		const restype = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '080', cmpycd: authstore.cmpycd })
		calctypeoptions.value = (restype.data || []).map((r: any) => normalizekeys(r))

		const resformula = await api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '090', cmpycd: authstore.cmpycd })
		formulaoptions.value = (resformula.data || []).map((r: any) => normalizekeys(r))

        nextTick(() => fetchleftgrid());
	} catch (e) { console.error('Ï¥àÍ∏∞ ?∞Ïù¥??Î°úÎìú ?§Ìå®') }
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.tabulator-row-selected) { background-color: #e7f1ff !important; }
.bg-light-yellow { background-color: #f9f6e7 !important; }
.bg-light-fix { background-color: #f8f9fa !important; }
</style>
