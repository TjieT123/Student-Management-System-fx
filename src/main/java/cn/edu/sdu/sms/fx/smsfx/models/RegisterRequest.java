package cn.edu.sdu.sms.fx.smsfx.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String role;
    private String phone;

    @JsonProperty("sch_id")
    private String schId;

    // 学生专用字段（注册时若 role=STUDENT 则必填）
    private String major;
    private String gender;

    @JsonProperty("s_class")
    private Integer sClass;

    public RegisterRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSchId() { return schId; }
    public void setSchId(String schId) { this.schId = schId; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getSClass() { return sClass; }
    public void setSClass(Integer sClass) { this.sClass = sClass; }
}
