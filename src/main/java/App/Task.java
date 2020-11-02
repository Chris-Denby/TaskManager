package App;

import com.microsoft.graph.models.extensions.Message;

import java.util.ArrayList;
import java.util.List;

public class Task implements Comparable
{
    private String customerReference = "nil";
    private String internalReference = "nil";
    private String receivedDate = "nil";
    private String respondedDate = "nil";
    private String customer = "nil";
    private double revenue = 0.00;
    private String state = "nil";
    private String preparedBy = "Unassigned";
    private int priority = 0; //0 = BAU, 1 = HIGH, 2 = IMMEDIATE
    private User owner = new User("Unassigned","","","");
    private Message email = null;
    private ArrayList<TaskNote> taskNotes = new ArrayList<>();
    private String messageId = "nil";
    private String oldInternalTaskReference = "nil";
    private String primaryKey;
    private int order;
    private int slaGap = 0;

    public Task()
    {

    }

    public String getPrimaryKey()
    {
        return primaryKey;
    }

    public void setPrimaryKey(String key)
    {
        this.primaryKey = key;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getInternalReference() {
        return internalReference;
    }

    public void setInternalReference(String internalReference) {
        this.internalReference = internalReference;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
        preparedBy = owner.toString();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRespondedDate() {
        return respondedDate;
    }

    public void setRespondedDate(String respondedDate) {
        this.respondedDate = respondedDate;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }

    //NON-PERSISTED FIELDS

    public Message getEmail() {
        return email;
    }

    public void setEmail(Message email) {
        this.email = email;
    }

    public String getOldInternalTaskReference() {
        return oldInternalTaskReference;
    }

    public void setOldInternalTaskReference(String oldInternalTaskReference) {
        this.oldInternalTaskReference = oldInternalTaskReference;
    }

    public int getSlaGap() {
        return slaGap;
    }

    public void setSlaGap(int slaGap) {
        this.slaGap = slaGap;
    }

    //CUSTOM METHODS

    public void addTaskNote(TaskNote note)
    {
        taskNotes.add(note);
    }

    public List getAllTaskNotes()
    {
        return taskNotes;
    }

    public String toString()
    {
        return customerReference;
    }

    public TaskNote getLastTaskNote()
    {
        int lastNoteIndex = taskNotes.size() -1;
        if(taskNotes.isEmpty())
        {
            return null;
        }
        else
        {
            return taskNotes.get(lastNoteIndex);
        }
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    @Override
    public int compareTo(Object o)
    {
        App.Task other = (App.Task) o;
        if(other.getOrder() < this.order)
        {
            return 1;
        }
        else
        {
            return 0;
        }

    }
}
