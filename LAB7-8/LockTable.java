/**
 * LockTable.java
 * Manages all locks in the Two-Phase Locking protocol
 * Tracks which transactions hold which locks on which records
 */

import java.util.*;

public class LockTable {
    // Maps record ID to list of locks on that record
    // Multiple SHARED locks can exist, but only one EXCLUSIVE lock
    private Map<Integer, List<Lock>> lockTable;
    
    // Maps transaction ID to list of locks it owns
    private Map<String, List<Lock>> transactionLocks;
    
    // Database records (simple key-value store)
    private Map<Integer, Integer> database;
    
    /**
     * Constructor initializes the lock table and database
     */
    public LockTable() {
        this.lockTable = new HashMap<>();
        this.transactionLocks = new HashMap<>();
        this.database = new HashMap<>();
        
        // Initialize some sample data
        for (int i = 1; i <= 10; i++) {
            database.put(i, i * 10); // Record i has initial value i*10
        }
    }
    
    /**
     * Attempt to acquire a SHARED (read) lock on a record
     * @param recordId The record to lock
     * @param transactionId The transaction requesting the lock
     * @return true if lock acquired, false if must wait
     */
    public boolean acquireSharedLock(int recordId, String transactionId) {
        // Check if transaction already has a lock on this record
        if (hasLock(recordId, transactionId)) {
            // Already has a lock, no need to acquire again
            return true;
        }
        
        // Check if any EXCLUSIVE lock exists from another transaction
        if (hasExclusiveLockByOther(recordId, transactionId)) {
            // Must wait - another transaction has exclusive lock
            return false;
        }
        
        // Can acquire shared lock
        Lock newLock = new Lock(recordId, Lock.LockType.SHARED, transactionId);
        addLock(recordId, transactionId, newLock);
        return true;
    }
    
    /**
     * Attempt to acquire an EXCLUSIVE (write) lock on a record
     * @param recordId The record to lock
     * @param transactionId The transaction requesting the lock
     * @return true if lock acquired or upgraded, false if must wait
     */
    public boolean acquireExclusiveLock(int recordId, String transactionId) {
        // Check if transaction already has an exclusive lock
        if (hasExclusiveLock(recordId, transactionId)) {
            // Already has exclusive lock, no need to acquire again
            return true;
        }
        
        // Check if transaction has a shared lock that needs upgrading
        if (hasSharedLock(recordId, transactionId)) {
            // Need to upgrade from SHARED to EXCLUSIVE
            // Check if other transactions have locks on this record
            if (hasLocksFromOthers(recordId, transactionId)) {
                // Cannot upgrade - other transactions have locks
                return false;
            }
            
            // Upgrade the lock
            upgradeLock(recordId, transactionId);
            return true;
        }
        
        // Check if any lock exists from another transaction
        if (hasAnyLockByOther(recordId, transactionId)) {
            // Must wait - another transaction has a lock
            return false;
        }
        
        // Can acquire exclusive lock
        Lock newLock = new Lock(recordId, Lock.LockType.EXCLUSIVE, transactionId);
        addLock(recordId, transactionId, newLock);
        return true;
    }
    
    /**
     * Release all locks held by a transaction (called on COMMIT)
     * @param transactionId The transaction to release locks for
     */
    public void releaseAllLocks(String transactionId) {
        List<Lock> locks = transactionLocks.get(transactionId);
        if (locks == null) return;
        
        // Remove all locks from the lock table
        for (Lock lock : locks) {
            int recordId = lock.getRecordId();
            if (lockTable.containsKey(recordId)) {
                lockTable.get(recordId).removeIf(l -> 
                    l.getTransactionId().equals(transactionId));
                
                // Remove empty lists
                if (lockTable.get(recordId).isEmpty()) {
                    lockTable.remove(recordId);
                }
            }
        }
        
        // Remove transaction's lock list
        transactionLocks.remove(transactionId);
    }
    
    /**
     * Read a value from the database
     * @param recordId The record to read
     * @return The value stored in the record
     */
    public Integer readRecord(int recordId) {
        return database.getOrDefault(recordId, null);
    }
    
    /**
     * Write a value to the database
     * @param recordId The record to write to
     * @param value The value to write
     */
    public void writeRecord(int recordId, int value) {
        database.put(recordId, value);
    }
    
    /**
     * Check if transaction has any lock on a record
     */
    private boolean hasLock(int recordId, String transactionId) {
        List<Lock> locks = lockTable.get(recordId);
        if (locks == null) return false;
        
        return locks.stream().anyMatch(l -> 
            l.getTransactionId().equals(transactionId));
    }
    
