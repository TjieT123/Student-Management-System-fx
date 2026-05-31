package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CourseSelectionController extends BaseController {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private Button searchBtn;
    @FXML private Button resetBtn;
    @FXML private VBox courseContainer;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageLabel;

    private int currentPage = 1;
    private int totalPages = 1;
    private Integer searchId;
    private String searchName;
    private Set<Integer> enrolledCourseIds = new HashSet<>();

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();

        loadEnrolledCourses();
        loadCourses(currentPage);

        searchBtn.setOnAction(e -> {
            currentPage = 1;
            String idText = idField.getText().trim();
            searchId = idText.isEmpty() ? null : Integer.parseInt(idText);
            searchName = nameField.getText().trim().isEmpty() ? null : nameField.getText().trim();
            loadCourses(currentPage);
        });

        resetBtn.setOnAction(e -> {
            idField.clear();
            nameField.clear();
            searchId = null;
            searchName = null;
            currentPage = 1;
            loadCourses(currentPage);
        });

        prevPageBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; loadCourses(currentPage); } });
        nextPageBtn.setOnAction(e -> { if (currentPage < totalPages) { currentPage++; loadCourses(currentPage); } });
    }

    private void loadEnrolledCourses() {
        try {
            PageResult<Course> myCourses = ApiClient.getMyCourses(1, 100);
            if (myCourses != null && myCourses.getList() != null) {
                enrolledCourseIds = myCourses.getList().stream()
                        .map(Course::getId).collect(Collectors.toSet());
            }
        } catch (Exception ignored) {}
    }

    private void loadCourses(int page) {
        courseContainer.getChildren().clear();
        try {
            PageResult<Course> result = ApiClient.getCourseList(page, 10, searchId, searchName, null);
            if (result != null) {
                totalPages = Math.max(1, (int) Math.ceil((double) result.getTotal() / result.getPageSize()));
                pageLabel.setText("第 " + page + "/" + totalPages + " 页");
                prevPageBtn.setDisable(page <= 1);
                nextPageBtn.setDisable(page >= totalPages);

                if (result.getList() != null) {
                    for (Course c : result.getList()) {
                        courseContainer.getChildren().add(createCourseCard(c));
                    }
                }
            }
        } catch (Exception e) {
            Label err = new Label("加载失败: " + e.getMessage());
            err.setStyle("-fx-text-fill: red;");
            courseContainer.getChildren().add(err);
        }
    }

    private HBox createCourseCard(Course c) {
        HBox card = new HBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            if (enrolledCourseIds.contains(c.getId())) {
                // 已选 → 课程详情页
                NavigationManager.navigateTo("course-detail-view.fxml",
                        (CourseDetailController controller) -> controller.setCourseId(c.getId()));
            } else {
                // 未选 → 选课详情页
                NavigationManager.navigateTo("course-selection-detail-view.fxml",
                        (CourseSelectionDetailController controller) -> controller.setCourseId(c.getId()));
            }
        });

        Label nameLabel = new Label(c.getCourseName());
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label teacherLabel = new Label("教师: " + (c.getTeacherName() != null ? c.getTeacherName() : ""));
        teacherLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        Label addressLabel = new Label("地点: " + (c.getAddress() != null ? c.getAddress() : ""));
        addressLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #95a5a6;");

        card.getChildren().addAll(nameLabel, spacer, teacherLabel, addressLabel);

        // 已选标签
        if (enrolledCourseIds.contains(c.getId())) {
            Label enrolledBadge = new Label("已选");
            enrolledBadge.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 2 8; -fx-background-radius: 3;");
            card.getChildren().add(enrolledBadge);
        }

        return card;
    }
}
