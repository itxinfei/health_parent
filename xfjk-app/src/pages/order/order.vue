<script setup lang="ts">
/** 体检预约：选择体检日期并提交 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

const setmealId = ref(0)
const date = ref('')
const today = new Date().toISOString().slice(0, 10)

onLoad((query) => {
  setmealId.value = Number(query?.setmealId ?? 0)
})

function onDateChange(e: { detail: { value: string } }) {
  date.value = e.detail.value
}

function submit() {
  if (!date.value) {
    uni.showToast({ title: '请选择体检日期', icon: 'none' })
    return
  }
  request({
    url: '/order/submit',
    method: 'POST',
    data: { setmealId: setmealId.value, orderDate: date.value },
  })
    .then(() => {
      uni.showToast({ title: '预约成功', icon: 'success' })
      // pages.json 未配置 tabBar，switchTab 会报 "can not switch to no-tabBar page"
      setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 1500)
    })
    .catch(() => {})
}
</script>

<template>
  <view class="page">
    <view class="row">
      <text class="label">体检套餐</text>
      <text class="value">ID: {{ setmealId }}</text>
    </view>
    <view class="row">
      <text class="label">体检日期</text>
      <picker mode="date" :start="today" @change="onDateChange">
        <view class="picker">{{ date || '请选择日期' }}</view>
      </picker>
    </view>
    <button class="btn" @tap="submit">提交预约</button>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 30rpx;
}
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #eee;
}
.label {
  font-size: 30rpx;
}
.value,
.picker {
  font-size: 28rpx;
  color: #1e90ff;
}
.btn {
  margin-top: 60rpx;
  background: #1e90ff;
  color: #fff;
  border-radius: 12rpx;
}
</style>
