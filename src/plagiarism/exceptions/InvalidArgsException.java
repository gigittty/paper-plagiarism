package plagiarism.exceptions;

/**
 * 参数异常：命令行参数数量或内容不合法时抛出。
 */
public class InvalidArgsException extends PlagiarismException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造参数异常。
     *
     * @param message 异常说明
     */
    public InvalidArgsException(final String message) {
        super(message);
    }
}
