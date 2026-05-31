package cn.edu.sdu.sms.fx.smsfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 管理员端用户视图对象（含学生扩展字段）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminUserVO {
    private Integer id;
    private String username;
    private String name;
    private String role;
    private String phone;

    @JsonProperty("sch_id")
    private String schId;

    private String major;
    private String gender;

    @JsonProperty("sClass")
    private Integer sClass;

    public AdminUserVO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

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
