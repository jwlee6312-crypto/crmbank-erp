<!--
	=============================================================
	프로그램명	  : 통합고객지원 (디자인 최적화 버전)
    프로그램 ID	: HGIA010U
	작성일자	    : 2026.04.11
	작성자	      : AI Assistant
	Description	: 고객정보 4대 핵심요소 강조 및 영역 확대 최적화
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
	<div class="hgia-workspace bg-light text-start d-flex flex-column overflow-hidden">
		<!-- 1. 상단 CTI 제어 헤더 -->
		<header class="omni-header d-flex align-items-center justify-content-between px-3 shadow-sm bg-white border-bottom py-2 flex-shrink-0">
			<div class="d-flex align-items-center gap-3">
				<h5 class="m-0 fw-bold text-dark d-flex align-items-center" style="font-size: 1.1rem;">
					<i class="bi bi-headset text-primary me-2 fs-5"></i>
					통합고객지원 관리
					<span v-if="ctiStore.incomingCall?.queue" class="badge bg-warning text-dark ms-3 fw-bold shadow-sm" style="font-size: 0.75rem;">
						{{ queueMap[ctiStore.incomingCall.queue] || '상담그룹' }}
					</span>
				</h5>
				<div v-if="ctiStore.isTalking" class="talking-status-badge d-flex align-items-center gap-2 px-3 py-1 rounded-pill bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 shadow-sm">
					<span class="dot-blink bg-danger"></span>
					<span class="fw-bold small" style="font-size: 0.75rem;">상담 중 (녹취중)</span>
				</div>
			</div>
			<div class="d-flex gap-1">
				<button class="btn btn-sm btn-outline-secondary rounded-pill px-3 fw-bold" @click="handleNew">초기화</button>
				<button v-if="!ctiStore.isTalking && ctiStore.incomingCall" class="btn btn-sm btn-success rounded-pill px-3 fw-bold shadow-sm" @click="handleAnswer">전화받기</button>
				<button v-if="ctiStore.incomingCall" class="btn btn-sm btn-info text-white rounded-pill px-3 fw-bold shadow-sm" :disabled="isTransferLimitReached" @click="openTransferModal">돌려주기</button>
				<button class="btn btn-sm btn-danger rounded-pill px-3 fw-bold shadow-sm" @click="handleHangup">전화끊기</button>
				<button
					class="btn btn-sm btn-primary rounded-pill px-4 fw-bold shadow-sm ms-2"
					:disabled="ctiStore.isTalking"
					@click="handleSave">
					<i class="bi bi-save me-1"></i>상담저장
				</button>
			</div>
		</header>

		<!-- 2. 메인 상담 본문 -->
		<div class="flex-grow-1 d-flex flex-column overflow-hidden p-2 gap-2" :class="ctiStore.isTalking ? 'bg-talking' : ''">

			<div v-if="ctiStore.isTalking" class="alert alert-danger d-flex align-items-center mb-0 py-1 px-3 shadow-sm border-0 flex-shrink-0" style="border-radius: 8px;">
				<div class="spinner-grow spinner-grow-sm text-danger me-2" style="width: 12px; height: 12px;"></div>
				<div class="fw-bold extra-small text-danger">고객과 실시간 상담 진행 중...</div>
			</div>

			<!-- 상담 섹션 (높이 60%) -->
			<div class="row g-2 flex-shrink-0" style="height: 60%;">
				<!-- [좌측] 고객정보 및 상품 -->
				<div class="col-md-4 d-flex flex-column gap-2 h-100">
					<div class="card shadow-sm border-0 border-top border-4 border-primary flex-shrink-0">
						<div class="card-header bg-white py-1 px-3 fw-bold extra-small text-primary">
							<i class="bi bi-person-badge-fill me-2"></i>고객 필수 정보
						</div>
						<div class="card-body p-2 pt-1">
							<div class="row g-0"> <!-- 간격 완전 제거 -->
								<div class="col-12 mb-2">
									<div class="input-group input-group-sm">
										<input type="text" class="form-control bg-white fw-bold text-primary border-0 border-bottom" :value="customerInfo.custnm + (customerInfo.custcd ? ' [' + customerInfo.custcd + ']' : '')" readonly />
										<button class="btn btn-sm btn-dark px-2" type="button" @click="openCustModal"><i class="bi bi-search"></i></button>
									</div>
								</div>
								<div class="col-6 pr-1">
									<label class="mini-label fw-bold text-muted mb-0">수신번호</label>
									<input type="text" class="form-control form-control-sm border-0 border-bottom bg-white fw-bold text-primary py-0" v-model="customerInfo.hpno" readonly />
								</div>
								<div class="col-6">
									<label class="mini-label fw-bold text-muted mb-0">담당자</label>
									<input type="text" class="form-control form-control-sm border-0 border-bottom bg-white fw-bold py-0" v-model="customerInfo.usernm" readonly />
								</div>
								<div class="col-12 mt-1">
									<label class="mini-label fw-bold text-muted mb-0">이메일 주소</label>
									<input type="text" class="form-control form-control-sm border-0 border-bottom bg-white fw-bold py-0" v-model="customerInfo.email" readonly />
								</div>
							</div>
						</div>
					</div>
					<!-- 가입상품 영역 대폭 확대 -->
					<div class="card shadow-sm border-0 border-top border-4 border-info flex-grow-1 overflow-hidden">
						<div class="card-header bg-white py-2 px-3 fw-bold small text-info d-flex justify-content-between align-items-center">
							<span><i class="bi bi-box-fill me-2"></i>가입상품 / 매출내역</span>
							<span class="badge bg-info bg-opacity-10 text-info rounded-pill px-2" style="font-size: 0.7rem;">{{ itemList.length }}건</span>
						</div>
						<div class="card-body p-0 overflow-auto">
							<table class="table table-hover table-sm mb-0 extra-small align-middle">
								<thead class="table-light sticky-top">
									<tr><th class="ps-3 text-start">매출일자</th><th class="text-start">상품명</th><th class="pe-3">출고번호</th></tr>
								</thead>
								<tbody class="cursor-pointer">
									<tr v-for="item in itemList" :key="item.iono" @click="selectProduct(item)" :class="{'table-primary-subtle': consultData.iono === item.iono}">
										<td class="ps-3 text-start">{{ item.salsymd }}</td>
										<td class="text-start fw-bold text-truncate" style="max-width: 150px;">{{ item.itemnm }}</td>
										<td class="pe-3">{{ item.iono }}</td>
									</tr>
									<tr v-if="itemList.length === 0"><td colspan="3" class="py-5 text-muted italic text-center">조회된 내역이 없습니다.</td></tr>
								</tbody>
							</table>
						</div>
					</div>
				</div>

				<!-- [우측] 상담작성 및 에스컬레이션/녹취 (영역 대폭 확보) -->
				<div class="col-md-8 d-flex flex-column gap-2 h-100">
					<div class="card shadow-sm border-0 border-top border-4 border-success h-100 overflow-hidden d-flex flex-column" :class="{'border-danger': ctiStore.isTalking}">
						<div class="card-header bg-white py-2 px-3 fw-bold small text-success d-flex justify-content-between align-items-center">
							<span><i class="bi bi-pencil-square me-2"></i>상담 내용 작성</span>
							<div class="form-check form-switch mb-0">
								<input class="form-check-input" type="checkbox" id="escCheck" v-model="consultData.escalation_yn" true-value="Y" false-value="N">
								<label class="form-check-label extra-small fw-bold text-danger" for="escCheck">타 부서 업무 이관</label>
							</div>
						</div>
						<div class="card-body p-3 pt-2 overflow-hidden d-flex flex-column gap-2">
							<div class="row g-2 flex-shrink-0">
								<div class="col-md-2">
									<label class="mini-label fw-bold mb-0">상담일자</label>
									<input type="date" class="form-control form-control-sm border-light shadow-none" v-model="consultData.date" />
								</div>
								<div class="col-md-5">
									<label class="mini-label fw-bold mb-0">상담근거 상품</label>
									<input type="text" class="form-control form-control-sm bg-primary-subtle border-0 fw-bold" :value="consultData.itemnm" readonly />
								</div>
								<div class="col-md-5">
									<label class="mini-label fw-bold mb-0">AI 상담 요약</label>
									<div class="input-group input-group-sm">
										<input type="text" class="form-control border-warning-subtle bg-warning bg-opacity-10 fw-bold" v-model="consultData.summary" placeholder="자동 생성">
										<button class="btn btn-warning fw-bold text-dark px-3" type="button" @click="handleAiSummarize" :disabled="isSummarizing">
											<i class="bi bi-robot me-1"></i> AI 생성
										</button>
									</div>
								</div>
							</div>

							<!-- 문의/답변 텍스트 영역 (공간 최대 확보) -->
							<div class="row g-2 flex-grow-1 overflow-hidden">
								<div class="col-md-6 d-flex flex-column h-100">
									<label class="mini-label fw-bold text-primary mb-0"><i class="bi bi-question-circle-fill me-1"></i>문의 내용</label>
									<textarea class="form-control form-control-sm border-light bg-light bg-opacity-50 flex-grow-1" v-model="consultData.inquiry" style="resize:none; font-size: 0.85rem;"></textarea>
								</div>
								<div class="col-md-6 d-flex flex-column h-100">
									<label class="mini-label fw-bold text-success mb-0"><i class="bi bi-check-circle-fill me-1"></i>답변 내용</label>
									<textarea class="form-control form-control-sm border-light bg-light bg-opacity-50 flex-grow-1" v-model="consultData.answer" style="resize:none; font-size: 0.85rem;"></textarea>
								</div>
							</div>

							<!-- 에스컬레이션 및 녹취정보 (확대 강조 영역) -->
							<div class="consult-footer-area flex-shrink-0 pt-2 border-top border-light">
								<div class="row g-2">
									<div v-if="consultData.escalation_yn === 'Y'" class="col-12 scale-in mb-1">
										<div class="p-2 bg-danger bg-opacity-10 rounded border border-danger border-opacity-25 shadow-sm">
											<label class="mini-label fw-bold text-danger mb-1"><i class="bi bi-exclamation-triangle-fill me-1"></i>이관 요청 상세 메모 (담당자 전달용)</label>
											<textarea class="form-control form-control-sm bg-white border-danger border-opacity-25" rows="3" v-model="consultData.esc_memo" placeholder="이관 부서에 전달할 요청사항을 상세히 입력해 주세요."></textarea>
										</div>
									</div>
									<div class="col-12" v-if="ctiStore.recordingFile || ctiStore.isTalking">
										<div class="recording-alert p-2 d-flex align-items-center shadow-sm border rounded"
											 :class="ctiStore.isTalking ? 'alert-danger border-danger-subtle bg-danger-subtle' : 'alert-success border-success-subtle bg-success-subtle'">
											<template v-if="ctiStore.isTalking">
												<div class="spinner-grow spinner-grow-sm text-danger me-2" style="width:10px; height:10px;"></div>
												<span class="extra-small fw-bold text-danger">고객 음성 실시간 녹취 진행 중...</span>
											</template>
											<template v-else-if="ctiStore.recordingFile">
												<i class="bi bi-mic-fill me-2 text-success"></i>
												<span class="extra-small fw-bold text-dark">녹취 파일 생성 완료: </span>
												<span class="extra-small fw-bold text-primary ms-2">{{ ctiStore.recordingFile }}</span>
											</template>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<!-- 3. 하단 통합 이력 탭 -->
			<div class="card shadow-sm border-0 overflow-hidden flex-grow-1 d-flex flex-column border-top border-4 border-dark">
				<div class="bg-white px-3 pt-2 d-flex justify-content-between align-items-center flex-shrink-0">
					<ul class="nav nav-tabs border-bottom-0 gap-1">
						<li class="nav-item"><button class="nav-link small py-1 px-3" :class="{ active: activeTab === 1 }" @click="activeTab = 1">과거 상담 이력</button></li>
						<li class="nav-item"><button class="nav-link small py-1 px-3" :class="{ active: activeTab === 2 }" @click="activeTab = 2">서비스 처리 내역</button></li>
						<li class="nav-item"><button class="nav-link small py-1 px-3" :class="{ active: activeTab === 3 }" @click="activeTab = 3">정산 / 수금 내역</button></li>
					</ul>
					<span class="extra-small text-muted fw-bold"><i class="bi bi-clock-history me-1"></i>고객 통합 히스토리</span>
				</div>
				<div class="tab-content bg-white flex-grow-1 overflow-hidden">
					<div v-show="activeTab === 1" class="h-100 p-2"><div ref="tableRef1" class="tabulator-omni-fit"></div></div>
					<div v-show="activeTab === 2" class="h-100 p-2"><div ref="tableRef2" class="tabulator-omni-fit"></div></div>
					<div v-show="activeTab === 3" class="h-100 p-2"><div ref="tableRef3" class="tabulator-omni-fit"></div></div>
				</div>
			</div>
		</div>

		<!-- 전화 돌려주기 모달 -->
		<div v-if="showTransferModal" class="transfer-modal-overlay d-flex justify-content-center align-items-center">
			<div class="transfer-modal card shadow-lg border-0 overflow-hidden" style="width: 600px; border-radius: 12px;">
				<div class="card-header bg-dark text-white fw-bold d-flex justify-content-between align-items-center py-3 border-0">
					<div class="d-flex align-items-center"><i class="bi bi-telephone-forward-fill me-2 text-info"></i>전화 돌려주기</div>
					<button type="button" class="btn-close btn-close-white" @click="showTransferModal = false"></button>
				</div>
				<div class="card-body p-0 bg-light">
					<div class="p-3 bg-white mx-3 my-3 rounded shadow-sm border border-info border-opacity-25 extra-small text-start">
						<div class="fw-bold text-info mb-2"><i class="bi bi-info-circle me-1"></i>전달될 상담 정보</div>
						<div class="row g-2">
							<div class="col-6"><strong>거래처:</strong> {{ customerInfo.custnm }}</div>
							<div class="col-12 text-truncate text-muted"><strong>문의요약:</strong> {{ consultData.summary || '(입력없음)' }}</div>
						</div>
					</div>
					<div class="agent-list p-3 pt-0 overflow-auto" style="max-height: 350px;">
						<table class="table table-hover table-sm align-middle mb-0 text-center extra-small bg-white rounded shadow-sm">
							<thead class="table-light">
								<tr><th class="py-2">내선</th><th>상담원</th><th>부서</th><th>상태</th><th>동작</th></tr>
							</thead>
							<tbody>
								<tr v-for="agent in filteredAgents" :key="agent.inner_no">
									<td class="fw-bold text-primary">{{ agent.inner_no }}</td>
									<td class="fw-bold">{{ agent.usernm }}</td>
									<td class="text-muted text-start ps-3">{{ agent.deptnm }}</td>
									<td>
										<span v-if="agent.isOnline" class="badge bg-success rounded-pill px-2">ONLINE</span>
										<span v-else class="badge bg-secondary rounded-pill px-2 opacity-50">OFF</span>
									</td>
									<td>
										<button class="btn btn-xs btn-primary rounded-pill px-3 py-0" :disabled="!agent.isOnline" @click="executeTransfer(agent.inner_no)">연결</button>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>

		<!-- 거래처 검색 모달 -->
		<Modal v-model:visible="showCustModal" :modalProps="custModalProps" />
	</div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount, computed, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useCtiStore } from '@/stores/ctiStore'
