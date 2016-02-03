package com.diyphotobooth.lordbritishix.client;

public class IpCameraException extends Exception {
    private final int errorCode;

    public IpCameraException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
