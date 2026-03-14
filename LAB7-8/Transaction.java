/**
 * Transaction.java
 * Represents a transaction with its list of tasks
 */

import java.util.*;

public class Transaction {
    private String transactionId;
    private List<Task> tasks;
    private int currentTaskIndex;  // Index of the current task to execute
    private boolean isWaiting;      // Whether transaction is waiting for a lock
    private boolean isCompleted;    // Whether all tasks are completed
    
    /**
     * Constructor
     * @param transactionId Unique identifier for the transaction
     */
    public Transaction(String transactionId) {
        this.transactionId = transactionId;
        this.tasks = new ArrayList<>();
        this.currentTaskIndex = 0;
        this.isWaiting = false;
        this.isCompleted = false;
    }
    
    /**
     * Add a READ task
     */
    public void addReadTask(int recordId) {
        tasks.add(new Task(transactionId, Task.TaskType.READ, recordId, null));
    }
    
    /**
     * Add a WRITE task
     */
    public void addWriteTask(int recordId, int value) {
        tasks.add(new Task(transactionId, Task.TaskType.WRITE, recordId, value));
    }
    
    /**
     * Add a COMMIT task
     */
    public void addCommitTask() {
        tasks.add(new Task(transactionId, Task.TaskType.COMMIT));
    }
    
    /**
     * Get the current task to execute
     */
    public Task getCurrentTask() {
        if (currentTaskIndex < tasks.size()) {
            return tasks.get(currentTaskIndex);
        }
        return null;
    }
    
    /**
     * Move to the next task
     */
    public void moveToNextTask() {
        currentTaskIndex++;
        if (currentTaskIndex >= tasks.size()) {
            isCompleted = true;
        }
        isWaiting = false;  // Clear waiting status when moving to next task
    }
    
    /**
     * Check if transaction has more tasks to execute
     */
    public boolean hasMoreTasks() {
        return currentTaskIndex < tasks.size();
    }
    
    // Getters and setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public boolean isWaiting() {
        return isWaiting;
    }
    
    public void setWaiting(boolean waiting) {
        isWaiting = waiting;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public List<Task> getTasks() {
        return tasks;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(transactionId).append(": ");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            switch (task.getTaskType()) {
                case READ:
                    sb.append("R(").append(task.getRecordId()).append(")");
                    break;
                case WRITE:
                    sb.append("W(").append(task.getRecordId())
                      .append(",").append(task.getValue()).append(")");
                    break;
                case COMMIT:
                    sb.append("C");
                    break;
            }
            if (i < tasks.size() - 1) {
                sb.append("; ");
            }
        }
        return sb.toString();
    }
}
