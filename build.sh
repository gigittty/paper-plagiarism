#!/usr/bin/env bash
# 编译并打包论文查重程序（需要 JDK 17），产出 main.jar
set -euo pipefail
rm -rf out && mkdir -p out
javac --release 17 -encoding UTF-8 -Xlint:all -Werror \
  -cp "lib/jieba-analysis.jar" -d out $(find src -name '*.java')
jar --create --file main.jar --manifest manifest.txt -C out .
echo "[OK] built main.jar"
