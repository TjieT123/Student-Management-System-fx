package cn.edu.sdu.sms.fx.smsfx.models;

public class EnrollRequest {
    private Integer courseId;

    public EnrollRequest() {}

    public EnrollRequest(Integer courseId) {
        this.courseId = courseId;
    }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
}
