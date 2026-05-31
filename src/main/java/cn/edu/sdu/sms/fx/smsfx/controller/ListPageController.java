package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 共用列表页控制器 —— 支持公告列表和作业列表两种类型
 */
public class ListPageController extends BaseController {

    public enum PageType {
        ANNOUNCEMENT, HOMEWORK
    }

    @FXML private Label pageTitleLabel;
    @FXML private VBox cardContainer;
    @FXML private HBox paginationBox;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageLabel;

    private PageType pageType;
    private Integer courseId;
    private String role;
    private int currentPage = 1;
    private int totalPages = 1;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();
    }

    /**
     * 设置页面类型和参数，由导航管理器在 navigateTo 时调用
     */
    public void setPageType(PageType type, Integer courseId, String role) {
        this.pageType = type;
        this.courseId = courseId;
        this.role = role;
        loadData();

        prevPageBtn.setOnAction(e -> {
            if (currentPage > 1) { currentPage--; loadData(); }
        });
        nextPageBtn.setOnAction(e -> {
            if (currentPage < totalPages) { currentPage++; loadData(); }
        });
    }

    private void loadData() {
        cardContainer.getChildren().clear();
        try {
            if (pageType == PageType.ANNOUNCEMENT) {
                pageTitleLabel.setText("📢 公告");
                PageResult<Announcement> result = ApiClient.getAnnouncementList(currentPage, 10);
                updatePagination(result);
                if (result.getList() != null) {
                    for (Announcement a : result.getList()) {
                        cardContainer.getChildren().add(createAnnouncementCard(a));
                    }
                }
            } else if (pageType == PageType.HOMEWORK) {
                pageTitleLabel.setText("📝 作业");
                if ("STUDENT".equals(role) && courseId != null) {
                    PageResult<StudentHomeworkItem> result =
                            ApiClient.getStudentHomeworkList(courseId, currentPage, 10);
                    updatePagination(result);
                    if (result.getList() != null) {
                        for (StudentHomeworkItem hw : result.getList()) {
                            cardContainer.getChildren().add(createStudentHomeworkCard(hw));
                        }
                    }
                } else if ("TEACHER".equals(role)) {
                    PageResult<Homework> allHomeworks = ApiClient.getHomeworkList(1, 100);
                    if (allHomeworks != null && allHomeworks.getList() != null) {
                        List<Homework> filtered = courseId != null ?
                                allHomeworks.getList().stream()
                                        .filter(h -> courseId.equals(h.getCourseId()))
                                        .collect(Collectors.toList()) :
                                allHomeworks.getList();

                        // 手动分页
                        int total = filtered.size();
                        int pageSize = 10;
                        totalPages = (int) Math.ceil((double) total / pageSize);
                        int from = (currentPage - 1) * pageSize;
                        int to = Math.min(from + pageSize, total);
                        List<Homework> pageItems = filtered.subList(from, to);

                        pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页");
                        prevPageBtn.setDisable(currentPage <= 1);
                        nextPageBtn.setDisable(currentPage >= totalPages);

                        for (Homework hw : pageItems) {
                            cardContainer.getChildren().add(createTeacherHomeworkCard(hw));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Label err = new Label("加载失败: " + e.getMessage());
            err.setStyle("-fx-text-fill: red;");
            cardContainer.getChildren().add(err);
        }
    }

    private void updatePagination(PageResult<?> result) {
        if (result != null) {
            totalPages = (int) Math.ceil((double) result.getTotal() / result.getPageSize());
        }
        pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页");
        prevPageBtn.setDisable(currentPage <= 1);
        nextPageBtn.setDisable(currentPage >= totalPages);
    }

    private HBox createAnnouncementCard(Announcement a) {
        HBox card = new HBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            NavigationManager.navigateTo("announcement-detail-view.fxml",
                    (AnnouncementDetailController controller) -> controller.setAnnouncementId(a.getId()));
        });
        Label titleLabel = new Label(a.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label publisher = new Label(a.getPublisherName() != null ? a.getPublisherName() : "");
        publisher.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        Label time = new Label(formatDateTime(a.getPublishTime()));
        time.setStyle("-fx-font-size: 12; -fx-text-fill: #95a5a6;");
        card.getChildren().addAll(titleLabel, spacer, publisher, time);
        return card;
    }

    private HBox createStudentHomeworkCard(StudentHomeworkItem hw) {
        HBox card = new HBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            NavigationManager.navigateTo("homework-submit-view.fxml",
                    (HomeworkSubmitController controller) -> controller.setHomeworkId(hw.getId(), courseId));
        });
        Label titleLabel = new Label(hw.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label deadline = new Label("截止: " + formatDateTime(hw.getDeadline()));
        deadline.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        Label status = createStatusLabel(hw.getStatus());
        card.getChildren().addAll(titleLabel, spacer, deadline, status);
        return card;
    }

    private HBox createTeacherHomeworkCard(Homework hw) {
        HBox card = new HBox(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-padding: 12; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            NavigationManager.navigateTo("homework-grading-list-view.fxml",
                    (HomeworkGradingListController controller) -> controller.setHomeworkId(hw.getId()));
        });
        Label titleLabel = new Label(hw.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label deadline = new Label("截止: " + formatDateTime(hw.getDeadline()));
        deadline.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        card.getChildren().addAll(titleLabel, spacer, deadline);
        return card;
    }

    private Label createStatusLabel(String status) {
        Label label = new Label();
        if (status == null) status = "UNSUBMIT";
        switch (status) {
            case "UNSUBMIT": label.setText("未提交"); label.setStyle("-fx-text-fill: gray; -fx-font-size: 12;"); break;
            case "SUBMITTED": label.setText("已提交"); label.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12;"); break;
            case "LATE": label.setText("迟交"); label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;"); break;
            case "GRADED": label.setText("已批改"); label.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;"); break;
            default: label.setText(status); break;
        }
        return label;
    }
}
