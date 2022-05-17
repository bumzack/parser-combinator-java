package at.grg.bumzack;

public class ParseResult<OUTPUT, INPUT> {

    private INPUT input;
    private OUTPUT output;
    private ParserStatus status;

    public ParseResult() {
    }

    public ParseResult(final INPUT input, final OUTPUT output, final ParserStatus status) {
        this.input = input;
        this.output = output;
        this.status = status;
    }

    public static <OOUTPUT, IINPUT> ParseResult<OOUTPUT, IINPUT> of(final IINPUT input, final OOUTPUT output, final ParserStatus status) {
        return new ParseResult<>(input, output, status);
    }

    public INPUT getInput() {
        return input;
    }

    public void setInput(INPUT input) {
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

    @Override
    public String toString() {
        return "Result{" +
                "input='" + input + '\'' +
                ", output=" + output +
                ", status=" + status +
                '}';
    }
}
