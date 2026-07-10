<!--
	=============================================================
	프로그램명	  : 내선번호(PJSIP) 관리 (소문자 표준 적용)
    프로그램 ID	: HGPA010U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
    <div class="hgpa010-wrapper bg-light text-start p-2 h-100">
        <!-- 1. 상단 툴바 -->
        <div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
            <div class="fw-bold text-dark small"><i class="bi bi-telephone-plus-fill me-2 text-primary"></i>교환기 내선번호(PJSIP) 설정</div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-sm btn-outline-secondary px-3" @click="initialize">초기화</button>
                <button class="btn btn-sm btn-dark px-3" @click="search">조회</button>
                <button class="btn btn-sm btn-outline-primary px-3" @click="add_row">행추가</button>
                <button class="btn btn-sm btn-primary px-4 fw-bold" @click="save"><i class="bi bi-save me-1"></i>저장</button>
                <button class="btn btn-sm btn-danger px-3" @click="delete_selected">삭제</button>
            </div>
        </div>

        <!-- 2. 검색 조건 -->
        <div class="card shadow-sm border-0 mb-1 flex-shrink-0">
            <div class="card-body p-2 px-3">
                <div class="row g-3 align-items-center">
                    <div class="col-auto">
                        <label class="small fw-bold me-2">내선번호:</label>
                        <input v-model="search_form.id" class="form-control form-control-sm d-inline-block w-auto" placeholder="번호 입력" @keyup.enter="search" />
                    </div>
                    <div class="col-auto">
                        <label class="small fw-bold me-2">상담원명:</label>
                        <input v-model="search_form.callerid" class="form-control form-control-sm d-inline-block w-auto" placeholder="이름 입력" @keyup.enter="search" />
                    </div>
                </div>
            </div>
        </div>

        <!-- 3. 메인 그리드 -->
        <div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-dark mt-1">
            <div class="card-header bg-white py-1 px-2 fw-bold small">내선번호 목록</div>
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
		placeholder: '데이터가 없습니다.',
        layout: 'fitColumns',
        selectable: true,
        height: '100%',
		columns: [
			{ formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", width: 40, headerSort: false },
			{ title: '내선번호', field: 'id', editor: 'input', hozAlign: 'center' },
			{ title: '비밀번호', field: 'password', editor: 'input', hozAlign: 'center' },
            { title: '상담원명(CID)', field: 'callerid', editor: 'input', hozAlign: 'left' },
			{ title: '컨텍스트', field: 'context', editor: 'input', hozAlign: 'center' },
			{ title: '전송방식', field: 'transport', editor: 'list',
                editorParams: { values: ["transport-udp", "transport-tcp"] }, hozAlign: 'center' },
			{ title: '동시접속', field: 'max_contacts', editor: 'number', hozAlign: 'center' },
			{ title: '녹취여부', field: 'record_yn', editor: 'list',
                editorParams: { values: { "Y": "사용", "N": "미사용" } }, hozAlign: 'center' },
			{ title: '코덱', field: 'allow', editor: 'input', hozAlign: 'center' },
		],
	})
    search()
}

async function search() {
	try {
        const { data } = await api.get('/api/crm/asterisk/pjsip/search', { params: search_form })
        // 💡 백엔드에서 소문자 키로 반환하므로 바로 세팅
        console.log(data)

        table_instance?.setData(data)
	} catch (error) { vAlertError('조회 실패') }
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
		vAlert('성공적으로 저장되었습니다.'); search()
	} catch (error) { vAlertError('저장 실패') }
}

async function delete_selected() {
	const selectedData = table_instance?.getSelectedData()
	if (!selectedData || selectedData.length === 0) return vAlertError('삭제할 행을 선택하세요.')
	if (!confirm('정말 삭제하시겠습니까?')) return
	try {
		await api.post('/api/crm/asterisk/pjsip/delete', selectedData)
		vAlert('삭제 완료'); search()
	} catch (error) { vAlertError('삭제 실패') }
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
