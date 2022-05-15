package at.grg.bumzack;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isAlphanumeric;

public class Parser {

    public <A> ParserFunc letterA() {
        return input -> {
            if (Objects.equals(input.substring(0, 1), "a")) {
                return new Result<A>(input.substring(1), null, ParserStatus.OK, null);
            }
            return new Result<A>(null, null, ParserStatus.Error, input);
        };
    }

    public ParserFunc<String> matchLiteral(final String expected) {
        return input -> {
            if (Objects.equals(input.substring(0, expected.length()), expected)) {
                return new Result<>(input.substring(expected.length()), expected, ParserStatus.OK, null);
            }
            return new Result<>(null, null, ParserStatus.Error, input);
        };
    }

    public ParserFunc<String> identifier() {
        return input -> {
            final StringBuilder matchedSB = new StringBuilder();

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

    public <A, B> ParserFunc<A> pairLeft(final ParserFunc<A> p1,
                                         final ParserFunc<B> p2) {
        return input -> {
            final var res1 = p1.parse(input);
            if (res1.getError().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            final Result<B> res2 = p2.parse(res1.getInput());
            if (res2.getError().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, res1.getInput());
            }
            return new Result<>(res2.getInput(), res1.getOutput(), ParserStatus.OK, null);
        };
    }

    public <A, B> ParserFunc<B> pairRight(final ParserFunc<A> p1,
                                          final ParserFunc<B> p2) {
        return input -> {
            final var res1 = p1.parse(input);
            if (res1.getError().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            final var res2 = p2.parse(res1.getInput());
            if (res2.getError().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, res1.getInput());
            }
            return new Result<>(res2.getInput(), res2.getOutput(), ParserStatus.OK, null);
        };
    }

    public <A, B> ParserFunc<Pair<A, B>> pair(final ParserFunc<A> p1,
                                              final ParserFunc<B> p2) {
        return input -> {
            final var res1 = p1.parse(input);
            if (res1.getError().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            final var res2 = p2.parse(res1.getInput());
            if (res2.getError().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, res1.getInput());
            }
            return new Result<>(res2.getInput(), Pair.of(res1.getOutput(), res2.getOutput()), ParserStatus.OK, null);
        };
    }


    public <A, B> ParserFunc<B> map(final ParserFunc<A> parser,
                                    final Function<A, B> mapFn) {
        return input -> {
            final var res1 = parser.parse(input);
            if (res1.getError().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            return new Result<>(res1.getInput(),
                    mapFn.apply(res1.getOutput()),
                    ParserStatus.OK,
                    null);
        };
    }

    public <A, B> ParserFunc<B> right(final ParserFunc<A> p1,
                                      final ParserFunc<B> p2) {
        return input -> pairRight(p1, p2).parse(input);
    }

    public <A, B> ParserFunc<A> left(final ParserFunc<A> p1,
                                     final ParserFunc<B> p2) {
        return input -> pairLeft(p1, p2).parse(input);
    }

    public <AAA> ParserFunc<List<AAA>> oneOrMore(final ParserFunc<AAA> parser) {
        return inp -> {
            final var result = new ArrayList<AAA>();

            var input = inp;

            if (StringUtils.isEmpty(input)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }

            final var res1 = parser.parse(input);
            if (res1.getError().equals(ParserStatus.Error)) {
                return new Result<>(null, null, ParserStatus.Error, input);
            }
            result.add(res1.getOutput());
            input = res1.getInput();

            var res = parser.parse(input);

            while (res.getError().equals(ParserStatus.OK)) {
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
                    res.setError(ParserStatus.Error);
                }
            }
            return new Result<>(input, result, ParserStatus.OK, null);
        };
    }

    public <AAA> ParserFunc<List<AAA>> zeroOrMore(final ParserFunc<AAA> parser) {
        return inp -> {
            final var result = new ArrayList<AAA>();

            var input = inp;

            if (StringUtils.isEmpty(input)) {
                return new Result<>(input, result, ParserStatus.OK, null);
            }

            var res = parser.parse(input);

            while (res.getError().equals(ParserStatus.OK)) {
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
                    res.setError(ParserStatus.Error);
                }
            }
            return new Result<>(input, result, ParserStatus.OK, null);
        };
    }


    public ParserFunc<Character> anyChar() {
        return input -> {
            final var chars = new StringCharacterIterator(input);
            if (chars.current() != CharacterIterator.DONE) {
                return new Result<>(StringUtils.substring(input, 1), chars.current(), ParserStatus.OK, input);
            }
            return new Result<>(null, null, ParserStatus.Error, input);
        };
    }

    public <AAA> ParserFunc<AAA> pred(final ParserFunc<AAA> p1,
                                      final Predicate<AAA> pred) {
        return input -> {
            final var res = p1.parse(input);

            if (pred.test(res.getOutput())) {
                return new Result<>(res.getInput(), res.getOutput(), ParserStatus.OK, input);
            }
            return new Result<>(null, null, ParserStatus.Error, input);
        };
    }


    public ParserFunc<Character> whiteSpace() {
        return input -> {
            final ParserFunc<Character> p = pred(anyChar(), c -> StringUtils.isWhitespace(String.valueOf(c)));
            return p.parse(input);
        };
    }

    public ParserFunc<List<Character>> space0() {
        return input -> zeroOrMore(whiteSpace()).parse(input);
    }

    public ParserFunc<List<Character>> space1() {
        return input -> oneOrMore(whiteSpace()).parse(input);
    }

    public ParserFunc<String> quotedString() {
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

    public ParserFunc<Pair<String, String>> attributePair() {
        return input -> pair(
                identifier(),
                right(
                        matchLiteral("="),
                        quotedString()
                )
        ).parse(input);
    }

    public ParserFunc<List<Pair<String, String>>> attributes() {
        return input -> zeroOrMore(
                right(
                        space1(),
                        attributePair()
                )
        ).parse(input);
    }
}
