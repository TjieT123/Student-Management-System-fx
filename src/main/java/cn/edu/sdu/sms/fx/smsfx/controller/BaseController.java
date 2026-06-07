package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.LoginData;
import cn.edu.sdu.sms.fx.smsfx.models.LoginResponse;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * 基础控制器 —— 所有页面控制器（除登录外）的父类
 * 提供顶部栏按钮绑定、导航方法和弹窗工具
 */
public abstract class BaseController {

    @FXML protected Button backButton;
    @FXML protected Label titleLabel;
    @FXML protected Button profileButton;

    /**
     * 初始化顶部栏事件（子类需在 initialize() 中调用 super.initialize()）
     */
    @FXML
    public void initialize() {
        // 默认隐藏返回按钮（主页不需要）
        if (backButton != null) {
            backButton.setVisible(false);
            backButton.setManaged(false);
        }
        // 点击标题回到主页
        if (titleLabel != null) {
            titleLabel.setOnMouseClicked(e -> goHome());
            titleLabel.setCursor(Cursor.HAND);
        }
        // 个人中心按钮
        if (profileButton != null) {
            profileButton.setOnAction(e -> goToProfile());
        }

        // 添加用户切换下拉框到顶部栏
        addUserSwitchComboBox();

        // 添加底部信息栏
        addFooterBar();
    }

    /**
     * 在个人中心按钮左侧添加用户切换下拉框
     */
    private void addUserSwitchComboBox() {
        if (profileButton == null) return;
        // 防止重复添加
        if (profileButton.getParent() instanceof HBox topBar
                && topBar.lookup("#userSwitchCombo") != null) return;

        ComboBox<String> userCombo = new ComboBox<>();
        userCombo.setId("userSwitchCombo");
        userCombo.setPrefWidth(130);
        userCombo.setPromptText("切换账号");
        userCombo.getItems().addAll("admin", "student01", "teacher01");
        // 选中当前用户
        if (SessionManager.getCurrentUser() != null) {
            userCombo.setValue(SessionManager.getCurrentUser().getUsername());
        }
        userCombo.setOnAction(e -> {
            String username = userCombo.getValue();
            if (username == null || username.isEmpty()) return;
            if (username.equals(SessionManager.getCurrentUser().getUsername())) return;
            switchUser(username);
        });

        javafx.scene.Node profileNode = profileButton;
        if (profileNode.getParent() instanceof HBox topBar) {
            topBar.getChildren().add(topBar.getChildren().indexOf(profileNode), userCombo);
        }
    }

