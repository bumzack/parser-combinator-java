package at.grg.bumzack;


import org.junit.jupiter.api.Test;

import static at.grg.bumzack.CsvParser.comment;
import static at.grg.bumzack.CsvParser.csvdataLine;
import static org.assertj.core.api.Assertions.assertThat;

public class CsvParserTest {

    @Test
    void testComment_ok() {

        final var c = comment();

        final var input = "#   hallo du\n ";
        final var expected = "   hallo du";

        System.out.println("input       " + input);
        System.out.println("expected    " + expected);

        final var result = c.parse(input);

        //  final var expected = new CsvLine(CsvLineTypeEnum.COMMENT, "   hallo    ");

        assertThat(result.getOutput()).isEqualTo(expected);
        assertThat(result.getInput()).isEqualTo(" ");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testComment_ok2() {

        final var c = comment();

        final var input = "#   hallo du\n  wer ist das  ";
        final var expected = "   hallo du";

        System.out.println("input       " + input);
        System.out.println("expected    " + expected);

        final var result = c.parse(input);

        //  final var expected = new CsvLine(CsvLineTypeEnum.COMMENT, "   hallo    ");

        assertThat(result.getOutput()).isEqualTo(expected);
        assertThat(result.getInput()).isEqualTo("  wer ist das  ");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testComment_ok3() {

        final var c = comment();

        final var input = "      #   hallo du\n  wer ist das  ";
        final var expected = "   hallo du";

        System.out.println("input       " + input);
        System.out.println("expected    " + expected);

        final var result = c.parse(input);

        //  final var expected = new CsvLine(CsvLineTypeEnum.COMMENT, "   hallo    ");

        assertThat(result.getOutput()).isEqualTo(expected);
        assertThat(result.getInput()).isEqualTo("  wer ist das  ");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testComment_ok4() {

        final var c = comment();

        final var input = "      #   hallo du\r  wer ist das  ";
        final var expected = "   hallo du";

        System.out.println("input       " + input);
        System.out.println("expected    " + expected);

        final var result = c.parse(input);

        //  final var expected = new CsvLine(CsvLineTypeEnum.COMMENT, "   hallo    ");

        assertThat(result.getOutput()).isEqualTo(expected);
        assertThat(result.getInput()).isEqualTo("  wer ist das  ");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testComment_ok5() {

        final var c = comment();

        final var input = "      #   hallo du\r  #   wer ist das  \n yoo ";
        final var expected = "   hallo du";

        System.out.println("input       " + input);
        System.out.println("expected    " + expected);

        final var result = c.parse(input);

        //  final var expected = new CsvLine(CsvLineTypeEnum.COMMENT, "   hallo    ");

        assertThat(result.getOutput()).isEqualTo(expected);
        assertThat(result.getInput()).isEqualTo("  #   wer ist das  \n yoo ");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);

        final var expected2 = "   wer ist das  ";
        final var result2 = c.parse(result.getInput());
        assertThat(result2.getOutput()).isEqualTo(expected2);
        assertThat(result2.getInput()).isEqualTo(" yoo ");
        assertThat(result2.getErrorMsg()).isEqualTo(null);
        assertThat(result2.getStatus()).isEqualTo(ParserStatus.OK);
    }

    @Test
    void testCsvdataLine_ok() {

        final var c = csvdataLine();

        final var input = "INSERT_UPDATE;halllo;123;georg ";
        final var expected = "   hallo du";

        System.out.println("input       " + input);
        System.out.println("expected    " + expected);

        final var result = c.parse(input);

        System.out.println("csvline " + result.getOutput());
         final var expected = new CsvLine(CsvLineTypeEnum.INSERT, Map.of);

        assertThat(result.getOutput()).isEqualTo(expected);
        assertThat(result.getInput()).isEqualTo("  wer ist das  ");
        assertThat(result.getErrorMsg()).isEqualTo(null);
        assertThat(result.getStatus()).isEqualTo(ParserStatus.OK);
    }
}