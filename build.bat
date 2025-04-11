@echo off

set SRC_DIR=src\com
set BIN_DIR=bin

if not exist %BIN_DIR% mkdir %BIN_DIR%

javac -d %BIN_DIR% %SRC_DIR%\CompressionUtils.java %SRC_DIR%\Compression.java %SRC_DIR%\QuadTreeNode.java

if %ERRORLEVEL% equ 0 (
    echo Compilation successful.

    java -cp %BIN_DIR% com.Compression
) else (
    echo Compilation failed.
)

pause
