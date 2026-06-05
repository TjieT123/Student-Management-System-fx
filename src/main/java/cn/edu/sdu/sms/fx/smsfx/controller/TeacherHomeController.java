package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.Map;

public class TeacherHomeController extends BaseController {

    @FXML private Label welcomeLabel;
    @FXML private VBox announcementContainer;
    @FXML private Button moreAnnouncementsBtn;
    @FXML private GridPane scheduleGrid;
    @FXML private ComboBox<Integer> weekSelector;
    @FXML private VBox courseCardBox, homeworkCardBox;
    @FXML private Label courseCountLabel, homeworkCountLabel;
    @FXML private HBox functionBox;
    private static final String[] TIME_SLOTS = {"8:00-9:50","10:10-12:00","14:00-15:50","16:10-18:00","19:00-20:50"};
    private static final String[] DAY_NAMES = {"周一","周二","周三","周四","周五","周六","周日"};

    @Override @FXML public void initialize() {
        super.initialize();
        User user = SessionManager.getCurrentUser();
        if (user != null) welcomeLabel.setText("欢迎你，" + user.getName() + "老师");
        loadAnnouncements();
        setupWeekSelector(); loadWeeklySchedule(getCurrentWeek()); loadHomeCards(); setupFunctionButtons();
        moreAnnouncementsBtn.setOnAction(e -> NavigationManager.navigateTo("list-page-view.fxml",
            c -> ((ListPageController)c).setPageType(ListPageController.PageType.ANNOUNCEMENT, null, "TEACHER")));
    }

    private void setupWeekSelector() {
        try { SemesterConfig sc = ApiClient.getSemesterConfig();
            if (sc != null && sc.getTotalWeeks() != null) {
                for (int w=1; w<=sc.getTotalWeeks(); w++) weekSelector.getItems().add(w);
                weekSelector.setValue(getCurrentWeek());
                weekSelector.setOnAction(e -> loadWeeklySchedule(weekSelector.getValue()));
            }
        } catch (Exception e) { weekSelector.setDisable(true); }
    }

    private int getCurrentWeek() {
        try { SemesterConfig sc = ApiClient.getSemesterConfig();
            if (sc != null && sc.getStartWeekDate() != null && sc.getTotalWeeks() != null) {
                java.time.LocalDate start = java.time.LocalDate.parse(sc.getStartWeekDate());
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, java.time.LocalDate.now());
                return Math.max(1, Math.min((int)(days/7)+1, sc.getTotalWeeks()));
            }
        } catch (Exception ignored) {}
        return 1;
    }

    private void loadWeeklySchedule(int week) {
        scheduleGrid.getChildren().clear();
        scheduleGrid.getColumnConstraints().clear(); scheduleGrid.getRowConstraints().clear();
        scheduleGrid.setGridLinesVisible(true);
        scheduleGrid.getColumnConstraints().add(new ColumnConstraints(70));
        for (int i=0;i<7;i++) scheduleGrid.getColumnConstraints().add(new ColumnConstraints(110));
        scheduleGrid.getRowConstraints().add(new RowConstraints(28));
        for (int i=0;i<5;i++) scheduleGrid.getRowConstraints().add(new RowConstraints(56));
        scheduleGrid.add(new Label(""), 0, 0);
        for (int d=0; d<7; d++) { Label h = new Label(DAY_NAMES[d]); h.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-alignment: center;"); h.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE); scheduleGrid.add(h, d+1, 0); }
        for (int s=0; s<5; s++) { Label tl = new Label(TIME_SLOTS[s]); tl.setStyle("-fx-font-size: 11; -fx-text-fill: #555; -fx-alignment: center;"); tl.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE); scheduleGrid.add(tl, 0, s+1); }
        try {
            List<Map<String, Object>> schedule = ApiClient.getTeacherSchedule(week);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            for (Map<String, Object> course : schedule) { String sj = (String) course.get("schedule"); if (sj==null) continue;
                List<Map<String, Object>> slots = mapper.readValue(sj, List.class);
                String cn = (String) course.get("courseName"); Object cidObj = course.get("id");
                for (Map<String, Object> sl : slots) { int day=((Number)sl.get("dayOfWeek")).intValue(); int s=((Number)sl.get("slot")).intValue();
                    Label cell = new Label(truncate(cn,8)); cell.setStyle("-fx-background-color: #d4e6f1; -fx-font-size: 12; -fx-font-weight: bold; -fx-alignment: center; -fx-text-fill: #2c3e50;"); cell.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
                    if (cidObj!=null) { final Integer cid=((Number)cidObj).intValue(); cell.setStyle(cell.getStyle()+"-fx-cursor: hand;"); cell.setOnMouseClicked(e->NavigationManager.navigateTo("course-detail-view.fxml", ctrl->((CourseDetailController)ctrl).setCourseId(cid))); }
                    scheduleGrid.add(cell, day, s);
                }
            }
        } catch (Exception ignored) {}
        for (int d=1;d<=7;d++) { final int day=d; for (int s=1;s<=5;s++) { final int slot=s;
            if (scheduleGrid.getChildren().stream().noneMatch(n->GridPane.getColumnIndex(n)!=null&&GridPane.getColumnIndex(n)==day&&GridPane.getRowIndex(n)!=null&&GridPane.getRowIndex(n)==slot)) {
                Label empty = new Label(""); empty.setStyle("-fx-background-color: #f5f5f5;"); empty.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE); scheduleGrid.add(empty, day, slot);
        } } }
    }

    private void loadHomeCards() {
        try { PageResult<Course> courses = ApiClient.getTeacherCourses(1,100);
            if (courses != null) courseCountLabel.setText(String.valueOf(courses.getTotal())); } catch (Exception e) { courseCountLabel.setText("-"); }
        try {
            PageResult<Homework> hws = ApiClient.getHomeworkList(1, 200);
            if (hws != null && hws.getList() != null) homeworkCountLabel.setText(String.valueOf(hws.getList().size()));
        } catch (Exception e) { homeworkCountLabel.setText("-"); }
        courseCardBox.setOnMouseClicked(e -> NavigationManager.navigateTo("list-page-view.fxml", c -> ((ListPageController)c).setPageType(ListPageController.PageType.COURSE, null, "TEACHER")));
        homeworkCardBox.setOnMouseClicked(e -> NavigationManager.navigateTo("homework-grading-list-view.fxml", c -> {}));
    }

    private void setupFunctionButtons() {
        String[][] btns = {{"📝", "发布作业", "homework-publish-view.fxml"}, {"📋", "作业批改", "homework-grading-list-view.fxml"},
            {"📊", "成绩录入", "score-entry-view.fxml"}, {"📈", "教学统计", ""}, {"📄", "课程资料", ""}};
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

    private String truncate(String s, int len) { return s==null?"":s.length()<=len?s:s.substring(0,len)+"…"; }

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
