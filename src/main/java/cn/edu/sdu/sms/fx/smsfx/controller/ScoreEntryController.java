package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

public class ScoreEntryController extends BaseController {

    @FXML private ComboBox<Course> courseSelector;
    @FXML private VBox contentArea;
    @FXML private Button loadBtn;
    @FXML private Label statusLabel;

    private Course currentCourse;
    private List<Student> allStudents = new ArrayList<>();
    private Map<String, Double> existingScores = new HashMap<>();
    private int currentPage = 1, pageSize = 10, totalPages = 1;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        loadTeacherCourses();
        if (loadBtn != null) { loadBtn.setVisible(false); loadBtn.setManaged(false); }
        courseSelector.setOnAction(e -> { if (courseSelector.getValue() != null) loadStudents(); });
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
            statusLabel.setText("共 " + allStudents.size() + " 名学生");
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
        scoreCol.setCellFactory(col -> new javafx.scene.control.TableCell<Student, Double>() {
            private final TextField textField = new TextField();
            {
                textField.setOnAction(e -> {
                    if (saveCurrentEdit()) commitEdit(existingScores.get(getTableRow().getItem() != null ? getTableRow().getItem().getSid() : null));
                });
            }
            /** 保存当前输入框的值到 existingScores，返回是否有效 */
            private boolean saveCurrentEdit() {
                Student st = getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()
                        ? getTableView().getItems().get(getIndex()) : null;
                if (st == null) return false;
                String s = textField.getText().trim();
                if (s.isEmpty()) { existingScores.remove(st.getSid()); return true; }
                try {
                    double v = Double.parseDouble(s);
                    if (v < 0 || v > 100) { showWarning("成绩必须在0-100之间"); textField.setText(""); return false; }
                    existingScores.put(st.getSid(), v);
                    return true;
                } catch (NumberFormatException ex) { showWarning("请输入0-100之间的数字"); textField.setText(""); return false; }
            }
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || getTableView() == null) { setGraphic(null); setText(null); return; }
                Student st = getTableView().getItems().get(getIndex());
                Double cur = st != null ? existingScores.get(st.getSid()) : null;
                if (isEditing()) { setGraphic(textField); setText(null); }
                else { setGraphic(null); setText(cur != null ? String.valueOf(cur) : (val != null ? String.valueOf(val) : "")); }
            }
            @Override public void startEdit() {
                super.startEdit();
                Student st = getTableView().getItems().get(getIndex());
                Double cur = existingScores.get(st.getSid());
                textField.setText(cur != null ? String.valueOf(cur) : "");
                setGraphic(textField); setText(null);
            }
            @Override public void cancelEdit() {
                saveCurrentEdit();
                super.cancelEdit();
                setGraphic(null);
                Student st = getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()
                        ? getTableView().getItems().get(getIndex()) : null;
                Double cur = st != null ? existingScores.get(st.getSid()) : getItem();
                setText(cur != null ? String.valueOf(cur) : "");
            }
        });
        scoreCol.setPrefWidth(100);
        table.getColumns().addAll(sidCol, nameCol, majorCol, classCol, scoreCol);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(25);
        table.setPrefHeight(278);
        table.getItems().addAll(pageStudents);
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
        // 自定义 cell 已在输入时实时更新 existingScores，无需额外处理
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
