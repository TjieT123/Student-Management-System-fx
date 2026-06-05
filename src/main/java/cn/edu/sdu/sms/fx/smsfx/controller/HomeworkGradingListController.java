package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;
import java.util.stream.Collectors;

public class HomeworkGradingListController extends BaseController {

    @FXML private Label homeworkTitleLabel;
    @FXML private VBox submissionContainer;
    @FXML private Button prevPageBtn, nextPageBtn, statisticsBtn;
    @FXML private Label pageLabel;
    @FXML private ComboBox<String> courseFilter, statusFilter;

    private Integer homeworkId;
    private int currentPage = 1, totalPages = 1;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        prevPageBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; loadSubmissions(); } });
        nextPageBtn.setOnAction(e -> { if (currentPage < totalPages) { currentPage++; loadSubmissions(); } });
        statisticsBtn.setOnAction(e -> handleStatistics());
        initFilters();
        loadSubmissions();
    }

    public void setHomeworkId(Integer homeworkId) {
        this.homeworkId = homeworkId;
        homeworkTitleLabel.setText("作业批改");
        initFilters();
        loadSubmissions();
    }

    private void initFilters() {
        courseFilter.getItems().clear(); statusFilter.getItems().clear();
        courseFilter.getItems().add("全部课程");
        statusFilter.getItems().addAll("全部状态", "待批改", "已批改");
        courseFilter.setValue("全部课程"); statusFilter.setValue("全部状态");
        try {
            PageResult<Course> courses = ApiClient.getTeacherCourses(1, 100);
            if (courses != null && courses.getList() != null) {
                for (Course c : courses.getList()) {
                    PageResult<Homework> hws = ApiClient.getHomeworkList(1, 200);
                    if (hws != null && hws.getList() != null) {
                        boolean hasSubmissions = hws.getList().stream().anyMatch(h -> c.getId().equals(h.getCourseId()));
                        if (hasSubmissions) courseFilter.getItems().add(c.getCourseName());
                    }
                }
            }
        } catch (Exception ignored) {}
        courseFilter.setOnAction(e -> { currentPage = 1; loadSubmissions(); });
        statusFilter.setOnAction(e -> { currentPage = 1; loadSubmissions(); });
    }

    private void loadSubmissions() {
        submissionContainer.getChildren().clear();
        try {
            List<HomeworkSubmit> allSubs = new ArrayList<>();
            Map<String, Integer> nameToId = new HashMap<>();

            // Collect homework list and submissions
            PageResult<Homework> allHw = ApiClient.getHomeworkList(1, 200);
            if (allHw == null || allHw.getList() == null) return;
            final List<Homework> hwList;
            if (homeworkId != null) {
                hwList = allHw.getList().stream().filter(h -> h.getId().equals(homeworkId)).collect(Collectors.toList());
            } else {
                hwList = allHw.getList();
            }
            for (Homework hw : hwList) {
                PageResult<HomeworkSubmit> subs = ApiClient.getSubmitList(hw.getId(), 1, 200);
                if (subs != null && subs.getList() != null) allSubs.addAll(subs.getList());
            }

            // Build course name -> courseId map
            try {
                PageResult<Course> courses = ApiClient.getTeacherCourses(1, 200);
                if (courses != null && courses.getList() != null)
                    for (Course c : courses.getList()) nameToId.put(c.getCourseName(), c.getId());
            } catch (Exception ignored) {}

            // Filter by course
            String cf = courseFilter.getValue();
            if (cf != null && !"全部课程".equals(cf)) {
                Integer cid = nameToId.get(cf);
                if (cid != null) {
                    final Integer fc = cid;
                    allSubs = allSubs.stream().filter(s ->
                        hwList.stream().anyMatch(h -> h.getId().equals(s.getHomeworkId()) && fc.equals(h.getCourseId()))
                    ).collect(Collectors.toList());
                }
            }

            // Filter by status
            String sf = statusFilter.getValue();
            if (sf != null && !"全部状态".equals(sf)) {
                if ("待批改".equals(sf)) allSubs = allSubs.stream().filter(s -> !"GRADED".equals(s.getStatus())).collect(Collectors.toList());
                else if ("已批改".equals(sf)) allSubs = allSubs.stream().filter(s -> "GRADED".equals(s.getStatus())).collect(Collectors.toList());
            }

            // Paginate
            totalPages = Math.max(1, (int) Math.ceil((double) allSubs.size() / 10));
            pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页（共 " + allSubs.size() + " 条）");
            prevPageBtn.setDisable(currentPage <= 1); nextPageBtn.setDisable(currentPage >= totalPages);
            int from = (currentPage - 1) * 10, to = Math.min(from + 10, allSubs.size());
            for (int i = from; i < to; i++) submissionContainer.getChildren().add(createSubmissionCard(allSubs.get(i)));
        } catch (Exception e) {
            submissionContainer.getChildren().add(new Label("加载失败: " + e.getMessage()));
        }
    }

    private HBox createSubmissionCard(HomeworkSubmit hs) {
        HBox card = new HBox(15); card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> NavigationManager.navigateTo("homework-grading-view.fxml",
            ctrl -> ((HomeworkGradingController)ctrl).setSubmitId(hs.getId(), hs.getHomeworkId())));
        Label name = new Label(hs.getStudentName() != null ? hs.getStudentName() : "未知");
        name.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label status = new Label();
        if ("GRADED".equals(hs.getStatus())) { status.setText("已批改 (" + (hs.getScore() != null ? hs.getScore() : "-") + "分)"); status.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;"); }
        else { status.setText("待批改"); status.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12;"); }
        card.getChildren().addAll(name, sp, status, new Label(formatDateTime(hs.getSubmitTime())));
        return card;
    }

    private void handleStatistics() {
        String cf = courseFilter.getValue();
        if (cf == null || "全部课程".equals(cf)) {
            // Show course picker dialog first
            showStatisticsCoursePicker();
        } else if (homeworkId != null) {
            statisticsBtn.setDisable(true); statisticsBtn.setText("加载中...");
            try {
                HomeworkStatistics stats = ApiClient.getHomeworkStatistics(homeworkId);
                showStatisticsDialog(stats != null ? stats : new HomeworkStatistics());
            } catch (Exception e) { showError("加载统计数据失败: " + e.getMessage()); }
            finally { statisticsBtn.setDisable(false); statisticsBtn.setText("统计"); }
        } else {
            // Specific course but no specific homework — find first homework for this course
            statisticsBtn.setDisable(true); statisticsBtn.setText("加载中...");
            try {
                PageResult<Homework> allHw = ApiClient.getHomeworkList(1, 200);
                Homework target = null;
                if (allHw != null && allHw.getList() != null) {
                    PageResult<Course> courses = ApiClient.getTeacherCourses(1, 200);
                    if (courses != null && courses.getList() != null) {
                        for (Course c : courses.getList()) {
                            if (c.getCourseName().equals(cf)) {
                                for (Homework hw : allHw.getList()) {
                                    if (c.getId().equals(hw.getCourseId())) { target = hw; break; }
                                }
                                break;
                            }
                        }
                    }
                }
                if (target == null) { showWarning("该课程暂无作业"); return; }
                HomeworkStatistics stats = ApiClient.getHomeworkStatistics(target.getId());
                showStatisticsDialog(stats != null ? stats : new HomeworkStatistics());
            } catch (Exception e) { showError("加载统计数据失败: " + e.getMessage()); }
            finally { statisticsBtn.setDisable(false); statisticsBtn.setText("统计"); }
        }
    }

    private void showStatisticsCoursePicker() {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("选择课程");
        ComboBox<String> cb = new ComboBox<>();
        try {
            PageResult<Course> courses = ApiClient.getTeacherCourses(1, 200);
            if (courses != null && courses.getList() != null)
                for (Course c : courses.getList()) cb.getItems().add(c.getCourseName());
        } catch (Exception ignored) {}
        if (cb.getItems().isEmpty()) { showWarning("暂无课程"); return; }
        cb.setValue(cb.getItems().get(0));
        VBox content = new VBox(10, new Label("请选择要查看统计的课程:"), cb);
        content.setPadding(new Insets(15));
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK && cb.getValue() != null) {
                statisticsBtn.setDisable(true); statisticsBtn.setText("加载中...");
                try {
                    PageResult<Homework> allHw = ApiClient.getHomeworkList(1, 200);
                    PageResult<Course> courses = ApiClient.getTeacherCourses(1, 200);
                    Homework target = null;
                    if (allHw != null && allHw.getList() != null && courses != null && courses.getList() != null) {
                        for (Course c : courses.getList()) {
                            if (c.getCourseName().equals(cb.getValue())) {
                                for (Homework hw : allHw.getList())
                                    if (c.getId().equals(hw.getCourseId())) { target = hw; break; }
                                break;
                            }
                        }
                    }
                    if (target == null) { showWarning("该课程暂无作业"); return; }
                    HomeworkStatistics stats = ApiClient.getHomeworkStatistics(target.getId());
                    showStatisticsDialog(stats != null ? stats : new HomeworkStatistics());
                } catch (Exception e) { showError("加载统计数据失败: " + e.getMessage()); }
                finally { statisticsBtn.setDisable(false); statisticsBtn.setText("统计"); }
            }
        });
    }

    private void showStatisticsDialog(HomeworkStatistics stats) {
        if (stats == null || stats.getTotalStudents() == null) { showWarning("暂无统计数据"); return; }
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("作业提交统计"); d.setResizable(true);
        VBox c = new VBox(15); c.setPadding(new Insets(20));
        HBox cards = new HBox(20); cards.setAlignment(javafx.geometry.Pos.CENTER);
        cards.getChildren().addAll(createStatCard("总人数", String.valueOf(stats.getTotalStudents()!=null?stats.getTotalStudents():0), "#2c3e50"),
            createStatCard("已提交", String.valueOf(stats.getSubmittedCount()!=null?stats.getSubmittedCount():0), "#27ae60"),
            createStatCard("未提交", String.valueOf(stats.getUnsubmittedCount()!=null?stats.getUnsubmittedCount():0), "#e74c3c"));
        VBox dist = new VBox(10); dist.setPadding(new Insets(10,0,0,0));
        Map<String,Integer> m = stats.getDistribution();
        if (m != null) {
            int t = stats.getSubmittedCount()!=null?stats.getSubmittedCount():1;
            dist.getChildren().addAll(createDistBar("优秀 (85-100)", m.getOrDefault("excellent",0), t, "#27ae60"),
                createDistBar("良   (70-84)", m.getOrDefault("good",0), t, "#3498db"),
                createDistBar("及格 (60-69)", m.getOrDefault("pass",0), t, "#f39c12"),
                createDistBar("不及格 (0-59)", m.getOrDefault("fail",0), t, "#e74c3c"));
        }
        int sub = stats.getSubmittedCount()!=null?stats.getSubmittedCount():0, unsub = stats.getUnsubmittedCount()!=null?stats.getUnsubmittedCount():0;
        if (sub+unsub>0) {
            PieChart pie = new PieChart(); pie.setLabelsVisible(true);
            PieChart.Data sD=new PieChart.Data("已提交 ("+sub+")",sub), uD=new PieChart.Data("未提交 ("+unsub+")",unsub);
            pie.setData(FXCollections.observableArrayList(sD,uD)); pie.setPrefSize(350,250);
            sD.nodeProperty().addListener((obs,old,n)->{if(n!=null)n.setStyle("-fx-pie-color: #27ae60;");});
            uD.nodeProperty().addListener((obs,old,n)->{if(n!=null)n.setStyle("-fx-pie-color: #e74c3c;");});
            HBox pb = new HBox(pie); pb.setAlignment(javafx.geometry.Pos.CENTER);
            c.getChildren().addAll(cards, new Separator(), pb, new Separator(), dist);
        } else c.getChildren().addAll(cards, new Separator(), dist);
        d.getDialogPane().setContent(c); d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE); d.showAndWait();
    }

    private VBox createStatCard(String l, String v, String clr) {
        VBox b = new VBox(5); b.setAlignment(javafx.geometry.Pos.CENTER);
        b.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"); b.setPrefWidth(130);
        b.getChildren().addAll(new Label(v){{setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: "+clr+";");}}, new Label(l){{setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");}});
        return b;
    }

    private HBox createDistBar(String l, int cnt, int t, String clr) {
        HBox b = new HBox(10); b.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label nl = new Label(l); nl.setPrefWidth(140); nl.setStyle("-fx-font-size: 13; -fx-text-fill: #555;");
        HBox bc = new HBox(); bc.setPrefHeight(22); HBox.setHgrow(bc, Priority.ALWAYS);
        double r = t>0?(double)cnt/t:0;
        Region f = new Region(); f.setStyle("-fx-background-color: "+clr+"; -fx-background-radius: 3 0 0 3;"); f.setPrefHeight(22); f.prefWidthProperty().bind(bc.widthProperty().multiply(r));
        Region g = new Region(); g.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 0 3 3 0;"); g.setPrefHeight(22); HBox.setHgrow(g, Priority.ALWAYS);
        bc.getChildren().addAll(f,g);
        b.getChildren().addAll(nl, bc, new Label(cnt+" 人"){{setPrefWidth(50); setStyle("-fx-font-size: 13; -fx-text-fill: "+clr+"; -fx-font-weight: bold;");}});
        return b;
    }
}
