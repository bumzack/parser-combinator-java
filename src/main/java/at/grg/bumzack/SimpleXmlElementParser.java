package at.grg.bumzack;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static at.grg.bumzack.Parser.*;

public class SimpleXmlElementParser {

    public static ParserFunc<Pair<String, String>> attributePair() {
        return input -> pair(
                identifier(),
                right(
                        matchLiteral("="),
                        quotedString()
                )
        ).parse(input);
    }

    public static ParserFunc<List<Pair<String, String>>> attributes() {
        return input -> zeroOrMore(
                right(
                        space1(),
                        attributePair()
                )
        ).parse(input);
    }

    public static ParserFunc<Pair<String, List<Pair<String, String>>>> xmlElementStart() {
        return input -> right(
                matchLiteral("<"),
                pair(
                        identifier(),
                        attributes()
                )
        ).parse(input);
    }

    public static ParserFunc<XmlElement> xmlSingleElement() {
        return input -> {
            final Function<Pair<String, List<Pair<String, String>>>, XmlElement> mapFn =
                    p -> {
                        final var xml = new XmlElement();
                        xml.setName(p.getLeft());
                        xml.setAttributes(p.getRight());
                        return xml;
                    };

            return map(
                    left(
                            xmlElementStart(),
                            matchLiteral("/>")
                    ),
                    mapFn
            ).parse(input);
        };
    }

    public static ParserFunc<XmlElement> xmlOpenElement() {
        return input -> {
            final Function<Pair<String, List<Pair<String, String>>>, XmlElement> mapFn =
                    p -> {
                        final var xml = new XmlElement();
                        xml.setName(p.getLeft());
                        xml.setAttributes(p.getRight());
                        return xml;
                    };

            return map(
                    left(
                            xmlElementStart(),
                            matchLiteral(">")
                    ),
                    mapFn
            ).parse(input);
        };
    }

    public static ParserFunc<String> xmlCloseElement(final String expectedName) {
        return input -> {
            final Predicate<String> pred = p -> StringUtils.equals(p, expectedName);

            return pred(
                    right(
                            matchLiteral("</"),
                            left(
                                    identifier(),
                                    matchLiteral(">")

                            )),
                    pred
            ).parse(input);
        };
    }

    public static ParserFunc<XmlElement> xmlParentElement() {
        return input -> {
            final BiFunction<XmlElement, List<XmlElement>, XmlElement> mapFn = (elem, l) -> {
                elem.setChildren(l);
                return elem;
            };

            final Function<XmlElement, ParserFunc<XmlElement>> andThenFn = xml -> mapBiFunc(
                    left(
                            zeroOrMore(xmlElement()),
                            xmlCloseElement(xml.getName())
                    ),
                    mapFn,
                    xml
            );

            final ParserFunc<XmlElement> parser = and_then(
                    xmlOpenElement(),
                    andThenFn
            );

            return parser.parse(input);
        };
    }

    public static ParserFunc<XmlElement> xmlElement() {
        return input -> whitespaceWrap(either(xmlSingleElement(), xmlParentElement())).parse(input);
    }

}
