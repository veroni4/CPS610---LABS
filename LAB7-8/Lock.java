/**
 * Lock.java
 * Represents a lock on a database record in the Two-Phase Locking protocol
 */

public class Lock {
    // Lock types
    public enum LockType {
        SHARED,      // Read lock - multiple transactions can hold simultaneously
        EXCLUSIVE    // Write lock - only one transaction can hold at a time
    }
    
    private int recordId;           // The record being locked
    private LockType lockType;      // Type of lock (SHARED or EXCLUSIVE)
    private String transactionId;   // Transaction that owns this lock
    
    /**
     * Constructor for creating a new lock
     * @param recordId The ID of the record to lock
     * @param lockType The type of lock (SHARED or EXCLUSIVE)
     * @param transactionId The transaction requesting the lock
     */
    public Lock(int recordId, LockType lockType, String transactionId) {
        this.recordId = recordId;
        this.lockType = lockType;
        this.transactionId = transactionId;
    }
    
    // Getters
    public int getRecordId() {
        return recordId;
    }
    
    public LockType getLockType() {
        return lockType;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    // Setter for lock type upgrade
    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }
    
    @Override
    public String toString() {
        return String.format("[Record=%d, Type=%s, Owner=%s]", 
            recordId, lockType, transactionId);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Lock lock = (Lock) obj;
        return recordId == lock.recordId && 
               lockType == lock.lockType && 
               transactionId.equals(lock.transactionId);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(recordId, lockType, transactionId);
    }
}
