package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            NavigationManager.navigateTo("homework-grading-list-view.fxml",
                    (HomeworkGradingListController controller) -> controller.setHomeworkId(hw.getId()));
        });

        Label titleLabel = new Label(hw.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label deadlineLabel = new Label("截止: " + formatDateTime(hw.getDeadline()));
        deadlineLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(titleLabel, spacer, deadlineLabel);
        return card;
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
}