import { useAuthStore } from '@/stores/authStore'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'
import { fetchSelectDataList } from '@/composables/useFetchSelectData'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const ctiStore = useCtiStore(); const authStore = useAuthStore()

const activeTab = ref(1); const itemList = ref<any[]>([]); const isSummarizing = ref(false)
const showTransferModal = ref(false); const searchAgent = ref(''); const agents = ref<any[]>([])
const result_codes = ref<any[]>([])

const queueMap: Record<string, string> = { 'sales_group': '영업상담', 'support_group': '고객지원상담', 'billing_group': '요금상담' };

const customerInfo = ref({ custcd: '', custnm: '미등록 고객', usernm: '', hpno: '', email: '', address: '' })
const consultData = ref({
	date: new Date().toISOString().substring(0, 10),
	summary: '', inquiry: '', answer: '',
	itemcd: '', itemnm: '', iono: '',
	deptcd: '', esc_memo: '', escalation_no: '', escalation_yn: 'N'
})

const showCustModal = ref(false)
const custModalProps = ref<ModalProps>({
	type: 'table', large: false, title: '거래처검색', defaultField: 'custnm',
	columns: [{ title: '코드', field: 'custcd', width: 100 }, { title: '거래처', field: 'custnm', hozAlign: 'left' }, { title: '전화번호', field: 'telno', hozAlign: 'center' }],
	path: '/popup/pop-cust', data: { sch_custnm: '' },
	onConfirm: (modalData) => {
		customerInfo.value.custcd = modalData.custcd; customerInfo.value.custnm = modalData.custnm;
		customerInfo.value.hpno = modalData.telno || customerInfo.value.hpno;
		showCustModal.value = false; loadTabData(1);
	},
})
const openCustModal = () => { showCustModal.value = true; }

