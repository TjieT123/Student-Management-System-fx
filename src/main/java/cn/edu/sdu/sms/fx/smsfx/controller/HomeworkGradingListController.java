package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class HomeworkGradingListController extends BaseController {

    @FXML private Label homeworkTitleLabel;
    @FXML private VBox submissionContainer;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageLabel;

    private Integer homeworkId;
    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();

        prevPageBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; loadSubmissions(); } });
        nextPageBtn.setOnAction(e -> { if (currentPage < totalPages) { currentPage++; loadSubmissions(); } });
    }

    public void setHomeworkId(Integer homeworkId) {
        this.homeworkId = homeworkId;
        homeworkTitleLabel.setText("作业批改");
        loadSubmissions();
    }

    private void loadSubmissions() {
        submissionContainer.getChildren().clear();
        try {
            PageResult<HomeworkSubmit> result = ApiClient.getSubmitList(homeworkId, currentPage, 10);
            if (result != null) {
                totalPages = Math.max(1, (int) Math.ceil((double) result.getTotal() / result.getPageSize()));
                pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页");
                prevPageBtn.setDisable(currentPage <= 1);
                nextPageBtn.setDisable(currentPage >= totalPages);

                if (result.getList() != null) {
                    for (HomeworkSubmit hs : result.getList()) {
                        submissionContainer.getChildren().add(createSubmissionCard(hs));
                    }
                }
            }
        } catch (Exception e) {
            Label err = new Label("加载失败: " + e.getMessage());
            err.setStyle("-fx-text-fill: red;");
            submissionContainer.getChildren().add(err);
        }
    }

    private HBox createSubmissionCard(HomeworkSubmit hs) {
        HBox card = new HBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            NavigationManager.navigateTo("homework-grading-view.fxml",
                    (HomeworkGradingController controller) ->
                            controller.setSubmitId(hs.getId(), homeworkId));
        });

        Label nameLabel = new Label(hs.getStudentName() != null ? hs.getStudentName() : "未知");
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label statusLabel = new Label();
        String status = hs.getStatus();
        if ("GRADED".equals(status)) {
            statusLabel.setText("已批改 (" + (hs.getScore() != null ? hs.getScore() : "-") + "分)");
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
        } else {
            statusLabel.setText("待批改");
            statusLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12;");
        }
        Label timeLabel = new Label(formatDateTime(hs.getSubmitTime()));
        timeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #95a5a6;");

        card.getChildren().addAll(nameLabel, spacer, statusLabel, timeLabel);
        return card;
    }
}
