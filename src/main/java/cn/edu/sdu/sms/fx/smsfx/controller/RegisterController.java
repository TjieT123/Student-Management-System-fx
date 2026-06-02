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

        ButtonType registerButtonType = new ButtonType("注册", ButtonBar.ButtonData.OK_DONE);
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

        // 结果转换
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
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

                boolean isStudent = "学生".equals(roleCombo.getValue());
                RegisterRequest req = new RegisterRequest();
                req.setUsername(usernameField.getText().trim());
                req.setPassword(passwordField.getText());
                req.setName(nameField.getText().trim());
                req.setRole(isStudent ? "STUDENT" : "TEACHER");
                req.setPhone(phoneField.getText().trim());
                req.setSchId(schIdField.getText().trim());

                if (isStudent) {
                    if (majorField.getText().trim().isEmpty()
                            || classField.getText().trim().isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "提示", "请填写专业和班级");
                        return null;
                    }
                    req.setMajor(majorField.getText().trim());
                    req.setGender(genderCombo.getValue());
                    try {
                        req.setSClass(Integer.parseInt(classField.getText().trim()));
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.WARNING, "提示", "班级请输入数字");
                        return null;
                    }
                }
                return req;
            }
            return null;
        });

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
