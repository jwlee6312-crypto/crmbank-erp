<!--
	=============================================================
	프로그램명	  : 캠페인 자료생성 (대문자 표준 적용)
    프로그램 ID	: HGOA050U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
    <div class="hgo050-wrapper bg-light text-start p-2">
        <div class="row g-2 h-100 content-area">
            <!-- [좌측] 캠페인 목록 선택 -->
            <div class="col-md-3 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column overflow-hidden border-top border-3 border-dark">
                    <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                        <span><i class="bi bi-megaphone-fill me-1 text-primary"></i>대상 캠페인 선택</span>
                        <button class="btn btn-xs btn-outline-secondary" @click="load_campaigns"><i class="bi bi-arrow-clockwise"></i></button>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="CAMP_LIST_REF" class="tabulator-custom" />
                    </div>
                </div>
            </div>

            <!-- [우측] 자료 생성 및 확인 -->
            <div class="col-md-9 h-100 d-flex flex-column">
                <ul class="nav nav-tabs custom-tabs bg-white border-bottom-0 ps-2 flex-shrink-0">
                    <li class="nav-item">
                        <button class="nav-link" :class="{active: ACTIVE_TAB === 'CHECK'}" @click="handle_tab_change('CHECK')">업로드 자료 확인</button>
                    </li>
                    <li class="nav-item">
                        <button class="nav-link" :class="{active: ACTIVE_TAB === 'CREATE'}" @click="handle_tab_change('CREATE')">새 자료 만들기(Excel)</button>
                    </li>
                </ul>

                <div class="tab-content flex-grow-1 d-flex flex-column bg-white shadow-sm border rounded-bottom overflow-hidden">
                    <div class="sub-toolbar d-flex justify-content-between align-items-center p-2 bg-light border-bottom">
                        <div class="d-flex gap-1 align-items-center">
                            <button class="btn btn-xs btn-outline-danger px-3 fw-bold" @click="handle_delete" :disabled="!SELECTED_CAMP">선정삭제</button>
                            <div class="ms-3 d-flex align-items-center gap-2">
                                <span class="small fw-bold text-muted">선택 캠페인:</span>
                                <span class="badge bg-primary px-2" v-if="SELECTED_CAMP">{{ SELECTED_CAMP.CAMP_NM }}</span>
                                <span class="text-muted small" v-else>좌측에서 캠페인을 먼저 선택하세요.</span>
                            </div>
                        </div>
                        <div class="d-flex gap-2 align-items-center" v-if="ACTIVE_TAB === 'CHECK'">
                            <input type="text" v-model="SEARCH_KEYWORD" class="form-control form-control-sm" style="width: 180px;" placeholder="고객명/연락처/이메일" @keyup.enter="search_target_list" />
                            <button class="btn btn-xs btn-dark px-3" @click="search_target_list">검색</button>
                        </div>
                    </div>

                    <!-- 메인 그리드 영역 -->
                    <div class="flex-grow-1 position-relative border-bottom bg-white">
                        <div ref="MAIN_GRID_REF" class="tabulator-custom" />
                    </div>

                    <!-- 하단 액션바 (업로드 도구) -->
                    <div class="footer-action bg-light p-2 d-flex justify-content-center align-items-center gap-3 flex-shrink-0">
                        <button type="button" class="btn btn-xs btn-outline-dark px-3 fw-bold shadow-sm" @click="download_template">
                            <i class="bi bi-file-earmark-arrow-down me-1"></i>표준 양식 다운로드
                        </button>

                        <div class="input-group input-group-sm w-auto border rounded bg-white shadow-sm overflow-hidden">
                            <input type="file" class="form-control border-0" id="campaignFile" @change="on_file_change" accept=".xlsx, .xls" style="width: 350px;" />
                        </div>
                        
                        <button class="btn btn-xs btn-primary px-4 fw-bold shadow-sm" :disabled="!SELECTED_FILE || !SELECTED_CAMP" @click="handle_upload">
                            <i class="bi bi-cloud-upload me-1"></i>선택 캠페인으로 업로드 시작
                        </button>
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
import * as XLSX from 'xlsx'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

// 💡 상태 변수 대문자 통일
const ACTIVE_TAB = ref('CREATE')
const SELECTED_CAMP = ref<any>(null)
const SELECTED_FILE = ref<File | null>(null)
const SEARCH_KEYWORD = ref('')

const CAMP_LIST_REF = ref<HTMLDivElement | null>(null)
const MAIN_GRID_REF = ref<HTMLDivElement | null>(null)
let CAMP_LIST_INSTANCE: Tabulator | null = null
let MAIN_GRID_INSTANCE: Tabulator | null = null

onMounted(() => {
    init_camp_list_grid();
    init_main_grid([]);
    load_campaigns();
})

const init_camp_list_grid = () => {
    CAMP_LIST_INSTANCE = new Tabulator(CAMP_LIST_REF.value!, {
        placeholder: '데이터 없음', layout: 'fitColumns', height: '100%', selectable: 1,
        columns: [
            { title: "캠페인명", field: "CAMP_NM", hozAlign: "left" },
            { title: "번호", field: "CAMP_NO", hozAlign: "center", width: 80 }
        ]
    })
    CAMP_LIST_INSTANCE.on("rowClick", (e, row) => {
        SELECTED_CAMP.value = row.getData()
        if (ACTIVE_TAB.value === 'CHECK') search_target_list()
    })
}

