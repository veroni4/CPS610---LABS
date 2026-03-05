"""
Algorithm for Testing Conflict-Serializability

This program implements the standard algorithm for testing conflict serializability:
1. Create a precedence graph (serialization graph) for the schedule
2. Check if the graph contains any cycles
3. If no cycles exist, the schedule is conflict serializable
4. Perform topological sort to find an equivalent serial schedule
"""

from collections import defaultdict, deque
import json

class Operation:
    def __init__(self, tid, op_type, item):
        self.tid = tid  # Transaction ID
        self.op_type = op_type  # 'R' for Read, 'W' for Write, 'C' for Commit
        self.item = item  # Data item (X, Y, Z, A, B, C, etc.)
    
    def __repr__(self):
        if self.op_type == 'C':
            return f"C{self.tid}"
        return f"{self.op_type}{self.tid}({self.item})"
    
    def __eq__(self, other):
        return (self.tid == other.tid and 
                self.op_type == other.op_type and 
                self.item == other.item)
    
    def __hash__(self):
        return hash((self.tid, self.op_type, self.item))

def parse_schedule(schedule_str):
    """Parse a schedule string into a list of Operation objects"""
    operations = []
    parts = [p.strip() for p in schedule_str.split(',')]
    
    for part in parts:
        if part.startswith('C'):
            # Commit operation
            tid = int(part[1:])
            operations.append(Operation(tid, 'C', None))
        else:
            # Read or Write operation
            op_type = part[0]  # 'R' or 'W'
            tid = int(part[1])
            item = part[3:-1]  # Extract item from parentheses
            operations.append(Operation(tid, op_type, item))
    
    return operations

def conflicts(op1, op2):
    """
    Check if two operations conflict.
    Two operations conflict if:
    1. They are from different transactions
    2. They operate on the same data item
    3. At least one of them is a write operation
    4. Neither is a commit operation
    """
    # Skip commit operations
    if op1.op_type == 'C' or op2.op_type == 'C':
        return False
    
    # Must be different transactions
    if op1.tid == op2.tid:
        return False
    
    # Must operate on the same data item
    if op1.item != op2.item:
        return False
    
    # At least one must be a write
    return op1.op_type == 'W' or op2.op_type == 'W'

def build_precedence_graph(schedule):
    """
    Build a precedence graph for a given schedule.
    
    Algorithm:
    1. For each pair of operations (Oi, Oj) where i < j:
       a. If Oi and Oj conflict
       b. Add edge Ti -> Tj to the precedence graph
    
    Returns:
    - graph: dictionary where keys are transaction IDs and values are sets of
             transaction IDs that must come after them
    - edges: list of tuples (Ti, Tj, reason) for detailed explanation
    """
    graph = defaultdict(set)
    edges = []
    
    # Get all unique transaction IDs
    transaction_ids = set(op.tid for op in schedule if op.op_type != 'C')
    for tid in transaction_ids:
        graph[tid] = set()
    
    # Find all conflicting operation pairs
    for i in range(len(schedule)):
        for j in range(i + 1, len(schedule)):
            if conflicts(schedule[i], schedule[j]):
                # Ti must come before Tj in any equivalent serial schedule
                ti = schedule[i].tid
                tj = schedule[j].tid
                
                graph[ti].add(tj)
                
                # Determine conflict type
                if schedule[i].op_type == 'W' and schedule[j].op_type == 'W':
                    conflict_type = "write-write"
                elif schedule[i].op_type == 'W' and schedule[j].op_type == 'R':
                    conflict_type = "write-read"
                else:  # schedule[i].op_type == 'R' and schedule[j].op_type == 'W'
                    conflict_type = "read-write"
                
                edges.append((ti, tj, schedule[i].item, conflict_type, i, j))
    
    return graph, edges

