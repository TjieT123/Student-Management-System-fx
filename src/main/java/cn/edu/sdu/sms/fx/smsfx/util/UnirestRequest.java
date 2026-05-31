package cn.edu.sdu.sms.fx.smsfx.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import java.util.Map;

/**
 * Unirest 工具类
 * 封装 HTTP 请求：GET、POST、PUT、DELETE
 * 支持 JSON 自动序列化与反序列化
 * 专门用于 JavaFX 项目调用后端接口
 */
public class UnirestRequest {

    /**
     * Jackson 对象映射器，用于 JSON 和 Java 对象互相转换
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 静态代码块：项目启动时执行一次
     * 配置全局超时时间，避免请求一直阻塞
     */
    static {
        Unirest.config()
                .connectTimeout(5000)   // 连接超时：5秒
                .socketTimeout(30000);  // 读取超时：30秒（AI判卷等耗时操作需要更长时间）
    }

    // ==================== GET 请求（无参数） ====================
    /**
     * 发送 GET 请求，不带参数
     * @param url 请求地址
     * @return 返回接口响应的字符串
     */
    public static String get(String url) {
        try {
            HttpResponse<String> response = Unirest.get(url)
                    .header("Accept", "application/json")
                    .asString();
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== GET 请求（带参数） ====================
    /**
     * 发送带参数的 GET 请求
     * @param url 请求地址
     * @param params 请求参数（Map 格式）
     * @return 接口响应字符串
     */
    public static String get(String url, Map<String, Object> params) {
        try {
            HttpResponse<String> response = Unirest.get(url)
                    .header("Accept", "application/json")
                    .queryString(params)
                    .asString();
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== GET 请求（自动转 Java 对象） ====================
    /**
     * 发送 GET 请求，并自动将返回的 JSON 转为指定 Java 对象
     * @param url 请求地址
     * @param clazz 要转换的目标对象类
     * @return 转换后的 Java 对象
     */
    public static <T> T get(String url, Class<T> clazz) {
        try {
            String body = get(url);
            return objectMapper.readValue(body, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== POST 请求（提交 JSON） ====================
    /**
     * 发送 POST 请求，提交 JSON 格式数据
     * @param url 请求地址
     * @param jsonBody 要提交的对象（自动转 JSON）
     * @return 接口返回字符串
     */
    public static String post(String url, Object jsonBody) {
        try {
            HttpResponse<String> response = Unirest.post(url)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== POST 请求（提交 JSON，返回对象） ====================
    /**
     * 发送 POST 请求，提交对象，接收对象
     * @param url 请求地址
     * @param jsonBody 提交的对象
     * @param clazz 要返回的对象类型
     * @return 接口返回的对象
     */
    public static <T> T post(String url, Object jsonBody, Class<T> clazz) {
        try {
            String body = post(url, jsonBody);
            return objectMapper.readValue(body, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== PUT 请求（更新数据） ====================
    /**
     * 发送 PUT 请求，用于更新数据
     * @param url 请求地址
     * @param jsonBody 更新的对象
     * @return 返回响应字符串
     */
    public static String put(String url, Object jsonBody) {
        try {
            return Unirest.put(url)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString()
                    .getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== DELETE 请求（删除数据） ====================
    /**
     * 发送 DELETE 请求，删除数据
     * @param url 请求地址
     * @return 返回响应字符串
     */
    public static String delete(String url) {
        try {
            return Unirest.delete(url)
                    .asString()
                    .getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== 带 Token 认证的 GET 请求 ====================

    /**
     * 发送带 Token 认证的 GET 请求
     */
    public static String getWithAuth(String url, String token) {
        try {
            HttpResponse<String> response = Unirest.get(url)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .asString();
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送带 Token 认证和查询参数的 GET 请求
     */
    public static String getWithAuth(String url, Map<String, Object> params, String token) {
        try {
            HttpResponse<String> response = Unirest.get(url)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .queryString(params)
                    .asString();
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== 带 Token 认证的 POST 请求 ====================

    /**
     * 发送带 Token 认证的 POST 请求
     */
    public static String postWithAuth(String url, Object jsonBody, String token) {
        try {
            HttpResponse<String> response = Unirest.post(url)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(jsonBody)
                    .asString();
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== 返回原始 HttpResponse 的方法（用于状态码检测） ====================

    /**
     * 发送带 Token 的 GET 请求，返回原始 HttpResponse 用于状态码检测
     */
    public static HttpResponse<String> getRaw(String url, Map<String, Object> params, String token) {
        try {
            if (token != null && !token.isEmpty()) {
                return Unirest.get(url)
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .queryString(params)
                        .asString();
            } else {
                return Unirest.get(url)
                        .header("Accept", "application/json")
                        .queryString(params)
                        .asString();
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送带 Token 的 POST 请求，返回原始 HttpResponse 用于状态码检测
     */
    public static HttpResponse<String> postRaw(String url, Object jsonBody, String token) {
        try {
            // 使用 Jackson 手动序列化，避免 Unirest 自动序列化失败
            String jsonStr = objectMapper.writeValueAsString(jsonBody);
            if (token != null && !token.isEmpty()) {
                return Unirest.post(url)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .body(jsonStr)
                        .asString();
            } else {
                return Unirest.post(url)
                        .header("Content-Type", "application/json")
                        .body(jsonStr)
                        .asString();
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送不带 Body 的带 Token POST 请求（用于 URL 路径参数）
     */
    public static HttpResponse<String> postRaw(String url, String token) {
        try {
            if (token != null && !token.isEmpty()) {
                return Unirest.post(url)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .asString();
            } else {
                return Unirest.post(url)
                        .header("Content-Type", "application/json")
                        .asString();
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== 关闭 Unirest 资源 ====================

    /**
     * 关闭 Unirest 客户端，释放线程资源
     * JavaFX 程序退出时调用
     */
    public static void shutdown() {
        Unirest.shutDown();
    }
}