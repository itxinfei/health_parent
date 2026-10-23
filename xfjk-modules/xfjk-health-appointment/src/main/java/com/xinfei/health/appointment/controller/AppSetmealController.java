package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.bo.SetmealBo;
import com.xinfei.health.appointment.domain.vo.SetmealVo;
import com.xinfei.health.appointment.service.ISetmealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端套餐（13 §4.2：公开接口，P03 列表 / P05 下单页数据源）
 *
 * <p>复用后台套餐服务；详情返回套餐-检查组-检查项三级结构。
 * 适用人群四维结构化字段待 §2.5 扩展表落地后叠加。
 *
 * @author xinfei
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/setmeals")
@Tag(name = "C端-套餐")
public class AppSetmealController extends BaseController {

    private final ISetmealService setmealService;

    /**
     * 套餐列表（name 模糊、sex 筛选）
     */
    @Operation(summary = "C端套餐列表")
    @GetMapping
    public TableDataInfo<SetmealVo> list(SetmealBo bo, PageQuery pageQuery) {
        return setmealService.queryPageList(bo, pageQuery);
    }

    /**
     * 套餐详情（含检查组与检查项三级结构、图片可访问地址）
     */
    @Operation(summary = "C端套餐详情")
    @GetMapping("/{id}")
    public R<SetmealVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return R.ok(setmealService.queryById(id));
    }

}
