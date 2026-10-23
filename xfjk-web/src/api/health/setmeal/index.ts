import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { SetmealVO, SetmealForm, SetmealQuery, SetmealImageVO } from '@/api/health/setmeal/types';
import { CheckgroupVO } from '@/api/health/checkgroup/types';

export const listSetmeal = (query?: SetmealQuery): AxiosPromise<SetmealVO[]> => {
  return request({
    url: '/health/setmeals',
    method: 'get',
    params: query
  });
};

/** 全量列表（无查询参数，C 端与本页候选共用） */
export const listAllSetmeal = (): AxiosPromise<SetmealVO[]> => {
  return request({
    url: '/health/setmeals/all',
    method: 'get'
  });
};

/** 详情：三级（套餐→检查组→检查项），imgUrl 仅此场景解析 */
export const getSetmeal = (id: string | number): AxiosPromise<SetmealVO> => {
  return request({
    url: '/health/setmeals/' + id,
    method: 'get'
  });
};

/** 套餐关联检查组（编辑回显勾选） */
export const getSetmealGroups = (id: string | number): AxiosPromise<CheckgroupVO[]> => {
  return request({
    url: '/health/setmeals/' + id + '/checkgroups',
    method: 'get'
  });
};

export const addSetmeal = (data: SetmealForm) => {
  return request({
    url: '/health/setmeals',
    method: 'post',
    data: data
  });
};

export const updateSetmeal = (id: string | number, data: SetmealForm) => {
  return request({
    url: '/health/setmeals/' + id,
    method: 'put',
    data: data
  });
};

/** 删除：存在订单返回 4001 */
export const delSetmeal = (id: string | number | Array<string | number>) => {
  return request({
    url: '/health/setmeals/' + id,
    method: 'delete'
  });
};

/** 套餐图片上传（走统一 request 实例自动带 token；返回 ossId 落库、url 回显） */
export const uploadSetmealImage = (file: File): AxiosPromise<SetmealImageVO> => {
  const formData = new FormData();
  formData.append('file', file);
  return request({
    url: '/health/setmeals/image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};
