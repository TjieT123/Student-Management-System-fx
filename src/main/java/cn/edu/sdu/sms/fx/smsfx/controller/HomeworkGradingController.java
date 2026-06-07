package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.Base64Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class HomeworkGradingController extends BaseController {

    @FXML private Label studentNameLabel, teacherNameLabel, submitTimeLabel, statusLabel, hwTitleLabel;
    @FXML private TextArea contentDisplay, hwContentArea;
    @FXML private TextField scoreField;
    @FXML private TextArea commentArea;
    @FXML private Button aiGradeBtn, submitGradeBtn;
    @FXML private Label aiStatusLabel;
    @FXML private VBox hwAttachmentBox, submissionAttachmentBox;

    private Integer submitId, homeworkId;
    private HomeworkSubmit currentSubmission;

    @Override @FXML public void initialize() { super.initialize(); enableBackButton(); }

    public void setSubmitId(Integer submitId, Integer homeworkId) {
        if (submitId == null) { showError("提交ID无效"); return; }
        this.submitId = submitId; this.homeworkId = homeworkId;
        loadSubmissionDetail();
        submitGradeBtn.setOnAction(e -> handleGrade());
        aiGradeBtn.setOnAction(e -> handleAiGrade());
    }

    private void loadSubmissionDetail() {
        try {
            currentSubmission = ApiClient.getSubmissionDetail(submitId);
            studentNameLabel.setText(currentSubmission.getStudentName() != null ? currentSubmission.getStudentName() : "未知");
            teacherNameLabel.setText(currentSubmission.getTeacherName() != null ? currentSubmission.getTeacherName() : "未知");
            submitTimeLabel.setText(formatDateTime(currentSubmission.getSubmitTime()));
            statusLabel.setText("GRADED".equals(currentSubmission.getStatus()) ? "已批改" : "待批改");
            contentDisplay.setText(currentSubmission.getContent() != null ? currentSubmission.getContent() : "无内容");
            if (currentSubmission.getAttachments() != null && !currentSubmission.getAttachments().isEmpty())
                buildAttachmentList(submissionAttachmentBox, currentSubmission.getAttachments());
            // Load homework info
            if (homeworkId != null) {
                try {
                    StudentHomeworkItem hw = ApiClient.getHomeworkContent(homeworkId);
                    if (hw != null) {
                        hwTitleLabel.setText(hw.getTitle() != null ? hw.getTitle() : "");
                        hwContentArea.setText(hw.getContent() != null ? hw.getContent() : "暂无作业描述");
                        if (hw.getAttachments() != null && !hw.getAttachments().isEmpty())
                            buildAttachmentList(hwAttachmentBox, hw.getAttachments());
                    }
                } catch (Exception ignored) {}
            }
            if ("GRADED".equals(currentSubmission.getStatus())) {
                if (currentSubmission.getScore() != null) scoreField.setText(String.valueOf(currentSubmission.getScore()));
                if (currentSubmission.getComment() != null) commentArea.setText(currentSubmission.getComment());
                submitGradeBtn.setText("更改批改");
            }
        } catch (Exception e) { showError("加载提交详情失败: " + e.getMessage()); }
    }

    private void buildAttachmentList(VBox box, java.util.List<AttachmentItem> items) {
        // Keep the title label (first child), remove old attachment rows
        if (box.getChildren().size() > 1) box.getChildren().remove(1, box.getChildren().size());
        box.setVisible(true); box.setManaged(true);
        for (AttachmentItem att : items) {
            HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(new Label("📎"), new Label(att.getFileName()), new Label(att.getSizeDisplay()));
            Button dl = new Button("下载"); dl.setStyle("-fx-font-size: 10; -fx-background-color: #3498db; -fx-text-fill: white;");
            dl.setOnAction(e -> {
                FileChooser fc = new FileChooser(); fc.setInitialFileName(att.getFileName());
                File f = fc.showSaveDialog(box.getScene().getWindow());
                if (f != null) try { Base64Util.decodeToFile(att.getBase64(), f); showInfo("保存成功"); } catch (Exception ex) { showError("保存失败"); }
            });
            row.getChildren().add(dl); box.getChildren().add(row);
        }
    }

    private void handleAiGrade() {
        if (homeworkId == null) { showError("无法获取作业信息"); return; }
        boolean hasContent = currentSubmission != null && currentSubmission.getContent() != null && !currentSubmission.getContent().trim().isEmpty();
        boolean hasAttachments = currentSubmission != null && currentSubmission.getAttachments() != null && !currentSubmission.getAttachments().isEmpty();
        if (!hasContent && !hasAttachments) {
            showWarning("学生尚未提交作业内容和附件，无法进行AI判卷"); return;
        }
        if (!showConfirm("AI判卷", "AI将读取学生提交内容、作业附件以及学生提交附件中的文本、PDF、Word、Excel文件。\n其他类型的附件（如图片）将被跳过。AI结果仅供参考。\n\n确定继续？")) return;
        aiGradeBtn.setDisable(true); aiGradeBtn.setText("AI判卷中...");
        aiStatusLabel.setText("正在调用AI，预计需要5-15秒..."); aiStatusLabel.setVisible(true); aiStatusLabel.setManaged(true);
        final int hwId = homeworkId, sId = submitId;
        new Thread(() -> {
            try {
                String ht = "", hc = "";
                PageResult<Homework> hwList = ApiClient.getHomeworkList(1, 100);
                if (hwList != null && hwList.getList() != null) for (Homework hw : hwList.getList())
                    if (hwId == hw.getId().intValue()) { ht = hw.getTitle() != null ? hw.getTitle() : ""; break; }
                if (ht.isEmpty()) { Platform.runLater(() -> { showError("无法获取作业标题"); aiGradeBtn.setDisable(false); aiGradeBtn.setText("AI判卷"); aiStatusLabel.setVisible(false); aiStatusLabel.setManaged(false); }); return; }
                // 通过详情接口获取完整作业内容（列表接口不含content字段）
                try {
                    StudentHomeworkItem detail = ApiClient.getHomeworkContent(hwId);
                    if (detail != null && detail.getContent() != null) hc = detail.getContent();
                } catch (Exception ignored) {}
                AiGradeResult result = ApiClient.aiGrade(sId, hwId, ht, hc);
                Platform.runLater(() -> {
                    if (result != null) {
                        if (result.getScore() == null || result.getScore() < 0 || result.getScore() > 100) { showError("AI返回分数无效"); aiGradeBtn.setDisable(false); aiGradeBtn.setText("AI判卷"); aiStatusLabel.setVisible(false); aiStatusLabel.setManaged(false); return; }
                        scoreField.setText(String.valueOf(result.getScore()));
                        StringBuilder fb = new StringBuilder(result.getComment()!=null?result.getComment():"");
                        if(result.getHighlights()!=null&&!result.getHighlights().isEmpty()) fb.append("\n\n【亮点】\n").append(result.getHighlights());
                        if(result.getSuggestions()!=null&&!result.getSuggestions().isEmpty()) fb.append("\n\n【改进建议】\n").append(result.getSuggestions());
                        commentArea.setText(fb.toString()); aiStatusLabel.setText("AI判卷完成，请审核后提交");
                        showInfo("AI判卷完成，分数和评语已填入，请人工审核后提交批改");
                    }
                    aiGradeBtn.setDisable(false); aiGradeBtn.setText("AI判卷");
                });
            } catch (Exception e) {
                Platform.runLater(() -> { showError("AI判卷失败: " + e.getMessage()); aiGradeBtn.setDisable(false); aiGradeBtn.setText("AI判卷"); aiStatusLabel.setVisible(false); aiStatusLabel.setManaged(false); });
            }
        }).start();
    }

    private void handleGrade() {
        String scoreText = scoreField.getText().trim();
        if (scoreText.isEmpty()) { showWarning("请输入分数"); return; }
        int score;
        try { score = Integer.parseInt(scoreText); if (score < 0 || score > 100) { showWarning("分数范围为 0-100"); return; } }
        catch (NumberFormatException e) { showWarning("请输入有效的数字分数"); return; }
        String comment = commentArea.getText() != null ? commentArea.getText().trim() : "";
        if (comment.length() > 5000) { showWarning("评语不能超过5000字符"); return; }
        submitGradeBtn.setDisable(true);
        try { ApiClient.checkHomework(new CheckHomeworkRequest(submitId, score, comment)); showInfo("批改成功"); goBack(); }
        catch (Exception e) { submitGradeBtn.setDisable(false); showError(e.getMessage()); }
    }
}
