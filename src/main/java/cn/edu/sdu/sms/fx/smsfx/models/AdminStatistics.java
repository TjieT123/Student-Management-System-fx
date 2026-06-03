package cn.edu.sdu.sms.fx.smsfx.models;

/**
 * 管理员首页统计数据
 */
public class AdminStatistics {
    private Integer totalUsers;
    private Integer adminCount;
    private Integer teacherCount;
    private Integer studentCount;

    public AdminStatistics() {}

    public Integer getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }

    public Integer getAdminCount() { return adminCount; }
    public void setAdminCount(Integer adminCount) { this.adminCount = adminCount; }

    public Integer getTeacherCount() { return teacherCount; }
    public void setTeacherCount(Integer teacherCount) { this.teacherCount = teacherCount; }

    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
}
