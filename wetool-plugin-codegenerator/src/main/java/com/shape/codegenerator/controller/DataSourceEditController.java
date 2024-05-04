package com.shape.codegenerator.controller;

import cn.hutool.core.util.StrUtil;
import com.shape.codegenerator.CodeGeneratorConfigCache;
import com.shape.codegenerator.CodeGeneratorSupporter;
import com.shape.codegenerator.datasource.pojo.DataSourcePO;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.IOException;

/**
 * @Author:Fan
 * @Description:
 * @Date:Create in 19:56 2021/8/5
 * @Modify By:
 **/
@Slf4j
public class DataSourceEditController implements BaseViewController {

    /**
     * 自定义tabId，用来防止与其他插件发生名称冲突
     */
    public static final String TAB_ID = "codeGenerator-dataSourceEdit";

    /**
     * 自定义tabName，Tab选项卡的标题
     */
    public static final String TAB_NAME = "数据源编辑";

    private String editGroupName = "";
    private String oldEditConnectName = "";

    @FXML
    private TextField connectNameTextField;
    @FXML
    private TextField hostTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private TextField userNameTextField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Button addDataSourceButton;

    @FXML
    private Button editDataSourceButton;

    @FXML
    public void addDataSourceEvent() {
        DataSourcePO dataSourcePO = buildDtaSourcePO();
        if (null == dataSourcePO) {
            return;
        }
        CodeGeneratorConfigCache.addDataSource(dataSourcePO, editGroupName);

    }

    @FXML
    public void editDataSourceEvent() {
        DataSourcePO dataSourcePO = buildDtaSourcePO();
        if (null == dataSourcePO) {
            return;
        }
        CodeGeneratorConfigCache.editDataSource(dataSourcePO, editGroupName, oldEditConnectName);
    }


    @FXML
    private void initialize() {
        BeanFactory.registerView(TAB_ID, TAB_NAME, this);
        log.debug("The dataSource edit initialize.");
    }

    public static Parent buildDataSourceEditUi() throws IOException {
        return FxUtils.loadFxml(CodeGeneratorSupporter.class, "/com/shape/codegenerator/dataSourceEdit.fxml", true);
    }


    public static String getViewName() {
        return TAB_ID + TAB_NAME;
    }

    private void initEvent(boolean isAdd, DataSourcePO editDataSourcePO, String groupName ) {
        log.info("当前组[{}]更新数据源数据[{}],是否为新增事件:[{}]", groupName, editDataSourcePO, isAdd);
        editGroupName = groupName;
        if (isAdd) {
            oldEditConnectName = "";
            editDataSourceButton.setDisable(true);
            addDataSourceButton.setDisable(false);
            connectNameTextField.setText("");
            hostTextField.setText("");
            portTextField.setText("");
            userNameTextField.setText("");
            passwordField.setText("");
            return;
        }
        oldEditConnectName = editDataSourcePO.getConnectName();
        editDataSourceButton.setDisable(false);
        addDataSourceButton.setDisable(true);
        connectNameTextField.setText(editDataSourcePO.getConnectName());
        hostTextField.setText(editDataSourcePO.getHost());
        portTextField.setText(editDataSourcePO.getPort());
        userNameTextField.setText(editDataSourcePO.getUserName());
        passwordField.setText(editDataSourcePO.getPwd());

    }

    public static void editEvent(Pane pane, Boolean isAdd, DataSourcePO editDataSourcePO, String editGroupName) {
        pane.getChildren().clear();
        Parent parent = null;
        try {
            parent = DataSourceEditController.buildDataSourceEditUi();
        } catch (IOException e) {
            log.error("load dataSourceGroupEdit page failed!", e);
        }
        pane.getChildren().add(parent);
        DataSourceEditController controller = (DataSourceEditController) BeanFactory.getView(DataSourceEditController.getViewName());
        controller.initEvent(isAdd, editDataSourcePO, editGroupName);
    }

    private DataSourcePO buildDtaSourcePO() {
        if (StrUtil.isEmpty(connectNameTextField.getText())) {
            FxDialogs.showError("数据源名称不能为空!");
            return null;
        }
        if (StrUtil.isEmpty(hostTextField.getText())) {
            FxDialogs.showError("Host值不能为空!");
            return null;
        }
        if (StrUtil.isEmpty(portTextField.getText())) {
            FxDialogs.showError("Port值不能为空!");
            return null;
        }
        if (StrUtil.isEmpty(userNameTextField.getText())) {
            FxDialogs.showError("用户名称不能为空!");
            return null;
        }
        if (StrUtil.isEmpty(passwordField.getText())) {
            FxDialogs.showError("密码不能为空!");
            return null;
        }
        DataSourcePO editDataSourcePO = new DataSourcePO();
        editDataSourcePO.setConnectName(connectNameTextField.getText());
        editDataSourcePO.setHost(hostTextField.getText());
        editDataSourcePO.setPort(portTextField.getText());
        editDataSourcePO.setUserName(userNameTextField.getText());
        editDataSourcePO.setPwd(passwordField.getText());
        return editDataSourcePO;
    }
}
