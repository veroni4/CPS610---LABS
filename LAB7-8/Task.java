/**
 * Task.java
 * Represents a single operation/task in a transaction
 */

public class Task {
    // Task types
    public enum TaskType {
        READ,       // Read operation
        WRITE,      // Write operation
        COMMIT      // Commit operation
    }
    
    private TaskType taskType;
    private int recordId;       // Record ID (not used for COMMIT)
    private Integer value;      // Value to write (only for WRITE)
    private String transactionId;
    
    /**
     * Constructor for READ and WRITE tasks
     */
    public Task(String transactionId, TaskType taskType, int recordId, Integer value) {
        this.transactionId = transactionId;
        this.taskType = taskType;
        this.recordId = recordId;
        this.value = value;
    }
    
    /**
     * Constructor for COMMIT task
     */
    public Task(String transactionId, TaskType taskType) {
        this.transactionId = transactionId;
        this.taskType = taskType;
        this.recordId = -1;  // Not applicable for COMMIT
        this.value = null;
    }
    
    // Getters
    public TaskType getTaskType() {
        return taskType;
    }
    
    public int getRecordId() {
        return recordId;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    @Override
    public String toString() {
        switch (taskType) {
            case READ:
                return String.format("%s:R(%d)", transactionId, recordId);
            case WRITE:
                return String.format("%s:W(%d,%d)", transactionId, recordId, value);
            case COMMIT:
                return String.format("%s:C", transactionId);
            default:
                return "";
        }
    }
}
