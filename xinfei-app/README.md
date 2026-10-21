# 心飞健康 · 微信小程序端（uniapp）

基于 legacy 移动端页面（套餐列表/详情、登录、体检预约）生成的 uniapp 工程，Vue3 + TypeScript。

## 页面

| 页面 | 路径 | 说明 |
| --- | --- | --- |
| 套餐列表（首页） | `pages/index/index` | 体检套餐卡片列表 |
| 套餐详情 | `pages/setmeal/detail` | 套餐说明与检查项目 |
| 体检预约 | `pages/order/order` | 选择体检日期提交预约 |
| 登录 | `pages/login/login` | 手机号 + 密码登录 |

## 使用

- 接口封装在 `src/utils/request.ts`，`BASE_URL` 当前指向 `http://192.168.20.130:8180`（后端容器），接口接入新后端后替换对应路径即可；后端接口就绪前列表页使用占位数据可预览
- `manifest.json` 中微信小程序 `appid` 留空，发布前填入自己的 appid
- HBuilderX 导入本目录即可运行到微信开发者工具；CLI 方式：`npm install && npm run dev:mp-weixin`
