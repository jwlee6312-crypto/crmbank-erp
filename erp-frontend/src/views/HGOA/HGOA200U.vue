<!--
	=============================================================
	프로그램명	  : 미결 응대 관리 (HGOA200U - 대문자 표준 적용)
    프로그램 ID	: HGOA200U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	설명         : 인바운드/아웃바운드 지표 통합 및 미결 응대 관리
	=============================================================
-->

<template>
    <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
    
    <div class="hgo200-wrapper p-2 bg-light h-100 d-flex flex-column gap-1 text-start">
        <!-- 1. 상단 헤더: 제목 및 미결 현황 요약 -->
        <div class="d-flex justify-content-between align-items-center mb-1 bg-white p-3 rounded shadow-sm border border-primary border-opacity-10">
            <div class="d-flex align-items-center gap-3">
                <span class="fw-bold fs-5 text-dark"><i class="bi bi-headset me-2 text-primary"></i>미결 응대 관리</span>
                <div class="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 px-3 py-2">
                    <span class="small me-2">미응대 고객 합계:</span><span class="fw-bold fs-6">{{ GRID_DATA.length }}</span>
                </div>
            </div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-sm btn-outline-dark px-3" @click="fetch_pending_list">
                    <i class="bi bi-arrow-clockwise me-1"></i>실시간 새로고침
                </button>
            </div>
        </div>

        <!-- 2. 메인 콘텐츠 -->
        <div class="row g-2 flex-grow-1 overflow-hidden">
            <!-- [좌측] 지표 및 미응대 리스트 영역 -->
            <div class="col-md-8 h-100 d-flex flex-column gap-2">
                
                <!-- 실시간 지표 요약 -->
                <div class="row g-2 flex-shrink-0">
                    <div class="col-md-6">
                        <div class="card border-0 shadow-sm border-top border-3 border-warning h-100">
                            <div class="card-header bg-white py-1 px-3 fw-bold small d-flex justify-content-between">
                                <span>오늘의 인바운드 현황</span>
                                <span class="text-muted extra-small">Live</span>
                            </div>
                            <div class="card-body p-2 d-flex justify-content-around text-center align-items-center">
                                <div><div class="text-muted extra-small">인입</div><div class="fs-5 fw-bold text-dark">{{ METRICS.INBOUND_TOTAL }}</div></div>
                                <div class="vr opacity-25" style="height: 20px;"></div>
                                <div><div class="text-muted extra-small text-success">응대</div><div class="fs-5 fw-bold text-success">{{ METRICS.INBOUND_ANSWERED }}</div></div>
                                <div class="vr opacity-25" style="height: 20px;"></div>
                                <div><div class="text-muted extra-small text-danger">포기</div><div class="fs-5 fw-bold text-danger">{{ METRICS.INBOUND_ABANDONED }}</div></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="card border-0 shadow-sm border-top border-3 border-success h-100">
                            <div class="card-header bg-white py-1 px-3 fw-bold small d-flex justify-content-between">
                                <span>아웃바운드 캠페인 실적</span>
                                <span class="text-success extra-small">Performance</span>
                            </div>
                            <div class="card-body p-2 d-flex justify-content-around text-center align-items-center">
                                <div><div class="text-muted extra-small">지시</div><div class="fs-5 fw-bold text-dark">{{ METRICS.OUTBOUND_TOTAL }}</div></div>
                                <div class="vr opacity-25" style="height: 20px;"></div>
                                <div><div class="text-muted extra-small text-primary">성공</div><div class="fs-5 fw-bold text-primary">{{ METRICS.OUTBOUND_SUCCESS }}</div></div>
                                <div class="vr opacity-25" style="height: 20px;"></div>
                                <div><div class="text-muted extra-small">성공률</div><div class="fs-5 fw-bold text-dark">{{ METRICS.OUTBOUND_RATE }}%</div></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 미응대 리스트 -->
                <div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-dark">
                    <div class="card-header bg-white py-2 px-3 fw-bold small border-bottom d-flex justify-content-between">
                        <span><i class="bi bi-list-ul me-2 text-primary"></i>미응대 고객 목록</span>
                    </div>
                    <div class="card-body p-0 position-relative bg-white">
                        <div ref="PENDING_TABLE_REF" class="tabulator-custom"></div>
                    </div>
                </div>
            </div>

            <!-- [우측] 응대 결과 등록 폼 -->
            <div class="col-md-4 h-100 d-flex flex-column gap-2">
                <div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-primary shadow-lg">
                    <div class="card-header bg-white py-2 px-3 fw-bold small border-bottom d-flex justify-content-between align-items-center">
                        <span><i class="bi bi-pencil-square me-2 text-primary"></i>응대 결과 등록</span>
                        <button class="btn btn-xs btn-primary shadow-none" v-if="SELECTED_ITEM" @click="make_call">전화걸기</button>
                    </div>
                    
                    <div class="card-body p-4 bg-white d-flex flex-column h-100">
                        <div class="customer-info-box mb-4 p-3 rounded-3 border"
                             :class="SELECTED_ITEM ? 'bg-primary bg-opacity-10 border-primary border-opacity-25' : 'bg-light border-dashed'">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <span v-if="SELECTED_ITEM" class="badge" :class="SELECTED_ITEM.CALL_TYPE === '콜백' ? 'bg-danger' : 'bg-warning text-dark'">
                                    {{ SELECTED_ITEM.CALL_TYPE }}
                                </span>
                                <span class="extra-small text-muted">{{ SELECTED_ITEM?.START_TIME || '--:--' }}</span>
                            </div>
                            <h3 class="fw-bold mb-0" :class="SELECTED_ITEM ? 'text-dark' : 'text-muted opacity-50'">
                                {{ SELECTED_ITEM?.CONTACT_NO || '미선택 고객' }}
                            </h3>
                        </div>

                        <div class="row g-3 flex-grow-1">
                            <div class="col-12 text-start">
                                <label class="form-label small fw-bold text-secondary">응대 결과 <span class="text-danger">*</span></label>
                                <select v-model="FORM.RSLT_CD" class="form-select shadow-none border-primary" :disabled="!SELECTED_ITEM">
                                    <option value="">결과를 선택해 주세요</option>
                                    <option v-for="code in RESULT_CODES" :key="code.CODECD" :value="code.CODECD">{{ code.CODENM }}</option>
                                </select>
                            </div>
                            <div class="col-12 text-start">
                                <label class="form-label small fw-bold text-secondary">상담 요약 및 메모</label>
                                <textarea v-model="FORM.REMARK" class="form-control shadow-none" rows="10" placeholder="내용 입력" :disabled="!SELECTED_ITEM"></textarea>
                            </div>
                            <div class="col-12 mt-auto pt-3">
                                <button class="btn btn-primary w-100 py-3 fw-bold shadow-sm" @click="handle_save" :disabled="IS_SAVING || !SELECTED_ITEM">
                                    <span v-if="IS_SAVING">처리 중...</span>
                                    <span v-else>응대 완료 처리</span>
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
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

