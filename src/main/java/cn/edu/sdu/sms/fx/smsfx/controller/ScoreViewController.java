package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.util.Map;

public class ScoreViewController extends BaseController {
    @FXML private Label gpaLabel;
    @FXML private VBox scoresContainer;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        loadScores();
    }

    private void loadScores() {
        try {
            Map<String, Object> gpa = ApiClient.getMyGPA();
            if (gpa != null) {
                Object gpaVal = gpa.get("gpa");
                gpaLabel.setText("GPA: " + (gpaVal != null ? String.format("%.2f", ((Number)gpaVal).doubleValue()) : "-"));
            }
            java.util.List<Map<String, Object>> scores = ApiClient.getMyScores();
            TableView<Map<String, Object>> table = new TableView<>();
            TableColumn<Map<String, Object>, String> cn = new TableColumn<>("课程"); cn.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("courseName")));
            TableColumn<Map<String, Object>, String> ct = new TableColumn<>("类型"); ct.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String)d.getValue().get("courseType")));
            TableColumn<Map<String, Object>, String> cr = new TableColumn<>("学分"); cr.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().get("credits") != null ? String.valueOf(d.getValue().get("credits")) : "3.0"));
            TableColumn<Map<String, Object>, String> sc = new TableColumn<>("成绩");
            sc.setCellValueFactory(d -> { Object s = d.getValue().get("finalScore"); return new javafx.beans.property.SimpleStringProperty(s != null ? String.format("%.1f", ((Number)s).doubleValue()) : "暂无成绩"); });
            TableColumn<Map<String, Object>, String> gp = new TableColumn<>("绩点");
            gp.setCellValueFactory(d -> { Object s = d.getValue().get("gradePoint"); return new javafx.beans.property.SimpleStringProperty(s != null ? String.format("%.1f", ((Number)s).doubleValue()) : "-"); });
            table.getColumns().addAll(cn, ct, cr, sc, gp);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.getItems().addAll(scores);
            scoresContainer.getChildren().add(table);
        } catch (Exception e) {
            gpaLabel.setText("加载失败: " + e.getMessage());
        }
    }
}
