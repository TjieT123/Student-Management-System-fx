package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.stream.Collectors;

public class CourseSelectionDetailController extends BaseController {

    @FXML private Label courseNameLabel;
    @FXML private Label teacherLabel;
    @FXML private Label addressLabel;
    @FXML private Label detailLabel;
    @FXML private Button enrollBtn;
    @FXML private Label enrolledLabel;
    @FXML private Button goToCourseBtn;

    private Integer courseId;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
        loadCourseDetail();
        checkEnrollment();
    }

    private void loadCourseDetail() {
        try {
            Course c = ApiClient.getCourseDetail(courseId);
            courseNameLabel.setText(c.getCourseName());
            teacherLabel.setText(c.getTeacherName() != null ? c.getTeacherName() : "未知");
            addressLabel.setText(c.getAddress() != null ? c.getAddress() : "未知");
            detailLabel.setText(c.getDetail() != null ? c.getDetail() : "暂无简介");
        } catch (Exception e) {
            showError("加载课程详情失败: " + e.getMessage());
        }
    }

    private void checkEnrollment() {
        try {
            PageResult<Course> myCourses = ApiClient.getMyCourses(1, 100);
            boolean enrolled = myCourses != null && myCourses.getList() != null &&
                    myCourses.getList().stream().anyMatch(c -> courseId.equals(c.getId()));

            if (enrolled) {
                enrolledLabel.setVisible(true);
                enrolledLabel.setManaged(true);
                goToCourseBtn.setVisible(true);
                goToCourseBtn.setManaged(true);
                goToCourseBtn.setOnAction(e -> {
                    NavigationManager.navigateTo("course-detail-view.fxml",
                            (CourseDetailController controller) -> controller.setCourseId(courseId));
                });
            } else {
                enrollBtn.setVisible(true);
                enrollBtn.setManaged(true);
                enrollBtn.setOnAction(e -> handleEnroll());
            }
        } catch (Exception ignored) {
            enrollBtn.setVisible(true);
            enrollBtn.setManaged(true);
            enrollBtn.setOnAction(e -> handleEnroll());
        }
    }

    private void handleEnroll() {
        try {
            ApiClient.enroll(courseId);
            showInfo("选课成功");
            enrollBtn.setVisible(false);
            enrollBtn.setManaged(false);
            enrolledLabel.setVisible(true);
            enrolledLabel.setManaged(true);
            goToCourseBtn.setVisible(true);
            goToCourseBtn.setManaged(true);
            goToCourseBtn.setOnAction(e -> {
                NavigationManager.navigateTo("course-detail-view.fxml",
                        (CourseDetailController controller) -> controller.setCourseId(courseId));
            });
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
}
