package com.xinfei.health.appointment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.appointment.domain.bo.SetmealBo;
import com.xinfei.health.appointment.domain.vo.CheckgroupVo;
import com.xinfei.health.appointment.domain.vo.SetmealImageVo;
import com.xinfei.health.appointment.domain.vo.SetmealVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 套餐 服务层（08 §3.3）
 *
 * @author xinfei
 */
public interface ISetmealService {

    /**
     * 分页查询套餐
     */
    TableDataInfo<SetmealVo> queryPageList(SetmealBo bo, PageQuery pageQuery);

    /**
     * 全量查询套餐（下拉选择用，仅返回启用项）
     */
    List<SetmealVo> queryList();

    /**
     * 查询套餐详情，含套餐-检查组-检查项三级结构与图片可访问地址
     */
    SetmealVo queryById(Integer id);

    /**
     * 查询套餐的检查组
     */
    List<CheckgroupVo> queryCheckGroups(Integer id);

    /**
     * 新增套餐（含关联检查组）
     *
     * @return 新记录主键
     */
    Integer insertByBo(SetmealBo bo);

    /**
     * 编辑套餐，关联按差异更新（删旧 + 补新）；保存时把图片记入已使用集合
     */
    Boolean updateByBo(SetmealBo bo);

    /**
     * 删除套餐，存在预约订单时抛 4001
     */
    Boolean deleteById(Integer id);

    /**
     * 上传套餐图片
     */
    SetmealImageVo uploadImage(MultipartFile file);

}
