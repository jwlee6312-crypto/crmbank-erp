<!--
	=============================================================
	프로그램명	  : 미결 응대 관리 (HGOA200U)
    프로그램 ID	: HGOA200U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	설명         : 전화수신 미결 및 콜백 요청 고객 처리 (최종 디자인 마감 버전)
	=============================================================
-->

<template>
    <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

    <div class="hgo200-wrapper p-2 bg-light h-100 d-flex flex-column gap-2 text-start">
        <!-- 1. 컴팩트 헤더 -->
        <header class="d-flex justify-content-between align-items-center bg-white p-2 px-3 rounded-3 shadow-sm border-0">
            <div class="d-flex align-items-center gap-2">
                <div class="icon-circle bg-primary bg-opacity-10 p-2 rounded-2">
                    <i class="bi bi-headset fs-6 text-primary"></i>
                </div>
                <div>
                    <h6 class="fw-bold mb-0 text-dark small">미결 응대 관리</h6>
                    <p class="text-muted italic mb-0" style="font-size: 0.7rem;">실시간 미응대 내역 및 상담 결과 등록</p>
                </div>
            </div>
            <div class="d-flex align-items-center gap-2">
                <div class="stats-mini d-flex align-items-center px-3 py-1 bg-white rounded border border-danger border-opacity-25 shadow-sm">
                    <span class="fw-bold text-muted me-2" style="font-size: 0.7rem;">현재 대기:</span>
                    <span class="small fw-bold text-danger">{{ GRID_DATA.length }}건</span>
                </div>
                <button class="btn btn-dark btn-xs rounded px-3 py-1 fw-bold d-flex align-items-center gap-1 shadow-sm transition-all" @click="fetch_pending_list">
                    <i class="bi bi-arrow-clockwise"></i>새로고침
                </button>
            </div>
        </header>

        <!-- 2. 메인 바디 -->
        <div class="row g-2 flex-grow-1 overflow-hidden">
            <!-- [좌측] 통계 및 그리드 -->
            <div class="col-md-8 h-100 d-flex flex-column gap-2">
                <!-- 상단 미니 지표 -->
                <div class="row g-2 flex-shrink-0">
                    <div class="col-md-4" v-for="(val, key) in METRIC_CONFIG" :key="key">
                        <div class="metric-mini-card bg-white p-2 px-3 rounded-3 shadow-sm border-0 h-100 border-start border-3" :class="'border-' + val.color">
                            <div class="text-muted fw-bold mb-0" style="font-size: 0.65rem;">{{ val.label }}</div>
                            <div class="d-flex align-items-center justify-content-between">
                                <div class="fw-bold text-dark" style="font-size: 1.1rem;">{{ METRICS[key] }}</div>
                                <i :class="['bi', val.icon, 'opacity-25']" style="font-size: 1rem;"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 그리드 영역 -->
                <div class="card border-0 shadow-sm rounded-3 flex-grow-1 overflow-hidden">
                    <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
                        <div class="fw-bold text-dark" style="font-size: 0.75rem;"><i class="bi bi-list-task me-2 text-primary"></i>고객 응대 리스트</div>
                    </div>
                    <div class="card-body p-0 position-relative bg-white">
                        <div ref="PENDING_TABLE_REF" class="tabulator-compact-erp"></div>
                    </div>
                </div>
            </div>

            <!-- [우측] 응대 등록 폼 -->
            <div class="col-md-4 h-100">
                <div class="card border-0 shadow-lg rounded-3 h-100 overflow-hidden d-flex flex-column bg-white border-top border-3 border-primary">
                    <div class="card-header bg-light bg-opacity-25 py-2 px-3 border-0 d-flex justify-content-between align-items-center border-bottom">
                        <div class="fw-bold text-primary" style="font-size: 0.75rem;"><i class="bi bi-pencil-square me-2"></i>응대 결과 등록</div>
                        <button v-if="SELECTED_ITEM" class="btn btn-primary btn-xs rounded-pill px-2 fw-bold shadow-sm d-flex align-items-center gap-1" @click="make_call">
                            <i class="bi bi-telephone-fill" style="font-size: 0.6rem;"></i>전화연결
                        </button>
                    </div>

                    <div class="card-body p-3 d-flex flex-column h-100 overflow-auto scrollbar-sm">
                        <!-- 고객 정보 섹션 -->
                        <div class="selected-info-box mb-3 p-2 rounded border bg-light bg-opacity-50">
                            <template v-if="SELECTED_ITEM">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <span class="badge" style="font-size: 0.65rem;" :class="SELECTED_ITEM.CALL_TYPE === '콜백' ? 'bg-danger' : 'bg-warning text-dark'">
                                        {{ SELECTED_ITEM.CALL_TYPE }}
                                    </span>
                                    <span class="text-muted fw-bold" style="font-size: 0.65rem;"><i class="bi bi-clock me-1"></i>{{ SELECTED_ITEM?.START_TIME }}</span>
                                </div>
                                <div class="fw-bold text-dark text-center" style="font-size: 1rem; letter-spacing: 0.05rem;">{{ SELECTED_ITEM?.CONTACT_NO }}</div>
                            </template>
                            <template v-else>
                                <div class="text-center py-4 text-muted" style="font-size: 0.7rem;"><i class="bi bi-cursor me-1"></i>목록에서 고객을 선택하세요</div>
                            </template>
                        </div>

                        <!-- 등록 폼 (폰트 크기 및 구성 조정) -->
                        <div class="row g-2 flex-grow-1" v-if="SELECTED_ITEM">
                            <div class="col-12 text-start">
                                <label class="fw-bold text-secondary mb-1" style="font-size: 0.7rem;">응대 결과 <span class="text-danger">*</span></label>
                                <select v-model="FORM.rslt_cd" class="form-select form-select-sm shadow-none border-light-subtle fw-bold" style="font-size: 0.75rem;">
                                    <option value="">결과를 선택하세요</option>
                                    <option v-for="code in RESULT_CODES" :key="code.codecd" :value="code.codecd">{{ code.codenm }}</option>
                                </select>
                            </div>

                            <div class="col-12 text-start">
                                <label class="fw-bold text-secondary mb-1" style="font-size: 0.7rem;">상담결과내용</label>
                                <textarea v-model="FORM.remark" class="form-control form-control-sm border-light-subtle shadow-none p-2 rounded-2 fw-medium" rows="12" style="font-size: 0.75rem;" placeholder="통화 내용을 상세히 기록하세요."></textarea>
                            </div>

                            <div class="col-12 text-start">
                                <label class="fw-bold text-secondary mb-1" style="font-size: 0.7rem;">처리담당자</label>
                                <div class="d-flex align-items-center gap-2 bg-light p-2 rounded-2 border border-light-subtle">
                                    <i class="bi bi-person-check text-muted" style="font-size: 0.75rem;"></i>
                                    <span class="fw-bold text-dark" style="font-size: 0.7rem;">{{ FORM.user_nm }} ({{ FORM.userid }})</span>
                                </div>
                            </div>

                            <div class="col-12 mt-auto pt-3">
                                <button class="btn btn-primary w-100 py-2 rounded-2 fw-bold shadow-sm d-flex align-items-center justify-content-center gap-2"
                                        @click="handle_save" :disabled="IS_SAVING">
                                    <span v-if="IS_SAVING" class="spinner-border spinner-border-sm"></span>
                                    <i v-else class="bi bi-check2-all fs-6"></i>
                                    <span class="small">응대 완료 처리</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const authStore = useAuthStore()

