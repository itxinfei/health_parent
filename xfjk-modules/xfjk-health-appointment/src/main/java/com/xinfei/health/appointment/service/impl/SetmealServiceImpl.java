package com.xinfei.health.appointment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.xinfei.common.core.exception.ServiceException;
import com.xinfei.common.core.utils.MapstructUtils;
import com.xinfei.common.core.utils.StreamUtils;
import com.xinfei.common.core.utils.StringUtils;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.redis.utils.RedisUtils;
import com.xinfei.health.appointment.constant.HealthCacheConstants;
import com.xinfei.health.appointment.constant.HealthErrorCode;
import com.xinfei.health.appointment.domain.CheckgroupCheckitem;
import com.xinfei.health.appointment.domain.HealthSetmealExt;
import com.xinfei.health.appointment.domain.Order;
import com.xinfei.health.appointment.domain.Setmeal;
import com.xinfei.health.appointment.domain.SetmealCheckgroup;
import com.xinfei.health.appointment.domain.bo.SetmealBo;
import com.xinfei.health.appointment.domain.bo.SetmealExtBo;
import com.xinfei.health.appointment.domain.vo.CheckgroupVo;
import com.xinfei.health.appointment.domain.vo.CheckitemVo;
import com.xinfei.health.appointment.domain.vo.SetmealImageVo;
import com.xinfei.health.appointment.domain.vo.SetmealVo;
import com.xinfei.health.appointment.mapper.CheckgroupCheckitemMapper;
import com.xinfei.health.appointment.mapper.CheckgroupMapper;
import com.xinfei.health.appointment.mapper.CheckitemMapper;
import com.xinfei.health.appointment.mapper.HealthSetmealExtMapper;
import com.xinfei.health.appointment.mapper.OrderMapper;
import com.xinfei.health.appointment.mapper.SetmealCheckgroupMapper;
import com.xinfei.health.appointment.mapper.SetmealMapper;
import com.xinfei.health.appointment.service.ISetmealService;
import com.xinfei.system.domain.vo.SysOssVo;
import com.xinfei.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 套餐 服务层处理
 *
 * @author xinfei
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SetmealServiceImpl implements ISetmealService {

    /**
     * 允许上传的图片扩展名（03 §6 上传校验）
     */
    private static final Set<String> ALLOWED_SUFFIX = Set.of(".jpg", ".jpeg", ".png", ".webp");

    /**
     * 单张图片大小上限 2MB
     */
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024L;

    private final SetmealMapper baseMapper;
    private final CheckgroupMapper checkgroupMapper;
    private final CheckitemMapper checkitemMapper;
    private final SetmealCheckgroupMapper setmealCheckgroupMapper;
    private final CheckgroupCheckitemMapper checkgroupCheckitemMapper;
    private final OrderMapper orderMapper;
    private final ISysOssService sysOssService;
    private final HealthSetmealExtMapper setmealExtMapper;

    @Override
    public TableDataInfo<SetmealVo> queryPageList(SetmealBo bo, PageQuery pageQuery) {
        Page<SetmealVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<SetmealVo> queryList() {
        // 下拉选择用全量套餐，按名称排序保证选项稳定
        LambdaQueryWrapper<Setmeal> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Setmeal::getName);
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public SetmealVo queryById(Integer id) {
        SetmealVo vo = baseMapper.selectVoById(id);
        if (ObjectUtil.isNull(vo)) {
            return null;
        }
        vo.setImgUrl(resolveImageUrl(vo.getImg()));
        vo.setCheckGroups(buildCheckGroups(id, true));
        vo.setExt(setmealExtMapper.selectBySetmealId(id));
        return vo;
    }

    @Override
    public List<CheckgroupVo> queryCheckGroups(Integer id) {
        return buildCheckGroups(id, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insertByBo(SetmealBo bo) {
        checkCodeUnique(bo.getCode(), null);
        Setmeal entity = MapstructUtils.convert(bo, Setmeal.class);
        baseMapper.insert(entity);
        saveRelations(entity.getId(), bo.getCheckGroupIds());
        markImageUsed(entity.getImg());
        // 08 §4.3：新增同事务写扩展表
        if (bo.getExt() != null) {
            insertExt(entity.getId(), bo.getExt());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(SetmealBo bo) {
        if (ObjectUtil.isNull(bo.getId())) {
            throw new ServiceException("套餐主键不能为空");
        }
        checkCodeUnique(bo.getCode(), bo.getId());
        Setmeal entity = MapstructUtils.convert(bo, Setmeal.class);
        boolean updated = baseMapper.updateById(entity) > 0;
        if (ObjectUtil.isNotNull(bo.getCheckGroupIds())) {
            diffUpdateRelations(bo.getId(), bo.getCheckGroupIds());
        }
        markImageUsed(entity.getImg());
        // ext upsert（08 §4.3：编辑走 upsert）
        if (bo.getExt() != null) {
            upsertExt(bo.getId(), bo.getExt());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Integer id) {
        long orderCount = orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
            .eq(Order::getSetmealId, id));
        if (orderCount > 0) {
            throw new ServiceException("该套餐下存在预约订单，不能删除", HealthErrorCode.SETMEAL_ORDER_EXISTS);
        }
        setmealCheckgroupMapper.deleteBySetmealId(id);
        // 级联清理扩展（08 §4.3）
        setmealExtMapper.deleteBySetmealId(id);
        return baseMapper.deleteById(id) > 0;
    }

    /**
     * 套餐 ext upsert：存在则更新，不存在则插入
     */
    private void upsertExt(Integer setmealId, SetmealExtBo ext) {
        HealthSetmealExt entity = setmealExtMapper.selectBySetmealId(setmealId);
        if (entity == null) {
            insertExt(setmealId, ext);
            return;
        }
        copyExt(ext, entity);
        setmealExtMapper.updateById(entity);
    }

    private void insertExt(Integer setmealId, SetmealExtBo ext) {
        HealthSetmealExt entity = new HealthSetmealExt();
        entity.setSetmealId(setmealId);
        copyExt(ext, entity);
        setmealExtMapper.insert(entity);
    }

    private void copyExt(SetmealExtBo ext, HealthSetmealExt entity) {
        entity.setSex(ObjectUtil.defaultIfNull(ext.getSex(), "0"));
        entity.setAgeMin(ext.getAgeMin());
        entity.setAgeMax(ext.getAgeMax());
        entity.setMarriedLimit(ObjectUtil.defaultIfNull(ext.getMarriedLimit(), "0"));
        entity.setHasSexHistory(ObjectUtil.defaultIfNull(ext.getHasSexHistory(), "0"));
        entity.setPregnancyWarn(ObjectUtil.defaultIfNull(ext.getPregnancyWarn(), "0"));
        entity.setSuitableDesc(ext.getSuitableDesc());
        entity.setDefaultConclusionId(ext.getDefaultConclusionId());
        entity.setDefaultAdviceId(ext.getDefaultAdviceId());
        entity.setNoticeText(ext.getNoticeText());
    }

    @Override
    public SetmealImageVo uploadImage(MultipartFile file) {
        if (ObjectUtil.isNull(file) || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new ServiceException("套餐图片不能超过 2MB");
        }
        String originalName = StringUtils.defaultString(file.getOriginalFilename());
        String suffix = originalName.toLowerCase(Locale.ROOT)
            .substring(originalName.lastIndexOf('.') + 1);
        if (!ALLOWED_SUFFIX.contains("." + suffix)) {
            throw new ServiceException("套餐图片仅支持 jpg / jpeg / png / webp 格式");
        }
        SysOssVo oss;
        try {
            oss = sysOssService.upload(file);
        } catch (RuntimeException e) {
            // OssException 不在 GlobalExceptionHandler 的消息透传列表里，不转 ServiceException 前端只会看到"发生未知异常"
            throw new ServiceException("图片存储配置有误，上传失败：" + e.getMessage());
        }
        String ossId = String.valueOf(oss.getOssId());
        // 上传集合供 12 的孤儿图清理任务比对，键名固定（08 §4.1）
        RedisUtils.addCacheSet(HealthCacheConstants.IMG_UPLOADED, ossId);
        SetmealImageVo result = new SetmealImageVo();
        result.setOssId(ossId);
        result.setUrl(oss.getUrl());
        return result;
    }

    /**
     * 组装套餐的检查组，withCheckItems 为 true 时继续填充组内检查项（三级详情）
     */
    private List<CheckgroupVo> buildCheckGroups(Integer setmealId, boolean withCheckItems) {
        List<Integer> groupIds = StreamUtils.toList(
            setmealCheckgroupMapper.selectBySetmealId(setmealId), SetmealCheckgroup::getCheckgroupId);
        if (CollUtil.isEmpty(groupIds)) {
            return List.of();
        }
        List<CheckgroupVo> groups = checkgroupMapper.selectVoByIds(groupIds);
        if (!withCheckItems) {
            return groups;
        }
        List<CheckgroupCheckitem> relations = checkgroupCheckitemMapper.selectByCheckgroupIds(groupIds);
        List<Integer> itemIds = StreamUtils.toList(relations, CheckgroupCheckitem::getCheckitemId).stream()
            .distinct().toList();
        Map<Integer, CheckitemVo> itemMap = itemIds.isEmpty() ? Map.of()
            : StreamUtils.toMap(checkitemMapper.selectVoByIds(itemIds), CheckitemVo::getId, item -> item);
        Map<Integer, List<CheckgroupCheckitem>> relationMap = StreamUtils.groupByKey(relations,
            CheckgroupCheckitem::getCheckgroupId);
        groups.forEach(group -> group.setCheckItems(
            relationMap.getOrDefault(group.getId(), List.of()).stream()
                .map(relation -> itemMap.get(relation.getCheckitemId()))
                .filter(ObjectUtil::isNotNull)
                .toList()));
        return groups;
    }

    /**
     * 由 img（OSS 主键）解析可访问地址；旧种子数据存的是七牛文件名，解析不出时返回空
     */
    private String resolveImageUrl(String img) {
        if (StringUtils.isBlank(img)) {
            return null;
        }
        try {
            SysOssVo oss = sysOssService.getById(Long.parseLong(img));
            return ObjectUtil.isNull(oss) ? null : oss.getUrl();
        } catch (NumberFormatException e) {
            log.debug("套餐图片标识非 OSS 主键，跳过地址解析：{}", img);
            return null;
        }
    }

    private void saveRelations(Integer setmealId, List<Integer> checkGroupIds) {
        if (CollUtil.isEmpty(checkGroupIds)) {
            return;
        }
        Set<Integer> distinctIds = new HashSet<>(checkGroupIds);
        setmealCheckgroupMapper.insertBatch(distinctIds.stream()
            .map(groupId -> newRelation(setmealId, groupId))
            .toList());
    }

    /**
     * 关联差异更新：删除不在新集合的旧关联、补插新增（08 §4.1）
     */
    private void diffUpdateRelations(Integer setmealId, List<Integer> checkGroupIds) {
        List<Integer> oldIds = StreamUtils.toList(
            setmealCheckgroupMapper.selectBySetmealId(setmealId), SetmealCheckgroup::getCheckgroupId);
        RelationDiff diff = RelationDiff.compute(oldIds, checkGroupIds);

        if (CollUtil.isNotEmpty(diff.toRemove())) {
            setmealCheckgroupMapper.delete(Wrappers.<SetmealCheckgroup>lambdaQuery()
                .eq(SetmealCheckgroup::getSetmealId, setmealId)
                .in(SetmealCheckgroup::getCheckgroupId, diff.toRemove()));
        }
        if (CollUtil.isNotEmpty(diff.toAdd())) {
            setmealCheckgroupMapper.insertBatch(diff.toAdd().stream()
                .map(groupId -> newRelation(setmealId, groupId))
                .toList());
        }
        log.info("套餐[{}]关联差异更新：删除 {} 条，新增 {} 条", setmealId,
            diff.toRemove().size(), diff.toAdd().size());
    }

    private SetmealCheckgroup newRelation(Integer setmealId, Integer checkgroupId) {
        SetmealCheckgroup relation = new SetmealCheckgroup();
        relation.setSetmealId(setmealId);
        relation.setCheckgroupId(checkgroupId);
        return relation;
    }

    private void markImageUsed(String img) {
        if (StringUtils.isNotBlank(img)) {
            RedisUtils.addCacheSet(HealthCacheConstants.IMG_USED, img);
        }
    }

    /**
     * 编码唯一校验（08 §4.1）
     */
    private void checkCodeUnique(String code, Integer excludeId) {
        boolean exists = baseMapper.exists(Wrappers.<Setmeal>lambdaQuery()
            .eq(Setmeal::getCode, code)
            .ne(ObjectUtil.isNotNull(excludeId), Setmeal::getId, excludeId));
        if (exists) {
            throw new ServiceException("套餐编码已存在：" + code);
        }
    }

    private LambdaQueryWrapper<Setmeal> buildQueryWrapper(SetmealBo bo) {
        return Wrappers.<Setmeal>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getName()), Setmeal::getName, bo.getName())
            .like(StringUtils.isNotBlank(bo.getCode()), Setmeal::getCode, bo.getCode())
            .like(StringUtils.isNotBlank(bo.getHelpCode()), Setmeal::getHelpCode, bo.getHelpCode())
            .eq(StringUtils.isNotBlank(bo.getSex()), Setmeal::getSex, bo.getSex())
            .orderByAsc(Setmeal::getCode);
    }

}
