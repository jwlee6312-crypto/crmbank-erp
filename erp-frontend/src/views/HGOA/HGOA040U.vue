<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?  : ?§Î¨∏?†ÌòïÎ≥?ÏßàÎ¨∏ Îß§Ìïë (?åÎ¨∏???úÏ? ?ÅÏö©)
    ?ÑÎ°úÍ∑∏Îû® ID	: HGOA040U
	?ëÏÑ±?ºÏûê	    : 25.03.06
	?ëÏÑ±??      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

    <div class="hgo040-wrapper bg-light text-start p-2">
        <div class="d-flex justify-content-between align-items-center mb-1 bg-white p-1 px-3 rounded shadow-sm border border-secondary-subtle">
            <div class="fw-bold text-dark small">
                <i class="bi bi-diagram-3-fill text-primary me-2"></i>?§Î¨∏?†ÌòïÎ≥?ÏßàÎ¨∏ Íµ¨ÏÑ± Í¥ÄÎ¶?
            </div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-xs btn-outline-secondary px-2" @click="initialize">Ï¥àÍ∏∞??/button>
                <button class="btn btn-xs btn-dark px-3" @click="search_types">Ï°∞Ìöå</button>
                <button class="btn btn-xs btn-primary px-3 fw-bold" @click="save"><i class="bi bi-save me-1"></i>?Ä??/button>
                <button class="btn btn-xs btn-outline-danger px-2" @click="delete_mapping">?ÑÏ≤¥??†ú</button>
            </div>
        </div>

        <div class="row g-1 flex-grow-1 content-body overflow-hidden">
            <!-- [Ï¢åÏ∏°] ?§Î¨∏?†Ìòï Î™©Î°ù -->
            <div class="col-md-3 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-dark">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom">
                        <i class="bi bi-list-task me-1"></i>?§Î¨∏?†Ìòï (910)
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="type_list_ref" class="tabulator-custom" />
                    </div>
                </div>
            </div>

            <!-- [Ï§ëÏïô] ?†Ï†ï??ÏßàÎ¨∏ -->
            <div class="col-md-4 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-primary">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom d-flex justify-content-between align-items-center">
                        <span class="text-truncate">
                            <i class="bi bi-check-circle-fill text-primary me-1"></i>
                            <span v-if="selected_surv_gb_nm" class="text-primary fw-bold">[{{ selected_surv_gb_nm }}] </span>?†Ï†ï ÏßàÎ¨∏
                        </span>
                        <span class="badge bg-light text-dark border rounded-pill">{{ selected_count }}</span>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="selected_table_ref" class="tabulator-custom" />
                        <div class="mapping-control-buttons shadow-sm border rounded bg-white">
                            <button class="btn btn-sm btn-outline-secondary mb-1" @click="remove_selected" title="?†Ï†ï Ï∑®ÏÜå"><i class="bi bi-chevron-double-right"></i></button>
                            <button class="btn btn-sm btn-primary" @click="add_from_master" title="ÏßàÎ¨∏ Ï∂îÍ?"><i class="bi bi-chevron-double-left"></i></button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- [?∞Ï∏°] ?ÑÏ≤¥ ÏßàÎ¨∏ ÎßàÏä§??-->
            <div class="col-md-5 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-secondary text-start">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom">
                        <i class="bi bi-database-fill-gear me-1"></i>?ÑÏ≤¥ ÏßàÎ¨∏ ÎßàÏä§??
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="total_table_ref" class="tabulator-custom" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const selected_surv_gb = ref('')
const selected_surv_gb_nm = ref('')
const selected_count = ref(0)

const type_list_ref = ref<HTMLDivElement | null>(null)
const selected_table_ref = ref<HTMLDivElement | null>(null)
const total_table_ref = ref<HTMLDivElement | null>(null)

let type_list_instance: Tabulator | null = null
let selected_table_instance: Tabulator | null = null
let total_table_instance: Tabulator | null = null

onMounted(() => {
	init_tables()
    search_types()
    search_all_questions()
})
onUnmounted(() => {
    type_list_instance?.destroy();
    selected_table_instance?.destroy();
    total_table_instance?.destroy();
})

