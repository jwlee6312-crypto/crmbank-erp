<!--
	=============================================================
	프로그램명	  : 캠페인 옴니채널 상담 워크스페이스
    프로그램 ID	: HGOA100U
	작성일자	    : 2026.05.19
	작성자	      : AI Assistant
	Description	: 캠페인 고객 상담 및 결과 저장 (상담원/내선/점수 연동 마감)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="omni-workspace bg-light position-relative text-start">
		<header class="omni-header d-flex align-items-center justify-content-between px-3 shadow-sm bg-white border-bottom py-2">
			<div class="d-flex align-items-center gap-2 flex-grow-1 overflow-hidden">
				<div class="campaign-title-area d-flex align-items-center flex-shrink-0">
					<i class="bi bi-megaphone-fill text-primary me-2 fs-5"></i>
					<select v-model="SELECTED_CAMP_NO" class="form-select form-select-sm fw-bold border-0 bg-light shadow-none" style="min-width: 250px;" @change="handle_camp_change">
						<option value="">캠페인을 선택하세요</option>
						<option v-for="cp in CAMPAIGNS" :key="cp.CAMP_NO" :value="cp.CAMP_NO">
							[{{ cp.survgbnM || '유형' }}] {{ cp.CAMP_NM }}
						</option>
					</select>
				</div>
				<div class="vr opacity-25 mx-2" style="height: 20px;"></div>
				<div class="stats-group d-flex gap-1 align-items-center overflow-auto hide-scrollbar flex-nowrap">
					<div class="stat-badge-mini pointer" :class="{active: CURRENT_FILTER === ''}" @click="apply_filter('')">
						<span class="label">전체</span><span class="value">{{ STATS.TOT_CNT || 0 }}</span>
					</div>
					<div v-for="item in DETAIL_STATS" :key="item.rslt_cd"
						 class="stat-badge-mini pointer" :class="{active: CURRENT_FILTER === item.rslt_cd}" @click="apply_filter(item.rslt_cd)">
						<span class="label">{{ item.RSLT_NM }}</span><span class="value">{{ item.CNT }}</span>
					</div>
				</div>
			</div>
			<div class="agent-info-area d-flex align-items-center gap-3 flex-shrink-0">
                <div v-if="ctiStore.isTalking" class="small text-danger animate-pulse fw-bold text-start"><i class="bi bi-record-circle me-1"></i>녹취 중...</div>
				<div class="agent-status-badge px-3 py-1 rounded bg-success bg-opacity-10 text-success fw-bold border border-success border-opacity-25">
					<span class="small">내선: {{ authStore.inner_no || '미등록' }}</span>
				</div>
			</div>
		</header>

		<main class="omni-body row g-0 overflow-hidden">
			<aside class="col-md-2 border-end bg-white d-flex flex-column h-100 shadow-sm overflow-hidden">
				<div class="flex-grow-1 d-flex flex-column overflow-hidden" style="flex-basis: 55%;">
					<div class="panel-header p-2 bg-light border-bottom d-flex gap-1">
						<input type="text" v-model="search_KEYWORD" class="form-control form-control-sm shadow-none" placeholder="검색..." @keyup.enter="load_customer_list">
						<button class="btn btn-sm btn-dark px-2" @click="load_customer_list"><i class="bi bi-search"></i></button>
					</div>
					<div class="flex-grow-1 position-relative">
						<div ref="CUSTOMER_TABLE_REF" class="tabulator-omni"></div>
					</div>
				</div>
				<div class="border-top bg-light bg-opacity-50 d-flex flex-column overflow-hidden" style="flex-basis: 45%;">
					<div class="card-header bg-white py-1 px-2 fw-bold small border-bottom text-secondary text-start">최근 상담 이력</div>
					<div class="p-2 timeline-scroll overflow-auto flex-grow-1 text-start">
						<div v-for="(log, idx) in TIMELINE_DATA" :key="idx" class="timeline-item border-bottom mb-1 pb-1">
							<div class="extra-small text-truncate">
                                <span class="text-muted">{{ format_date(log.ADDTIME) }}</span>
                                <span class="fw-bold text-dark mx-1">[{{ log.RSLT_NM }}]</span>
                                <span class="text-secondary ms-2 fw-normal">{{ log.remark || log.MEMO }}</span>
                            </div>
						</div>
					</div>
				</div>
			</aside>

			<section class="col-md-7 d-flex flex-column h-100 overflow-hidden bg-white p-2 gap-1 border-end text-start">
				<div class="card border shadow-sm flex-shrink-0 mb-1 border-0 border-start border-4 border-primary">
					<div class="card-body p-0">
						<div class="d-flex align-items-center bg-white px-3 py-2 text-start gap-4">
							<div class="d-flex align-items-center gap-2 flex-grow-1 overflow-hidden">
								<h5 class="fw-bold text-dark mb-0">{{ SELECTED_CUSTOMER?.CUST_NM || '상담 대상' }}</h5>
								<span class="fw-bold text-muted small">({{ CUSTOMER_INFO.TEL_NO || '-' }})</span>
								<div class="vr opacity-25 mx-2" style="height: 18px;"></div>
								<span class="fw-bold text-primary text-truncate small">{{ CUSTOMER_INFO.email || '-' }}</span>
							</div>
							<div class="btn-group btn-group-sm shadow-sm">
								<button class="btn btn-primary px-2 fw-bold btn-sm" @click="make_call" :disabled="ctiStore.isTalking">통화</button>
								<button class="btn btn-info text-white px-2 fw-bold btn-sm" @click="handle_invite">초대</button>
							</div>
						</div>
						<div class="ext-data-scroll bg-light p-2 border-top shadow-inner text-start overflow-auto" style="max-height: 80px;">
							<div v-if="SELECTED_CUSTOMER" class="d-flex flex-wrap gap-2">
								<div v-for="(val, key) in FILTERED_EXT_DATA" :key="key" class="badge bg-white text-dark border fw-normal extra-small px-2 py-1">
									<span class="text-muted fw-bold me-1">{{ key }}:</span>{{ val }}
								</div>
							</div>
                            <div v-else class="text-muted extra-small italic opacity-50 text-start">고객을 선택해 주세요.</div>
						</div>
					</div>
				</div>

				<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column border-top border-4 border-success">
					<div class="card-header bg-white py-1 px-3 fw-bold small text-success border-bottom text-start">상담 설문</div>
                    <div class="card-body p-0 overflow-auto flex-grow-1 bg-white text-start">
						<div class="survey-container px-3 py-2">
							<div v-for="(q, idx) in SURVEY_QUESTIONS" :key="q.SURV_NO" class="mb-3 border-bottom pb-2">
								<div class="fw-bold small mb-1 text-start">Q{{ idx + 1 }}. {{ q.QUESTION }}</div>
								<div class="d-flex flex-wrap gap-1 mb-2">
									<span v-for="sample in q.SAMPLE_LIST" :key="sample.NO"
                                          class="badge border px-2 py-1 pointer transition-all"
                                          :class="q.USER_ANS_NO === sample.NO ? 'bg-primary text-white shadow-sm' : 'bg-light text-dark'"
                                          @click="q.USER_ANS_NO = sample.NO; q.USER_ESSAY = sample.TEXT">
                                        {{ sample.TEXT }}
                                    </span>
								</div>
								<input type="text" v-model="q.USER_ESSAY" class="form-control form-control-sm" placeholder="상세 답변 또는 기타 의견">
							</div>
						</div>
					</div>
					<div class="fixed-footer-section border-top bg-white p-3 flex-shrink-0 shadow-inner">
						<div class="row g-2 align-items-center">
							<div class="col-md-3">
								<select v-model="CALL_RESULT.rslt_cd" class="form-select form-select-sm fw-bold border-success shadow-none">
									<option value="">결과 선택</option>
									<option v-for="code in result_codeS" :key="code.codecd" :value="code.codecd">{{ code.codenm }}</option>
								</select>
							</div>
							<div class="col-md-6"><input type="text" v-model="CALL_RESULT.MEMO" class="form-control form-control-sm border-success shadow-none" placeholder="상담 중요 요약 메모"></div>
							<div class="col-md-3"><button class="btn btn-success btn-sm w-100 fw-bold py-2 shadow-sm transition-all" @click="handle_save_all" :disabled="IS_SAVING || !SELECTED_CUSTOMER">상담 통합 저장</button></div>
						</div>
					</div>
				</div>
			</section>

			<aside class="col-md-3 bg-white d-flex flex-column h-100 shadow-sm border-start overflow-hidden">
				<div class="chat-header px-3 py-3 bg-dark text-white d-flex align-items-center justify-content-between">
					<h6 class="fw-bold mb-0 small"><i class="bi bi-chat-dots-fill me-2 text-info"></i>실시간 상담</h6>
                    <button class="btn btn-outline-info btn-xs py-0 px-2 fw-bold" @click="handle_clear_chat" style="font-size: 0.7rem;">초기화</button>
				</div>
				<div class="chat-log flex-grow-1 p-3 bg-light overflow-auto text-start" ref="AGENT_SCROLL_REF">
					<div v-for="(msg, idx) in AGENT_CHAT_HISTORY" :key="idx" class="d-flex mb-3" :class="msg.IS_ME ? 'justify-content-end' : 'justify-content-start'">
						<div class="p-2 px-3 rounded shadow-sm" :class="msg.IS_ME ? 'bg-success text-white' : 'bg-white text-dark'" style="max-width: 90%;">
							<div class="small" style="white-space: pre-wrap;">{{ msg.TEXT }}</div>
						</div>
					</div>
				</div>
				<div class="chat-input p-3 border-top bg-white">
					<div class="input-group input-group-sm">
						<input type="text" v-model="AGENT_REPLY_INPUT" class="form-control bg-light border-0 shadow-none px-3" placeholder="답장 입력..." @keyup.enter="send_agent_reply">
						<button class="btn btn-success px-3" @click="send_agent_reply"><i class="bi bi-send-fill"></i></button>
					</div>
				</div>
			</aside>
		</main>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useCtiStore } from '@/stores/ctiStore'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const authStore = useAuthStore(); const ctiStore = useCtiStore()

