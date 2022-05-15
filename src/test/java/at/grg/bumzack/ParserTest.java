package at.grg.bumzack;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;


class ParserTest {
    final Parser<String, String, String> parser = new Parser();

    @Test
    void testMatchA_ok() {
        final var parseLetterA = parser.letterA();
        final var result = parseLetterA.parse("a XXXXsdlfsjf");
        System.out.println("result " + result);
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testMatchA_error() {
        final var parseLetterA = parser.letterA();
        final var result = parseLetterA.parse("b sdfsf fdsf d");
        assertThat(result.getError()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testMatchLiteral_ok() {
        final var literalParserAbc = parser.matchLiteral("abc");
        final var result = literalParserAbc.parse("abcabc asddasd");
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testMatchLiteral_error() {
        final var literalParserAbc = parser.matchLiteral("abc");
        final var result = literalParserAbc.parse("aba abc");
        assertThat(result.getError()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testIdentifier_ok() {
        final var idenitifierFn = parser.idenitifier();
        final var result = idenitifierFn.parse("i-am-an-identifier");
        assertThat(result.getInput()).isEqualTo(StringUtils.EMPTY);
        assertThat(result.getOutput()).isEqualTo("i-am-an-identifier");
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testIdentifier_ok2() {
        final var idenitifierFn = parser.idenitifier();
        final var result = idenitifierFn.parse("not entirely an identifier");
        assertThat(result.getInput()).isEqualTo(" entirely an identifier");
        assertThat(result.getOutput()).isEqualTo("not");
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testIdentifier_error() {
        final var idenitifierFn = parser.idenitifier();
        final var result = idenitifierFn.parse("!not at all an identifier");
        assertThat(result.getInput()).isEqualTo("!not at all an identifier");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getError()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testPairRight_ok() {
        final var opener = parser.matchLiteral("<");
        final var idenitifierFn = parser.idenitifier();

        final var tagOpenerRight = parser.pairRight(opener, idenitifierFn);

        final var result = tagOpenerRight.parse("<my-first-element/>");
        assertThat(result.getInput()).isEqualTo("/>");
        assertThat(result.getOutput()).isEqualTo("my-first-element");
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPairLeft_ok() {
        final var opener = parser.matchLiteral("<");
        final var idenitifierFn = parser.idenitifier();

        final var tagOpenerLeft = parser.pairLeft(opener, idenitifierFn);

        final var left = tagOpenerLeft.parse("<my-first-element/>");
        assertThat(left.getInput()).isEqualTo("/>");
        assertThat(left.getOutput()).isEqualTo("<");
        assertThat(left.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPair_error() {
        final var opener = parser.matchLiteral("<");
        final var idenitifierFn = parser.idenitifier();

        final var tagOpener = parser.pairRight(opener, idenitifierFn);

        final var result = tagOpener.parse("oops");
        assertThat(result.getErrorMsg()).isEqualTo("oops");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getError()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testPair_error2() {
        final var opener = parser.matchLiteral("<");
        final var idenitifierFn = parser.idenitifier();
        final var tagOpener = parser.pairRight(opener, idenitifierFn);

        final var result = tagOpener.parse("<!oops");
        assertThat(result.getErrorMsg()).isEqualTo("!oops");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getError()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testMap_ok() {
        final ParserFunc<String> opener = parser.matchLiteral("<");
        final ParserFunc<String> idenitifierFn = parser.idenitifier();
        final ParserFunc<String> tagOpener = parser.pairRight(opener, idenitifierFn);

        // ????
        final Function<String, String> map = p -> StringUtils.upperCase(p);

        final var m = parser.map(tagOpener, map);

        final var result = m.parse("<my-first-element/>");

        assertThat(result.getInput()).isEqualTo("/>");
        assertThat(result.getOutput()).isEqualTo("MY-FIRST-ELEMENT");
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testOneOrMore_ok() {
        final ParserFunc<String> literal = parser.matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = parser.oneOrMore(literal);

        final var result = oneormore.parse("hahaha");

        assertThat(result.getInput()).isEqualTo("");
        final var r = result.getOutput();
        assertThat(r.get(0)).isEqualTo("ha");
        assertThat(r.get(1)).isEqualTo("ha");
        assertThat(r.get(2)).isEqualTo("ha");
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testOneOrMore_error() {
        final ParserFunc<String> literal = parser.matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = parser.oneOrMore(literal);

        final var result = oneormore.parse("ahaha");

        assertThat(result.getErrorMsg()).isEqualTo("ahaha");
        assertThat(result.getError()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testOneOrMore_error2() {
        final ParserFunc<String> literal = parser.matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = parser.oneOrMore(literal);

        final var result = oneormore.parse("");

        assertThat(result.getErrorMsg()).isEqualTo("");
        assertThat(result.getError()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testZeroOrMore_ok() {
        final ParserFunc<String> literal = parser.matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = parser.zeroOrMore(literal);

        final var result = oneormore.parse("hahaha");

        assertThat(result.getInput()).isEqualTo("");
        final var r = result.getOutput();
        assertThat(r.get(0)).isEqualTo("ha");
        assertThat(r.get(1)).isEqualTo("ha");
        assertThat(r.get(2)).isEqualTo("ha");
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testZeroOrMore_error() {
        final ParserFunc<String> literal = parser.matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = parser.zeroOrMore(literal);

        final var result = oneormore.parse("ahaha");

        assertThat(result.getOutput()).isEqualTo(Collections.emptyList());
        assertThat(result.getInput()).isEqualTo("ahaha");
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testZeroOrMore_error2() {
        final ParserFunc<String> literal = parser.matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = parser.zeroOrMore(literal);

        final var result = oneormore.parse("");
        assertThat(result.getOutput()).isEqualTo(Collections.emptyList());
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testRight_ok() {
        final ParserFunc<String> identifier = parser.idenitifier();
        final ParserFunc<String> literal = parser.matchLiteral("<");
        final ParserFunc<String> right = parser.right(literal, identifier);

        final var result = right.parse("<my-first-element/>");
        assertThat(result.getOutput()).isEqualTo("/>");
        assertThat(result.getInput()).isEqualTo("my-first-element");
        assertThat(result.getError()).isEqualTo(ParserStatus.OK);
    }

}