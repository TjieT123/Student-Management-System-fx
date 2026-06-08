module cn.edu.sdu.sms.fx.smsfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    requires org.kordamp.bootstrapfx.core;

    requires com.fasterxml.jackson.databind;

    opens cn.edu.sdu.sms.fx.smsfx to javafx.fxml;
    opens cn.edu.sdu.sms.fx.smsfx.controller to javafx.fxml;
    opens cn.edu.sdu.sms.fx.smsfx.models to com.fasterxml.jackson.databind, javafx.base;
    exports cn.edu.sdu.sms.fx.smsfx;
}