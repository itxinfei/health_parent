/** 服务端接口封装（微信小程序端） */
export const BASE_URL = 'http://192.168.20.130:8180'

export interface ApiResult<T = unknown> {
  code: number
  msg: string
  data: T
}

/** 统一请求：自动携带登录态，非 200 统一 toast */
export function request<T = unknown>(options: UniApp.RequestOptions): Promise<ApiResult<T>> {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') as string | undefined
    uni.request({
      ...options,
      url: BASE_URL + options.url,
      header: {
        ...(options.header ?? {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      success: (res) => {
        const body = res.data as ApiResult<T>
        if (body.code === 200 || body.code === 0) {
          resolve(body)
        } else {
          uni.showToast({ title: body.msg || '请求失败', icon: 'none' })
          reject(body)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      },
    })
  })
}
