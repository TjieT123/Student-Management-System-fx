package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
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
    @FXML private Button submitGradeBtn;

    private Integer submitId;
    private HomeworkSubmit currentSubmission;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();
    }

    public void setSubmitId(Integer submitId) {
        this.submitId = submitId;
        loadSubmissionDetail();
        submitGradeBtn.setOnAction(e -> handleGrade());
    }

    private void loadSubmissionDetail() {
        try {
            currentSubmission = ApiClient.getSubmissionDetail(submitId);
            studentNameLabel.setText(currentSubmission.getStudentName() != null ?
                    currentSubmission.getStudentName() : "未知");
            submitTimeLabel.setText(currentSubmission.getSubmitTime() != null ?
                    currentSubmission.getSubmitTime() : "");
            statusLabel.setText("GRADED".equals(currentSubmission.getStatus()) ? "已批改" : "待批改");
            contentDisplay.setText(currentSubmission.getContent() != null ?
                    currentSubmission.getContent() : "无内容");

            // 如果已批改，预填分数和评语
            if ("GRADED".equals(currentSubmission.getStatus())) {
                if (currentSubmission.getScore() != null) {
                    scoreField.setText(String.valueOf(currentSubmission.getScore()));
                }
                if (currentSubmission.getComment() != null) {
                    commentArea.setText(currentSubmission.getComment());
                }
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
