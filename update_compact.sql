UPDATE COM_MANUAL
SET content = N'<div style="font-family: ''Malgun Gothic'', sans-serif; max-width: 800px; margin: 10px auto; border: 2px solid #005a9f; background: #fff; border-radius: 8px; overflow: hidden; color: #333;">

    <!-- 🔵 컴팩트 헤더 -->
    <div style="background: #005a9f; padding: 15px 25px; color: white; display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #004085;">
        <h2 style="margin: 0; font-size: 22px; font-weight: bold;">[HSIP_100U] 수입발주작업 가이드</h2>
        <span style="font-size: 12px; background: rgba(255,255,255,0.2); padding: 3px 10px; border-radius: 4px;">무역/수입관리 표준</span>
    </div>

    <div style="padding: 20px;">
        <!-- 1. 개요 (밀도 있는 구성) -->
        <div style="margin-bottom: 20px; padding: 12px 15px; background: #f1f8ff; border: 1px solid #cfe2ff; border-radius: 4px;">
            <b style="color: #005a9f; font-size: 15px;">■ 프로그램 개요 :</b>
            <span style="font-size: 15px; line-height: 1.5;">수입 품목의 발주 정보를 관리하며, 외화 금액 및 원가 산정용 제비용을 통합 관리합니다.</span>
        </div>

        <!-- 2. 사용법 (짜임새 있는 표 형식) -->
        <h4 style="font-size: 16px; font-weight: bold; border-bottom: 1px solid #eee; padding-bottom: 8px; margin-bottom: 12px; color: #005a9f;">2. 업무 처리 가이드</h4>
        <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 15px;">
            <tr style="border-bottom: 1px solid #eee;">
                <th style="width: 80px; padding: 10px; background: #f8f9fa; border: 1px solid #dee2e6; text-align: center; color: #005a9f;">조회</th>
                <td style="padding: 10px; border: 1px solid #dee2e6; line-height: 1.6;">
                    • <b>발주부서/PO No</b> 입력 후 [검색]<br>
                    • [이전/다음] 버튼으로 부서 내 발주서 연속 탐색
                </td>
            </tr>
            <tr style="border-bottom: 1px solid #eee;">
                <th style="padding: 10px; background: #f8f9fa; border: 1px solid #dee2e6; text-align: center; color: #2b8a3e;">입력</th>
                <td style="padding: 10px; border: 1px solid #dee2e6; line-height: 1.6;">
                    • [신규] 클릭 ➔ 마스터 정보 입력 (거래처 선택 시 마스터 자동 연동)<br>
                    • 하단 리스트 <b>행추가</b> ➔ 품목/수량/단가 입력 [No 에 A 표시]<br>
                    • [저장] 버튼 클릭으로 최종 확정
                </td>
            </tr>
            <tr style="border-bottom: 1px solid #eee;">
                <th style="padding: 10px; background: #f8f9fa; border: 1px solid #dee2e6; text-align: center; color: #e67e22;">수정/삭제</th>
                <td style="padding: 10px; border: 1px solid #dee2e6; line-height: 1.6;">
                    • 수정 대상 조회 ➔ 내역 변경 [No 에 U 표시] ➔ [저장]<br>
                    • 개별 삭제 : [행삭제] 클릭 [No 에 D 표시] ➔ [저장]
                </td>
            </tr>
        </table>

        <!-- 3. 주요항목 설명 (그리드 타입) -->
        <h4 style="font-size: 16px; font-weight: bold; border-bottom: 1px solid #eee; padding-bottom: 8px; margin-bottom: 12px; color: #005a9f;">3. 주요 항목 설명</h4>
        <div style="display: flex; flex-wrap: wrap; gap: 5px; margin-bottom: 25px;">
            <div style="width: 49%; background: #fcfcfc; border: 1px solid #eee; padding: 8px 12px; font-size: 14px;"><b>- 발주일자:</b> 발주서 발행 공식 일자</div>
            <div style="width: 49%; background: #fcfcfc; border: 1px solid #eee; padding: 8px 12px; font-size: 14px;"><b>- 수입구분:</b> 직수입/단순대행/선매발주</div>
            <div style="width: 49%; background: #fcfcfc; border: 1px solid #eee; padding: 8px 12px; font-size: 14px;"><b>- 원산지:</b> 수입 대상 국가</div>
            <div style="width: 49%; background: #fcfcfc; border: 1px solid #eee; padding: 8px 12px; font-size: 14px;"><b>- 환율:</b> 발주 시점 환산 기준 환율</div>
            <div style="width: 49%; background: #fcfcfc; border: 1px solid #eee; padding: 8px 12px; font-size: 14px;"><b>- L/C & 통관비:</b> 원화 비용 가산</div>
            <div style="width: 49%; background: #fcfcfc; border: 1px solid #eee; padding: 8px 12px; font-size: 14px;"><b>- 단가:</b> 구매 단위당 외화 단가</div>
        </div>

        <!-- 4. 제약 사항 (확실한 강조) -->
        <div style="background-color: #fff5f5; border: 1px solid #ffc9c9; padding: 15px; border-radius: 4px; margin-bottom: 20px;">
            <p style="margin: 0; color: #c92a2a; font-size: 14px; line-height: 1.6;">
                <b>⚠️ 주의:</b> 선적 처리(HSIP_110U)된 발주서는 <b>수정 및 삭제가 절대 불가</b>합니다. <br>
                수정이 필요한 경우 반드시 선행된 선적 데이터를 취소한 후 진행하십시오.
            </p>
        </div>

        <!-- 5. 선행 작업 -->
        <div style="font-size: 14px; color: #666; border-top: 1px dashed #ccc; padding-top: 15px;">
            <b>[선행작업]</b> 부서 등록 ➔ 거래처 등록 ➔ 수출입거래처 세부설정 ➔ 품목 등록 ➔ 항구/공항 코드 등록
        </div>
    </div>

    <!-- 📄 푸터 -->
    <div style="background: #f8f9fa; padding: 10px 25px; border-top: 1px solid #eee; text-align: center; color: #aaa; font-size: 11px;">
        ERP STANDARD SYSTEM DOCUMENT | HSIP_100U
    </div>
</div>',
upd_dt = GETDATE(),
upd_id = 'SYSTEM'
WHERE progid = 'HSIP_100U';
