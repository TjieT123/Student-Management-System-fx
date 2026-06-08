package cn.edu.sdu.sms.fx.smsfx.util;

/** 简单 HTTP 响应封装，替代 kong.unirest.HttpResponse */
public class HttpResponse {
    private final int status;
    private final String body;

    public HttpResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus() { return status; }
    public String getBody() { return body; }
}
