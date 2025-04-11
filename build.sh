SRC_DIR="src/com"
BIN_DIR="bin"

mkdir -p $BIN_DIR

javac -d $BIN_DIR $SRC_DIR/CompressionUtils.java $SRC_DIR/Compression.java $SRC_DIR/QuadTreeNode.java

java -cp $BIN_DIR com.Compression