package com.vulncollab.common.error;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }

    public UnauthorizedException(String code, String message) {
        super(code, message);
    }
}
