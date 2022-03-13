package edu.nyp.deafapp.Model;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class Task {

    private int taskId;
    private String content;
    private boolean isFinish;

    public Task(int taskId, String content, boolean isFinish) {
        this.taskId = taskId;
        this.content = content;
        this.isFinish = isFinish;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }
}
