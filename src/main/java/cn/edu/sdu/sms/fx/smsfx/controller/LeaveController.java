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

public class LeaveController extends BaseController {
    @FXML private VBox leaveList;
    @FXML private ComboBox<String> leaveType;
    @FXML private DatePicker startDate, endDate;
    @FXML private TextArea leaveReason;
    @FXML private Button submitBtn;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        leaveType.getItems().addAll("事假", "病假", "参赛", "其他");
        leaveType.setValue("事假");
        submitBtn.setOnAction(e -> handleSubmit());
        loadLeaves();
    }

    private void handleSubmit() {
        String type = leaveType.getValue();
        LocalDate sd = startDate.getValue(), ed = endDate.getValue();
        String reason = leaveReason.getText() != null ? leaveReason.getText().trim() : "";
        if (sd == null || ed == null) { showWarning("请选择开始和结束日期"); return; }
        if (sd.isAfter(ed)) { showWarning("开始日期不能晚于结束日期"); return; }
        if (reason.isEmpty()) { showWarning("请填写请假事由"); return; }
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", type); data.put("startDate", sd.toString()); data.put("endDate", ed.toString());
            data.put("reason", reason); data.put("destination", "");
            ApiClient.applyLeave(data);
            showInfo("提交成功"); loadLeaves();
            startDate.setValue(null); endDate.setValue(null); leaveReason.clear();
        } catch (Exception ex) { showError("提交失败: " + ex.getMessage()); }
    }

    private void loadLeaves() {
        leaveList.getChildren().clear();
        try {
            PageResult<Map<String, Object>> result = ApiClient.getMyLeaves(1, 50);
            if (result != null && result.getList() != null) {
                for (Map<String, Object> lr : result.getList()) {
                    HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 10;");
                    String st = (String) lr.get("status"); String color = "PENDING".equals(st) ? "#f39c12" : "APPROVED".equals(st) ? "#27ae60" : "#e74c3c";
                    Label status = new Label("PENDING".equals(st) ? "待审批" : "APPROVED".equals(st) ? "已通过" : "已驳回");
                    status.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12;");
                    Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
                    card.getChildren().addAll(
                        new Label(lr.get("startDate") + " ~ " + lr.get("endDate")), sp, status);
                    leaveList.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            leaveList.getChildren().add(new Label("加载失败: " + e.getMessage()));
        }
    }
}
