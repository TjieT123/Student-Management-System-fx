package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
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
    @FXML private VBox previousSubmissionBox;
    @FXML private TextArea previousContentArea;
    @FXML private HBox gradeBox;
    @FXML private Label scoreLabel;
    @FXML private Label commentLabel;
    @FXML private Label deadlinePassedLabel;
    @FXML private VBox submitArea;
    @FXML private TextArea contentArea;
    @FXML private Button submitBtn;

    private Integer homeworkId;
    private Integer courseId;
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
        this.courseId = courseId;
        loadHomeworkInfo();
    }

    private void loadHomeworkInfo() {
        try {
            // 从学生作业列表接口获取作业信息
            PageResult<StudentHomeworkItem> result = ApiClient.getStudentHomeworkList(courseId, 1, 20);
            if (result != null && result.getList() != null) {
                for (StudentHomeworkItem hw : result.getList()) {
                    if (homeworkId.equals(hw.getId())) {
                        homeworkItem = hw;
                        break;
                    }
                }
            }

            if (homeworkItem != null) {
                hwTitleLabel.setText(homeworkItem.getTitle());
                courseLabel.setText(homeworkItem.getCourseName() != null ? homeworkItem.getCourseName() : "");
                teacherLabel.setText(homeworkItem.getTeacherName() != null ? homeworkItem.getTeacherName() : "");
                deadlineLabel.setText(homeworkItem.getDeadline() != null ? homeworkItem.getDeadline() : "");
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

        // 判断截止时间
        boolean deadlinePassed = false;
        if (homeworkItem != null && homeworkItem.getDeadline() != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime deadline = LocalDateTime.parse(homeworkItem.getDeadline(), formatter);
                deadlinePassed = LocalDateTime.now().isAfter(deadline);
            } catch (Exception ignored) {}
        }

        // 如果未提交且截止时间已过
        if ("UNSUBMIT".equals(currentStatus) && deadlinePassed) {
            deadlinePassedLabel.setVisible(true);
            deadlinePassedLabel.setManaged(true);
            submitArea.setVisible(false);
            submitArea.setManaged(false);
            return;
        }

        // 如果已批改
        if ("GRADED".equals(currentStatus)) {
            submitArea.setVisible(false);
            submitArea.setManaged(false);
            previousSubmissionBox.setVisible(true);
            previousSubmissionBox.setManaged(true);
            gradeBox.setVisible(true);
            gradeBox.setManaged(true);
            loadSubmissionDetail();
            return;
        }

        // 如果已提交或迟交
        if ("SUBMITTED".equals(currentStatus) || "LATE".equals(currentStatus)) {
            previousSubmissionBox.setVisible(true);
            previousSubmissionBox.setManaged(true);
            loadSubmissionDetail();
        }

        // 不是已批改且未过截止时间 → 显示提交区域
        if (!"GRADED".equals(currentStatus) && !deadlinePassed) {
            submitArea.setVisible(true);
            submitArea.setManaged(true);
        }
    }

    private void loadSubmissionDetail() {
        // 尝试从提交历史中获取 submission id
        // 由于 API 限制，我们从 submit list 中找
        // 简化处理：如果有 previousContent 就显示
        if (homeworkItem != null && homeworkItem.getStatus() != null
                && !"UNSUBMIT".equals(homeworkItem.getStatus())) {
            // 从提交列表查找 submission
            try {
                PageResult<HomeworkSubmit> submits = ApiClient.getSubmitList(homeworkId, 1, 100);
                if (submits != null && submits.getList() != null) {
                    String currentUserSchId = cn.edu.sdu.sms.fx.smsfx.util.SessionManager
                            .getCurrentUser().getSchId();
                    for (HomeworkSubmit hs : submits.getList()) {
                        if (currentUserSchId != null && currentUserSchId.equals(hs.getSid())) {
                            submissionId = hs.getId();
                            // 获取详情（含 content、comment、score）
                            HomeworkSubmit detail = ApiClient.getStudentSubmissionDetail(submissionId);
                            if (detail != null) {
                                previousContentArea.setText(detail.getContent() != null ?
                                        detail.getContent() : "");
                                if ("GRADED".equals(currentStatus)) {
                                    scoreLabel.setText("分数: " +
                                            (detail.getScore() != null ? detail.getScore() : "-"));
                                    commentLabel.setText("评语: " +
                                            (detail.getComment() != null ? detail.getComment() : "无"));
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
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
