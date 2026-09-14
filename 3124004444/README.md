# 论文查重（Paper Plagiarism Checker）· Java / JDK 17


给定「原文」与在其基础上经过**增、删、改**得到的「抄袭版」，计算并输出两者的重复率
（浮点型，保留两位小数）。

## 运行方式

```bash
java -jar main.jar [原文文件] [抄袭版论文的文件] [答案文件]
```

参数为绝对路径、以空格分隔、路径中不含空格，例如：

```bat
java -jar main.jar C:\tests\orig.txt C:\tests\orig_add.txt C:\tests\ans.txt
```

答案文件内容形如 `95.60`（重复率百分比，两位小数）。

## 环境要求

- **JDK 17**（`javac`/`java`/`jar`）。构建脚本以 `--release 17` 编译，产物字节码兼容 JDK 17。
- 依赖 `lib/jieba-analysis.jar`（中文分词，词典打包在 jar 内，**运行期不联网**）。

## 目录结构

```text
.
├── src/plagiarism/                 # 源码（package plagiarism）
│   ├── Main.java                   # 入口：解析参数 → 读取 → 计算 → 写出
│   ├── exceptions/                 # PlagiarismException + 3 个具体异常
│   ├── io/FileIO.java              # 文件读写（UTF-8，异常转换）
│   ├── seg/Tokenizer.java          # 中文分词（jieba 优先，2-gram 降级）
│   └── sim/CosineSimilarity.java   # 词频向量 + 余弦相似度
├── test/plagiarism/                # JUnit 5 单元测试（29 个用例）
├── lib/                            # 依赖 jar：jieba / junit / jacoco
├── manifest.txt                    # jar 清单（Main-Class + Class-Path）
├── performance/                    # 性能分析：Profile.java / timing.csv / 分析图
├── samples/                        # 课堂样例（orig.txt / orig_0.8_add.txt …）
├── main.jar                        # 已编译的可执行 jar（发布到 Releases）
├── build.bat / build.sh            # 编译打包脚本
├── test.bat / test.sh              # 单元测试 + JaCoCo 覆盖率脚本
├── checkstyle.xml                  # 代码规范检查配置（IDE 可选）
├── PSP.md / REPORT.md              # PSP 工时表 / 博客正文
└── README.md
```

## 构建

```bat
:: Windows
build.bat

:: Linux / macOS / Git Bash
bash build.sh
```

脚本执行：`javac --release 17 -encoding UTF-8 -Xlint:all -Werror` 编译全部源码，并用 `jar` 打包出
`main.jar`。其中 `-Xlint:all -Werror` 表示**把编译器警告当作错误**，确保**零警告**通过。

## 测试与代码质量

```bat
:: 单元测试 + 分支覆盖率（JUnit5 + JaCoCo）
test.bat
```

- 单元测试：`java -jar lib/junit-platform-console-standalone.jar ...`（详见脚本）
- 覆盖率报告：`build/coverage-report/index.html`
- 代码质量：`javac --release 17 -Xlint:all -Werror`（0 警告）；`checkstyle.xml` 供 IDE 辅助检查。

## 性能分析

```bat
:: 1) 生成分阶段耗时数据与 JFR 记录
javac -encoding UTF-8 -cp "out;lib/jieba-analysis.jar" -d build/perf performance/Profile.java
java -XX:StartFlightRecording=filename=performance/perf.jfr,settings=profile ^
     -cp "out;build/perf;lib/jieba-analysis.jar" perf.Profile

:: 2) 由 timing.csv 绘制性能分析图（需 Python + matplotlib）
python performance/plot_perf.py
```

## 算法

词频向量的**余弦相似度**：`sim = (A·B) / (|A|·|B|)`，重复率 `= sim × 100`。
对词序不敏感、对增删词稳健，适合检测抄袭改写的文本。详见 [REPORT.md](REPORT.md)。

当前结果：**单元测试 29 个（27 通过，2 个依赖课堂 del/dis 样例、样例缺失时自动跳过）· 整体覆盖率约 91%（分支约 81%）· javac 零警告 · 单次运行约 0.55s**。
