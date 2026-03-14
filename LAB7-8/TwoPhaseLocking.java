/**
 * TwoPhaseLocking.java
 * Implements the Two-Phase Locking (2PL) concurrency control protocol
 * 
 * The 2PL protocol ensures serializability by using locks:
 * - Growing Phase: Transaction acquires locks but cannot release any
 * - Shrinking Phase: Transaction releases locks but cannot acquire any new ones
 * 
 * This implementation uses:
 * - SHARED locks for READ operations (multiple transactions can hold)
 * - EXCLUSIVE locks for WRITE operations (only one transaction can hold)
 * - Lock upgrade from SHARED to EXCLUSIVE when needed
 * - Round-robin scheduling for concurrent transaction execution
 */

import java.util.*;

public class TwoPhaseLocking {
    private LockTable lockTable;
    private List<Transaction> transactions;
    private List<String> executionSchedule;  // Track order of executed tasks
    private int roundNumber;
    
    //Constructor
    public TwoPhaseLocking() {
        this.lockTable = new LockTable();
        this.transactions = new ArrayList<>();
        this.executionSchedule = new ArrayList<>();
        this.roundNumber = 1;
    }
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }
    
    //Execute all transactions using Round-Robin scheduling with 2PL
    public void executeTransactions() {
        System.out.println("\nInput Transactions:");
        for (Transaction tx : transactions) {
            System.out.println("  " + tx.toString());
        }
        System.out.println("\n" + "=" + "=".repeat(79));
        System.out.println("EXECUTION TRACE (Round-Robin Scheduling)");
        System.out.println("=" + "=".repeat(79) + "\n");
        
        boolean allCompleted = false;
        
        while (!allCompleted) {
            System.out.println("─".repeat(80));
            System.out.println("ROUND " + roundNumber);
            System.out.println("─".repeat(80));
            
            boolean taskExecutedInRound = false;
            
            // Try to execute one task from each transaction
            for (Transaction transaction : transactions) {
                if (transaction.isCompleted()) {
                    continue;  // Skip completed transactions
                }
                
                if (!transaction.hasMoreTasks()) {
                    continue;  // Skip if no more tasks
                }
                
                // Get current task
                Task currentTask = transaction.getCurrentTask();
                
                // Try to execute the task
                boolean executed = executeTask(transaction, currentTask);
                
                if (executed) {
                    taskExecutedInRound = true;
                }
            }
            
            // Display lock table after each round
            lockTable.displayLockTable();
            System.out.println();
            
            // Check if all transactions are completed
            allCompleted = transactions.stream().allMatch(Transaction::isCompleted);
            
            // If no task was executed in this round and not all completed, 
            // we might have a deadlock, but we continue (in real system would detect deadlock)
            if (!taskExecutedInRound && !allCompleted) {
                System.out.println("  WARNING: No tasks executed in this round.");
                System.out.println("  Checking for waiting transactions...\n");
            }
            
            roundNumber++;
        }
        
        // Display final results
        displayResults();
    }
    
    /**
     * Execute a single task according to 2PL protocol
     * 
     * @param transaction The transaction executing the task
     * @param task The task to execute
     * @return true if task was executed, false if transaction must wait
     */
    private boolean executeTask(Transaction transaction, Task task) {
        String txId = transaction.getTransactionId();
        boolean executed = false;
        
        System.out.println("  Processing: " + task.toString());
        
        switch (task.getTaskType()) {
            case "READ":
                executed = executeReadTask(transaction, task);
                break;
                
            case "WRITE":
                executed = executeWriteTask(transaction, task);
                break;
                
            case "COMMIT":
                executed = executeCommitTask(transaction, task);
                break;
        }
        
        if (executed) {
            executionSchedule.add(task.toString());
            transaction.moveToNextTask();
            System.out.println("    Status: ✓ EXECUTED");
        } else {
            transaction.setWaiting(true);
            System.out.println("    Status: ✗ WAITING (lock not available)");
        }
        
        return executed;
    }
    
    /**
     * Execute a READ task
     * 
     * Process (following pseudo-code from image):
     * 1. Check if LOCK(X) is "unlocked"
     *    - If yes: Set LOCK(X) to "read-locked", increment no_of_reads(X)
     * 2. Else if LOCK(X) is "read-locked"
     *    - Increment no_of_reads(X) (another reader joins)
     * 3. Else (LOCK(X) is "write-locked")
     *    - Wait until LOCK(X) is "unlocked"
     */
    private boolean executeReadTask(Transaction transaction, Task task) {
        int recordId = task.getRecordId();
        String txId = transaction.getTransactionId();
        
        // Try to acquire shared lock
        boolean lockAcquired = lockTable.acquireSharedLock(recordId, txId);
        
        if (lockAcquired) {
            // Read the value
            Integer value = lockTable.readRecord(recordId);
            System.out.println("    Action: Acquired SHARED lock on Record " + recordId);
            System.out.println("    Read Value: " + value);
            return true;
        } else {
            System.out.println("    Action: Cannot acquire SHARED lock (EXCLUSIVE lock held by another transaction)");
            return false;
        }
    }
    
    /**
     * Execute a WRITE task
     * 
     * Process (following pseudo-code from image):
     * 1. Check if LOCK(X) is "unlocked"
     *    - If yes: Set LOCK(X) to "write-locked"
     * 2. Else (LOCK(X) is locked by someone)
     *    - Wait until LOCK(X) is "unlocked"
     */
    private boolean executeWriteTask(Transaction transaction, Task task) {
        int recordId = task.getRecordId();
        int value = task.getValue();
        String txId = transaction.getTransactionId();
        
        // Try to acquire exclusive lock
        boolean lockAcquired = lockTable.acquireExclusiveLock(recordId, txId);
        
        if (lockAcquired) {
            // Check if this is an upgrade
            List<Lock> txLocks = lockTable.getTransactionLocks(txId);
            boolean isUpgrade = txLocks.stream()
                .anyMatch(l -> l.getRecordId() == recordId && 
                          l.getLockType() == Lock.LockType.EXCLUSIVE);
            
            // Write the value
            lockTable.writeRecord(recordId, value);
            
            if (isUpgrade) {
                System.out.println("    Action: Upgraded to EXCLUSIVE lock on Record " + recordId);
            } else {
                System.out.println("    Action: Acquired EXCLUSIVE lock on Record " + recordId);
            }
            System.out.println("    Write Value: " + value + " to Record " + recordId);
            return true;
        } else {
            System.out.println("    Action: Cannot acquire EXCLUSIVE lock (lock held by another transaction)");
            return false;
        }
    }
    
    /**
     * Execute a COMMIT task
     * 
     * Process (following pseudo-code from image - unlock operation):
     * 1. If LOCK(X) is "write-locked"
     *    - Set LOCK(X) to "unlocked"
     *    - Wakeup waiting transactions if any
     * 2. Else if LOCK(X) is "read-locked"
     *    - Decrement no_of_reads(X)
     *    - If no_of_reads(X) = 0, set LOCK(X) to "unlocked"
     *    - Wakeup waiting transactions if any
     */
    private boolean executeCommitTask(Transaction transaction, Task task) {
        String txId = transaction.getTransactionId();
        
        // Get all locks held by this transaction before releasing
        List<Lock> locksToRelease = new ArrayList<>(lockTable.getTransactionLocks(txId));
        
        // Release all locks
        lockTable.releaseAllLocks(txId);
        
        System.out.println("    Action: Released all locks");
        if (!locksToRelease.isEmpty()) {
            System.out.print("    Released: ");
            for (int i = 0; i < locksToRelease.size(); i++) {
                Lock lock = locksToRelease.get(i);
                System.out.print(lock.getLockType() + "(Record " + lock.getRecordId() + ")");
                if (i < locksToRelease.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
        
        // Mark any waiting transactions to try again
        for (Transaction tx : transactions) {
            if (tx.isWaiting()) {
                tx.setWaiting(false);
            }
        }
        
        return true;
    }
    
    /**
     * Display final execution results
     */
    private void displayResults() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EXECUTION COMPLETE");
        System.out.println("=".repeat(80));
        
        System.out.println("\nFinal Execution Schedule:");
        int taskNum = 1;
        int currentRound = 1;
        int tasksInRound = 0;
        
        System.out.print("  Round " + currentRound + " --> ");
        
        for (String task : executionSchedule) {
            System.out.print(task);
            tasksInRound++;
            
            // Determine if we should start a new round
            // Simple heuristic: new round after certain number of tasks
            if (tasksInRound >= transactions.size() || taskNum == executionSchedule.size()) {
                if (taskNum < executionSchedule.size()) {
                    System.out.println();
                    currentRound++;
                    System.out.print("  Round " + currentRound + " --> ");
                    tasksInRound = 0;
                }
            } else {
                System.out.print("; ");
            }
            
            taskNum++;
        }
        System.out.println("\n");
        
        // Display final database state
        lockTable.displayDatabase();
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    /**
     * Main method to test the 2PL algorithm
     */
    public static void main(String[] args) {
        TwoPhaseLocking tpl = new TwoPhaseLocking();
        
        // Create Transaction 1: W(1, 5); C
        Transaction t1 = new Transaction("T1");
        t1.addWriteTask(1, 5);
        t1.addCommitTask();
        
        // Create Transaction 2: R(9); R(7); C
        Transaction t2 = new Transaction("T2");
        t2.addReadTask(9);
        t2.addReadTask(7);
        t2.addCommitTask();
        
        // Create Transaction 3: R(1); C
        Transaction t3 = new Transaction("T3");
        t3.addReadTask(1);
        t3.addCommitTask();
        
        // Add transactions to the system
        tpl.addTransaction(t1);
        tpl.addTransaction(t2);
        tpl.addTransaction(t3);
        
        // Execute with 2PL protocol
        tpl.executeTransactions();
    }
}