const isTransferLimitReached = computed(() => (ctiStore.incomingCall?.transfer_cnt || 0) >= 2);

const handleAiSummarize = async () => {
	if (!consultData.value.inquiry) { vAlertError('문의 내용을 입력해 주세요.'); return; }
	isSummarizing.value = true;
	try {
		const res = await api.post('/crm/inbound/ai-summarize', { trb_ment: consultData.value.inquiry, ans_ment: consultData.value.answer });
		if (res.data) { consultData.value.summary = res.data.summary; consultData.value.deptcd = res.data.deptcd; }
	} catch (e) { vAlertError('AI 요약 실패'); }
	finally { isSummarizing.value = false; }
}

const loadAgents = async () => {
	try {
		const [userRes, activeExtensRes] = await Promise.all([api.get('/common/user/list', { params: { schuseyn: 'Y' } }), api.get('/crm/cti/active-agents')]);
		const activeExtens = activeExtensRes.data || [];
		agents.value = (userRes.data || []).filter((u: any) => u.inner_no).map((u: any) => ({ ...u, isOnline: activeExtens.includes(u.inner_no) }));
	} catch (e) {}
}

const filteredAgents = computed(() => {
	if (!searchAgent.value) return agents.value;
	const search = searchAgent.value.toLowerCase();
	return agents.value.filter(a => (a.usernm?.toLowerCase().includes(search)) || (a.inner_no?.includes(search)));
})

