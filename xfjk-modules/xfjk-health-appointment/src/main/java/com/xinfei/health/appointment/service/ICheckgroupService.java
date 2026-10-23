package com.xinfei.health.appointment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.appointment.domain.bo.CheckgroupBo;
import com.xinfei.health.appointment.domain.vo.CheckgroupVo;
import com.xinfei.health.appointment.domain.vo.CheckitemVo;

import java.util.List;

/**
 * 检查组 服务层（08 §3.2）
 *
 * @author xinfei
 */
public interface ICheckgroupService {

    /**
     * 分页查询检查组
     */
    TableDataInfo<CheckgroupVo> queryPageList(CheckgroupBo bo, PageQuery pageQuery);

    /**
     * 查询全部检查组（供套餐选择）
     */
    List<CheckgroupVo> queryList(CheckgroupBo bo);

    /**
     * 查询检查组详情
     */
    CheckgroupVo queryById(Integer id);

    /**
     * 查询组内检查项
     */
    List<CheckitemVo> queryCheckItems(Integer id);

    /**
     * 新增检查组（含关联检查项）
     *
     * @return 新记录主键
     */
    Integer insertByBo(CheckgroupBo bo);

    /**
     * 编辑检查组，关联按差异更新（删旧 + 补新）
     */
    Boolean updateByBo(CheckgroupBo bo);

    /**
     * 删除检查组，被套餐引用时抛 3002
     */
    Boolean deleteById(Integer id);

}
