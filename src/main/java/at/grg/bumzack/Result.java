package at.grg.bumzack;

public class Result<OUTPUT> {

    private String input;
    private OUTPUT output;
    private ParserStatus error;
    private String errorMsg;

    public Result() {
    }

    public Result(String input, OUTPUT output, ParserStatus error, String errorMsg) {
        this.input = input;
        this.output = output;
        this.error = error;
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

    public ParserStatus getError() {
        return error;
    }

    public void setError(ParserStatus error) {
        this.error = error;
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
                ", error=" + error +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
