package com.shape.codegenerator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.shape.codegenerator.constants.CodeGeneratorConstant;
import com.shape.codegenerator.datasource.pojo.DataSourceGroupPO;
import com.shape.codegenerator.datasource.pojo.DataSourceJsonConfig;
import com.shape.codegenerator.datasource.pojo.DataSourcePO;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.boot.base.FileUtils;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.util.FxDialogs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author shapeFan
 * @description
 * @createTime 11:39 2024/05/04
 */

@Slf4j
public class CodeGeneratorConfigCache {

    private final static String dbJsonConfigPath = FileUtils.currentWorkDir() + File.separator + "db.json";

    private static volatile DataSourceJsonConfig dataSourceJsonConfig;

    public static DataSourceJsonConfig getInstance() {
        if (null == dataSourceJsonConfig) {
            synchronized (CodeGeneratorConfigCache.class) {
                if (null == dataSourceJsonConfig) {
                    try {
                        if (!FileUtil.exist(dbJsonConfigPath)) {
                            Files.createFile(Paths.get(dbJsonConfigPath));
                        }
                    } catch (Exception e) {
                        log.error("codeGenerator:代码生成器插件初始化失败");
                        return new DataSourceJsonConfig();
                    }
                    String configStr = FileUtil.readUtf8String(dbJsonConfigPath);
                    if (!StrUtil.isEmpty(configStr)) {
                        JSONObject dbConfigJsonObject = JSON.parseObject(FileUtil.readUtf8String(dbJsonConfigPath), Feature.OrderedField);
                        dataSourceJsonConfig = JSON.toJavaObject(dbConfigJsonObject, DataSourceJsonConfig.class);
                    }else {
                        dataSourceJsonConfig = new DataSourceJsonConfig().setGroupList(new ArrayList<>());
                    }
                }
            }
        }
        return dataSourceJsonConfig;
    }

    /**
     * 添加组
     * @param groupName
     */
    public static void addGroup(String groupName) {
        if (editGroupCheck(groupName)) return;

        DataSourceGroupPO addGroupPO = new DataSourceGroupPO();
        addGroupPO.setGroupName(groupName);
        addGroupPO.setDbList(new ArrayList<>());
        CodeGeneratorConfigCache.getInstance().getGroupList().add(addGroupPO);
        EventCenter.publishEvent(CodeGeneratorConstant.DATASOURCE_TREE_VIEW_REFRESH, new Date());
    }

    /**
     * 编辑组
     * @param editGroupName   编辑后的组名
     * @param oldEditGroupName 原组名
     */
    public static void editGroup(String editGroupName, String oldEditGroupName) {
        if (editGroupCheck(editGroupName)) return;
        CodeGeneratorConfigCache.getInstance().getGroupList().forEach(
                v -> {
                     if(v.getGroupName().equals(oldEditGroupName)){
                         v.setGroupName(editGroupName);
                     }
                }
        );
        EventCenter.publishEvent(CodeGeneratorConstant.DATASOURCE_TREE_VIEW_REFRESH, new Date());
    }

    /**
     * 编辑组检查
     * @param groupName
     * @return
     */
    private static boolean editGroupCheck(String groupName) {
        Boolean existGroupName = existGroupName(groupName);
        if (existGroupName) {
            FxDialogs.showError("已存在组名称！");
            return true;
        }
        return false;
    }

    /**
     * 数据源组名是否已存在
     *
     * @param groupName 组名
     * @return
     */
    public static Boolean existGroupName(String groupName) {
        return CodeGeneratorConfigCache.getInstance().getGroupList().stream()
                .map(DataSourceGroupPO::getGroupName)
                .anyMatch(v -> v.equals(groupName));
    }


    public static List<String> groupNameList() {
        return CodeGeneratorConfigCache.getInstance().getGroupList().stream()
                .map(DataSourceGroupPO::getGroupName)
                .collect(Collectors.toList());
    }

