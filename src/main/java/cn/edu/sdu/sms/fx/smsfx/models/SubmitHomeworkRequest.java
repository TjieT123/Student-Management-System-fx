package cn.edu.sdu.sms.fx.smsfx.models;

public class SubmitHomeworkRequest {
    private Integer homeworkId;
    private String content;

    public SubmitHomeworkRequest() {}

    public SubmitHomeworkRequest(Integer homeworkId, String content) {
        this.homeworkId = homeworkId;
        this.content = content;
    }

    public Integer getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Integer homeworkId) { this.homeworkId = homeworkId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
