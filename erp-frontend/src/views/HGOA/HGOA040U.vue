<!--
	=============================================================
	프로그램명	  : 설문유형별 질문 매핑 (대문자 표준 및 MSSQL 통합판)
    프로그램 ID	: HGOA040U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

    <div class="hgo040-wrapper bg-light text-start p-2">
        <div class="d-flex justify-content-between align-items-center mb-1 bg-white p-1 px-3 rounded shadow-sm border border-secondary-subtle">
            <div class="fw-bold text-dark small">
                <i class="bi bi-diagram-3-fill text-primary me-2"></i>설문유형별 질문 구성 관리
            </div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-xs btn-outline-secondary px-2" @click="initialize">초기화</button>
                <button class="btn btn-xs btn-dark px-3" @click="search_types">조회</button>
                <button class="btn btn-xs btn-primary px-3 fw-bold" @click="save"><i class="bi bi-save me-1"></i>저장</button>
                <button class="btn btn-xs btn-outline-danger px-2" @click="delete_mapping">전체삭제</button>
            </div>
        </div>

        <div class="row g-1 flex-grow-1 content-body overflow-hidden">
            <!-- [좌측] 설문유형 목록 -->
            <div class="col-md-3 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-dark">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom">
                        <i class="bi bi-list-task me-1"></i>설문유형 (910)
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="TYPE_LIST_REF" class="tabulator-custom" />
                    </div>
                </div>
            </div>

            <!-- [중앙] 선정된 질문 -->
            <div class="col-md-4 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-primary">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom d-flex justify-content-between align-items-center">
                        <span class="text-truncate">
                            <i class="bi bi-check-circle-fill text-primary me-1"></i>
                            <span v-if="SELECTED_SURV_GB_NM" class="text-primary fw-bold">[{{ SELECTED_SURV_GB_NM }}] </span>선정 질문
                        </span>
                        <span class="badge bg-light text-dark border rounded-pill">{{ SELECTED_COUNT }}</span>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="SELECTED_TABLE_REF" class="tabulator-custom" />
                        <div class="mapping-control-buttons shadow-sm border rounded bg-white">
                            <button class="btn btn-sm btn-outline-secondary mb-1" @click="remove_selected" title="선정 취소"><i class="bi bi-chevron-double-right"></i></button>
                            <button class="btn btn-sm btn-primary" @click="add_from_master" title="질문 추가"><i class="bi bi-chevron-double-left"></i></button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- [우측] 전체 질문 마스터 -->
            <div class="col-md-5 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-secondary text-start">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom">
                        <i class="bi bi-database-fill-gear me-1"></i>전체 질문 마스터
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="TOTAL_TABLE_REF" class="tabulator-custom" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const SELECTED_SURV_GB = ref('')
const SELECTED_SURV_GB_NM = ref('')
const SELECTED_COUNT = ref(0)

const TYPE_LIST_REF = ref<HTMLDivElement | null>(null)
const SELECTED_TABLE_REF = ref<HTMLDivElement | null>(null)
const TOTAL_TABLE_REF = ref<HTMLDivElement | null>(null)

let TYPE_LIST_INSTANCE: Tabulator | null = null
let SELECTED_TABLE_INSTANCE: Tabulator | null = null
let TOTAL_TABLE_INSTANCE: Tabulator | null = null

onMounted(() => {
	init_tables()
    search_types()
    search_all_questions()
})

function init_tables() {
	TYPE_LIST_INSTANCE = new Tabulator(TYPE_LIST_REF.value!, {
		layout: 'fitColumns', selectable: 1, height: '100%',
		columns: [
            { title: '코드', field: 'CODECD', width: 80, hozAlign: 'center' },
            { title: '유형명', field: 'CODENM', hozAlign: 'left' }
        ]
	})
	TYPE_LIST_INSTANCE.on("rowClick", (e, row) => {
        const data = row.getData()
		SELECTED_SURV_GB.value = data.CODECD
        SELECTED_SURV_GB_NM.value = data.CODENM
		search_selected_questions(data.CODECD)
	})

    const q_cols = [
        { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false, width: 40 },
        { title: "번호", field: "SURV_NO", hozAlign: "center", width: 80 },
        { title: "질문내용", field: "QUESTION", hozAlign: "left", formatter: "textarea" },
        { title: "유형", field: "ANS_TP", hozAlign: "center", width: 70, formatter: (c) => c.getValue()==='010'?'객관':'주관' }
    ]

    SELECTED_TABLE_INSTANCE = new Tabulator(SELECTED_TABLE_REF.value!, {
        layout: 'fitColumns', height: '100%', selectable: true, movableRows: true,
        columns: [ { rowHandle:true, formatter:"handle", headerSort:false, frozen:true, width:30, minWidth:30 }, ...q_cols ]
    })
    SELECTED_TABLE_INSTANCE.on("dataChanged", () => { SELECTED_COUNT.value = SELECTED_TABLE_INSTANCE?.getData().length || 0 })

    TOTAL_TABLE_INSTANCE = new Tabulator(TOTAL_TABLE_REF.value!, {
        layout: 'fitColumns', height: '100%', selectable: true, columns: q_cols
    })
}

