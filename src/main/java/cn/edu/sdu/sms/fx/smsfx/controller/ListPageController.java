package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private boolean teacherFiltersReady = false;

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
                pageTitleLabel.setText("📝 作业管理");
                if ("STUDENT".equals(role)) {
                    loadAllStudentHomeworks();
                    List<StudentHomeworkItem> filtered = applyFilters(allStudentHomeworks);
                    totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / 10));
                    pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页");
                    prevPageBtn.setDisable(currentPage <= 1); nextPageBtn.setDisable(currentPage >= totalPages);
                    int from = (currentPage - 1) * 10, to = Math.min(from + 10, filtered.size());
                    for (int i = from; i < to; i++) cardContainer.getChildren().add(createStudentHomeworkCard(filtered.get(i)));
                } else {
                    filterBox.setVisible(true); filterBox.setManaged(true);
                    if (!teacherFiltersReady) initTeacherHomeworkFilters();
                    PageResult<Homework> all = ApiClient.getHomeworkList(1, 200);
                    if (all != null && all.getList() != null) {
                        List<Homework> filtered = applyTeacherFilters(all.getList());
                        totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / 10));
                        pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页（共 " + filtered.size() + " 条）");
                        prevPageBtn.setDisable(currentPage <= 1); nextPageBtn.setDisable(currentPage >= totalPages);
                        int from = (currentPage - 1) * 10, to = Math.min(from + 10, filtered.size());
                        for (int i = from; i < to; i++) cardContainer.getChildren().add(createTeacherHomeworkCardWithActions(filtered.get(i)));
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

    private HBox createTeacherHomeworkCardWithActions(Homework hw) {
        HBox card = new HBox(15); card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> NavigationManager.navigateTo("homework-grading-list-view.fxml",
            ctrl -> ((HomeworkGradingListController)ctrl).setHomeworkId(hw.getId())));
        VBox info = new VBox(3);
        Label t = new Label(hw.getTitle()); t.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        String courseName = getTeacherCourseName(hw.getCourseId());
        boolean expired = isDeadlinePassed(hw.getDeadline());
        Label detail = new Label("课程: " + courseName + "  |  " + (expired ? "已截止" : "未截止"));
        detail.setStyle("-fx-font-size: 11; -fx-text-fill: " + (expired ? "#e74c3c" : "#27ae60") + ";");
        info.getChildren().addAll(t, detail);
        Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        Button editBtn = new Button("编辑"); editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
        Button delBtn = new Button("删除"); delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
        editBtn.setOnAction(e -> { e.consume(); showHomeworkEditDialog(hw); });
        delBtn.setOnAction(e -> { e.consume(); if(showConfirm("确认删除","确定删除作业《"+hw.getTitle()+"》？")){try{ApiClient.deleteHomework(hw.getId());showInfo("删除成功");loadData();}catch(Exception ex){showError(ex.getMessage());}} });
        card.getChildren().addAll(info, sp, new Label("截止: "+formatDateTime(hw.getDeadline())), editBtn, delBtn);
        return card;
    }

    private void showHomeworkEditDialog(Homework hw) {
        try {
            Dialog<ButtonType> d = new Dialog<>(); d.setTitle("编辑作业"); d.setResizable(true);
            VBox box = new VBox(10); box.setPadding(new javafx.geometry.Insets(15));
            TextField titleF=new TextField(hw.getTitle()!=null?hw.getTitle():"");
            TextArea contentF=new TextArea(); contentF.setPrefRowCount(4);
            try{StudentHomeworkItem detail=ApiClient.getHomeworkContent(hw.getId());if(detail!=null&&detail.getContent()!=null)contentF.setText(detail.getContent());}catch(Exception ignored){}
            DatePicker dp=new DatePicker(); dp.setEditable(false); ComboBox<String> hh=new ComboBox<>(), mm=new ComboBox<>();
            for(int i=0;i<24;i++)hh.getItems().add(String.format("%02d",i));
            mm.getItems().addAll("00","30","59");
            if(hw.getDeadline()!=null){try{String dl=hw.getDeadline().replace("T"," ");String[] parts=dl.split(" ");dp.setValue(java.time.LocalDate.parse(parts[0]));String[] tm=parts[1].split(":");hh.setValue(tm[0]);mm.setValue(tm[1]);}catch(Exception ignored){}}
            hh.setValue("23");mm.setValue("59");

            // Attachment management
            VBox attBox = new VBox(5);
            java.util.List<AttachmentItem> editAtts = new java.util.ArrayList<>();
            try {
                java.util.List<AttachmentItem> existing = ApiClient.getHomeworkAttachments(hw.getId());
                if (existing != null) editAtts.addAll(existing);
            } catch (Exception ignored) {}
            final Runnable[] refreshAtts = {null};
            refreshAtts[0] = () -> {
                attBox.getChildren().clear();
                for (int i = 0; i < editAtts.size(); i++) {
                    AttachmentItem ai = editAtts.get(i);
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
                    AttachmentItem ai = new AttachmentItem();
                    ai.setFileName(file.getName());
                    ai.setFileType(cn.edu.sdu.sms.fx.smsfx.util.Base64Util.guessMimeType(file.getName()));
                    ai.setSize(file.length()); ai.setBase64(b64);
                    ApiClient.uploadHomeworkAttachment(hw.getId(), ai.getFileName(), ai.getFileType(), ai.getSize(), b64);
                    editAtts.add(ai); refreshAtts[0].run();
                } catch (Exception ex) { showError("上传失败: " + ex.getMessage()); }
            });
            addRow.getChildren().addAll(addBtn, new Label("(每个文件≤10MB)"));
            box.getChildren().addAll(
                new HBox(10, new Label("标题:"), titleF),
                new HBox(10, new Label("内容:"), contentF),
                new HBox(10, new Label("截止日期:"), dp),
                new HBox(10, new Label("时/分:"), new HBox(5, hh, mm)),
                new Label("附件:"), addRow, attBox
            );
            d.getDialogPane().setContent(new ScrollPane(box));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.showAndWait().ifPresent(r->{if(r==ButtonType.OK){
                String t=titleF.getText().trim();if(t.isEmpty()){showWarning("标题不能为空");return;}
                String deadline=dp.getValue()+" "+hh.getValue()+":"+mm.getValue()+":00";
                try{ApiClient.updateHomework(hw.getId(),t,contentF.getText()!=null?contentF.getText().trim():"",deadline);showInfo("修改成功");loadData();}catch(Exception ex){showError(ex.getMessage());}
            }});
        } catch(Exception e){showError(e.getMessage());}
    }

    private void initTeacherHomeworkFilters() {
        teacherFiltersReady = true;  // Set flag first to break recursion
        courseFilter.getItems().clear(); statusFilter.getItems().clear();
        courseFilter.getItems().add("全部课程"); statusFilter.getItems().addAll("全部状态","已截止","未截止");
        courseFilter.setValue("全部课程"); statusFilter.setValue("全部状态");
        try{PageResult<Course> courses=ApiClient.getTeacherCourses(1,200);if(courses!=null&&courses.getList()!=null)for(Course c:courses.getList())courseFilter.getItems().add(c.getCourseName());}catch(Exception ignored){}
        courseFilter.setOnAction(e->{currentPage=1;loadData();}); statusFilter.setOnAction(e->{currentPage=1;loadData();});
    }

    private List<Homework> applyTeacherFilters(List<Homework> list) {
        String cf=courseFilter.getValue(), sf=statusFilter.getValue();
        return list.stream().filter(h->{
            if(cf!=null&&!"全部课程".equals(cf)){
                String cn=getTeacherCourseName(h.getCourseId()); if(!cf.equals(cn)) return false;
            }
            if(sf!=null&&!"全部状态".equals(sf)){
                boolean expired=isDeadlinePassed(h.getDeadline());
                if("已截止".equals(sf)&&!expired)return false; if("未截止".equals(sf)&&expired)return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    private String getTeacherCourseName(Integer cid){try{PageResult<Course> cs=ApiClient.getTeacherCourses(1,200);if(cs!=null&&cs.getList()!=null)for(Course c:cs.getList())if(c.getId().equals(cid))return c.getCourseName();}catch(Exception ignored){}return String.valueOf(cid);}
    private boolean isDeadlinePassed(String dl){try{return java.time.LocalDateTime.now().isAfter(java.time.LocalDateTime.parse(dl.replace("T"," "),java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));}catch(Exception e){return false;}}

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
