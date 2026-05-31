package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.Announcement;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AnnouncementDetailController extends BaseController {

    @FXML private Label announcementTitleLabel;
    @FXML private Label publisherLabel;
    @FXML private Label timeLabel;
    @FXML private Label contentLabel;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();
    }

    public void setAnnouncementId(Integer id) {
        try {
            Announcement a = ApiClient.getAnnouncementDetail(id);
            announcementTitleLabel.setText(a.getTitle());
            publisherLabel.setText("发布者：" + (a.getPublisherName() != null ? a.getPublisherName() : ""));
            timeLabel.setText("发布时间：" + formatDateTime(a.getPublishTime()));
            contentLabel.setText(a.getContent() != null ? a.getContent() : "暂无内容");
        } catch (Exception e) {
            showError("加载公告详情失败: " + e.getMessage());
        }
    }
}
