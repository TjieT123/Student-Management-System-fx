package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.*;

public class AdminHomeController extends BaseController {

    @FXML private VBox sidebar;
    @FXML private Label sectionTitle;
    @FXML private StackPane contentArea;
    @FXML private Button homeBtn;
    @FXML private Button teacherMgmtBtn;
    @FXML private Button studentMgmtBtn;
    @FXML private Button adminMgmtBtn;
    @FXML private Button courseMgmtBtn;
    @FXML private Button announcementMgmtBtn;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        homeBtn.setOnAction(e -> showDashboard());
        teacherMgmtBtn.setOnAction(e -> showTeacherManagement());
        studentMgmtBtn.setOnAction(e -> showStudentManagement());
        adminMgmtBtn.setOnAction(e -> showAdminManagement());
        courseMgmtBtn.setOnAction(e -> showCourseManagement());
        announcementMgmtBtn.setOnAction(e -> showAnnouncementManagement());

        // 新增P1管理面板按钮
        addSidebarButton("🏆 荣誉管理", e -> showHonorManagement());
        addSidebarButton("💡 实践管理", e -> showPracticeManagement());
        addSidebarButton("🏥 请假审批", e -> showLeaveManagement());
        addSidebarButton("🎉 活动管理", e -> showActivityManagement());

