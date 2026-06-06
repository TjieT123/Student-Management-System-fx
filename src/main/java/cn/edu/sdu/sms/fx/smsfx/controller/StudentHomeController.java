package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class StudentHomeController extends BaseController {

    @FXML private Label welcomeLabel;
    @FXML private VBox announcementContainer;
    @FXML private Button moreAnnouncementsBtn;
    @FXML private VBox homeworkCardBox, courseCardBox;
    @FXML private Label homeworkCountLabel, homeworkDetailLabel, courseCountLabel;
    @FXML private HBox functionBox;

    @Override @FXML
    public void initialize() {
        super.initialize();
        User user = SessionManager.getCurrentUser();
        if (user != null) welcomeLabel.setText("欢迎你，" + user.getName() + "同学");
        loadAnnouncements();
        loadHomeCards();
        setupFunctionButtons();

        moreAnnouncementsBtn.setOnAction(e -> NavigationManager.navigateTo("list-page-view.fxml",
            c -> ((ListPageController)c).setPageType(ListPageController.PageType.ANNOUNCEMENT, null, "STUDENT")));
    }

    private void loadHomeCards() {
        try {
            PageResult<Course> courses = ApiClient.getMyCourses(1, 100);
            if (courses != null && courses.getList() != null) courseCountLabel.setText(String.valueOf(courses.getTotal()));
        } catch (Exception e) { courseCountLabel.setText("-"); }
        // 加载作业统计
        int total = 0, unsubmitted = 0, submitted = 0;
        try {
            PageResult<Course> courses = ApiClient.getMyCourses(1, 100);
            if (courses != null && courses.getList() != null) {
                for (Course c : courses.getList()) {
                    PageResult<StudentHomeworkItem> hw = ApiClient.getStudentHomeworkList(c.getId(), 1, 100);
                    if (hw != null && hw.getList() != null) {
                        total += hw.getList().size();
                        for (StudentHomeworkItem h : hw.getList()) {
                            if ("UNSUBMIT".equals(h.getStatus())) unsubmitted++;
                            else submitted++;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        homeworkCountLabel.setText(String.valueOf(total));
        homeworkDetailLabel.setText(unsubmitted + " 待提交 | " + submitted + " 已提交");
        courseCardBox.setOnMouseClicked(e -> NavigationManager.navigateTo("list-page-view.fxml",
            c -> ((ListPageController)c).setPageType(ListPageController.PageType.COURSE, null, "STUDENT")));
        homeworkCardBox.setOnMouseClicked(e -> NavigationManager.navigateTo("list-page-view.fxml",
            c -> ((ListPageController)c).setPageType(ListPageController.PageType.HOMEWORK, null, "STUDENT")));
    }

    private void setupFunctionButtons() {
        String[][] btns = {{"🎓", "选课中心", "course-selection-view.fxml"},
            {"📊", "成绩查看", "score-view.fxml"}, {"🏆", "荣誉管理", "honor-view.fxml"},
            {"💡", "创新实践", "innovation-practice-view.fxml"}, {"🏥", "请假申请", "leave-view.fxml"},
            {"🎉", "日常活动", "activity-view.fxml"}};
        for (String[] b : btns) {
            VBox card = new VBox(4); card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 10 6; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
            card.setPrefSize(140, 70);
            Label icon = new Label(b[0]); icon.setStyle("-fx-font-size: 22;");
            Label text = new Label(b[1]); text.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            card.getChildren().addAll(icon, text);
            final String fxml = b[2];
            card.setOnMouseClicked(e -> NavigationManager.navigateTo(fxml, null));
            functionBox.getChildren().add(card);
        }
    }

    private void loadAnnouncements() {
        announcementContainer.getChildren().clear();
        try {
            PageResult<Announcement> result = ApiClient.getAnnouncementList(1, 2, null, null);
            if (result != null && result.getList() != null) {
                for (Announcement a : result.getList()) announcementContainer.getChildren().add(createAnnouncementCard(a));
            }
        } catch (Exception e) {
            Label err = new Label("加载公告失败: " + e.getMessage());
            err.setStyle("-fx-text-fill: red;"); announcementContainer.getChildren().add(err);
        }
    }

    private HBox createAnnouncementCard(Announcement a) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> NavigationManager.navigateTo("announcement-detail-view.fxml",
            ctrl -> ((AnnouncementDetailController)ctrl).setAnnouncementId(a.getId())));
        Label title = new Label(a.getTitle());
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(title, spacer,
            new Label(a.getPublisherName() != null ? a.getPublisherName() : ""),
            new Label(formatDateTime(a.getPublishTime())));
        return card;
    }

}
