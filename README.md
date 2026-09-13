# 论文查重（Paper Plagiarism Checker）

> **注意**：请将本项目文件夹重命名为**你的学号**后，放入 GitHub 仓库根目录，例如
> `https://github.com/<用户名>/<仓库名>/tree/main/<学号>`，并在博客正文首行给出该链接。

给定「原文」与在其基础上经过**增、删、改**得到的「抄袭版」，计算并输出两者的重复率
（浮点型，保留两位小数）。

## 运行方式

```bash
# 1. 安装依赖
pip install -r requirements.txt

# 2. 运行（Windows 示例）
python main.py C:\tests\orig.txt C:\tests\orig_add.txt C:\tests\ans.txt
```

参数：`[原文文件] [抄袭版论文的文件] [答案文件]`（绝对路径，路径中不含空格）。
答案文件内容形如 `95.60`（重复率百分比，两位小数）。

> 若班级标准答案采用**比例形式**（`0.96`），把 `main.py` 的 `RATE_SCALE` 由 `100` 改为 `1` 即可。

## 目录结构

```text
.
├── main.py                 # 程序入口：解析命令行 → 读取 → 计算 → 写出
├── plagiarism/             # 核心包
│   ├── exceptions.py       # 自定义异常
│   ├── file_io.py          # 文件读写（UTF-8，异常转换）
│   ├── tokenizer.py        # 中文分词（jieba / 字符级降级）
│   └── similarity.py       # 词频向量 + 余弦相似度
├── test_plagiarism.py      # 单元测试（unittest，30+ 断言）
├── analyze.py              # cProfile 性能分析脚本
├── performance/
│   └── profile_result.png  # 性能分析图（自动生成）
├── samples/                # 自建样例（orig / add / del / dis / rep）
├── requirements.txt        # 运行依赖
├── requirements-dev.txt    # 测试 / 质量 / 性能分析依赖
├── PSP.md                  # PSP 工时表（预估 + 实际）
├── REPORT.md               # 博客正文（设计 / 性能 / 测试 / 异常）
├── .flake8 / .coveragerc   # 静态检查与覆盖率配置
└── README.md
```

## 算法

词频向量的**余弦相似度**：`sim = (A·B) / (|A|·|B|)`，重复率 `= sim × 100`。
对词序不敏感、对增删词稳健，适合检测抄袭改写的文本。详见 [REPORT.md](REPORT.md)。

## 测试与代码质量

```bash
# 单元测试 + 覆盖率（分支覆盖）
coverage run -m pytest test_plagiarism.py -v
coverage report -m

# 静态检查（应无任何警告）
flake8 .
pylint plagiarism main.py test_plagiarism.py analyze.py

# 性能分析（生成 performance/profile_result.png）
python analyze.py
```

当前结果：**单元测试全部通过 · 覆盖率 97%（核心包 100%）· flake8 0 警告 · pylint 10.00/10**。
