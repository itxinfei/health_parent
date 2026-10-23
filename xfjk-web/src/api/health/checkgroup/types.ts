import { CheckitemVO } from '@/api/health/checkitem/types';

export interface CheckgroupExtVO {
  deptId: number;
  specimen: string;
  dualDeptFlag: string;
  isFasting: string;
  defaultConclusionId: number;
  sortNo: number;
}

export interface CheckgroupVO {
  id: number;
  code: string;
  name: string;
  helpCode: string;
  sex: string;
  remark: string;
  attention: string;
  checkItems?: CheckitemVO[];
  ext?: CheckgroupExtVO;
}

export interface CheckgroupExtForm {
  deptId?: number;
  specimen?: string;
  dualDeptFlag?: string;
  isFasting?: string;
  defaultConclusionId?: number;
  sortNo?: number;
}

export interface CheckgroupForm extends BaseEntity {
  id?: number;
  code?: string;
  name?: string;
  helpCode?: string;
  sex?: string;
  remark?: string;
  attention?: string;
  // 关联检查项 id 集合；空数组表示清空关联（08 §5.1 差异更新）
  checkItemIds: number[];
  ext: CheckgroupExtForm;
}

export interface CheckgroupQuery extends PageQuery {
  code?: string;
  name?: string;
  sex?: string;
}
