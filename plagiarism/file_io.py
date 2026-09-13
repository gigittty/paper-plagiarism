"""文件读写模块。

负责以 UTF-8 编码读取原文 / 抄袭版文本，并将重复率结果写入答案文件。
所有底层 I/O 错误都会转换为自定义异常，便于上层统一处理与测试。
"""

import os

from .exceptions import EncodingError, FileReadError

EXPECTED_ENCODING = "utf-8"


def read_text(path):
    """读取文本文件内容。

    Args:
        path: 文件绝对路径。

    Returns:
        文件内容字符串。

    Raises:
        FileReadError: 当路径非法、文件不存在或无读取权限时抛出。
        EncodingError: 当文件不是 UTF-8 编码时抛出。
    """
    if not isinstance(path, str) or not path.strip():
        raise FileReadError(f"文件路径为空或非法: {path!r}")
    if not os.path.isfile(path):
        raise FileReadError(f"文件不存在: {path}")
    try:
        with open(path, "r", encoding=EXPECTED_ENCODING) as handle:
            return handle.read()
    except UnicodeDecodeError as exc:
        raise EncodingError(f"文件编码不是 UTF-8: {path}") from exc


def write_result(path, content):
    """将重复率结果写入答案文件。

    Args:
        path: 答案文件绝对路径。
        content: 待写入的字符串内容（如 "85.23"）。

    Raises:
        FileReadError: 当路径非法或无法写入（无权限等）时抛出。
    """
    if not isinstance(path, str) or not path.strip():
        raise FileReadError(f"答案文件路径为空或非法: {path!r}")
    try:
        with open(path, "w", encoding=EXPECTED_ENCODING) as handle:
            handle.write(content)
    except OSError as exc:
        raise FileReadError(f"无法写入答案文件: {path}") from exc
