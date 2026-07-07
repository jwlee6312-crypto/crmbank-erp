UPDATE COM_MANUAL
SET content = N'<div style="font-family: ''Malgun Gothic'', sans-serif; max-width: 900px; margin: 0 auto; border: 1px solid #e2e8f0; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.08);">
    <div style="background: linear-gradient(135deg, #005a9f 0%, #003a6a 100%); padding: 35px; color: white;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <span style="background: rgba(255,255,255,0.2); padding: 4px 12px; border-radius: 4px; font-size: 12px; letter-spacing: 1px;">PROCUREMENT MODULE</span>
                <h1 style="margin: 10px 0 0; font-size: 30px; font-weight: 800;">수입발주작업 <span style="font-weight: 300; opacity: 0.8;">HSIP_100U</span></h1>
            </div>
            <div style="text-align: right; opacity: 0.7;">
                <div style="font-size: 14px;">표준 업무 매뉴얼</div>
                <div style="font-size: 11px;">v1.0 (2025.02.24)</div>
            </div>
        </div>
    </div>
    <div style="padding: 40px;">
        <div style="background: #f8fafc; border-left: 5px solid #005a9f; padding: 20px; border-radius: 0 8px 8px 0; margin-bottom: 40px;">
            <h4 style="margin: 0 0 10px; color: #005a9f; font-weight: 800;">1. 프로그램 개요</h4>
            <p style="margin: 0; color: #334155; line-height: 1.6;">해외 거래처로부터 원부자재 및 제품을 수입하기 위해 <b>발주 마스터 정보와 상세 품목 내역을 입력</b>하고 관리합니다. 입력된 데이터는 향후 입고 및 원가 산정의 기초가 됩니다.</p>
        </div>
        <h4 style="font-size: 20px; border-bottom: 2px solid #eee; padding-bottom: 10px; margin-bottom: 20px; color: #1e293b; font-weight: 800;">2. 상세 업무 가이드</h4>
        <div style="margin-bottom: 30px;">
            <div style="display: flex; gap: 10px; margin-bottom: 15px;">
                <span style="background: #005a9f; color: white; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: bold;">조회</span>
                <span style="color: #64748b; font-size: 13px;">기존 발주서 확인 및 연속 탐색</span>
            </div>
            <div style="padding-left: 15px; border-left: 1px solid #e2e8f0; margin-left: 10px; color: #475569; font-size: 14px; line-height: 1.8;">
                • 발주부서, PO No를 입력 후 [검색]을 클릭합니다.<br>
                • <b style="color: #005a9f;">[이전/다음]</b> 버튼을 통해 부서 내 발주 전표를 순차적으로 빠르게 검토할 수 있습니다.
            </div>
        </div>
        <div style="margin-bottom: 30px;">
            <div style="display: flex; gap: 10px; margin-bottom: 15px;">
                <span style="background: #2b8a3e; color: white; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: bold;">입력</span>
                <span style="color: #64748b; font-size: 13px;">신규 발주서 등록 절차</span>
            </div>
            <div style="padding: 20px; background: #fcfdfe; border: 1px solid #e2e8f0; border-radius: 8px; color: #475569; font-size: 14px; line-height: 2;">
                1. <b>[신규]</b> 버튼 클릭 (빈 화면 활성화)<br>
                2. 거래처 선택 ➔ 마스터 정보(원산지, 항구, 결제조건 등) <b>자동 연동 확인</b><br>
                3. 그리드 <b>행추가</b> ➔ 품목/수량/단가 입력 <span style="background: #e7f5ff; color: #1971c2; padding: 1px 5px; border-radius: 3px; font-size: 11px; font-weight: bold;">No : A 표시</span><br>
                4. <b>[저장]</b> 클릭하여 전표 확정
            </div>
        </div>
        <div style="margin-bottom: 40px;">
            <div style="display: flex; gap: 10px; margin-bottom: 15px;">
                <span style="background: #e67e22; color: white; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: bold;">수정/삭제</span>
                <span style="color: #64748b; font-size: 13px;">데이터 변경 및 처리 제한</span>
            </div>
            <div style="padding-left: 15px; border-left: 1px solid #e2e8f0; margin-left: 10px; color: #475569; font-size: 14px; line-height: 1.8;">
                • 행 상태 코드 의미 - <b style="color: #e67e22;">[U]</b>: 수정됨, <b style="color: #c92a2a;">[D]</b>: 삭제대상<br>
                • <span style="background: #fff5f5; color: #c92a2a; border: 1px solid #ffc9c9; padding: 4px 10px; border-radius: 4px; font-weight: bold; display: inline-block; margin-top: 10px;">⚠️ 이미 선적(HSIP_110U)된 발주서는 수정 및 삭제가 절대 불가합니다.</span>
            </div>
        </div>
        <h4 style="font-size: 18px; color: #1e293b; margin-bottom: 15px; font-weight: 800;">3. 주요 항목 설명</h4>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 40px;">
            <div style="background: #f8fafc; padding: 12px; border-radius: 6px; font-size: 13px;"><b>PO No:</b> 회사의 발번 규칙에 의거 자동/수동 입력</div>
            <div style="background: #f8fafc; padding: 12px; border-radius: 6px; font-size: 13px;"><b>수입구분:</b> 직수입 / 단순대행 / 선매발주 구분</div>
            <div style="background: #f8fafc; padding: 12px; border-radius: 6px; font-size: 13px;"><b>환율:</b> 발주 시점 기준 환율 (원화 금액 계산용)</div>
            <div style="background: #f8fafc; padding: 12px; border-radius: 6px; font-size: 13px;"><b>Offer No:</b> 계약 참조 번호</div>
        </div>
        <div style="background: #eef2ff; border: 1px solid #c7d2fe; border-radius: 12px; padding: 25px;">
            <h4 style="margin: 0 0 15px; color: #4338ca; font-size: 16px; font-weight: 800;">✅ 원활한 입력을 위한 선행 작업</h4>
            <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                <span style="background: white; border: 1px solid #c7d2fe; padding: 5px 12px; border-radius: 6px; font-size: 12px; color: #4338ca;">부서 등록</span>
                <span style="background: white; border: 1px solid #c7d2fe; padding: 5px 12px; border-radius: 6px; font-size: 12px; color: #4338ca;">거래처 등록</span>
                <span style="background: white; border: 1px solid #c7d2fe; padding: 5px 12px; border-radius: 6px; font-size: 12px; color: #4338ca;">품목 정보</span>
                <span style="background: white; border: 1px solid #c7d2fe; padding: 5px 12px; border-radius: 6px; font-size: 12px; color: #4338ca;">항구/코드 정보</span>
            </div>
        </div>
    </div>
    <div style="background: #f8fafc; padding: 20px 40px; border-top: 1px solid #eee; text-align: center; color: #94a3b8; font-size: 12px;">
        HAI-ON NET CRM | ENTERPRISE STANDARD DOCUMENT
    </div>
</div>',
upd_dt = GETDATE(),
upd_id = 'SYSTEM'
WHERE progid = 'HSIP_100U';
