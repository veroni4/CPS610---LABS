#!/bin/bash
# compile.sh - Compile all Java files for Two-Phase Locking

echo "=========================================="
echo "Two-Phase Locking - Compilation Script"
echo "=========================================="
echo ""

# Check if javac is available
if ! command -v javac &> /dev/null
then
    echo "ERROR: javac not found. Please install Java JDK."
    echo "Download from: https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi

echo "Java compiler found: $(javac -version 2>&1)"
echo ""

# Clean old class files
echo "Cleaning old .class files..."
rm -f *.class
echo ""

# Compile all Java files
echo "Compiling Java files..."
javac Lock.java Task.java Transaction.java LockTable.java TwoPhaseLocking.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    echo "Generated class files:"
    ls -1 *.class
    echo ""
    echo "To run the program, execute:"
    echo "  java TwoPhaseLocking"
    echo ""
else
    echo "✗ Compilation failed. Please check error messages above."
    exit 1
fi
