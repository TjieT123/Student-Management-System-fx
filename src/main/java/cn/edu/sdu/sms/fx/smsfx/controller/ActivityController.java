package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.PageResult;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Map;

public class ActivityController extends BaseController {
    @FXML private VBox activityContainer;
    @FXML private TextField searchField;
    @FXML private Button prevPageBtn, nextPageBtn;
    @FXML private Label pageLabel;

    private int currentPage = 1, totalPages = 1;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        searchField.textProperty().addListener((obs,o,n) -> { currentPage=1; loadActivities(); });
        prevPageBtn.setOnAction(e -> { if(currentPage>1){currentPage--;loadActivities();} });
        nextPageBtn.setOnAction(e -> { if(currentPage<totalPages){currentPage++;loadActivities();} });
        loadActivities();
    }

    private void loadActivities() {
        activityContainer.getChildren().clear();
        try {
            String kw = searchField.getText() != null ? searchField.getText().trim() : "";
            PageResult<Map<String, Object>> result = ApiClient.getStudentActivities(currentPage, 10, kw.isEmpty() ? null : kw);
            if (result != null && result.getList() != null) {
                totalPages = Math.max(1, (int) Math.ceil((double) result.getTotal() / 10));
                pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页");
                prevPageBtn.setDisable(currentPage <= 1); nextPageBtn.setDisable(currentPage >= totalPages);
                for (Map<String, Object> a : result.getList()) {
                    HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
                    VBox info = new VBox(4);
                    info.getChildren().add(new Label((String) a.get("title")) {{ setStyle("-fx-font-weight: bold; -fx-font-size: 14;"); }});
                    info.getChildren().add(new Label("地点: " + (a.get("location")!=null?a.get("location"):"") + " | 日期: " + (a.get("date")!=null?a.get("date").toString():"")));
                    Object rc = a.get("registered_count"); Object mp = a.get("max_participants");
                    int max = mp!=null ? ((Number)mp).intValue() : 0;
                    Boolean registered = (Boolean) a.get("isRegistered");
                    String statusText = registered != null && registered ? "✅ 已报名" : "📝 未报名";
                    info.getChildren().add(new Label("报名: " + (rc!=null?rc:"0") + "/" + (max>0?String.valueOf(max):"不限") + "  " + statusText));
                    Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
                    Button detailBtn = new Button("详情"); detailBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
                    detailBtn.setOnAction(e -> showDetail(a));
                    card.getChildren().addAll(info, sp, detailBtn);
                    activityContainer.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            activityContainer.getChildren().add(new Label("加载失败: " + e.getMessage()));
        }
    }

    private void showDetail(Map<String, Object> a) {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("活动详情"); d.setResizable(true);
        VBox c = new VBox(10); c.setStyle("-fx-padding: 15;");
        c.getChildren().add(new Label("📌 " + a.get("title")) {{ setStyle("-fx-font-weight: bold; -fx-font-size: 16;"); }});
        c.getChildren().add(new Label("内容: " + (a.get("content")!=null?a.get("content"):"暂无")));
        c.getChildren().add(new Label("地点: " + (a.get("location")!=null?a.get("location"):"")));
        c.getChildren().add(new Label("日期: " + (a.get("date")!=null?a.get("date").toString():"")));
        Object rc = a.get("registered_count"); Object mp = a.get("max_participants");
        int max = mp!=null ? ((Number)mp).intValue() : 0; int reg = rc!=null ? ((Number)rc).intValue() : 0;
        c.getChildren().add(new Label("报名人数: " + reg + "/" + (max>0?String.valueOf(max):"不限")));
        Boolean registered = (Boolean) a.get("isRegistered");
        Boolean full = (Boolean) a.get("isFull");
        Button actionBtn = new Button();
        if (registered != null && registered) {
            actionBtn.setText("取消报名"); actionBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            actionBtn.setOnAction(e -> { try { ApiClient.cancelActivityRegistration(((Number)a.get("id")).longValue()); d.close(); loadActivities(); } catch (Exception ex) { showError(ex.getMessage()); } });
        } else if (full != null && full) {
            actionBtn.setText("已满"); actionBtn.setDisable(true);
        } else {
            actionBtn.setText("我要报名"); actionBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            actionBtn.setOnAction(e -> { try { ApiClient.registerActivity(((Number)a.get("id")).longValue()); showInfo("报名成功"); d.close(); loadActivities(); } catch (Exception ex) { showError(ex.getMessage()); } });
        }
        c.getChildren().add(actionBtn);
        d.getDialogPane().setContent(c);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }
}