        showDashboard();
    }

    private void addSidebarButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14; -fx-alignment: CENTER_LEFT; -fx-pref-width: 180; -fx-padding: 10 20;");
        btn.setOnAction(handler);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14; -fx-alignment: CENTER_LEFT; -fx-pref-width: 180; -fx-padding: 10 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14; -fx-alignment: CENTER_LEFT; -fx-pref-width: 180; -fx-padding: 10 20;"));
        sidebar.getChildren().add(btn);
    }

    private void showDashboard() {
        contentArea.getChildren().clear();
        sectionTitle.setText("管理后台首页");

        try {
            AdminStatistics stats = ApiClient.getAdminStatistics();
            VBox dash = new VBox(25);
            dash.setPadding(new Insets(30));
            dash.setAlignment(javafx.geometry.Pos.CENTER);

            // 数字卡片行
            HBox cards = new HBox(25);
            cards.setAlignment(javafx.geometry.Pos.CENTER);
            cards.getChildren().addAll(
                    createDashCard("总用户数", stats.getTotalUsers(), "#2c3e50"),
                    createDashCard("管理员", stats.getAdminCount(), "#8e44ad"),
                    createDashCard("教师", stats.getTeacherCount(), "#2980b9"),
                    createDashCard("学生", stats.getStudentCount(), "#27ae60")
            );

            // 饼状图
            javafx.scene.chart.PieChart pieChart = new javafx.scene.chart.PieChart();
            pieChart.setTitle("用户分布");
            pieChart.setLabelsVisible(true);
            pieChart.setPrefSize(400, 300);
            javafx.scene.chart.PieChart.Data adminSlice =
                    new javafx.scene.chart.PieChart.Data("管理员 (" + stats.getAdminCount() + ")", stats.getAdminCount());
            javafx.scene.chart.PieChart.Data teacherSlice =
                    new javafx.scene.chart.PieChart.Data("教师 (" + stats.getTeacherCount() + ")", stats.getTeacherCount());
            javafx.scene.chart.PieChart.Data studentSlice =
                    new javafx.scene.chart.PieChart.Data("学生 (" + stats.getStudentCount() + ")", stats.getStudentCount());
            pieChart.setData(javafx.collections.FXCollections.observableArrayList(
                    adminSlice, teacherSlice, studentSlice));
            adminSlice.nodeProperty().addListener((obs, old, n) ->
            { if (n != null) n.setStyle("-fx-pie-color: #8e44ad;"); });
            teacherSlice.nodeProperty().addListener((obs, old, n) ->
            { if (n != null) n.setStyle("-fx-pie-color: #2980b9;"); });
            studentSlice.nodeProperty().addListener((obs, old, n) ->
            { if (n != null) n.setStyle("-fx-pie-color: #27ae60;"); });

            HBox pieBox = new HBox(pieChart);
            pieBox.setAlignment(javafx.geometry.Pos.CENTER);

            // 学期设置按钮
            Button semBtn = new Button("📅 学期设置");
            semBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-size: 14;");
            semBtn.setOnAction(e -> showSemesterSettings());
            dash.getChildren().addAll(cards, pieBox, semBtn);
            contentArea.getChildren().add(dash);
        } catch (Exception e) {
            Label err = new Label("加载统计数据失败: " + e.getMessage());
            err.setStyle("-fx-text-fill: red; -fx-font-size: 14;");
            contentArea.getChildren().add(err);
        }
    }

    private VBox createDashCard(String label, Integer value, String color) {
        VBox card = new VBox(8);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPrefWidth(160);
        card.setPrefHeight(100);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-padding: 15;");

        Label valueLabel = new Label(value != null ? String.valueOf(value) : "0");
        valueLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(valueLabel, textLabel);
        return card;
    }

    // ==================== 搜索栏工具 ====================

    private HBox createSearchBar(String idPrompt, String namePrompt,
                                  Runnable onSearch, Runnable onReset) {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(5, 0, 5, 0));
        TextField idField = new TextField();
        idField.setPromptText(idPrompt);
        idField.setPrefWidth(130);
        TextField nameField = new TextField();
        nameField.setPromptText(namePrompt);
        nameField.setPrefWidth(160);
        Button searchBtn = new Button("搜索");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        searchBtn.setOnAction(e -> onSearch.run());
        Button resetBtn = new Button("重置");
        resetBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        resetBtn.setOnAction(e -> {
            idField.clear();
            nameField.clear();
            onReset.run();
        });
        bar.getChildren().addAll(idField, nameField, searchBtn, resetBtn);
        bar.setUserData(new TextField[]{idField, nameField});
        return bar;
    }

    private String getSearchId(HBox bar) {
        TextField[] fields = (TextField[]) bar.getUserData();
        return fields[0].getText().trim();
    }

    private String getSearchName(HBox bar) {
        TextField[] fields = (TextField[]) bar.getUserData();
        return fields[1].getText().trim();
    }

    private HBox createPagination(int page, int total, Runnable prevAction, Runnable nextAction) {
        HBox box = new HBox(15);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setUserData("pagination");
        Button prevBtn = new Button("上一页");
        prevBtn.setOnAction(e -> prevAction.run());
        prevBtn.setDisable(page <= 1);
        Button nextBtn = new Button("下一页");
        nextBtn.setOnAction(e -> nextAction.run());
        nextBtn.setDisable(page >= total);
        box.getChildren().addAll(prevBtn, new Label("第 " + page + "/" + total + " 页"), nextBtn);
        return box;
    }

    private void updatePaginationLabel(HBox searchBar, int page, int total) {
        if (searchBar.getParent() instanceof VBox panel) {
            for (javafx.scene.Node node : panel.getChildren()) {
                if (node instanceof HBox pagination
                        && "pagination".equals(pagination.getUserData())
                        && pagination.getChildren().size() >= 3) {
                    Label label = (Label) pagination.getChildren().get(1);
                    label.setText("第 " + page + "/" + total + " 页");
                    ((Button) pagination.getChildren().get(0)).setDisable(page <= 1);
                    ((Button) pagination.getChildren().get(2)).setDisable(page >= total);
                    break;
                }
            }
        }
    }

    private void setRowVisible(GridPane grid, int targetRow, boolean visible) {
        for (javafx.scene.Node node : grid.getChildren()) {
            Integer r = GridPane.getRowIndex(node);
            if (r != null && r == targetRow) {
                node.setVisible(visible);
                node.setManaged(visible);
            }
        }
    }

    // ==================== 通用：AdminUserVO 列表 + 搜索 + 分页 ====================

    private void loadUserPage(String role, TableView<AdminUserVO> table, HBox searchBar,
                               int[] pageRef, int[] totalRef) {
        try {
            String schId = getSearchId(searchBar);
            String name = getSearchName(searchBar);
            PageResult<AdminUserVO> result = ApiClient.getAdminUserList(role, pageRef[0], 10, schId, name);
            if (result != null) {
                table.setItems(FXCollections.observableArrayList(
                        result.getList() != null ? result.getList() : List.of()));
                totalRef[0] = Math.max(1, (int) Math.ceil((double) result.getTotal() / 10));
                updatePaginationLabel(searchBar, pageRef[0], totalRef[0]);
            }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private List<TableColumn<AdminUserVO, ?>> baseUserColumns() {
        TableColumn<AdminUserVO, String> schIdCol = new TableColumn<>("学号/工号");
        schIdCol.setCellValueFactory(new PropertyValueFactory<>("schId"));
        TableColumn<AdminUserVO, String> usernameCol = new TableColumn<>("用户名");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<AdminUserVO, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<AdminUserVO, String> phoneCol = new TableColumn<>("手机号");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        return List.of(schIdCol, usernameCol, nameCol, phoneCol);
    }

    private TableColumn<AdminUserVO, Void> createUserActionCol(String role,
                                                                 TableView<AdminUserVO> table,
                                                                 HBox searchBar,
                                                                 int[] pageRef, int[] totalRef) {
        TableColumn<AdminUserVO, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            private final Button resetPwdBtn = new Button("重置密码");
            private final Button detailBtn = new Button("详情");
            private final HBox box;
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                resetPwdBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                detailBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
                box = "STUDENT".equals(role) ? new HBox(5, editBtn, deleteBtn, resetPwdBtn, detailBtn) : new HBox(5, editBtn, deleteBtn, resetPwdBtn);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                AdminUserVO user = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showAddEditUserDialog(user, role));
                detailBtn.setOnAction(e -> showStudentDetailDialog(user));
                deleteBtn.setOnAction(e -> {
                    if (showConfirm("删除确认", "确定要删除用户 " + user.getUsername() + " 吗？")) {
                        try {
                            ApiClient.deleteUser(user.getId());
                            showInfo("删除成功");
                            loadUserPage(role, table, searchBar, pageRef, totalRef);
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    }
                });
                resetPwdBtn.setOnAction(e -> {
                    if (showConfirm("重置密码", "确定要将用户 " + user.getUsername() + " 的密码重置为 123456 吗？")) {
                        try {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", user.getId());
                            data.put("password", "123456");
                            ApiClient.updateUser(data);
                            showInfo("密码已重置为 123456");
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    }
                });
                setGraphic(box);
            }
        });
        return actionCol;
    }

    // ==================== 1. 教师管理（合并教师用户管理） ====================

    private void showTeacherManagement() {
        contentArea.getChildren().clear();
        sectionTitle.setText("教师管理");

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Button addBtn = new Button("添加教师");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        HBox searchBar = createSearchBar("工号", "姓名",
                () -> refreshTeacherTable(), () -> refreshTeacherTable());
        searchBar.getChildren().add(addBtn);

        var pageRef = new int[]{1};
        var totalRef = new int[]{1};

        TableView<AdminUserVO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(baseUserColumns());
        table.getColumns().add(createUserActionCol("TEACHER", table, searchBar, pageRef, totalRef));

        addBtn.setOnAction(e -> showAddEditUserDialog(null, "TEACHER"));

        String role = "TEACHER";
        Runnable loadPage = () -> loadUserPage(role, table, searchBar, pageRef, totalRef);

        Button searchBtn = (Button) searchBar.getChildren().get(2);
        searchBtn.setOnAction(e -> { pageRef[0] = 1; loadPage.run(); });
        Button resetBtn = (Button) searchBar.getChildren().get(3);
        resetBtn.setOnAction(e -> {
            ((TextField)((TextField[])searchBar.getUserData())[0]).clear();
            ((TextField)((TextField[])searchBar.getUserData())[1]).clear();
            pageRef[0] = 1; loadPage.run();
        });

        loadPage.run();

        HBox pagination = createPagination(pageRef[0], totalRef[0],
                () -> { if (pageRef[0] > 1) { pageRef[0]--; loadPage.run(); } },
                () -> { if (pageRef[0] < totalRef[0]) { pageRef[0]++; loadPage.run(); } });

        panel.getChildren().addAll(searchBar, table, pagination);
        contentArea.getChildren().add(panel);
    }

    private void refreshTeacherTable() { showTeacherManagement(); }

    // ==================== 2. 学生管理（合并学生用户管理） ====================

    private void showStudentManagement() {
        contentArea.getChildren().clear();
        sectionTitle.setText("学生管理");

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Button addBtn = new Button("添加学生");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        HBox searchBar = createSearchBar("学号", "姓名",
                () -> refreshStudentTable(), () -> refreshStudentTable());
        searchBar.getChildren().add(addBtn);

        var pageRef = new int[]{1};
        var totalRef = new int[]{1};

        TableView<AdminUserVO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(baseUserColumns());
        TableColumn<AdminUserVO, String> majorCol = new TableColumn<>("专业");
        majorCol.setCellValueFactory(new PropertyValueFactory<>("major"));
        TableColumn<AdminUserVO, String> genderCol = new TableColumn<>("性别");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        TableColumn<AdminUserVO, Integer> classCol = new TableColumn<>("班级");
        classCol.setCellValueFactory(new PropertyValueFactory<>("sClass"));
        table.getColumns().addAll(majorCol, genderCol, classCol);
        table.getColumns().add(createUserActionCol("STUDENT", table, searchBar, pageRef, totalRef));

        addBtn.setOnAction(e -> showAddEditUserDialog(null, "STUDENT"));

        String role = "STUDENT";
        Runnable loadPage = () -> loadUserPage(role, table, searchBar, pageRef, totalRef);

        Button searchBtn = (Button) searchBar.getChildren().get(2);
        searchBtn.setOnAction(e -> { pageRef[0] = 1; loadPage.run(); });
        Button resetBtn = (Button) searchBar.getChildren().get(3);
        resetBtn.setOnAction(e -> {
            ((TextField)((TextField[])searchBar.getUserData())[0]).clear();
            ((TextField)((TextField[])searchBar.getUserData())[1]).clear();
            pageRef[0] = 1; loadPage.run();
        });

        loadPage.run();

        HBox pagination = createPagination(pageRef[0], totalRef[0],
                () -> { if (pageRef[0] > 1) { pageRef[0]--; loadPage.run(); } },
                () -> { if (pageRef[0] < totalRef[0]) { pageRef[0]++; loadPage.run(); } });

        panel.getChildren().addAll(searchBar, table, pagination);
        contentArea.getChildren().add(panel);
    }

    private void refreshStudentTable() { showStudentManagement(); }

    // ==================== 3. 管理员管理 ====================

    private void showAdminManagement() {
        contentArea.getChildren().clear();
        sectionTitle.setText("管理员管理");

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Button addBtn = new Button("添加管理员");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        HBox searchBar = createSearchBar("工号", "姓名",
                () -> refreshAdminTable(), () -> refreshAdminTable());
        searchBar.getChildren().add(addBtn);

        var pageRef = new int[]{1};
        var totalRef = new int[]{1};

        TableView<AdminUserVO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(baseUserColumns());
        table.getColumns().add(createUserActionCol("ADMIN", table, searchBar, pageRef, totalRef));

        addBtn.setOnAction(e -> showAddEditUserDialog(null, "ADMIN"));

        String role = "ADMIN";
        Runnable loadPage = () -> loadUserPage(role, table, searchBar, pageRef, totalRef);

        Button searchBtn = (Button) searchBar.getChildren().get(2);
        searchBtn.setOnAction(e -> { pageRef[0] = 1; loadPage.run(); });
        Button resetBtn = (Button) searchBar.getChildren().get(3);
        resetBtn.setOnAction(e -> {
            ((TextField)((TextField[])searchBar.getUserData())[0]).clear();
            ((TextField)((TextField[])searchBar.getUserData())[1]).clear();
            pageRef[0] = 1; loadPage.run();
        });

        loadPage.run();

        HBox pagination = createPagination(pageRef[0], totalRef[0],
                () -> { if (pageRef[0] > 1) { pageRef[0]--; loadPage.run(); } },
                () -> { if (pageRef[0] < totalRef[0]) { pageRef[0]++; loadPage.run(); } });

        panel.getChildren().addAll(searchBar, table, pagination);
        contentArea.getChildren().add(panel);
    }

    private void refreshAdminTable() { showAdminManagement(); }

    // ==================== 通用：添加/编辑用户对话框 ====================

    private void showAddEditUserDialog(AdminUserVO existingUser, String role) {
        Dialog<ButtonType> dialog = new Dialog<>();
        String roleLabel = role.equals("TEACHER") ? "教师" : role.equals("STUDENT") ? "学生" : "管理员";
        dialog.setTitle(existingUser == null ? "添加" + roleLabel : "编辑" + roleLabel);
        dialog.setResizable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField schIdField = new TextField();

        int row = 0;
        if (existingUser == null) {
            grid.add(new Label("用户名:"), 0, row); grid.add(usernameField, 1, row++);
            grid.add(new Label("密码:"), 0, row); grid.add(passwordField, 1, row++);
        }
        grid.add(new Label("姓名:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("手机号:"), 0, row); grid.add(phoneField, 1, row++);
        Label schIdLabel = new Label(
                role.equals("TEACHER") ? "工号:" : role.equals("STUDENT") ? "学号:" : "工号:");
        grid.add(schIdLabel, 0, row); grid.add(schIdField, 1, row++);

        // 学生专用字段
        TextField majorField = new TextField();
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("男", "女");
        TextField classField = new TextField();
        int majorRow = row;
        grid.add(new Label("专业:"), 0, row); grid.add(majorField, 1, row++);
        int genderRow = row;
        grid.add(new Label("性别:"), 0, row); grid.add(genderCombo, 1, row++);
        int classRow = row;
        grid.add(new Label("班级:"), 0, row); grid.add(classField, 1, row++);

        boolean isStudent = "STUDENT".equals(role);
        setRowVisible(grid, majorRow, isStudent);
        setRowVisible(grid, genderRow, isStudent);
        setRowVisible(grid, classRow, isStudent);

        if (existingUser != null) {
            usernameField.setText(existingUser.getUsername());
            usernameField.setDisable(true);
            nameField.setText(existingUser.getName());
            phoneField.setText(existingUser.getPhone() != null ? existingUser.getPhone() : "");
            schIdField.setText(existingUser.getSchId() != null ? existingUser.getSchId() : "");
            schIdField.setDisable(true);
            if (isStudent) {
                majorField.setText(existingUser.getMajor() != null ? existingUser.getMajor() : "");
                genderCombo.setValue(existingUser.getGender() != null ? existingUser.getGender() : "男");
                classField.setText(existingUser.getSClass() != null ? String.valueOf(existingUser.getSClass()) : "");
            }
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    // 必填字段校验
                    String phone = phoneField.getText().trim();
                    String nameText = nameField.getText().trim();
                    String schIdText = schIdField.getText().trim();
                    if (existingUser == null) {
                        String uname = usernameField.getText().trim();
                        String pwd = passwordField.getText();
                        if (uname.isEmpty() || pwd.isEmpty() || nameText.isEmpty() || schIdText.isEmpty()) {
                            showWarning("请填写所有必填字段（用户名、密码、姓名、工号/学号）"); return;
                        }
                        if (uname.length() < 3) { showWarning("用户名长度不能少于3位"); return; }
                        if (pwd.length() < 6) { showWarning("密码长度不能少于6位"); return; }
                    } else {
                        if (nameText.isEmpty()) { showWarning("姓名不能为空"); return; }
                    }
                    // 电话校验
                    String phoneErr = validatePhone(phone);
                    if (phoneErr != null) { showWarning(phoneErr); return; }
                    // 班级校验
                    String classText = classField.getText().trim();
                    if (isStudent) {
                        String majorText = majorField.getText().trim();
                        if (majorText.isEmpty() || classText.isEmpty()) {
                            showWarning("请填写专业和班级"); return;
                        }
                        if (!classText.isEmpty()) {
                            String classErr = validatePositiveInt(classText, "班级");
                            if (classErr != null) { showWarning(classErr); return; }
                        }
                    }

                    if (existingUser != null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", existingUser.getId());
                        data.put("name", nameField.getText().trim());
                        data.put("phone", phone);
                        if (isStudent) {
                            data.put("major", majorField.getText().trim());
                            data.put("gender", genderCombo.getValue());
                            if (!classText.isEmpty())
                                data.put("sClass", Integer.parseInt(classText));
                        }
                        ApiClient.updateUser(data);
                        showInfo("修改成功");
                    } else if (isStudent) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("username", usernameField.getText().trim());
                        data.put("password", passwordField.getText());
                        data.put("name", nameField.getText().trim());
                        data.put("phone", phone);
                        data.put("sid", schIdField.getText().trim());
                        data.put("major", majorField.getText().trim());
                        data.put("gender", genderCombo.getValue());
                        if (!classText.isEmpty())
                            data.put("s_class", Integer.parseInt(classText));
                        ApiClient.addStudentUser(data);
                        showInfo("添加成功");
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("username", usernameField.getText().trim());
                        data.put("password", passwordField.getText());
                        data.put("name", nameField.getText().trim());
                        data.put("phone", phone);
                        data.put("sch_id", schIdField.getText().trim());
                        data.put("role", role);
                        ApiClient.addUser(data);
                        showInfo("添加成功");
                    }
                    switch (role) {
                        case "TEACHER" -> refreshTeacherTable();
                        case "STUDENT" -> refreshStudentTable();
                        default -> refreshAdminTable();
                    }
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    // ==================== 4. 课程管理 ====================

    private void showCourseManagement() {
        contentArea.getChildren().clear();
        sectionTitle.setText("课程管理");

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        HBox searchBar = new HBox(10);
        TextField idField = new TextField();
        idField.setPromptText("课程ID");
        idField.setPrefWidth(100);
        TextField nameField = new TextField();
        nameField.setPromptText("课程名");
        nameField.setPrefWidth(200);
        Button searchBtn = new Button("搜索");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        Button resetBtn = new Button("重置");
        resetBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        Button addBtn = new Button("添加课程");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        ComboBox<Teacher> teacherFilter = new ComboBox<>();
        teacherFilter.setPromptText("按教师筛选");
        try { teacherFilter.getItems().addAll(ApiClient.getAllTeachers(null, null)); }
        catch (Exception ignored) {}

        searchBar.getChildren().addAll(idField, nameField, searchBtn, resetBtn, teacherFilter, addBtn);

        TableView<Course> table = new TableView<>();
        TableColumn<Course, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Course, String> cnameCol = new TableColumn<>("课程名");
        cnameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        TableColumn<Course, String> addrCol = new TableColumn<>("授课地点");
        addrCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        TableColumn<Course, String> tnameCol = new TableColumn<>("任课教师");
        tnameCol.setCellValueFactory(new PropertyValueFactory<>("teacherName"));

        TableColumn<Course, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            private final HBox box = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Course c = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showCourseEditDialog(c));
                deleteBtn.setOnAction(e -> {
                    if (showConfirm("删除确认", "确定要删除课程 " + c.getCourseName() + " 吗？")) {
                        try {
                            ApiClient.adminDeleteCourse(c.getId());
                            showInfo("删除成功");
                            showCourseManagement();
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    }
                });
                setGraphic(box);
            }
        });
        table.getColumns().addAll(idCol, cnameCol, addrCol, tnameCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        var pageRef = new int[]{1};
        var totalRef = new int[]{1};
        Runnable loadData = () -> {
            try {
                Integer searchId = idField.getText().trim().isEmpty() ? null : Integer.parseInt(idField.getText().trim());
                String searchName = nameField.getText().trim().isEmpty() ? null : nameField.getText().trim();
                String tid = teacherFilter.getValue() != null ? teacherFilter.getValue().getSchId() : null;

                PageResult<Course> result = ApiClient.getCourseList(pageRef[0], 10, searchId, searchName, tid);
                if (result != null) {
                    table.setItems(FXCollections.observableArrayList(result.getList()));
                    totalRef[0] = Math.max(1, (int) Math.ceil((double) result.getTotal() / 10));
                    updatePaginationLabel(searchBar, pageRef[0], totalRef[0]);
                }
            } catch (Exception e) { showError(e.getMessage()); }
        };

        searchBtn.setOnAction(e -> { pageRef[0] = 1; loadData.run(); });
        resetBtn.setOnAction(e -> { idField.clear(); nameField.clear(); teacherFilter.setValue(null); pageRef[0] = 1; loadData.run(); });
        addBtn.setOnAction(e -> showCourseEditDialog(null));
        teacherFilter.setOnAction(e -> { pageRef[0] = 1; loadData.run(); });
        loadData.run();

        HBox pagination = createPagination(pageRef[0], totalRef[0],
                () -> { if (pageRef[0] > 1) { pageRef[0]--; loadData.run(); } },
                () -> { if (pageRef[0] < totalRef[0]) { pageRef[0]++; loadData.run(); } });

        panel.getChildren().addAll(searchBar, table, pagination);
        contentArea.getChildren().add(panel);
    }

    private void showCourseEditDialog(Course existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "添加课程" : "编辑课程");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField courseIdField = new TextField();
        TextField nameField = new TextField();
        TextArea detailField = new TextArea();
        detailField.setPrefRowCount(3);
        TextField addressField = new TextField();
        ComboBox<Teacher> teacherCombo = new ComboBox<>();
        try { teacherCombo.getItems().addAll(ApiClient.getAllTeachers(null, null)); }
        catch (Exception ignored) {}

        // 编辑已有课程时，通过详情接口获取完整信息（含 detail、teacherId）
        final Course[] fullCourseRef = new Course[1];
        if (existing != null) {
            Course full;
            try { full = ApiClient.getCourseDetail(existing.getId()); }
            catch (Exception e) { full = existing; }
            if (full == null) full = existing;
            fullCourseRef[0] = full;

            courseIdField.setText(String.valueOf(full.getId()));
            courseIdField.setDisable(true);
            nameField.setText(full.getCourseName() != null ? full.getCourseName() : "");
            detailField.setText(full.getDetail() != null ? full.getDetail() : "");
            addressField.setText(full.getAddress() != null ? full.getAddress() : "");
            if (full.getTeacherId() != null) {
                for (Teacher t : teacherCombo.getItems()) {
                    if (full.getTeacherId().equals(t.getSchId())) { teacherCombo.setValue(t); break; }
                }
            }
        }

        int row = 0;
        grid.add(new Label("课程ID:"), 0, row); grid.add(courseIdField, 1, row++);
        grid.add(new Label("课程名:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("课程详情:"), 0, row); grid.add(detailField, 1, row++);
        grid.add(new Label("授课地点:"), 0, row); grid.add(addressField, 1, row++);
        grid.add(new Label("任课教师:"), 0, row); grid.add(teacherCombo, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // 必填校验
                String cid = courseIdField.getText().trim();
                String cname = nameField.getText() != null ? nameField.getText().trim() : "";
                String caddr = addressField.getText() != null ? addressField.getText().trim() : "";
                if (existing == null && cid.isEmpty()) { showWarning("请输入课程ID"); return; }
                if (cname.isEmpty()) { showWarning("课程名不能为空"); return; }
                if (caddr.isEmpty()) { showWarning("授课地点不能为空"); return; }

                Map<String, Object> data = new HashMap<>();
                if (existing != null) {
                    data.put("id", fullCourseRef[0].getId());
                } else {
                    data.put("courseId", cid);
                }
                data.put("courseName", cname);
                data.put("detail", detailField.getText() != null ? detailField.getText().trim() : "");
                data.put("address", caddr);
                if (teacherCombo.getValue() != null) data.put("teacherId", teacherCombo.getValue().getSchId());

                try {
                    if (existing != null) {
                        ApiClient.adminUpdateCourse(data);
                    } else {
                        ApiClient.adminAddCourse(data);
                    }
                    showInfo(existing != null ? "修改成功" : "添加成功");
                    showCourseManagement();
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    // ==================== 5. 公告管理 ====================

    private void showAnnouncementManagement() {
        contentArea.getChildren().clear();
        sectionTitle.setText("公告管理");

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Button publishBtn = new Button("发布公告");
        publishBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        HBox searchBar = createSearchBar("公告ID", "标题",
                () -> refreshAnnouncementTable(), () -> refreshAnnouncementTable());
        searchBar.getChildren().add(publishBtn);

        TableView<Announcement> table = new TableView<>();
        TableColumn<Announcement, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Announcement, String> titleCol = new TableColumn<>("标题");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Announcement, String> publisherCol = new TableColumn<>("发布人");
        publisherCol.setCellValueFactory(new PropertyValueFactory<>("publisherName"));
        TableColumn<Announcement, String> timeCol = new TableColumn<>("发布时间");
        timeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        formatDateTime(cellData.getValue().getPublishTime())));

        TableColumn<Announcement, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button detailBtn = new Button("详情");
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            private final HBox box = new HBox(4, detailBtn, editBtn, deleteBtn);
            {
                detailBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Announcement a = getTableView().getItems().get(getIndex());
                detailBtn.setOnAction(e -> showAnnouncementDetail(a.getId()));
                editBtn.setOnAction(e -> showEditAnnouncementDialog(a));
                deleteBtn.setOnAction(e -> {
                    if (showConfirm("删除确认", "确定要删除公告 \"" + a.getTitle() + "\" 吗？")) {
                        try {
                            ApiClient.deleteAnnouncement(a.getId());
                            showInfo("删除成功");
                            refreshAnnouncementTable();
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    }
                });
                setGraphic(box);
            }
        });
        table.getColumns().addAll(idCol, titleCol, publisherCol, timeCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        publishBtn.setOnAction(e -> showPublishAnnouncementDialog());

        var annPageRef = new int[]{1};
        var annTotalRef = new int[]{1};
        Runnable load = () -> {
            try {
                String sId = getSearchId(searchBar);
                String sTitle = getSearchName(searchBar);
                PageResult<Announcement> result = ApiClient.getAnnouncementList(
                        annPageRef[0], 10, sId, sTitle);
                if (result != null) {
                    table.setItems(FXCollections.observableArrayList(
                            result.getList() != null ? result.getList() : List.of()));
                    annTotalRef[0] = Math.max(1, (int) Math.ceil((double) result.getTotal() / 10));
                    updatePaginationLabel(searchBar, annPageRef[0], annTotalRef[0]);
                }
            } catch (Exception e) { showError(e.getMessage()); }
        };

        ((Button) searchBar.getChildren().get(2)).setOnAction(e -> { annPageRef[0] = 1; load.run(); });
        ((Button) searchBar.getChildren().get(3)).setOnAction(e -> {
            ((TextField)((TextField[])searchBar.getUserData())[0]).clear();
            ((TextField)((TextField[])searchBar.getUserData())[1]).clear();
            annPageRef[0] = 1; load.run();
        });
        load.run();

        HBox annPagination = createPagination(annPageRef[0], annTotalRef[0],
                () -> { if (annPageRef[0] > 1) { annPageRef[0]--; load.run(); } },
                () -> { if (annPageRef[0] < annTotalRef[0]) { annPageRef[0]++; load.run(); } });

        panel.getChildren().addAll(searchBar, table, annPagination);
        contentArea.getChildren().add(panel);
    }

    private void refreshAnnouncementTable() { showAnnouncementManagement(); }

    private void showPublishAnnouncementDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("发布公告");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        TextArea contentField = new TextArea();
        contentField.setPrefRowCount(5);
        TextField publisherNameField = new TextField();

        grid.add(new Label("标题:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("内容:"), 0, 1); grid.add(contentField, 1, 1);
        grid.add(new Label("发布人姓名:"), 0, 2); grid.add(publisherNameField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String title = titleField.getText().trim();
                String content = contentField.getText() != null ? contentField.getText().trim() : "";
                if (title.isEmpty()) { showWarning("标题不能为空"); return; }
                if (content.isEmpty()) { showWarning("内容不能为空"); return; }
                try {
                    ApiClient.publishAnnouncement(title, content,
                            publisherNameField.getText().trim());
                    showInfo("发布成功");
                    refreshAnnouncementTable();
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    private void showEditAnnouncementDialog(Announcement existing) {
        // 先获取完整内容
        String fullContent = "";
        try {
            Announcement detail = ApiClient.getAnnouncementDetail(existing.getId());
            if (detail != null && detail.getContent() != null) fullContent = detail.getContent();
        } catch (Exception ignored) {}

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("编辑公告");
        dialog.setResizable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField titleField = new TextField(existing.getTitle() != null ? existing.getTitle() : "");
        TextArea contentArea = new TextArea(fullContent);
        contentArea.setPrefRowCount(5);
        TextField publisherNameField = new TextField(
                existing.getPublisherName() != null ? existing.getPublisherName() : "");

        grid.add(new Label("标题:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("内容:"), 0, 1); grid.add(contentArea, 1, 1);
        grid.add(new Label("发布人姓名:"), 0, 2); grid.add(publisherNameField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String title = titleField.getText().trim();
                String content = contentArea.getText() != null ? contentArea.getText().trim() : "";
                if (title.isEmpty()) { showWarning("标题不能为空"); return; }
                if (content.isEmpty()) { showWarning("内容不能为空"); return; }
                try {
                    ApiClient.updateAnnouncement(existing.getId(),
                            title, content,
                            publisherNameField.getText().trim());
                    showInfo("修改成功");
                    refreshAnnouncementTable();
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    private void showAnnouncementDetail(Integer id) {
        try {
            Announcement a = ApiClient.getAnnouncementDetail(id);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("公告详情");
            alert.setHeaderText(a.getTitle());
            alert.setContentText("发布人：" + a.getPublisherName() + "\n时间：" + formatDateTime(a.getPublishTime())
                    + "\n\n" + (a.getContent() != null ? a.getContent() : ""));
            alert.showAndWait();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    /** 查看学生详情（含扩展字段编辑） */
    private void showStudentDetailDialog(AdminUserVO user) {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("学生详情 - " + user.getName()); d.setResizable(true);
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(15));
        int r = 0;

        TextField nameF = new TextField(user.getName() != null ? user.getName() : "");
        TextField phoneF = new TextField(user.getPhone() != null ? user.getPhone() : "");
        TextField schIdF = new TextField(user.getSchId() != null ? user.getSchId() : ""); schIdF.setDisable(true);
        TextField majorF = new TextField(user.getMajor() != null ? user.getMajor() : "");
        ComboBox<String> genderCb = new ComboBox<>(); genderCb.getItems().addAll("男","女"); genderCb.setValue(user.getGender()!=null?user.getGender():"男");
        TextField classF = new TextField(user.getSClass() != null ? String.valueOf(user.getSClass()) : "");

        DatePicker birthPicker = new DatePicker();
        if (user.getBirthDate() != null && !user.getBirthDate().isEmpty())
            try { birthPicker.setValue(java.time.LocalDate.parse(user.getBirthDate())); } catch (Exception ignored) {}
        ComboBox<Integer> enrollCombo = new ComboBox<>();
        int thisYear = java.time.Year.now().getValue();
        for (int y = thisYear; y >= thisYear - 10; y--) enrollCombo.getItems().add(y);
        if (user.getEnrollmentYear() != null) enrollCombo.setValue(user.getEnrollmentYear());
        TextField idCardF = new TextField(user.getIdCard() != null ? user.getIdCard() : "");
        TextField nativeF = new TextField(user.getNativePlace() != null ? user.getNativePlace() : "");
        ComboBox<String> politicalCombo = new ComboBox<>();
        politicalCombo.getItems().addAll("群众","共青团员","中共党员","其他");
        if (user.getPoliticalStatus() != null) politicalCombo.setValue(user.getPoliticalStatus());
        TextField addressF = new TextField(user.getAddress() != null ? user.getAddress() : "");
        TextField contactNameF = new TextField(user.getContactName() != null ? user.getContactName() : "");
        TextField contactPhoneF = new TextField(user.getContactPhone() != null ? user.getContactPhone() : "");
        TextField relationF = new TextField(user.getSocialRelations() != null ? user.getSocialRelations() : "");

        g.addRow(r++, new Label("姓名:"), nameF);
        g.addRow(r++, new Label("手机号:"), phoneF);
        g.addRow(r++, new Label("学号:"), schIdF);
        g.addRow(r++, new Label("专业:"), majorF);
        g.addRow(r++, new Label("性别:"), genderCb);
        g.addRow(r++, new Label("班级:"), classF);
        g.addRow(r++, new Label("出生日期:"), birthPicker);
        g.addRow(r++, new Label("入学年份:"), enrollCombo);
        g.addRow(r++, new Label("身份证号:"), idCardF);
        g.addRow(r++, new Label("籍贯:"), nativeF);
        g.addRow(r++, new Label("政治面貌:"), politicalCombo);
        g.addRow(r++, new Label("家庭住址:"), addressF);
        g.addRow(r++, new Label("紧急联系人:"), contactNameF);
        g.addRow(r++, new Label("紧急联系人电话:"), contactPhoneF);
        g.addRow(r++, new Label("与紧急联系人关系:"), relationF);

        d.getDialogPane().setContent(new ScrollPane(g));
        ButtonType saveBtn = new ButtonType("保存", ButtonBar.ButtonData.APPLY);
        d.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CLOSE);
        d.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                String idCard = idCardF.getText().trim();
                if (!idCard.isEmpty()) {
                    if (!idCard.matches("\\d{17}[\\dXx]")) { showWarning("身份证号必须为18位数字"); return; }
                }
                String cp = contactPhoneF.getText().trim();
                if (!cp.isEmpty()) { String pe = validatePhone(cp); if (pe != null) { showWarning("紧急联系人电话: "+pe); return; } }
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", user.getId());
                    data.put("name", nameF.getText().trim());
                    data.put("phone", phoneF.getText().trim());
                    data.put("major", majorF.getText().trim());
                    data.put("gender", genderCb.getValue());
                    String ct = classF.getText().trim();
                    if (!ct.isEmpty()) data.put("sClass", Integer.parseInt(ct));
                    if (birthPicker.getValue() != null) data.put("birthDate", birthPicker.getValue().toString());
                    data.put("enrollmentYear", enrollCombo.getValue());
                    data.put("idCard", idCard);
                    data.put("nativePlace", nativeF.getText().trim());
                    data.put("politicalStatus", politicalCombo.getValue());
                    data.put("address", addressF.getText().trim());
                    data.put("contactName", contactNameF.getText().trim());
                    data.put("contactPhone", cp);
                    data.put("socialRelations", relationF.getText().trim());
                    ApiClient.updateUser(data);
                    showInfo("保存成功");
                } catch (Exception e) { showError("保存失败: " + e.getMessage()); }
            }
        });
    }

    // -- Honor Management --
    private void showHonorManagement() { contentArea.getChildren().clear(); sectionTitle.setText("荣誉管理");
        VBox p = new VBox(10); p.setPadding(new Insets(15));
        Button addBtn = new Button("添加荣誉"); addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        HBox bar = new HBox(10, new Label("学号:")); TextField sidF = new TextField(); sidF.setPrefWidth(130);
        Button searchBtn = new Button("搜索"); searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        bar.getChildren().addAll(sidF, searchBtn, addBtn);
        TableView<Map<String,Object>> table = new TableView<>();
        TableColumn<Map<String,Object>,String> col1 = new TableColumn<>("学号"); col1.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("sid")));
        TableColumn<Map<String,Object>,String> col2 = new TableColumn<>("标题"); col2.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("title")));
        TableColumn<Map<String,Object>,String> col3 = new TableColumn<>("类型"); col3.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("type")));
        TableColumn<Map<String,Object>,String> col4 = new TableColumn<>("级别"); col4.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("level")));
        TableColumn<Map<String,Object>,String> col5 = new TableColumn<>("日期"); col5.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().get("awardDate")!=null?d.getValue().get("awardDate").toString():""));
        TableColumn<Map<String,Object>,Void> act = new TableColumn<>("操作"); act.setCellFactory(col -> {
            javafx.scene.control.TableCell<Map<String,Object>,Void> cell = new javafx.scene.control.TableCell<>() {
                private final Button del = new Button("删除");
                { del.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"); }
                @Override protected void updateItem(Void i, boolean e) { super.updateItem(i,e); if(e){setGraphic(null);return;}
                    Map<String,Object> r = getTableView().getItems().get(getIndex());
                    del.setOnAction(ev -> { if(showConfirm("确认","确定删除?")){ApiClient.deleteHonor(((Number)r.get("id")).intValue()); refreshHonor(table,sidF.getText().trim());} });
                    setGraphic(del);
                }
            }; return cell;
        });
        table.getColumns().addAll(col1,col2,col3,col4,col5,act); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Runnable load = () -> { try { PageResult<Map<String,Object>> r = ApiClient.getHonorList(1,100,sidF.getText().trim()); if(r!=null&&r.getList()!=null) table.getItems().setAll(r.getList()); } catch(Exception ex){showError(ex.getMessage());} };
        searchBtn.setOnAction(e -> load.run()); load.run();
        addBtn.setOnAction(e -> { Dialog<ButtonType> d = new Dialog<>(); d.setTitle("添加荣誉"); GridPane g = new GridPane(); g.setHgap(10);g.setVgap(8);g.setPadding(new Insets(15));
            TextField sidI=new TextField(),titleI=new TextField(),typeI=new TextField(),levelI=new TextField(),descI=new TextField(); DatePicker dateI=new DatePicker();
            g.addRow(0,new Label("学号:"),sidI);g.addRow(1,new Label("标题:"),titleI);g.addRow(2,new Label("类型:"),typeI);g.addRow(3,new Label("级别:"),levelI);g.addRow(4,new Label("日期:"),dateI);g.addRow(5,new Label("描述:"),descI);
            d.getDialogPane().setContent(g); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
            d.showAndWait().ifPresent(r2->{if(r2==ButtonType.OK){try{Map<String,Object> m=new HashMap<>(); m.put("sid",sidI.getText().trim());m.put("title",titleI.getText().trim());m.put("type",typeI.getText().trim());m.put("level",levelI.getText().trim());m.put("awardDate",dateI.getValue()!=null?dateI.getValue().toString():"");m.put("description",descI.getText().trim()); ApiClient.addHonor(m);load.run();}catch(Exception ex){showError(ex.getMessage());}}}); });
        p.getChildren().addAll(bar,table); contentArea.getChildren().add(p);
    }
    private void refreshHonor(TableView<Map<String,Object>> t, String sid) { try { PageResult<Map<String,Object>> r = ApiClient.getHonorList(1,100,sid); if(r!=null&&r.getList()!=null) t.getItems().setAll(r.getList()); } catch(Exception ignored){} }

    // -- Practice Management --
    private void showPracticeManagement() { contentArea.getChildren().clear(); sectionTitle.setText("创新实践审批");
        VBox p = new VBox(10); p.setPadding(new Insets(15));
        TableView<Map<String,Object>> table = new TableView<>();
        TableColumn<Map<String,Object>,String> c1 = new TableColumn<>("学号"); c1.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("sid")));
        TableColumn<Map<String,Object>,String> c2 = new TableColumn<>("标题"); c2.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("title")));
        TableColumn<Map<String,Object>,String> c3 = new TableColumn<>("类型"); c3.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("type")));
        TableColumn<Map<String,Object>,String> c4 = new TableColumn<>("状态"); c4.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("status")));
        TableColumn<Map<String,Object>,Void> act = new TableColumn<>("操作"); act.setCellFactory(col -> {
            javafx.scene.control.TableCell<Map<String,Object>,Void> cell = new javafx.scene.control.TableCell<>() {
                private final Button app = new Button("通过"); { app.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;"); }
                private final Button rej = new Button("驳回"); { rej.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"); }
                private final HBox bx = new HBox(5, app, rej);
                @Override protected void updateItem(Void i, boolean e) { super.updateItem(i,e); if(e){setGraphic(null);return;}
                    Map<String,Object> r = getTableView().getItems().get(getIndex());
                    app.setOnAction(ev -> { Map<String,Object> d = new HashMap<>(); d.put("id",((Number)r.get("id")).longValue()); d.put("status","APPROVED"); d.put("comment",""); ApiClient.approvePractice(d); refreshPractice(table); });
                    rej.setOnAction(ev -> { Map<String,Object> d = new HashMap<>(); d.put("id",((Number)r.get("id")).longValue()); d.put("status","REJECTED"); d.put("comment",""); ApiClient.approvePractice(d); refreshPractice(table); });
                    setGraphic(bx);
                }
            }; return cell;
        });
        table.getColumns().addAll(c1,c2,c3,c4,act); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        refreshPractice(table);
        p.getChildren().add(table); contentArea.getChildren().add(p);
    }
    private void refreshPractice(TableView<Map<String,Object>> t) { try { PageResult<Map<String,Object>> r = ApiClient.getPendingPractices(1,100); if(r!=null&&r.getList()!=null) t.getItems().setAll(r.getList()); } catch(Exception ignored){} }

    // -- Leave Management --
    private void showLeaveManagement() { contentArea.getChildren().clear(); sectionTitle.setText("请假审批");
        VBox p = new VBox(10); p.setPadding(new Insets(15));
        TableView<Map<String,Object>> table = new TableView<>();
        TableColumn<Map<String,Object>,String> c1 = new TableColumn<>("学号"); c1.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("sid")));
        TableColumn<Map<String,Object>,String> c2 = new TableColumn<>("类型"); c2.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("type")));
        TableColumn<Map<String,Object>,String> c3 = new TableColumn<>("开始"); c3.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().get("startDate")!=null?d.getValue().get("startDate").toString():""));
        TableColumn<Map<String,Object>,String> c4 = new TableColumn<>("结束"); c4.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().get("endDate")!=null?d.getValue().get("endDate").toString():""));
        TableColumn<Map<String,Object>,String> c5 = new TableColumn<>("状态"); c5.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("status")));
        TableColumn<Map<String,Object>,Void> act = new TableColumn<>("操作"); act.setCellFactory(col -> {
            javafx.scene.control.TableCell<Map<String,Object>,Void> cell = new javafx.scene.control.TableCell<>() {
                private final Button app = new Button("通过"); { app.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;"); }
                private final Button rej = new Button("驳回"); { rej.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"); }
                private final HBox bx = new HBox(5, app, rej);
                @Override protected void updateItem(Void i, boolean e) { super.updateItem(i,e); if(e){setGraphic(null);return;}
                    Map<String,Object> r = getTableView().getItems().get(getIndex());
                    app.setOnAction(ev -> { Map<String,Object> d = new HashMap<>(); d.put("id",((Number)r.get("id")).longValue()); d.put("status","APPROVED"); d.put("comment",""); ApiClient.approveLeave(d); refreshLeave(table); });
                    rej.setOnAction(ev -> { Map<String,Object> d = new HashMap<>(); d.put("id",((Number)r.get("id")).longValue()); d.put("status","REJECTED"); d.put("comment",""); ApiClient.approveLeave(d); refreshLeave(table); });
                    setGraphic(bx);
                }
            }; return cell;
        });
        table.getColumns().addAll(c1,c2,c3,c4,c5,act); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        refreshLeave(table);
        p.getChildren().add(table); contentArea.getChildren().add(p);
    }
    private void refreshLeave(TableView<Map<String,Object>> t) { try { PageResult<Map<String,Object>> r = ApiClient.getPendingLeaves(1,100); if(r!=null&&r.getList()!=null) t.getItems().setAll(r.getList()); } catch(Exception ignored){} }

    // -- Activity Management --
    private void showActivityManagement() { contentArea.getChildren().clear(); sectionTitle.setText("活动管理");
        VBox p = new VBox(10); p.setPadding(new Insets(15));
        Button addBtn = new Button("发布活动"); addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        TableView<Map<String,Object>> table = new TableView<>();
        TableColumn<Map<String,Object>,String> c1 = new TableColumn<>("标题"); c1.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("title")));
        TableColumn<Map<String,Object>,String> c2 = new TableColumn<>("地点"); c2.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("location")));
        TableColumn<Map<String,Object>,String> c3 = new TableColumn<>("日期"); c3.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().get("date")!=null?d.getValue().get("date").toString():""));
        TableColumn<Map<String,Object>,String> c4 = new TableColumn<>("报名/上限"); c4.setCellValueFactory(d -> { Object rc=d.getValue().get("registered_count"); Object mp=d.getValue().get("maxParticipants"); return new javafx.beans.property.SimpleStringProperty((rc!=null?rc:"0")+"/"+(mp!=null&&(Integer)mp>0?mp:"∞")); });
        addBtn.setOnAction(e -> {
            Dialog<ButtonType> d = new Dialog<>(); d.setTitle("发布活动"); GridPane g = new GridPane(); g.setHgap(10);g.setVgap(8);g.setPadding(new Insets(15));
            TextField titleI=new TextField(),locI=new TextField(),cntI=new TextField(); TextArea contentI=new TextArea(); contentI.setPrefRowCount(3); DatePicker dateI=new DatePicker();
            g.addRow(0,new Label("标题:"),titleI);g.addRow(1,new Label("内容:"),contentI);g.addRow(2,new Label("地点:"),locI);g.addRow(3,new Label("日期:"),dateI);g.addRow(4,new Label("人数上限:"),cntI);
            d.getDialogPane().setContent(g); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
            d.showAndWait().ifPresent(r2->{if(r2==ButtonType.OK){try{Map<String,Object> m=new HashMap<>(); m.put("title",titleI.getText().trim());m.put("content",contentI.getText()!=null?contentI.getText().trim():"");m.put("location",locI.getText().trim());m.put("date",dateI.getValue()!=null?dateI.getValue().toString():"");m.put("maxParticipants",cntI.getText().trim().isEmpty()?0:Integer.parseInt(cntI.getText().trim())); ApiClient.publishActivity(m);refreshActivity(table);}catch(Exception ex){showError(ex.getMessage());}}});
        });
        TableColumn<Map<String,Object>,Void> act = new TableColumn<>("操作"); act.setCellFactory(col -> {
            javafx.scene.control.TableCell<Map<String,Object>,Void> cell = new javafx.scene.control.TableCell<>() {
                private final Button del = new Button("删除"); { del.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"); }
                @Override protected void updateItem(Void i, boolean e) { super.updateItem(i,e); if(e){setGraphic(null);return;}
                    Map<String,Object> r = getTableView().getItems().get(getIndex());
                    del.setOnAction(ev -> { if(showConfirm("确认","确定删除?")){ApiClient.adminDeleteActivity(((Number)r.get("id")).intValue()); refreshActivity(table);} });
                    setGraphic(del);
                }
            }; return cell;
        });
        table.getColumns().addAll(c1,c2,c3,c4,act); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        refreshActivity(table);
        p.getChildren().addAll(addBtn,table); contentArea.getChildren().add(p);
    }
    private void refreshActivity(TableView<Map<String,Object>> t) { try { PageResult<Map<String,Object>> r = ApiClient.getAdminActivities(1,100); if(r!=null&&r.getList()!=null) t.getItems().setAll(r.getList()); } catch(Exception ignored){} }

    /** 学期设置弹窗 */
    private void showSemesterSettings() {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("学期设置");
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(20));
        TextField nameF = new TextField(); nameF.setPromptText("学期名称");
        DatePicker dateF = new DatePicker();
        TextField weeksF = new TextField(); weeksF.setPromptText("总周数");
        g.add(new Label("学期名称:"),0,0); g.add(nameF,1,0);
        g.add(new Label("第一周周一:"),0,1); g.add(dateF,1,1);
        g.add(new Label("总周数:"),0,2); g.add(weeksF,1,2);
        try {
            SemesterConfig sc = ApiClient.getSemesterConfig();
            if (sc != null) { nameF.setText(sc.getSemesterName()!=null?sc.getSemesterName():""); weeksF.setText(sc.getTotalWeeks()!=null?String.valueOf(sc.getTotalWeeks()):""); }
        } catch (Exception ignored) {}
        d.getDialogPane().setContent(g); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) {
            Map<String, Object> data = new HashMap<>();
            data.put("semesterName", nameF.getText().trim());
            data.put("startWeekDate", dateF.getValue()!=null?dateF.getValue().toString():"");
            data.put("totalWeeks", Integer.parseInt(weeksF.getText().trim()));
            try { ApiClient.saveSemesterConfig(data); showInfo("保存成功"); } catch (Exception e) { showError(e.getMessage()); }
        }});
    }
}
