package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.AttachmentItem;
import cn.edu.sdu.sms.fx.smsfx.models.PageResult;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.Base64Util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InnovationPracticeController extends BaseController {
    @FXML private VBox practiceList;
    @FXML private TextField titleField, orgField, roleField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker startDate, endDate;
    @FXML private TextArea descField, resultField;
    @FXML private Button submitBtn, prevPageBtn, nextPageBtn;
    @FXML private Label pageLabel;
    @FXML private VBox attachmentBox;
    @FXML private TextField searchTitleF;
    @FXML private ComboBox<String> filterTypeCb, filterStatusCb;
    @FXML private Button searchBtn;

    private int currentPage = 1, totalPages = 1;
    private List<AttachmentItem> currentAttachments = new ArrayList<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private List<Map<String, Object>> allPractices = new ArrayList<>();

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        typeCombo.getItems().addAll("社会实践", "学科竞赛", "科技成果", "培训讲座", "创新项目", "校外实习");
        typeCombo.setValue("社会实践");
        submitBtn.setOnAction(e -> handleSubmit());
        prevPageBtn.setOnAction(e -> { if(currentPage>1){currentPage--;renderPractices();} });
        nextPageBtn.setOnAction(e -> { if(currentPage<totalPages){currentPage++;renderPractices();} });
        // Filter controls
        if (filterTypeCb != null) {
            filterTypeCb.getItems().addAll("全部类型","社会实践","学科竞赛","科技成果","培训讲座","创新项目","校外实习");
            filterTypeCb.setValue("全部类型");
            filterTypeCb.setOnAction(e -> { currentPage=1; renderPractices(); });
        }
        if (filterStatusCb != null) {
            filterStatusCb.getItems().addAll("全部状态","待审批","已通过","已驳回");
            filterStatusCb.setValue("全部状态");
            filterStatusCb.setOnAction(e -> { currentPage=1; renderPractices(); });
        }
        if (searchBtn != null) searchBtn.setOnAction(e -> { currentPage=1; renderPractices(); });
        refreshAttachmentDisplay();
        loadAllPractices();
    }

    @FXML private void handleAddAttachment() {
        FileChooser fc = new FileChooser(); fc.setTitle("选择附件");
        File file = fc.showOpenDialog(null);
        if (file == null) return;
        if (file.length() > 5 * 1024 * 1024) { showWarning("文件不能超过5MB"); return; }
        try {
            String base64 = Base64Util.encodeFile(file);
            AttachmentItem ai = new AttachmentItem();
            ai.setFileName(file.getName()); ai.setFileType(getFileType(file.getName()));
            ai.setSize(file.length()); ai.setBase64(base64);
            currentAttachments.add(ai);
            refreshAttachmentDisplay();
        } catch (Exception ex) { showError("读取文件失败: " + ex.getMessage()); }
    }

    private void refreshAttachmentDisplay() {
        if (attachmentBox == null) return;
        attachmentBox.getChildren().clear();
        for (int i = 0; i < currentAttachments.size(); i++) {
            AttachmentItem ai = currentAttachments.get(i);
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().add(new Label("📎 " + ai.getFileName() + " (" + (ai.getSize()/1024) + "KB)"));
            Button del = new Button("删除");
            final int idx = i;
            del.setOnAction(e -> { currentAttachments.remove(idx); refreshAttachmentDisplay(); });
            row.getChildren().add(del);
            attachmentBox.getChildren().add(row);
        }
    }

    private void handleSubmit() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) { showWarning("请输入标题"); return; }
        LocalDate sd = startDate.getValue(), ed = endDate.getValue();
        if (sd != null && ed != null && sd.isAfter(ed)) { showWarning("开始日期不能晚于结束日期"); return; }
        String desc = descField.getText() != null ? descField.getText().trim() : "";
        String result = resultField.getText() != null ? resultField.getText().trim() : "";
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("title", title); data.put("type", typeCombo.getValue());
            data.put("startDate", sd != null ? sd.toString() : ""); data.put("endDate", ed != null ? ed.toString() : "");
            data.put("organization", orgField.getText().trim()); data.put("role", roleField.getText().trim());
            data.put("description", desc); data.put("result", result);
            data.put("attachments", mapper.writeValueAsString(currentAttachments));
            ApiClient.submitPractice(data);
            showInfo("提交成功"); currentPage=1; currentAttachments.clear(); loadAllPractices();
            titleField.clear(); orgField.clear(); roleField.clear(); descField.clear(); resultField.clear();
            refreshAttachmentDisplay();
        } catch (Exception ex) { showError("提交失败: " + ex.getMessage()); }
    }

    private void loadAllPractices() {
        allPractices.clear();
        try {
            PageResult<Map<String, Object>> r = ApiClient.getMyPractices(1, 200);
            if (r != null && r.getList() != null) allPractices = r.getList();
        } catch (Exception ignored) {}
        currentPage = 1;
        renderPractices();
    }

    private List<Map<String, Object>> getFiltered() {
        String kw = searchTitleF != null ? searchTitleF.getText().trim() : "";
        String ft = filterTypeCb != null ? filterTypeCb.getValue() : "全部类型";
        String fs = filterStatusCb != null ? filterStatusCb.getValue() : "全部状态";
        return allPractices.stream().filter(p -> {
            if (!kw.isEmpty()) {
                String t = (String) p.get("title");
                if (t == null || !t.toLowerCase().contains(kw.toLowerCase())) return false;
            }
            if (ft != null && !"全部类型".equals(ft)) {
                String t = (String) p.get("type");
                if (!ft.equals(t)) return false;
            }
            if (fs != null && !"全部状态".equals(fs)) {
                String s = (String) p.get("status");
                String mapped = "待审批".equals(fs) ? "PENDING" : "已通过".equals(fs) ? "APPROVED" : "REJECTED";
                if (!mapped.equals(s)) return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    private void renderPractices() {
        practiceList.getChildren().clear();
        List<Map<String, Object>> filtered = getFiltered();
        totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / 10));
        pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页（共 " + filtered.size() + " 条）");
        prevPageBtn.setDisable(currentPage <= 1); nextPageBtn.setDisable(currentPage >= totalPages);
        int from = (currentPage - 1) * 10, to = Math.min(from + 10, filtered.size());
        for (int i = from; i < to; i++) {
            Map<String, Object> p = filtered.get(i);
            HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand;");
            VBox info = new VBox(4);
            info.getChildren().add(new Label((String) p.get("title")) {{ setStyle("-fx-font-weight: bold; -fx-font-size: 14;"); }});
            info.getChildren().add(new Label("类型: " + (p.get("type") != null ? p.get("type") : "")));
            String st = (String) p.get("status");
            String color = "PENDING".equals(st) ? "#f39c12" : "APPROVED".equals(st) ? "#27ae60" : "#e74c3c";
            String stText = "PENDING".equals(st) ? "待审批" : "APPROVED".equals(st) ? "已通过" : "已驳回";
            Label status = new Label(stText); status.setStyle("-fx-text-fill: "+color+"; -fx-font-weight: bold; -fx-font-size: 12;");
            Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
            card.getChildren().addAll(info, sp, status);
            card.setOnMouseClicked(e -> {
                String curSt = (String) p.get("status");
                if ("PENDING".equals(curSt) || "REJECTED".equals(curSt)) {
                    showEditDialog(p);
                } else {
                    showDetail(p);
                }
            });
            practiceList.getChildren().add(card);
        }
    }

    private void showDetail(Map<String, Object> p) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("实践详情"); a.setHeaderText((String)p.get("title")); a.setResizable(true);
        StringBuilder sb = new StringBuilder();
        sb.append("类型: ").append(p.get("type")!=null?p.get("type"):"").append("\n");
        sb.append("开始日期: ").append(fmtDateStr(p.get("startDate"))).append("\n");
        sb.append("结束日期: ").append(fmtDateStr(p.get("endDate"))).append("\n");
        sb.append("组织单位: ").append(p.get("organization")!=null?p.get("organization"):"").append("\n");
        sb.append("担任角色: ").append(p.get("role")!=null?p.get("role"):"").append("\n");
        sb.append("描述: ").append(p.get("description")!=null?p.get("description"):"").append("\n");
        sb.append("成果: ").append(p.get("result")!=null?p.get("result"):"").append("\n");
        String st = (String)p.get("status");
        sb.append("状态: ").append("PENDING".equals(st)?"待审批":"APPROVED".equals(st)?"已通过":"已驳回");
        if (p.get("reviewComment") != null && !p.get("reviewComment").toString().isEmpty())
            sb.append("\n审批意见: ").append(p.get("reviewComment"));
        try {
            String attStr = (String) p.get("attachments");
            if (attStr != null && !attStr.isEmpty() && !"[]".equals(attStr)) {
                List<Map<String, Object>> atts = mapper.readValue(attStr, new TypeReference<List<Map<String, Object>>>() {});
                sb.append("\n\n附件:");
                for (int i = 0; i < atts.size(); i++) {
                    sb.append("\n  ").append(i+1).append(". ").append(atts.get(i).get("fileName"));
                }
            }
        } catch (Exception ignored) {}
        a.setContentText(sb.toString());
        a.showAndWait();
    }

    private void showEditDialog(Map<String, Object> p) {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("编辑实践 - " + (p.get("title")!=null?p.get("title"):"")); d.setResizable(true);
        VBox box = new VBox(10); box.setStyle("-fx-padding: 15;");
        // Show admin review comment if rejected
        String curSt = (String)p.get("status");
        if ("REJECTED".equals(curSt) && p.get("reviewComment") != null && !p.get("reviewComment").toString().isEmpty()) {
            Label commentLabel = new Label("⚠ 审批意见: " + p.get("reviewComment"));
            commentLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-padding: 0 0 5 0;");
            box.getChildren().add(commentLabel);
        }
        TextField titleF = new TextField((String)p.get("title"));
        ComboBox<String> typeCb = new ComboBox<>();
        typeCb.getItems().addAll("社会实践", "学科竞赛", "科技成果", "培训讲座", "创新项目", "校外实习");
        typeCb.setValue((String)p.get("type"));
        DatePicker sdF = new DatePicker(); sdF.setEditable(false);
        DatePicker edF = new DatePicker(); edF.setEditable(false);
        try { Object sd = p.get("startDate"); if (sd != null) sdF.setValue(java.time.LocalDate.parse(sd.toString().substring(0,10))); } catch (Exception ignored) {}
        try { Object ed = p.get("endDate"); if (ed != null) edF.setValue(java.time.LocalDate.parse(ed.toString().substring(0,10))); } catch (Exception ignored) {}
        TextField orgF = new TextField((String)p.get("organization"));
        TextField roleF = new TextField((String)p.get("role"));
        TextArea descF = new TextArea((String)p.get("description")); descF.setPrefRowCount(3);
        TextArea resultF = new TextArea((String)p.get("result")); resultF.setPrefRowCount(2);

        // Load existing attachments for editing
        List<AttachmentItem> editAttachments = new ArrayList<>();
        VBox editAttBox = new VBox(5);
        try {
            String attStr = (String) p.get("attachments");
            if (attStr != null && !attStr.isEmpty() && !"[]".equals(attStr)) {
                List<Map<String, Object>> atts = mapper.readValue(attStr, new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> am : atts) {
                    AttachmentItem ai = new AttachmentItem();
                    ai.setFileName((String) am.get("fileName"));
                    ai.setFileType((String) am.get("fileType"));
                    ai.setSize(am.get("size") != null ? ((Number) am.get("size")).longValue() : 0L);
                    ai.setBase64((String) am.get("base64"));
                    editAttachments.add(ai);
                }
            }
        } catch (Exception ignored) {}
        // Use array to allow self-referencing in lambda
        final Runnable[] refreshEditAtt = {null};
        refreshEditAtt[0] = () -> {
            editAttBox.getChildren().clear();
            for (int i = 0; i < editAttachments.size(); i++) {
                AttachmentItem ai = editAttachments.get(i);
                HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().add(new Label("📎 " + ai.getFileName() + " (" + (ai.getSize()/1024) + "KB)"));
                final int idx = i;
                Button delBtn = new Button("删除");
                delBtn.setOnAction(e -> { editAttachments.remove(idx); refreshEditAtt[0].run(); });
                row.getChildren().add(delBtn);
                editAttBox.getChildren().add(row);
            }
        };
        refreshEditAtt[0].run();
        HBox addAttRow = new HBox(10);
        Button addAttBtn = new Button("📎 添加附件");
        addAttBtn.setOnAction(ev -> {
            FileChooser fc = new FileChooser(); fc.setTitle("选择附件");
            File file = fc.showOpenDialog(null);
            if (file == null) return;
            if (file.length() > 5 * 1024 * 1024) { showWarning("文件不能超过5MB"); return; }
            try {
                AttachmentItem ai = new AttachmentItem();
                ai.setFileName(file.getName()); ai.setFileType(getFileType(file.getName()));
                ai.setSize(file.length()); ai.setBase64(Base64Util.encodeFile(file));
                editAttachments.add(ai);
                refreshEditAtt[0].run();
            } catch (Exception ex) { showError("读取文件失败: " + ex.getMessage()); }
        });
        addAttRow.getChildren().addAll(addAttBtn, new Label("(每个文件≤5MB)"));

        box.getChildren().addAll(
            new HBox(10, new Label("标题:"), titleF),
            new HBox(10, new Label("类型:"), typeCb),
            new HBox(10, new Label("开始:"), sdF, new Label("结束:"), edF),
            new HBox(10, new Label("单位:"), orgF),
            new HBox(10, new Label("角色:"), roleF),
            new HBox(10, new Label("描述:"), descF),
            new HBox(10, new Label("成果:"), resultF),
            new Label("附件:"), addAttRow, editAttBox
        );
        d.getDialogPane().setContent(new ScrollPane(box));
        ButtonType resubmitBtn = new ButtonType("再次申请", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(resubmitBtn, ButtonType.CANCEL);
        d.showAndWait().ifPresent(r -> {
            if (r == resubmitBtn) {
                String title = titleF.getText().trim();
                if (title.isEmpty()) { showWarning("请输入标题"); return; }
                if (sdF.getValue() != null && edF.getValue() != null && sdF.getValue().isAfter(edF.getValue())) {
                    showWarning("开始日期不能晚于结束日期"); return;
                }
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", ((Number)p.get("id")).longValue());
                    data.put("title", title);
                    data.put("type", typeCb.getValue());
                    data.put("startDate", sdF.getValue() != null ? sdF.getValue().toString() : "");
                    data.put("endDate", edF.getValue() != null ? edF.getValue().toString() : "");
                    data.put("organization", orgF.getText().trim());
                    data.put("role", roleF.getText().trim());
                    data.put("description", descF.getText() != null ? descF.getText().trim() : "");
                    data.put("result", resultF.getText() != null ? resultF.getText().trim() : "");
                    data.put("attachments", mapper.writeValueAsString(editAttachments));
                    ApiClient.updatePractice(data);
                    showInfo("修改成功，已重新提交审批"); currentPage = 1; loadAllPractices();
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

    private String getFileType(String name) {
        if (name == null) return "application/octet-stream";
        String ext = name.substring(name.lastIndexOf('.')+1).toLowerCase();
        return switch (ext) {
            case "pdf" -> "application/pdf"; case "doc","docx" -> "application/msword";
            case "xls","xlsx" -> "application/vnd.ms-excel"; case "ppt","pptx" -> "application/vnd.ms-powerpoint";
            case "png" -> "image/png"; case "jpg","jpeg" -> "image/jpeg";
            case "zip" -> "application/zip"; case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }
}
