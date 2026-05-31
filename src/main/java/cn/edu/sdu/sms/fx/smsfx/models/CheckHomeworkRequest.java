package cn.edu.sdu.sms.fx.smsfx.models;

public class CheckHomeworkRequest {
    private Integer submitId;
    private Integer score;
    private String comment;

    public CheckHomeworkRequest() {}

    public CheckHomeworkRequest(Integer submitId, Integer score, String comment) {
        this.submitId = submitId;
        this.score = score;
        this.comment = comment;
    }

    public Integer getSubmitId() { return submitId; }
    public void setSubmitId(Integer submitId) { this.submitId = submitId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
