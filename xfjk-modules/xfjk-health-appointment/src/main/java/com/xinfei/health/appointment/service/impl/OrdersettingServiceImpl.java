package com.xinfei.health.appointment.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.Ordersetting;
import com.xinfei.health.appointment.domain.bo.OrdersettingImportBo;
import com.xinfei.health.appointment.domain.vo.OrdersettingVo;
import com.xinfei.health.appointment.mapper.OrderMapper;
import com.xinfei.health.appointment.mapper.OrdersettingMapper;
import com.xinfei.health.appointment.service.IOrdersettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 预约名额设置 服务层处理（08 §4.4、§5.2）
 *
 * @author xinfei
 */
@RequiredArgsConstructor
@Service
public class OrdersettingServiceImpl implements IOrdersettingService {

    private final OrdersettingMapper baseMapper;
    private final OrderMapper orderMapper;

    @Override
    public List<OrdersettingVo> queryByMonth(Integer year, Integer month) {
        YearMonth ym = YearMonth.of(year, month);
        return queryByRange(ym.atDay(1), ym.atEndOfMonth());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importExcel(List<OrdersettingImportBo> rows) {
        if (CollUtil.isEmpty(rows)) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        int ok = 0;
        for (OrdersettingImportBo row : rows) {
            // 仅导入晚于今天的日期（08 §5.2），非法行直接丢弃由回执提示
            LocalDate date;
            try {
                date = LocalDate.parse(StringUtils.trimToEmpty(row.getOrderDate()));
            } catch (Exception e) {
                continue;
            }
            Integer number;
            try {
                number = Integer.valueOf(StringUtils.trimToEmpty(row.getNumber()));
            } catch (Exception e) {
                continue;
            }
            if (date.isAfter(today) && number > 0 && setNumber(date, number)) {
                ok++;
            }
        }
        return ok;
    }

    @Override
    public List<OrdersettingVo> queryByRange(LocalDate begin, LocalDate end) {
        List<Ordersetting> list = baseMapper.selectList(Wrappers.<Ordersetting>lambdaQuery()
            .ge(Ordersetting::getOrderDate, begin)
            .le(Ordersetting::getOrderDate, end)
            .orderByAsc(Ordersetting::getOrderDate));
        return list.stream().map(this::toVo).toList();
    }

    @Override
    public Boolean setNumber(LocalDate orderDate, Integer number) {
        if (ObjectUtil.isNull(number) || number <= 0) {
            throw new ServiceException("可预约人数必须为正整数", HealthErrorCode.ORDERSETTING_INVALID);
        }
        int updated = baseMapper.updateNumber(orderDate, number);
        if (updated == 0) {
            Ordersetting entity = new Ordersetting();
            entity.setOrderDate(orderDate);
            entity.setNumber(number);
            entity.setReservations(0);
            baseMapper.insert(entity);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByDate(LocalDate orderDate) {
        if (orderMapper.exists(
            Wrappers.<com.xinfei.health.appointment.domain.Order>lambdaQuery()
                .eq(com.xinfei.health.appointment.domain.Order::getOrderDate, orderDate))) {
            throw new ServiceException("该日存在预约订单，不可删除", HealthErrorCode.ORDERSETTING_INVALID);
        }
        return baseMapper.delete(Wrappers.<Ordersetting>lambdaQuery()
            .eq(Ordersetting::getOrderDate, orderDate)) > 0;
    }

    @Override
    public boolean occupy(LocalDate orderDate) {
        return baseMapper.occupy(orderDate) > 0;
    }

    @Override
    public void release(LocalDate orderDate) {
        baseMapper.release(orderDate);
    }

    /**
     * 转 VO，remain 由 COALESCE 口径计算（08 §5.2）
     */
    private OrdersettingVo toVo(Ordersetting entity) {
        OrdersettingVo vo = new OrdersettingVo();
        vo.setOrderDate(entity.getOrderDate());
        vo.setNumber(entity.getNumber());
        vo.setReservations(ObjectUtil.defaultIfNull(entity.getReservations(), 0));
        vo.setRemain(Math.max(ObjectUtil.defaultIfNull(entity.getNumber(), 0) - vo.getReservations(), 0));
        return vo;
    }

}