const CURRENT_FILTER = ref(''); const SESSION_START_TIME = ref('');
const STATS = reactive<any>({ TOT_CNT: 0, READY_CNT: 0 }); const DETAIL_STATS = ref<any[]>([])
const CAMPAIGNS = ref<any[]>([]); const SELECTED_CAMP_NO = ref('')
const search_KEYWORD = ref(''); const SELECTED_CUSTOMER = ref<any>(null)
const CUSTOMER_INFO = reactive({ email: '', TEL_NO: '' })
const FILTERED_EXT_DATA = ref<any>({}); const result_codeS = ref<any[]>([])
const SURVEY_QUESTIONS = ref<any[]>([]); const ACTIVE_QUESTION_NO = ref('')
const CALL_RESULT = reactive({ rslt_cd: '', MEMO: '' })
const AGENT_CHAT_HISTORY = ref<any[]>([]); const AGENT_REPLY_INPUT = ref('')
const TIMELINE_DATA = ref<any[]>([])
const IS_SAVING = ref(false)

let CUSTOMER_TABLE_INSTANCE: Tabulator | null = null; const CUSTOMER_TABLE_REF = ref<HTMLDivElement | null>(null)
const AGENT_SCROLL_REF = ref<HTMLElement | null>(null)

const parseExtData = (ext: any) => {
    if (!ext) return {};
    try {
        let p = ext;
        if (typeof p === 'string') { p = JSON.parse(p); if (typeof p === 'string') p = JSON.parse(p); }
        return typeof p === 'object' ? p : {};
    } catch (e) { return {}; }
}

