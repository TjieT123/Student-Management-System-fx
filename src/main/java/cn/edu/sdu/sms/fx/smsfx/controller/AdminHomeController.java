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

    private final List<Button> allMenuButtons = new ArrayList<>();
    private static final String BTN_BASE = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-padding: 8 20;";
    private static final String BTN_HOVER = "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-padding: 8 20;";
    private static final String BTN_ACTIVE = "-fx-background-color: #1a6fb5; -fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; -fx-padding: 8 20;";

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        homeBtn.setOnAction(e -> { showDashboard(); highlightButton(homeBtn); });
        sidebar.getChildren().clear();

        addSectionLabel("信息管理");
        addMenuButton("教师管理", e -> showTeacherManagement());
        addMenuButton("学生管理", e -> showStudentManagement());
        addMenuButton("管理员管理", e -> showAdminManagement());

        addSectionLabel("教务管理");
        addMenuButton("课程管理", e -> showCourseManagement());
        addMenuButton("公告管理", e -> showAnnouncementManagement());

        addSectionLabel("学工管理");
        addMenuButton("🏆 荣誉管理", e -> showHonorManagement());
        addMenuButton("💡 创新实践审批", e -> showPracticeManagement());
        addMenuButton("🏥 请假审批", e -> showLeaveManagement());
        addMenuButton("🎉 活动管理", e -> showActivityManagement());

        showDashboard();
    }

    private void addSectionLabel(String text) {
        Label lbl = new Label("  " + text);
        lbl.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11; -fx-padding: 10 10 2 10; -fx-font-weight: bold;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        sidebar.getChildren().add(lbl);
    }

    private void addMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(BTN_BASE);
        btn.setOnAction(e -> { handler.handle(e); highlightButton(btn); });
        btn.setOnMouseEntered(e -> { if (!BTN_ACTIVE.equals(btn.getStyle())) btn.setStyle(BTN_HOVER); });
        btn.setOnMouseExited(e -> { if (!BTN_ACTIVE.equals(btn.getStyle())) btn.setStyle(BTN_BASE); });
        allMenuButtons.add(btn);
        sidebar.getChildren().add(btn);
    }

    private void highlightButton(Button active) {
        for (Button b : allMenuButtons) b.setStyle(BTN_BASE);
        if (active != null) active.setStyle(BTN_ACTIVE);
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

            dash.getChildren().addAll(cards, pieBox);
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
        Button resetBtn = new Button("重置搜索");
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
        schIdCol.setPrefWidth(100);
        TableColumn<AdminUserVO, String> usernameCol = new TableColumn<>("用户名");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(90);
        TableColumn<AdminUserVO, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(70);
        TableColumn<AdminUserVO, String> phoneCol = new TableColumn<>("手机号");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);
        return List.of(schIdCol, usernameCol, nameCol, phoneCol);
    }

    private TableColumn<AdminUserVO, Void> createUserActionCol(String role,
                                                                 TableView<AdminUserVO> table,
                                                                 HBox searchBar,
                                                                 int[] pageRef, int[] totalRef) {
        TableColumn<AdminUserVO, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(210); actionCol.setMinWidth(180);
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
                box = new HBox(5, detailBtn, resetPwdBtn, deleteBtn);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                AdminUserVO user = getTableView().getItems().get(getIndex());
                detailBtn.setOnAction(e -> {
                    if ("STUDENT".equals(role)) showStudentDetailDialog(user,
                        () -> loadUserPage(role, table, searchBar, pageRef, totalRef));
                    else showUserDetailDialog(user, role);
                });
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
        table.setFixedCellSize(25);
        table.setPrefHeight(278);
        table.setMinHeight(278);
        table.setMaxHeight(278);
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
        table.setFixedCellSize(25);
        table.setPrefHeight(278);
        table.setMinHeight(278);
        table.setMaxHeight(278);
        table.getColumns().addAll(baseUserColumns());
        TableColumn<AdminUserVO, String> majorCol = new TableColumn<>("专业");
        majorCol.setCellValueFactory(new PropertyValueFactory<>("major"));
        TableColumn<AdminUserVO, String> genderCol = new TableColumn<>("性别");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(50); genderCol.setMaxWidth(60);
        TableColumn<AdminUserVO, Integer> classCol = new TableColumn<>("班级");
        classCol.setCellValueFactory(new PropertyValueFactory<>("sClass"));
        classCol.setPrefWidth(50); classCol.setMaxWidth(60);
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
        table.setFixedCellSize(25);
        table.setPrefHeight(278);
        table.setMinHeight(278);
        table.setMaxHeight(278);
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
        TextField passwordField = new TextField();
        passwordField.setText("123456"); // 默认密码，明文显示
        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField schIdField = new TextField();

        int row = 0;
        if (existingUser == null) {
            grid.add(new Label("用户名:"), 0, row); grid.add(usernameField, 1, row++);
            grid.add(new Label("密码:"), 0, row); grid.add(passwordField, 1, row++);
        }
        grid.add(new Label("姓名:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("手机号(选填):"), 0, row); grid.add(phoneField, 1, row++);
        Label schIdLabel = new Label(
                role.equals("TEACHER") ? "工号:" : role.equals("STUDENT") ? "学号:" : "工号:");
        grid.add(schIdLabel, 0, row); grid.add(schIdField, 1, row++);

        // 学生专用字段
        TextField majorField = new TextField();
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("男", "女");
        TextField classField = new TextField();
        ComboBox<Integer> gradeField = new ComboBox<>();
        for (int y = 2030; y >= 1930; y--) gradeField.getItems().add(y);
        int curYr = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        gradeField.setValue(curYr >= 1930 && curYr <= 2030 ? curYr : 2030);
        gradeField.setVisibleRowCount(15);
        int majorRow = row;
        grid.add(new Label("专业:"), 0, row); grid.add(majorField, 1, row++);
        int genderRow = row;
        grid.add(new Label("性别:"), 0, row); grid.add(genderCombo, 1, row++);
        int classRow = row;
        grid.add(new Label("班级:"), 0, row); grid.add(classField, 1, row++);
        int gradeRow = row;
        grid.add(new Label("年级:"), 0, row); grid.add(gradeField, 1, row++);

        boolean isStudent = "STUDENT".equals(role);
        setRowVisible(grid, majorRow, isStudent);
        setRowVisible(grid, genderRow, isStudent);
        setRowVisible(grid, classRow, isStudent);
        setRowVisible(grid, gradeRow, isStudent);

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
                if (existingUser.getGrade() != null) gradeField.setValue(existingUser.getGrade());
            }
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final boolean isStudentFinal = isStudent;
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            ev.consume(); // 阻止自动关闭，校验失败时对话框保持打开
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
                    if (!uname.matches("[\\x00-\\x7F]+")) { showWarning("用户名只能包含英文、数字和特殊字符，不能包含中文"); return; }
                    if (!pwd.matches("[\\x00-\\x7F]+")) { showWarning("密码只能包含英文、数字和特殊字符，不能包含中文"); return; }
                } else {
                    if (nameText.isEmpty()) { showWarning("姓名不能为空"); return; }
                }
                // 学号/工号校验：只能包含数字和字母
                if (!schIdText.isEmpty() && !schIdText.matches("[a-zA-Z0-9]+")) {
                    showWarning("学号/工号只能包含数字和字母"); return;
                }
                // 电话校验
                String phoneErr = validatePhone(phone);
                if (phoneErr != null) { showWarning(phoneErr); return; }
                // 班级校验
                String classText = classField.getText().trim();
                if (isStudentFinal) {
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
                    if (isStudentFinal) {
                        data.put("major", majorField.getText().trim());
                        data.put("gender", genderCombo.getValue());
                        if (!classText.isEmpty())
                            data.put("sClass", Integer.parseInt(classText));
                        if (gradeField.getValue() != null) data.put("grade", gradeField.getValue());
                    }
                    ApiClient.updateUser(data);
                    showInfo("修改成功");
                } else if (isStudentFinal) {
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
                    if (gradeField.getValue() != null) data.put("grade", String.valueOf(gradeField.getValue()));
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
                dialog.setResult(ButtonType.OK);
            } catch (Exception e) { showError(e.getMessage()); }
        });
        dialog.showAndWait();
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
        Button resetBtn = new Button("重置搜索");
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
        TableColumn<Course, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(d -> {
            String t = d.getValue().getType();
            return new javafx.beans.property.SimpleStringProperty("REQUIRED".equals(t)?"必修":"ELECTIVE".equals(t)?"选修":t!=null?t:"");
        });
        TableColumn<Course, String> credCol = new TableColumn<>("学分");
        credCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        TableColumn<Course, String> addrCol = new TableColumn<>("授课地点");
        addrCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        TableColumn<Course, String> tnameCol = new TableColumn<>("任课教师");
        tnameCol.setCellValueFactory(new PropertyValueFactory<>("teacherName"));

        TableColumn<Course, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button detailBtn = new Button("详情");
            private final Button deleteBtn = new Button("删除");
            private final HBox box = new HBox(5, detailBtn, deleteBtn);
            { detailBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;"); deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Course c = getTableView().getItems().get(getIndex());
                detailBtn.setOnAction(e -> showCourseDetailDialog(c));
                deleteBtn.setOnAction(e -> {
                    if (showConfirm("删除确认", "确定要删除课程 " + c.getCourseName() + " 吗？")) {
                        try { ApiClient.adminDeleteCourse(c.getId()); showInfo("删除成功"); showCourseManagement(); }
                        catch (Exception ex) { showError(ex.getMessage()); }
                    }
                });
                setGraphic(box);
            }
        });
        table.getColumns().addAll(idCol, cnameCol, typeCol, credCol, addrCol, tnameCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(25);
        table.setPrefHeight(278);
        table.setMinHeight(278);
        table.setMaxHeight(278);

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

    /** 课程详情弹窗（查看+编辑，不含材料） */
    private void showCourseDetailDialog(Course course) {
        Course full = course; try { Course f = ApiClient.getCourseDetail(course.getId()); if(f!=null) full=f; } catch(Exception ignored){}
        final Course cRef = full;
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("课程详情 - " + cRef.getCourseName()); d.setResizable(true);
        GridPane g = new GridPane(); g.setHgap(10);g.setVgap(8);g.setPadding(new Insets(15));
        TextField idF = new TextField(String.valueOf(cRef.getId())); idF.setDisable(true);
        TextField nameF = new TextField(cRef.getCourseName()!=null?cRef.getCourseName():"");
        ComboBox<String> typeCb = new ComboBox<>(); typeCb.getItems().addAll("必修","选修");
        typeCb.setValue("REQUIRED".equals(cRef.getType())?"必修":"ELECTIVE".equals(cRef.getType())?"选修":"必修");
        TextField credF = new TextField(cRef.getCredits()!=null?String.valueOf(cRef.getCredits().intValue()):"3");
        TextArea detailF = new TextArea(cRef.getDetail()!=null?cRef.getDetail():""); detailF.setPrefRowCount(4);
        TextField addrF = new TextField(cRef.getAddress()!=null?cRef.getAddress():"");
        ComboBox<Teacher> teacherCb = new ComboBox<>();
        try { teacherCb.getItems().addAll(ApiClient.getAllTeachers(null,null)); } catch(Exception ignored){}
        if(cRef.getTeacherId()!=null) for(Teacher t:teacherCb.getItems()) if(cRef.getTeacherId().equals(t.getSchId())){teacherCb.setValue(t);break;}
        java.util.List<Control> edits = java.util.List.of(nameF,typeCb,credF,detailF,addrF,teacherCb);
        edits.forEach(c->c.setDisable(true));
        g.addRow(0,new Label("课程ID:"),idF);g.addRow(1,new Label("课程名:"),nameF);
        g.addRow(2,new Label("类型:"),typeCb);g.addRow(3,new Label("学分:"),credF);
        g.addRow(4,new Label("课程详情:"),detailF);g.addRow(5,new Label("授课地点:"),addrF);
        g.addRow(6,new Label("任课教师:"),teacherCb);
        d.getDialogPane().setContent(g);
        ButtonType editBtn = new ButtonType("编辑",javafx.scene.control.ButtonBar.ButtonData.OTHER);
        ButtonType saveBtn = new ButtonType("保存",javafx.scene.control.ButtonBar.ButtonData.APPLY);
        d.getDialogPane().getButtonTypes().addAll(editBtn,saveBtn,ButtonType.CLOSE);
        javafx.scene.Node saveNode = d.getDialogPane().lookupButton(saveBtn);
        if(saveNode!=null) saveNode.setDisable(true);
        d.getDialogPane().lookupButton(editBtn).addEventFilter(javafx.event.ActionEvent.ACTION, ev->{
            ev.consume(); edits.forEach(c->c.setDisable(false)); if(saveNode!=null) saveNode.setDisable(false);
        });
        d.showAndWait().ifPresent(r->{if(r==saveBtn){
            String crText=credF.getText().trim(); if(crText.isEmpty()){showWarning("请输入学分");return;}
            double credits; try{credits=Double.parseDouble(crText);}catch(NumberFormatException ex){showWarning("学分必须为数字");return;}
            if(credits<=0){showWarning("学分必须大于0");return;}
            try{Map<String,Object> data=new HashMap<>(); data.put("id",cRef.getId());
                data.put("courseName",nameF.getText().trim());data.put("type","必修".equals(typeCb.getValue())?"REQUIRED":"ELECTIVE");
                data.put("credits",credits);
                data.put("detail",detailF.getText().trim());data.put("address",addrF.getText().trim());
                if(teacherCb.getValue()!=null) data.put("teacherId",teacherCb.getValue().getSchId());
                ApiClient.adminUpdateCourse(data);showInfo("保存成功");showCourseManagement();}
            catch(Exception ex){showError("保存失败: "+ex.getMessage());}
        }});
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
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("必修", "选修");
        typeCombo.setValue("必修");
        TextField creditsField = new TextField("3");
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
            if (full.getType() != null) typeCombo.setValue("REQUIRED".equals(full.getType()) ? "必修" : "选修");
            creditsField.setText(full.getCredits() != null ? String.valueOf(full.getCredits().intValue()) : "3");
            if (full.getTeacherId() != null) {
                for (Teacher t : teacherCombo.getItems()) {
                    if (full.getTeacherId().equals(t.getSchId())) { teacherCombo.setValue(t); break; }
                }
            }
        }

        int row = 0;
        if (existing != null) {
            grid.add(new Label("课程ID:"), 0, row); grid.add(courseIdField, 1, row++);
        }
        grid.add(new Label("课程名:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("课程类型:"), 0, row); grid.add(typeCombo, 1, row++);
        grid.add(new Label("学分:"), 0, row); grid.add(creditsField, 1, row++);
        grid.add(new Label("课程详情:"), 0, row); grid.add(detailField, 1, row++);
        grid.add(new Label("授课地点:"), 0, row); grid.add(addressField, 1, row++);
        grid.add(new Label("任课教师:"), 0, row); grid.add(teacherCombo, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            ev.consume(); // 阻止自动关闭
            String cname = nameField.getText() != null ? nameField.getText().trim() : "";
            String caddr = addressField.getText() != null ? addressField.getText().trim() : "";
            String crText = creditsField.getText() != null ? creditsField.getText().trim() : "3";
            if (cname.isEmpty()) { showWarning("课程名不能为空"); return; }
            if (caddr.isEmpty()) { showWarning("授课地点不能为空"); return; }
            if (teacherCombo.getValue() == null) { showWarning("请选择任课教师"); return; }
            double credits; try { credits = Double.parseDouble(crText); if (credits <= 0) { showWarning("学分必须大于0"); return; } } catch (NumberFormatException ex) { showWarning("学分必须为数字"); return; }
            Map<String, Object> data = new HashMap<>();
            if (existing != null) data.put("id", fullCourseRef[0].getId());
            data.put("courseName", cname);
            data.put("type", "必修".equals(typeCombo.getValue()) ? "REQUIRED" : "ELECTIVE");
            data.put("credits", credits);
            data.put("detail", detailField.getText() != null ? detailField.getText().trim() : "");
            data.put("address", caddr);
            data.put("teacherId", teacherCombo.getValue().getSchId());
            try {
                if (existing != null) ApiClient.adminUpdateCourse(data);
                else ApiClient.adminAddCourse(data);
                showInfo(existing != null ? "修改成功" : "添加成功");
                showCourseManagement();
                dialog.setResult(ButtonType.OK);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        dialog.showAndWait();
    }

    // ==================== 5. 公告管理 ====================

    private void showAnnouncementManagement() {
        contentArea.getChildren().clear();
        sectionTitle.setText("公告管理");

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Button publishBtn = new Button("发布公告");
        publishBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        HBox searchBar = new HBox(10);
        searchBar.setPadding(new Insets(5, 0, 5, 0));
        TextField annTitleField = new TextField();
        annTitleField.setPromptText("标题");
        annTitleField.setPrefWidth(200);
        Button annSearchBtn = new Button("搜索");
        annSearchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        Button annResetBtn = new Button("重置搜索");
        annResetBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        searchBar.getChildren().addAll(new Label(""), annTitleField, annSearchBtn, annResetBtn, publishBtn);

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
            private final Button deleteBtn = new Button("删除");
            private final HBox box = new HBox(5, detailBtn, deleteBtn);
            {
                detailBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-size: 11;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Announcement a = getTableView().getItems().get(getIndex());
                detailBtn.setOnAction(e -> showAnnouncementDetailDialog(a));
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
        table.getColumns().addAll(titleCol, publisherCol, timeCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(25);
        table.setPrefHeight(278);
        table.setMinHeight(278);
        table.setMaxHeight(278);

        publishBtn.setOnAction(e -> showPublishAnnouncementDialog());

        var annPageRef = new int[]{1};
        var annTotalRef = new int[]{1};
        Runnable load = () -> {
            try {
                String sTitle = annTitleField.getText().trim();
                if (sTitle.isEmpty()) sTitle = null;
                PageResult<Announcement> result = ApiClient.getAnnouncementList(
                        annPageRef[0], 10, null, sTitle);
                if (result != null) {
                    table.setItems(FXCollections.observableArrayList(
                            result.getList() != null ? result.getList() : List.of()));
                    annTotalRef[0] = Math.max(1, (int) Math.ceil((double) result.getTotal() / 10));
                    updatePaginationLabel(searchBar, annPageRef[0], annTotalRef[0]);
                }
            } catch (Exception e) { showError(e.getMessage()); }
        };

        annSearchBtn.setOnAction(e -> { annPageRef[0] = 1; load.run(); });
        annResetBtn.setOnAction(e -> { annTitleField.clear(); annPageRef[0] = 1; load.run(); });
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
        grid.add(new Label("发布者:"), 0, 2); grid.add(publisherNameField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            ev.consume();
            String title = titleField.getText().trim();
            String content = contentField.getText() != null ? contentField.getText().trim() : "";
            if (title.isEmpty()) { showWarning("标题不能为空"); return; }
            if (content.isEmpty()) { showWarning("内容不能为空"); return; }
            try {
                ApiClient.publishAnnouncement(title, content,
                        publisherNameField.getText().trim());
                showInfo("发布成功");
                refreshAnnouncementTable();
                dialog.setResult(ButtonType.OK);
            } catch (Exception e) { showError(e.getMessage()); }
        });
        dialog.showAndWait();
    }

    private void showAnnouncementDetailDialog(Announcement existing) {
        String fullContent = "";
        try { Announcement d = ApiClient.getAnnouncementDetail(existing.getId()); if(d!=null&&d.getContent()!=null) fullContent = d.getContent(); } catch(Exception ignored){}
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle("公告详情"); dialog.setResizable(true);
        GridPane g = new GridPane(); g.setHgap(10);g.setVgap(10);g.setPadding(new Insets(20));
        TextField titleF = new TextField(existing.getTitle()!=null?existing.getTitle():"");
        TextArea contentF = new TextArea(fullContent); contentF.setPrefRowCount(5);
        TextField pubF = new TextField(existing.getPublisherName()!=null?existing.getPublisherName():"");
        Label timeL = new Label(formatDateTime(existing.getPublishTime()));
        java.util.List<Control> edits = java.util.List.of(titleF,contentF,pubF);
        edits.forEach(c->c.setDisable(true));
        g.add(new Label("标题:"),0,0);g.add(titleF,1,0);
        g.add(new Label("发布时间:"),0,1);g.add(timeL,1,1);
        g.add(new Label("内容:"),0,2);g.add(contentF,1,2);
        g.add(new Label("发布人:"),0,3);g.add(pubF,1,3);
        dialog.getDialogPane().setContent(g);
        ButtonType editBtn = new ButtonType("编辑",ButtonBar.ButtonData.OTHER);
        ButtonType saveBtn = new ButtonType("保存",ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(editBtn,saveBtn,ButtonType.CLOSE);
        javafx.scene.Node saveNode = dialog.getDialogPane().lookupButton(saveBtn);
        if(saveNode!=null) saveNode.setDisable(true);
        dialog.getDialogPane().lookupButton(editBtn).addEventFilter(javafx.event.ActionEvent.ACTION, ev->{
            ev.consume(); edits.forEach(c->c.setDisable(false)); if(saveNode!=null) saveNode.setDisable(false);
        });
        dialog.showAndWait().ifPresent(r->{if(r==saveBtn){
            String t=titleF.getText().trim(),c=contentF.getText()!=null?contentF.getText().trim():"";
            if(t.isEmpty()){showWarning("标题不能为空");return;} if(c.isEmpty()){showWarning("内容不能为空");return;}
            try{ApiClient.updateAnnouncement(existing.getId(),t,c,pubF.getText().trim());showInfo("修改成功");refreshAnnouncementTable();}
            catch(Exception e){showError(e.getMessage());}
        }});
    }

    /** 查看学生详情（含扩展字段编辑） */
    private void showStudentDetailDialog(AdminUserVO user) { showStudentDetailDialog(user, null); }
    private void showStudentDetailDialog(AdminUserVO user, Runnable onSave) {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("学生详情 - " + user.getName()); d.setResizable(true);
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(15));
        int r = 0;

        TextField nameF = new TextField(user.getName() != null ? user.getName() : "");
        TextField phoneF = new TextField(user.getPhone() != null ? user.getPhone() : "");
        TextField schIdF = new TextField(user.getSchId() != null ? user.getSchId() : ""); schIdF.setDisable(true);
        TextField majorF = new TextField(user.getMajor() != null ? user.getMajor() : "");
        ComboBox<String> genderCb = new ComboBox<>(); genderCb.getItems().addAll("男","女"); genderCb.setValue(user.getGender()!=null?user.getGender():"男");
        TextField classF = new TextField(user.getSClass() != null ? String.valueOf(user.getSClass()) : "");

        TextField idCardF = new TextField(user.getIdCard() != null ? user.getIdCard() : "");
        DatePicker birthPicker = new DatePicker(); birthPicker.setEditable(false);
        if (user.getBirthDate() != null && !user.getBirthDate().isEmpty())
            try { birthPicker.setValue(java.time.LocalDate.parse(user.getBirthDate())); } catch (Exception ignored) {}
        Label birthHint = new Label("");
        birthHint.setStyle("-fx-font-size: 10; -fx-text-fill: #e67e22;");

        // Auto-parse birth date from ID card
        idCardF.textProperty().addListener((obs, old, val) -> {
            if (!idCardF.isVisible()) return;
            if (val != null && val.length() == 18) {
                try {
                    String bd = val.substring(6, 10) + "-" + val.substring(10, 12) + "-" + val.substring(12, 14);
                    java.time.LocalDate parsed = java.time.LocalDate.parse(bd);
                    birthPicker.setValue(parsed);
                    birthPicker.setDisable(true);
                    birthPicker.setStyle("-fx-opacity: 1; -fx-background-color: white;");
                    birthHint.setText("已从身份证号解析出生日期");
                    birthHint.setVisible(true);
                } catch (Exception ignored) {}
            } else {
                birthPicker.setDisable(false);
                birthPicker.setStyle(null);
                birthHint.setText("");
                birthHint.setVisible(false);
            }
        });
        // Lock birth date on init if ID card already has 18 digits
        if (user.getIdCard() != null && user.getIdCard().length() == 18) {
            birthPicker.setDisable(true);
            birthPicker.setStyle("-fx-opacity: 1; -fx-background-color: white;");
            birthHint.setText("已从身份证号解析出生日期");
        }

        TextField nativeF = new TextField(user.getNativePlace() != null ? user.getNativePlace() : "");
        ComboBox<String> politicalCombo = new ComboBox<>();
        politicalCombo.getItems().addAll("群众","共青团员","中共党员","其他");
        if (user.getPoliticalStatus() != null) politicalCombo.setValue(user.getPoliticalStatus());
        TextField addressF = new TextField(user.getAddress() != null ? user.getAddress() : "");
        TextField contactNameF = new TextField(user.getContactName() != null ? user.getContactName() : "");
        TextField contactPhoneF = new TextField(user.getContactPhone() != null ? user.getContactPhone() : "");
        TextField relationF = new TextField(user.getSocialRelations() != null ? user.getSocialRelations() : "");
        ComboBox<Integer> gradeF = new ComboBox<>();
        for (int y = 2030; y >= 1930; y--) gradeF.getItems().add(y);
        if (user.getGrade() != null) gradeF.setValue(user.getGrade());
        gradeF.setVisibleRowCount(15);

        g.addRow(r++, new Label("姓名:"), nameF);
        g.addRow(r++, new Label("手机号:"), phoneF);
        g.addRow(r++, new Label("学号:"), schIdF);
        g.addRow(r++, new Label("专业:"), majorF);
        g.addRow(r++, new Label("性别:"), genderCb);
        g.addRow(r++, new Label("班级:"), classF);
        g.addRow(r++, new Label("身份证号:"), idCardF);
        VBox birthBox = new VBox(2, birthPicker, birthHint);
        g.addRow(r++, new Label("出生日期:"), birthBox);
        g.addRow(r++, new Label("籍贯:"), nativeF);
        g.addRow(r++, new Label("政治面貌:"), politicalCombo);
        g.addRow(r++, new Label("家庭住址:"), addressF);
        g.addRow(r++, new Label("紧急联系人:"), contactNameF);
        g.addRow(r++, new Label("紧急联系人电话:"), contactPhoneF);
        g.addRow(r++, new Label("与紧急联系人关系:"), relationF);
        g.addRow(r++, new Label("年级:"), gradeF);

        java.util.List<Control> edits = java.util.List.of(nameF,phoneF,majorF,genderCb,classF,idCardF,birthPicker,nativeF,politicalCombo,addressF,contactNameF,contactPhoneF,relationF,gradeF);
        edits.forEach(c->c.setDisable(true));
        d.getDialogPane().setContent(new ScrollPane(g));
        ButtonType editBtn = new ButtonType("编辑", ButtonBar.ButtonData.OTHER);
        ButtonType saveBtn = new ButtonType("保存", ButtonBar.ButtonData.APPLY);
        d.getDialogPane().getButtonTypes().addAll(editBtn, saveBtn, ButtonType.CLOSE);
        javafx.scene.Node saveNode = d.getDialogPane().lookupButton(saveBtn);
        if(saveNode!=null) saveNode.setDisable(true);
        d.getDialogPane().lookupButton(editBtn).addEventFilter(javafx.event.ActionEvent.ACTION, ev->{
            ev.consume(); edits.forEach(c->c.setDisable(false)); if(saveNode!=null) saveNode.setDisable(false);
        });
        d.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                String idCard = idCardF.getText().trim();
                if (!idCard.isEmpty()) { String idErr = validateIdCard(idCard); if (idErr != null) { showWarning(idErr); return; } }
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
                    if (!ct.isEmpty()) {
                        try {
                            int cv = Integer.parseInt(ct);
                            if (cv <= 0) { showWarning("班级必须为大于0的正整数"); return; }
                            data.put("sClass", cv);
                        } catch (NumberFormatException ex) { showWarning("班级必须为大于0的正整数"); return; }
                    }
                    if (birthPicker.getValue() != null) data.put("birthDate", birthPicker.getValue().toString());
                    data.put("idCard", idCard);
                    data.put("nativePlace", nativeF.getText().trim());
                    data.put("politicalStatus", politicalCombo.getValue());
                    data.put("address", addressF.getText().trim());
                    data.put("contactName", contactNameF.getText().trim());
                    data.put("contactPhone", cp);
                    data.put("socialRelations", relationF.getText().trim());
                    if (gradeF.getValue() != null) data.put("grade", gradeF.getValue());
                    ApiClient.updateUser(data);
                    showInfo("保存成功");
                    if (onSave != null) onSave.run();
                } catch (Exception e) { showError("保存失败: " + e.getMessage()); }
            }
        });
    }

    /** 教师/管理员详情（查看+编辑） */
    private void showUserDetailDialog(AdminUserVO user, String role) {
        String roleLabel = "TEACHER".equals(role) ? "教师" : "管理员";
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle(roleLabel + "详情 - " + user.getName()); d.setResizable(true);
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(15));
        int r = 0;

        TextField nameF = new TextField(user.getName() != null ? user.getName() : "");
        TextField phoneF = new TextField(user.getPhone() != null ? user.getPhone() : "");
        TextField schIdF = new TextField(user.getSchId() != null ? user.getSchId() : ""); schIdF.setDisable(true);
        TextField usernameF = new TextField(user.getUsername() != null ? user.getUsername() : ""); usernameF.setDisable(true);

        g.addRow(r++, new Label("用户名:"), usernameF);
        g.addRow(r++, new Label("姓名:"), nameF);
        g.addRow(r++, new Label("手机号:"), phoneF);
        Label schIdLabel = new Label("TEACHER".equals(role) ? "工号:" : "工号:");
        g.addRow(r++, schIdLabel, schIdF);

        java.util.List<Control> edits = java.util.List.of(nameF, phoneF);
        edits.forEach(c -> c.setDisable(true));
        d.getDialogPane().setContent(g);
        ButtonType editBtn = new ButtonType("编辑", ButtonBar.ButtonData.OTHER);
        ButtonType saveBtn = new ButtonType("保存", ButtonBar.ButtonData.APPLY);
        d.getDialogPane().getButtonTypes().addAll(editBtn, saveBtn, ButtonType.CLOSE);
        javafx.scene.Node saveNode = d.getDialogPane().lookupButton(saveBtn);
        if(saveNode != null) saveNode.setDisable(true);
        d.getDialogPane().lookupButton(editBtn).addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            ev.consume(); edits.forEach(c -> c.setDisable(false)); if(saveNode != null) saveNode.setDisable(false);
        });
        d.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                String nameText = nameF.getText().trim();
                if (nameText.isEmpty()) { showWarning("姓名不能为空"); return; }
                String phone = phoneF.getText().trim();
                if (!phone.isEmpty()) { String pe = validatePhone(phone); if (pe != null) { showWarning(pe); return; } }
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", user.getId());
                    data.put("name", nameText);
                    data.put("phone", phone);
                    ApiClient.updateUser(data);
                    showInfo("保存成功");
                    switch (role) {
                        case "TEACHER" -> refreshTeacherTable();
                        default -> refreshAdminTable();
                    }
                } catch (Exception e) { showError("保存失败: " + e.getMessage()); }
            }
        });
    }

    // -- Honor Management --
    private void showHonorManagement() { contentArea.getChildren().clear(); sectionTitle.setText("荣誉管理");
        VBox p = new VBox(10); p.setPadding(new Insets(15));
        Button addBtn = new Button("发放荣誉"); addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        TextField nameF = new TextField(); nameF.setPrefWidth(120); nameF.setPromptText("姓名");
        Button searchBtn = new Button("搜索"); searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        Button resetBtn = new Button("重置搜索"); resetBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        HBox bar = new HBox(10, nameF, searchBtn, resetBtn, addBtn);
        TableView<Map<String,Object>> table = new TableView<>();
        TableColumn<Map<String,Object>,String> col0 = new TableColumn<>("姓名"); col0.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("studentName")));
        TableColumn<Map<String,Object>,String> col1 = new TableColumn<>("学号"); col1.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("sid")));
        TableColumn<Map<String,Object>,String> col2 = new TableColumn<>("标题"); col2.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("title")));
        TableColumn<Map<String,Object>,String> col3 = new TableColumn<>("比赛名称"); col3.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("type")));
        TableColumn<Map<String,Object>,String> col4 = new TableColumn<>("级别"); col4.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("level")));
        TableColumn<Map<String,Object>,String> col5 = new TableColumn<>("日期"); col5.setCellValueFactory(d -> {
            Object dt = d.getValue().get("award_date");
            if(dt == null) return new javafx.beans.property.SimpleStringProperty("");
            try { return new javafx.beans.property.SimpleStringProperty(fmtDateStr(dt)); }
            catch(Exception e) { return new javafx.beans.property.SimpleStringProperty(dt.toString()); }
        });
        final int[] hp = {1}, ht = {1};
        Runnable load = () -> refreshHonor(table,null,nameF.getText().trim(),hp,ht);
        Button prevB = new Button("上一页"), nextB = new Button("下一页"); Label pagLab = new Label();
        HBox pag = new HBox(15, prevB, pagLab, nextB); pag.setAlignment(javafx.geometry.Pos.CENTER); pag.setPadding(new Insets(10));
        Runnable pagRefresh = () -> { load.run(); pagLab.setText("第 "+hp[0]+"/"+ht[0]+" 页"); prevB.setDisable(hp[0]<=1); nextB.setDisable(hp[0]>=ht[0]); };
        prevB.setOnAction(e -> { if(hp[0]>1){hp[0]--;pagRefresh.run();} }); nextB.setOnAction(e -> { if(hp[0]<ht[0]){hp[0]++;pagRefresh.run();} });
        TableColumn<Map<String,Object>,Void> act = new TableColumn<>("操作"); act.setCellFactory(col -> {
            javafx.scene.control.TableCell<Map<String,Object>,Void> cell = new javafx.scene.control.TableCell<>() {
                private final Button detail = new Button("详情"); { detail.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;"); }
                private final Button del = new Button("删除"); { del.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"); }
                private final HBox bx = new HBox(5, detail, del);
                @Override protected void updateItem(Void i, boolean e) { super.updateItem(i,e); if(e){setGraphic(null);return;}
                    Map<String,Object> r = getTableView().getItems().get(getIndex());
                    detail.setOnAction(ev -> showHonorDetailDialog(((Number)r.get("id")).longValue(), pagRefresh));
                    del.setOnAction(ev -> { if(showConfirm("确认","确定删除?")){ApiClient.deleteHonor(((Number)r.get("id")).intValue()); load.run();} });
                    setGraphic(bx);
                }
            }; return cell;
        });
        table.getColumns().addAll(col0,col1,col2,col3,col4,col5,act); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(25);
        table.setPrefHeight(278);
        table.setMinHeight(278);
        table.setMaxHeight(278);
        searchBtn.setOnAction(e -> { hp[0]=1; pagRefresh.run(); });
        resetBtn.setOnAction(e -> { nameF.clear(); hp[0]=1; pagRefresh.run(); });
        pagRefresh.run();
        addBtn.setOnAction(e -> {
            Dialog<ButtonType> d = new Dialog<>(); d.setTitle("发放荣誉"); GridPane g = new GridPane(); g.setHgap(10);g.setVgap(8);g.setPadding(new Insets(15));
            TextField sidI=new TextField(),titleI=new TextField(),typeI=new TextField(),levelI=new TextField(),descI=new TextField(); DatePicker dateI=new DatePicker(); dateI.setEditable(false);
            g.addRow(0,new Label("学号:"),sidI);g.addRow(1,new Label("标题:"),titleI);g.addRow(2,new Label("比赛名称:"),typeI);g.addRow(3,new Label("级别(选填):"),levelI);g.addRow(4,new Label("日期:"),dateI);g.addRow(5,new Label("描述:"),descI);
            d.getDialogPane().setContent(g); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
            d.showAndWait().ifPresent(r2->{if(r2==ButtonType.OK){
                String sid=sidI.getText().trim(); if(sid.isEmpty()){showWarning("请输入学号");return;}
                if(titleI.getText().trim().isEmpty()){showWarning("请输入标题");return;}
                try{Map<String,Object> m=new HashMap<>(); m.put("sid",sid);m.put("title",titleI.getText().trim());m.put("type",typeI.getText().trim());m.put("level",levelI.getText().trim());if(dateI.getValue()!=null) m.put("awardDate",dateI.getValue().toString()); m.put("description",descI.getText().trim()); ApiClient.addHonor(m);pagRefresh.run();}catch(Exception ex){showError(ex.getMessage());}}});
        });
        p.getChildren().addAll(bar,table,pag); contentArea.getChildren().add(p);
    }
    private void showHonorDetailDialog(Long id, Runnable onSave) {
        try {
            Map<String,Object> h = ApiClient.getHonorDetail(id);
            if(h==null){showError("加载失败");return;}
            Dialog<ButtonType> d = new Dialog<>(); d.setTitle("荣誉详情"); d.setResizable(true);
            GridPane g = new GridPane(); g.setHgap(10);g.setVgap(8);g.setPadding(new Insets(15));
            TextField sidI=new TextField((String)h.get("sid")),titleI=new TextField((String)h.get("title")),typeI=new TextField((String)h.get("type")),levelI=new TextField((String)h.get("level")),descI=new TextField((String)h.get("description"));
            DatePicker dateI=new DatePicker(); dateI.setEditable(false); try{Object ad=h.get("award_date");if(ad!=null)dateI.setValue(java.time.LocalDate.parse(ad.toString().substring(0,10)));}catch(Exception ignored){}
            java.util.List<Control> edits = java.util.List.of(sidI,titleI,typeI,levelI,descI,dateI);
            edits.forEach(c->c.setDisable(true));
            g.addRow(0,new Label("学号:"),sidI);g.addRow(1,new Label("姓名:"),new Label((String)h.get("studentName")));
            g.addRow(2,new Label("标题:"),titleI);g.addRow(3,new Label("类型:"),typeI);g.addRow(4,new Label("级别:"),levelI);
            g.addRow(5,new Label("日期:"),dateI);g.addRow(6,new Label("描述:"),descI);
            d.getDialogPane().setContent(g);
            ButtonType editBtn = new ButtonType("编辑", javafx.scene.control.ButtonBar.ButtonData.OTHER);
            ButtonType saveBtn = new ButtonType("保存", javafx.scene.control.ButtonBar.ButtonData.APPLY);
            d.getDialogPane().getButtonTypes().addAll(editBtn, saveBtn, ButtonType.CLOSE);
            javafx.scene.Node saveNode = d.getDialogPane().lookupButton(saveBtn);
            if(saveNode!=null) saveNode.setDisable(true);
            d.getDialogPane().lookupButton(editBtn).addEventFilter(javafx.event.ActionEvent.ACTION, ev->{
                ev.consume();
                edits.forEach(c->c.setDisable(false));
                if(saveNode!=null) saveNode.setDisable(false);
            });
            d.showAndWait().ifPresent(r->{if(r==saveBtn){try{Map<String,Object> m=new HashMap<>(); m.put("id",id);m.put("sid",sidI.getText().trim());m.put("title",titleI.getText().trim());m.put("type",typeI.getText().trim());m.put("level",levelI.getText().trim());if(dateI.getValue()!=null) m.put("awardDate",dateI.getValue().toString()); m.put("description",descI.getText().trim()); ApiClient.updateHonor(m);onSave.run();}catch(Exception ex){showError(ex.getMessage());}}});
        } catch(Exception e){showError("加载失败: "+e.getMessage());}
    }
    private void refreshHonor(TableView<Map<String,Object>> t, String sid, String name, int[] page, int[] total) { try { PageResult<Map<String,Object>> r = ApiClient.getHonorList(page[0],10,sid,name); if(r!=null&&r.getList()!=null) t.getItems().setAll(r.getList()); total[0]=Math.max(1,(int)Math.ceil((double)r.getTotal()/10)); } catch(Exception ignored){} }
    private String fmtDateStr(Object dt) { try { String s=dt instanceof Number?new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(((Number)dt).longValue())):dt.toString(); if(s.length()>=10) s=s.substring(0,10); java.time.LocalDate ld=java.time.LocalDate.parse(s); return ld.getYear()+"年"+ld.getMonthValue()+"月"+ld.getDayOfMonth()+"日"; } catch(Exception e){return dt.toString();} }

    // -- Practice Management --
    private TextField practiceNameF, practiceTitleF;
    private ComboBox<String> practiceTypeCb, practiceStatusCb;
    private void showPracticeManagement() { contentArea.getChildren().clear(); sectionTitle.setText("创新实践审批");
        VBox p = new VBox(10); p.setPadding(new Insets(15));
        // Search bar
        practiceNameF = new TextField(); practiceNameF.setPrefWidth(100); practiceNameF.setPromptText("学生姓名");
        practiceTitleF = new TextField(); practiceTitleF.setPrefWidth(100); practiceTitleF.setPromptText("标题");
        practiceTypeCb = new ComboBox<>(); practiceTypeCb.getItems().addAll("全部类型","社会实践","学科竞赛","科技成果","培训讲座","创新项目","校外实习");
        practiceTypeCb.setValue("全部类型"); practiceTypeCb.setPrefWidth(100);
        practiceStatusCb = new ComboBox<>(); practiceStatusCb.getItems().addAll("全部状态","待审批","已通过","已驳回");
        practiceStatusCb.setValue("全部状态"); practiceStatusCb.setPrefWidth(100);
        Button searchBtn = new Button("搜索"); searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        Button resetBtn = new Button("重置搜索"); resetBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        HBox bar = new HBox(10, practiceNameF, practiceTitleF, practiceTypeCb, practiceStatusCb, searchBtn, resetBtn);

        TableView<Map<String,Object>> table = new TableView<>();
        TableColumn<Map<String,Object>,String> c0 = new TableColumn<>("姓名"); c0.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("student_name")));
        TableColumn<Map<String,Object>,String> c1 = new TableColumn<>("标题"); c1.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("title")));
        TableColumn<Map<String,Object>,String> c2 = new TableColumn<>("类型"); c2.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("type")));
        TableColumn<Map<String,Object>,String> c3 = new TableColumn<>("状态"); c3.setCellValueFactory(d -> {
            String st = (String)d.getValue().get("status");
            return new javafx.beans.property.SimpleStringProperty("PENDING".equals(st)?"待审批":"APPROVED".equals(st)?"已通过":"已驳回");
        });
        final int[] pp = {1}, pt = {1};
        Runnable load = () -> refreshPractice(table,pp,pt);
        Button prevB = new Button("上一页"), nextB = new Button("下一页"); Label pagLab = new Label();
        HBox pag = new HBox(15, prevB, pagLab, nextB); pag.setAlignment(javafx.geometry.Pos.CENTER); pag.setPadding(new Insets(10));
        Runnable refresh = () -> { load.run(); pagLab.setText("第 "+pp[0]+"/"+pt[0]+" 页"); prevB.setDisable(pp[0]<=1); nextB.setDisable(pp[0]>=pt[0]); };
        prevB.setOnAction(e -> { if(pp[0]>1){pp[0]--;refresh.run();} }); nextB.setOnAction(e -> { if(pp[0]<pt[0]){pp[0]++;refresh.run();} });
        searchBtn.setOnAction(e -> { pp[0]=1; refresh.run(); });
        resetBtn.setOnAction(e -> { practiceNameF.clear(); practiceTitleF.clear(); practiceTypeCb.setValue("全部类型"); practiceStatusCb.setValue("全部状态"); pp[0]=1; refresh.run(); });

        TableColumn<Map<String,Object>,Void> act = new TableColumn<>("操作"); act.setCellFactory(col -> {
            javafx.scene.control.TableCell<Map<String,Object>,Void> cell = new javafx.scene.control.TableCell<>() {
                private final Button detail = new Button("详情"); { detail.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;"); }
                @Override protected void updateItem(Void i, boolean e) { super.updateItem(i,e); if(e){setGraphic(null);return;}
                    Map<String,Object> r = getTableView().getItems().get(getIndex());
                    detail.setOnAction(ev -> showPracticeDetailDialog(((Number)r.get("id")).longValue(), refresh));
                    setGraphic(detail);
                }
            }; return cell;
        });
        table.getColumns().addAll(c0,c1,c2,c3,act); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(25);
        table.setPrefHeight(278);
        table.setMinHeight(278);
        table.setMaxHeight(278);
        refresh.run();
        p.getChildren().addAll(bar,table,pag); contentArea.getChildren().add(p);
    }
    private String getPracticeFilterName() { String v = practiceNameF != null ? practiceNameF.getText().trim() : ""; return v.isEmpty() ? null : v; }
    private String getPracticeFilterTitle() { String v = practiceTitleF != null ? practiceTitleF.getText().trim() : ""; return v.isEmpty() ? null : v; }
    private String getPracticeFilterType() { String v = practiceTypeCb != null ? practiceTypeCb.getValue() : "全部类型"; return "全部类型".equals(v) ? null : v; }
    private String getPracticeFilterStatus() { String v = practiceStatusCb != null ? practiceStatusCb.getValue() : "全部状态"; return "全部状态".equals(v) ? null : ("待审批".equals(v) ? "PENDING" : "已通过".equals(v) ? "APPROVED" : "REJECTED"); }
    private void refreshPractice(TableView<Map<String,Object>> t, int[] page, int[] total) { try { PageResult<Map<String,Object>> r = ApiClient.getPendingPractices(page[0],10, getPracticeFilterName(), getPracticeFilterTitle(), getPracticeFilterType(), getPracticeFilterStatus()); if(r!=null&&r.getList()!=null) t.getItems().setAll(r.getList()); total[0]=Math.max(1,(int)Math.ceil((double)r.getTotal()/10)); } catch(Exception ignored){} }

    private void showPracticeDetailDialog(Long id, Runnable onSave) {
        try {
            Map<String,Object> p = ApiClient.getPracticeDetail(id);
            if (p == null) { showError("加载失败: 未获取到数据"); return; }
            String st = (String)p.get("status");
            String stText = "PENDING".equals(st)?"待审批":"APPROVED".equals(st)?"已通过":"已驳回";

            Dialog<ButtonType> d = new Dialog<>(); d.setTitle("实践详情"); d.setResizable(true);
            VBox box = new VBox(8); box.setStyle("-fx-padding: 15;");
            box.getChildren().addAll(
                new Label("姓名: " + (p.get("student_name")!=null?p.get("student_name"):"")),
                new Label("学号: " + (p.get("sid")!=null?p.get("sid"):"")),
                new Label("标题: " + (p.get("title")!=null?p.get("title"):"")),
                new Label("类型: " + (p.get("type")!=null?p.get("type"):"")),
                new Label("开始日期: " + fmtDateStr(p.get("start_date"))),
                new Label("结束日期: " + fmtDateStr(p.get("end_date"))),
                new Label("组织单位: " + (p.get("organization")!=null?p.get("organization"):"")),
                new Label("担任角色: " + (p.get("role")!=null?p.get("role"):"")),
                new Label("状态: " + stText)
            );
            if (p.get("description") != null) box.getChildren().add(new Label("描述: " + p.get("description")));
            if (p.get("result") != null) box.getChildren().add(new Label("成果: " + p.get("result")));
            if (p.get("review_comment") != null && !p.get("review_comment").toString().isEmpty())
                box.getChildren().add(new Label("审批意见: " + p.get("review_comment")));
            // Attachments
            try {
                String attStr = (String) p.get("attachments");
                if (attStr != null && !attStr.isEmpty() && !"[]".equals(attStr)) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    List<Map<String, Object>> atts = mapper.readValue(attStr, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
                    box.getChildren().add(new Label("附件:"));
                    for (int i = 0; i < atts.size(); i++) {
                        Map<String, Object> ai = atts.get(i);
                        String fn = (String) ai.get("fileName");
                        String b64 = (String) ai.get("base64");
                        HBox attRow = new HBox(10);
                        attRow.getChildren().add(new Label("📎 " + fn));
                        Button downBtn = new Button("下载");
                        downBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
                        final byte[] data = java.util.Base64.getDecoder().decode(b64);
                        downBtn.setOnAction(ev -> {
                            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                            fc.setInitialFileName(fn);
                            java.io.File target = fc.showSaveDialog(null);
                            if (target != null) try { java.nio.file.Files.write(target.toPath(), data); } catch (Exception ex2) { showError("保存失败: "+ex2.getMessage()); }
                        });
                        attRow.getChildren().add(downBtn);
                        box.getChildren().add(attRow);
                    }
                }
            } catch (Exception ignored) {}

            d.getDialogPane().setContent(new ScrollPane(box));
            // Use explicit Buttons for approve/reject instead of ButtonType
            ButtonType closeBtn = new ButtonType("关闭", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            if ("PENDING".equals(st)) {
                ButtonType approveBtn = new ButtonType("通过", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                ButtonType rejectBtn = new ButtonType("驳回", javafx.scene.control.ButtonBar.ButtonData.OTHER);
                d.getDialogPane().getButtonTypes().addAll(approveBtn, rejectBtn, closeBtn);
                // Handle approve/reject via button lookup
                javafx.scene.Node approveNode = d.getDialogPane().lookupButton(approveBtn);
                javafx.scene.Node rejectNode = d.getDialogPane().lookupButton(rejectBtn);
                if (approveNode != null) approveNode.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
                    ev.consume();
                    TextInputDialog tid = new TextInputDialog();
                    tid.setTitle("审批通过"); tid.setHeaderText("请输入通过意见（可选）"); tid.setContentText("意见:");
                    tid.showAndWait().ifPresent(comment -> {
                        try {
                            Map<String,Object> d2 = new HashMap<>();
                            d2.put("id", id); d2.put("status", "APPROVED");
                            d2.put("comment", comment != null ? comment : "");
                            ApiClient.approvePractice(d2);
                            showInfo("已通过"); onSave.run();
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    });
                    // close this dialog
                    d.setResult(closeBtn);
                });
                if (rejectNode != null) rejectNode.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
                    ev.consume();
                    TextInputDialog tid = new TextInputDialog();
                    tid.setTitle("驳回"); tid.setHeaderText("请输入驳回原因"); tid.setContentText("原因:");
                    tid.showAndWait().ifPresent(comment -> {
                        try {
                            Map<String,Object> d2 = new HashMap<>();
                            d2.put("id", id); d2.put("status", "REJECTED");
                            d2.put("comment", comment != null ? comment : "");
                            ApiClient.approvePractice(d2);
                            showInfo("已驳回"); onSave.run();
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    });
                    d.setResult(closeBtn);
                });
            } else {
                d.getDialogPane().getButtonTypes().add(closeBtn);
            }
            d.showAndWait();
        } catch (Exception ex) { showError("加载失败: " + ex.getMessage()); }
    }

    // -- Leave Management --
    private TextField leaveNameF;
    private ComboBox<String> leaveTypeCb, leaveStatusCb;
    private void showLeaveManagement() { contentArea.getChildren().clear(); sectionTitle.setText("请假审批");
        VBox p = new VBox(10); p.setPadding(new Insets(15));
        // Search bar
        leaveNameF = new TextField(); leaveNameF.setPrefWidth(100); leaveNameF.setPromptText("学生姓名");
        leaveTypeCb = new ComboBox<>(); leaveTypeCb.getItems().addAll("全部类型","事假","病假","参赛","其他");
        leaveTypeCb.setValue("全部类型"); leaveTypeCb.setPrefWidth(100);
        leaveStatusCb = new ComboBox<>(); leaveStatusCb.getItems().addAll("全部状态","待审批","已通过","已驳回");
        leaveStatusCb.setValue("全部状态"); leaveStatusCb.setPrefWidth(100);
        Button searchBtn = new Button("搜索"); searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        Button resetBtn = new Button("重置搜索"); resetBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        HBox bar = new HBox(10, leaveNameF, leaveTypeCb, leaveStatusCb, searchBtn, resetBtn);

        TableView<Map<String,Object>> table = new TableView<>();
        TableColumn<Map<String,Object>,String> c0 = new TableColumn<>("姓名"); c0.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("student_name")));
        TableColumn<Map<String,Object>,String> c1 = new TableColumn<>("类型"); c1.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("type")));
        TableColumn<Map<String,Object>,String> c2 = new TableColumn<>("开始"); c2.setCellValueFactory(d -> {
            Object v = d.getValue().get("start_date");
            return new javafx.beans.property.SimpleStringProperty(v!=null?fmtDateStr(v):"");
        });
        TableColumn<Map<String,Object>,String> c3 = new TableColumn<>("结束"); c3.setCellValueFactory(d -> {
            Object v = d.getValue().get("end_date");
            return new javafx.beans.property.SimpleStringProperty(v!=null?fmtDateStr(v):"");
        });
        TableColumn<Map<String,Object>,String> c4 = new TableColumn<>("状态"); c4.setCellValueFactory(d -> {
            String st = (String)d.getValue().get("status");
            return new javafx.beans.property.SimpleStringProperty("PENDING".equals(st)?"待审批":"APPROVED".equals(st)?"已通过":"已驳回");
        });
        final int[] lp = {1}, lt = {1};
        Runnable load = () -> refreshLeave(table,lp,lt);
        Button prevB = new Button("上一页"), nextB = new Button("下一页"); Label pagLab = new Label();
        HBox pag = new HBox(15, prevB, pagLab, nextB); pag.setAlignment(javafx.geometry.Pos.CENTER); pag.setPadding(new Insets(10));
        Runnable refresh = () -> { load.run(); pagLab.setText("第 "+lp[0]+"/"+lt[0]+" 页"); prevB.setDisable(lp[0]<=1); nextB.setDisable(lp[0]>=lt[0]); };
        prevB.setOnAction(e -> { if(lp[0]>1){lp[0]--;refresh.run();} }); nextB.setOnAction(e -> { if(lp[0]<lt[0]){lp[0]++;refresh.run();} });
        searchBtn.setOnAction(e -> { lp[0]=1; refresh.run(); });
        resetBtn.setOnAction(e -> { leaveNameF.clear(); leaveTypeCb.setValue("全部类型"); leaveStatusCb.setValue("全部状态"); lp[0]=1; refresh.run(); });

        TableColumn<Map<String,Object>,Void> act = new TableColumn<>("操作"); act.setCellFactory(col -> {
            javafx.scene.control.TableCell<Map<String,Object>,Void> cell = new javafx.scene.control.TableCell<>() {
                private final Button detail = new Button("详情"); { detail.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;"); }
                @Override protected void updateItem(Void i, boolean e) { super.updateItem(i,e); if(e){setGraphic(null);return;}
                    Map<String,Object> r = getTableView().getItems().get(getIndex());
                    detail.setOnAction(ev -> showLeaveDetailDialog(((Number)r.get("id")).longValue(), refresh));
                    setGraphic(detail);
                }
            }; return cell;
        });
        table.getColumns().addAll(c0,c1,c2,c3,c4,act); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(25);
        table.setPrefHeight(278);
        table.setMinHeight(278);
        table.setMaxHeight(278);
        refresh.run();
        p.getChildren().addAll(bar,table,pag); contentArea.getChildren().add(p);
    }
    private String getLeaveFilterName() { String v = leaveNameF != null ? leaveNameF.getText().trim() : ""; return v.isEmpty() ? null : v; }
    private String getLeaveFilterType() { String v = leaveTypeCb != null ? leaveTypeCb.getValue() : "全部类型"; return "全部类型".equals(v) ? null : v; }
    private String getLeaveFilterStatus() { String v = leaveStatusCb != null ? leaveStatusCb.getValue() : "全部状态"; return "全部状态".equals(v) ? null : ("待审批".equals(v) ? "PENDING" : "已通过".equals(v) ? "APPROVED" : "REJECTED"); }
    private void refreshLeave(TableView<Map<String,Object>> t, int[] page, int[] total) { try { PageResult<Map<String,Object>> r = ApiClient.getPendingLeaves(page[0],10, getLeaveFilterName(), getLeaveFilterType(), getLeaveFilterStatus()); if(r!=null&&r.getList()!=null) t.getItems().setAll(r.getList()); total[0]=Math.max(1,(int)Math.ceil((double)r.getTotal()/10)); } catch(Exception ignored){} }

    private void showLeaveDetailDialog(Long id, Runnable onSave) {
        try {
            Map<String,Object> lr = ApiClient.getLeaveDetail(id);
            if (lr == null) { showError("加载失败: 未获取到数据"); return; }
            String st = (String)lr.get("status");
            String stText = "PENDING".equals(st)?"待审批":"APPROVED".equals(st)?"已通过":"已驳回";

            Dialog<ButtonType> d = new Dialog<>(); d.setTitle("请假详情"); d.setResizable(true);
            VBox box = new VBox(8); box.setStyle("-fx-padding: 15;");
            box.getChildren().addAll(
                new Label("姓名: " + (lr.get("student_name")!=null?lr.get("student_name"):"")),
                new Label("学号: " + (lr.get("sid")!=null?lr.get("sid"):"")),
                new Label("类型: " + (lr.get("type")!=null?lr.get("type"):"")),
                new Label("开始日期: " + fmtDateStr(lr.get("start_date"))),
                new Label("结束日期: " + fmtDateStr(lr.get("end_date"))),
                new Label("事由: " + (lr.get("reason")!=null?lr.get("reason"):"")),
                new Label("状态: " + stText)
            );
            if (lr.get("review_comment") != null && !lr.get("review_comment").toString().isEmpty())
                box.getChildren().add(new Label("审批意见: " + lr.get("review_comment")));

            d.getDialogPane().setContent(new ScrollPane(box));
            ButtonType closeBtn = new ButtonType("关闭", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            if ("PENDING".equals(st)) {
                ButtonType approveBtn = new ButtonType("通过", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                ButtonType rejectBtn = new ButtonType("驳回", javafx.scene.control.ButtonBar.ButtonData.OTHER);
                d.getDialogPane().getButtonTypes().addAll(approveBtn, rejectBtn, closeBtn);
                javafx.scene.Node approveNode = d.getDialogPane().lookupButton(approveBtn);
                javafx.scene.Node rejectNode = d.getDialogPane().lookupButton(rejectBtn);
                if (approveNode != null) approveNode.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
                    ev.consume();
                    TextInputDialog tid = new TextInputDialog();
                    tid.setTitle("审批通过"); tid.setHeaderText("请输入通过意见（可选）"); tid.setContentText("意见:");
                    tid.showAndWait().ifPresent(comment -> {
                        try {
                            Map<String,Object> d2 = new HashMap<>();
                            d2.put("id", id); d2.put("status", "APPROVED");
                            d2.put("comment", comment != null ? comment : "");
                            ApiClient.approveLeave(d2);
                            showInfo("已通过"); onSave.run();
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    });
                    d.setResult(closeBtn);
                });
                if (rejectNode != null) rejectNode.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
                    ev.consume();
                    TextInputDialog tid = new TextInputDialog();
                    tid.setTitle("驳回"); tid.setHeaderText("请输入驳回原因"); tid.setContentText("原因:");
                    tid.showAndWait().ifPresent(comment -> {
                        try {
                            Map<String,Object> d2 = new HashMap<>();
                            d2.put("id", id); d2.put("status", "REJECTED");
                            d2.put("comment", comment != null ? comment : "");
                            ApiClient.approveLeave(d2);
                            showInfo("已驳回"); onSave.run();
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    });
                    d.setResult(closeBtn);
                });
            } else {
                d.getDialogPane().getButtonTypes().add(closeBtn);
            }
            d.showAndWait();
        } catch (Exception ex) { showError("加载失败: " + ex.getMessage()); }
    }

    // -- Activity Management --
    private TextField activityTitleF;
    private void showActivityManagement() { contentArea.getChildren().clear(); sectionTitle.setText("活动管理");
        VBox p = new VBox(10); p.setPadding(new Insets(15));
        Button addBtn = new Button("发布活动"); addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        activityTitleF = new TextField(); activityTitleF.setPrefWidth(150); activityTitleF.setPromptText("标题搜索");
        Button actSearchBtn = new Button("搜索"); actSearchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        Button actResetBtn = new Button("重置搜索"); actResetBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        HBox bar = new HBox(10, activityTitleF, actSearchBtn, actResetBtn, addBtn);
        TableView<Map<String,Object>> table = new TableView<>();
        TableColumn<Map<String,Object>,String> c1 = new TableColumn<>("标题"); c1.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("title")));
        TableColumn<Map<String,Object>,String> c2 = new TableColumn<>("地点"); c2.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("location")));
        TableColumn<Map<String,Object>,String> c3 = new TableColumn<>("日期"); c3.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().get("date")!=null?d.getValue().get("date").toString():""));
        TableColumn<Map<String,Object>,String> c4 = new TableColumn<>("报名/上限"); c4.setCellValueFactory(d -> { Object rc=d.getValue().get("registered_count"); Object mp=d.getValue().get("max_participants"); int max = mp!=null ? ((Number)mp).intValue() : 0; return new javafx.beans.property.SimpleStringProperty((rc!=null?rc:"0")+"/"+(max>0?String.valueOf(max):"∞")); });
        final int[] ap = {1}, at = {1};
        Runnable load = () -> refreshActivity(table,ap,at);
        actSearchBtn.setOnAction(e -> { ap[0]=1; load.run(); });
        actResetBtn.setOnAction(e -> { activityTitleF.clear(); ap[0]=1; load.run(); });
        addBtn.setOnAction(e -> {
            Dialog<ButtonType> d = new Dialog<>(); d.setTitle("发布活动"); GridPane g = new GridPane(); g.setHgap(10);g.setVgap(8);g.setPadding(new Insets(15));
            TextField titleI=new TextField(),locI=new TextField(),cntI=new TextField(); TextArea contentI=new TextArea(); contentI.setPrefRowCount(3); DatePicker dateI=new DatePicker(); dateI.setEditable(false);
            ComboBox<String> hourCb = new ComboBox<>(); for(int i=0;i<24;i++) hourCb.getItems().add(String.format("%02d",i)); hourCb.setValue("00"); hourCb.setPrefWidth(60);
            ComboBox<String> minCb = new ComboBox<>(); for(int i=0;i<60;i++) minCb.getItems().add(String.format("%02d",i)); minCb.setValue("00"); minCb.setPrefWidth(60);
            HBox timeBox = new HBox(5, hourCb, new Label(":"), minCb);
            g.addRow(0,new Label("标题:"),titleI);g.addRow(1,new Label("内容:"),contentI);g.addRow(2,new Label("地点:"),locI);g.addRow(3,new Label("日期:"),dateI);g.addRow(4,new Label("时间:"),timeBox);g.addRow(5,new Label("人数上限:"),cntI);
            d.getDialogPane().setContent(g); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
            d.showAndWait().ifPresent(r2->{if(r2==ButtonType.OK){try{
                if(dateI.getValue()!=null && dateI.getValue().isBefore(java.time.LocalDate.now())){showWarning("活动日期不能早于今天");return;}
                String cntText=cntI.getText().trim(); int maxP;
                try { maxP = cntText.isEmpty() ? 0 : Integer.parseInt(cntText); }
                catch (NumberFormatException ex) { showWarning("人数上限必须为正整数"); return; }
                if(maxP<0){showWarning("人数上限不能为负数");return;} if(maxP==0){showWarning("人数上限不能为0");return;}
                String dateStr = dateI.getValue()!=null ? dateI.getValue().toString()+" "+hourCb.getValue()+":"+minCb.getValue()+":00" : "";
                Map<String,Object> m=new HashMap<>(); m.put("title",titleI.getText().trim());m.put("content",contentI.getText()!=null?contentI.getText().trim():"");m.put("location",locI.getText().trim());m.put("date",dateStr);m.put("maxParticipants",maxP); ApiClient.publishActivity(m);load.run();}catch(Exception ex){showError(ex.getMessage());}}});
        });
        TableColumn<Map<String,Object>,Void> act = new TableColumn<>("操作"); act.setCellFactory(col -> {
            javafx.scene.control.TableCell<Map<String,Object>,Void> cell = new javafx.scene.control.TableCell<>() {
                private final Button detail = new Button("详情"); { detail.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;"); }
                private final Button del = new Button("删除"); { del.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"); }
                private final HBox bx = new HBox(5, detail, del);
                @Override protected void updateItem(Void i, boolean e) { super.updateItem(i,e); if(e){setGraphic(null);return;}
                    Map<String,Object> r = getTableView().getItems().get(getIndex());
                    detail.setOnAction(ev -> showActivityDetailDialog(r, load));
                    del.setOnAction(ev -> { if(showConfirm("确认","确定删除?")){ApiClient.adminDeleteActivity(((Number)r.get("id")).intValue()); load.run();} });
                    setGraphic(bx);
                }
            }; return cell;
        });
        table.getColumns().addAll(c1,c2,c3,c4,act); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(25);
        table.setPrefHeight(278);
        table.setMinHeight(278);
        table.setMaxHeight(278);
        Button prevB = new Button("上一页"), nextB = new Button("下一页");
        Label pagLab = new Label();
        HBox pag = new HBox(15, prevB, pagLab, nextB); pag.setAlignment(javafx.geometry.Pos.CENTER); pag.setPadding(new Insets(10));
        Runnable refresh = () -> { refreshActivity(table,ap,at); pagLab.setText("第 "+ap[0]+"/"+at[0]+" 页"); prevB.setDisable(ap[0]<=1); nextB.setDisable(ap[0]>=at[0]); };
        prevB.setOnAction(e -> { if(ap[0]>1){ap[0]--;refresh.run();} });
        nextB.setOnAction(e -> { if(ap[0]<at[0]){ap[0]++;refresh.run();} });
        refresh.run();
        p.getChildren().addAll(bar,table,pag); contentArea.getChildren().add(p);
    }
    private void refreshActivity(TableView<Map<String,Object>> t, int[] page, int[] total) { try { String kw = activityTitleF != null ? activityTitleF.getText().trim() : ""; if (kw.isEmpty()) kw = null; PageResult<Map<String,Object>> r = ApiClient.getAdminActivities(page[0],10, kw); if(r!=null&&r.getList()!=null) t.getItems().setAll(r.getList()); total[0]=Math.max(1,(int)Math.ceil((double)r.getTotal()/10)); } catch(Exception ignored){} }

    private void showActivityDetailDialog(Map<String,Object> act, Runnable onSave) {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("活动详情"); d.setResizable(true);
        GridPane g = new GridPane(); g.setHgap(10);g.setVgap(8);g.setPadding(new Insets(15));
        TextField titleF = new TextField((String)act.get("title"));
        TextArea contentF = new TextArea((String)act.get("content")); contentF.setPrefRowCount(4);
        TextField locF = new TextField((String)act.get("location"));
        DatePicker dateF = new DatePicker(); dateF.setEditable(false);
        try{Object dt=act.get("date");if(dt!=null)dateF.setValue(java.time.LocalDate.parse(dt.toString().substring(0,10)));}catch(Exception ignored){}
        // Parse time from date string
        String timeStr = "";
        try { Object dt=act.get("date"); if(dt!=null){ String s=dt.toString(); if(s.length()>=16){ timeStr=s.substring(11,16); } } } catch(Exception ignored){}
        String[] hm = timeStr.isEmpty() ? new String[]{"00","00"} : timeStr.split(":");
        ComboBox<String> hourCb = new ComboBox<>(); for(int i=0;i<24;i++) hourCb.getItems().add(String.format("%02d",i)); hourCb.setValue(hm[0]); hourCb.setPrefWidth(60);
        ComboBox<String> minCb = new ComboBox<>(); for(int i=0;i<60;i++) minCb.getItems().add(String.format("%02d",i)); minCb.setValue(hm.length>1?hm[1]:"00"); minCb.setPrefWidth(60);
        HBox timeBox = new HBox(5, hourCb, new Label(":"), minCb);
        TextField cntF = new TextField(act.get("max_participants")!=null?String.valueOf(act.get("max_participants")):"0");
        java.util.List<Control> edits = java.util.List.of(titleF,contentF,locF,dateF,hourCb,minCb,cntF);
        edits.forEach(c->c.setDisable(true));
        g.addRow(0,new Label("标题:"),titleF);g.addRow(1,new Label("内容:"),contentF);
        g.addRow(2,new Label("地点:"),locF);g.addRow(3,new Label("日期:"),dateF);
        g.addRow(4,new Label("时间:"),timeBox);
        g.addRow(5,new Label("人数上限:"),cntF);
        d.getDialogPane().setContent(g);
        ButtonType editBtn = new ButtonType("编辑",ButtonBar.ButtonData.OTHER);
        ButtonType saveBtn = new ButtonType("保存",ButtonBar.ButtonData.APPLY);
        d.getDialogPane().getButtonTypes().addAll(editBtn,saveBtn,ButtonType.CLOSE);
        javafx.scene.Node saveNode = d.getDialogPane().lookupButton(saveBtn);
        if(saveNode!=null) saveNode.setDisable(true);
        d.getDialogPane().lookupButton(editBtn).addEventFilter(javafx.event.ActionEvent.ACTION, ev->{
            ev.consume(); edits.forEach(c->c.setDisable(false)); if(saveNode!=null) saveNode.setDisable(false);
        });
        d.showAndWait().ifPresent(r->{if(r==saveBtn){
            try{
                if(dateF.getValue()!=null && dateF.getValue().isBefore(java.time.LocalDate.now())){showWarning("活动日期不能早于今天");return;}
                String cntText=cntF.getText().trim(); int maxP;
                try { maxP = cntText.isEmpty() ? 0 : Integer.parseInt(cntText); }
                catch (NumberFormatException ex) { showWarning("人数上限必须为正整数"); return; }
                if(maxP<0){showWarning("人数上限不能为负数");return;} if(maxP==0){showWarning("人数上限不能为0");return;}
                String dateStr = dateF.getValue()!=null ? dateF.getValue().toString()+" "+hourCb.getValue()+":"+minCb.getValue()+":00" : "";
                Map<String,Object> m=new HashMap<>(); m.put("id",((Number)act.get("id")).longValue()); m.put("title",titleF.getText().trim());m.put("content",contentF.getText()!=null?contentF.getText().trim():"");m.put("location",locF.getText().trim());m.put("date",dateStr);m.put("maxParticipants",maxP); ApiClient.adminUpdateActivity(m);onSave.run();}
            catch(Exception ex){showError(ex.getMessage());}
        }});
    }

}
