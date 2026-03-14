# Two-Phase Locking (2PL) Implementation - Project Summary

## Overview

This project implements the **Two-Phase Locking (2PL)** concurrency control protocol for database transaction management.

## Problem Addressed

### Concurrent Transaction Management
In multi-user database systems, multiple transactions may attempt to access and modify the same data simultaneously. Without proper concurrency control, this leads to:
- **Lost Updates**: Concurrent writes overwrite each other
- **Dirty Reads**: Reading uncommitted data
- **Inconsistent Reads**: Different values at different times
- **Phantom Reads**: New records appearing mid-transaction

### Solution: Two-Phase Locking
The 2PL protocol ensures **serializability** through a two-phase approach:
1. **Growing Phase**: Transaction acquires locks but cannot release any
2. **Shrinking Phase**: Transaction releases locks but cannot acquire new ones

## Implementation Highlights

### Data Structures

#### 1. Lock Table
```
Purpose: Track all locks in the system
Structure: Map<RecordID → List<Lock>>
```

#### 2. Transaction Locks
```
Purpose: Track locks owned by each transaction
Structure: Map<TransactionID → List<Lock>>
```

#### 3. Database
```
Purpose: Store actual data values
Structure: Map<RecordID → Value>
```

#### 4. Lock Types
- **SHARED (Read Lock)**: Multiple transactions can hold simultaneously
- **EXCLUSIVE (Write Lock)**: Only one transaction can hold at a time

### Key Features

1. **Lock Compatibility Enforcement**
   - SHARED + SHARED = ✓ Compatible
   - SHARED + EXCLUSIVE = ✗ Incompatible
   - EXCLUSIVE + Any = ✗ Incompatible

2. **Lock Upgrade Mechanism**
   - Upgrades SHARED → EXCLUSIVE when needed
   - Only if no other transactions hold locks
   - Prevents upgrade deadlocks

3. **Round-Robin Scheduling**
   - Fair execution: One task per transaction per round
   - Automatic retry for waiting transactions
   - Continues until all transactions complete

## Test Results

### Test Transactions
```
T1: W(1, 5); C
T2: R(9); R(7); C
T3: R(1); C
```

### Execution Trace

**ROUND 1:**
- T1:W(1,5) ✓ - Acquires EXCLUSIVE lock on Record 1, writes 5
- T2:R(9) ✓ - Acquires SHARED lock on Record 9, reads 90
- T3:R(1) ✗ - WAITS (T1 holds EXCLUSIVE on Record 1)

**ROUND 2:**
- T1:C ✓ - Releases EXCLUSIVE lock on Record 1
- T2:R(7) ✓ - Acquires SHARED lock on Record 7, reads 70
- T3:R(1) ✓ - Acquires SHARED lock on Record 1, reads 5 (updated value)

**ROUND 3:**
- T2:C ✓ - Releases locks on Records 7 and 9
- T3:C ✓ - Releases lock on Record 1

### Final Schedule
```
Round 1 --> T1:W(1,5); T2:R(9)
Round 2 --> T1:C; T2:R(7); T3:R(1)
Round 3 --> T2:C; T3:C
```

**✓ MATCHES EXPECTED OUTPUT**

### Key Observations

1. **Conflict Handling**: T3 correctly waits for T1 to release EXCLUSIVE lock
2. **Value Consistency**: T3 reads updated value (5) instead of original (10)
3. **Concurrency**: T2 executes independently (no conflicts with T1/T3)
4. **Serializability**: Equivalent to serial schedule T1 → T2 → T3

## File Deliverables

### Source Code
1. **Lock.java** - Lock data structure (SHARED/EXCLUSIVE)
2. **Task.java** - Transaction operation (READ/WRITE/COMMIT)
3. **Transaction.java** - Transaction state management
4. **LockTable.java** - Core lock table implementation
5. **TwoPhaseLocking.java** - Main 2PL algorithm with round-robin

### Documentation
1. **README.md** - Quick start guide and usage instructions
2. **TWO_PHASE_LOCKING_DOCUMENTATION.md** - Comprehensive algorithm explanation
3. **PROJECT_SUMMARY.md** - This document

## Technical Approach

### Lock Acquisition Algorithm

**For READ (SHARED lock):**
```
if record is unlocked OR has only SHARED locks:
    acquire SHARED lock
else if another transaction has EXCLUSIVE lock:
    WAIT
```

**For WRITE (EXCLUSIVE lock):**
```
if record is unlocked:
    acquire EXCLUSIVE lock
else if this transaction has SHARED lock AND no other locks exist:
    upgrade to EXCLUSIVE
else:
    WAIT
```

**For COMMIT:**
```
release all locks owned by this transaction
wake up waiting transactions
```

### Round-Robin Execution
```
for each round:
    for each transaction:
        if not completed:
            try to execute next task
            if successful: move to next task
            else: mark as waiting
    continue until all transactions complete
```
