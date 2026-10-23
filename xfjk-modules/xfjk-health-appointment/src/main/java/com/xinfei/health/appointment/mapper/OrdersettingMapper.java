package com.xinfei.health.appointment.mapper;

import com.xinfei.common.mybatis.core.mapper.BaseMapperPlus;
import com.xinfei.health.appointment.domain.Ordersetting;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 预约名额设置 数据层
 *
 * <p>名额增减一律走条件更新（08 §12 禁止先查后改，防超卖）。
 *
 * @author xinfei
 */
public interface OrdersettingMapper extends BaseMapperPlus<Ordersetting, Ordersetting> {

    /**
     * 条件占用一个名额：剩余 > 0 才 +1（08 §5.3 步骤 7）
     *
     * @return 受影响行数，0 表示名额已满
     */
    @Update("""
        UPDATE t_ordersetting
        SET reservations = COALESCE(reservations, 0) + 1
        WHERE orderDate = #{orderDate}
          AND COALESCE(number, 0) - COALESCE(reservations, 0) > 0
        """)
    int occupy(@Param("orderDate") LocalDate orderDate);

    /**
     * 条件释放一个名额，下限 0（08 §5.6）
     */
    @Update("""
        UPDATE t_ordersetting
        SET reservations = GREATEST(COALESCE(reservations, 0) - 1, 0)
        WHERE orderDate = #{orderDate}
        """)
    int release(@Param("orderDate") LocalDate orderDate);

    /**
     * 仅更新某日可预约人数，不触碰 reservations（08 §5.2）
     */
    @Update("""
        UPDATE t_ordersetting SET number = #{number} WHERE orderDate = #{orderDate}
        """)
    int updateNumber(@Param("orderDate") LocalDate orderDate, @Param("number") Integer number);

}
