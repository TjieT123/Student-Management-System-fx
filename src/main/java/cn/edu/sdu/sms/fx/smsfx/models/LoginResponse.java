package cn.edu.sdu.sms.fx.smsfx.models;

public class LoginResponse {
    private  Integer code;
    private  LoginData data;
    private  String msg;

    public LoginResponse() {
    }

    public LoginResponse(Integer code, LoginData loginData, String msg) {
        this.code = code;
        this.data = loginData;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public LoginData getData() {
        return data;
    }

    public void setData(LoginData data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
