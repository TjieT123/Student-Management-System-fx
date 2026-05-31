package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class HomeworkGradingController extends BaseController {

    @FXML private Label studentNameLabel;
    @FXML private Label submitTimeLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea contentDisplay;
    @FXML private TextField scoreField;
    @FXML private TextArea commentArea;
    @FXML private Button aiGradeBtn;
    @FXML private Button submitGradeBtn;
    @FXML private Label aiStatusLabel;

    private Integer submitId;
    private Integer homeworkId;
    private HomeworkSubmit currentSubmission;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();
    }

    public void setSubmitId(Integer submitId, Integer homeworkId) {
        this.submitId = submitId;
        this.homeworkId = homeworkId;
        loadSubmissionDetail();
        submitGradeBtn.setOnAction(e -> handleGrade());
        aiGradeBtn.setOnAction(e -> handleAiGrade());
    }

    /**
     * AI 判卷
     */
    private void handleAiGrade() {
        // 前置校验
        if (homeworkId == null) {
            showError("无法获取作业信息，请返回上一页重新进入");
            return;
        }
        if (currentSubmission == null || currentSubmission.getContent() == null
                || currentSubmission.getContent().trim().isEmpty()) {
            showWarning("学生尚未提交作业内容，无法进行AI判卷");
            return;
        }

        if (!showConfirm("AI判卷", "将使用 AI 对学生提交进行自动评分，AI 结果仅供参考，请人工审核后提交。\n\n确定继续？")) {
            return;
        }

        aiGradeBtn.setDisable(true);
        aiGradeBtn.setText("AI判卷中...");
        aiStatusLabel.setText("正在调用AI，预计需要5-15秒...");
        aiStatusLabel.setVisible(true);
        aiStatusLabel.setManaged(true);

        final int hwId = homeworkId;
        final int sId = submitId;

        new Thread(() -> {
            try {
                // 获取作业标题和内容
                String homeworkTitle = "";
                String homeworkContent = "";
                PageResult<Homework> hwList = ApiClient.getHomeworkList(1, 100);
                if (hwList != null && hwList.getList() != null) {
                    for (Homework hw : hwList.getList()) {
                        if (hwId == hw.getId().intValue()) {
                            homeworkTitle = hw.getTitle() != null ? hw.getTitle() : "";
                            homeworkContent = hw.getContent() != null ? hw.getContent() : homeworkTitle;
                            break;
                        }
                    }
                }

                if (homeworkTitle.isEmpty()) {
                    Platform.runLater(() -> {
                        showError("无法获取作业标题，请检查作业是否存在");
                        aiGradeBtn.setDisable(false);
                        aiGradeBtn.setText("AI判卷");
                        aiStatusLabel.setVisible(false);
                        aiStatusLabel.setManaged(false);
                    });
                    return;
                }

                AiGradeResult result = ApiClient.aiGrade(sId, homeworkTitle, homeworkContent);

                Platform.runLater(() -> {
                    if (result != null) {
                        scoreField.setText(String.valueOf(result.getScore()));
                        StringBuilder fullComment = new StringBuilder();
                        fullComment.append(result.getComment() != null ? result.getComment() : "");
                        if (result.getHighlights() != null && !result.getHighlights().isEmpty()) {
                            fullComment.append("\n\n【亮点】\n").append(result.getHighlights());
                        }
                        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
                            fullComment.append("\n\n【改进建议】\n").append(result.getSuggestions());
                        }
                        commentArea.setText(fullComment.toString());
                        aiStatusLabel.setText("AI判卷完成，请审核后提交");
                        showInfo("AI判卷完成，分数和评语已填入，请人工审核后提交批改");
                    }
                    aiGradeBtn.setDisable(false);
                    aiGradeBtn.setText("AI判卷");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("404")) {
                        msg = "后端AI判卷接口尚未实现（POST /api/teacher/homework/ai-grade），请联系后端开发";
                    }
                    showError("AI判卷失败: " + msg);
                    aiGradeBtn.setDisable(false);
                    aiGradeBtn.setText("AI判卷");
                    aiStatusLabel.setVisible(false);
                    aiStatusLabel.setManaged(false);
                });
            }
        }).start();
    }

    private void loadSubmissionDetail() {
        try {
            currentSubmission = ApiClient.getSubmissionDetail(submitId);
            studentNameLabel.setText(currentSubmission.getStudentName() != null ?
                    currentSubmission.getStudentName() : "未知");
            submitTimeLabel.setText(formatDateTime(currentSubmission.getSubmitTime()));
            statusLabel.setText("GRADED".equals(currentSubmission.getStatus()) ? "已批改" : "待批改");
            contentDisplay.setText(currentSubmission.getContent() != null ?
                    currentSubmission.getContent() : "无内容");

            // 如果已批改，预填分数和评语，并更改按钮文字
            if ("GRADED".equals(currentSubmission.getStatus())) {
                if (currentSubmission.getScore() != null) {
                    scoreField.setText(String.valueOf(currentSubmission.getScore()));
                }
                if (currentSubmission.getComment() != null) {
                    commentArea.setText(currentSubmission.getComment());
                }
                submitGradeBtn.setText("更改批改");
            }
        } catch (Exception e) {
            showError("加载提交详情失败: " + e.getMessage());
        }
    }

    private void handleGrade() {
        String scoreText = scoreField.getText().trim();
        if (scoreText.isEmpty()) { showWarning("请输入分数"); return; }

        int score;
        try {
            score = Integer.parseInt(scoreText);
            if (score < 0 || score > 100) { showWarning("分数范围为 0-100"); return; }
        } catch (NumberFormatException e) {
            showWarning("请输入有效的数字分数"); return;
        }

        try {
            CheckHomeworkRequest req = new CheckHomeworkRequest(submitId, score, commentArea.getText().trim());
            ApiClient.checkHomework(req);
            showInfo("批改成功");
            goBack();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
}