const init_main_grid = (data: any[]) => {
    let columns: any[] = [
        { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false, width: 40 },
        { title: "고객명", field: "CUST_NM", hozAlign: "center", width: 100 },
        { title: "연락처", field: "TEL_NO", hozAlign: "center", width: 120 },
        { title: "이메일", field: "EMAIL", hozAlign: "center", width: 180 },
        { title: "상태", field: "STATUS", hozAlign: "center", width: 80 }
    ]

    // 💡 동적 컬럼 처리 (대문자 필드)
    if (data.length > 0 && data[0].EXT_DATA) {
        // 1. EXT_DATA가 문자열이라면 객체로 파싱 (이미 객체라면 그대로 사용)
        let extDataSample = data[0].EXT_DATA;
        if (typeof extDataSample === 'string') {
            try {
                extDataSample = JSON.parse(extDataSample);
            } catch (e) {
                console.error("EXT_DATA 파싱 실패:", e);
            }
        }

        // 2. 파싱된 객체의 키를 순회하며 컬럼 추가
        Object.keys(extDataSample).forEach(key => {
            columns.push({
                title: key,
                field: `EXT_DATA.${key}`,
                hozAlign: "left",
                width: 120
            });
        });
    }
    if (data.length > 0 && data[0].EXT_DATA) {
        Object.keys(data[0].EXT_DATA).forEach(key => {
            columns.push({ title: key, field: `EXT_DATA.${key}`, hozAlign: "left", width: 120 })
        })
    }

    if (MAIN_GRID_INSTANCE) MAIN_GRID_INSTANCE.destroy()
    MAIN_GRID_INSTANCE = new Tabulator(MAIN_GRID_REF.value!, {
        data: data, layout: "fitColumns", height: "100%", selectable: true, columns: columns
    })
}

const load_campaigns = async () => {
    try {
        const { data } = await api.get('/crm/outbound/camp-list')
        CAMP_LIST_INSTANCE?.setData(data)
    } catch (e) {}
}

const handle_tab_change = (tab: string) => {
    ACTIVE_TAB.value = tab
    if (tab === 'CHECK' && SELECTED_CAMP.value) search_target_list()
}

const search_target_list = async () => {
    if (!SELECTED_CAMP.value) return
    try {
        const { data } = await api.get('/crm/outbound/call-list', {
            params: { CAMP_NO: SELECTED_CAMP.value.CAMP_NO, KEYWORD: SEARCH_KEYWORD.value }
        })
        init_main_grid(data)
    } catch (e) { vAlertError('조회 실패') }
}

const download_template = () => {
    const headers = ['고객명', '연락처', '이메일', '거래처명', '가입상품', '비고']
    const testData = [['홍길동', '010-1111-2222', 'hong@test.com', '(주)하이온', '인터넷', '테스트']]
    const worksheet = XLSX.utils.aoa_to_sheet([headers, ...testData])
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, "TargetList")
    XLSX.writeFile(workbook, "캠페인_업로드_양식.xlsx")
}

const handle_upload = async () => {
    if (!SELECTED_FILE.value || !SELECTED_CAMP.value) return
    const reader = new FileReader()
    reader.onload = async (e: any) => {
        const workbook = XLSX.read(new Uint8Array(e.target.result), { type: 'array' })
        const json: any[] = XLSX.utils.sheet_to_json(workbook.Sheets[workbook.SheetNames[0]])
        const targetList = json.map(row => {
            const item: any = {
                CMPYCD: SELECTED_CAMP.value.CMPYCD,
                CAMP_NO: SELECTED_CAMP.value.CAMP_NO,
                SURV_GB: SELECTED_CAMP.value.SURV_GB,
                CUST_NM: row['고객명'],
                TEL_NO: row['연락처'],
                EMAIL: row['이메일'] || '',
                STATUS: '010',
                EXT_DATA: {}
            }
            Object.keys(row).forEach(key => {
                if (!['고객명', '연락처', '이메일'].includes(key)) {
                    item.EXT_DATA[key] = row[key];
                }
            })
            return item
        })
        try {
            await api.post('/crm/outbound/call-list/save', targetList)
            vAlert('정상적으로 업로드되었습니다.');
            ACTIVE_TAB.value = 'CHECK'; search_target_list();
        } catch (e) { vAlertError('업로드 실패'); }
    }
    reader.readAsArrayBuffer(SELECTED_FILE.value)
}

const handle_delete = async () => {
    const selectedData = MAIN_GRID_INSTANCE?.getSelectedData()
    if (!selectedData?.length) return vAlertError('삭제할 자료를 선택하세요.')
    if (!confirm('선택한 자료를 삭제하시겠습니까?')) return
    try {
        await api.post('/crm/outbound/call-list/delete', {
            CAMP_NO: SELECTED_CAMP.value.CAMP_NO,
            TARGETS: selectedData
        })
        vAlert('삭제되었습니다.'); search_target_list();
    } catch (e) { vAlertError('삭제 실패'); }
}

const on_file_change = (e: any) => { SELECTED_FILE.value = e.target.files[0] }
</script>

<style scoped>
.hgo050-wrapper { height: calc(100vh - 110px); display: flex; flex-direction: column; overflow: hidden; }
.content-area { flex-grow: 1; min-height: 0; }
.custom-tabs .nav-link { font-size: 0.85rem; font-weight: bold; padding: 8px 25px; border: 1px solid #dee2e6; background: #f8f9fa; border-radius: 8px 8px 0 0; }
.custom-tabs .nav-link.active { color: #0d6efd; background: #fff; border-top: 3px solid #0d6efd; }
.tabulator-custom { height: 100%; border-top: 1px solid #dee2e6; font-size: 0.85rem; }
.footer-action { border-top: 2px solid #dee2e6; background: #f1f3f5 !important; }
.btn-xs { padding: 2px 10px; font-size: 0.75rem; font-weight: bold; }
</style>
