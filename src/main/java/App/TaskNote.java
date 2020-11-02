package App;

public class TaskNote
{
    private String taskId;
    private String primaryKey;
    private String noteBody;
    private String author;
    private String createdDate;
    private boolean persist = false;

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setNoteBody(String noteBody)
    {
        this.noteBody = noteBody;
    }

    public String getNoteBody()
    {
        return noteBody;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String toString()
    {
        return noteBody;
    }

    public boolean getPersist() {
        return persist;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }
}
