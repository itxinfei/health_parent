<template>
  <div class="p-2">
    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['health:forbid:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['health:order:query']" type="warning" plain icon="MagicStick" @click="openTrial">套餐试算</el-button>
          </el-col>
          <right-toolbar :search="false" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="forbidList">
        <el-table-column label="规则编码" prop="ruleCode" width="100" />
        <el-table-column label="作用范围" prop="scope" width="90" align="center">
          <template #default="scope">
            <dict-tag :options="health_forbid_scope" :value="scope.row.scope" />
          </template>
        </el-table-column>
        <el-table-column label="目标" min-width="150" show-overflow-tooltip>
          <template #default="scope">{{ targetName(scope.row.scope, scope.row.targetId) }}</template>
        </el-table-column>
        <el-table-column label="触发条件" min-width="200" show-overflow-tooltip>
          <template #default="scope">{{ condText(scope.row.conditionJson) }}</template>
        </el-table-column>
        <el-table-column label="处置档" prop="action" width="90" align="center">
          <template #default="scope">
            <dict-tag :options="health_forbid_action" :value="scope.row.action" />
          </template>
        </el-table-column>
        <el-table-column label="提示文案" prop="tipText" min-width="200" show-overflow-tooltip />
        <el-table-column label="优先级" prop="priority" width="70" align="center" class-name="tabular-nums" />
        <el-table-column label="启用" width="70" align="center">
          <template #default="scope">
            <el-switch v-hasPermi="['health:forbid:edit']" :model-value="scope.row.enabled === '1'" @change="(v: boolean) => toggleEnabled(scope.row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="编辑" placement="top">
              <el-button v-hasPermi="['health:forbid:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['health:forbid:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="640px" append-to-body>
      <el-form ref="forbidFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="form.ruleCode" placeholder="锚定 18 E 组编号，如 E6" maxlength="32" :disabled="form.id !== undefined" />
        </el-form-item>
        <el-form-item label="作用范围" prop="scope">
          <el-select v-model="form.scope" placeholder="请选择" style="width: 100%" @change="form.targetId = undefined">
            <el-option v-for="dict in health_forbid_scope" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标" prop="targetId">
          <el-select v-model="form.targetId" placeholder="按作用范围选择" filterable style="width: 100%">
            <el-option v-for="opt in targetOptions(form.scope)" :key="opt.id" :label="`${opt.name}（${opt.code}）`" :value="opt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发条件">
          <div class="w-full">
            <div v-for="(row, idx) in condRows" :key="idx" class="cond-row mb8">
              <el-select v-model="row.key" placeholder="条件键" style="width: 130px" @change="onCondKeyChange(row)">
                <el-option
                  v-for="dict in health_forbid_cond_key"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                  :disabled="dict.value !== row.key && condRows.some((r) => r.key === dict.value)"
                />
              </el-select>
              <!-- 枚举键：多选值数组 -->
              <el-select v-if="isEnumKey(row.key)" v-model="row.values" multiple collapse-tags placeholder="命中值" style="flex: 1">
                <el-option v-for="dict in enumOptions(row.key)" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
              <!-- 年龄键：数值 -->
              <el-input-number v-else-if="isAgeKey(row.key)" v-model="row.num" :min="0" :max="150" controls-position="right" style="flex: 1" />
              <!-- 布尔键 -->
              <el-select v-else-if="isBoolKey(row.key)" v-model="row.bool" placeholder="值" style="width: 120px">
                <el-option label="是" :value="true" />
                <el-option label="否" :value="false" />
              </el-select>
              <span v-else class="el-text--placeholder">先选条件键</span>
              <el-button link type="danger" icon="Delete" :disabled="condRows.length === 1" @click="condRows.splice(idx, 1)" />
            </div>
            <el-button v-if="condRows.length < 8" link type="primary" icon="Plus" @click="condRows.push({ key: '', values: [], num: undefined, bool: undefined })">添加条件（多键 AND）</el-button>
            <div class="text-xs c-tip">空条件 = 命中所有人（运营显式配置行为，`08` §2.7）</div>
          </div>
        </el-form-item>
        <el-form-item label="处置档" prop="action">
          <el-select v-model="form.action" placeholder="请选择" style="width: 100%">
            <el-option v-for="dict in health_forbid_action" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="提示文案" prop="tipText">
          <el-input v-model="form.tipText" type="textarea" :rows="2" placeholder="必须能直接展示给用户（18 E13）" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="0" :max="9999" controls-position="right" />
          <span class="ml8 text-xs c-tip">冲突时小者生效</span>
        </el-form-item>
        <el-form-item label="启用" prop="enabled">
          <el-switch v-model="form.enabled" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="dialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 套餐试算弹窗 -->
    <el-dialog v-model="trial.visible" title="套餐禁忌试算" width="640px" append-to-body>
      <el-form :model="trial.form" label-width="90px">
        <el-form-item label="套餐" required>
          <el-select v-model="trial.form.setmealId" placeholder="请选择套餐" filterable style="width: 100%">
            <el-option v-for="s in setmealOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="trial.form.sex" clearable placeholder="不限" style="width: 100%">
                <el-option v-for="dict in health_sex_scope" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="trial.form.age" :min="0" :max="150" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="婚况">
              <el-select v-model="trial.form.married" clearable placeholder="不限" style="width: 100%">
                <el-option v-for="dict in health_marital_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性生活史">
              <el-select v-model="trial.form.hasSexHistory" clearable placeholder="不限" style="width: 100%">
                <el-option v-for="dict in health_sex_history" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="妊娠状态">
              <el-select v-model="trial.form.pregnancy" clearable placeholder="不限" style="width: 100%">
                <el-option v-for="dict in health_pregnancy_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="经期" label-width="50px">
              <el-switch v-model="trial.form.inMenstruation" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="金属" label-width="50px">
              <el-switch v-model="trial.form.metalImplant" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" :loading="trial.loading" @click="doTrial">开始试算</el-button>
        </el-form-item>
      </el-form>
      <template v-if="trial.result">
        <el-alert :type="trial.result.pass ? 'success' : 'error'" :closable="false" class="mb8">
          {{ trial.result.pass ? '放行（无拦截级命中）' : `拦截：${trial.result.hitCount} 条拦截级命中` }}
        </el-alert>
        <div v-for="sec in trialSections" :key="sec.label">
          <template v-if="sec.list.length">
            <div class="font-bold mb4">{{ sec.label }}（{{ sec.list.length }}）</div>
            <el-table :data="sec.list" border size="small" class="mb8">
              <el-table-column label="规则" prop="ruleCode" width="80" />
              <el-table-column label="命中项" prop="itemName" min-width="140" show-overflow-tooltip />
              <el-table-column label="提示" prop="tipText" min-width="180" show-overflow-tooltip />
            </el-table>
          </template>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Forbid" lang="ts">
import { listForbid, addForbid, updateForbid, delForbid, checkForbid } from '@/api/health/forbid';
import { ForbidVO, ForbidForm, ForbidCheckForm, ForbidCheckVO, ForbidHitVO } from '@/api/health/forbid/types';
import { listAllCheckitem } from '@/api/health/checkitem';
import { listAllCheckgroup } from '@/api/health/checkgroup';
import { listAllSetmeal } from '@/api/health/setmeal';
import { CheckitemVO } from '@/api/health/checkitem/types';
import { CheckgroupVO } from '@/api/health/checkgroup/types';
import { SetmealVO } from '@/api/health/setmeal/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { health_forbid_scope, health_forbid_action, health_forbid_cond_key, health_sex_scope, health_marital_status, health_sex_history, health_pregnancy_status } = toRefs<any>(
  proxy?.useDict('health_forbid_scope', 'health_forbid_action', 'health_forbid_cond_key', 'health_sex_scope', 'health_marital_status', 'health_sex_history', 'health_pregnancy_status')
);

