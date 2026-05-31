package cn.edu.sdu.sms.fx.smsfx.models;

public class PublishHomeworkRequest {
    private Integer courseId;
    private String title;
    private String content;
    private String deadline;

    public PublishHomeworkRequest() {}

    public PublishHomeworkRequest(Integer courseId, String title, String content, String deadline) {
        this.courseId = courseId;
        this.title = title;
        this.content = content;
        this.deadline = deadline;
    }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
}
