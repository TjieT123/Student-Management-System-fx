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

import java.util.Map;

public class HomeworkGradingListController extends BaseController {

    @FXML private Label homeworkTitleLabel;
    @FXML private VBox submissionContainer;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageLabel;
    @FXML private Button statisticsBtn;

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
        statisticsBtn.setOnAction(e -> handleStatistics());
    }

    public void setHomeworkId(Integer homeworkId) {
        if (homeworkId == null) { showError("作业ID无效"); return; }
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

    private void handleStatistics() {
        statisticsBtn.setDisable(true);
        statisticsBtn.setText("加载中...");
        try {
            HomeworkStatistics stats = ApiClient.getHomeworkStatistics(homeworkId);
            if (stats == null) {
                showWarning("暂无统计数据");
                return;
            }
            showStatisticsDialog(stats);
        } catch (Exception e) {
            showError("加载统计数据失败: " + e.getMessage());
        } finally {
            statisticsBtn.setDisable(false);
            statisticsBtn.setText("统计");
        }
    }

    private void showStatisticsDialog(HomeworkStatistics stats) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("作业提交统计");
        dialog.setHeaderText("提交概览");
        dialog.setResizable(true);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // 数字卡片区
        HBox cards = new HBox(20);
        cards.setAlignment(javafx.geometry.Pos.CENTER);
        cards.getChildren().addAll(
                createStatCard("总人数", String.valueOf(stats.getTotalStudents() != null ? stats.getTotalStudents() : 0), "#2c3e50"),
                createStatCard("已提交", String.valueOf(stats.getSubmittedCount() != null ? stats.getSubmittedCount() : 0), "#27ae60"),
                createStatCard("未提交", String.valueOf(stats.getUnsubmittedCount() != null ? stats.getUnsubmittedCount() : 0), "#e74c3c")
        );

        // 分数段分布
        VBox distBox = new VBox(10);
        distBox.setPadding(new Insets(10, 0, 0, 0));
        Label distTitle = new Label("分数段分布");
        distTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Map<String, Integer> dist = stats.getDistribution();
        if (dist != null) {
            int submittedTotal = stats.getSubmittedCount() != null ? stats.getSubmittedCount() : 1;
            distBox.getChildren().add(distTitle);
            distBox.getChildren().add(createDistBar("优秀 (85-100)", dist.getOrDefault("excellent", 0), submittedTotal, "#27ae60"));
            distBox.getChildren().add(createDistBar("良   (70-84)",   dist.getOrDefault("good", 0),      submittedTotal, "#3498db"));
            distBox.getChildren().add(createDistBar("及格 (60-69)",  dist.getOrDefault("pass", 0),      submittedTotal, "#f39c12"));
            distBox.getChildren().add(createDistBar("不及格 (0-59)", dist.getOrDefault("fail", 0),      submittedTotal, "#e74c3c"));
        }

        // 饼状图：已提交 vs 未提交
        int submitted = stats.getSubmittedCount() != null ? stats.getSubmittedCount() : 0;
        int unsubmitted = stats.getUnsubmittedCount() != null ? stats.getUnsubmittedCount() : 0;
        if (submitted + unsubmitted > 0) {
            PieChart pieChart = new PieChart();
            pieChart.setTitle("提交情况");
            pieChart.setLabelsVisible(true);
            PieChart.Data submittedSlice = new PieChart.Data("已提交 (" + submitted + ")", submitted);
            PieChart.Data unsubmittedSlice = new PieChart.Data("未提交 (" + unsubmitted + ")", unsubmitted);
            pieChart.setData(FXCollections.observableArrayList(submittedSlice, unsubmittedSlice));
            pieChart.setPrefSize(350, 250);
            // 监听节点创建时机，节点就绪后设置颜色
            submittedSlice.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) node.setStyle("-fx-pie-color: #27ae60;");
            });
            unsubmittedSlice.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) node.setStyle("-fx-pie-color: #e74c3c;");
            });

            HBox pieBox = new HBox(pieChart);
            pieBox.setAlignment(javafx.geometry.Pos.CENTER);
            content.getChildren().addAll(cards, new Separator(), pieBox, new Separator(), distBox);
        } else {
            content.getChildren().addAll(cards, new Separator(), distBox);
        }
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        card.setPrefWidth(130);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(valueLabel, textLabel);
        return card;
    }

    private HBox createDistBar(String label, int count, int total, String color) {
        HBox bar = new HBox(10);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(label);
        nameLabel.setPrefWidth(140);
        nameLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #555;");

        // 进度条：彩色填充 + 灰色剩余，用 HBox 精确控制宽度
        HBox barContainer = new HBox();
        barContainer.setPrefHeight(22);
        barContainer.setMinWidth(0);
        HBox.setHgrow(barContainer, javafx.scene.layout.Priority.ALWAYS);

        double ratio = total > 0 ? (double) count / total : 0;

        Region fill = new Region();
        fill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 3 0 0 3;");
        fill.setPrefHeight(22);
        fill.setMinWidth(0);
        fill.prefWidthProperty().bind(barContainer.widthProperty().multiply(ratio));

        Region gray = new Region();
        gray.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 0 3 3 0;");
        gray.setPrefHeight(22);
        gray.setMinWidth(0);
        HBox.setHgrow(gray, javafx.scene.layout.Priority.ALWAYS);

        barContainer.getChildren().addAll(fill, gray);

        // 右侧显示人数
        Label countLabel = new Label(count + " 人");
        countLabel.setPrefWidth(50);
        countLabel.setStyle("-fx-font-size: 13; -fx-text-fill: " + color + "; -fx-font-weight: bold;");

        bar.getChildren().addAll(nameLabel, barContainer, countLabel);
        return bar;
    }
}
