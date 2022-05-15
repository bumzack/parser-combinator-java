package at.grg.bumzack;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;


class ParserTest {
    final Parser parser = new Parser();

    @Test
    void testMatchA_ok() {
        final var parseLetterA = parser.letterA();
        final var result = parseLetterA.parse("a XXXXsdlfsjf");
        System.out.println("result " + result);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testMatchA_error() {
        final var parseLetterA = parser.letterA();
        final var result = parseLetterA.parse("b sdfsf fdsf d");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testMatchLiteral_ok() {
        final var literalParserAbc = parser.matchLiteral("abc");
        final var result = literalParserAbc.parse("abcabc asddasd");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testMatchLiteral_error() {
        final var literalParserAbc = parser.matchLiteral("abc");
        final var result = literalParserAbc.parse("aba abc");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testIdentifier_ok() {
        final var idenitifierFn = parser.identifier();
        final var result = idenitifierFn.parse("i-am-an-identifier");
        assertThat(result.getInput()).isEqualTo(StringUtils.EMPTY);
        assertThat(result.getOutput()).isEqualTo("i-am-an-identifier");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testIdentifier_ok2() {
        final var idenitifierFn = parser.identifier();
        final var result = idenitifierFn.parse("not entirely an identifier");
        assertThat(result.getInput()).isEqualTo(" entirely an identifier");
        assertThat(result.getOutput()).isEqualTo("not");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testIdentifier_error() {
        final var idenitifierFn = parser.identifier();
        final var result = idenitifierFn.parse("!not at all an identifier");
        assertThat(result.getInput()).isEqualTo(null);
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
        assertThat(result.getErrorMsg()).isEqualTo("!not at all an identifier");
    }

    @Test
    void testPairRight_ok() {
        final var opener = parser.matchLiteral("<");
        final var idenitifierFn = parser.identifier();

        final var tagOpenerRight = parser.pairRight(opener, idenitifierFn);

        final var result = tagOpenerRight.parse("<my-first-element/>");
        assertThat(result.getInput()).isEqualTo("/>");
        assertThat(result.getOutput()).isEqualTo("my-first-element");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPairLeft_ok() {
        final var opener = parser.matchLiteral("<");
        final var idenitifierFn = parser.identifier();

        final var tagOpenerLeft = parser.pairLeft(opener, idenitifierFn);

        final var left = tagOpenerLeft.parse("<my-first-element/>");
        assertThat(left.getInput()).isEqualTo("/>");
        assertThat(left.getOutput()).isEqualTo("<");
        assertThat(left.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPair_error() {
        final var opener = parser.matchLiteral("<");
        final var idenitifierFn = parser.identifier();

        final var tagOpener = parser.pairRight(opener, idenitifierFn);

        final var result = tagOpener.parse("oops");
        assertThat(result.getErrorMsg()).isEqualTo("oops");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testPair_error2() {
        final var opener = parser.matchLiteral("<");
        final var idenitifierFn = parser.identifier();
        final var tagOpener = parser.pairRight(opener, idenitifierFn);

        final var result = tagOpener.parse("<!oops");
        assertThat(result.getErrorMsg()).isEqualTo("!oops");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testMap_ok() {
        final ParserFunc<String> opener = parser.matchLiteral("<");
        final ParserFunc<String> idenitifierFn = parser.identifier();
        final ParserFunc<String> tagOpener = parser.pairRight(opener, idenitifierFn);

        // ????
        final Function<String, String> map = p -> StringUtils.upperCase(p);

        final var m = parser.map(tagOpener, map);

        final var result = m.parse("<my-first-element/>");

        assertThat(result.getInput()).isEqualTo("/>");
        assertThat(result.getOutput()).isEqualTo("MY-FIRST-ELEMENT");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
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
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testOneOrMore_error() {
        final ParserFunc<String> literal = parser.matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = parser.oneOrMore(literal);

        final var result = oneormore.parse("ahaha");

        assertThat(result.getErrorMsg()).isEqualTo("ahaha");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testOneOrMore_error2() {
        final ParserFunc<String> literal = parser.matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = parser.oneOrMore(literal);

        final var result = oneormore.parse("");

        assertThat(result.getErrorMsg()).isEqualTo("");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
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
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testZeroOrMore_error() {
        final ParserFunc<String> literal = parser.matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = parser.zeroOrMore(literal);

        final var result = oneormore.parse("ahaha");

        assertThat(result.getOutput()).isEqualTo(Collections.emptyList());
        assertThat(result.getInput()).isEqualTo("ahaha");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testZeroOrMore_error2() {
        final ParserFunc<String> literal = parser.matchLiteral("ha");
        final ParserFunc<List<String>> oneormore = parser.zeroOrMore(literal);

        final var result = oneormore.parse("");
        assertThat(result.getOutput()).isEqualTo(Collections.emptyList());
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testRight_ok() {
        final ParserFunc<String> identifier = parser.identifier();
        final ParserFunc<String> literal = parser.matchLiteral("<");
        final ParserFunc<String> right = parser.right(literal, identifier);

        final var result = right.parse("<my-first-element/>");
        assertThat(result.getOutput()).isEqualTo("my-first-element");
        assertThat(result.getInput()).isEqualTo("/>");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testLeft_ok() {
        final ParserFunc<String> identifier = parser.identifier();
        final ParserFunc<String> literal = parser.matchLiteral("<");
        final ParserFunc<String> left = parser.left(literal, identifier);

        final var result = left.parse("<my-first-element/>");
        assertThat(result.getOutput()).isEqualTo("<");
        assertThat(result.getInput()).isEqualTo("/>");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPred_ok() {
        final var pred = parser.pred(parser.anyChar(), c -> c.equals('o'));

        final var result = pred.parse("omg");
        assertThat(result.getOutput()).isEqualTo('o');
        assertThat(result.getInput()).isEqualTo("mg");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testPred_error() {
        final ParserFunc<Character> pred = parser.pred(parser.anyChar(), c -> c.equals('o'));

        final var result = pred.parse("lol");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getInput()).isEqualTo(null);
        assertThat(result.getErrorMsg()).isEqualTo("lol");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testSpace0_ok() {
        final var space0 = parser.space0();

        final var result = space0.parse("   lol");
        assertThat(result.getOutput()).isEqualTo(List.of(' ', ' ', ' '));
        assertThat(result.getInput()).isEqualTo("lol");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testSpace1_ok() {
        final var space1 = parser.space1();

        final var result = space1.parse("   lol");
        assertThat(result.getOutput()).isEqualTo(List.of(' ', ' ', ' '));
        assertThat(result.getInput()).isEqualTo("lol");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testSpace1_error() {
        final var space1 = parser.space1();

        final var result = space1.parse("lol");
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getInput()).isEqualTo(null);
        assertThat(result.getErrorMsg()).isEqualTo("lol");
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }


    @Test
    void testQuotedString_ok() {
        final var quotedString = parser.quotedString();

        final var result = quotedString.parse("\"Hello Joe!\"");
        assertThat(result.getOutput()).isEqualTo("Hello Joe!");
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testQuotedString_error() {
        final var quotedString = parser.quotedString();

        final var input = " Hello Joe!\"";
        final var result = quotedString.parse(input);
        assertThat(result.getOutput()).isEqualTo(null);
        assertThat(result.getInput()).isEqualTo(null);
        assertThat(result.getErrorMsg()).isEqualTo(input);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.Error);
    }

    @Test
    void testAttributePair_ok() {
        final var attributePair = parser.attributePair();

        final var input = "one=\"1\"";
        final var result = attributePair.parse(input);
        assertThat(result.getOutput()).isEqualTo(Pair.of("one", "1"));
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testAttributes_ok() {
        final var attributes = parser.attributes();

        final var input = " one=\"1\"    two=\"2\"";
        final var result = attributes.parse(input);
        assertThat(result.getOutput()).isEqualTo(List.of(Pair.of("one", "1"), Pair.of("two", "2")));
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testXmlELement_ok() {
        final var xmlSingleElement = parser.xmlSingleElement();

        final var expected = new XmlElement();
        expected.setName("div");
        expected.setAttributes(List.of(Pair.of("class", "float")));
        expected.setChildren(null);


        final var input = "<div class=\"float\"/>";
        final var result = xmlSingleElement.parse(input);
        assertThat(result.getOutput()).isEqualTo(expected);
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testXmlParentElement_ok() {
        final var parentElement = parser.xmlElement();

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
        final var result = parentElement.parse(input);

        System.out.println("our XML " + result.getOutput());
        System.out.println("\n\n");
        assertThat(result.getOutput()).isEqualTo(expected);
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }
}