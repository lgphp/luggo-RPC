package ai.totok.infra.luggo.core;

import java.io.Serializable;

/**
 * @author lgphp
 * @date 2020-03-15 10:52
 * @description
 */
public class RPCReponseVO implements Serializable {

    private static final long serialVersionUID = -4140602418425506468L;
    private String requestId;
    private Object retObj;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getRetObj() {
        return retObj;
    }

    public void setRetObj(Object retObj) {
        this.retObj = retObj;
    }
}