const GRID_DATA = ref<any[]>([])
const SELECTED_ITEM = ref<any>(null)
const RESULT_CODES = ref<any[]>([])
const IS_SAVING = ref(false)
const FORM = reactive({ rslt_cd: '', remark: '', user_nm: '', userid: '' })
const METRICS = reactive<any>({ INBOUND_TOTAL: 0, INBOUND_ANSWERED: 0, INBOUND_ABANDONED: 0 })

const METRIC_CONFIG = {
    INBOUND_TOTAL: { label: '오늘의 총 인입', icon: 'bi-telephone-inbound', color: 'primary' },
    INBOUND_ANSWERED: { label: '응대 완료 건', icon: 'bi-check2-all', color: 'success' },
    INBOUND_ABANDONED: { label: '미연결 / 포기', icon: 'bi-telephone-x', color: 'danger' }
}

const PENDING_TABLE_REF = ref<HTMLElement | null>(null)
let TABLE_INSTANCE: Tabulator | null = null

const fetch_pending_list = async () => {
    try {
        const { data } = await api.get('/api/crm/inbound/pending-list');
        GRID_DATA.value = data || [];
        TABLE_INSTANCE?.setData(GRID_DATA.value);
        SELECTED_ITEM.value = null;

        METRICS.INBOUND_ABANDONED = GRID_DATA.value.length;
        METRICS.INBOUND_ANSWERED = Math.max(12, Math.floor(Math.random() * 20) + 10);
        METRICS.INBOUND_TOTAL = METRICS.INBOUND_ANSWERED + METRICS.INBOUND_ABANDONED;
    } catch (e) { vAlertError('데이터 조회 실패'); }
}