async function search_types() {
    try {
        const data = await fetchCrmSelectData('910')
        TYPE_LIST_INSTANCE?.setData(data)
    } catch (e) { console.error('설문유형 조회 실패') }
}

async function search_all_questions() {
    try {
        const { data } = await api.get('/crm/outbound/surv/mst/search')
        TOTAL_TABLE_INSTANCE?.setData(data)
    } catch (e) { console.error('질문마스터 조회 실패') }
}

async function search_selected_questions(SURV_GB: string) {
    try {
        const { data } = await api.get('/crm/outbound/mapping/list', { params: { SURV_GB: SURV_GB } })
        SELECTED_TABLE_INSTANCE?.setData(data)
        SELECTED_COUNT.value = data.length
    } catch (e) { console.error('매핑목록 조회 실패') }
}

function add_from_master() {
    if (!SELECTED_SURV_GB.value) return vAlertError('설문유형을 먼저 선택하세요.')
    const selected = TOTAL_TABLE_INSTANCE?.getSelectedData()
    if (!selected || selected.length === 0) return
    const current_data = SELECTED_TABLE_INSTANCE?.getData() || []
    selected.forEach(item => {
        if (!current_data.find(c => c.SURV_NO === item.SURV_NO)) {
            SELECTED_TABLE_INSTANCE?.addRow({ ...item, SORTCD: String(current_data.length + 1).padStart(3, '0') })
        }
    })
    TOTAL_TABLE_INSTANCE?.deselectRow()
}

function remove_selected() {
    const selected_rows = SELECTED_TABLE_INSTANCE?.getSelectedRows()
    if (!selected_rows || selected_rows.length === 0) return
    selected_rows.forEach(row => row.delete())
}

async function save() {
    if (!SELECTED_SURV_GB.value) return vAlertError('설문유형을 선택해주세요.')
    try {
        const payload = {
            SURV_GB: SELECTED_SURV_GB.value,
            QUESTIONS: SELECTED_TABLE_INSTANCE?.getData()
        }
        await api.post('/crm/outbound/mapping/save', payload)
        vAlert('정상적으로 저장되었습니다.')
        search_selected_questions(SELECTED_SURV_GB.value)
    } catch (e) { vAlertError('저장 실패') }
}

async function delete_mapping() {
    if (!SELECTED_SURV_GB.value) return vAlertError('대상을 선택하세요.')
    if (!confirm('정말 삭제하시겠습니까?')) return
    try {
        await api.post('/crm/outbound/mapping/delete', { SURV_GB: SELECTED_SURV_GB.value })
        vAlert('삭제되었습니다.')
        initialize()
        search_types()
    } catch (e) { vAlertError('삭제 실패') }
}

function initialize() {
    SELECTED_SURV_GB.value = '';
    SELECTED_SURV_GB_NM.value = '';
    SELECTED_COUNT.value = 0;
    TYPE_LIST_INSTANCE?.deselectRow();
    SELECTED_TABLE_INSTANCE?.clearData();
}
</script>

<style scoped>
.hgo040-wrapper { height: calc(100vh - 110px); display: flex; flex-direction: column; overflow: hidden; }
.content-body { flex-grow: 1; overflow: hidden; }
.tabulator-custom { height: 100%; border: 1px solid #dee2e6; font-size: 0.8rem; border-radius: 4px; }
.mapping-control-buttons { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); z-index: 100; display: flex; flex-direction: column; padding: 6px; gap: 8px; }
.btn-xs { padding: 2px 8px; font-size: 0.7rem; font-weight: bold; }
</style>
