package ai.totok.infra.luggo.core;

import java.io.Serializable;

/**
 * @author lgphp
 * @date 2020-03-15 10:52
 * @description
 */
public class RPCInvokeVO  implements Serializable {

    private static final long serialVersionUID = -8540474686290164480L;
    private String requestId;
    private String serverName;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[]  args;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
