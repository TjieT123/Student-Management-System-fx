package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HonorViewController extends BaseController {
    @FXML private VBox contentBox;
    @FXML private TextField searchField;

    private List<Map<String, Object>> allHonors = new ArrayList<>();

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        loadHonors();
    }

    @FXML private void onSearch() { renderHonors(); }
    @FXML private void onReset() {
        if (searchField != null) searchField.clear();
        renderHonors();
    }

    private String fmtDate(Object dt) { try { String s=dt instanceof Number?new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(((Number)dt).longValue())):dt.toString(); if(s.length()>=10)s=s.substring(0,10); java.time.LocalDate ld=java.time.LocalDate.parse(s); return ld.getYear()+"年"+ld.getMonthValue()+"月"+ld.getDayOfMonth()+"日"; } catch(Exception e){return dt!=null?dt.toString():"";} }

    private List<Map<String, Object>> getFiltered() {
        String kw = searchField != null ? searchField.getText().trim() : "";
        if (kw.isEmpty()) return allHonors;
        return allHonors.stream().filter(h -> {
            String title = (String) h.get("title");
            return title != null && title.toLowerCase().contains(kw.toLowerCase());
        }).collect(Collectors.toList());
    }

    private void loadHonors() {
        try {
            allHonors = ApiClient.getMyHonors();
            if (allHonors == null) allHonors = new ArrayList<>();
        } catch (Exception e) { allHonors = new ArrayList<>(); }
        renderHonors();
    }

    private void renderHonors() {
        contentBox.getChildren().clear();
        Label hint = new Label("💡 提示：点击荣誉卡片可查看详情");
        hint.setStyle("-fx-font-size: 12; -fx-text-fill: #95a5a6; -fx-padding: 0 0 10 0;");
        contentBox.getChildren().add(hint);
        List<Map<String, Object>> filtered = getFiltered();
        if (filtered.isEmpty()) {
            contentBox.getChildren().add(new Label("暂无荣誉记录"));
            return;
        }
        for (Map<String, Object> h : filtered) {
            HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
            String title = (String) h.get("title"), type = (String) h.get("type"), level = (String) h.get("level");
            Object date = h.get("award_date"); String desc = (String) h.get("description");
            VBox info = new VBox(4);
            info.getChildren().add(new Label(title != null ? title : "") {{ setStyle("-fx-font-size: 14; -fx-font-weight: bold;"); }});
            info.getChildren().add(new Label((type != null ? type : "") + " | " + (level != null ? level : "")));
            Region sp = new Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
            String dateStr = fmtDate(date); final String ds = dateStr;
            card.getChildren().addAll(info, sp, new Label(dateStr));
            card.setOnMouseClicked(e -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("荣誉详情"); a.setHeaderText(title);
                a.setContentText("比赛名称: " + (type != null ? type : "") + "\n级别: " + (level != null ? level : "")
                    + "\n日期: " + ds + "\n描述: " + (desc != null ? desc : ""));
                a.showAndWait();
            });
            contentBox.getChildren().add(card);
        }
    }
}
