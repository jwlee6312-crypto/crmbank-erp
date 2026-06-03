<!--
	=============================================================
	프로그램명	  : 캠페인 속성(JSON) 매핑 관리 (대문자 표준 적용)
    프로그램 ID	: HGOA060U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hgo060-wrapper bg-light text-start p-2">
        <!-- 1. 상단 툴바 -->
        <div class="d-flex justify-content-between align-items-center mb-2 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
            <div class="fw-bold text-dark small"><i class="bi bi-diagram-3-fill text-primary me-2"></i>캠페인 속성(JSON) 매핑 관리</div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-xs btn-dark px-3" @click="search">조회</button>
                <button class="btn btn-xs btn-primary px-3 fw-bold" @click="save"><i class="bi bi-save me-1"></i>저장</button>
            </div>
        </div>

        <div class="row g-2 content-body">
            <!-- 2. 좌측: 설문유형 목록 -->
            <div class="col-md-3 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column overflow-hidden border-top border-3 border-dark">
                    <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                        <span><i class="bi bi-tags-fill me-1"></i>설문유형 (910)</span>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="type_grid_ref" class="tabulator-full-height" />
                    </div>
                </div>
            </div>

            <!-- 3. 우측: 매핑 상세 설정 -->
            <div class="col-md-9 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column overflow-hidden border-top border-3 border-primary">
                    <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                        <span>
                            <i class="bi bi-list-check me-1"></i>
                            <span v-if="SELECTED_SURV_GB_NM" class="text-primary fw-bold">[{{ SELECTED_SURV_GB_NM }}] </span>
                            매핑 리스트 (엑셀 헤더 ↔ DB 표준키)
                        </span>
                        <button class="btn btn-xs btn-outline-primary" @click="add_row">+ 행추가</button>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="mapping_grid_ref" class="tabulator-full-height" />
                    </div>
                </div>
            </div>
        </div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { useAuthStore } from '@/stores/authStore'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const type_grid_ref = ref<HTMLDivElement | null>(null)
const mapping_grid_ref = ref<HTMLDivElement | null>(null)
let type_grid_instance: Tabulator | null = null
let mapping_grid_instance: Tabulator | null = null

const SELECTED_SURV_GB = ref('')
const SELECTED_SURV_GB_NM = ref('')

onMounted(async () => {
    init_grids()
    try {
        // 💡 대문자 코드 데이터 로드
        const codes = await fetchCrmSelectData('910')
        type_grid_instance?.setData(codes)
    } catch (e) { console.error('코드 로드 실패') }
})

const init_grids = () => {
    // 1. 좌측 설문유형 그리드 (대문자 필드)
    type_grid_instance = new Tabulator(type_grid_ref.value!, {
        layout: "fitColumns",
        height: "100%",
        selectable: 1,
        placeholder: "데이터 없음",
        columns: [
            { title: "코드", field: "codecd", width: 80, hozAlign: "center" },
            { title: "유형명", field: "codenm", hozAlign: "left" }
        ],
    })

    type_grid_instance.on("rowClick", (e, row) => {
        const data = row.getData()
        SELECTED_SURV_GB.value = data.codecd
        SELECTED_SURV_GB_NM.value = data.codenm
        search()
    })

    // 2. 우측 매핑 설정 그리드 (대문자 필드)
    mapping_grid_instance = new Tabulator(mapping_grid_ref.value!, {
        layout: "fitColumns",
        height: "100%",
        placeholder: "유형을 선택하세요",
        columns: [
            { title: "엑셀 헤더명 (한글)", field: "ATTR_NM", editor: "input", headerSort: false },
            { title: "DB 저장 키 (영문 표준)", field: "ATTR_KEY", editor: "input", headerSort: false },
            {
                title: "데이터 타입", field: "DATA_TYPE", editor: "list", width: 120,
                editorParams: { values: ["STRING", "NUMBER", "DATE"] }
            },
            { title: "통계활용", field: "STATS_YN", editor: "tickCross", width: 90, hozAlign: "center", formatter: "tickCross" },
            {
                formatter: "buttonCross", width: 40, hozAlign: "center", headerSort: false,
                cellClick: (e, cell) => {
                    if(confirm('이 매핑 항목을 삭제하시겠습니까?')) cell.getRow().delete()
                }
            }
        ]
    })
}

const search = async () => {
    if (!SELECTED_SURV_GB.value) return
    try {
        // 💡 대문자 파라미터 적용
        const { data } = await api.get('/crm/outbound/attr-mapper/list', {
            params: { cmpycd: authStore.cmpycd },
            params: { SURV_GB: SELECTED_SURV_GB.value },
        })
        console.log(data)
        mapping_grid_instance?.setData(data)
    } catch (e) { vAlertError('조회 실패') }
}

const add_row = () => {
    if (!SELECTED_SURV_GB.value) return vAlertError('먼저 설문유형을 선택하세요.')
    mapping_grid_instance?.addRow({
        SURV_GB: SELECTED_SURV_GB.value,
        DATA_TYPE: 'STRING',
        STATS_YN: 'Y'
    })
}

const save = async () => {
    if (!SELECTED_SURV_GB.value) return vAlertError('유형이 선택되지 않았습니다.')
    const data = mapping_grid_instance?.getData()

    // 유효성 검사 (대문자 필드)
    const invalid = data?.some(item => !item.ATTR_NM || !item.ATTR_KEY)
    if (invalid) return vAlertError('헤더명과 DB 키는 필수 입력입니다.')

    try {
        // 💡 대문자 페이로드 구성
        await api.post('/crm/outbound/attr-mapper/save', {
            SURV_GB: SELECTED_SURV_GB.value,
            LIST: data
        })
        vAlert('정상적으로 저장되었습니다.')
        search()
    } catch (e) { vAlertError('저장 중 오류가 발생했습니다.') }
}

function initialize() {
    SELECTED_SURV_GB.value = '';
    SELECTED_SURV_GB_NM.value = '';
    type_grid_instance?.deselectRow();
    mapping_grid_instance?.clearData();
}
</script>

<style scoped>
.hgo060-wrapper {
    height: calc(100vh - 110px);
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.content-body {
    flex-grow: 1;
    overflow: hidden;
    min-height: 0;
}

.tabulator-full-height { height: 100%; border-top: 1px solid #dee2e6; font-size: 0.8rem; }
.btn-xs { padding: 1px 8px; font-size: 0.75rem; font-weight: bold; }
</style>
