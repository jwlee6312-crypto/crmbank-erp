<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?  : ?´ì„ ë²ˆí˜¸(PJSIP) ê´€ë¦?(?Œë¬¸???œì? ?ìš©)
    ?„ë¡œê·¸ë¨ ID	: HGPA010U
	?‘ì„±?¼ì	    : 25.03.06
	?‘ì„±??      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
    <div class="hgpa010-wrapper bg-light text-start p-2 h-100">
        <!-- 1. ?ë‹¨ ?´ë°” -->
        <div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
            <div class="fw-bold text-dark small"><i class="bi bi-telephone-plus-fill me-2 text-primary"></i>êµí™˜ê¸??´ì„ ë²ˆí˜¸(PJSIP) ?¤ì •</div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-sm btn-outline-secondary px-3" @click="initialize">ì´ˆê¸°??/button>
                <button class="btn btn-sm btn-dark px-3" @click="search">ì¡°íšŒ</button>
                <button class="btn btn-sm btn-outline-primary px-3" @click="add_row">?‰ì¶”ê°€</button>
                <button class="btn btn-sm btn-primary px-4 fw-bold" @click="save"><i class="bi bi-save me-1"></i>?€??/button>
                <button class="btn btn-sm btn-danger px-3" @click="delete_selected">?? œ</button>
            </div>
        </div>

        <!-- 2. ê²€??ì¡°ê±´ -->
        <div class="card shadow-sm border-0 mb-1 flex-shrink-0">
            <div class="card-body p-2 px-3">
                <div class="row g-3 align-items-center">
                    <div class="col-auto">
                        <label class="small fw-bold me-2">?´ì„ ë²ˆí˜¸:</label>
                        <input v-model="search_form.id" class="form-control form-control-sm d-inline-block w-auto" placeholder="ë²ˆí˜¸ ?…ë ¥" @keyup.enter="search" />
                    </div>
                    <div class="col-auto">
                        <label class="small fw-bold me-2">?ë‹´?ëª…:</label>
                        <input v-model="search_form.callerid" class="form-control form-control-sm d-inline-block w-auto" placeholder="?´ë¦„ ?…ë ¥" @keyup.enter="search" />
                    </div>
                </div>
            </div>
        </div>

        <!-- 3. ë©”ì¸ ê·¸ë¦¬??-->
        <div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-dark mt-1">
            <div class="card-header bg-white py-1 px-2 fw-bold small">?´ì„ ë²ˆí˜¸ ëª©ë¡</div>
            <div class="card-body p-0 bg-white position-relative">
                <div ref="TABLE_REF" class="tabulator-full-height" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const search_form = reactive({ id: '', callerid: '' })
const TABLE_REF = ref<HTMLDivElement | null>(null)
let table_instance: Tabulator | null = null

onMounted(() => {
    nextTick(() => init_table())
})
onUnmounted(() => { if (table_instance) table_instance.destroy(); })

function init_table() {
	if (!TABLE_REF.value) return
    if (table_instance) table_instance.destroy();
	table_instance = new Tabulator(TABLE_REF.value, {
		placeholder: '?°ì´?°ê? ?†ìŠµ?ˆë‹¤.',
        layout: 'fitColumns',
        selectable: true,
        height: '100%',
		columns: [
			{ formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", width: 40, headerSort: false },
			{ title: '?´ì„ ë²ˆí˜¸', field: 'id', editor: 'input', hozAlign: 'center' },
			{ title: 'ë¹„ë?ë²ˆí˜¸', field: 'password', editor: 'input', hozAlign: 'center' },
            { title: '?ë‹´?ëª…(CID)', field: 'callerid', editor: 'input', hozAlign: 'left' },
			{ title: 'ì»¨í…?¤íŠ¸', field: 'context', editor: 'input', hozAlign: 'center' },
			{ title: '?„ì†¡ë°©ì‹', field: 'transport', editor: 'list',
                editorParams: { values: ["transport-udp", "transport-tcp"] }, hozAlign: 'center' },
			{ title: '?™ì‹œ?‘ì†', field: 'max_contacts', editor: 'number', hozAlign: 'center' },
			{ title: '?¹ì·¨?¬ë?', field: 'record_yn', editor: 'list',
                editorParams: { values: { "Y": "?¬ìš©", "N": "ë¯¸ì‚¬?? } }, hozAlign: 'center' },
			{ title: 'ì½”ë±', field: 'allow', editor: 'input', hozAlign: 'center' },
		],
	})
    search()
}

async function search() {
	try {
        const { data } = await api.get('/api/crm/asterisk/pjsip/search', { params: search_form })
        // ?’¡ ë°±ì—”?œì—???Œë¬¸???¤ë¡œ ë°˜í™˜?˜ë?ë¡?ë°”ë¡œ ?¸íŒ…
        console.log(data)

        table_instance?.setData(data)
	} catch (error) { vAlertError('ì¡°íšŒ ?¤íŒ¨') }
}

function add_row() {
	table_instance?.addRow({
		id: '', password: '', callerid: '', context: 'from-internal',
		transport: 'transport-udp', max_contacts: 1, record_yn: 'Y', allow: 'ulaw,alaw'
	}, true)
}

async function save() {
	try {
		await api.post('/api/crm/asterisk/pjsip/save', table_instance?.getData())
		vAlert('?±ê³µ?ìœ¼ë¡??€?¥ë˜?ˆìŠµ?ˆë‹¤.'); search()
	} catch (error) { vAlertError('?€???¤íŒ¨') }
}

async function delete_selected() {
	const selectedData = table_instance?.getSelectedData()
	if (!selectedData || selectedData.length === 0) return vAlertError('?? œ???‰ì„ ? íƒ?˜ì„¸??')
	if (!confirm('?•ë§ ?? œ?˜ì‹œê² ìŠµ?ˆê¹Œ?')) return
	try {
		await api.post('/api/crm/asterisk/pjsip/delete', selectedData)
		vAlert('?? œ ?„ë£Œ'); search()
	} catch (error) { vAlertError('?? œ ?¤íŒ¨') }
}

function initialize() {
	Object.assign(search_form, { id: '', callerid: '' })
	table_instance?.clearData()
}
</script>

<style scoped>
.hgpa010-wrapper { height: calc(100vh - 110px); display: flex; flex-direction: column; overflow: hidden; }
.tabulator-full-height { flex-grow: 1; border-top: 1px solid #dee2e6; font-size: 0.85rem; height: 100% !important; }
</style>
