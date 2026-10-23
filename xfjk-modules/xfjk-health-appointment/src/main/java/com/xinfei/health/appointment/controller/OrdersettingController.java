package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.excel.core.ExcelResult;
import com.xinfei.common.excel.utils.ExcelUtil;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.bo.OrdersettingImportBo;
import com.xinfei.health.appointment.domain.vo.OrdersettingVo;
import com.xinfei.health.appointment.service.IOrdersettingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约名额设置（08 §4.4）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/ordersettings")
@Tag(name = "管理端-预约设置")
public class OrdersettingController extends BaseController {

    private final IOrdersettingService ordersettingService;

    /**
     * Excel 批量导入名额（仅接受晚于今天的日期，失败行在回执中提示）
     */
    @Operation(summary = "预约名额 Excel 批量导入")
    @SaCheckPermission("health:ordersetting:import")
    @Log(title = "预约设置", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> upload(@RequestPart("file") MultipartFile file) throws Exception {
        ExcelResult<OrdersettingImportBo> result = ExcelUtil.importExcel(file.getInputStream(),
            OrdersettingImportBo.class, true);
        int ok = ordersettingService.importExcel(result.getList());
        int bad = result.getErrorList().size();
        return R.ok("导入成功 " + ok + " 行" + (bad > 0 ? "，失败 " + bad + " 行：" + String.join("；", result.getErrorList()) : ""));
    }

    /**
     * 按月历查询名额
     */
    @Operation(summary = "月历查询预约名额")
    @SaCheckPermission("health:ordersetting:list")
    @GetMapping
    public R<List<OrdersettingVo>> list(@NotNull(message = "年份不能为空")
                                        @Min(2000) @Max(2999) Integer year,
                                        @NotNull(message = "月份不能为空")
                                        @Min(1) @Max(12) Integer month) {
        return R.ok(ordersettingService.queryByMonth(year, month));
    }

    /**
     * 按起止日期查询剩余名额（团检备单预览依赖）
     */
    @Operation(summary = "按日期区间查询剩余名额")
    @SaCheckPermission("health:ordersetting:list")
    @GetMapping("/range")
    public R<List<OrdersettingVo>> range(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return R.ok(ordersettingService.queryByRange(begin, end));
    }

    /**
     * 设置某日可预约人数（只更新 number，不重置 reservations）
     */
    @Operation(summary = "设置某日可预约人数")
    @SaCheckPermission("health:ordersetting:edit")
    @Log(title = "预约设置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> setNumber(@RequestBody OrdersettingVo vo) {
        if (vo.getOrderDate() == null || vo.getNumber() == null) {
            return R.fail("预约日期与可预约人数不能为空");
        }
        return toAjax(ordersettingService.setNumber(vo.getOrderDate(), vo.getNumber()));
    }

    /**
     * 删除某日设置（存在订单返回 5008）
     */
    @Operation(summary = "删除某日预约设置")
    @SaCheckPermission("health:ordersetting:edit")
    @Log(title = "预约设置", businessType = BusinessType.DELETE)
    @DeleteMapping
    public R<Void> remove(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDate) {
        return toAjax(ordersettingService.deleteByDate(orderDate));
    }

}
