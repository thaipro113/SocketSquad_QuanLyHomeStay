package common;

import java.io.Serializable;

public class Payload implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Action {
        LOGIN,
        LOGOUT,
        LOGOUT,
        GET_ROOMS,
        ADD_ROOM,
        UPDATE_ROOM,
        DELETE_ROOM,
        GET_SERVICES,
        GET_TENANTS,
        ADD_TENANT,
        UPDATE_TENANT,
        DELETE_TENANT,
        CALCULATE_COST,
        GET_INVOICES,
        DELETE_INVOICE,
        UPLOAD_FILE,
        DOWNLOAD_FILE,
        GET_IMAGE,
        CHECKOUT_ROOM,
        CHECKIN_ROOM,
        GET_TENANT_HISTORY,
        SUCCESS,
        FAILURE
    }

    private Action action;
    private Object data;
    private String message;

    public Payload(Action action, Object data) {
        this.action = action;
        this.data = data;
    }

    public Payload(Action action, Object data, String message) {
        this.action = action;
        this.data = data;
        this.message = message;
    }

    public Action getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