// 💡 지시하신 모든 변수명 대문자 통일
const GRID_DATA = ref<any[]>([])
const SELECTED_ITEM = ref<any>(null)
const RESULT_CODES = ref<any[]>([])
const IS_SAVING = ref(false)
const FORM = reactive({ RSLT_CD: '', REMARK: '' })
const METRICS = reactive({ INBOUND_TOTAL: 0, INBOUND_ANSWERED: 0, INBOUND_ABANDONED: 0, OUTBOUND_TOTAL: 0, OUTBOUND_SUCCESS: 0, OUTBOUND_RATE: 0 })

const PENDING_TABLE_REF = ref<HTMLElement | null>(null)
let TABLE_INSTANCE: Tabulator | null = null

const fetch_pending_list = async () => {
    try {
        const { data } = await api.get('/crm/inbound/pending-list');
        GRID_DATA.value = data;
        TABLE_INSTANCE?.setData(data);
        SELECTED_ITEM.value = null;
    } catch (e) { vAlertError('조회 실패'); }
}

const handle_save = async () => {
    if (!FORM.RSLT_CD) return vAlertError('응대 결과를 선택하세요.');
    IS_SAVING.value = true;
    try {
        const payload = { INTERACTION_ID: SELECTED_ITEM.value.INTERACTION_ID, ...FORM };
        await api.post('/crm/inbound/interaction/save-response', payload);
        vAlert('저장되었습니다.');
        fetch_pending_list();
    } catch (e) { vAlertError('저장 실패'); } finally { IS_SAVING.value = false; }
}

const make_call = () => { vAlert(SELECTED_ITEM.value.CONTACT_NO + ' 번호로 연결합니다.'); }

onMounted(async () => {
    try { RESULT_CODES.value = await fetchCrmSelectData('920'); } catch (e) {}
    
    TABLE_INSTANCE = new Tabulator(PENDING_TABLE_REF.value!, {
        layout: "fitColumns", height: "100%", selectable: 1, placeholder: "데이터 없음",
        columns: [
            { title: "구분", field: "CALL_TYPE", width: 100, hozAlign: "center", formatter: (c:any) => {
                const val = c.getValue();
                const color = val === '콜백' ? 'bg-danger' : 'bg-warning text-dark';
                return `<span class="badge ${color}">${val}</span>`;
            }},
            { title: "고객번호", field: "CONTACT_NO", width: 140, hozAlign: "center", cssClass: "fw-bold text-primary" },
            { title: "발생시각", field: "START_TIME", hozAlign: "center" },
            { title: "시도", field: "RETRY_CNT", width: 60, hozAlign: "center", formatter: (c:any) => c.getValue() + '회' }
        ]
    });
    
    TABLE_INSTANCE.on("rowClick", (e, row) => {
        SELECTED_ITEM.value = row.getData();
        FORM.RSLT_CD = ''; FORM.REMARK = '';
    });

    fetch_pending_list();
});
</script>

<style scoped>
.hgo200-wrapper { height: calc(100vh - 110px); }
.tabulator-custom { height: 100%; border-top: 1px solid #dee2e6; font-size: 0.85rem; }
.extra-small { font-size: 0.72rem; }
.btn-xs { padding: 2px 10px; font-size: 0.75rem; font-weight: bold; }
</style>