const openTransferModal = async () => { await loadAgents(); showTransferModal.value = true; }

const executeTransfer = async (targetExten: string) => {
	if (!confirm(`${targetExten}번 상담원에게 전화를 돌려주시겠습니까?`)) return;
	try {
		await api.get(`/crm/cti/transfer`, {
			params: {
				exten: ctiStore.incomingCall?.exten, target: targetExten, custcd: customerInfo.value.custcd,
				itemnm: consultData.value.itemnm, iono: consultData.value.iono,
				summary: consultData.value.summary, inquiry: consultData.value.inquiry, answer: consultData.value.answer
			}
		});
		showTransferModal.value = false; vAlert('전화 돌려주기가 시도되었습니다.');
	} catch (e) { vAlertError('돌려주기 실패'); }
}

const tableRef1 = ref<HTMLDivElement | null>(null); const tableRef2 = ref<HTMLDivElement | null>(null); const tableRef3 = ref<HTMLDivElement | null>(null);
let tableInstance1: Tabulator | null = null; let tableInstance2: Tabulator | null = null; let tableInstance3: Tabulator | null = null;

const loadTabData = async (tab: number) => {
	if (!customerInfo.value.custcd) return;
	try {
		let url = tab === 1 ? '/crm/inbound/call-history' : tab === 2 ? '/crm/inbound/service-history' : '/crm/inbound/settle-history';
		const res = await api.get(url, { params: { custcd: customerInfo.value.custcd } });
		if (tab === 1) tableInstance1?.setData(res.data || []);
		else if (tab === 2) tableInstance2?.setData(res.data || []);
		else if (tab === 3) tableInstance3?.setData(res.data || []);
	} catch (e) {}
};

