<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb8">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="检查项编码" prop="code">
              <el-input v-model="queryParams.code" placeholder="请输入编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="检查项名称" prop="name">
              <el-input v-model="queryParams.name" placeholder="请输入名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="适用性别" prop="sex">
              <el-select v-model="queryParams.sex" placeholder="全部" clearable style="width: 120px">
                <el-option v-for="dict in health_sex_scope" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['health:checkitem:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['health:checkitem:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="checkitemList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="编码" prop="code" width="110" show-overflow-tooltip />
        <el-table-column label="名称" prop="name" min-width="140" show-overflow-tooltip />
        <el-table-column label="性别" prop="sex" width="80" align="center">
          <template #default="scope">
            <dict-tag :options="health_sex_scope" :value="scope.row.sex" />
          </template>
        </el-table-column>
        <el-table-column label="类型" prop="type" width="80" align="center">
          <template #default="scope">
            <dict-tag :options="health_checkitem_type" :value="scope.row.type" />
          </template>
        </el-table-column>
        <el-table-column label="价格" prop="price" width="100" align="right" class-name="tabular-nums" />
        <el-table-column label="结果类型" width="90" align="center">
          <template #default="scope">
            <dict-tag :options="health_result_type" :value="scope.row.ext?.resultType" />
          </template>
        </el-table-column>
        <el-table-column label="单位" width="80" align="center">
          <template #default="scope">{{ scope.row.ext?.unit || '-' }}</template>
        </el-table-column>
        <el-table-column label="所属科室" min-width="110" show-overflow-tooltip>
          <template #default="scope">{{ deptName(scope.row.ext?.deptId) }}</template>
        </el-table-column>
        <el-table-column label="空腹" width="70" align="center">
          <template #default="scope">
            <dict-tag :options="health_flag_yn" :value="scope.row.ext?.isFasting" />
          </template>
        </el-table-column>
        <el-table-column label="放射" width="70" align="center">
          <template #default="scope">
            <dict-tag :options="health_flag_yn" :value="scope.row.ext?.isRadiation" />
          </template>
        </el-table-column>
        <el-table-column label="必检" width="70" align="center">
          <template #default="scope">
            <dict-tag :options="health_flag_yn" :value="scope.row.ext?.isRequired" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="编辑" placement="top">
              <el-button v-hasPermi="['health:checkitem:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['health:checkitem:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" append-to-body>
      <el-form ref="checkitemFormRef" :model="form" :rules="rules" label-width="90px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="base">
            <el-form-item label="编码" prop="code">
              <el-input v-model="form.code" placeholder="唯一编码，≤16 字符" maxlength="16" />
            </el-form-item>
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入检查项名称" maxlength="32" />
            </el-form-item>
            <el-form-item label="适用性别" prop="sex">
              <el-select v-model="form.sex" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in health_sex_scope" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="适用年龄" prop="age">
              <el-input v-model="form.age" placeholder="年龄描述，如 18-60" maxlength="32" />
            </el-form-item>
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in health_checkitem_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="价格(元)" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :max="9999999" controls-position="right" style="width: 100%" />
            </el-form-item>
            <el-form-item label="注意事项" prop="attention">
              <el-input v-model="form.attention" type="textarea" :rows="2" maxlength="128" show-word-limit />
            </el-form-item>
            <el-form-item label="项目说明" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="128" show-word-limit />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="扩展信息" name="ext">
            <el-form-item label="结果类型" prop="ext.resultType">
              <el-select v-model="form.ext.resultType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in health_result_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="单位" prop="ext.unit">
              <el-input v-model="form.ext.unit" placeholder="结果类型为数值时必填，如 mmol/L" maxlength="32" />
            </el-form-item>
            <el-form-item label="所属科室" prop="ext.deptId">
              <el-tree-select
                v-model="form.ext.deptId"
                :data="deptTree"
                :props="{ value: 'deptId', label: 'deptName', children: 'children' } as any"
                value-key="deptId"
                check-strictly
                placeholder="请选择科室"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="标本" prop="ext.specimen">
              <el-select v-model="form.ext.specimen" placeholder="请选择" clearable style="width: 100%">
                <el-option v-for="dict in health_specimen" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-row>
              <el-col :span="8">
                <el-form-item label="空腹" label-width="60px">
                  <el-radio-group v-model="form.ext.isFasting">
                    <el-radio v-for="dict in health_flag_yn" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="放射" label-width="60px">
                  <el-radio-group v-model="form.ext.isRadiation">
                    <el-radio v-for="dict in health_flag_yn" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="必检" label-width="60px">
                  <el-radio-group v-model="form.ext.isRequired">
                    <el-radio v-for="dict in health_flag_yn" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="排序号">
              <el-input-number v-model="form.ext.sortNo" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
            <el-form-item label="检前须知">
              <el-input v-model="form.ext.noticeText" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="参考区间" name="refs">
            <el-table :data="form.refs" border size="small">
              <el-table-column label="性别" width="92">
                <template #default="scope">
                  <el-select v-model="scope.row.sex" size="small" placeholder="通用">
                    <el-option v-for="dict in health_sex_scope" :key="dict.value" :label="dict.label" :value="dict.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="年龄" width="130">
                <template #default="scope">
                  <div class="flex items-center gap-1">
                    <el-input-number v-model="scope.row.ageMin" size="small" :min="0" :max="150" :controls="false" placeholder="min" class="flex-1" />
                    <el-input-number v-model="scope.row.ageMax" size="small" :min="0" :max="150" :controls="false" placeholder="max" class="flex-1" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="生理状态" width="96">
                <template #default="scope">
                  <el-select v-model="scope.row.physioState" size="small" placeholder="通用">
                    <el-option v-for="dict in health_physio_state" :key="dict.value" :label="dict.label" :value="dict.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="区间原文" min-width="120">
                <template #default="scope">
                  <el-input v-model="scope.row.refText" size="small" placeholder="如 3.9-6.1" />
                </template>
              </el-table-column>
              <el-table-column label="下限" width="86">
                <template #default="scope">
                  <el-input-number v-model="scope.row.refMin" size="small" :controls="false" :precision="4" placeholder="下限" style="width: 100%" />
                </template>
              </el-table-column>
              <el-table-column label="上限" width="86">
                <template #default="scope">
                  <el-input-number v-model="scope.row.refMax" size="small" :controls="false" :precision="4" placeholder="上限" style="width: 100%" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="55" align="center">
                <template #default="scope">
                  <el-button link type="primary" icon="Delete" @click="removeRefRow(scope.$index)"></el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button class="mt-2" size="small" icon="Plus" @click="addRefRow">新增区间</el-button>
            <div class="mt-1 text-xs color-gray-400">新增检查项至少配置一条参考区间；同（性别+年龄+生理状态）组合不可重复</div>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Checkitem" lang="ts">
import { listCheckitem, getCheckitem, addCheckitem, updateCheckitem, delCheckitem } from '@/api/health/checkitem';
import { CheckitemVO, CheckitemQuery, CheckitemForm, CheckitemRefForm } from '@/api/health/checkitem/types';
import { listDept } from '@/api/system/dept';
import { DeptVO } from '@/api/system/dept/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { health_sex_scope, health_checkitem_type, health_result_type, health_specimen, health_flag_yn, health_physio_state } = toRefs<any>(
  proxy?.useDict('health_sex_scope', 'health_checkitem_type', 'health_result_type', 'health_specimen', 'health_flag_yn', 'health_physio_state')
);

