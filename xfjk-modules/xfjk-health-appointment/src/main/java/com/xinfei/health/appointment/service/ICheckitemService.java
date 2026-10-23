package com.xinfei.health.appointment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.appointment.domain.HealthCheckitemRef;
import com.xinfei.health.appointment.domain.bo.CheckitemBo;
import com.xinfei.health.appointment.domain.bo.CheckitemRefBo;
import com.xinfei.health.appointment.domain.vo.CheckitemVo;

import java.util.List;

/**
 * 检查项 服务层（08 §3.1、§4.1）
 *
 * @author xinfei
 */
public interface ICheckitemService {

    /**
     * 分页查询检查项
     */
    TableDataInfo<CheckitemVo> queryPageList(CheckitemBo bo, PageQuery pageQuery);

    /**
     * 查询全部检查项（供检查组选择）
     */
    List<CheckitemVo> queryList(CheckitemBo bo);

    /**
     * 查询检查项详情（含 ext 与 refs）
     */
    CheckitemVo queryById(Integer id);

    /**
     * 新增检查项：同事务落 ext 与至少一条参考区间（缺失 5007）
     *
     * @return 新记录主键
     */
    Integer insertByBo(CheckitemBo bo);

    /**
     * 编辑检查项：ext upsert、refs 差异更新
     */
    Boolean updateByBo(CheckitemBo bo);

    /**
     * 删除检查项，被检查组引用时抛 3001；级联删 ext 与 refs
     */
    Boolean deleteById(Integer id);

    /**
     * 查询检查项的参考区间（按优先级排序）
     */
    List<HealthCheckitemRef> queryRefs(Integer checkitemId);

    /**
     * 新增参考区间（5012 校验通过后落库）
     */
    Long insertRef(Integer checkitemId, CheckitemRefBo bo);

    /**
     * 编辑参考区间
     */
    Boolean updateRef(Long refId, CheckitemRefBo bo);

    /**
     * 删除参考区间
     */
    Boolean deleteRef(Long refId);

}
