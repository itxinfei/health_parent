/**
 * 后端响应适配层
 *
 * 新后端统一返回 {code, msg, data} 且 HTTP 状态恒为 200；分页接口返回 TableDataInfo
 * {code, msg, rows, total}（没有 data 字段）；页面沿用传智健康的旧约定（rows/total 平铺、
 * flag/message 表示结果）。这里一次性转换，避免 16 个页面各写一遍。
 *
 * 关键作用：后端未实现的路径会返回 code=404 而 HTTP 仍是 200，axios 不会 reject，
 * 页面于是静默显示"暂无数据"。本层把这种情况转成显式提示。
 */
(function () {
    if (typeof axios === 'undefined') {
        console.error('[api] 未找到全局 axios，请把 js/api.js 放在 axios 之后引入');
        return;
    }

    var TOKEN_KEY = 'xfjk_token';
    var USER_KEY = 'xfjk_username';
    var ADDRESS_MISSING = '请求地址不存在';

    function notify(text) {
        if (typeof ELEMENT !== 'undefined' && ELEMENT.Message) {
            ELEMENT.Message.error(text);
        }
    }

    // 拦截器已经弹过提示，调用点漏写 .catch 时不该再刷一条 unhandledrejection 噪音
    function markNotified(error) {
        if (error && typeof error === 'object') {
            error.xfjkNotified = true;
        }
        return error;
    }
    window.addEventListener('unhandledrejection', function (event) {
        if (event.reason && event.reason.xfjkNotified) {
            event.preventDefault();
        }
    });

    // status/flag/message 定义为不可枚举属性，避免回填表单时被 JSON.stringify 带进请求体
    function wrap(payload, msg) {
        // 标量（如上传返回的文件名）原样返回，页面直接当值用
        if (typeof payload === 'string' || typeof payload === 'number' || typeof payload === 'boolean') {
            return payload;
        }
        var data;
        if (Array.isArray(payload)) {
            data = payload;
        } else if (payload && typeof payload === 'object') {
            data = Object.assign({}, payload);
        } else {
            data = {};
        }
        Object.defineProperties(data, {
            status: { value: 0, enumerable: false },
            flag: { value: true, enumerable: false },
            message: { value: msg || '', enumerable: false }
        });
        return data;
    }

    axios.interceptors.request.use(function (config) {
        config.headers = config.headers || {};
        if (window.XfjkConfig && window.XfjkConfig.clientId) {
            config.headers.ClientID = window.XfjkConfig.clientId;
        }
        var token = localStorage.getItem(TOKEN_KEY);
        if (token) {
            config.headers.Authorization = 'Bearer ' + token;
        }
        return config;
    });

    // 分页信封（后端 TableDataInfo）没有 data 字段，rows/total 在顶层，整体透传
    function isPageEnvelope(body) {
        return Object.prototype.hasOwnProperty.call(body, 'rows');
    }

    function wrapPage(payload) {
        var data = {
            rows: payload.rows || [],
            total: payload.total || 0
        };
        Object.defineProperties(data, {
            status: { value: 0, enumerable: false },
            flag: { value: true, enumerable: false },
            message: { value: payload.msg || '', enumerable: false }
        });
        return data;
    }

    axios.interceptors.response.use(function (response) {
        var body = response.data;
        // 文件流等非统一封装的响应原样放行
        if (!body || typeof body !== 'object' || typeof body.code !== 'number') {
            return response;
        }
        if (body.code !== 200 && body.code !== 0) {
            var path = (response.config && response.config.url) || '';
            var text = (body.code === 404 && body.msg === ADDRESS_MISSING)
                ? '后端接口未实现：' + path
                : (body.msg || '请求失败（code=' + body.code + '）');
            console.error('[api] ' + text);
            notify(text);
            if (body.code === 401) {
                localStorage.removeItem(TOKEN_KEY);
                window.location.href = (window.XfjkConfig && window.XfjkConfig.loginPage) || '/login.html';
            }
            return Promise.reject(markNotified(new Error(text)));
        }
        response.data = isPageEnvelope(body) ? wrapPage(body) : wrap(body.data, body.msg);
        return response;
    }, function (error) {
        // 真正的网络/HTTP 层错误（如 nginx 502）
        notify('请求失败：' + (error.message || '网络异常'));
        return Promise.reject(markNotified(error));
    });

    window.XfjkApi = {
        /**
         * el-upload 自己发请求，不走 axios 拦截器，鉴权头要手工拼。
         * 后端 SaInterceptor 会校验 header/param 的 clientid 与 token 内的是否一致，
         * 少了它即使 token 正确也会被判未登录。
         */
        uploadHeaders: function () {
            var headers = {};
            if (window.XfjkConfig && window.XfjkConfig.clientId) {
                headers.ClientID = window.XfjkConfig.clientId;
            }
            var token = localStorage.getItem(TOKEN_KEY);
            if (token) {
                headers.Authorization = 'Bearer ' + token;
            }
            return headers;
        },

        /**
         * el-upload 的回调拿到的是原始响应体，不经过 axios 拦截器，
         * 用这个统一判定，避免三个上传页各写一套字段兼容逻辑。
         */
        fromUpload: function (body) {
            var ok = !!body && (body.code === 200 || body.code === 0 || body.flag === true);
            var text = (body && (body.msg || body.message)) || '';
            var data = (body && body.data !== undefined && body.data !== null) ? body.data : null;
            return { ok: ok, text: text, data: data };
        }
    };

    window.XfjkAuth = {
        TOKEN_KEY: TOKEN_KEY,
        token: function () {
            return localStorage.getItem(TOKEN_KEY);
        },
        // 登录名只做展示用；权威身份以后端 /auth/list 一类接口为准
        username: function () {
            return localStorage.getItem(USER_KEY) || '';
        },
        signin: function (token, username) {
            localStorage.setItem(TOKEN_KEY, token);
            if (username) {
                localStorage.setItem(USER_KEY, username);
            }
        },
        signout: function () {
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(USER_KEY);
        }
    };
})();
