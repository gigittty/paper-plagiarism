package plagiarism.exceptions;

/**
 * 论文查重程序的异常基类。
 *
 * <p>所有业务异常均继承此类，便于在入口处统一捕获并返回受控的退出码，
 * 避免出现「异常退出」而被判 0 分的情况。
 */
public class PlagiarismException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 构造带说明信息的异常。
     *
     * @param message 异常说明
     */
    public PlagiarismException(final String message) {
        super(message);
    }

    /**
     * 构造带原因异常的异常。
     *
     * @param message 异常说明
     * @param cause   底层原因
     */
    public PlagiarismException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
