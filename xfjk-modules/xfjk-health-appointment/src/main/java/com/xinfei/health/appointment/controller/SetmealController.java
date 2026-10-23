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
import com.xinfei.health.appointment.domain.bo.SetmealBo;
import com.xinfei.health.appointment.domain.vo.CheckgroupVo;
import com.xinfei.health.appointment.domain.vo.SetmealImageVo;
import com.xinfei.health.appointment.domain.vo.SetmealVo;
import com.xinfei.health.appointment.service.ISetmealService;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 套餐管理（08 §3.3）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/setmeals")
@Tag(name = "管理端-套餐")
public class SetmealController extends BaseController {

    private final ISetmealService setmealService;

    /**
     * 分页查询套餐
     */
    @Operation(summary = "分页查询套餐")
    @SaCheckPermission("health:setmeal:list")
    @GetMapping
    public TableDataInfo<SetmealVo> list(SetmealBo bo, PageQuery pageQuery) {
        return setmealService.queryPageList(bo, pageQuery);
    }

    /**
     * 全量查询套餐（禁忌试算等下拉选择用）
     */
    @Operation(summary = "全量查询套餐")
    @SaCheckPermission("health:setmeal:query")
    @GetMapping("/all")
    public R<List<SetmealVo>> all() {
        return R.ok(setmealService.queryList());
    }

    /**
     * 查询套餐详情（套餐-检查组-检查项三级）
     */
    @Operation(summary = "查询套餐详情")
    @SaCheckPermission("health:setmeal:query")
    @GetMapping("/{id}")
    public R<SetmealVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return R.ok(setmealService.queryById(id));
    }

    /**
     * 查询套餐的检查组
     */
    @Operation(summary = "查询套餐的检查组")
    @SaCheckPermission("health:setmeal:query")
    @GetMapping("/{id}/checkgroups")
    public R<List<CheckgroupVo>> checkGroups(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return R.ok(setmealService.queryCheckGroups(id));
    }

    /**
     * 新增套餐（含关联检查组）
     */
    @Operation(summary = "新增套餐")
    @SaCheckPermission("health:setmeal:add")
    @Log(title = "套餐管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Integer> add(@Validated @RequestBody SetmealBo bo) {
        return R.ok(setmealService.insertByBo(bo));
    }

    /**
     * 编辑套餐（关联差异更新）
     */
    @Operation(summary = "编辑套餐")
    @SaCheckPermission("health:setmeal:edit")
    @Log(title = "套餐管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}")
    public R<Void> edit(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                        @Validated @RequestBody SetmealBo bo) {
        bo.setId(id);
        return toAjax(setmealService.updateByBo(bo));
    }

    /**
     * 删除套餐（存在预约订单返回 4001）
     */
    @Operation(summary = "删除套餐")
    @SaCheckPermission("health:setmeal:remove")
    @Log(title = "套餐管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return toAjax(setmealService.deleteById(id));
    }

    /**
     * 上传套餐图片，返回 OSS 主键与可访问地址
     */
    @Operation(summary = "上传套餐图片")
    @SaCheckPermission("health:setmeal:edit")
    @Log(title = "套餐管理", businessType = BusinessType.UPDATE)
    @PostMapping("/image")
    public R<SetmealImageVo> uploadImage(@RequestPart("file") MultipartFile file) {
        return R.ok(setmealService.uploadImage(file));
    }

}
