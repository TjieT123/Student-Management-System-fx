package cn.edu.sdu.sms.fx.smsfx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * 导航管理器 —— 管理场景切换、导航历史和页面间参数传递
 */
public class NavigationManager {

    private static Stage primaryStage;
    private static final Deque<NavEntry> history = new ArrayDeque<>();
    private static String currentFxmlPath;
    private static Consumer<Object> currentConfigurer;
    private static boolean isHomePage;

    private static class NavEntry {
        final String fxmlPath;
        final Consumer<Object> configurer;
        final boolean isHome;

        NavEntry(String fxmlPath, Consumer<Object> configurer, boolean isHome) {
            this.fxmlPath = fxmlPath;
            this.configurer = configurer;
            this.isHome = isHome;
        }
    }

    private NavigationManager() {}

    /**
     * 初始化导航管理器（应用启动时调用一次）
     */
    public static void init(Stage stage) {
        primaryStage = stage;
    }

    /**
     * 通用页面导航
     * @param fxmlPath FXML 文件路径（相对于 resources 目录）
     * @param configurer 控制器配置器 Lambda，用于传递参数
     */
    public static <T> void navigateTo(String fxmlPath, Consumer<T> configurer) {
        navigateInternal(fxmlPath, configurer, false);
    }

    /**
     * 导航到主页（会清空历史栈）
     */
    public static <T> void navigateToHome(String fxmlPath, Consumer<T> configurer) {
        navigateInternal(fxmlPath, configurer, true);
    }

    @SuppressWarnings("unchecked")
    private static <T> void navigateInternal(String fxmlPath, Consumer<T> configurer, boolean isHome) {
        try {
            // 如果当前有页面，则将当前页压入历史栈（主页导航除外）
            if (currentFxmlPath != null && !isHome) {
                history.push(new NavEntry(currentFxmlPath, currentConfigurer, isHomePage));
            }

            // 如果是主页导航，清空历史栈
            if (isHome) {
                history.clear();
            }

            // 加载新 FXML
            FXMLLoader loader = new FXMLLoader(
                    NavigationManager.class.getResource("/cn/edu/sdu/sms/fx/smsfx/fxml/" + fxmlPath));
            Parent root = loader.load();

            // 配置控制器
            if (configurer != null) {
                T controller = loader.getController();
                configurer.accept(controller);
            }

            // 创建场景并设置
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

            // 更新当前状态
            currentFxmlPath = fxmlPath;
            currentConfigurer = (Consumer<Object>) configurer;
            isHomePage = isHome;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("页面导航失败: " + fxmlPath, e);
        }
    }

    /**
     * 返回上一页
     */
    public static void goBack() {
        if (history.isEmpty()) {
            return;
        }

        NavEntry entry = history.pop();
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationManager.class.getResource("/cn/edu/sdu/sms/fx/smsfx/fxml/" + entry.fxmlPath));
            Parent root = loader.load();

            if (entry.configurer != null) {
                Object controller = loader.getController();
                entry.configurer.accept(controller);
            }

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

            currentFxmlPath = entry.fxmlPath;
            currentConfigurer = entry.configurer;
            isHomePage = entry.isHome;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到登录页面
     */
    public static void goToLogin() {
        history.clear();
        currentFxmlPath = null;
        currentConfigurer = null;
        isHomePage = false;

        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationManager.class.getResource("/cn/edu/sdu/sms/fx/smsfx/fxml/login-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前是否在主页
     */
    public static boolean isAtHome() {
        return isHomePage;
    }
}
