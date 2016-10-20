package computationalModel.line.comments;

import computationalModel.line.CMLine;

/**
 * абстрактный класс, реализующий коментарии вида:
 * <имя_комента>DELIMITER<строка_описывающее_действие>
 * <строка_описывающее_действие> - храниться в lastComment(парамметр для любого комментария)
 */
public abstract class Comment {
    public static String DELIMITER = "=";
    protected String lastComment;
    public Comment() {
        lastComment = null;
    }
    public void setLastComment(String lastComment) {
        this.lastComment = lastComment;
    }
    public abstract void correct(CMLine cmLine);
    public void correct(CMLine cmLine, String comment){
        lastComment = comment;
        correct(cmLine);
    }
}
