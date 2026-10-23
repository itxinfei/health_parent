<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb8">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="套餐编码" prop="code">
              <el-input v-model="queryParams.code" placeholder="请输入编码" clearable style="width: 160px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="套餐名称" prop="name">
              <el-input v-model="queryParams.name" placeholder="请输入名称" clearable style="width: 160px" @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['health:setmeal:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['health:setmeal:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="setmealList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="编码" prop="code" width="90" show-overflow-tooltip />
        <el-table-column label="名称" prop="name" min-width="150" show-overflow-tooltip />
        <el-table-column label="性别" prop="sex" width="80" align="center">
          <template #default="scope">
            <dict-tag :options="health_sex_scope" :value="scope.row.sex" />
          </template>
        </el-table-column>
        <el-table-column label="适用年龄" prop="age" width="100" align="center" />
        <el-table-column label="助记码" prop="helpCode" width="100" show-overflow-tooltip />
        <el-table-column label="价格(元)" prop="price" width="110" align="right" class-name="tabular-nums" />
        <el-table-column label="说明" prop="remark" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="130" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="编辑" placement="top">
              <el-button v-hasPermi="['health:setmeal:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['health:setmeal:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" append-to-body>
      <el-form ref="setmealFormRef" :model="form" :rules="rules" label-width="90px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="base">
            <el-form-item label="编码" prop="code">
              <el-input v-model="form.code" placeholder="唯一编码，≤8 字符" maxlength="8" />
            </el-form-item>
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入套餐名称" maxlength="128" />
            </el-form-item>
            <el-form-item label="助记码" prop="helpCode">
              <el-input v-model="form.helpCode" placeholder="拼音助记码，C 端检索用" maxlength="16" />
            </el-form-item>
            <el-form-item label="适用性别" prop="sex">
              <el-select v-model="form.sex" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in health_sex_scope" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="适用年龄" prop="age">
              <el-input v-model="form.age" placeholder="年龄描述，如 18-60" maxlength="32" />
            </el-form-item>
            <el-form-item label="价格(元)" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :max="9999999" controls-position="right" style="width: 100%" />
            </el-form-item>
            <el-form-item label="套餐图片">
              <div class="flex items-center gap-3">
                <el-upload :show-file-list="false" accept="image/jpeg,image/png" :http-request="doUpload">
                  <el-button icon="Upload" :loading="uploading">上传 JPG/PNG ≤2MB</el-button>
                </el-upload>
                <el-image v-if="imgUrl" :src="imgUrl" fit="cover" class="h-16 w-16 rounded border" />
                <el-button v-if="form.img" link type="primary" @click="clearImage">移除</el-button>
              </div>
            </el-form-item>
            <el-form-item label="注意事项" prop="attention">
              <el-input v-model="form.attention" type="textarea" :rows="2" maxlength="128" show-word-limit />
            </el-form-item>
            <el-form-item label="说明" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="128" show-word-limit />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="检查组" name="groups">
            <el-transfer
              v-model="checkedGroupIds"
              :data="groupOptions"
              :props="{ key: 'id', label: 'name' } as any"
              :titles="['候选检查组', '套餐含组']"
              filterable
              filter-placeholder="按名称筛选"
              :button-texts="['<', '>']"
            />
          </el-tab-pane>

          <el-tab-pane label="适用人群" name="ext">
            <el-row>
              <el-col :span="12">
                <el-form-item label="年龄下限">
                  <el-input-number v-model="form.ext.ageMin" :min="0" :max="150" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="年龄上限">
                  <el-input-number v-model="form.ext.ageMax" :min="0" :max="150" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="婚姻限制" prop="ext.marriedLimit">
              <el-select v-model="form.ext.marriedLimit" placeholder="不限" clearable style="width: 100%">
                <el-option v-for="dict in health_marital_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="性生活史" prop="ext.hasSexHistory">
              <el-select v-model="form.ext.hasSexHistory" placeholder="不限" clearable style="width: 100%">
                <el-option v-for="dict in health_sex_history" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="妊娠警示" prop="ext.pregnancyWarn">
              <el-select v-model="form.ext.pregnancyWarn" placeholder="不限" clearable style="width: 100%">
                <el-option v-for="dict in health_pregnancy_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="适用描述">
              <el-input v-model="form.ext.suitableDesc" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="仅展示用，禁止作为校验依据（18 E11）" />
            </el-form-item>
            <el-form-item label="检前须知">
              <el-input v-model="form.ext.noticeText" type="textarea" :rows="2" maxlength="1000" show-word-limit />
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

<script setup name="Setmeal" lang="ts">
import { listSetmeal, getSetmeal, getSetmealGroups, addSetmeal, updateSetmeal, delSetmeal, uploadSetmealImage } from '@/api/health/setmeal';
import { SetmealVO, SetmealQuery, SetmealForm } from '@/api/health/setmeal/types';
import { listAllCheckgroup } from '@/api/health/checkgroup';
import { CheckgroupVO } from '@/api/health/checkgroup/types';
import { UploadRequestOptions } from 'element-plus';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { health_sex_scope, health_marital_status, health_sex_history, health_pregnancy_status } = toRefs<any>(
  proxy?.useDict('health_sex_scope', 'health_marital_status', 'health_sex_history', 'health_pregnancy_status')
);

const setmealList = ref<SetmealVO[]>([]);
const groupOptions = ref<CheckgroupVO[]>([]);
const checkedGroupIds = ref<number[]>([]);
const imgUrl = ref('');
const uploading = ref(false);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<number | string>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const activeTab = ref('base');

const queryFormRef = ref<ElFormInstance>();
const setmealFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: SetmealForm = {
  id: undefined,
  name: undefined,
  code: undefined,
  helpCode: undefined,
  sex: '0',
  age: undefined,
  price: 0,
  remark: undefined,
  attention: undefined,
  img: undefined,
  checkGroupIds: [],
  ext: {
    ageMin: undefined,
    ageMax: undefined,
    marriedLimit: undefined,
    hasSexHistory: undefined,
    pregnancyWarn: undefined,
    suitableDesc: undefined,
    noticeText: undefined
  }
};

const data = reactive<PageData<SetmealForm, SetmealQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    code: undefined,
    sex: undefined
  },
  rules: {
    name: [
      { required: true, message: '套餐名称不能为空', trigger: 'blur' },
      { max: 128, message: '套餐名称长度不能超过 128 个字符', trigger: 'blur' }
    ],
    code: [
      { required: true, message: '套餐编码不能为空', trigger: 'blur' },
      { max: 8, message: '套餐编码长度不能超过 8 个字符', trigger: 'blur' }
    ],
    helpCode: [
      { required: true, message: '助记码不能为空', trigger: 'blur' },
      { max: 16, message: '助记码长度不能超过 16 个字符', trigger: 'blur' }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

const getList = async () => {
  loading.value = true;
  const res = await listSetmeal(queryParams.value);
  setmealList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const cancel = () => {
  reset();
  dialog.visible = false;
};

const reset = () => {
  form.value = { ...initFormData, ext: { ...initFormData.ext }, checkGroupIds: [] };
  checkedGroupIds.value = [];
  imgUrl.value = '';
  setmealFormRef.value?.resetFields();
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

const handleSelectionChange = (selection: SetmealVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

const doUpload = async (options: UploadRequestOptions) => {
  const file = options.file as File;
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    proxy?.$modal.msgError('仅支持 JPG/PNG');
    return;
  }
  if (file.size / 1024 / 1024 > 2) {
    proxy?.$modal.msgError('图片不能超过 2MB');
    return;
  }
  uploading.value = true;
  try {
    const res = await uploadSetmealImage(file);
    form.value.img = res.data.ossId;
    imgUrl.value = res.data.url;
  } finally {
    uploading.value = false;
  }
};

const clearImage = () => {
  form.value.img = undefined;
  imgUrl.value = '';
};

const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '新增套餐';
};

const handleUpdate = async (row?: SetmealVO) => {
  reset();
  const id = row?.id || ids.value[0];
  const [detailRes, groupsRes] = await Promise.all([getSetmeal(id), getSetmealGroups(id)]);
  Object.assign(form.value, detailRes.data);
  if (!form.value.ext) form.value.ext = { ...initFormData.ext };
  imgUrl.value = detailRes.data.imgUrl || '';
  checkedGroupIds.value = groupsRes.data.map((g) => g.id);
  dialog.visible = true;
  dialog.title = '编辑套餐';
};

const submitForm = () => {
  setmealFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      activeTab.value = 'base';
      return;
    }
    form.value.checkGroupIds = checkedGroupIds.value;
    // ext.sex 与主表 sex 同源（08 §2.5），避免两处不一致
    form.value.ext.sex = form.value.sex;
    buttonLoading.value = true;
    try {
      if (form.value.id) {
        await updateSetmeal(form.value.id, form.value);
      } else {
        await addSetmeal(form.value);
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    } finally {
      buttonLoading.value = false;
    }
  });
};

const handleDelete = async (row?: SetmealVO) => {
  const delIds = row?.id ? [row.id] : ids.value;
  await proxy?.$modal.confirm('是否确认删除选中的 ' + delIds.length + ' 个套餐？存在预约订单的套餐将返回 4001 拒绝。');
  try {
    await delSetmeal(delIds);
    proxy?.$modal.msgSuccess('删除成功');
  } finally {
    await getList();
  }
};

onMounted(async () => {
  const res = await listAllCheckgroup();
  groupOptions.value = res.data;
  getList();
});
</script>

<style scoped>
:deep(.tabular-nums) {
  font-variant-numeric: tabular-nums;
}
</style>
