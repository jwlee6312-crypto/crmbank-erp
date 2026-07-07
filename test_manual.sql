MERGE INTO COM_MANUAL AS T
USING (SELECT 'HSIP_100U' AS progid) AS S
ON T.progid = S.progid
WHEN MATCHED THEN
    UPDATE SET
        prognm = N'수입발주작업',
        content = N'<h2><strong>[HSIP_100U] 수입발주작업 업무 매뉴얼</strong></h2>
<hr>

<h3><strong>1. 개 요</strong></h3>
<p>수입 품목에 대한 발주 정보를 등록하고 관리하는 프로그램입니다. 거래처별 발주 조건을 설정하고, 외화 금액 및 소요 비용(L/C, 통관 등)을 관리하여 정확한 수입 원가를 산출할 수 있습니다.</p>

<h3><strong>2. 선행 작업 (사전 준비)</strong></h3>
<p>정상적인 입력을 위해 다음 메뉴에서 기초 정보가 등록되어 있어야 합니다.</p>
<ul>
    <li><strong>부서 관리:</strong> 주문 부서(발주 부서) 등록</li>
    <li><strong>고객정보 관리:</strong> 구매 거래처(수입처) 등록</li>
    <li><strong>수출입거래처 관리:</strong> 거래처별 세부 수입 조건 설정</li>
    <li><strong>품목 등록:</strong> 수입 대상 품목 및 구매 단위 등록</li>
    <li><strong>코드정보 등록:</strong> 선적항, 도착항, 보관장소 코드 등록</li>
</ul>

<h3><strong>3. 조회 및 검색</strong></h3>
<p><strong>3.1 조건 검색</strong></p>
<ul>
    <li>상단 필터에서 <strong>발주일자</strong>와 <strong>거래처명</strong>을 입력한 후 <strong>[조회]</strong> 버튼을 클릭합니다.</li>
    <li>좌측 <strong>''발주 목록''</strong> 그리드에 조회된 리스트가 출력됩니다.</li>
</ul>
<p><strong>3.2 상세 내용 확인</strong></p>
<ul>
    <li>좌측 리스트에서 특정 PO를 클릭하면 우측 폼과 하단 그리드에 상세 발주 내역이 자동 로드됩니다.</li>
</ul>

<h3><strong>4. 입력 및 저장 절차 (신규 등록)</strong></h3>
<p><strong>Step 1. 화면 초기화</strong></p>
<ul>
    <li>상단 액션 바의 <strong>[초기화]</strong> 버튼을 클릭하여 입력 대기 상태로 만듭니다.</li>
</ul>
<p><strong>Step 2. 마스터 정보 입력</strong></p>
<ul>
    <li><strong>거래처 선택:</strong> 돋보기 아이콘을 눌러 거래처를 선택합니다. (선택 시 원산지, 항구, 결제조건 등이 마스터 기반으로 자동 세팅됩니다.)</li>
    <li><strong>환율 설정:</strong> 결제 통화를 선택하고 현재 환율을 입력합니다. (원화 합계 계산의 기준이 됩니다.)</li>
</ul>
<p><strong>Step 3. 품목 내역 입력</strong></p>
<ul>
    <li>하단 그리드에서 <strong>[+ 행추가]</strong> 버튼을 클릭합니다.</li>
    <li>새로 생성된 행의 품목명을 클릭하여 품목을 선택하고 <strong>수량(Qty)</strong>과 <strong>단가(Price)</strong>를 입력합니다.</li>
    <li>입력 중인 행은 상태란에 <span style=\"background-color: #0d6efd; color: white; padding: 2px 5px; border-radius: 3px;\">신규</span> 배지가 표시됩니다.</li>
</ul>
<p><strong>Step 4. 저장 완료</strong></p>
<ul>
    <li>데이터 검토 후 상단의 <strong>[저장]</strong> 버튼을 클릭하여 최종 확정합니다.</li>
</ul>

<h3><strong>5. 수정 및 삭제 방법</strong></h3>
<p><strong>5.1 내역 수정</strong></p>
<ul>
    <li>수정할 발주서를 조회한 후 내용을 변경합니다. 변경된 행은 상태란에 <span style=\"background-color: #ffc107; color: black; padding: 2px 5px; border-radius: 3px;\">수정</span> 배지가 표시됩니다.</li>
    <li>품목 삭제가 필요한 경우 해당 행을 선택하고 <strong>[- 행삭제]</strong>를 누르면 <span style=\"background-color: #dc3545; color: white; padding: 2px 5px; border-radius: 3px;\">삭제</span> 상태로 변경되며, 저장 시 반영됩니다.</li>
</ul>
<p><strong>5.2 전표 삭제</strong></p>
<ul>
    <li>발주서 전체를 삭제하려면 상단의 <strong>[전체삭제]</strong> 버튼을 이용합니다.</li>
</ul>

