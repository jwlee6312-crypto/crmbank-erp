<!--
	=============================================================
	프로그램명	  : 다이얼플랜 관리 (대문자 표준 적용)
    프로그램 ID	: HGPA030U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
    <div class="hgpa030-wrapper bg-light text-start p-2 h-100">
        <!-- 1. 상단 메인 툴바 -->
        <div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
            <div class="fw-bold text-dark small"><i class="bi bi- Mortarboard-fill me-2 text-primary"></i>다이얼플랜 관리 (Dialplan Management)</div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-sm btn-outline-secondary px-3" @click="initialize">초기화</button>
                <button class="btn btn-sm btn-dark px-3" @click="search">조회</button>
                <button class="btn btn-sm btn-primary px-4 fw-bold" @click="save"><i class="bi bi-save me-1"></i>저장</button>
                <button class="btn btn-sm btn-danger px-3" @click="delete_selected">삭제</button>
            </div>
        </div>

        <!-- 2. 검색 조건 (대문자 변수) -->
        <div class="card shadow-sm border-0 mb-1 flex-shrink-0">
            <div class="card-body p-2 px-3">
                <div class="row g-3 align-items-center">
                    <div class="col-auto">
                        <label class="small fw-bold me-2 text-muted">컨텍스트:</label>
                        <input v-model="search_form.context" class="form-control form-control-sm d-inline-block w-auto" placeholder="Context" @keyup.enter="search" />
                    </div>
                    <div class="col-auto">
                        <label class="small fw-bold me-2 text-muted">번호:</label>
                        <input v-model="search_form.exten" class="form-control form-control-sm d-inline-block w-auto" placeholder="exten" @keyup.enter="search" />
                    </div>
                </div>
            </div>
        </div>

        <!-- 3. 메인 그리드 및 가이드 -->
        <div class="row g-1 flex-grow-1 overflow-hidden mt-1">
            <div class="col-md-9 h-100 d-flex flex-column">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column overflow-hidden border-top border-3 border-dark">
                    <div class="card-header bg-white py-1 px-2 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                        <span>Extensions List</span>
                        <button class="btn btn-xs btn-outline-primary fw-bold py-0" @click="add_row">행추가</button>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative" style="min-height: 500px;">
                        <div ref="table_ref" class="tabulator-full-height" />
                    </div>
                </div>
            </div>

            <div class="col-md-3 h-100 overflow-auto">
                <div class="card shadow-sm border-0 h-100 border-top border-3 border-info">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom">가이드</div>
                    <div class="card-body p-2 bg-white small text-start">
                        <p class="mb-1 text-primary fw-bold">내선 통화 (Dial)</p>
                        <code class="d-block mb-2 p-1 bg-light border text-dark">app: Dial, data: pjsip/101</code>
                        <p class="mb-1 text-primary fw-bold">안내 재생 (Playback)</p>
                        <code class="d-block mb-2 p-1 bg-light border text-dark">app: Playback, data: custom/welcome</code>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

// 💡 상태 변수 대문자 통일
const search_form = reactive({ context: '', exten: '' })
const table_ref = ref<HTMLDivElement | null>(null)
let table_instance: Tabulator | null = null

onMounted(() => {
    nextTick(() => init_table())
})
onUnmounted(() => { if (table_instance) table_instance.destroy(); })

function init_table() {
	if (!table_ref.value) return
    if (table_instance) table_instance.destroy();
	table_instance = new Tabulator(table_ref.value, {
		placeholder: '데이터가 없습니다.',
		layout: 'fitColumns',
		selectable: true,
		height: '100%',
		columns: [
			{ formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", width: 40, headerSort: false },
            // 💡 필드명 대문자 적용
			{ title: '컨텍스트', field: 'context', editor: 'input' },
			{ title: '번호 (Exten)', field: 'exten', editor: 'input' },
			{ title: '순위', field: 'priority', editor: 'number', width: 60, hozAlign: 'center' },
			{ title: '명령어', field: 'app', editor: 'input' },
			{ title: '데이터', field: 'appdata', editor: 'input' },
		],
	})
    search()
}

async function search() {
	try {
		const { data } = await api.get('/api/crm/asterisk/extension/search', { params: search_form })
        if (Array.isArray(data)) {
            await table_instance?.setData(data)
            if(data.length > 0) vAlert(`${data.length}건이 조회되었습니다.`)
        }
	} catch (error) { vAlertError('조회 실패') }
}

function add_row() {
    table_instance?.addRow({
        context: 'from-internal',
        exten: '',
        priority: 1,
        app: '',
        appdata: ''
    }, true)
}

async function save() {
    const data = table_instance?.getData()
    try {
        await api.post('/api/crm/asterisk/extension/save', data);
        vAlert('성공적으로 저장되었습니다.');
        search();
    } catch (e) { vAlertError('저장 실패') }
}

async function delete_selected() {
    const selectedData = table_instance?.getSelectedData();
    if (!selectedData || selectedData.length === 0) return vAlertError('삭제할 행을 선택하세요.');
    if (confirm('정말 삭제하시겠습니까?')) {
        try {
            await api.post('/api/crm/asterisk/extension/delete', selectedData);
            vAlert('삭제 완료');
            search();
        } catch (e) { vAlertError('삭제 실패') }
    }
}

function initialize() {
    Object.assign(search_form, { context: '', exten: '' });
    table_instance?.clearData();
    vAlert('초기화 완료');
}
</script>

<style scoped>
.hgpa030-wrapper { height: calc(100vh - 110px); display: flex; flex-direction: column; overflow: hidden; }
.tabulator-full-height { flex-grow: 1; border-top: 1px solid #dee2e6; font-size: 0.85rem; height: 100% !important; }
</style>
