import { CheckgroupVO } from '@/api/health/checkgroup/types';

export interface SetmealExtVO {
  sex: string;
  ageMin: number;
  ageMax: number;
  marriedLimit: string;
  hasSexHistory: string;
  pregnancyWarn: string;
  suitableDesc: string;
  defaultConclusionId: number;
  defaultAdviceId: number;
  noticeText: string;
}

export interface SetmealVO {
  id: number;
  name: string;
  code: string;
  helpCode: string;
  sex: string;
  age: string;
  price: number;
  remark: string;
  attention: string;
  // OSS ossId 字符串（落库值）；imgUrl 仅详情场景解析，列表为 null
  img: string;
  imgUrl?: string;
  checkGroups?: CheckgroupVO[];
  ext?: SetmealExtVO;
}

export interface SetmealExtForm {
  sex?: string;
  ageMin?: number;
  ageMax?: number;
  marriedLimit?: string;
  hasSexHistory?: string;
  pregnancyWarn?: string;
  suitableDesc?: string;
  defaultConclusionId?: number;
  defaultAdviceId?: number;
  noticeText?: string;
}

export interface SetmealForm extends BaseEntity {
  id?: number;
  name?: string;
  code?: string;
  helpCode?: string;
  sex?: string;
  age?: string;
  price?: number;
  remark?: string;
  attention?: string;
  img?: string;
  // 关联检查组 id 集合；空数组表示清空关联（08 §5.1 差异更新）
  checkGroupIds: number[];
  ext: SetmealExtForm;
}

export interface SetmealQuery extends PageQuery {
  name?: string;
  code?: string;
  helpCode?: string;
  sex?: string;
}

export interface SetmealImageVO {
  ossId: string;
  url: string;
}
