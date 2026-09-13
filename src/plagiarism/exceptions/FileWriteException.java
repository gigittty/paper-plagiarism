package plagiarism.exceptions;

/**
 * 文件写入异常：答案文件无法写出时抛出。
 */
public class FileWriteException extends PlagiarismException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造文件写入异常。
     *
     * @param message 异常说明
     */
    public FileWriteException(final String message) {
        super(message);
    }

    /**
     * 构造带原因的文件写入异常。
     *
     * @param message 异常说明
     * @param cause   底层原因
     */
    public FileWriteException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
