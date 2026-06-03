package cn.edu.sdu.sms.fx.smsfx.util;

import cn.edu.sdu.sms.fx.smsfx.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import kong.unirest.HttpResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 中央 API 客户端 —— 所有 HTTP 请求的门面
 * 处理 Token 注入、401 自动刷新、JSON 序列化/反序列化
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:1010";
    private static final ObjectMapper mapper = new ObjectMapper();

    private ApiClient() {}

    // ==================== 内部工具方法 ====================

    private static String url(String path) {
        return BASE_URL + path;
    }

    /**
     * 将对象序列化为 JSON 字符串
     */
    private static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new ApiException(500, "JSON序列化失败: " + e.getMessage());
        }
    }

    /**
     * 从 JSON 响应中提取 data 字段
     */
    private static JsonNode parseResponse(String responseJson) {
        if (responseJson == null || responseJson.isEmpty()) {
            throw new ApiException(500, "服务器无响应");
        }
        try {
            JsonNode root = mapper.readTree(responseJson);
            JsonNode codeNode = root.get("code");
            if (codeNode == null) {
                // 响应中没有 code 字段，可能返回了错误页面或非标准格式
                String body = responseJson.length() > 200 ?
                        responseJson.substring(0, 200) + "..." : responseJson;
                throw new ApiException(500, "响应格式异常: " + body);
            }
            int code = codeNode.asInt();
            if (code != 200) {
                String msg = root.has("msg") ? root.get("msg").asText() : "未知错误";
                throw new ApiException(code, msg);
            }
            return root.get("data");
        } catch (JsonProcessingException e) {
            String body = responseJson.length() > 200 ?
                    responseJson.substring(0, 200) + "..." : responseJson;
            throw new ApiException(500, "响应解析失败: " + e.getMessage() + "\n原始响应: " + body);
        }
    }

    /**
     * 从 data JsonNode 转换为目标类型
     */
    private static <T> T convertData(JsonNode dataNode, Class<T> dataClass) {
        if (dataNode == null || dataNode.isNull()) return null;
        try {
            return mapper.treeToValue(dataNode, dataClass);
        } catch (JsonProcessingException e) {
            throw new ApiException(500, "数据转换失败: " + e.getMessage());
        }
    }

    /**
     * 从 data JsonNode 转换为泛型类型（用于 PageResult）
     */
    private static <T> T convertData(JsonNode dataNode, com.fasterxml.jackson.core.type.TypeReference<T> typeRef) {
        if (dataNode == null || dataNode.isNull()) return null;
        try {
            return mapper.readValue(mapper.treeAsTokens(dataNode), typeRef);
        } catch (Exception e) {
            throw new ApiException(500, "数据转换失败: " + e.getMessage());
        }
    }

    /**
     * 执行带 Token 的请求，自动处理 401 刷新
     */
    private static <T> T executeWithAuth(Class<T> dataClass, RequestExecutor executor) {
        String token = SessionManager.getToken();
        HttpResponse<String> response = executor.execute(token);

        if (response == null) {
            throw new ApiException(500, "网络连接失败，请检查网络");
        }

        // 401 处理：尝试刷新 Token
        if (response.getStatus() == 401) {
            boolean refreshed = tryRefreshToken();
            if (refreshed) {
                // 用新 Token 重试一次
                response = executor.execute(SessionManager.getToken());
                if (response == null) {
                    throw new ApiException(500, "网络连接失败，请检查网络");
                }
            } else {
                // 刷新失败，跳转登录页
                Platform.runLater(() -> {
                    SessionManager.clear();
                    NavigationManager.goToLogin();
                    showAlert(Alert.AlertType.ERROR, "登录已过期", "请重新登录");
                });
                return null;
            }
        }

        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, dataClass);
    }

    /**
     * 尝试刷新 Token
     */
    private static boolean tryRefreshToken() {
        String refreshToken = SessionManager.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            return false;
        }
        try {
            RefreshTokenRequest req = new RefreshTokenRequest(refreshToken);
            HttpResponse<String> response = UnirestRequest.postRaw(url("/auth/refresh"), req, null);
            if (response != null && response.getStatus() == 200) {
                JsonNode dataNode = parseResponse(response.getBody());
                if (dataNode != null && dataNode.has("token")) {
                    String newToken = dataNode.get("token").asText();
                    SessionManager.updateToken(newToken);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FunctionalInterface
    private interface RequestExecutor {
        HttpResponse<String> execute(String token);
    }

    // ==================== 认证接口 ====================

    /**
     * 用户登录
     */
    public static LoginResponse login(String username, String password) {
        LoginRequest req = new LoginRequest(username, password);
        HttpResponse<String> response = UnirestRequest.postRaw(url("/auth/login"), req, null);
        if (response == null) {
            throw new ApiException(500, "网络连接失败，请检查网络");
        }
        JsonNode root;
        try {
            root = mapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new ApiException(500, "响应解析失败: " + e.getMessage());
        }
        int code = root.get("code").asInt();
        if (code != 200) {
            String msg = root.has("msg") ? root.get("msg").asText() : "未知错误";
            throw new ApiException(code, msg);
        }
        try {
            JsonNode dataNode = root.get("data");
            LoginData loginData = mapper.treeToValue(dataNode, LoginData.class);
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setCode(code);
            loginResponse.setData(loginData);
            loginResponse.setMsg(root.get("msg").asText());
            return loginResponse;
        } catch (Exception e) {
            throw new ApiException(500, "数据解析失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    public static User register(RegisterRequest req) {
        HttpResponse<String> response = UnirestRequest.postRaw(url("/auth/register"), req, null);
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, User.class);
    }

    /**
     * 修改密码
     */
    public static boolean changePassword(String oldPassword, String newPassword) {
        ChangePasswordRequest req = new ChangePasswordRequest(oldPassword, newPassword);
        HttpResponse<String> response = UnirestRequest.postRaw(url("/auth/change-password"), req,
                SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        parseResponse(response.getBody()); // 仅校验 code==200，不关心 data
        return true;
    }

    // ==================== 课程接口 ====================

    /**
     * 分页获取课程列表（支持筛选）
     */
    public static PageResult<Course> getCourseList(int page, int pageSize,
                                                    Integer id, String courseName, String teacherId) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        if (id != null) params.put("id", id);
        if (courseName != null && !courseName.isEmpty()) params.put("courseName", courseName);
        if (teacherId != null && !teacherId.isEmpty()) params.put("teacherId", teacherId);

        HttpResponse<String> response = UnirestRequest.getRaw(url("/api/course/list"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<Course>>() {});
    }

    /**
     * 获取课程详情
     */
    public static Course getCourseDetail(Integer id) {
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/course/" + id), new HashMap<>(), SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, Course.class);
    }

    /**
     * 学生选课
     */
    public static boolean enroll(Integer courseId) {
        EnrollRequest req = new EnrollRequest(courseId);
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/course/enroll"), req, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.postRaw(url("/api/course/enroll"), req, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return false;
            }
        }
        parseResponse(response.getBody());
        return true;
    }

    /**
     * 取消选课
     */
    public static boolean cancelEnroll(Integer courseId) {
        EnrollRequest req = new EnrollRequest(courseId);
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/course/cancel"), req, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.postRaw(url("/api/course/cancel"), req, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return false;
            }
        }
        parseResponse(response.getBody());
        return true;
    }

    /**
     * 获取我的选课列表（学生）
     */
    public static PageResult<Course> getMyCourses(int page, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/course/my-courses"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.getRaw(url("/api/course/my-courses"), params, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<Course>>() {});
    }

    /**
     * 获取教师所教课程列表
     */
    public static PageResult<Course> getTeacherCourses(int page, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/course/teacher-courses"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.getRaw(url("/api/course/teacher-courses"), params, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<Course>>() {});
    }

    /**
     * 获取课程选课学生列表（仅任课教师可查看）
     */
    public static List<Student> getEnrolledStudents(Integer courseId) {
        Map<String, Object> params = new HashMap<>();
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/course/" + courseId + "/students"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.getRaw(
                        url("/api/course/" + courseId + "/students"), params, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<List<Student>>() {});
    }

    /**
     * 修改课程
     */
    public static Course updateCourse(Integer id, String courseName, String detail,
                                       String address, String teacherId) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", id);
        if (courseName != null) body.put("courseName", courseName);
        if (detail != null) body.put("detail", detail);
        if (address != null) body.put("address", address);
        if (teacherId != null) body.put("teacherId", teacherId);

        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/course/update"), body, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, Course.class);
    }

    // ==================== 教师作业接口 ====================

    /**
     * 发布作业
     */
    public static Homework publishHomework(PublishHomeworkRequest req) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/teacher/homework/publish"), req, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.postRaw(
                        url("/api/teacher/homework/publish"), req, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, Homework.class);
    }

    /**
     * 获取作业列表（教师端，全局）
     */
    public static PageResult<Homework> getHomeworkList(int page, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/teacher/homework/list"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.getRaw(
                        url("/api/teacher/homework/list"), params, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<Homework>>() {});
    }

    /**
     * 获取作业提交列表
     */
    public static PageResult<HomeworkSubmit> getSubmitList(int homeworkId, int page, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("homeworkId", homeworkId);
        params.put("page", page);
        params.put("pageSize", pageSize);
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/teacher/homework/submit/list"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<HomeworkSubmit>>() {});
    }

    /**
     * 批改作业
     */
    public static HomeworkSubmit checkHomework(CheckHomeworkRequest req) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/teacher/homework/check"), req, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.postRaw(
                        url("/api/teacher/homework/check"), req, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, HomeworkSubmit.class);
    }

    /**
     * 获取提交详情（教师端，含 content 和 comment）
     */
    public static HomeworkSubmit getSubmissionDetail(Integer submissionId) {
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/teacher/homework/submission/" + submissionId), new HashMap<>(),
                SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.getRaw(
                        url("/api/teacher/homework/submission/" + submissionId), new HashMap<>(),
                        SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, HomeworkSubmit.class);
    }

    /**
     * AI 判卷
     */
    public static AiGradeResult aiGrade(Integer submitId, String homeworkTitle, String homeworkContent) {
        Map<String, Object> body = new HashMap<>();
        body.put("submitId", submitId);
        body.put("homeworkTitle", homeworkTitle);
        body.put("homeworkContent", homeworkContent);
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/teacher/homework/ai-grade"), body, SessionManager.getToken());
        if (response == null) {
            throw new ApiException(500, "网络连接失败（AI判卷耗时较长，请确认后端AI服务已启动且超时设置充足）");
        }
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.postRaw(
                        url("/api/teacher/homework/ai-grade"), body, SessionManager.getToken());
                if (response == null) {
                    throw new ApiException(500, "网络连接失败");
                }
            } else {
                handleAuthFailure();
                return null;
            }
        }
        // 检查非 200 状态码（如 503 AI 服务不可用）
        if (response.getStatus() != 200) {
            String bodyPreview = response.getBody();
            if (bodyPreview != null && bodyPreview.length() > 200) {
                bodyPreview = bodyPreview.substring(0, 200);
            }
            throw new ApiException(response.getStatus(),
                    "AI判卷服务异常 (HTTP " + response.getStatus() + "): " + bodyPreview);
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, AiGradeResult.class);
    }

    /**
     * AI 学习建议（学生端）
     */
    public static AiSuggestionResult getAiSuggestion(Integer submissionId) {
        Map<String, Object> body = new HashMap<>();
        body.put("submissionId", submissionId);
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/student/homework/ai-suggestion"), body, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败（AI建议生成耗时较长，请稍后重试）");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.postRaw(
                        url("/api/student/homework/ai-suggestion"), body, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        if (response.getStatus() != 200) {
            throw new ApiException(response.getStatus(), "AI建议生成失败，请稍后重试");
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, AiSuggestionResult.class);
    }

    /**
     * 获取作业提交统计（教师端）
     */
    public static HomeworkStatistics getHomeworkStatistics(Integer homeworkId) {
        Map<String, Object> params = new HashMap<>();
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/teacher/homework/" + homeworkId + "/statistics"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.getRaw(
                        url("/api/teacher/homework/" + homeworkId + "/statistics"), params, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, HomeworkStatistics.class);
    }

    // ==================== 学生作业接口 ====================

    /**
     * 获取课程作业列表（学生端，含提交状态）
     */
    public static PageResult<StudentHomeworkItem> getStudentHomeworkList(int courseId, int page, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("courseId", courseId);
        params.put("page", page);
        params.put("pageSize", pageSize);
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/student/homework/list"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.getRaw(
                        url("/api/student/homework/list"), params, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<StudentHomeworkItem>>() {});
    }

    /**
     * 获取作业内容（含题目要求、截止时间、提交状态等信息）
     */
    public static StudentHomeworkItem getHomeworkContent(Integer homeworkId) {
        Map<String, Object> params = new HashMap<>();
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/student/homework/" + homeworkId + "/content"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.getRaw(
                        url("/api/student/homework/" + homeworkId + "/content"), params, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, StudentHomeworkItem.class);
    }

    /**
     * 获取学生自己的作业提交记录
     */
    public static HomeworkSubmit getMySubmission(Integer homeworkId) {
        Map<String, Object> params = new HashMap<>();
        params.put("homeworkId", homeworkId);
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/student/homework/my-submission"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.getRaw(
                        url("/api/student/homework/my-submission"), params, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, HomeworkSubmit.class);
    }

    /**
     * 提交作业
     */
    public static HomeworkSubmit submitHomework(SubmitHomeworkRequest req) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/student/homework/submit"), req, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.postRaw(
                        url("/api/student/homework/submit"), req, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, HomeworkSubmit.class);
    }

    /**
     * 获取提交详情（学生端）
     */
    public static HomeworkSubmit getStudentSubmissionDetail(Integer submissionId) {
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/student/homework/submission/" + submissionId), new HashMap<>(), SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, HomeworkSubmit.class);
    }

    // ==================== 学生接口 ====================

    /**
     * 分页获取所有学生
     */
    public static PageResult<Student> getAllStudents(int page, int pageSize,
                                                      String sid, String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        if (sid != null && !sid.isEmpty()) params.put("sid", sid);
        if (name != null && !name.isEmpty()) params.put("name", name);
        HttpResponse<String> response = UnirestRequest.getRaw(url("/getAll"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<Student>>() {});
    }

    // ==================== 公告接口 ====================

    /**
     * 分页获取公告列表
     */
    public static PageResult<Announcement> getAnnouncementList(int page, int pageSize,
                                                                String id, String title) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        if (id != null && !id.isEmpty()) params.put("id", id);
        if (title != null && !title.isEmpty()) params.put("title", title);
        HttpResponse<String> response = UnirestRequest.getRaw(url("/api/announcement/list"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<Announcement>>() {});
    }

    /**
     * 获取公告详情
     */
    public static Announcement getAnnouncementDetail(Integer id) {
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/announcement/" + id), new HashMap<>(), SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, Announcement.class);
    }

    /**
     * 发布公告
     */
    public static boolean publishAnnouncement(String title, String content, String publisherName) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("content", content);
        body.put("publisherName", publisherName);
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/announcement/publish"), body, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.postRaw(
                        url("/api/announcement/publish"), body, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return false;
            }
        }
        parseResponse(response.getBody());
        return true;
    }

    /**
     * 删除公告
     */
    public static boolean deleteAnnouncement(Integer id) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/announcement/delete/" + id), SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.postRaw(
                        url("/api/announcement/delete/" + id), SessionManager.getToken());
            } else {
                handleAuthFailure();
                return false;
            }
        }
        parseResponse(response.getBody());
        return true;
    }

    // ==================== 管理员接口 ====================

    /**
     * 获取管理员用户列表（按角色）
     */
    public static PageResult<AdminUserVO> getAdminUserList(String role, int page, int pageSize,
                                                            String schId, String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        if (schId != null && !schId.isEmpty()) params.put("schId", schId);
        if (name != null && !name.isEmpty()) params.put("name", name);
        String endpoint;
        switch (role) {
            case "TEACHER": endpoint = "/api/admin/user/teacher/list"; break;
            case "STUDENT": endpoint = "/api/admin/user/student/list"; break;
            case "ADMIN": endpoint = "/api/admin/user/admin/list"; break;
            default: throw new IllegalArgumentException("Invalid role: " + role);
        }
        HttpResponse<String> response = UnirestRequest.getRaw(url(endpoint), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        if (response.getStatus() == 401) {
            if (tryRefreshToken()) {
                response = UnirestRequest.getRaw(url(endpoint), params, SessionManager.getToken());
            } else {
                handleAuthFailure();
                return null;
            }
        }
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<AdminUserVO>>() {});
    }

    /**
     * 获取用户详情
     */
    public static User getUserDetail(Integer id) {
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/admin/user/" + id), new HashMap<>(), SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, User.class);
    }

    /**
     * 添加用户
     */
    public static User addUser(Map<String, Object> userData) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/user/add"), userData, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, User.class);
    }

    /**
     * 修改用户
     */
    public static User updateUser(Map<String, Object> userData) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/user/update"), userData, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, User.class);
    }

    /**
     * 删除用户
     */
    public static boolean deleteUser(Integer userId) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/user/delete/" + userId), SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        parseResponse(response.getBody());
        return true;
    }

    /**
     * 获取所有教师列表（不分页，供下拉框等使用）
     */
    public static List<Teacher> getAllTeachers(String schId, String name) {
        PageResult<Teacher> result = getAllTeachersPaged(1, 9999, schId, name);
        return result != null ? result.getList() : null;
    }

    /**
     * 分页获取教师列表
     */
    public static PageResult<Teacher> getAllTeachersPaged(int page, int pageSize,
                                                           String schId, String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        if (schId != null && !schId.isEmpty()) params.put("schId", schId);
        if (name != null && !name.isEmpty()) params.put("name", name);
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/admin/teacher/list"), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<Teacher>>() {});
    }

    /**
     * 添加教师
     */
    public static Teacher addTeacher(String schId, String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("sch_id", schId);
        body.put("name", name);
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/teacher/add"), body, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, Teacher.class);
    }

    /**
     * 修改教师
     */
    public static Teacher updateTeacher(String schId, String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("sch_id", schId);
        body.put("name", name);
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/teacher/update"), body, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, Teacher.class);
    }

    /**
     * 删除教师（级联删除 user）
     */
    public static boolean deleteTeacher(String schId) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/teacher/delete/" + schId), SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        parseResponse(response.getBody());
        return true;
    }

    /**
     * 添加学生（仅 student 表）
     */
    public static Student addStudent(Map<String, Object> studentData) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/student/add"), studentData, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, Student.class);
    }

    /**
     * 修改学生
     */
    public static Student updateStudent(Map<String, Object> studentData) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/student/update"), studentData, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, Student.class);
    }

    /**
     * 删除学生（级联删除 user）
     */
    public static boolean deleteStudent(String sid) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/student/delete/" + sid), SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        parseResponse(response.getBody());
        return true;
    }

    /**
     * 添加学生用户（原子操作：user + student）
     */
    public static Map<String, Object> addStudentUser(Map<String, Object> data) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/student-user/add"), data, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 添加课程（管理员）
     */
    public static Course adminAddCourse(Map<String, Object> courseData) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/course/add"), courseData, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, Course.class);
    }

    /**
     * 修改课程（管理员）
     */
    public static Course adminUpdateCourse(Map<String, Object> courseData) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/course/update"), courseData, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, Course.class);
    }

    /**
     * 删除课程（管理员）
     */
    public static boolean adminDeleteCourse(Integer id) {
        HttpResponse<String> response = UnirestRequest.postRaw(
                url("/api/admin/course/delete/" + id), SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        parseResponse(response.getBody());
        return true;
    }

    /**
     * 管理员查询教师课程
     */
    public static PageResult<Course> getTeacherCoursesByAdmin(String teacherId, int page, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        HttpResponse<String> response = UnirestRequest.getRaw(
                url("/api/admin/course/teacher/" + teacherId), params, SessionManager.getToken());
        if (response == null) throw new ApiException(500, "网络连接失败");
        JsonNode dataNode = parseResponse(response.getBody());
        return convertData(dataNode, new TypeReference<PageResult<Course>>() {});
    }

    // ==================== 通用错误处理 ====================

    /**
     * 处理认证失败：清理会话并跳转到登录页
     */
    private static void handleAuthFailure() {
        Platform.runLater(() -> {
            SessionManager.clear();
            NavigationManager.goToLogin();
            showAlert(Alert.AlertType.ERROR, "登录已过期", "请重新登录");
        });
    }
}
