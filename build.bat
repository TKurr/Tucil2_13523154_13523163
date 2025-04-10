@echo off

:: Set the source files and output directory
set SRC_DIR=src\com
set BIN_DIR=bin

:: Create the output directory if it doesn't exist
if not exist %BIN_DIR% mkdir %BIN_DIR%

:: Compile the Java files
javac -d %BIN_DIR% %SRC_DIR%\CompressionUtils.java %SRC_DIR%\Compression.java %SRC_DIR%\QuadTreeNode.java

:: Check if the compilation was successful
if %ERRORLEVEL% equ 0 (
    echo Compilation successful.

    :: Run the program
    java -cp %BIN_DIR% com.Compression
) else (
    echo Compilation failed.
)

pause
