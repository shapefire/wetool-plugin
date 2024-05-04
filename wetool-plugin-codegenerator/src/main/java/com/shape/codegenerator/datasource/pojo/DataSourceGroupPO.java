package com.shape.codegenerator.datasource.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author shapeFan
 * @description
 * @createTime 13:51 2024/04/29
 */
@Data
@Accessors(chain = true)
public class DataSourceGroupPO {

    /**
     * 组名
     */
    private String groupName;

    /**
     * 数据源集合
     */
    private List<DataSourcePO> dbList;
}
