package com.hedvig.notificationService.serviceIntegration.memberService.exceptions;

public class BankIdError extends RuntimeException {

    private static final long serialVersionUID = 7440313931511731452L;
    private final ErrorType errorType;
    private final String message;

    public BankIdError(ErrorType errorType, String message) {
        this.errorType = errorType;
        this.message = message;
    }


    public ErrorType getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }
}
