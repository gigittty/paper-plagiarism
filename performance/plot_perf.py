"""Render the performance analysis bar chart from performance/timing.csv.

Run (managed venv with matplotlib installed):
    .venv/Scripts/python.exe performance/plot_perf.py
"""
import csv

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

STAGES = []
MS = []
with open("performance/timing.csv", encoding="utf-8") as handle:
    for row in csv.DictReader(handle):
        STAGES.append(row["stage"])
        MS.append(float(row["ms"]))

FIGURE, AX = plt.subplots(figsize=(9, 5))
BARS = AX.barh(STAGES, MS, color="#4C72B0")
AX.set_xlabel("Time (ms)")
AX.set_title("Paper Plagiarism - Stage Timing (cold run, incl. one-time jieba dict load)")
for BAR, VALUE in zip(BARS, MS):
    AX.text(
        BAR.get_width() + max(MS) * 0.01,
        BAR.get_y() + BAR.get_height() / 2,
        f"{VALUE:.2f}",
        va="center",
    )
AX.grid(axis="x", linestyle=":", alpha=0.5)
FIGURE.tight_layout()
FIGURE.savefig("performance/profile_result.png", dpi=120)
print("saved performance/profile_result.png")
