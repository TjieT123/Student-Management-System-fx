package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.ApiException;
import cn.edu.sdu.sms.fx.smsfx.models.RegisterRequest;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

/**
 * 注册对话框控制器
 */
public class RegisterController {

    /**
     * 显示注册对话框
     */
    public static void showDialog() {
        Dialog<RegisterRequest> dialog = new Dialog<>();
        dialog.setTitle("用户注册");
        dialog.setHeaderText("请输入注册信息");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // 按钮
        ButtonType registerButtonType = new ButtonType("注册", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        // 表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("用户名");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("密码");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("确认密码");
        TextField nameField = new TextField();
        nameField.setPromptText("姓名");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("STUDENT", "TEACHER");
        roleCombo.setValue("STUDENT");
        roleCombo.setPromptText("角色");

        TextField phoneField = new TextField();
        phoneField.setPromptText("手机号");
        TextField schIdField = new TextField();
        schIdField.setPromptText("工号/学号");

        grid.add(new Label("用户名:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("密码:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("确认密码:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(new Label("姓名:"), 0, 3);
        grid.add(nameField, 1, 3);
        grid.add(new Label("角色:"), 0, 4);
        grid.add(roleCombo, 1, 4);
        grid.add(new Label("手机号:"), 0, 5);
        grid.add(phoneField, 1, 5);
        grid.add(new Label("工号/学号:"), 0, 6);
        grid.add(schIdField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // 请求焦点
        Platform.runLater(usernameField::requestFocus);

        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                // 验证
                if (usernameField.getText().trim().isEmpty()
                        || passwordField.getText().isEmpty()
                        || nameField.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "提示", "请填写所有必填字段");
                    return null;
                }
                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert(Alert.AlertType.WARNING, "提示", "两次输入的密码不一致");
                    return null;
                }

                RegisterRequest req = new RegisterRequest();
                req.setUsername(usernameField.getText().trim());
                req.setPassword(passwordField.getText());
                req.setName(nameField.getText().trim());
                req.setRole(roleCombo.getValue());
                req.setPhone(phoneField.getText().trim());
                req.setSchId(schIdField.getText().trim());
                return req;
            }
            return null;
        });

        // 处理注册
        var result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                ApiClient.register(req);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("注册成功");
                alert.setHeaderText(null);
                alert.setContentText("注册成功，请登录");
                alert.showAndWait();
            } catch (ApiException e) {
                if (e.getCode() == 409) {
                    showAlert(Alert.AlertType.ERROR, "注册失败", "用户名已存在");
                } else {
                    showAlert(Alert.AlertType.ERROR, "注册失败", e.getMessage());
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "网络错误", "网络连接失败，请检查网络");
            }
        });
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
