<script setup lang="ts">
/** 首页：体检套餐列表 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

interface Setmeal {
  id: number
  name: string
  price: number
  remark: string
  img: string
}

const list = ref<Setmeal[]>([])

onLoad(() => {
  uni.setNavigationBarTitle({ title: '心飞健康' })
  request<Setmeal[]>({ url: '/setmeal/list' })
    .then((res) => (list.value = res.data))
    .catch(() => {
      // 后端接口接入前的占位数据，保证页面可预览
      list.value = [
        { id: 1, name: '入职体检套餐', price: 200, remark: '基础入职检查', img: '' },
        { id: 2, name: '常规体检套餐', price: 350, remark: '血常规/尿常规/胸片', img: '' },
        { id: 3, name: '尊贵VIP套餐', price: 800, remark: '全面深度体检', img: '' },
      ]
    })
})

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/setmeal/detail?id=${id}` })
}
</script>

<template>
  <view class="page">
    <view v-for="item in list" :key="item.id" class="card" @tap="goDetail(item.id)">
      <image class="card-img" :src="item.img || '/static/logo.png'" mode="aspectFill" />
      <view class="card-body">
        <view class="card-name">{{ item.name }}</view>
        <view class="card-remark">{{ item.remark }}</view>
        <view class="card-price">￥{{ item.price }}</view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 20rpx;
}
.card {
  display: flex;
  margin-bottom: 20rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  background: #fff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}
.card-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}
.card-body {
  flex: 1;
  margin-left: 20rpx;
}
.card-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1e90ff;
}
.card-remark {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #888;
}
.card-price {
  margin-top: 12rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #e6543a;
}
</style>
