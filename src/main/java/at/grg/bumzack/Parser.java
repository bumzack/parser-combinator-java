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

import static org.apache.commons.lang3.StringUtils.isAlphanumeric;

public class Parser {

    public static ParserFunc<String> letterA() {
        return input -> {
            if (Objects.equals(input.substring(0, 1), "a")) {
                return new Result<>(input.substring(1), null, ParserStatus.OK, null);
            }
            return new Result<>(null, null, ParserStatus.Error, input);
        };
    }

    public static ParserFunc<String> matchLiteral(final String expected) {
        return input -> {
            if (Objects.equals(input.substring(0, expected.length()), expected)) {
                return new Result<>(input.substring(expected.length()), expected, ParserStatus.OK, null);
            }
            return new Result<>(null, null, ParserStatus.Error, input);
        };
    }

    public static ParserFunc<String> identifier() {
        return input -> {
            final var matchedSB = new StringBuilder();

            final var chars = new StringCharacterIterator(input);

            if (chars.current() != CharacterIterator.DONE && StringUtils.isAlpha(String.valueOf(chars.current()))) {
                matchedSB.append(chars.current());
            } else {
                return new Result<>(null, null, ParserStatus.Error, input);
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

            return new Result<>(StringUtils.substring(input, nextIdx), matched, ParserStatus.OK, null);
        };
    }

    public static <A, B> ParserFunc<A> pairLeft(final ParserFunc<A> p1,
                                                final ParserFunc<B> p2) {
        return input -> {
            final var res1 = p1.parse(input);
            if (res1.getStatus().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            final var res2 = p2.parse(res1.getInput());
            if (res2.getStatus().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, res1.getInput());
            }
            return new Result<>(res2.getInput(), res1.getOutput(), ParserStatus.OK, null);
        };
    }

    public static <A, B> ParserFunc<B> pairRight(final ParserFunc<A> p1,
                                                 final ParserFunc<B> p2) {
        return input -> {
            final var res1 = p1.parse(input);
            if (res1.getStatus().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            final var res2 = p2.parse(res1.getInput());
            if (res2.getStatus().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, res1.getInput());
            }
            return new Result<>(res2.getInput(), res2.getOutput(), ParserStatus.OK, null);
        };
    }

    public static <A, B> ParserFunc<Pair<A, B>> pair(final ParserFunc<A> p1,
                                                     final ParserFunc<B> p2) {
        return input -> {
            final var res1 = p1.parse(input);
            if (res1.getStatus().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            final var res2 = p2.parse(res1.getInput());
            if (res2.getStatus().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, res1.getInput());
            }
            return new Result<>(res2.getInput(), Pair.of(res1.getOutput(), res2.getOutput()), ParserStatus.OK, null);
        };
    }


    public static <A, B> ParserFunc<B> map(final ParserFunc<A> parser,
                                           final Function<A, B> mapFn) {
        return input -> {
            final var res1 = parser.parse(input);
            if (res1.getStatus().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            return new Result<>(res1.getInput(),
                    mapFn.apply(res1.getOutput()),
                    ParserStatus.OK,
                    null);
        };
    }

    public static <A, B, T> ParserFunc<B> mapBiFunc(final ParserFunc<A> parser,
                                                    final BiFunction<T, A, B> mapBiFn,
                                                    final T param) {
        return input -> {
            final var res1 = parser.parse(input);
            if (res1.getStatus().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            return new Result<>(res1.getInput(),
                    mapBiFn.apply(param, res1.getOutput()),
                    ParserStatus.OK,
                    null);
        };
    }

    public static <A, B> ParserFunc<B> right(final ParserFunc<A> p1,
                                             final ParserFunc<B> p2) {
        return input -> pairRight(p1, p2).parse(input);
    }

    public static <A, B> ParserFunc<A> left(final ParserFunc<A> p1,
                                            final ParserFunc<B> p2) {
        return input -> pairLeft(p1, p2).parse(input);
    }

    public static <A> ParserFunc<List<A>> oneOrMore(final ParserFunc<A> parser) {
        return inp -> {
            final var result = new ArrayList<A>();

            var input = inp;

            if (StringUtils.isEmpty(input)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }

            final var res1 = parser.parse(input);
            if (res1.getStatus().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            result.add(res1.getOutput());
            input = res1.getInput();

            var res = parser.parse(input);

            while (res.getStatus().equals(ParserStatus.OK)) {
                result.add(res.getOutput());
                input = res.getInput();
                final var size = Optional.ofNullable(res.getInput())
                        .map(String::length)
                        .orElse(-1);

                if (size > 0) {
                    res = parser.parse(input);
                } else {
                    // abort loop
                    res.setStatus(ParserStatus.Error);
                }
            }
            return new Result<>(input, result, ParserStatus.OK, null);
        };
    }

    public static <A> ParserFunc<List<A>> zeroOrMore(final ParserFunc<A> parser) {
        return inp -> {
            final var result = new ArrayList<A>();

            var input = inp;

            if (StringUtils.isEmpty(input)) {
                return new Result<>(input, result, ParserStatus.OK, null);
            }

            var res = parser.parse(input);

            while (res.getStatus().equals(ParserStatus.OK)) {
                result.add(res.getOutput());
                input = res.getInput();
                final var size = Optional.ofNullable(res.getInput())
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(String::length)
                        .orElse(-1);

                if (size > 0) {
                    res = parser.parse(input);
                } else {
                    // abort loop
                    res.setStatus(ParserStatus.Error);
                }
            }
            return new Result<>(input, result, ParserStatus.OK, null);
        };
    }


    public static ParserFunc<Character> anyChar() {
        return input -> {
            final var chars = new StringCharacterIterator(input);
            if (chars.current() != CharacterIterator.DONE) {
                return new Result<>(StringUtils.substring(input, 1), chars.current(), ParserStatus.OK, input);
            }
            return new Result<>(null, null, ParserStatus.Error, input);
        };
    }

    public static <A> ParserFunc<A> pred(final ParserFunc<A> p1,
                                         final Predicate<A> pred) {
        return input -> {
            final var res = p1.parse(input);

            if (pred.test(res.getOutput())) {
                return new Result<>(res.getInput(), res.getOutput(), ParserStatus.OK, input);
            }
            return new Result<>(null, null, ParserStatus.Error, input);
        };
    }


    public static ParserFunc<Character> whiteSpace() {
        return input -> {
            final ParserFunc<Character> p = pred(anyChar(), c -> StringUtils.isWhitespace(String.valueOf(c)));
            return p.parse(input);
        };
    }

    public static ParserFunc<List<Character>> space0() {
        return input -> zeroOrMore(whiteSpace()).parse(input);
    }

    public static ParserFunc<List<Character>> space1() {
        return input -> oneOrMore(whiteSpace()).parse(input);
    }

    public static ParserFunc<String> quotedString() {
        return input -> {
            final Function<List<Character>, String> mapFn = l -> l.stream().map(String::valueOf).collect(Collectors.joining());

            return map(
                    right(
                            matchLiteral("\""),
                            left(
                                    zeroOrMore(pred(anyChar(), c -> !Objects.equals('\"', c))),
                                    matchLiteral("\"")
                            )
                    ),
                    mapFn
            ).parse(input);
        };
    }

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

    public static <A> ParserFunc<A> either(final ParserFunc<A> p1,
                                           final ParserFunc<A> p2) {
        return input -> {
            final var res1 = p1.parse(input);
            if (res1.getStatus().equals(ParserStatus.OK)) {
                return res1;
            }
            return p2.parse(input);
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


    public static <A, B> ParserFunc<B> and_then(final ParserFunc<A> parser,
                                                final Function<A, ParserFunc<B>> fun) {
        return input -> {
            final var res = parser.parse(input);
            if (res.getStatus().equals(ParserStatus.OK)) {
                return fun.apply(res.getOutput()).parse(res.getInput());
            }
            return new Result<>(null, null, ParserStatus.Error, input);
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

    public static <A> ParserFunc<A> whitespaceWrap(final ParserFunc<A> parser) {
        return input -> right(space0(), left(parser, space0())).parse(input);
    }

    public static ParserFunc<XmlElement> xmlElement() {
        return input -> whitespaceWrap(either(xmlSingleElement(), xmlParentElement())).parse(input);
    }
}