<h3><strong>6. 주요 항목 가이드</strong></h3>
<table style=\"width: 100%; border-collapse: collapse; border: 1px solid #dee2e6;\">
    <tr style=\"background-color: #f8f9fa;\">
        <th style=\"border: 1px solid #dee2e6; padding: 8px; width: 25%;\">항목명</th>
        <th style=\"border: 1px solid #dee2e6; padding: 8px;\">상세 설명</th>
    </tr>
    <tr>
        <td style=\"border: 1px solid #dee2e6; padding: 8px; font-weight: bold;\">PO No</td>
        <td style=\"border: 1px solid #dee2e6; padding: 8px;\">회사 발번 규칙에 의한 고유 번호 (조회 시 Key값)</td>
    </tr>
    <tr>
        <td style=\"border: 1px solid #dee2e6; padding: 8px; font-weight: bold;\">수입구분</td>
        <td style=\"border: 1px solid #dee2e6; padding: 8px;\">직수입 / 단순대행 / 선매발주 중 선택</td>
    </tr>
    <tr>
        <td style=\"border: 1px solid #dee2e6; padding: 8px; font-weight: bold;\">통화 / 환율</td>
        <td style=\"border: 1px solid #dee2e6; padding: 8px;\">외화 결제 기준 및 원화 환산을 위한 기준 환율</td>
    </tr>
    <tr>
        <td style=\"border: 1px solid #dee2e6; padding: 8px; font-weight: bold;\">소요비용</td>
        <td style=\"border: 1px solid #dee2e6; padding: 8px;\">L/C 개설비 및 통관 예상 비용 (원화 합계에 가산됨)</td>
    </tr>
</table>

<h3><strong>7. 💡 업무 핵심 팁 (주의사항)</strong></h3>
<ul>
    <li><span style=\"color: #dc3545; font-weight: bold;\">⚠️ 선적 완료 건 처리 불가:</span> 이미 선적 처리(HSIP_110U)된 발주서는 <strong>수정 및 삭제가 불가능</strong>합니다. 수정을 원할 경우 선적 메뉴에서 해당 건을 먼저 취소해야 합니다.</li>
    <li><span style=\"color: #0d6efd; font-weight: bold;\">💡 자동 계산 기능:</span> 수량과 단가를 입력하면 외화 금액과 환율이 곱해진 원화 금액이 실시간으로 합산됩니다.</li>
    <li><span style=\"color: #6c757d; font-weight: bold;\">🚩 행 상태 관리:</span> 그리드 왼쪽의 상태 표시 배지를 통해 내가 어떤 작업을 하고 있는지(신규/수정/삭제) 저장 전에 최종 확인하십시오.</li>
</ul>',
        upd_dt = GETDATE(),
        upd_id = 'AI_TEST'