def find_cycle_dfs(graph):
    """
    Detect cycles in a directed graph using DFS.
    Returns the cycle path if found, None otherwise.
    """
    visited = set() # Visited Nodes
    rec_stack = set() # Nodes currently in the recursion stack (active path)
    parent = {} # Tracking parent nodes for cycle reconstruction
    
    def dfs(node, path):
        # When entering a node, mark it visited and add it to the recursion stack
        visited.add(node)
        rec_stack.add(node)
        path.append(node)
        
        for neighbor in sorted(graph.get(node, [])):
            # For each unvisited neighbor, recursively call dfs
            #  If a unvisited neighbor leads to a cycle, return it immediately
            if neighbor not in visited:
                parent[neighbor] = node
                cycle = dfs(neighbor, path[:])
                if cycle:
                    return cycle
            elif neighbor in rec_stack:
                # Found a cycle
                cycle_start = path.index(neighbor)
                return path[cycle_start:] + [neighbor]
            
        # Remove the node from rec_stack when done (backtrack)
        rec_stack.remove(node)
        return None
    
    for node in sorted(graph.keys()):
        if node not in visited:
            cycle = dfs(node, [])
            if cycle:
                return cycle
    
    return None

def topological_sort(graph):
    """
    Perform topological sort using Kahn's algorithm.
    Returns the sorted order if no cycle exists, None otherwise.
    In the graph, Nodes are transactions and Edges are "must come before" constraints.
    GENERATES A VALID EQUIVALENT SERIAL SCHDULE !!
    EX: if T1 → T2 exists (T1 must precede T2), the sort ensures T1 appears before T2 in the output.
    """
    # Calculate in-degrees
    # Counting how many edges point into each node
    in_degree = defaultdict(int)
    all_nodes = set(graph.keys())
    for neighbors in graph.values():
        all_nodes.update(neighbors)
    
    for node in all_nodes:
        if node not in in_degree:
            in_degree[node] = 0
    
    for neighbors in graph.values():
        for neighbor in neighbors:
            in_degree[neighbor] += 1
    
    # Find all nodes with in-degree 0
    queue = deque([node for node in sorted(all_nodes) if in_degree[node] == 0])
    result = []
    
    while queue:
        node = queue.popleft()
        result.append(node)
        
        for neighbor in sorted(graph.get(node, [])):
            in_degree[neighbor] -= 1 # one less dependency
            if in_degree[neighbor] == 0: # can be processed next
                queue.append(neighbor)
    
    # If we couldn't process all nodes, there's a cycle
    if len(result) != len(all_nodes):
        return None
    
    return result

def find_all_topological_sorts(graph):
    """
    Find ALL possible topological sorts (equivalent serial schedules) using backtracking.
    This represents all valid serial schedules that respect the precedence constraints.
    """
    # Get all nodes and calculate how many dependencies each node has
    all_nodes = set(graph.keys())
    for neighbors in graph.values():
        all_nodes.update(neighbors)
    all_nodes = sorted(list(all_nodes))
    
    # Calculate in-degrees
    in_degree = {node: 0 for node in all_nodes}
    for neighbors in graph.values():
        for neighbor in neighbors:
            in_degree[neighbor] += 1
    
    all_sorts = []
    
    def backtrack(current_order, remaining_nodes, current_in_degree):
        # Base case: all nodes have been ordered
        if not remaining_nodes:
            all_sorts.append(list(current_order))
            return
        
        # Try adding each node with in-degree 0 (no remaining dependencies)
        for node in remaining_nodes:
            if current_in_degree[node] == 0:
                # Choose this node
                current_order.append(node)
                new_remaining = [n for n in remaining_nodes if n != node]
                
                # Update in-degrees for neighbors
                new_in_degree = current_in_degree.copy()
                for neighbor in graph.get(node, []):
                    new_in_degree[neighbor] -= 1
                
                # Recurse
                backtrack(current_order, new_remaining, new_in_degree)
                
                # Backtrack
                current_order.pop()
    
    backtrack([], all_nodes, in_degree)
    return all_sorts

