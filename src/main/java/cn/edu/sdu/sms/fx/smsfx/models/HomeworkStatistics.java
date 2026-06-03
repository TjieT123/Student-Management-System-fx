package cn.edu.sdu.sms.fx.smsfx.models;

import java.util.Map;

/**
 * 作业提交统计数据
 */
public class HomeworkStatistics {
    private Integer totalStudents;
    private Integer submittedCount;
    private Integer unsubmittedCount;
    private Map<String, Integer> distribution;  // fail / pass / good / excellent

    public HomeworkStatistics() {}

    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }

    public Integer getSubmittedCount() { return submittedCount; }
    public void setSubmittedCount(Integer submittedCount) { this.submittedCount = submittedCount; }

    public Integer getUnsubmittedCount() { return unsubmittedCount; }
    public void setUnsubmittedCount(Integer unsubmittedCount) { this.unsubmittedCount = unsubmittedCount; }

    public Map<String, Integer> getDistribution() { return distribution; }
    public void setDistribution(Map<String, Integer> distribution) { this.distribution = distribution; }
}
