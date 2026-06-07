package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreViewController extends BaseController {
    @FXML private VBox scoresContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button prevPageBtn, nextPageBtn;
    @FXML private Label pageLabel;

    private List<Map<String, Object>> allScores = new ArrayList<>();
    private int currentPage = 1, totalPages = 1;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        if (typeFilter != null) {
            typeFilter.getItems().addAll("全部类型", "必修", "选修");
            typeFilter.setValue("全部类型");
            typeFilter.setOnAction(e -> { currentPage = 1; renderScores(); });
        }
        if (prevPageBtn != null) prevPageBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; renderScores(); } });
        if (nextPageBtn != null) nextPageBtn.setOnAction(e -> { if (currentPage < totalPages) { currentPage++; renderScores(); } });
        loadScores();
    }

    @FXML private void onSearch() { currentPage = 1; renderScores(); }
    @FXML private void onReset() {
        if (searchField != null) searchField.clear();
        if (typeFilter != null) typeFilter.setValue("全部类型");
        currentPage = 1; renderScores();
    }

    private void loadScores() {
        try {
            allScores = ApiClient.getMyScores();
            if (allScores == null) allScores = new ArrayList<>();
        } catch (Exception e) { allScores = new ArrayList<>(); }
        currentPage = 1;
        renderScores();
    }

    private List<Map<String, Object>> getFiltered() {
        String kw = searchField != null ? searchField.getText().trim() : "";
        String tf = typeFilter != null ? typeFilter.getValue() : "全部类型";
        return allScores.stream().filter(s -> {
            if (!kw.isEmpty()) {
                String cn = (String) s.get("courseName");
                if (cn == null || !cn.toLowerCase().contains(kw.toLowerCase())) return false;
            }
            if (tf != null && !"全部类型".equals(tf)) {
                String ct = (String) s.get("courseType");
                String mapped = "必修".equals(tf) ? "REQUIRED" : "ELECTIVE";
                if (!mapped.equals(ct)) return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    private void renderScores() {
        scoresContainer.getChildren().clear();
        List<Map<String, Object>> filtered = getFiltered();
        totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / 10));
        if (pageLabel != null) pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页（共 " + filtered.size() + " 条）");
        if (prevPageBtn != null) prevPageBtn.setDisable(currentPage <= 1);
        if (nextPageBtn != null) nextPageBtn.setDisable(currentPage >= totalPages);

        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, String> cn = new TableColumn<>("课程");
        cn.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty((String) d.getValue().get("courseName")));

        TableColumn<Map<String, Object>, String> ct = new TableColumn<>("类型");
        ct.setCellValueFactory(d -> {
            String t = (String) d.getValue().get("courseType");
            return new javafx.beans.property.SimpleStringProperty("REQUIRED".equals(t) ? "必修" : "ELECTIVE".equals(t) ? "选修" : t != null ? t : "");
        });

        TableColumn<Map<String, Object>, String> cr = new TableColumn<>("学分");
        cr.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().get("credits") != null ? String.valueOf(d.getValue().get("credits")) : "3.0"));

        TableColumn<Map<String, Object>, String> sc = new TableColumn<>("成绩");
        sc.setCellValueFactory(d -> {
            Object s = d.getValue().get("finalScore");
            return new javafx.beans.property.SimpleStringProperty(s != null ? String.format("%.1f", ((Number) s).doubleValue()) : "暂无成绩");
        });

        table.getColumns().addAll(cn, ct, cr, sc);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        int from = (currentPage - 1) * 10, to = Math.min(from + 10, filtered.size());
        table.getItems().addAll(filtered.subList(from, to));
        scoresContainer.getChildren().add(table);
    }
}
