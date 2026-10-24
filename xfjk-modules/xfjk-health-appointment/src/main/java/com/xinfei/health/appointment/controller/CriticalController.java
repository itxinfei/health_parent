package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.bo.CriticalHandleBo;
import com.xinfei.health.appointment.domain.bo.CriticalQueryBo;
import com.xinfei.health.appointment.domain.vo.CriticalLogVo;
import com.xinfei.health.appointment.service.IHealthCriticalService;

/**
 * 危急值（07 §3.2.1 / §5.5；权限点封闭为 list / add / edit 三点）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/criticals")
@Tag(name = "危急值登记")
public class CriticalController extends BaseController {

    private final IHealthCriticalService criticalService;

    @Operation(summary = "危急值分页查询（质控清单与专人查访入口）")
    @SaCheckPermission("health:critical:list")
    @GetMapping
    public TableDataInfo<CriticalLogVo> list(CriticalQueryBo bo, PageQuery pageQuery) {
        return criticalService.queryPageList(bo, pageQuery);
    }

    @Operation(summary = "填写处置结果与接收确认")
    @SaCheckPermission("health:critical:edit")
    @Log(title = "危急值处置", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/handle")
    public R<Void> handle(@NotNull(message = "危急值id不能为空") @PathVariable Long id,
                          @RequestBody CriticalHandleBo bo) {
        criticalService.handle(id, bo);
        return R.ok();
    }

}