function init_tables() {
    if (type_list_instance) type_list_instance.destroy();
	type_list_instance = new Tabulator(type_list_ref.value!, {
		layout: 'fitColumns', selectable: 1, height: '100%',
		columns: [
            { title: 'ÏΩîÎìú', field: 'codecd', width: 80, hozAlign: 'center' },
            { title: '?†ÌòïÎ™?, field: 'codenm', hozAlign: 'left' }
        ]
	})
	type_list_instance.on("rowClick", (e, row) => {
        const data = row.getData()
		selected_surv_gb.value = data.codecd
        selected_surv_gb_nm.value = data.codenm
		search_selected_questions(data.codecd)
	})

    const q_cols = [
        { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false, width: 40 },
        { title: "Î≤àÌò∏", field: "surv_no", hozAlign: "center", width: 80 },
        { title: "ÏßàÎ¨∏?¥Ïö©", field: "question", hozAlign: "left", formatter: "textarea" },
        { title: "?†Ìòï", field: "ans_tp", hozAlign: "center", width: 70, formatter: (c) => c.getValue()==='010'?'Í∞ùÍ?':'Ï£ºÍ?' }
    ]

    if (selected_table_instance) selected_table_instance.destroy();
    selected_table_instance = new Tabulator(selected_table_ref.value!, {
        layout: 'fitColumns', height: '100%', selectable: true, movableRows: true,
        columns: [ { rowHandle:true, formatter:"handle", headerSort:false, frozen:true, width:30, minWidth:30 }, ...q_cols ]
    })
    selected_table_instance.on("dataChanged", () => { selected_count.value = selected_table_instance?.getData().length || 0 })

    if (total_table_instance) total_table_instance.destroy();
    total_table_instance = new Tabulator(total_table_ref.value!, {
        layout: 'fitColumns', height: '100%', selectable: true, columns: q_cols
    })
}

async function search_types() {
    try {
        const data = await fetchCrmSelectData('910')
        type_list_instance?.setData(data)
    } catch (e) { console.error('?§Î¨∏?†Ìòï Ï°∞Ìöå ?§Ìå®') }
}

async function search_all_questions() {
    try {
        const { data } = await api.get('/crm/outbound/surv/mst/search')
        total_table_instance?.setData(data)
    } catch (e) { console.error('ÏßàÎ¨∏ÎßàÏä§??Ï°∞Ìöå ?§Ìå®') }
}

async function search_selected_questions(surv_gb: string) {
    try {
        const { data } = await api.get('/crm/outbound/mapping/list', { params: { surv_gb: surv_gb } })
        selected_table_instance?.setData(data)
        selected_count.value = data.length
    } catch (e) { console.error('Îß§ÌïëÎ™©Î°ù Ï°∞Ìöå ?§Ìå®') }
}

function add_from_master() {
    if (!selected_surv_gb.value) return vAlertError('?§Î¨∏?†Ìòï??Î®ºÏ? ?†ÌÉù?òÏÑ∏??')
    const selected = total_table_instance?.getSelectedData()
    if (!selected || selected.length === 0) return
    const current_data = selected_table_instance?.getData() || []
    selected.forEach(item => {
        if (!current_data.find(c => c.surv_no === item.surv_no)) {
            selected_table_instance?.addRow({ ...item, sortcd: String(current_data.length + 1).padStart(3, '0') })
        }
    })
    total_table_instance?.deselectRow()
}

function remove_selected() {
    const selected_rows = selected_table_instance?.getSelectedRows()
    if (!selected_rows || selected_rows.length === 0) return
    selected_rows.forEach(row => row.delete())
}

async function save() {
    if (!selected_surv_gb.value) return vAlertError('?§Î¨∏?†Ìòï???†ÌÉù?¥Ï£º?∏Ïöî.')
    try {
        const payload = {
            surv_gb: selected_surv_gb.value,
            questions: selected_table_instance?.getData()
        }
        await api.post('/crm/outbound/mapping/save', payload)
        vAlert('?ïÏÉÅ?ÅÏúºÎ°??Ä?•Îêò?àÏäµ?àÎã§.')
        search_selected_questions(selected_surv_gb.value)
    } catch (e) { vAlertError('?Ä???§Ìå®') }
}

async function delete_mapping() {
    if (!selected_surv_gb.value) return vAlertError('?Ä?ÅÏùÑ ?†ÌÉù?òÏÑ∏??')
    if (!confirm('?ïÎßê ??†ú?òÏãúÍ≤†Ïäµ?àÍπå?')) return
    try {
        await api.post('/crm/outbound/mapping/delete', { surv_gb: selected_surv_gb.value })
        vAlert('??†ú?òÏóà?µÎãà??')
        initialize()
        search_types()
    } catch (e) { vAlertError('??†ú ?§Ìå®') }
}

function initialize() {
    selected_surv_gb.value = '';
    selected_surv_gb_nm.value = '';
    selected_count.value = 0;
    type_list_instance?.deselectRow();
    selected_table_instance?.clearData();
}
</script>

<style scoped>
.hgo040-wrapper { height: calc(100vh - 110px); display: flex; flex-direction: column; overflow: hidden; }
.content-body { flex-grow: 1; overflow: hidden; }
.tabulator-custom { height: 100%; border: 1px solid #dee2e6; font-size: 0.8rem; border-radius: 4px; }
.mapping-control-buttons { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); z-index: 100; display: flex; flex-direction: column; padding: 6px; gap: 8px; }
.btn-xs { padding: 2px 8px; font-size: 0.7rem; font-weight: bold; }
</style>
