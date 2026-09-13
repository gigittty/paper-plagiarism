# 论文查重（Paper Plagiarism Checker）· 作业仓库

本仓库为「高级软件工程」第一次个人编程作业（论文查重）的代码仓库。

- **学号**：3124004444
- **语言 / 环境**：Java（JDK 17）
- **项目源码**：[`3124004444/`](3124004444)
- **可执行程序**：`3124004444/main.jar`（同时发布在仓库 **Releases**：`v1.0`）

## 仓库结构

```text
.
├── .gitignore          # 忽略编译产物等
├── README.md           # 本文件（仓库说明）
└── 3124004444/         # 学号文件夹：完整项目
    ├── src/plagiarism/ # 源码
    ├── test/plagiarism/# JUnit 5 单元测试
    ├── lib/            # 依赖 jar（jieba / junit / jacoco）
    ├── main.jar        # 已编译可执行 jar
    ├── samples/        # 样例数据
    ├── performance/    # 性能分析脚本与性能图
    ├── PSP.md          # PSP 工时表
    ├── REPORT.md       # 博客正文
    ├── README.md       # 项目详细说明
    └── build.sh / build.bat / test.sh / test.bat
```

## 运行方式

```bash
java -jar main.jar [原文文件] [抄袭版论文的文件] [答案文件]
```

例如：

```bat
java -jar main.jar C:\tests\orig.txt C:\tests\orig_add.txt C:\tests\ans.txt
```

答案文件内容为重复率（浮点型，精确到小数点后两位），例如 `95.59`。

## 构建与测试

```bat
:: 编译打包（产出 main.jar），在学号文件夹内执行
cd 3124004444
build.bat

:: 运行单元测试并生成 JaCoCo 覆盖率报告
test.bat
```

> 详细的设计说明、性能改进、单元测试与异常处理见 [`3124004444/REPORT.md`](3124004444/REPORT.md)。
