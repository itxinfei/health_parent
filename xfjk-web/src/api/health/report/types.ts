/** 体检报告（07 §2.6~2.11 / §3.2 / §5.3~5.6 / §6） */

export interface ReportVO {
  id?: number;
  reportNo?: string;
  memberId?: number;
  memberName?: string;
  memberSex?: string;
  archiveNo?: string;
  orderId?: number;
  examNo?: string;
  batchId?: number;
  checkDate?: string;
  doctor?: string;
  source?: string;
  status?: string;
  abnormalCount?: number;
  criticalCount?: number;
  conclusionType?: string;
  conclusionFinal?: string;
  fileUrl?: string;
  revokeFlag?: string;
  summaryBy?: string;
  summaryTime?: string;
  finalCheckBy?: string;
  finalCheckTime?: string;
  sourceReportId?: number;
  remark?: string;
  createTime?: string;
}

export interface ReportItemVO {
  id?: number;
  reportId?: number;
  checkitemId?: number;
  deptId?: number;
  itemName?: string;
  resultType?: string;
  result?: string;
  unit?: string;
  refRange?: string;
  refMin?: number;
  refMax?: number;
  refConfigured?: string;
  abnormalFlag?: string;
  qualitative?: string;
  edgeFlag?: string;
  abnormalGrade?: string;
  criticalFlag?: string;
  majorPositive?: string;
  itemStatus?: string;
  discardReason?: string;
  mergeGroupId?: string;
  prevResult?: string;
  prevAbnormalFlag?: string;
  prevRefRange?: string;
  dualDeptFlag?: string;
  peerDeptStatus?: string;
  sortNo?: number;
  doctor?: string;
  checkTime?: string;
  checkDevice?: string;
  remark?: string;
}

export interface ReportItemResultVO {
  id?: number;
  abnormalFlag?: string;
  refRange?: string;
  refConfigured?: string;
  criticalFlag?: string;
  abnormalGrade?: string;
  majorPositive?: string;
  edgeFlag?: string;
  /** 录入提示：偏高/偏低/临界/危急值/复查建议（18 D3/D8） */
  tip?: string;
}

export interface DeptSummaryVO {
  id?: number;
  reportId?: number;
  deptId?: number;
  summaryText?: string;
  printLabel?: string;
  lastModifyBy?: string;
  lastModifyTime?: string;
  submitFlag?: string;
  sendBackFlag?: string;
  sendBackReason?: string;
}

export interface ReportConclusionVO {
  id?: number;
  reportId?: number;
  conclusionDictId?: number;
  conclusionType?: string;
  itemIds?: string;
  adviceIds?: string;
  grade?: string;
  mergeGroupId?: string;
  finalFlag?: string;
  sortNo?: number;
}

export interface CriticalLogVO {
  id?: number;
  reportId?: number;
  reportItemId?: number;
  memberId?: number;
  memberName?: string;
  archiveNo?: string;
  itemName?: string;
  criticalValue?: string;
  ruleCode?: string;
  deptId?: number;
  reportTime?: string;
  receiver?: string;
  receiverConfirmTime?: string;
  notifyWay?: string;
  handleResult?: string;
  handled?: boolean;
  confirmed?: boolean;
  trackBy?: string;
  trackTime?: string;
  trackRemark?: string;
}

export interface ReportArchiveVO {
  id?: number;
  reportId?: number;
  type?: string;
  ossId?: number;
  pageCount?: number;
  remark?: string;
  createTime?: string;
}

export interface ReportDetailVO {
  report?: ReportVO;
  deptSummaryText?: string;
  abnormalSummary?: string;
  conclusion?: string;
  basis?: string;
  advice?: string;
  summary?: string;
  conclusionJson?: string;
  firstCheckBy?: string;
  firstCheckTime?: string;
  summaryBy?: string;
  summaryTime?: string;
  revokeReason?: string;
  revokeTime?: string;
  items?: ReportItemVO[];
  deptSummaries?: DeptSummaryVO[];
  conclusions?: ReportConclusionVO[];
  criticals?: CriticalLogVO[];
  archives?: ReportArchiveVO[];
  prevReport?: ReportVO;
}

export interface ReportQuery {
  pageNum?: number;
  pageSize?: number;
  archiveNo?: string;
  memberId?: number;
  batchId?: number;
  examNo?: string;
  status?: string;
  source?: string;
  beginDate?: string;
  endDate?: string;
}

export interface ReportForm {
  memberId?: number;
  checkDate?: string;
  source?: string;
  orderId?: number;
  examNo?: string;
  batchId?: number;
  doctor?: string;
  remark?: string;
}

export interface ReportItemForm {
  checkitemId?: number;
  itemName: string;
  resultType: string;
  result: string;
  unit?: string;
  deptId?: number;
  itemStatus: string;
  discardReason?: string;
  qualitative?: string;
  abnormalFlag?: string;
  majorPositive?: string;
  edgeFlag?: string;
  mergeGroupId?: string;
  doctor?: string;
  checkDevice?: string;
  sortNo?: number;
  remark?: string;
}

export interface ConclusionForm {
  conclusionDictId?: number;
  conclusionType?: string;
  itemIds?: number[];
  adviceIds?: number[];
  grade?: string;
  mergeGroupId?: string;
  sortNo?: number;
}

export interface ConclusionDictVO {
  id?: number;
  code?: string;
  term?: string;
  conclusionType?: string;
  abnormalGrade?: string;
  normalFlag?: number;
  majorPositiveFlag?: number;
  mergeGroupCode?: string;
  status?: string;
}

export interface ReportParseVO {
  total?: number;
  success?: number;
  unmatched?: number;
  errors?: { row?: number; message?: string }[];
}

export interface ReportCompareVO {
  reportId?: number;
  reportNo?: string;
  checkDate?: string;
  checkitemId?: number;
  itemName?: string;
  result?: string;
  unit?: string;
  refRange?: string;
  abnormalFlag?: string;
}
