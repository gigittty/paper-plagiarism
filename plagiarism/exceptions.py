"""自定义异常定义。

本模块集中定义查重程序在参数解析、文件读写与计算过程中可能抛出的异常，
便于调用方按异常类型做精细化处理，也便于单元测试针对每种异常场景断言。
"""


class PlagiarismError(Exception):
    """所有查重相关异常的基类。"""


class ArgumentError(PlagiarismError):
    """命令行参数数量或格式不正确。"""


class FileReadError(PlagiarismError):
    """文件无法读取（路径非法、文件不存在或无写入权限）。"""


class EncodingError(PlagiarismError):
    """文件编码不是预期的 UTF-8。"""


class EmptyTextError(PlagiarismError):
    """原文或抄袭版文本为空或仅含空白字符。"""
