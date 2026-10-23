package com.xinfei.health.appointment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.appointment.domain.bo.AppOrderBo;
import com.xinfei.health.appointment.domain.vo.AppOrderVo;

import java.time.LocalDate;

/**
 * C 端预约下单 服务层（08 §5.3、13 §5.2）
 *
 * @author xinfei
 */
public interface IAppOrderService {

    /**
     * 预检日期：已设置且剩余名额 > 0（13 §5.2 UI 步骤 2，不消费任何配额）
     *
     * @return true 可约；否则抛 5001/5002
     */
    boolean checkDate(LocalDate orderDate);

    /**
     * 预检验证码：只比对不删除（13 R5，真正消费在下单事务内）
     */
    boolean checkCode(String phone, String smsCode);

    /**
     * C 端下单（08 §5.3 九步事务；禁忌校验待 §2.7 规则表落地后叠加）
     *
     * @return 下单结果
     */
    AppOrderVo submitOrder(AppOrderBo bo);

    /**
     * 我的预约列表（13 §4.2：tab → flow_status 唯一映射，仅本人）
     *
     * @param tab 待检查/已完成/已取消/全部
     */
    TableDataInfo<AppOrderVo> myOrders(Integer memberId, String tab, PageQuery pageQuery);

    /**
     * 我的订单详情（仅本人）
     */
    AppOrderVo myOrder(Integer memberId, Integer orderId);

    /**
     * 我的订单改期（仅本人；08 §5.4 原子双更新）
     */
    void rescheduleMine(Integer memberId, Integer orderId, LocalDate targetDate);

    /**
     * 我的订单取消（仅本人；08 §5.6，reason 必填）
     */
    void cancelMine(Integer memberId, Integer orderId, String reason);

}
