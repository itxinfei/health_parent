export interface ForbidVO {
  id: number;
  ruleCode: string;
  scope: string;
  targetId: number;
  conditionJson: string;
  action: string;
  tipText: string;
  priority: number;
  enabled: string;
  createTime?: string;
  updateTime?: string;
}

export interface ForbidForm extends BaseEntity {
  id?: number;
  ruleCode?: string;
  scope?: string;
  targetId?: number;
  conditionJson?: string;
  action?: string;
  tipText?: string;
  priority?: number;
  enabled?: string;
}

/** 试算入参（08 §6.2，与 C 端 forbid-preview 同构） */
export interface ForbidCheckForm {
  setmealId?: number;
  sex?: string;
  married?: string;
  hasSexHistory?: string;
  pregnancy?: string;
  age?: number;
  inMenstruation?: boolean;
  metalImplant?: boolean;
}

export interface ForbidHitVO {
  ruleCode: string;
  action: string;
  targetId: number;
  itemName: string;
  tipText: string;
}

export interface ForbidCheckVO {
  pass: boolean;
  hitCount: number;
  hits: ForbidHitVO[];
  warns: ForbidHitVO[];
  delays: ForbidHitVO[];
}
