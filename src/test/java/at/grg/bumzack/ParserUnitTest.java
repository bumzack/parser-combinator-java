package at.grg.bumzack;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static at.grg.bumzack.Parser.*;
import static org.assertj.core.api.Assertions.assertThat;


public class ParserUnitTest {
    @Test
    void testMatchA_ok() {
        final var parseLetterA = letterA();
        final var result = parseLetterA.parse("a XXXXsdlfsjf");
        System.out.println("result " + result);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testMatchA_error() {
        final var parseLetterA = letterA();
        final var result = parseLetterA.parse("b sdfsf fdsf d");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testMatchLiteral_ok() {
        final var literalParserAbc = matchLiteral("abc");
        final var result = literalParserAbc.parse("abcabc asddasd");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testMatchLiteral_error() {
        final var literalParserAbc = matchLiteral("abc");
        final var result = literalParserAbc.parse("aba abc");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testIdentifier_ok() {
        final var idenitifierFn = identifier();
        final var result = idenitifierFn.parse("i-am-an-identifier");
        assertThat(result.getInput()).isEqualTo(StringUtils.EMPTY);
        assertThat(result.getOutput()).isEqualTo("i-am-an-identifier");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testIdentifier_ok2() {
        final var idenitifierFn = identifier();
        final var result = idenitifierFn.parse("not entirely an identifier");
        assertThat(result.getInput()).isEqualTo(" entirely an identifier");
        assertThat(result.getOutput()).isEqualTo("not");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testIdentifier_error() {
        final var idenitifierFn = identifier();
        final var result = idenitifierFn.parse("!not at all an identifier");
        assertThat(result.getInput()).isEqualTo(null);
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
        assertThat(result.getErrorMsg()).isEqualTo("!not at all an identifier");
    }

    @Test
    void testPairRight_ok() {
        final var opener = matchLiteral("<");
        final var idenitifierFn = identifier();

        final var tagOpenerRight = pairRight(opener, idenitifierFn);

        final var result = tagOpenerRight.parse("<my-first-element/>");
        assertThat(result.getInput()).isEqualTo("/>");
        assertThat(result.getOutput()).isEqualTo("my-first-element");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPairLeft_ok() {
        final var opener = matchLiteral("<");
        final var idenitifierFn = identifier();

        final var tagOpenerLeft = pairLeft(opener, idenitifierFn);

        final var left = tagOpenerLeft.parse("<my-first-element/>");
        assertThat(left.getInput()).isEqualTo("/>");
        assertThat(left.getOutput()).isEqualTo("<");
        assertThat(left.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPair_error() {
        final var opener = matchLiteral("<");
        final var idenitifierFn = identifier();

        final var tagOpener = pairRight(opener, idenitifierFn);

        final var result = tagOpener.parse("oops");
        assertThat(result.getErrorMsg()).isEqualTo("oops");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testPair_error2() {
        final var opener = matchLiteral("<");
        final var idenitifierFn = identifier();
        final var tagOpener = Parser.pairRight(opener, idenitifierFn);

        final var result = tagOpener.parse("<!oops");
        assertThat(result.getErrorMsg()).isEqualTo("!oops");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testMap_ok() {
        final ParserFunc<String> opener = matchLiteral("<");
        final ParserFunc<String> idenitifierFn = identifier();
        final ParserFunc<String> tagOpener = pairRight(opener, idenitifierFn);

        // ????
        final Function<String, String> map = StringUtils::upperCase;

        final var m = Parser.map(tagOpener, map);

        final var result = m.parse("<my-first-element/>");

        assertThat(result.getInput()).isEqualTo("/>");
        assertThat(result.getOutput()).isEqualTo("MY-FIRST-ELEMENT");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testOneOrMore_ok() {
        final ParserFunc<String> literal = matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = oneOrMore(literal);

        final var result = oneormore.parse("hahaha");

        assertThat(result.getInput()).isEqualTo("");
        final var r = result.getOutput();
        assertThat(r.get(0)).isEqualTo("ha");
        assertThat(r.get(1)).isEqualTo("ha");
        assertThat(r.get(2)).isEqualTo("ha");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testOneOrMore_error() {
        final ParserFunc<String> literal = matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = oneOrMore(literal);

        final var result = oneormore.parse("ahaha");

        assertThat(result.getErrorMsg()).isEqualTo("ahaha");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testOneOrMore_error2() {
        final ParserFunc<String> literal = matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = oneOrMore(literal);

        final var result = oneormore.parse("");

        assertThat(result.getErrorMsg()).isEqualTo("");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testZeroOrMore_ok() {
        final ParserFunc<String> literal = matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = zeroOrMore(literal);

        final var result = oneormore.parse("hahaha");

        assertThat(result.getInput()).isEqualTo("");
        final var r = result.getOutput();
        assertThat(r.get(0)).isEqualTo("ha");
        assertThat(r.get(1)).isEqualTo("ha");
        assertThat(r.get(2)).isEqualTo("ha");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testZeroOrMore_error() {
        final ParserFunc<String> literal = matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = zeroOrMore(literal);

        final var result = oneormore.parse("ahaha");

        assertThat(result.getOutput()).isEqualTo(Collections.emptyList());
        assertThat(result.getInput()).isEqualTo("ahaha");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testZeroOrMore_error2() {
        final ParserFunc<String> literal = matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = zeroOrMore(literal);

        final var result = oneormore.parse("");
        assertThat(result.getOutput()).isEqualTo(Collections.emptyList());
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testRight_ok() {
        final ParserFunc<String> identifier = identifier();
        final ParserFunc<String> literal = matchLiteral("<");
        final ParserFunc<String> right = right(literal, identifier);

        final var result = right.parse("<my-first-element/>");
        assertThat(result.getOutput()).isEqualTo("my-first-element");
        assertThat(result.getInput()).isEqualTo("/>");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testLeft_ok() {
        final ParserFunc<String> identifier = identifier();
        final ParserFunc<String> literal = matchLiteral("<");
        final ParserFunc<String> left = left(literal, identifier);

        final var result = left.parse("<my-first-element/>");
        assertThat(result.getOutput()).isEqualTo("<");
        assertThat(result.getInput()).isEqualTo("/>");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPred_ok() {
        final var pred = pred(Parser.anyChar(), c -> c.equals('o'));

        final var result = pred.parse("omg");
        assertThat(result.getOutput()).isEqualTo('o');
        assertThat(result.getInput()).isEqualTo("mg");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPred_error() {
        final ParserFunc<Character> pred = pred(Parser.anyChar(), c -> c.equals('o'));

        final var result = pred.parse("lol");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getInput()).isEqualTo(null);
        assertThat(result.getErrorMsg()).isEqualTo("lol");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testSpace0_ok() {
        final var space0 = space0();

        final var result = space0.parse("   lol");
        assertThat(result.getOutput()).isEqualTo(List.of(' ', ' ', ' '));
        assertThat(result.getInput()).isEqualTo("lol");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testSpace1_ok() {
        final var space1 = space1();

        final var result = space1.parse("   lol");
        assertThat(result.getOutput()).isEqualTo(List.of(' ', ' ', ' '));
        assertThat(result.getInput()).isEqualTo("lol");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testSpace1_error() {
        final var space1 = space1();

        final var result = space1.parse("lol");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getInput()).isEqualTo(null);
        assertThat(result.getErrorMsg()).isEqualTo("lol");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }


    @Test
    void testQuotedString_ok() {
        final var quotedString = quotedString();

        final var result = quotedString.parse("\"Hello Joe!\"");
        assertThat(result.getOutput()).isEqualTo("Hello Joe!");
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testQuotedString_error() {
        final var quotedString = quotedString();

        final var input = " Hello Joe!\"";
        final var result = quotedString.parse(input);
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getInput()).isEqualTo(null);
        assertThat(result.getErrorMsg()).isEqualTo(input);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }


}