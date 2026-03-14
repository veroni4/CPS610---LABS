# Two-Phase Locking (2PL) Implementation

## Quick Start

### Running the Java Implementation

To run the Java implementation, compile and execute:

```bash
# Compile all Java files
javac *.java

# Run the main program
java TwoPhaseLocking
```

## Files Included

### Java Implementation
- **Lock.java** - Represents individual locks (SHARED or EXCLUSIVE)
- **Task.java** - Represents operations (READ, WRITE, COMMIT)
- **Transaction.java** - Manages transaction state and task list
- **LockTable.java** - Core lock management and database operations
- **TwoPhaseLocking.java** - Main 2PL algorithm with round-robin scheduling


### Documentation
- **TWO_PHASE_LOCKING_DOCUMENTATION.md** - Comprehensive explanation of the algorithm
- **README.md** - This file


### In Java (TwoPhaseLocking.java)

Edit the `main()` method:

```java
public static void main(String[] args) {
    TwoPhaseLocking tpl = new TwoPhaseLocking();
    
    // Create Transaction 1
    Transaction t1 = new Transaction("T1");
    t1.addWriteTask(1, 5);    // W(1, 5)
    t1.addCommitTask();        // C
    
    // Add transactions
    tpl.addTransaction(t1);
    
    // Execute
    tpl.executeTransactions();
}
```

## Example Test Cases

### Test Case 1: Basic Conflict
```
T1: W(1, 5); C
T2: R(1); C
```
Expected: T2 waits until T1 commits

### Test Case 2: Multiple Readers
```
T1: R(1); C
T2: R(1); C
T3: R(1); C
```
Expected: All execute concurrently (SHARED locks compatible)

### Test Case 3: Lock Upgrade
```
T1: R(1); W(1, 10); C
```
Expected: SHARED lock upgraded to EXCLUSIVE for write

### Test Case 4: Write-Write Conflict
```
T1: W(1, 5); C
T2: W(1, 6); C
```
Expected: T2 waits until T1 commits

## Understanding the Output

### Execution Trace
Shows each round of execution with:
- Task being processed
- Lock action taken (acquired/waiting)
- Current lock table state

Example:
```
ROUND 1
─────────
  Processing: T1:W(1,5)
    Action: Acquired EXCLUSIVE lock on Record 1
    Write Value: 5 to Record 1
    Status: ✓ EXECUTED

  Lock Table:
    Record 1: EXCLUSIVE(T1)
```

### Final Results
- **Execution Schedule**: Order tasks were actually executed
- **Database State**: Final values in all records

## Key Concepts

### Lock Types
- **SHARED (Read Lock)**: Multiple transactions can hold simultaneously
- **EXCLUSIVE (Write Lock)**: Only one transaction can hold at a time

### Lock Compatibility
- SHARED + SHARED = Compatible ✓
- SHARED + EXCLUSIVE = Incompatible ✗
- EXCLUSIVE + Any = Incompatible ✗

### Round-Robin Scheduling
Each round attempts to execute one task from each transaction in order.
If a transaction is waiting for a lock, it's skipped until next round.

## Further Reading

- See TWO_PHASE_LOCKING_DOCUMENTATION.md for detailed algorithm explanation
- Pseudo-code reference: See uploaded image for lock/unlock operations
- Database Systems Concepts (Silberschatz et al.) - Chapter on Concurrency Control

## Contact

For questions or issues with this implementation, refer to the comprehensive documentation or examine the well-commented source code.
