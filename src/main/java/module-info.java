module cn.edu.sdu.sms.fx.smsfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

//    requires kong.unirest;          // Unirest
    requires com.fasterxml.jackson.databind;
    requires unirest.java;  // JSON解析

    opens cn.edu.sdu.sms.fx.smsfx to javafx.fxml;
    opens cn.edu.sdu.sms.fx.smsfx.models to com.fasterxml.jackson.databind;
    exports cn.edu.sdu.sms.fx.smsfx;
}