const fetch_messages = async () => {
    if (!CUSTOMER_INFO.email) return;
    try {
        const res = await api.get('/common/chat/messages', { params: { email: CUSTOMER_INFO.email } });
        const list = res.data?.data || [];
        const reversedList = [...list].reverse();
        const newHistory = reversedList.filter((m: any) => String(m.message_type).split('.')[0] !== '2').map((m: any) => ({
            TEXT: m.content || '',
            IS_ME: String(m.message_type).split('.')[0] === '1' || m.message_type === 'outgoing'
        }));
        if (newHistory.length > 0 && JSON.stringify(newHistory) !== JSON.stringify(AGENT_CHAT_HISTORY.value)) {
            AGENT_CHAT_HISTORY.value = newHistory;
            nextTick(scrollChatToBottom);
        }
    } catch (e) {}
}

const send_agent_reply = async () => {
    const content = AGENT_REPLY_INPUT.value.trim();
    if (!content || !CUSTOMER_INFO.email) return;
    try {
        await api.post('/common/chat/reply', { email: CUSTOMER_INFO.email, content: content });
        AGENT_REPLY_INPUT.value = '';
        await fetch_messages();
    } catch (e) { vAlertError('전송 실패'); }
}

const handle_clear_chat = async () => {
    if (!CUSTOMER_INFO.email) return vAlertError('대상 고객 없음');
    if (!confirm('대화 이력을 삭제하고 상담창을 초기화하시겠습니까?')) return;
    try {
        await api.post('/common/chat/clear', { email: CUSTOMER_INFO.email });
        AGENT_CHAT_HISTORY.value = [];
        vAlert('초기화되었습니다.');
    } catch (e) { vAlertError('실패'); }
}

