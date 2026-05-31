package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TeacherHomeController extends BaseController {

    @FXML private Label welcomeLabel;
    @FXML private VBox announcementContainer;
    @FXML private VBox courseContainer;
    @FXML private Button moreAnnouncementsBtn;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageLabel;

    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    @FXML
    public void initialize() {
        super.initialize();

        // 设置欢迎语
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("欢迎你，" + user.getName() + "老师");
        }

        // 加载公告
        loadAnnouncements();

        // 加载课程
        loadCourses(currentPage);

        // "查看更多"公告
        moreAnnouncementsBtn.setOnAction(e -> {
            NavigationManager.navigateTo("list-page-view.fxml", (ListPageController controller) -> {
                controller.setPageType(ListPageController.PageType.ANNOUNCEMENT, null, "TEACHER");
            });
        });

        // 分页按钮
        prevPageBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadCourses(currentPage);
            }
        });
        nextPageBtn.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadCourses(currentPage);
            }
        });
    }

    private void loadAnnouncements() {
        announcementContainer.getChildren().clear();
        try {
            PageResult<Announcement> result = ApiClient.getAnnouncementList(1, 2);
            if (result != null && result.getList() != null) {
                for (Announcement a : result.getList()) {
                    HBox card = createAnnouncementCard(a);
                    announcementContainer.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            Label errorLabel = new Label("加载公告失败: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            announcementContainer.getChildren().add(errorLabel);
        }
    }

    private HBox createAnnouncementCard(Announcement a) {
        HBox card = new HBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            NavigationManager.navigateTo("announcement-detail-view.fxml",
                    (AnnouncementDetailController controller) -> controller.setAnnouncementId(a.getId()));
        });

        Label titleLabel = new Label(a.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label publisherLabel = new Label(a.getPublisherName() != null ? a.getPublisherName() : "");
        publisherLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        Label timeLabel = new Label(formatDateTime(a.getPublishTime()));
        timeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #95a5a6;");

        card.getChildren().addAll(titleLabel, spacer, publisherLabel, timeLabel);
        return card;
    }

    private void loadCourses(int page) {
        courseContainer.getChildren().clear();
        try {
            PageResult<Course> result = ApiClient.getTeacherCourses(page, 10);
            if (result != null) {
                totalPages = (int) Math.ceil((double) result.getTotal() / result.getPageSize());
                pageLabel.setText("第 " + page + "/" + totalPages + " 页");
                prevPageBtn.setDisable(page <= 1);
                nextPageBtn.setDisable(page >= totalPages);

                if (result.getList() != null) {
                    for (Course c : result.getList()) {
                        HBox card = createCourseCard(c);
                        courseContainer.getChildren().add(card);
                    }
                }
            }
        } catch (Exception e) {
            Label errorLabel = new Label("加载课程失败: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            courseContainer.getChildren().add(errorLabel);
        }
    }

    private HBox createCourseCard(Course c) {
        HBox card = new HBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            NavigationManager.navigateTo("course-detail-view.fxml",
                    (CourseDetailController controller) -> controller.setCourseId(c.getId()));
        });

        Label nameLabel = new Label(c.getCourseName());
        nameLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label teacherLabel = new Label("教师: " + (c.getTeacherName() != null ? c.getTeacherName() : ""));
        teacherLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");
        Label addressLabel = new Label("地点: " + (c.getAddress() != null ? c.getAddress() : ""));
        addressLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #95a5a6;");

        card.getChildren().addAll(nameLabel, spacer, teacherLabel, addressLabel);
        return card;
    }
}
