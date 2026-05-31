package cn.edu.sdu.sms.fx.smsfx.util;

import cn.edu.sdu.sms.fx.smsfx.models.User;

/**
 * 会话管理器 —— 单例模式，在内存中存储当前用户会话信息
 */
public class SessionManager {

    private static User currentUser;
    private static String token;
    private static String refreshToken;

    private SessionManager() {}

    /**
     * 设置登录会话
     */
    public static void setSession(User user, String token, String refreshToken) {
        SessionManager.currentUser = user;
        SessionManager.token = token;
        SessionManager.refreshToken = refreshToken;
    }

    /**
     * 仅更新 token（刷新后使用）
     */
    public static void updateToken(String newToken) {
        SessionManager.token = newToken;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getToken() {
        return token;
    }

    public static String getRefreshToken() {
        return refreshToken;
    }

    public static String getRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    /**
     * 清除会话（退出登录时调用）
     */
    public static void clear() {
        currentUser = null;
        token = null;
        refreshToken = null;
    }
}
