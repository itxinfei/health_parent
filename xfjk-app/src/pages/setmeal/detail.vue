<script setup lang="ts">
/** 套餐详情 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

interface Setmeal {
  id: number
  name: string
  price: number
  remark: string
  detail: string
}

const item = ref<Setmeal>({ id: 0, name: '', price: 0, remark: '', detail: '' })

onLoad((query) => {
  const id = Number(query?.id ?? 0)
  request<Setmeal>({ url: `/setmeal/${id}` })
    .then((res) => (item.value = res.data))
    .catch(() => {
      // 后端接口接入前的占位数据
      item.value = {
        id,
        name: '常规体检套餐',
        price: 350,
        remark: '血常规/尿常规/胸片',
        detail: '包含一般检查、内科、外科、血常规、尿常规、心电图、胸部正位片等检查项目。',
      }
    })
})

function goOrder() {
  uni.navigateTo({ url: `/pages/order/order?setmealId=${item.value.id}` })
}
</script>

<template>
  <view class="page">
    <view class="name">{{ item.name }}</view>
    <view class="price">￥{{ item.price }}</view>
    <view class="section">
      <view class="section-title">套餐说明</view>
      <view class="section-body">{{ item.remark }}</view>
    </view>
    <view class="section">
      <view class="section-title">检查项目</view>
      <view class="section-body">{{ item.detail || '暂无详情' }}</view>
    </view>
    <button class="btn" @tap="goOrder">立即预约</button>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 30rpx;
  padding-bottom: env(safe-area-inset-bottom, 0px);
}
.name {
  font-size: 40rpx;
  font-weight: 700;
  color: #1e90ff;
}
.price {
  margin: 16rpx 0 30rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #e6543a;
}
.section {
  margin-bottom: 30rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}
.section-body {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}
.btn {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  bottom: calc(30rpx + env(safe-area-inset-bottom, 0px));
  background: #1e90ff;
  color: #fff;
  border-radius: 12rpx;
}
</style>
