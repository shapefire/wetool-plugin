package com.shape.codegenerator.datasource.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author shapeFan
 * @description
 * @createTime 14:23 2024/04/29
 */
@Data
@Accessors(chain = true)
public class DataSourceJsonConfig {

    private List<DataSourceGroupPO> groupList;
}
