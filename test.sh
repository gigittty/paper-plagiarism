#!/usr/bin/env bash
# 运行 JUnit 5 单元测试并生成 JaCoCo 覆盖率报告
# 注意：classpath 分隔符在 Windows 为 ";"，在 Linux/macOS 请改为 ":"
set -euo pipefail
SEP=";"
[ -d out ] || bash build.sh
mkdir -p build/test-classes
javac -encoding UTF-8 -cp "out${SEP}lib/junit-platform-console-standalone.jar" \
  -d build/test-classes $(find test -name '*.java')
java -javaagent:lib/org.jacoco.agent-runtime.jar=destfile=build/jacoco.exec \
  -jar lib/junit-platform-console-standalone.jar \
  --classpath "out${SEP}build/test-classes${SEP}lib/jieba-analysis.jar" \
  --scan-classpath build/test-classes
java -jar lib/org.jacoco.cli-nodeps.jar report build/jacoco.exec \
  --classfiles out --sourcefiles src --html build/coverage-report
echo "[OK] coverage report -> build/coverage-report/index.html"
