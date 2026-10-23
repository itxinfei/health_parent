<template>
  <div class="app-container home">
    <section class="hero">
      <div class="hero-main">
        <h1>{{ title }}</h1>
        <p class="hero-desc">面向机构客户的健康管理系统，覆盖检查预约、套餐配置、评估与干预随访全流程。</p>
      </div>
      <el-icon class="hero-icon" :size="56"><FirstAidKit /></el-icon>
    </section>

    <h2 class="block-title">业务管理</h2>
    <el-row :gutter="16">
      <el-col v-for="card in liveCards" :key="card.path" :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="entry-card" @click="go(card.path)">
          <div class="entry-head">
            <el-icon :size="22"><component :is="card.icon" /></el-icon>
            <span class="entry-name">{{ card.name }}</span>
          </div>
          <p class="entry-desc">{{ card.desc }}</p>
          <el-tag size="small" type="success" effect="light">已上线</el-tag>
        </el-card>
      </el-col>
    </el-row>

    <h2 class="block-title">建设中</h2>
    <el-row :gutter="16">
      <el-col v-for="m in plannedModules" :key="m.name" :xs="12" :sm="8" :md="4">
        <el-card shadow="never" class="planned-card">
          <span class="planned-name">{{ m.name }}</span>
          <p class="planned-desc">{{ m.desc }}</p>
        </el-card>
      </el-col>
    </el-row>

    <p class="home-footer">{{ title }} · 管理端</p>
  </div>
</template>

<script setup name="Index" lang="ts">
const title = import.meta.env.VITE_APP_TITLE || '心飞健康';
const router = useRouter();

const liveCards = [
  { name: '检查项管理', desc: '维护检查/检验项目、扩展信息与参考区间', path: '/appointment/checkitem', icon: 'Memo' },
  { name: '检查组管理', desc: '组合检查项，配置承检科室与标本要求', path: '/appointment/checkgroup', icon: 'Files' },
  { name: '套餐管理', desc: '面向 C 端售卖的体检套餐与适用人群', path: '/appointment/setmeal', icon: 'Box' }
];

const plannedModules = [
  { name: '预约与订单', desc: '号源排期、下单支付' },
  { name: '健康评估', desc: '问卷量表与风险分层' },
  { name: '干预随访', desc: '个性化管理方案' },
  { name: '知识库', desc: '健康内容与素材' },
  { name: '企业服务', desc: '机构客户与团队预约' },
  { name: '统计报表', desc: '经营与健康指标看板' }
];

const go = (path: string) => router.push(path);
</script>

<style lang="scss" scoped>
.home {
  .hero {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 28px 32px;
    margin-bottom: 24px;
    border-radius: var(--app-radius-lg, 8px);
    background: linear-gradient(135deg, color-mix(in srgb, var(--el-color-primary) 14%, var(--el-bg-color)) 0%, var(--el-bg-color) 60%);
    border: 1px solid var(--el-border-color-lighter);

    h1 {
      margin: 0 0 8px;
      font-size: 26px;
      color: var(--el-text-color-primary);
    }

    .hero-desc {
      margin: 0;
      color: var(--el-text-color-regular);
      font-size: 14px;
    }

    .hero-icon {
      color: var(--el-color-primary);
      flex-shrink: 0;
    }
  }

  .block-title {
    font-size: 16px;
    margin: 0 0 12px;
    color: var(--el-text-color-primary);
  }

  .entry-card {
    cursor: pointer;
    margin-bottom: 16px;
    height: calc(100% - 16px);

    .entry-head {
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--el-color-primary);

      .entry-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
    }

    .entry-desc {
      margin: 10px 0 12px;
      font-size: 13px;
      color: var(--el-text-color-secondary);
      min-height: 20px;
    }
  }

  .planned-card {
    margin-bottom: 16px;
    background: var(--el-fill-color-lighter);

    .planned-name {
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .planned-desc {
      margin: 8px 0 0;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  .home-footer {
    margin-top: 24px;
    text-align: center;
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }
}
</style>
