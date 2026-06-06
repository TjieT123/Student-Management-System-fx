package cn.edu.sdu.sms.fx.smsfx.controller;

import cn.edu.sdu.sms.fx.smsfx.models.Student;
import cn.edu.sdu.sms.fx.smsfx.models.User;
import cn.edu.sdu.sms.fx.smsfx.util.ApiClient;
import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.Year;
import java.util.*;

public class ProfileController extends BaseController {

    @FXML private Label avatarIcon, nameLabel, roleLabel, infoNameLabel, schIdLabel, phoneLabel, infoRoleLabel;
    @FXML private TextField phoneEditField;
    @FXML private Button editPhoneBtn, changePasswordBtn, logoutBtn;
    @FXML private PasswordField oldPasswordField, newPasswordField, confirmPasswordField;
    @FXML private HBox editBtnRow;
    @FXML private VBox infoBox;

    private DatePicker birthPicker;
    private Label birthLockHint;
    private ComboBox<Integer> enrollCombo;
    private TextField idCardField, nativeField, addressField, contactNameField, contactPhoneField, relationField;
    private ComboBox<String> politicalCombo;
    private Label gradeLabel;
    private TextField gradeField;
    private Button saveBtn;

    @Override @FXML public void initialize() {
        super.initialize(); enableBackButton();
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        avatarIcon.setText(roleToIcon(user.getRole()));
        nameLabel.setText(user.getName());
        roleLabel.setText(roleToChinese(user.getRole()));
        infoNameLabel.setText(user.getName());
        schIdLabel.setText(user.getSchId() != null ? user.getSchId() : "");
        phoneLabel.setText(user.getPhone() != null ? user.getPhone() : "");
        infoRoleLabel.setText(roleToChinese(user.getRole()));
        phoneEditField.setText(user.getPhone() != null ? user.getPhone() : "");

        editPhoneBtn.setText("编辑");
        editPhoneBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        changePasswordBtn.setOnAction(e -> handleChangePassword());
        logoutBtn.setOnAction(e -> handleLogout());

        // Phone editing for ALL roles (admin, teacher, student)
        editPhoneBtn.setOnAction(e -> {
            phoneLabel.setVisible(false); phoneLabel.setManaged(false);
            phoneEditField.setVisible(true); phoneEditField.setManaged(true);
            editPhoneBtn.setVisible(false);
            // Save button for phone-only edit (admin/teacher)
            if (!"STUDENT".equals(user.getRole())) {
                Button phoneSaveBtn = new Button("保存");
                phoneSaveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                editBtnRow.getChildren().add(phoneSaveBtn);
                phoneSaveBtn.setOnAction(ev -> {
                    String phone = phoneEditField.getText().trim();
                    if (phone.isEmpty()) { showWarning("请输入手机号"); return; }
                    String pe = validatePhone(phone); if (pe != null) { showWarning(pe); return; }
                    try {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", user.getId()); data.put("phone", phone);
                        ApiClient.updateUser(data);
                        user.setPhone(phone); phoneLabel.setText(phone);
                        phoneLabel.setVisible(true); phoneLabel.setManaged(true);
                        phoneEditField.setVisible(false); phoneEditField.setManaged(false);
                        editPhoneBtn.setVisible(true);
                        editBtnRow.getChildren().remove(phoneSaveBtn);
                        showInfo("保存成功");
                    } catch (Exception ex) { showError("保存失败: " + ex.getMessage()); }
                });
            }
        });

        if ("STUDENT".equals(user.getRole())) loadExtendedInfo();
    }

    // Labels for display mode
    private Label birthLabel, enrollLabel, idCardLabel, nativeLabel, politicalLabel, addressLabel, contactNameLabel, contactPhoneLabel, relationLabel;

