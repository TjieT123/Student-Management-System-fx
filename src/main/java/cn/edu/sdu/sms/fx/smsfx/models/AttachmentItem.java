package cn.edu.sdu.sms.fx.smsfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 附件数据模型（Base64 方案）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttachmentItem {
    private String fileName;
    private String fileType;
    private Long size;
    private String base64;

    public AttachmentItem() {}

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getBase64() { return base64; }
    public void setBase64(String base64) { this.base64 = base64; }

    public String getSizeDisplay() {
        if (size == null) return "未知";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }
}
