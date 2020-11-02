package App;

import com.microsoft.graph.models.extensions.Message;

import java.util.List;

public class SelectedTask
{
    Task taskInList;
    Task taskInCache;

    public SelectedTask(Task taskInList, Task taskInCache)
    {
        this.taskInList = taskInList;
        this.taskInCache = taskInCache;
    }

    public String getKey()
    {
        return taskInCache.getPrimaryKey();
    }

    public String getCustomerReference() {
        return taskInCache.getCustomerReference();
    }

    public void setCustomerReference(String customerReference) {
        taskInList.setCustomerReference(customerReference);
        taskInCache.setCustomerReference(customerReference);
    }

    public String getInternalReference()
    {
        return taskInCache.getInternalReference();
    }

    public void setInternalReference(String internalReference) {
        taskInList.setInternalReference(internalReference);
        taskInCache.setInternalReference(internalReference);
    }

    public String getReceivedDate() {
        return taskInCache.getReceivedDate();
    }

    public void setReceivedDate(String receivedDate) {
        taskInList.setReceivedDate(receivedDate);
        taskInCache.setReceivedDate(receivedDate);
    }

    public int getPriority() {
        return taskInCache.getPriority();
    }

    public void setPriority(int priority) {
        taskInList.setPriority(priority);
        taskInCache.setPriority(priority);
    }

    public User getOwner() {
        return taskInCache.getOwner();
    }

    public String getMessageId() {
        return taskInCache.getMessageId();
    }

    public void setMessageId(String messageId) {
        taskInList.setMessageId(messageId);
        taskInCache.setMessageId(messageId);
    }

    public String getRespondedDate() {
        return taskInCache.getRespondedDate();
    }

    public void setRespondedDate(String respondedDate) {
        taskInList.setRespondedDate(respondedDate);
        taskInCache.setRespondedDate(respondedDate);
    }

    public String getCustomer() {
        return taskInCache.getCustomer();
    }

    public void setCustomer(String customer) {
        taskInList.setCustomer(customer);
        taskInCache.setCustomer(customer);
    }

    public double getRevenue() {
        return taskInCache.getRevenue();
    }

    public void setRevenue(double revenue) {
        taskInList.setRevenue(revenue);
        taskInCache.setRevenue(revenue);
    }

    public String getState() {
        return taskInCache.getState();
    }

    public void setState(String state) {
        taskInList.setState(state);
        taskInCache.setState(state);
    }

    public String getPreparedBy() {
        return taskInCache.getPreparedBy();
    }

    public void setPreparedBy(String preparedBy) {
        taskInList.setPreparedBy(preparedBy);
        taskInCache.setPreparedBy(preparedBy);
    }

    //NON-PERSISTED FIELDS

    public Message getEmail() {
        return taskInCache.getEmail();
    }

    public void setEmail(Message email) {
        taskInList.setEmail(email);
        taskInCache.setEmail(email);
    }

    public String getOldTaskReference() {
        return taskInCache.getOldInternalTaskReference();
    }

    public void setOldTaskReference(String oldTaskReference) {
        taskInList.setOldInternalTaskReference(oldTaskReference);
        taskInCache.setOldInternalTaskReference(oldTaskReference);
    }

    //CUSTOM METHODS

    public void addTaskNote(TaskNote note)
    {
        taskInList.addTaskNote(note);
        taskInCache.addTaskNote(note);
    }

    public List getAllTaskNotes()
    {
        return taskInCache.getAllTaskNotes();
    }

    public String toString()
    {
        return taskInCache.getCustomerReference();
    }

    public TaskNote getLastTaskNote()
    {
        return taskInCache.getLastTaskNote();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
