package vip.linhs.stock.model.vo;

public class CommonResponse {

    private String message;

    public CommonResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "CommonResponse [message=" + message + "]";
    }

    public static CommonResponse buildResponse(String message) {
        return new CommonResponse(message);
    }

}
