package ifer.android.handnotepad.api;

/**
 * Created by ifer on 20/6/2017.
 */

public class ResponseMessage {

    private int status;
    private String message;
    private String data;

    public ResponseMessage(int status, String message) {
        super();
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
