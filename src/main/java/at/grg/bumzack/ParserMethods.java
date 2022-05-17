package at.grg.bumzack;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isAlphanumeric;

public class ParserMethods {

    public static Result<ParseResult<String, String>, Error> letterA(final String str) {
        if (Objects.equals(str.substring(0, 1), "a")) {
            return new Result<>(ParseResult.of(str.substring(1), null, ParserStatus.OK), null);
        }
        return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of(str));
    }


    public static Function<String, Result<ParseResult<String, String>, Error>> matchLiteral(final String expected) {
        return (e) -> {
            if (Objects.equals(e.substring(0, expected.length()), expected)) {
                return new Result<>(ParseResult.of(e.substring(expected.length()), expected, ParserStatus.OK), null);
            }
            return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of(e));
        };
    }

    public static Result<ParseResult<String, String>, Error> identifier(final String input) {
        final var matchedSB = new StringBuilder();

        final var chars = new StringCharacterIterator(input);

        if (chars.current() != CharacterIterator.DONE && StringUtils.isAlpha(String.valueOf(chars.current()))) {
            matchedSB.append(chars.current());
        } else {
            return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of(input));
        }

        while (chars.next() != CharacterIterator.DONE) {
            final var c = String.valueOf(chars.current());
            if (isAlphanumeric(c) || StringUtils.equals("-", c)) {
                matchedSB.append(c);
            } else {
                break;
            }
        }
        final var matched = matchedSB.toString();
        final var nextIdx = matched.length();

        return new Result<>(ParseResult.of(StringUtils.substring(input, nextIdx), matched, ParserStatus.OK), null);
    }

    //
    public static <A, B, T> Function<T, Result<ParseResult<A, T>, Error>> pairLeft(final Function<T, Result<ParseResult<A, T>, Error>> p1,
                                                                                   final Function<T, Result<ParseResult<B, T>, Error>> p2) {
        return input -> {
            final var res1 = p1.apply(input);
            if (res1.get().getStatus().equals(ParserStatus.Error)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) input));
            }
            final var res2 = p2.apply(res1.get().getInput());
            if (res2.get().getStatus().equals(ParserStatus.Error)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) res1.get().getInput()));
            }
            return new Result<>(ParseResult.of(res2.get().getInput(), res1.get().getOutput(), ParserStatus.OK), null);
        };
    }

    public static <A, B, T> Function<T, Result<ParseResult<B, T>, Error>> pairRight(final Function<T, Result<ParseResult<A, T>, Error>> p1,
                                                                                    final Function<T, Result<ParseResult<B, T>, Error>> p2) {
        return input -> {
            final var res1 = p1.apply(input);
            if (res1.get().getStatus().equals(ParserStatus.Error)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) input));
            }
            final var res2 = p2.apply(res1.get().getInput());
            if (res2.get().getStatus().equals(ParserStatus.Error)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) res1.get().getInput()));
            }
            return new Result<>(ParseResult.of(res2.get().getInput(), res2.get().getOutput(), ParserStatus.OK), null);
        };
    }

    public static <A, B, T> Function<T, Result<ParseResult<Pair<A, B>, T>, Error>> pair(final Function<T, Result<ParseResult<A, T>, Error>> p1,
                                                                                        final Function<T, Result<ParseResult<B, T>, Error>> p2) {
        return input -> {
            final var res1 = p1.apply(input);
            if (res1.get().getStatus().equals(ParserStatus.Error)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) input));
            }
            final var res2 = p2.apply(res1.get().getInput());
            if (res2.get().getStatus().equals(ParserStatus.Error)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) res1.get().getInput()));
            }
            return new Result<>(ParseResult.of(res2.get().getInput(), Pair.of(res1.get().getOutput(), res2.get().getOutput()), ParserStatus.OK), null);
        };
    }


    public static <A, B, T> Function<T, Result<ParseResult<B, T>, Error>> map(final Function<T, Result<ParseResult<A, T>, Error>> parser,
                                                                              final Function<A, B> mapFn) {
        return input -> {
            final var res1 = parser.apply(input);
            if (res1.get().getStatus().equals(ParserStatus.Error)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) input));
            }
            return new Result<>(ParseResult.of(res1.get().getInput(), mapFn.apply(res1.get().getOutput()), ParserStatus.OK), null);
        };
    }

    public static <A, B, T> Function<T, Result<ParseResult<B, T>, Error>> mapBiFunc(final Function<T, Result<ParseResult<A, T>, Error>> parser,
                                                                                    final BiFunction<T, A, B> mapBiFn,
                                                                                    final T param) {
        return input -> {
            final var res1 = parser.apply(input);
            if (res1.get().getStatus().equals(ParserStatus.Error)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) input));
            }
            return new Result<>(ParseResult.of(res1.get().getInput(),
                    mapBiFn.apply(param, res1.get().getOutput()),
                    ParserStatus.OK),
                    null);
        };
    }

    public static <A, B, T> Function<T, Result<ParseResult<B, T>, Error>> right(final Function<T, Result<ParseResult<A, T>, Error>> p1,
                                                                                final Function<T, Result<ParseResult<B, T>, Error>> p2) {
        return input -> pairRight(p1, p2).apply(input);
    }

    public static <A, B, T> Function<T, Result<ParseResult<A, T>, Error>> left(final Function<T, Result<ParseResult<A, T>, Error>> p1,
                                                                               final Function<T, Result<ParseResult<B, T>, Error>> p2) {
        return input -> pairLeft(p1, p2).apply(input);
    }

    public static <A, T> Function<T, Result<ParseResult<List<A>, T>, Error>> oneOrMore(final Function<T, Result<ParseResult<A, T>, Error>> parser) {
        return inp -> {
            final var result = new ArrayList<A>();

            var input = inp;

            if (nonNull(input)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) input));

            }

            final var res1 = parser.apply(input);
            if (res1.get().getStatus().equals(ParserStatus.Error)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) input));

            }
            result.add(res1.get().getOutput());
            input = res1.get().getInput();

            var res = parser.apply(input);

            while (res.get().getStatus().equals(ParserStatus.OK)) {
                result.add(res.get().getOutput());
                input = res.get().getInput();

                final var size = Optional.ofNullable(res.get().getInput())
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(String::length)
                        .orElse(-1);

                if (size > 0) {
                    res = parser.apply(input);
                } else {
                    // abort loop
                    res.get().setStatus(ParserStatus.Error);
                }
            }
            return new Result<>(ParseResult.of(input,
                    result,
                    ParserStatus.OK),
                    null);
        };
    }

    public static <A, T> Function<T, Result<ParseResult<List<A>, T>, Error>> zeroOrMore(final Function<T, Result<ParseResult<A, T>, Error>> parser) {
        return inp -> {
            final var result = new ArrayList<A>();

            var input = inp;

            if (nonNull(input)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) input));
            }

            var res = parser.apply(input);

            while (res.get().getStatus().equals(ParserStatus.OK)) {
                result.add(res.get().getOutput());
                input = res.get().getInput();
                final var size = Optional.ofNullable(res.get().getInput())
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(String::length)
                        .orElse(-1);

                if (size > 0) {
                    res = parser.apply(input);
                } else {
                    // abort loop
                    res.get().setStatus(ParserStatus.Error);
                }
            }
            return new Result<>(ParseResult.of(input,
                    result,
                    ParserStatus.OK),
                    null);
        };
    }


    public static Result<ParseResult<Character, String>, Error> anyChar(final String input) {
        final var chars = new StringCharacterIterator(input);
        if (chars.current() != CharacterIterator.DONE) {
            return new Result<ParseResult<Character, String>, Error>(ParseResult.of(StringUtils.substring(input, 1), chars.current(), ParserStatus.OK), null);
        }
        return new Result<>(ParseResult.of(null, null, ParserStatus.Error), null);
    }

    public static <A, T> Function<String, Result<ParseResult<A, T>, Error>> pred(final Function<String, Result<ParseResult<A, T>, Error>> p1,
                                                                                 final Predicate<A> pred) {
        return input -> {
            final var res = p1.apply(input);
            if (pred.test(res.get().getOutput())) {
                return new Result<>(ParseResult.of(res.get().getInput(), res.get().getOutput(), ParserStatus.OK), null);
            }
            return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of(input));
        };
    }

    public static Function<String, Result<ParseResult<Character, String>, Error>> whiteSpace() {
        return input -> {
            final var p = pred(ParserMethods::anyChar, c -> StringUtils.isWhitespace(String.valueOf(c)));
            return p.apply(input);
        };
    }

    public static Function<String, Result<ParseResult<List<Character>, String>, Error>> space0() {
        return input -> zeroOrMore(whiteSpace()).apply(input);
    }

    public static Function<String, Result<ParseResult<List<Character>, String>, Error>> space1() {
        return input -> oneOrMore(whiteSpace()).apply(input);
    }

    public static <A, T> Function<T, Result<ParseResult<A, T>, Error>> either(final Function<T, Result<ParseResult<A, T>, Error>> p1,
                                                                              final Function<T, Result<ParseResult<A, T>, Error>> p2) {
        return input -> {
            final var res1 = p1.apply(input);
            if (res1.get().getStatus().equals(ParserStatus.OK)) {
                return res1;
            }
            return p2.apply(input);
        };
    }

    public static Function<String, Result<ParseResult<String, String>, Error>> quotedString() {
        return input -> {
            final Function<List<Character>, String> mapFn = l -> l.stream().map(String::valueOf).collect(Collectors.joining());

            return map(
                    right(
                            matchLiteral("\""),
                            left(
                                    zeroOrMore(pred(ParserMethods::anyChar, c -> !Objects.equals('\"', c))),
                                    matchLiteral("\"")
                            )
                    ),
                    mapFn
            ).apply(input);
        };
    }

    public static Function<String, Result<ParseResult<Pair<String, String>, String>, Error>> attributePair() {
        return input -> pair(
                ParserMethods::identifier,
                right(
                        matchLiteral("="),
                        quotedString()
                )
        ).apply(input);
    }

    public static Function<String, Result<ParseResult<List<Pair<String, String>>, String>, Error>> attributes() {
        return input -> zeroOrMore(
                right(
                        space1(),
                        attributePair()
                )
        ).apply(input);
    }

    public static Function<String, Result<ParseResult<Pair<String, List<Pair<String, String>>>, String>, Error>> xmlElementStart() {
        return input -> right(
                matchLiteral("<"),
                pair(
                        ParserMethods::identifier,
                        attributes()
                )
        ).apply(input);
    }

    public static Function<String, Result<ParseResult<XmlElement, String>, Error>> xmlSingleElement() {
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
            ).apply(input);
        };
    }

    public static Function<String, Result<ParseResult<XmlElement, String>, Error>> xmlOpenElement() {
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
            ).apply(input);
        };
    }

    public static Function<String, Result<ParseResult<String, String>, Error>> xmlCloseElement(final String expectedName) {
        return input -> {
            final Predicate<String> pred = p -> StringUtils.equals(p, expectedName);

            return pred(
                    right(
                            matchLiteral("</"),
                            left(
                                    ParserMethods::identifier,
                                    matchLiteral(">")

                            )),
                    pred
            ).apply(input);
        };
    }

    public static <A, B, T> Function<T, Result<ParseResult<B, T>, Error>> and_then(final Function<T, Result<ParseResult<A, T>, Error>> parser,
                                                                                   final Function<A, Function<T, Result<ParseResult<B, T>, Error>>> fun) {
        return input -> {
            final var res = parser.apply(input);
            if (res.get().getStatus().equals(ParserStatus.OK)) {
                return fun.apply(res.get().getOutput()).apply(res.get().getInput());
            }
            return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of((String) input));
        };
    }

    public static <A, B, T> Function<T, Result<ParseResult<B, T>, Error>> mapBiFunccccc(final Function<T, Result<ParseResult<A, T>, Error>> parser,
                                                                                        final BiFunction<T, A, B> mapBiFn,
                                                                                        final T param) {
        return input -> {
            final var res1 = parser.apply(input);
            if (res1.get().getStatus().equals(ParserStatus.Error)) {
                return new Result<>(ParseResult.of(null, null, ParserStatus.Error), Error.of(input.toString()));
            }
            return new Result<>(ParseResult.of(res1.get().getInput(),
                    mapBiFn.apply(param, res1.get().getOutput()),
                    ParserStatus.OK),
                    null);
        };
    }

    public static <T extends Object> Function<T, Result<ParseResult<XmlElement, T>, Error>> xmlParentElement() {
        return input -> {
            final BiFunction<XmlElement, List<XmlElement>, XmlElement> mapFn1 = (elem, l) -> {
                elem.setChildren(l);
                return elem;
            };

            Function<XmlElement, Function<String, Result<ParseResult<List<XmlElement>, String>, Error>>> fn = (x) -> {
                return left(
                        zeroOrMore(xmlElement()),
                        xmlCloseElement(x.getName())
                );
            };

            final List<XmlElement> l = new ArrayList<>();
            final XmlElement xml1 = new XmlElement();
            Function<String, Result<ParseResult<List<XmlElement>, String>, Error>> apply = fn.apply(xml1);

            final Function<XmlElement, Result<ParseResult<List<XmlElement>, XmlElement>, Error>> andThenFn = xml -> mapBiFunccccc(
                    getParser(xml),
                    mapFn1,
                    xml
            );

            final Function<XmlElement, Result<ParseResult<List<XmlElement>>, Error>> objectParserFunc = and_then(
                    xmlOpenElement(),
                    andThenFn
            );

            return objectParserFunc.apply(input);
        };
    }

    private static Function<String, Result<ParseResult<List<XmlElement>, String>, Error>> getParser(final XmlElement xml) {
        return left(
                zeroOrMore(xmlElement()),
                xmlCloseElement(xml.getName())
        );
    }


    // Function<String, Result<ParseResult<A>, Error>>
    private static <T> Function<String, Result<ParseResult<List<XmlElement>, String>, Error>> getLeft(final XmlElement xml) {
        return left(
                zeroOrMore(xmlElement()),
                xmlCloseElement(xml.getName())
        );
    }

    public static Function<String, Result<ParseResult<XmlElement, String>, Error>> whitespaceWrap(final Function<String, Result<ParseResult<XmlElement, String>, Error>> parser) {
        return input -> right(space0(), left(parser, space0())).apply(input);
    }

    public static Function<String, Result<ParseResult<XmlElement, String>, Error>> xmlElement() {
        return input -> whitespaceWrap(either(xmlSingleElement(), xmlParentElement())).apply(input);
    }
}