def is_valid_serial_schedule(schedule_operations, serial_order, graph):
    """
    Check if a given serial order respects all precedence constraints.
    """
    # Create position map for the serial order
    position = {tid: i for i, tid in enumerate(serial_order)}
    
    # Check all edges in the graph
    for ti in graph:
        for tj in graph[ti]:
            if position[ti] >= position[tj]:
                return False
    
    return True

def test_conflict_serializability(schedule_name, schedule_str):
    """
    Test if a schedule is conflict serializable using the standard algorithm.
    """
    print("\n" + "=" * 80)
    print(f"TESTING: {schedule_name}")
    print("=" * 80)
    
    # Parse the schedule
    schedule = parse_schedule(schedule_str)
    
    print(f"\n Schedule:")
    print(f"   {schedule_str}")
    print(f"\n   Parsed as: {' → '.join(str(op) for op in schedule)}")
    
    # Step 1: Build the precedence graph
    print("\n" + "-" * 80)
    print("STEP 1: Building Precedence Graph")
    print("-" * 80)
    
    graph, edges = build_precedence_graph(schedule)
    
    if not edges:
        print("   No conflicts found - trivially serializable")
    else:
        print(f"   Found {len(edges)} conflict(s):\n")
        
        for ti, tj, item, conflict_type, pos_i, pos_j in edges:
            print(f"   • T{ti} → T{tj}  [{conflict_type} on {item}]")
            print(f"     {schedule[pos_i]} appears before {schedule[pos_j]}")
    
    # Display the precedence graph
    print(f"\n   Precedence Graph (Edges):")
    if not graph or all(len(neighbors) == 0 for neighbors in graph.values()):
        print("   (No edges - no conflicts)")
    else:
        for tid in sorted(graph.keys()):
            if graph[tid]:
                for neighbor in sorted(graph[tid]):
                    print(f"   T{tid} → T{neighbor}")
    
    # Step 2: Check for cycles
    print("\n" + "-" * 80)
    print("STEP 2: Checking for Cycles")
    print("-" * 80)
    
    cycle = find_cycle_dfs(graph)
    
    if cycle:
        print(f"    CYCLE DETECTED: {' → '.join(f'T{t}' for t in cycle)}")
        print(f"\n   This cycle means:")
        for i in range(len(cycle) - 1):
            print(f"   • T{cycle[i]} must precede T{cycle[i+1]}")
        print(f"   • But T{cycle[-1]} = T{cycle[0]}, creating a circular dependency!")
    else:
        print("   ✓ No cycles detected - graph is acyclic")
    
    # Step 3: Determine serializability
    print("\n" + "-" * 80)
    print("STEP 3: Serializability Result")
    print("-" * 80)
    
    is_serializable = (cycle is None)
    equivalent_serials = []
    
    if is_serializable:
        print("    CONFLICT SERIALIZABLE")
        
        # Find ALL equivalent serial schedules
        print("\n" + "-" * 80)
        print("STEP 4: Finding All Equivalent Serial Schedules")
        print("-" * 80)
        
        all_serial_orders = find_all_topological_sorts(graph)
        equivalent_serials = all_serial_orders
        
        print(f"\n   Total equivalent serial schedules: {len(all_serial_orders)}")
        print(f"\n   All Equivalent Serial Schedules:")
        
        for i, serial_order in enumerate(all_serial_orders, 1):
            print(f"   {i}. T{' → T'.join(map(str, serial_order))}")
        
        # Explain why these are all valid
        print(f"\n   Explanation:")
        print(f"   All {len(all_serial_orders)} serial schedule(s) above respect the precedence")
        print(f"   constraints from the conflict graph. Any of these orderings would")
        print(f"   produce the same final result as the original interleaved schedule.")
        
    else:
        print("     NOT CONFLICT SERIALIZABLE")
        print("\n   Reason: The precedence graph contains a cycle,")
        print("   which means no equivalent serial schedule exists.")
    
    return is_serializable, graph, edges, cycle, equivalent_serials