interface TargetOption {
  id: number;
  code: string;
  name: string;
}

interface CondRow {
  key: string;
  values: string[];
  num?: number;
  bool?: boolean;
}

const loading = ref(true);
const buttonLoading = ref(false);
const forbidList = ref<ForbidVO[]>([]);
const checkitemOptions = ref<TargetOption[]>([]);
const checkgroupOptions = ref<TargetOption[]>([]);
const setmealOptions = ref<SetmealVO[]>([]);
const condRows = ref<CondRow[]>([{ key: '', values: [] }]);
const forbidFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: ForbidForm = {
  id: undefined,
  ruleCode: undefined,
  scope: 'CHECKITEM',
  targetId: undefined,
  conditionJson: '{}',
  action: '1',
  tipText: undefined,
  priority: 100,
  enabled: '1'
};

const data = reactive<PageData<ForbidForm, {}>>({
  form: { ...initFormData },
  queryParams: {},
  rules: {
    ruleCode: [
      { required: true, message: '规则编码不能为空', trigger: 'blur' },
      { max: 32, message: '规则编码不能超过 32 字符', trigger: 'blur' }
    ],
    scope: [{ required: true, message: '作用范围不能为空', trigger: 'change' }],
    targetId: [{ required: true, message: '目标不能为空', trigger: 'change' }],
    action: [{ required: true, message: '处置档不能为空', trigger: 'change' }],
    tipText: [{ required: true, message: '提示文案不能为空', trigger: 'blur' }]
  }
});
const { form, rules } = toRefs(data);

