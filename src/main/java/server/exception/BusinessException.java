package server.exception;

public class BusinessException extends Exception {

    private String errorCode;
    private Object relatedData;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, String errorCode, Object relatedData) {
        super(message);
        this.errorCode = errorCode;
        this.relatedData = relatedData;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getRelatedData() {
        return relatedData;
    }

    @Override
    public String toString() {
        if (errorCode != null) {
            return String.format("BusinessException [%s]: %s", errorCode, getMessage());
        }
        return "BusinessException: " + getMessage();
    }

    public static class ErrorCode {
        public static final String ROOM_OCCUPIED = "ROOM_OCCUPIED";
        public static final String ROOM_NOT_FOUND = "ROOM_NOT_FOUND";
        public static final String TENANT_NOT_FOUND = "TENANT_NOT_FOUND";
        public static final String DUPLICATE_ENTRY = "DUPLICATE_ENTRY";
        public static final String INVOICE_EXISTS = "INVOICE_EXISTS";
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String DATABASE_ERROR = "DATABASE_ERROR";
        public static final String FILE_NOT_FOUND = "FILE_NOT_FOUND";
    }
}