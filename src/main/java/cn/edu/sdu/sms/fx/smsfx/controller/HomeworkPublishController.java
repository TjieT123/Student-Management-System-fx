package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

public class HomeworkPublishController extends BaseController {

    @FXML private ComboBox<Course> courseCombo;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private DatePicker deadlineDatePicker;
    @FXML private TextField deadlineTimeField;
    @FXML private Button publishBtn;
    @FXML private Button cancelBtn;

    private Integer preselectedCourseId;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();

        loadTeacherCourses();

        courseCombo.setConverter(new StringConverter<Course>() {
            @Override public String toString(Course c) {
                return c != null ? c.getCourseName() : "";
            }
            @Override public Course fromString(String s) { return null; }
        });

        deadlineTimeField.setText("23:59:59");

        publishBtn.setOnAction(e -> handlePublish());
        cancelBtn.setOnAction(e -> goBack());
    }

    public void setPreselectedCourseId(Integer courseId) {
        this.preselectedCourseId = courseId;
    }

    private void loadTeacherCourses() {
        try {
            PageResult<Course> result = ApiClient.getTeacherCourses(1, 100);
            if (result != null && result.getList() != null) {
                courseCombo.getItems().setAll(result.getList());
                if (preselectedCourseId != null) {
                    for (Course c : result.getList()) {
                        if (preselectedCourseId.equals(c.getId())) {
                            courseCombo.setValue(c);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            showError("加载课程列表失败: " + e.getMessage());
        }
    }

    private void handlePublish() {
        Course selectedCourse = courseCombo.getValue();
        if (selectedCourse == null) { showWarning("请选择课程"); return; }
        String title = titleField.getText().trim();
        if (title.isEmpty()) { showWarning("请输入作业标题"); return; }
        String content = contentArea.getText().trim();
        if (content.isEmpty()) { showWarning("请输入作业内容"); return; }
        if (deadlineDatePicker.getValue() == null) { showWarning("请选择截止日期"); return; }
        String timeText = deadlineTimeField.getText().trim();
        if (timeText.isEmpty()) { timeText = "23:59:59"; }

        String deadline = deadlineDatePicker.getValue().toString() + " " + timeText;

        try {
            PublishHomeworkRequest req = new PublishHomeworkRequest(selectedCourse.getId(), title, content, deadline);
            ApiClient.publishHomework(req);
            showInfo("发布成功");
            goBack();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
}
