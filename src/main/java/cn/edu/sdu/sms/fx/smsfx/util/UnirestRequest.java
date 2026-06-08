package cn.edu.sdu.sms.fx.smsfx.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * HTTP 请求工具类（基于 JDK java.net.http.HttpClient）
 * 封装 GET/POST/PUT/DELETE 请求，支持 JSON 序列化与 Token 认证
 */
public class UnirestRequest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // ==================== 内部方法 ====================

    private static String buildQueryString(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("?");
        for (var entry : params.entrySet()) {
            if (!sb.toString().equals("?")) sb.append("&");
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private static HttpRequest.Builder requestBuilder(String url, String token) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30));
        if (token != null && !token.isEmpty()) {
            b.header("Authorization", "Bearer " + token);
        }
        return b;
    }

    private static cn.edu.sdu.sms.fx.smsfx.util.HttpResponse send(HttpRequest request) {
        try {
            java.net.http.HttpResponse<String> r = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            return new HttpResponse(r.statusCode(), r.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== GET ====================

    public static String get(String url) {
        HttpRequest r = requestBuilder(url, null)
                .header("Accept", "application/json").GET().build();
        HttpResponse resp = send(r);
        return resp != null ? resp.getBody() : null;
    }

    public static String get(String url, Map<String, Object> params) {
        String fullUrl = url + buildQueryString(params);
        HttpRequest r = requestBuilder(fullUrl, null)
                .header("Accept", "application/json").GET().build();
        HttpResponse resp = send(r);
        return resp != null ? resp.getBody() : null;
    }

    public static <T> T get(String url, Class<T> clazz) {
        try {
            String body = get(url);
            return body != null ? objectMapper.readValue(body, clazz) : null;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    // ==================== POST ====================

    public static String post(String url, Object jsonBody) {
        try {
            String json = objectMapper.writeValueAsString(jsonBody);
            HttpRequest r = requestBuilder(url, null)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse resp = send(r);
            return resp != null ? resp.getBody() : null;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public static <T> T post(String url, Object jsonBody, Class<T> clazz) {
        try {
            String body = post(url, jsonBody);
            return body != null ? objectMapper.readValue(body, clazz) : null;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    // ==================== PUT ====================

    public static String put(String url, Object jsonBody) {
        try {
            String json = objectMapper.writeValueAsString(jsonBody);
            HttpRequest r = requestBuilder(url, null)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse resp = send(r);
            return resp != null ? resp.getBody() : null;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    // ==================== DELETE ====================

    public static String delete(String url) {
        HttpRequest r = requestBuilder(url, null).DELETE().build();
        HttpResponse resp = send(r);
        return resp != null ? resp.getBody() : null;
    }

    // ==================== 带 Token 的 GET ====================

    public static String getWithAuth(String url, String token) {
        HttpRequest r = requestBuilder(url, token)
                .header("Accept", "application/json").GET().build();
        HttpResponse resp = send(r);
        return resp != null ? resp.getBody() : null;
    }

    public static String getWithAuth(String url, Map<String, Object> params, String token) {
        String fullUrl = url + buildQueryString(params);
        HttpRequest r = requestBuilder(fullUrl, token)
                .header("Accept", "application/json").GET().build();
        HttpResponse resp = send(r);
        return resp != null ? resp.getBody() : null;
    }

    // ==================== 带 Token 的 POST ====================

    public static String postWithAuth(String url, Object jsonBody, String token) {
        try {
            String json = objectMapper.writeValueAsString(jsonBody);
            HttpRequest r = requestBuilder(url, token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse resp = send(r);
            return resp != null ? resp.getBody() : null;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    // ==================== 返回原始 HttpResponse 的方法 ====================

    public static HttpResponse getRaw(String url, Map<String, Object> params, String token) {
        String fullUrl = url + buildQueryString(params);
        HttpRequest r = requestBuilder(fullUrl, token)
                .header("Accept", "application/json").GET().build();
        return send(r);
    }

    public static HttpResponse postRaw(String url, Object jsonBody, String token) {
        try {
            String json = objectMapper.writeValueAsString(jsonBody);
            HttpRequest r = requestBuilder(url, token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            return send(r);
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public static HttpResponse postRaw(String url, String token) {
        HttpRequest r = requestBuilder(url, token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        return send(r);
    }

    public static HttpResponse putRaw(String url, Object jsonBody, String token) {
        try {
            String json = objectMapper.writeValueAsString(jsonBody);
            HttpRequest r = requestBuilder(url, token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json)).build();
            return send(r);
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public static HttpResponse deleteRaw(String url, String token) {
        HttpRequest r = requestBuilder(url, token).DELETE().build();
        return send(r);
    }

    public static HttpResponse deleteRawWithBody(String url, Object jsonBody, String token) {
        try {
            String json = objectMapper.writeValueAsString(jsonBody);
            HttpRequest r = requestBuilder(url, token)
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(json)).build();
            return send(r);
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    // ==================== 关闭 ====================

    public static void shutdown() { /* HttpClient is auto-managed, no explicit shutdown needed */ }
}
