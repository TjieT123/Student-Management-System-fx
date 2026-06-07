package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.ApiException;
import cn.edu.sdu.sms.fx.smsfx.models.RegisterRequest;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

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
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        int row = 0;

        // ========== 基础字段 ==========
        TextField usernameField = new TextField();
        usernameField.setPromptText("用户名");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("密码");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("确认密码");
        TextField nameField = new TextField();
        nameField.setPromptText("姓名");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("学生", "老师");
        roleCombo.setValue("学生");

        TextField phoneField = new TextField();
        phoneField.setPromptText("手机号（选填）");

        Label schIdLabel = new Label("学号:");
        TextField schIdField = new TextField();
        schIdField.setPromptText("学号");

        grid.add(new Label("用户名:"), 0, row); grid.add(usernameField, 1, row++);
        grid.add(new Label("密码:"), 0, row); grid.add(passwordField, 1, row++);
        grid.add(new Label("确认密码:"), 0, row); grid.add(confirmPasswordField, 1, row++);
        grid.add(new Label("姓名:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("身份:"), 0, row); grid.add(roleCombo, 1, row++);
        grid.add(new Label("手机号(选填):"), 0, row); grid.add(phoneField, 1, row++);
        grid.add(schIdLabel, 0, row); grid.add(schIdField, 1, row++);

        // ========== 学生专用字段 ==========
        Label majorLabel = new Label("专业:");
        TextField majorField = new TextField();
        majorField.setPromptText("专业");

        Label genderLabel = new Label("性别:");
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("男", "女");
        genderCombo.setValue("男");

        Label classLabel = new Label("班级:");
        TextField classField = new TextField();
        classField.setPromptText("班级");

        Label gradeLabel = new Label("年级:");
        ComboBox<Integer> gradeField = new ComboBox<>();
        // 一次性添加101个年份 (2030~1930)
        Integer[] years = new Integer[101];
        for (int i = 0; i <= 100; i++) years[i] = 2030 - i;
        gradeField.getItems().addAll(years);
        gradeField.setValue(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
        gradeField.setVisibleRowCount(20);

        Label idCardLabel = new Label("身份证号:");
        TextField idCardField = new TextField();
        idCardField.setPromptText("18位身份证号");

        Label birthLabel = new Label("出生日期:");
        DatePicker birthPicker = new DatePicker();
        birthPicker.setEditable(false);
        Label birthHint = new Label("");
        birthHint.setStyle("-fx-font-size: 10; -fx-text-fill: #e67e22;");
        VBox birthBox = new VBox(2, birthPicker, birthHint);

        // Auto-parse birth date from ID card
        idCardField.textProperty().addListener((obs, old, val) -> {
            if (val != null && val.length() == 18) {
                try {
                    String bd = val.substring(6, 10) + "-" + val.substring(10, 12) + "-" + val.substring(12, 14);
                    java.time.LocalDate parsed = java.time.LocalDate.parse(bd);
                    birthPicker.setValue(parsed);
                    birthPicker.setDisable(true);
                    birthPicker.setStyle("-fx-opacity: 1; -fx-background-color: white;");
                    birthHint.setText("已从身份证号解析出生日期");
                    birthHint.setVisible(true);
                } catch (Exception ignored) {}
            } else {
                birthPicker.setDisable(false);
                birthPicker.setStyle(null);
                birthHint.setText("");
                birthHint.setVisible(false);
            }
        });

        Label nativeLabel = new Label("籍贯:");
        TextField nativeField = new TextField();
        nativeField.setPromptText("籍贯");

        Label politicalLabel = new Label("政治面貌:");
        ComboBox<String> politicalCombo = new ComboBox<>();
        politicalCombo.getItems().addAll("群众", "共青团员", "中共党员", "其他");
        politicalCombo.setValue("群众");

        Label addressLabel = new Label("家庭住址:");
        TextField addressField = new TextField();
        addressField.setPromptText("家庭住址");

        Label contactNameLabel = new Label("紧急联系人:");
        TextField contactNameField = new TextField();
        contactNameField.setPromptText("紧急联系人姓名");

        Label contactPhoneLabel = new Label("联系人电话:");
        TextField contactPhoneField = new TextField();
        contactPhoneField.setPromptText("紧急联系人电话");

        Label relationLabel = new Label("联系人关系:");
        TextField relationField = new TextField();
        relationField.setPromptText("与紧急联系人关系");

        // 记录学生字段行索引
        int majorRow = row;
        grid.add(majorLabel, 0, row); grid.add(majorField, 1, row++);
        int genderRow = row;
        grid.add(genderLabel, 0, row); grid.add(genderCombo, 1, row++);
        int classRow = row;
        grid.add(classLabel, 0, row); grid.add(classField, 1, row++);
        int gradeRow = row;
        grid.add(gradeLabel, 0, row); grid.add(gradeField, 1, row++);
        int idCardRow = row;
        grid.add(idCardLabel, 0, row); grid.add(idCardField, 1, row++);
        int birthRow = row;
        grid.add(birthLabel, 0, row); grid.add(birthBox, 1, row++);
        int nativeRow = row;
        grid.add(nativeLabel, 0, row); grid.add(nativeField, 1, row++);
        int politicalRow = row;
        grid.add(politicalLabel, 0, row); grid.add(politicalCombo, 1, row++);
        int addressRow = row;
        grid.add(addressLabel, 0, row); grid.add(addressField, 1, row++);
        int contactNameRow = row;
        grid.add(contactNameLabel, 0, row); grid.add(contactNameField, 1, row++);
        int contactPhoneRow = row;
        grid.add(contactPhoneLabel, 0, row); grid.add(contactPhoneField, 1, row++);
        int relationRow = row;
        grid.add(relationLabel, 0, row); grid.add(relationField, 1, row++);

        // 所有学生字段的行索引数组
        int[] studentRows = {majorRow, genderRow, classRow, gradeRow,
                idCardRow, birthRow, nativeRow, politicalRow,
                addressRow, contactNameRow, contactPhoneRow, relationRow};

        // 角色切换
        roleCombo.setOnAction(e -> {
            boolean isStudent = "学生".equals(roleCombo.getValue());
            for (int r : studentRows) setRowVisible(grid, r, isStudent);
            schIdLabel.setText(isStudent ? "学号:" : "工号:");
            schIdField.setPromptText(isStudent ? "学号" : "工号");
        });

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setMinHeight(650);
        Platform.runLater(usernameField::requestFocus);

        // ========== 注册按钮 ==========
        Button registerBtn = (Button) dialog.getDialogPane().lookupButton(registerButtonType);
        registerBtn.setOnAction(e -> {
            boolean isStudent = "学生".equals(roleCombo.getValue());

            // 基础字段检查
            if (usernameField.getText().trim().isEmpty()
                    || passwordField.getText().isEmpty()
                    || nameField.getText().trim().isEmpty()
                    || confirmPasswordField.getText().isEmpty()
                    || schIdField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "提示", "请填写所有必填字段");
                return;
            }
            if (usernameField.getText().trim().length() < 3) {
                showAlert(Alert.AlertType.WARNING, "提示", "用户名长度不能少于3位");
                return;
            }
            if (passwordField.getText().length() < 6) {
                showAlert(Alert.AlertType.WARNING, "提示", "密码长度不能少于6位");
                return;
            }
            // 用户名和密码不能包含中文
            String unameText = usernameField.getText().trim();
            String pwdText = passwordField.getText();
            if (!unameText.matches("[\\x00-\\x7F]+")) {
                showAlert(Alert.AlertType.WARNING, "提示", "用户名只能包含英文、数字和特殊字符，不能包含中文");
                return;
            }
            if (!pwdText.matches("[\\x00-\\x7F]+")) {
                showAlert(Alert.AlertType.WARNING, "提示", "密码只能包含英文、数字和特殊字符，不能包含中文");
                return;
            }
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                showAlert(Alert.AlertType.WARNING, "提示", "两次输入的密码不一致");
                return;
            }

            // 学号/工号校验
            String schIdText = schIdField.getText().trim();
            if (!schIdText.matches("[a-zA-Z0-9]+")) {
                showAlert(Alert.AlertType.WARNING, "提示", "学号/工号只能包含数字和字母");
                return;
            }

            String nameText = nameField.getText().trim();

            // 电话校验（选填）
            String phone = phoneField.getText().trim();
            if (!phone.isEmpty() && !phone.matches("\\d{11}")) {
                showAlert(Alert.AlertType.WARNING, "提示", "手机号必须为11位数字");
                return;
            }

            RegisterRequest req = new RegisterRequest();
            req.setUsername(usernameField.getText().trim());
            req.setPassword(passwordField.getText());
            req.setName(nameText);
            req.setRole(isStudent ? "STUDENT" : "TEACHER");
            req.setPhone(phone);
            req.setSchId(schIdText);

            if (isStudent) {
                // 学生必填字段校验
                String majorText = majorField.getText().trim();
                String classText = classField.getText().trim();
                if (majorText.isEmpty()) { showAlert(Alert.AlertType.WARNING, "提示", "请填写专业"); return; }
                if (classText.isEmpty()) { showAlert(Alert.AlertType.WARNING, "提示", "请填写班级"); return; }
                try {
                    int cls = Integer.parseInt(classText);
                    if (cls <= 0) { showAlert(Alert.AlertType.WARNING, "提示", "班级必须为正整数"); return; }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.WARNING, "提示", "班级格式不正确，请输入正整数"); return;
                }

                // 年级（下拉框已限制范围）
                if (gradeField.getValue() == null) { showAlert(Alert.AlertType.WARNING, "提示", "请选择年级"); return; }
                req.setGrade(gradeField.getValue());

                // 身份证号校验
                String idCard = idCardField.getText().trim();
                if (idCard.isEmpty()) { showAlert(Alert.AlertType.WARNING, "提示", "请填写身份证号"); return; }
                String idErr = validateIdCard(idCard);
                if (idErr != null) { showAlert(Alert.AlertType.WARNING, "提示", idErr); return; }

                // 出生日期
                if (birthPicker.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "提示", "请填写出生日期"); return;
                }

                // 籍贯
                String nativeText = nativeField.getText().trim();
                if (nativeText.isEmpty()) { showAlert(Alert.AlertType.WARNING, "提示", "请填写籍贯"); return; }

                // 家庭住址
                String addressText = addressField.getText().trim();
                if (addressText.isEmpty()) { showAlert(Alert.AlertType.WARNING, "提示", "请填写家庭住址"); return; }

                // 紧急联系人
                String contactNameText = contactNameField.getText().trim();
                if (contactNameText.isEmpty()) { showAlert(Alert.AlertType.WARNING, "提示", "请填写紧急联系人"); return; }

                // 紧急联系人电话
                String contactPhoneText = contactPhoneField.getText().trim();
                if (contactPhoneText.isEmpty()) { showAlert(Alert.AlertType.WARNING, "提示", "请填写紧急联系人电话"); return; }
                if (!contactPhoneText.matches("\\d{11}")) {
                    showAlert(Alert.AlertType.WARNING, "提示", "紧急联系人电话必须为11位数字"); return;
                }

                // 与紧急联系人关系
                String relationText = relationField.getText().trim();
                if (relationText.isEmpty()) { showAlert(Alert.AlertType.WARNING, "提示", "请填写与紧急联系人的关系"); return; }

                // 设置学生字段
                req.setMajor(majorText);
                req.setGender(genderCombo.getValue());
                req.setSClass(Integer.parseInt(classText));
                req.setIdCard(idCard);
                req.setBirthDate(birthPicker.getValue().toString());
                req.setNativePlace(nativeText);
                req.setPoliticalStatus(politicalCombo.getValue());
                req.setAddress(addressText);
                req.setContactName(contactNameText);
                req.setContactPhone(contactPhoneText);
                req.setSocialRelations(relationText);
            }

            dialog.setResult(req);
        });

        // 循环：注册失败时保持对话框，直到成功或用户取消
        while (true) {
            var result = dialog.showAndWait();
            if (result.isEmpty() || !(result.get() instanceof RegisterRequest)) break;

            try {
                ApiClient.register(result.get());
                showAlert(Alert.AlertType.INFORMATION, "注册成功", "注册成功，请登录");
                break;
            } catch (ApiException ex) {
                showAlert(Alert.AlertType.ERROR, "注册失败", ex.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "网络错误", "网络连接失败，请检查网络");
            }
        }
    }

    /** 18位身份证校验（与 BaseController 一致） */
    private static String validateIdCard(String id) {
        if (id == null || id.isEmpty()) return "身份证号不能为空";
        if (id.length() != 18) return "身份证号必须为18位";
        if (!id.substring(0, 17).matches("\\d{17}")) return "身份证号前17位必须为数字";
        char last = id.charAt(17);
        if (!Character.isDigit(last) && last != 'X' && last != 'x') return "身份证号第18位必须为数字或X";
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < 17; i++) sum += (id.charAt(i) - '0') * weights[i];
        if (Character.toUpperCase(last) != checkCodes[sum % 11]) return "身份证号校验位不正确";
        // 校验出生日期
        try {
            String bd = id.substring(6, 10) + "-" + id.substring(10, 12) + "-" + id.substring(12, 14);
            java.time.LocalDate.parse(bd);
        } catch (Exception e) { return "身份证号中出生日期不合法"; }
        return null;
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
