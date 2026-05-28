package com.vulncollab.common.error;

public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }

    public NotFoundException(String code, String message) {
        super(code, message);
    }
}
