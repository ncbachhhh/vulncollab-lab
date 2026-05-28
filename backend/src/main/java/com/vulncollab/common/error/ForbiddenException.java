package com.vulncollab.common.error;

public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }

    public ForbiddenException(String code, String message) {
        super(code, message);
    }
}