const checkitemList = ref<CheckitemVO[]>([]);
const deptFlat = ref<DeptVO[]>([]);
const deptTree = ref<any[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<number | string>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const activeTab = ref('base');

const queryFormRef = ref<ElFormInstance>();
const checkitemFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: CheckitemForm = {
  id: undefined,
  code: undefined,
  name: undefined,
  sex: '0',
  age: undefined,
  price: 0,
  type: undefined,
  attention: undefined,
  remark: undefined,
  ext: {
    resultType: undefined,
    unit: undefined,
    deptId: undefined,
    specimen: undefined,
    isFasting: '0',
    isRadiation: '0',
    isRequired: '0',
    sortNo: 0,
    noticeText: undefined
  },
  refs: []
};

const data = reactive<PageData<CheckitemForm, CheckitemQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    code: undefined,
    name: undefined,
    sex: undefined
  },
  rules: {
    code: [
      { required: true, message: '检查项编码不能为空', trigger: 'blur' },
      { max: 16, message: '检查项编码长度不能超过 16 个字符', trigger: 'blur' }
    ],
    name: [
      { required: true, message: '检查项名称不能为空', trigger: 'blur' },
      { max: 32, message: '检查项名称长度不能超过 32 个字符', trigger: 'blur' }
    ],
    type: [{ required: true, message: '类型不能为空', trigger: 'change' }],
    'ext.resultType': [{ required: true, message: '结果类型不能为空', trigger: 'change' }],
    'ext.deptId': [{ required: true, message: '所属科室不能为空', trigger: 'change' }],
    'ext.unit': [
      {
        validator: (_rule: any, value: string, callback: any) => {
          // 08 §7：结果类型为数值(0)时单位必填 → 后端 5007
          if (form.value.ext?.resultType === '0' && !value) {
            callback(new Error('结果类型为数值时单位必填'));
          } else {
            callback();
          }
        },
        trigger: 'blur'
      }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

const deptName = (deptId?: number) => {
  if (!deptId) return '-';
  return deptFlat.value.find((d) => d.deptId === deptId)?.deptName ?? '-';
};

const loadDepts = async () => {
  const res = await listDept();
  deptFlat.value = res.data;
  deptTree.value = proxy?.handleTree(res.data, 'deptId', 'parentId');
};

const getList = async () => {
  loading.value = true;
  const res = await listCheckitem(queryParams.value);
  checkitemList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const cancel = () => {
  reset();
  dialog.visible = false;
};

const reset = () => {
  form.value = { ...initFormData, ext: { ...initFormData.ext }, refs: [] };
  checkitemFormRef.value?.resetFields();
  activeTab.value = 'base';
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleSelectionChange = (selection: CheckitemVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

const handleAdd = () => {
  reset();
  addRefRow();
  dialog.visible = true;
  dialog.title = '新增检查项';
};

const handleUpdate = async (row?: CheckitemVO) => {
  reset();
  const id = row?.id || ids.value[0];
  const res = await getCheckitem(id);
  Object.assign(form.value, res.data);
  if (!form.value.ext) form.value.ext = { ...initFormData.ext };
  if (!form.value.refs) form.value.refs = [];
  dialog.visible = true;
  dialog.title = '编辑检查项';
};

const addRefRow = () => {
  const row: CheckitemRefForm = { sex: '0', physioState: '0' };
  form.value.refs = [...(form.value.refs || []), row];
};

const removeRefRow = (index: number) => {
  form.value.refs!.splice(index, 1);
};

const validateRefs = (): boolean => {
  const refs = form.value.refs || [];
  if (!form.value.id && refs.length === 0) {
    proxy?.$modal.msgError('新增检查项至少配置一条参考区间');
    return false;
  }
  if (refs.some((r) => !r.refText)) {
    proxy?.$modal.msgError('参考区间的区间原文不能为空');
    return false;
  }
  const bad = refs.find((r) => r.refMin != null && r.refMax != null && r.refMin >= r.refMax);
  if (bad) {
    proxy?.$modal.msgError('参考区间下限必须小于上限');
    return false;
  }
  const key = (r: CheckitemRefForm) => `${r.sex ?? ''}|${r.ageMin ?? ''}|${r.ageMax ?? ''}|${r.physioState ?? ''}`;
  const seen = new Set<string>();
  for (const r of refs) {
    if (seen.has(key(r))) {
      proxy?.$modal.msgError('参考区间的（性别+年龄+生理状态）组合重复');
      return false;
    }
    seen.add(key(r));
  }
  return true;
};

const submitForm = () => {
  checkitemFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      activeTab.value = 'base';
      return;
    }
    if (!validateRefs()) return;
    buttonLoading.value = true;
    try {
      if (form.value.id) {
        await updateCheckitem(form.value.id, form.value);
      } else {
        await addCheckitem(form.value);
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    } finally {
      buttonLoading.value = false;
    }
  });
};

const handleDelete = async (row?: CheckitemVO) => {
  const delIds = row?.id ? [row.id] : ids.value;
  await proxy?.$modal.confirm('是否确认删除选中的 ' + delIds.length + ' 条检查项？被检查组引用的项将返回 3001 拒绝。');
  try {
    await delCheckitem(delIds);
    proxy?.$modal.msgSuccess('删除成功');
  } finally {
    await getList();
  }
};

onMounted(() => {
  loadDepts();
  getList();
});
</script>

<style scoped>
:deep(.tabular-nums) {
  font-variant-numeric: tabular-nums;
}
</style>