watch(activeTab, async (tab) => { await loadTabData(tab); });

const initGrids = async () => {
	await nextTick();
    const commonConfig = { layout: 'fitColumns', height: '100%', placeholder: "내역이 없습니다." };
	if (tableRef1.value && !tableInstance1) tableInstance1 = new Tabulator(tableRef1.value, { ...commonConfig, columns: [{ title: "접수번호", field: "svcno", width: 120 }, { title: "상담일시", field: "start_time", width: 150 }, { title: "상담원", field: "consultnm", width: 100 }, { title: "상담요약", field: "summary", hozAlign: 'left' }]});
	if (tableRef2.value && !tableInstance2) tableInstance2 = new Tabulator(tableRef2.value, { ...commonConfig, columns: [{ title: "접수번호", field: "svcno", width: 120 }, { title: "접수일자", field: "acceptymd", width: 120 }, { title: "수리기사", field: "fixed_usernm", width: 100 }, { title: "수리결과", field: "fixed_ment", hozAlign: 'left' }]});
	if (tableRef3.value && !tableInstance3) tableInstance3 = new Tabulator(tableRef3.value, { ...commonConfig, columns: [{ title: "발생일", field: "ymd", width: 120 }, { title: "품목/적요", field: "itemnm", hozAlign: 'left' }, { title: "합계", field: "totamt", hozAlign: 'right', formatter: 'money', width: 120 }]});
};

