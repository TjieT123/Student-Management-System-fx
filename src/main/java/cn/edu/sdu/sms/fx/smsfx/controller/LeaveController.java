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
import java.util.*;
import java.util.stream.Collectors;

public class LeaveController extends BaseController {
    @FXML private VBox leaveList;
    @FXML private ComboBox<String> leaveType;
    @FXML private DatePicker startDate, endDate;
    @FXML private TextArea leaveReason;
    @FXML private Button submitBtn, prevPageBtn, nextPageBtn;
    @FXML private Label pageLabel;
    @FXML private ComboBox<String> filterStatusCb;

    private int currentPage = 1, totalPages = 1;
    private List<Map<String, Object>> allLeaves = new ArrayList<>();

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        leaveType.getItems().addAll("事假", "病假", "参赛", "其他");
        leaveType.setValue("事假");
        submitBtn.setOnAction(e -> handleSubmit());
        prevPageBtn.setOnAction(e -> { if(currentPage>1){currentPage--;renderLeaves();} });
        nextPageBtn.setOnAction(e -> { if(currentPage<totalPages){currentPage++;renderLeaves();} });
        if (filterStatusCb != null) {
            filterStatusCb.getItems().addAll("全部状态","待审批","已通过","已驳回");
            filterStatusCb.setValue("全部状态");
            filterStatusCb.setOnAction(e -> { currentPage=1; renderLeaves(); });
        }
        loadAllLeaves();
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
            data.put("reason", reason);
            ApiClient.applyLeave(data);
            showInfo("提交成功"); currentPage=1; loadAllLeaves();
            startDate.setValue(null); endDate.setValue(null); leaveReason.clear();
        } catch (Exception ex) { showError("提交失败: " + ex.getMessage()); }
    }

    private void loadAllLeaves() {
        allLeaves.clear();
        try {
            PageResult<Map<String, Object>> result = ApiClient.getMyLeaves(1, 200);
            if (result != null && result.getList() != null) allLeaves = result.getList();
        } catch (Exception ignored) {}
        currentPage = 1;
        renderLeaves();
    }

    private List<Map<String, Object>> getFiltered() {
        String fs = filterStatusCb != null ? filterStatusCb.getValue() : "全部状态";
        return allLeaves.stream().filter(lr -> {
            if (fs != null && !"全部状态".equals(fs)) {
                String s = (String) lr.get("status");
                String mapped = "待审批".equals(fs) ? "PENDING" : "已通过".equals(fs) ? "APPROVED" : "REJECTED";
                return mapped.equals(s);
            }
            return true;
        }).collect(Collectors.toList());
    }

    private void renderLeaves() {
        leaveList.getChildren().clear();
        Label hint = new Label("💡 提示：点击「待审批」或「已驳回」的卡片可编辑后重新提交");
        hint.setStyle("-fx-font-size: 12; -fx-text-fill: #95a5a6; -fx-padding: 0 0 8 0;");
        leaveList.getChildren().add(hint);
        List<Map<String, Object>> filtered = getFiltered();
        totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / 10));
        pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页（共 " + filtered.size() + " 条）");
        prevPageBtn.setDisable(currentPage <= 1); nextPageBtn.setDisable(currentPage >= totalPages);
        int from = (currentPage - 1) * 10, to = Math.min(from + 10, filtered.size());
        for (int i = from; i < to; i++) {
            Map<String, Object> lr = filtered.get(i);
            HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand;");
            String st = (String) lr.get("status"); String color = "PENDING".equals(st) ? "#f39c12" : "APPROVED".equals(st) ? "#27ae60" : "#e74c3c";
            String stText = "PENDING".equals(st) ? "待审批" : "APPROVED".equals(st) ? "已通过" : "已驳回";
            Label status = new Label(stText); status.setStyle("-fx-text-fill: "+color+"; -fx-font-weight: bold; -fx-font-size: 12;");
            Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
            String dateRange = fmtDateStr(lr.get("startDate")) + " ~ " + fmtDateStr(lr.get("endDate"));
            card.getChildren().addAll(new Label(dateRange), sp, status);
            card.setOnMouseClicked(e -> {
                String curSt = (String) lr.get("status");
                if ("PENDING".equals(curSt) || "REJECTED".equals(curSt)) {
                    showEditDialog(lr);
                } else {
                    showDetail(lr);
                }
            });
            leaveList.getChildren().add(card);
        }
    }

    private void showDetail(Map<String, Object> lr) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("请假详情"); a.setHeaderText(fmtDateStr(lr.get("startDate")) + " ~ " + fmtDateStr(lr.get("endDate")));
        StringBuilder sb = new StringBuilder();
        sb.append("类型: ").append(lr.get("type")!=null?lr.get("type"):"").append("\n");
        sb.append("事由: ").append(lr.get("reason")!=null?lr.get("reason"):"").append("\n");
        String st = (String)lr.get("status");
        sb.append("状态: ").append("PENDING".equals(st)?"待审批":"APPROVED".equals(st)?"已通过":"已驳回").append("\n");
        if(lr.get("reviewComment")!=null && !lr.get("reviewComment").toString().isEmpty())
            sb.append("审批意见: ").append(lr.get("reviewComment"));
        a.setContentText(sb.toString()); a.showAndWait();
    }

    private void showEditDialog(Map<String, Object> lr) {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("编辑请假"); d.setResizable(true);
        VBox box = new VBox(10); box.setStyle("-fx-padding: 15;");
        String curSt = (String)lr.get("status");
        if ("REJECTED".equals(curSt) && lr.get("reviewComment") != null && !lr.get("reviewComment").toString().isEmpty()) {
            Label commentLabel = new Label("⚠ 审批意见: " + lr.get("reviewComment"));
            commentLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-padding: 0 0 5 0;");
            box.getChildren().add(commentLabel);
        }
        ComboBox<String> typeCb = new ComboBox<>();
        typeCb.getItems().addAll("事假", "病假", "参赛", "其他");
        typeCb.setValue((String)lr.get("type"));
        DatePicker sdF = new DatePicker(); sdF.setEditable(false);
        DatePicker edF = new DatePicker(); edF.setEditable(false);
        try { Object sd = lr.get("startDate"); if (sd != null) sdF.setValue(java.time.LocalDate.parse(sd.toString().substring(0,10))); } catch (Exception ignored) {}
        try { Object ed = lr.get("endDate"); if (ed != null) edF.setValue(java.time.LocalDate.parse(ed.toString().substring(0,10))); } catch (Exception ignored) {}
        TextArea reasonF = new TextArea((String)lr.get("reason")); reasonF.setPrefRowCount(3);
        box.getChildren().addAll(
            new HBox(10, new Label("类型:"), typeCb),
            new HBox(10, new Label("开始:"), sdF, new Label("结束:"), edF),
            new HBox(10, new Label("事由:"), reasonF)
        );
        d.getDialogPane().setContent(box);
        ButtonType resubmitBtn = new ButtonType("再次申请", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(resubmitBtn, ButtonType.CANCEL);
        d.showAndWait().ifPresent(r -> {
            if (r == resubmitBtn) {
                String reason = reasonF.getText() != null ? reasonF.getText().trim() : "";
                if (reason.isEmpty()) { showWarning("请填写请假事由"); return; }
                if (sdF.getValue() != null && edF.getValue() != null && sdF.getValue().isAfter(edF.getValue())) {
                    showWarning("开始日期不能晚于结束日期"); return;
                }
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", ((Number)lr.get("id")).longValue());
                    data.put("type", typeCb.getValue());
                    data.put("startDate", sdF.getValue() != null ? sdF.getValue().toString() : "");
                    data.put("endDate", edF.getValue() != null ? edF.getValue().toString() : "");
                    data.put("reason", reason);
                    ApiClient.updateLeave(data);
                    showInfo("修改成功，已重新提交审批"); currentPage = 1; loadAllLeaves();
                } catch (Exception ex) { showError("修改失败: " + ex.getMessage()); }
            }
        });
    }

    private String fmtDateStr(Object dt) {
        if (dt == null) return "";
        try { String s = dt.toString(); if (s.length() >= 10) s = s.substring(0,10);
            java.time.LocalDate ld = java.time.LocalDate.parse(s);
            return ld.getYear()+"年"+ld.getMonthValue()+"月"+ld.getDayOfMonth()+"日";
        } catch (Exception e) { return dt.toString(); }
    }
}
