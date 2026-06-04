package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.Base64Util;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeworkPublishController extends BaseController {

    @FXML private ComboBox<Course> courseCombo;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private DatePicker deadlineDatePicker;
    @FXML private ComboBox<String> deadlineHourField;
    @FXML private ComboBox<String> deadlineMinuteField;
    @FXML private Button publishBtn;
    @FXML private Button cancelBtn;

    private Integer preselectedCourseId;
    private List<AttachmentItem> pendingAttachments = new ArrayList<>();
    private VBox publishAttachmentBox;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();

        loadTeacherCourses();

        courseCombo.setConverter(new StringConverter<Course>() {
            @Override public String toString(Course c) { return c != null ? c.getCourseName() : ""; }
            @Override public Course fromString(String s) { return null; }
        });

        // 时、分分开选择
        for (int h = 0; h < 24; h++) deadlineHourField.getItems().add(String.format("%02d", h));
        deadlineHourField.setValue("23");
        deadlineMinuteField.getItems().addAll("00", "30", "59");
        deadlineMinuteField.setValue("59");

        // 附件区域（动态添加到发布按钮上方）
        publishAttachmentBox = new VBox(5);
        Button selectFileBtn = new Button("选择附件");
        selectFileBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 12;");
        selectFileBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("选择附件");
            File file = chooser.showOpenDialog(publishBtn.getScene().getWindow());
            if (file != null) {
                try {
                    String base64 = Base64Util.encodeFile(file, 5 * 1024 * 1024);
                    AttachmentItem item = new AttachmentItem();
                    item.setFileName(file.getName());
                    item.setFileType(Base64Util.guessMimeType(file.getName()));
                    item.setSize(file.length());
                    item.setBase64(base64);
                    pendingAttachments.add(item);
                    refreshPublishAttachments();
                } catch (Exception ex) { showError("读取文件失败: " + ex.getMessage()); }
            }
        });
        publishAttachmentBox.getChildren().add(selectFileBtn);
        // publishBtn 在 HBox 按钮行中，往上找到 content VBox，把附件区插入到按钮行之前
        javafx.scene.Node btnRow = publishBtn.getParent();  // HBox（按钮行）
        if (btnRow != null && btnRow.getParent() instanceof VBox contentVBox) {
            int btnIdx = contentVBox.getChildren().indexOf(btnRow);
            VBox attachSection = new VBox(5,
                    new Label("📎 附件（可选）："), publishAttachmentBox);
            attachSection.setStyle("-fx-padding: 5 0;");
            contentVBox.getChildren().add(btnIdx, attachSection);
        }

        publishBtn.setOnAction(e -> handlePublish());
        cancelBtn.setOnAction(e -> goBack());
    }

    private void refreshPublishAttachments() {
        publishAttachmentBox.getChildren().clear();
        if (publishAttachmentBox.getChildren().isEmpty()) {
            Button selectBtn = new Button("选择附件");
            selectBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 12;");
            selectBtn.setOnAction(e -> {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("选择附件");
                File file = chooser.showOpenDialog(publishBtn.getScene().getWindow());
                if (file != null) {
                    try {
                        String base64 = Base64Util.encodeFile(file, 5 * 1024 * 1024);
                        AttachmentItem item = new AttachmentItem();
                        item.setFileName(file.getName());
                        item.setFileType(Base64Util.guessMimeType(file.getName()));
                        item.setSize(file.length());
                        item.setBase64(base64);
                        pendingAttachments.add(item);
                        refreshPublishAttachments();
                    } catch (Exception ex) { showError("读取文件失败: " + ex.getMessage()); }
                }
            });
            publishAttachmentBox.getChildren().add(selectBtn);
        }
        for (int i = 0; i < pendingAttachments.size(); i++) {
            AttachmentItem att = pendingAttachments.get(i);
            final int idx = i;
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            Label icon = new Label(getFileIcon(att.getFileName()));
            Label name = new Label(att.getFileName());
            name.setStyle("-fx-font-size: 12;");
            Label size = new Label(att.getSizeDisplay());
            size.setStyle("-fx-font-size: 11; -fx-text-fill: #95a5a6;");
            Button delBtn = new Button("删除");
            delBtn.setStyle("-fx-font-size: 10; -fx-background-color: #e74c3c; -fx-text-fill: white;");
            delBtn.setOnAction(e -> { pendingAttachments.remove(idx); refreshPublishAttachments(); });
            row.getChildren().addAll(icon, name, size, delBtn);
            publishAttachmentBox.getChildren().add(row);
        }
        // 补一个选择按钮在最下面
        Button moreBtn = new Button("+ 添加更多");
        moreBtn.setStyle("-fx-font-size: 11; -fx-background-color: transparent; -fx-text-fill: #3498db;");
        moreBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("选择附件");
            File file = chooser.showOpenDialog(publishBtn.getScene().getWindow());
            if (file != null) {
                try {
                    String base64 = Base64Util.encodeFile(file, 5 * 1024 * 1024);
                    AttachmentItem item = new AttachmentItem();
                    item.setFileName(file.getName());
                    item.setFileType(Base64Util.guessMimeType(file.getName()));
                    item.setSize(file.length());
                    item.setBase64(base64);
                    pendingAttachments.add(item);
                    refreshPublishAttachments();
                } catch (Exception ex) { showError("读取文件失败: " + ex.getMessage()); }
            }
        });
        publishAttachmentBox.getChildren().add(moreBtn);
    }

    private String getFileIcon(String name) {
        if (name == null) return "📎";
        String l = name.toLowerCase();
        if (l.endsWith(".pdf")) return "📕";
        if (l.endsWith(".doc") || l.endsWith(".docx")) return "📘";
        if (l.endsWith(".xls") || l.endsWith(".xlsx")) return "📊";
        if (l.endsWith(".zip") || l.endsWith(".rar") || l.endsWith(".7z")) return "📦";
        if (l.endsWith(".jpg") || l.endsWith(".png") || l.endsWith(".jpeg")) return "🖼";
        return "📎";
    }

    public void setPreselectedCourseId(Integer courseId) {
        this.preselectedCourseId = courseId;
    }

    private void loadTeacherCourses() {
        try {
            PageResult<Course> result = ApiClient.getTeacherCourses(1, 100);
            if (result != null && result.getList() != null) {
                courseCombo.getItems().setAll(result.getList());
                if (preselectedCourseId != null) {
                    for (Course c : result.getList()) {
                        if (preselectedCourseId.equals(c.getId())) {
                            courseCombo.setValue(c);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            showError("加载课程列表失败: " + e.getMessage());
        }
    }

    private void handlePublish() {
        Course selectedCourse = courseCombo.getValue();
        if (selectedCourse == null) { showWarning("请选择课程"); return; }
        String title = titleField.getText().trim();
        if (title.isEmpty()) { showWarning("请输入作业标题"); return; }
        String content = contentArea.getText().trim();
        if (content.isEmpty()) { showWarning("请输入作业内容"); return; }
        if (deadlineDatePicker.getValue() == null) { showWarning("请选择截止日期"); return; }
        String hour = deadlineHourField.getValue() != null ? deadlineHourField.getValue() : "23";
        String minute = deadlineMinuteField.getValue() != null ? deadlineMinuteField.getValue() : "59";

        String deadline = deadlineDatePicker.getValue().toString() + " " + hour + ":" + minute + ":00";

        // 校验截止时间不早于当前时间
        try {
            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            java.time.LocalDateTime deadlineDt = java.time.LocalDateTime.parse(deadline, fmt);
            if (deadlineDt.isBefore(java.time.LocalDateTime.now())) {
                showWarning("截止时间不能早于当前时间");
                return;
            }
        } catch (Exception e) {
            showWarning("截止时间格式错误，请重新输入");
            return;
        }

        try {
            PublishHomeworkRequest req = new PublishHomeworkRequest(selectedCourse.getId(), title, content, deadline);
            Homework published = ApiClient.publishHomework(req);
            // 上传附件，失败则回滚（删除已发布的作业）
            if (published != null && published.getId() != null && !pendingAttachments.isEmpty()) {
                List<String> failedFiles = new ArrayList<>();
                for (AttachmentItem att : pendingAttachments) {
                    try {
                        ApiClient.uploadHomeworkAttachment(published.getId(),
                                att.getFileName(), att.getFileType(), att.getSize(), att.getBase64());
                    } catch (Exception e) {
                        failedFiles.add(att.getFileName());
                    }
                }
                if (!failedFiles.isEmpty()) {
                    // 附件失败，删除已发布的作业，回滚
                    try { ApiClient.deleteHomework(published.getId()); } catch (Exception ignored) {}
                    showError("以下附件上传失败，作业已取消发布：\n" + String.join("\n", failedFiles));
                    return;  // 留在发布页面，不跳转
                }
            }
            showInfo("发布成功");
            goBack();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
}