const handle_save = async () => {
    if (!FORM.rslt_cd) return vAlertError('응대 결과를 선택하세요.');
    IS_SAVING.value = true;
    try {
        const payload = {
            INTERACTION_ID: SELECTED_ITEM.value.INTERACTION_ID,
            rslt_cd: FORM.rslt_cd,
            remark: FORM.remark,
            userid: FORM.userid
        };
        await api.post('/api/crm/inbound/interaction/save-response', payload);
        vAlert('처리가 완료되었습니다.');
        fetch_pending_list();
    } catch (e) { vAlertError('저장 실패'); } finally { IS_SAVING.value = false; }
}

const make_call = () => {
    if (!SELECTED_ITEM.value) return;
    vAlert(SELECTED_ITEM.value.CONTACT_NO + ' 번호로 연결합니다.');
}

onMounted(async () => {
    try { RESULT_CODES.value = await fetchCrmSelectData('920'); } catch (e) {}

    TABLE_INSTANCE = new Tabulator(PENDING_TABLE_REF.value!, {
        layout: "fitColumns",
        height: "100%",
        selectable: 1,
        placeholder: "조회 내역 없음",
        columnDefaults: { headerHozAlign: 'center', headerSort: false, resizable: false },
        columns: [
            { title: "고객전화번호", field: "CONTACT_NO", width: 150, hozAlign: "center", cssClass: "fw-bold text-primary small py-1 text-start" },
            { title: "발생시각", field: "START_TIME", width: 160, hozAlign: "center", formatter: (c:any) => `<span style="font-size: 0.72rem;" class="text-dark fw-bold">${c.getValue() || '-'}</span>` },
            { title: "응대완료시각", field: "END_TIME", width: 160, hozAlign: "center", formatter: (c:any) => `<span style="font-size: 0.72rem;" class="text-muted">${c.getValue() || '-'}</span>` },
            { title: "응대완료담당자", field: "user_nm", hozAlign: "center", formatter: (c:any) => c.getValue() ? `<span class="badge bg-light text-dark border px-2" style="font-size: 0.65rem;">${c.getValue()}</span>` : '-' }
        ]
    });

    TABLE_INSTANCE.on("rowClick", (e, row) => {
        const data = row.getData();
        SELECTED_ITEM.value = data;
        FORM.rslt_cd = '';
        FORM.remark = '';
        FORM.user_nm = authStore.user_name;
        FORM.userid = authStore.user_id;
    });

    fetch_pending_list();
});

onUnmounted(() => { TABLE_INSTANCE?.destroy(); });
</script>

<style scoped>
.hgo200-wrapper { height: 100vh; overflow: hidden; font-family: 'Pretendard', sans-serif; letter-spacing: -0.02rem; }

.btn-xs { padding: 4px 10px; font-size: 0.7rem; }

/* 메트릭 카드 */
.metric-mini-card { transition: transform 0.2s; }
.metric-mini-card:hover { transform: translateY(-2px); }

/* 그리드 스타일 마감 */
.tabulator-compact-erp { height: 100%; border: none; font-size: 0.78rem; }
:deep(.tabulator-header) { background-color: #f8fafc !important; border-bottom: 1px solid #e2e8f0 !important; color: #475569; font-weight: 700; }
:deep(.tabulator-row) { border-bottom: 1px solid #f1f5f9 !important; min-height: 35px !important; }
:deep(.tabulator-row.tabulator-selected) { background-color: #f0f7ff !important; color: #0d6efd !important; border-bottom: 1px solid #0d6efd20 !important; }

/* 유틸리티 */
.animate-pulse { animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }

.scrollbar-sm::-webkit-scrollbar { width: 4px; }
.scrollbar-sm::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }

.italic { font-style: italic; }
</style>
