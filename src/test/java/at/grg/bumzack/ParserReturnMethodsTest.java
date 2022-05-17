package at.grg.bumzack;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static at.grg.bumzack.ParserMethods.*;
import static org.assertj.core.api.Assertions.assertThat;


class ParserReturnMethodsTest {

    @Test
    void testMatchA_ok() {
        final var parseLetterA = letterA("a XXXXsdlfsjf");
        System.out.println("result " + parseLetterA);
        assertThat(parseLetterA.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testMatchA_error() {
        final var parseLetterA = letterA("b sdfsf fdsf d");
        assertThat(parseLetterA.get().getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testMatchLiteral_ok() {
        final var literalParserAbc = matchLiteral("abc");
        final var result = literalParserAbc.apply("abcabc asddasd");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testMatchLiteral_error() {
        final var literalParserAbc = matchLiteral("abc");
        final var result = literalParserAbc.apply("aba abc");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testIdentifier_ok() {
        final var idenitifierFn = identifier("i-am-an-identifier");
        assertThat(idenitifierFn.get().getInput()).isEqualTo(StringUtils.EMPTY);
        assertThat(idenitifierFn.get().getOutput()).isEqualTo("i-am-an-identifier");
        assertThat(idenitifierFn.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testIdentifier_ok2() {
        final var idenitifierFn = identifier("not entirely an identifier");
        assertThat(idenitifierFn.get().getInput()).isEqualTo(" entirely an identifier");
        assertThat(idenitifierFn.get().getOutput()).isEqualTo("not");
        assertThat(idenitifierFn.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testIdentifier_error() {
        final var idenitifierFn = identifier("!not at all an identifier");
        assertThat(idenitifierFn.get().getInput()).isEqualTo(null);
        assertThat(idenitifierFn.get().getOutput()).isEqualTo(null);
        assertThat(idenitifierFn.get().getStatus()).isEqualTo(ParserStatus.Error);
        assertThat(idenitifierFn.getErr().getErrorMsg()).isEqualTo("!not at all an identifier");
    }

    @Test
    void testPairRight_ok() {
        final var opener = matchLiteral("<");

        final var tagOpenerRight = pairRight(opener, ParserMethods::identifier);

        final var result = tagOpenerRight.apply("<my-first-element/>");
        assertThat(result.get().getInput()).isEqualTo("/>");
        assertThat(result.get().getOutput()).isEqualTo("my-first-element");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPairLeft_ok() {
        final var opener = matchLiteral("<");

        final var tagOpenerLeft = pairLeft(opener, ParserMethods::identifier);

        final var left = tagOpenerLeft.apply("<my-first-element/>");
        assertThat(left.get().getInput()).isEqualTo("/>");
        assertThat(left.get().getOutput()).isEqualTo("<");
        assertThat(left.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPair_error() {
        final var opener = matchLiteral("<");

        final var tagOpener = pairRight(opener, ParserMethods::identifier);

        final var result = tagOpener.apply("oops");
        assertThat(result.getErr().getErrorMsg()).isEqualTo("oops");
        assertThat(result.get().getOutput()).isEqualTo(null);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testPair_error2() {
        final var opener = matchLiteral("<");
        final var tagOpener = ParserMethods.<String, String>pairRight(opener, ParserMethods::identifier);

        final var result = tagOpener.apply("<!oops");
        assertThat(result.getErr().getErrorMsg()).isEqualTo("!oops");
        assertThat(result.get().getOutput()).isEqualTo(null);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testMap_ok() {
        final var opener = matchLiteral("<");
        final var tagOpener = pairRight(opener, ParserMethods::identifier);

        // ????
        final Function<String, String> map = StringUtils::upperCase;

        final var m = ParserMethods.map(tagOpener, map);

        final var result = m.apply("<my-first-element/>");

        assertThat(result.get().getInput()).isEqualTo("/>");
        assertThat(result.get().getOutput()).isEqualTo("MY-FIRST-ELEMENT");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testOneOrMore_ok() {
        final var literal = matchLiteral("ha");
        final var oneormore = oneOrMore(literal);

        final var result = oneormore.apply("hahaha");

        assertThat(result.get().getInput()).isEqualTo("");
        final var r = result.get().getOutput();
        assertThat(r.get(0)).isEqualTo("ha");
        assertThat(r.get(1)).isEqualTo("ha");
        assertThat(r.get(2)).isEqualTo("ha");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testOneOrMore_error() {
        final var literal = matchLiteral("ha");
        final var oneormore = oneOrMore(literal);

        final var result = oneormore.apply("ahaha");

        assertThat(result.getErr().getErrorMsg()).isEqualTo("ahaha");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testOneOrMore_error2() {
        final var literal = matchLiteral("ha");
        final var oneormore = oneOrMore(literal);

        final var result = oneormore.apply("");

        assertThat(result.getErr().getErrorMsg()).isEqualTo("");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testZeroOrMore_ok() {
        final var literal = matchLiteral("ha");
        final var oneormore = zeroOrMore(literal);

        final var result = oneormore.apply("hahaha");

        assertThat(result.get().getInput()).isEqualTo("");
        final var r = result.get().getOutput();
        assertThat(r.get(0)).isEqualTo("ha");
        assertThat(r.get(1)).isEqualTo("ha");
        assertThat(r.get(2)).isEqualTo("ha");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testZeroOrMore_error() {
        final var literal = matchLiteral("ha");
        final var oneormore = zeroOrMore(literal);

        final var result = oneormore.apply("ahaha");

        assertThat(result.get().getOutput()).isEqualTo(Collections.emptyList());
        assertThat(result.get().getInput()).isEqualTo("ahaha");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testZeroOrMore_error2() {
        final var literal = matchLiteral("ha");
        final var oneormore = zeroOrMore(literal);

        final var result = oneormore.apply("");
        assertThat(result.get().getOutput()).isEqualTo(null);
        assertThat(result.get().getInput()).isEqualTo(null);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.Error);
        assertThat(result.getErr().getErrorMsg()).isEqualTo("");
    }

    @Test
    void testRight_ok() {
        final var literal = matchLiteral("<");
        final var right = right(literal, ParserMethods::identifier);

        final var result = right.apply("<my-first-element/>");
        assertThat(result.get().getOutput()).isEqualTo("my-first-element");
        assertThat(result.get().getInput()).isEqualTo("/>");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testLeft_ok() {
        final var literal = matchLiteral("<");
        final var left = left(literal, ParserMethods::identifier);

        final var result = left.apply("<my-first-element/>");
        assertThat(result.get().getOutput()).isEqualTo("<");
        assertThat(result.get().getInput()).isEqualTo("/>");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPred_ok() {
        final var pred = pred(ParserMethods::anyChar, c -> c.equals('o'));

        final var result = pred.apply("omg");
        assertThat(result.get().getOutput()).isEqualTo('o');
        assertThat(result.get().getInput()).isEqualTo("mg");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPred_error() {
        final var pred = pred(ParserMethods::anyChar, c -> c.equals('o'));

        final var result = pred.apply("lol");
        assertThat(result.get().getOutput()).isEqualTo(null);
        assertThat(result.get().getInput()).isEqualTo(null);
        assertThat(result.getErr().getErrorMsg()).isEqualTo("lol");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testSpace0_ok() {
        final var space0 = space0();

        final var result = space0.apply("   lol");
        assertThat(result.get().getOutput()).isEqualTo(List.of(' ', ' ', ' '));
        assertThat(result.get().getInput()).isEqualTo("lol");
        assertThat(result.getErr()).isEqualTo(null);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testSpace1_ok() {
        final var space1 = space1();

        final var result = space1.apply("   lol");
        assertThat(result.get().getOutput()).isEqualTo(List.of(' ', ' ', ' '));
        assertThat(result.get().getInput()).isEqualTo("lol");
        assertThat(result.getErr()).isEqualTo(null);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testSpace1_error() {
        final var space1 = space1();

        final var result = space1.apply("lol");
        assertThat(result.get().getOutput()).isEqualTo(null);
        assertThat(result.get().getInput()).isEqualTo(null);
        assertThat(result.getErr().getErrorMsg()).isEqualTo("lol");
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.Error);
    }


    @Test
    void testQuotedString_ok() {
        final var quotedString = quotedString();

        final var result = quotedString.apply("\"Hello Joe!\"");
        assertThat(result.get().getOutput()).isEqualTo("Hello Joe!");
        assertThat(result.get().getInput()).isEqualTo("");
        assertThat(result.getErr()).isEqualTo(null);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testQuotedString_error() {
        final var quotedString = quotedString();

        final var input = " Hello Joe!\"";
        final var result = quotedString.apply(input);
        assertThat(result.get().getOutput()).isEqualTo(null);
        assertThat(result.get().getInput()).isEqualTo(null);
        assertThat(result.getErr().getErrorMsg()).isEqualTo(input);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testAttributePair_ok() {
        final var attributePair = attributePair();

        final var input = "one=\"1\"";
        final var result = attributePair.apply(input);
        assertThat(result.get().getOutput()).isEqualTo(Pair.of("one", "1"));
        assertThat(result.get().getInput()).isEqualTo("");
        assertThat(result.getErr()).isEqualTo(null);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testAttributes_ok() {
        final var attributes = attributes();

        final var input = " one=\"1\"    two=\"2\"";
        final var result = attributes.apply(input);
        assertThat(result.get().getOutput()).isEqualTo(List.of(Pair.of("one", "1"), Pair.of("two", "2")));
        assertThat(result.get().getInput()).isEqualTo("");
        assertThat(result.getErr()).isEqualTo(null);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testXmlELement_ok() {
        final var xmlSingleElement = xmlSingleElement();

        final var expected = new XmlElement();
        expected.setName("div");
        expected.setAttributes(List.of(Pair.of("class", "float")));
        expected.setChildren(null);


        final var input = "<div class=\"float\"/>";
        final var result = xmlSingleElement.apply(input);
        assertThat(result.get().getOutput()).isEqualTo(expected);
        assertThat(result.get().getInput()).isEqualTo("");
        assertThat(result.getErr()).isEqualTo(null);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testXmlParentElement_ok() {
        final var parentElement = xmlElement();

        final var bottom = new XmlElement();
        bottom.setName("bottom");
        bottom.setAttributes(List.of(Pair.of("label", "Another Bottom")));
        bottom.setChildren(null);

        final var middle = new XmlElement();
        middle.setName("middle");
        middle.setAttributes(Collections.emptyList());
        middle.setChildren(List.of(bottom));

        final var semiBottom = new XmlElement();
        semiBottom.setName("semi-bottom");
        semiBottom.setAttributes(List.of(Pair.of("label", "Bottom")));
        semiBottom.setChildren(null);

        final var expected = new XmlElement();
        expected.setName("top");
        expected.setAttributes(List.of(Pair.of("label", "Top")));
        expected.setChildren(List.of(semiBottom, middle));

        final var input = "<top label=\"Top\">\n" +
                "            <semi-bottom label=\"Bottom\"/>\n" +
                "            <middle>\n" +
                "                <bottom label=\"Another Bottom\"/>\n" +
                "            </middle>\n" +
                "        </top>";
        final var result = parentElement.apply(input);

        System.out.println("our XML " + result.get().getOutput());
        System.out.println("\n\n");
        assertThat(result.get().getOutput()).isEqualTo(expected);
        assertThat(result.get().getInput()).isEqualTo("");
        assertThat(result.get().getErrorMsg()).isEqualTo(null);
        assertThat(result.get().getStatus()).isEqualTo(ParserStatus.OK);
    }
}