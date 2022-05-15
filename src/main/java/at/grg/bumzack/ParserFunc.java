package at.grg.bumzack;

@FunctionalInterface
public interface ParserFunc<OUTPUT> {

    Result<OUTPUT> parse(final String input);

}
