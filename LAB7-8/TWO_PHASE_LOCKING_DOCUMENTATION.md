# Two-Phase Locking (2PL) Algorithm Implementation

## Problem Summary

### Concurrency Control in Database Systems

In a multi-user database system, multiple transactions may attempt to access and modify the same data simultaneously. Without proper concurrency control.

### The Two-Phase Locking Solution

The Two-Phase Locking (2PL) protocol ensures **serializability** - the concurrent execution of transactions produces the same result as some serial (one-at-a-time) execution.

**Key Principle**: Each transaction follows two phases:
1. **Growing Phase**: Transaction acquires locks but CANNOT release any
2. **Shrinking Phase**: Transaction releases locks but CANNOT acquire any new ones

## Algorithm Implementation

### Lock Types

#### SHARED Lock (Read Lock)
- **Purpose**: Allow multiple transactions to READ the same record
- **Rules**:
  - Multiple SHARED locks can coexist on one record
  - Cannot be acquired if EXCLUSIVE lock exists from another transaction
  - Used for READ operations

#### EXCLUSIVE Lock (Write Lock)
- **Purpose**: Allow only one transaction to WRITE to a record
- **Rules**:
  - Only ONE EXCLUSIVE lock can exist on a record
  - Cannot be acquired if ANY lock exists from another transaction
  - Can upgrade from SHARED to EXCLUSIVE if this transaction is the only holder
  - Used for WRITE operations

### Lock Acquisition Rules (Following Pseudo-code)

#### READ Operation (acquire_shared_lock):
```
if LOCK(X) = "unlocked":
    LOCK(X) ← "read-locked"
    no_of_reads(X) ← 1
elif LOCK(X) = "read-locked":
    no_of_reads(X) ← no_of_reads(X) + 1
else:  # LOCK(X) = "write-locked"
    wait until LOCK(X) = "unlocked"
    (lock manager wakes up the transaction)
```

**Implementation Logic**:
1. Check if transaction already has ANY lock → Allow (already have access)
2. Check if another transaction has EXCLUSIVE lock → WAIT
3. Otherwise → Acquire SHARED lock

#### WRITE Operation (acquire_exclusive_lock):
```
if LOCK(X) = "unlocked":
    LOCK(X) ← "write-locked"
else:
    wait until LOCK(X) = "unlocked"
    (lock manager wakes up the transaction)
```

**Implementation Logic**:
1. Check if transaction already has EXCLUSIVE lock → Allow (already have it)
2. Check if transaction has SHARED lock:
   - If other transactions also have locks → WAIT
   - Otherwise → UPGRADE to EXCLUSIVE
3. Check if any other transaction has ANY lock → WAIT
4. Otherwise → Acquire EXCLUSIVE lock

#### COMMIT Operation (release_all_locks):
```
if LOCK(X) = "write-locked":
    LOCK(X) ← "unlocked"
    wakeup waiting transactions, if any
elif LOCK(X) = "read-locked":
    no_of_reads(X) ← no_of_reads(X) - 1
    if no_of_reads(X) = 0:
        LOCK(X) ← "unlocked"
        wakeup waiting transactions, if any
```

**Implementation Logic**:
1. Get all locks owned by this transaction
2. Remove all locks from lock_table
3. Remove transaction from transaction_locks
4. Wake up waiting transactions (set waiting flag to false)

### Round-Robin Scheduling
- Each transaction gets one chance per round
- If a transaction is WAITING (cannot acquire lock), skip to next transaction
- If a transaction is COMPLETED, skip to next transaction
- Continue until all transactions are completed

## Execution Trace Analysis

### Test Case: Three Concurrent Transactions

**Input Transactions**:
```
T1: W(1, 5); C
T2: R(9); R(7); C
T3: R(1); C
```

### Round-by-Round Execution

#### ROUND 1:
```
T1:W(1,5) - Acquires EXCLUSIVE lock on Record 1, writes 5
T2:R(9)   - Acquires SHARED lock on Record 9, reads 90
T3:R(1)   - WAITS (T1 holds EXCLUSIVE lock on Record 1)

Lock Table After Round 1:
  Record 1: EXCLUSIVE(T1)
  Record 9: SHARED(T2)
```

**Why T3 must wait?**
- T1 has EXCLUSIVE lock on Record 1
- SHARED lock cannot be acquired while EXCLUSIVE lock exists
- T3 must wait until T1 releases the lock

#### ROUND 2:
```
T1:C      - Releases EXCLUSIVE lock on Record 1
T2:R(7)   - Acquires SHARED lock on Record 7, reads 70
T3:R(1)   - Acquires SHARED lock on Record 1, reads 5 (updated value)

Lock Table After Round 2:
  Record 1: SHARED(T3)
  Record 7: SHARED(T2)
  Record 9: SHARED(T2)
```

**Why T3 can now proceed?**
- T1 released EXCLUSIVE lock on Record 1 during COMMIT
- Record 1 is now unlocked
- T3 can acquire SHARED lock

**Note**: T3 reads the NEW value (5) written by T1, not the original value (10)

#### ROUND 3:
```
T2:C      - Releases SHARED locks on Records 7 and 9
T3:C      - Releases SHARED lock on Record 1

Lock Table After Round 3:
  EMPTY (all locks released)
```

### Final Execution Schedule

```
Round 1 --> T1:W(1,5); T2:R(9)
Round 2 --> T1:C; T2:R(7); T3:R(1)
Round 3 --> T2:C; T3:C
```

**Compared to Expected**:
```
Expected: Round 1 --> T1:W(1,5); T2:R(9)
          Round 2 --> T1:C; T2:R(7); T3:R(1)
          Round 3 --> T2:C; T3:C
```

**Result**: ✓ MATCHES EXPECTED SCHEDULE