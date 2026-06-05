package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Collectors;

public class ListPageController extends BaseController {

    public enum PageType { ANNOUNCEMENT, HOMEWORK, COURSE }

    @FXML private Label pageTitleLabel;
    @FXML private VBox cardContainer;
    @FXML private HBox paginationBox, filterBox;
    @FXML private Button prevPageBtn, nextPageBtn;
    @FXML private Label pageLabel;
    @FXML private ComboBox<String> courseFilter, statusFilter;

    private PageType pageType;
    private Integer courseId;
    private String role;
    private int currentPage = 1, totalPages = 1;
    private List<StudentHomeworkItem> allStudentHomeworks = new ArrayList<>();

    @Override @FXML public void initialize() { super.initialize(); enableBackButton(); }

    public void setPageType(PageType type, Integer courseId, String role) {
        this.pageType = type; this.courseId = courseId; this.role = role;
        filterBox.setVisible(false); filterBox.setManaged(false);
        if (type == PageType.HOMEWORK && "STUDENT".equals(role)) {
            initHomeworkFilters();
        }
        loadData();
        prevPageBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        nextPageBtn.setOnAction(e -> { if (currentPage < totalPages) { currentPage++; loadData(); } });
    }

    private void initHomeworkFilters() {
        filterBox.setVisible(true); filterBox.setManaged(true);
        courseFilter.getItems().clear(); statusFilter.getItems().clear();
        courseFilter.getItems().add("全部课程");
        statusFilter.getItems().addAll("全部状态", "未提交", "已提交", "已批改", "迟交");
        courseFilter.setValue("全部课程"); statusFilter.setValue("全部状态");
        try {
            PageResult<Course> courses = ApiClient.getMyCourses(1, 100);
            if (courses != null && courses.getList() != null) {
                for (Course c : courses.getList()) courseFilter.getItems().add(c.getCourseName());
            }
        } catch (Exception ignored) {}
        courseFilter.setOnAction(e -> { currentPage = 1; loadData(); });
        statusFilter.setOnAction(e -> { currentPage = 1; loadData(); });
    }

