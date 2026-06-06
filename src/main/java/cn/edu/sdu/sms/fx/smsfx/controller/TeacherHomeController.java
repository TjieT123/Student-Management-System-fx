package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class TeacherHomeController extends BaseController {

    @FXML private Label welcomeLabel;
    @FXML private VBox announcementContainer;
    @FXML private Button moreAnnouncementsBtn;
    @FXML private VBox courseCardBox, homeworkCardBox;
    @FXML private Label courseCountLabel, homeworkCountLabel;
    @FXML private HBox functionBox;

    @Override @FXML public void initialize() {
        super.initialize();
        User user = SessionManager.getCurrentUser();
        if (user != null) welcomeLabel.setText("欢迎你，" + user.getName() + "老师");
        loadAnnouncements();
        loadHomeCards(); setupFunctionButtons();
        moreAnnouncementsBtn.setOnAction(e -> NavigationManager.navigateTo("list-page-view.fxml",
            c -> ((ListPageController)c).setPageType(ListPageController.PageType.ANNOUNCEMENT, null, "TEACHER")));
    }

    private void loadHomeCards() {
        try { PageResult<Course> courses = ApiClient.getTeacherCourses(1,100);
            if (courses != null) courseCountLabel.setText(String.valueOf(courses.getTotal())); } catch (Exception e) { courseCountLabel.setText("-"); }
        try {
            PageResult<Homework> hws = ApiClient.getHomeworkList(1, 200);
            if (hws != null && hws.getList() != null) homeworkCountLabel.setText(String.valueOf(hws.getList().size()));
        } catch (Exception e) { homeworkCountLabel.setText("-"); }
        courseCardBox.setOnMouseClicked(e -> NavigationManager.navigateTo("list-page-view.fxml", c -> ((ListPageController)c).setPageType(ListPageController.PageType.COURSE, null, "TEACHER")));
        homeworkCardBox.setOnMouseClicked(e -> NavigationManager.navigateTo("list-page-view.fxml", c -> ((ListPageController)c).setPageType(ListPageController.PageType.HOMEWORK, null, "TEACHER")));
    }

    private void setupFunctionButtons() {
        String[][] btns = {{"📝", "发布作业", "homework-publish-view.fxml"},
            {"📊", "成绩录入", "score-entry-view.fxml"}, {"📄", "课程资料", "course-materials-view.fxml"}};
        for (String[] b : btns) {
            VBox card = new VBox(4); card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 10 6; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
            card.setPrefSize(140, 70);
            card.getChildren().addAll(new Label(b[0]) {{ setStyle("-fx-font-size: 22;"); }}, new Label(b[1]) {{ setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"); }});
            final String fxml = b[2];
            card.setOnMouseClicked(e -> { if (!fxml.isEmpty()) NavigationManager.navigateTo(fxml, null); else showInfo("功能开发中"); });
            functionBox.getChildren().add(card);
        }
    }

    private void loadAnnouncements() {
        announcementContainer.getChildren().clear();
        try { PageResult<Announcement> r = ApiClient.getAnnouncementList(1,2,null,null);
            if (r!=null && r.getList()!=null) for (Announcement a : r.getList()) announcementContainer.getChildren().add(createAnnouncementCard(a));
        } catch (Exception e) { announcementContainer.getChildren().add(new Label("加载失败")); }
    }

    private HBox createAnnouncementCard(Announcement a) {
        HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> NavigationManager.navigateTo("announcement-detail-view.fxml", ctrl -> ((AnnouncementDetailController)ctrl).setAnnouncementId(a.getId())));
        Label t = new Label(a.getTitle()); t.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        card.getChildren().addAll(t, sp, new Label(a.getPublisherName()!=null?a.getPublisherName():""), new Label(formatDateTime(a.getPublishTime())));
        return card;
    }

}
