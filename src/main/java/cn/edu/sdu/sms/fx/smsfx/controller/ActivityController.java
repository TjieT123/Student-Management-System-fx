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

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        loadActivities();
    }

    private void loadActivities() {
        activityContainer.getChildren().clear();
        try {
            PageResult<Map<String, Object>> result = ApiClient.getStudentActivities(1, 50);
            if (result != null && result.getList() != null) {
                for (Map<String, Object> a : result.getList()) {
                    HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12;");
                    VBox info = new VBox(4);
                    info.getChildren().add(new Label((String) a.get("title")) {{ setStyle("-fx-font-weight: bold; -fx-font-size: 14;"); }});
                    info.getChildren().add(new Label("地点: " + (a.get("location")!=null?a.get("location"):"") + " | 日期: " + (a.get("date")!=null?a.get("date").toString():"")));
                    Object rc = a.get("registered_count"); Object mp = a.get("maxParticipants");
                    info.getChildren().add(new Label("报名: " + (rc!=null?rc:"0") + "/" + (mp!=null&&(Integer)mp>0?mp:"不限")));
                    Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
                    Boolean registered = (Boolean) a.get("isRegistered");
                    Boolean full = (Boolean) a.get("isFull");
                    Button actionBtn = new Button();
                    if (registered != null && registered) {
                        actionBtn.setText("取消报名"); actionBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        actionBtn.setOnAction(e -> {
                            try { ApiClient.cancelActivityRegistration(((Number)a.get("id")).longValue()); loadActivities(); }
                            catch (Exception ex) { showError(ex.getMessage()); }
                        });
                    } else if (full != null && full) {
                        actionBtn.setText("已满"); actionBtn.setDisable(true);
                    } else {
                        actionBtn.setText("报名"); actionBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                        actionBtn.setOnAction(e -> {
                            try { ApiClient.registerActivity(((Number)a.get("id")).longValue()); showInfo("报名成功"); loadActivities(); }
                            catch (Exception ex) { showError(ex.getMessage()); }
                        });
                    }
                    card.getChildren().addAll(info, sp, actionBtn);
                    activityContainer.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            activityContainer.getChildren().add(new Label("加载失败: " + e.getMessage()));
        }
    }
}
