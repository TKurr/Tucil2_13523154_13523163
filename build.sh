#!/bin/bash

# Set the source files and output directory
SRC_DIR="src/com"
BIN_DIR="bin"

# Create the output directory if it doesn't exist
mkdir -p $BIN_DIR

# Compile the Java files
javac -d $BIN_DIR $SRC_DIR/CompressionUtils.java $SRC_DIR/Compression.java $SRC_DIR/QuadTreeNode.java

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful."

    # Run the program
    java -cp $BIN_DIR com.Compression
else
    echo "Compilation failed."
fi
