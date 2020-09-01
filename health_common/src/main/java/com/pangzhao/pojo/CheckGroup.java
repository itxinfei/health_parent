package com.pangzhao.pojo;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * 检查组
 */
@Data
@ToString
@Table(name = "t_checkgroup")
public class CheckGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;//主键
    @Column
    private String code;//编码
    @Column
    private String name;//名称
    @Column(name = "helpCode")
    private String helpCode;//助记
    @Column
    private String sex;//适用性别
    @Column
    private String remark;//介绍
    @Column
    private String attention;//注意事项

    private List<CheckItem> checkItemList;

}
