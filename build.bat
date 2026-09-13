@echo off
setlocal
rem 编译并打包论文查重程序（需要 JDK 11+），产出 main.jar
if not exist out mkdir out
javac --release 11 -encoding UTF-8 -Xlint:all -Werror -cp "lib\jieba-analysis.jar" -d out ^
  src\plagiarism\Main.java ^
  src\plagiarism\exceptions\*.java ^
  src\plagiarism\io\*.java ^
  src\plagiarism\seg\*.java ^
  src\plagiarism\sim\*.java
if errorlevel 1 (
  echo [FAIL] compile error
  exit /b 1
)
jar --create --file main.jar --manifest manifest.txt -C out .
if errorlevel 1 exit /b 1
echo [OK] built main.jar
endlocal
