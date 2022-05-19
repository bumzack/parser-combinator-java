package at.grg.bumzack;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static at.grg.bumzack.Parser.*;
import static at.grg.bumzack.SimpleXmlElementParser.attributes;

public class CsvParser {


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

    public static ParserFunc<CsvLine> csvdataLine() {
        return input -> {
            final Function<List<String>, CsvLine> mapFn = (elem) -> {
                return new CsvLine(CsvLineTypeEnum.INSERT, Map.of("a", "n"));
            };

            final Function<String, ParserFunc<CsvLine>> andThenFn = xml -> map(
                    zeroOrMore(pairLeft(matchLiteral(";"), identifier())),
                    mapFn
            );

            final ParserFunc<CsvLine> parser = and_then(
                    matchLiteral("INSERT_UPDATE"),
                    andThenFn
            );

            return parser.parse(input);
        };
    }

    public static ParserFunc<CsvLine> csvComment() {
        return input -> {

            final ParserFunc<String> startComment = matchLiteral("#");
            final ParserFunc<String> eol = matchLiteral("\n");

            final var parser = whitespaceWrap(right(startComment, left(anyChar(), eol)));

            final Function<Character, CsvLine> mapFn = (s) -> new CsvLine(CsvLineTypeEnum.COMMENT, Map.of("a", "b"));

            return map(parser, mapFn).parse(input);
        };
    }

    public static ParserFunc<CsvLine> csv() {
        return input -> whitespaceWrap(either(csvComment(), csvData())).parse(input);
    }

    private static ParserFunc<CsvLine> csvData() {
        return input -> {
            final ParserFunc<CsvLine> parser = null;
            return parser.parse(input);
        };
    }

    public static ParserFunc<String> comment() {
        return input -> {
            final Function<List<Character>, String> mapFn = l -> l.stream().map(String::valueOf).collect(Collectors.joining());

            return map(
                    right(
                            right(space0(), matchLiteral("#")),
                            left(
                                    zeroOrMore(pred(anyChar(), c -> !(Objects.equals('\n', c) || Objects.equals('\r', c)))),
                                    either(matchLiteral("\n"), matchLiteral("\r"))
                            )
                    ),
                    mapFn
            ).parse(input);
        };
    }

}
