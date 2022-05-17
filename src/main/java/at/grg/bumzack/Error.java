package at.grg.bumzack;


public class Error {

    private final String errorMsg;

    public Error(String errorMsg) {
        this.errorMsg = errorMsg;
    }


    public static Error of(final String msg) {
        return new Error(msg);
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public String toString() {
        return "Error{" +
                "errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
