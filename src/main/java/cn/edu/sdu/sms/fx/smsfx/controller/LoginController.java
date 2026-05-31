package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.ApiException;
import cn.edu.sdu.sms.fx.smsfx.models.LoginData;
import cn.edu.sdu.sms.fx.smsfx.models.LoginResponse;
import cn.edu.sdu.sms.fx.smsfx.models.User;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

public class LoginController {

    @FXML private AnchorPane rootPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    /**
     * 初始化 - 设置全屏背景图片，并按图片比例调整窗口
     */
    @FXML
    public void initialize() {
        try {
            Image bgImage = new Image(
                    getClass().getResource("/cn/edu/sdu/sms/fx/smsfx/sdu.jpg").toExternalForm());
            BackgroundImage bg = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, false, true)
            );
            rootPane.setBackground(new Background(bg));

            // 按图片比例调整窗口大小
            double imgW = bgImage.getWidth();
            double imgH = bgImage.getHeight();
            if (imgW > 0 && imgH > 0) {
                double ratio = imgW / imgH;
                // 以图片高度 700 为基准计算宽度
                double winH = 700;
                double winW = winH * ratio;
                javafx.application.Platform.runLater(() -> {
                    rootPane.getScene().getWindow().setWidth(winW);
                    rootPane.getScene().getWindow().setHeight(winH);
                    rootPane.getScene().getWindow().centerOnScreen();
                });
            }
        } catch (Exception e) {
            System.err.println("背景图片加载失败: " + e.getMessage());
        }
    }

    /**
     * 处理登录按钮点击
     */
    @FXML
    protected void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "提示", "请输入用户名和密码");
            return;
        }

        try {
            LoginResponse loginResponse = ApiClient.login(username, password);
            LoginData data = loginResponse.getData();
            User user = data.getUser();

            // 存储会话信息
            SessionManager.setSession(user, data.getToken(), data.getRefreshToken());

            // 根据角色跳转
            String role = user.getRole();
            switch (role) {
                case "ADMIN":
                    NavigationManager.navigateToHome("admin-home-view.fxml", null);
                    break;
                case "TEACHER":
                    NavigationManager.navigateToHome("teacher-home-view.fxml", null);
                    break;
                case "STUDENT":
                    NavigationManager.navigateToHome("student-home-view.fxml", null);
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, "错误", "未知的用户角色: " + role);
            }
        } catch (ApiException e) {
            if (e.getCode() == 401) {
                showAlert(Alert.AlertType.ERROR, "登录失败", "用户名或密码错误");
            } else {
                showAlert(Alert.AlertType.ERROR, "登录失败", e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "网络错误", "网络连接失败，请检查网络\n\n详细信息: " + e.getMessage());
        }
    }

    /**
     * 显示注册对话框
     */
    @FXML
    protected void showRegisterDialog() {
        RegisterController.showDialog();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