    /**
     * 切换用户：调用登录 API 获取新 Token，跳转对应主页
     */
    private void switchUser(String username) {
        try {
            LoginResponse loginResponse = ApiClient.login(username, "123456");
            LoginData data = loginResponse.getData();
            SessionManager.setSession(data.getUser(), data.getToken(), data.getRefreshToken());
            goHome();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "切换失败", "无法切换到用户: " + username + "\n" + e.getMessage());
        }
    }

    /**
     * 在页面底部添加信息栏
     */
    private void addFooterBar() {
        if (titleLabel == null || titleLabel.getParent() == null) return;
        // 防止重复添加
        javafx.scene.Node root = titleLabel.getScene() != null
                ? titleLabel.getScene().getRoot() : null;
        if (root instanceof VBox rootVBox
                && rootVBox.lookup("#systemFooter") != null) return;

        String footerText = "服务器：http://localhost:1010  数据库：java_2_52  "
                + "团队编号：52  成员：202500550148-余小鹿、202500550436-薛煜昕、202400201138-陶天杰、202420161250-李昊";

        Label footer = new Label(footerText);
        footer.setId("systemFooter");
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setStyle("-fx-font-size: 11; -fx-text-fill: #95a5a6; "
                + "-fx-background-color: #f8f9fa; -fx-padding: 6 15; "
                + "-fx-border-color: #ecf0f1; -fx-border-width: 1 0 0 0;");
        footer.setAlignment(javafx.geometry.Pos.CENTER);

        // 延迟添加，确保场景已就绪
        Platform.runLater(() -> {
            if (titleLabel.getScene() != null
                    && titleLabel.getScene().getRoot() instanceof VBox rootVBox) {
                // 给内容区设置 VBox.vgrow，使底部栏始终在底部
                if (rootVBox.getChildren().size() >= 2
                        && rootVBox.getChildren().get(1) instanceof javafx.scene.layout.Region content) {
                    VBox.setVgrow(content, Priority.ALWAYS);
                }
                rootVBox.getChildren().add(footer);
            }
        });
    }

    /**
     * 显示返回按钮并绑定行为
     */
    protected void enableBackButton() {
        if (backButton != null) {
            backButton.setVisible(true);
            backButton.setManaged(true);
            backButton.setOnAction(e -> goBack());
        }
    }

    /**
     * 返回主页（根据角色路由）
     */
    @FXML
    protected void goHome() {
        String role = SessionManager.getRole();
        if (role == null) {
            NavigationManager.goToLogin();
            return;
        }
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
                NavigationManager.goToLogin();
        }
    }

    /**
     * 跳转到个人中心
     */
    @FXML
    protected void goToProfile() {
        NavigationManager.navigateTo("profile-view.fxml", null);
    }

    /**
     * 返回上一页
     */
    @FXML
    protected void goBack() {
        NavigationManager.goBack();
    }

    // ==================== 弹窗工具方法 ====================

    protected void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "提示", message);
    }

    protected void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "错误", message);
    }

    protected void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "警告", message);
    }

    protected boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // ==================== 工具方法 ====================

    /**
     * 将日期时间字符串从 ISO 格式转换为中文格式
     * 支持 "YYYY-MM-DDTHH:mm:ss" 和 "YYYY-MM-DD HH:mm:ss" 两种输入
     * 输出格式: "YYYY年MM月DD日 HH:mm:ss"
     */
    protected String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return "";
        try {
            String normalized = dateTimeStr.replace("T", " ");
            String[] parts = normalized.split(" ");
            if (parts.length >= 2) {
                String[] dateParts = parts[0].split("-");
                if (dateParts.length == 3) {
                    return dateParts[0] + "年" + dateParts[1] + "月" + dateParts[2] + "日 " + parts[1];
                }
            }
            return normalized;
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    /**
     * 将角色英文转为中文
     */
    protected String roleToChinese(String role) {
        if (role == null) return "";
        switch (role) {
            case "ADMIN": return "管理员";
            case "TEACHER": return "教师";
            case "STUDENT": return "学生";
            default: return role;
        }
    }

    /**
     * 校验手机号：必须 11 位数字。返回 null 表示通过，否则返回错误信息。
     */
    protected String validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) return null; // 允许为空
        if (!phone.matches("\\d{11}")) return "手机号必须为11位数字";
        return null;
    }

    /**
     * 校验身份证号：18位格式 + 校验位算法 + 出生日期有效性。返回 null 表示通过，否则返回错误信息。
     */
    public static String validateIdCard(String id) {
        if (id == null || id.isEmpty()) return null; // 允许为空
        if (!id.matches("\\d{17}[\\dXx]")) return "身份证号必须为18位，前17位为数字，最后一位为数字或X";
        // 校验位算法
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] chk = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < 17; i++) sum += (id.charAt(i) - '0') * weights[i];
        if (Character.toUpperCase(id.charAt(17)) != chk[sum % 11]) return "身份证号校验位不正确";
        // 校验出生日期：不能超过今天，月份1-12，日期有效
        try {
            int year = Integer.parseInt(id.substring(6, 10));
            int month = Integer.parseInt(id.substring(10, 12));
            int day = Integer.parseInt(id.substring(12, 14));
            if (month < 1 || month > 12) return "身份证号中月份不合法";
            java.time.LocalDate birthDate = java.time.LocalDate.of(year, month, day); // 自动校验日期有效性
            if (birthDate.isAfter(java.time.LocalDate.now())) return "身份证号中出生日期不能超过当前日期";
        } catch (Exception e) { return "身份证号中出生日期不合法"; }
        return null;
    }

    /**
     * 校验正整数（> 0）。null/空返回 null（表示通过/不强制）
     */
    protected String validatePositiveInt(String text, String fieldName) {
        if (text == null || text.isEmpty()) return null;
        try {
            int val = Integer.parseInt(text);
            if (val <= 0) return fieldName + "必须为正整数";
        } catch (NumberFormatException e) {
            return fieldName + "格式不正确，请输入正整数";
        }
        return null;
    }

    /**
     * 校验分数：0-100 的整数
     */
    protected String validateScore(String text) {
        if (text == null || text.isEmpty()) return "请输入分数";
        try {
            int val = Integer.parseInt(text);
            if (val < 0 || val > 100) return "分数必须在0-100之间";
        } catch (NumberFormatException e) {
            return "分数格式不正确";
        }
        return null;
    }

    /**
     * 根据角色返回头像图标文字
     */
    protected String roleToIcon(String role) {
        if (role == null) return "?";
        switch (role) {
            case "ADMIN": return "管";
            case "TEACHER": return "教";
            case "STUDENT": return "学";
            default: return "?";
        }
    }
}