    /**
     * 添加数据源数据
     * @param dataSourcePO  新增的数据
     * @param editGroupName 添加数据源的所属组名称
     */
    public static void addDataSource(DataSourcePO dataSourcePO, String editGroupName) {
        Boolean exist = existDataSourceConnectNameUnderGroup(editGroupName, dataSourcePO.getConnectName());
        if (exist) {
            FxDialogs.showError(StrFormatter.format("当前组[{}]下已存在 连接名称为[{}]的数据，无法添加!", editGroupName, dataSourcePO.getConnectName()));
            return;
        }
        CodeGeneratorConfigCache.getInstance().getGroupList()
                .forEach(v ->{
                    if (v.getGroupName().equals(editGroupName)) {
                        v.getDbList().add(dataSourcePO);
                    }
                });

        EventCenter.publishEvent(CodeGeneratorConstant.DATASOURCE_TREE_VIEW_REFRESH, new Date());
    }

    public static void editDataSource(DataSourcePO dataSourcePO, String editGroupName, String oldEditConnectName) {
        if (!oldEditConnectName.equals(dataSourcePO.getConnectName())) {
            Boolean exist = existDataSourceConnectNameUnderGroup(editGroupName, dataSourcePO.getConnectName());
            if (exist) {
                FxDialogs.showError(StrFormatter.format("当前组[{}]下已存在 连接名称为[{}]的数据，无法更新!", editGroupName, dataSourcePO.getConnectName()));
                return;
            }
        }
        CodeGeneratorConfigCache.getInstance().getGroupList().forEach(v -> {
            if (v.getGroupName().equals(editGroupName)) {
                v.getDbList().forEach(db ->{
                    if (oldEditConnectName.equals(db.getConnectName())) {
                        db.setConnectName(dataSourcePO.getConnectName());
                        db.setHost(dataSourcePO.getHost());
                        db.setPort(dataSourcePO.getPort());
                        db.setUserName(dataSourcePO.getUserName());
                        db.setPwd(dataSourcePO.getPwd());
                    }
                });
            }
        });
        EventCenter.publishEvent(CodeGeneratorConstant.DATASOURCE_TREE_VIEW_REFRESH, new Date());

    }

    public static Boolean existDataSourceConnectNameUnderGroup(String groupName, String dataSourceConnectName) {
        return CodeGeneratorConfigCache.getInstance().getGroupList()
                .stream()
                .filter(v -> v.getGroupName().equals(groupName))
                .flatMap(v -> v.getDbList().stream().map(DataSourcePO::getConnectName))
                .anyMatch(v -> v.contains(dataSourceConnectName));

    }

    //刷新数据源配置文件
    public static void reFreshConfigFile() {
        String configJsonStr = JSON.toJSONString(CodeGeneratorConfigCache.getInstance(), true);
        FileUtil.writeUtf8String(configJsonStr, dbJsonConfigPath);
    }



    public static void createJsonContentTest() {
        DataSourcePO dataSourcePO1 = new DataSourcePO();
        dataSourcePO1.setConnectName("1");
        dataSourcePO1.setHost("1");
        dataSourcePO1.setPort("1");
        dataSourcePO1.setUserName("1");
        dataSourcePO1.setPwd("1");
        DataSourcePO dataSourcePO2 = new DataSourcePO();
        dataSourcePO2.setConnectName("2");
        dataSourcePO2.setHost("2");
        dataSourcePO2.setPort("2");
        dataSourcePO2.setUserName("2");
        dataSourcePO2.setPwd("2");
        DataSourcePO dataSourcePO3 = new DataSourcePO();
        dataSourcePO3.setConnectName("3");
        dataSourcePO3.setHost("3");
        dataSourcePO3.setPort("3");
        dataSourcePO3.setUserName("3");
        dataSourcePO3.setPwd("3");
        DataSourceGroupPO dataSourceGroupPO1 = new DataSourceGroupPO();
        dataSourceGroupPO1.setGroupName("1");
        dataSourceGroupPO1.setDbList(Stream.of(dataSourcePO1, dataSourcePO2).collect(Collectors.toList()));
        DataSourceGroupPO dataSourceGroupPO2 = new DataSourceGroupPO();
        dataSourceGroupPO2.setGroupName("2");
        dataSourceGroupPO2.setDbList(Stream.of(dataSourcePO3).collect(Collectors.toList()));

        DataSourceJsonConfig dataSourceJsonConfig = new DataSourceJsonConfig();
        dataSourceJsonConfig.setGroupList(Stream.of(dataSourceGroupPO1, dataSourceGroupPO2).collect(Collectors.toList()));
        String configJsonStr = JSON.toJSONString(dataSourceJsonConfig, true);
        FileUtil.writeUtf8String(configJsonStr, dbJsonConfigPath);
    }
}
