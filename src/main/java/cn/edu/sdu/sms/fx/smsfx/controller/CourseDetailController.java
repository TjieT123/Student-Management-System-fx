package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CourseDetailController extends BaseController {

    @FXML private Label courseNameLabel;
    @FXML private Label teacherLabel;
    @FXML private Label addressLabel;
    @FXML private Label detailLabel;
    @FXML private Button enrollCancelBtn;
    @FXML private Button publishHomeworkBtn;
    @FXML private Button viewStudentsBtn;
    @FXML private Button scoreStatsBtn;
    @FXML private Button moreHomeworkBtn;
    @FXML private VBox homeworkContainer;

    private Integer courseId;
    private Course currentCourse;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();
    }

    public void setCourseId(Integer courseId) {
        if (courseId == null) { showError("课程ID无效"); return; }
        this.courseId = courseId;
        loadCourseDetail();
        loadHomeworkList();
    }

    private void loadCourseDetail() {
        try {
            // 详情接口获取 courseName/detail/address
            currentCourse = ApiClient.getCourseDetail(courseId);
            courseNameLabel.setText(currentCourse.getCourseName());
            addressLabel.setText(currentCourse.getAddress() != null ? currentCourse.getAddress() : "未知");
            detailLabel.setText(currentCourse.getDetail() != null ? currentCourse.getDetail() : "暂无简介");

            // teacherName 需通过列表接口获取（详情接口不含此字段）
            String teacherName = "未知";
            try {
                PageResult<Course> listResult = ApiClient.getCourseList(1, 1, courseId, null, null);
                if (listResult != null && listResult.getList() != null && !listResult.getList().isEmpty()) {
                    Course listCourse = listResult.getList().get(0);
                    teacherName = listCourse.getTeacherName() != null ? listCourse.getTeacherName() : "未知";
                }
            } catch (Exception ignored) {}
            teacherLabel.setText(teacherName);

            String role = SessionManager.getRole();
            if ("STUDENT".equals(role)) {
                enrollCancelBtn.setVisible(true);
                enrollCancelBtn.setManaged(true);
                enrollCancelBtn.setOnAction(e -> handleCancelEnroll());
                publishHomeworkBtn.setVisible(false);
                publishHomeworkBtn.setManaged(false);
                scoreStatsBtn.setVisible(true);
                scoreStatsBtn.setManaged(true);
                scoreStatsBtn.setOnAction(e -> handleScoreStatistics());
            } else if ("TEACHER".equals(role)) {
                enrollCancelBtn.setVisible(false);
                enrollCancelBtn.setManaged(false);
                publishHomeworkBtn.setVisible(true);
                publishHomeworkBtn.setManaged(true);
                publishHomeworkBtn.setOnAction(e -> {
                    NavigationManager.navigateTo("homework-publish-view.fxml",
                            (HomeworkPublishController controller) -> controller.setPreselectedCourseId(courseId));
                });
                viewStudentsBtn.setVisible(true);
                viewStudentsBtn.setManaged(true);
                viewStudentsBtn.setOnAction(e -> handleViewStudents());
            }
            // Materials section for all roles
            loadMaterials();
        } catch (Exception e) {
            showError("加载课程详情失败: " + e.getMessage());
        }
    }

    private void loadHomeworkList() {
        homeworkContainer.getChildren().clear();
        String role = SessionManager.getRole();

        try {
            if ("STUDENT".equals(role)) {
                PageResult<StudentHomeworkItem> result = ApiClient.getStudentHomeworkList(courseId, 1, 5);
                if (result != null && result.getList() != null) {
                    for (StudentHomeworkItem hw : result.getList()) {
                        homeworkContainer.getChildren().add(createStudentHomeworkCard(hw));
                    }
                }
            } else if ("TEACHER".equals(role)) {
                // 教师获取全局作业列表后按 courseId 过滤
                PageResult<Homework> allHomeworks = ApiClient.getHomeworkList(1, 100);
                if (allHomeworks != null && allHomeworks.getList() != null) {
                    List<Homework> filtered = allHomeworks.getList().stream()
                            .filter(h -> courseId.equals(h.getCourseId()))
                            .limit(5)
                            .collect(Collectors.toList());
                    for (Homework hw : filtered) {
                        homeworkContainer.getChildren().add(createTeacherHomeworkCard(hw));
                    }
                }
            }

            // "查看更多"按钮
            moreHomeworkBtn.setOnAction(e -> {
                NavigationManager.navigateTo("list-page-view.fxml", (ListPageController controller) -> {
                    controller.setPageType(ListPageController.PageType.HOMEWORK, courseId, role);
                });
            });
        } catch (Exception e) {
            Label err = new Label("加载作业失败: " + e.getMessage());
            err.setStyle("-fx-text-fill: red;");
            homeworkContainer.getChildren().add(err);
        }
    }

    private HBox createStudentHomeworkCard(StudentHomeworkItem hw) {
        HBox card = new HBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            NavigationManager.navigateTo("homework-submit-view.fxml",
                    (HomeworkSubmitController controller) -> controller.setHomeworkId(hw.getId(), courseId));
        });

        Label titleLabel = new Label(hw.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label deadlineLabel = new Label("截止: " + formatDateTime(hw.getDeadline()));
        deadlineLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        Label statusLabel = createStatusLabel(hw.getStatus());

        card.getChildren().addAll(titleLabel, spacer, deadlineLabel);
        // 已批改时显示分数（60分及以上绿色，60分以下红色）
        if ("GRADED".equals(hw.getStatus()) && hw.getScore() != null) {
            Label scoreLabel = new Label(hw.getScore() + "分");
            String color = hw.getScore() >= 60 ? "#27ae60" : "#e74c3c";
            scoreLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 13;");
            card.getChildren().add(scoreLabel);
        }
        card.getChildren().add(statusLabel);
        return card;
    }

    private HBox createTeacherHomeworkCard(Homework hw) {
        HBox card = new HBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12;");

        Label titleLabel = new Label(hw.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label deadlineLabel = new Label("截止: " + formatDateTime(hw.getDeadline()));
        deadlineLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        // 编辑按钮
        Button editBtn = new Button("编辑");
        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
        editBtn.setOnAction(e -> showEditHomeworkDialog(hw));

        // 删除按钮
        Button deleteBtn = new Button("删除");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
        deleteBtn.setOnAction(e -> {
            if (showConfirm("删除作业", "确定要删除作业 \"" + hw.getTitle() + "\" 吗？\n将同时删除该作业下的所有学生提交记录。")) {
                try {
                    ApiClient.deleteHomework(hw.getId());
                    showInfo("删除成功");
                    loadHomeworkList();
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        });

        // 点击标题区域跳转到批改列表
        HBox clickArea = new HBox(15, titleLabel, spacer, deadlineLabel);
        clickArea.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(clickArea, javafx.scene.layout.Priority.ALWAYS);
        clickArea.setStyle("-fx-cursor: hand;");
        clickArea.setOnMouseClicked(e -> {
            NavigationManager.navigateTo("homework-grading-list-view.fxml",
                    (HomeworkGradingListController controller) -> controller.setHomeworkId(hw.getId()));
        });

        card.getChildren().addAll(clickArea, editBtn, deleteBtn);
        return card;
    }

    private void showEditHomeworkDialog(Homework hw) {
        // 通过学生接口获取作业完整内容
        String fullContent = "";
        try {
            StudentHomeworkItem detail = ApiClient.getHomeworkContent(hw.getId());
            if (detail != null && detail.getContent() != null) {
                fullContent = detail.getContent();
            }
        } catch (Exception ignored) {}

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("编辑作业");
        dialog.setResizable(true);

        VBox box = new VBox(10); box.setPadding(new Insets(15));

        TextField titleField = new TextField(hw.getTitle() != null ? hw.getTitle() : "");
        TextArea contentArea = new TextArea(fullContent);
        contentArea.setPrefRowCount(5);

        DatePicker datePicker = new DatePicker(); datePicker.setEditable(false);
        ComboBox<String> hourField = new ComboBox<>();
        hourField.setEditable(false); hourField.setPrefWidth(65); hourField.setVisibleRowCount(12);
        for (int h = 0; h < 24; h++) hourField.getItems().add(String.format("%02d", h));
        ComboBox<String> minuteField = new ComboBox<>();
        minuteField.setEditable(false); minuteField.setPrefWidth(65); minuteField.setVisibleRowCount(12);
        minuteField.getItems().addAll("00", "30", "59");

        if (hw.getDeadline() != null && !hw.getDeadline().isEmpty()) {
            String dl = hw.getDeadline().replace("T", " ");
            try {
                String[] parts = dl.split(" ");
                if (parts.length >= 1) datePicker.setValue(java.time.LocalDate.parse(parts[0]));
                if (parts.length >= 2) {
                    String[] timeParts = parts[1].split(":");
                    if (timeParts.length >= 1) hourField.setValue(timeParts[0]);
                    if (timeParts.length >= 2) minuteField.setValue(timeParts[1]);
                }
            } catch (Exception ignored) {}
        }
        if (hourField.getValue() == null) hourField.setValue("23");
        if (minuteField.getValue() == null) minuteField.setValue("59");
        HBox deadlineBox = new HBox(5, datePicker, hourField, new Label("时"), minuteField, new Label("分"));
        deadlineBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Attachment management
        VBox attBox = new VBox(5);
        java.util.List<cn.edu.sdu.sms.fx.smsfx.models.AttachmentItem> editAtts = new java.util.ArrayList<>();
        try {
            java.util.List<cn.edu.sdu.sms.fx.smsfx.models.AttachmentItem> existing = ApiClient.getHomeworkAttachments(hw.getId());
            if (existing != null) editAtts.addAll(existing);
        } catch (Exception ignored) {}
        final Runnable[] refreshAtts = {null};
        refreshAtts[0] = () -> {
            attBox.getChildren().clear();
            for (int i = 0; i < editAtts.size(); i++) {
                cn.edu.sdu.sms.fx.smsfx.models.AttachmentItem ai = editAtts.get(i);
                HBox row = new HBox(10); row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                row.getChildren().add(new Label("📎 " + ai.getFileName() + " (" + ai.getSizeDisplay() + ")"));
                final int idx = i;
                Button delBtn = new Button("删除");
                delBtn.setOnAction(e -> { editAtts.remove(idx); refreshAtts[0].run(); });
                row.getChildren().add(delBtn);
                attBox.getChildren().add(row);
            }
        };
        refreshAtts[0].run();
        HBox addRow = new HBox(10);
        Button addBtn = new Button("📎 添加附件");
        addBtn.setOnAction(ev -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser(); fc.setTitle("选择附件");
            java.io.File file = fc.showOpenDialog(null);
            if (file == null) return;
            if (file.length() > 10 * 1024 * 1024) { showWarning("文件不能超过10MB"); return; }
            try {
                String b64 = cn.edu.sdu.sms.fx.smsfx.util.Base64Util.encodeFile(file);
                cn.edu.sdu.sms.fx.smsfx.models.AttachmentItem ai = new cn.edu.sdu.sms.fx.smsfx.models.AttachmentItem();
                ai.setFileName(file.getName());
                ai.setFileType(cn.edu.sdu.sms.fx.smsfx.util.Base64Util.guessMimeType(file.getName()));
                ai.setSize(file.length()); ai.setBase64(b64);
                ApiClient.uploadHomeworkAttachment(hw.getId(), ai.getFileName(), ai.getFileType(), ai.getSize(), b64);
                editAtts.add(ai); refreshAtts[0].run();
            } catch (Exception ex) { showError("上传失败: " + ex.getMessage()); }
        });
        addRow.getChildren().addAll(addBtn, new Label("(每个文件≤10MB)"));

        box.getChildren().addAll(
            new HBox(10, new Label("标题:"), titleField),
            new HBox(10, new Label("内容:"), contentArea),
            new HBox(10, new Label("截止时间:"), deadlineBox),
            new Label("附件:"), addRow, attBox
        );
        dialog.getDialogPane().setContent(new ScrollPane(box));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    String title = titleField.getText() != null ? titleField.getText().trim() : "";
                    String content = contentArea.getText() != null ? contentArea.getText().trim() : "";
                    if (title.isEmpty()) { showWarning("作业标题不能为空"); return; }
                    if (title.length() > 200) { showWarning("标题不能超过200字符"); return; }
                    if (content.length() > 10000) { showWarning("内容不能超过10000字符"); return; }
                    if (datePicker.getValue() == null) { showWarning("请选择截止日期"); return; }
                    String hour = hourField.getValue() != null ? hourField.getValue() : "23";
                    String minute = minuteField.getValue() != null ? minuteField.getValue() : "59";
                    String deadline = datePicker.getValue().toString() + " " + hour + ":" + minute + ":00";
                    try {
                        java.time.format.DateTimeFormatter fmt =
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        java.time.LocalDateTime dt = java.time.LocalDateTime.parse(deadline, fmt);
                        if (dt.isBefore(java.time.LocalDateTime.now())) {
                            showWarning("截止时间不能早于当前时间"); return;
                        }
                    } catch (java.time.format.DateTimeParseException ex) {
                        showWarning("日期或时间格式错误"); return;
                    }
                    ApiClient.updateHomework(hw.getId(), title, content, deadline);
                    showInfo("修改成功");
                    loadHomeworkList();
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    private Label createStatusLabel(String status) {
        Label label = new Label();
        if (status == null) status = "UNSUBMIT";
        switch (status) {
            case "UNSUBMIT":
                label.setText("未提交");
                label.setStyle("-fx-text-fill: gray; -fx-font-size: 12;");
                break;
            case "SUBMITTED":
                label.setText("已提交");
                label.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12;");
                break;
            case "LATE":
                label.setText("迟交");
                label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                break;
            case "GRADED":
                label.setText("已批改");
                label.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
                break;
            default:
                label.setText(status);
        }
        return label;
    }

    private void handleViewStudents() {
        try {
            List<Student> students = ApiClient.getEnrolledStudents(courseId);
            if (students == null || students.isEmpty()) {
                showInfo("暂无学生选课");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("选课学生列表");
            dialog.setHeaderText(currentCourse.getCourseName() + " — 选课学生（共 " + students.size() + " 人）");
            dialog.setResizable(true);

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

            table.getColumns().addAll(sidCol, nameCol, majorCol, genderCol, classCol);
            table.setItems(FXCollections.observableArrayList(students));
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefHeight(400);
            table.setPrefWidth(600);

            VBox content = new VBox(10, table);
            content.setPadding(new Insets(10));
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();
        } catch (Exception e) {
            showError("加载选课学生失败: " + e.getMessage());
        }
    }

    private void handleScoreStatistics() {
        try {
            // 获取所有作业（含分数）
            PageResult<StudentHomeworkItem> result = ApiClient.getStudentHomeworkList(courseId, 1, 100);
            if (result == null || result.getList() == null || result.getList().isEmpty()) {
                showInfo("暂无作业数据");
                return;
            }

            // 筛选已批改的作业，按 deadline 排序（时间线）
            List<StudentHomeworkItem> graded = new ArrayList<>();
            for (StudentHomeworkItem hw : result.getList()) {
                if ("GRADED".equals(hw.getStatus()) && hw.getScore() != null) {
                    graded.add(hw);
                }
            }

            if (graded.isEmpty()) {
                showInfo("暂无已批改的作业");
                return;
            }

            // 超过 10 次只保留最近 10 次
            if (graded.size() > 10) {
                graded = graded.subList(graded.size() - 10, graded.size());
            }

            // 创建折线图
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel("作业序号");
            xAxis.setTickUnit(1);
            xAxis.setMinorTickVisible(false);
            xAxis.setForceZeroInRange(false);
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(0);
            xAxis.setUpperBound(10);
            NumberAxis yAxis = new NumberAxis(0, 100, 10);
            yAxis.setLabel("分数");

            LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle("作业成绩趋势 — " + currentCourse.getCourseName() + "（只统计最近10次）");
            chart.setPrefSize(600, 350);
            chart.setCreateSymbols(true);

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("成绩");
            for (int i = 0; i < graded.size(); i++) {
                series.getData().add(new XYChart.Data<>(i + 1, graded.get(i).getScore()));
            }
            chart.getData().add(series);

            // 及格线
            XYChart.Series<Number, Number> passLine = new XYChart.Series<>();
            passLine.setName("及格线(60)");
            passLine.getData().add(new XYChart.Data<>(0, 60));
            passLine.getData().add(new XYChart.Data<>(10, 60));
            chart.getData().add(passLine);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("作业成绩统计");
            dialog.setHeaderText(currentCourse.getCourseName() + " — 已批改 " + graded.size() + " 次作业");
            dialog.setResizable(true);

            VBox content = new VBox(10, chart);
            content.setPadding(new Insets(10));
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (Exception e) {
            showError("加载成绩统计失败: " + e.getMessage());
        }
    }

    private void handleCancelEnroll() {
        if (showConfirm("取消选课", "确定要取消该课程吗？取消后将无法查看课程作业和提交记录。")) {
            try {
                ApiClient.cancelEnroll(courseId);
                showInfo("已取消选课");
                goHome();
            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    /** 加载课程资料 */
    private VBox materialsBox;
    private void loadMaterials() {
        if (materialsBox == null) {
            materialsBox = new VBox(5);
            materialsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-padding: 10;");
            if (homeworkContainer.getParent() instanceof VBox parent) {
                parent.getChildren().add(parent.getChildren().indexOf(moreHomeworkBtn.getParent()), materialsBox);
            }
        }
        materialsBox.getChildren().clear();
        materialsBox.getChildren().add(new Label("📎 课程资料:"));
        String role = SessionManager.getRole();
        try {
            java.util.List<AttachmentItem> items = ApiClient.getCourseMaterials(courseId);
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    AttachmentItem att = items.get(i);
                    final int idx = i;
                    HBox row = new HBox(8); row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    row.getChildren().addAll(new Label(getIcon(att.getFileName())), new Label(att.getFileName()), new Label(att.getSizeDisplay()));
                    Button dl = new Button("下载"); dl.setStyle("-fx-font-size: 10; -fx-background-color: #3498db; -fx-text-fill: white;");
                    dl.setOnAction(e -> downloadMaterial(att));
                    row.getChildren().add(dl);
                    if ("TEACHER".equals(role)) {
                        Button del = new Button("删除"); del.setStyle("-fx-font-size: 10; -fx-background-color: #e74c3c; -fx-text-fill: white;");
                        del.setOnAction(e -> { try { ApiClient.deleteCourseMaterial(courseId, idx); loadMaterials(); } catch (Exception ex) { showError(ex.getMessage()); } });
                        row.getChildren().add(del);
                    }
                    materialsBox.getChildren().add(row);
                }
            }
            if ("TEACHER".equals(role)) {
                Button upBtn = new Button("+ 上传资料"); upBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11;");
                upBtn.setOnAction(e -> {
                    javafx.stage.FileChooser fc = new javafx.stage.FileChooser(); fc.setTitle("选择文件");
                    java.io.File f = fc.showOpenDialog(materialsBox.getScene().getWindow());
                    if (f != null) {
                        try {
                            String b64 = cn.edu.sdu.sms.fx.smsfx.util.Base64Util.encodeFile(f, 5*1024*1024);
                            ApiClient.addCourseMaterial(courseId, f.getName(), cn.edu.sdu.sms.fx.smsfx.util.Base64Util.guessMimeType(f.getName()), f.length(), b64);
                            loadMaterials();
                        } catch (Exception ex) { showError("上传失败: " + ex.getMessage()); }
                    }
                });
                materialsBox.getChildren().add(upBtn);
            }
        } catch (Exception ignored) {}
    }

    private void downloadMaterial(AttachmentItem att) {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser(); fc.setTitle("保存"); fc.setInitialFileName(att.getFileName());
        java.io.File f = fc.showSaveDialog(materialsBox.getScene().getWindow());
        if (f != null) { try { cn.edu.sdu.sms.fx.smsfx.util.Base64Util.decodeToFile(att.getBase64(), f); showInfo("保存成功"); } catch (Exception e) { showError("保存失败"); } }
    }

    private String getIcon(String n) { if(n==null)return"📎"; String l=n.toLowerCase(); if(l.endsWith(".pdf"))return"📕"; if(l.endsWith(".doc")||l.endsWith(".docx"))return"📘"; if(l.endsWith(".xls")||l.endsWith(".xlsx"))return"📊"; if(l.endsWith(".zip")||l.endsWith(".rar"))return"📦"; return"📎"; }

}
