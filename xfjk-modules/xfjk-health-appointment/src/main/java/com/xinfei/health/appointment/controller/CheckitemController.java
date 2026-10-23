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
import com.xinfei.health.appointment.domain.HealthCheckitemRef;
import com.xinfei.health.appointment.domain.bo.CheckitemBo;
import com.xinfei.health.appointment.domain.bo.CheckitemRefBo;
import com.xinfei.health.appointment.domain.vo.CheckitemVo;
import com.xinfei.health.appointment.service.ICheckitemService;
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
 * 检查项管理（08 §3.1）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/checkitems")
@Tag(name = "管理端-检查项")
public class CheckitemController extends BaseController {

    private final ICheckitemService checkitemService;

    /**
     * 分页查询检查项
     */
    @Operation(summary = "分页查询检查项")
    @SaCheckPermission("health:checkitem:list")
    @GetMapping
    public TableDataInfo<CheckitemVo> list(CheckitemBo bo, PageQuery pageQuery) {
        return checkitemService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询全部检查项（供检查组选择）
     */
    @Operation(summary = "查询全部检查项")
    @SaCheckPermission("health:checkitem:query")
    @GetMapping("/all")
    public R<List<CheckitemVo>> all(CheckitemBo bo) {
        return R.ok(checkitemService.queryList(bo));
    }

    /**
     * 查询检查项详情
     */
    @Operation(summary = "查询检查项详情")
    @SaCheckPermission("health:checkitem:query")
    @GetMapping("/{id}")
    public R<CheckitemVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return R.ok(checkitemService.queryById(id));
    }

    /**
     * 新增检查项
     */
    @Operation(summary = "新增检查项")
    @SaCheckPermission("health:checkitem:add")
    @Log(title = "检查项管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Integer> add(@Validated @RequestBody CheckitemBo bo) {
        return R.ok(checkitemService.insertByBo(bo));
    }

    /**
     * 编辑检查项
     */
    @Operation(summary = "编辑检查项")
    @SaCheckPermission("health:checkitem:edit")
    @Log(title = "检查项管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}")
    public R<Void> edit(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                        @Validated @RequestBody CheckitemBo bo) {
        bo.setId(id);
        return toAjax(checkitemService.updateByBo(bo));
    }

    /**
     * 删除检查项（被检查组引用返回 3001）
     */
    @Operation(summary = "删除检查项")
    @SaCheckPermission("health:checkitem:remove")
    @Log(title = "检查项管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return toAjax(checkitemService.deleteById(id));
    }

    /**
     * 查询参考区间列表（08 §4.1）
     */
    @Operation(summary = "查询参考区间列表")
    @SaCheckPermission("health:checkitem:query")
    @GetMapping("/{id}/refs")
    public R<List<HealthCheckitemRef>> refs(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return R.ok(checkitemService.queryRefs(id));
    }

    /**
     * 新增参考区间（5012 校验：上下限/小数位/组合唯一）
     */
    @Operation(summary = "新增参考区间")
    @SaCheckPermission("health:checkitem:edit")
    @Log(title = "检查项管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/{id}/refs")
    public R<Long> addRef(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                          @Validated @RequestBody CheckitemRefBo bo) {
        return R.ok(checkitemService.insertRef(id, bo));
    }

    /**
     * 编辑参考区间
     */
    @Operation(summary = "编辑参考区间")
    @SaCheckPermission("health:checkitem:edit")
    @Log(title = "检查项管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/refs/{refId}")
    public R<Void> editRef(@NotNull(message = "主键不能为空") @PathVariable Long refId,
                           @Validated @RequestBody CheckitemRefBo bo) {
        return toAjax(checkitemService.updateRef(refId, bo));
    }

    /**
     * 删除参考区间
     */
    @Operation(summary = "删除参考区间")
    @SaCheckPermission("health:checkitem:edit")
    @Log(title = "检查项管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/refs/{refId}")
    public R<Void> removeRef(@NotNull(message = "主键不能为空") @PathVariable Long refId) {
        return toAjax(checkitemService.deleteRef(refId));
    }

}
