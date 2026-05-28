package com.vulncollab.common.error;

public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", message);
    }

    public BadRequestException(String code, String message) {
        super(code, message);
    }
}
