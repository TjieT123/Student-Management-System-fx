package cn.edu.sdu.sms.fx.smsfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SemesterConfig {
    private Integer id;
    private String semesterName;
    private String startWeekDate;
    private Integer totalWeeks;

    public SemesterConfig() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSemesterName() { return semesterName; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }

    public String getStartWeekDate() { return startWeekDate; }
    public void setStartWeekDate(String startWeekDate) { this.startWeekDate = startWeekDate; }

    public Integer getTotalWeeks() { return totalWeeks; }
    public void setTotalWeeks(Integer totalWeeks) { this.totalWeeks = totalWeeks; }
}
