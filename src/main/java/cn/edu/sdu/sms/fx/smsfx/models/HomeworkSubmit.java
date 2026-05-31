package cn.edu.sdu.sms.fx.smsfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HomeworkSubmit {
    private Integer id;
    private Integer homeworkId;
    private String sid;
    private String content;
    private Integer score;
    private String comment;
    private String status;      // SUBMITTED / GRADED
    private String submitTime;
    private String studentName;

    public HomeworkSubmit() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Integer homeworkId) { this.homeworkId = homeworkId; }

    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubmitTime() { return submitTime; }
    public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