const scrollChatToBottom = () => { if (AGENT_SCROLL_REF.value) AGENT_SCROLL_REF.value.scrollTop = AGENT_SCROLL_REF.value.scrollHeight; }

const handle_camp_change = async () => {
    if (!SELECTED_CAMP_NO.value) return;
    SELECTED_CUSTOMER.value = null; SURVEY_QUESTIONS.value = []; TIMELINE_DATA.value = [];
    try {
        const res = await api.get('/crm/outbound/camp-stats', { params: { CAMP_NO: SELECTED_CAMP_NO.value } });
        Object.assign(STATS, res.data); DETAIL_STATS.value = res.data.DETAILS || [];
        const camp = CAMPAIGNS.value.find(c => c.CAMP_NO === SELECTED_CAMP_NO.value);
        if (camp) {
            const sres = await api.get('/crm/outbound/surv-form', { params: { SURV_GB: camp.SURV_GB } });
            const list = Array.isArray(sres.data) ? sres.data : [];
            SURVEY_QUESTIONS.value = list.map((q: any) => {
                // 💡 NO:TEXT:POINT 형식 파싱
                const samples = q.ANSWERS ? q.ANSWERS.split(/ \/ |, /).map((s: string) => {
                    const p = s.split(':');
                    return {
                        NO: p[0]?.trim(),
                        TEXT: (p[1] || p[0])?.trim(),
                        POINT: p[2]?.trim() || '0'
                    };
                }) : [];
                return { SURV_NO: q.SURV_NO, QUESTION: q.QUESTION, USER_ESSAY: '', USER_ANS_NO: '', SAMPLE_LIST: samples };
            });
        }
        load_customer_list();
    } catch (e) { vAlertError('로드 실패'); }
}

const load_customer_list = async () => {
    if (!SELECTED_CAMP_NO.value) return;
    try {
        const { data } = await api.get('/crm/outbound/call-list', { params: { CAMP_NO: SELECTED_CAMP_NO.value, KEYWORD: search_KEYWORD.value, FILTER: CURRENT_FILTER.value } });
        CUSTOMER_TABLE_INSTANCE?.setData(data);
    } catch (e) {}
}

const handle_save_all = async () => {
    if (!SELECTED_CUSTOMER.value) return vAlertError('대상 선택 필요');
    if (!CALL_RESULT.rslt_cd) return vAlertError('결과 선택 필요');
    IS_SAVING.value = true;
    try {
        const chat_log_str = AGENT_CHAT_HISTORY.value.map(m => `[${m.IS_ME?'상담원':'고객'}] ${m.TEXT}`).join('\n');
        await api.post('/omni/chat/save-consolidated', {
            cmpycd: authStore.cmpycd || 'haionnet',
            CAMP_NO: SELECTED_CAMP_NO.value,
            CALL_SEQ: SELECTED_CUSTOMER.value.CALL_SEQ,
            CUST_email: CUSTOMER_INFO.email,
            CUST_NM: SELECTED_CUSTOMER.value.CUST_NM,
            CALL_telno: CUSTOMER_INFO.TEL_NO,
            rslt_cd: CALL_RESULT.rslt_cd,
            MEMO: CALL_RESULT.MEMO,
            userid: authStore.user_id,
            LINE_NUM: authStore.inner_no,
            START_TIME: SESSION_START_TIME.value,
            END_TIME: new Date().toISOString().replace('T', ' ').substring(0, 19),
            MEDIA_TYPE: AGENT_CHAT_HISTORY.value.length > 0 ? 'chat' : 'CALL',
            CHAT_HISTORY: chat_log_str,
            SURVEYS: SURVEY_QUESTIONS.value.map(q => {
                // 💡 선택된 답변의 점수(point)를 함께 전달
                const selectedSample = q.SAMPLE_LIST.find(s => s.NO === q.USER_ANS_NO);
                return {
                    surv_no: q.SURV_NO,
                    ans_no: q.USER_ANS_NO || '001',
                    essay: q.USER_ESSAY,
                    point: selectedSample ? selectedSample.POINT : '0'
                };
            })
        });
        vAlert('저장되었습니다.'); handle_camp_change();
    } catch (e) { vAlertError('저장 실패'); } finally { IS_SAVING.value = false; }
}

