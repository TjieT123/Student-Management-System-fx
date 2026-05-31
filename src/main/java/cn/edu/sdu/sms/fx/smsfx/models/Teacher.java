package cn.edu.sdu.sms.fx.smsfx.models;

public class Teacher {
    private String schId;
    private String name;

    public Teacher() {}

    public String getSchId() { return schId; }
    public void setSchId(String schId) { this.schId = schId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name + " (" + schId + ")";
    }
}
