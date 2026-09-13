"""论文查重程序入口。

用法:
    python main.py [原文文件] [抄袭版论文的文件] [答案文件]

从命令行读取三个绝对路径，计算抄袭版相对原文的重复率，
以浮点型（百分比形式，保留两位小数）写入答案文件。

说明:
    - 重复率 = 余弦相似度 * 100，结果形如 "85.23"。
    - 若班级标准答案采用「比例形式（0.xx）」，只需将下方 RATE_SCALE 改为 1 即可。
    - 若班级参考算法为 SimHash，可替换 similarity 模块而不影响本入口。
"""

import sys

from plagiarism import file_io, similarity
from plagiarism.exceptions import (
    ArgumentError,
    EmptyTextError,
    EncodingError,
    FileReadError,
)

EXIT_OK = 0
EXIT_ERR = 1
# 重复率输出比例：100 表示百分比形式（85.23），1 表示比例形式（0.85）。
RATE_SCALE = 100


def parse_args(argv):
    """解析命令行参数，返回 (orig_path, copy_path, ans_path) 三元组。

    Args:
        argv: 命令行参数列表（含脚本名）。

    Returns:
        三个绝对路径组成的元组。

    Raises:
        ArgumentError: 当参数数量不为 3 时抛出。
    """
    if len(argv) != 4:
        raise ArgumentError(
            "用法: python main.py [原文文件] [抄袭版论文的文件] [答案文件]"
        )
    return argv[1], argv[2], argv[3]


def main(argv=None):
    """程序主流程，返回进程退出码。

    Args:
        argv: 命令行参数列表；默认取 sys.argv。

    Returns:
        退出码：0 表示成功，1 表示发生可处理的错误。
    """
    if argv is None:
        argv = sys.argv
    try:
        orig_path, copy_path, ans_path = parse_args(argv)
        orig_text = file_io.read_text(orig_path)
        copy_text = file_io.read_text(copy_path)
        sim = similarity.compute_similarity(orig_text, copy_text)
        rate = round(sim * RATE_SCALE, 2)
        file_io.write_result(ans_path, f"{rate:.2f}")
    except ArgumentError as exc:
        print(f"参数错误: {exc}", file=sys.stderr)
        return EXIT_ERR
    except FileReadError as exc:
        print(f"文件读取错误: {exc}", file=sys.stderr)
        return EXIT_ERR
    except EncodingError as exc:
        print(f"编码错误: {exc}", file=sys.stderr)
        return EXIT_ERR
    except EmptyTextError as exc:
        print(f"文本为空: {exc}", file=sys.stderr)
        return EXIT_ERR
    except Exception as exc:  # pylint: disable=broad-exception-caught
        # 兜底捕获，确保任何意外错误都以受控方式退出（退出码 1），
        # 避免「异常退出」触发评测 0 分。仅打印信息，不向上抛。
        print(f"未知错误: {exc}", file=sys.stderr)
        return EXIT_ERR
    return EXIT_OK


if __name__ == "__main__":
    sys.exit(main())
