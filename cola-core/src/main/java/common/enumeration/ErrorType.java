package common.enumeration;

public enum ErrorType {
    /**
     * 序列化异常
     */
    SERIALIZER_ERROR("序列化故障"), PLACEHOLDER_ERROR("占位符替换错误");

    private String msg;

    public String getMsg() {
        return msg;
    }

    ErrorType(String msg) {
        this.msg = msg;
    }
}