def main():
    
    # Define the schedules to test
    schedules = {
        "Schedule 1": "R1(X), W1(X), R3(X), W3(X), W2(X), R1(Y), W1(Y), C1, W3(Y), C3, R2(Y), W2(Y), C2",
        "Schedule 2": "R2(X), W2(X), R3(Y), W3(Y), R3(Z), W3(Z), C3, R2(Z), W2(Z), C2, R1(X), W1(X), C1",
        "Schedule 3": "W1(A), W2(B), W3(C), R1(X), R2(X), R1(Y), W1(X), C1, W2(Y), C2, W3(Y), C3"
    }
    
    results = {}
    
    # Test each schedule
    for schedule_name, schedule_str in schedules.items():
        is_serializable, graph, edges, cycle, equivalent_serials = test_conflict_serializability(
            schedule_name, schedule_str
        )
        results[schedule_name] = {
            "schedule": schedule_str,
            "serializable": is_serializable,
            "num_conflicts": len(edges),
            "has_cycle": cycle is not None,
            "equivalent_serial_schedules": [
                "T" + " → T".join(map(str, order)) for order in equivalent_serials
            ] if is_serializable else []
        }
    
    # Summary
    print("\n\n" + "=" * 80)
    print("SUMMARY OF RESULTS")
    print("=" * 80)
    
    for schedule_name, result in results.items():
        status = "SERIALIZABLE" if result["serializable"] else "✗ NOT SERIALIZABLE"
        print(f"\n{schedule_name}:")
        print(f"  Status: {status}")
        print(f"  Conflicts found: {result['num_conflicts']}")
        print(f"  Contains cycle: {'Yes' if result['has_cycle'] else 'No'}")
        
        if result["serializable"] and result["equivalent_serial_schedules"]:
            print(f"  Number of equivalent serial schedules: {len(result['equivalent_serial_schedules'])}")
            print(f"  Equivalent serial schedules:")
            for i, serial in enumerate(result["equivalent_serial_schedules"], 1):
                print(f"    {i}. {serial}")
    
    # Create a detailed output file
    print("\n" + "=" * 80)
    print("CREATING DETAILED REPORT")
    print("=" * 80)
    
    with open('detailed_results.txt', 'w') as f:
        f.write("=" * 80 + "\n")
        f.write("CONFLICT SERIALIZABILITY TEST - DETAILED RESULTS\n")
        f.write("=" * 80 + "\n\n")
        
        for schedule_name, result in results.items():
            f.write("\n" + "=" * 80 + "\n")
            f.write(f"{schedule_name}\n")
            f.write("=" * 80 + "\n\n")
            
            f.write(f"Original Schedule:\n")
            f.write(f"  {result['schedule']}\n\n")
            
            status = "CONFLICT SERIALIZABLE" if result["serializable"] else "NOT CONFLICT SERIALIZABLE"
            f.write(f"Result: {status}\n")
            f.write(f"Conflicts Detected: {result['num_conflicts']}\n")
            f.write(f"Contains Cycle: {'Yes' if result['has_cycle'] else 'No'}\n\n")
            
            if result["serializable"]:
                f.write(f"Number of Equivalent Serial Schedules: {len(result['equivalent_serial_schedules'])}\n\n")
                f.write("All Equivalent Serial Schedules:\n")
                for i, serial in enumerate(result["equivalent_serial_schedules"], 1):
                    f.write(f"  {i}. {serial}\n")
                f.write("\n")
            else:
                f.write("No equivalent serial schedule exists (schedule contains a cycle).\n\n")
    
    # Save results as JSON
    with open('serializability_results.json', 'w') as f:
        json.dump(results, f, indent=2)

if __name__ == "__main__":
    main()
