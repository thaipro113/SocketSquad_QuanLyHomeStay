package server.exception;

/**
 * Exception thrown when data validation fails
 * Used for input validation errors (invalid format, out of range, etc.)
 */
public class ValidationException extends Exception {

    private String fieldName;
    private Object invalidValue;

    /**
     * Constructor with message only
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with field name and invalid value for detailed error reporting
     */
    public ValidationException(String message, String fieldName, Object invalidValue) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    @Override
    public String toString() {
        if (fieldName != null) {
            return String.format("ValidationException: %s [Field: %s, Value: %s]",
                    getMessage(), fieldName, invalidValue);
        }
        return "ValidationException: " + getMessage();
    }
}