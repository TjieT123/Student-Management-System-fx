package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.PageResult;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class InnovationPracticeController extends BaseController {
    @FXML private VBox practiceList;
    @FXML private TextField titleField, orgField, roleField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker startDate, endDate;
    @FXML private TextArea descField, resultField;
    @FXML private Button submitBtn;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        typeCombo.getItems().addAll("社会实践", "学科竞赛", "科技成果", "培训讲座", "创新项目", "校外实习");
        typeCombo.setValue("社会实践");
        submitBtn.setOnAction(e -> handleSubmit());
        loadPractices();
    }

    private void handleSubmit() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) { showWarning("请输入标题"); return; }
        LocalDate sd = startDate.getValue(), ed = endDate.getValue();
        String desc = descField.getText() != null ? descField.getText().trim() : "";
        String result = resultField.getText() != null ? resultField.getText().trim() : "";
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("title", title); data.put("type", typeCombo.getValue());
            data.put("startDate", sd != null ? sd.toString() : ""); data.put("endDate", ed != null ? ed.toString() : "");
            data.put("organization", orgField.getText().trim()); data.put("role", roleField.getText().trim());
            data.put("description", desc); data.put("result", result); data.put("attachments", "[]");
            ApiClient.submitPractice(data);
            showInfo("提交成功"); loadPractices();
            titleField.clear(); orgField.clear(); roleField.clear(); descField.clear(); resultField.clear();
        } catch (Exception ex) { showError("提交失败: " + ex.getMessage()); }
    }

    private void loadPractices() {
        practiceList.getChildren().clear();
        try {
            PageResult<Map<String, Object>> result = ApiClient.getMyPractices(1, 50);
            if (result != null && result.getList() != null) {
                for (Map<String, Object> p : result.getList()) {
                    HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 10;");
                    VBox info = new VBox(4);
                    info.getChildren().add(new Label((String) p.get("title")) {{ setStyle("-fx-font-weight: bold; -fx-font-size: 14;"); }});
                    info.getChildren().add(new Label("类型: " + (p.get("type") != null ? p.get("type") : "")));
                    String st = (String) p.get("status");
                    String color = "PENDING".equals(st) ? "#f39c12" : "APPROVED".equals(st) ? "#27ae60" : "#e74c3c";
                    Label status = new Label("PENDING".equals(st) ? "待审批" : "APPROVED".equals(st) ? "已通过" : "已驳回");
                    status.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12;");
                    Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
                    card.getChildren().addAll(info, sp, status);
                    practiceList.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            practiceList.getChildren().add(new Label("加载失败: " + e.getMessage()));
        }
    }
}
