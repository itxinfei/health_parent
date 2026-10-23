<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb8">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="检查组编码" prop="code">
              <el-input v-model="queryParams.code" placeholder="请输入编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="检查组名称" prop="name">
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
            <el-button v-hasPermi="['health:checkgroup:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['health:checkgroup:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="checkgroupList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="编码" prop="code" width="120" show-overflow-tooltip />
        <el-table-column label="名称" prop="name" min-width="140" show-overflow-tooltip />
        <el-table-column label="性别" prop="sex" width="80" align="center">
          <template #default="scope">
            <dict-tag :options="health_sex_scope" :value="scope.row.sex" />
          </template>
        </el-table-column>
        <el-table-column label="助记码" prop="helpCode" width="110" show-overflow-tooltip />
        <el-table-column label="承检科室" min-width="110" show-overflow-tooltip>
          <template #default="scope">{{ deptName(scope.row.ext?.deptId) }}</template>
        </el-table-column>
        <el-table-column label="说明" prop="remark" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="130" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="编辑" placement="top">
              <el-button v-hasPermi="['health:checkgroup:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['health:checkgroup:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" append-to-body>
      <el-form ref="checkgroupFormRef" :model="form" :rules="rules" label-width="90px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="base">
            <el-form-item label="编码" prop="code">
              <el-input v-model="form.code" placeholder="唯一编码，≤32 字符" maxlength="32" />
            </el-form-item>
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入检查组名称" maxlength="32" />
            </el-form-item>
            <el-form-item label="适用性别" prop="sex">
              <el-select v-model="form.sex" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in health_sex_scope" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="助记码" prop="helpCode">
              <el-input v-model="form.helpCode" placeholder="拼音助记码" maxlength="32" />
            </el-form-item>
            <el-form-item label="注意事项" prop="attention">
              <el-input v-model="form.attention" type="textarea" :rows="2" maxlength="128" show-word-limit />
            </el-form-item>
            <el-form-item label="说明" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="128" show-word-limit />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="检查项" name="items">
            <el-transfer
              v-model="checkedItemIds"
              :data="itemOptions"
              :props="{ key: 'id', label: 'name' } as any"
              :titles="['候选检查项', '组内检查项']"
              filterable
              filter-placeholder="按名称筛选"
              :button-texts="['<', '>']"
            />
          </el-tab-pane>

          <el-tab-pane label="扩展信息" name="ext">
            <el-form-item label="承检科室" prop="ext.deptId">
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
              <el-col :span="12">
                <el-form-item label="双科室分检" label-width="100px">
                  <el-radio-group v-model="form.ext.dualDeptFlag">
                    <el-radio v-for="dict in health_flag_yn" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="空腹" label-width="60px">
                  <el-radio-group v-model="form.ext.isFasting">
                    <el-radio v-for="dict in health_flag_yn" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="排序号">
              <el-input-number v-model="form.ext.sortNo" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
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

<script setup name="Checkgroup" lang="ts">
import { listCheckgroup, getCheckgroup, getCheckgroupItems, addCheckgroup, updateCheckgroup, delCheckgroup } from '@/api/health/checkgroup';
import { CheckgroupVO, CheckgroupQuery, CheckgroupForm } from '@/api/health/checkgroup/types';
import { listAllCheckitem } from '@/api/health/checkitem';
import { CheckitemVO } from '@/api/health/checkitem/types';
import { listDept } from '@/api/system/dept';
import { DeptVO } from '@/api/system/dept/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { health_sex_scope, health_specimen, health_flag_yn } = toRefs<any>(proxy?.useDict('health_sex_scope', 'health_specimen', 'health_flag_yn'));

const checkgroupList = ref<CheckgroupVO[]>([]);
const itemOptions = ref<CheckitemVO[]>([]);
const checkedItemIds = ref<number[]>([]);
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
const checkgroupFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: CheckgroupForm = {
  id: undefined,
  code: undefined,
  name: undefined,
  helpCode: undefined,
  sex: '0',
  remark: undefined,
  attention: undefined,
  checkItemIds: [],
  ext: {
    deptId: undefined,
    specimen: undefined,
    dualDeptFlag: '0',
    isFasting: '0',
    sortNo: 0
  }
};

const data = reactive<PageData<CheckgroupForm, CheckgroupQuery>>({
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
      { required: true, message: '检查组编码不能为空', trigger: 'blur' },
      { max: 32, message: '检查组编码长度不能超过 32 个字符', trigger: 'blur' }
    ],
    name: [
      { required: true, message: '检查组名称不能为空', trigger: 'blur' },
      { max: 32, message: '检查组名称长度不能超过 32 个字符', trigger: 'blur' }
    ],
    'ext.deptId': [{ required: true, message: '承检科室不能为空', trigger: 'change' }]
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
  const res = await listCheckgroup(queryParams.value);
  checkgroupList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const cancel = () => {
  reset();
  dialog.visible = false;
};

const reset = () => {
  form.value = { ...initFormData, ext: { ...initFormData.ext }, checkItemIds: [] };
  checkedItemIds.value = [];
  checkgroupFormRef.value?.resetFields();
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

const handleSelectionChange = (selection: CheckgroupVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

const handleAdd = async () => {
  reset();
  dialog.visible = true;
  dialog.title = '新增检查组';
};

const handleUpdate = async (row?: CheckgroupVO) => {
  reset();
  const id = row?.id || ids.value[0];
  const [detailRes, itemsRes] = await Promise.all([getCheckgroup(id), getCheckgroupItems(id)]);
  Object.assign(form.value, detailRes.data);
  if (!form.value.ext) form.value.ext = { ...initFormData.ext };
  checkedItemIds.value = itemsRes.data.map((i) => i.id);
  dialog.visible = true;
  dialog.title = '编辑检查组';
};

const submitForm = () => {
  checkgroupFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      activeTab.value = 'base';
      return;
    }
    form.value.checkItemIds = checkedItemIds.value;
    buttonLoading.value = true;
    try {
      if (form.value.id) {
        await updateCheckgroup(form.value.id, form.value);
      } else {
        await addCheckgroup(form.value);
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    } finally {
      buttonLoading.value = false;
    }
  });
};

const handleDelete = async (row?: CheckgroupVO) => {
  const delIds = row?.id ? [row.id] : ids.value;
  await proxy?.$modal.confirm('是否确认删除选中的 ' + delIds.length + ' 个检查组？删除将同时解除其下检查项关联；被套餐引用将返回 3002 拒绝。');
  try {
    await delCheckgroup(delIds);
    proxy?.$modal.msgSuccess('删除成功');
  } finally {
    await getList();
  }
};

onMounted(async () => {
  loadDepts();
  const res = await listAllCheckitem();
  itemOptions.value = res.data;
  getList();
});
</script>
