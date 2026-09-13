"""论文查重算法包。

提供文件读写、分词、相似度计算与自定义异常等子模块。
入口程序 main.py 会调用本包完成论文重复率的计算。
"""

from . import exceptions, file_io, similarity, tokenizer

__all__ = ["exceptions", "file_io", "similarity", "tokenizer"]
