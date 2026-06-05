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

    // 扩展字段
    private String birthDate;
    private Integer enrollmentYear;
    private String idCard;
    private String nativePlace;
    private String politicalStatus;
    private String address;
    private String contactName;
    private String contactPhone;
    private String socialRelations;

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

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public Integer getEnrollmentYear() { return enrollmentYear; }
    public void setEnrollmentYear(Integer enrollmentYear) { this.enrollmentYear = enrollmentYear; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getNativePlace() { return nativePlace; }
    public void setNativePlace(String nativePlace) { this.nativePlace = nativePlace; }
    public String getPoliticalStatus() { return politicalStatus; }
    public void setPoliticalStatus(String politicalStatus) { this.politicalStatus = politicalStatus; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getSocialRelations() { return socialRelations; }
    public void setSocialRelations(String socialRelations) { this.socialRelations = socialRelations; }
}
