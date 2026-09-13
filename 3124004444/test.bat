@echo off
setlocal
rem 运行 JUnit 5 单元测试并生成 JaCoCo 覆盖率报告
if not exist out call build.bat
if errorlevel 1 exit /b 1
if not exist build\test-classes mkdir build\test-classes
javac -encoding UTF-8 -cp "out;lib\junit-platform-console-standalone.jar" -d build\test-classes ^
  test\plagiarism\*.java ^
  test\plagiarism\io\*.java ^
  test\plagiarism\seg\*.java ^
  test\plagiarism\sim\*.java
if errorlevel 1 exit /b 1
java -javaagent:lib\org.jacoco.agent-runtime.jar=destfile=build\jacoco.exec ^
  -jar lib\junit-platform-console-standalone.jar ^
  --classpath "out;build\test-classes;lib\jieba-analysis.jar" ^
  --scan-classpath build\test-classes
java -jar lib\org.jacoco.cli-nodeps.jar report build\jacoco.exec ^
  --classfiles out --sourcefiles src --html build\coverage-report
echo [OK] tests done; coverage report -^> build\coverage-report\index.html
endlocal