    private void loadExtendedInfo() {
        try {
            Student sp = ApiClient.getStudentProfile();
            if (sp == null) return;

            // Display labels
            birthLabel = new Label(fmtDate(sp.getBirthDate())); birthLabel.setStyle("-fx-text-fill: #555;");
            enrollLabel = valLabel(sp.getEnrollmentYear() != null ? String.valueOf(sp.getEnrollmentYear()) : null);
            idCardLabel = valLabel(sp.getIdCard());
            nativeLabel = valLabel(sp.getNativePlace());
            politicalLabel = valLabel(sp.getPoliticalStatus());
            addressLabel = valLabel(sp.getAddress());
            contactNameLabel = valLabel(sp.getContactName());
            contactPhoneLabel = valLabel(sp.getContactPhone());
            relationLabel = valLabel(sp.getSocialRelations());
            gradeLabel = valLabel(sp.getGrade() != null ? String.valueOf(sp.getGrade()) : null);

            // Input controls (initially hidden)
            birthPicker = new DatePicker();
            birthPicker.setEditable(false);
            if (sp.getBirthDate() != null && !sp.getBirthDate().isEmpty())
                try { birthPicker.setValue(java.time.LocalDate.parse(sp.getBirthDate())); } catch (Exception ignored) {}
            birthPicker.setVisible(false); birthPicker.setManaged(false);

            enrollCombo = new ComboBox<>();
            int ty = Year.now().getValue();
            for (int y = ty; y >= ty - 10; y--) enrollCombo.getItems().add(y);
            if (sp.getEnrollmentYear() != null) enrollCombo.setValue(sp.getEnrollmentYear());
            enrollCombo.setVisible(false); enrollCombo.setManaged(false);

            idCardField = new TextField(sp.getIdCard() != null ? sp.getIdCard() : ""); idCardField.setVisible(false); idCardField.setManaged(false);
            // Auto-fill and lock birth date from ID card (only when in edit mode)
            idCardField.textProperty().addListener((obs, old, val) -> {
                if (!idCardField.isVisible()) return;
                if (val != null && val.length() == 18) {
                    try {
                        String bd = val.substring(6, 10) + "-" + val.substring(10, 12) + "-" + val.substring(12, 14);
                        java.time.LocalDate parsed = java.time.LocalDate.parse(bd);
                        if (parsed.isAfter(java.time.LocalDate.now())) { showWarning("身份证号中的出生日期不能晚于当前日期"); return; }
                        birthPicker.setValue(parsed);
                        birthPicker.setDisable(true); birthPicker.setStyle("-fx-opacity: 1; -fx-background-color: white;");
                        birthLockHint.setVisible(true);
                    } catch (Exception ignored) {}
                } else {
                    birthPicker.setValue(null);
                    birthPicker.setDisable(false); birthPicker.setStyle(null);
                    birthLockHint.setVisible(false);
                }
            });
            nativeField = new TextField(sp.getNativePlace() != null ? sp.getNativePlace() : ""); nativeField.setVisible(false); nativeField.setManaged(false);
            politicalCombo = new ComboBox<>();
            politicalCombo.getItems().addAll("群众", "共青团员", "中共党员", "其他");
            if (sp.getPoliticalStatus() != null) politicalCombo.setValue(sp.getPoliticalStatus());
            politicalCombo.setVisible(false); politicalCombo.setManaged(false);
            addressField = new TextField(sp.getAddress() != null ? sp.getAddress() : ""); addressField.setVisible(false); addressField.setManaged(false);
            contactNameField = new TextField(sp.getContactName() != null ? sp.getContactName() : ""); contactNameField.setVisible(false); contactNameField.setManaged(false);
            contactPhoneField = new TextField(sp.getContactPhone() != null ? sp.getContactPhone() : ""); contactPhoneField.setVisible(false); contactPhoneField.setManaged(false);
            relationField = new TextField(sp.getSocialRelations() != null ? sp.getSocialRelations() : ""); relationField.setVisible(false); relationField.setManaged(false);
            gradeField = new TextField(sp.getGrade() != null ? String.valueOf(sp.getGrade()) : ""); gradeField.setVisible(false); gradeField.setManaged(false);

            birthLockHint = new Label("已锁定，从身份证号解析出出生日期");
            birthLockHint.setStyle("-fx-font-size: 10; -fx-text-fill: #e67e22;"); birthLockHint.setVisible(false);
            VBox birthBox = new VBox(2, birthPicker, birthLockHint);

            List<Label> labels = List.of(idCardLabel, birthLabel, enrollLabel, gradeLabel, nativeLabel, politicalLabel, addressLabel, contactNameLabel, contactPhoneLabel, relationLabel);
            List<Node> inputs = List.of(idCardField, birthBox, enrollCombo, gradeField, nativeField, politicalCombo, addressField, contactNameField, contactPhoneField, relationField);

            GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(8);
            String[] names = {"身份证号:", "出生日期:", "入学年份:", "年级:", "籍贯:", "政治面貌:", "家庭住址:", "紧急联系人:", "紧急联系人电话:", "与紧急联系人关系:"};
            for (int i = 0; i < names.length; i++)
                grid.addRow(i, new Label(names[i]), labels.get(i), inputs.get(i));

            saveBtn = new Button("保存"); saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;"); saveBtn.setVisible(false);
            editBtnRow.getChildren().add(saveBtn);

            editPhoneBtn.setOnAction(e -> {
                phoneLabel.setVisible(false); phoneLabel.setManaged(false);
                phoneEditField.setVisible(true); phoneEditField.setManaged(true);
                labels.forEach(l -> { l.setVisible(false); l.setManaged(false); });
                inputs.forEach(c -> { c.setVisible(true); c.setManaged(true); c.setDisable(false); });
                birthPicker.setVisible(true); birthPicker.setManaged(true); birthPicker.setDisable(false);
                // Lock birth date if ID card already has 18 digits
                if (idCardField.getText() != null && idCardField.getText().length() == 18) {
                    birthPicker.setDisable(true); birthPicker.setStyle("-fx-opacity: 1; -fx-background-color: white;");
                    birthLockHint.setVisible(true);
                } else { birthLockHint.setVisible(false); }
                editPhoneBtn.setVisible(false); saveBtn.setVisible(true);
            });

            saveBtn.setOnAction(e -> {
                String phone = phoneEditField.getText().trim();
                if (phone.isEmpty()) { showWarning("请输入手机号"); return; }
                String pe = validatePhone(phone); if (pe != null) { showWarning(pe); return; }
                String idCard = idCardField.getText().trim();
                if (!idCard.isEmpty()) { String err = validateIdCard(idCard); if (err != null) { showWarning(err); return; } }
                String cp = contactPhoneField.getText().trim();
                if (!cp.isEmpty()) { String cpe = validatePhone(cp); if (cpe != null) { showWarning("紧急联系人电话: " + cpe); return; } }
                if (nativeField.getText().trim().isEmpty()) { showWarning("籍贯不能为空"); return; }
                if (addressField.getText().trim().isEmpty()) { showWarning("家庭住址不能为空"); return; }
                if (contactNameField.getText().trim().isEmpty()) { showWarning("紧急联系人不能为空"); return; }
                if (cp.isEmpty()) { showWarning("紧急联系人电话不能为空"); return; }
                String gText = gradeField.getText().trim();
                if (gText.isEmpty()) { showWarning("年级不能为空"); return; }
                try {
                    int gv = Integer.parseInt(gText);
                    if (gv < 1990 || gv > 2050) { showWarning("年级必须在1990-2050之间"); return; }
                } catch (NumberFormatException ex) { showWarning("年级必须为整数"); return; }
                try {
                    User user = SessionManager.getCurrentUser();
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", user.getId()); data.put("phone", phone);
                    if (birthPicker.getValue() != null) data.put("birthDate", birthPicker.getValue().toString());
                    data.put("enrollmentYear", enrollCombo.getValue()); data.put("idCard", idCard);
                    data.put("nativePlace", nativeField.getText().trim()); data.put("politicalStatus", politicalCombo.getValue());
                    data.put("address", addressField.getText().trim()); data.put("contactName", contactNameField.getText().trim());
                    data.put("contactPhone", cp); data.put("socialRelations", relationField.getText().trim());
                    data.put("grade", Integer.parseInt(gText));
                    ApiClient.updateUser(data);
                    user.setPhone(phone); phoneLabel.setText(phone);
                    phoneLabel.setVisible(true); phoneLabel.setManaged(true);
                    phoneEditField.setVisible(false); phoneEditField.setManaged(false);
                    // Update labels with new values
                    birthLabel.setText(fmtDate(birthPicker.getValue() != null ? birthPicker.getValue().toString() : null));
                    enrollLabel.setText(valText(enrollCombo.getValue() != null ? String.valueOf(enrollCombo.getValue()) : null));
                    idCardLabel.setText(valText(idCard)); nativeLabel.setText(valText(nativeField.getText().trim()));
                    politicalLabel.setText(valText(politicalCombo.getValue())); addressLabel.setText(valText(addressField.getText().trim()));
                    contactNameLabel.setText(valText(contactNameField.getText().trim())); contactPhoneLabel.setText(valText(cp));
                    relationLabel.setText(valText(relationField.getText().trim()));
                    gradeLabel.setText(valText(gText));
                    labels.forEach(l -> { l.setVisible(true); l.setManaged(true); });
                    inputs.forEach(c -> { c.setVisible(false); c.setManaged(false); c.setDisable(false); });
                    birthPicker.setVisible(false); birthPicker.setManaged(false); birthPicker.setStyle(null); birthPicker.setDisable(false);
                    birthLockHint.setVisible(false);
                    editPhoneBtn.setVisible(true); saveBtn.setVisible(false);
                    showInfo("保存成功");
                } catch (Exception ex) { showError("保存失败: " + ex.getMessage()); }
            });

            Separator sep = new Separator(); sep.setStyle("-fx-padding: 5 0 0 0;");
            infoBox.getChildren().addAll(sep, grid);
        } catch (Exception ignored) {}
    }

