package ifer.android.handnotepad.util;

/**
 * Created by ifer on 24/7/2017.
 */

public class APIError {
     private String message;
    private int code;

    public APIError() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
