package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;

import java.util.*;

public class ScoreEntryController extends BaseController {

    @FXML private ComboBox<Course> courseSelector;
    @FXML private VBox contentArea;
    @FXML private Button loadBtn;
    @FXML private Label statusLabel;

    private Course currentCourse;
    private List<Student> allStudents = new ArrayList<>();
    private Map<String, Double> existingScores = new HashMap<>();
    private int currentPage = 1, pageSize = 15, totalPages = 1;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        loadTeacherCourses();
        loadBtn.setOnAction(e -> loadStudents());
        courseSelector.setOnAction(e -> loadStudents());
    }

    private void loadTeacherCourses() {
        try {
            PageResult<Course> courses = ApiClient.getTeacherCourses(1, 100);
            if (courses != null && courses.getList() != null) courseSelector.getItems().addAll(courses.getList());
        } catch (Exception e) { statusLabel.setText("加载课程失败"); }
    }

    private void loadStudents() {
        currentCourse = courseSelector.getValue();
        if (currentCourse == null) return;
        contentArea.getChildren().clear();
        allStudents.clear(); existingScores.clear();
        statusLabel.setText("加载中...");
        try {
            java.util.List<java.util.Map<String, Object>> scores = ApiClient.getCourseScores(currentCourse.getId());
            if (scores != null) for (java.util.Map<String, Object> s : scores) {
                Object fs = s.get("final_score");
                if (fs != null) existingScores.put((String) s.get("sid"), ((Number) fs).doubleValue());
            }
        } catch (Exception e) { statusLabel.setText("加载已有成绩失败: " + e.getMessage()); return; }
        try {
            java.util.List<Student> students = ApiClient.getEnrolledStudents(currentCourse.getId());
            if (students == null || students.isEmpty()) { statusLabel.setText("该课程暂无选课学生"); return; }
            allStudents = students;
            totalPages = Math.max(1, (int) Math.ceil((double) allStudents.size() / pageSize));
            currentPage = 1;
            statusLabel.setText("共 " + allStudents.size() + " 名学生（已有 " + existingScores.size() + " 人录入成绩）");
            renderTable();
        } catch (Exception e) { statusLabel.setText("加载学生列表失败: " + e.getMessage()); }
    }

    private void renderTable() {
        contentArea.getChildren().clear();
        int from = (currentPage - 1) * pageSize;
        int to = Math.min(from + pageSize, allStudents.size());
        List<Student> pageStudents = allStudents.subList(from, to);

        TableView<Student> table = new TableView<>();
        TableColumn<Student, String> sidCol = new TableColumn<>("学号"); sidCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSid()));
        TableColumn<Student, String> nameCol = new TableColumn<>("姓名"); nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        TableColumn<Student, String> majorCol = new TableColumn<>("专业"); majorCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMajor() != null ? d.getValue().getMajor() : ""));
        TableColumn<Student, String> classCol = new TableColumn<>("班级"); classCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSClass() != null ? String.valueOf(d.getValue().getSClass()) : ""));
        TableColumn<Student, Double> scoreCol = new TableColumn<>("期末成绩");
        scoreCol.setCellValueFactory(d -> {
            Double sc = existingScores.get(d.getValue().getSid());
            return new javafx.beans.property.SimpleDoubleProperty(sc != null ? sc : 0).asObject();
        });
        scoreCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter() {
            @Override public Double fromString(String s) {
                if (s == null || s.trim().isEmpty()) return null;
                try { return Double.parseDouble(s.trim()); }
                catch (NumberFormatException e) { return null; }
            }
            @Override public String toString(Double v) {
                return v == null ? "" : String.valueOf(v.intValue());
            }
        }));
        scoreCol.setOnEditCommit(e -> {
            Double val = e.getNewValue();
            if (val == null) {
                showWarning("请输入0-100之间的数字，不能为字母或空");
                e.getTableView().refresh();
                return;
            }
            if (val < 0 || val > 100) { showWarning("成绩必须在0-100之间"); e.getTableView().refresh(); return; }
            existingScores.put(e.getRowValue().getSid(), val);
            e.getTableView().refresh();
        });
        scoreCol.setPrefWidth(100);
        table.getColumns().addAll(sidCol, nameCol, majorCol, classCol, scoreCol);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getItems().addAll(pageStudents);
        table.setPrefHeight(500);
        contentArea.getChildren().add(table);

        // Pagination
        HBox pagination = new HBox(10);
        pagination.setAlignment(javafx.geometry.Pos.CENTER);
        Button prevBtn = new Button("上一页"); prevBtn.setDisable(currentPage <= 1);
        Button nextBtn = new Button("下一页"); nextBtn.setDisable(currentPage >= totalPages);
        Label pageLabel = new Label("第 " + currentPage + "/" + totalPages + " 页");
        prevBtn.setOnAction(e -> { currentPage--; renderTable(); });
        nextBtn.setOnAction(e -> { currentPage++; renderTable(); });
        pagination.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        Button saveBtn = new Button("💾 全部保存");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14;");
        saveBtn.setOnAction(e -> saveScores());
        contentArea.getChildren().addAll(pagination, saveBtn);
    }

    private void saveScores() {
        if (currentCourse == null) return;
        try {
            List<Map<String, Object>> data = new ArrayList<>();
            for (Map.Entry<String, Double> e : existingScores.entrySet()) {
                Map<String, Object> m = new HashMap<>();
                m.put("sid", e.getKey()); m.put("finalScore", e.getValue()); data.add(m);
            }
            if (data.isEmpty()) { showWarning("没有需要保存的成绩"); return; }
            ApiClient.saveScores(currentCourse.getId(), data);
            showInfo("保存成功（" + data.size() + " 条记录）");
        } catch (Exception ex) { showError("保存失败: " + ex.getMessage()); }
    }
}
