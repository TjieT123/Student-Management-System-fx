package cn.edu.sdu.sms.fx.smsfx.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Base64 编解码工具 — 用于附件功能
 */
public class Base64Util {

    private static final long DEFAULT_MAX_SIZE = 5 * 1024 * 1024; // 5MB

    public static String encodeFile(File file) throws IOException {
        return encodeFile(file, DEFAULT_MAX_SIZE);
    }

    public static String encodeFile(File file, long maxSize) throws IOException {
        if (file.length() > maxSize) {
            throw new IOException("文件过大，最大支持 " + (maxSize / 1024 / 1024) + "MB");
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static void decodeToFile(String base64, File saveFile) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(base64);
        Files.write(saveFile.toPath(), bytes);
    }

    public static String guessMimeType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf"))   return "application/pdf";
        if (lower.endsWith(".doc"))   return "application/msword";
        if (lower.endsWith(".docx"))  return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls"))   return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx"))  return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".zip"))   return "application/zip";
        if (lower.endsWith(".rar"))   return "application/x-rar-compressed";
        if (lower.endsWith(".7z"))    return "application/x-7z-compressed";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))   return "image/png";
        if (lower.endsWith(".txt"))   return "text/plain";
        if (lower.endsWith(".java"))  return "text/x-java-source";
        if (lower.endsWith(".py"))    return "text/x-python";
        if (lower.endsWith(".cpp") || lower.endsWith(".c")) return "text/x-c";
        return "application/octet-stream";
    }
}
