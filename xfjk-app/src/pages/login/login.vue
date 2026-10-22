<script setup lang="ts">
/** 登录页 */
import { ref } from 'vue'
import { request } from '@/utils/request'

const phone = ref('')
const password = ref('')

function doLogin() {
  if (!phone.value || !password.value) {
    uni.showToast({ title: '请输入账号密码', icon: 'none' })
    return
  }
  request<{ token: string }>({
    url: '/member/login',
    method: 'POST',
    data: { phone: phone.value, password: password.value },
  })
    .then((res) => {
      uni.setStorageSync('token', res.data.token)
      uni.switchTab({ url: '/pages/index/index' })
    })
    .catch(() => {})
}
</script>

<template>
  <view class="page">
    <view class="logo">
      <image src="/static/logo.png" mode="aspectFit" class="logo-img" />
      <text class="logo-title">心飞健康</text>
    </view>
    <input v-model="phone" class="input" type="number" placeholder="请输入手机号" />
    <input v-model="password" class="input" password placeholder="请输入密码" />
    <button class="btn" @tap="doLogin">登录</button>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 120rpx 60rpx 0;
}
.logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;
}
.logo-img {
  width: 140rpx;
  height: 140rpx;
}
.logo-title {
  margin-top: 16rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #1e90ff;
}
.input {
  height: 90rpx;
  margin-bottom: 30rpx;
  padding: 0 30rpx;
  border: 1rpx solid #ddd;
  border-radius: 12rpx;
  font-size: 28rpx;
}
.btn {
  margin-top: 40rpx;
  background: #1e90ff;
  color: #fff;
  border-radius: 12rpx;
}
</style>
