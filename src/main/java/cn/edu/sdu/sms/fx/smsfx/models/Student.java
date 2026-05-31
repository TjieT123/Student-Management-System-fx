package cn.edu.sdu.sms.fx.smsfx.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Student {
    private String sid;
    private String name;
    private String major;
    private String gender;

    @JsonProperty("sClass")
    private Integer sClass;

    public Student() {}

    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getSClass() { return sClass; }
    public void setSClass(Integer sClass) { this.sClass = sClass; }
}
