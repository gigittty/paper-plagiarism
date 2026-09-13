"""性能分析脚本（开发阶段使用，非程序运行依赖）。

使用 cProfile 对「读取 -> 分词 -> 相似度计算 -> 写出」全流程做性能采样，
找出消耗最大的函数，并用 matplotlib 生成性能分析图
performance/profile_result.png。

运行（已激活 venv，在项目根目录执行）：
    python analyze.py
"""

import cProfile
import pstats
from pathlib import Path

from plagiarism import file_io, similarity

HERE = Path(__file__).resolve().parent
SAMPLES = HERE / "samples"
CHART_PATH = HERE / "performance" / "profile_result.png"
ANS_PATH = HERE / "performance" / "profile_ans.txt"


def _make_large_text(base, repeat=200):
    """将基础文本重复多次，构造较大的输入以触发可观测的性能特征。"""
    return (base + "\n") * repeat


def run_pipeline(orig_text, copy_text, ans_path):
    """执行一次完整的查重计算并写出结果。"""
    sim = similarity.compute_similarity(orig_text, copy_text)
    rate = round(sim * 100, 2)
    file_io.write_result(ans_path, f"{rate:.2f}")
    return rate


def main():  # pylint: disable=too-many-locals
    """运行性能分析并生成图表。"""
    base_orig = file_io.read_text(str(SAMPLES / "orig.txt"))
    base_copy = file_io.read_text(str(SAMPLES / "orig_add.txt"))
    large_orig = _make_large_text(base_orig)
    large_copy = _make_large_text(base_copy)

    profiler = cProfile.Profile()
    profiler.enable()
    for _ in range(5):
        run_pipeline(large_orig, large_copy, str(ANS_PATH))
    profiler.disable()

    stats = pstats.Stats(profiler).sort_stats("cumulative")
    print("===== cProfile 统计（按累积时间 Top 15）=====")
    stats.print_stats(15)

    # 收集各函数的「独占时间(tottime)」用于绘图。
    rows = []
    for key, value in stats.stats.items():
        func_label = f"{Path(key[0]).name}:{key[1]}:{key[2]}"
        rows.append((func_label, value[2]))
    rows.sort(key=lambda item: item[1], reverse=True)
    top = rows[:12]

    try:
        import matplotlib  # pylint: disable=import-outside-toplevel

        matplotlib.use("Agg")
        import matplotlib.pyplot as plt  # pylint: disable=import-outside-toplevel

        names = [item[0] for item in top][::-1]
        times = [item[1] for item in top][::-1]
        fig, ax = plt.subplots(figsize=(11, 6))
        ax.barh(names, times, color="#4C72B0")
        ax.set_xlabel("Exclusive time (seconds)")
        ax.set_title("Paper Plagiarism Checker - Performance Profile (Top 12 by tottime)")
        fig.tight_layout()
        CHART_PATH.parent.mkdir(parents=True, exist_ok=True)
        fig.savefig(str(CHART_PATH), dpi=120)
        print(f"性能分析图已生成: {CHART_PATH}")
    except Exception as exc:  # pylint: disable=broad-exception-caught,too-many-locals
        print(f"生成图表失败（文本统计已输出）: {exc}")

    if ANS_PATH.exists():
        ANS_PATH.unlink()


if __name__ == "__main__":
    main()
