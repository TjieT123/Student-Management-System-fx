package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.Map;

public class HonorViewController extends BaseController {
    @FXML private VBox contentBox;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        loadHonors();
    }

    private void loadHonors() {
        contentBox.getChildren().clear();
        try {
            List<Map<String, Object>> honors = ApiClient.getMyHonors();
            if (honors == null || honors.isEmpty()) {
                Label empty = new Label("暂无荣誉记录");
                empty.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14;");
                contentBox.getChildren().add(empty);
                return;
            }
            for (Map<String, Object> h : honors) {
                HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT);
                card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12;");
                String title = (String) h.get("title");
                String type = (String) h.get("type");
                String level = (String) h.get("level");
                Object date = h.get("awardDate");
                String desc = (String) h.get("description");
                Label titleLabel = new Label((title != null ? title : "") + "  ");
                titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
                Label typeLabel = new Label((type != null ? type : "") + " | " + (level != null ? level : ""));
                typeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
                Region spacer = new Region(); HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                Label dateLabel = new Label(date != null ? date.toString() : "");
                dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #95a5a6;");
                card.getChildren().addAll(titleLabel, typeLabel, spacer, dateLabel);
                contentBox.getChildren().add(card);
            }
        } catch (Exception e) {
            contentBox.getChildren().add(new Label("加载失败: " + e.getMessage()));
        }
    }
}
