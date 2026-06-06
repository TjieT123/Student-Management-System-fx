package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.AttachmentItem;
import cn.edu.sdu.sms.fx.smsfx.models.Course;
import cn.edu.sdu.sms.fx.smsfx.models.PageResult;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.Base64Util;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class CourseMaterialsController extends BaseController {
    @FXML private ComboBox<Course> courseCombo;
    @FXML private VBox materialsBox;
    @FXML private Button uploadBtn;
    @FXML private Label hintLabel;

    private Integer selectedCourseId = null;
    private List<AttachmentItem> currentMaterials = null;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        hintLabel.setText("请先选择一门课程，然后上传或管理课程资料");
        loadTeacherCourses();
        courseCombo.setOnAction(e -> loadMaterials());
        uploadBtn.setOnAction(e -> handleUpload());
        uploadBtn.setDisable(true);
    }

    private void loadTeacherCourses() {
        try {
            PageResult<Course> result = ApiClient.getTeacherCourses(1, 200);
            if (result != null && result.getList() != null) {
                courseCombo.getItems().setAll(result.getList());
                courseCombo.setCellFactory(cell -> new javafx.scene.control.ListCell<>() {
                    @Override protected void updateItem(Course c, boolean empty) {
                        super.updateItem(c, empty);
                        setText(empty || c == null ? null : c.getCourseName());
                    }
                });
                courseCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
                    @Override protected void updateItem(Course c, boolean empty) {
                        super.updateItem(c, empty);
                        setText(empty || c == null ? "请选择课程" : c.getCourseName());
                    }
                });
            }
        } catch (Exception e) { showError("加载课程失败: " + e.getMessage()); }
    }

    private void loadMaterials() {
        materialsBox.getChildren().clear();
        Course c = courseCombo.getValue();
        if (c == null) { uploadBtn.setDisable(true); return; }
        selectedCourseId = c.getId();
        if (selectedCourseId == null) { showError("课程ID为空"); return; }
        uploadBtn.setDisable(false);
        try {
            currentMaterials = ApiClient.getCourseMaterials(selectedCourseId);
            if (currentMaterials != null) {
                for (int i = 0; i < currentMaterials.size(); i++) {
                    AttachmentItem ai = currentMaterials.get(i);
                    HBox row = new HBox(10);
                    row.setStyle("-fx-padding: 5; -fx-background-color: #ecf0f1; -fx-background-radius: 3;");
                    row.getChildren().add(new Label("📎 " + ai.getFileName() + " (" + ai.getSizeDisplay() + ")"));
                    final int idx = i;
                    Button delBtn = new Button("删除");
                    delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
                    delBtn.setOnAction(e -> {
                        try {
                            ApiClient.deleteCourseMaterial(selectedCourseId, idx);
                            showInfo("删除成功"); loadMaterials();
                        } catch (Exception ex) { showError(ex.getMessage()); }
                    });
                    row.getChildren().add(delBtn);
                    materialsBox.getChildren().add(row);
                }
            }
        } catch (Exception e) {
            materialsBox.getChildren().add(new Label("加载失败: " + e.getMessage()));
        }
    }

    private void handleUpload() {
        if (selectedCourseId == null) return;
        FileChooser fc = new FileChooser(); fc.setTitle("选择课程资料");
        File file = fc.showOpenDialog(null);
        if (file == null) return;
        if (file.length() > 10 * 1024 * 1024) { showWarning("文件不能超过10MB"); return; }
        try {
            String base64 = Base64Util.encodeFile(file, 10 * 1024 * 1024);
            String fileType = getFileType(file.getName());
            ApiClient.addCourseMaterial(selectedCourseId, file.getName(), fileType, file.length(), base64);
            showInfo("上传成功"); loadMaterials();
        } catch (Exception ex) { showError("上传失败: " + ex.getMessage()); }
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
