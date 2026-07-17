<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?  : ÏßàÎ¨∏ Î∞??µÎ? ?±Î°ù (?åÎ¨∏???úÏ? ?ÅÏö©)
    ?ÑÎ°úÍ∑∏Îû® ID	: HGOA030U
	?ëÏÑ±?ºÏûê	    : 25.03.06
	?ëÏÑ±??      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

    <div class="hgo030-wrapper bg-light text-start p-2 h-100 d-flex flex-column">
        <!-- Ï°∞Ìöå Ï°∞Í±¥ -->
        <div class="card shadow-sm border-0 mb-1 flex-shrink-0">
            <div class="card-body p-1 px-3">
                <div class="row align-items-center g-2">
                    <div class="col-auto">
                        <span class="badge bg-primary px-2 py-1 small"><i class="bi bi-search me-1"></i>ÏßàÎ¨∏ Í≤Ä??/span>
                    </div>
                    <div class="col-4">
                        <input v-model="search_form.question" class="form-control form-control-sm" placeholder="ÏßàÎ¨∏ ?¥Ïö©???ÖÎ†•?òÏÑ∏??.." @keyup.enter="search" />
                    </div>
                    <div class="col-auto">
                        <button class="btn btn-sm btn-dark px-3 fw-bold" @click="search" style="height: 26px; font-size: 0.8rem;">Ï°∞Ìöå</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-1 mb-1 flex-shrink-0">
            <!-- [Ï¢åÏ∏°] ÏßàÎ¨∏ Í∏∞Î≥∏ ?ïÎ≥¥ -->
            <div class="col-lg-4 col-md-5">
                <div class="card shadow-sm border-0 h-100 border-top border-3 border-secondary">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom-0">
                        <i class="bi bi-info-circle-fill text-secondary me-1"></i>ÏßàÎ¨∏ Í∏∞Î≥∏ ?ïÎ≥¥
                    </div>
                    <div class="card-body p-2 pt-0">
                        <table class="table table-sm form-table mb-0">
                            <colgroup><col style="width: 30%" /><col style="width: 70%" /></colgroup>
                            <tbody>
                                <tr>
                                    <th class="required small">ÏßàÎ¨∏?¥Ïö©</th>
                                    <td>
                                        <textarea v-model="mst_form.question" class="form-control form-control-sm shadow-none" rows="3" style="font-size: 0.85rem;" placeholder="ÏßàÎ¨∏ Î¨∏Íµ¨ ?ÖÎ†•"></textarea>
                                    </td>
                                </tr>
                                <tr>
                                    <th class="small">?µÎ??†Ìòï</th>
                                    <td>
                                        <select v-model="mst_form.ans_tp" class="form-select form-select-sm shadow-none" @change="handle_type_change">
                                            <option value="010">Í∞ùÍ???(?†ÌÉù??</option>
                                            <option value="020">Ï£ºÍ???(?úÏà†??</option>
                                            <option value="030">?ºÌï©??(Í∞ùÍ?+Ï£ºÍ?)</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <th class="small">?ïÎ†¨?úÏÑú</th>
                                    <td>
                                        <input v-model="mst_form.dspord" type="text" maxlength="3" class="form-control form-control-sm w-50 shadow-none" />
                                    </td>
                                </tr>
                                <tr>
                                    <th class="small">?¨Ïö©?¨Î?</th>
                                    <td>
                                        <div class="form-check form-switch p-0 ps-5">
                                            <input class="form-check-input ms-0" type="checkbox" v-model="mst_form.useyn" true-value="Y" false-value="N">
                                            <label class="form-check-label small fw-bold ms-2" :class="is_used ? 'text-primary' : 'text-danger'">
                                                {{ is_used ? '?¨Ïö©?? : 'ÎØ∏ÏÇ¨?? }}
                                            </label>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- [?∞Ï∏°] ?µÎ? ?ÅÏÑ∏ Íµ¨ÏÑ± -->
            <div class="col-lg-8 col-md-7">
                <div class="card shadow-sm border-0 h-100 border-top border-3 border-primary">
                    <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between align-items-center border-bottom-0">
                        <span><i class="bi bi-check-square-fill text-primary me-1"></i>?µÎ? ?ÅÏÑ∏ Íµ¨ÏÑ±</span>
                        <div class="btn-group">
                            <button class="btn btn-xs btn-outline-primary px-2" @click="initialize">?†Í∑ú</button>
                            <button class="btn btn-xs btn-primary px-3 fw-bold" @click="save"><i class="bi bi-save me-1"></i>?Ä??/button>
                            <button class="btn btn-xs btn-outline-danger px-2" @click="delete_item">??†ú</button>
                        </div>
                    </div>
                    <div class="card-body p-2 pt-0">
                        <div class="answer-scroll-container border rounded overflow-auto bg-white" style="height: 185px;">
                            <table class="table table-sm table-hover mb-0 text-center align-middle dtl-grid-table">
                                <thead class="table-light sticky-top">
                                    <tr class="small text-muted">
                                        <th style="width: 40px;">No</th>
                                        <th>?µÎ? Î≥¥Í∏∞ ?¥Ïö© (?ÅÎã¥?êÏö© ?àÎ¨∏)</th>
                                        <th style="width: 60px;">Î∞∞Ï†ê</th>
                                        <th style="width: 60px;">?¨Ïö©</th>
                                        <th style="width: 80px;">ÏßÅÏ†ë?ÖÎ†•</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="(item, index) in dtl_list" :key="index">
                                        <td class="small fw-bold text-secondary">{{ index + 1 }}</td>
                                        <td class="p-0 px-1">
                                            <input v-model="item.ans_cont" class="form-control form-control-sm border-0 bg-transparent py-0 shadow-none"
                                                   :disabled="mst_form.ans_tp === '020' && index > 0" placeholder="?ÅÎã¥ ???∏Ï∂ú???µÎ? ?¥Ïö© ?ÖÎ†•" />
                                        </td>
                                        <td class="p-0">
                                            <input v-model.number="item.ans_point" type="number" class="form-control form-control-sm border-0 bg-transparent text-center py-0 shadow-none"
                                                   :disabled="mst_form.ans_tp === '020'" />
                                        </td>
                                        <td><input v-model="item.useyn" type="checkbox" true-value="Y" false-value="N" class="form-check-input shadow-none" /></td>
                                        <td><input v-model="item.essay_yn" type="checkbox" true-value="Y" false-value="N" class="form-check-input shadow-none" /></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ?òÎã® Î™©Î°ù -->
        <div class="card shadow-sm border-0 flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-dark text-white py-1 px-3 fw-bold small d-flex justify-content-between align-items-center">
                <span><i class="bi bi-collection-fill me-2 text-info"></i>ÏßàÎ¨∏ Î∞??µÎ? ?µÌï© ?∞Ïù¥?∞Î≤†?¥Ïä§</span>
            </div>
            <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                <div ref="table_ref" class="tabulator-full-height" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const search_form = reactive({ question: '' })
const mst_form = reactive({ cmpycd: '', surv_no: '', question: '', ans_tp: '010', dspord: '001', useyn: 'Y' })
const dtl_list = ref<any[]>([])

const is_used = computed(() => (mst_form.useyn || "").toString().toUpperCase() === 'Y')

function create_empty_dtl(ansNo: string) { return { ans_no: ansNo, ans_cont: '', ans_point: 0, useyn: 'Y', essay_yn: 'N' } }
function reset_dtl() { dtl_list.value = []; for (let i = 1; i <= 8; i++) dtl_list.value.push(create_empty_dtl(String(i).padStart(3, '0'))) }

const handle_type_change = () => { if (mst_form.ans_tp === '020') { reset_dtl(); dtl_list.value[0].ans_cont = '?êÏú† ?úÏà†???ëÎãµ'; dtl_list.value[0].essay_yn = 'Y'; } }

const table_ref = ref<HTMLDivElement | null>(null)
let table_instance: Tabulator | null = null

onMounted(() => { reset_dtl(); init_table(); search(); })
onUnmounted(() => { if (table_instance) table_instance.destroy(); })

function init_table() {
	if (!table_ref.value) return
    if (table_instance) table_instance.destroy();
	table_instance = new Tabulator(table_ref.value, {
		placeholder: '?±Î°ù??ÏßàÎ¨∏???ÜÏäµ?àÎã§.', layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 20,
		columns: [
            { title: "No", formatter: "rownum", hozAlign: "center", width: 50 },
            { title: "ÏßàÎ¨∏Î≤àÌò∏", field: "surv_no", hozAlign: "center", width: 100 },
            { title: "ÏßàÎ¨∏ ?¥Ïö©", field: "question", hozAlign: "left", formatter: (cell) => `<div class="fw-bold text-dark py-1">${cell.getValue() || ''}</div>` },
            { title: "?†Ìòï", field: "ans_tp", hozAlign: "center", width: 80, formatter: (cell) => cell.getValue() === '010' ? 'Í∞ùÍ?' : cell.getValue() === '020' ? 'Ï£ºÍ?' : '?ºÌï©' },
            { title: "?ïÎ†¨", field: "dspord", hozAlign: "center", width: 60 },
            { title: "?¨Ïö©", field: "useyn", width: 80, hozAlign: "center",
              formatter: (cell) => {
                const val = String(cell.getValue() || '').trim().toUpperCase();
                return val === 'Y' ? '<b class="text-primary">?¨Ïö©</b>' : '';
              }
            }        ]
	})
    table_instance.on("rowClick", (e, row) => load_detail(row.getData()));
}

async function search() {
	try {
		const { data } = await api.get('/crm/outbound/surv/mst/search', { params: search_form })
		table_instance?.setData(data)
	} catch (e) { vAlertError('Ï°∞Ìöå ?§Ìå®'); }
}

async function load_detail(data: any) {
    Object.assign(mst_form, data)
    try {
        const { data: details } = await api.get('/crm/outbound/surv/dtl/search', { params: { surv_no: mst_form.surv_no } });
        reset_dtl();
        if (details && details.length > 0) {
            details.forEach((d: any, idx: number) => {
                if (idx < 8) dtl_list.value[idx] = d;
            });
        }
    } catch (e) { vAlertError('?ÅÏÑ∏ Ï°∞Ìöå ?§Ìå®'); }
}

async function save() {
    if (!mst_form.question) return vAlertError('ÏßàÎ¨∏ ?¥Ïö©???ÖÎ†•?òÏÑ∏??');
    const valid_dtl = dtl_list.value.filter(item => (item.ans_cont || "").trim() !== '');

    const payload = {
        mst: mst_form,
        dtl: valid_dtl
    };

    try {
        await api.post('/crm/outbound/surv/save', payload);
        vAlert('?Ä?•Îêò?àÏäµ?àÎã§.'); initialize(); search();
    } catch (e) { vAlertError('?Ä???§Ìå®'); }
}

async function delete_item() {
    if (!mst_form.surv_no) return vAlertError('?Ä???†ÌÉù ?ÑÏöî');
    if (!confirm('??†ú?òÏãúÍ≤†Ïäµ?àÍπå?')) return;
    try {
        await api.post('/crm/outbound/surv/delete', { surv_no: mst_form.surv_no });
        vAlert('??†ú?òÏóà?µÎãà??'); initialize(); search();
    } catch (e) { vAlertError('??†ú ?§Ìå®'); }
}

function initialize() {
    Object.assign(mst_form, { cmpycd: '', surv_no: '', question: '', ans_tp: '010', dspord: '001', useyn: 'Y' });
    reset_dtl();
    table_instance?.deselectRow();
}
</script>

<style scoped>
.hgo030-wrapper { height: calc(100vh - 110px); }
.form-table th { background-color: #f8f9fa; font-size: 0.8rem; text-align: right; padding: 6px 12px; font-weight: bold; border: 1px solid #dee2e6; }
.form-table td { padding: 4px 8px; border: 1px solid #dee2e6; }
.tabulator-full-height { height: 100% !important; }
.btn-xs { padding: 2px 8px; font-size: 0.75rem; font-weight: bold; }
</style>
