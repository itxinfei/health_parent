/**
 * 前端运行期配置
 *
 * clientId 是 sys_client 表里的公开客户端标识（浏览器请求本来就要带），
 * 不是密钥；真正的凭据仍由用户输入、后端校验。换客户端记录时改这里即可。
 */
window.XfjkConfig = {
    // 管理端使用的 pc 客户端，对应 sys_client.client_id
    clientId: 'e5cd7e4891bf95d1d19206ce24a7b32e',
    // 登录成功后落地页
    homePage: '/pages/index.html',
    loginPage: '/login.html'
};
