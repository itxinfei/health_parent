<template>
  <div class="p-2">
    <el-card shadow="hover" class="mb8">
      <el-form :inline="true">
        <el-form-item label="月份">
          <el-date-picker v-model="monthPick" type="month" :clearable="false" value-format="YYYY-MM" placeholder="选择月份" style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getList">查询月历</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['health:ordersetting:edit']" type="primary" plain icon="Plus" @click="handleAddDay">设置某日名额</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-upload v-hasPermi="['health:ordersetting:import']" :show-file-list="false" accept=".xlsx,.xls" :http-request="doImport">
              <el-button plain icon="Upload" :loading="importing">Excel 批量导入</el-button>
            </el-upload>
          </el-col>
          <el-col :span="14">
            <span class="tip-text">导入列：日期(YYYY-MM-DD)、可预约人数，仅晚于今天的日期生效</span>
          </el-col>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="settingList">
        <el-table-column label="日期" prop="orderDate" width="140" />
        <el-table-column label="可预约人数" width="220">
          <template #default="scope">
            <el-input-number v-model="scope.row.number" :min="0" :max="9999" size="small" controls-position="right" style="width: 140px" />
            <el-button
              v-hasPermi="['health:ordersetting:edit']"
              link
              type="primary"
              class="ml8"
              :loading="scope.row._saving"
              @click="handleSave(scope.row)"
            >保存</el-button>
          </template>
        </el-table-column>
        <el-table-column label="已预约" prop="reservations" width="100" align="right" class-name="tabular-nums" />
        <el-table-column label="剩余名额" prop="remain" width="100" align="right" class-name="tabular-nums">
          <template #default="scope">
            <el-text :type="scope.row.remain > 0 ? 'success' : 'danger'">{{ scope.row.remain }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="星期" width="90" align="center">
          <template #default="scope">{{ weekdayText(scope.row.orderDate) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="90" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="删除某日设置（当日存在订单不可删，返回 5008）" placement="top">
              <el-button v-hasPermi="['health:ordersetting:edit']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && settingList.length === 0" description="本月尚未设置名额" />
    </el-card>

    <el-dialog v-model="addDialog.visible" title="设置某日可预约人数" width="420px" append-to-body>
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="110px">
        <el-form-item label="预约日期" prop="orderDate">
          <el-date-picker
            v-model="addForm.orderDate"
            type="date"
            value-format="YYYY-MM-DD"
            :disabled-date="(d: Date) => d.getTime() <= todayTime"
            placeholder="仅晚于今天"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="可预约人数" prop="number">
          <el-input-number v-model="addForm.number" :min="1" :max="9999" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitAddDay">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Ordersetting" lang="ts">
import { listOrdersetting, setOrdersetting, delOrdersetting, uploadOrdersetting } from '@/api/health/ordersetting';
import { OrdersettingVO } from '@/api/health/ordersetting/types';
import { UploadRequestOptions } from 'element-plus';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

interface RowOrdersettingVO extends OrdersettingVO {
  _saving?: boolean;
}

const WEEKDAYS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

const loading = ref(false);
const importing = ref(false);
const buttonLoading = ref(false);
const settingList = ref<RowOrdersettingVO[]>([]);

const now = new Date();
const monthPick = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`);
const todayTime = now.getTime();

const addFormRef = ref<ElFormInstance>();
const addDialog = reactive<DialogOption>({ visible: false, title: '' });
const addForm = reactive<{ orderDate?: string; number?: number }>({ orderDate: undefined, number: 10 });
const addRules = {
  orderDate: [{ required: true, message: '预约日期不能为空', trigger: 'change' }],
  number: [{ required: true, message: '可预约人数不能为空', trigger: 'blur' }]
};

const weekdayText = (date: string) => {
  const d = new Date(date.replace(/-/g, '/'));
  return WEEKDAYS[d.getDay()];
};

const getList = async () => {
  loading.value = true;
  try {
    const [y, m] = monthPick.value.split('-').map(Number);
    const res = await listOrdersetting(y, m);
    settingList.value = (res.data || []).map((row) => ({ ...row, _saving: false }));
  } finally {
    loading.value = false;
  }
};

const handleSave = async (row: RowOrdersettingVO) => {
  row._saving = true;
  try {
    await setOrdersetting({ orderDate: row.orderDate, number: row.number });
    proxy?.$modal.msgSuccess('已保存（不重置已预约数）');
    await getList();
  } finally {
    row._saving = false;
  }
};

const handleDelete = async (row: RowOrdersettingVO) => {
  await proxy?.$modal.confirm(`确认删除 ${row.orderDate} 的名额设置吗？当日存在订单将拒绝（5008）`);
  await delOrdersetting(row.orderDate);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

const handleAddDay = () => {
  addForm.orderDate = undefined;
  addForm.number = 10;
  addFormRef.value?.clearValidate();
  addDialog.visible = true;
};

const submitAddDay = async () => {
  await addFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) return;
    buttonLoading.value = true;
    try {
      await setOrdersetting({ orderDate: addForm.orderDate, number: addForm.number });
      proxy?.$modal.msgSuccess('设置成功');
      addDialog.visible = false;
      await getList();
    } finally {
      buttonLoading.value = false;
    }
  });
};

const doImport = async (options: UploadRequestOptions) => {
  const file = options.file as File;
  importing.value = true;
  try {
    const res = await uploadOrdersetting(file);
    await ElMessageBox.alert(res.msg || '导入完成', '导入回执', { confirmButtonText: '知道了' });
    await getList();
  } finally {
    importing.value = false;
  }
};

onMounted(() => {
  getList();
});
</script>

<style scoped>
.tip-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 32px;
}
.ml8 {
  margin-left: 8px;
}
</style>
