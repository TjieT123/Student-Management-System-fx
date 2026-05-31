package cn.edu.sdu.sms.fx.smsfx;

import cn.edu.sdu.sms.fx.smsfx.util.NavigationManager;
import cn.edu.sdu.sms.fx.smsfx.util.UnirestRequest;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SmsApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // 初始化导航管理器
        NavigationManager.init(stage);

        // 加载登录页面
        FXMLLoader fxmlLoader = new FXMLLoader(
                SmsApplication.class.getResource("fxml/login-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("学生管理系统");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        UnirestRequest.shutdown();
    }
}
