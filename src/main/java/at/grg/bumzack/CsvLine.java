package at.grg.bumzack;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CsvLine {
    private CsvLineTypeEnum type;
    private Map<String, String> data;
    private String comment;

    public CsvLine(final CsvLineTypeEnum type, final Map<String, String> data) {
        this.type = type;
        this.data = data;
    }

    public CsvLine(final CsvLineTypeEnum type, final String comment) {
        this.type = type;
        this.comment = comment;
    }

    public CsvLineTypeEnum getType() {
        return type;
    }

    public void setType(CsvLineTypeEnum type) {
        this.type = type;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final var collect = Optional.ofNullable(data).stream()
                .flatMap(e -> e.entrySet().stream().distinct())
                .map(e -> StringUtils.join(" -> ", e.getKey(), e.getValue()))
                .collect(Collectors.joining(" || "));

        return "CsvLine{" +
                "type=" + type +
                ", comment='" + comment + "'" +
                ", data=" + collect +
                '}';
    }
}
