<!--
	=============================================================
	프로그램명  : 내선번호(PJSIP) 관리
    프로그램 ID	: HGPA010U
	작성일자	    : 2025.03.14
	작성자      : AI Assistant
    설명        : 교환기 내선번호(PJSIP) 설정 및 관리 (표준 UI 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
    <div class="erp-container d-flex flex-column h-100 bg-white">
        <!-- 🚀 1. 상단 액션 바 -->
        <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
            <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
                <i class="bi bi-telephone-plus-fill me-2 text-primary" style="font-size: 18px;"></i>
                시스템관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
                통신관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
                <span class="text-primary fw-bolder">내선번호(PJSIP) 관리 (HGPA010U)</span>
            </div>
            <div class="btn-group-erp d-flex gap-1 pe-3">
                <button class="btn-erp btn-init" @click="initialize">초기화</button>
                <button class="btn-erp btn-search" @click="search">조회</button>
                <button class="btn-erp btn-add" @click="addRow">행추가</button>
                <button class="btn-erp btn-save" @click="save">저장</button>
                <button class="btn-erp btn-delete" @click="deleteSelected">삭제</button>
            </div>
        </div>

        <!-- 💡 2. 메인 컨텐츠 영역 -->
        <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">
            <!-- [상단] 조회 필터 -->
            <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
                <div class="card-body p-2 bg-white">
                    <div class="d-flex align-items-center flex-wrap gap-3 small">
                        <div class="d-flex align-items-center">
                            <span class="erp-label"><i class="bi bi-dot"></i>내선번호</span>
                            <input v-model="searchForm.id" class="form-control form-control-sm" style="width: 150px;" placeholder="번호 입력" @keyup.enter="search" />
                        </div>
                        <div class="d-flex align-items-center">
                            <span class="erp-label"><i class="bi bi-dot"></i>상담원명</span>
                            <input v-model="searchForm.callerid" class="form-control form-control-sm" style="width: 150px;" placeholder="이름 입력" @keyup.enter="search" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- [하단] 데이터 그리드 -->
            <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
                    <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 내선번호 목록</span>
                </div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                    <div ref="tableRef" class="tabulator-instance flex-grow-1" />
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
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const searchForm = reactive({ id: '', callerid: '' })
const tableRef = ref<HTMLDivElement | null>(null)
let tableInstance: Tabulator | null = null

// 데이터 키 표준화 (소문자 원칙)
const normalizeKeys = (obj: any) => {
  const n: any = {};
  if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

const initTable = () => {
	if (!tableRef.value) return
    if (tableInstance) tableInstance.destroy();
	tableInstance = new Tabulator(tableRef.value, {
		placeholder: '데이터가 없습니다.',
        layout: 'fitColumns',
        selectable: true,
        height: '100%',
        columnDefaults: { headerSort: false, headerHozAlign: 'center', hozAlign: 'center', vertAlign: 'middle' },
		columns: [
			{ formatter: "rowSelection", titleFormatter: "rowSelection", width: 40 },
			{ title: '내선번호', field: 'id', editor: 'input', width: 120, cssClass: 'fw-bold text-primary' },
			{ title: '비밀번호', field: 'password', editor: 'input', width: 120 },
            { title: '상담원명(CID)', field: 'callerid', editor: 'input', hozAlign: 'left', minWidth: 150 },
			{ title: '컨텍스트', field: 'context', editor: 'input', width: 150 },
			{ title: '전송방식', field: 'transport', editor: 'list', width: 150,
                editorParams: { values: ["transport-udp", "transport-tcp"] } },
			{ title: '동시접속', field: 'max_contacts', editor: 'number', width: 100 },
			{ title: '녹취여부', field: 'record_yn', editor: 'list', width: 100,
                editorParams: { values: { "Y": "사용", "N": "미사용" } },
                formatter: (cell) => cell.getValue() === 'Y' ? '사용' : '미사용'
            },
			{ title: '코덱', field: 'allow', editor: 'input', width: 150 },
		],
	})
}

async function search() {
	try {
        const { data } = await api.get('/api/crm/asterisk/pjsip/search', { params: searchForm })
        tableInstance?.setData((data || []).map((row: any) => normalizeKeys(row)))
        vAlert('조회되었습니다.')
	} catch (error) { vAlertError('조회 중 오류가 발생했습니다.') }
}

function addRow() {
	tableInstance?.addRow({
		id: '', password: '', callerid: '', context: 'from-internal',
		transport: 'transport-udp', max_contacts: 1, record_yn: 'Y', allow: 'ulaw,alaw'
	}, true)
}

async function save() {
    const data = tableInstance?.getData();
    if (!data || data.length === 0) return vAlertError('저장할 데이터가 없습니다.');
    if (!confirm('저장하시겠습니까?')) return

	try {
		await api.post('/api/crm/asterisk/pjsip/save', data)
		vAlert('성공적으로 저장되었습니다.');
        search()
	} catch (error) { vAlertError('저장 중 오류가 발생했습니다.') }
}

async function deleteSelected() {
	const selectedData = tableInstance?.getSelectedData()
	if (!selectedData || selectedData.length === 0) return vAlertError('삭제할 행을 선택하세요.')
	if (!confirm('정말로 삭제하시겠습니까?')) return
	try {
		await api.post('/api/crm/asterisk/pjsip/delete', selectedData)
		vAlert('삭제되었습니다.');
        search()
	} catch (error) { vAlertError('삭제 중 오류가 발생했습니다.') }
}

function initialize() {
	Object.assign(searchForm, { id: '', callerid: '' })
	tableInstance?.clearData()
}

onMounted(() => {
    nextTick(() => {
        initTable()
        search()
    })
})

onUnmounted(() => { if (tableInstance) tableInstance.destroy(); })
</script>

<style scoped>
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
