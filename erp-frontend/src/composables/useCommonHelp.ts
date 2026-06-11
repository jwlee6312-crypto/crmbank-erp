import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import type { ModalProps } from '@/types/modal'

export function useCommonHelp() {
  const authStore = useAuthStore()
  const modalVisible = ref(false)
  const modalProps = reactive<ModalProps>({
    title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table'
  })

  /**
   * 💡 공통 도움창 호출 함수
   * @param type 팝업 유형
   * @param callback 선택 완료 후 콜백
   * @param extraData 추가 파라미터 (gubun, gbncd, search 등)
   */
  const openHelp = (type: string, callback: (data: any) => void, extraData: any = {}) => {
    const commonPath = '/api/ha00/HA00_00P_STR'

    if (type === 'DEPT') {
      Object.assign(modalProps, {
        title: '부서 선택',
        path: commonPath,
        defaultField: 'deptnm',
        large: false,
        data: { gubun: 'D0', cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '부서코드', field: 'deptcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '부서명', field: 'deptnm', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'CUST') {
      const gubun = (extraData.gubun || 'C4').toUpperCase()
      Object.assign(modalProps, {
        title: '거래처 선택',
        path: commonPath,
        defaultField: 'custnm',
        large: true,
        data: { gubun: gubun, cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '거래처코드', field: 'custcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '거래처명', field: 'custnm', minWidth: 250, widthGrow: 1, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'ACCT') {
      // 💡 대소문자 무시 (e0 -> E0) 및 지불수단 여부 판별
      const gubun = (extraData.gubun || 'A0').toUpperCase()
      const isMethod = gubun === 'E0'

      Object.assign(modalProps, {
        title: isMethod ? '지불수단(계정) 선택' : '계정과목 선택',
        path: commonPath,
        defaultField: isMethod ? 'codenm' : 'acctnm',
        large: true,
        data: { gubun: gubun, cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '계정코드', field: 'acctcd', width: 120, hozAlign: 'center', headerSort: false },
          {
            title: '계정명',
            field: isMethod ? 'codenm' : 'acctnm',
            minWidth: 200, widthGrow: 1,
            hozAlign: 'left',
            headerSort: true,
            // 💡 필드명이 acctnm, codenm, cdnm 등으로 혼용되어도 명칭을 표시하도록 포맷터 적용
            formatter: (cell: any) => {
              const d = cell.getData();
              return d.acctnm || d.codenm || d.cdnm || d.name || '';
            }
          },
          { title: '비고', field: isMethod ? 'remark' : 'cacctnm', minWidth: 150, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'MGT') {
      Object.assign(modalProps, {
        title: '관리번호(은행) 선택',
        path: commonPath,
        defaultField: 'mgtnm',
        large: true,
        data: { gubun: 'M0', cmpycd: authStore.cmpycd, gbncd: extraData.mgtgbn || '', code: extraData.search || '', remark: extraData.acctcd || '' },
        columns: [
          { title: '계좌번호', field: 'mgtno', width: 150, hozAlign: 'center', headerSort: false },
          { title: '은행코드', field: 'bankcd', width: 100, hozAlign: 'center', headerSort: false },
          { title: '은행명', field: 'mgtnm', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: true },
          { title: '비고', field: 'remark', minWidth: 150, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'PRJ') {
      Object.assign(modalProps, {
        title: '프로젝트 선택',
        path: commonPath,
        defaultField: 'prjnm',
        large: true,
        data: { gubun: 'P0', cmpycd: authStore.cmpycd, gbncd: extraData.prjcd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '코드', field: 'prjcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '프로젝트명', field: 'prjnm', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: true },
          { title: '비고', field: 'bigo', minWidth: 150, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'USER') {
      Object.assign(modalProps, {
        title: '사용자 선택',
        path: commonPath,
        defaultField: 'usernm',
        large: false,
        data: { gubun: 'U0', cmpycd: authStore.cmpycd, gbncd: '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '사번', field: 'userid', width: 120, hozAlign: 'center', headerSort: false },
          { title: '성명', field: 'usernm', minWidth: 150, widthGrow: 1, hozAlign: 'left', headerSort: true }
        ],
        onConfirm: callback
      })
    }
    modalVisible.value = true
  }

  return { modalVisible, modalProps, openHelp }
}