    /**
     * Check if transaction has a SHARED lock on a record
     */
    private boolean hasSharedLock(int recordId, String transactionId) {
        List<Lock> locks = lockTable.get(recordId);
        if (locks == null) return false;
        
        return locks.stream().anyMatch(l -> 
            l.getTransactionId().equals(transactionId) && 
            l.getLockType() == Lock.LockType.SHARED);
    }
    
    /**
     * Check if transaction has an EXCLUSIVE lock on a record
     */
    private boolean hasExclusiveLock(int recordId, String transactionId) {
        List<Lock> locks = lockTable.get(recordId);
        if (locks == null) return false;
        
        return locks.stream().anyMatch(l -> 
            l.getTransactionId().equals(transactionId) && 
            l.getLockType() == Lock.LockType.EXCLUSIVE);
    }
    
    /**
     * Check if another transaction has an EXCLUSIVE lock on a record
     */
    private boolean hasExclusiveLockByOther(int recordId, String transactionId) {
        List<Lock> locks = lockTable.get(recordId);
        if (locks == null) return false;
        
        return locks.stream().anyMatch(l -> 
            !l.getTransactionId().equals(transactionId) && 
            l.getLockType() == Lock.LockType.EXCLUSIVE);
    }
    
    /**
     * Check if any other transaction has any lock on a record
     */
    private boolean hasAnyLockByOther(int recordId, String transactionId) {
        List<Lock> locks = lockTable.get(recordId);
        if (locks == null) return false;
        
        return locks.stream().anyMatch(l -> 
            !l.getTransactionId().equals(transactionId));
    }
    
    /**
     * Check if other transactions (not this one) have locks on record
     */
    private boolean hasLocksFromOthers(int recordId, String transactionId) {
        List<Lock> locks = lockTable.get(recordId);
        if (locks == null) return false;
        
        // Check if there are locks from other transactions
        long otherLocks = locks.stream()
            .filter(l -> !l.getTransactionId().equals(transactionId))
            .count();
        
        return otherLocks > 0;
    }
    
    /**
     * Add a lock to the lock table
     */
    private void addLock(int recordId, String transactionId, Lock lock) {
        // Add to lock table
        lockTable.computeIfAbsent(recordId, k -> new ArrayList<>()).add(lock);
        
        // Add to transaction's lock list
        transactionLocks.computeIfAbsent(transactionId, k -> new ArrayList<>()).add(lock);
    }
    
    /**
     * Upgrade a SHARED lock to EXCLUSIVE
     */
    private void upgradeLock(int recordId, String transactionId) {
        // Find and upgrade in lock table
        List<Lock> locks = lockTable.get(recordId);
        if (locks != null) {
            for (Lock lock : locks) {
                if (lock.getTransactionId().equals(transactionId)) {
                    lock.setLockType(Lock.LockType.EXCLUSIVE);
                    break;
                }
            }
        }
        
        // Find and upgrade in transaction locks
        List<Lock> txLocks = transactionLocks.get(transactionId);
        if (txLocks != null) {
            for (Lock lock : txLocks) {
                if (lock.getRecordId() == recordId) {
                    lock.setLockType(Lock.LockType.EXCLUSIVE);
                    break;
                }
            }
        }
    }
    
    /**
     * Display the current state of the lock table
     */
    public void displayLockTable() {
        if (lockTable.isEmpty()) {
            System.out.println("    Lock Table: EMPTY");
            return;
        }
        
        System.out.println("    Lock Table:");
        for (Map.Entry<Integer, List<Lock>> entry : lockTable.entrySet()) {
            int recordId = entry.getKey();
            List<Lock> locks = entry.getValue();
            
            System.out.print("      Record " + recordId + ": ");
            for (int i = 0; i < locks.size(); i++) {
                Lock lock = locks.get(i);
                System.out.print(lock.getLockType() + "(" + lock.getTransactionId() + ")");
                if (i < locks.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }
    
    /**
     * Get locks held by a specific transaction
     */
    public List<Lock> getTransactionLocks(String transactionId) {
        return transactionLocks.getOrDefault(transactionId, new ArrayList<>());
    }
    
    /**
     * Display the database state
     */
    public void displayDatabase() {
        System.out.println("\n  Database State:");
        List<Integer> sortedKeys = new ArrayList<>(database.keySet());
        Collections.sort(sortedKeys);
        
        for (Integer recordId : sortedKeys) {
            System.out.println("    Record " + recordId + " = " + database.get(recordId));
        }
    }
}