const trial = reactive<{ visible: boolean; loading: boolean; form: ForbidCheckForm; result: ForbidCheckVO | null }>({
  visible: false,
  loading: false,
  form: { setmealId: undefined, sex: undefined, married: undefined, hasSexHistory: undefined, pregnancy: undefined, age: undefined, inMenstruation: false, metalImplant: false },
  result: null
});

const trialSections = computed<{ label: string; list: ForbidHitVO[] }[]>(() => [
  { label: '拦截（action=0）', list: trial.result?.hits ?? [] },
  { label: '提示（action=1）', list: trial.result?.warns ?? [] },
  { label: '延后补检（action=2）', list: trial.result?.delays ?? [] }
]);

const ENUM_KEY_DICTS: Record<string, any> = {
  sex: health_sex_scope,
  married: health_marital_status,
  hasSexHistory: health_sex_history,
  pregnancy: health_pregnancy_status
};
const isEnumKey = (k: string) => k in ENUM_KEY_DICTS;
const isAgeKey = (k: string) => k === 'ageMin' || k === 'ageMax';
const isBoolKey = (k: string) => k === 'inMenstruation' || k === 'metalImplant';
const enumOptions = (k: string) => ENUM_KEY_DICTS[k]?.value ?? [];

const targetOptions = (scope?: string): TargetOption[] =>
  scope === 'CHECKGROUP' ? checkgroupOptions.value : scope === 'SETMEAL' ? setmealOptions.value.map((s) => ({ id: s.id, code: s.code, name: s.name })) : checkitemOptions.value;

const targetName = (scope: string, targetId: number) => targetOptions(scope).find((o) => o.id === targetId)?.name ?? `#${targetId}`;

/** conditionJson → 可读文本（列表列展示） */
const condText = (json: string) => {
  let obj: Record<string, any>;
  try {
    obj = JSON.parse(json || '{}');
  } catch {
    return json;
  }
  const keys = Object.keys(obj);
  if (!keys.length) return '所有人';
  const label = (k: string) => health_forbid_cond_key.value.find((d: any) => d.value === k)?.label ?? k;
  const vlabel = (k: string, v: string) => enumOptions(k).find((d: any) => d.value === v)?.label ?? v;
  return keys
    .map((k) => {
      const v = obj[k];
      if (Array.isArray(v)) return `${label(k)}∈[${v.map((x: string) => vlabel(k, x)).join(',')}]`;
      if (typeof v === 'boolean') return `${label(k)}=${v ? '是' : '否'}`;
      return `${label(k)}=${v}`;
    })
    .join(' 且 ');
};

