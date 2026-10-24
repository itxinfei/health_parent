/** 订单流转扩展（t_health_order_ext，08 §2.6） */
export interface OrderExtVO {
  id: number;
  orderId: number;
  flowStatus: string;
  examNo: string;
  batchId: number;
  batchMemberId: number;
  cancelReason: string;
  cancelBy: string;
  cancelTime: string;
  postponeCount: number;
  postponeUntil: string;
  printTime: string;
  issueTime: string;
  registerBy: string;
  registerTime: string;
  reportId: number;
}

/** 订单流水（t_health_order_log，08 §2.9） */
export interface OrderLogVO {
  id: number;
  orderId: number;
  action: string;
  fromStatus: string;
  toStatus: string;
  operator: string;
  operatorRole: string;
  remark: string;
  createTime: string;
}

/** 订单视图（08 §6.4：主表 + ext，详情另含 logs） */
export interface OrderVO {
  id: number;
  memberId: number;
  orderDate: string;
  orderType: string;
  orderStatus: string;
  setmealId: number;
  ext?: OrderExtVO;
  logs?: OrderLogVO[];
}

/** 分页查询入参（08 §4.5，query 绑定） */
export interface OrderQuery extends PageQuery {
  orderDateBegin?: string;
  orderDateEnd?: string;
  orderStatus?: string;
  flowStatus?: string;
  memberId?: number;
  setmealId?: number;
  batchId?: number;
}
