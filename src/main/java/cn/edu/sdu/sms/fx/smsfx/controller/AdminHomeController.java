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

    // Sidebar buttons
    @FXML private VBox userMgmtSubmenu;
    @FXML private Button teacherUserListBtn;
    @FXML private Button studentUserListBtn;
    @FXML private Button adminUserListBtn;
    @FXML private Button teacherMgmtBtn;
    @FXML private Button studentMgmtBtn;
    @FXML private Button courseMgmtBtn;
    @FXML private Button announcementMgmtBtn;

    @Override
    @FXML
    public void initialize() {
        super.initialize();

        // 用户管理菜单展开
        Button userMgmtToggle = new Button("▸ 用户管理");
        userMgmtToggle.setMaxWidth(200);
        userMgmtToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-font-size: 14;");
        userMgmtToggle.setOnAction(e -> {
            boolean visible = !userMgmtSubmenu.isVisible();
            userMgmtSubmenu.setVisible(visible);
            userMgmtSubmenu.setManaged(visible);
            userMgmtToggle.setText(visible ? "▾ 用户管理" : "▸ 用户管理");
        });
        sidebar.getChildren().add(2, userMgmtToggle);

        // 侧边栏按钮事件
        teacherUserListBtn.setOnAction(e -> showUserManagement("TEACHER"));
        studentUserListBtn.setOnAction(e -> showUserManagement("STUDENT"));
        adminUserListBtn.setOnAction(e -> showUserManagement("ADMIN"));
        teacherMgmtBtn.setOnAction(e -> showTeacherManagement());
        studentMgmtBtn.setOnAction(e -> showStudentManagement());
        courseMgmtBtn.setOnAction(e -> showCourseManagement());
        announcementMgmtBtn.setOnAction(e -> showAnnouncementManagement());
    }

    // ==================== 工具方法 ====================

    private HBox createPagination(int page, int total, Runnable prevAction, Runnable nextAction) {
        HBox box = new HBox(15);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPadding(new Insets(10));
        Button prevBtn = new Button("上一页");
        prevBtn.setOnAction(e -> prevAction.run());
        prevBtn.setDisable(page <= 1);
        Button nextBtn = new Button("下一页");
        nextBtn.setOnAction(e -> nextAction.run());
        nextBtn.setDisable(page >= total);
        box.getChildren().addAll(prevBtn, new Label("第 " + page + "/" + total + " 页"), nextBtn);
        return box;
    }

    // ==================== 1. 用户管理（教师/学生/管理员列表） ====================

    private void showUserManagement(String role) {
        contentArea.getChildren().clear();
        String title = role.equals("TEACHER") ? "教师用户列表" : role.equals("STUDENT") ? "学生用户列表" : "管理员用户列表";
        sectionTitle.setText(title);

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        // 添加按钮
        Button addBtn = new Button("添加" + (role.equals("TEACHER") ? "教师" : role.equals("STUDENT") ? "学生" : "管理员") + "用户");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addBtn.setOnAction(e -> showAddEditUserDialog(null, role));

        // 表格
        TableView<AdminUserVO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AdminUserVO, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<AdminUserVO, String> usernameCol = new TableColumn<>("用户名");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<AdminUserVO, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<AdminUserVO, String> phoneCol = new TableColumn<>("手机号");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<AdminUserVO, String> schIdCol = new TableColumn<>("工号/学号");
        schIdCol.setCellValueFactory(new PropertyValueFactory<>("schId"));

        table.getColumns().addAll(idCol, usernameCol, nameCol, phoneCol, schIdCol);

        if ("STUDENT".equals(role)) {
            TableColumn<AdminUserVO, String> majorCol = new TableColumn<>("专业");
            majorCol.setCellValueFactory(new PropertyValueFactory<>("major"));
            TableColumn<AdminUserVO, String> genderCol = new TableColumn<>("性别");
            genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
            TableColumn<AdminUserVO, Integer> classCol = new TableColumn<>("班级");
            classCol.setCellValueFactory(new PropertyValueFactory<>("sClass"));
            table.getColumns().addAll(majorCol, genderCol, classCol);
        }

        // 操作列
        TableColumn<AdminUserVO, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            private final HBox box = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                AdminUserVO user = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showAddEditUserDialog(user, role));
                deleteBtn.setOnAction(e -> {
                    if (showConfirm("删除确认", "确定要删除用户 " + user.getUsername() + " 吗？")) {
                        try {
                            ApiClient.deleteUser(user.getId());
                            showInfo("删除成功");
                            showUserManagement(role);
                        } catch (Exception ex) {
                            showError(ex.getMessage());
                        }
                    }
                });
                setGraphic(box);
            }
        });
        table.getColumns().add(actionCol);

        // 分页加载
        var pageRef = new int[]{1};
        var totalRef = new int[]{1};
        Runnable loadData = () -> {
            try {
                PageResult<AdminUserVO> result = ApiClient.getAdminUserList(role, pageRef[0], 10);
                if (result != null) {
                    table.setItems(FXCollections.observableArrayList(result.getList()));
                    totalRef[0] = Math.max(1, (int) Math.ceil((double) result.getTotal() / 10));
                }
            } catch (Exception e) { showError(e.getMessage()); }
        };
        loadData.run();

        HBox pagination = createPagination(pageRef[0], totalRef[0],
                () -> { if (pageRef[0] > 1) { pageRef[0]--; loadData.run(); } },
                () -> { if (pageRef[0] < totalRef[0]) { pageRef[0]++; loadData.run(); } });

        panel.getChildren().addAll(addBtn, table, pagination);
        contentArea.getChildren().add(panel);
    }

    private void showAddEditUserDialog(AdminUserVO existingUser, String role) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingUser == null ? "添加用户" : "编辑用户");
        dialog.setHeaderText(null);

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
        grid.add(new Label("工号/学号:"), 0, row); grid.add(schIdField, 1, row++);

        if (existingUser != null) {
            usernameField.setText(existingUser.getUsername());
            usernameField.setDisable(true);
            nameField.setText(existingUser.getName());
            phoneField.setText(existingUser.getPhone() != null ? existingUser.getPhone() : "");
            schIdField.setText(existingUser.getSchId() != null ? existingUser.getSchId() : "");
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Map<String, Object> data = new HashMap<>();
            if (existingUser != null) {
                data.put("id", existingUser.getId());
            } else {
                data.put("username", usernameField.getText().trim());
                data.put("password", passwordField.getText());
            }
            data.put("name", nameField.getText().trim());
            data.put("phone", phoneField.getText().trim());
            data.put("sch_id", schIdField.getText().trim());
            data.put("role", role);

            try {
                if (existingUser != null) {
                    ApiClient.updateUser(data);
                } else {
                    ApiClient.addUser(data);
                }
                showInfo(existingUser != null ? "修改成功" : "添加成功");
                showUserManagement(role);
            } catch (Exception e) {
                showError(e.getMessage());
            }
            }
        });
    }

    // ==================== 2. 教师管理 ====================

    private void showTeacherManagement() {
        contentArea.getChildren().clear();
        sectionTitle.setText("教师管理");

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Button addBtn = new Button("添加教师");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        TableView<Teacher> table = new TableView<>();
        TableColumn<Teacher, String> idCol = new TableColumn<>("工号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("schId"));
        TableColumn<Teacher, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Teacher, Void> actionCol = new TableColumn<>("操作");
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
                Teacher t = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showTeacherEditDialog(t));
                deleteBtn.setOnAction(e -> {
                    if (showConfirm("删除确认", "确定要删除教师 " + t.getName() + " 吗？将同时删除关联的用户记录。")) {
                        try {
                            ApiClient.deleteTeacher(t.getSchId());
                            showInfo("删除成功");
                            showTeacherManagement();
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    }
                });
                setGraphic(box);
            }
        });
        table.getColumns().addAll(idCol, nameCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        addBtn.setOnAction(e -> showTeacherEditDialog(null));

        Runnable loadData = () -> {
            try {
                List<Teacher> teachers = ApiClient.getAllTeachers();
                table.setItems(FXCollections.observableArrayList(teachers));
            } catch (Exception e) { showError(e.getMessage()); }
        };
        loadData.run();

        panel.getChildren().addAll(addBtn, table);
        contentArea.getChildren().add(panel);
    }

    private void showTeacherEditDialog(Teacher existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "添加教师" : "编辑教师");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField schIdField = new TextField();
        TextField nameField = new TextField();

        grid.add(new Label("工号:"), 0, 0); grid.add(schIdField, 1, 0);
        grid.add(new Label("姓名:"), 0, 1); grid.add(nameField, 1, 1);

        if (existing != null) {
            schIdField.setText(existing.getSchId());
            schIdField.setDisable(true);
            nameField.setText(existing.getName());
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    if (existing != null) {
                        ApiClient.updateTeacher(schIdField.getText(), nameField.getText());
                    } else {
                        ApiClient.addTeacher(schIdField.getText(), nameField.getText());
                    }
                    showInfo(existing != null ? "修改成功" : "添加成功");
                    showTeacherManagement();
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    // ==================== 3. 学生管理 ====================

    private void showStudentManagement() {
        contentArea.getChildren().clear();
        sectionTitle.setText("学生管理");

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Button addBtn = new Button("添加学生");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        Button addStudentUserBtn = new Button("添加学生用户（原子操作）");
        addStudentUserBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");

        TableView<Student> table = new TableView<>();
        TableColumn<Student, String> sidCol = new TableColumn<>("学号");
        sidCol.setCellValueFactory(new PropertyValueFactory<>("sid"));
        TableColumn<Student, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Student, String> majorCol = new TableColumn<>("专业");
        majorCol.setCellValueFactory(new PropertyValueFactory<>("major"));
        TableColumn<Student, String> genderCol = new TableColumn<>("性别");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        TableColumn<Student, Integer> classCol = new TableColumn<>("班级");
        classCol.setCellValueFactory(new PropertyValueFactory<>("sClass"));

        TableColumn<Student, Void> actionCol = new TableColumn<>("操作");
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
                Student s = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showStudentEditDialog(s));
                deleteBtn.setOnAction(e -> {
                    if (showConfirm("删除确认", "确定要删除学生 " + s.getName() + " 吗？将同时删除关联的用户记录。")) {
                        try {
                            ApiClient.deleteStudent(s.getSid());
                            showInfo("删除成功");
                            showStudentManagement();
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    }
                });
                setGraphic(box);
            }
        });
        table.getColumns().addAll(sidCol, nameCol, majorCol, genderCol, classCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        addBtn.setOnAction(e -> showStudentEditDialog(null));
        addStudentUserBtn.setOnAction(e -> showAddStudentUserDialog());

        var pageRef = new int[]{1};
        var totalRef = new int[]{1};
        Runnable loadData = () -> {
            try {
                PageResult<Student> result = ApiClient.getAllStudents(pageRef[0], 10);
                if (result != null) {
                    table.setItems(FXCollections.observableArrayList(result.getList()));
                    totalRef[0] = Math.max(1, (int) Math.ceil((double) result.getTotal() / 10));
                }
            } catch (Exception e) { showError(e.getMessage()); }
        };
        loadData.run();

        HBox pagination = createPagination(pageRef[0], totalRef[0],
                () -> { if (pageRef[0] > 1) { pageRef[0]--; loadData.run(); } },
                () -> { if (pageRef[0] < totalRef[0]) { pageRef[0]++; loadData.run(); } });

        panel.getChildren().addAll(new HBox(10, addBtn, addStudentUserBtn), table, pagination);
        contentArea.getChildren().add(panel);
    }

    private void showStudentEditDialog(Student existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "添加学生" : "编辑学生");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField sidField = new TextField();
        TextField nameField = new TextField();
        TextField majorField = new TextField();
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("男", "女");
        TextField classField = new TextField();

        grid.add(new Label("学号:"), 0, 0); grid.add(sidField, 1, 0);
        grid.add(new Label("姓名:"), 0, 1); grid.add(nameField, 1, 1);
        grid.add(new Label("专业:"), 0, 2); grid.add(majorField, 1, 2);
        grid.add(new Label("性别:"), 0, 3); grid.add(genderCombo, 1, 3);
        grid.add(new Label("班级:"), 0, 4); grid.add(classField, 1, 4);

        if (existing != null) {
            sidField.setText(existing.getSid());
            sidField.setDisable(true);
            nameField.setText(existing.getName());
            majorField.setText(existing.getMajor());
            genderCombo.setValue(existing.getGender());
            classField.setText(existing.getSClass() != null ? String.valueOf(existing.getSClass()) : "");
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Map<String, Object> data = new HashMap<>();
                data.put("sid", sidField.getText());
                data.put("name", nameField.getText());
                data.put("major", majorField.getText());
                data.put("gender", genderCombo.getValue());
                try { data.put("s_class", Integer.parseInt(classField.getText())); }
                catch (NumberFormatException e) { showWarning("班级请输入数字"); return; }

                try {
                    if (existing != null) {
                        ApiClient.updateStudent(data);
                    } else {
                        ApiClient.addStudent(data);
                    }
                    showInfo(existing != null ? "修改成功" : "添加成功");
                    showStudentManagement();
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    private void showAddStudentUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("添加学生用户（原子操作）");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField sidField = new TextField();
        TextField majorField = new TextField();
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("男", "女");
        genderCombo.setValue("男");
        TextField classField = new TextField();

        int row = 0;
        grid.add(new Label("用户名:"), 0, row); grid.add(usernameField, 1, row++);
        grid.add(new Label("密码:"), 0, row); grid.add(passwordField, 1, row++);
        grid.add(new Label("姓名:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("手机号:"), 0, row); grid.add(phoneField, 1, row++);
        grid.add(new Label("学号:"), 0, row); grid.add(sidField, 1, row++);
        grid.add(new Label("专业:"), 0, row); grid.add(majorField, 1, row++);
        grid.add(new Label("性别:"), 0, row); grid.add(genderCombo, 1, row++);
        grid.add(new Label("班级:"), 0, row); grid.add(classField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Map<String, Object> data = new HashMap<>();
                data.put("username", usernameField.getText());
                data.put("password", passwordField.getText());
                data.put("name", nameField.getText());
                data.put("phone", phoneField.getText());
                data.put("sid", sidField.getText());
                data.put("major", majorField.getText());
                data.put("gender", genderCombo.getValue());
                try { data.put("s_class", Integer.parseInt(classField.getText())); }
                catch (NumberFormatException e) { showWarning("班级请输入数字"); return; }

                try {
                    ApiClient.addStudentUser(data);
                    showInfo("添加成功");
                    showStudentManagement();
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

        // 搜索栏
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

        // 教师筛选
        ComboBox<Teacher> teacherFilter = new ComboBox<>();
        teacherFilter.setPromptText("按教师筛选");
        try {
            List<Teacher> teachers = ApiClient.getAllTeachers();
            teacherFilter.getItems().addAll(teachers);
        } catch (Exception ignored) {}

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
                Integer searchId = idField.getText().trim().isEmpty() ? null :
                        Integer.parseInt(idField.getText().trim());
                String searchName = nameField.getText().trim().isEmpty() ? null :
                        nameField.getText().trim();
                String tid = teacherFilter.getValue() != null ? teacherFilter.getValue().getSchId() : null;

                PageResult<Course> result = ApiClient.getCourseList(pageRef[0], 10, searchId, searchName, tid);
                if (result != null) {
                    table.setItems(FXCollections.observableArrayList(result.getList()));
                    totalRef[0] = Math.max(1, (int) Math.ceil((double) result.getTotal() / 10));
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
        try {
            teacherCombo.getItems().addAll(ApiClient.getAllTeachers());
        } catch (Exception ignored) {}

        if (existing != null) {
            courseIdField.setText(String.valueOf(existing.getId()));
            courseIdField.setDisable(true);
            nameField.setText(existing.getCourseName());
            detailField.setText(existing.getDetail());
            addressField.setText(existing.getAddress());
            if (existing.getTeacherId() != null) {
                for (Teacher t : teacherCombo.getItems()) {
                    if (existing.getTeacherId().equals(t.getSchId())) {
                        teacherCombo.setValue(t); break;
                    }
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
                Map<String, Object> data = new HashMap<>();
                if (existing != null) {
                    data.put("id", existing.getId());
                } else {
                    data.put("courseId", courseIdField.getText().trim());
                }
                data.put("courseName", nameField.getText().trim());
                data.put("detail", detailField.getText().trim());
                data.put("address", addressField.getText().trim());
                if (teacherCombo.getValue() != null) {
                    data.put("teacherId", teacherCombo.getValue().getSchId());
                }

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
                detailBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Announcement a = getTableView().getItems().get(getIndex());
                detailBtn.setOnAction(e -> showAnnouncementDetail(a.getId()));
                deleteBtn.setOnAction(e -> {
                    if (showConfirm("删除确认", "确定要删除公告 \"" + a.getTitle() + "\" 吗？")) {
                        try {
                            ApiClient.deleteAnnouncement(a.getId());
                            showInfo("删除成功");
                            showAnnouncementManagement();
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    }
                });
                setGraphic(box);
            }
        });
        table.getColumns().addAll(idCol, titleCol, publisherCol, timeCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        publishBtn.setOnAction(e -> showPublishAnnouncementDialog());

        var pageRef = new int[]{1};
        var totalRef = new int[]{1};
        Runnable loadData = () -> {
            try {
                PageResult<Announcement> result = ApiClient.getAnnouncementList(pageRef[0], 10);
                if (result != null) {
                    table.setItems(FXCollections.observableArrayList(result.getList()));
                    totalRef[0] = Math.max(1, (int) Math.ceil((double) result.getTotal() / 10));
                }
            } catch (Exception e) { showError(e.getMessage()); }
        };
        loadData.run();

        HBox pagination = createPagination(pageRef[0], totalRef[0],
                () -> { if (pageRef[0] > 1) { pageRef[0]--; loadData.run(); } },
                () -> { if (pageRef[0] < totalRef[0]) { pageRef[0]++; loadData.run(); } });

        panel.getChildren().addAll(publishBtn, table, pagination);
        contentArea.getChildren().add(panel);
    }

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
                try {
                    ApiClient.publishAnnouncement(titleField.getText(), contentField.getText(),
                            publisherNameField.getText());
                    showInfo("发布成功");
                    showAnnouncementManagement();
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
}
