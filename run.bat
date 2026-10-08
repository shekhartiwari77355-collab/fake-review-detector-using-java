@echo off
echo Compiling Fake Review Detector...
javac FakeReviewDetector.java
if errorlevel 1 (
    echo Compilation failed. Make sure Java JDK 17+ is installed.
    pause
    exit /b 1
)
echo.
echo Starting server...
echo Open http://localhost:8080 in your browser.
echo Press Ctrl+C to stop.
java FakeReviewDetector
pause
