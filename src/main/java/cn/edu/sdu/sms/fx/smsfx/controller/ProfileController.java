package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.User;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

public class ProfileController extends BaseController {

    @FXML private Label avatarIcon;
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Label infoNameLabel;
    @FXML private Label schIdLabel;
    @FXML private Label phoneLabel;
    @FXML private TextField phoneEditField;
    @FXML private Button editPhoneBtn;
    @FXML private Label infoRoleLabel;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordBtn;
    @FXML private Button logoutBtn;

    private boolean editingPhone = false;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        enableBackButton();

        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        // 头像
        avatarIcon.setText(roleToIcon(user.getRole()));
        nameLabel.setText(user.getName());
        roleLabel.setText(roleToChinese(user.getRole()));

        // 基本信息
        infoNameLabel.setText(user.getName());
        schIdLabel.setText(user.getSchId() != null ? user.getSchId() : "");
        phoneLabel.setText(user.getPhone() != null ? user.getPhone() : "");
        infoRoleLabel.setText(roleToChinese(user.getRole()));

        // 修改手机号
        editPhoneBtn.setOnAction(e -> handlePhoneEdit());

        // 修改密码
        changePasswordBtn.setOnAction(e -> handleChangePassword());

        // 退出登录
        logoutBtn.setOnAction(e -> handleLogout());
    }

    private void handlePhoneEdit() {
        if (!editingPhone) {
            // 进入编辑模式
            editingPhone = true;
            phoneLabel.setVisible(false);
            phoneLabel.setManaged(false);
            phoneEditField.setText(phoneLabel.getText());
            phoneEditField.setVisible(true);
            phoneEditField.setManaged(true);
            editPhoneBtn.setText("保存");
            editPhoneBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12;");
        } else {
            // 保存
            String newPhone = phoneEditField.getText().trim();
            if (newPhone.isEmpty()) {
                showWarning("请输入手机号");
                return;
            }
            try {
                User user = SessionManager.getCurrentUser();
                Map<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("phone", newPhone);
                ApiClient.updateUser(data);
                showInfo("手机号修改成功");
                // 更新本地缓存
                user.setPhone(newPhone);
                phoneLabel.setText(newPhone);
                // 退出编辑模式
                exitPhoneEditMode();
            } catch (Exception ex) {
                showError("修改失败: " + ex.getMessage());
            }
        }
    }

    private void exitPhoneEditMode() {
        editingPhone = false;
        phoneLabel.setVisible(true);
        phoneLabel.setManaged(true);
        phoneEditField.setVisible(false);
        phoneEditField.setManaged(false);
        editPhoneBtn.setText("修改");
        editPhoneBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 12;");
    }

    private void handleChangePassword() {
        String oldPwd = oldPasswordField.getText();
        String newPwd = newPasswordField.getText();
        String confirmPwd = confirmPasswordField.getText();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            showWarning("请填写所有密码字段");
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            showWarning("两次输入的新密码不一致");
            return;
        }

        try {
            ApiClient.changePassword(oldPwd, newPwd);
            showInfo("密码修改成功");
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } catch (Exception ex) {
            showError("旧密码错误");
        }
    }

    private void handleLogout() {
        if (showConfirm("退出登录", "确定要退出登录吗？")) {
            SessionManager.clear();
            NavigationManager.goToLogin();
        }
    }
}
