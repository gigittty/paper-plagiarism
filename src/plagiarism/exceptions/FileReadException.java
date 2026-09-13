package plagiarism.exceptions;

/**
 * 文件读取异常：原文或抄袭版论文无法读取时抛出。
 */
public class FileReadException extends PlagiarismException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造文件读取异常。
     *
     * @param message 异常说明
     */
    public FileReadException(final String message) {
        super(message);
    }

    /**
     * 构造带原因的文件读取异常。
     *
     * @param message 异常说明
     * @param cause   底层原因
     */
    public FileReadException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
