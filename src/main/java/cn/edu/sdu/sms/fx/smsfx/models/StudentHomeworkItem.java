package cn.edu.sdu.sms.fx.smsfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 学生端作业列表项（含提交状态）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentHomeworkItem {
    private Integer id;
    private String title;
    private String deadline;
    private String courseName;
    private String teacherName;
    private String status;  // UNSUBMIT / SUBMITTED / LATE / GRADED
    private Integer score;   // 分数（仅已批改时返回）

    public StudentHomeworkItem() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}
