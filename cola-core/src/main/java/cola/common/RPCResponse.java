package cola.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lcf
 */
@Data
public class RPCResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;
    private Exception error;
    private Object result;

    public boolean hasError() {
        return error != null;
    }
}
