package com.shape.codegenerator;

import com.shape.codegenerator.constants.CodeGeneratorConstant;
import com.shape.codegenerator.controller.CodeGeneratorController;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.WePluginSupporter;
import org.code4everything.wetool.plugin.support.config.WePluginInfo;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.event.EventMode;
import org.code4everything.wetool.plugin.support.util.FxUtils;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Slf4j
public class CodeGeneratorSupporter implements WePluginSupporter {

    /**
     * 初始化操作
     *
     * @return 初始化是否成功，返回true时继续加载插件，否则放弃加载
     */
    @Override
    public boolean initialize() {
        log.info("initialize generateCode plugin");
        //进行数据源数据初始化
        CodeGeneratorConfigCache.getInstance();
        return true;
    }

    /**
     * 注册插件到主界面菜单，可返回NULL，可不实现此方法
     *
     * @return 返回的 {@link MenuItem} 将被添加到主界面的插件菜单
     */
    @Override
    public MenuItem registerBarMenu() {
        return FxUtils.createBarMenuItem("代码生成器", event -> initBootIfConfigured());
    }

    /**
     * 注册成功之后的回调
     */
    @Override
    public void registered(WePluginInfo info, MenuItem barMenu, java.awt.MenuItem trayMenu) {
        log.info("codeGenerator plugin registered success");

        //注册代码生成器相关事件
        EventCenter.registerEvent(CodeGeneratorConstant.DATASOURCE_TREE_VIEW_REFRESH, EventMode.SINGLE_SUB);
    }

    @Override
    public void initBootIfConfigured() {
        // 注意保证fxml文件的url路径唯一性
        Pane node = FxUtils.loadFxml(CodeGeneratorSupporter.class, "/com/shape/codegenerator/codeGenerator.fxml", true);
       // FxDialogs.showInformation(Example.TAB_NAME, "welcome to wetool plugin");
        FxUtils.openTab(node, CodeGeneratorController.TAB_ID, CodeGeneratorController.TAB_NAME);
        //FxUtils.getStage().setTitle("插件示例");
        //FxUtils.getStage().getScene().setRoot(node);
    }

}
