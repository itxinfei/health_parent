package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.bo.CheckgroupBo;
import com.xinfei.health.appointment.domain.vo.CheckgroupVo;
import com.xinfei.health.appointment.domain.vo.CheckitemVo;
import com.xinfei.health.appointment.service.ICheckgroupService;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 检查组管理（08 §3.2）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/checkgroups")
@Tag(name = "管理端-检查组")
public class CheckgroupController extends BaseController {

    private final ICheckgroupService checkgroupService;

    /**
     * 分页查询检查组
     */
    @Operation(summary = "分页查询检查组")
    @SaCheckPermission("health:checkgroup:list")
    @GetMapping
    public TableDataInfo<CheckgroupVo> list(CheckgroupBo bo, PageQuery pageQuery) {
        return checkgroupService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询全部检查组（供套餐选择）
     */
    @Operation(summary = "查询全部检查组")
    @SaCheckPermission("health:checkgroup:query")
    @GetMapping("/all")
    public R<List<CheckgroupVo>> all(CheckgroupBo bo) {
        return R.ok(checkgroupService.queryList(bo));
    }

    /**
     * 查询检查组详情
     */
    @Operation(summary = "查询检查组详情")
    @SaCheckPermission("health:checkgroup:query")
    @GetMapping("/{id}")
    public R<CheckgroupVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return R.ok(checkgroupService.queryById(id));
    }

    /**
     * 查询组内检查项
     */
    @Operation(summary = "查询组内检查项")
    @SaCheckPermission("health:checkgroup:query")
    @GetMapping("/{id}/checkitems")
    public R<List<CheckitemVo>> checkItems(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return R.ok(checkgroupService.queryCheckItems(id));
    }

    /**
     * 新增检查组（含关联检查项）
     */
    @Operation(summary = "新增检查组")
    @SaCheckPermission("health:checkgroup:add")
    @Log(title = "检查组管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Integer> add(@Validated @RequestBody CheckgroupBo bo) {
        return R.ok(checkgroupService.insertByBo(bo));
    }

    /**
     * 编辑检查组（关联差异更新）
     */
    @Operation(summary = "编辑检查组")
    @SaCheckPermission("health:checkgroup:edit")
    @Log(title = "检查组管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}")
    public R<Void> edit(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                        @Validated @RequestBody CheckgroupBo bo) {
        bo.setId(id);
        return toAjax(checkgroupService.updateByBo(bo));
    }

    /**
     * 删除检查组（被套餐引用返回 3002）
     */
    @Operation(summary = "删除检查组")
    @SaCheckPermission("health:checkgroup:remove")
    @Log(title = "检查组管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return toAjax(checkgroupService.deleteById(id));
    }

}
