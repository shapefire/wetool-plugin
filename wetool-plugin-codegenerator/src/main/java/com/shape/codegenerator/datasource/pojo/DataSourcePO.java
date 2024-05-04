package com.shape.codegenerator.datasource.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author shapeFan
 * @description 数据源PO
 * @createTime 9:29 2024/04/29
 */
@Data
@Accessors(chain = true)
public class DataSourcePO {

    private String connectName;

    private String host;

    private String port;

    private String userName;

    private String pwd;

}
