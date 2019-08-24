package org.code4everything.wetool.plugin.test;

import cn.hutool.core.io.IoUtil;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson.JSON;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.WeApplication;
import org.code4everything.wetool.plugin.support.WePluginSupportable;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.code4everything.wetool.plugin.support.config.WeInitialize;
import org.code4everything.wetool.plugin.support.config.WePluginInfo;
import org.code4everything.wetool.plugin.support.config.WeTab;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author pantao
 * @since 2019/8/23
 */
@Slf4j
public class WetoolTest extends WeApplication {

    private static WePluginSupportable supportable;

    private static WePluginInfo info;

    public static void runTest(WePluginSupportable supportable, String[] args) {
        runTest(supportable, getConfig(), args);
    }

    public static void runTest(WePluginSupportable supportable, WeConfig config, String[] args) {
        String json = IoUtil.read(WetoolTest.class.getResourceAsStream("/plugin.json"), "utf-8");
        runTest(JSON.parseObject(json, WePluginInfo.class), supportable, config, args);
    }

    public static void runTest(WePluginInfo info, WePluginSupportable supportable, String[] args) {
        runTest(info, supportable, getConfig(), args);
    }

    public static void runTest(WePluginInfo info, WePluginSupportable supportable, WeConfig config, String[] args) {
        log.info("start wetool on os: {}", SystemUtil.getOsInfo().getName());
        WetoolTest.info = info;
        WetoolTest.supportable = supportable;
        BeanFactory.register(config);
        launch(args);
    }

    public static WeConfig getConfig() {
        WeConfig config = new WeConfig();
        config.setAutoWrap(true);
        config.setClipboardSize(20);
        config.setFileFilter("^[^.].*$");
        config.setQuickStarts(new ArrayList<>());
        config.setPluginDisables(new HashSet<>());

        WeInitialize initialize = new WeInitialize();
        initialize.setFullscreen(false);
        initialize.setHeight(700);
        initialize.setHide(false);
        initialize.setWidth(1000);

        WeTab tab = new WeTab();
        tab.setLoads(new ArrayList<>());
        tab.setSupports(new ArrayList<>());

        initialize.setTabs(tab);
        config.setInitialize(initialize);
        return config;
    }

    @Override
    public void start(Stage stage) {
        super.start(stage);
        mainController.registerPlugin(info, supportable);
    }
}
