package at.grg.bumzack;

import org.apache.commons.lang3.StringUtils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isAlphanumeric;

public class Parser<OUTPUT, OUTPUT2, OUTPUT3> {

    public ParserFunc letterA() {
        return input -> {
            if (Objects.equals(input.substring(0, 1), "a")) {
                return new Result<OUTPUT>(input.substring(1), null, ParserStatus.OK, null);
            }
            return new Result<OUTPUT>(null, null, ParserStatus.Error, input);
        };
    }

    public ParserFunc<OUTPUT> matchLiteral(final String expected) {
        return input -> {
            if (Objects.equals(input.substring(0, expected.length()), expected)) {
                return new Result<OUTPUT>(input.substring(expected.length()), (OUTPUT) expected, ParserStatus.OK, null);
            }
            return new Result<>(null, null, ParserStatus.Error, input);
        };
    }

    public ParserFunc<String> idenitifier() {
        return input -> {
            final StringBuilder matchedSB = new StringBuilder();

            final var chars = new StringCharacterIterator(input);

            if (chars.current() != CharacterIterator.DONE && StringUtils.isAlpha(String.valueOf(chars.current()))) {
                matchedSB.append(chars.current());
            } else {
                return new Result<>(input, null, ParserStatus.Error, input);
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

            return new Result<String>(input.substring(nextIdx), matched, ParserStatus.OK, null);
        };
    }

    public ParserFunc<OUTPUT> pairLeft(final ParserFunc<OUTPUT> p1,
                                       final ParserFunc<OUTPUT2> p2) {
        return input -> {
            final var res1 = p1.parse(input);
            if (res1.getError().equals(ParserStatus.Error)) {
                return new Result<>(res1.getInput(), null, ParserStatus.Error, input);
            }
            final Result<OUTPUT2> res2 = p2.parse(res1.getInput());
            if (res2.getError().equals(ParserStatus.Error)) {
                return new Result<>(res1.getInput(), null, ParserStatus.Error, res1.getInput());
            }
            return new Result<>(res2.getInput(), res1.getOutput(), ParserStatus.OK, null);
        };
    }

    public ParserFunc<OUTPUT2> pairRight(final ParserFunc<OUTPUT> p1,
                                         final ParserFunc<OUTPUT2> p2) {
        return input -> {
            final var res1 = p1.parse(input);
            if (res1.getError().equals(ParserStatus.Error)) {
                return new Result<>(res1.getInput(), null, ParserStatus.Error, input);
            }
            final var res2 = p2.parse(res1.getInput());
            if (res2.getError().equals(ParserStatus.Error)) {
                return new Result<>(res1.getInput(), null, ParserStatus.Error, res1.getInput());
            }
            return new Result<>(res2.getInput(), res2.getOutput(), ParserStatus.OK, null);
        };
    }

    public ParserFunc<OUTPUT2> map(final ParserFunc<OUTPUT> parser,
                                   final Function<OUTPUT, OUTPUT2> mapFn) {
        return input -> {
            final var res1 = parser.parse(input);
            if (res1.getError().equals(ParserStatus.Error)) {
                return new Result<>(input, null, ParserStatus.Error, input);
            }
            return new Result<>(res1.getInput(),
                    mapFn.apply(res1.getOutput()),
                    ParserStatus.OK,
                    null);
        };
    }

    public ParserFunc<List<OUTPUT>> oneOrMore(final ParserFunc<OUTPUT> parser) {
        return inp -> {
            final var result = new ArrayList<OUTPUT>();

            var input = inp;

            if (StringUtils.isEmpty(input)) {
                return new Result<>(input, null, ParserStatus.Error, input);
            }

            final var res1 = parser.parse(input);
            if (res1.getError().equals(ParserStatus.Error)) {
                return new Result<>(input, null, ParserStatus.Error, input);
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

    public ParserFunc<List<OUTPUT>> zeroOrMore(final ParserFunc<OUTPUT> parser) {
        return inp -> {
            final var result = new ArrayList<OUTPUT>();

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

    public ParserFunc<OUTPUT2> right(final ParserFunc<OUTPUT> p1,
                                     final ParserFunc<OUTPUT2> p2) {
        return input -> pairRight(p1, p2).parse(input);
    }

    public ParserFunc<OUTPUT> left(final ParserFunc<OUTPUT> p1,
                                   final ParserFunc<OUTPUT2> p2) {
        return input -> pairLeft(p1, p2).parse(input);
    }
}
