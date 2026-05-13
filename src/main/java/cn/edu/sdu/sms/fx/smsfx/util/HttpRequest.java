package cn.edu.sdu.sms.fx.smsfx.util;

import cn.edu.sdu.sms.fx.smsfx.models.LoginResponse;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final String baseUrl = "http://localhost:1010";

    public static LoginResponse loginRequest(String username, String password) {
        String url = baseUrl + "/auth/login";
        Map<String,String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        return UnirestRequest.post(url, params, LoginResponse.class);
    }
}
