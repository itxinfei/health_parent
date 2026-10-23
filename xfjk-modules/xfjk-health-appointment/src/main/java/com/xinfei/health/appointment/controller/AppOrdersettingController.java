package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.vo.OrdersettingVo;
import com.xinfei.health.appointment.service.IOrdersettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * C 端可约日期（13 §4.2：只回 remain>0 且晚于今天的日期，不回名额原值）
 *
 * @author xinfei
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/ordersettings")
@Tag(name = "C端-可约日期")
public class AppOrdersettingController extends BaseController {

    private static final int DEFAULT_MONTHS = 1;
    private static final int MAX_MONTHS = 3;

    private final IOrdersettingService ordersettingService;

    /**
     * 未来可约日期列表（P04 选日期数据源）
     */
    @Operation(summary = "查询未来可约日期")
    @GetMapping("/available")
    public List<AvailableDateVo> available(@RequestParam(defaultValue = "1") int months) {
        int m = Math.min(Math.max(months, 1), MAX_MONTHS);
        LocalDate today = LocalDate.now();
        List<OrdersettingVo> list = ordersettingService.queryByRange(today.plusDays(1), today.plusMonths(m));
        return list.stream()
            .filter(v -> v.getRemain() != null && v.getRemain() > 0)
            .map(v -> {
                AvailableDateVo vo = new AvailableDateVo();
                vo.setDate(v.getOrderDate().toString());
                vo.setRemainLabel("余 " + v.getRemain());
                return vo;
            })
            .toList();
    }

    /**
     * 只暴露日期与剩余文案，不回 number/reservations 原值（13 §4.2）
     */
    @Data
    public static class AvailableDateVo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String date;

        private String remainLabel;

    }

}
