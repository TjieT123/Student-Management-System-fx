package cn.edu.sdu.sms.fx.smsfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {
    private Integer id;
    private String courseName;
    private String detail;
    private String address;
    private String teacherId;
    private String teacherName;
    private String type;
    private Double credits;

    public Course() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getCredits() { return credits; }
    public void setCredits(Double credits) { this.credits = credits; }

    @Override
    public String toString() {
        return courseName;
    }
}
