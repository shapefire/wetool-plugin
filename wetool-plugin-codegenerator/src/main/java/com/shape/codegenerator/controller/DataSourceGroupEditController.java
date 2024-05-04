package com.shape.codegenerator.controller;

import com.shape.codegenerator.CodeGeneratorConfigCache;
import com.shape.codegenerator.CodeGeneratorSupporter;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
public class DataSourceGroupEditController implements BaseViewController {

    /**
     * 自定义tabId，用来防止与其他插件发生名称冲突
     */
    public static final String TAB_ID = "codeGenerator-groupEdit";

    /**
     * 自定义tabName，Tab选项卡的标题
     */
    public static final String TAB_NAME = "组编辑";

    private String oldEditGroupName = "";

    @FXML
    private TextField groupNameTextField;

    @FXML
    private Button addGroupButton;

    @FXML
    private Button editGroupButton;

    @FXML
    public void addGroupEvent() {
        String addGroupName = groupNameTextField.getText();
        CodeGeneratorConfigCache.addGroup(addGroupName);
    }


    @FXML
    public void editGroupEvent() {
        String editGroupName = groupNameTextField.getText();
        if (editGroupName.equals(oldEditGroupName)) {
            FxDialogs.showInformation(null, "组名称未更改,无需更新!");
            return;
        }
        CodeGeneratorConfigCache.editGroup(editGroupName, oldEditGroupName);
    }


    @FXML
    private void initialize() {
        BeanFactory.registerView(TAB_ID, TAB_NAME, this);
        log.debug("The dataSource Group edit initialize.");
    }

    public static Parent buildDataSourceGroupEditUi() throws IOException {
        return FxUtils.loadFxml(CodeGeneratorSupporter.class, "/com/shape/codegenerator/dataSourceGroupEdit.fxml", true);
    }


    public static String getViewName() {
        return TAB_ID + TAB_NAME;
    }

    public void setGroupNameTextField(String content) {
        groupNameTextField.setText(content);
    }

    private void initEvent(boolean isAdd, String groupNameTextValue) {
        if (isAdd) {
            editGroupButton.setDisable(true);
            addGroupButton.setDisable(false);
            oldEditGroupName = "";
            this.setGroupNameTextField(oldEditGroupName);
            return;
        }
        addGroupButton.setDisable(true);
        editGroupButton.setDisable(false);
        oldEditGroupName = groupNameTextValue;
        this.setGroupNameTextField(oldEditGroupName);
    }

    public static void editEvent(Pane pane, Boolean isAdd, String groupNameTextValue) {
        pane.getChildren().clear();
        Parent parent = null;
        try {
            parent = DataSourceGroupEditController.buildDataSourceGroupEditUi();
        } catch (IOException e) {
            log.error("load dataSourceGroupEdit page failed!", e);
        }
        pane.getChildren().add(parent);
        DataSourceGroupEditController controller = (DataSourceGroupEditController) BeanFactory.getView(DataSourceGroupEditController.getViewName());
        controller.initEvent(isAdd, groupNameTextValue);
    }

}
