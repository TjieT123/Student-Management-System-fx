package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    @FXML private Separator gradeSeparator;
    @FXML private VBox gradeBox;
    @FXML private Label scoreLabel;
    @FXML private Label commentLabel;
    @FXML private Label deadlinePassedLabel;
    @FXML private VBox submitArea;
    @FXML private TextArea contentArea;
    @FXML private Button submitBtn;

    private Integer homeworkId;
    private StudentHomeworkItem homeworkItem;
    private String currentStatus;
    private Integer submissionId;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();
    }

    public void setHomeworkId(Integer homeworkId, Integer courseId) {
        this.homeworkId = homeworkId;
        loadHomeworkInfo();
    }

    private void loadHomeworkInfo() {
        try {
            // 使用新接口直接获取作业内容+状态
            homeworkItem = ApiClient.getHomeworkContent(homeworkId);

            if (homeworkItem != null) {
                hwTitleLabel.setText(homeworkItem.getTitle());
                courseLabel.setText(homeworkItem.getCourseName() != null ? homeworkItem.getCourseName() : "");
                teacherLabel.setText(homeworkItem.getTeacherName() != null ? homeworkItem.getTeacherName() : "");
                deadlineLabel.setText(formatDateTime(homeworkItem.getDeadline()));
                homeworkContentArea.setText(homeworkItem.getContent() != null
                        && !homeworkItem.getContent().isEmpty()
                        ? homeworkItem.getContent() : "暂无作业内容描述");
                currentStatus = homeworkItem.getStatus();
                updateStatusDisplay();
            }
        } catch (Exception e) {
            showError("加载作业信息失败: " + e.getMessage());
        }

        // 提交按钮
        submitBtn.setOnAction(e -> handleSubmit());
    }

    private void updateStatusDisplay() {
        if (currentStatus == null) currentStatus = "UNSUBMIT";

        switch (currentStatus) {
            case "UNSUBMIT":
                statusLabel.setText("未提交");
                statusLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
                break;
            case "SUBMITTED":
                statusLabel.setText("已提交");
                statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                break;
            case "LATE":
                statusLabel.setText("迟交");
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                break;
            case "GRADED":
                statusLabel.setText("已批改");
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                break;
        }

        // 判断截止时间（兼容 ISO 格式 T 分隔符和空格分隔符）
        boolean deadlinePassed = false;
        if (homeworkItem != null && homeworkItem.getDeadline() != null) {
            try {
                String normalized = homeworkItem.getDeadline().replace("T", " ");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime deadline = LocalDateTime.parse(normalized, formatter);
                deadlinePassed = LocalDateTime.now().isAfter(deadline);
            } catch (Exception e) {
                System.err.println("截止时间解析失败: " + homeworkItem.getDeadline());
            }
        }

        // 已批改：显示提交内容+批改结果，隐藏提交区
        if ("GRADED".equals(currentStatus)) {
            submitArea.setVisible(false);
            submitArea.setManaged(false);
            previousSubmissionBox.setVisible(true);
            previousSubmissionBox.setManaged(true);
            gradeBox.setVisible(true);
            gradeBox.setManaged(true);
            gradeSeparator.setVisible(true);
            gradeSeparator.setManaged(true);
            loadSubmissionDetail();
            return;
        }

        // 截止时间已过 → 不允许提交/修改
        if (deadlinePassed) {
            submitArea.setVisible(false);
            submitArea.setManaged(false);
            deadlinePassedLabel.setVisible(true);
            deadlinePassedLabel.setManaged(true);
            if (!"UNSUBMIT".equals(currentStatus)) {
                // 已提交/迟交的，仍显示已提交内容
                previousSubmissionBox.setVisible(true);
                previousSubmissionBox.setManaged(true);
                gradeBox.setVisible(false);
                gradeBox.setManaged(false);
                gradeSeparator.setVisible(false);
                gradeSeparator.setManaged(false);
                loadSubmissionDetail();
            }
            return;
        }

        // 截止前：已提交或迟交 → 显示提交内容 + 提交区（允许覆盖提交）
        if ("SUBMITTED".equals(currentStatus) || "LATE".equals(currentStatus)) {
            previousSubmissionBox.setVisible(true);
            previousSubmissionBox.setManaged(true);
            gradeBox.setVisible(false);
            gradeBox.setManaged(false);
            gradeSeparator.setVisible(false);
            gradeSeparator.setManaged(false);
            loadSubmissionDetail();
        }

        // 截止前、未批改 → 显示提交区域
        submitArea.setVisible(true);
        submitArea.setManaged(true);
    }

    private void loadSubmissionDetail() {
        if (homeworkItem != null && homeworkItem.getStatus() != null
                && !"UNSUBMIT".equals(homeworkItem.getStatus())) {
            try {
                // 使用学生专用接口获取自己的提交记录
                HomeworkSubmit detail = ApiClient.getMySubmission(homeworkId);
                if (detail != null) {
                    submissionId = detail.getId();
                    // 提交内容
                    previousContentArea.setText(detail.getContent() != null ?
                            detail.getContent() : "无内容");
                    // 提交时间
                    submitTimeLabel.setText("提交时间：" + formatDateTime(detail.getSubmitTime()));
                    // 批改结果（仅已批改时显示）
                    if ("GRADED".equals(currentStatus)) {
                        String scoreText = detail.getScore() != null ?
                                String.valueOf(detail.getScore()) : "-";
                        scoreLabel.setText(scoreText + " 分");
                        String color = detail.getScore() != null && detail.getScore() >= 60 ?
                                "#27ae60" : "#e74c3c";
                        scoreLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
                        commentLabel.setText(detail.getComment() != null ?
                                detail.getComment() : "无评语");
                    }
                }
            } catch (Exception e) {
                previousContentArea.setText("加载提交详情失败: " + e.getMessage());
                System.err.println("加载学生提交详情失败: " + e.getMessage());
            }
        }
    }

    private void handleSubmit() {
        String content = contentArea.getText().trim();
        if (content.isEmpty()) { showWarning("请输入作业内容"); return; }

        try {
            SubmitHomeworkRequest req = new SubmitHomeworkRequest(homeworkId, content);
            ApiClient.submitHomework(req);
            showInfo("提交成功");
            // 刷新页面
            loadHomeworkInfo();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
}
