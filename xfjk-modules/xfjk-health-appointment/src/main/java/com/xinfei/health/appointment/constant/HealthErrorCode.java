package com.xinfei.health.appointment.constant;

/**
 * 预约管理业务错误码（口径见 03 §3、08 §4.1）
 *
 * @author xinfei
 */
public final class HealthErrorCode {

    /**
     * 检查项被检查组引用，不可删除
     */
    public static final int CHECKITEM_REFERENCED = 3001;

    /**
     * 检查组被套餐引用，不可删除
     */
    public static final int CHECKGROUP_REFERENCED = 3002;

    /**
     * 套餐下存在预约订单，不可删除
     */
    public static final int SETMEAL_ORDER_EXISTS = 4001;

    /**
     * 短信验证码错误或已过期（03 §3 已登记）
     */
    public static final int SMS_CODE_INVALID = 2001;

    /**
     * 预约日期未设置名额
     */
    public static final int ORDERSETTING_NOT_FOUND = 5001;

    /**
     * 名额已满 / 条件更新失败（防超卖回滚）
     */
    public static final int ORDER_QUOTA_FULL = 5002;

    /**
     * 重复预约（同会员/同日/同套餐）
     */
    public static final int ORDER_DUPLICATE = 5003;

    /**
     * 非法状态迁移或前置状态不满足
     */
    public static final int ORDER_FLOW_ILLEGAL = 5006;

    /**
     * 预约设置非法（人数非正整数 / 该日存在订单不可删除）
     */
    public static final int ORDERSETTING_INVALID = 5008;

    /**
     * 命中拦截级禁忌，禁止下单或改期
     */
    public static final int FORBID_BLOCKED = 5009;

    /**
     * 禁忌规则非法（未知条件键 / ruleCode 重复 / scope 非法）
     */
    public static final int FORBID_INVALID = 5013;

    /**
     * 扩展信息必填项缺失（ext 缺失 / resultType 越界 / resultType=0 无 unit / 无参考区间）
     */
    public static final int EXT_REQUIRED = 5007;

    /**
     * 编码重复（检查项/检查组/套餐，08 §8 替代散落的「已存在」提示）
     */
    public static final int CODE_DUPLICATE = 5011;

    /**
     * 参考区间非法（上下限倒置 / 小数超 4 位 / 组合重复）
     */
    public static final int REF_INVALID = 5012;

    // ---------- 会员档案块 6001~6013（07 §7.1，03 §3 已登记） ----------

    /**
     * 会员不存在
     */
    public static final int MEMBER_NOT_FOUND = 6001;

    /**
     * 档案号生成冲突（重试 3 次仍撞唯一索引，07 §2.2）
     */
    public static final int ARCHIVE_NO_CONFLICT = 6002;

    /**
     * 身份证号不合法（18 H13 校验，07 §7）
     */
    public static final int IDCARD_INVALID = 6005;

    /**
     * 血压录入非法（尾数非偶，18 B5 同规则，07 §7）
     */
    public static final int BP_INVALID = 6007;

    /**
     * 导出行数超限（06 §9，会员/统计导出共用）
     */
    public static final int EXPORT_ROW_LIMIT = 6104;

    private HealthErrorCode() {
    }

}