const init_customer_table = () => {
    CUSTOMER_TABLE_INSTANCE = new Tabulator(CUSTOMER_TABLE_REF.value!, {
        layout: "fitColumns", height: "100%", selectable: 1, placeholder: "데이터 없음",
        columns: [
            { title: "고객/연락처", field: "CUST_NM", hozAlign: "left", formatter: (cell:any) => {
                const d = cell.getRow().getData();
                return `<div class="py-1 text-start"><div class="fw-bold small text-dark">${d.CUST_NM}</div><div class="text-muted extra-small">${d.TEL_NO || ''}</div></div>`;
            }},
            { title: "상태", field: "RSLT_NM", hozAlign: "center", cssClass: "small" }
        ]
    });
    CUSTOMER_TABLE_INSTANCE.on("rowClick", (e, row) => {
        const d = row.getData();
        SELECTED_CUSTOMER.value = d;
        CUSTOMER_INFO.TEL_NO = d.TEL_NO; CUSTOMER_INFO.email = d.email;
        FILTERED_EXT_DATA.value = parseExtData(d.EXT_DATA);
        SESSION_START_TIME.value = new Date().toISOString().replace('T', ' ').substring(0, 19);
        fetch_messages();
    });
}

const apply_filter = (code: string) => { CURRENT_FILTER.value = code; load_customer_list(); }
const format_date = (dt: any) => dt ? new Date(dt).toLocaleString() : '';
const make_call = () => { if (SELECTED_CUSTOMER.value && CUSTOMER_INFO.TEL_NO) api.get('/crm/cti/make-call', { params: { exten: authStore.inner_no, dest: CUSTOMER_INFO.TEL_NO.replace(/-/g, ''), context: 'outbound-call' } }); }
const handle_invite = async () => { if (CUSTOMER_INFO.email) { await api.post('/mail/send-invite', { toEmail: CUSTOMER_INFO.email, custNm: SELECTED_CUSTOMER.value.CUST_NM, custcd: SELECTED_CUSTOMER.value.CALL_SEQ.toString() }); vAlert('초대장 발송 완료'); } }

onMounted(() => {
    init_customer_table();
    api.get('/crm/outbound/camp-list').then(res => CAMPAIGNS.value = res.data);
    fetchCrmSelectData('920').then(res => result_codeS.value = res);
    const itv = setInterval(fetch_messages, 3000);
    onUnmounted(() => clearInterval(itv));
});
</script>

<style scoped>
.omni-workspace { height: 100vh; overflow: hidden; }
.omni-body { padding: 0 10px 50px 10px !important; height: calc(100vh - 55px - 50px) !important; display: flex; overflow: hidden; }
.omni-header { height: 55px; flex-shrink: 0; }
.stat-badge-mini { padding: 4px 10px; border-radius: 4px; border: 1px solid #ddd; background: #fff; font-size: 0.75rem; cursor: pointer; }
.stat-badge-mini.active { background: #0d6efd; color: #fff; }
.tabulator-omni { height: 100%; border: none; font-size: 0.8rem; }
.extra-small { font-size: 0.75rem; }
.btn-xs { padding: 1px 5px; font-size: 0.7rem; }
.fw-black { font-weight: 900; }
</style>
