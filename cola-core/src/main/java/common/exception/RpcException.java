package common.exception;

import common.enumeration.ErrorType;
import common.util.PlaceHolderUtil;

/**
 * @author lcf
 */
public class RpcException extends RuntimeException {
    /**
     * 具体异常类型
     */
    private ErrorType errorType;

    public RpcException(ErrorType errorType, String message, Object... args) {
        super(PlaceHolderUtil.replace(message, args));
        this.errorType = errorType;
    }

    public RpcException(Throwable cause, ErrorType errorType, String message, Object... args) {
        super(PlaceHolderUtil.replace(message, args), cause);
        this.errorType = errorType;
    }


    public ErrorType getErrorType() {
        return errorType;
    }
}
