/** 月历名额（08 §5.2：GET /health/ordersettings 返回当月每天） */
export interface OrdersettingVO {
  orderDate: string;
  number: number;
  reservations: number;
  remain: number;
}

/** 设置某日名额 body（PUT 仅更新 number，不重置 reservations） */
export interface OrdersettingForm {
  orderDate?: string;
  number?: number;
}
