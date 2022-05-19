package at.grg.bumzack;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static at.grg.bumzack.SimpleXmlElementParser.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SimpleXmlElementParserTest {
    @Test
    void testAttributePair_ok() {
        final var attributePair = attributePair();

        final var input = "one=\"1\"";
        final var result = attributePair.parse(input);
        assertThat(result.getOutput()).isEqualTo(Pair.of("one", "1"));
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testAttributes_ok() {
        final var attributes = attributes();

        final var input = " one=\"1\"    two=\"2\"";
        final var result = attributes.parse(input);
        assertThat(result.getOutput()).isEqualTo(List.of(Pair.of("one", "1"), Pair.of("two", "2")));
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }


    @Test
    void testXmlELement_ok() {
        final var xmlSingleElement = xmlSingleElement();

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
        final var result = parentElement.parse(input);

        System.out.println("our XML " + result.getOutput());
        System.out.println("\n\n");
        assertThat(result.getOutput()).isEqualTo(expected);
        assertThat(result.getInput()).isEqualTo("");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }
}