    private String valText(String s) { return s != null && !s.isEmpty() ? s : "暂无信息"; }
    private Label valLabel(String v) { Label l = new Label(valText(v)); l.setStyle("-fx-text-fill: #555;"); return l; }
    private String fmtDate(String d) {
        if (d == null || d.isEmpty()) return "暂无信息";
        try { java.time.LocalDate ld = java.time.LocalDate.parse(d); return ld.getYear() + "年" + ld.getMonthValue() + "月" + ld.getDayOfMonth() + "日"; }
        catch (Exception e) { return d; }
    }

    private void handleChangePassword() {
        String oldPwd = oldPasswordField.getText(), newPwd = newPasswordField.getText(), confirmPwd = confirmPasswordField.getText();
        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) { showWarning("请填写所有密码字段"); return; }
        if (newPwd.length() < 6) { showWarning("新密码长度不能少于6位"); return; }
        if (!newPwd.equals(confirmPwd)) { showWarning("两次输入的新密码不一致"); return; }
        if (oldPwd.equals(newPwd)) { showWarning("新密码不能与旧密码相同"); return; }
        try { ApiClient.changePassword(oldPwd, newPwd); showInfo("密码修改成功"); oldPasswordField.clear(); newPasswordField.clear(); confirmPasswordField.clear(); }
        catch (Exception ex) { showError("旧密码错误"); }
    }

    private void handleLogout() {
        if (showConfirm("退出登录", "确定要退出登录吗？")) { SessionManager.clear(); NavigationManager.goToLogin(); }
    }

}
