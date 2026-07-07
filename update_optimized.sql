UPDATE COM_MANUAL
SET content = N'<div style="font-family: ''Malgun Gothic'', sans-serif; max-width: 720px; margin: 5px auto; border: 1px solid #005a9f; background: #fff; color: #333; line-height: 1.5; font-size: 13px;">

    <!-- 🟦 타이틀 바 (높이 축소) -->
    <div style="background: #005a9f; padding: 10px 15px; color: white; display: flex; justify-content: space-between; align-items: center;">
        <span style="font-size: 16px; font-weight: bold;">[HSIP_100U] 수입발주작업 매뉴얼</span>
        <span style="font-size: 11px; opacity: 0.8;">ERP Standard</span>
    </div>

    <div style="padding: 15px;">
        <!-- 1. 개요 (간결하게) -->
        <div style="margin-bottom: 15px; padding: 8px 12px; background: #f8f9fa; border-left: 3px solid #005a9f;">
            <b>▣ 개요 :</b> 수입 발주 정보 등록 및 외화/부대비용 관리
        </div>

        <!-- 2. 사용법 (촘촘한 표 구조) -->
        <div style="margin-bottom: 20px;">
            <table style="width: 100%; border-collapse: collapse; table-layout: fixed;">
                <tr style="background: #f1f3f5;">
                    <th style="width: 70px; padding: 6px; border: 1px solid #ccc; text-align: center;">구분</th>
                    <th style="padding: 6px; border: 1px solid #ccc; text-align: center;">업무 절차 및 방법</th>
                </tr>
                <tr>
                    <th style="padding: 8px; border: 1px solid #eee; background: #fdfdfd; text-align: center; color: #005a9f;">조회</th>
                    <td style="padding: 8px; border: 1px solid #eee;">
                        • 발주부서, PO No 입력 후 <b>[조회]</b><br>
                        • <b>[이전/다음]</b> 버튼으로 전표 연속 탐색
                    </td>
                </tr>
                <tr>
                    <th style="padding: 8px; border: 1px solid #eee; background: #fdfdfd; text-align: center; color: #2b8a3e;">입력</th>
                    <td style="padding: 8px; border: 1px solid #eee;">
                        • <b>[신규]</b> ➔ 부서, 거래처 등 마스터 정보 입력<br>
                        • 하단 그리드 <b>행추가</b> ➔ 품목/수량/단가 입력 <span style="color:red;">[No : A]</span><br>
                        • <b>[저장]</b> 버튼으로 최종 확정
                    </td>
                </tr>
                <tr>
                    <th style="padding: 8px; border: 1px solid #eee; background: #fdfdfd; text-align: center; color: #e67e22;">수정/삭제</th>
                    <td style="padding: 8px; border: 1px solid #eee;">
                        • 내역 수정 후 <b>[저장]</b> <span style="color:red;">[No : U]</span><br>
                        • 특정 행 삭제 : <b>행삭제</b> 클릭 <span style="color:red;">[No : D]</span> ➔ <b>[저장]</b>
                    </td>
                </tr>
            </table>
        </div>

        <!-- 3. 항목 설명 (2단 배열로 압축) -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 5px; margin-bottom: 20px; background: #fcfcfc; padding: 10px; border: 1px solid #f1f1f1;">
            <div>• <b>발주일자:</b> 발주 전표 공식 일자</div>
            <div>• <b>수입구분:</b> 직수입/단순대행/선매</div>
            <div>• <b>원산지:</b> 수입 대상 국가</div>
            <div>• <b>환율:</b> 외화 환산 기준 환율</div>
            <div>• <b>비용:</b> L/C 및 통관 원화 비용</div>
            <div>• <b>단가:</b> 품목별 수입 외화 단가</div>
        </div>

        <!-- 4. 제약 및 주의사항 (가독성 강조) -->
        <div style="background-color: #fff5f5; border: 1px solid #ffc9c9; padding: 10px; color: #c92a2a; margin-bottom: 15px;">
            <b>⚠️ 주의:</b> 선적 처리(HSIP_110U)된 발주서는 <b>수정 및 삭제가 불가</b>합니다.<br>
            반드시 선행된 선적 데이터를 취소한 후 진행하십시오.
        </div>

        <!-- 5. 선행 작업 -->
        <div style="font-size: 12px; color: #777; border-top: 1px solid #eee; padding-top: 10px;">
            <b>[선행작업]</b> 부서등록 ➔ 거래처등록 ➔ 수출입거래처설정 ➔ 품목등록 ➔ 항구/코드등록
        </div>
    </div>
</div>',
upd_dt = GETDATE(),
upd_id = 'SYSTEM'
WHERE progid = 'HSIP_100U';
