package com.xinfei.health.appointment.service;

import com.xinfei.health.appointment.domain.bo.OrdersettingImportBo;
import com.xinfei.health.appointment.domain.vo.OrdersettingVo;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约名额设置 服务层（08 §4.4、§5.2）
 *
 * @author xinfei
 */
public interface IOrdersettingService {

    /**
     * Excel 批量导入（08 §5.2：仅导入晚于今天的日期；已存在只更新 number 不重置 reservations）
     *
     * @return 成功行数
     */
    int importExcel(List<OrdersettingImportBo> rows);

    /**
     * 按月查询名额（月历）
     *
     * @param year  年
     * @param month 月（1-12）
     */
    List<OrdersettingVo> queryByMonth(Integer year, Integer month);

    /**
     * 按起止日期查询剩余名额（20 §8.2 备单预览依赖）
     */
    List<OrdersettingVo> queryByRange(LocalDate begin, LocalDate end);

    /**
     * 设置某日可预约人数：存在则只更新 number，不存在则插入 reservations=0；不重置 reservations
     */
    Boolean setNumber(LocalDate orderDate, Integer number);

    /**
     * 删除某日设置；该日存在订单返回 5008
     */
    Boolean deleteByDate(LocalDate orderDate);

    /**
     * 条件占用一个名额（同一事务内调用，08 §5.3 步骤 7）
     *
     * @return true 占用成功；false 名额已满
     */
    boolean occupy(LocalDate orderDate);

    /**
     * 条件释放一个名额，下限 0
     */
    void release(LocalDate orderDate);

}
