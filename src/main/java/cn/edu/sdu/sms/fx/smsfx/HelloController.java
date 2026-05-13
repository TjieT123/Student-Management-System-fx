package cn.edu.sdu.sms.fx.smsfx;

import cn.edu.sdu.sms.fx.smsfx.models.LoginResponse;
import cn.edu.sdu.sms.fx.smsfx.util.HttpRequest;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");

        LoginResponse loginResponse = HttpRequest.loginRequest("admin", "123456");
        if (loginResponse != null) {
            System.out.println(loginResponse.getData().getToken());
        } else {
            System.out.println("loss");
        }
    }
}
