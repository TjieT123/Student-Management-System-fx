package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.ApiException;
import cn.edu.sdu.sms.fx.smsfx.models.RegisterRequest;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * 注册对话框控制器
 */
public class RegisterController {

    public static void showDialog() {
        Dialog<RegisterRequest> dialog = new Dialog<>();
        dialog.setTitle("用户注册");
        dialog.setHeaderText("请输入注册信息");
        dialog.setResizable(true);

        ButtonType registerButtonType = new ButtonType("注册", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        int row = 0;

        TextField usernameField = new TextField();
        usernameField.setPromptText("用户名");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("密码");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("确认密码");
        TextField nameField = new TextField();
        nameField.setPromptText("姓名");

        // 身份选择（显示中文，传英文值）
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("学生", "老师");
        roleCombo.setValue("学生");

        TextField phoneField = new TextField();
        phoneField.setPromptText("手机号");

        Label schIdLabel = new Label("学号:");
        TextField schIdField = new TextField();
        schIdField.setPromptText("学号");

        // 学生专用字段
        Label majorLabel = new Label("专业:");
        TextField majorField = new TextField();
        majorField.setPromptText("专业");

        Label genderLabel = new Label("性别:");
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("男", "女");
        genderCombo.setValue("男");

        Label classLabel = new Label("班级:");
        TextField classField = new TextField();
        classField.setPromptText("班级（数字）");

        // 基础字段
        grid.add(new Label("用户名:"), 0, row);
        grid.add(usernameField, 1, row++);
        grid.add(new Label("密码:"), 0, row);
        grid.add(passwordField, 1, row++);
        grid.add(new Label("确认密码:"), 0, row);
        grid.add(confirmPasswordField, 1, row++);
        grid.add(new Label("姓名:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("身份:"), 0, row);
        grid.add(roleCombo, 1, row++);
        grid.add(new Label("手机号:"), 0, row);
        grid.add(phoneField, 1, row++);
        grid.add(schIdLabel, 0, row);
        grid.add(schIdField, 1, row++);

        // 学生专用字段行索引
        int majorRow = row;
        grid.add(majorLabel, 0, row);
        grid.add(majorField, 1, row++);
        int genderRow = row;
        grid.add(genderLabel, 0, row);
        grid.add(genderCombo, 1, row++);
        int classRow = row;
        grid.add(classLabel, 0, row);
        grid.add(classField, 1, row++);

        // 角色切换时：显示/隐藏学生字段，更改学号/工号标签
        roleCombo.setOnAction(e -> {
            boolean isStudent = "学生".equals(roleCombo.getValue());
            setRowVisible(grid, majorRow, isStudent);
            setRowVisible(grid, genderRow, isStudent);
            setRowVisible(grid, classRow, isStudent);
            schIdLabel.setText(isStudent ? "学号:" : "工号:");
            schIdField.setPromptText(isStudent ? "学号" : "工号");
        });

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(usernameField::requestFocus);

        // 拦截注册按钮：前端校验失败时不关闭对话框
        Button registerBtn = (Button) dialog.getDialogPane().lookupButton(registerButtonType);
        registerBtn.setOnAction(e -> {
            // 必填字段检查
            if (usernameField.getText().trim().isEmpty()
                    || passwordField.getText().isEmpty()
                    || nameField.getText().trim().isEmpty()
                    || confirmPasswordField.getText().isEmpty()
                    || schIdField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "提示", "请填写所有必填字段");
                return;
            }
            // 用户名长度
            if (usernameField.getText().trim().length() < 3) {
                showAlert(Alert.AlertType.WARNING, "提示", "用户名长度不能少于3位");
                return;
            }
            // 密码长度
            if (passwordField.getText().length() < 6) {
                showAlert(Alert.AlertType.WARNING, "提示", "密码长度不能少于6位");
                return;
            }
            // 两次密码一致
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                showAlert(Alert.AlertType.WARNING, "提示", "两次输入的密码不一致");
                return;
            }

            boolean isStudent = "学生".equals(roleCombo.getValue());

            // 电话校验
            String phone = phoneField.getText().trim();
            if (!phone.isEmpty() && !phone.matches("\\d{11}")) {
                showAlert(Alert.AlertType.WARNING, "提示", "手机号必须为11位数字");
                return;
            }

            // 班级校验
            String classText = classField.getText().trim();
            if (isStudent && !classText.isEmpty()) {
                try {
                    int cls = Integer.parseInt(classText);
                    if (cls <= 0) { showAlert(Alert.AlertType.WARNING, "提示", "班级必须为正整数"); return; }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.WARNING, "提示", "班级格式不正确，请输入正整数"); return;
                }
            }

            RegisterRequest req = new RegisterRequest();
            req.setUsername(usernameField.getText().trim());
            req.setPassword(passwordField.getText());
            req.setName(nameField.getText().trim());
            req.setRole(isStudent ? "STUDENT" : "TEACHER");
            req.setPhone(phone);
            req.setSchId(schIdField.getText().trim());

            if (isStudent) {
                if (majorField.getText().trim().isEmpty()
                        || classText.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "提示", "请填写专业和班级");
                    return;
                }
                req.setMajor(majorField.getText().trim());
                req.setGender(genderCombo.getValue());
                req.setSClass(Integer.parseInt(classText));
            }

            // 验证通过，关闭对话框并传递结果
            dialog.setResult(req);
        });

        // 循环：注册失败时保持对话框，直到成功或用户取消
        while (true) {
            var result = dialog.showAndWait();
            if (result.isEmpty()) break; // 用户点击取消

            try {
                ApiClient.register(result.get());
                showAlert(Alert.AlertType.INFORMATION, "注册成功", "注册成功，请登录");
                break;
            } catch (ApiException ex) {
                if (ex.getCode() == 409) {
                    showAlert(Alert.AlertType.ERROR, "注册失败", "用户名已存在，请更换用户名");
                } else {
                    showAlert(Alert.AlertType.ERROR, "注册失败", ex.getMessage());
                }
                // 对话框自动重新弹出，字段内容保留
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "网络错误", "网络连接失败，请检查网络");
            }
        }
    }

    private static void setRowVisible(GridPane grid, int row, boolean visible) {
        for (javafx.scene.Node node : grid.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(node);
            if (nodeRow != null && nodeRow == row) {
                node.setVisible(visible);
                node.setManaged(visible);
            }
        }
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
