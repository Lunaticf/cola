package cola.common;

import lombok.Data;

/**
 * @author lcf
 */
@Data
public class RpcResponse {
    private String requestId;
    private String error;
    private Object result;

    public boolean isError() {
        return error != null;
    }
}
