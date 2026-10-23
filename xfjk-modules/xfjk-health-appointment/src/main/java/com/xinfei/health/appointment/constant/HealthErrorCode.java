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

    private HealthErrorCode() {
    }

}