const getList = async () => {
  loading.value = true;
  const res = await listForbid();
  forbidList.value = res.data;
  loading.value = false;
};

const getTargets = async () => {
  const [ci, cg, sm] = await Promise.all([listAllCheckitem(), listAllCheckgroup(), listAllSetmeal()]);
  checkitemOptions.value = ci.data.map((x: CheckitemVO) => ({ id: x.id, code: x.code, name: x.name }));
  checkgroupOptions.value = cg.data.map((x: CheckgroupVO) => ({ id: x.id, code: x.code, name: x.name }));
  setmealOptions.value = sm.data;
};

const onCondKeyChange = (row: CondRow) => {
  row.values = [];
  row.num = undefined;
  row.bool = undefined;
};

/** 编辑回显：conditionJson → condRows */
const parseCondRows = (json: string): CondRow[] => {
  let obj: Record<string, any>;
  try {
    obj = JSON.parse(json || '{}');
  } catch {
    return [{ key: '', values: [] }];
  }
  const rows = Object.keys(obj).map<CondRow>((k) => ({
    key: k,
    values: Array.isArray(obj[k]) ? obj[k].map(String) : [],
    num: typeof obj[k] === 'number' ? obj[k] : undefined,
    bool: typeof obj[k] === 'boolean' ? obj[k] : undefined
  }));
  return rows.length ? rows : [{ key: '', values: [] }];
};

/** 提交：condRows → conditionJson（丢弃未填完的行；未知键由后端 5013 兜底） */
const buildConditionJson = (): string => {
  const obj: Record<string, any> = {};
  for (const row of condRows.value) {
    if (!row.key) continue;
    if (isEnumKey(row.key)) {
      if (row.values.length) obj[row.key] = row.values;
    } else if (isAgeKey(row.key)) {
      if (row.num !== undefined) obj[row.key] = row.num;
    } else if (isBoolKey(row.key)) {
      if (row.bool !== undefined) obj[row.key] = row.bool;
    }
  }
  return JSON.stringify(obj);
};

const reset = () => {
  form.value = { ...initFormData };
  condRows.value = [{ key: '', values: [] }];
  forbidFormRef.value?.resetFields();
};

const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '新增禁忌规则';
};

const handleUpdate = (row: ForbidVO) => {
  reset();
  form.value = { ...row };
  condRows.value = parseCondRows(row.conditionJson);
  dialog.visible = true;
  dialog.title = `编辑禁忌规则 ${row.ruleCode}`;
};

const submitForm = () => {
  forbidFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) return;
    buttonLoading.value = true;
    form.value.conditionJson = buildConditionJson();
    try {
      if (form.value.id) {
        await updateForbid(form.value);
      } else {
        await addForbid(form.value);
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    } finally {
      buttonLoading.value = false;
    }
  });
};

const toggleEnabled = async (row: ForbidVO, v: boolean) => {
  try {
    await updateForbid({ ...row, enabled: v ? '1' : '0' });
    proxy?.$modal.msgSuccess(v ? '已启用' : '已停用');
  } catch {
    // 拦截器已弹错误提示（如 RepeatSubmit 幂等拦截），此处吞掉避免未处理 Promise 报错
  } finally {
    // 失败也回读列表，保证开关与库内一致
    await getList();
  }
};

const handleDelete = async (row: ForbidVO) => {
  await proxy?.$modal.confirm(`确认删除规则「${row.ruleCode}」吗？`);
  await delForbid(row.id);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

const openTrial = () => {
  trial.result = null;
  trial.visible = true;
};

const doTrial = async () => {
  if (!trial.form.setmealId) {
    proxy?.$modal.msgWarning('请选择套餐');
    return;
  }
  trial.loading = true;
  try {
    const res = await checkForbid(trial.form);
    trial.result = res.data;
  } finally {
    trial.loading = false;
  }
};

onMounted(async () => {
  await getTargets();
  await getList();
});
</script>

<style scoped>
.cond-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.c-tip {
  color: var(--el-text-color-secondary);
}
</style>
