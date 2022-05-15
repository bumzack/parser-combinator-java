package at.grg.bumzack;

public class Result<OUTPUT> {

    private String input;
    private OUTPUT output;
    private ParserStatus status;
    private String errorMsg;

    public Result() {
    }

    public Result(final String input, final OUTPUT output, final ParserStatus status, final String errorMsg) {
        this.input = input;
        this.output = output;
        this.status = status;
        this.errorMsg = errorMsg;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public OUTPUT getOutput() {
        return output;
    }

    public void setOutput(OUTPUT output) {
        this.output = output;
    }

    public ParserStatus getStatus() {
        return status;
    }

    public void setStatus(ParserStatus status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "Result{" +
                "input='" + input + '\'' +
                ", output=" + output +
                ", status=" + status +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