WHEN NOT MATCHED THEN
    INSERT (progid, prognm, content, upd_dt, upd_id)
    VALUES ('HSIP_100U', N'수입발주작업', N'<h2><strong>[HSIP_100U] 수입발주작업 업무 매뉴얼</strong></h2>
<hr>

<h3><strong>1. 개 요</strong></h3>
<p>수입 품목에 대한 발주 정보를 등록하고 관리하는 프로그램입니다. 거래처별 발주 조건을 설정하고, 외화 금액 및 소요 비용(L/C, 통관 등)을 관리하여 정확한 수입 원가를 산출할 수 있습니다.</p>

<h3><strong>2. 선행 작업 (사전 준비)</strong></h3>
<p>정상적인 입력을 위해 다음 메뉴에서 기초 정보가 등록되어 있어야 합니다.</p>
<ul>
    <li><strong>부서 관리:</strong> 주문 부서(발주 부서) 등록</li>
    <li><strong>고객정보 관리:</strong> 구매 거래처(수입처) 등록</li>
    <li><strong>수출입거래처 관리:</strong> 거래처별 세부 수입 조건 설정</li>
    <li><strong>품목 등록:</strong> 수입 대상 품목 및 구매 단위 등록</li>
    <li><strong>코드정보 등록:</strong> 선적항, 도착항, 보관장소 코드 등록</li>
</ul>

<h3><strong>3. 조회 및 검색</strong></h3>
<p><strong>3.1 조건 검색</strong></p>
<ul>
    <li>상단 필터에서 <strong>발주일자</strong>와 <strong>거래처명</strong>을 입력한 후 <strong>[조회]</strong> 버튼을 클릭합니다.</li>
    <li>좌측 <strong>''발주 목록''</strong> 그리드에 조회된 리스트가 출력됩니다.</li>
</ul>
<p><strong>3.2 상세 내용 확인</strong></p>
<ul>
    <li>좌측 리스트에서 특정 PO를 클릭하면 우측 폼과 하단 그리드에 상세 발주 내역이 자동 로드됩니다.</li>
</ul>

<h3><strong>4. 입력 및 저장 절차 (신규 등록)</strong></h3>
<p><strong>Step 1. 화면 초기화</strong></p>
<ul>
    <li>상단 액션 바의 <strong>[초기화]</strong> 버튼을 클릭하여 입력 대기 상태로 만듭니다.</li>
</ul>
<p><strong>Step 2. 마스터 정보 입력</strong></p>
<ul>
    <li><strong>거래처 선택:</strong> 돋보기 아이콘을 눌러 거래처를 선택합니다. (선택 시 원산지, 항구, 결제조건 등이 마스터 기반으로 자동 세팅됩니다.)</li>
    <li><strong>환율 설정:</strong> 결제 통화를 선택하고 현재 환율을 입력합니다. (원화 합계 계산의 기준이 됩니다.)</li>
</ul>
<p><strong>Step 3. 품목 내역 입력</strong></p>
<ul>
    <li>하단 그리드에서 <strong>[+ 행추가]</strong> 버튼을 클릭합니다.</li>
    <li>새로 생성된 행의 품목명을 클릭하여 품목을 선택하고 <strong>수량(Qty)</strong>과 <strong>단가(Price)</strong>를 입력합니다.</li>
    <li>입력 중인 행은 상태란에 <span style=\"background-color: #0d6efd; color: white; padding: 2px 5px; border-radius: 3px;\">신규</span> 배지가 표시됩니다.</li>
</ul>
<p><strong>Step 4. 저장 완료</strong></p>
<ul>
    <li>데이터 검토 후 상단의 <strong>[저장]</strong> 버튼을 클릭하여 최종 확정합니다.</li>
</ul>

<h3><strong>5. 수정 및 삭제 방법</strong></h3>
<p><strong>5.1 내역 수정</strong></p>
<ul>
    <li>수정할 발주서를 조회한 후 내용을 변경합니다. 변경된 행은 상태란에 <span style=\"background-color: #ffc107; color: black; padding: 2px 5px; border-radius: 3px;\">수정</span> 배지가 표시됩니다.</li>
    <li>품목 삭제가 필요한 경우 해당 행을 선택하고 <strong>[- 행삭제]</strong>를 누르면 <span style=\"background-color: #dc3545; color: white; padding: 2px 5px; border-radius: 3px;\">삭제</span> 상태로 변경되며, 저장 시 반영됩니다.</li>
</ul>
<p><strong>5.2 전표 삭제</strong></p>
<ul>
    <li>발주서 전체를 삭제하려면 상단의 <strong>[전체삭제]</strong> 버튼을 이용합니다.</li>
</ul>

<h3><strong>6. 주요 항목 가이드</strong></h3>
<table style=\"width: 100%; border-collapse: collapse; border: 1px solid #dee2e6;\">
    <tr style=\"background-color: #f8f9fa;\">
        <th style=\"border: 1px solid #dee2e6; padding: 8px; width: 25%;\">항목명</th>
        <th style=\"border: 1px solid #dee2e6; padding: 8px;\">상세 설명</th>
    </tr>
    <tr>
        <td style=\"border: 1px solid #dee2e6; padding: 8px; font-weight: bold;\">PO No</td>
        <td style=\"border: 1px solid #dee2e6; padding: 8px;\">회사 발번 규칙에 의한 고유 번호 (조회 시 Key값)</td>
    </tr>
    <tr>
        <td style=\"border: 1px solid #dee2e6; padding: 8px; font-weight: bold;\">수입구분</td>
        <td style=\"border: 1px solid #dee2e6; padding: 8px;\">직수입 / 단순대행 / 선매발주 중 선택</td>
    </tr>
    <tr>
        <td style=\"border: 1px solid #dee2e6; padding: 8px; font-weight: bold;\">통화 / 환율</td>
        <td style=\"border: 1px solid #dee2e6; padding: 8px;\">외화 결제 기준 및 원화 환산을 위한 기준 환율</td>
    </tr>
    <tr>
        <td style=\"border: 1px solid #dee2e6; padding: 8px; font-weight: bold;\">소요비용</td>
        <td style=\"border: 1px solid #dee2e6; padding: 8px;\">L/C 개설비 및 통관 예상 비용 (원화 합계에 가산됨)</td>
    </tr>
</table>

<h3><strong>7. 💡 업무 핵심 팁 (주의사항)</strong></h3>
<ul>
    <li><span style=\"color: #dc3545; font-weight: bold;\">⚠️ 선적 완료 건 처리 불가:</span> 이미 선적 처리(HSIP_110U)된 발주서는 <strong>수정 및 삭제가 불가능</strong>합니다. 수정을 원할 경우 선적 메뉴에서 해당 건을 먼저 취소해야 합니다.</li>
    <li><span style=\"color: #0d6efd; font-weight: bold;\">💡 자동 계산 기능:</span> 수량과 단가를 입력하면 외화 금액과 환율이 곱해진 원화 금액이 실시간으로 합산됩니다.</li>
    <li><span style=\"color: #6c757d; font-weight: bold;\">🚩 행 상태 관리:</span> 그리드 왼쪽의 상태 표시 배지를 통해 내가 어떤 작업을 하고 있는지(신규/수정/삭제) 저장 전에 최종 확인하십시오.</li>
</ul>', GETDATE(), 'AI_TEST');
