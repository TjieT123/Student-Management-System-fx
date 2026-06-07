package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.Base64Util;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HomeworkSubmitController extends BaseController {

    @FXML private Label hwTitleLabel;
    @FXML private Label courseLabel;
    @FXML private Label teacherLabel;
    @FXML private Label deadlineLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea homeworkContentArea;
    @FXML private VBox previousSubmissionBox;
    @FXML private TextArea previousContentArea;
    @FXML private Label submitTimeLabel;
    @FXML private VBox submittedAttachmentBox;
    @FXML private Separator gradeSeparator;
    @FXML private VBox gradeBox;
    @FXML private Label scoreLabel;
    @FXML private Label commentLabel;
    @FXML private Button aiSuggestionBtn;
    @FXML private VBox aiSuggestionBox;
    @FXML private Label aiSuggestionLabel;
    @FXML private Label deadlinePassedLabel;
    @FXML private VBox submitArea;
    @FXML private TextArea contentArea;
    @FXML private Button selectFileBtn;
    @FXML private VBox pendingAttachmentBox;
    @FXML private VBox homeworkAttachmentBox;
    @FXML private Button submitBtn;

    private Integer homeworkId;
    private StudentHomeworkItem homeworkItem;
    private String currentStatus;
    private Integer submissionId;
    private List<AttachmentItem> pendingAttachments = new ArrayList<>();
    private List<AttachmentItem> submittedAttachments = new ArrayList<>();

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();
        selectFileBtn.setOnAction(e -> handleSelectFile());
    }

    public void setHomeworkId(Integer homeworkId, Integer courseId) {
        this.homeworkId = homeworkId;
        loadHomeworkInfo();
    }

    private void loadHomeworkInfo() {
        try {
            homeworkItem = ApiClient.getHomeworkContent(homeworkId);
            if (homeworkItem != null) {
                hwTitleLabel.setText(homeworkItem.getTitle());
                courseLabel.setText(homeworkItem.getCourseName() != null ? homeworkItem.getCourseName() : "");
                teacherLabel.setText(homeworkItem.getTeacherName() != null ? homeworkItem.getTeacherName() : "");
                deadlineLabel.setText(formatDateTime(homeworkItem.getDeadline()));
                homeworkContentArea.setText(homeworkItem.getContent() != null
                        && !homeworkItem.getContent().isEmpty()
                        ? homeworkItem.getContent() : "暂无作业内容描述");
                // 显示教师上传的作业附件
                refreshHomeworkAttachmentList();
                currentStatus = homeworkItem.getStatus();
                updateStatusDisplay();
            }
        } catch (Exception e) {
            showError("加载作业信息失败: " + e.getMessage());
        }
        submitBtn.setOnAction(e -> handleSubmit());
    }

    private void updateStatusDisplay() {
        if (currentStatus == null) currentStatus = "UNSUBMIT";
        switch (currentStatus) {
            case "UNSUBMIT" -> { statusLabel.setText("未提交"); statusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;"); }
            case "SUBMITTED" -> { statusLabel.setText("已提交"); statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;"); }
            case "LATE" -> { statusLabel.setText("迟交"); statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); }
            case "GRADED" -> { statusLabel.setText("已批改"); statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); }
        }

        boolean deadlinePassed = false;
        if (homeworkItem != null && homeworkItem.getDeadline() != null) {
            try {
                String normalized = homeworkItem.getDeadline().replace("T", " ");
                LocalDateTime deadline = LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                deadlinePassed = LocalDateTime.now().isAfter(deadline);
            } catch (Exception e) { System.err.println("截止时间解析失败: " + homeworkItem.getDeadline()); }
        }

        if ("GRADED".equals(currentStatus)) {
            submitArea.setVisible(false); submitArea.setManaged(false);
            previousSubmissionBox.setVisible(true); previousSubmissionBox.setManaged(true);
            gradeBox.setVisible(true); gradeBox.setManaged(true);
            gradeSeparator.setVisible(true); gradeSeparator.setManaged(true);
            loadSubmissionDetail();
            return;
        }

        if (deadlinePassed) {
            submitArea.setVisible(false); submitArea.setManaged(false);
            deadlinePassedLabel.setVisible(true); deadlinePassedLabel.setManaged(true);
            if (!"UNSUBMIT".equals(currentStatus)) {
                previousSubmissionBox.setVisible(true); previousSubmissionBox.setManaged(true);
                loadSubmissionDetail();
            }
            return;
        }

        if ("SUBMITTED".equals(currentStatus) || "LATE".equals(currentStatus)) {
            previousSubmissionBox.setVisible(true); previousSubmissionBox.setManaged(true);
            loadSubmissionDetail();
        }

        submitArea.setVisible(true); submitArea.setManaged(true);
    }

    private void loadSubmissionDetail() {
        submittedAttachments.clear();
        if (homeworkItem != null && homeworkItem.getStatus() != null
                && !"UNSUBMIT".equals(homeworkItem.getStatus())) {
            try {
                HomeworkSubmit detail = ApiClient.getMySubmission(homeworkId);
                if (detail != null) {
                    submissionId = detail.getId();
                    previousContentArea.setText(detail.getContent() != null ? detail.getContent() : "无内容");
                    submitTimeLabel.setText("提交时间：" + formatDateTime(detail.getSubmitTime()));
                    if ("GRADED".equals(currentStatus)) {
                        String scoreText = detail.getScore() != null ? String.valueOf(detail.getScore()) : "-";
                        scoreLabel.setText(scoreText + " 分");
                        String color = detail.getScore() != null && detail.getScore() >= 60 ? "#27ae60" : "#e74c3c";
                        scoreLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
                        commentLabel.setText(detail.getComment() != null ? detail.getComment() : "无评语");
                        aiSuggestionBtn.setVisible(true); aiSuggestionBtn.setManaged(true);
                        aiSuggestionBox.setVisible(false); aiSuggestionBox.setManaged(false);
                        aiSuggestionBtn.setOnAction(e -> {
                            if (showConfirm("AI学习建议", "AI将读取你的提交内容，以及作业附件和提交附件中的文本、PDF、Word、Excel文件。\n其他类型的附件将被跳过。\n\n确定继续？"))
                                handleAiSuggestion(detail.getId());
                        });
                    }
                    // 显示已提交的附件
                    if (detail.getAttachments() != null && !detail.getAttachments().isEmpty()) {
                        submittedAttachments.addAll(detail.getAttachments());
                        refreshSubmittedAttachmentList();
                    }
                }
            } catch (Exception e) {
                previousContentArea.setText("加载提交详情失败: " + e.getMessage());
            }
        }
    }

    private void handleSelectFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择附件");
        File file = chooser.showOpenDialog(selectFileBtn.getScene().getWindow());
        if (file == null) return;
        try {
            String base64 = Base64Util.encodeFile(file, 5 * 1024 * 1024);
            AttachmentItem item = new AttachmentItem();
            item.setFileName(file.getName());
            item.setFileType(Base64Util.guessMimeType(file.getName()));
            item.setSize(file.length());
            item.setBase64(base64);
            pendingAttachments.add(item);
            refreshPendingAttachmentList();
        } catch (Exception e) {
            showError("读取文件失败: " + e.getMessage());
        }
    }

    private void refreshPendingAttachmentList() {
        pendingAttachmentBox.getChildren().clear();
        for (int i = 0; i < pendingAttachments.size(); i++) {
            AttachmentItem att = pendingAttachments.get(i);
            final int idx = i;
            HBox row = createAttachmentRow(att, true, () -> {
                if (showConfirm("确认删除", "确定要删除附件 \"" + att.getFileName() + "\" 吗？")) {
                    pendingAttachments.remove(idx);
                    refreshPendingAttachmentList();
                }
            }, () -> downloadAttachment(att));
            pendingAttachmentBox.getChildren().add(row);
        }
    }

    /** 显示教师上传的作业附件（只读，可下载） */
    private void refreshHomeworkAttachmentList() {
        if (homeworkItem == null || homeworkItem.getAttachments() == null
                || homeworkItem.getAttachments().isEmpty()) {
            homeworkAttachmentBox.setVisible(false);
            homeworkAttachmentBox.setManaged(false);
            return;
        }
        // 清除旧内容，保留标题 label
        if (homeworkAttachmentBox.getChildren().size() > 1) {
            homeworkAttachmentBox.getChildren().remove(1, homeworkAttachmentBox.getChildren().size());
        }
        for (AttachmentItem att : homeworkItem.getAttachments()) {
            HBox row = createAttachmentRow(att, false, null, () -> downloadAttachment(att));
            homeworkAttachmentBox.getChildren().add(row);
        }
        homeworkAttachmentBox.setVisible(true);
        homeworkAttachmentBox.setManaged(true);
    }

    private void refreshSubmittedAttachmentList() {
        submittedAttachmentBox.getChildren().clear();
        if (submittedAttachments.isEmpty()) return;
        submittedAttachmentBox.setVisible(true);
        submittedAttachmentBox.setManaged(true);
        Label title = new Label("📎 已提交附件：");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        submittedAttachmentBox.getChildren().add(title);
        boolean canDelete = !"GRADED".equals(currentStatus);
        for (int i = 0; i < submittedAttachments.size(); i++) {
            AttachmentItem att = submittedAttachments.get(i);
            final int idx = i;
            HBox row = createAttachmentRow(att, canDelete, () -> {
                if (showConfirm("确认删除", "确定要删除附件 \"" + att.getFileName() + "\" 吗？\n删除后不可恢复。")) {
                    try {
                        ApiClient.deleteSubmissionAttachment(homeworkId, idx);
                        showInfo("删除成功");
                        loadHomeworkInfo();
                    } catch (Exception ex) { showError(ex.getMessage()); }
                }
            }, () -> downloadAttachment(att));
            submittedAttachmentBox.getChildren().add(row);
        }
    }

    private HBox createAttachmentRow(AttachmentItem att, boolean showDelete,
                                      Runnable onDelete, Runnable onDownload) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(getFileIcon(att.getFileName()));
        Label name = new Label(att.getFileName());
        name.setStyle("-fx-font-size: 12;");
        Label size = new Label(att.getSizeDisplay());
        size.setStyle("-fx-font-size: 11; -fx-text-fill: #95a5a6;");
        Button downloadBtn = new Button("下载");
        downloadBtn.setStyle("-fx-font-size: 10; -fx-background-color: #3498db; -fx-text-fill: white;");
        downloadBtn.setOnAction(e -> onDownload.run());
        row.getChildren().addAll(icon, name, size, downloadBtn);
        if (showDelete) {
            Button delBtn = new Button("删除");
            delBtn.setStyle("-fx-font-size: 10; -fx-background-color: #e74c3c; -fx-text-fill: white;");
            delBtn.setOnAction(e -> onDelete.run());
            row.getChildren().add(delBtn);
        }
        return row;
    }

    private void downloadAttachment(AttachmentItem att) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("保存附件");
        chooser.setInitialFileName(att.getFileName());
        File file = chooser.showSaveDialog(submitBtn.getScene().getWindow());
        if (file == null) return;
        try {
            Base64Util.decodeToFile(att.getBase64(), file);
            showInfo("保存成功");
        } catch (Exception e) {
            showError("保存失败: " + e.getMessage());
        }
    }

    private String getFileIcon(String name) {
        if (name == null) return "📎";
        String l = name.toLowerCase();
        if (l.endsWith(".pdf")) return "📕";
        if (l.endsWith(".doc") || l.endsWith(".docx")) return "📘";
        if (l.endsWith(".xls") || l.endsWith(".xlsx")) return "📊";
        if (l.endsWith(".zip") || l.endsWith(".rar") || l.endsWith(".7z")) return "📦";
        if (l.endsWith(".jpg") || l.endsWith(".png") || l.endsWith(".jpeg")) return "🖼";
        if (l.endsWith(".java") || l.endsWith(".py") || l.endsWith(".c") || l.endsWith(".cpp")) return "💻";
        return "📎";
    }

    private void handleAiSuggestion(Integer submissionId) {
        aiSuggestionBtn.setText("生成中...");
        aiSuggestionBtn.setDisable(true);
        new Thread(() -> {
            try {
                AiSuggestionResult result = ApiClient.getAiSuggestion(submissionId);
                Platform.runLater(() -> {
                    if (result != null && result.getSuggestion() != null) {
                        aiSuggestionLabel.setText(result.getSuggestion());
                        aiSuggestionBox.setVisible(true); aiSuggestionBox.setManaged(true);
                    } else { showWarning("AI 未返回建议"); }
                    aiSuggestionBtn.setText("AI 学习建议"); aiSuggestionBtn.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("AI 建议生成失败: " + e.getMessage());
                    aiSuggestionBtn.setText("AI 学习建议"); aiSuggestionBtn.setDisable(false);
                });
            }
        }).start();
    }

    private void handleSubmit() {
        String content = contentArea.getText() != null ? contentArea.getText().trim() : "";
        if (content.isEmpty() && pendingAttachments.isEmpty()) {
            showWarning("提交内容和附件不能同时为空，请至少填写一项");
            return;
        }
        try {
            SubmitHomeworkRequest req = new SubmitHomeworkRequest(homeworkId, content);
            ApiClient.submitHomework(req);
            // 上传待提交的附件
            List<String> failedFiles = new java.util.ArrayList<>();
            for (AttachmentItem att : pendingAttachments) {
                try {
                    ApiClient.uploadSubmissionAttachment(homeworkId,
                            att.getFileName(), att.getFileType(), att.getSize(), att.getBase64());
                } catch (Exception e) {
                    failedFiles.add(att.getFileName());
                }
            }
            pendingAttachments.clear();
            refreshPendingAttachmentList();  // 刷新UI，移除旧的删除按钮
            if (!failedFiles.isEmpty()) {
                showWarning("以下附件上传失败：\n" + String.join("\n", failedFiles));
            }
            showInfo("提交成功");
            loadHomeworkInfo();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
}
