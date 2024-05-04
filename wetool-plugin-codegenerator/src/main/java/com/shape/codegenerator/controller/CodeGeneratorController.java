package com.shape.codegenerator.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.shape.codegenerator.CodeGeneratorConfigCache;
import com.shape.codegenerator.constants.CodeGeneratorConstant;
import com.shape.codegenerator.datasource.pojo.DataSourceGroupPO;
import com.shape.codegenerator.datasource.pojo.DataSourcePO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.event.EventCenter;

import java.util.List;

/**
 * @author shapeFan
 * @description
 * @createTime 9:20 2024/04/28
 */
@Slf4j
public class CodeGeneratorController implements BaseViewController {

    /**
     * 自定义tabId，用来防止与其他插件发生名称冲突
     */
    public static final String TAB_ID = "codeGenerator";

    /**
     * 自定义tabName，Tab选项卡的标题
     */
    public static final String TAB_NAME = "代码生成器";

    @FXML
    private TreeView<DataSourcePO> dataSourceTreeView;

    @FXML
    private VBox dataSourceContentLoadVbox;



    @FXML
    private void initialize() {
        //构建数据源树
        DataSourcePO root = new DataSourcePO();
        root.setConnectName("数据源");
        TreeItem<DataSourcePO> rootItem = new TreeItem<>(root);
        dataSourceTreeView.setRoot(rootItem);
        dataSourceTreeView.setShowRoot(false);
        //定义选择事件
        dataSourceTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) -> onSelectItem(newValue)
                );
        
        dataSourceTreeView.setOnMouseClicked(v  -> {
            Node targetNode = v.getPickResult().getIntersectedNode();
            log.info("node:{}", targetNode);
            if (targetNode.toString().contains("null")) {
                // 如果点击的是空白地方或者树节点之外的地方，清除选中状态
                dataSourceTreeView.getSelectionModel().clearSelection();
            }

        });


        //自定义cell 展示名称
        dataSourceTreeView.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<DataSourcePO> call(TreeView<DataSourcePO> param) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(DataSourcePO item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getConnectName());
                        }
                    }
                };
            }
        });

        ContextMenu contextMenu = new ContextMenu();
        dataSourceTreeView.setContextMenu(contextMenu);

        dataSourceTreeView.setOnContextMenuRequested(v ->{
            TreeItem<DataSourcePO> selectedItem = dataSourceTreeView.getSelectionModel().getSelectedItem();
            contextMenu.getItems().clear();
            if (null == selectedItem) {
                log.info("codeGenerator-未选中");
                MenuItem addGroupMenuItem = new MenuItem("新增组");
                contextMenu.getItems().add(addGroupMenuItem);
                //组编辑事件
                addGroupMenuItem.setOnAction(itemEvent -> DataSourceGroupEditController.editEvent(dataSourceContentLoadVbox, true, ""));

            }else if(StrUtil.isEmpty(selectedItem.getValue().getHost())){
                log.info("codeGenerator-选中的是组节点！");
                MenuItem addDataSourceMenuItem = new MenuItem("新增数据源");
                MenuItem editGroupMenuItem = new MenuItem("编辑组");
                contextMenu.getItems().add(addDataSourceMenuItem);
                contextMenu.getItems().add(editGroupMenuItem);
                //组编辑事件
                editGroupMenuItem.setOnAction(itemEvent -> DataSourceGroupEditController.editEvent(dataSourceContentLoadVbox, false, selectedItem.getValue().getConnectName()));
                //数据源新增事件
                addDataSourceMenuItem.setOnAction(itemEvent -> DataSourceEditController.editEvent(dataSourceContentLoadVbox, true,null, selectedItem.getValue().getConnectName()));
            } else if (StrUtil.isNotEmpty(selectedItem.getValue().getHost())) {
                log.info("codeGenerator-选中的是数据源节点！");
                MenuItem editDataSourceMenuItem = new MenuItem("编辑数据源");
                contextMenu.getItems().add(editDataSourceMenuItem);
                //数据源编辑事件
                editDataSourceMenuItem.setOnAction(itemEvent -> DataSourceEditController.editEvent(dataSourceContentLoadVbox, false, selectedItem.getValue(), selectedItem.getParent().getValue().getConnectName()));
            }
            contextMenu.show(dataSourceTreeView, v.getScreenX(), v.getScreenY());
        });

        //组建数据源组
        loadDataSourceItem(rootItem);

        EventCenter.subscribeEvent(CodeGeneratorConstant.DATASOURCE_TREE_VIEW_REFRESH, (s, date, eventMessage) -> {
            log.info("刷新数据源树视图和重写数据源配置配置文件...");
            rootItem.getChildren().clear();
            loadDataSourceItem(rootItem);
            Platform.runLater(() -> dataSourceContentLoadVbox.getChildren().clear());

            CodeGeneratorConfigCache.reFreshConfigFile();
        });

    }

    private void loadDataSourceItem(TreeItem<DataSourcePO> rootItem) {
        List<DataSourceGroupPO> groupList = CodeGeneratorConfigCache.getInstance().getGroupList();
        if (CollUtil.isEmpty(groupList)) {
            log.info("当前仍未添加数据源数据！");
            return ;
        }
        for (DataSourceGroupPO dataSourceGroupPO : groupList) {
            //添加数据源组节点
            DataSourcePO dataSourceGroup = new DataSourcePO().setConnectName(dataSourceGroupPO.getGroupName());
            TreeItem<DataSourcePO> group = new TreeItem<>(dataSourceGroup);
            rootItem.getChildren().add(group);
            if (CollUtil.isEmpty(dataSourceGroupPO.getDbList())) {
                continue;
            }
            //添加数据源节点
            for (DataSourcePO dataSource : dataSourceGroupPO.getDbList()) {
                TreeItem<DataSourcePO> dataSourceTreeItem = new TreeItem<>(dataSource);
                group.getChildren().add(dataSourceTreeItem);
            }

        }
    }

    
    private void onSelectItem(TreeItem<DataSourcePO> item) {
        log.info("选择的节点是：{}", item);
    }

    public static String getViewName() {
        return TAB_ID + TAB_NAME;
    }
}
