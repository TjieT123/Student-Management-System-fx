package cn.edu.sdu.sms.fx.smsfx.models;

/**
 * AI 判卷返回结果
 */
public class AiGradeResult {
    private Integer score;
    private String comment;
    private String highlights;
    private String suggestions;

    public AiGradeResult() {}

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getHighlights() { return highlights; }
    public void setHighlights(String highlights) { this.highlights = highlights; }

    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
}