const setCallData = async (data: any) => {
	if (!data) return;
	customerInfo.value = { custcd: data.custcd || '', custnm: data.custnm || '미등록', usernm: data.usernm || '', hpno: data.hpno || data.callerid || '', email: data.email || '', address: data.address || '' };
	itemList.value = data.itemlist || [];
	if (data.isTransferred || data.queue) {
		consultData.value.itemnm = data.itemnm || ''; consultData.value.iono = data.iono || ''; consultData.value.itemcd = data.itemcd || '';
		consultData.value.inquiry = data.inquiry || ''; consultData.value.answer = data.answer || '';
		if (data.queue && queueMap[data.queue]) consultData.value.summary = `[${queueMap[data.queue]} 인입] `;
	}
	activeTab.value = 1; await loadTabData(1);
};

onMounted(() => { initGrids(); fetchSelectDataList<any>('/code', { cdtype: '920' }).then(res => result_codes.value = res); if (ctiStore.incomingCall) setCallData(ctiStore.incomingCall); });

const handleAnswer = async () => { try { await api.get(`/crm/cti/answer?exten=${ctiStore.incomingCall?.exten}`); } catch (e) {} }
const handleHangup = async () => { try { await api.get(`/crm/cti/hangup?exten=${ctiStore.incomingCall?.exten}`); } catch (e) {} }
const selectProduct = (item: any) => { consultData.value.itemnm = item.itemnm; consultData.value.iono = item.iono; consultData.value.itemcd = item.itemcd; }
const handleNew = () => { if(confirm('새 상담을 시작하시겠습니까?')) location.reload(); };

const handleSave = async () => {
	if (ctiStore.isTalking) { vAlertError('통화 중에는 저장할 수 없습니다.'); return; }
	if (!customerInfo.value.custcd) { vAlertError('거래처를 선택해 주세요.'); return; }
	try {
		await api.post('/crm/inbound/save', {
			dto: { ...consultData.value, custcd: customerInfo.value.custcd, cmpycd: 'HAIONNET', svcymd: consultData.value.date.replaceAll('-', ''), linkedid: ctiStore.incomingCall?.linkedid },
			recordings: ctiStore.recordingFile ? [ctiStore.recordingFile] : []
		});
		vAlert('상담 내용이 저장되었습니다.');
	} catch (e) { vAlertError('저장 실패'); }
};

onBeforeUnmount(() => { tableInstance1?.destroy(); tableInstance2?.destroy(); tableInstance3?.destroy(); });
</script>

<style scoped>
.hgia-workspace { height: 100vh; display: flex; flex-direction: column; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.omni-header { min-height: 56px; }
.dot-blink { width: 10px; height: 10px; border-radius: 50%; animation: blinker 1s linear infinite; }
@keyframes blinker { 50% { opacity: 0; } }
.transfer-modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.5); z-index: 11000; }
.btn-xs { padding: 1px 5px; font-size: 0.75rem; }
.mini-label { font-size: 0.75rem; color: #6c757d; display: block; margin-bottom: 2px; }
.extra-small { font-size: 0.75rem; }
.bg-talking { background-color: #fcf2f2 !important; }
.transition-all { transition: all 0.3s ease; }
.tabulator-omni-fit { height: 100% !important; border: 1px solid #dee2e6; font-size: 0.85rem; }
.card { border-radius: 8px; }
.nav-tabs .nav-link { border: 0; color: #666; font-weight: 600; }
.nav-tabs .nav-link.active { color: #0d6efd; border-bottom: 2px solid #0d6efd; background: transparent; }
.form-control-sm, .form-select-sm { font-size: 0.85rem; }
.recording-alert { border-radius: 6px; }
.consult-footer-area { background: #fcfcfc; border-radius: 0 0 8px 8px; }
</style>
