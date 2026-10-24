import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { OrdersettingVO, OrdersettingForm } from '@/api/health/ordersetting/types';

/** 按月历查询名额（GET /health/ordersettings?year&month，08 §4.4） */
export const listOrdersetting = (year: number, month: number): AxiosPromise<OrdersettingVO[]> => {
  return request({
    url: '/health/ordersettings',
    method: 'get',
    params: { year, month }
  });
};

/** 设置某日可预约人数（PUT，仅更新 number，不重置 reservations） */
export const setOrdersetting = (data: OrdersettingForm) => {
  return request({
    url: '/health/ordersettings',
    method: 'put',
    data: data
  });
};

/** 删除某日设置（存在订单返回 5008） */
export const delOrdersetting = (orderDate: string) => {
  return request({
    url: '/health/ordersettings',
    method: 'delete',
    params: { orderDate }
  });
};

/** Excel 批量导入名额（列：日期、可预约人数；仅晚于今天的日期生效） */
export const uploadOrdersetting = (file: File): AxiosPromise<string> => {
  const formData = new FormData();
  formData.append('file', file);
  return request({
    url: '/health/ordersettings/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};
