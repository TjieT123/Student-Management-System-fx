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
import java.util.*;

public class HomeworkGradingListController extends BaseController {

    @FXML private Label homeworkTitleLabel;
    @FXML private VBox submissionContainer;
    @FXML private Button prevPageBtn, nextPageBtn, statisticsBtn;
    @FXML private Label pageLabel;

    private Integer homeworkId;
    private int currentPage = 1, totalPages = 1;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        prevPageBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; loadSubmissions(); } });
        nextPageBtn.setOnAction(e -> { if (currentPage < totalPages) { currentPage++; loadSubmissions(); } });
        statisticsBtn.setOnAction(e -> handleStatistics());
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
                prevPageBtn.setDisable(currentPage <= 1); nextPageBtn.setDisable(currentPage >= totalPages);
                if (result.getList() != null) for (HomeworkSubmit hs : result.getList())
                    submissionContainer.getChildren().add(createSubmissionCard(hs));
            }
        } catch (Exception e) {
            submissionContainer.getChildren().add(new Label("加载失败: " + e.getMessage()));
        }
    }

    private HBox createSubmissionCard(HomeworkSubmit hs) {
        HBox card = new HBox(15); card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> NavigationManager.navigateTo("homework-grading-view.fxml",
            ctrl -> ((HomeworkGradingController)ctrl).setSubmitId(hs.getId(), homeworkId)));
        Label name = new Label(hs.getStudentName() != null ? hs.getStudentName() : "未知");
        name.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label status = new Label();
        if ("GRADED".equals(hs.getStatus())) { status.setText("已批改 ("+(hs.getScore()!=null?hs.getScore():"-")+"分)"); status.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;"); }
        else { status.setText("待批改"); status.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12;"); }
        card.getChildren().addAll(name, sp, status, new Label(formatDateTime(hs.getSubmitTime())));
        return card;
    }

    private void handleStatistics() {
        statisticsBtn.setDisable(true); statisticsBtn.setText("加载中...");
        try {
            HomeworkStatistics stats = ApiClient.getHomeworkStatistics(homeworkId);
            if (stats == null) { showWarning("暂无统计数据"); return; }
            showStatisticsDialog(stats);
        } catch (Exception e) { showError("加载统计数据失败: " + e.getMessage()); }
        finally { statisticsBtn.setDisable(false); statisticsBtn.setText("统计"); }
    }

    private void showStatisticsDialog(HomeworkStatistics stats) {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("作业提交统计"); d.setResizable(true);
        VBox c = new VBox(15); c.setPadding(new Insets(20));
        HBox cards = new HBox(20); cards.setAlignment(javafx.geometry.Pos.CENTER);
        cards.getChildren().addAll(createStatCard("总人数",String.valueOf(stats.getTotalStudents()!=null?stats.getTotalStudents():0),"#2c3e50"),
            createStatCard("已提交",String.valueOf(stats.getSubmittedCount()!=null?stats.getSubmittedCount():0),"#27ae60"),
            createStatCard("未提交",String.valueOf(stats.getUnsubmittedCount()!=null?stats.getUnsubmittedCount():0),"#e74c3c"));
        VBox dist = new VBox(10); dist.setPadding(new Insets(10,0,0,0));
        Map<String,Integer> m = stats.getDistribution();
        if(m!=null){int t=stats.getSubmittedCount()!=null?stats.getSubmittedCount():1;
            dist.getChildren().addAll(createDistBar("优秀(85-100)",m.getOrDefault("excellent",0),t,"#27ae60"),
                createDistBar("良(70-84)",m.getOrDefault("good",0),t,"#3498db"),
                createDistBar("及格(60-69)",m.getOrDefault("pass",0),t,"#f39c12"),
                createDistBar("不及格(0-59)",m.getOrDefault("fail",0),t,"#e74c3c"));}
        int sub=stats.getSubmittedCount()!=null?stats.getSubmittedCount():0,unsub=stats.getUnsubmittedCount()!=null?stats.getUnsubmittedCount():0;
        if(sub+unsub>0){PieChart pie=new PieChart();pie.setLabelsVisible(true);
            PieChart.Data sD=new PieChart.Data("已提交("+sub+")",sub),uD=new PieChart.Data("未提交("+unsub+")",unsub);
            pie.setData(FXCollections.observableArrayList(sD,uD));pie.setPrefSize(350,250);
            sD.nodeProperty().addListener((obs,old,n)->{if(n!=null)n.setStyle("-fx-pie-color: #27ae60;");});
            uD.nodeProperty().addListener((obs,old,n)->{if(n!=null)n.setStyle("-fx-pie-color: #e74c3c;");});
            HBox pb=new HBox(pie);pb.setAlignment(javafx.geometry.Pos.CENTER);
            c.getChildren().addAll(cards,new Separator(),pb,new Separator(),dist);}
        else c.getChildren().addAll(cards,new Separator(),dist);
        // 查看未提交名单按钮
        if (unsub > 0) {
            Button viewUnsubBtn = new Button("查看未提交名单（" + unsub + " 人）");
            viewUnsubBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            viewUnsubBtn.setOnAction(e -> {
                try {
                    List<Map<String, Object>> list = ApiClient.getUnsubmittedStudents(homeworkId);
                    showUnsubmittedDialog(list);
                } catch (Exception ex) { showError("加载未提交名单失败: " + ex.getMessage()); }
            });
            c.getChildren().add(viewUnsubBtn);
        }
        d.getDialogPane().setContent(c);d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);d.showAndWait();
    }
    private VBox createStatCard(String l,String v,String clr){VBox b=new VBox(5);b.setAlignment(javafx.geometry.Pos.CENTER);b.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");b.setPrefWidth(130);b.getChildren().addAll(new Label(v){{setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: "+clr+";");}},new Label(l){{setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");}});return b;}
    private HBox createDistBar(String l,int cnt,int t,String clr){HBox b=new HBox(10);b.setAlignment(javafx.geometry.Pos.CENTER_LEFT);Label nl=new Label(l);nl.setPrefWidth(140);nl.setStyle("-fx-font-size: 13; -fx-text-fill: #555;");HBox bc=new HBox();bc.setPrefHeight(22);HBox.setHgrow(bc,Priority.ALWAYS);double r=t>0?(double)cnt/t:0;Region f=new Region();f.setStyle("-fx-background-color: "+clr+"; -fx-background-radius: 3 0 0 3;");f.setPrefHeight(22);f.prefWidthProperty().bind(bc.widthProperty().multiply(r));Region g=new Region();g.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 0 3 3 0;");g.setPrefHeight(22);HBox.setHgrow(g,Priority.ALWAYS);bc.getChildren().addAll(f,g);b.getChildren().addAll(nl,bc,new Label(cnt+" 人"){{setPrefWidth(50);setStyle("-fx-font-size: 13; -fx-text-fill: "+clr+"; -fx-font-weight: bold;");}});return b;}

    private void showUnsubmittedDialog(List<Map<String, Object>> unsubmitted) {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("未提交名单"); d.setResizable(true);
        d.setHeaderText("未提交学生（共 " + unsubmitted.size() + " 人）");
        int pageSize = 10;
        int totalPages = Math.max(1, (int) Math.ceil((double) unsubmitted.size() / pageSize));
        int[] cp = {1};

        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> sidCol = new TableColumn<>("学号");
        sidCol.setCellValueFactory(d2 -> new javafx.beans.property.SimpleStringProperty((String)d2.getValue().get("sid")));
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(d2 -> new javafx.beans.property.SimpleStringProperty((String)d2.getValue().get("name")));
        TableColumn<Map<String, Object>, String> majorCol = new TableColumn<>("专业");
        majorCol.setCellValueFactory(d2 -> new javafx.beans.property.SimpleStringProperty((String)d2.getValue().get("major")));
        TableColumn<Map<String, Object>, String> classCol = new TableColumn<>("班级");
        classCol.setCellValueFactory(d2 -> new javafx.beans.property.SimpleStringProperty(d2.getValue().get("s_class") != null ? String.valueOf(d2.getValue().get("s_class")) : ""));
        table.getColumns().addAll(sidCol, nameCol, majorCol, classCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(25);
        table.setPrefHeight(278);

        Runnable load = () -> {
            int from = (cp[0] - 1) * pageSize;
            int to = Math.min(from + pageSize, unsubmitted.size());
            table.setItems(FXCollections.observableArrayList(unsubmitted.subList(from, to)));
        };
        load.run();

        HBox pag = new HBox(10); pag.setAlignment(javafx.geometry.Pos.CENTER);
        Button prev = new Button("上一页"); Button next = new Button("下一页");
        Label pl = new Label("第 " + cp[0] + "/" + totalPages + " 页");
        prev.setDisable(true); next.setDisable(totalPages <= 1);
        Runnable refreshPag = () -> { load.run(); pl.setText("第 " + cp[0] + "/" + totalPages + " 页"); prev.setDisable(cp[0] <= 1); next.setDisable(cp[0] >= totalPages); };
        prev.setOnAction(e -> { if (cp[0] > 1) { cp[0]--; refreshPag.run(); } });
        next.setOnAction(e -> { if (cp[0] < totalPages) { cp[0]++; refreshPag.run(); } });
        pag.getChildren().addAll(prev, pl, next);

        VBox content = new VBox(10, table, pag); content.setPadding(new Insets(10));
        d.getDialogPane().setContent(content); d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }
}
