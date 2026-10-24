/** 危急值查询入参（07 §3.2.1） */
export interface CriticalQuery {
  pageNum?: number;
  pageSize?: number;
  /** 派生筛选：false → handle_result 为空（未处理） */
  handled?: boolean;
  /** 派生筛选：false → receiver_confirm_time 为空（未送达确认） */
  confirmed?: boolean;
  /** 六个码：D4-WBC-LOW/HIGH、D4-HB-LOW/HIGH、D4-PLT-LOW/HIGH */
  ruleCode?: string;
  archiveNo?: string;
  memberId?: number;
  receiver?: string;
  notifyWay?: string;
  beginTime?: string;
  endTime?: string;
  deptId?: number;
}

export interface CriticalHandleForm {
  receiver?: string;
  receiverConfirmTime?: string;
  notifyWay?: string;
  handleResult?: string;
  trackBy?: string;
  trackTime?: string;
  trackRemark?: string;
}