    private void loadData() {
        cardContainer.getChildren().clear();
        try {
            if (pageType == PageType.ANNOUNCEMENT) {
                pageTitleLabel.setText("📢 公告");
                PageResult<Announcement> result = ApiClient.getAnnouncementList(currentPage, 10, null, null);
                updatePagination(result);
                if (result.getList() != null) for (Announcement a : result.getList()) cardContainer.getChildren().add(createAnnouncementCard(a));
            } else if (pageType == PageType.COURSE) {
                pageTitleLabel.setText("📚 我的课程");
                if ("STUDENT".equals(role)) {
                    PageResult<Course> result = ApiClient.getMyCourses(currentPage, 10);
                    updatePagination(result);
                    if (result.getList() != null) for (Course c : result.getList()) cardContainer.getChildren().add(createCourseCard(c));
                } else {
                    PageResult<Course> result = ApiClient.getTeacherCourses(currentPage, 10);
                    updatePagination(result);
                    if (result.getList() != null) for (Course c : result.getList()) cardContainer.getChildren().add(createCourseCard(c));
                }
            } else if (pageType == PageType.HOMEWORK) {
                pageTitleLabel.setText("📝 我的作业");
                if ("STUDENT".equals(role)) {
                    loadAllStudentHomeworks();
                    List<StudentHomeworkItem> filtered = applyFilters(allStudentHomeworks);
                    totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / 10));
                    pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页");
                    prevPageBtn.setDisable(currentPage <= 1); nextPageBtn.setDisable(currentPage >= totalPages);
                    int from = (currentPage - 1) * 10, to = Math.min(from + 10, filtered.size());
                    for (int i = from; i < to; i++) cardContainer.getChildren().add(createStudentHomeworkCard(filtered.get(i)));
                } else {
                    PageResult<Homework> all = ApiClient.getHomeworkList(1, 200);
                    if (all != null && all.getList() != null) {
                        List<Homework> filtered = courseId != null ? all.getList().stream().filter(h -> courseId.equals(h.getCourseId())).collect(Collectors.toList()) : all.getList();
                        totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / 10));
                        pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页");
                        prevPageBtn.setDisable(currentPage <= 1); nextPageBtn.setDisable(currentPage >= totalPages);
                        int from = (currentPage - 1) * 10, to = Math.min(from + 10, filtered.size());
                        for (int i = from; i < to; i++) cardContainer.getChildren().add(createTeacherHomeworkCard(filtered.get(i)));
                    }
                }
            }
        } catch (Exception e) {
            cardContainer.getChildren().add(new Label("加载失败: " + e.getMessage()));
        }
    }

    private void loadAllStudentHomeworks() {
        allStudentHomeworks.clear();
        try {
            PageResult<Course> courses = ApiClient.getMyCourses(1, 100);
            if (courses != null && courses.getList() != null) {
                for (Course c : courses.getList()) {
                    PageResult<StudentHomeworkItem> hw = ApiClient.getStudentHomeworkList(c.getId(), 1, 100);
                    if (hw != null && hw.getList() != null) allStudentHomeworks.addAll(hw.getList());
                }
            }
        } catch (Exception ignored) {}
    }

    private List<StudentHomeworkItem> applyFilters(List<StudentHomeworkItem> list) {
        String cf = courseFilter.getValue();
        String sf = statusFilter.getValue();
        return list.stream().filter(h -> {
            if (cf != null && !"全部课程".equals(cf) && !cf.equals(h.getCourseName())) return false;
            if (sf != null && !"全部状态".equals(sf)) {
                String mapped = switch (sf) { case "未提交" -> "UNSUBMIT"; case "已提交" -> "SUBMITTED"; case "已批改" -> "GRADED"; case "迟交" -> "LATE"; default -> null; };
                if (mapped != null && !mapped.equals(h.getStatus())) return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    private void updatePagination(PageResult<?> result) {
        if (result != null) totalPages = (int) Math.ceil((double) result.getTotal() / result.getPageSize());
        pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页");
        prevPageBtn.setDisable(currentPage <= 1); nextPageBtn.setDisable(currentPage >= totalPages);
    }

    private HBox createAnnouncementCard(Announcement a) {
        HBox card = new HBox(15); card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> NavigationManager.navigateTo("announcement-detail-view.fxml", ctrl -> ((AnnouncementDetailController)ctrl).setAnnouncementId(a.getId())));
        Label t = new Label(a.getTitle()); t.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        card.getChildren().addAll(t, sp, new Label(a.getPublisherName()!=null?a.getPublisherName():""), new Label(formatDateTime(a.getPublishTime())));
        return card;
    }

    private HBox createStudentHomeworkCard(StudentHomeworkItem hw) {
        HBox card = new HBox(15); card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> NavigationManager.navigateTo("homework-submit-view.fxml", ctrl -> ((HomeworkSubmitController)ctrl).setHomeworkId(hw.getId(), null)));
        Label t = new Label(hw.getTitle()); t.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        card.getChildren().addAll(t, sp, new Label("截止: " + formatDateTime(hw.getDeadline())));
        if ("GRADED".equals(hw.getStatus()) && hw.getScore() != null) {
            Label s = new Label(hw.getScore()+"分"); s.setStyle("-fx-text-fill: "+(hw.getScore()>=60?"#27ae60":"#e74c3c")+"; -fx-font-weight: bold; -fx-font-size: 13;");
            card.getChildren().add(s);
        }
        card.getChildren().add(createStatusLabel(hw.getStatus()));
        return card;
    }

    private HBox createTeacherHomeworkCard(Homework hw) {
        HBox card = new HBox(15); card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> NavigationManager.navigateTo("homework-grading-list-view.fxml", ctrl -> ((HomeworkGradingListController)ctrl).setHomeworkId(hw.getId())));
        Label t = new Label(hw.getTitle()); t.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        card.getChildren().addAll(t, sp, new Label("截止: "+formatDateTime(hw.getDeadline())));
        return card;
    }

    private Label createStatusLabel(String status) {
        Label l = new Label(); if (status==null) status="UNSUBMIT";
        switch (status) { case "UNSUBMIT": l.setText("未提交"); l.setStyle("-fx-text-fill: gray; -fx-font-size: 12;"); break; case "SUBMITTED": l.setText("已提交"); l.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12;"); break; case "LATE": l.setText("迟交"); l.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;"); break; case "GRADED": l.setText("已批改"); l.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;"); break; default: l.setText(status); }
        return l;
    }

    private HBox createCourseCard(Course c) {
        HBox card = new HBox(15); card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> NavigationManager.navigateTo("course-detail-view.fxml", ctrl -> ((CourseDetailController)ctrl).setCourseId(c.getId())));
        Label n = new Label(c.getCourseName()); n.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        card.getChildren().addAll(n, sp, new Label("教师: "+(c.getTeacherName()!=null?c.getTeacherName():"")), new Label("地点: "+(c.getAddress()!=null?c.getAddress():"")));
        return card;
    }
}
